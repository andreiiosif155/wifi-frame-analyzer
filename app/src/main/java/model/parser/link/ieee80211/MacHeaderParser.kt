package model.parser.link.ieee80211

import model.link.MAC_ADDRESS_SIZE_BYTES
import model.link.MacAddress
import model.link.ieee80211.ControlHeader
import model.link.ieee80211.ControlSubtype
import model.link.ieee80211.DataHeader
import model.link.ieee80211.FrameControl
import model.link.ieee80211.FrameFlags
import model.link.ieee80211.FrameType
import model.link.ieee80211.Ieee80211Header
import model.link.ieee80211.MANAGEMENT_DATA_BASE_HEADER_SIZE_BYTES
import model.link.ieee80211.ManagementHeader
import model.link.ieee80211.QOS_CONTROL_SIZE_BYTES
import model.link.ieee80211.QOS_SUBTYPE_MASK
import model.link.ieee80211.QosControl
import model.link.ieee80211.SUPPORTED_PROTOCOL_VERSION
import model.link.ieee80211.SequenceControl

private const val UINT16_SIZE_BYTES = 2
private const val UINT32_SIZE_BYTES = 4
private const val BITS_PER_BYTE = 8

private const val DURATION_ID_OFFSET = 2
private const val ADDRESS_1_OFFSET = 4
private const val ADDRESS_2_OFFSET = 10
private const val ADDRESS_3_OFFSET = 16
private const val SEQUENCE_CONTROL_OFFSET = 22
private const val MANAGEMENT_HT_CONTROL_OFFSET = 24

private const val PROTOCOL_VERSION_MASK = 0x03
private const val FRAME_TYPE_SHIFT = 2
private const val FRAME_TYPE_MASK = 0x03
private const val FRAME_SUBTYPE_SHIFT = 4
private const val FRAME_SUBTYPE_MASK = 0x0F
private const val FLAG_MASK = 0x01
private const val TO_DS_SHIFT = 8
private const val FROM_DS_SHIFT = 9
private const val MORE_FRAGMENTS_SHIFT = 10
private const val RETRY_SHIFT = 11
private const val POWER_MANAGEMENT_SHIFT = 12
private const val MORE_DATA_SHIFT = 13
private const val PROTECTED_FRAME_SHIFT = 14
private const val ORDER_SHIFT = 15

private const val SEQUENCE_NUMBER_SHIFT = 4
private const val FRAGMENT_NUMBER_MASK = 0x0F

class MacHeaderParser {
    fun readMacAddress(bytes: ByteArray, offset: Int): MacAddress {
        require(offset >= 0)

        require(offset <= bytes.size - MAC_ADDRESS_SIZE_BYTES) {
            "Not enough bytes to read a MAC address"
        }

        var addressValue: Long = 0

        val addressBytes: ByteArray = bytes.copyOfRange(offset, offset + MAC_ADDRESS_SIZE_BYTES)

        for (byte in addressBytes) {
            val unsignedByte = byte.toUByte()
            addressValue = addressValue shl BITS_PER_BYTE
            addressValue += unsignedByte.toLong()
        }

        return MacAddress(addressValue)
    }

    fun readUInt16LittleEndian(bytes: ByteArray, offset: Int): Int {
        require(offset >= 0)

        require(offset <= bytes.size - UINT16_SIZE_BYTES) {
            "Not enough bytes to read a 16-bit value"
        }

        var parsedValue: Int = 0
        val lowByte = bytes[offset].toUByte().toInt()
        val highByte = bytes[offset + 1].toUByte().toInt()

        parsedValue = parsedValue + highByte
        parsedValue = parsedValue shl BITS_PER_BYTE
        parsedValue = parsedValue + lowByte
        return parsedValue
    }

    fun readUInt32LittleEndian(bytes: ByteArray, offset: Int): Int {
        require(offset >= 0)

        require(offset <= bytes.size - UINT32_SIZE_BYTES) {
            "Not enough bytes to read a 32-bit value"
        }

        var valueBits: Int = 0
        val lowByte = bytes[offset].toUByte().toInt()
        val middleLowByte = bytes[offset + 1].toUByte().toInt()
        val middleHighByte = bytes[offset + 2].toUByte().toInt()
        val highByte = bytes[offset + 3].toUByte().toInt()

        valueBits = valueBits + highByte
        valueBits = valueBits shl BITS_PER_BYTE
        valueBits = valueBits + middleHighByte
        valueBits = valueBits shl BITS_PER_BYTE
        valueBits = valueBits + middleLowByte
        valueBits = valueBits shl BITS_PER_BYTE
        valueBits = valueBits + lowByte
        // pastram cei 32 de biti pentru HT Control chiar daca Int este negativ
        return valueBits
    }

    fun readFrameControl(bytes: ByteArray, offset: Int): FrameControl {
        val frameControlBits = this.readUInt16LittleEndian(bytes, offset)
        val protocolVersion: Int = frameControlBits and PROTOCOL_VERSION_MASK
        require(protocolVersion == SUPPORTED_PROTOCOL_VERSION) {
            "Putem procesa doar versiuna 0"
        }
        var frameType: FrameType?
        frameType =
            FrameType.fromCode((frameControlBits shr FRAME_TYPE_SHIFT) and FRAME_TYPE_MASK)

        require(frameType != null) {
            "Type is not correct"
        }


        val subtypeCode: Int = (frameControlBits shr FRAME_SUBTYPE_SHIFT) and FRAME_SUBTYPE_MASK


        val toDs: Boolean =
            when ((frameControlBits shr TO_DS_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }

        val fromDs: Boolean =
            when ((frameControlBits shr FROM_DS_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }

        val moreFragments: Boolean =
            when ((frameControlBits shr MORE_FRAGMENTS_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }
        val retry: Boolean =
            when ((frameControlBits shr RETRY_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }
        val powerManagement: Boolean =
            when ((frameControlBits shr POWER_MANAGEMENT_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }
        val moreData: Boolean =
            when ((frameControlBits shr MORE_DATA_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }
        val protectedFrame: Boolean =
            when ((frameControlBits shr PROTECTED_FRAME_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }
        val order: Boolean =
            when ((frameControlBits shr ORDER_SHIFT) and FLAG_MASK) {
                0 -> false
                else -> true
            }

        return FrameControl(
            protocolVersion, frameType, subtypeCode,
            FrameFlags(
                toDs = toDs, fromDs = fromDs,
                retry = retry, powerManagement = powerManagement,
                moreData = moreData, protectedFrame = protectedFrame,
                order = order, moreFragments = moreFragments
            )
        )

    }

    fun readSequenceControl(bytes: ByteArray, offset: Int): SequenceControl {
        val sequenceControlBits = this.readUInt16LittleEndian(bytes, offset)
        val sequenceNumber = (sequenceControlBits shr SEQUENCE_NUMBER_SHIFT)
        val fragmentNumber = (sequenceControlBits and FRAGMENT_NUMBER_MASK)

        return SequenceControl(sequenceNumber, fragmentNumber)
    }

    fun readQosControl(bytes: ByteArray, offset: Int): QosControl {
        return QosControl(this.readUInt16LittleEndian(bytes, offset))
    }

    // offsetul indica inceputul antetului MAC
    fun readManagementHeader(bytes: ByteArray, offset: Int, frameControl: FrameControl): ManagementHeader {
        require(frameControl.type == FrameType.MANAGEMENT) {
            "The header is not the expected type: Management"
        }

        val durationId = this.readUInt16LittleEndian(bytes, offset + DURATION_ID_OFFSET)
        val dstAddr = this.readMacAddress(bytes, offset + ADDRESS_1_OFFSET)
        val srcAddr = this.readMacAddress(bytes, offset + ADDRESS_2_OFFSET)
        val bssid = this.readMacAddress(bytes, offset + ADDRESS_3_OFFSET)
        val sequenceControl = this.readSequenceControl(bytes, offset + SEQUENCE_CONTROL_OFFSET)
        var htControl: Int? = null
        if (frameControl.flags.order) {
            htControl = this.readUInt32LittleEndian(bytes, offset + MANAGEMENT_HT_CONTROL_OFFSET)
        }

        return ManagementHeader(
            dstAddr, srcAddr, bssid, sequenceControl,
            frameControl, durationId, htControl
        )

    }

    fun readControlHeader(bytes: ByteArray, offset: Int, frameControl: FrameControl): ControlHeader {
        require(frameControl.type == FrameType.CONTROL) {
            "The header is not the expected type: Control"
        }
        val durationId = this.readUInt16LittleEndian(bytes, offset + DURATION_ID_OFFSET)
        val rxAddr = this.readMacAddress(bytes, offset + ADDRESS_1_OFFSET)
        var txAddr : MacAddress? = null
        val subtypeCode = frameControl.subtypeCode

        if (subtypeCode != ControlSubtype.ACK.code && subtypeCode != ControlSubtype.CTS.code) {
            txAddr = this.readMacAddress(bytes, offset + ADDRESS_2_OFFSET)
        }

        return ControlHeader(rxAddr, frameControl, durationId, txAddr);

    }

    fun readDataHeader(bytes: ByteArray, offset: Int, frameControl: FrameControl): DataHeader {
        require(frameControl.type == FrameType.DATA) {
            "The header is not the expected type: Data"
        }

        val durationId = this.readUInt16LittleEndian(bytes, offset + DURATION_ID_OFFSET)
        val rxAddr = this.readMacAddress(bytes, offset + ADDRESS_1_OFFSET)
        val txAddr = this.readMacAddress(bytes, offset + ADDRESS_2_OFFSET)
        val srcOrDstOrBssid = this.readMacAddress(bytes, offset + ADDRESS_3_OFFSET)
        val sequenceControl = this.readSequenceControl(bytes, offset + SEQUENCE_CONTROL_OFFSET)

        // pozitia QoS Control depinde de prezenta adresei suplimentare
        var nextFieldOffset = offset + MANAGEMENT_DATA_BASE_HEADER_SIZE_BYTES
        var srcOrBssid: MacAddress? = null
        if (frameControl.flags.toDs && frameControl.flags.fromDs) {
            srcOrBssid = this.readMacAddress(bytes, nextFieldOffset)
            nextFieldOffset += MAC_ADDRESS_SIZE_BYTES
        }

        val hasQosControl = (frameControl.subtypeCode and QOS_SUBTYPE_MASK) != 0
        var qosControl: QosControl? = null
        if (hasQosControl) {
            qosControl = this.readQosControl(bytes, nextFieldOffset)
            nextFieldOffset += QOS_CONTROL_SIZE_BYTES
        }

        var htControl: Int? = null
        if (hasQosControl && frameControl.flags.order) {
            htControl = this.readUInt32LittleEndian(bytes, nextFieldOffset)
        }

        return DataHeader(
            rxAddr = rxAddr, txAddr = txAddr,
            address3 = srcOrDstOrBssid, address4 = srcOrBssid,
            sequenceControl = sequenceControl, frameControl = frameControl,
            durationId = durationId, htControl = htControl,
            qosControl = qosControl
        )
    }

    fun readHeader(bytes: ByteArray, offset: Int): Ieee80211Header {
        val frameControl = this.readFrameControl(bytes, offset)

        return when(frameControl.type) {
            FrameType.DATA -> this.readDataHeader(bytes, offset, frameControl)
            FrameType.MANAGEMENT -> this.readManagementHeader(bytes, offset, frameControl)
            FrameType.CONTROL -> this.readControlHeader(bytes, offset, frameControl)
            FrameType.EXTENSION ->
                throw IllegalArgumentException("Unsupported frame type: ${frameControl.type}")
        }
    }

}
