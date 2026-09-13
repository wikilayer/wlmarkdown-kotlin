package org.wikilayer.wlmarkdown

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DialectTests {
    @Serializable
    data class Written(
        @SerialName("callout_class_by_marker") val calloutClassByMarker: Map<String, String>,
        @SerialName("map_marker") val mapMarker: String,
        @SerialName("ref_schemes") val refSchemes: List<String>,
    )

    private val written = read()

    private fun read(): Written {
        val spoken =
            requireNotNull(javaClass.getResourceAsStream("/rules.yaml")) {
                "the rules every port answers to are unreadable"
            }.use { it.readBytes().decodeToString() }
        val lenient = Yaml(configuration = YamlConfiguration(strictMode = false))
        return lenient.decodeFromString(Written.serializer(), spoken)
    }

    @Test
    fun `every marker the rules name carries its class`() {
        assertThat(written.calloutClassByMarker).describedAs("with no markers this test cannot fail").isNotEmpty()
        written.calloutClassByMarker.forEach { (marker, calloutClass) ->
            assertThat(Dialect().recognise("> $marker\n> Body.\n"))
                .describedAs("$marker should carry class $calloutClass")
                .isEqualTo(listOf(Found(kind = "callout", calloutClass = calloutClass, text = "Body.")))
        }
    }

    @Test
    fun `the markers on offer are every one the rules name`() {
        assertThat(Dialect().markers)
            .isEqualTo((written.calloutClassByMarker.keys + written.mapMarker).sorted())
    }

    @Test
    fun `the classes on offer are the ones the rules name`() {
        assertThat(Dialect().classes).isEqualTo(
            written.calloutClassByMarker.values
                .distinct()
                .sorted(),
        )
    }

    @Test
    fun `the schemes on offer are the ones the rules name`() {
        assertThat(Dialect().schemes).isEqualTo(written.refSchemes.sorted())
    }

    @Test
    fun `every scheme the rules name is read as one`() {
        assertThat(written.refSchemes).describedAs("with no schemes this test cannot fail").isNotEmpty()
        written.refSchemes.forEach { scheme ->
            assertThat(Dialect().recognise("A [label]($scheme:1).\n"))
                .describedAs("$scheme should be read as a scheme")
                .isEqualTo(
                    listOf(Found(kind = "link", scheme = scheme, destination = "$scheme:1", text = "label")),
                )
        }
    }

    @Test
    fun `the marker the rules name opens a place`() {
        val found = Dialect().recognise("> ${written.mapMarker}\n> 44.7866, 20.4489\n")
        assertThat(found.firstOrNull()?.kind).describedAs("the map marker went unrecognised").isEqualTo("map")
    }
}
