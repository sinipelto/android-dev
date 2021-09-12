package fi.tuni.sinipelto.disqs.model

import com.google.firebase.Timestamp

data class Vote(

    val userId: String? = null,

    val positive: Boolean? = null,

    val created: Timestamp? = null

)