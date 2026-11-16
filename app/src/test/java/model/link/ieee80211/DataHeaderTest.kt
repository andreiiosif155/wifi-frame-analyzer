package model.link.ieee80211

import model.link.MacAddress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DataHeaderTest {
    private val forwardedSrcAddr = MacAddress(0x020000000004L)
    private val qosControl = QosControl(5)

    @Test
    fun ordinaryDataHas24ByteHeader() {
        assertEquals(24, dataHeader().headerLengthBytes)
    }

    @Test
    fun fourAddressDataHas30ByteHeader() {
        val header = dataHeader(
            flags = FrameFlags(toDs = true, fromDs = true),
            srcOrBssid = forwardedSrcAddr
        )

        assertEquals(30, header.headerLengthBytes)
    }

    @Test
    fun qosDataHas26ByteHeader() {
        val header = dataHeader(subtypeCode = 8, qosControl = qosControl)

        assertEquals(26, header.headerLengthBytes)
    }

    @Test
    fun fourAddressQosDataHas32ByteHeader() {
        val header = dataHeader(
            subtypeCode = 8,
            flags = FrameFlags(toDs = true, fromDs = true),
            srcOrBssid = forwardedSrcAddr,
            qosControl = qosControl
        )

        assertEquals(32, header.headerLengthBytes)
    }

    @Test
    fun qosDataWithHtControlHas30ByteHeader() {
        val header = dataHeader(
            subtypeCode = 8,
            flags = FrameFlags(order = true),
            qosControl = qosControl,
            htControl = 0
        )

        assertEquals(30, header.headerLengthBytes)
    }

    @Test
    fun fourAddressQosDataWithHtControlHas36ByteHeader() {
        val header = dataHeader(
            subtypeCode = 8,
            flags = FrameFlags(toDs = true, fromDs = true, order = true),
            srcOrBssid = forwardedSrcAddr,
            qosControl = qosControl,
            htControl = 0
        )

        assertEquals(36, header.headerLengthBytes)
    }

    @Test
    fun oneDsFlagDoesNotRequireOptionalAddress() {
        for (flags in listOf(FrameFlags(toDs = true), FrameFlags(fromDs = true))) {
            assertEquals(24, dataHeader(flags = flags).headerLengthBytes)
        }
    }

    @Test
    fun requiresOptionalAddressWhenBothDsFlagsAreSet() {
        assertThrows(IllegalArgumentException::class.java) {
            dataHeader(flags = FrameFlags(toDs = true, fromDs = true))
        }
    }

    @Test
    fun rejectsOptionalAddressUnlessBothDsFlagsAreSet() {
        val flagCases = listOf(FrameFlags(), FrameFlags(toDs = true), FrameFlags(fromDs = true))

        for (flags in flagCases) {
            assertThrows(IllegalArgumentException::class.java) {
                dataHeader(flags = flags, srcOrBssid = forwardedSrcAddr)
            }
        }
    }

    @Test
    fun qosNullAlsoCarriesQosControl() {
        val header = dataHeader(subtypeCode = 12, qosControl = qosControl)

        assertEquals(26, header.headerLengthBytes)
    }

    @Test
    fun requiresQosControlForQosDataAndQosNull() {
        for (subtypeCode in listOf(8, 12)) {
            assertThrows(IllegalArgumentException::class.java) {
                dataHeader(subtypeCode = subtypeCode)
            }
        }
    }

    @Test
    fun rejectsQosControlForNonQosSubtypes() {
        for (subtypeCode in listOf(0, 4)) {
            assertThrows(IllegalArgumentException::class.java) {
                dataHeader(subtypeCode = subtypeCode, qosControl = qosControl)
            }
        }
    }

    @Test
    fun requiresHtControlForOrderedQosData() {
        assertThrows(IllegalArgumentException::class.java) {
            dataHeader(subtypeCode = 8, flags = FrameFlags(order = true), qosControl = qosControl)
        }
    }

    @Test
    fun rejectsHtControlWithoutOrder() {
        assertThrows(IllegalArgumentException::class.java) {
            dataHeader(subtypeCode = 8, qosControl = qosControl, htControl = 0)
        }
    }

    @Test
    fun orderOnNonQosDataDoesNotAddHtControl() {
        assertEquals(24, dataHeader(flags = FrameFlags(order = true)).headerLengthBytes)
    }

    @Test
    fun rejectsHtControlForOrderedNonQosData() {
        assertThrows(IllegalArgumentException::class.java) {
            dataHeader(flags = FrameFlags(order = true), htControl = 0)
        }
    }

    private fun dataHeader(
        subtypeCode: Int = 0,
        flags: FrameFlags = FrameFlags(),
        srcOrBssid: MacAddress? = null,
        qosControl: QosControl? = null,
        htControl: Int? = null
    ): DataHeader = DataHeader(
        rxAddr = MacAddress(0x020000000001L),
        txAddr = MacAddress(0x020000000002L),
        address3 = MacAddress(0x020000000003L),
        address4 = srcOrBssid,
        sequenceControl = SequenceControl(100, 0),
        frameControl = FrameControl(0, FrameType.DATA, subtypeCode, flags),
        durationId = 10,
        htControl = htControl,
        qosControl = qosControl
    )
}
