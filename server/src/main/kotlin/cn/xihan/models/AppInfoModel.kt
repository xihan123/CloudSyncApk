package cn.xihan.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable


/**
 * App信息模型
 * @param name 应用名称
 * @param packageName 应用包名
 * @param latestAppInfoModel 最新版本信息模型
 * @param historyAppInfoModelList 历史版本信息模型列表
 * @param createTime 创建时间
 * @param updateTime 更新时间
 */
@Serializable
data class AppInfoListModel(
    var name: String = "",
    var packageName: String = "",
    var latestAppInfoModel: AppInfoModel? = null,
    var historyAppInfoModelList: List<AppInfoModel> = listOf(),
    var createTime: String = "",
    var updateTime: String = ""
)

/**
 * 应用信息模型
 * @param name 应用名称
 * @param packageName 应用包名
 * @param versionCode 应用版本号
 * @param versionName 应用版本名称
 * @param minSdkVersion 最低支持SDK版本
 * @param targetSdkVersion 目标SDK版本
 * @param icon 应用图标
 * @param apkUrl 应用下载地址
 * @param apkSize 应用大小
 * @param apkMd5 应用MD5
 */
@Serializable
data class AppInfoModel(
    var name: String = "",
    var packageName: String = "",
    var versionCode: Int = 0,
    var versionName: String = "",
    var minSdkVersion: Int = 0,
    var targetSdkVersion: Int = 0,
    var icon: String = "",
    var apkUrl: String = "",
    var apkSize: Long = 0L,
    var apkMd5: String = ""
)

object AppInfoListTable : IntIdTable() {

    val name = text("name")
    val packageName = text("package_name")
    val latestAppInfoModel = text("latest_app_info_model")
    val historyAppInfoModelList = text("history_app_info_model_list")
    val createTime = text("create_time")
    val updateTime = text("update_time")
}