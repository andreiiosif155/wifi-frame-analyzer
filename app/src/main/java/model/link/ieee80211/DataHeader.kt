package model.link.ieee80211

import model.link.MAC_ADDRESS_SIZE_BYTES
import model.link.MacAddress

class DataHeader(
    val rxAddr: MacAddress, val txAddr: MacAddress,
    // rolul acestor doua adrese depinde de directie si de agregare
    val address3: MacAddress, val address4: MacAddress? = null,
    val sequenceControl: SequenceControl, override val frameControl: FrameControl,
    override val durationId: Int, val htControl: Int? = null,
    val qosControl: QosControl? = null
) : Ieee80211Header {

    override val headerLengthBytes: Int =
        MANAGEMENT_DATA_BASE_HEADER_SIZE_BYTES +
                (if (address4 != null) MAC_ADDRESS_SIZE_BYTES else 0) +
                (if (qosControl != null) QOS_CONTROL_SIZE_BYTES else 0) +
                (if (htControl != null) HT_CONTROL_SIZE_BYTES else 0)

    init {
        require(frameControl.protocolVersion == SUPPORTED_PROTOCOL_VERSION) {
            "Only protocol version 0 is supported"
        }
        require(frameControl.type == FrameType.DATA)

        require(durationId in 0..MAX_DURATION_ID)

        val flags = frameControl.flags

        val needsSrcOrBssid = flags.toDs && flags.fromDs

        val hasQosControl = (frameControl.subtypeCode and QOS_SUBTYPE_MASK) != 0

        require((address4 != null) == needsSrcOrBssid)
        require((qosControl != null) == hasQosControl)

        require((htControl != null) == (hasQosControl && flags.order))
    }
}
