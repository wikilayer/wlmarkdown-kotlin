package org.wikilayer.wlmarkdown

import org.commonmark.node.BlockQuote
import org.commonmark.node.Paragraph

private const val MARKER_AND_COORDINATES = 2

internal data class Point(
    val lat: String,
    val lng: String,
)

internal val Dialect.blanks: Set<Char>
    get() = rules.blanks.toSet()

private val Dialect.signs: Set<Char>
    get() = rules.coordinate.signs.toSet()

internal fun Dialect.place(
    quote: BlockQuote,
    scan: Scan,
): Found? {
    val lines = linesOf(quote, scan)
    val point = pointOn(lines.getOrNull(1)) ?: return null
    return if (onEarth(point)) {
        Found(kind = "map", lat = point.lat, lng = point.lng, caption = captionIn(lines))
    } else {
        Found(kind = "unreadable", text = asWritten(quote, scan))
    }
}

private fun linesOf(
    quote: BlockQuote,
    scan: Scan,
): List<String> = (quote.firstChild as? Paragraph)?.let { scan.rawLines(it) }.orEmpty()

private fun Dialect.pointOn(line: String?): Point? {
    val written = line?.split(",", limit = MARKER_AND_COORDINATES).orEmpty()
    if (written.size != MARKER_AND_COORDINATES) return null

    val lat = written[0].trim { it in blanks }
    val lng = written[1].trim { it in blanks }
    return if (reads(lat) && reads(lng)) Point(lat, lng) else null
}

private fun Dialect.onEarth(point: Point): Boolean =
    within(point.lat, rules.coordinate.latitudeWithin) &&
        within(point.lng, rules.coordinate.longitudeWithin)

private fun Dialect.captionIn(lines: List<String>): String =
    lines
        .drop(MARKER_AND_COORDINATES)
        .map { line -> line.trim { it in blanks } }
        .filter { it.isNotEmpty() }
        .joinToString(" ")

internal fun Dialect.reads(spoken: String): Boolean {
    val rest = if (spoken.firstOrNull() in signs) spoken.drop(1) else spoken
    val parts = rest.split(rules.coordinate.point)
    return parts.size <= MARKER_AND_COORDINATES && parts.all { digitsOnly(it) }
}

internal fun Dialect.within(
    spoken: String,
    bound: String,
): Boolean {
    val unsigned = spoken.dropWhile { it in signs }
    val parts = unsigned.split(rules.coordinate.point)
    val whole = parts[0].dropWhile { it == '0' }
    return when {
        whole.isEmpty() -> true
        whole.length != bound.length -> whole.length < bound.length
        whole != bound -> whole < bound
        else -> parts.size == 1 || parts[1].all { it == '0' }
    }
}

private fun Dialect.digitsOnly(spoken: String): Boolean =
    spoken.isNotEmpty() && spoken.all { it in rules.coordinate.digits }
