package com.aegisauth.domain.embedding

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

interface FaceEmbeddingEngine {
    companion object {
        const val EMBEDDING_DIM = 192
    }
    suspend fun generateEmbedding(faceBitmap: Bitmap, face: Face?): FloatArray
}

@Singleton
class TfliteFaceEmbeddingEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : FaceEmbeddingEngine {

    companion object {
        const val EMBEDDING_DIM = 192
        private const val MODEL_INPUT_SIZE = 112
        private const val MODEL_FILE_NAME = "mobile_face_net.tflite"
        private const val TAG = "AegisEmbedding"
    }

    private var interpreter: Interpreter? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val assetManager = context.assets
            val fileDescriptor = assetManager.openFd(MODEL_FILE_NAME)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val options = Interpreter.Options().apply {
                setNumThreads(4)
                useNNAPI = false // deterministic inference across ARM/x86 architectures
            }
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "MobileFaceNet TFLite model loaded successfully from assets ($EMBEDDING_DIM dimensions).")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load MobileFaceNet TFLite model: ${e.localizedMessage}")
        }
    }

    override suspend fun generateEmbedding(faceBitmap: Bitmap, face: Face?): FloatArray = withContext(Dispatchers.Default) {
        val interp = interpreter
        if (interp == null) {
            Log.w(TAG, "Interpreter null, using fallback deterministic feature extraction.")
            return@withContext fallbackEmbedding(faceBitmap)
        }

        // 1. Scale cropped face bitmap to model input dimension (112 x 112)
        val resizedBitmap = if (faceBitmap.width == MODEL_INPUT_SIZE && faceBitmap.height == MODEL_INPUT_SIZE) {
            faceBitmap
        } else {
            Bitmap.createScaledBitmap(faceBitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)
        }

        // 2. Allocate direct ByteBuffer: 1 x 112 x 112 x 3 channels x 4 bytes (Float32)
        val inputBuffer = ByteBuffer.allocateDirect(1 * MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val pixels = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE)

        // 3. Preprocess with MobileFaceNet normalization: (pixel - 127.5) / 128.0
        for (p in pixels) {
            val r = ((p shr 16) and 0xFF).toFloat()
            val g = ((p shr 8) and 0xFF).toFloat()
            val b = (p and 0xFF).toFloat()

            inputBuffer.putFloat((r - 127.5f) / 128.0f)
            inputBuffer.putFloat((g - 127.5f) / 128.0f)
            inputBuffer.putFloat((b - 127.5f) / 128.0f)
        }

        // 4. Run inference -> shape [1, 192]
        val outputArray = Array(1) { FloatArray(EMBEDDING_DIM) }
        synchronized(interp) {
            interp.run(inputBuffer, outputArray)
        }

        val rawEmbedding = outputArray[0]

        // 5. L2 Normalize the embedding vector for unit hypersphere comparison
        normalizeVector(rawEmbedding)
    }

    private fun fallbackEmbedding(faceBitmap: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(faceBitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)
        val pixels = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        scaled.getPixels(pixels, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE)
        val raw = FloatArray(EMBEDDING_DIM)

        for (i in 0 until EMBEDDING_DIM) {
            val pIdx = (i * 65) % pixels.size
            val p = pixels[pIdx]
            val r = (p shr 16 and 0xFF) / 255.0f
            val g = (p shr 8 and 0xFF) / 255.0f
            val b = (p and 0xFF) / 255.0f
            raw[i] = 0.299f * r + 0.587f * g + 0.114f * b
        }
        return normalizeVector(raw)
    }

    private fun normalizeVector(vector: FloatArray): FloatArray {
        var sumSquares = 0.0f
        for (v in vector) {
            sumSquares += v * v
        }
        val norm = sqrt(sumSquares).coerceAtLeast(1e-6f)
        val normalized = FloatArray(vector.size)
        for (i in vector.indices) {
            normalized[i] = vector[i] / norm
        }
        return normalized
    }
}

@Singleton
class OnDeviceFaceEmbeddingEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : FaceEmbeddingEngine {
    private val delegate = TfliteFaceEmbeddingEngine(context)

    override suspend fun generateEmbedding(faceBitmap: Bitmap, face: Face?): FloatArray {
        return delegate.generateEmbedding(faceBitmap, face)
    }
}

