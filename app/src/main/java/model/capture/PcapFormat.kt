package model.capture

import java.nio.ByteOrder

data class PcapFormat(
    val byteOrder: ByteOrder,
    val timestampPrecision: TimestampPrecision
)
