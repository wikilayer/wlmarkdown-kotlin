package org.wikilayer.wlmarkdown

import org.commonmark.node.BlockQuote
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text

/** Returns the reader-visible words in a markdown document. */
fun Dialect.plainText(source: String): String {
    val reading = Reading(source, this)
    return squeezed(plainWords(reading.document, reading))
}

private fun Dialect.plainWords(
    node: Node,
    reading: Reading,
): String =
    when (node) {
        is BlockQuote -> plainQuote(node, reading)
        is FencedCodeBlock -> node.literal
        is IndentedCodeBlock -> node.literal
        is Code -> node.literal
        is Text -> node.literal
        is SoftLineBreak, is HardLineBreak -> " "
        is ListItem -> plainChildren(node, reading).withoutTaskMark()
        else -> plainNonLeaf(node, reading)
    }

private fun Dialect.plainNonLeaf(
    node: Node,
    reading: Reading,
): String =
    when {
        node.standsBesideItsNeighbours() -> plainChildren(node, reading)
        node !is org.commonmark.node.Block -> node.children().joinToString("") { plainWords(it, reading) }
        else -> plainChildren(node, reading)
    }

private fun Dialect.plainQuote(
    quote: BlockQuote,
    reading: Reading,
): String {
    val place = reading.place(quote)
    return when {
        reading.unreadable(quote) != null -> ""
        place != null -> plainText(place.caption)
        reading.calloutClass(quote) != null -> wordsInside(quote, reading.scan)
        else -> plainChildren(quote, reading)
    }
}

private fun Dialect.plainChildren(
    node: Node,
    reading: Reading,
): String {
    val children = node.children().toList()
    val separator =
        if (node.standsBesideItsNeighbours() ||
            children.any { it is org.commonmark.node.Block }
        ) {
            " "
        } else {
            ""
        }
    return children
        .map { plainWords(it, reading) }
        .filter { it.isNotEmpty() }
        .joinToString(separator)
}

private fun String.withoutTaskMark(): String =
    listOf("[ ] ", "[x] ", "[X] ").firstOrNull(::startsWith)?.let(::removePrefix) ?: this
