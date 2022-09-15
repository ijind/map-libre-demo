package ai.txai.common.utils

import ai.txai.common.BuildConfig
import ai.txai.common.log.LOG

/**
 * use ai.txai.common.log.LOG
 */
@Deprecated("")
object LogUtils {

    private fun isDebuggable() = BuildConfig.DEBUG

    fun e(tag: String, msg: String) {
        if (!isDebuggable()) return
        LOG.e("%s:%s", tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (!isDebuggable()) return
        LOG.w("%s:%s", tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (!isDebuggable()) return
        LOG.d("%s:%s", tag, msg)
    }

    fun i(tag: String, msg: String) {
        if (!isDebuggable()) return
        LOG.i("%s:%s", tag, msg)
    }
}