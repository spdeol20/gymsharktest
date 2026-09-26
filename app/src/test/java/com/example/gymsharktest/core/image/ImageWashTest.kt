package com.example.gymsharktest.core.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageWashTest {

    @Test
    fun `a studio white photo has no wash`() {
        assertNull(sampleWashColor(solid(255, 255, 255, count = 64)))
    }

    @Test
    fun `a near black garment has no wash`() {
        assertNull(sampleWashColor(solid(12, 12, 14, count = 64)))
    }

    @Test
    fun `a coloured garment on white returns that colour`() {
        val pixels = solid(248, 248, 246, count = 80) + solid(180, 32, 48, count = 20)

        val wash = sampleWashColor(pixels)

        assertEquals(180, wash?.red)
        assertEquals(32, wash?.green)
        assertEquals(48, wash?.blue)
    }

    @Test
    fun `an empty sample has no wash`() {
        assertNull(sampleWashColor(IntArray(0)))
    }
}

private fun solid(red: Int, green: Int, blue: Int, count: Int): IntArray =
    IntArray(count) { (0xFF shl 24) or (red shl 16) or (green shl 8) or blue }
