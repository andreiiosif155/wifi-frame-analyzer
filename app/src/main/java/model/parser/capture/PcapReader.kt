package model.parser.capture

import model.capture.PCAP_GLOBAL_HEADER_SIZE_BYTES
import model.capture.PcapFormat
import model.capture.PcapGlobalHeader
import model.capture.TimestampPrecision
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val MAGIC_NUMBER_SIZE_BYTES = 4
private const val MAGIC_NUMBER_HEX_WIDTH = MAGIC_NUMBER_SIZE_BYTES * 2
private const val LITTLE_ENDIAN_MICROSECONDS_MAGIC = 0xD4C3_B2A1L
private const val BIG_ENDIAN_MICROSECONDS_MAGIC = 0xA1B2_C3D4L
private const val LITTLE_ENDIAN_NANOSECONDS_MAGIC = 0x4D3C_B2A1L
private const val BIG_ENDIAN_NANOSECONDS_MAGIC = 0xA1B2_3C4DL
private const val MAJOR_VERSION_OFFSET = 4
private const val MINOR_VERSION_OFFSET = 6
private const val SNAPSHOT_LENGTH_OFFSET = 16
private const val LINK_TYPE_AND_INFO_OFFSET = 20
private const val UNSIGNED_SHORT_MASK = 0xFFFF
private const val UNSIGNED_INT_MASK = 0xFFFF_FFFFL

class PcapReader {
    // offsetul indica inceputul antetului global pcap
    fun readFormat(bytes: ByteArray, offset: Int): PcapFormat {
        require(offset >= 0) {
            "Offset must be non-negative"
        }
        require(offset <= bytes.size - MAGIC_NUMBER_SIZE_BYTES) {
            "Not enough bytes to read the PCAP magic number"
        }

        // citim semnatura in ordinea din fisier ca sa aflam ordinea celorlalte campuri
        var magicNumber = 0L
        for (index in 0 until MAGIC_NUMBER_SIZE_BYTES) {
            magicNumber = (magicNumber shl Byte.SIZE_BITS) or bytes[offset + index].toUByte().toLong()
        }

        return when (magicNumber) {
            LITTLE_ENDIAN_MICROSECONDS_MAGIC -> PcapFormat(ByteOrder.LITTLE_ENDIAN, TimestampPrecision.MICROSECONDS)
            BIG_ENDIAN_MICROSECONDS_MAGIC -> PcapFormat(ByteOrder.BIG_ENDIAN, TimestampPrecision.MICROSECONDS)
            LITTLE_ENDIAN_NANOSECONDS_MAGIC -> PcapFormat(ByteOrder.LITTLE_ENDIAN, TimestampPrecision.NANOSECONDS)
            BIG_ENDIAN_NANOSECONDS_MAGIC -> PcapFormat(ByteOrder.BIG_ENDIAN, TimestampPrecision.NANOSECONDS)
            else -> throw IllegalArgumentException(
                "Unsupported PCAP magic number: 0x${magicNumber.toString(16).padStart(MAGIC_NUMBER_HEX_WIDTH, '0')}"
            )
        }
    }

    fun readGlobalHeader(bytes: ByteArray, offset: Int): PcapGlobalHeader {
        require(offset >= 0) {
            "Offset must be non-negative"
        }
        require(offset <= bytes.size - PCAP_GLOBAL_HEADER_SIZE_BYTES) {
            "Not enough bytes to read the PCAP global header"
        }

        val format = this.readFormat(bytes, offset)
        // copiem antetul ca offseturile campurilor sa fie relative la inceputul lui
        val headerBytes = bytes.copyOfRange(offset, offset + PCAP_GLOBAL_HEADER_SIZE_BYTES)
        val headerBuffer = ByteBuffer.wrap(headerBytes).order(format.byteOrder)
        val versionMajor = headerBuffer.getShort(MAJOR_VERSION_OFFSET).toInt() and UNSIGNED_SHORT_MASK
        val versionMinor = headerBuffer.getShort(MINOR_VERSION_OFFSET).toInt() and UNSIGNED_SHORT_MASK
        val snapshotLengthBytes = headerBuffer.getInt(SNAPSHOT_LENGTH_OFFSET).toLong() and UNSIGNED_INT_MASK
        val linkTypeAndAdditionalInfo = headerBuffer.getInt(LINK_TYPE_AND_INFO_OFFSET).toLong() and UNSIGNED_INT_MASK

        return PcapGlobalHeader(
            format = format,
            versionMajor = versionMajor,
            versionMinor = versionMinor,
            snapshotLengthBytes = snapshotLengthBytes,
            linkTypeAndAdditionalInfo = linkTypeAndAdditionalInfo
        )
    }
}
