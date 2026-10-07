package com.aegisauth.core.camera

import android.graphics.*
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class FaceFrameAnalyzer(
    private val onFaceAnalyzed: (face: Face?, frameWidth: Int, frameHeight: Int, faceBitmap: Bitmap?) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )

    private val isBusy = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || isBusy.get()) {
            imageProxy.close()
            return
        }

        isBusy.set(true)
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                val primaryFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                var faceBitmap: Bitmap? = null

                if (primaryFace != null) {
                    try {
                        val fullBitmap = imageProxyToBitmap(imageProxy)
                        if (fullBitmap != null) {
                            val bounds = primaryFace.boundingBox
                            val left = bounds.left.coerceAtLeast(0)
                            val top = bounds.top.coerceAtLeast(0)
                            val right = bounds.right.coerceAtMost(fullBitmap.width)
                            val bottom = bounds.bottom.coerceAtMost(fullBitmap.height)
                            val width = (right - left).coerceAtLeast(1)
                            val height = (bottom - top).coerceAtLeast(1)

                            if (width > 10 && height > 10 && left + width <= fullBitmap.width && top + height <= fullBitmap.height) {
                                faceBitmap = Bitmap.createBitmap(fullBitmap, left, top, width, height)
                            }
                        }
                    } catch (e: Exception) {
                        // Safe fallback
                    }
                }

                val frameW = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.height else imageProxy.width
                val frameH = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.width else imageProxy.height
                onFaceAnalyzed(primaryFace, frameW, frameH, faceBitmap)
            }
            .addOnFailureListener {
                onFaceAnalyzed(null, imageProxy.width, imageProxy.height, null)
            }
            .addOnCompleteListener {
                isBusy.set(false)
                imageProxy.close()
            }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        // Build a correctly-strided NV21 byte array
        val nv21 = ByteArray(width * height * 3 / 2)

        // Copy Y plane — skip padding bytes on each row
        var nv21Idx = 0
        for (row in 0 until height) {
            yBuffer.position(row * yRowStride)
            yBuffer.get(nv21, nv21Idx, width)
            nv21Idx += width
        }

        // Interleave V and U (NV21 = Y + VU interleaved)
        val chromaHeight = height / 2
        val chromaWidth = width / 2
        for (row in 0 until chromaHeight) {
            for (col in 0 until chromaWidth) {
                val uvOffset = row * uvRowStride + col * uvPixelStride
                vBuffer.position(uvOffset)
                nv21[nv21Idx++] = vBuffer.get()
                uBuffer.position(uvOffset)
                nv21[nv21Idx++] = uBuffer.get()
            }
        }

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 85, out)
        val imageBytes = out.toByteArray()
        val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply {
                postRotate(rotationDegrees.toFloat())
                // Mirror horizontally for front camera selfie alignment
                postScale(-1f, 1f, originalBitmap.width / 2f, originalBitmap.height / 2f)
            }
            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        } else {
            originalBitmap
        }
    }
}
