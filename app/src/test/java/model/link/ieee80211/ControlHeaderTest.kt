package model.link.ieee80211

import model.link.MacAddress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ControlHeaderTest {
    private val rxAddr = MacAddress(0x001122334455L)
    private val txAddr = MacAddress(0x66778899AABBL)

    private val receiverOnlySubtypes = listOf(
        ControlSubtype.ACK,
        ControlSubtype.CTS
    )

    private val transmitterAddressSubtypes = listOf(
        ControlSubtype.TRIGGER,
        ControlSubtype.BLOCK_ACK_REQUEST,
        ControlSubtype.BLOCK_ACK,
        ControlSubtype.PS_POLL,
        ControlSubtype.RTS,
        ControlSubtype.CF_END,
        ControlSubtype.CF_END_ACK
    )

    @Test
    fun ackAndCtsHaveTenByteCommonHeaders() {
        for (subtype in receiverOnlySubtypes) {
            val header = createHeader(subtype)
            assertEquals(subtype.name, 10, header.headerLengthBytes)
        }
    }

    @Test
    fun supportedTwoAddressSubtypesHaveSixteenByteCommonHeaders() {
        for (subtype in transmitterAddressSubtypes) {
            val header = createHeader(subtype, txAddr)
            assertEquals(subtype.name, 16, header.headerLengthBytes)
        }
    }

    @Test
    fun ackAndCtsRejectTransmitterAddress() {
        for (subtype in receiverOnlySubtypes) {
            assertThrows(subtype.name, IllegalArgumentException::class.java) {
                createHeader(subtype, txAddr)
            }
        }
    }

    @Test
    fun rejectsMissingTransmitterAddressWhenSubtypeRequiresIt() {
        for (subtype in transmitterAddressSubtypes) {
            assertThrows(subtype.name, IllegalArgumentException::class.java) {
                createHeader(subtype)
            }
        }
    }

    @Test
    fun controlExtensionIsRejectedAsUnsupported() {
        val frameControl = FrameControl(0, FrameType.CONTROL, 6, FrameFlags())

        val exception = assertThrows(IllegalArgumentException::class.java) {
            ControlHeader(rxAddr, frameControl, 0, txAddr)
        }

        assertEquals("Unsupported control subtype", exception.message)
    }

    @Test
    fun rejectsAFrameControlFromAnotherFamily() {
        val frameControl = FrameControl(
            0, FrameType.DATA, ControlSubtype.RTS.code, FrameFlags()
        )

        assertThrows(IllegalArgumentException::class.java) {
            ControlHeader(rxAddr, frameControl, 0, txAddr)
        }
    }

    @Test
    fun rejectsAnUnsupportedProtocolVersion() {
        val frameControl = FrameControl(
            1, FrameType.CONTROL, ControlSubtype.RTS.code, FrameFlags()
        )

        assertThrows(IllegalArgumentException::class.java) {
            ControlHeader(rxAddr, frameControl, 0, txAddr)
        }
    }

    private fun createHeader(
        subtype: ControlSubtype,
        txAddr: MacAddress? = null
    ): ControlHeader {
        val frameControl = FrameControl(0, FrameType.CONTROL, subtype.code, FrameFlags())
        val durationId = if (subtype == ControlSubtype.PS_POLL) 0xC123 else 0

        return ControlHeader(rxAddr, frameControl, durationId, txAddr)
    }
}
