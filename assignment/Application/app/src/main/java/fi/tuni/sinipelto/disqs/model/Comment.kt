package fi.tuni.sinipelto.disqs.model

import com.google.firebase.Timestamp

data class Comment(

    val commentId: Int? = null,

    val postId: Int? = null,

    val userId: String? = null,

    val content: String? = null,

    val created: Timestamp? = null

)