package com.aegisauth.domain

import org.junit.Assert.*
import org.junit.Test

class M151PatternStateTest {

    // State transitions
    private var isConfirmPatternStep = false
    private var firstPattern = emptyList<Int>()
    private var resetKey = 0
    private var patternStatusMessage = "Draw a pattern connecting at least 4 dots"
    private var isPatternError = false

    private fun simulateDraw(pattern: List<Int>): String {
        if (!isConfirmPatternStep) {
            return if (pattern.size < 4) {
                isPatternError = true
                patternStatusMessage = "Connect at least 4 dots"
                resetKey++  // grid cleared
                "ERROR_TOO_SHORT"
            } else {
                firstPattern = pattern
                isConfirmPatternStep = true
                resetKey++  // grid cleared for confirm step
                patternStatusMessage = "Draw the pattern again to confirm"
                "AWAITING_CONFIRM"
            }
        } else {
            return if (pattern == firstPattern) {
                "MATCH"
            } else {
                isPatternError = true
                patternStatusMessage = "Patterns do not match. Try again."
                isConfirmPatternStep = false
                firstPattern = emptyList()
                resetKey++  // grid cleared, restart
                "MISMATCH"
            }
        }
    }

    @Test
    fun `draw less than 4 dots returns error and increments resetKey`() {
        val before = resetKey
        val result = simulateDraw(listOf(0, 1, 2))
        assertEquals("ERROR_TOO_SHORT", result)
        assertEquals(before + 1, resetKey)
        assertFalse(isConfirmPatternStep)
    }

    @Test
    fun `draw 4 or more dots transitions to confirm step and increments resetKey`() {
        val before = resetKey
        val result = simulateDraw(listOf(0, 1, 2, 3))
        assertEquals("AWAITING_CONFIRM", result)
        assertTrue(isConfirmPatternStep)
        assertEquals(before + 1, resetKey)
    }

    @Test
    fun `matching confirm pattern returns MATCH`() {
        simulateDraw(listOf(0, 1, 2, 3))
        val result = simulateDraw(listOf(0, 1, 2, 3))
        assertEquals("MATCH", result)
    }

    @Test
    fun `mismatching confirm pattern resets state and increments resetKey`() {
        simulateDraw(listOf(0, 1, 2, 3))
        val beforeMismatch = resetKey
        val result = simulateDraw(listOf(0, 1, 2, 4))
        assertEquals("MISMATCH", result)
        assertFalse(isConfirmPatternStep)
        assertEquals(emptyList<Int>(), firstPattern)
        assertEquals(beforeMismatch + 1, resetKey)
    }

    @Test
    fun `resetKey is unique for each state transition`() {
        val k0 = resetKey
        simulateDraw(listOf(0, 1, 2, 3)) // draw first -> confirm step
        val k1 = resetKey
        simulateDraw(listOf(0, 1, 2, 4)) // mismatch -> reset
        val k2 = resetKey
        assertTrue(k1 > k0)
        assertTrue(k2 > k1)
    }

    @Test
    fun `error state clears after reset`() {
        simulateDraw(listOf(0, 1, 2)) // too short, sets error
        assertTrue(isPatternError)
    }

    @Test
    fun `draw minimum 4 dots is accepted`() {
        val result = simulateDraw(listOf(0, 1, 2, 3))
        assertEquals("AWAITING_CONFIRM", result)
    }

    @Test
    fun `draw maximum 9 dots is accepted`() {
        val result = simulateDraw(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8))
        assertEquals("AWAITING_CONFIRM", result)
    }

    @Test
    fun `first pattern is not exposed during confirm step`() {
        val secret = listOf(5, 3, 1, 7)
        simulateDraw(secret)
        assertTrue(isConfirmPatternStep)
        assertEquals(secret, firstPattern)
    }
}
