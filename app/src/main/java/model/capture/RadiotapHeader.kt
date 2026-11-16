package model.capture

import kotlin.math.sign

class RadiotapHeader(
    val version: Int, val headerLengthBytes: Int, val frequencyMhz: Int?,
    val channelFlags: Int?, val signalDbm: Int?, val noiseDbm: Int?
) {
    val snrDb = if (signalDbm != null && noiseDbm != null) {
        signalDbm - noiseDbm;
    } else null;

}
