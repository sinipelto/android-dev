package fi.tuni.sinipelto.disqs.ui.adapter

import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.AppData
import fi.tuni.sinipelto.disqs.model.Post
import fi.tuni.sinipelto.disqs.model.Vote
import fi.tuni.sinipelto.disqs.ui.activity.MainActivity
import fi.tuni.sinipelto.disqs.ui.fragment.CommentFragment
import fi.tuni.sinipelto.disqs.ui.fragment.PhotoFragment
import fi.tuni.sinipelto.disqs.ui.viewholder.RecyclerViewHolder
import fi.tuni.sinipelto.disqs.ui.viewmodel.PageViewModel
import fi.tuni.sinipelto.disqs.util.ButtonManager
import fi.tuni.sinipelto.disqs.util.Database
import java.util.*

class PostListAdapter(
    private val viewModel: PageViewModel,
    private val parentFrag: Fragment,
    private val appData: AppData,
) :
    RecyclerView.Adapter<RecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_post, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val post = viewModel.posts.value?.get(position) ?: return

        //Log.d(TAG, "OnBind called - Post: ${post.postId}")

        val contentView = holder.itemView.findViewById<TextView>(R.id.postRecyclerItemText)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.postRecyclerItemImage)
        val postedView = holder.itemView.findViewById<TextView>(R.id.postPostedValue)
        val commentButton = holder.itemView.findViewById<TextView>(R.id.commentButton)
        val deleteButton = holder.itemView.findViewById<TextView>(R.id.deleteButton)
        val upvoteButton = holder.itemView.findViewById<Button>(R.id.upvoteButton)
        val downvoteButton = holder.itemView.findViewById<Button>(R.id.downvoteButton)
        val votesCount = holder.itemView.findViewById<TextView>(R.id.voteScoreText)
        val separator = holder.itemView.findViewById<View>(R.id.postSeparator)

        // Current user does not own the post
        if (appData.currentUser.userId != post.userId) {
            deleteButton.isClickable = false
            deleteButton.visibility = View.INVISIBLE
        } else {
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                //Log.d(TAG, "POST DELETE BUTTON CLICKED")

                deleteButton.isClickable = false // Prevent multiple clicks

                Database.removePost(post, object : FirebaseCallback<Post> {
                    override fun onResult(result: Post?) {
                        if (result == null) throw NullPointerException("Deleted post result was null!")

                        viewModel.deletePost(result)

                        // Notify parent (recyclerview)
                        notifyDataSetChanged()

                        val posts = viewModel.posts.value!!

                        // If no posts left, display empty posts message
                        when {
                            posts.count() <= 0 -> {
                                (it.context as MainActivity).findViewById<TextView>(R.id.emptyPostText).visibility =
                                    View.VISIBLE
                            }
                            posts.filter { itpost -> itpost.userId == appData.currentUser.userId }
                                .count() <= 0 -> {
                                (it.context as MainActivity).findViewById<TextView>(R.id.ownPostsEmptyText).visibility =
                                    View.VISIBLE
                            }
                            else -> {
                                (it.context as MainActivity).findViewById<TextView>(R.id.emptyPostText).visibility =
                                    View.GONE
                                (it.context as MainActivity).findViewById<TextView>(R.id.ownPostsEmptyText).visibility =
                                    View.GONE
                            }
                        }
                    }

                    override fun onError(t: Throwable) {
                        Log.e(
                            TAG,
                            "ERROR: Failed to remove post: ${t.message} => ${t.printStackTrace()}"
                        )
                    }
                })
            }
        }

        // Initially disable voting buttons, until databse fetch completes and we know for sure
        ButtonManager.setButtonState(upvoteButton, false)
        ButtonManager.setButtonState(downvoteButton, false)

        // Fetch the votes and determine if voting allowed
        Database.getPostVotes(post, object : FirebaseCallback<List<Vote>> {
            override fun onResult(result: List<Vote>?) {
                if (result != null) {
                    votesCount.text =
                        result.map { if (it.positive!!) return@map 1 else return@map -1 }.sum()
                            .toString()

                    val ownVote = result.firstOrNull { it.userId == appData.currentUser.userId }

                    if (ownVote != null) {
                        ButtonManager.setButtonState(upvoteButton, false)
                        ButtonManager.setButtonState(downvoteButton, false)
                    } else {
                        ButtonManager.setButtonState(upvoteButton, true)
                        ButtonManager.setButtonState(downvoteButton, true)

                        upvoteButton.setOnClickListener {
                            ButtonManager.setButtonState(upvoteButton, false)
                            ButtonManager.setButtonState(downvoteButton, false)

                            val vote = Vote(
                                appData.currentUser.userId!!,
                                true,
                                null,
                            )

                            Database.insertPostVote(post, vote, object : FirebaseCallback<Vote> {
                                override fun onResult(result: Vote?) {}

                                override fun onError(t: Throwable) {
                                    Log.e(
                                        TAG,
                                        "ERROR: Failed to add vote: ${t.message} => ${t.printStackTrace()}"
                                    )
                                    Snackbar.make(
                                        parentFrag.requireContext(),
                                        parentFrag.requireView(),
                                        parentFrag.requireContext().getString(
                                            R.string.add_vote_failed
                                        ),
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            })

                            votesCount.text = (votesCount.text.toString().toInt() + 1).toString()
                        }

                        downvoteButton.setOnClickListener {
                            ButtonManager.setButtonState(upvoteButton, false)
                            ButtonManager.setButtonState(downvoteButton, false)

                            val vote = Vote(
                                appData.currentUser.userId!!,
                                false,
                                null,
                            )

                            Database.insertPostVote(post, vote, object : FirebaseCallback<Vote> {
                                override fun onResult(result: Vote?) {}

                                override fun onError(t: Throwable) {
                                    Log.e(
                                        TAG,
                                        "ERROR: Failed to add downvote: ${t.message} => ${t.printStackTrace()}"
                                    )
                                    Snackbar.make(
                                        parentFrag.requireContext(),
                                        parentFrag.requireView(),
                                        parentFrag.requireContext().getString(
                                            R.string.add_vote_failed
                                        ),
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            })

                            votesCount.text = (votesCount.text.toString().toInt() - 1).toString()
                        }
                    }
                } else {
                    throw NullPointerException("ERROR: Votes result was null")
                }
            }

            override fun onError(t: Throwable) {
                Log.e(TAG, "Failed to read votes from DB: ${t.message} => ${t.printStackTrace()}")
            }
        })

        val insideComment: Boolean = try {
            parentFrag as CommentFragment
            //Log.d(TAG, "CommentFragment cast success -> insideComment = true")
            true
        } catch (t: Throwable) {
            //Log.d(
//                TAG,
//                "Failed to cast parent fragment into CommentFragment -> not inside comment view!"
//            )
            false
        }

        if (insideComment) {
            separator.visibility = View.GONE
            commentButton.visibility = View.INVISIBLE
        } else {
            separator.visibility = View.VISIBLE
            commentButton.visibility = View.VISIBLE
            commentButton.setOnClickListener {
                //Log.d(TAG, "COMMENT BUTTON CLICKED")

                val frag = CommentFragment.newInstance(0, parentFrag, appData, post)

                parentFrag.parentFragmentManager
                    .beginTransaction()
                    .hide(parentFrag)
                    .add(R.id.mainLayout, frag, "commentFrag")
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }

        if (post.image == null) {
            imageView.visibility = View.GONE
        } else {
            val image = post.image!!
            val rotation = post.imageRotation

            imageView.setImageBitmap(image)
            imageView.visibility = View.VISIBLE

            if (rotation != null) {
                imageView.rotation = rotation
            }

            imageView.setOnClickListener {
                val frag =
                    PhotoFragment.newInstance(parentFrag, image, rotation ?: 0F, !insideComment)

                parentFrag.parentFragmentManager
                    .beginTransaction()
                    .hide(parentFrag)
                    .add(R.id.mainLayout, frag, "photoFrag")
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }

        contentView.text = post.content
        postedView.text = DateFormat.format("d.M.yyyy HH:mm:ss", post.created?.toDate() ?: Date(0))
    }

    override fun getItemCount(): Int {
        return viewModel.posts.value?.size ?: 0
    }

    companion object {
        private const val TAG = "disqs.PostListAdapter"
    }
}