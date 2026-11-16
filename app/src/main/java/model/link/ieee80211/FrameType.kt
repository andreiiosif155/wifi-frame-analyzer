package model.link.ieee80211

enum class FrameType(val code: Int) {
    MANAGEMENT(0),
    CONTROL(1),
    DATA(2),
    EXTENSION(3);

    companion object {
        private val byCode = entries.associateBy { it.code }

        fun fromCode(code: Int): FrameType? = byCode[code]
    }
}
