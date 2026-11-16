package model.capture

internal const val PCAP_GLOBAL_HEADER_SIZE_BYTES = 24

private const val SUPPORTED_PCAP_MAJOR_VERSION = 2
private const val SUPPORTED_PCAP_MINOR_VERSION = 4
private const val MAX_UNSIGNED_INT = 0xFFFF_FFFFL
private const val LINK_TYPE_MASK = 0xFFFFL
private const val RESERVED_LINK_INFO_MASK = 0x0BFF_0000L
private const val FCS_LENGTH_PRESENT_MASK = 0x0400_0000L
private const val FCS_LENGTH_WORDS_SHIFT = 28
private const val FCS_LENGTH_WORDS_MASK = 0x0FL
private const val FCS_WORD_SIZE_BYTES = 2

data class PcapGlobalHeader(
    val format: PcapFormat,
    val versionMajor: Int,
    val versionMinor: Int,
    val snapshotLengthBytes: Long,
    val linkTypeAndAdditionalInfo: Long
) {
    val headerLengthBytes: Int = PCAP_GLOBAL_HEADER_SIZE_BYTES
    val linkType: Int = (linkTypeAndAdditionalInfo and LINK_TYPE_MASK).toInt()

    // pastram null ca sa distingem lungimea necunoscuta de o lungime zero
    val fcsLengthBytes: Int? = if ((linkTypeAndAdditionalInfo and FCS_LENGTH_PRESENT_MASK) != 0L) {
        (((linkTypeAndAdditionalInfo ushr FCS_LENGTH_WORDS_SHIFT) and FCS_LENGTH_WORDS_MASK) *
                FCS_WORD_SIZE_BYTES).toInt()
    } else null

    init {
        require(versionMajor == SUPPORTED_PCAP_MAJOR_VERSION && versionMinor == SUPPORTED_PCAP_MINOR_VERSION) {
            "Unsupported PCAP version: $versionMajor.$versionMinor"
        }
        require(snapshotLengthBytes in 1L..MAX_UNSIGNED_INT) {
            "PCAP snapshot length must be a positive unsigned 32-bit value"
        }
        require(linkTypeAndAdditionalInfo in 0L..MAX_UNSIGNED_INT) {
            "PCAP link-layer information must fit in 32 bits"
        }
        require((linkTypeAndAdditionalInfo and RESERVED_LINK_INFO_MASK) == 0L) {
            "Reserved bits in PCAP link-layer information must be zero"
        }
    }
}
