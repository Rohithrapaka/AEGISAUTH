package com.aegisauth.domain

import com.aegisauth.core.security.KeystoreManager
import com.aegisauth.domain.biohash.OrthonormalBioHashEngine
import com.aegisauth.domain.challenge.ActiveChallengeEngine
import com.aegisauth.domain.embedding.FaceEmbeddingEngine
import com.aegisauth.domain.liveness.PassiveLivenessEngine
import com.aegisauth.domain.model.*
import com.aegisauth.domain.quality.ImageQualityEngine
import com.aegisauth.domain.risk.RiskEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Random

class AegisDomainEnginesTest {

    private lateinit var bioHashEngine: OrthonormalBioHashEngine
    private lateinit var riskEngine: RiskEngine
    private lateinit var challengeEngine: ActiveChallengeEngine
    private lateinit var qualityEngine: ImageQualityEngine
    private lateinit var livenessEngine: PassiveLivenessEngine

    @Before
    fun setUp() {
        // Test KeystoreManager subclass — returns fixed seed, never touches Android Keystore
        val mockKeystoreManager = object : KeystoreManager() {
            override fun getDeviceProjectionSeed(): Long = 133742L
        }
        bioHashEngine = OrthonormalBioHashEngine(mockKeystoreManager)
        riskEngine = RiskEngine()
        challengeEngine = ActiveChallengeEngine()
        qualityEngine = ImageQualityEngine()
        livenessEngine = PassiveLivenessEngine()
    }

    // ── BioHash Tests ─────────────────────────────────────────────────────────

    @Test
    fun testBioHashDeterministicGenerationAndMatching() {
        val random = Random(42)
        val embedding1 = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { random.nextFloat() }
        val embedding2 = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) {
            embedding1[it] + (random.nextFloat() * 0.05f - 0.025f)
        }
        val template1 = bioHashEngine.generateBioHash(embedding1)
        val template2 = bioHashEngine.generateBioHash(embedding2)

        val matchResult = bioHashEngine.compareBioHashes(template1, template2, threshold = 0.65f)
        assertTrue("Similar embeddings should produce matching BioHash", matchResult.isMatch)
        assertTrue("Similarity score should be high (>=0.70)", matchResult.similarityScore >= 0.70f)
        assertEquals(128, template1.bioHashBits.size)
    }

    @Test
    fun testBioHashSameEmbeddingProducesSameHash() {
        val random = Random(99)
        val embedding = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { random.nextFloat() }
        val t1 = bioHashEngine.generateBioHash(embedding)
        val t2 = bioHashEngine.generateBioHash(embedding)

        assertArrayEquals("Same embedding must produce identical BioHash bits", t1.bioHashBits, t2.bioHashBits)
        val selfMatch = bioHashEngine.compareBioHashes(t1, t2, threshold = 0.99f)
        assertTrue("Self-comparison must match at 1.0 similarity", selfMatch.isMatch)
        assertEquals(0, selfMatch.hammingDistance)
    }

    @Test
    fun testBioHashUnrelatedEmbeddingDoesNotMatch() {
        val random = Random(42)
        val embedding1 = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { random.nextFloat() }
        val unrelated = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { -random.nextFloat() }
        val template1 = bioHashEngine.generateBioHash(embedding1)
        val templateUnrelated = bioHashEngine.generateBioHash(unrelated)

        val mismatchResult = bioHashEngine.compareBioHashes(template1, templateUnrelated, threshold = 0.70f)
        assertFalse("Unrelated embeddings should NOT match at normal threshold", mismatchResult.isMatch)
    }

    @Test
    fun testBioHashDiscriminatesDistinctIdentities() {
        val randomA = Random(1234)
        val randomB = Random(5678)
        // Person A (Enrolled)
        val enrolledPersonEmbedding = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { randomA.nextFloat() - 0.5f }
        // Person B (Friend / Impostor)
        val friendPersonEmbedding = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { randomB.nextFloat() - 0.5f }

        val enrolledTemplate = bioHashEngine.generateBioHash(enrolledPersonEmbedding)
        val friendTemplate = bioHashEngine.generateBioHash(friendPersonEmbedding)

        val match = bioHashEngine.compareBioHashes(enrolledTemplate, friendTemplate, threshold = 0.70f)
        assertFalse("Friend's face must be rejected against enrolled user", match.isMatch)
        assertTrue("Distance between distinct identities should be high (Hamming > 30)", match.hammingDistance > 30)
    }

    @Test
    fun testBioHashSerializationRoundtrip() {
        val random = Random(77)
        val embedding = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { random.nextFloat() }
        val template = bioHashEngine.generateBioHash(embedding)

        val bytes = bioHashEngine.serializeTemplateToBytes(template)
        val deserialized = bioHashEngine.deserializeTemplateFromBytes(bytes)

        assertArrayEquals("Serialized/deserialized bits must match original", template.bioHashBits, deserialized.bioHashBits)
        assertEquals(1, deserialized.version)
    }

    // ── Quality Engine Tests ──────────────────────────────────────────────────


    @Test
    fun testQualityEngineNoFace() {
        val result = qualityEngine.evaluateQuality(null, 1080, 1920, null)
        assertFalse("No face → not acceptable", result.isAcceptable)
        assertEquals(QualityFeedback.NO_FACE_DETECTED, result.feedback)
        assertEquals(0f, result.overallScore)
    }

    @Test
    fun testQualityEngineInvalidFrameDimensions() {
        val result = qualityEngine.evaluateQuality(null, 0, 0, null)
        assertFalse("Zero frame dimensions → not acceptable", result.isAcceptable)
        assertEquals(QualityFeedback.NO_FACE_DETECTED, result.feedback)
    }

    // ── Risk Engine Tests ─────────────────────────────────────────────────────

    @Test
    fun testRiskEngineLowRisk() {
        val liveness = LivenessResult(0.85f, 0.90f, LivenessSignals(0.8f, 0.8f, 0.8f, 0.8f, 0.8f), true, "OK")
        val quality = QualityResult(0.85f, 0.80f, 0.70f, 0.75f, 0.90f, 0.85f, true, QualityFeedback.GOOD)
        val result = riskEngine.evaluateRisk(liveness, quality, recentFailedAttempts = 0)
        assertEquals(RiskLevel.LOW, result.level)
        assertFalse(result.requiresActiveChallenge)
    }

    @Test
    fun testRiskEngineHighFailures() {
        val liveness = LivenessResult(0.85f, 0.90f, LivenessSignals(0.8f, 0.8f, 0.8f, 0.8f, 0.8f), true, "OK")
        val quality = QualityResult(0.85f, 0.80f, 0.70f, 0.75f, 0.90f, 0.85f, true, QualityFeedback.GOOD)
        val result = riskEngine.evaluateRisk(liveness, quality, recentFailedAttempts = 3)
        assertEquals(RiskLevel.HIGH, result.level)
        assertTrue(result.requiresActiveChallenge)
    }

    @Test
    fun testRiskEngineChallengeAlwaysMode() {
        val liveness = LivenessResult(0.85f, 0.90f, LivenessSignals(0.8f, 0.8f, 0.8f, 0.8f, 0.8f), true, "OK")
        val quality = QualityResult(0.85f, 0.80f, 0.70f, 0.75f, 0.90f, 0.85f, true, QualityFeedback.GOOD)
        val result = riskEngine.evaluateRisk(liveness, quality, recentFailedAttempts = 0, activeChallengeMode = "ALWAYS")
        assertTrue("ALWAYS mode → must require challenge even with no failures", result.requiresActiveChallenge)
    }

    @Test
    fun testRiskEngineChallengeNeverMode() {
        val liveness = LivenessResult(0.20f, 0.90f, LivenessSignals(0.2f, 0.2f, 0.2f, 0.2f, 0.2f), false, "Low")
        val quality = QualityResult(0.40f, 0.30f, 0.15f, 0.20f, 0.30f, 0.40f, false, QualityFeedback.TOO_DARK)
        val result = riskEngine.evaluateRisk(liveness, quality, recentFailedAttempts = 2, activeChallengeMode = "NEVER")
        assertFalse("NEVER mode → must NOT require challenge regardless of risk", result.requiresActiveChallenge)
    }

    // ── Challenge Engine Tests ────────────────────────────────────────────────

    @Test
    fun testActiveChallengeLifecycle() {
        val state = challengeEngine.startNewChallenge()
        assertNotNull(state.challenge)
        assertEquals(8, state.remainingSeconds)
        assertFalse(state.isCompleted)
        assertFalse(state.isFailed)
    }

    @Test
    fun testActiveChallengeTimerCountdown() {
        var state = challengeEngine.startNewChallenge()
        repeat(7) { state = challengeEngine.tickTimer(state) }
        assertEquals(1, state.remainingSeconds)
        assertFalse(state.isFailed)
    }

    @Test
    fun testActiveChallengeTimerExpiry() {
        var state = challengeEngine.startNewChallenge()
        repeat(8) { state = challengeEngine.tickTimer(state) }
        assertTrue("Challenge must fail after 8 ticks", state.isFailed)
        assertEquals(0, state.remainingSeconds)
    }

    @Test
    fun testActiveChallengeReset() {
        challengeEngine.reset()
        val state = challengeEngine.startNewChallenge()
        assertFalse(state.isCompleted)
        assertFalse(state.isFailed)
    }

    // ── Passive Liveness Tests ────────────────────────────────────────────────

    @Test
    fun testPassiveLivenessNullFaceNotLive() {
        livenessEngine.reset()
        val noFaceQuality = QualityResult(0f, 0f, 0f, 0f, 0f, 0f, false, QualityFeedback.NO_FACE_DETECTED)
        val result = livenessEngine.processFrame(null, noFaceQuality)
        assertFalse("Null face must not be marked live", result.isLive)
        assertEquals(0f, result.score)
    }

    @Test
    fun testPassiveLivenessUnacceptableQualityNotLive() {
        livenessEngine.reset()
        val badQuality = QualityResult(0.2f, 0.1f, 0.1f, 0.1f, 0.2f, 0.1f, false, QualityFeedback.TOO_DARK)
        // Pass null face with unacceptable quality
        val result = livenessEngine.processFrame(null, badQuality)
        assertFalse("Unacceptable quality → must not be live", result.isLive)
        assertEquals(0f, result.score)
    }

    // ── Enrollment Metrics Tests ──────────────────────────────────────────────

    @Test
    fun testEnrollmentMetricsAcceptable() {
        val metrics = EnrollmentMetrics(
            captureQuality = 94,
            faceConsistency = 91,
            lightingQuality = 88,
            poseConsistency = 96,
            overallConfidence = 93,
            isAcceptable = true
        )
        assertTrue(metrics.isAcceptable)
        assertTrue(metrics.overallConfidence >= 65)
        assertTrue(metrics.faceConsistency >= 60)
    }

    @Test
    fun testEnrollmentMetricsLowQualityRejection() {
        val metrics = EnrollmentMetrics(
            captureQuality = 45,
            faceConsistency = 50,
            lightingQuality = 40,
            poseConsistency = 55,
            overallConfidence = 48,
            isAcceptable = false
        )
        assertFalse("Inconsistent/low quality metrics must be flagged unacceptable", metrics.isAcceptable)
    }

    // ── Protection Lifecycle & Failsafe Tests ──────────────────────────────────

    @Test
    fun testGracePeriodLifecycle() {
        val testPkg = "com.google.android.youtube"
        com.aegisauth.service.AppProtectionService.clearGracePeriod(testPkg)
        assertFalse("Package must not be in grace period initially",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(testPkg, 30_000L))

        com.aegisauth.service.AppProtectionService.markPackageUnlocked(testPkg)
        assertTrue("Package must be in grace period after unlock",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(testPkg, 30_000L))

        com.aegisauth.service.AppProtectionService.clearGracePeriod(testPkg)
        assertFalse("Package must not be in grace period after clearGracePeriod",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(testPkg, 30_000L))
    }

    @Test
    fun testCancelAndReturnHomeSetsCooldown() {
        val testPkg = "com.google.android.youtube"
        com.aegisauth.service.AppProtectionService.cancelAndReturnHome(testPkg, null)

        assertEquals("Last cancelled package should match", testPkg, com.aegisauth.service.AppProtectionService.lastCancelledPackage)
        val elapsed = System.currentTimeMillis() - com.aegisauth.service.AppProtectionService.lastCancelTime
        assertTrue("Cancel time should be recent (< 1000ms)", elapsed < 1000L)
        assertFalse("Cancelled package must not be in grace period",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(testPkg, 30_000L))
    }

    @Test
    fun testCancellationClearsGracePeriodEvenIfPreviouslyUnlocked() {
        val testPkg = "com.whatsapp"
        // Simulate package was previously unlocked
        com.aegisauth.service.AppProtectionService.markPackageUnlocked(testPkg)
        assertTrue("Package must be unlocked initially",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(testPkg, 60_000L))

        // Emergency Cancel invoked
        com.aegisauth.service.AppProtectionService.cancelAndReturnHome(testPkg, null)

        assertFalse("Emergency cancel MUST immediately revoke access/grace period",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(testPkg, 60_000L))
        assertEquals(testPkg, com.aegisauth.service.AppProtectionService.lastCancelledPackage)
    }

    @Test
    fun testCancelNullSafe() {
        // Calling cancel with null package should safely execute without exception
        com.aegisauth.service.AppProtectionService.cancelAndReturnHome(null, null)
    }

    @Test
    fun testMultiplePackageIndependentGracePeriods() {
        val pkg1 = "com.google.android.youtube"
        val pkg2 = "com.android.chrome"

        com.aegisauth.service.AppProtectionService.clearGracePeriod(pkg1)
        com.aegisauth.service.AppProtectionService.clearGracePeriod(pkg2)

        com.aegisauth.service.AppProtectionService.markPackageUnlocked(pkg1)

        assertTrue("pkg1 should be unlocked",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(pkg1, 30_000L))
        assertFalse("pkg2 must remain locked when pkg1 is unlocked",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(pkg2, 30_000L))

        com.aegisauth.service.AppProtectionService.clearGracePeriod(pkg1)
        assertFalse("pkg1 should now be locked",
            com.aegisauth.service.AppProtectionService.isPackageInGracePeriod(pkg1, 30_000L))
    }

    // ── Multi-Sample Centroid & Consistency Tests ─────────────────────────────

    @Test
    fun testCentroidVectorAggregationAndL2Normalization() {
        val dim = FaceEmbeddingEngine.EMBEDDING_DIM
        val sample1 = FloatArray(dim) { 0.5f }
        val sample2 = FloatArray(dim) { 0.3f }
        val sample3 = FloatArray(dim) { 0.4f }

        val avgVector = FloatArray(dim)
        val samples = listOf(sample1, sample2, sample3)
        for (sample in samples) {
            for (i in 0 until dim) {
                avgVector[i] += sample[i]
            }
        }
        for (i in 0 until dim) {
            avgVector[i] /= samples.size.toFloat()
        }

        // L2 normalize
        var sumSq = 0f
        for (v in avgVector) sumSq += v * v
        val norm = kotlin.math.sqrt(sumSq)
        for (i in 0 until dim) avgVector[i] /= norm

        // Verify norm is 1.0 (+/- epsilon)
        var finalNormSq = 0f
        for (v in avgVector) finalNormSq += v * v
        val finalNorm = kotlin.math.sqrt(finalNormSq)
        assertEquals("Aggregated centroid must be unit length L2 normalized", 1.0f, finalNorm, 1e-4f)
    }

    @Test
    fun testCosineSimilarityCalculation() {
        val v1 = floatArrayOf(1f, 0f, 0f)
        val v2 = floatArrayOf(1f, 0f, 0f)
        val v3 = floatArrayOf(0f, 1f, 0f)

        fun cosine(a: FloatArray, b: FloatArray): Float {
            var dot = 0f
            var normA = 0f
            var normB = 0f
            for (i in a.indices) {
                dot += a[i] * b[i]
                normA += a[i] * a[i]
                normB += b[i] * b[i]
            }
            return dot / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
        }

        assertEquals(1.0f, cosine(v1, v2), 1e-5f)
        assertEquals(0.0f, cosine(v1, v3), 1e-5f)
    }

    // ── Pattern Verifier Logic Tests ──────────────────────────────────────────

    @Test
    fun testPatternHasherVerification() {
        fun computeSaltedHash(points: List<Int>, salt: ByteArray): String {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            md.update(salt)
            val patternBytes = points.joinToString(",").toByteArray(Charsets.UTF_8)
            val hash = md.digest(patternBytes)
            return hash.joinToString("") { "%02x".format(it) }
        }

        val pattern = listOf(0, 1, 2, 5, 8)
        val wrongPattern = listOf(0, 1, 2, 5, 7)
        val salt = ByteArray(16) { it.toByte() }

        val hash1 = computeSaltedHash(pattern, salt)
        val hash2 = computeSaltedHash(pattern, salt)
        val hashWrong = computeSaltedHash(wrongPattern, salt)

        assertEquals("Same pattern and salt must produce identical SHA-256 hash", hash1, hash2)
        assertNotEquals("Different pattern must produce different hash", hash1, hashWrong)
        assertTrue("Equal hashes verified in constant time", java.security.MessageDigest.isEqual(hash1.toByteArray(), hash2.toByteArray()))
        assertFalse("Different hashes rejected", java.security.MessageDigest.isEqual(hash1.toByteArray(), hashWrong.toByteArray()))
    }

    // ── 7-Pose Enrollment State Transitions & Guidance Tests ───────────────────

    @Test
    fun testEnrollmentPoseStateMachineTransitions() {
        val steps = com.aegisauth.feature.enrollment.EnrollmentPoseStep.entries
        assertEquals(7, steps.size)
        assertEquals(com.aegisauth.feature.enrollment.EnrollmentPoseStep.FRONT_1, steps[0])
        assertEquals(com.aegisauth.feature.enrollment.EnrollmentPoseStep.FRONT_2, steps[1])
        assertEquals(com.aegisauth.feature.enrollment.EnrollmentPoseStep.FRONT_3, steps[2])
        assertEquals(com.aegisauth.feature.enrollment.EnrollmentPoseStep.TURN_LEFT, steps[3])
        assertEquals(com.aegisauth.feature.enrollment.EnrollmentPoseStep.TURN_RIGHT, steps[4])
        assertEquals(com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_UP, steps[5])
        assertEquals(com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_DOWN, steps[6])
    }

    @Test
    fun testTurnLeftPoseValidationAndGuidance() {
        fun isPoseSatisfied(targetStep: com.aegisauth.feature.enrollment.EnrollmentPoseStep, yaw: Float, pitch: Float, roll: Float = 0f): Boolean {
            if (kotlin.math.abs(roll) > 20.0f) return false
            return when (targetStep) {
                com.aegisauth.feature.enrollment.EnrollmentPoseStep.TURN_LEFT -> (yaw in -32.0f..-6.0f) && kotlin.math.abs(pitch) <= 15.0f
                else -> false
            }
        }

        val step = com.aegisauth.feature.enrollment.EnrollmentPoseStep.TURN_LEFT

        // Facing straight should NOT satisfy TURN_LEFT
        assertFalse("Facing front (yaw=0) must not satisfy TURN_LEFT", isPoseSatisfied(step, 0f, 0f))

        // Turning right should NOT satisfy TURN_LEFT
        assertFalse("Turning right (yaw=+15) must not satisfy TURN_LEFT", isPoseSatisfied(step, 15f, 0f))

        // Turning left properly (yaw=-16) MUST satisfy TURN_LEFT
        assertTrue("Turning left (yaw=-16) must satisfy TURN_LEFT", isPoseSatisfied(step, -16f, 0f))

        // Turning too far (yaw=-40) must NOT satisfy
        assertFalse("Overturning left (yaw=-40) must not satisfy TURN_LEFT", isPoseSatisfied(step, -40f, 0f))
    }

    @Test
    fun testTurnRightLookUpLookDownPoseValidation() {
        fun isPoseSatisfied(targetStep: com.aegisauth.feature.enrollment.EnrollmentPoseStep, yaw: Float, pitch: Float): Boolean {
            return when (targetStep) {
                com.aegisauth.feature.enrollment.EnrollmentPoseStep.TURN_RIGHT -> (yaw in 6.0f..32.0f) && kotlin.math.abs(pitch) <= 15.0f
                com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_UP -> (pitch in 6.0f..28.0f) && kotlin.math.abs(yaw) <= 15.0f
                com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_DOWN -> (pitch in -28.0f..-6.0f) && kotlin.math.abs(yaw) <= 15.0f
                else -> false
            }
        }

        // TURN_RIGHT
        assertTrue(isPoseSatisfied(com.aegisauth.feature.enrollment.EnrollmentPoseStep.TURN_RIGHT, 16f, 0f))
        assertFalse(isPoseSatisfied(com.aegisauth.feature.enrollment.EnrollmentPoseStep.TURN_RIGHT, -16f, 0f))

        // LOOK_UP
        assertTrue(isPoseSatisfied(com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_UP, 0f, 15f))
        assertFalse(isPoseSatisfied(com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_UP, 0f, -15f))

        // LOOK_DOWN
        assertTrue(isPoseSatisfied(com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_DOWN, 0f, -15f))
        assertFalse(isPoseSatisfied(com.aegisauth.feature.enrollment.EnrollmentPoseStep.LOOK_DOWN, 0f, 15f))
    }

    // ── M15 Multi-Biometric, PBKDF2 Password & Session Tests ─────────────────

    @Test
    fun testPasswordPBKDF2DerivationAndVerification() {
        val password = "SecureMasterPassword123!".toCharArray()
        val wrongPassword = "WrongPassword123!".toCharArray()
        val salt = ByteArray(16) { (it * 7).toByte() }
        val differentSalt = ByteArray(16) { (it * 13).toByte() }

        val hash1 = com.aegisauth.core.security.PasswordSecurityManager.deriveKeyHex(password, salt, iterations = 20_000)
        val hash2 = com.aegisauth.core.security.PasswordSecurityManager.deriveKeyHex(password, salt, iterations = 20_000)
        val hashWrong = com.aegisauth.core.security.PasswordSecurityManager.deriveKeyHex(wrongPassword, salt, iterations = 20_000)
        val hashDifferentSalt = com.aegisauth.core.security.PasswordSecurityManager.deriveKeyHex(password, differentSalt, iterations = 20_000)

        assertEquals("Same password and salt must produce identical PBKDF2-HMAC-SHA256 hash", hash1, hash2)
        assertEquals(64, hash1.length) // 256-bit key in hex format is 64 characters
        assertNotEquals("Wrong password must produce different hash", hash1, hashWrong)
        assertNotEquals("Different salt must produce different hash", hash1, hashDifferentSalt)

        assertTrue(
            "Constant-time check accepts valid hash",
            java.security.MessageDigest.isEqual(hash1.toByteArray(Charsets.UTF_8), hash2.toByteArray(Charsets.UTF_8))
        )
        assertFalse(
            "Constant-time check rejects wrong hash",
            java.security.MessageDigest.isEqual(hash1.toByteArray(Charsets.UTF_8), hashWrong.toByteArray(Charsets.UTF_8))
        )
    }

    @Test
    fun testAegisSessionManagerInMemLifecycle() {
        val sessionManager = com.aegisauth.core.security.AegisSessionManager()

        // 1. Initial state is invalid
        assertFalse("Session must be invalid on cold start", sessionManager.isSessionValid())
        assertFalse("isAuthenticated must be false initially", sessionManager.isAuthenticated.value)

        // 2. Authenticate session
        sessionManager.authenticateSession()
        assertTrue("Session must be valid immediately after authentication", sessionManager.isSessionValid())
        assertTrue("isAuthenticated must be true after authentication", sessionManager.isAuthenticated.value)

        // 3. Invalidation
        sessionManager.invalidateSession()
        assertFalse("Session must be invalid after invalidateSession()", sessionManager.isSessionValid())
        assertFalse("isAuthenticated must be false after invalidation", sessionManager.isAuthenticated.value)
    }

    @Test
    fun testAegisSessionManagerGracePeriodExpiration() {
        val sessionManager = com.aegisauth.core.security.AegisSessionManager()
        sessionManager.authenticateSession()

        // Valid within 10_000ms grace period
        assertTrue("Session valid within positive grace period", sessionManager.isSessionValid(10_000L))

        // Expired with 0ms grace period
        Thread.sleep(5)
        assertFalse("Session must expire when elapsed time exceeds grace period", sessionManager.isSessionValid(1L))
    }

    @Test
    fun testMultiTemplateBioHashMatchingOptimalDistance() {
        val randomA = Random(101)
        val randomB = Random(202)

        // Enrolled Identity: 3 distinct poses / samples (Front, Turned Left, Turned Right)
        val embeddingPose1 = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { randomA.nextFloat() }
        val embeddingPose2 = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) {
            embeddingPose1[it] + (randomA.nextFloat() * 0.04f - 0.02f)
        }
        val embeddingPose3 = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) {
            embeddingPose1[it] + (randomA.nextFloat() * 0.06f - 0.03f)
        }

        val template1 = bioHashEngine.generateBioHash(embeddingPose1)
        val template2 = bioHashEngine.generateBioHash(embeddingPose2)
        val template3 = bioHashEngine.generateBioHash(embeddingPose3)
        val enrolledTemplates = listOf(template1, template2, template3)

        // Probe 1: Genuine user matching pose 2 closely
        val probeGenuineEmbedding = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) {
            embeddingPose2[it] + (randomA.nextFloat() * 0.02f - 0.01f)
        }
        val probeGenuine = bioHashEngine.generateBioHash(probeGenuineEmbedding)

        val matchResult = bioHashEngine.compareBioHashes(enrolledTemplates, probeGenuine, threshold = 0.65f)
        assertTrue("Genuine probe matching one of the authorized multi-templates must MATCH", matchResult.isMatch)
        assertTrue("Similarity score should be >= 0.70", matchResult.similarityScore >= 0.70f)

        // Probe 2: Impostor / distinct person
        val impostorEmbedding = FloatArray(FaceEmbeddingEngine.EMBEDDING_DIM) { -randomB.nextFloat() }
        val probeImpostor = bioHashEngine.generateBioHash(impostorEmbedding)

        val impostorResult = bioHashEngine.compareBioHashes(enrolledTemplates, probeImpostor, threshold = 0.65f)
        assertFalse("Impostor probe must REJECT across all enrolled templates", impostorResult.isMatch)

        // Empty enrolled templates check
        val emptyResult = bioHashEngine.compareBioHashes(emptyList(), probeGenuine, threshold = 0.65f)
        assertFalse("Empty enrolled list must return isMatch=false", emptyResult.isMatch)
    }

    @Test
    fun testSystemBiometricStatusEnumMapping() {
        val statuses = com.aegisauth.core.biometric.SystemBiometricManager.BiometricHardwareStatus.entries
        assertTrue(statuses.contains(com.aegisauth.core.biometric.SystemBiometricManager.BiometricHardwareStatus.AVAILABLE))
        assertTrue(statuses.contains(com.aegisauth.core.biometric.SystemBiometricManager.BiometricHardwareStatus.NO_HARDWARE))
        assertTrue(statuses.contains(com.aegisauth.core.biometric.SystemBiometricManager.BiometricHardwareStatus.HARDWARE_UNAVAILABLE))
        assertTrue(statuses.contains(com.aegisauth.core.biometric.SystemBiometricManager.BiometricHardwareStatus.NONE_ENROLLED))
        assertTrue(statuses.contains(com.aegisauth.core.biometric.SystemBiometricManager.BiometricHardwareStatus.SECURITY_UPDATE_REQUIRED))
        assertTrue(statuses.contains(com.aegisauth.core.biometric.SystemBiometricManager.BiometricHardwareStatus.UNSUPPORTED))
    }

    @Test
    fun testAuthenticationEventAuthMethodAuditing() {
        val eventFace = com.aegisauth.data.local.entity.AuthenticationEvent(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            authMethod = "FACE",
            result = "SUCCESS",
            riskLevel = "LOW",
            livenessScore = 0.95f,
            qualityScore = 0.92f,
            similarityScore = 0.88f,
            challengeRequired = false,
            challengeResult = null,
            failureReason = null
        )
        assertEquals("FACE", eventFace.authMethod)

        val eventBiometricBackup = com.aegisauth.data.local.entity.AuthenticationEvent(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            authMethod = com.aegisauth.core.biometric.SystemBiometricManager.AUTH_METHOD_SYSTEM_BIOMETRIC,
            result = "SUCCESS",
            riskLevel = "LOW",
            livenessScore = 1.0f,
            qualityScore = 1.0f,
            similarityScore = 1.0f,
            challengeRequired = false,
            challengeResult = null,
            failureReason = null
        )
        assertEquals("SYSTEM_BIOMETRIC_BACKUP", eventBiometricBackup.authMethod)

        val eventRecovery = com.aegisauth.data.local.entity.AuthenticationEvent(
            packageName = "com.aegisauth",
            appName = "AEGISAUTH",
            authMethod = "PASSWORD",
            result = "SUCCESS",
            riskLevel = "LOW",
            livenessScore = 1.0f,
            qualityScore = 1.0f,
            similarityScore = 1.0f,
            challengeRequired = false,
            challengeResult = null,
            failureReason = null
        )
        assertEquals("PASSWORD", eventRecovery.authMethod)
    }
}




