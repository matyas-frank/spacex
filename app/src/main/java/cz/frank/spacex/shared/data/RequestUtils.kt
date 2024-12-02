package cz.frank.spacex.shared.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class RequestBody(val query: JsonElement = JsonObject(mapOf()), val options: RequestOptions)

@Serializable
data class RequestOptions(
    val select: JsonElement,
    val sort: JsonElement = JsonObject(mapOf()),
    val limit: Int = 10,
    val page: Int = 21,
    val populate: List<JsonElement> = listOf()
)

@Serializable
data class PaginatedResponse<T>(
    val docs: List<T>,
    val page: Int,
    val hasPrevPage: Boolean,
    val hasNextPage: Boolean,
    val prevPage: Int?,
    val nextPage: Int?
)

fun JsonObjectBuilder.putSelection(selection: SelectionBuilder.() -> Unit) {
    put("select", buildSelection(selection))
}

fun buildSelection(selection: SelectionBuilder.() -> Unit) = buildJsonObject {
    object : SelectionBuilder {
        override fun select(property: String) {
            put(property, 1)
        }
    }.selection()
}

interface SelectionBuilder {
    fun select(property: String)
}
