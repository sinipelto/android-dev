package fi.tuni.sinipelto.disqs.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.databinding.FragmentCameraBinding
import fi.tuni.sinipelto.disqs.ui.activity.MainActivity
import fi.tuni.sinipelto.disqs.ui.activity.PermissionDeniedSettingsActivity
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@Suppress("DEPRECATION")
class CameraFragment : Fragment() {

    lateinit var mParentFrag: Fragment

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
    private var visible: Boolean = false

    private var cancelButton: Button? = null
    private var photoButton: Button? = null
    private var flipButton: Button? = null

    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var preview: Preview
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider

    private var cameraSelector: CameraSelector? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        (activity as? MainActivity)?.tabLayout?.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request camera permissions if not already granted
        if (!cameraPermissionGranted()) {
            requestPermissions(
                REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        visible = true

        cancelButton = binding.cameraCancelButton
        photoButton = binding.takePhotoButton
        flipButton = binding.cameraFlipButton

        fullscreenContent = binding.cameraViewFinder
        fullscreenContentControls = binding.cameraFullscreenControls

        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        flipButton?.setOnClickListener {
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            } else if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }

        restoreButtons()
    }

    private fun cameraPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        //Log.d(TAG, "PermissionsResult called.")

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (cameraPermissionGranted()) {
                startCamera()
            } else {
                Snackbar.make(
                    requireContext(),
                    requireView(),
                    getString(R.string.camera_perms_denied),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(
                        R.string.settings_button,
                        PermissionDeniedSettingsActivity(requireContext(), requireActivity())
                    )
                    .show()
                exitFragment()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        startCamera()
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
        (activity as? MainActivity)?.tabLayout?.visibility = View.VISIBLE
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cancelButton = null
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun exitFragment() {
        parentFragmentManager
            .beginTransaction()
            .remove(this)
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

    private fun startCamera() {
        // Select camera to use (stable reference)
        val cameraSelector = cameraSelector ?: return

        // Restore buttons into take-photo mode
        restoreButtons()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(view?.findViewById<PreviewView>(R.id.cameraViewFinder)?.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun restoreButtons() {
        cancelButton!!.text = getString(R.string.back_button_text)
        photoButton!!.text = getString(R.string.take_photo_text)

        cancelButton?.setOnClickListener {
            // if image not captured, simply close the fragment
            exitFragment()
        }

        photoButton?.setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {
        if (!this::mParentFrag.isInitialized) return

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    //Log.d(TAG, "Image captured successfully!")
                    cameraProvider.unbind(preview)

                    cancelButton!!.text = getString(R.string.retry_photo_text)
                    photoButton!!.text = getString(R.string.accept_photo_text)

                    cancelButton!!.setOnClickListener {
                        // Restore buttons and restart camera + preview
                        startCamera()
                    }

                    photoButton!!.setOnClickListener {
                        val img = image.convertImageProxyToBitmap()

                        // Handle image rotation for picture, depending on the camera used
                        val rotation: Float = when (cameraSelector) {
                            CameraSelector.DEFAULT_FRONT_CAMERA -> {
                                -90F
                            }
                            CameraSelector.DEFAULT_BACK_CAMERA -> {
                                +90F
                            }
                            else -> {
                                throw UnsupportedOperationException("Camera selection has unknown value stored.")
                            }
                        }

                        // Pass the stored image to the world fragment
                        (mParentFrag as WorldFragment)
                            .setStoredImage(img, rotation)

                        val snack = Snackbar.make(
                            requireContext(),
                            requireView(),
                            getString(R.string.photo_attach_success),
                            Snackbar.LENGTH_SHORT
                        )
                        val params = snack.view.layoutParams as CoordinatorLayout.LayoutParams
                        params.gravity = Gravity.TOP
                        snack.view.layoutParams = params
                        snack.show()

                        // Finally, close this fragment and go back to World Fragment
                        exitFragment()
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    super.onError(exc)
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Snackbar.make(
                        requireContext(),
                        requireView(),
                        getString(R.string.image_capture_failed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
    }

    // Function to handle the image proxy for taken picture
    // to convert it into a readable bitmap to be inserted into the image preview widget
    private fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
        if (planes.size >= 3) {
            val yBuffer = planes[0].buffer // Y
            val vuBuffer = planes[2].buffer // VU

            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()

            val nv21 = ByteArray(ySize + vuSize)

            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } else {
            val buffer = planes[0].buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    companion object {
        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300

        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val TAG = "disqs.CameraFragment"

        /**
         * Returns a new instance of this fragment.
         */
        @JvmStatic
        fun newInstance(parentFrag: Fragment): CameraFragment {
            return CameraFragment().apply {
                mParentFrag = parentFrag
                arguments = Bundle()
            }
        }
    }
}