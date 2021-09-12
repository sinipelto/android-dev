package fi.tuni.sinipelto.disqs.ui.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import fi.tuni.sinipelto.disqs.databinding.FragmentPhotoBinding
import fi.tuni.sinipelto.disqs.ui.activity.MainActivity

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@Suppress("DEPRECATION")
class PhotoFragment : Fragment() {

    lateinit var mParentFrag: Fragment
    lateinit var mBitmap: Bitmap
    var mRotation: Float = 0F
    var mRestoreTabs: Boolean = true

    private val hideHandler = Handler()

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }

    private var visible: Boolean = true

    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentPhotoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var exitButton: Button? = null
    private var photoView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!this::mBitmap.isInitialized) return

        visible = true

        fullscreenContent = binding.photoImageView

        photoView = binding.photoImageView
        exitButton = binding.photoBackButton

        exitButton!!.setOnClickListener {
            exitFragment()
        }

        photoView!!.setImageBitmap(mBitmap)
        photoView!!.rotation = mRotation

        (activity as? MainActivity)?.tabLayout?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mRestoreTabs) {
            (activity as? MainActivity)?.tabLayout?.visibility = View.VISIBLE
        }
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        exitButton = null
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun exitFragment() {
        if (!this::mParentFrag.isInitialized) return

        parentFragmentManager
            .beginTransaction()
            .remove(this)
            .show(mParentFrag)
            .disallowAddToBackStack()
            .commitAllowingStateLoss()
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    companion object {
        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300

//        private const val TAG = "disqs.PhotoFragment"

        /**
         * Returns a new instance of this fragment.
         */
        @JvmStatic
        fun newInstance(
            parentFrag: Fragment,
            bitmap: Bitmap,
            rotation: Float,
            restoreTabs: Boolean
        ): PhotoFragment {
            return PhotoFragment().apply {
                mParentFrag = parentFrag
                mBitmap = bitmap
                mRotation = rotation
                mRestoreTabs = restoreTabs
                arguments = Bundle()
            }
        }
    }
}