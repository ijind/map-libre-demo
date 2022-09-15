package ai.txai.common.log;

import android.content.Context;

import com.elvishew.xlog.LogUtils;
import com.elvishew.xlog.XLog;

import java.io.IOException;

/**
 * Time: 02/03/2022
 * Author Hay
 * <p>
 * 后续写入文件扩展此类
 */
public class LOG {

    public static void d(String tag, String msg, Object... args) {
        try {
            XLog.tag(tag).d(msg, args);
        } catch (Exception exception) {
            LOG.e(tag, "LOG error %s", exception);
        }
    }

    public static void i(String msg, Object... args) {
        try {
            XLog.i(msg, args);
        } catch (Exception exception) {
            LOG.e("LOG", "LOG error %s", exception);
        }
    }

    public static void i(String tag, String msg, Object... args) {
        try {
            XLog.tag(tag).i(msg, args);
        } catch (Exception exception) {
            LOG.e(tag, "LOG error %s", exception);
        }
    }

    public static void w(String tag, String msg, Object... args) {
        XLog.tag(tag).w(msg, args);
    }

    public static void e(String tag, String msg, Object... args) {
        XLog.tag(tag).e(msg, args);
    }

    public static void e(Throwable t, String msg, Object... args) {
        XLog.e(msg, t);
    }

    public static void json(String json) {
        XLog.json(json);
    }

    public static String compress(Context context) throws IOException {
        final String folderPath = context.getExternalFilesDir("XLog").getPath();
        final String zipPath = folderPath + "Zip.zip";
        LogUtils.compress(folderPath, zipPath);
        return zipPath;
    }
}
