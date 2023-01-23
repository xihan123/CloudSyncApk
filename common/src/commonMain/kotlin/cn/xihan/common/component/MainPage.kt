@file:OptIn(ExperimentalMaterial3Api::class)

package cn.xihan.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xihan.common.UpdateScreen
import cn.xihan.common.platformName
import cn.xihan.common.ui.query.QueryScreen
import cn.xihan.common.ui.upload.UploadScreen
import cn.xihan.common.utils.M
import com.lt.compose_views.util.rememberMutableStateOf
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    modifier: Modifier = Modifier,
) {
    val items = mutableListOf(
        MainScreen.Upload,
        MainScreen.Query
    )
    if (platformName == "Android") {
        items += MainScreen.Update
    }
    val navigator = rememberNavigator()
    val currentScreen = rememberMutableStateOf(0)
    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier,
        bottomBar = {
            NiaNavigationBar(
                modifier = M
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                items.forEachIndexed { index, screen ->
                    NiaNavigationRailItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                                1 -> Icon(Icons.Filled.Search, contentDescription = null)
                                2 -> Icon(Icons.Filled.Star, contentDescription = null)
                            }
                        },
                        label = { Text(screen.title) },
                        selected = currentScreen.value == index,
                        onClick = {
                            currentScreen.value = index
                            navigator.navigate(
                                screen.route,
                                NavOptions(
                                    launchSingleTop = true,
                                    popUpTo = PopUpTo(
                                        route = MainScreen.Update.route,
                                        inclusive = true,
                                    )
                                ),
                            )
                        },
                        modifier = M.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navigator = navigator,
            navTransition = NavTransition(),
            initialRoute = MainScreen.Upload.route,
            modifier = M.padding(paddingValues)
        ) {
            scene(
                route = MainScreen.Upload.route,
                navTransition = NavTransition(),
            ) {
                UploadScreen()
            }

            scene(
                route = MainScreen.Query.route,
                navTransition = NavTransition(),
            ) {
                QueryScreen()
            }

            scene(
                route = MainScreen.Update.route,
                navTransition = NavTransition(),
            ) {
                UpdateScreen()
            }

        }

    }

}


private sealed class MainScreen(val route: String, val title: String) {

    /**
     * 上传
     */
    object Upload : MainScreen("upload", "上传")

    /**
     * 查询
     */
    object Query : MainScreen("query", "查询")

    /**
     * 检查更新
     */
    object Update : MainScreen("update", "更新")


}