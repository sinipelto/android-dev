package fi.tuni.sinipelto.disqs.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class User(

    var userId: String? = null, // Local generated user ID -> provide compability after local-cloud migration

    var firebaseUid: String? = null,

    var providerId: String? = null,

    var tenantId: String? = null,

    var displayName: String? = null,

    var email: String? = null,

    var phone: String? = null,

    var anonymous: Boolean? = null,

    var lastLocation: GeoPoint? = null,

    var nearestPoint: String? = null,

    var nearestPointLocation: GeoPoint? = null,

    var created: Timestamp? = null,

    var lastActivity: Timestamp? = null

)