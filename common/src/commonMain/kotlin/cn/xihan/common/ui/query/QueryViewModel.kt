package cn.xihan.common.ui.query

import cn.xihan.common.MyApplication
import cn.xihan.common.base.BaseViewModel
import cn.xihan.common.base.ViewState
import cn.xihan.common.models.AppInfoListModel
import cn.xihan.common.models.PageModel
import cn.xihan.common.utils.kJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.decodeFromString

class QueryViewModel : BaseViewModel() {

    private val _state = MutableStateFlow(CurrentQueryViewState())
    val state: StateFlow<CurrentQueryViewState> = _state

    /**
     * 分页获取应用信息列表
     */
    fun getAppInfoList(page: Int = 1, pageSize: Int = 20) {
        if (page == 1) {
            showLoading()
        }
        callApi {
            MyApplication().getAppInfoList(page = page, pageSize = pageSize).collect { response ->
                when {
                    response.isNotBlank() && "{" in response -> {
                        val pageModel = kJson.decodeFromString<PageModel<AppInfoListModel>>(response)
                        _state.update {
                            val list = pageModel.list
                            if (pageModel.page == 1) {
                                it.currentQueryAppInfoListModel = list
                            } else {
                                it.currentQueryAppInfoListModel += list
                            }
                            it.copy(
                                loading = false,
                                refreshing = false,
                                error = null,
                                errorMessage = null,
                                pageModel = pageModel,
                                page = pageModel.page,
                                total = pageModel.total
                            )
                        }
                    }

                    "{" !in response -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                refreshing = false,
                                error = null,
                                pageModel = null,
                                errorMessage = response,
                                page = 0,
                                total = 1
                            )
                        }
                    }

                }


            }
        }
    }

    /**
     * 根据 参数 获取应用信息列表
     */
    fun getAppInfoListByParams(param: String) {
        if (param.isBlank()) {
            getAppInfoList()
            return
        }
        callApi {
            MyApplication().getAppInfoListByParams(param = param).collect { list ->
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = null,
                        currentQueryAppInfoListModel = list
                    )
                }
            }
        }
    }

    fun onRefreshCurrentMain(showRefresh: Boolean = true) = ioLaunch {
        _state.update {
            it.copy(
                refreshing = showRefresh,
                loading = !showRefresh
            )
        }
        getAppInfoList()
    }

    override fun hideLoading() {
        _state.update {
            it.copy(
                loading = false, refreshing = false
            )
        }
    }

    private fun showLoading() {
        _state.update {
            it.copy(loading = true, refreshing = true)
        }
    }

    init {
        getAppInfoList()
    }


}

data class CurrentQueryViewState(
    override val loading: Boolean = false,
    override val refreshing: Boolean = false,
    override val error: Exception? = null,
    val errorMessage: String? = null,
    var searchQuery: String = "",
    var currentQueryAppInfoListModel: List<AppInfoListModel> = emptyList(),
    val pageModel: PageModel<AppInfoListModel>? = null,
    var page: Int = 0,
    var total: Int = 1
) : ViewState(loading, refreshing, error)