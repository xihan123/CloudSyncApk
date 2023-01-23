package cn.xihan.common.repository

import cn.xihan.common.network.RemoteService
import io.ktor.client.request.*
import io.ktor.utils.io.*


class RemoteRepository(
    private val remoteService: RemoteService
) : Repository {

    /**
     * 获取 Http 版本
     */
    fun getHttpVersion() = remoteService.getHttpVersion()

    /**
     * 获取应用信息
     * @param packageName 包名
     */
    fun getAppInfo(packageName: String) = remoteService.getAppInfo(packageName)

    /**
     * 分页获取应用信息
     * @param page 页码
     * @param pageSize 每页数量
     */
    fun getAppInfoList(page: Int, pageSize: Int) =
        remoteService.getAppInfoList(page, pageSize)

    /**
     * 根据 参数 获取应用信息列表
     */
    fun getAppInfoListByParams(param: String) =
        remoteService.getAppInfoListByParams(param)

    /**
     * 校验应用更新
     */
    fun checkUpdate(
        apkFile: ByteReadChannel,
        contentLength: String,
        ext: HttpRequestBuilder.() -> Unit
    ) = remoteService.checkUpdate(
            apkFile = apkFile,
            contentLength = contentLength,
            ext = ext
        )

    /**
     * 删除云端应用
     */
    fun deleteApp(packageName: String) = remoteService.deleteApp(packageName)

    /**
     * 更新云端信息
     */
    fun updateApp(
        apkFile: ByteReadChannel,
        contentLength: String,
        ext: HttpRequestBuilder.() -> Unit
    ) = remoteService.uploadApp(
        apkFile = apkFile,
        contentLength = contentLength,
        ext = ext
    )

    /**
     * 下载云端应用
     */
    fun downloadApp(
        packageName: String,
        versionCode: Int,
        ext: HttpRequestBuilder.() -> Unit
    ) = remoteService.downloadApp(
        packageName = packageName,
        versionCode = versionCode,
        ext = ext
    )

}