package model.link.ieee80211

private const val MAX_SEQUENCE_NUMBER = 4095
private const val MAX_FRAGMENT_NUMBER = 15

class SequenceControl(val sequenceNumber : Int, val fragmentNumber : Int) {
    init {
        require(sequenceNumber in 0..MAX_SEQUENCE_NUMBER) {
            "sequenceNumber must fit in 12 bits"
        }
        require(fragmentNumber in 0..MAX_FRAGMENT_NUMBER) {
            "fragmentNumber must fit in 4 bits"
        }
    }
}
