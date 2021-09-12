package fi.tuni.sinipelto.disqs.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.databinding.FragmentOwnpostsBinding
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.AppData
import fi.tuni.sinipelto.disqs.model.Post
import fi.tuni.sinipelto.disqs.ui.adapter.PostListAdapter
import fi.tuni.sinipelto.disqs.ui.viewmodel.PageViewModel
import fi.tuni.sinipelto.disqs.util.BinaryConverter
import fi.tuni.sinipelto.disqs.util.Database

/**
 * A placeholder fragment containing a simple view.
 */
class OwnpostsFragment : Fragment() {

    lateinit var mAppData: AppData

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentOwnpostsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: PostListAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshView: SwipeRefreshLayout
    private lateinit var emptyText: TextView

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
        _binding = FragmentOwnpostsBinding.inflate(inflater, container, false)
        val root = binding.root

        if (!this::mAppData.isInitialized) return root

        // Load stored view(s) for the fragment
        refreshView = root.findViewById(R.id.ownPostPullToRefresh)
        recyclerView = root.findViewById(R.id.ownPostRecyclerView)
        emptyText = root.findViewById(R.id.ownPostsEmptyText)

        // Initialize view(s)
        refreshView.setOnRefreshListener {
            try {
                refreshView.isRefreshing = true
                refreshPosts()
                refreshView.isRefreshing = false
            } catch (e: Exception) {
                refreshView.isRefreshing = false
                Snackbar.make(
                    requireContext(),
                    requireView(),
                    getString(R.string.posts_refresh_failed),
                    Snackbar.LENGTH_LONG
                ).show()
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
        fun newInstance(sectionNumber: Int, appData: AppData): OwnpostsFragment {
            return OwnpostsFragment().apply {
                mAppData = appData
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private fun refreshPosts() {
        if (!this::mAppData.isInitialized) return

        emptyText.text = getString(R.string.posts_load_text)
        emptyText.visibility = View.VISIBLE

        Database.getUserPosts(mAppData.currentUser, object : FirebaseCallback<List<Post>> {
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
        if (!this::mAppData.isInitialized) return true

        if (posts == null) {
            Database.getUserPosts(mAppData.currentUser, object : FirebaseCallback<List<Post>> {
                override fun onResult(result: List<Post>?) {
                    if (!result.isNullOrEmpty()) {
                        setEmptyText(false)
                    } else {
                        setEmptyText(true)
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e(
                        TAG,
                        "ERROR: Failed to fetch user posts for emptiness check: ${t.message} => ${t.printStackTrace()}"
                    )
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

    fun setEmptyText(visible: Boolean) {
        if (visible) {
            emptyText.text = getString(R.string.no_own_posts_text)
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.GONE
        }
    }
}