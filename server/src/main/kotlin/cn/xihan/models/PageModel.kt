package cn.xihan.models

import kotlinx.serialization.Serializable

/**
 * 分页模型
 * @param page 页码
 * @param pageSize 每页数量
 * @param total 总数
 * @param list 数据列表
 */
@Serializable
data class PageModel<T>(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<T>
)