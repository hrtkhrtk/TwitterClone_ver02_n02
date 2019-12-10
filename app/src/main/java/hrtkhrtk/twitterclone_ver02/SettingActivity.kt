//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_setting.*
import java.io.ByteArrayOutputStream

class SettingActivity : AppCompatActivity(), DatabaseReference.CompletionListener {
    companion object {
        private val PERMISSIONS_REQUEST_CODE_01 = 201 // icon_image
        private val PERMISSIONS_REQUEST_CODE_02 = 202 // background_image
        private val CHOOSER_REQUEST_CODE_01 = 101 // icon_image
        private val CHOOSER_REQUEST_CODE_02 = 102 // background_image
    }

    private var mIconImageUri: Uri? = null
    private var mBackgroundImageUri: Uri? = null

    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // UIの初期設定
        title = "設定"

        // EditTextに反映させる
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // ログインしていない場合は何もしない
            Snackbar.make(findViewById(android.R.id.content), "ログインしていません", Snackbar.LENGTH_LONG)
                .setAction("Log in") {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }.show()
        } else {
            val userRef = mDataBaseReference.child("users").child(user!!.uid)
            userRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<String, String> // ここは必ず存在
                        nicknameText.setText(data["nickname"])
                        selfIntroductionText.setText(data["self_introduction"])
                        textEmail.setText(data["email"])
                        textIdForSearch.setText(data["id_for_search"])
                        val data2 = snapshot.value as Map<String, Long>
                        val created_at_Long = data2["created_at"]!! // ここは必ず存在
                        textCreatedAt.setText(getDateTime(created_at_Long))
                        textStatus.setText(data["status"] as String)
                        val available_to_Long = data2["available_to"]!! // ここは必ず存在
                        textAvailableTo.setText(getDateTime(available_to_Long, "yyyy/MM/dd"))

                        val icon_image = data["icon_image"] as String
                        val background_image = data["background_image"] as String

                        if (icon_image.isNotEmpty()) { // 参考：Lesson8のMainActivity.kt
                            val bytes = Base64.decode(icon_image, Base64.DEFAULT) // 参考：Lesson8のMainActivity.kt
                            val imageBytes = bytes.clone() // 参考：Lesson8のQuestion.kt
                            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).copy(Bitmap.Config.ARGB_8888, true) // 参考：Lesson8のQuestionsListAdapter.kt
                            iconImageView.setImageBitmap(image)
                        }

                        if (background_image.isNotEmpty()) { // 参考：Lesson8のMainActivity.kt
                            val bytes = Base64.decode(background_image, Base64.DEFAULT) // 参考：Lesson8のMainActivity.kt
                            val imageBytes = bytes.clone() // 参考：Lesson8のQuestion.kt
                            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).copy(Bitmap.Config.ARGB_8888, true) // 参考：Lesson8のQuestionsListAdapter.kt
                            backgroundImageView.setImageBitmap(image)
                        }
                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                }
            )
        }


        changeButton.setOnClickListener{v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていない場合は何もしない
                Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG)
                    .setAction("Log in") {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }.show()
            } else {
                val nickname = nicknameText.text.toString()
                val self_introduction = selfIntroductionText.text.toString()

                if (nickname.length != 0) {
                    val userRef = mDataBaseReference.child("users").child(user.uid)
                    val data = HashMap<String, String>()
                    data["nickname"] = nickname
                    data["self_introduction"] = self_introduction

                    // 添付画像を取得する
                    val drawableIconImageView = iconImageView.drawable as? BitmapDrawable
                    // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
                    if (drawableIconImageView != null) {
                        val bitmap = drawableIconImageView.bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                        val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                        data["icon_image"] = bitmapString
                    }

                    // 添付画像を取得する
                    val drawableBackgroundImageView = backgroundImageView.drawable as? BitmapDrawable
                    // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
                    if (drawableBackgroundImageView != null) {
                        val bitmap = drawableBackgroundImageView.bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                        val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                        data["background_image"] = bitmapString
                    }

                    userRef.updateChildren(data as Map<String, String>, this)
                    // Lesson8
                    // 「保存する際はDatabaseReferenceクラスのsetValueを使いますが、今回は第2引数も指定しています。
                    // 第2引数にはCompletionListenerクラスを指定します（今回はActivityがCompletionListenerクラスを実装している）。」
                    progressBar.visibility = View.VISIBLE

                    /*
                    // 変更した表示名をPreferenceに保存する
                    val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val editor = sp.edit()
                    editor.putString(NameKEY, nickname)
                    editor.commit()
                    */

                    Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show()
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
                }
            }
        }


        iconImageView.setOnClickListener { // 参考：Lesson4項目3.1「補足」
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser(CHOOSER_REQUEST_CODE_01)
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE_01)
                }
            } else {
                showChooser(CHOOSER_REQUEST_CODE_01)
            }
        }


        backgroundImageView.setOnClickListener { // 参考：Lesson4項目3.1「補足」
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser(CHOOSER_REQUEST_CODE_02)
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE_02)
                }
            } else {
                showChooser(CHOOSER_REQUEST_CODE_02)
            }
        }


        toPurchasingActivityButton.setOnClickListener { v ->
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, PurchasingActivity::class.java)
                startActivity(intent)
            }
        }


        logoutButton.setOnClickListener { v ->
            FirebaseAuth.getInstance().signOut()
            nicknameText.setText("")
            selfIntroductionText.setText("")
            textEmail.setText("")
            textIdForSearch.setText("")
            textCreatedAt.setText("")
            textStatus.setText("")
            textAvailableTo.setText("")

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE_01) {
            if (resultCode != Activity.RESULT_OK) {
                if (mIconImageUri != null) {
                    contentResolver.delete(mIconImageUri!!, null, null)
                    mIconImageUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mIconImageUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            iconImageView.setImageBitmap(resizedImage)

            mIconImageUri = null
        }
        else if (requestCode == CHOOSER_REQUEST_CODE_02) {
            if (resultCode != Activity.RESULT_OK) {
                if (mBackgroundImageUri != null) {
                    contentResolver.delete(mBackgroundImageUri!!, null, null)
                    mBackgroundImageUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mBackgroundImageUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            backgroundImageView.setImageBitmap(resizedImage)

            mBackgroundImageUri = null
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE_01 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser(CHOOSER_REQUEST_CODE_01)
                }
                return
            }
            PERMISSIONS_REQUEST_CODE_02 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser(CHOOSER_REQUEST_CODE_02)
                }
                return
            }
        }
    }


    private fun showChooser(CHOOSER_REQUEST_CODE: Int) {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (CHOOSER_REQUEST_CODE == CHOOSER_REQUEST_CODE_01) {
            mIconImageUri = contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mIconImageUri)
        }
        else if (CHOOSER_REQUEST_CODE == CHOOSER_REQUEST_CODE_02) {
            mBackgroundImageUri = contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mBackgroundImageUri)
        }

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }


    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            Snackbar.make(findViewById(android.R.id.content), "変更に成功しました", Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "変更に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
}
