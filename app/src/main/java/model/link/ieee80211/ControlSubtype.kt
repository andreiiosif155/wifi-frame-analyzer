package model.link.ieee80211

enum class ControlSubtype(val code: Int) {
    TRIGGER(2),
    BLOCK_ACK_REQUEST(8),
    BLOCK_ACK(9),
    PS_POLL(10),
    RTS(11),
    CTS(12),
    ACK(13),
    CF_END(14),
    CF_END_ACK(15);

    companion object {
        private val byCode = entries.associateBy { it.code }

        fun fromCode(code: Int): ControlSubtype? = byCode[code]
    }
}
