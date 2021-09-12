package fi.tuni.sinipelto.disqs.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.databinding.FragmentCommentBinding
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.AppData
import fi.tuni.sinipelto.disqs.model.Comment
import fi.tuni.sinipelto.disqs.model.Post
import fi.tuni.sinipelto.disqs.ui.activity.MainActivity
import fi.tuni.sinipelto.disqs.ui.adapter.CommentListAdapter
import fi.tuni.sinipelto.disqs.ui.adapter.PostListAdapter
import fi.tuni.sinipelto.disqs.ui.viewmodel.PageViewModel
import fi.tuni.sinipelto.disqs.util.Constants
import fi.tuni.sinipelto.disqs.util.Database
import fi.tuni.sinipelto.disqs.util.KeyboardManager

/**
 * A placeholder fragment containing a simple view.
 */
class CommentFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    lateinit var mParentFrag: Fragment
    lateinit var mAppData: AppData
    lateinit var mPost: Post

    private lateinit var pageViewModel: PageViewModel
    private lateinit var commentAdapter: CommentListAdapter
    private lateinit var postAdapter: PostListAdapter

    private var _binding: FragmentCommentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var postRecyclerView: RecyclerView

    private lateinit var refreshView: SwipeRefreshLayout

    private lateinit var emptyText: TextView

    private lateinit var textInput: EditText

    private lateinit var sendButton: Button
    private lateinit var backButton: Button

    private var canRefresh: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        val root = binding.root

        if (!this::mPost.isInitialized) return root

        (activity as? MainActivity)?.tabLayout?.visibility = View.GONE

        // Load stored view(s) for the fragment
        emptyText = root.findViewById(R.id.emptyCommentText)
        textInput = root.findViewById(R.id.addCommentEditText)
        sendButton = root.findViewById(R.id.sendCommentButton)
        backButton = root.findViewById(R.id.commentBackButton)

        // Initialize view(s)
        refreshView = root.findViewById(R.id.commentPullToRefresh)
        refreshView.setOnRefreshListener(this)

        postRecyclerView = root.findViewById(R.id.commentedPostRecyclerView)
        postRecyclerView.layoutManager = LinearLayoutManager(context)
        postRecyclerView.itemAnimator = DefaultItemAnimator()

        pageViewModel.posts.observe(viewLifecycleOwner, {
            postAdapter = PostListAdapter(pageViewModel, this, mAppData)
            postRecyclerView.adapter = postAdapter
        })

        commentRecyclerView = root.findViewById(R.id.commentRecyclerView)
        commentRecyclerView.layoutManager = LinearLayoutManager(context)
        commentRecyclerView.itemAnimator = DefaultItemAnimator()

        pageViewModel.comments.observe(viewLifecycleOwner, {
            commentAdapter = CommentListAdapter(pageViewModel, mAppData)
            commentRecyclerView.adapter = commentAdapter
        })

        backButton.setOnClickListener {
            exitFragment()
        }

        sendButton.setOnClickListener {
            // Check edittext not empty
            if (textInput.text.isNullOrEmpty()) {
                Snackbar.make(
                    requireContext(),
                    requireView(),
                    getString(R.string.empty_text_input_error),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Filter and process inserted text
            var text = textInput.text.toString()

            while (text.contains("\n\n")) {
                text = text.replace("\n\n", "\n")
            }

            // Ensure input length under constraints
            if (text.length > Constants.INPUT_MAX_LENGTH) {
                Snackbar.make(
                    requireContext(),
                    requireView(),
                    getString(R.string.text_too_long),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            setUiState(false)

            try {
                Database.getPostLatestComment(mPost, object : FirebaseCallback<Comment> {
                    override fun onResult(result: Comment?) {
                        // determine comment ID
                        val maxId = result?.commentId

                        val id: Int = if (maxId == null) {
                            Log.w(TAG, "Failed to determine ID for Comment from DB!")
                            1
                        } else {
                            maxId.plus(1)
                        }

                        //  create object
                        val newObj = Comment(
                            id,
                            mPost.postId!!,
                            mAppData.currentUser.userId!!,
                            text,
                            null,
                        )

                        //  send object to viewmodel to be stored & added to db
                        try {
                            Database.insertComment(
                                newObj,
                                object : FirebaseCallback<Comment> {
                                    override fun onResult(result: Comment?) {
                                        if (result == null) throw NullPointerException("Inserted comment was null!")

                                        pageViewModel.insertComment(result)

                                        // if posting succeded (and db add ok)
                                        //  notify adapter that item was added
                                        commentAdapter.notifyDataSetChanged()
                                        commentRecyclerView.scrollToPosition(0)

                                        setUiState(true)
                                    }

                                    override fun onError(t: Throwable) {
                                        Log.e(TAG, "Failed to insert comment for post")
                                        throw t
                                    }

                                })
                        } catch (t: Throwable) {
                            Log.e(
                                TAG,
                                "ERROR: Failed to insert new comment: ${t.message} => ${t.printStackTrace()}"
                            )
                            throw t // Cannot proceed if insertion failed
                        }
                    }

                    override fun onError(t: Throwable) {
                        Log.e(
                            TAG,
                            "ERROR: Failed to fetch post latest comment: ${t.message} => ${t.printStackTrace()}"
                        )
                        //Log.d(TAG, "Passing null value onResult..")
                        onResult(null)
                    }
                })

                // reset edit text field
                textInput.text.clear()

                // Finally, hide the keyboard
                KeyboardManager.hideKeyboard(requireActivity())

            } catch (t: Throwable) {
                Log.e(
                    TAG,
                    "ERROR: Failed to insert new comment: ${t.message} => ${t.printStackTrace()}"
                )

                Snackbar.make(
                    requireContext(),
                    requireView(),
                    getString(R.string.create_comment_failed),
                    Snackbar.LENGTH_LONG
                ).show()

                //Log.d(TAG, "Restoring UI..")
                setUiState(true)
            }
        }

        refreshComments()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.tabLayout?.visibility = View.VISIBLE
        _binding = null
    }

    override fun onRefresh() {
        try {
            refreshView.isRefreshing = true
            refreshComments()
            refreshView.isRefreshing = false
        } catch (t: Throwable) {
            refreshView.isRefreshing = false
            Snackbar.make(
                requireContext(),
                requireView(),
                getString(R.string.posts_refresh_failed),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val TAG = "disqs.OwnPostsFragment"

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(
            sectionNumber: Int,
            parentFrag: Fragment,
            appData: AppData,
            post: Post
        ): CommentFragment {
            return CommentFragment().apply {
                mParentFrag = parentFrag
                mAppData = appData
                mPost = post
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private fun setUiState(active: Boolean) {
        canRefresh = active
        textInput.isClickable = active
        sendButton.isClickable = active
    }

    private fun exitFragment() {
        if (!this::mParentFrag.isInitialized) return

        // Do not add back-stack (irreversible operation!)
        parentFragmentManager
            .beginTransaction()
            .remove(this)
            .show(mParentFrag)
            .disallowAddToBackStack()
            .commitAllowingStateLoss()
    }

    private fun refreshComments() {
        if (!this::mPost.isInitialized) return

        // Ensure the post still exists in DB
        Database.getPostComments(mPost, object : FirebaseCallback<List<Comment>> {
            override fun onResult(result: List<Comment>?) {
                if (result != null) {
                    pageViewModel.loadPosts(listOf(mPost))
                    pageViewModel.loadComments(result)
                    postAdapter.notifyDataSetChanged()
                    commentAdapter.notifyDataSetChanged()
                    checkCommentsEmpty(result)
                } else {
                    Snackbar.make(
                        requireContext(),
                        requireView(),
                        getString(R.string.post_not_available),
                        Snackbar.LENGTH_LONG
                    ).show()
                    exitFragment()
                }
            }

            override fun onError(t: Throwable) {
                Log.e(
                    TAG,
                    "ERROR: Failed to fetch post for comments: ${t.message} => ${t.printStackTrace()}"
                )
                onResult(null)
            }
        })
    }

    private fun checkCommentsEmpty(items: List<Comment>? = null) {
        if (!this::mPost.isInitialized) return

        if (items == null) {
            Database.getPostComments(mPost, object : FirebaseCallback<List<Comment>> {
                override fun onResult(result: List<Comment>?) {
                    if (result != null) {
                        if (result.count() > 0) {
                            emptyText.visibility = View.GONE
                        } else {
                            emptyText.visibility = View.VISIBLE
                        }
                    } else {
                        Snackbar.make(
                            requireContext(),
                            requireView(),
                            getString(R.string.post_not_available),
                            Snackbar.LENGTH_LONG
                        ).show()
                        exitFragment()
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e(
                        TAG,
                        "ERROR: Failed to fetch post for comments: ${t.message} => ${t.printStackTrace()}"
                    )
                    onResult(null)
                }
            })

        } else {
            return if (items.count() > 0) {
                emptyText.visibility = View.GONE
            } else {
                emptyText.visibility = View.VISIBLE
            }
        }
    }
}