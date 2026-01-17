package rs.moma.lights.data.models

data class Group(
    val id: Int = -1,
    val name: String = "",
    val day: Light = Light(),
    val night: Light = Light()
)