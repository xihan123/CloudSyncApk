package cn.xihan.plugins

import cn.xihan.models.AppInfoListModel
import cn.xihan.models.PageModel
import cn.xihan.savePath
import cn.xihan.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.*

fun Application.configureRouting() {
    install(AutoHeadResponse)
    routing {
        get("/version") {
            call.respondText("HTTP version is ${call.request.httpVersion}")
        }

        /**
         * 服务器如已有网站 则无需使用此方法
         */
        static("/") {
            staticRootFolder = File(savePath)
            files(".")
        }

        post("/upload") {
            val contentLength =
                call.request.header(HttpHeaders.ContentLength)?.toLong() ?: return@post call.respondText("上传文件为空")
            println("contentLength: $contentLength")
            val tempFile = File("$savePath/temp/temp-${UUID.randomUUID()}.apk").apply {
                if (!exists()) {
                    parentFile.mkdirs()
                    createNewFile()
                } else {
                    delete()
                    createNewFile()
                }
            }
            call.receiveChannel().copyAndClose(tempFile.writeChannel())
            if (tempFile.isNotApk()) {
                return@post call.respondText("上传文件不是apk")
            }
            runBlocking(Dispatchers.IO) {
                if (tempFile.length() == contentLength) {
                    println("tempFilePath: ${tempFile.absolutePath}\nfileLength:${tempFile.length()}")
                    tempFile.absolutePath.analysisAPK()?.let { appInfoModel ->
                        val apkPath = "$savePath/${appInfoModel.name}/"
                        val apkName = "${appInfoModel.name}-${appInfoModel.versionName}-${appInfoModel.versionCode}.apk"
                        val apkFile = File("$apkPath/$apkName").apply {
                            if (!exists()) {
                                parentFile.mkdirs()
                                createNewFile()
                            }
                        }
                        println("apkName: ${apkFile.name}")
                        tempFile.copyTo(apkFile, true)
                        tempFile.delete()
                        appInfoModel.apkUrl = apkFile.path.url
                        appInfoModel.apkSize = apkFile.length()
                        appInfoModel.apkMd5 = getHashes(apkFile).values.first()
                        when {
                            appInfoModel.name.isBlank() -> {
                                return@let call.respondText("解析失败,应用名为空")
                            }

                            appInfoModel.packageName.isBlank() -> {
                                return@let call.respondText("解析失败,包名为空")
                            }
                        }
                        val appInfoListModel =
                            dao.findAppInfoByPackageName(appInfoModel.packageName) ?: AppInfoListModel()
                        if (appInfoListModel.name.isBlank() && appInfoListModel.packageName.isBlank()) {
                            appInfoListModel.name = appInfoModel.name
                            appInfoListModel.packageName = appInfoModel.packageName
                        }
                        appInfoListModel.updateTime = System.currentTimeMillis().formatTime
                        if (appInfoListModel.createTime.isBlank()) {
                            appInfoListModel.createTime = System.currentTimeMillis().formatTime
                        }
                        appInfoListModel.historyAppInfoModelList += appInfoModel
                        if (appInfoListModel.historyAppInfoModelList.size > 1) {
                            appInfoListModel.historyAppInfoModelList =
                                appInfoListModel.historyAppInfoModelList.distinctBy { it.versionCode }
                            val latestAppInfoModel =
                                appInfoListModel.historyAppInfoModelList.sortedByDescending { it1 -> it1.versionCode }
                            appInfoListModel.latestAppInfoModel = latestAppInfoModel.first()
                        } else {
                            appInfoListModel.latestAppInfoModel = appInfoModel
                        }
                        dao.updateAppInfo(appInfoListModel)
                        call.respondText(json.encodeToString(appInfoModel), contentType = ContentType.Application.Json)
                    } ?: call.respondText("解析失败")
                }
            }
        }

        get("/appInfo") {
            val packageName = call.request.queryParameters["packageName"]
            packageName?.let {
                dao.findAppInfoByPackageName(packageName)?.let {
                    call.respondText(json.encodeToString(it), contentType = ContentType.Application.Json)
                } ?: call.respondText("未找到该应用")
            } ?: call.respondText("未找到应用信息")
        }

        post("/update") {
            //val packageNameList = call.request.queryParameters["packageNameList"]?.split(",")
            val contentLength =
                call.request.header(HttpHeaders.ContentLength)?.toLong() ?: return@post call.respondText("上传文件为空")
            println("update contentLength: $contentLength")
            val tempFile = File("$savePath/temp/temp${UUID.randomUUID()}.txt").apply {
                if (!exists()) {
                    parentFile.mkdirs()
                    createNewFile()
                } else {
                    delete()
                    createNewFile()
                }
            }
            call.receiveChannel().copyAndClose(tempFile.writeChannel())

            runBlocking(Dispatchers.IO) {
                println("update tempFilePath: ${tempFile.absolutePath}\nfileLength:${tempFile.length()}")
                if (tempFile.length() == contentLength) {
                    val packageNameList = json.decodeFromString<List<String>>(tempFile.readText())

                    println("packageNameList: $packageNameList")

                    if (packageNameList.isEmpty()) {
                        tempFile.delete()
                        return@runBlocking call.respondText("未找到应用信息")
                    }
                    val appInfoList = dao.findAppInfoByPackageNameList(packageNameList)
                    if (appInfoList.isEmpty()) {
                        tempFile.delete()
                        return@runBlocking call.respondText("未找到应用信息")
                    }
                    call.respondText(json.encodeToString(appInfoList), contentType = ContentType.Application.Json)
                    tempFile.delete()
                }
            }

        }

        get("/appInfoList") {
            val param = call.request.queryParameters["param"]
            if (!param.isNullOrBlank()) {
                val list = dao.findAppInfoByParam(param)
                return@get call.respondText(json.encodeToString(list), contentType = ContentType.Application.Json)
            }
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val appInfoList = dao.getAppInfoByPage(page, pageSize)
            if (appInfoList.isEmpty()){
                return@get call.respondText("未找到应用信息")
            }
            val total = dao.getTotalPage(pageSize)
            println("total: $total")
            val pageModel = PageModel(
                page = page, pageSize = pageSize, total = total, list = appInfoList
            )
            call.respondText(json.encodeToString(pageModel), contentType = ContentType.Application.Json)
        }

        delete("/delete") {
            val packageName = call.request.queryParameters["packageName"]
            val versionCode = call.request.queryParameters["versionCode"]?.toInt()
            when {
                !packageName.isNullOrBlank() -> {
                    val isSuccess = dao.deleteAppInfo(packageName)
                    val file = File("$savePath/$it")
                    when {
                        isSuccess && file.exists() -> {
                            file.deleteRecursively()
                            call.respondText("删除成功")
                        }

                        !file.exists() -> call.respondText("未找到该应用")
                        else -> call.respondText("删除失败")
                    }
                }

                !packageName.isNullOrBlank() && versionCode != null -> {
                    val isSuccess = dao.deleteAppInfo(packageName, versionCode)
                    val apkFile = File("$savePath/$packageName/").listFiles()
                        ?.find { it1 -> it1.name.contains(versionCode.toString()) }
                    when {
                        isSuccess && apkFile != null && apkFile.exists() -> {
                            apkFile.delete()
                            call.respondText("删除成功")
                        }

                        apkFile == null || !apkFile.exists() -> call.respondText("未找到该应用")
                        else -> call.respondText("删除失败")
                    }

                }

                else -> call.respondText("参数错误")
            }

        }

        // downloaded
        get("/download") {
            val packageName = call.request.queryParameters["packageName"]
            val versionCode = call.request.queryParameters["versionCode"]?.toInt()
            if (packageName.isNullOrBlank() || versionCode == null) {
                call.respondText("参数错误")
                return@get
            }
            val appInfoModel = dao.findAppInfoByPackageName(packageName)
            if (appInfoModel == null) {
                call.respondText("未找到该应用")
                return@get
            }
            val responseAppInfoListModel =
                appInfoModel.historyAppInfoModelList.firstOrNull { it.versionCode == versionCode }
            responseAppInfoListModel?.let {
                val apkFile = File(it.apkUrl)
                if (!apkFile.exists()) {
                    call.respondText("未找到该应用")
                    return@get
                }
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        "${it.name}-${it.versionName}-${it.versionCode}.apk"
                    )
                        .toString()
                )
                call.respondFile(apkFile)
            } ?: call.respondText("未找到该应用")


        }

    }
}
