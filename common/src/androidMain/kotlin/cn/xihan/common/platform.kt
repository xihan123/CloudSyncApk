package cn.xihan.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.xihan.common.component.ErrorItem
import cn.xihan.common.component.VsScaffold
import cn.xihan.common.ui.update.UpdateViewModel
import cn.xihan.common.utils.AndroidAppInfoItem
import cn.xihan.common.utils.M
import cn.xihan.common.utils.downloadUrl
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.channel.sendTag
import com.dylanc.longan.application
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import org.koin.androidx.compose.get
import java.io.File
import java.io.InputStream


actual val platformName: String = "Android"

@Composable
actual fun FileDialog(onCloseRequest: (result: String?) -> Unit) {

    sendTag("openFile")
    LocalLifecycleOwner.current.receiveEvent<String> {
        onCloseRequest(it)
    }
}

@Composable
actual fun Image(
    url: String, modifier: Modifier
) {
    CoilImage(
        imageModel = { url }, imageOptions = ImageOptions(
            contentScale = ContentScale.Crop, alignment = Alignment.Center
        ), modifier = modifier
    )
}

/**
 * 调用浏览器打开链接
 * @param url 链接
 */
actual fun openUrl(url: String) {
    sendEvent(url, "openUrl")
}

actual val keyStoreFile: InputStream get() = application.assets.open("keystore.jks")

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UpdateScreen(
    modifier: Modifier
) {
//    val viewModel = viewModel(UpdateViewModel::class) {
//        UpdateViewModel()
//    }
    val viewModel = get<UpdateViewModel>()
    val state by viewModel.state.collectAsState()

    VsScaffold(
        state = state,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "云端应用")
                },
                modifier = M.fillMaxWidth(),
                actions = {
                    IconButton(onClick = {
                        viewModel.onRefreshCurrentMain()
                    }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null
                        )
                    }
                },
                navigationIcon = {},
                scrollBehavior = null
            )
        }) { _, _ ->
        LazyColumn(
            modifier = M.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = M.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "显示系统应用",
                        modifier = M.weight(1f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Checkbox(
                        checked = state.isShowSystemApp, onCheckedChange = {
                            viewModel.changeShowSystemAppStatus(it)
                        }
                    )
                }
            }

            item {
                if (state.uploading) {
                    LinearProgressIndicator(
                        progress = state.progress,
                        modifier = M.fillMaxWidth().padding(16.dp)
                    )
                }
            }

            if (!state.currentUpdateList.isNullOrEmpty()) {
                items(state.currentUpdateList!!.size) {
                    AndroidAppInfoItem(
                        appInfoModel = state.currentUpdateList!![it],
                        modifier = M.wrapContentHeight().fillMaxWidth().padding(16.dp),
                        onUploadClick = {
                            viewModel.updateApp(File(state.currentUpdateList!![it].filePath))
                        },
                        onDownloadClick = {
                            openUrl(state.currentUpdateList!![it].latestAppInfoModel.apkUrl.downloadUrl)
                        }
                    )
                }
            } else {
                item {
                    ErrorItem("暂无应用更新")
                }
            }

        }

    }

    //println("list: ${list.size}")

}