package cn.xihan.common.network

import cn.xihan.common.models.AppInfoListModel
import cn.xihan.common.models.AppInfoModel
import de.jensklingenberg.ktorfit.http.*
import de.jensklingenberg.ktorfit.http.Headers
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow

interface RemoteService {

    /**
     * 获取 Http 版本
     */
    @GET("version")
    fun getHttpVersion(): Flow<String>

    /**
     * 获取应用信息
     * @param packageName 包名
     */
    @GET("appInfo")
    fun getAppInfo(@Query("packageName") packageName: String): Flow<AppInfoModel>

    /**
     * 分页获取应用信息
     * @param page 页码
     * @param pageSize 每页数量
     */
    @GET("appInfoList")
    fun getAppInfoList(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Flow<String>

    /**
     * 根据 参数 获取应用信息列表
     */
    @GET("appInfoList")
    fun getAppInfoListByParams(
        @QueryName param: String
    ): Flow<List<AppInfoListModel>>

    /**
     * 校验应用更新
     */
    @POST("update")
    @Headers("Content-Type: application/octet-stream")
//    @Headers("Content-Type: application/json")
    fun checkUpdate(
        @Body apkFile: ByteReadChannel,
        @Header("Content-Length") contentLength: String,
        @ReqBuilder ext: HttpRequestBuilder.() -> Unit
    ): Flow<String>

    /**
     * 删除云端应用
     */
    @DELETE("delete")
    fun deleteApp(@Query("packageName") packageName: String): Flow<Boolean>

    /**
     * 更新云端信息
     */
    @POST("upload")
    @Headers("Content-Type: application/octet-stream")
    fun uploadApp(
        @Body apkFile: ByteReadChannel,
        @Header("Content-Length") contentLength: String,
        @ReqBuilder ext: HttpRequestBuilder.() -> Unit
    ): Flow<String>

    /**
     * 下载云端应用
     */
    @GET("download")
    @Headers("Content-Type: application/octet-stream")
    fun downloadApp(
        @Query("packageName") packageName: String,
        @Query("versionCode") versionCode: Int,
        @ReqBuilder ext: HttpRequestBuilder.() -> Unit
    ): Flow<ByteReadChannel>

}
