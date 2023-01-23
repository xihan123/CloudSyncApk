package cn.xihan.utils

import cn.xihan.dbName
import cn.xihan.dbPassword
import cn.xihan.dbUrl
import cn.xihan.models.AppInfoListModel
import cn.xihan.models.AppInfoListTable
import cn.xihan.plugins.json
import cn.xihan.utils.DatabaseFactory.dbQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val mysql = Database.connect(
            url = dbUrl,
            driver = "com.mysql.cj.jdbc.Driver",
            user = dbName,
            password = dbPassword
        )
        transaction(mysql) {
            SchemaUtils.create(AppInfoListTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}

interface DAOFacade {

    /**
     * 获取所有应用信息
     */
    suspend fun getAllAppInfo(): List<AppInfoListModel>

    /**
     * 分页获取应用信息
     */
    suspend fun getAppInfoByPage(page: Int, pageSize: Int): List<AppInfoListModel>

    /**
     * 根据每页数量获取总页数
     */
    suspend fun getTotalPage(pageSize: Int): Int

    /**
     * 根据 包名 获取的应用信息
     */
    suspend fun findAppInfoByPackageName(packageName: String): AppInfoListModel?

    /**
     * 根据 包名列表 获取的应用信息
     */
    suspend fun findAppInfoByPackageNameList(packageNameList: List<String>): List<AppInfoListModel>

    /**
     * 根据 参数 精确或者模糊搜索应用信息列表
     */
    suspend fun findAppInfoByParam(param: String): List<AppInfoListModel>

    /**
     * 更新应用信息
     */
    suspend fun updateAppInfo(appInfoListModel: AppInfoListModel): Boolean

    /**
     * 删除应用信息
     */
    suspend fun deleteAppInfo(packageName: String): Boolean

    /**
     * 删除指定版本的应用信息
     */
    suspend fun deleteAppInfo(packageName: String, versionCode: Int): Boolean

}

class DAOFacadeImpl : DAOFacade {

    private fun resultRowToAppInfo(row: ResultRow) = AppInfoListModel(
        name = row[AppInfoListTable.name],
        packageName = row[AppInfoListTable.packageName],
        latestAppInfoModel = json.decodeFromString(row[AppInfoListTable.latestAppInfoModel]),
        historyAppInfoModelList = json.decodeFromString(row[AppInfoListTable.historyAppInfoModelList]),
        createTime = row[AppInfoListTable.createTime],
        updateTime = row[AppInfoListTable.updateTime],
    )

    override suspend fun getAllAppInfo(): List<AppInfoListModel> = dbQuery {
        AppInfoListTable.selectAll().map { resultRowToAppInfo(it) }
    }

    override suspend fun getAppInfoByPage(page: Int, pageSize: Int): List<AppInfoListModel> = dbQuery {
        AppInfoListTable.selectAll().limit(pageSize, ((page - 1) * pageSize).toLong()).map { resultRowToAppInfo(it) }
    }

    override suspend fun getTotalPage(pageSize: Int): Int = dbQuery {
        // 获取每页数量的总页数
        AppInfoListTable.selectAll().count() / pageSize
    }.plus(1).toInt()

    override suspend fun findAppInfoByPackageName(packageName: String): AppInfoListModel? = dbQuery {
        AppInfoListTable.select { AppInfoListTable.packageName eq packageName }.map { resultRowToAppInfo(it) }
            .singleOrNull()
    }

    override suspend fun findAppInfoByPackageNameList(packageNameList: List<String>): List<AppInfoListModel> = dbQuery{
        AppInfoListTable.select { AppInfoListTable.packageName inList packageNameList }.map { resultRowToAppInfo(it) }
    }

    override suspend fun findAppInfoByParam(param: String): List<AppInfoListModel> = dbQuery {
        AppInfoListTable.select { AppInfoListTable.name like "%$param%" or (AppInfoListTable.packageName like "%$param%") }
            .map { resultRowToAppInfo(it) }
    }

    suspend fun findAppInfoByName(name: String): List<AppInfoListModel> = dbQuery {
        AppInfoListTable.select { AppInfoListTable.name like "%$name%" }.map { resultRowToAppInfo(it) }
    }

    suspend fun findAppInfoListByPackageName(packageName: String): List<AppInfoListModel> = dbQuery {
        AppInfoListTable.select { AppInfoListTable.packageName like "%$packageName%" }.map { resultRowToAppInfo(it) }
    }

    override suspend fun updateAppInfo(appInfoListModel: AppInfoListModel): Boolean = dbQuery {
        try {
            findAppInfoByPackageName(appInfoListModel.packageName)?.let {
                dataBaseUpdateAppInfo(appInfoListModel)
            } ?: dataBaseInsertAppInfo(appInfoListModel)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteAppInfo(packageName: String): Boolean = dbQuery {
        AppInfoListTable.deleteWhere { AppInfoListTable.packageName eq packageName } > 0
    }

    override suspend fun deleteAppInfo(packageName: String, versionCode: Int): Boolean = dbQuery {
        try {
            val appInfoListModel = findAppInfoByPackageName(packageName)
            appInfoListModel?.let {
                val historyAppInfoModelList = it.historyAppInfoModelList.toMutableList()
                historyAppInfoModelList.removeIf { appInfoModel -> appInfoModel.versionCode == versionCode }
                it.historyAppInfoModelList = historyAppInfoModelList
                dataBaseUpdateAppInfo(it)
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 数据库插入应用信息
     */
    private suspend fun dataBaseInsertAppInfo(appInfoListModel: AppInfoListModel): Boolean = dbQuery {
        try {
            AppInfoListTable.insert {
                it[name] = appInfoListModel.name
                it[packageName] = appInfoListModel.packageName
                it[latestAppInfoModel] = json.encodeToString(appInfoListModel.latestAppInfoModel)
                it[historyAppInfoModelList] = json.encodeToString(appInfoListModel.historyAppInfoModelList)
                it[createTime] = appInfoListModel.createTime
                it[updateTime] = appInfoListModel.updateTime
            }
            true
        } catch (e: Exception) {
            false
        }

    }

    /**
     * 数据库更新应用信息
     */
    private suspend fun dataBaseUpdateAppInfo(appInfoListModel: AppInfoListModel): Boolean = dbQuery {
        try {
            AppInfoListTable.update({ AppInfoListTable.packageName eq appInfoListModel.packageName }) {
                it[name] = appInfoListModel.name
                it[latestAppInfoModel] = json.encodeToString(appInfoListModel.latestAppInfoModel)
                it[historyAppInfoModelList] = json.encodeToString(appInfoListModel.historyAppInfoModelList)
                it[updateTime] = appInfoListModel.updateTime
            } > 0
        } catch (e: Exception) {
            false
        }
    }

}

val dao: DAOFacade = DAOFacadeImpl()