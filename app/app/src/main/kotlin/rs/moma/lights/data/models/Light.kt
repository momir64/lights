package rs.moma.lights.data.models

data class Light(
    val ip: String = "",
    val type: LightType = LightType.Switch,
    val id: Int? = null,
    val brightness: Int? = null,
    val on: Boolean? = null
)