package model.link.ieee80211

internal const val SUPPORTED_PROTOCOL_VERSION = 0
internal const val MANAGEMENT_DATA_BASE_HEADER_SIZE_BYTES = 24
internal const val QOS_CONTROL_SIZE_BYTES = 2
internal const val QOS_SUBTYPE_MASK = 0x08
internal const val HT_CONTROL_SIZE_BYTES = 4
internal const val MAX_DURATION_ID = 0xFFFF

sealed interface Ieee80211Header {
    val frameControl: FrameControl
    val durationId: Int
    val headerLengthBytes: Int
}
