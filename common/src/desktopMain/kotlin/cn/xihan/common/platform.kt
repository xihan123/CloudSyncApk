package cn.xihan.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.AwtWindow
import com.lt.load_the_image.rememberImagePainter
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI


actual val platformName: String = "Desktop"

@Composable
actual fun FileDialog(onCloseRequest: (result: String?) -> Unit) = AwtWindow(
    create = {
        object : FileDialog(
            Frame(),
            "选择Apk文件",
            LOAD
        ) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory + file)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)

@Composable
actual fun Image(url: String, modifier: Modifier) {
    androidx.compose.foundation.Image(
        painter = rememberImagePainter(url),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

/**
 * 调用浏览器打开链接
 * @param url 链接
 */
actual fun openUrl(url: String) {
    Desktop.getDesktop().browse(URI(url))
}

actual val keyStoreFile: InputStream get() = FileInputStream("F:\\Java\\Projects\\CloudSyncApk\\build\\keystore.jks")

@Composable
actual fun UpdateScreen(modifier: Modifier) {}