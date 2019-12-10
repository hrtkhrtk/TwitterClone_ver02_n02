//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import java.text.SimpleDateFormat
import java.util.*

const val USER_ID_FOR_PendingIntent = "hrtkhrtk.twitterclone_ver02.user_id"
const val REQUEST_CODE_FOR_PendingIntent = 0 // 数字に意味はない // 参考：https://developer.android.com/reference/android/app/PendingIntent.html#getBroadcast(android.content.Context,%20int,%20android.content.Intent,%20int)

//const val NameKEY = "name"          // Preferenceに表示名を保存する時のキー

const val id__nav_posts = 0
const val id__nav_search_posts = 1
const val id__nav_search_users = 2
const val id__nav_followings_list = 3
const val id__nav_followers_list = 4
const val id__nav_favorites_list = 5
const val id__nav_my_posts = 6
const val id__nav_policy = 7

fun getDateTime(data_Long: Long, pattern: String = "yyyy/MM/dd HH:mm:ss"): String? {
    // 参考：Lesson3「引数にはデフォルト値を指定することができます」
    // 参考：https://qiita.com/emboss369/items/5a3ddea301cbf79d971a
    // 参考：https://stackoverflow.com/questions/47250263/kotlin-convert-timestamp-to-datetime
    try {
        val sdf = SimpleDateFormat(pattern)
        val netDate = Date(data_Long)
        return sdf.format(netDate)
    } catch (e: Exception) {
        return e.toString()
    }
}
