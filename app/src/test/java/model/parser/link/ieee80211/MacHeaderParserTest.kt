package model.parser.link.ieee80211

import model.link.MacAddress
import model.link.ieee80211.ControlHeader
import model.link.ieee80211.ControlSubtype
import model.link.ieee80211.DataHeader
import model.link.ieee80211.DataSubtype
import model.link.ieee80211.FrameType
import model.link.ieee80211.ManagementHeader
import model.link.ieee80211.ManagementSubtype
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MacHeaderParserTest {
    @Test
    fun parserTest() {
        // octetii peste 127 verifica interpretarea fara semn
        val bytes: ByteArray = byteArrayOf(
            0x55, 0x66, 0x00, 0x1A,
            0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(),
            0x01, 0x77, 0x88.toByte()
        )
        val offset: Int = 2

        val macAddress: MacAddress =
            MacHeaderParser().readMacAddress(bytes, offset)

        assertEquals(MacAddress(0x001AABCDEF01), macAddress)
    }

    @Test
    fun readUInt16LittleEndianTest() {
        val bytes: ByteArray = byteArrayOf(0x55, 0x34, 0x12, 0x66)
        val offset: Int = 1
        val parser: MacHeaderParser = MacHeaderParser()

        val value: Int = parser.readUInt16LittleEndian(bytes, offset)

        assertEquals(0x1234, value)
    }

    @Test
    fun readSimpleDataHeaderWithNonZeroOffset() {
        val bytes: ByteArray = byteArrayOf(
            0x55, 0x66,
            0x08, 0x00,
            0x34, 0x12,
            0x00, 0x1A, 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x01,
            0x10, 0x20, 0x30, 0x40, 0x50, 0x60,
            0xA0.toByte(), 0xB0.toByte(), 0xC0.toByte(),
            0xD0.toByte(), 0xE0.toByte(), 0xF0.toByte(),
            0x35, 0x12
        )
        val offset = 2
        val parser = MacHeaderParser()

        val header = parser.readHeader(bytes, offset)

        assertTrue(header is DataHeader)
        val dataHeader : DataHeader = header as DataHeader
        assertEquals(FrameType.DATA, dataHeader.frameControl.type)
        assertEquals(DataSubtype.DATA.code, dataHeader.frameControl.subtypeCode)
        assertEquals(0x1234, dataHeader.durationId)
        assertEquals(MacAddress(0x001AABCDEF01L), dataHeader.rxAddr)
        assertEquals(MacAddress(0x102030405060L), dataHeader.txAddr)
        assertEquals(MacAddress(0xA0B0C0D0E0F0L), dataHeader.address3)

        assertEquals(0x123, dataHeader.sequenceControl.sequenceNumber)
        assertEquals(5, dataHeader.sequenceControl.fragmentNumber)
        assertEquals(24, dataHeader.headerLengthBytes)
        assertNull(dataHeader.address4)
        assertNull(dataHeader.qosControl)
        assertNull(dataHeader.htControl)
    }

    @Test
    fun readAckHeaderWithNonZeroOffset() {
        val header = MacHeaderParser().readHeader(ackBytes, 2)

        assertTrue(header is ControlHeader)
        val controlHeader = header as ControlHeader
        assertEquals(FrameType.CONTROL, controlHeader.frameControl.type)
        assertEquals(ControlSubtype.ACK.code, controlHeader.frameControl.subtypeCode)
        assertEquals(0x1234, controlHeader.durationId)
        assertEquals(MacAddress(0x001AABCDEF01L), controlHeader.rxAddr)
        assertNull(controlHeader.txAddr)
        assertEquals(10, controlHeader.headerLengthBytes)
    }

    @Test
    fun readCtsHeaderWithNonZeroOffset() {
        val header = MacHeaderParser().readHeader(ctsBytes, 2)

        assertTrue(header is ControlHeader)
        val controlHeader = header as ControlHeader
        assertEquals(FrameType.CONTROL, controlHeader.frameControl.type)
        assertEquals(ControlSubtype.CTS.code, controlHeader.frameControl.subtypeCode)
        assertEquals(0x1234, controlHeader.durationId)
        assertEquals(MacAddress(0x001AABCDEF01L), controlHeader.rxAddr)
        assertNull(controlHeader.txAddr)
        assertEquals(10, controlHeader.headerLengthBytes)
    }

    @Test
    fun readRtsHeaderWithNonZeroOffset() {
        val header = MacHeaderParser().readHeader(rtsBytes, 2)

        assertTrue(header is ControlHeader)
        val controlHeader = header as ControlHeader
        assertEquals(FrameType.CONTROL, controlHeader.frameControl.type)
        assertEquals(ControlSubtype.RTS.code, controlHeader.frameControl.subtypeCode)
        assertEquals(0x1234, controlHeader.durationId)
        assertEquals(MacAddress(0x001AABCDEF01L), controlHeader.rxAddr)
        assertEquals(MacAddress(0x102030405060L), controlHeader.txAddr)
        assertEquals(16, controlHeader.headerLengthBytes)
    }

    @Test
    fun readManagementHeaderWithNonZeroOffset() {
        val header = MacHeaderParser().readHeader(managementBytes, 2)

        assertTrue(header is ManagementHeader)
        val managementHeader = header as ManagementHeader
        assertEquals(FrameType.MANAGEMENT, managementHeader.frameControl.type)
        assertEquals(ManagementSubtype.PROBE_REQUEST.code, managementHeader.frameControl.subtypeCode)
        assertEquals(0x1234, managementHeader.durationId)
        assertEquals(MacAddress(0x001AABCDEF01L), managementHeader.dstAddr)
        assertEquals(MacAddress(0x102030405060L), managementHeader.srcAddr)
        assertEquals(MacAddress(0xA0B0C0D0E0F0L), managementHeader.bssid)
        assertEquals(0x123, managementHeader.sequenceControl.sequenceNumber)
        assertEquals(5, managementHeader.sequenceControl.fragmentNumber)
        assertEquals(24, managementHeader.headerLengthBytes)
        assertNull(managementHeader.htControl)
    }

    @Test
    fun readManagementHeaderWithHtControlAndNonZeroOffset() {
        val header = MacHeaderParser().readHeader(managementHtBytes, 2)

        assertTrue(header is ManagementHeader)
        val managementHeader = header as ManagementHeader
        assertEquals(FrameType.MANAGEMENT, managementHeader.frameControl.type)
        assertEquals(13, managementHeader.frameControl.subtypeCode)
        assertTrue(managementHeader.frameControl.flags.order)
        assertEquals(0x1234, managementHeader.durationId)
        assertEquals(MacAddress(0x001AABCDEF01L), managementHeader.dstAddr)
        assertEquals(MacAddress(0x102030405060L), managementHeader.srcAddr)
        assertEquals(MacAddress(0xA0B0C0D0E0F0L), managementHeader.bssid)
        assertEquals(0x123, managementHeader.sequenceControl.sequenceNumber)
        assertEquals(5, managementHeader.sequenceControl.fragmentNumber)
        assertEquals(28, managementHeader.headerLengthBytes)
        assertEquals(0x89ABCDEF.toInt(), managementHeader.htControl)
    }

    @Test
    fun rejectsTruncatedControlHeaders() {
        val parser = MacHeaderParser()
        val fixtures = listOf("ACK" to ackBytes, "CTS" to ctsBytes, "RTS" to rtsBytes)

        for ((name, bytes) in fixtures) {
            for (headerBytes in 0 until bytes.size - 2) {
                val truncated = bytes.copyOf(2 + headerBytes)
                assertThrows("$name with $headerBytes header bytes", IllegalArgumentException::class.java) {
                    parser.readHeader(truncated, 2)
                }
            }
        }
    }

    @Test
    fun rejectsTruncatedManagementHeaders() {
        val parser = MacHeaderParser()
        val fixtures = listOf("Management" to managementBytes, "Management HT" to managementHtBytes)

        for ((name, bytes) in fixtures) {
            for (headerBytes in 0 until bytes.size - 2) {
                val truncated = bytes.copyOf(2 + headerBytes)
                assertThrows("$name with $headerBytes header bytes", IllegalArgumentException::class.java) {
                    parser.readHeader(truncated, 2)
                }
            }
        }
    }

    @Test
    fun rejectsUnsupportedControlSubtype() {
        val bytes = rtsBytes.copyOf()
        bytes[2] = 0x04

        val exception = assertThrows(IllegalArgumentException::class.java) {
            MacHeaderParser().readHeader(bytes, 2)
        }

        assertEquals("Unsupported control subtype", exception.message)
    }

    // prefixul verifica citirea antetului din mijlocul bufferului
    private val ackBytes = byteArrayOf(
        0x55, 0x66,
        0xD4.toByte(), 0x00,
        0x34, 0x12,
        0x00, 0x1A, 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x01
    )

    private val ctsBytes = byteArrayOf(
        0x55, 0x66,
        0xC4.toByte(), 0x00,
        0x34, 0x12,
        0x00, 0x1A, 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x01
    )

    private val rtsBytes = byteArrayOf(
        0x55, 0x66,
        0xB4.toByte(), 0x00,
        0x34, 0x12,
        0x00, 0x1A, 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x01,
        0x10, 0x20, 0x30, 0x40, 0x50, 0x60
    )

    private val managementBytes = byteArrayOf(
        0x55, 0x66,
        0x40, 0x00,
        0x34, 0x12,
        0x00, 0x1A, 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x01,
        0x10, 0x20, 0x30, 0x40, 0x50, 0x60,
        0xA0.toByte(), 0xB0.toByte(), 0xC0.toByte(),
        0xD0.toByte(), 0xE0.toByte(), 0xF0.toByte(),
        0x35, 0x12
    )

    private val managementHtBytes = byteArrayOf(
        0x55, 0x66,
        0xD0.toByte(), 0x80.toByte(),
        0x34, 0x12,
        0x00, 0x1A, 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x01,
        0x10, 0x20, 0x30, 0x40, 0x50, 0x60,
        0xA0.toByte(), 0xB0.toByte(), 0xC0.toByte(),
        0xD0.toByte(), 0xE0.toByte(), 0xF0.toByte(),
        0x35, 0x12,
        0xEF.toByte(), 0xCD.toByte(), 0xAB.toByte(), 0x89.toByte()
    )
}
