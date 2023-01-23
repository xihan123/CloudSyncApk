package cn.xihan.cloudsync

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import cn.xihan.cloudsync.base.BaseComposeActivity
import cn.xihan.common.component.MainPage
import cn.xihan.common.utils.M
import cn.xihan.common.utils.MyEvent
import com.drake.channel.receiveEvent
import com.drake.channel.receiveTag
import com.drake.channel.sendEvent
import com.dylanc.longan.lifecycleOwner
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog

class MainActivity : BaseComposeActivity() {

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // uri 转为 file
            val path = it.path?.removePrefix("/document/primary:")

            sendEvent("${Environment.getExternalStorageDirectory().path}/$path", "filePath")
        }
    }

    @Composable
    override fun ComposeContent() {
        MainPage(
            modifier = M.fillMaxSize()
                .systemBarsPadding()
        )


    }

    override fun init() {
        lifecycleOwner.receiveTag("openFile") {
            println("openFile: $it")
            getContent.launch("*/*")
        }

        lifecycleOwner.receiveEvent<String>("openUrl") {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(it)
            startActivity(intent)
        }

        lifecycleOwner.receiveEvent<MyEvent>("dialog") {
            TipDialog.show(
                it.message,
                if (it.type == 0) WaitDialog.TYPE.WARNING else if (it.type == 1) WaitDialog.TYPE.ERROR else WaitDialog.TYPE.SUCCESS
            )
        }

    }

}