package org.wikilayer.wlmarkdown

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser

@Serializable
internal data class Coordinate(
    val signs: String,
    val digits: String,
    val point: String,
    @SerialName("latitude_within") val latitudeWithin: String,
    @SerialName("longitude_within") val longitudeWithin: String,
)

@Serializable
internal data class Rules(
    @SerialName("callout_class_by_marker") val calloutClassByMarker: Map<String, String>,
    @SerialName("map_marker") val mapMarker: String,
    val coordinate: Coordinate,
    val blanks: String,
    @SerialName("ref_schemes") val refSchemes: List<String>,
)

/** The rules and readers of the WikiLayer markdown dialect. */
class Dialect {
    internal val rules: Rules = written

    /** Every marker that can open a dialect construct, in sorted order. */
    val markers: List<String>
        get() = (rules.calloutClassByMarker.keys + rules.mapMarker).sorted()

    /** Every callout class the dialect can report, in sorted order. */
    val classes: List<String>
        get() =
            rules.calloutClassByMarker.values
                .distinct()
                .sorted()

    /** Every node-reference scheme the dialect can report, in sorted order. */
    val schemes: List<String>
        get() = rules.refSchemes.sorted()

    /** Creates a commonmark parser with the extensions and source spans the dialect requires. */
    fun parser(): Parser = built

    /** Returns dialect constructs in document order. */
    fun recognise(source: String): List<Found> = Reading(source, this).recognise()

    /** Returns the dialect scheme at the start of a destination, when present. */
    fun scheme(destination: String): String? = schemeIn(destination).ifEmpty { null }

    internal fun schemeIn(destination: String): String =
        rules.refSchemes.firstOrNull { destination.startsWith("$it:") } ?: ""

    private companion object {
        val written: Rules = read()

        val built: Parser =
            Parser
                .builder()
                .extensions(
                    listOf(
                        TablesExtension.create(),
                        StrikethroughExtension.create(),
                        TaskListItemsExtension.create(),
                        AutolinkExtension.create(),
                    ),
                ).includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build()

        fun read(): Rules {
            val spoken =
                requireNotNull(Dialect::class.java.getResourceAsStream("/rules.yaml")) {
                    "rules.yaml is not on the classpath, so the dialect knows nothing"
                }.use { it.readBytes().decodeToString() }
            return Yaml.default.decodeFromString(Rules.serializer(), spoken)
        }
    }
}
