package model.capture

class RawInfo80211(frameBytes: ByteArray) {
    // copiem cadrul ca apelantul sa poata reutiliza bufferul de intrare
    private val storedFrameBytes: ByteArray = frameBytes.copyOf()
    val frameSizeBytes: Int = storedFrameBytes.size

    fun copyBytes(): ByteArray {
        return storedFrameBytes.copyOf()
    }
}
