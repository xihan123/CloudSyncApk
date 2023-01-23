package cn.xihan.common

import cn.xihan.common.repository.RemoteRepository
import io.ktor.client.request.*
import io.ktor.utils.io.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyApplication : KoinComponent {

    private val remoteRepository: RemoteRepository by inject()

    /**
     * 获取 Http 版本
     */
    fun getHttpVersion() = remoteRepository.getHttpVersion()

    /**
     * 获取应用信息
     * @param packageName 包名
     */
    fun getAppInfo(packageName: String) = remoteRepository.getAppInfo(packageName)

    /**
     * 分页获取应用信息
     * @param page 页码
     * @param pageSize 每页数量
     */
    fun getAppInfoList(page: Int, pageSize: Int) =
        remoteRepository.getAppInfoList(page, pageSize)

    /**
     * 根据 参数 获取应用信息列表
     */
    fun getAppInfoListByParams(param: String) =
        remoteRepository.getAppInfoListByParams(param)

    /**
     * 校验应用更新
     */
    fun checkUpdate(
        apkFile: ByteReadChannel,
        contentLength: String,
        ext: HttpRequestBuilder.() -> Unit
    ) = remoteRepository.checkUpdate(
        apkFile = apkFile,
        contentLength = contentLength,
        ext = ext
    )

    /**
     * 删除云端应用
     */
    fun deleteApp(packageName: String) = remoteRepository.deleteApp(packageName)

    /**
     * 更新云端信息
     */
    fun updateApp(
        apkFile: ByteReadChannel,
        contentLength: String,
        ext: HttpRequestBuilder.() -> Unit
    ) = remoteRepository.updateApp(
        apkFile = apkFile,
        contentLength = contentLength,
        ext = ext
    )

    /**
     * 下载应用
     */
    fun downloadApp(
        packageName: String,
        versionCode: Int,
        ext: HttpRequestBuilder.() -> Unit
    ) = remoteRepository.downloadApp(packageName, versionCode, ext)

}