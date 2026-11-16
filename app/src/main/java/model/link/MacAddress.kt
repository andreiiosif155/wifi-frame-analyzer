package model.link

internal const val MAC_ADDRESS_SIZE_BYTES = 6

private const val MAX_MAC_ADDRESS_VALUE = 0xFFFF_FFFF_FFFFL
private const val HEX_DIGITS_PER_BYTE = 2
private const val MAC_ADDRESS_HEX_LENGTH = MAC_ADDRESS_SIZE_BYTES * HEX_DIGITS_PER_BYTE

data class MacAddress(val value: Long) {
    init {
        require(value in 0L..MAX_MAC_ADDRESS_VALUE) {
            "A MAC address must fit in 48 bits"
        }
    }

    override fun toString(): String {
        return value.toString(16).padStart(MAC_ADDRESS_HEX_LENGTH, '0')
            .chunked(HEX_DIGITS_PER_BYTE).joinToString(":")
    }
}
