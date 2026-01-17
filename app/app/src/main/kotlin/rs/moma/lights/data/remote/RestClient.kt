package rs.moma.lights.data.remote

import retrofit2.converter.gson.GsonConverterFactory
import rs.moma.lights.data.adapters.LightTypeAdapter
import okhttp3.logging.HttpLoggingInterceptor
import rs.moma.lights.data.models.LightType
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import com.google.gson.Gson
import retrofit2.Retrofit

object RestClient {
    private const val BASE_URL = "https://lights.moma.rs"

    private val client by lazy {
        val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                WebSocketClient.connect()
                val builder = chain.request().newBuilder()
                AuthService.getHeaderValue()?.let { builder.addHeader("auth", it) }
                val response = chain.proceed(builder.build())
                if (response.code == 401) AuthService.triggerLogout()
                response
            }
            .build()
    }

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LightType::class.java, LightTypeAdapter())
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(client)
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}