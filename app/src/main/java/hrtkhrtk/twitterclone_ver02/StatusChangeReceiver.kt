package hrtkhrtk.twitterclone_ver02

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class StatusChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("test191208n10", "onReceive")

        val extras = intent!!.extras
        val user_id = extras!!.get(USER_ID_FOR_PendingIntent) as String

        // ほかの人がログイン中でもstatusを変更する
        // statusをupdate
        val data = HashMap<String, String>()
        data["status"] = 0.toString() // 0:お試しユーザー、1:サブスクユーザー
        FirebaseDatabase.getInstance().reference.child("users").child(user_id).updateChildren(data as Map<String, String>)
    }
}