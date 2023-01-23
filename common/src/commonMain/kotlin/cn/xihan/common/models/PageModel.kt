package cn.xihan.common.models

import kotlinx.serialization.Serializable

/**
 * 分页模型
 */
@Serializable
data class PageModel<T>(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<T>
)