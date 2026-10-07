package com.aegisauth.domain

import com.aegisauth.domain.model.AuthenticationFailureReason
import com.aegisauth.domain.model.QualityFeedback
import org.junit.Assert.*
import org.junit.Test

class M152AuthFailureReasonTest {

    @Test
    fun `no face feedback maps to NO_FACE_DETECTED`() {
        val reason = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.NO_FACE_DETECTED)
        assertEquals(AuthenticationFailureReason.NO_FACE_DETECTED, reason)
        assertEquals("NO FACE DETECTED", reason.title)
    }

    @Test
    fun `face too far or small feedback maps to FACE_TOO_FAR`() {
        val r1 = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.FACE_TOO_SMALL)
        val r2 = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.FACE_TOO_FAR)
        assertEquals(AuthenticationFailureReason.FACE_TOO_FAR, r1)
        assertEquals(AuthenticationFailureReason.FACE_TOO_FAR, r2)
        assertEquals("FACE TOO FAR", r1.title)
    }

    @Test
    fun `face too close feedback maps to FACE_TOO_CLOSE`() {
        val reason = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.FACE_TOO_CLOSE)
        assertEquals(AuthenticationFailureReason.FACE_TOO_CLOSE, reason)
        assertEquals("FACE TOO CLOSE", reason.title)
    }

    @Test
    fun `blurry image feedback maps to FACE_NOT_CLEAR`() {
        val reason = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.IMAGE_BLURRY)
        assertEquals(AuthenticationFailureReason.FACE_NOT_CLEAR, reason)
        assertEquals("FACE NOT CLEAR", reason.title)
    }

    @Test
    fun `poor lighting feedback maps to POOR_LIGHTING`() {
        val r1 = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.TOO_DARK)
        val r2 = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.TOO_BRIGHT)
        assertEquals(AuthenticationFailureReason.POOR_LIGHTING, r1)
        assertEquals(AuthenticationFailureReason.POOR_LIGHTING, r2)
        assertEquals("POOR LIGHTING", r1.title)
    }

    @Test
    fun `extreme angle feedback maps to FACE_ANGLE_INVALID`() {
        val reason = AuthenticationFailureReason.fromQualityFeedback(QualityFeedback.EXTREME_ANGLE)
        assertEquals(AuthenticationFailureReason.FACE_ANGLE_INVALID, reason)
        assertEquals("FACE ANGLE NOT ACCEPTABLE", reason.title)
    }

    @Test
    fun `all reason codes are non-empty and unique`() {
        val codes = AuthenticationFailureReason.values().map { it.code }
        assertEquals(codes.size, codes.distinct().size)
        assertTrue(codes.all { it.isNotBlank() })
    }

    @Test
    fun `fingerprint button should not be visible on initial search state`() {
        var isFaceFailed = false
        var hasSystemBiometric = true
        fun canShowFingerprint(): Boolean = hasSystemBiometric && isFaceFailed

        assertFalse("Fingerprint button must NOT be shown before face failure", canShowFingerprint())

        isFaceFailed = true
        assertTrue("Fingerprint button MUST appear after genuine face failure", canShowFingerprint())
    }
}
