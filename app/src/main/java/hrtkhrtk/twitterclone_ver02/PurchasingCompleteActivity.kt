//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_purchasing_complete.*

class PurchasingCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchasing_complete)

        // UIの初期設定
        title = "購入完了"

        val extras = intent.extras
        val price = extras!!.get("price") as Int
        val purchaseTo = extras.get("purchaseTo") as Long

        textPurchaseTo.setText(getDateTime(purchaseTo, "yyyy/MM/dd"))
        textPrice.setText(price.toString())

        backButton.setOnClickListener { v ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val user = FirebaseAuth.getInstance().currentUser!! // ここはログインユーザしか来ない
        val resultIntent = Intent(this, StatusChangeReceiver::class.java)
        resultIntent.putExtra(USER_ID_FOR_PendingIntent, user.uid)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE_FOR_PendingIntent,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, purchaseTo, resultPendingIntent) // Unix時間はUTCでの時間であることに注意


    }
}
