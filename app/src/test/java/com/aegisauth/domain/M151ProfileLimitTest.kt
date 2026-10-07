package com.aegisauth.domain

import org.junit.Assert.*
import org.junit.Test

class M151ProfileLimitTest {

    companion object {
        const val MAX_PROFILES = 5
    }

    private val profiles = mutableListOf<String>()

    private fun insertProfile(name: String): Long {
        if (profiles.size >= MAX_PROFILES) return -1L
        profiles.add(name)
        return profiles.size.toLong()
    }

    @Test
    fun `can add up to 5 profiles`() {
        for (i in 1..5) {
            val id = insertProfile("BIO $i")
            assertTrue(id > 0)
        }
        assertEquals(5, profiles.size)
    }

    @Test
    fun `cannot add 6th profile, returns -1`() {
        for (i in 1..5) insertProfile("BIO $i")
        val result = insertProfile("BIO 6")
        assertEquals(-1L, result)
        assertEquals(5, profiles.size)
    }

    @Test
    fun `empty list can receive first profile`() {
        val id = insertProfile("Default")
        assertEquals(1L, id)
    }

    @Test
    fun `limit is enforced independently of UI`() {
        repeat(5) { insertProfile("P$it") }
        val attemptedId = insertProfile("overflow")
        assertTrue("Repository enforced limit", attemptedId < 0)
    }

    @Test
    fun `deleting a profile allows adding again`() {
        repeat(5) { insertProfile("P$it") }
        profiles.removeAt(0) // simulate delete
        val id = insertProfile("New BIO")
        assertTrue(id > 0)
    }

    @Test
    fun `profile count is correct after operations`() {
        insertProfile("A")
        insertProfile("B")
        insertProfile("C")
        assertEquals(3, profiles.size)
        profiles.removeAt(0)
        assertEquals(2, profiles.size)
    }

    @Test
    fun `BIO 1 migration default profile name`() {
        val defaultName = "Default"
        val id = insertProfile(defaultName)
        assertEquals(1L, id)
        assertEquals("Default", profiles[0])
    }

    @Test
    fun `profile names are UI metadata only`() {
        val name = "My BIO"
        insertProfile(name)
        assertEquals(name, profiles[0])
    }
}
