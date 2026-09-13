package org.wikilayer.wlmarkdown

import org.assertj.core.api.Assertions.assertThat
import org.commonmark.node.BlockQuote
import org.junit.jupiter.api.Test

class ReadingTests {
    private fun quoteIn(source: String): Pair<BlockQuote, Reading> {
        val reading = Reading(source)
        val quote =
            requireNotNull(
                reading.document
                    .children()
                    .filterIsInstance<BlockQuote>()
                    .firstOrNull(),
            ) {
                "the source of this test is not a quote, so the test proves nothing"
            }
        return quote to reading
    }

    @Test
    fun `a quote carrying a marker says so`() {
        val (marked, reading) = quoteIn("> [!MAP]\n> not a point at all\n")
        assertThat(reading.opensAConstruct(marked))
            .describedAs("a host looking for what the dialect turned down starts from this answer")
            .isTrue()

        val (plain, plainly) = quoteIn("> Plain quoted words.\n")
        assertThat(plainly.opensAConstruct(plain)).isFalse()

        val (sharing, shared) = quoteIn("> [!NOTE] see below\n> Body.\n")
        assertThat(shared.opensAConstruct(sharing))
            .describedAs("a marker sharing its line was never a candidate")
            .isFalse()
    }

    @Test
    fun `what an ordinary quote hides is not a construct`() {
        val (outer, reading) = quoteIn("> Plain quoted words.\n>\n> > [!MAP]\n> > 44.7866, 20.4489\n")
        val inner = outer.children().filterIsInstance<BlockQuote>()

        assertThat(inner).describedAs("the source of this test no longer nests a quote").hasSize(1)
        assertThat(reading.place(inner.first()))
            .describedAs("an ordinary quote stops the dialect, and a host must get the answer recognise gives")
            .isNull()
    }

    @Test
    fun `a destination names its scheme or none`() {
        val dialect = Dialect()
        assertThat(dialect.scheme("page:home"))
            .describedAs("what follows a scheme is as often a name as a number")
            .isEqualTo("page")
        assertThat(dialect.scheme("block:50386")).isEqualTo("block")
        assertThat(dialect.scheme("https://example.invalid/page")).isNull()
    }

    @Test
    fun `a host that parses with the dialect's own parser gets the same answers`() {
        val source = "> [!MAP]\n> 44.7866, 20.4489\n> Belgrade\n"
        val dialect = Dialect()
        val parsed = dialect.parser().parse(source)
        val quote = parsed.children().filterIsInstance<BlockQuote>().first()

        assertThat(Reading(source, dialect).place(quote))
            .describedAs("a host walks its own tree, and the reading must answer about it")
            .isEqualTo(Found(kind = "map", lat = "44.7866", lng = "20.4489", caption = "Belgrade"))
    }
}
