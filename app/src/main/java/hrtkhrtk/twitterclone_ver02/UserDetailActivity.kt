//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_user_detail.*

class UserDetailActivity : AppCompatActivity() {

    private lateinit var mPostForShowingArrayList: ArrayList<PostForShowing>
    private lateinit var mPostArrayList: ArrayList<Post>
    private lateinit var mUserDetail: UserDetail
    private lateinit var mAdapter: UserDetailListAdapter
    private lateinit var mPostRef: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val post_id = dataSnapshot.key!!

            for (postForShowing in mPostForShowingArrayList) {
                // 同じpost_idのものが存在しているときは何もしない
                if (post_id == postForShowing.postId) {
                    return
                }
            }

            val text = map["text"] ?: ""
            val map2 = dataSnapshot.value as Map<String, Long>
            val created_at_Long = map2["created_at"]!! // ここは必ず存在
            val favoriters_list = map["favoriters_list"] as java.util.ArrayList<String>? ?: ArrayList<String>() // こんな書き方でいい？
            val iconImage = mUserDetail.iconImage
            val nickname = mUserDetail.nickname
            val user_id = mUserDetail.userId

            val postClass = Post(iconImage, nickname, text, created_at_Long, favoriters_list, user_id, post_id)
            mPostArrayList.add(postClass)
            val sorted_mPostArrayList = mPostArrayList.sortedWith(compareBy({-it.createdAt})) // マイナスをつけて降順にする // 参考：https://android.benigumo.com/20180206/sortedwith-compareby/
            mPostForShowingArrayList.clear()
            for ((index, element) in sorted_mPostArrayList.withIndex()) {
                val postForShowing = PostForShowing(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId, index)
                mPostForShowingArrayList.add(postForShowing)
                mAdapter.notifyDataSetChanged()
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        // 渡ってきたオブジェクトを保持する
        val extras = intent.extras
        mPostForShowingArrayList = extras!!.get("postForShowingArrayList") as ArrayList<PostForShowing> // 渡ってくる前にすでに並べ替えられている
        mPostArrayList = ArrayList<Post>()
        for (element in mPostForShowingArrayList) {
            val postClass = Post(element.bytes, element.nickname, element.text, element.createdAt, element.favoritersList, element.userId, element.postId)
            mPostArrayList.add(postClass)
        }

        mUserDetail = extras.get("userDetail") as UserDetail

        title = mUserDetail.nickname

        // ListViewの準備
        mAdapter = UserDetailListAdapter(this, mUserDetail, mPostForShowingArrayList)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mPostRef = dataBaseReference.child("posts").child(mUserDetail.userId)
        mPostRef.addChildEventListener(mEventListener)
    }
}
