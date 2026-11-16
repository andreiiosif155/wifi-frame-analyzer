package model.parser.capture

import model.capture.PcapFormat
import model.capture.TimestampPrecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.ByteOrder

private const val LINK_INFO_FIXTURE_OFFSET = 20
private val SNAPSHOT_LENGTH_FIXTURE_INDICES = 16 until 20

class PcapReaderTest {
    @Test
    fun readsAllFourFormatsFromExactlyFourBytes() {
        val reader = PcapReader()

        for ((magicBytes, expectedFormat) in formatFixtures) {
            assertEquals(expectedFormat, reader.readFormat(magicBytes, 0))
        }
    }

    @Test
    fun readsAllFourFormatsWithPrefixAndSuffix() {
        val reader = PcapReader()

        for ((magicBytes, expectedFormat) in formatFixtures) {
            val bytes = byteArrayOf(0x55, 0x66) + magicBytes + byteArrayOf(0x77, 0x11)
            assertEquals(expectedFormat, reader.readFormat(bytes, 2))
        }
    }

    @Test
    fun rejectsTruncatedMagicNumbers() {
        val reader = PcapReader()

        for ((magicBytes, _) in formatFixtures) {
            for (offset in listOf(0, 2)) {
                for (length in 0 until 4) {
                    val bytes = ByteArray(offset) { 0x55 } + magicBytes.copyOf(length)
                    assertThrows("Offset $offset with $length magic bytes", IllegalArgumentException::class.java) {
                        reader.readFormat(bytes, offset)
                    }
                }
            }
        }
    }

    @Test
    fun rejectsNegativeOffsets() {
        val bytes = formatFixtures.first().first

        for (offset in listOf(-1, Int.MIN_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) {
                PcapReader().readFormat(bytes, offset)
            }
        }
    }

    @Test
    fun rejectsOffsetsWithoutFourAvailableBytes() {
        val bytes = formatFixtures.first().first

        for (offset in listOf(1, bytes.size, bytes.size + 1, Int.MAX_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) {
                PcapReader().readFormat(bytes, offset)
            }
        }
    }

    @Test
    fun rejectsUnknownMagicNumber() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PcapReader().readFormat(byteArrayOf(0x01, 0x02, 0x03, 0x04), 0)
        }

        assertEquals("Unsupported PCAP magic number: 0x01020304", exception.message)
    }

    @Test
    fun rejectsPcapNgMagicNumber() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PcapReader().readFormat(byteArrayOf(0x0A, 0x0D, 0x0D, 0x0A), 0)
        }

        assertEquals("Unsupported PCAP magic number: 0x0a0d0d0a", exception.message)
    }

    @Test
    fun readsGlobalHeadersInAllFormatsWithZeroAndNonZeroOffsets() {
        val reader = PcapReader()

        for ((magicBytes, expectedFormat) in formatFixtures) {
            val headerBytes = globalHeaderBytes(magicBytes, expectedFormat.byteOrder)
            for (offset in listOf(0, 2)) {
                val bytes = ByteArray(offset) { 0x55 } + headerBytes
                val header = reader.readGlobalHeader(bytes, offset)

                assertEquals(expectedFormat, header.format)
                assertEquals(2, header.versionMajor)
                assertEquals(4, header.versionMinor)
                assertEquals(65535L, header.snapshotLengthBytes)
                assertEquals(127L, header.linkTypeAndAdditionalInfo)
                assertEquals(127, header.linkType)
                assertEquals(24, header.headerLengthBytes)
                assertNull(header.fcsLengthBytes)
            }
        }
    }

    @Test
    fun readsMaximumUnsignedSnapshotLength() {
        for ((magicBytes, format) in formatFixtures) {
            val bytes = globalHeaderBytes(magicBytes, format.byteOrder)
            for (index in SNAPSHOT_LENGTH_FIXTURE_INDICES) {
                bytes[index] = 0xFF.toByte()
            }

            val header = PcapReader().readGlobalHeader(bytes, 0)

            assertEquals(0xFFFF_FFFFL, header.snapshotLengthBytes)
        }
    }

    @Test
    fun readsFullSixteenBitLinkTypeWithPrefixAndSuffix() {
        val littleEndianField = byteArrayOf(0x34, 0x12, 0x00, 0x00)
        for ((headerFixture, linkInfoBytes) in listOf(
            littleEndianGlobalHeader to littleEndianField,
            bigEndianGlobalHeader to littleEndianField.reversedArray()
        )) {
            val headerBytes = headerFixture.copyOf()
            linkInfoBytes.copyInto(headerBytes, destinationOffset = LINK_INFO_FIXTURE_OFFSET)
            val bytes = byteArrayOf(0x55, 0x66) + headerBytes + byteArrayOf(0x77, 0x11)

            val header = PcapReader().readGlobalHeader(bytes, 2)

            assertEquals(0x1234, header.linkType)
            assertEquals(0x1234L, header.linkTypeAndAdditionalInfo)
        }
    }

    @Test
    fun extractsLinkTypeAndOptionalFcsLength() {
        val cases = listOf(
            Triple(0x2400007FL, 4, byteArrayOf(0x7F, 0x00, 0x00, 0x24)),
            Triple(0xA000007FL, null, byteArrayOf(0x7F, 0x00, 0x00, 0xA0.toByte())),
            Triple(0x0400007FL, 0, byteArrayOf(0x7F, 0x00, 0x00, 0x04)),
            Triple(0xF400007FL, 30, byteArrayOf(0x7F, 0x00, 0x00, 0xF4.toByte()))
        )

        for ((expectedLinkInfo, expectedFcsLengthBytes, littleEndianField) in cases) {
            for ((headerFixture, linkInfoBytes) in listOf(
                littleEndianGlobalHeader to littleEndianField,
                bigEndianGlobalHeader to littleEndianField.reversedArray()
            )) {
                val bytes = headerFixture.copyOf()
                linkInfoBytes.copyInto(bytes, destinationOffset = LINK_INFO_FIXTURE_OFFSET)
                val header = PcapReader().readGlobalHeader(bytes, 0)

                assertEquals(expectedLinkInfo, header.linkTypeAndAdditionalInfo)
                assertEquals(127, header.linkType)
                assertEquals(expectedFcsLengthBytes, header.fcsLengthBytes)
            }
        }
    }

    @Test
    fun rejectsReservedBitsInLinkLayerInformation() {
        val invalidFields = listOf(
            byteArrayOf(0x7F, 0x00, 0x01, 0x00),
            byteArrayOf(0x7F, 0x00, 0x00, 0x02),
            byteArrayOf(0x7F, 0x00, 0x00, 0x08)
        )

        for (littleEndianField in invalidFields) {
            for ((headerFixture, linkInfoBytes) in listOf(
                littleEndianGlobalHeader to littleEndianField,
                bigEndianGlobalHeader to littleEndianField.reversedArray()
            )) {
                val bytes = headerFixture.copyOf()
                linkInfoBytes.copyInto(bytes, destinationOffset = LINK_INFO_FIXTURE_OFFSET)

                val exception = assertThrows(IllegalArgumentException::class.java) {
                    PcapReader().readGlobalHeader(bytes, 0)
                }
                assertEquals("Reserved bits in PCAP link-layer information must be zero", exception.message)
            }
        }
    }

    @Test
    fun rejectsZeroSnapshotLength() {
        for (headerFixture in listOf(littleEndianGlobalHeader, bigEndianGlobalHeader)) {
            val bytes = headerFixture.copyOf()
            for (index in SNAPSHOT_LENGTH_FIXTURE_INDICES) {
                bytes[index] = 0
            }

            assertThrows(IllegalArgumentException::class.java) {
                PcapReader().readGlobalHeader(bytes, 0)
            }
        }
    }

    @Test
    fun rejectsUnsupportedGlobalHeaderVersions() {
        val invalidVersions = listOf(
            Triple(4, byteArrayOf(0x03, 0x00), "3.4"),
            Triple(6, byteArrayOf(0x03, 0x00), "2.3"),
            Triple(4, byteArrayOf(0xFF.toByte(), 0xFF.toByte()), "65535.4")
        )

        for ((headerFixture, byteOrder) in listOf(
            littleEndianGlobalHeader to ByteOrder.LITTLE_ENDIAN,
            bigEndianGlobalHeader to ByteOrder.BIG_ENDIAN
        )) {
            for ((versionOffset, littleEndianField, version) in invalidVersions) {
                val bytes = headerFixture.copyOf()
                val versionBytes = if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    littleEndianField
                } else {
                    littleEndianField.reversedArray()
                }
                versionBytes.copyInto(bytes, destinationOffset = versionOffset)

                val exception = assertThrows(IllegalArgumentException::class.java) {
                    PcapReader().readGlobalHeader(bytes, 0)
                }
                assertEquals("Unsupported PCAP version: $version", exception.message)
            }
        }
    }

    @Test
    fun rejectsTruncatedGlobalHeaders() {
        val reader = PcapReader()

        for ((magicBytes, format) in formatFixtures) {
            val headerBytes = globalHeaderBytes(magicBytes, format.byteOrder)
            for (offset in listOf(0, 2)) {
                for (length in 0 until 24) {
                    val bytes = ByteArray(offset) { 0x55 } + headerBytes.copyOf(length)
                    assertThrows("Offset $offset with $length header bytes", IllegalArgumentException::class.java) {
                        reader.readGlobalHeader(bytes, offset)
                    }
                }
            }
        }
    }

    @Test
    fun rejectsInvalidGlobalHeaderOffsets() {
        for (offset in listOf(-1, Int.MIN_VALUE, 1, 24, 25, Int.MAX_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) {
                PcapReader().readGlobalHeader(littleEndianGlobalHeader, offset)
            }
        }
    }

    @Test
    fun rejectsUnsupportedGlobalHeaderMagic() {
        for (magicBytes in listOf(
            byteArrayOf(0x01, 0x02, 0x03, 0x04),
            byteArrayOf(0x0A, 0x0D, 0x0D, 0x0A)
        )) {
            val bytes = littleEndianGlobalHeader.copyOf()
            magicBytes.copyInto(bytes)

            assertThrows(IllegalArgumentException::class.java) {
                PcapReader().readGlobalHeader(bytes, 0)
            }
        }
    }

    private fun globalHeaderBytes(magicBytes: ByteArray, byteOrder: ByteOrder): ByteArray {
        val bytes = if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            littleEndianGlobalHeader.copyOf()
        } else {
            bigEndianGlobalHeader.copyOf()
        }
        magicBytes.copyInto(bytes)
        return bytes
    }

    // lasam campurile rezervate nenule ca sa verificam ca cititorul le ignora
    private val littleEndianGlobalHeader = byteArrayOf(
        0xD4.toByte(), 0xC3.toByte(), 0xB2.toByte(), 0xA1.toByte(),
        0x02, 0x00, 0x04, 0x00,
        0x11, 0x22, 0x33, 0x44,
        0x55, 0x66, 0x77, 0x88.toByte(),
        0xFF.toByte(), 0xFF.toByte(), 0x00, 0x00,
        0x7F, 0x00, 0x00, 0x00
    )

    private val bigEndianGlobalHeader = byteArrayOf(
        0xA1.toByte(), 0xB2.toByte(), 0xC3.toByte(), 0xD4.toByte(),
        0x00, 0x02, 0x00, 0x04,
        0x11, 0x22, 0x33, 0x44,
        0x55, 0x66, 0x77, 0x88.toByte(),
        0x00, 0x00, 0xFF.toByte(), 0xFF.toByte(),
        0x00, 0x00, 0x00, 0x7F
    )

    private val formatFixtures = listOf(
        byteArrayOf(0xD4.toByte(), 0xC3.toByte(), 0xB2.toByte(), 0xA1.toByte()) to
                PcapFormat(ByteOrder.LITTLE_ENDIAN, TimestampPrecision.MICROSECONDS),
        byteArrayOf(0xA1.toByte(), 0xB2.toByte(), 0xC3.toByte(), 0xD4.toByte()) to
                PcapFormat(ByteOrder.BIG_ENDIAN, TimestampPrecision.MICROSECONDS),
        byteArrayOf(0x4D, 0x3C, 0xB2.toByte(), 0xA1.toByte()) to
                PcapFormat(ByteOrder.LITTLE_ENDIAN, TimestampPrecision.NANOSECONDS),
        byteArrayOf(0xA1.toByte(), 0xB2.toByte(), 0x3C, 0x4D) to
                PcapFormat(ByteOrder.BIG_ENDIAN, TimestampPrecision.NANOSECONDS)
    )
}
