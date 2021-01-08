package com.example.testapp

import Classifier
import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {


    var triggerLimit = 5 //initialise value for limit of triggers until level 3 drowsiness

    var backgroundColor="#2E334A"//initial background colour

    val ALARMLIST= arrayListOf(R.raw.alarm1,R.raw.alarm2,R.raw.alarm3,R.raw.alarm4)

    var alarm=1//initial alarm sound

    var directionsTo="petrol station"

    val coloursArray= arrayListOf<Int>(
        Color.BLACK,
        Color.BLUE,
        Color.CYAN,
        Color.GRAY,
        Color.GREEN,
        Color.RED,
        Color.WHITE,
        Color.YELLOW,
        3027786
    )

    private val TAG = "CameraXBasic"
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    var runAnalysis: Boolean = false
    private var triggers = 0

    var classifier: Classifier = Classifier(this@MainActivity)


    private val highAccuracyOpts = FaceDetectorOptions.Builder().setPerformanceMode(
        FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
    ).build()
    val faceDetector = FaceDetection.getClient(highAccuracyOpts)


    val Logs = LogTools(this@MainActivity)

    private lateinit var cameraExecutor: ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        setUpPreferences()
        requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), 1)

        cameraExecutor = Executors.newSingleThreadExecutor()

        classifier.initialize().addOnSuccessListener { Log.d("Classifier", "Initialised")}

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val firstStart=prefs.getBoolean("firstRun",true)
        val editor = prefs.edit()
        if(firstStart) {
            Logs.createLogs()
            editor.putBoolean("firstRun",false)
            editor.apply()

        }
        Logs.updateLogs()


    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun setUpPreferences(){
        val preferences=PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

    }

    fun startButtonPressed(V: View) {
        runAnalysis = true
    }

    fun assessButtonPressed(V: View) {
        val intent = Intent(this, Assessment::class.java)
        startActivity(intent)
    }


    val sharedPreferenceChangeListener:SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPref, key ->
            Log.d(key.toString(), "Changed")
            when(key){
                "triggerPreference" -> {
                    triggerLimit = sharedPref.getString(key, "5")?.toInt() ?: 5
                }
                "backgroundColourPreference" -> {
                    try {
                        val colorNo = sharedPref.getString(key, "3027786")?.toInt() ?: 1
                        MainLayout.setBackgroundColor(coloursArray[colorNo])
                    } catch (e: NumberFormatException) {
                        println(e.toString())
                    }
                }
                "directionsPreference" -> {
                        directionsTo= sharedPref.getString(key, "petrol station").toString()
                }
                "alarmPreference" ->{
                    alarm= (sharedPref.getString(key, "1")?.toInt() ?: 1)-1
                }
            }

        }


    fun settingsButtonPressed(V: View){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)

    }

    fun logButtonPressed(V: View){
        val intent = Intent(this, com.example.testapp.LogActivity::class.java)
        startActivity(intent)

    }


    fun stopButtonPressed(V: View) {
        triggers=0
        runAnalysis = false
    }

    private fun level1Drowsiness() {
        val now= Calendar.getInstance()
        val hour=now.get(Calendar.HOUR_OF_DAY)
        Logs.logLevel2Drowsiness(hour)
        val mMediaPlayer = MediaPlayer.create(this, ALARMLIST[alarm])
        mMediaPlayer!!.isLooping = false
        mMediaPlayer!!.start()




    }

    private fun level2Drowsiness() {
        val now= Calendar.getInstance()
        val hour=now.get(Calendar.HOUR_OF_DAY)
        Logs.logLevel3Drowsiness(hour)

        val gmmIntentUri: Uri = Uri.parse("geo:0,0?q=$directionsTo")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(
                cameraExecutor, DrowsinessAnalyzer(
                    classifier,
                    faceDetector
                )
            )

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    inner class DrowsinessAnalyzer(
        private val classifier: Classifier,
        private val faceDetector: com.google.mlkit.vision.face.FaceDetector,
    ) : ImageAnalysis.Analyzer {

        private var currentClassTime = 0
        private var lastClassTime = 0

        @SuppressLint("UnsafeExperimentalUsageError")

        override fun analyze(image: ImageProxy) {
            if (runAnalysis) {
                val inputMediaImage = image.image
                val inputBitmap=image.toBitmap()
                if (inputMediaImage != null) {
                    val FBimage =InputImage.fromMediaImage(
                        inputMediaImage,
                        image.imageInfo.rotationDegrees
                    )
                    val result = faceDetector.process(FBimage)
                        .addOnSuccessListener { faces ->
                            for (face in faces) {
                                val coord = face.boundingBox
                                val inputBitmap = RotateBitmap(inputBitmap, 270F)
                                try {
                                    val extractedFace = Bitmap.createBitmap(
                                        inputBitmap!!,
                                        (coord.exactCenterX() / 2).toInt(),
                                        (coord.exactCenterY() / 2).toInt(),
                                        coord.width(),
                                        coord.height()

                                    )
                                    if (extractedFace != null) {
                                        val classification = classifier.classify(
                                            extractedFace,
                                            drowsinessLabel,
                                            seekBar.progress
                                        )
                                        if (classification == 1) {
                                            currentClassTime =
                                                (SystemClock.uptimeMillis().toInt()) / 1000
                                            if ((currentClassTime - lastClassTime) > 3) {
                                                lastClassTime = currentClassTime
                                                triggers += 1
                                                triggerLabel.text = "Triggers=$triggers"
                                                if (triggers == triggerLimit) {
                                                    triggers = 0
                                                    level2Drowsiness()


                                                } else level1Drowsiness()


                                            }}}}
                                catch (e: java.lang.Exception) {
                                    Log.d("Exception", e.toString())
                                }


                            }
                            image.close()
                        }

                        .addOnFailureListener { e ->
                            Log.d("ERROR", e.toString())
                            image.close()
                        }
                }else image.close()

            }else image.close()


        }
    }

        fun ImageProxy.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer // Y
            val uBuffer = planes[1].buffer // U
            val vBuffer = planes[2].buffer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        fun RotateBitmap(source: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }



    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}





