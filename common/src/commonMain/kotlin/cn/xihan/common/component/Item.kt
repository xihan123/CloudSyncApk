@file:OptIn(ExperimentalMaterial3Api::class)

package cn.xihan.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.xihan.common.Image
import cn.xihan.common.models.AppInfoListModel
import cn.xihan.common.models.AppInfoModel
import cn.xihan.common.openUrl
import cn.xihan.common.utils.M
import cn.xihan.common.utils.downloadUrl
import cn.xihan.common.utils.iconUrl
import cn.xihan.common.utils.size
import com.lt.compose_views.other.VerticalSpace
import com.lt.compose_views.util.rememberMutableStateOf

/**
 * 错误模型
 */
@Composable
fun ErrorItem(
    errorMessage: String = "未知错误",
) {
    Text(
        text = errorMessage,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}

/**
 * 应用信息模型
 */
@Composable
fun AppInfoItem(
    uploadAppInfoModel: AppInfoModel,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            modifier = M.padding(16.dp)
        ) {
            if (uploadAppInfoModel.icon.isBlank()) {
                Spacer(modifier = Modifier.size(50.dp))
            } else {
                Image(
                    url = uploadAppInfoModel.icon.iconUrl,
                    modifier = Modifier.size(50.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {

                Text(
                    text = uploadAppInfoModel.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                VerticalSpace(5.dp)

                Text(
                    text = uploadAppInfoModel.versionName
                )


            }
        }

        HorizontalItem("包名:", uploadAppInfoModel.packageName)

        HorizontalItem("版本号:", uploadAppInfoModel.versionCode.toString())

        HorizontalItem("最低支持版本:", uploadAppInfoModel.minSdkVersion.toString())

        HorizontalItem("目标版本:", uploadAppInfoModel.targetSdkVersion.toString())

        HorizontalItem("下载地址:", uploadAppInfoModel.apkUrl.downloadUrl)

        HorizontalItem("安装包大小:", uploadAppInfoModel.apkSize.size)

        HorizontalItem("MD5:", uploadAppInfoModel.apkMd5)

    }
}

/**
 * 横向模型
 */
@Composable
fun HorizontalItem(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.width(18.dp))
        SelectionContainer {
            Text(
                text = content
            )
        }

    }
}

/**
 * 搜索标题栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchByTextAppBar(
    modifier: Modifier = Modifier,
    text: String = "",
    onTextChange: (String) -> Unit = {},
    onClickSearch: () -> Unit = {}
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 10.dp),
        value = text,
        onValueChange = onTextChange,
        singleLine = true,
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            Row {
                if (text.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onTextChange("")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                }

                IconButton(onClick = onClickSearch) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                }
            }

        }
    )

}

/**
 * 查询结果模型
 */
@ExperimentalMaterial3Api
@Composable
fun QueryResultItem(
    appInfoListModel: AppInfoListModel,
    modifier: Modifier = Modifier
) {
    // 展开状态
    var expanded by rememberMutableStateOf(false)
    val historyList = appInfoListModel.historyAppInfoModelList
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column {
            Row(
                modifier = M.padding(16.dp)
            ) {
                if (appInfoListModel.latestAppInfoModel?.icon.isNullOrBlank()) {
                    Spacer(modifier = Modifier.size(50.dp))
                } else {
                    Image(
                        url = appInfoListModel.latestAppInfoModel!!.icon.iconUrl,
                        modifier = Modifier.size(50.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = M.weight(1f)
                ) {

                    Text(
                        text = appInfoListModel.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    VerticalSpace(5.dp)

                    Text(
                        text = appInfoListModel.latestAppInfoModel!!.versionName
                    )
                }

                Button(
                    onClick = {
                        openUrl(appInfoListModel.latestAppInfoModel!!.apkUrl.downloadUrl)
                    }
                ) {
                    Text(text = "下载")
                }

            }

            if (historyList.isNotEmpty()) {
                Row(
                    modifier = M.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "历史版本",
                        modifier = M.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            expanded = !expanded
                        }
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }

                if (expanded) {
                    LazyColumn(
                        modifier = M.height((historyList.size * 100).dp)
                    ) {
                        items(historyList.size) { index ->
                            HistoryVersionItem(
                                appInfoModel = historyList[index],

                                )
                        }
                    }
                }
            }


        }
    }

}

/**
 * 历史版本模型
 */
@Composable
fun HistoryVersionItem(
    appInfoModel: AppInfoModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = M.padding(16.dp)
    ) {
        if (appInfoModel.icon.isBlank()) {
            Spacer(modifier = Modifier.size(50.dp))
        } else {
            Image(
                url = appInfoModel.icon.iconUrl,
                modifier = Modifier.size(50.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = M.weight(1f)
        ) {

            Text(
                text = appInfoModel.versionName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            VerticalSpace(5.dp)

            Text(
                text = appInfoModel.versionCode.toString()
            )
        }

        Button(
            onClick = {
                openUrl(appInfoModel.apkUrl.downloadUrl)
            }
        ) {
            Text(text = "下载")
        }

    }
}


