
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.xihan.common.component.MainPage
import cn.xihan.common.di.appModule
import cn.xihan.common.utils.M
import moe.tlaster.precompose.PreComposeWindow
import org.koin.core.context.startKoin


fun main() {
    startKoin {
        modules(appModule)
    }
    application {
        val windowState = rememberWindowState()
        PreComposeWindow(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "云端同步Apk",
        ) {
            MainPage(modifier = M.fillMaxSize())
        }
    }
}

