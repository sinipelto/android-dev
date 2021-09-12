package fi.tuni.sinipelto.disqs.ui.fragment

import android.graphics.Bitmap
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
import fi.tuni.sinipelto.disqs.databinding.FragmentWorldBinding
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.AppData
import fi.tuni.sinipelto.disqs.model.Post
import fi.tuni.sinipelto.disqs.ui.adapter.PostListAdapter
import fi.tuni.sinipelto.disqs.ui.viewmodel.PageViewModel
import fi.tuni.sinipelto.disqs.util.BinaryConverter
import fi.tuni.sinipelto.disqs.util.Constants
import fi.tuni.sinipelto.disqs.util.Database
import fi.tuni.sinipelto.disqs.util.KeyboardManager
import java.util.*


/**
 * A placeholder fragment containing a simple view.
 */
class WorldFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    lateinit var mAppData: AppData

    private lateinit var pageViewModel: PageViewModel
    private lateinit var adapter: PostListAdapter

    private var _binding: FragmentWorldBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshView: SwipeRefreshLayout
    private lateinit var emptyText: TextView
    private lateinit var textInput: EditText
    private lateinit var sendButton: Button
    private lateinit var photoButton: Button

    private var storedImage: Bitmap? = null
    private var imgRotation: Float? = null

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

        _binding = FragmentWorldBinding.inflate(inflater, container, false)
        val root = binding.root

        if (!this::mAppData.isInitialized) return root

        // Load stored view(s) for the fragment
        refreshView = root.findViewById(R.id.postPullToRefresh)
        recyclerView = root.findViewById(R.id.postRecyclerView)
        emptyText = root.findViewById(R.id.emptyPostText)
        textInput = root.findViewById(R.id.addPostEditText)
        photoButton = root.findViewById(R.id.attachButton)
        sendButton = root.findViewById(R.id.sendPostButton)

        // Initialize view(s)
        refreshView.setOnRefreshListener(this)

        photoButton.setOnClickListener {
            // open camera view
            try {
                val frag = CameraFragment.newInstance(this)
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainLayout, frag, "cameraFrag")
                    .addToBackStack(null)
                    .commit()

            } catch (exc: Exception) {
                //Log.e(TAG, "Failed to start CameraFragment", exc)
            }
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

            val image = storedImage
            val rotation = imgRotation

            // Huge try-catch to handle UI controls on any exception
            try {
                // Determine ID for the post from the latest post stored in DB right now
                Database.getLatestPost(true, object : FirebaseCallback<Post> {
                    override fun onResult(result: Post?) {
                        val id: Int = if (result?.postId == null) {
                            //Log.w(TAG, "Failed to determine ID for Post from DB")
                            1
                        } else {
                            result.postId.plus(1)
                        }

                        // create post object
                        // if image property not null -> set post.image = propertyvalue
                        val newPost = Post(
                            id,
                            mAppData.currentUser.userId!!,
                            text,
                            image,
                            if (image != null) BinaryConverter.bitmapToBase64(image) else null,
                            rotation,
                            null, // Time comes from sever (Firestore)!
                            false
                        )

                        // send object to viewmodel to be stored & added to db
                        try {
                            Database.insertPost(newPost, object : FirebaseCallback<Post> {
                                override fun onResult(result: Post?) {
                                    if (result == null) throw NullPointerException("ERROR: Resulted Post was null!")
                                    else {
                                        // Load image bitmap into RAM if image exists
                                        if (result.imageData != null) {
                                            result.image =
                                                BinaryConverter.base64ToBitmap(result.imageData)
                                        }

                                        // Insert the post into the view
                                        pageViewModel.insertPost(result)

                                        // if posting succeded (and db add ok)
                                        // notify adapter that item was added
                                        adapter.notifyDataSetChanged()
                                        recyclerView.scrollToPosition(0)

                                        setUiState(true)
                                    }
                                }

                                override fun onError(t: Throwable) {
                                    throw t
                                }
                            })
                        } catch (t: Throwable) {
                            // Cannot proceed if post insertion failed
                            //Log.e(
//                                TAG,
//                                "ERROR: Failed to insert new post: ${t.message} => ${t.printStackTrace()}"
//                            )
                            throw t
                        }
                    }

                    override fun onError(t: Throwable) {
                        // Cannot proceed if post insertion failed
                        //Log.e(TAG, "ERROR: Failed to fetch latest post! Fallback to null value..")
                        onResult(null)
                    }
                })

                // clear text field
                textInput.text.clear()

                // set image property null (clean old image from memory)
                storedImage = null

                // Hide the keyboard after send
                KeyboardManager.hideKeyboard(requireActivity())

            } catch (t: Throwable) {
                //Log.d(
//                    TAG,
//                    "Exception occurred during post insertion: ${t.message} => ${t.printStackTrace()}"
//                )

                Snackbar.make(
                    requireContext(),
                    requireView(),
                    getString(R.string.create_post_failed),
                    Snackbar.LENGTH_LONG
                ).show()

                //Log.d(TAG, "Releasing UI..")
                setUiState(true)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()

        pageViewModel.posts.observe(viewLifecycleOwner, {
            adapter = PostListAdapter(pageViewModel, this, mAppData)
            recyclerView.adapter = adapter
        })

        refreshPosts()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRefresh() {
        if (!canRefresh) return

        try {
            refreshView.isRefreshing = true
            refreshPosts()
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
        private const val TAG = "disqs.WorldFragment"

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
        fun newInstance(sectionNumber: Int, appData: AppData): WorldFragment {
            return WorldFragment().apply {
                mAppData = appData
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private fun refreshPosts() {
        emptyText.text = getString(R.string.posts_load_text)
        emptyText.visibility = View.VISIBLE

        Database.getAllPosts(object : FirebaseCallback<List<Post>> {
            override fun onResult(result: List<Post>?) {
                if (result == null) {
                    //Log.e(TAG, "ERROR: RefreshPosts: Received null result value.")
                    pageViewModel.loadPosts(listOf())
                    adapter.notifyDataSetChanged()
                } else {
                    if (checkPostsEmpty(result)) {
                        //Log.w(TAG, "WARN: Posts list was empty.")
                        pageViewModel.loadPosts(listOf())
                        adapter.notifyDataSetChanged()
                    } else {
                        pageViewModel.loadPosts(result.map {
                            if (it.imageData != null) {
                                it.image = BinaryConverter.base64ToBitmap(it.imageData)
                            }
                            it
                        }.sortedByDescending { it.postId })
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onError(t: Throwable) {
                Log.e(TAG, "ERROR: Failed to refresh posts: ${t.message} => ${t.printStackTrace()}")
            }
        })
    }

    private fun checkPostsEmpty(posts: List<Post>? = null): Boolean {
        if (posts == null) {
            Database.getLatestPost(false, object : FirebaseCallback<Post> {
                override fun onResult(result: Post?) {
                    if (result != null) {
                        setEmptyText(false)
                    } else {
                        setEmptyText(true)
                    }
                }

                override fun onError(t: Throwable) {
                    //Log.e(
//                        TAG,
//                        "ERROR: Failed to fetch latest post for emptiness check: ${t.message} => ${t.printStackTrace()}"
//                    )
                    setEmptyText(true)
                }
            })
        } else {
            return if (posts.count() > 0) {
                setEmptyText(false)
                false
            } else {
                setEmptyText(true)
                true
            }
        }

        return false
    }

    private fun setUiState(active: Boolean) {
        canRefresh = active
        photoButton.isClickable = active
        textInput.isClickable = active
        sendButton.isClickable = active
    }

    fun setStoredImage(image: Bitmap, rotation: Float) {
        storedImage = image
        imgRotation = rotation
    }

    fun setEmptyText(visible: Boolean) {
        if (visible) {
            emptyText.text = getString(R.string.no_world_posts_text)
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.GONE
        }
    }
}
