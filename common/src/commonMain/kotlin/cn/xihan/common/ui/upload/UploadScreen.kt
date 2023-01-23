package cn.xihan.common.ui.upload

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xihan.common.FileDialog
import cn.xihan.common.component.AppInfoItem
import cn.xihan.common.component.ErrorItem
import cn.xihan.common.utils.M
import com.lt.compose_views.util.rememberMutableStateOf
import moe.tlaster.precompose.ui.viewModel
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel = viewModel(UploadViewModel::class) { UploadViewModel() },
) {
    val state by viewModel.state.collectAsState()
    val isDialogOpen = rememberMutableStateOf(false)
    val fileFilePath = rememberMutableStateOf("")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "云端应用")
                },
                modifier = M
                    .fillMaxWidth(),
                actions = {},
                navigationIcon = {},
                scrollBehavior = null
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = M.fillMaxSize().padding(padding)
        ) {
            item {
                Card(
                    modifier = M.fillMaxWidth().wrapContentHeight().padding(16.dp),
                ) {
                    Column {
                        TextField(
                            value = fileFilePath.value,
                            onValueChange = {
                                fileFilePath.value = it
                            },
                            singleLine = true,
                            modifier = M.fillMaxWidth(),
                            label = {
                                Text("文件路径")
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    isDialogOpen.value = true
                                    viewModel.clearUploadingState()
                                }) {
                                    Icon(
                                        Icons.Filled.FileOpen,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        Button(
                            onClick = {
                                viewModel.updateApp(File(fileFilePath.value))
                            },
                            modifier = M.fillMaxWidth()
                        ) {
                            Text("上传")
                        }


                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = state.progress,
                                modifier = M.weight(1f).padding(8.dp)
                            )
                            Text(
                                text = "${state.progressPercentage}%",
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        when {
                            state.uploadAppInfoModel != null -> {
                                AppInfoItem(state.uploadAppInfoModel!!)
                            }

                            !state.errorMessage.isNullOrBlank() -> {
                                ErrorItem(state.errorMessage ?: "未知错误")
                            }
                        }
                    }
                }
            }
        }

        if (isDialogOpen.value) {
            FileDialog(
                onCloseRequest = {
                    isDialogOpen.value = false
                    if (!it.isNullOrBlank() && "null" !in it) {
                        fileFilePath.value = it
                    }
                    println("it: $it")
                }
            )

        }

    }

}
