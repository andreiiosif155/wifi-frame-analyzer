package model.capture

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class RawInfo80211Test {
    @Test
    fun modifyingInputBufferDoesNotChangeStoredFrameBytes() {
        val inputFrameBytes = byteArrayOf(10, 20, 30)
        val frame = RawInfo80211(inputFrameBytes)

        inputFrameBytes[1] = 5

        assertEquals(3, frame.frameSizeBytes)
        assertArrayEquals(byteArrayOf(10, 20, 30), frame.copyBytes())
    }

    @Test
    fun modifyingReturnedCopyDoesNotChangeStoredFrameBytes() {
        val frame = RawInfo80211(byteArrayOf(10, 20, 30))
        val copiedFrameBytes = frame.copyBytes()

        copiedFrameBytes[1] = 5

        assertArrayEquals(byteArrayOf(10, 20, 30), frame.copyBytes())
    }
}
