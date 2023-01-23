package cn.xihan.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xihan.common.models.AppInfoModel
import com.lt.compose_views.other.VerticalSpace
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android 信息模型
 * @param name 应用名称
 * @param packageName 包名
 * @param versionName 版本名称
 * @param versionCode 版本号
 * @param icon 图标
 * @param isSystem 是否为系统应用
 * @param size 应用大小
 * @param md5 MD5
 * @param filePath 文件路径
 * @param downloadFilePath 下载文件路径
 * @param updateTime 更新时间
 * @param state 下载或上传 0:下载 1:上传 2:安装
 * @param latestAppInfoModel 最新版本信息
 */
data class AndroidAppInfoModel(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val icon: Drawable,
    val isSystem: Boolean,
    val size: Long,
    val md5: String,
    var filePath: String = "",
    var downloadFilePath: String = "",
    val updateTime: String,
    var state: Int,
    val latestAppInfoModel: AppInfoModel
)

// 获取所有安装的应用信息
@SuppressLint("QueryPermissionsNeeded")
fun Context.getAllAppLists(): List<PackageInfo> {
    val privatePackageInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getInstalledPackages(0)
    }
    if (!settings["isShowSystem", false]) {
        privatePackageInfoList.removeAll { packageInfo ->
            packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        }
    }
    println("packageInfoListSize: ${privatePackageInfoList.size}")
    return privatePackageInfoList
}


/**
 * android 应用信息模型
 */
@Composable
fun AndroidAppInfoItem(
    appInfoModel: AndroidAppInfoModel,
    modifier: Modifier = Modifier,
    onUploadClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onInstallClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = M
                .wrapContentSize()
        ) {
            Row(
                modifier = M
                    .padding(16.dp)
            ) {
                CoilImage(
                    imageModel = { appInfoModel.icon },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    ),
                    modifier = M
                        .size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = M.weight(1f)
                ) {

                    Text(
                        text = appInfoModel.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    VerticalSpace(5.dp)

                    val downloadOrUploadText =
                        "${appInfoModel.latestAppInfoModel.versionName} ${if (appInfoModel.state == 0) ">" else "<"} ${appInfoModel.versionName}"
                    Text(
                        text = "$downloadOrUploadText\n${appInfoModel.updateTime.timeStamp.timeAgo}  ${appInfoModel.latestAppInfoModel.apkSize.size}",
                    )
                }


                IconButton(onClick = {

                }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null)
                }

                Button(
                    onClick = {
                        when (appInfoModel.state) {
                            0 -> onDownloadClick()
                            1 -> onUploadClick()
                            else -> onInstallClick()
                        }
                    }
                ) {
                    Text(
                        text = when (appInfoModel.state) {
                            0 -> "下载"
                            1 -> "上传"
                            else -> "安装"
                        }
                    )
                }
            }

        }


    }
}


/**
 * 获取文件的 MD5 值 拓展属性
 */
val File.md5 get() = md5()
private fun File.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    val inputStream = FileInputStream(this)
    val buffer = ByteArray(8192)
    var length = inputStream.read(buffer)
    while (length != -1) {
        digest.update(buffer, 0, length)
        length = inputStream.read(buffer)
    }
    val bigInt = BigInteger(1, digest.digest())
    return bigInt.toString(16)
}

/**
 * 被永久忽略的应用
 */
val permanentIgnoreAppList: List<String>
    get() = settings["permanentIgnoreAppList", "[]"].split(",")

/**
 * 被忽略的应用
 */
val ignoreAppList: List<Map<String, Int>>
    get() = settings["ignoreAppList", "a:1"].split(",").map {
        val split = it.split(":")
        mapOf(split[0] to split[1].toInt())
    }

/**
 * 新增被永久忽略的应用
 */
fun addPermanentIgnoreApp(packageName: String) {
    val list = permanentIgnoreAppList.toMutableList()
    list.add(packageName)
    settings["permanentIgnoreAppList"] = list.joinToString(",")
}

/**
 * 移除被永久忽略的应用
 */
fun removePermanentIgnoreApp(packageName: String) {
    val list = permanentIgnoreAppList.toMutableList()
    list.remove(packageName)
    settings["permanentIgnoreAppList"] = list.joinToString(",")
}

/**
 * 新增被忽略的应用
 */
fun addIgnoreApp(packageName: String, versionCode: Int) {
    val list = ignoreAppList.toMutableList()
    list.add(mapOf(packageName to versionCode))
    settings["ignoreAppList"] = list.joinToString(",")
}

/**
 * 移除被忽略的应用
 */
fun removeIgnoreApp(packageName: String) {
    val list = ignoreAppList.toMutableList()
    list.filter { it.containsKey(packageName) }.forEach {
        list.remove(it)
        settings["ignoreAppList"] = list.joinToString(",")
    }
}

/**
 * 批量忽略
 */
fun batchIgnoreApp(packageNameList: List<String>, isPermanentIgnore: Boolean = true) {
    val list = (if (isPermanentIgnore) permanentIgnoreAppList else ignoreAppList).toMutableList()
    list.addAll(packageNameList)
    settings["ignoreAppList"] = list.joinToString(",")
}

/**
 * 是否永久忽略 拓展属性
 */
val String.isPermanentIgnore: Boolean
    get() = this in permanentIgnoreAppList

/**
 * 是否忽略 拓展函数
 * @param packageName 包名
 * @param versionCode 版本号
 */
fun isIgnore(packageName: String, versionCode: Int?): Boolean =
    ignoreAppList.any { it.containsKey(packageName) && it[packageName] != versionCode }


/**
 * 下载还是上传
 * @param latestVersionCode 最新版本号
 * @param localVersionCode 本地版本号
 */
fun isDownloadOrUpload(latestVersionCode: Int, localVersionCode: Int): Int {
    return when {
        latestVersionCode > localVersionCode -> 0
        latestVersionCode < localVersionCode -> 1
        else -> -1
    }
}

/**
 * 是否系统应用 拓展属性
 */
val ApplicationInfo.isSystem: Boolean
    get() = flags and ApplicationInfo.FLAG_SYSTEM != 0

/**
 * 字符串时间转为时间戳 拓展属性
 */
val String.timeStamp: Long
    get() = try {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }

/**
 * 传入 时间 返回 多久前 拓展属性
 */
val Long.timeAgo: String
    get() {
        val now = System.currentTimeMillis()
        val diff = now - this
        return when {
            diff < 60000 -> "刚刚"
            diff < 3600000 -> "${diff / 60000}分钟前"
            diff < 86400000 -> "${diff / 3600000}小时前"
            diff < 2592000000 -> "${diff / 86400000}天前"
            diff < 31104000000 -> "${diff / 2592000000}月前"
            else -> "${diff / 31104000000}年前"
        }
    }


/**
 * 下载路径 静态属性
 */
val downloadPath: String
    get() = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/CloudSyncApk"

/*
/**
 * 安装Apk
 */
fun File.installApk() {
    if (!exists()) return
    try {
        // 申请su权限
        val process = Runtime.getRuntime().exec("su")
        val dataOutputStream = DataOutputStream(process.outputStream)
        // 执行pm install命令
        dataOutputStream.writeBytes("pm install -r $absolutePath\n")
        dataOutputStream.writeBytes("exit\n")
        dataOutputStream.flush()
        process.waitFor()
        // 获取命令执行结果
        val inputStream = BufferedReader(InputStreamReader(process.inputStream))
        val msg = StringBuffer()
        var line: String?
        while (inputStream.readLine().also { line = it } != null) {
            msg.append(line)
        }
        if (msg.isNotEmpty()) {
            println("msg: $msg")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

 */






