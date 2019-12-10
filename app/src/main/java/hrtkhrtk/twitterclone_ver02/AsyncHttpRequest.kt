package hrtkhrtk.twitterclone_ver02

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.AsyncTask.execute
import android.util.Log
import android.widget.TextView
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.OkHttpClient
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Request
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.snackbar.Snackbar

class AsyncHttpRequest(private val mainActivity: Activity, private val token: String, private val price: Long, private val purchaseTo: Long, private val currentUID: String)// 呼び出し元のアクティビティ
    : AsyncTask<Uri.Builder, Void, String>() {

    // このメソッドは必ずオーバーライドする必要があるよ
    // ここが非同期で処理される部分みたいたぶん。
    override fun doInBackground(vararg builder: Uri.Builder): String {
        // httpリクエスト投げる処理を書く。
        val client = OkHttpClient()
        val MIMEType = MediaType.parse("application/json; charset=utf-8")
        val dataToSend_Map = HashMap<String, String>()
        dataToSend_Map["payjp-token"] = token
        dataToSend_Map["price"] = price.toString()
        dataToSend_Map["purchaseTo"] = purchaseTo.toString()
        dataToSend_Map["currentUID"] = currentUID

        val mapper = ObjectMapper() // 参考：https://qiita.com/opengl-8080/items/b613b9b3bc5d796c840c
        val dataToSend_json = mapper.writeValueAsString(dataToSend_Map)

        val url = "https://twitterclone-api-ver01.herokuapp.com/payment"

        val requestBody = RequestBody.create(MIMEType, dataToSend_json)
        val request = Request.Builder().url(url).post(requestBody).build()
        val response = client.newCall(request).execute()

        return response.body()!!.string()
    }


    // このメソッドは非同期処理の終わった後に呼び出されます
    override fun onPostExecute(result: String) {
        val mapper = jacksonObjectMapper()
        val result_Map = mapper.readValue<Map<String, String>>(result)

        if (result_Map["status"] == "success") {
            val intent = Intent(mainActivity, PurchasingCompleteActivity::class.java)
            intent.putExtra("price", result_Map["price"]!!.toInt())
            intent.putExtra("purchaseTo", result_Map["purchaseTo"]!!.toLong())
            mainActivity.startActivity(intent)
        } else {
            Snackbar.make(mainActivity.findViewById(android.R.id.content), result_Map["status"]!!, Snackbar.LENGTH_SHORT).show()
        }
    }
}
