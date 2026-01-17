package rs.moma.lights

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import rs.moma.lights.viewmodels.MainViewModel
import rs.moma.lights.ui.components.Navigation
import androidx.activity.compose.setContent
import rs.moma.lights.ui.theme.LightsTheme
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import android.os.Bundle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vm: MainViewModel by viewModels()
        splashScreen.setKeepOnScreenCondition {
            vm.isLoggedIn.value == null
        }

        setContent {
            LightsTheme {
                Navigation()
            }
        }

        vm.login()
    }
}