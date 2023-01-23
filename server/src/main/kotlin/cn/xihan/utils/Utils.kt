package cn.xihan.utils

import cn.xihan.models.AppInfoModel
import cn.xihan.savePath
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

typealias Result = Map<String, List<File>>

/**
 * 算法
 */
enum class Algorithm {
    MD5, SHA1, SHA256;

    private val _defaultBlockSize: Int = 2048
    fun getHash(file: File, blockSize: Int = _defaultBlockSize): String {
        val messageDigest = MessageDigest.getInstance(this.name)
        messageDigest.reset()
        file.forEachBlock(blockSize) { bytes, size ->
            messageDigest.update(bytes, 0, size)
        }
        return messageDigest
            .digest()
            .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
            .uppercase()
    }

}

suspend fun getHashes(target: File): Map<File, String> = coroutineScope {
    val files = target.walkTopDown().filter { it.isFile }.toList()
    val hashes = files
        .map { file -> async { Algorithm.MD5.getHash(file) } }
        .toList()
    files.zip(hashes.awaitAll()).toMap()
}

suspend fun mapHashes(target: File): Result =
    getHashes(target).asSequence().groupBy({ it.value }, { it.key })

fun hashCalculationAsync(
    target: File,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
): Deferred<Result> {
    return scope.async { mapHashes(target) }
}

inline fun safeRun(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
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
 * 是 Debian 系统
 */
fun isDebian(): Boolean = isLinux() && File("/etc/debian_version").exists()

/**
 * 解析 Apk 信息
 */
fun String.analysisAPK(): AppInfoModel? = runCatching {
    val appInfoModel = AppInfoModel()
    val runtime = Runtime.getRuntime()
    val command = when {
        isDebian() -> "aapt2 dump badging $this"
        isWindows() -> "cmd /c aapt dump badging $this"
        isLinux() -> "aapt dump badging $this"
        else -> return@runCatching null
    }
    val process = runtime.exec(command)
    val inputStream = process.inputStream
    val bufferedReader = inputStream.bufferedReader()
    val line = bufferedReader.readText()
    // 查找包名
    val packageName = Regex("package: name='(.*?)' versionCode").find(line)?.groupValues?.get(1)
    // 查找版本号
    val versionCode = Regex("versionCode='(.*?)' versionName").find(line)?.groupValues?.get(1)?.toInt()
    // 查找版本名称
    val versionName = Regex("versionName='(.*?)' platformBuildVersionName").find(line)?.groupValues?.get(1)

    // 查找图标名称
    val icon = Regex("application: label='(.*?)' icon='(.*?)'").find(line)?.groupValues?.get(2)
    // 查找应用名称
    val appName = Regex("application-label:'(.*?)'").find(line)?.groupValues?.get(1)
    // 查找minSdkVersion
    val minSdkVersion = Regex("sdkVersion:'(.*?)'").find(line)?.groupValues?.get(1)?.toInt()
    // 查找targetSdkVersion
    val targetSdkVersion = Regex("targetSdkVersion:'(.*?)'").find(line)?.groupValues?.get(1)?.toInt()
    appInfoModel.packageName = packageName ?: ""
    appInfoModel.versionCode = versionCode ?: 0
    appInfoModel.versionName = versionName ?: ""
    //appInfoModel.icon = icon ?: ""
    appInfoModel.name = appName ?: ""
    appInfoModel.minSdkVersion = minSdkVersion ?: 0
    appInfoModel.targetSdkVersion = targetSdkVersion ?: 0
    if (!icon.isNullOrBlank() && ".xml" !in icon) {
        // 解压 Apk 获取图标
        unZipApk(this, icon, isLinux(), appInfoModel, runtime)
    }
    appInfoModel
}.getOrNull()

/**
 * 解压 Apk 获取图标
 * @param apkPath Apk 路径
 * @param iconPath 图标路径
 * @param isLinux 是否是 Linux 系统
 * @param appInfoModel AppInfoModel
 * @param runtime 运行命令
 */
@Throws(Exception::class)
fun unZipApk(
    apkPath: String,
    iconPath: String,
    isLinux: Boolean = isLinux(),
    appInfoModel: AppInfoModel,
    runtime: Runtime
) {
    val path = "$savePath/${appInfoModel.name}"
    val iconOutPath = "$path/icon/${appInfoModel.versionCode}/"
    val iconOutFile = File(iconOutPath)
    iconOutFile.parentFile?.mkdirs()
    if (!iconOutFile.exists()) {
        iconOutFile.mkdirs()
    }
    safeRun {
        // 删除子目录文件
        iconOutFile.listFiles()?.forEach { it.delete() }
    }

    val process = if (isLinux) {
        runtime.exec("7za e -y $apkPath $iconPath -o${iconOutFile.absolutePath}")
    } else {
        // windows 运行 cmd 命令
        runtime.exec("cmd /c 7z e -y $apkPath $iconPath -o${iconOutFile.absolutePath}")
    }
    process.waitFor()
    val iconSuffix = iconPath.substring(iconPath.lastIndexOf(".") + 1)
    val newName = "${appInfoModel.packageName}.$iconSuffix"
    val iconFile = iconOutFile.listFiles()?.first()
    iconFile?.let {
        safeRun {
            it.renameTo(File(iconOutPath, newName))
        }
        appInfoModel.icon = iconOutPath + newName
    }

}

/**
 * 获取 Url 拓展属性
 */
val String.url: String get() = this.removePrefix(savePath).replace("\\", "/")

/**
 * 生成 yyyy-MM-dd HH:mm:ss 格式的时间
 */
val Long.formatTime: String get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(this))

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









