package ai.txai.common.mvvm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.ImmersionBar;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import ai.txai.common.R;
import ai.txai.common.base.BaseActivity;
import ai.txai.common.base.BaseScrollActivity;
import ai.txai.common.base.ParameterField;
import ai.txai.common.data.ToastInfo;
import ai.txai.common.databinding.CommonFragmentScrollBinding;
import ai.txai.common.manager.LifeCycleManager;
import ai.txai.common.network.NetUtils;
import ai.txai.common.network.NetworkObserver;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.ToastUtils;
import ai.txai.common.widget.popupview.CustomLoadingPopupView;
import ai.txai.common.widget.popupview.UpdateVersionView;

public abstract class BaseMvvmActivity<V extends ViewBinding, VM extends BaseViewModel>
        extends BaseActivity implements IBaseMvvmView {

    protected V binding;
    protected VM viewModel;

    private CustomLoadingPopupView loadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //私有的初始化Databinding和ViewModel方法
        initViewDataBinding();
        //私有的ViewModel与View的契约事件回调逻辑
        registerUIChangeLiveDataCallBack();

        viewModel.onCreate(getIntent().getExtras());
        initViewObservable();

        setStatusBarFontStyle(true);

        NetworkUtils.registerNetworkStatusChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetUtils.INSTANCE.isInternetEnable()) {
            onDisconnected();
        }
    }

    /**
     * 注入绑定
     */
    private void initViewDataBinding() {
        binding = initViewBinding();
        setContentView(binding.getRoot());
        viewModel = initViewModel();
        if (viewModel == null) {
            Class modelClass = null;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
                for (Type argument : arguments) {
                    if (argument instanceof Class && BaseViewModel.class.isAssignableFrom((Class) argument)) {
                        modelClass = (Class) argument;
                        break;
                    }
                }
            }
            if (modelClass == null) {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) createViewModel(this, modelClass);
        }
        //让ViewModel拥有View的生命周期感应
        getLifecycle().addObserver(viewModel);
        //注入RxLifecycle生命周期
        viewModel.injectLifecycleProvider(this);
    }

    @Override
    public void onConnected(@Nullable NetworkUtils.NetworkType networkType) {
        onRefresh();
    }

    @Override
    public void onDisconnected() {
        hideLoading();
        super.onDisconnected();
    }

    @Override
    public void onRefresh() {

    }

    protected abstract V initViewBinding();

    //注册ViewModel与View的契约UI回调事件
    private void registerUIChangeLiveDataCallBack() {
        //跳入新页面
        viewModel.getUC().getJumpPage().observe(this, params -> {
            String path = (String) params.get(ParameterField.AROUTER_PATH);
            Bundle bundle = (Bundle) params.get(ParameterField.BUNDLE);
            Object flags = (params.get(ParameterField.FLAGS));
            if (flags == null) {
                ARouterUtils.navigation(this, path, bundle);
            } else {
                ARouterUtils.navigation(this, (int) flags, path, bundle);
            }
        });
        //关闭界面
        viewModel.getUC().getFinishEvent().observe(this, v -> finish());
        //关闭上一层
        viewModel.getUC().getOnBackPressedEvent().observe(this, v -> onBackPressed());

        viewModel.getUC().getShowToastEvent().observe(this, this::showToast);
        viewModel.getUC().getFinishAllActivities().observe(this, v ->
                LifeCycleManager.INSTANCE.finishAllActivities());

        viewModel.getUC().getShowLoading().observe(this, pair -> {
            Boolean second = pair.second;
            if (second == Boolean.TRUE) {
                showLoading(pair.first);
            } else {
                hideLoading();
            }
        });

        viewModel.getUC().getCheckUpdateVersionEvent().observe(this, updateVersionInfo -> {
            if (updateVersionInfo == null) return;

            final String version = "V" + updateVersionInfo.getNewVersion();
            final UpdateVersionView view = new UpdateVersionView(this);
            view.setOnUpgradeConfirmListener(() -> {
                final String url = updateVersionInfo.getDownLoadUrl();
                if (url.isEmpty()) return;

                final Intent intent = new Intent();
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            });
            view.showPop(version, updateVersionInfo.getUpdateBrief(),
                    UpdateVersionView.NORMAL_TYPE);
        });
    }

    public void setStatusBarFontStyle(boolean isDark) {
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarColor(android.R.color.transparent)
                .statusBarAlpha(0.0f)
                .statusBarDarkFont(isDark)
                .init();
    }

    public void showToast(ToastInfo info) {
        if (info == null) {
            return;
        }
        String content = info.getContent();
        boolean isShowDrawable = info.isShowDrawable();
        showToast(content, isShowDrawable);
    }

    public void showToast(String content, boolean isShowDrawable) {
        ToastUtils.INSTANCE.show(content, isShowDrawable);
        viewModel.getUC().getShowToastEvent().postValue(null);
    }

    public void showLoading(String text) {
        if (loadingView == null) {
            loadingView = new CustomLoadingPopupView(this);
        }
        loadingView.showPopup(text);
    }

    public void showLoading(String text, CustomLoadingPopupView.TimeoutListener listener) {
        showLoading(text);
        loadingView.setTimeoutListener(listener);
    }

    public void hideLoading() {
        if (loadingView != null) {
            loadingView.dismissPopup();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (AndroidUtils.INSTANCE.isShouldHideKeyboard(v, ev)) {
                KeyboardUtils.hideSoftInput(this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void initViewObservable() {

    }

    public VM getViewModel() {
        return viewModel;
    }

    /**
     * 初始化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    public VM initViewModel() {
        return null;
    }

    /**
     * 创建ViewModel
     *
     * @param cls
     * @param <T>
     * @return
     */
    public <T extends ViewModel> T createViewModel(BaseMvvmActivity activity, Class<T> cls) {
        return ViewModelProviders.of(activity).get(cls);
    }

    public void postDelay(Runnable runnable, long delay) {
        ThreadUtils.runOnUiThreadDelayed(() -> {
            if (!isFinishing()) {
                runnable.run();
            }
        }, delay);
    }

    @Override
    protected void onDestroy() {
        if (loadingView != null) {
            loadingView.onDestroy();
            loadingView = null;
        }
        NetworkUtils.unregisterNetworkStatusChangedListener(this);
        super.onDestroy();
    }

    public void post(Runnable runnable) {
        ThreadUtils.runOnUiThread(() -> {
            if (!isFinishing()) {
                runnable.run();
            }
        });
    }
}
