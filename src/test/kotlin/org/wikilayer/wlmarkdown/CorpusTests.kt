package org.wikilayer.wlmarkdown

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.commonmark.node.BlockQuote
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class CorpusTests {
    @Serializable
    data class Case(
        val name: String,
        val markdown: String,
        val found: List<Found> = emptyList(),
        val declined: List<String> = emptyList(),
    )

    @Serializable
    data class Corpus(
        val cases: List<Case>,
    )

    private val cases = read().cases

    private fun read(): Corpus {
        val written =
            requireNotNull(javaClass.getResourceAsStream("/dialect.yaml")) {
                "the corpus every port answers to is unreadable"
            }.use { it.readBytes().decodeToString() }
        return Yaml.default.decodeFromString(Corpus.serializer(), written)
    }

    @Test
    fun `the corpus holds cases, so this test can fail`() {
        assertThat(cases).hasSizeGreaterThan(20)
        assertThat(cases.map { it.markdown }).allMatch { it.isNotEmpty() }
    }

    @TestFactory
    fun `the dialect answers every case the corpus defines it by`(): List<DynamicTest> =
        cases.map { one ->
            DynamicTest.dynamicTest(one.name) {
                assertThat(Dialect().recognise(one.markdown))
                    .describedAs("the dialect recognised something other than what the corpus names")
                    .isEqualTo(one.found)
            }
        }

    @TestFactory
    fun `the dialect turns down every quote the corpus says it turns down`(): List<DynamicTest> =
        cases.map { one ->
            DynamicTest.dynamicTest(one.name) {
                assertThat(Reading(one.markdown).declined())
                    .describedAs("the dialect turned down something other than what the corpus names")
                    .isEqualTo(one.declined.map { Declined(it) })
            }
        }

    @TestFactory
    fun `a host asking about a quote it parsed itself is told what the corpus names`(): List<DynamicTest> =
        cases.map { one ->
            DynamicTest.dynamicTest(one.name) {
                val reading = Reading(one.markdown)
                val quotes = reading.document.children().filterIsInstance<BlockQuote>()
                if (quotes.size != 1 || one.found.size > 1) return@dynamicTest

                val quote = quotes.first()
                val said = one.found.firstOrNull()
                assertThat(reading.place(quote))
                    .describedAs("place answers about the quote differently from recognise")
                    .isEqualTo(said?.takeIf { it.kind == "map" })
                assertThat(reading.unreadable(quote))
                    .describedAs("unreadable answers about the quote differently from recognise")
                    .isEqualTo(said?.takeIf { it.kind == "unreadable" }?.text)
                assertThat(reading.calloutClass(quote))
                    .describedAs("calloutClass answers about the quote differently from recognise")
                    .isEqualTo(said?.takeIf { it.kind == "callout" }?.calloutClass)
            }
        }
}
