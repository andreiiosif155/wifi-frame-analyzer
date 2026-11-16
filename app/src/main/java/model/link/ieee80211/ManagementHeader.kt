package model.link.ieee80211

import model.link.MacAddress

class ManagementHeader(
    val dstAddr: MacAddress, val srcAddr: MacAddress,
    val bssid: MacAddress, val sequenceControl: SequenceControl,
    override val frameControl: FrameControl, override val durationId: Int,
    val htControl: Int? = null
) : Ieee80211Header {
    override val headerLengthBytes: Int =
        if (htControl != null) MANAGEMENT_DATA_BASE_HEADER_SIZE_BYTES + HT_CONTROL_SIZE_BYTES
        else MANAGEMENT_DATA_BASE_HEADER_SIZE_BYTES

    init {
        require(frameControl.protocolVersion == SUPPORTED_PROTOCOL_VERSION) {
            "Only protocol version 0 is supported"
        }
        require(frameControl.type == FrameType.MANAGEMENT) {
            "Expected a management frame"
        }

        require(durationId in 0..MAX_DURATION_ID) {
            "Duration/ID must fit in 16 bits"
        }

        require((htControl != null) == frameControl.flags.order) {
            "HT Control presence must match the Order flag"
        }
    }
}
