package org.wikilayer.wlmarkdown

import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text

internal fun Dialect.squeezed(spoken: String): String =
    spoken
        .map { if (it in blanks) ' ' else it }
        .joinToString("")
        .split(' ')
        .filter { it.isNotEmpty() }
        .joinToString(" ")

internal fun Dialect.words(
    node: Node,
    outside: Scan? = null,
): String =
    when {
        outside != null && node is BlockQuote && place(node, outside) != null -> ""
        node is FencedCodeBlock || node is IndentedCodeBlock -> ""
        node is Code -> node.literal
        node is Text -> node.literal
        node is SoftLineBreak || node is HardLineBreak -> " "
        node is ListItem -> spaced(node, outside)
        node.standsBesideItsNeighbours() -> spaced(node, outside)
        node !is Block -> node.children().joinToString("") { words(it, outside) }
        else -> spaced(node, outside)
    }

internal fun Node.standsBesideItsNeighbours(): Boolean =
    this is TableBlock || this is TableHead || this is TableBody || this is TableRow || this is TableCell

private fun Dialect.spaced(
    node: Node,
    outside: Scan?,
): String =
    node
        .children()
        .map { words(it, outside) }
        .filter { it.isNotEmpty() }
        .joinToString(" ")

internal fun Dialect.wordsInside(
    quote: BlockQuote,
    scan: Scan,
): String {
    var openingDropped = false
    val spoken =
        quote.children().map { child ->
            if (child is Paragraph && !openingDropped) {
                openingDropped = true
                inlineLines(child).drop(1).joinToString(" ") { line ->
                    line.joinToString("") { words(it, scan) }
                }
            } else {
                words(child, scan)
            }
        }
    return squeezed(spoken.joinToString(" "))
}

internal fun Dialect.asWritten(
    quote: BlockQuote,
    scan: Scan,
): String =
    squeezed(
        quote.children().joinToString(" ") { child ->
            if (child is Paragraph) scan.rawLines(child).joinToString(" ") else words(child, null)
        },
    )

internal fun inlineLines(paragraph: Paragraph): List<List<Node>> {
    val written = mutableListOf<List<Node>>()
    var line = mutableListOf<Node>()
    for (child in paragraph.children()) {
        if (child is SoftLineBreak || child is HardLineBreak) {
            written.add(line)
            line = mutableListOf()
        } else {
            line.add(child)
        }
    }
    written.add(line)
    return written
}
