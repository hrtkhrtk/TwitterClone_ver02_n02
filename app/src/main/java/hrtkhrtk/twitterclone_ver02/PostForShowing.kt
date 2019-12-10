//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import java.io.Serializable

class PostForShowing(
    val bytes: ByteArray,
    val nickname: String,
    val text: String,
    val createdAt: Long,
    val favoritersList: ArrayList<String>,
    val userId: String,
    val postId: String,
    val positionInArrayList: Int) : Serializable {

    val iconImage: ByteArray

    init {
        iconImage = bytes.clone()
    }
}