package rs.moma.lights.data.models

import com.google.gson.annotations.SerializedName

data class Schedule(
    val hour: Int = 0,
    val minute: Int = 0,

    @SerializedName("all_off")
    val allOff: Boolean = false,

    @SerializedName("night_mode")
    val nightMode: Boolean = false,
)