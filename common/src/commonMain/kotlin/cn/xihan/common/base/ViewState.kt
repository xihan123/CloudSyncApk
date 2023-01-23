package cn.xihan.common.base

/**
 * ViewState
 * @param loading 是否显示加载中
 * @param refreshing 是否显示刷新中
 * @param error 异常
 */
open class ViewState(
    open val loading: Boolean = false,
    open val refreshing: Boolean = false,
    open val error: Exception? = null,
)