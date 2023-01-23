package cn.xihan.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.io.InputStream


expect val platformName: String

expect val keyStoreFile: InputStream

@Composable
expect fun FileDialog(
    onCloseRequest: (result: String?) -> Unit
)

@Composable
expect fun Image(
    url: String,
    modifier: Modifier = Modifier,
)

/**
 * 调用浏览器打开链接
 * @param url 链接
 */
expect fun openUrl(url: String)

@Composable
expect fun UpdateScreen(
    modifier: Modifier = Modifier,
)


