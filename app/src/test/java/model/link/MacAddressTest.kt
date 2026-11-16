package model.link

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MacAddressTest {
    @Test
    fun afiseazaAdresaInHexazecimal() {
        val macAddress: MacAddress = MacAddress(0x001A2B3C4D5EL)
        val formattedAddress: String = macAddress.toString()
        assertEquals("00:1a:2b:3c:4d:5e", formattedAddress)
    }

    @Test
    fun afiseazaAdresaInHexazecimalZero() {
        val macAddress: MacAddress = MacAddress(0L)
        val formattedAddress: String = macAddress.toString()
        assertEquals("00:00:00:00:00:00", formattedAddress)
    }

    @Test
    fun respingeAdresaCareDepaseste48DeBiti() {
        assertThrows(IllegalArgumentException::class.java) {
            MacAddress(0x1_0000_0000_0000L)
        }
    }

    @Test
    fun respingeAdresaNegativa() {
        assertThrows(IllegalArgumentException::class.java) {
            MacAddress(-1L)
        }
    }

}
