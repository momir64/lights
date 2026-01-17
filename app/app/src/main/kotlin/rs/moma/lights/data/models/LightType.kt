package rs.moma.lights.data.models

enum class LightType {
    Switch,
    Dimmer,
    Bulb;

    override
    fun toString(): String = name.lowercase()

    companion object {
        fun fromString(value: String): LightType = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: throw IllegalArgumentException("Unknown LightType: $value")
    }
}