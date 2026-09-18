package org.wikilayer.wlmarkdown

import org.commonmark.node.BlockQuote
import org.commonmark.node.Node

/** Answers dialect questions about a commonmark tree and its original source. */
class Reading(
    private val source: String,
    internal val dialect: Dialect = Dialect(),
) {
    internal val scan = Scan(source)

    /** The lazily parsed document for this reader's source. */
    val document: Node by lazy { dialect.parser().parse(source) }

    /** Returns the class of a quote accepted as a callout at its position. */
    fun calloutClass(quote: BlockQuote): String? =
        if (quoteAbove(quote) != null) null else dialect.rules.calloutClassByMarker[scan.openingLine(quote)]

    /** Returns the placed map represented by a quote. */
    fun place(quote: BlockQuote): Found? = written(quote)?.takeIf { it.kind == "map" }

    /** Returns the source-preserving words of an out-of-bounds map. */
    fun unreadable(quote: BlockQuote): String? = written(quote)?.takeIf { it.kind == "unreadable" }?.text

    /** Returns whether the quote starts with any marker known to the dialect. */
    fun opensAConstruct(quote: BlockQuote): Boolean = scan.openingLine(quote) in dialect.markers

    /** Returns marked quotes the dialect declined to turn into constructs. */
    fun declined(): List<Declined> = TurnedDown(this).inside(document)

    /** Returns dialect constructs in document order. */
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
