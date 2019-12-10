package hrtkhrtk.twitterclone_ver02

import android.app.Activity
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.TextView
import okhttp3.OkHttpClient
import okhttp3.Request
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class AsyncHttpRequestForWeatherInformation(private val mainActivity: Activity) : AsyncTask<Uri.Builder, Void, String>() {

    // このメソッドは必ずオーバーライドする必要があるよ
    // ここが非同期で処理される部分みたいたぶん。
    override fun doInBackground(vararg builder: Uri.Builder): String {
        val url = "http://weather.livedoor.com/forecast/webservice/json/v1?city=270000" // 270000は大阪

        // httpリクエスト投げる処理を書く。
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        return response.body()!!.string()
    }

    // このメソッドは非同期処理の終わった後に呼び出されます
    override fun onPostExecute(result: String) {
        val mapper = jacksonObjectMapper()
        val result_Map = mapper.readValue<Map<String, Any>>(result)
        val forecasts = result_Map["forecasts"] as ArrayList<Any>
        val todayForcasts = forecasts[0] as Map<String, String> // 今日の天気は1番目の要素（たぶん）

        val cityTextView = mainActivity.findViewById<View>(R.id.cityTextView) as TextView
        val weatherTextView = mainActivity.findViewById<View>(R.id.weatherTextView) as TextView
        cityTextView.setText("大阪")
        weatherTextView.setText(todayForcasts["telop"])
    }
}