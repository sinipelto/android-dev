package fi.tuni.sinipelto.disqs.util

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.gson.GsonBuilder
import fi.tuni.sinipelto.disqs.model.User
import java.util.*

class UserManager {
    companion object {

        fun getLocalUser(firebaseUser: FirebaseUser, context: Context, userFile: String): User {
            // Read user from app data file or create new user if local user file does not exist
            val user = ensureLocalUserCreated(firebaseUser, context, userFile)
            if (user != null) return user

            // convert from JSON to object
            val json = FileHandler.readAppFile(context, userFile)
            val gson = GsonBuilder().create()
            return gson.fromJson(json, User::class.java)
        }

        fun updateLocalUser(
            firebaseUser: FirebaseUser,
            user: User,
            context: Context,
            userFile: String
        ) {
            fillUserInfo(firebaseUser, user)

            val gson = GsonBuilder().create()
            val json = gson.toJson(user)

            FileHandler.writeAppFile(context, userFile, json)
        }

        private fun ensureLocalUserCreated(
            firebaseUser: FirebaseUser,
            context: Context,
            userFile: String
        ): User? {
            // Check if user already exists
            // If does, skip creating new user
            if (FileHandler.fileExists(context, userFile)) return null

            val user = User(
                UUID.randomUUID().toString()
            )

            // Fill firebase details into the user object
            fillUserInfo(firebaseUser, user)

            val gson = GsonBuilder().create()
            val json = gson.toJson(user)

            FileHandler.writeAppFile(context, userFile, json)

            return user
        }

        fun fillUserInfo(firebaseUser: FirebaseUser, user: User) {
            user.anonymous = firebaseUser.isAnonymous
            user.displayName = firebaseUser.displayName
            user.email = firebaseUser.email
            user.phone = firebaseUser.phoneNumber
            user.firebaseUid = firebaseUser.uid
            user.providerId = firebaseUser.providerId
            user.tenantId = firebaseUser.tenantId
        }

//        private const val TAG = "disqs.UserManager"
    }
}