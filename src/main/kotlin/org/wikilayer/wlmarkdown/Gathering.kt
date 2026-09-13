package org.wikilayer.wlmarkdown

import org.commonmark.node.BlockQuote
import org.commonmark.node.Link
import org.commonmark.node.Node

internal class Gathering(
    private val reading: Reading,
) {
    private val dialect = reading.dialect
    private val scan = reading.scan
    private val found = mutableListOf<Found>()

    fun of(document: Node): List<Found> {
        document.children().forEach { gather(it) }
        return found
    }

    private fun gather(node: Node) {
        when (node) {
            is BlockQuote -> gatherQuote(node)
            is Link -> reported(node)?.let { found.add(it) }
            else -> node.children().forEach { gather(it) }
        }
    }

    private fun gatherQuote(quote: BlockQuote) {
        val place = placeIn(quote)
        if (place != null) {
            found.add(place)
            return
        }

        val calloutClass = dialect.rules.calloutClassByMarker[scan.openingLine(quote)]
        if (calloutClass == null) {
            gatherLinksOnly(quote)
            return
        }

        found.add(Found(kind = "callout", calloutClass = calloutClass, text = dialect.wordsInside(quote, scan)))
        gatherInsideCallout(quote)
    }

    private fun gatherInsideCallout(quote: BlockQuote) {
        for (child in quote.children()) {
            if (child !is BlockQuote) {
                gather(child)
                continue
            }
            val inner = placeIn(child)
            if (inner != null) found.add(inner) else gatherLinksOnly(child)
        }
    }

    private fun gatherLinksOnly(node: Node) {
        for (child in node.children()) {
            if (child is Link) reported(child)?.let { found.add(it) } else gatherLinksOnly(child)
        }
    }

    private fun placeIn(quote: BlockQuote): Found? =
        if (scan.openingLine(quote) == dialect.rules.mapMarker) dialect.place(quote, scan) else null

    private fun reported(link: Link): Found? =
        if (scan.isAutolink(link)) {
            null
        } else {
            Found(
                kind = "link",
                scheme = dialect.schemeIn(link.destination),
                destination = link.destination,
                text = dialect.squeezed(dialect.words(link)),
            )
        }
}
