package model.link.ieee80211

import model.link.MacAddress

private const val CONTROL_BASE_HEADER_SIZE_BYTES = 10
private const val CONTROL_HEADER_WITH_TX_ADDR_SIZE_BYTES = 16

class ControlHeader(
    val rxAddr: MacAddress,
    override val frameControl: FrameControl,
    override val durationId: Int,
    val txAddr: MacAddress? = null
) : Ieee80211Header {

    init {
        require(frameControl.protocolVersion == SUPPORTED_PROTOCOL_VERSION) {
            "Only protocol version 0 is supported"
        }

        require(frameControl.type == FrameType.CONTROL)
        require(durationId in 0..MAX_DURATION_ID)

        require(ControlSubtype.entries.any {
            it.code == frameControl.subtypeCode
        }) {
            "Unsupported control subtype"
        }

        val needsTxAddr =
            frameControl.subtypeCode != ControlSubtype.ACK.code &&
                    frameControl.subtypeCode != ControlSubtype.CTS.code

        require((txAddr != null) == needsTxAddr) {
            "Address 2 presence must match the control subtype"
        }
    }

    override val headerLengthBytes: Int =
        if (txAddr != null) CONTROL_HEADER_WITH_TX_ADDR_SIZE_BYTES else CONTROL_BASE_HEADER_SIZE_BYTES
}
