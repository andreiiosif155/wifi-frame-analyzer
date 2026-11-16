package model.link.ieee80211

import org.junit.Assert.assertEquals
import org.junit.Test

class QosControlTest {
    @Test
    fun trafficIdExtraction() {
        val qosControl: QosControl = QosControl(0xAB65)
        val trafficId: Int = qosControl.trafficId
        assertEquals(5, trafficId);
    }

    @Test
    fun ackPolicyExtraction() {
        val qosControl: QosControl = QosControl(0xAB65)
        val ackPolicy: Int = qosControl.ackPolicy
        assertEquals(3, ackPolicy)
    }

    @Test
    fun ackPolicyExtractionComplex() {
        val qosControl: QosControl = QosControl(0x0040)
        val ackPolicy: Int = qosControl.ackPolicy
        assertEquals(2, ackPolicy)
    }
}
