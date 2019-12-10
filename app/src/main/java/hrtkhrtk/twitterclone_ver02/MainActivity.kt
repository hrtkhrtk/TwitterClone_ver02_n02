//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mPostArrayList: ArrayList<Post>
    private lateinit var mPostForShowingArrayList: ArrayList<PostForShowing>
    private lateinit var mAdapter: PostForShowingsListAdapter

    private val mEventListenerForPostsRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val posts_list_all = dataSnapshot.value as HashMap<String, String>? ?: HashMap<String, String>() // ここはnullかも

            for (user_id in posts_list_all.keys) {
                val posts_list_each = posts_list_all[user_id] as Map<String, String> // ここは必ず存在（たぶん）

                mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot_in_userRef: DataSnapshot) {
                            val data_in_userRef = snapshot_in_userRef.value as Map<String, String> // ここは必ず存在
                            val iconImageString = data_in_userRef["icon_image"]!! // ここは必ず存在
                            val nickname = data_in_userRef["nickname"]!! // ここは必ず存在

                            val bytes =
                                if (iconImageString.isNotEmpty()) {
                                    Base64.decode(iconImageString, Base64.DEFAULT)
                                } else {
                                    byteArrayOf()
                                }

                            for (post_id in posts_list_each.keys) {
                                val post_each = posts_list_each[post_id] as Map<String, String> // ここは必ず存在
                                val text = post_each["text"]!! // ここは必ず存在
                                val post_each_2 = posts_list_each[post_id] as Map<String, Long> // ここは必ず存在
                                val created_at_Long = post_each_2["created_at"]!! // ここは必ず存在
                                val favoriters_list = post_each["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？


                                val postClass = Post(bytes, nickname, text, created_at_Long, favoriters_list, user_id, post_id)
                                mPostArrayList.add(postClass)
                                val sorted_mPostArrayList = mPostArrayList.sortedWith(compareBy({-it.createdAt})) // マイナスをつけて降順にする // 参考：https://android.benigumo.com/20180206/sortedwith-compareby/
                                //mPostForShowingArrayList = sorted_mPostArrayList.mapIndexed { index, element -> PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index) } as ArrayList<PostForShowing> // 参考：https://qiita.com/kirimin/items/5e2e12a9500e77445fda
                                //mAdapter.notifyDataSetChanged()
                                //理由は不明だが↑この書き方ではダメっぽい
                                //のでこの↓この書き方に変更
                                mPostForShowingArrayList.clear()
                                for ((index, element) in sorted_mPostArrayList.withIndex()) {
                                    val postForShowing = PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index)
                                    mPostForShowingArrayList.add(postForShowing)
                                    mAdapter.notifyDataSetChanged()
                                }
                            }
                        }

                        override fun onCancelled(firebaseError_in_userRef: DatabaseError) {}
                    }
                )
            }
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }



    private val mEventListenerForFavoritesListRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Postのリストをクリア
            mPostForShowingArrayList.clear()
            mPostArrayList.clear()
            val favorites_list = dataSnapshot.value as ArrayList<Map<String, String>>? ?: ArrayList<Map<String, String>>()
            if (favorites_list.isEmpty()) {
                mAdapter.notifyDataSetChanged()
            }
            for (favorite_element in favorites_list) {
                mDatabaseReference.child("posts").child(favorite_element["user_id"]!!).child(favorite_element["post_id"]!!).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot_in_postRef: DataSnapshot) {
                            val data_in_postRef = snapshot_in_postRef.value as Map<String, String> // ここは必ず存在
                            val text = data_in_postRef["text"]!! // ここは必ず存在
                            val data_in_postRef_2 = snapshot_in_postRef.value as Map<String, Long> // ここは必ず存在
                            val created_at_Long = data_in_postRef_2["created_at"]!! // ここは必ず存在
                            val favoriters_list = data_in_postRef["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？

                            mDatabaseReference.child("users").child(favorite_element["user_id"]!!).addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot_in_userRef: DataSnapshot) {
                                        val data_in_userRef = snapshot_in_userRef.value as Map<String, String> // ここは必ず存在
                                        val iconImageString = data_in_userRef["icon_image"]!! // ここは必ず存在
                                        val nickname = data_in_userRef["nickname"]!! // ここは必ず存在

                                        val bytes =
                                            if (iconImageString.isNotEmpty()) {
                                                Base64.decode(iconImageString, Base64.DEFAULT)
                                            } else {
                                                byteArrayOf()
                                            }

                                        val postClass = Post(bytes, nickname, text, created_at_Long, favoriters_list, favorite_element["user_id"]!!, favorite_element["post_id"]!!)
                                        mPostArrayList.add(postClass)
                                        val sorted_mPostArrayList = mPostArrayList.sortedWith(compareBy({-it.createdAt})) // マイナスをつけて降順にする // 参考：https://android.benigumo.com/20180206/sortedwith-compareby/
                                        mPostForShowingArrayList.clear()
                                        for ((index, element) in sorted_mPostArrayList.withIndex()) {
                                            val postForShowing = PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index)
                                            mPostForShowingArrayList.add(postForShowing)
                                            mAdapter.notifyDataSetChanged()
                                        }
                                    }

                                    override fun onCancelled(firebaseError_in_userRef: DatabaseError) {}
                                }
                            )
                        }

                        override fun onCancelled(firebaseError_in_postRef: DatabaseError) {}
                    }
                )
            }
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }



    private val mEventListenerForFollowingsListRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val followings_list = dataSnapshot.value as ArrayList<String>? ?: ArrayList<String>()

            val user = FirebaseAuth.getInstance().currentUser!! // ここはログインユーザしか来ない
            var followings_list_with_current_user = followings_list
            followings_list_with_current_user.add(user.uid)

            for (user_id in followings_list_with_current_user) {
                mDatabaseReference.child("posts").child(user_id).addChildEventListener(
                    object : ChildEventListener {
                        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                            val map = dataSnapshot.value as Map<String, String>
                            val text = map["text"] ?: ""
                            val map2 = dataSnapshot.value as Map<String, Long>
                            val created_at_Long = map2["created_at"]!! // ここは必ず存在
                            val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
                            val post_id = dataSnapshot.key!!

                            var iconImageString: String? = null
                            var nickname: String? = null

                            mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val data = snapshot.value as Map<String, String> // ここは必ず存在
                                        iconImageString = data["icon_image"]
                                        nickname = data["nickname"]

                                        val bytes =
                                            if (iconImageString!!.isNotEmpty()) {
                                                Base64.decode(iconImageString, Base64.DEFAULT)
                                            } else {
                                                byteArrayOf()
                                            }

                                        val postClass = Post(bytes, nickname!!, text, created_at_Long, favoriters_list, user_id, post_id)
                                        mPostArrayList.add(postClass)
                                        val sorted_mPostArrayList = mPostArrayList.sortedWith(compareBy({-it.createdAt})) // マイナスをつけて降順にする // 参考：https://android.benigumo.com/20180206/sortedwith-compareby/
                                        mPostForShowingArrayList.clear()
                                        for ((index, element) in sorted_mPostArrayList.withIndex()) {
                                            val postForShowing = PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index)
                                            mPostForShowingArrayList.add(postForShowing)
                                            mAdapter.notifyDataSetChanged()
                                        }
                                    }

                                    override fun onCancelled(firebaseError: DatabaseError) {}
                                }
                            )
                        }

                        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

                        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

                        override fun onCancelled(databaseError: DatabaseError) {}
                    }
                )
            }
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }



    private val mEventListenerForMyPosts = object : ChildEventListener { // 仮置き // TODO:
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val text = map["text"] ?: ""
            val map2 = dataSnapshot.value as Map<String, Long>
            val created_at_Long = map2["created_at"]!! // ここは必ず存在
            val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
            val post_id = dataSnapshot.key!!

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            val user_id = user!!.uid
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<String, String> // 必ず存在
                        val iconImageString = data["icon_image"] as String
                        val nickname = data["nickname"] as String

                        val bytes =
                            if (iconImageString.isNotEmpty()) {
                                Base64.decode(iconImageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }

                        val postClass = Post(bytes, nickname, text, created_at_Long, favoriters_list, user_id, post_id)
                        mPostArrayList.add(postClass)
                        val sorted_mPostArrayList = mPostArrayList.sortedWith(compareBy({-it.createdAt})) // マイナスをつけて降順にする // 参考：https://android.benigumo.com/20180206/sortedwith-compareby/
                        mPostForShowingArrayList.clear()
                        for ((index, element) in sorted_mPostArrayList.withIndex()) {
                            val postForShowing = PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index)
                            mPostForShowingArrayList.add(postForShowing)
                            mAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                }
            )
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {}
    }



    private val mEventListenerForStatusRef = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val status = dataSnapshot.value as String // ここは必ず存在
            if (status.toInt() == 0) {
                mPostForShowingArrayList.clear()
                mPostArrayList.clear() // ここでやる必要はそんなになさそうだが
                mAdapter.notifyDataSetChanged()

                Snackbar.make(findViewById(android.R.id.content), "statusが0です", Snackbar.LENGTH_LONG).show()
            }
            else if (status.toInt() == 1) {
                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser!! // ここはログインユーザしか来ない

                // removeいる？ // TODO:
                mDatabaseReference.child("users").child(user.uid).child("favorites_list").removeEventListener(mEventListenerForFavoritesListRef)
                mDatabaseReference.child("users").child(user.uid).child("favorites_list").addValueEventListener(mEventListenerForFavoritesListRef)
            }
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        // httpリクエストを入れる変数
        val builder = Uri.Builder()
        val task = AsyncHttpRequestForWeatherInformation(this)
        task.execute(builder)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { _ ->
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@MainActivity, PostSendActivity::class.java)
                startActivity(intent)
            }
        }



        searchButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val searchText = searchWindow.text.toString()

            if (searchText.isEmpty()) {
                Snackbar.make(v, "入力して下さい", Snackbar.LENGTH_LONG).show()

                //これがいるのか不明
                // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mPostForShowingArrayList.clear()
                mPostArrayList.clear()
                mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
                mListView.adapter = mAdapter

                title = "post一覧"

                mDatabaseReference.child("posts").addListenerForSingleValueEvent(mEventListenerForPostsRef) // ひとまずSingleValueEventで
            } else {
                //これがいるのか不明
                // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mPostForShowingArrayList.clear()
                mPostArrayList.clear()
                mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
                mListView.adapter = mAdapter

                title = "検索結果"

                mDatabaseReference.child("posts").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val posts_list_all = dataSnapshot.value as HashMap<String, String>? ?: HashMap<String, String>() // ここはnullかも

                            for (user_id in posts_list_all.keys) {
                                val posts_list_each = posts_list_all[user_id] as Map<String, String> // ここは必ず存在（たぶん）
                                for (post_id in posts_list_each.keys) {
                                    val post_each = posts_list_each[post_id] as Map<String, String> // ここは必ず存在
                                    val text = post_each["text"]!! // ここは必ず存在

                                    val regex = Regex(searchText) // 参考：http://extra-vision.blogspot.com/2016/11/kotlin.html
                                    if (regex.containsMatchIn(text)) {
                                        val post_each_2 = posts_list_each[post_id] as Map<String, Long> // ここは必ず存在
                                        val created_at_Long = post_each_2["created_at"]!! // ここは必ず存在
                                        val favoriters_list = post_each["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？

                                        mDatabaseReference.child("users").child(user_id).addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot_in_userRef: DataSnapshot) {
                                                    val data_in_userRef = snapshot_in_userRef.value as Map<String, String> // ここは必ず存在
                                                    val iconImageString = data_in_userRef["icon_image"]!! // ここは必ず存在
                                                    val nickname = data_in_userRef["nickname"]!! // ここは必ず存在

                                                    val bytes =
                                                        if (iconImageString.isNotEmpty()) {
                                                            Base64.decode(iconImageString, Base64.DEFAULT)
                                                        } else {
                                                            byteArrayOf()
                                                        }

                                                    val postClass = Post(bytes, nickname, text, created_at_Long, favoriters_list, user_id, post_id)
                                                    mPostArrayList.add(postClass)
                                                    val sorted_mPostArrayList = mPostArrayList.sortedWith(compareBy({-it.createdAt})) // マイナスをつけて降順にする // 参考：https://android.benigumo.com/20180206/sortedwith-compareby/
                                                    mPostForShowingArrayList.clear()
                                                    for ((index, element) in sorted_mPostArrayList.withIndex()) {
                                                        val postForShowing = PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index)
                                                        mPostForShowingArrayList.add(postForShowing)
                                                        mAdapter.notifyDataSetChanged()
                                                    }
                                                }

                                                override fun onCancelled(firebaseError_in_userRef: DatabaseError) {}
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )
            }
        }



        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navHeader = findViewById<RelativeLayout>(R.id.nav_header)
        val navListView = findViewById<ListView>(R.id.nav_menu_items)
        val navFooter = findViewById<FrameLayout>(R.id.nav_footer)

        val navigationItemList = ArrayList<NavigationItem>()
        val navigationItem_01 = NavigationItem(id__nav_posts, "posts")
        val navigationItem_02 = NavigationItem(id__nav_search_posts, "search_posts")
        val navigationItem_03 = NavigationItem(id__nav_search_users, "search_users")
        val navigationItem_04 = NavigationItem(id__nav_followings_list, "followings_list")
        val navigationItem_05 = NavigationItem(id__nav_followers_list, "followers_list")
        val navigationItem_06 = NavigationItem(id__nav_favorites_list, "favorites_list")
        val navigationItem_07 = NavigationItem(id__nav_my_posts, "my_posts")

        navigationItemList.add(navigationItem_01)
        navigationItemList.add(navigationItem_02)
        navigationItemList.add(navigationItem_03)
        navigationItemList.add(navigationItem_04)
        navigationItemList.add(navigationItem_05)
        navigationItemList.add(navigationItem_06)
        navigationItemList.add(navigationItem_07)

        val navigationListAdapter = NavigationListAdapter(this)
        navigationListAdapter.setNavigationItemArrayList(navigationItemList)
        navListView.adapter = navigationListAdapter
        navigationListAdapter.notifyDataSetChanged() // これがいるか不明


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<String, String> // ここは必ず存在

                        val nicknameText_in_nav_header_main = navHeader.findViewById<TextView>(R.id.nicknameTextView)
                        nicknameText_in_nav_header_main.setText(data["nickname"])

                        val idForSearchText_in_nav_header_main = navHeader.findViewById<TextView>(R.id.idForSearchTextView)
                        idForSearchText_in_nav_header_main.setText(data["id_for_search"])

                        val followingsNumberText_in_nav_header_main = navHeader.findViewById<TextView>(R.id.followingsNumberTextView)
                        val followings_list = data["followings_list"] as ArrayList<String>? ?: ArrayList<String>()
                        followingsNumberText_in_nav_header_main.setText(followings_list.size.toString())

                        val followersNumberText_in_nav_header_main = navHeader.findViewById<TextView>(R.id.followersNumberTextView)
                        val followers_list = data["followers_list"] as ArrayList<String>? ?: ArrayList<String>()
                        followersNumberText_in_nav_header_main.setText(followers_list.size.toString())

                        val imageView_in_nav_header_main = navHeader.findViewById<View>(R.id.imageView) as ImageView
                        val iconImageString = data["icon_image"]
                        val bytes =
                            if (iconImageString!!.isNotEmpty()) {
                                Base64.decode(iconImageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }
                        if (bytes.isNotEmpty()) {
                            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                            imageView_in_nav_header_main.setImageBitmap(image)
                        }
                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                }
            )
        }


        val titleInNavFooter = navFooter.findViewById<TextView>(R.id.titleInNavFooter)
        titleInNavFooter.text = "policy"
        titleInNavFooter.setOnClickListener { v ->
            val intent = Intent(this@MainActivity, PolicyActivity::class.java)
            startActivity(intent)
        }


        navListView.setOnItemClickListener { _, _, position, _ ->
            val item_id = navigationItemList[position].id

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)

            if ((item_id == id__nav_posts) || (item_id == id__nav_favorites_list) || (item_id == id__nav_my_posts) || (item_id == id__nav_search_posts)) {
                // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mPostForShowingArrayList.clear()
                mPostArrayList.clear()
                mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
                mListView.adapter = mAdapter


                if (item_id == id__nav_posts) {
                    searchWindow.visibility = View.GONE
                    searchButton.visibility = View.GONE

                    mToolbar.title = "posts"
                    // ログイン済みのユーザーを取得する
                    val user = FirebaseAuth.getInstance().currentUser

                    // ログインしていなければログイン画面に遷移させる
                    if (user == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        // removeいる？
                        // remove↓これで大丈夫？ // TODO:
                        //mDatabaseReference.child("users").child(user.uid).child("followings_list").removeEventListener(mEventListenerForFollowingsListRef)
                        mDatabaseReference.child("users").child(user.uid).child("favorites_list").removeEventListener(mEventListenerForFavoritesListRef)
                        mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsListRef)
                    }
                } else if (item_id == id__nav_my_posts) {
                    searchWindow.visibility = View.GONE
                    searchButton.visibility = View.GONE

                    mToolbar.title = "my_posts"

                    // ログイン済みのユーザーを取得する
                    val user = FirebaseAuth.getInstance().currentUser

                    // ログインしていなければログイン画面に遷移させる
                    if (user == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        // removeいる？
                        // remove↓これで大丈夫？ // TODO:
                        //mDatabaseReference.child("posts").child(user.uid).removeEventListener(mEventListenerForMyPosts)
                        mDatabaseReference.child("users").child(user.uid).child("favorites_list").removeEventListener(mEventListenerForFavoritesListRef)
                        mDatabaseReference.child("posts").child(user.uid).addChildEventListener(mEventListenerForMyPosts)
                    }
                }
                else if (item_id == id__nav_favorites_list) {
                    searchWindow.visibility = View.GONE
                    searchButton.visibility = View.GONE

                    mToolbar.title = "favorites_list"

                    // ログイン済みのユーザーを取得する
                    val user = FirebaseAuth.getInstance().currentUser

                    // ログインしていなければログイン画面に遷移させる
                    if (user == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        // removeいる？ // TODO:
                        mDatabaseReference.child("users").child(user.uid).child("status").removeEventListener(mEventListenerForStatusRef)
                        mDatabaseReference.child("users").child(user.uid).child("status").addValueEventListener(mEventListenerForStatusRef)
                    }
                }
                else if (item_id == id__nav_search_posts) {
                    title = "最初に表示されるのはpost一覧"

                    searchWindow.visibility = View.VISIBLE
                    searchButton.visibility = View.VISIBLE

                    val user = FirebaseAuth.getInstance().currentUser // ここはログインしてなくても来れる予定だが仮置き // TODO:
                    mDatabaseReference.child("users").child(user!!.uid).child("favorites_list").removeEventListener(mEventListenerForFavoritesListRef)
                    mDatabaseReference.child("posts").addListenerForSingleValueEvent(mEventListenerForPostsRef) // ひとまずSingleValueEventで
                }
            }
            else if ((item_id == id__nav_search_users) || (item_id == id__nav_followings_list) || (item_id == id__nav_followers_list)) {
                if (item_id == id__nav_search_users) {
                    val intent = Intent(this@MainActivity, UsersListActivity::class.java)
                    intent.putExtra("id", item_id)
                    startActivity(intent)
                }
                else if ((item_id == id__nav_followings_list) || (item_id == id__nav_followers_list)) {
                    // ログイン済みのユーザーを取得する
                    val user = FirebaseAuth.getInstance().currentUser

                    // ログインしていなければログイン画面に遷移させる
                    if (user == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        val intent = Intent(this@MainActivity, UsersListActivity::class.java)
                        intent.putExtra("id", item_id)
                        startActivity(intent)
                    }
                }
            }
        }



        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = PostForShowingsListAdapter(this)
        mPostArrayList = ArrayList<Post>()
        mPostForShowingArrayList = ArrayList<PostForShowing>()
        mAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        // これが必要か不明
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        // Postのリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mPostForShowingArrayList.clear()
        mPostArrayList.clear()
        mAdapter.setPostForShowingArrayList(mPostForShowingArrayList)
        mListView.adapter = mAdapter

        searchWindow.visibility = View.GONE
        searchButton.visibility = View.GONE

        mToolbar.title = "posts"

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        // ログインしていなければログイン画面に遷移させる
        if (user == null) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        else {
            // removeいる？
            // remove↓これで大丈夫？ // TODO:
            //mDatabaseReference.child("users").child(user.uid).child("followings_list").removeEventListener(mEventListenerForFollowingsListRef)
            mDatabaseReference.child("users").child(user.uid).child("followings_list").addValueEventListener(mEventListenerForFollowingsListRef)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_SettingActivity) {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                return true
            } else {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        else if (id == R.id.action_PurchasingActivity) {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                return true
            } else {
                val intent = Intent(this, PurchasingActivity::class.java)
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
