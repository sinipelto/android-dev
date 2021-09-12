package fi.tuni.sinipelto.disqs.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.Comment
import fi.tuni.sinipelto.disqs.model.Post
import fi.tuni.sinipelto.disqs.model.User
import fi.tuni.sinipelto.disqs.model.Vote

class Database {
    companion object {

        private const val USERS_COLLECTION_NAME = "users"
        private const val POSTS_COLLECTION_NAME = "posts"
        private const val COMMENTS_COLLECTION_NAME = "comments"
        private const val VOTES_COLLECTION_NAME = "votes"

        fun getAllPosts(callback: FirebaseCallback<List<Post>>, limit: Long = 100) {
            Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)
                .orderBy(Post::deleted.name)
                .whereNotEqualTo(Post::deleted.name, true)
                .orderBy(Post::postId.name, Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener {
                    //Log.d(TAG, "Posts fetched successfully.")
                    callback.onResult(it.toObjects())
                }
                .addOnFailureListener {
                    //Log.e(TAG, "Failed to get all posts: ${it.message} => ${it.printStackTrace()}")
                    callback.onError(it)
                }
        }

        fun getUserPosts(user: User, callback: FirebaseCallback<List<Post>>, limit: Long = 100) {
            Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)
                .orderBy(Post::deleted.name)
                .whereNotEqualTo(Post::deleted.name, true)
                .whereEqualTo(Post::userId.name, user.userId)
                .orderBy(Post::postId.name, Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener {
                    //Log.d(TAG, "Posts fetched successfully.")
                    callback.onResult(it.toObjects())
                }
                .addOnFailureListener {
                    //Log.e(TAG, "Failed to get all posts: ${it.message} => ${it.printStackTrace()}")
                    callback.onError(it)
                }
        }

        fun getLatestPost(allowDeleted: Boolean, callback: FirebaseCallback<Post>) {
            val coll = Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)

            // Add query to filter out deleted posts if requested
            val query: Query? = if (!allowDeleted) {
                coll
                    .orderBy(Post::deleted.name)
                    .whereNotEqualTo(Post::deleted.name, true) // == false or null
            } else {
                null
            }

            // If no delete filter added, add query directly the collection
            query ?: coll
                .orderBy(Post::postId.name, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    callback.onResult((it.toObjects<Post>()).firstOrNull())
                }
                .addOnFailureListener {
                    callback.onError(it)
                }
        }

        fun insertPost(post: Post, callback: FirebaseCallback<Post>) {
            val postDoc =
                Firebase.firestore
                    .collection(POSTS_COLLECTION_NAME)
                    .document(post.postId.toString())

            postDoc
                .set(post)
                .addOnSuccessListener {
                    //Log.d(TAG, "Post inserted successfully.")

                    postDoc
                        .update(Post::created.name, FieldValue.serverTimestamp())
                        .addOnSuccessListener {
                            postDoc
                                .get()
                                .addOnSuccessListener {
                                    callback.onResult(it.toObject())
                                }
                                .addOnFailureListener {
                                    //Log.e(
//                                        TAG,
//                                        "Failed to get post after update: ${it.message} => ${it.printStackTrace()}"
//                                    )
                                    callback.onError(it)
                                }
                        }
                        .addOnFailureListener {
                            //Log.e(
//                                TAG,
//                                "Failed to update post server timestamp: ${it.message} => ${it.printStackTrace()}"
//                            )
                            callback.onError(it)
                        }
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to insert Post: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun removePost(post: Post, callback: FirebaseCallback<Post>) {
            post.deleted = true

            Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)
                .document(post.postId.toString())
                .set(post)
                .addOnSuccessListener {
                    callback.onResult(post)
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to remove post from DB: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun getPostComments(
            post: Post,
            callback: FirebaseCallback<List<Comment>>,
            limit: Long = 100
        ) {
            val postColl = Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)

            postColl
                .orderBy(Post::deleted.name)
                .whereNotEqualTo(Post::deleted.name, true)
                .whereEqualTo(Post::postId.name, post.postId)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    val obj = it.toObjects<Post>().firstOrNull()

                    if (obj != null && obj.deleted != true) {
                        postColl
                            .document(post.postId.toString())
                            .collection(COMMENTS_COLLECTION_NAME)
                            .orderBy(Comment::commentId.name, Query.Direction.DESCENDING)
                            .limit(limit)
                            .get()
                            .addOnSuccessListener { comments ->
                                callback.onResult(comments.toObjects())
                            }
                            .addOnFailureListener { commentError ->
                                //Log.e(
//                                    TAG,
//                                    "ERROR: Failed to fetch post comments for post ${post.postId}: ${commentError.message} => ${commentError.printStackTrace()}"
//                                )
                                callback.onError(commentError)
                            }

                    } else {
                        //Log.w(TAG, "Failed to fetch post: Post was null or deleted.")
                        callback.onResult(null)
                    }
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to fetch post: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun getPostLatestComment(post: Post, callback: FirebaseCallback<Comment>) {
            val postColl = Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)

            postColl
                .orderBy(Post::deleted.name)
                .whereNotEqualTo(Post::deleted.name, true)
                .whereEqualTo(Post::postId.name, post.postId)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    val obj = it.toObjects<Post>().firstOrNull()

                    if (obj != null && obj.deleted != true) {
                        postColl
                            .document(post.postId.toString())
                            .collection(COMMENTS_COLLECTION_NAME)
                            .orderBy(Comment::commentId.name, Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { comment ->
                                callback.onResult(comment.toObjects<Comment>().firstOrNull())
                            }
                            .addOnFailureListener { commentError ->
                                //Log.e(
//                                    TAG,
//                                    "ERROR: Failed to fetch latest comment for post ${post.postId}: ${commentError.message} => ${commentError.printStackTrace()}"
//                                )
                                callback.onError(commentError)
                            }
                    } else {
                        //Log.w(TAG, "Failed to fetch post: Post was null or deleted.")
                        callback.onResult(null)
                    }
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to fetch post: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun insertComment(comment: Comment, callback: FirebaseCallback<Comment>) {
            val collDoc = Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)
                .document(comment.postId.toString())
                .collection(COMMENTS_COLLECTION_NAME)
                .document(comment.commentId.toString())

            collDoc.set(comment)
                .addOnSuccessListener {
                    //Log.d(TAG, "Comment inserted successfully to post.")
                    collDoc
                        .update(Comment::created.name, FieldValue.serverTimestamp())
                        .addOnSuccessListener {
                            //Log.d(TAG, "Comment updated successfully.")
                            collDoc
                                .get()
                                .addOnSuccessListener {
                                    callback.onResult(it.toObject())
                                }
                                .addOnFailureListener {
                                    //Log.e(
//                                        TAG,
//                                        "Failed to fetch inserted comment: ${it.message} => ${it.printStackTrace()}"
//                                    )
                                    callback.onError(it)
                                }
                        }
                        .addOnFailureListener {
                            //Log.e(
//                                TAG,
//                                "Failed to update comment timestamp: ${it.message} => ${it.printStackTrace()}"
//                            )
                            callback.onError(it)
                        }
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to insert comment for post: ${comment.postId}: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun getPostVotes(post: Post, callback: FirebaseCallback<List<Vote>>) {
            Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)
                .document(post.postId.toString())
                .collection(VOTES_COLLECTION_NAME)
                .get()
                .addOnSuccessListener {
                    callback.onResult(it.toObjects())
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to retrieve Post Votes: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }

        }

        fun getCommentVotes(comment: Comment, callback: FirebaseCallback<List<Vote>>) {
            Firebase.firestore
                .collection(POSTS_COLLECTION_NAME)
                .document(comment.postId.toString())
                .collection(COMMENTS_COLLECTION_NAME)
                .document(comment.commentId.toString())
                .collection(VOTES_COLLECTION_NAME)
                .get()
                .addOnSuccessListener {
                    callback.onResult(it.toObjects())
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to retrieve Post Votes: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun insertPostVote(post: Post, vote: Vote, callback: FirebaseCallback<Vote>) {
            insertVoteIfNotExists(
                Firebase.firestore
                    .collection(POSTS_COLLECTION_NAME)
                    .document(post.postId.toString())
                    .collection(VOTES_COLLECTION_NAME),
                vote, callback
            )
        }

        fun insertCommentVote(comment: Comment, vote: Vote, callback: FirebaseCallback<Vote>) {
            insertVoteIfNotExists(
                Firebase.firestore
                    .collection(POSTS_COLLECTION_NAME)
                    .document(comment.postId.toString())
                    .collection(COMMENTS_COLLECTION_NAME)
                    .document(comment.commentId.toString())
                    .collection(VOTES_COLLECTION_NAME),
                vote, callback
            )
        }

        private fun insertVoteIfNotExists(
            voteColl: CollectionReference,
            vote: Vote,
            callback: FirebaseCallback<Vote>
        ) {
            voteColl
                .whereEqualTo(Vote::userId.name, vote.userId)
                .get()
                .addOnSuccessListener { snap ->
                    if (snap.isEmpty) {
                        voteColl
                            .add(vote)
                            .addOnSuccessListener { doc ->
                                //Log.d(TAG, "Vote insert OK")
                                doc
                                    .update(Vote::created.name, FieldValue.serverTimestamp())
                                    .addOnSuccessListener {
                                        //Log.d(TAG, "Vote timestamp update OK")
                                        doc
                                            .get()
                                            .addOnSuccessListener {
                                                callback.onResult(it.toObject())
                                            }
                                            .addOnFailureListener {
                                                //Log.e(
//                                                    TAG,
//                                                    "ERROR: Failed to retrieve inserted Vote: ${it.message} => ${it.printStackTrace()}"
//                                                )
                                                callback.onError(it)
                                            }
                                    }
                                    .addOnFailureListener {
                                        callback.onError(it)
                                    }
                            }
                            .addOnFailureListener {
                                //Log.e(
//                                    TAG,
//                                    "ERROR: Failed to insert Vote: ${it.message} => ${it.printStackTrace()}"
//                                )
                                callback.onError(it)
                            }
                    }
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to get existing vote for user: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun getUser(authId: String, callback: FirebaseCallback<User>) {
            Firebase.firestore
                .collection("users")
                .document(authId)
                .get()
                .addOnSuccessListener {
                    callback.onResult(it.toObject())
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to read User from DB: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun getAllUsers(callback: FirebaseCallback<List<User>>) {
            Firebase.firestore
                .collection(USERS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener {
                    //Log.d(TAG, "Get all users OK")
                    callback.onResult(it.toObjects())
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "Failed to fetch all users: ${it.message} => ${it.printStackTrace()}"
//                    )
                    callback.onError(it)
                }
        }

        fun insertUser(authId: String, user: User) {
            val userDoc = Firebase.firestore
                .collection(USERS_COLLECTION_NAME)
                .document(authId)

            userDoc
                .set(user)
                .addOnSuccessListener {
                    //Log.d(TAG, "User upsert OK")
                    userDoc
                        .update(
                            mapOf(
                                Pair(User::created.name, FieldValue.serverTimestamp()),
                                Pair(
                                    User::lastActivity.name, FieldValue.serverTimestamp()
                                )
                            )
                        )
                        .addOnSuccessListener {
                            //Log.d(TAG, "User timestamps updated OK")
                        }
                        .addOnFailureListener {
                            //Log.e(TAG, "ERROR: Failed to update user timestamps!")
                            throw it
                        }
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to insert or update user ${user.userId}: ${it.message} => ${it.printStackTrace()}"
//                    )
                    throw it
                }
        }

        fun updateUserActivity(authId: String) {
            Firebase.firestore
                .collection(USERS_COLLECTION_NAME)
                .document(authId)
                .update(User::lastActivity.name, FieldValue.serverTimestamp())
                .addOnSuccessListener {
                    //Log.d(TAG, "User activity update OK.")
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to insert or update User UID ${authId}: ${it.message} => ${it.printStackTrace()}"
//                    )
                }
        }

        fun updateUserLocation(
            authId: String,
            location: Location?,
            nearestPlaceId: String?,
            nearestPlaceLocation: LatLng?
        ) {
            if (location == null && (nearestPlaceId == null && nearestPlaceLocation == null))
                throw NullPointerException("ERROR: Both location and nearestplace are null!")

            val fields = mutableMapOf<String, Any?>()

            if (location != null) {
                fields[User::lastLocation.name] = GeoPoint(location.latitude, location.longitude)
            }

            if (nearestPlaceLocation != null) {
                fields[User::nearestPoint.name] = nearestPlaceId
                fields[User::nearestPointLocation.name] =
                    GeoPoint(nearestPlaceLocation.latitude, nearestPlaceLocation.longitude)
            }

            Firebase.firestore
                .collection(USERS_COLLECTION_NAME)
                .document(authId)
                .update(fields)
                .addOnSuccessListener {
                    //Log.d(TAG, "User Location update OK")
                }
                .addOnFailureListener {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to update location: ${it.message} => ${it.printStackTrace()}"
//                    )
                }
        }

//        private const val TAG = "disqs.Database"
    }
}