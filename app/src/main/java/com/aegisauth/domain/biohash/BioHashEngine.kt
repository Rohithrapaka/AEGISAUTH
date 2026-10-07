package com.aegisauth.domain.biohash

import com.aegisauth.core.security.KeystoreManager
import com.aegisauth.domain.embedding.FaceEmbeddingEngine
import com.aegisauth.domain.model.BioHashTemplate
import com.aegisauth.domain.model.MatchingResult
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

interface BioHashEngine {
    fun generateBioHash(embedding: FloatArray): BioHashTemplate
    fun compareBioHashes(enrolled: BioHashTemplate, probe: BioHashTemplate, threshold: Float): MatchingResult
    fun compareBioHashes(enrolledTemplates: List<BioHashTemplate>, probe: BioHashTemplate, threshold: Float): MatchingResult
    fun serializeTemplateToBytes(template: BioHashTemplate): ByteArray
    fun deserializeTemplateFromBytes(bytes: ByteArray): BioHashTemplate
}

@Singleton
class OrthonormalBioHashEngine @Inject constructor(
    private val keystoreManager: KeystoreManager
) : BioHashEngine {

    companion object {
        private const val BIOHASH_BITS_LENGTH = 128
    }

    // Cached projection matrix derived from device-specific Keystore seed
    private val projectionMatrix: Array<FloatArray> by lazy {
        generateOrthonormalMatrix(
            rows = BIOHASH_BITS_LENGTH,
            cols = FaceEmbeddingEngine.EMBEDDING_DIM,
            seed = keystoreManager.getDeviceProjectionSeed()
        )
    }

    override fun generateBioHash(embedding: FloatArray): BioHashTemplate {
        require(embedding.size == FaceEmbeddingEngine.EMBEDDING_DIM) {
            "Embedding vector length must match ${FaceEmbeddingEngine.EMBEDDING_DIM} (was ${embedding.size})"
        }

        // 1. Matrix multiplication: Projected = ProjectionMatrix * NormalizedEmbedding
        val projected = FloatArray(BIOHASH_BITS_LENGTH)
        for (i in 0 until BIOHASH_BITS_LENGTH) {
            var dot = 0.0f
            val row = projectionMatrix[i]
            for (j in embedding.indices) {
                dot += row[j] * embedding[j]
            }
            projected[i] = dot
        }

        // 2. Quantization (Binarization): bit is true if projected >= 0, false otherwise
        val bioHashBits = BooleanArray(BIOHASH_BITS_LENGTH)
        for (i in 0 until BIOHASH_BITS_LENGTH) {
            bioHashBits[i] = projected[i] >= 0.0f
        }

        val templateId = UUID.randomUUID().toString()
        return BioHashTemplate(
            id = templateId,
            version = 1,
            bioHashBits = bioHashBits,
            featureLength = BIOHASH_BITS_LENGTH
        )
    }

    override fun compareBioHashes(
        enrolled: BioHashTemplate,
        probe: BioHashTemplate,
        threshold: Float
    ): MatchingResult {
        val len = minOf(enrolled.bioHashBits.size, probe.bioHashBits.size)
        var hammingDistance = 0

        for (i in 0 until len) {
            if (enrolled.bioHashBits[i] != probe.bioHashBits[i]) {
                hammingDistance++
            }
        }

        val normalizedDistance = hammingDistance.toFloat() / len.toFloat()
        val similarityScore = (1.0f - normalizedDistance).coerceIn(0.0f, 1.0f)
        val isMatch = similarityScore >= threshold

        return MatchingResult(
            isMatch = isMatch,
            similarityScore = similarityScore,
            hammingDistance = hammingDistance,
            thresholdUsed = threshold
        )
    }

    override fun compareBioHashes(
        enrolledTemplates: List<BioHashTemplate>,
        probe: BioHashTemplate,
        threshold: Float
    ): MatchingResult {
        if (enrolledTemplates.isEmpty()) {
            return MatchingResult(
                isMatch = false,
                similarityScore = 0.0f,
                hammingDistance = BIOHASH_BITS_LENGTH,
                thresholdUsed = threshold
            )
        }

        // Evaluate probe against each authorized enrolled template, choose minimum Hamming distance in BioHash domain
        var bestResult: MatchingResult? = null
        for (template in enrolledTemplates) {
            val result = compareBioHashes(template, probe, threshold)
            if (bestResult == null || result.hammingDistance < bestResult.hammingDistance) {
                bestResult = result
            }
        }

        return bestResult ?: MatchingResult(
            isMatch = false,
            similarityScore = 0.0f,
            hammingDistance = BIOHASH_BITS_LENGTH,
            thresholdUsed = threshold
        )
    }

    override fun serializeTemplateToBytes(template: BioHashTemplate): ByteArray {
        val bits = template.bioHashBits
        val byteCount = (bits.size + 7) / 8
        val packedBytes = ByteArray(byteCount + 4) // 4 bytes version + packed bits

        packedBytes[0] = (template.version shr 24).toByte()
        packedBytes[1] = (template.version shr 16).toByte()
        packedBytes[2] = (template.version shr 8).toByte()
        packedBytes[3] = template.version.toByte()

        for (i in bits.indices) {
            if (bits[i]) {
                val byteIndex = 4 + (i / 8)
                val bitIndex = i % 8
                packedBytes[byteIndex] = (packedBytes[byteIndex].toInt() or (1 shl bitIndex)).toByte()
            }
        }
        return packedBytes
    }

    override fun deserializeTemplateFromBytes(bytes: ByteArray): BioHashTemplate {
        require(bytes.size >= 4) { "Invalid template byte length" }
        val version = ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)

        val totalBits = (bytes.size - 4) * 8
        val bits = BooleanArray(totalBits)

        for (i in 0 until totalBits) {
            val byteIndex = 4 + (i / 8)
            val bitIndex = i % 8
            bits[i] = ((bytes[byteIndex].toInt() shr bitIndex) and 1) == 1
        }

        return BioHashTemplate(
            id = "enrolled_template",
            version = version,
            bioHashBits = bits,
            featureLength = totalBits
        )
    }

    /**
     * Generates a deterministic pseudo-random matrix using Gram-Schmidt orthonormalization.
     */
    private fun generateOrthonormalMatrix(rows: Int, cols: Int, seed: Long): Array<FloatArray> {
        val random = Random(seed)
        val matrix = Array(rows) { FloatArray(cols) }

        // 1. Fill with standard Gaussian random variables
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                matrix[i][j] = random.nextGaussian().toFloat()
            }
        }

        // 2. Modified Gram-Schmidt process for rows
        for (i in 0 until rows) {
            // Subtract projections onto previous rows
            for (j in 0 until i) {
                var dot = 0.0f
                for (k in 0 until cols) {
                    dot += matrix[i][k] * matrix[j][k]
                }
                for (k in 0 until cols) {
                    matrix[i][k] -= dot * matrix[j][k]
                }
            }

            // Normalize row vector
            var sumSq = 0.0f
            for (k in 0 until cols) {
                sumSq += matrix[i][k] * matrix[i][k]
            }
            val norm = sqrt(sumSq).coerceAtLeast(1e-6f)
            for (k in 0 until cols) {
                matrix[i][k] /= norm
            }
        }

        return matrix
    }
}
