package cn.xihan.common.ui.query

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import cn.xihan.common.component.ErrorItem
import cn.xihan.common.component.QueryResultItem
import cn.xihan.common.component.SearchByTextAppBar
import cn.xihan.common.component.VsScaffold
import cn.xihan.common.utils.M
import com.lt.compose_views.util.rememberMutableStateOf
import kotlinx.coroutines.launch
import moe.tlaster.precompose.ui.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryScreen(
    viewModel: QueryViewModel = viewModel(QueryViewModel::class) { QueryViewModel() },
) {

    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    var text by rememberMutableStateOf(value = "")
    VsScaffold(
        state = state,
        topBar = {
            QueryAppBar(
                isTop = isTop,
                onRetryClick = {
                    viewModel.onRefreshCurrentMain()
                },
                onSlideClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(if (isTop) 100 else 0)
                    }
                }
            )
        }
    ) { _, _ ->
        LazyColumn(
            modifier = M.fillMaxSize(),
            state = listState,
        ) {

            item {
                SearchByTextAppBar(
                    text = text,
                    onTextChange = {
                        text = it
                        viewModel.getAppInfoListByParams(it)
                    },
                    onClickSearch = { viewModel.getAppInfoListByParams(text) },
                )
            }

            when {
                state.error != null -> {
                    item {
                        ErrorItem(state.error?.message ?: "未知错误")
                    }
                }

                !state.errorMessage.isNullOrBlank() -> {
                    item {
                        ErrorItem("${state.errorMessage}")
                    }
                }

                state.currentQueryAppInfoListModel.isNotEmpty() && state.pageModel != null -> {

                    items(state.currentQueryAppInfoListModel.size) { index ->
                        QueryResultItem(
                            appInfoListModel = state.currentQueryAppInfoListModel[index],
                            modifier = M.fillMaxWidth(),
                        )
                    }

                    item {
                        LaunchedEffect(Unit) {
                            if (state.pageModel!!.page < state.pageModel!!.total) {
                                viewModel.getAppInfoList(page = state.page + 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryAppBar(
    isTop: Boolean = true,
    onRetryClick: () -> Unit = {},
    onSlideClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "云端应用")
        },
        modifier = M
            .fillMaxWidth(),
        actions = {
            IconButton(onClick = onRetryClick) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null
                )
            }
            IconButton(onClick = onSlideClick) {
                if (isTop) {
                    Icon(Icons.Filled.KeyboardArrowDown, null)
                } else {
                    Icon(Icons.Filled.KeyboardArrowUp, null)
                }
            }
        },
        navigationIcon = {},
        scrollBehavior = null
    )

}