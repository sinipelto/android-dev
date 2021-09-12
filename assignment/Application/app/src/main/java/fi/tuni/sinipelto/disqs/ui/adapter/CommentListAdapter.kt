package fi.tuni.sinipelto.disqs.ui.adapter

import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.AppData
import fi.tuni.sinipelto.disqs.model.Vote
import fi.tuni.sinipelto.disqs.ui.viewholder.RecyclerViewHolder
import fi.tuni.sinipelto.disqs.ui.viewmodel.PageViewModel
import fi.tuni.sinipelto.disqs.util.ButtonManager
import fi.tuni.sinipelto.disqs.util.Database
import java.util.*

class CommentListAdapter(private val viewModel: PageViewModel, private val appData: AppData) :
    RecyclerView.Adapter<RecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_comment, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val comment = viewModel.comments.value?.getOrNull(position) ?: return

        val contentView = holder.itemView.findViewById<TextView>(R.id.commentRecyclerItemText)
        val postedView = holder.itemView.findViewById<TextView>(R.id.commentPostedValue)
        val upvoteButton = holder.itemView.findViewById<Button>(R.id.commentUpvoteButton)
        val downvoteButton = holder.itemView.findViewById<Button>(R.id.commentDownvoteButton)
        val votesCount = holder.itemView.findViewById<TextView>(R.id.commentVoteScoreText)

        contentView.text = comment.content
        postedView.text = DateFormat.format("d.M.yyyy HH:mm:ss", comment.created!!.toDate())

        ButtonManager.setButtonState(upvoteButton, false)
        ButtonManager.setButtonState(downvoteButton, false)

        Database.getCommentVotes(comment, object : FirebaseCallback<List<Vote>> {
            override fun onResult(result: List<Vote>?) {
                if (result != null) {
                    votesCount.text =
                        result.map { if (it.positive!!) return@map 1 else return@map -1 }.sum()
                            .toString()

                    // If current user already voted, disable vote buttons
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
                                null
                            )

                            Database.insertCommentVote(
                                comment,
                                vote,
                                object : FirebaseCallback<Vote> {
                                    override fun onResult(result: Vote?) {}

                                    override fun onError(t: Throwable) {
                                        Log.e(
                                            TAG,
                                            "ERROR: Failed to add vote: ${t.message} => ${t.printStackTrace()}"
                                        )
                                        Snackbar.make(
                                            it.context,
                                            it.rootView,
                                            it.context.getString(
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
                                null
                            )

                            Database.insertCommentVote(
                                comment,
                                vote,
                                object : FirebaseCallback<Vote> {
                                    override fun onResult(result: Vote?) {}

                                    override fun onError(t: Throwable) {
                                        Log.e(
                                            TAG,
                                            "ERROR: Failed to add vote: ${t.message} => ${t.printStackTrace()}"
                                        )
                                        Snackbar.make(
                                            it.context,
                                            it.rootView,
                                            it.context.getString(
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
                    throw NullPointerException("ERROR: Result was null!")
                }
            }

            override fun onError(t: Throwable) {
                Log.e(TAG, "Failed to read votes from DB: ${t.message} => ${t.printStackTrace()}")
                onResult(listOf())
            }
        })
    }

    override fun getItemCount(): Int {
        return viewModel.comments.value?.size ?: 0
    }

    companion object {
        private const val TAG = "disqs.CommentListAdapter"
    }
}