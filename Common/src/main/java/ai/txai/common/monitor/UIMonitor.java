package ai.txai.common.monitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Printer;

import java.util.ArrayList;
import java.util.List;

/**
 * Time: 2020-03-18
 * Author Hay
 */
public class UIMonitor {
    private static UIMonitor sInstance = new UIMonitor();
    private HandlerThread mUIThread = new HandlerThread("ui-monitor");
    private Handler mIoHandler;
    private static final long TIME_BLOCK = 500;
    private static final int ANR_TIME_BLOCK = 3000;

    private static List<UIMonitorListener> listeners = new ArrayList<>();

    public interface UIMonitorListener {
        public void onMonitor(String msg);
        public void onAnr(String msg);
    }

    public void regListener(UIMonitorListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void unRegListener(UIMonitorListener listener) {
        listeners.remove(listener);
    }

    private UIMonitor() {
        mUIThread.start();
        mIoHandler = new Handler(mUIThread.getLooper());
    }

    private static Runnable mLogRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
            for (StackTraceElement s : stackTrace) {
                sb.append(s.toString() + "\n");
            }
            String log = "UIMonitor:" + sb.toString();
            Log.e("UIMonitor", log);
            for(UIMonitorListener listener:listeners){
                listener.onMonitor(log);
            }
        }
    };

    private static Runnable mAnrRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
            for (StackTraceElement s : stackTrace) {
                sb.append(s.toString() + "\n");
            }
            String log = "ANR:" + sb.toString();
            Log.e("UIMonitor", log);
            for(UIMonitorListener listener:listeners){
                listener.onAnr(log);
            }
        }
    };

    public static UIMonitor getInstance() {
        return sInstance;
    }

    public void startMonitor() {
        mIoHandler.postDelayed(mLogRunnable, TIME_BLOCK);
    }

    public void removeMonitor() {
        mIoHandler.removeCallbacks(mLogRunnable);
    }


    public void startAnrMonitor() {
        mIoHandler.postDelayed(mAnrRunnable, ANR_TIME_BLOCK);
    }

    public void removeAnrMonitor() {
        mIoHandler.removeCallbacks(mAnrRunnable);
    }


    public void init() {
        Looper.getMainLooper().setMessageLogging(new Printer() {
            @Override
            public void println(String x) {
                if (x.startsWith(">>>>> Dispatching")) {
                    UIMonitor.getInstance().startMonitor();
                    UIMonitor.getInstance().startAnrMonitor();
                }
                if (x.startsWith("<<<<< Finished")) {
                    UIMonitor.getInstance().removeMonitor();
                    UIMonitor.getInstance().removeAnrMonitor();
                }
            }
        });
    }

}
