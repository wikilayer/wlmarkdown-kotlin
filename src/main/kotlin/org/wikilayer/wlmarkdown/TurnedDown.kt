package org.wikilayer.wlmarkdown

import org.commonmark.node.BlockQuote
import org.commonmark.node.Node

internal class TurnedDown(
    private val reading: Reading,
) {
    private val refused = mutableListOf<Declined>()

    fun inside(document: Node): List<Declined> {
        gather(document)
        return refused
    }

    private fun gather(node: Node) {
        if (node is BlockQuote && reading.madeNothingOf(node) && reading.opensAConstruct(node)) {
            refused.add(Declined(reading.scan.openingLine(node)))
        }
        node.children().forEach { gather(it) }
    }
}
