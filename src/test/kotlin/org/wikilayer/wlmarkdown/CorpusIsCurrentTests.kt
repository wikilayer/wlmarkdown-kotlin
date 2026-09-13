package org.wikilayer.wlmarkdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class CorpusIsCurrentTests {
    @TestFactory
    fun `every file copied from the leading port is still the one it holds`(): List<DynamicTest> =
        COPIED.map { path ->
            DynamicTest.dynamicTest(path) {
                val copy = File(path)
                assertThat(copy).describedAs("$path is missing; run make sync-corpus").exists()
                assertThat(copy.readBytes())
                    .describedAs(
                        "$path is not the file the leading port holds, so this port answers an " +
                            "older dialect than the others; run make sync-corpus",
                    ).isEqualTo(leading(copy.name))
            }
        }

    @Test
    fun `the list names every file sync-corpus copies, so none is checked by accident`() {
        assertThat(COPIED)
            .describedAs("sync-corpus copies the rules to the library and the cases to the tests")
            .containsExactlyInAnyOrder(
                "src/main/resources/rules.yaml",
                "src/test/resources/dialect.yaml",
            )
    }

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
        val COPIED =
            listOf(
                "src/main/resources/rules.yaml",
                "src/test/resources/dialect.yaml",
            )
        const val LEADING_PORT = "https://raw.githubusercontent.com/wikilayer/wlmarkdown/main/corpus/"
        const val SECONDS_BEFORE_GIVING_UP = 20L
        const val FOUND_IT = 200
    }
}
