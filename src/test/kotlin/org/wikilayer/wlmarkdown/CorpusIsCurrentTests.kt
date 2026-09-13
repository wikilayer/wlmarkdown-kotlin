package org.wikilayer.wlmarkdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class CorpusIsCurrentTests {
    @Test
    fun `the rules here are the rules the leading port holds`() {
        assertThat(held("/rules.yaml"))
            .describedAs(STALE, "rules.yaml")
            .isEqualTo(leading("rules.yaml"))
    }

    @Test
    fun `the cases here are the cases the leading port holds`() {
        assertThat(held("/dialect.yaml"))
            .describedAs(STALE, "dialect.yaml")
            .isEqualTo(leading("dialect.yaml"))
    }

    @Test
    fun `the rules the library carries are the rules the tests answer to`() {
        val carried =
            requireNotNull(Dialect::class.java.getResourceAsStream("/rules.yaml")) {
                "the library carries no rules.yaml, so the dialect knows nothing"
            }.use { it.readBytes() }
        assertThat(carried)
            .describedAs("the two copies of rules.yaml drifted; run make sync-corpus")
            .isEqualTo(held("/rules.yaml"))
    }

    private fun held(name: String): ByteArray =
        requireNotNull(javaClass.getResourceAsStream(name)) {
            "$name is not on the test classpath; run make sync-corpus"
        }.use { it.readBytes() }

    private fun leading(name: String): ByteArray {
        val answer =
            client.send(
                HttpRequest
                    .newBuilder(URI.create(LEADING + name))
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
        const val LEADING = "https://raw.githubusercontent.com/wikilayer/wlmarkdown/main/corpus/"
        const val SECONDS_BEFORE_GIVING_UP = 20L
        const val FOUND_IT = 200
        const val STALE =
            "%s here is not the one the leading port holds, so this port is answering an " +
                "older dialect than the others; run make sync-corpus"
    }
}
