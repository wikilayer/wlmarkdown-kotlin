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
        COPIED_FROM_THE_LEADING_PORT.map { name ->
            DynamicTest.dynamicTest(name) {
                assertThat(held("/$name"))
                    .describedAs(
                        "$name here is not the one the leading port holds, so this port answers " +
                            "an older dialect than the others; run make sync-corpus",
                    ).isEqualTo(leading(name))
            }
        }

    @TestFactory
    fun `every file the library carries is the one the tests answer to`(): List<DynamicTest> =
        CARRIED_BY_THE_LIBRARY.map { name ->
            DynamicTest.dynamicTest(name) {
                assertThat(carried("/$name"))
                    .describedAs("the two copies of $name drifted; run make sync-corpus")
                    .isEqualTo(held("/$name"))
            }
        }

    @Test
    fun `the lists name every shared file, so nothing is checked by accident`() {
        assertThat(COPIED_FROM_THE_LEADING_PORT).containsAll(CARRIED_BY_THE_LIBRARY)
        assertThat(COPIED_FROM_THE_LEADING_PORT).hasSizeGreaterThan(1)
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

    private companion object {
        val COPIED_FROM_THE_LEADING_PORT = listOf("rules.yaml", "dialect.yaml")
        val CARRIED_BY_THE_LIBRARY = listOf("rules.yaml")
        const val LEADING_PORT = "https://raw.githubusercontent.com/wikilayer/wlmarkdown/main/corpus/"
        const val SECONDS_BEFORE_GIVING_UP = 20L
        const val FOUND_IT = 200
    }
}
