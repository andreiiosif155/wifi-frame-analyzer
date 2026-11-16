package model.link.ieee80211

private const val MAX_QOS_CONTROL_VALUE = 0xFFFF
private const val TRAFFIC_ID_MASK = 0x0F
private const val ACK_POLICY_MASK = 0x03
private const val ACK_POLICY_SHIFT = 5

data class QosControl(val rawValue: Int) {
    init {
        require(rawValue in 0..MAX_QOS_CONTROL_VALUE) {
            "QoS Control must fit in 16 bits"
        }
    }

    val trafficId: Int = rawValue and TRAFFIC_ID_MASK

    val ackPolicy: Int = (rawValue ushr ACK_POLICY_SHIFT) and ACK_POLICY_MASK
}
