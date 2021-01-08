import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.call
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class Classifier(private val context: Context) {

    private var interpreter: Interpreter? = null

    private var isInitialized = false
        private set

    private var gpuDelegate: GpuDelegate? = null

    var labels = ArrayList<String>()

    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var modelInputSize: Int = 0



    fun initialize(): Task<Void> {
        return call(
            executorService,
            {
                initializeInterpreter()
                null
            }
        )
    }

    @Throws(IOException::class)
    private fun initializeInterpreter() {

        val assetManager = context.assets
        val model = loadModelFile(assetManager, "LiteModel.tflite")

        labels = loadLines(context, "Labels.txt")
        val options = Interpreter.Options()
        gpuDelegate = GpuDelegate()
        options.addDelegate(gpuDelegate)
        val interpreter = Interpreter(model, options)

        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        Log.d("Input",inputShape[1].toString())
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * CHANNEL_SIZE

        this.interpreter = interpreter

        isInitialized = true
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(IOException::class)
    fun loadLines(context: Context, filename: String): ArrayList<String> {
        val s = Scanner(InputStreamReader(context.assets.open(filename)))
        val labels = ArrayList<String>()
        while (s.hasNextLine()) {
            labels.add(s.nextLine())
        }
        s.close()
        return labels
    }

    private fun getMaxResult(result: FloatArray,confidence:Int): Int {
        var index = 0
        val confidence = confidence.toFloat()/100F
        if (result[1]>confidence){
            index = 1
            }
        return index


    }


     fun classify(bitmap: Bitmap,drowsyLabel: TextView,confidence: Int): Int {

        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }

        val resizedImage =
            Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val byteBuffer = convertBitmapToByteBuffer(resizedImage)


        val output = Array(1) { FloatArray(labels.size) }

        interpreter?.run(byteBuffer, output)

        var index = getMaxResult(output[0],confidence)
        var result = "${labels[index]}\n"

        drowsyLabel.text="State:$result"

        return index
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputImageWidth) {
            for (j in 0 until inputImageHeight) {
                val pixelVal = pixels[pixel++]
                byteBuffer.putFloat(((((pixelVal shr 16 and 0xFF)) / 127.5F)-1F).toFloat())
                byteBuffer.putFloat(((((pixelVal shr 8 and 0xFF)) / 127.5F)-1F).toFloat())
                byteBuffer.putFloat(((((pixelVal and 0xFF)) / 127.5F)-1F).toFloat())
            }
        }
        bitmap.recycle()
        return byteBuffer
    }

    companion object {
        private const val FLOAT_TYPE_SIZE = 4
        private const val CHANNEL_SIZE = 3
    }
}
