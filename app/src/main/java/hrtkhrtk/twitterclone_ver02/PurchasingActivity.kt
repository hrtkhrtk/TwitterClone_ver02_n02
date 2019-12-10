//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import java.util.*
import jp.pay.android.Payjp
import jp.pay.android.PayjpConfiguration
import jp.pay.android.Task
import jp.pay.android.model.CardBrand
import jp.pay.android.model.Token
import jp.pay.android.ui.widget.PayjpCardFormFragment
import jp.pay.android.ui.widget.PayjpCardFormView
import android.app.DatePickerDialog
import com.google.android.material.snackbar.Snackbar
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlin.math.*

import kotlinx.android.synthetic.main.activity_purchasing.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T

class PurchasingActivity : AppCompatActivity() {

    val mCardFormFragment = PayjpCardFormFragment.newInstance()
    var mUtc :Long? = null
    var mAvailableToLong :Long? = null
    var mPrice :Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchasing)

        // UIの初期設定
        title = "購入ページ"
        textPrice.text = "unknown"

        // または Payjp.init("pk_test_0383a1b8f91e8a6e3ea0e2a9")
        Payjp.init(
            PayjpConfiguration.Builder("pk_test_0ca997049dcd98ddfbc2f04a")
                .setDebugEnabled(BuildConfig.DEBUG)
                .setLocale(Locale.JAPAN)
                .build())

        supportFragmentManager.beginTransaction()
            .replace(R.id.card_form_view, mCardFormFragment as Fragment, "TAG_CARD_FORM")
            .commit()

        dateButton.setOnClickListener {
            var availableTo_year :Int = 2020 // デフォルト
            var availableTo_month :Int = 1 // デフォルト
            var availableTo_day :Int = 1 // デフォルト
            if (mAvailableToLong != null) {
                val cal_mAvailableToLong = Calendar.getInstance()
                cal_mAvailableToLong.setTimeInMillis(mAvailableToLong!!)
                availableTo_year = cal_mAvailableToLong.get(Calendar.YEAR)
                availableTo_month = cal_mAvailableToLong.get(Calendar.MONTH)
                availableTo_day = cal_mAvailableToLong.get(Calendar.DAY_OF_MONTH)
            }

            val datePickerDialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    mPrice = null
                    textPrice.text = "unknown"

                    val cal = Calendar.getInstance() // 参考：http://www.jp-z.jp/changelog/2011-06-03-1.html
                    cal.set(year, month, dayOfMonth, 23, 59, 59) // その日が終わるまで
                    mUtc = cal.timeInMillis
                    val dateString = year.toString() + "/" + String.format("%02d", month + 1) + "/" + String.format("%02d", dayOfMonth)
                    dateButton.text = dateString

                    if (mAvailableToLong != null) {
                        val total_day_mUtc = floor((mUtc!! / (24*60*60*1000)).toDouble()).toInt() // 参考：http://y-anz-m.blogspot.com/2018/04/kotlinmath.html
                        val total_day_mAvailableToLong = floor((mAvailableToLong!! / (24*60*60*1000)).toDouble()).toInt()
                        if (total_day_mUtc > total_day_mAvailableToLong) {
                            mPrice = (total_day_mUtc - total_day_mAvailableToLong) * 10 // 1日10円
                            textPrice.text = mPrice.toString()
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "available_toより先の日付を入力してください", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }, availableTo_year, availableTo_month, availableTo_day) // 仮置き
            datePickerDialog.show()
        }

        submitButton.setOnClickListener {v ->
            if (mUtc == null) {
                Snackbar.make(v, "日付を入力してください", Snackbar.LENGTH_LONG).show()
            }
            else if (mAvailableToLong == null) {
                Snackbar.make(v, "データを読み込んでいるため少しお待ちください", Snackbar.LENGTH_LONG).show()
            }
            else { // mUtc も mAvailableToLong も null じゃない
                if (mPrice != null) {
                    onClickSubmit()
                } else {
                    Snackbar.make(v, "available_toより先の日付を入力してください", Snackbar.LENGTH_LONG).show()
                }
            }
        }


        val user = FirebaseAuth.getInstance().currentUser!! // ここはログインユーザしか来ない
        FirebaseDatabase.getInstance().reference.child("users").child(user.uid).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<String, Long> // ここは必ず存在
                    mAvailableToLong = data["available_to"] // ここは必ず存在
                    textAvailableTo.setText(getDateTime(mAvailableToLong!!, "yyyy/MM/dd"))
                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            }
        )
    }


    fun onClickSubmit() {
        if (!mCardFormFragment.validateCardForm()) return
        mCardFormFragment.createToken().enqueue(object : Task.Callback<Token> {
            override fun onSuccess(data: Token) {
                // httpリクエストを入れる変数
                val builder = Uri.Builder()

                val user = FirebaseAuth.getInstance().currentUser!! // ここはログインユーザしか来ない
                val task = AsyncHttpRequest(this@PurchasingActivity, data.id, mPrice!!.toLong(), mUtc!!, user.uid)
                task.execute(builder)
            }

            override fun onError(throwable: Throwable) {
                Log.e("CardFormViewSample", "failure creating token", throwable)
            }
        })
    }
}
