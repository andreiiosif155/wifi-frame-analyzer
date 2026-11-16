package model.link.ieee80211

enum class ManagementSubtype(val code: Int) {
    PROBE_REQUEST(4),
    PROBE_RESPONSE(5),
    BEACON(8),
    AUTHENTICATION(11);

    companion object {
        private val byCode = entries.associateBy { it.code }

        fun fromCode(code: Int): ManagementSubtype? = byCode[code]
    }
}
