package com.aegisauth.domain.risk

import com.aegisauth.domain.model.LivenessResult
import com.aegisauth.domain.model.QualityResult
import com.aegisauth.domain.model.RiskLevel
import com.aegisauth.domain.model.RiskResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskEngine @Inject constructor() {

    companion object {
        const val LOW_RISK_THRESHOLD = 0.30f
        const val HIGH_RISK_THRESHOLD = 0.65f
    }

    /**
     * Evaluates comprehensive authentication risk.
     *
     * @param liveness Liveness evaluation result
     * @param quality Image quality metrics
     * @param recentFailedAttempts Number of failed authentications in recent time window
     * @param activeChallengeMode Preference: "ALWAYS", "ADAPTIVE", "NEVER"
     */
    fun evaluateRisk(
        liveness: LivenessResult,
        quality: QualityResult,
        recentFailedAttempts: Int,
        activeChallengeMode: String = "ADAPTIVE"
    ): RiskResult {
        val reasons = mutableListOf<String>()
        var penaltyScore = 0.0f

        // 1. Liveness Risk
        if (liveness.score < 0.60f) {
            val livenessRisk = (1f - liveness.score) * 0.40f
            penaltyScore += livenessRisk
            reasons.add("Sub-optimal passive liveness (${(liveness.score * 100).toInt()}%)")
        }

        // 2. Image Quality Risk
        if (quality.overallScore < 0.70f) {
            val qualityRisk = (1f - quality.overallScore) * 0.25f
            penaltyScore += qualityRisk
            reasons.add("Marginal frame quality")
        }

        // 3. Pose / Extreme Angle Risk
        if (quality.poseScore < 0.70f) {
            penaltyScore += 0.15f
            reasons.add("Non-frontal face orientation")
        }

        // 4. Repeated Failed Attempts Risk
        if (recentFailedAttempts > 0) {
            val failurePenalty = (recentFailedAttempts * 0.20f).coerceAtMost(0.50f)
            penaltyScore += failurePenalty
            reasons.add("$recentFailedAttempts recent failed attempt(s) detected")
        }

        // Normalize overall risk score (0.0 = safe, 1.0 = dangerous)
        val normalizedRiskScore = penaltyScore.coerceIn(0.0f, 1.0f)

        // Determine Risk Level
        val level = when {
            normalizedRiskScore >= HIGH_RISK_THRESHOLD || recentFailedAttempts >= 2 -> RiskLevel.HIGH
            normalizedRiskScore >= LOW_RISK_THRESHOLD || recentFailedAttempts == 1 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        // Active Challenge Requirement based on Mode & Risk
        val requiresChallenge = when (activeChallengeMode) {
            "ALWAYS" -> true
            "NEVER" -> false
            else -> level == RiskLevel.HIGH || (level == RiskLevel.MEDIUM && liveness.score < 0.65f)
        }

        if (reasons.isEmpty()) {
            reasons.add("Optimal biometric confidence and safe context")
        }

        return RiskResult(
            level = level,
            riskScore = normalizedRiskScore,
            reasons = reasons,
            requiresActiveChallenge = requiresChallenge
        )
    }
}
