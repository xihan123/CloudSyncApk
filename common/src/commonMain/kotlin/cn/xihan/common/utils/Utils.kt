package cn.xihan.common.utils

import androidx.compose.ui.Modifier
import com.russhwolf.settings.Settings
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import java.io.File


/**
 * BASE_API 服务器地址
 */
const val BASE_API = "http://0.0.0.0:8080/"

/**
 * 服务端内容全局保存路径 要和服务端保持一致
 */
val savePath = "server/CloudSyncApp"
/*
if (isWindows())
"server/CloudSyncApp"
else
"/home/xihan/CloudSyncApp"

 */
/**
 * android termux 保存路径
 * "/data/data/com.termux/files/home/CloudSyncApp"
 */

typealias M = Modifier

val settings: Settings = Settings()

val kJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private const val LINUX_OS_STR = "linux"
private const val WINDOWS_OS_STR = "windows"
private fun getOsName(): String {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains(LINUX_OS_STR) -> LINUX_OS_STR
        osName.contains(WINDOWS_OS_STR) -> WINDOWS_OS_STR
        else -> osName
    }
}

/**
 * 是否是windows系统
 */
fun isWindows(): Boolean = getOsName() == WINDOWS_OS_STR

/**
 * 是否是linux系统
 */
fun isLinux(): Boolean = getOsName() == LINUX_OS_STR

/**
 * 根据 File 头文件判断文件不是Apk
 */
fun File.isNotApk(): Boolean {
    val apkHeader = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
    val header = ByteArray(4)
    inputStream().use {
        it.read(header)
    }
    return !(apkHeader contentEquals header)
}

/**
 * 图片地址 拓展函数
 */
val String.iconUrl: String
    get() = "${
        BASE_API.replace(
            "https",
            "http"
        )
    }${this.removePrefix("${savePath}/")}".encodeURLPath()

/**
 * 下载地址 拓展函数
 */
val String.downloadUrl: String get() = "$BASE_API${this.removePrefix("${savePath}/")}"

/**
 * 获取 Url 拓展属性
 */
val String.url: String get() = this.removePrefix(savePath).replace("\\", "/")

/**
 * Apk 大小 拓展函数
 */
val Long.size: String
    get() = when {
        this < 1024 -> "${this}B"
        this < 1024 * 1024 -> "${this / 1024}KB"
        this < 1024 * 1024 * 1024 -> "${this / 1024 / 1024}MB"
        else -> "${this / 1024 / 1024 / 1024}GB"
    }

/**
 * MyEvent
 * @param message 消息
 * @param type 类型 0:警告 1:错误 2:成功
 */
data class MyEvent(
    val message: String,
    val type: Int
)

