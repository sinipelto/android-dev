package fi.tuni.sinipelto.cameraapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var viewFinder: PreviewView
    private lateinit var imgHolder: ImageView
    private lateinit var captureButton: Button
    private lateinit var flipButton: AppCompatImageButton

    // Current camera default to back camera
    private var currentCamera: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera(currentCamera)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Retrieve UI elements into memory
        captureButton = findViewById(R.id.camera_capture_button)
        flipButton = findViewById(R.id.flip_camera_button)
        viewFinder = findViewById(R.id.viewFinder)
        imgHolder = findViewById(R.id.imageHolder)

        // Set up the listener for take photo button
        captureButton.setOnClickListener {
            takePhoto()
        }

        // Change between front and back camera
        flipButton.setOnClickListener {
            cameraExecutor.shutdown()
            currentCamera = if (currentCamera == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            startCamera(currentCamera)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(currentCamera)
            } else {
                Toast.makeText(
                    this,
                    "Permission was not granted for Camera. Please enable camera permission from the app settings.",
                    Toast.LENGTH_SHORT
                ).show()
//                finish()
            }
        }
    }

    private fun takePhoto() {
        // Double check we have camera perms before trying to start camera
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
            return
        }

        // Ensure image capture not null
        val imageCapture = imageCapture ?: return

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this@MainActivity),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(
                        this@MainActivity,
                        "Failed taking picture. Try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // To be able to use the picture image as an object,
                // we need to retrieve the actual image from the proxy
                // using an currently experimental method
                @SuppressLint("UnsafeExperimentalUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    if (image.image == null) {
                        Log.e(TAG, "Capture Error: $image had image value null.")
                        Toast.makeText(
                            this@MainActivity,
                            "Failed taking picture. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    // Handle image rotation for picture, depending on the camera used
                    if (currentCamera == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        imgHolder.rotation = -90F
                    } else if (currentCamera == CameraSelector.DEFAULT_BACK_CAMERA) {
                        imgHolder.rotation = +90F
                    }

                    // Convert the received image into bitmap and set the holder source to it
                    imgHolder.setImageBitmap(image.convertImageProxyToBitmap())

                    // Otherwise fails to get another picture
                    image.close()
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

    // Takes camera to be used as parameter (defaults to back camera)
    private fun startCamera(camera: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera (preview screen and capture handler)
                cameraProvider.bindToLifecycle(
                    this, camera, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "fi.tuni.sinipelto.cameraapp.MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }
}