package fi.tuni.sinipelto.disqs.model

import com.google.firebase.auth.FirebaseUser

data class AppData(

    // Locally stored separate user object, synced with DB users collection
    val currentUser: User,

    // Firebase Auth Unique User -> not used until user logs in non-Anonymously
    // (currentuser.userid for local storage, used with linking objects to users)
    val firebaseUser: FirebaseUser
)