package com.gomap.robotaxi;

import androidx.multidex.MultiDexApplication;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.NetworkUtils;
import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.gomap.robotaxi.login.LoginObserve;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;

import ai.txai.common.external.LoginManager;
import ai.txai.common.log.CrashHandler;
import ai.txai.common.log.CustomLogFileNameGenerator;
import ai.txai.common.monitor.UIMonitor;
import ai.txai.common.scheme.SchemeDispatchManager;
import ai.txai.common.thread.TScheduler;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.DeviceUtils;
import ai.txai.database.GreenDaoHelper;
import ai.txai.database.listener.DbListener;
import ai.txai.database.user.User;
import ai.txai.payment.PaymentSchemeDispatcher;

/**
 * Time: 2/21/22
 * Author Hay
 */
public class BaseApplication extends MultiDexApplication {

    private boolean debug = BuildConfig.DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();
        //主线程耗时监控
        UIMonitor.getInstance().init();
        // Save crash info
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
        //ARouter
        if (debug) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        initXLog();
        ARouter.init(this);
        AndroidUtils.INSTANCE.init(this);
        DeviceUtils.INSTANCE.initDeviceId(this);

        //登录成功的操作，初始化其他模块
        LoginObserve listener = new LoginObserve();
        LoginManager.getInstance().registerLoginListener(listener);
        NetworkUtils.registerNetworkStatusChangedListener(listener);
        //GreenDao
        TScheduler.INSTANCE.single().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                GreenDaoHelper.getInstance().init(BaseApplication.this, debug, DeviceUtils.INSTANCE.getDeviceId(), new DbListener() {
                    @Override
                    public void userDBInit(User user) {
                        LoginManager.getInstance().whenLogin(user, true);
                    }
                });
            }
        });

        //for deepLink
        SchemeDispatchManager.getInstance().register(new PaymentSchemeDispatcher());
        customSmartRefreshLayout();
    }

    private void customSmartRefreshLayout() {
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.header_pulling);//"下拉可以刷新";
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.header_refreshing);//"正在刷新...";
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.header_loading);//"正在加载...";
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.header_release);//"释放立即刷新";
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.header_finish);//"刷新完成";
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.header_failed);//"刷新失败";
        ClassicsHeader.REFRESH_HEADER_UPDATE = getString(R.string.header_update);//"上次更新 M-d HH:mm";
        ClassicsHeader.REFRESH_HEADER_UPDATE = getString(R.string.header_update);//"'Last update' M-d HH:mm";
        ClassicsHeader.REFRESH_HEADER_SECONDARY = getString(R.string.header_secondary);//"释放进入二楼"

        ClassicsFooter.REFRESH_FOOTER_PULLING = getString(R.string.footer_pulling);//"上拉加载更多";
        ClassicsFooter.REFRESH_FOOTER_RELEASE = getString(R.string.footer_release);//"释放立即加载";
        ClassicsFooter.REFRESH_FOOTER_LOADING = getString(R.string.footer_loading);//"正在刷新...";
        ClassicsFooter.REFRESH_FOOTER_REFRESHING = getString(R.string.footer_refreshing);//"正在加载...";
        ClassicsFooter.REFRESH_FOOTER_FINISH = getString(R.string.footer_finish);//"加载完成";
        ClassicsFooter.REFRESH_FOOTER_FAILED = getString(R.string.footer_failed);//"加载失败";
        ClassicsFooter.REFRESH_FOOTER_NOTHING = getString(R.string.footer_nothing);//"全部加载完成";
    }

//    private void initXLog() {
//        System.loadLibrary("c++_shared");
//        System.loadLibrary("marsxlog");
//
//        final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
//        final String logPath = SDCARD + "/marssample/log";
//
//        final String cachePath = this.getExternalFilesDir("XLog") + "/xlog";
//        LOG.d("ApplicationTest", "log path ppp %s cache path %s", logPath, cachePath);
//
//        Xlog xlog = new Xlog();
//        Log.setLogImp(xlog);
//
//        if (BuildConfig.DEBUG) {
//            Log.setConsoleLogOpen(true);
//            Log.appenderOpen(Xlog.LEVEL_ALL, Xlog.AppednerModeAsync, cachePath, logPath, "TXAI", 0);
//        } else {
//            Log.setConsoleLogOpen(false);
//            Log.appenderOpen(Xlog.LEVEL_INFO, Xlog.AppednerModeAsync, cachePath, logPath, "", 0);
//        }
//    }

    private void initXLog() {
        final String cachePath = this.getExternalFilesDir("XLog").getPath();
        LogConfiguration config = new LogConfiguration.Builder()
                .tag("TXAI")
                .enableThreadInfo()
                .enableStackTrace(2)
                .enableBorder()
                .logLevel(LogLevel.ALL).build();
        Printer androidPrinter = new AndroidPrinter();
        Printer systemPrinter = new ConsolePrinter();
        Printer filePrinter = new FilePrinter
                .Builder(cachePath)
                .flattener(new PatternFlattener("{d yyyy-MM-dd HH:mm:ss.SSS} {l}/{t}: {m}")) //log 写入格式
                .cleanStrategy(new FileLastModifiedCleanStrategy(7 * 24 * 60 * 60 * 1000)) // 三天后清除
                .fileNameGenerator(new CustomLogFileNameGenerator()) //按日期定义文件名
                .build();

        XLog.init(config, androidPrinter, filePrinter);
    }
}
