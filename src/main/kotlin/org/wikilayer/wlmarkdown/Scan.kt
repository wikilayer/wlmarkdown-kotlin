package org.wikilayer.wlmarkdown

import org.commonmark.node.BlockQuote
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.Paragraph

internal class Scan(
    private val source: String,
) {
    fun rawLines(paragraph: Paragraph): List<String> =
        paragraph.sourceSpans.map { source.substring(it.inputIndex, it.inputIndex + it.length) }

    fun openingLine(quote: BlockQuote): String =
        (quote.firstChild as? Paragraph)?.let { rawLines(it).firstOrNull() } ?: ""

    fun isAutolink(link: Link): Boolean =
        link.sourceSpans.firstOrNull()?.let { source.startsWith("<", it.inputIndex) } ?: false
}

internal fun Node.children(): List<Node> = generateSequence(firstChild) { it.next }.toList()
