package model.link.ieee80211

class FrameFlags(
    val toDs: Boolean = false,
    val fromDs: Boolean = false,
    val moreFragments: Boolean = false,
    val retry: Boolean = false,
    val powerManagement: Boolean = false,
    val moreData: Boolean = false,
    val protectedFrame: Boolean = false,
    val order: Boolean = false
)
