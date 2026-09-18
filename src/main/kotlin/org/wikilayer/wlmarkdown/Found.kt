package org.wikilayer.wlmarkdown

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One dialect construct found in a document. */
@Serializable
data class Found(
    /** The construct kind: `callout`, `map`, `unreadable`, or `link`. */
    val kind: String,
    /** The callout class, or an empty string for another kind. */
    @SerialName("class") val calloutClass: String = "",
    /** The latitude exactly as written, or an empty string for another kind. */
    val lat: String = "",
    /** The longitude exactly as written, or an empty string for another kind. */
    val lng: String = "",
    /** The map caption, or an empty string for another kind. */
    val caption: String = "",
    /** The recognised node-reference scheme, or an empty string when none applies. */
    val scheme: String = "",
    /** The link destination read by commonmark, or an empty string for another kind. */
    val destination: String = "",
    /** Reader-visible or source-preserving text carried by the construct. */
    val text: String = "",
)

/** A dialect marker present in a quote the dialect left unchanged. */
data class Declined(
    /** The marker exactly as the dialect spells it. */
    val marker: String,
)
