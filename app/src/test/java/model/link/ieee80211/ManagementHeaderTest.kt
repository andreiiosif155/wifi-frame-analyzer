package model.link.ieee80211

import model.link.MacAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ManagementHeaderTest {
    @Test
    fun headerLengthTest24() {
        val testObj: ManagementHeader = ManagementHeader(
            MacAddress(0xAB65), MacAddress(0xAB66),
            MacAddress(0xAB67),
            SequenceControl(1, 1),
            FrameControl(0, FrameType.MANAGEMENT, 4,
                flags = FrameFlags()), 10, null);
        val testHeaderLength: Int = testObj.headerLengthBytes;
        assertEquals(24, testHeaderLength)
    }

    @Test
    fun headerLengthTest28() {
        val testObj: ManagementHeader = ManagementHeader(
            MacAddress(0xAB65), MacAddress(0xAB66),
            MacAddress(0xAB67),
            SequenceControl(1, 1),
            FrameControl(0, FrameType.MANAGEMENT, 4,
                flags = FrameFlags(order = true)), 10, 2);
        val testHeaderLength: Int = testObj.headerLengthBytes;
        assertEquals(28, testHeaderLength)
    }
    @Test
    fun requiresHtControlWhenOrderIsSet() {
        assertThrows(IllegalArgumentException::class.java) {
            managementHeader(flags = FrameFlags(order = true))
        }
    }

    @Test
    fun rejectsHtControlWhenOrderIsNotSet() {
        assertThrows(IllegalArgumentException::class.java) {
            managementHeader(htControl = 2)
        }
    }

    @Test
    fun zeroHtControlStillAddsFourBytes() {
        val header = managementHeader(flags = FrameFlags(order = true), htControl = 0)

        assertEquals(28, header.headerLengthBytes)
    }

    private fun managementHeader(
        flags: FrameFlags = FrameFlags(),
        htControl: Int? = null
    ): ManagementHeader = ManagementHeader(
        dstAddr = MacAddress(0x020000000001L),
        srcAddr = MacAddress(0x020000000002L),
        bssid = MacAddress(0x020000000003L),
        sequenceControl = SequenceControl(100, 0),
        frameControl = FrameControl(0, FrameType.MANAGEMENT, ManagementSubtype.PROBE_REQUEST.code, flags),
        durationId = 10,
        htControl = htControl
    )
}
