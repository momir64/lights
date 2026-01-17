package rs.moma.lights.data.remote

import rs.moma.lights.data.models.Schedule
import rs.moma.lights.data.models.Group
import retrofit2.http.DELETE
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.GET
import retrofit2.Response

interface ApiService {
    @GET("/")
    suspend fun ping(): Response<Unit>

    @GET("/refresh")
    suspend fun refresh(): Response<Unit>

    @GET("/on/{id}")
    suspend fun on(@Path("id") id: Int, @Query("light") light: String?): Response<Unit>

    @GET("/off/{id}")
    suspend fun off(@Path("id") id: Int, @Query("light") light: String?): Response<Unit>

    @GET("/mode/{state}")
    suspend fun mode(@Path("state") state: String): Response<Unit>

    @GET("/all-off/{on}")
    suspend fun allOff(@Path("on") on: String): Response<Unit>

    @GET("/brightness/{value}")
    suspend fun brightness(@Path("value") value: Float): Response<Unit>

    @GET("/save")
    suspend fun save(): Response<Unit>

    @GET("/reset")
    suspend fun reset(): Response<Unit>

    @PUT("/update")
    suspend fun update(@Body groups: List<Group>): Response<Unit>

    @DELETE("/delete/{id}")
    suspend fun delete(@Path("id") id: Int): Response<Unit>

    @POST("/schedule")
    suspend fun schedule(@Body schedule: Schedule): Response<Unit>
}