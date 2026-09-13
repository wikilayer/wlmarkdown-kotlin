package org.wikilayer.wlmarkdown

import org.commonmark.node.BlockQuote
import org.commonmark.node.Node

class Reading(
    source: String,
    internal val dialect: Dialect = Dialect(),
) {
    internal val scan = Scan(source)

    val document: Node = dialect.parser().parse(source)

    fun calloutClass(quote: BlockQuote): String? =
        if (quoteAbove(quote) != null) null else dialect.rules.calloutClassByMarker[scan.openingLine(quote)]

    fun place(quote: BlockQuote): Found? = written(quote)?.takeIf { it.kind == "map" }

    fun unreadable(quote: BlockQuote): String? = written(quote)?.takeIf { it.kind == "unreadable" }?.text

    fun opensAConstruct(quote: BlockQuote): Boolean = scan.openingLine(quote) in dialect.markers

    fun declined(): List<Declined> = TurnedDown(this).inside(document)

    fun recognise(): List<Found> = Gathering(this).of(document)

    internal fun madeNothingOf(quote: BlockQuote): Boolean =
        place(quote) == null && unreadable(quote) == null && calloutClass(quote) == null

    private fun written(quote: BlockQuote): Found? =
        if (standsWhereTheDialectLooks(quote) && scan.openingLine(quote) == dialect.rules.mapMarker) {
            dialect.place(quote, scan)
        } else {
            null
        }

    private fun quoteAbove(node: Node): BlockQuote? =
        generateSequence(node.parent) { it.parent }.filterIsInstance<BlockQuote>().firstOrNull()

    private fun standsWhereTheDialectLooks(quote: BlockQuote): Boolean {
        val above = quoteAbove(quote) ?: return true
        return calloutClass(above) != null
    }
}
