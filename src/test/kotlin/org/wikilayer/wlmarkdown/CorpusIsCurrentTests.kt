package org.wikilayer.wlmarkdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class CorpusIsCurrentTests {
    @TestFactory
    fun `every file copied from the leading port is still the one it holds`(): List<DynamicTest> =
        COPIED.map { shared ->
            DynamicTest.dynamicTest(shared.name) {
                assertThat(held("/${shared.name}"))
                    .describedAs(
                        "${shared.name} here is not the one the leading port holds, so this port " +
                            "answers an older dialect than the others; run make sync-corpus",
                    ).isEqualTo(leading(shared.name))
            }
        }

    @TestFactory
    fun `a file lying here twice is the same file in both places`(): List<DynamicTest> =
        COPIED.filter { it.theLibraryCarriesItToo }.map { shared ->
            DynamicTest.dynamicTest(shared.name) {
                assertThat(carried("/${shared.name}"))
                    .describedAs("the two copies of ${shared.name} drifted; run make sync-corpus")
                    .isEqualTo(held("/${shared.name}"))
            }
        }

    @Test
    fun `the list names every file sync-corpus copies, so none is checked by accident`() {
        assertThat(COPIED.map { it.name })
            .describedAs("sync-corpus copies rules.yaml to two places and dialect.yaml to one")
            .containsExactlyInAnyOrder("rules.yaml", "dialect.yaml")
        assertThat(COPIED.filter { it.theLibraryCarriesItToo }.map { it.name })
            .containsExactly("rules.yaml")
    }

    private fun held(name: String): ByteArray =
        requireNotNull(javaClass.getResourceAsStream(name)) {
            "$name is not on the test classpath; run make sync-corpus"
        }.use { it.readBytes() }

    private fun carried(name: String): ByteArray =
        requireNotNull(Dialect::class.java.getResourceAsStream(name)) {
            "the library carries no $name, so the dialect knows nothing"
        }.use { it.readBytes() }

    private fun leading(name: String): ByteArray {
        val answer =
            client.send(
                HttpRequest
                    .newBuilder(URI.create(LEADING_PORT + name))
                    .timeout(Duration.ofSeconds(SECONDS_BEFORE_GIVING_UP))
                    .build(),
                HttpResponse.BodyHandlers.ofByteArray(),
            )
        check(answer.statusCode() == FOUND_IT) {
            "the leading port answered ${answer.statusCode()} for $name, so this test proves nothing"
        }
        return answer.body()
    }

    private val client: HttpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(SECONDS_BEFORE_GIVING_UP))
            .build()

    private data class Shared(
        val name: String,
        val theLibraryCarriesItToo: Boolean,
    )

    private companion object {
        val COPIED =
            listOf(
                Shared("rules.yaml", theLibraryCarriesItToo = true),
                Shared("dialect.yaml", theLibraryCarriesItToo = false),
            )
        const val LEADING_PORT = "https://raw.githubusercontent.com/wikilayer/wlmarkdown/main/corpus/"
        const val SECONDS_BEFORE_GIVING_UP = 20L
        const val FOUND_IT = 200
    }
}
