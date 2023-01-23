package cn.xihan.common.ui.update

import android.os.Build
import cn.xihan.common.MyApplication
import cn.xihan.common.base.BaseViewModel
import cn.xihan.common.base.ViewState
import cn.xihan.common.models.AppInfoListModel
import cn.xihan.common.models.AppInfoModel
import cn.xihan.common.utils.*
import com.drake.channel.sendEvent
import com.dylanc.longan.application
import com.dylanc.longan.context
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.ktor.client.plugins.*
import io.ktor.util.cio.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

class UpdateViewModel : BaseViewModel() {

    private val _state = MutableStateFlow(CurrentUpdateViewState())
    val state: StateFlow<CurrentUpdateViewState> = _state

    /**
     * 获取所有安装的应用信息
     */
    fun getAllAppLists() {
        showLoading()
        callApi {
            val allAppInfoList = application.context.getAllAppLists()
            val file = File("$downloadPath/appInfoList.txt").apply {
                parentFile?.mkdirs()
                if (!exists()) {
                    createNewFile()
                }
            }

            file.writeText(kJson.encodeToString(allAppInfoList.map { it.packageName }))
            MyApplication().checkUpdate(
                apkFile = file.readChannel(),
                contentLength = file.length().toString()
            ) {
                onUpload { bytesSentTotal, contentLength ->
                    _state.update {
                        it.copy(
                            progress = bytesSentTotal.toFloat() / contentLength.toFloat(),
                        )
                    }
                }
            }.collect { response ->
                when {
                    response.isNotBlank() && "{" in response -> {
                        val appInfoList = kJson.decodeFromString<List<AppInfoListModel>>(response)
                        val list = arrayListOf<AndroidAppInfoModel>()
                        appInfoList.forEach { appInfoModel ->
                            allAppInfoList.find { it1 -> it1.packageName == appInfoModel.packageName }
                                ?.let { packageInfo ->
                                    val localVersion = packageInfo.versionName
                                    val localVersionCode =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageInfo.longVersionCode.toInt() else packageInfo.versionCode
                                    val apkFile = File(packageInfo.applicationInfo.sourceDir)
                                    val apkSize = apkFile.length()
                                    val apkMd5 = apkFile.md5
                                    val filePath = apkFile.absolutePath
                                    val icon = packageInfo.applicationInfo.loadIcon(application.context.packageManager)
                                    val isDownloadOrUpload = isDownloadOrUpload(
                                        appInfoModel.latestAppInfoModel?.versionCode ?: 0,
                                        localVersionCode
                                    )
                                    if (isDownloadOrUpload == -1) return@let
                                    val isIgnore =
                                        isIgnore(appInfoModel.packageName, appInfoModel.latestAppInfoModel?.versionCode)
                                    val isPermanentIgnore = appInfoModel.packageName.isPermanentIgnore
                                    if (isIgnore || isPermanentIgnore) return@let
                                    val androidAppInfoModel = AndroidAppInfoModel(
                                        name = appInfoModel.name,
                                        packageName = appInfoModel.packageName,
                                        versionName = localVersion,
                                        versionCode = localVersionCode,
                                        icon = icon,
                                        isSystem = packageInfo.applicationInfo.isSystem,
                                        size = apkSize,
                                        md5 = apkMd5,
                                        filePath = filePath,
                                        updateTime = appInfoModel.updateTime,
                                        state = isDownloadOrUpload,
                                        latestAppInfoModel = appInfoModel.latestAppInfoModel!!
                                    )
                                    list.add(androidAppInfoModel)
                                } ?: return@forEach
                        }

                        if (list.isNotEmpty()) {
                            _state.update {
                                it.copy(
                                    loading = false,
                                    refreshing = false,
                                    error = null,
                                    errorMessage = null,
                                    currentUpdateList = list
                                )
                            }
                        } else {
                            hideLoading()
                        }

                    }

                    "{" !in response -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                refreshing = false,
                                error = null,
                                errorMessage = response
                            )
                        }
                    }
                }


//                _state.update {
//                    it.copy(
//                        currentUpdateList = appInfoList
//                    )
//                }


            }


        }
    }

    /**
     * 更改显示系统应用状态
     */
    fun changeShowSystemAppStatus(isShowSystemApp: Boolean) {
        _state.update {
            it.copy(
                isShowSystemApp = isShowSystemApp
            )
        }
        settings["isShowSystem"] = isShowSystemApp
        getAllAppLists()
    }

    /**
     * 上传本地应用
     */
    fun updateApp(apkFile: File) {
        callApi {
            if (apkFile.isNotApk()) {
                sendEvent(MyEvent("上传的不是Apk", 0), "dialog")
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
                        val appInfoModel = kJson.decodeFromString<AppInfoModel>(response)
                        _state.update {
                            it.copy(
                                loading = false,
                                refreshing = false,
                                uploading = false,
                                error = null,
                                errorMessage = null
                            )
                        }
                        sendEvent(
                            MyEvent(
                                "${appInfoModel.name}-${appInfoModel.versionName}-${appInfoModel.versionCode} 上传成功",
                                2
                            ), "dialog"
                        )
                        onRefreshCurrentMain()
                    }

                    "{" !in response -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                refreshing = false,
                                uploading = false,
                                error = null,
                                errorMessage = response
                            )
                        }

                        sendEvent(MyEvent(response, 1), "dialog")
                    }
                }
            }

        }
    }

    fun onRefreshCurrentMain(showRefresh: Boolean = true) = ioLaunch {
        _state.update {
            it.copy(
                refreshing = showRefresh,
                loading = !showRefresh
            )
        }
        getAllAppLists()
    }

    override fun hideLoading() {
        _state.update {
            it.copy(
                loading = false, refreshing = false
            )
        }
    }

    private fun showLoading() {
        _state.update {
            it.copy(loading = true, refreshing = true)
        }
    }

    /**
     * 更改 uploading 状态
     */
    fun clearUploadingState() = ioLaunch {
        _state.update {
            it.copy(
                uploading = false,
                progress = 0f,
                progressPercentage = 0
            )
        }
    }

    init {
        getAllAppLists()
    }

}

data class CurrentUpdateViewState(
    override val loading: Boolean = false,
    override val refreshing: Boolean = false,
    override val error: Exception? = null,
    val errorMessage: String? = null,
    val currentUpdateList: List<AndroidAppInfoModel>? = null,
    val uploading: Boolean = false,
    val progress: Float = 0f,
    val progressPercentage: Int = 0,
    var isShowSystemApp: Boolean = settings["isShowSystem", false]
) : ViewState(loading, refreshing, error)
