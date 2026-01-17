package rs.moma.lights.data.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import rs.moma.lights.data.models.Config
import java.util.concurrent.TimeUnit
import okhttp3.WebSocketListener
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import android.util.Log
import okhttp3.Request
import okio.ByteString

object WebSocketClient {
    private const val WS_URL = "ws://lights.moma.rs/subscribe"
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private var ws: WebSocket? = null

    private val _configFlow = MutableSharedFlow<Config>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val configFlow = _configFlow.asSharedFlow()

    fun connect() {
        if (ws != null) return
        val reqBuilder = Request.Builder().url(WS_URL)
        AuthService.getHeaderValue()?.let { reqBuilder.addHeader("auth", it) }
        val request = reqBuilder.build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.i("WebSocket receive", text)
                val config = RestClient.gson.fromJson(text, Config::class.java)
                _configFlow.tryEmit(config)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {}

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                if (response?.code == 401) AuthService.triggerLogout()
                ws = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (code == 401) AuthService.triggerLogout()
                ws = null
            }
        })
    }

    fun disconnect() {
        ws?.cancel()
        ws = null
    }
}