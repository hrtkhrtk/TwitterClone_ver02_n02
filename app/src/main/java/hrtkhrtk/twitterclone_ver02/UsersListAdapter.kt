//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UsersListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mUserArrayList = ArrayList<User>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mUserArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mUserArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_users, parent, false)
        }

        val nicknameText = convertView!!.findViewById<View>(R.id.nicknameTextView) as TextView
        nicknameText.text = mUserArrayList[position].nickname

        val idForSearchText = convertView.findViewById<View>(R.id.idForSearchTextView) as TextView
        idForSearchText.text = mUserArrayList[position].idForSearch

        val selfIntroductionText = convertView.findViewById<View>(R.id.selfIntroductionTextView) as TextView
        selfIntroductionText.text = mUserArrayList[position].selfIntroduction

        val bytes = mUserArrayList[position].iconImage
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val iconImageView = convertView.findViewById<View>(R.id.iconImageView) as ImageView
            iconImageView.setImageBitmap(image)
        }


        val userId = mUserArrayList[position].userId
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // ログインしていない場合は何もしない
        } else if (user.uid != userId) { // 自分じゃなければ
            val followButton = convertView.findViewById<Button>(R.id.followButton) // as Buttonを付けるとエラーになる

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val currentUserRef = dataBaseReference.child("users").child(user.uid)
            currentUserRef.addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData = snapshot.value as MutableMap<String, String>

                        if (userData!!["followings_list"] == null) {
                            followButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                            followButton.text = "follow"
                        }
                        else {
                            val existingFollowingsList = userData!!["followings_list"] as ArrayList<String>

                            if (!(existingFollowingsList.contains(userId))) { // 含まれなければ
                                followButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                followButton.text = "follow"
                            }
                            else { // 含まれていれば
                                followButton.setBackgroundColor(Color.parseColor("#ff0000")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                followButton.text = "unfollow"
                            }
                        }
                    }
                    override fun onCancelled(firebaseError: DatabaseError) {}
                }
            )

            followButton.visibility = View.VISIBLE

            followButton.setOnClickListener { v ->
                // ログイン済みのユーザーを取得する
                val userInFollowButton = FirebaseAuth.getInstance().currentUser

                if (userInFollowButton == null) {
                    // ログインしていない場合は何もしない
                    Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
                } else {
                    // Firebaseに保存する
                    val dataBaseReferenceInFollowButton = FirebaseDatabase.getInstance().reference
                    val currentUserRefInFollowButton = dataBaseReferenceInFollowButton.child("users").child(user.uid)
                    val followeeUserRefInFollowButton = dataBaseReferenceInFollowButton.child("users").child(userId)

                    var userDataInFollowButton : MutableMap<String, String>? = null

                    currentUserRefInFollowButton.addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                userDataInFollowButton = snapshot.value as MutableMap<String, String>

                                if (userDataInFollowButton!!["followings_list"] == null) { // リストに入っていない（リストがない）
                                    val existingFollowingsListInCurrentUser = ArrayList<String>()
                                    existingFollowingsListInCurrentUser.add(userId)
                                    dataBaseReference.child("users").child(user.uid).child("followings_list").setValue(existingFollowingsListInCurrentUser)

                                    followeeUserRefInFollowButton.addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val userData = snapshot.value as MutableMap<String, String>
                                                val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>? ?: ArrayList<String>()
                                                existingFollowersListInFolloweeUser.add(user.uid)
                                                dataBaseReference.child("users").child(userId).child("followers_list").setValue(existingFollowersListInFolloweeUser)
                                            }
                                            override fun onCancelled(firebaseError: DatabaseError) {}
                                        }
                                    )
                                }
                                else {
                                    val existingFollowingsListInCurrentUser = userDataInFollowButton!!["followings_list"] as ArrayList<String>

                                    if (!(existingFollowingsListInCurrentUser.contains(userId))) { // 含まれなければ追加
                                        existingFollowingsListInCurrentUser.add(userId)
                                        dataBaseReference.child("users").child(user.uid).child("followings_list").setValue(existingFollowingsListInCurrentUser)

                                        followeeUserRefInFollowButton.addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val userData = snapshot.value as MutableMap<String, String>
                                                    val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>? ?: ArrayList<String>()
                                                    existingFollowersListInFolloweeUser.add(user.uid)
                                                    dataBaseReference.child("users").child(userId).child("followers_list").setValue(existingFollowersListInFolloweeUser)
                                                }
                                                override fun onCancelled(firebaseError: DatabaseError) {}
                                            }
                                        )
                                    }
                                    else { // 含まれていれば削除
                                        existingFollowingsListInCurrentUser.remove(userId) // 参考：Lesson3項目11.3
                                        dataBaseReference.child("users").child(user.uid).child("followings_list").setValue(existingFollowingsListInCurrentUser)

                                        followeeUserRefInFollowButton.addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val userData = snapshot.value as MutableMap<String, String>
                                                    val existingFollowersListInFolloweeUser = userData["followers_list"] as ArrayList<String>
                                                    existingFollowersListInFolloweeUser.remove(user.uid)
                                                    dataBaseReference.child("users").child(userId).child("followers_list").setValue(existingFollowersListInFolloweeUser)
                                                }
                                                override fun onCancelled(firebaseError: DatabaseError) {}
                                            }
                                        )
                                    }
                                }
                            }
                            override fun onCancelled(firebaseError: DatabaseError) {}
                        }
                    )
                }
            }
        } else if (user.uid == userId) { // 自分の時
            val followButton = convertView.findViewById<Button>(R.id.followButton) // as Buttonを付けるとエラーになる
            followButton.visibility = View.GONE
        }


        return convertView
    }

    fun setUserArrayList(userArrayList: ArrayList<User>) {
        mUserArrayList = userArrayList
    }
}