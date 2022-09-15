package ai.txai.common.log

import ai.txai.common.utils.AndroidUtils
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler: Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        LOG.e("Crash!!", "${thread.name}\nThrowable ${printStackTraceInfo(throwable)}")
//        AndroidUtils.delayOperation(2000) {
//            android.os.Process.killProcess(android.os.Process.myPid())
//            exitProcess(1)
//        }
    }

    private fun printStackTraceInfo(throwable: Throwable): String {
        var pw: PrintWriter? = null
        val writer = StringWriter()
        try {
            pw = PrintWriter(writer)
            throwable.printStackTrace(pw)
        } catch (e: Exception) {
            return ""
        } finally {
            pw?.close()
        }

        return writer.toString()
    }
}