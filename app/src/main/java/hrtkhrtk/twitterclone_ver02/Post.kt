package hrtkhrtk.twitterclone_ver02

import java.io.Serializable

class Post(
    val bytes: ByteArray,
    val nickname: String,
    val text: String,
    val createdAt: Long,
    val favoritersList: ArrayList<String>,
    val userId: String,
    val postId: String) : Serializable {
}