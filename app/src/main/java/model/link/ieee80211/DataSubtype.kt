package model.link.ieee80211

enum class DataSubtype(val code: Int) {
    DATA(0),
    DATA_CF_ACK(1),
    DATA_CF_POLL(2),
    DATA_CF_ACK_POLL(3),
    NULL(4),
    CF_ACK(5),
    CF_POLL(6),
    CF_ACK_POLL(7),
    QOS_DATA(8),
    QOS_DATA_CF_ACK(9),
    QOS_DATA_CF_POLL(10),
    QOS_DATA_CF_ACK_POLL(11),
    QOS_NULL(12),
    QOS_CF_POLL(14),
    QOS_CF_ACK_POLL(15);

    companion object {
        private val byCode = entries.associateBy { it.code }

        fun fromCode(code: Int): DataSubtype? = byCode[code]
    }
}
