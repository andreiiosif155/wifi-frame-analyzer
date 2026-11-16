package model.link.ieee80211

private const val MAX_PROTOCOL_VERSION = 3
private const val MAX_SUBTYPE_CODE = 15

class FrameControl(val protocolVersion: Int, val type: FrameType, val subtypeCode: Int, val flags: FrameFlags) {
    init {
        require(protocolVersion in 0..MAX_PROTOCOL_VERSION) {
            "protocolVersion must fit in 2 bits"
        }
        require(subtypeCode in 0..MAX_SUBTYPE_CODE) {
            "subtype must fit in 4 bits"
        }
    }
}
