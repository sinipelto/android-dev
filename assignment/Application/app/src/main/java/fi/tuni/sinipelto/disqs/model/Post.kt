package fi.tuni.sinipelto.disqs.model

import android.graphics.Bitmap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Post(

    val postId: Int? = null,

    val userId: String? = null,

    val content: String? = null,

    @get:Exclude @set:Exclude var image: Bitmap? = null, // EXCLUDE stored bitmap from firestore!

    val imageData: String? = null, // Can be null (no photo)

    val imageRotation: Float? = null, // null if no photo attached

    val created: Timestamp? = null,

    var deleted: Boolean? = null

)