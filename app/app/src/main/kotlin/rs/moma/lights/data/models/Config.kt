package rs.moma.lights.data.models

import com.google.gson.annotations.SerializedName

data class Config(
    @SerializedName("night_mode")
    val nightMode: Boolean = false,

    @SerializedName("all_off")
    val allOff: Boolean = false,

    var brightness: Float = 50f,
    var groups: List<Group> = emptyList(),
    val schedule: Schedule = Schedule(),
    val scalable: Boolean = true
)