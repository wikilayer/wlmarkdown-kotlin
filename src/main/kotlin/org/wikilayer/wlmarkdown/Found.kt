package org.wikilayer.wlmarkdown

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Found(
    val kind: String,
    @SerialName("class") val calloutClass: String = "",
    val lat: String = "",
    val lng: String = "",
    val caption: String = "",
    val scheme: String = "",
    val destination: String = "",
    val text: String = "",
)

data class Declined(
    val marker: String,
)
