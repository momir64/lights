package rs.moma.lights.data.models

enum class LightMode {
    Day,
    Night;

    override fun toString() = name.lowercase()
}