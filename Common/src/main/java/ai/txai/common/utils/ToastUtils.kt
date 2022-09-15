package ai.txai.common.utils

import ai.txai.common.R
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.ThreadUtils
import java.lang.ref.WeakReference

object ToastUtils {
    private const val TAG = "ToastUtils"
    private var toast: WeakReference<Toast>? = null
    private var lastMessage = ""
    private var lastShowTime = 0L

    private fun createToast(message: String, isShowDrawable: Boolean): Toast {
        val context = AndroidUtils.getApplicationContext()
        val view = LayoutInflater.from(context).inflate(R.layout.commonview_toast_layout, null)
        val toastView = view.findViewById<TextView>(R.id.tv_message)
        if (!isShowDrawable) {
            toastView.setCompoundDrawables(null, null, null, null)
        }
        toastView.text = message
        val toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.view = view
        return toast
    }

    fun show(resId: Int, isShowDrawable: Boolean = false) {
        if (AndroidUtils.isAppForeground()) {
            showInternal(
                AndroidUtils.getApplicationContext().getString(resId),
                isShowDrawable
            )
        }
    }

    fun show(message: String, isShowDrawable: Boolean = false) {
        if (AndroidUtils.isAppForeground()) {
            showInternal(message, isShowDrawable)
        }
    }

    private fun showInternal(message: String, isShowDrawable: Boolean) {
        ThreadUtils.runOnUiThread {
            if (message == lastMessage &&
                System.currentTimeMillis() - lastShowTime < 1000) {
                return@runOnUiThread
            }

            toast?.get()?.cancel()
            val localToast = createToast(message, isShowDrawable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                localToast?.addCallback(
                    object : Toast.Callback() {
                        override fun onToastShown() {
                            super.onToastShown()
                            toast = WeakReference(localToast)
                        }

                        override fun onToastHidden() {
                            super.onToastHidden()
                            toast?.get()?.view = null
                            toast = null
                        }
                    })
            } else {
                toast = WeakReference(localToast)
            }
            localToast.show()
            lastMessage = message
            lastShowTime = System.currentTimeMillis()
        }
    }
}