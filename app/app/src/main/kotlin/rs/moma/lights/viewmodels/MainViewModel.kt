package rs.moma.lights.viewmodels

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import rs.moma.lights.data.remote.WebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import rs.moma.lights.data.remote.AuthService
import rs.moma.lights.data.remote.RestClient
import rs.moma.lights.data.local.SecureStore
import rs.moma.lights.data.models.LightMode
import rs.moma.lights.data.models.Schedule
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.asStateFlow
import rs.moma.lights.data.models.Config
import androidx.lifecycle.viewModelScope
import rs.moma.lights.data.models.Group
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.app.Application
import android.widget.Toast

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val context = getApplication<Application>()
    private val api = RestClient.api

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _config = MutableStateFlow(Config())
    val config = _config.asStateFlow()

    init {
        WebSocketClient.configFlow.onEach { _config.value = it }.launchIn(viewModelScope)
        AuthService.logoutFlow.onEach {
            logout()
            val text = "Authentication failed, wrong password."
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }.launchIn(viewModelScope)
    }

    private suspend fun ping(): Boolean? {
        return try {
            api.ping().isSuccessful
        } catch (_: Exception) {
            null
        }
    }

    fun login(password: String? = null) = viewModelScope.launch {
        val password = password ?: SecureStore.load(context)
        AuthService.password = password
        val result = ping()

        if (result == false)
            Toast.makeText(context, "Wrong password", Toast.LENGTH_LONG).show()
        else if (result == null)
            Toast.makeText(context, "Error connecting to the server", Toast.LENGTH_LONG).show()

        if (password == null || result != true) {
            AuthService.password = null
            _isLoggedIn.value = false
            SecureStore.clear(context)
            return@launch
        }

        SecureStore.save(context, password)
        WebSocketClient.connect()
        _isLoggedIn.value = true
    }

    fun logout() = viewModelScope.launch {
        SecureStore.clear(context)
        _isLoggedIn.value = false
        AuthService.password = null
        WebSocketClient.disconnect()
    }

    fun off(id: Int, light: LightMode?) = viewModelScope.launch { api.off(id, light?.toString()) }
    fun on(id: Int, light: LightMode?) = viewModelScope.launch { api.on(id, light?.toString()) }
    fun delete(lightGroupId: Int) = viewModelScope.launch { api.delete(lightGroupId) }
    fun reset() = viewModelScope.launch { api.reset() }

    fun save() = viewModelScope.launch {
        api.save()
        Toast.makeText(context, "Configuration saved successfully", Toast.LENGTH_SHORT).show()
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        api.refresh()
        _isRefreshing.value = false
    }

    fun setBrightness(value: Float) = viewModelScope.launch {
        _config.value = _config.value.copy(brightness = value)
        api.brightness(value)
    }

    fun setNightMode(on: Boolean) = viewModelScope.launch {
        api.mode(if (on) "night" else "day")
    }

    fun setAllOff(on: Boolean) = viewModelScope.launch {
        api.allOff(if (on) "on" else "off")
    }

    fun setGroupBrightness(id: Int, brightness: Int) = viewModelScope.launch {
        api.update(_config.value.groups.map {
            if (it.id == id) it.copy(night = it.night.copy(brightness = brightness)) else it
        })
    }

    fun updateGroup(group: Group) = viewModelScope.launch {
        api.update(_config.value.groups.map { if (it.id == group.id) group else it })
    }

    fun addGroup(group: Group) = viewModelScope.launch {
        api.update(_config.value.groups + group)
    }

    fun moveGroup(fromIndex: Int, toIndex: Int) = viewModelScope.launch {
        val currentList = _config.value.groups.toMutableList()
        currentList.add(toIndex, currentList.removeAt(fromIndex))
        _config.value = _config.value.copy(groups = currentList)
        api.update(_config.value.groups)
    }

    fun schedule(hour: Int, minute: Int, allOff: Boolean, nightMode: Boolean) = viewModelScope.launch {
        api.schedule(Schedule(hour, minute, allOff, nightMode))
    }
}