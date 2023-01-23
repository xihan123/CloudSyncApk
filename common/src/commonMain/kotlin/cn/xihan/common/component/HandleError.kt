package cn.xihan.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.xihan.common.base.ViewState

@Composable
fun <VS : ViewState> HandleError(
    modifier: Modifier = Modifier,
    state: VS,
    onShowSnackBar: (message: String) -> Unit = {},
    onRetryClick: () -> Unit = {},
    content: @Composable (VS) -> Unit,
) {
    Box(modifier = modifier) {
        content(state)

        if (state.loading) {
            FullScreenLoading()
        }

        if (state.error != null) {

            FullScreenError(
                message = state.error?.message ?: "未知错误",
                onRetryClick = onRetryClick
            )

            LaunchedEffect(key1 = true) {
                onShowSnackBar.invoke(state.error?.message ?: "未知错误")
            }
        }
    }
}

@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.then(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                ),
        ), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.background(Color.Transparent),
            strokeWidth = 5.dp,
        )
    }
}

@Composable
fun FullScreenError(
    modifier: Modifier = Modifier,
    message: String,
    onRetryClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.then(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent),
        ), contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onRetryClick) {
            Text(text = message)
        }
    }
}
