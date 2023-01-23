package cn.xihan.cloudsync.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import cn.xihan.common.utils.settings
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import moe.tlaster.precompose.lifecycle.PreComposeActivity
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import moe.tlaster.precompose.ui.LocalLifecycleOwner
import moe.tlaster.precompose.ui.LocalViewModelStoreOwner

abstract class BaseComposeActivity : PreComposeActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        init()
        customCheckPermission()
        setContent {
            Mdc3Theme {
                val systemUiController = rememberSystemUiController()
                val darkIcons =
                    if (settings.getBoolean("followSystemTheme", false)) isSystemInDarkTheme() else settings.getBoolean(
                        "nightMode",
                        true
                    )

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !darkIcons
                    )
                }

                ComposeContent()
            }
        }
    }

    private fun customCheckPermission() {
        if (!XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)) {
            XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request { _, all ->
                    if (all) {
                        // 用户已经同意该权限
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        XXPermissions.startPermissionActivity(this, Permission.MANAGE_EXTERNAL_STORAGE)
                    }
                }

        }

        if (!XXPermissions.isGranted(this, Permission.PACKAGE_USAGE_STATS)) {
            XXPermissions.with(this)
                .permission(Permission.REQUEST_INSTALL_PACKAGES, Permission.PACKAGE_USAGE_STATS)
                .request { _, all ->
                    if (all) {
                        // 用户已经同意该权限
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        XXPermissions.startPermissionActivity(this, Permission.REQUEST_INSTALL_PACKAGES)
                    }
                }
        }

    }

    open fun init() {}

    @Composable
    abstract fun ComposeContent()
}

fun BaseComposeActivity.setContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit
) {
    val existingComposeView = window.decorView
        .findViewById<ViewGroup>(android.R.id.content)
        .getChildAt(0) as? ComposeView

    if (existingComposeView != null) with(existingComposeView) {
        setParentCompositionContext(parent)
        setContent {
            ContentInternal(content)
        }
    } else ComposeView(this).apply {
        // Set content and parent **before** setContentView
        // to have ComposeView create the composition on attach
        setParentCompositionContext(parent)
        setContent {
            ContentInternal(content)
        }
        // Set the view tree owners before setting the content view so that the inflation process
        // and attach listeners will see them already present
        setOwners()
        setContentView(this, DefaultActivityContentLayoutParams)
    }
}

private fun BaseComposeActivity.setOwners() {
    val decorView = window.decorView
    if (decorView.findViewTreeLifecycleOwner() == null) {
        decorView.setViewTreeLifecycleOwner(this)
    }
    if (decorView.findViewTreeSavedStateRegistryOwner() == null) {
        decorView.setViewTreeSavedStateRegistryOwner(this)
    }
}

@Composable
private fun BaseComposeActivity.ContentInternal(content: @Composable () -> Unit) {
    ProvideAndroidCompositionLocals {
        content.invoke()
    }
}

@Composable
private fun BaseComposeActivity.ProvideAndroidCompositionLocals(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLifecycleOwner provides this,
        LocalViewModelStoreOwner provides this,
        LocalBackDispatcherOwner provides this,
    ) {
        content.invoke()
    }
}

private val DefaultActivityContentLayoutParams = ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
)
