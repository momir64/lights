package rs.moma.lights

import androidx.lifecycle.DefaultLifecycleObserver
import rs.moma.lights.data.remote.WebSocketClient
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import android.app.Application

class LightsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }
}

class AppLifecycleObserver : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        WebSocketClient.disconnect()
        WebSocketClient.connect()
    }
}
