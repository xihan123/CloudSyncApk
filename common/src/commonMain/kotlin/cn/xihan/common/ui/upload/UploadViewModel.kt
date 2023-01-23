package cn.xihan.common.ui.upload

import cn.xihan.common.MyApplication
import cn.xihan.common.base.BaseViewModel
import cn.xihan.common.base.ViewState
import cn.xihan.common.models.AppInfoModel
import cn.xihan.common.utils.isNotApk
import cn.xihan.common.utils.kJson
import io.ktor.client.plugins.*
import io.ktor.util.cio.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.decodeFromString
import java.io.File

class UploadViewModel : BaseViewModel() {

    private val _state = MutableStateFlow(CurrentUploadViewState())
    val state: StateFlow<CurrentUploadViewState> = _state

    /**
     * 上传新应用
     */
    fun updateApp(apkFile: File) {
        callApi {
            if (apkFile.isNotApk()){
                _state.update {
                    it.copy(
                        errorMessage = "上传文件不是apk"
                    )
                }
                return@callApi
            }
            MyApplication().updateApp(
                apkFile = apkFile.readChannel(),
                contentLength = apkFile.length().toString()
            ) {
                onUpload { bytesSentTotal, contentLength ->
                    _state.update {
                        it.copy(
                            uploading = true,
                            progress = bytesSentTotal.toFloat() / contentLength.toFloat(),
                            progressPercentage = (bytesSentTotal * 100 / contentLength).toInt()
                        )
                    }
                }
            }.collect { response ->
                when {
                    response.isNotBlank() && "{" in response -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                refreshing = false,
                                uploading = false,
                                error = null,
                                errorMessage = null,
                                uploadAppInfoModel = kJson.decodeFromString(response)
                            )
                        }
                    }

                    "{" !in response -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                refreshing = false,
                                uploading = false,
                                error = null,
                                errorMessage = response,
                                uploadAppInfoModel = null
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 更改 uploading 状态
     */
    fun clearUploadingState() = ioLaunch {
        _state.update {
            it.copy(
                uploadAppInfoModel = null,
                errorMessage = null,
                uploading = false,
                progress = 0f,
                progressPercentage = 0
            )
        }
    }

}

data class CurrentUploadViewState(
    override val loading: Boolean = false,
    override val refreshing: Boolean = false,
    override val error: Exception? = null,
    val errorMessage: String? = null,
    val uploading: Boolean = false,
    val progress: Float = 0f,
    val progressPercentage: Int = 0,
    val uploadAppInfoModel: AppInfoModel? = null
) : ViewState(loading, refreshing, error)