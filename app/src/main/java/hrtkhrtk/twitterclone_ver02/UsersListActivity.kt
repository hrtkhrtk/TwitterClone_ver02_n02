//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_users_list.*

class UsersListActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mUserArrayList: ArrayList<User>
    private lateinit var mAdapter: UsersListAdapter

    private val mEventListenerForUsersList = object : ChildEventListener { // 仮置き // TODO:
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val iconImageString = map["icon_image"] ?: ""
            val nickname = map["nickname"] ?: ""
            val idForSearch = map["id_for_search"] ?: ""
            val selfIntroduction = map["self_introduction"] ?: ""
            val userId = dataSnapshot.key!!

            val bytes =
                if (iconImageString.isNotEmpty()) {
                    Base64.decode(iconImageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val userClassInstance = User(bytes, nickname, idForSearch, selfIntroduction, userId)
            mUserArrayList.add(userClassInstance)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {} // TODO:

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {} // TODO:

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {}
    }


    private val mEventListenerForFollowingsFollowersListRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            mUserArrayList.clear()
            val list = dataSnapshot.value as ArrayList<String>? ?: ArrayList<String>()
            if (list.isEmpty()) {
                mAdapter.notifyDataSetChanged()
            }
            for (user_id in list) {
                mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val map = snapshot.value as Map<String, String>

                            val iconImageString = map["icon_image"] ?: ""
                            val nickname = map["nickname"] ?: ""
                            val idForSearch = map["id_for_search"] ?: ""
                            val selfIntroduction = map["self_introduction"] ?: ""
                            val userId = snapshot.key!!

                            val bytes =
                                if (iconImageString.isNotEmpty()) {
                                    Base64.decode(iconImageString, Base64.DEFAULT)
                                } else {
                                    byteArrayOf()
                                }

                            val userClassInstance = User(bytes, nickname, idForSearch, selfIntroduction, userId)
                            mUserArrayList.add(userClassInstance)
                            mAdapter.notifyDataSetChanged()
                        }
                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )
            }
        }
        override fun onCancelled(firebaseError: DatabaseError) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = UsersListAdapter(this)
        mUserArrayList = ArrayList<User>()
        mAdapter.notifyDataSetChanged()


        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@UsersListActivity, UserDetailActivity::class.java)

            val userId = mUserArrayList[position].userId
            mDatabaseReference.child("users").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot_n01: DataSnapshot) {
                        val data_n01 = snapshot_n01.value as Map<String, String>? // ここは空ではない
                        val backgroundImageString = data_n01!!["background_image"] as String
                        val iconImageString = data_n01["icon_image"] as String
                        val bytesForBackgroundImage =
                            if (backgroundImageString.isNotEmpty()) {
                                Base64.decode(backgroundImageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }
                        val bytesForIconImage =
                            if (iconImageString.isNotEmpty()) {
                                Base64.decode(iconImageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }
                        val nickname = data_n01["nickname"] as String
                        val id_for_search = data_n01["id_for_search"] as String
                        val self_introduction = data_n01["self_introduction"] as String
                        val data_n01_2 = snapshot_n01.value as Map<String, Long> // ここは空ではない
                        val created_at_Long = data_n01_2["created_at"]!! // ここは必ず存在
                        val followings_list = data_n01["followings_list"] as ArrayList<String>? ?: ArrayList<String>()
                        val followers_list = data_n01["followers_list"] as ArrayList<String>? ?: ArrayList<String>()

                        val userDetail = UserDetail(bytesForBackgroundImage, bytesForIconImage, nickname, id_for_search, self_introduction, created_at_Long, followings_list, followers_list, userId)


                        mDatabaseReference.child("posts").child(userId).addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot_n02: DataSnapshot) {
                                    val data_n02 = snapshot_n02.value as HashMap<String, String>? ?: HashMap<String, String>() // ここはnullかも

                                    val postArrayList = ArrayList<Post>()
                                    val postForShowingArrayList = ArrayList<PostForShowing>()
                                    for (post_id in data_n02.keys) {
                                        val post_element = data_n02[post_id] as Map<String, String>
                                        val post_text = post_element["text"]
                                        val post_element_2 = data_n02[post_id] as Map<String, Long>
                                        val post_created_at_Long = post_element_2["created_at"]!! // ここは必ず存在する
                                        val post_favoriters_list = post_element["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？

                                        val postClass = Post(bytesForIconImage, nickname, post_text!!, post_created_at_Long, post_favoriters_list, userId, post_id)
                                        postArrayList.add(postClass)
                                    }
                                    val sorted_postArrayList = postArrayList.sortedWith(compareBy({-it.createdAt})) // マイナスをつけて降順にする // 参考：https://android.benigumo.com/20180206/sortedwith-compareby/
                                    for ((index, element) in sorted_postArrayList.withIndex()) {
                                        val postForShowing = PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index)
                                        postForShowingArrayList.add(postForShowing)
                                    }

                                    intent.putExtra("userDetail", userDetail)
                                    intent.putExtra("postForShowingArrayList", postForShowingArrayList)
                                    startActivity(intent)
                                }

                                override fun onCancelled(firebaseError_n02: DatabaseError) {}
                            }
                        )
                    }

                    override fun onCancelled(firebaseError_n01: DatabaseError) {}
                }
            )
        }


        searchButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val searchText = searchWindow.text.toString()

            if (searchText.isEmpty()) {
                Snackbar.make(v, "入力して下さい", Snackbar.LENGTH_LONG).show()

                //これがいるのか不明
                // Userのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mUserArrayList.clear()
                mAdapter.setUserArrayList(mUserArrayList)
                mListView.adapter = mAdapter

                title = "user一覧"

                mDatabaseReference.child("users").addChildEventListener(mEventListenerForUsersList)
            } else {
                //これがいるのか不明
                // Userのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mUserArrayList.clear()
                mAdapter.setUserArrayList(mUserArrayList)
                mListView.adapter = mAdapter

                title = "検索結果"

                mDatabaseReference.child("id_for_search_list").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as HashMap<String, String>? ?: HashMap<String, String>() // ここは存在するとみなしていいと思うが安全のため

                            if (data.keys.contains(searchText)) { // 含まれていれば
                                val data_02 = data[searchText] as Map<String, String>
                                val user_id = data_02["user_id"]!!

                                mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val map = snapshot.value as Map<String, String>

                                            val iconImageString = map["icon_image"] ?: ""
                                            val nickname = map["nickname"] ?: ""
                                            val idForSearch = map["id_for_search"] ?: ""
                                            val selfIntroduction = map["self_introduction"] ?: ""
                                            val userId = snapshot.key!!

                                            val bytes =
                                                if (iconImageString.isNotEmpty()) {
                                                    Base64.decode(iconImageString, Base64.DEFAULT)
                                                } else {
                                                    byteArrayOf()
                                                }

                                            val userClassInstance = User(bytes, nickname, idForSearch, selfIntroduction, userId)
                                            mUserArrayList.add(userClassInstance)
                                            mAdapter.notifyDataSetChanged()
                                        }
                                        override fun onCancelled(firebaseError: DatabaseError) {}
                                    }
                                )
                            } else { // 含まれていなければ
                                Snackbar.make(v, "見つかりませんでした", Snackbar.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )
            }
        }


        val extras = intent.extras
        val id = extras!!.get("id") as Int

        //これがいるのか不明
        // Userのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mUserArrayList.clear()
        mAdapter.setUserArrayList(mUserArrayList)
        mListView.adapter = mAdapter


        if (id == id__nav_search_users) {
            title = "最初に表示されるのは一覧"

            searchWindow.visibility = View.VISIBLE
            searchButton.visibility = View.VISIBLE

            mDatabaseReference.child("users").addChildEventListener(mEventListenerForUsersList)
        }
        else if (id == id__nav_followings_list) {
            title = "followings_list"

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser!! // ログインしていないとここには来ない
            mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsFollowersListRef)
        }
        else if (id == id__nav_followers_list) {
            title = "followers_list"

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser!! // ログインしていないとここには来ない
            mDatabaseReference.child("users").child(user.uid).child("followers_list").addValueEventListener(mEventListenerForFollowingsFollowersListRef)
        }
    }
}
