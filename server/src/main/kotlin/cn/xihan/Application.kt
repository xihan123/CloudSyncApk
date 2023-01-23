package cn.xihan

import cn.xihan.plugins.configureHTTP
import cn.xihan.plugins.configureRouting
import cn.xihan.plugins.configureSerialization
import cn.xihan.utils.DatabaseFactory
import cn.xihan.utils.isWindows
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * 服务端内容全局保存路径
 * linux 建议使用全路径
 * windows 全路径和相对路径都可以 以下为相对路径也就是运行目录下的server文件夹
 */
val savePath = if (isWindows())
    "server/CloudSyncApp"
else
    "/home/xihan/CloudSyncApp"

/**
 * 证书别名
 */
const val certificateAlias = "xihan"
/**
 * 证书密码 我随机生成的
 */
const val certificatePassword = "6ozaqkmiK2UJ"
/**
 * 数据库地址  数据库名
 */
const val dbUrl = "jdbc:mysql://192.168.43.110:3306/test2"
/**
 * 数据库用户名
 */
const val dbName ="test2"
/**
 * 数据库密码
 */
const val dbPassword = "test2"

fun main() {
    /**
     * 初始化数据库
     */
    /**
     * 我使用的是mysql数据库 你可以使用其他数据库 如sqlite等
     */
    DatabaseFactory.init()
    /**
     * 需要SSL证书取消以下注释
     */
    /*
    val keyStoreFile = File("build/keystore.jks")
    val keyStoreFileBak = File("android/src/main/assets/keystore.jks")
    val keyStore = buildKeyStore {
        certificate(certificateAlias) {
            password = certificatePassword
            domains = listOf(
                "127.0.0.1",
                "0.0.0.0:8080",
                "localhost",
                "xihan.site",
                "*.xihan.site"
            )
            daysValid = 36500
            keySizeInBits = 4096
            hash = HashAlgorithm.SHA256
        }
    }
    keyStore.apply {
        saveToFile(keyStoreFile, certificatePassword)
        saveToFile(keyStoreFileBak, certificatePassword)
    }

     */

    val environment = applicationEngineEnvironment {
        connector {
            // 改为自己本机IP 或者 127.0.0.1, 0.0.0.0 都可以
            host = "0.0.0.0"
            // 服务端口
            port = 8080
        }

        // SSL证书配置
        /*
        sslConnector(
            keyStore = keyStore,
            keyAlias = certificateAlias,
            keyStorePassword = { certificatePassword.toCharArray() },
            privateKeyPassword = { certificatePassword.toCharArray() }) {
            port = 8443//33333
            keyStorePath = keyStoreFile
        }

         */

        module {
            configureSerialization()
            configureHTTP()
            configureRouting()
        }
    }
    embeddedServer(Netty, environment).start(wait = true)
}
