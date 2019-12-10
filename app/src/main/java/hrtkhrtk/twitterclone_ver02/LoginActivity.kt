//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult

import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance()

        // ログイン処理のリスナー
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                val user = mAuth.currentUser
                val usersRef = mDataBaseReference.child("users").child(user!!.uid)

                usersRef.addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            //val data = snapshot.value as Map<String, String>
                            //saveName(data["nickname"]!!)

                            val data2 = snapshot.value as Map<String, Long>
                            val available_to_Long = data2["available_to"]!! // ここは必ず存在
                            if (System.currentTimeMillis() > available_to_Long) {
                                // statusをupdate
                                val dataForUpdate = HashMap<String, String>()
                                dataForUpdate["status"] = 0.toString() // 0:お試しユーザー、1:サブスクユーザー
                                usersRef.updateChildren(dataForUpdate as Map<String, String>)
                            } else {
                                // statusをupdate // 本来は必要ないが、データが整合してなかったときのため
                                val dataForUpdate = HashMap<String, String>()
                                dataForUpdate["status"] = 1.toString() // 0:お試しユーザー、1:サブスクユーザー
                                usersRef.updateChildren(dataForUpdate as Map<String, String>)

                                val resultIntent = Intent(this@LoginActivity, StatusChangeReceiver::class.java)
                                resultIntent.putExtra(USER_ID_FOR_PendingIntent, user.uid)
                                val resultPendingIntent = PendingIntent.getBroadcast(
                                    this@LoginActivity,
                                    REQUEST_CODE_FOR_PendingIntent,
                                    resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                                alarmManager.set(AlarmManager.RTC_WAKEUP, available_to_Long, resultPendingIntent) // Unix時間はUTCでの時間であることに注意
                            }
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE

                // Activityを閉じる
                finish()

            } else {
                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        // UIの準備
        title = "Log in"

        loginButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6) {
                login(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }

        toSignupActivityButton.setOnClickListener { v ->
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    /*
    private fun saveName(name: String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }
    */
}
