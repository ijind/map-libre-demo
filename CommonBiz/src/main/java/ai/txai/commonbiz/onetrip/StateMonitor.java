package ai.txai.commonbiz.onetrip;

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
public class StateMonitor {
    private static StateMonitor sInstance = new StateMonitor();
    private HandlerThread mUIThread = new HandlerThread("one-trip-monitor");
    private Handler mIoHandler;
    private static final long TIME_BLOCK = 500;
    private static final int ANR_TIME_BLOCK = 3000;
    private Looper looper;

    private static List<UIMonitorListener> listeners = new ArrayList<>();

    public interface UIMonitorListener {
        void onMonitor(String msg);

        void onAnr(String msg);
    }

    public void regListener(UIMonitorListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void unRegListener(UIMonitorListener listener) {
        listeners.remove(listener);
    }

    private StateMonitor() {
        mUIThread.start();
        mIoHandler = new Handler(mUIThread.getLooper());
    }

    private Runnable mLogRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = looper.getThread().getStackTrace();
            for (StackTraceElement s : stackTrace) {
                sb.append(s.toString() + "\n");
            }
            String log = "StateMonitor:" + sb.toString();
            Log.e("StateMonitor", log);
            for (UIMonitorListener listener : listeners) {
                listener.onMonitor(log);
            }
        }
    };

    private Runnable mAnrRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = looper.getThread().getStackTrace();
            for (StackTraceElement s : stackTrace) {
                sb.append(s.toString() + "\n");
            }
            String log = "ANR:" + sb.toString();
            Log.e("StateMonitor", log);
            for (UIMonitorListener listener : listeners) {
                listener.onAnr(log);
            }
        }
    };

    public static StateMonitor getInstance() {
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


    public void init(Looper looper) {
        this.looper = looper;
        looper.setMessageLogging(new Printer() {
            @Override
            public void println(String x) {
                if (x.startsWith(">>>>> Dispatching")) {
                    StateMonitor.getInstance().startMonitor();
                    StateMonitor.getInstance().startAnrMonitor();
                }
                if (x.startsWith("<<<<< Finished")) {
                    StateMonitor.getInstance().removeMonitor();
                    StateMonitor.getInstance().removeAnrMonitor();
                }
            }
        });
    }

}
