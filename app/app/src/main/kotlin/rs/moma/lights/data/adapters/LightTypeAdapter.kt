package rs.moma.lights.data.adapters

import rs.moma.lights.data.models.LightType
import java.lang.reflect.Type
import com.google.gson.*

class LightTypeAdapter : JsonSerializer<LightType>, JsonDeserializer<LightType> {
    override fun serialize(
        src: LightType, typeOfSrc: Type, context: JsonSerializationContext
    ): JsonElement = JsonPrimitive(src.name.lowercase())

    override fun deserialize(
        json: JsonElement, typeOfT: Type, context: JsonDeserializationContext
    ): LightType = LightType.fromString(json.asString)
}
