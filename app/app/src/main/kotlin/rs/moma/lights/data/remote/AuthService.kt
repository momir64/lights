package rs.moma.lights.data.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import android.util.Base64

object AuthService {
    var password: String? = null

    private val _logoutFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val logoutFlow = _logoutFlow.asSharedFlow()

    fun triggerLogout(displayToast: Boolean = true) {
        _logoutFlow.tryEmit(displayToast)
    }

    private val _offlineFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val offlineFlow = _offlineFlow.asSharedFlow()

    fun triggerOffline() {
        _offlineFlow.tryEmit(Unit)
    }

    fun getHeaderValue(): String? {
        val password = password ?: return null
        return Base64.encodeToString(password.toByteArray(), Base64.NO_WRAP)
    }
}