package org.wikilayer.wlmarkdown

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class PlainTextTests {
    @Serializable
    data class Case(
        val name: String,
        val markdown: String,
        val plain: String,
    )

    @Serializable
    data class Corpus(
        val cases: List<Case>,
    )

    @TestFactory
    fun `plainText answers the shared corpus`(): List<DynamicTest> =
        cases.map { written ->
            DynamicTest.dynamicTest(written.name) {
                assertThat(Dialect().plainText(written.markdown)).isEqualTo(written.plain)
            }
        }

    @Test
    fun `the corpus is not empty`() {
        assertThat(cases).isNotEmpty()
    }

    private companion object {
        val cases: List<Case> =
            requireNotNull(PlainTextTests::class.java.getResourceAsStream("/plain_text.yaml"))
                .use { Yaml.default.decodeFromString(Corpus.serializer(), it.readBytes().decodeToString()).cases }
    }
}
