package org.wikilayer.wlmarkdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Tag("reaches-the-leading-port")
class CorpusIsCurrentTests {
    @TestFactory
    fun `every file copied from the leading port is still the one it holds`(): List<DynamicTest> =
        COPIED.map { path ->
            DynamicTest.dynamicTest(path) {
                val copy = File(repository, path)
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
            .describedAs("a file sync-corpus copies and this list does not name goes unchecked")
            .containsExactlyInAnyOrderElementsOf(whatSyncCorpusCopies())
    }

    private fun whatSyncCorpusCopies(): List<String> {
        val lines = File(repository, "Makefile").readLines()
        val directories =
            lines
                .filter { " = " in it }
                .associate { it.substringBefore(" = ").trim() to it.substringAfter(" = ").trim() }
        val copies =
            lines
                .dropWhile { !it.startsWith("sync-corpus:") }
                .drop(1)
                .takeWhile { it.startsWith("\t") }
        assertThat(copies).describedAs("sync-corpus copies nothing, so this test reads nothing").isNotEmpty()

        return copies.map { line ->
            val said = line.trim().split(" ")
            val file = said[1].substringAfterLast('/')
            val into = said[2].removePrefix("\$(").substringBefore(")")
            "${directories.getValue(into)}/$file"
        }
    }

    private val repository: File =
        File(
            requireNotNull(System.getProperty("wlmarkdown.repository")) {
                "nobody said where the checkout is, so these tests cannot find the shared files"
            },
        )

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
