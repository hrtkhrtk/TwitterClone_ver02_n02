//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_post_send.*

class PostSendActivity : AppCompatActivity(), DatabaseReference.CompletionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_send)

        // UIの準備
        postButton.setOnClickListener{ v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val user = FirebaseAuth.getInstance().currentUser!! // このActivity（ページ）に来るのは、ログインユーザだけなので「!!」をつけてよい
            val postRef = dataBaseReference.child("posts").child(user.uid)

            val postText = postText.text.toString()
            if (postText.length != 0) {
                val data = HashMap<String, Any>()
                data["text"] = postText
                data["created_at"] = ServerValue.TIMESTAMP
                progressBar.visibility = View.VISIBLE
                postRef.push().setValue(data, this)
            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
}
