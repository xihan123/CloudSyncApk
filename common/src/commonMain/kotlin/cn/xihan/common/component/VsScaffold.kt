@file:OptIn(ExperimentalMaterial3Api::class)

package cn.xihan.common.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import cn.xihan.common.base.ViewState
import cn.xihan.common.utils.M

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <VS : ViewState> VsScaffold(
    modifier: Modifier = Modifier,
    state: VS,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    onShowSnackBar: (message: String) -> Unit = {},
    onRetryClick: () -> Unit = {},
    content: @Composable (PaddingValues, VS) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
    ) { paddingValues ->
        HandleError(
            modifier = M
                .fillMaxSize()
                .padding(paddingValues),
            state = state,
            onShowSnackBar = onShowSnackBar,
            onRetryClick = onRetryClick,
        ) { viewState ->
            content.invoke(paddingValues, viewState)
        }
    }
}