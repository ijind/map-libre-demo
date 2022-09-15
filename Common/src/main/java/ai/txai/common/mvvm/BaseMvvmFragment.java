package ai.txai.common.mvvm;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import ai.txai.common.R;
import ai.txai.common.base.BaseActivity;
import ai.txai.common.base.BaseFragment;
import ai.txai.common.base.ParameterField;
import ai.txai.common.data.ToastInfo;
import ai.txai.common.databinding.CommonFragmentContentListBinding;
import ai.txai.common.databinding.CommonFragmentScrollBinding;
import ai.txai.common.manager.LifeCycleManager;
import ai.txai.common.network.NetUtils;
import ai.txai.common.network.NetworkObserver;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.ToastUtils;
import ai.txai.common.widget.popupview.CommonLoadingStatusView;
import kotlin.Unit;


public abstract class BaseMvvmFragment<V extends ViewBinding, VM extends BaseViewModel> extends
        BaseFragment implements IBaseMvvmView {

    protected V binding;
    protected VM viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = initViewBinding(inflater, container);
        //私有的初始化Databinding和ViewModel方法
        initViewDataBinding();
        NetworkUtils.registerNetworkStatusChangedListener(this);
        initNetStatus();
        return binding.getRoot();
    }

    protected abstract V initViewBinding(@NonNull LayoutInflater inflater, ViewGroup container);

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //私有的ViewModel与View的契约事件回调逻辑
        registerUIChangeLiveDataCallBack();
        viewModel.onCreate(getArguments());
        //页面事件监听的方法，一般用于ViewModel层转到View层的事件注册
        initViewObservable();
    }

    @Override
    public void onStart() {
        super.onStart();
        NetUtils.INSTANCE.setShowToastWhenNoInternet(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!NetUtils.INSTANCE.isInternetEnable()) {
            onDisconnected();
        }
    }

    private void initNetStatus() {
        if (binding instanceof CommonFragmentScrollBinding) {
            ((CommonFragmentScrollBinding) binding).statusView.setLoadingHandler(this::onRefresh);
        }
    }

    @Override
    public void onConnected(@Nullable NetworkUtils.NetworkType networkType) {
        if (binding instanceof CommonFragmentScrollBinding) {
            final CommonFragmentScrollBinding scrollBinding = ((CommonFragmentScrollBinding) binding);
            scrollBinding.statusView.setVisibility(View.GONE);
            scrollBinding.statusView.loadSuccess();
            scrollBinding.toolbarTitleTv.setVisibility(View.GONE);
        } else if (binding instanceof CommonFragmentContentListBinding) {
            final CommonFragmentContentListBinding scrollBinding = ((CommonFragmentContentListBinding) binding);
            scrollBinding.statusView.setVisibility(View.GONE);
            scrollBinding.statusView.loadSuccess();
            scrollBinding.toolbarTitleTv.setVisibility(View.GONE);
        }
        onRefresh();
    }

    @Override
    public void onDisconnected() {
        hideLoading();
        if (binding instanceof CommonFragmentScrollBinding) {
            final CommonFragmentScrollBinding scrollBinding = ((CommonFragmentScrollBinding) binding);
            scrollBinding.statusView.setVisibility(View.VISIBLE);
            scrollBinding.statusView.loadError();
            scrollBinding.toolbarTitleTv.setVisibility(View.VISIBLE);
            scrollBinding.toolbarTitleTv.setTextColor(Color.parseColor("#040818"));
        } else if (binding instanceof CommonFragmentContentListBinding) {
            final CommonFragmentContentListBinding scrollBinding = ((CommonFragmentContentListBinding) binding);
            scrollBinding.statusView.setVisibility(View.VISIBLE);
            scrollBinding.statusView.loadError();
            scrollBinding.toolbarTitleTv.setVisibility(View.VISIBLE);
            ThreadUtils.runOnUiThreadDelayed(() -> {
                scrollBinding.toolbarTitleTv.setTextColor(Color.parseColor("#040818"));
            }, 500);
        } else {
            final String message = getResources().getString(R.string.commonview_no_internet_prompt);
            ToastUtils.INSTANCE.show(message, false);
        }
    }

    @Override
    public void onRefresh() {

    }

    /**
     * 注入绑定
     */
    private void initViewDataBinding() {
        viewModel = initViewModel();
        if (viewModel == null) {
            Class modelClass = null;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
                for (Type argument : arguments) {
                    if (BaseViewModel.class.isAssignableFrom((Class) argument)) {
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

    /**
     * =====================================================================
     **/
    //注册ViewModel与View的契约UI回调事件
    private void registerUIChangeLiveDataCallBack() {
        //跳入新页面
        viewModel.getUC().getJumpPage().observe(this, params -> {
            String path = (String) params.get(ParameterField.AROUTER_PATH);
            Bundle bundle = (Bundle) params.get(ParameterField.BUNDLE);
            Object flags = (params.get(ParameterField.FLAGS));
            if (flags == null) {
                ARouterUtils.navigation(getActivity(), path, bundle);
            } else {
                ARouterUtils.navigation(getActivity(), (int) flags, path, bundle);
            }
        });
        //关闭界面
        viewModel.getUC().getFinishEvent().observe(this, v -> getActivity().finish());
        //关闭上一层
        viewModel.getUC().getOnBackPressedEvent().observe(this, v -> getActivity().onBackPressed());

        viewModel.getUC().getFinishAllActivities().observe(this, v ->
                LifeCycleManager.INSTANCE.finishAllActivities());

        viewModel.getUC().getShowToastEvent().observe(this, this::showToast);
        viewModel.getUC().getShowLoading().observe(this, pair -> {
            Boolean second = pair.second;
            if (second == Boolean.TRUE) {
                showLoading(pair.first);
            } else {
                hideLoading();
            }
        });
    }

    public void hideLoading() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseMvvmActivity) {
            ((BaseMvvmActivity) activity).hideLoading();
        }
    }

    public void showLoading(String text) {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseMvvmActivity) {
            ((BaseMvvmActivity) activity).showLoading(text);
        }
    }

    private void showToast(ToastInfo info) {
        if (info == null) {
            return;
        }
        String content = info.getContent();
        boolean isShowDrawable = info.isShowDrawable();
        showToast(content, isShowDrawable);
    }

    public void showToast(String content, boolean isShowDrawable) {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseMvvmActivity) {
            ((BaseMvvmActivity) activity).showToast(content, isShowDrawable);
        }
        viewModel.getUC().getShowToastEvent().postValue(null);
    }

    /**
     * 初始化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    public VM initViewModel() {
        return null;
    }

    @Override
    public void initViewObservable() {

    }

    /**
     * 创建ViewModel
     *
     * @param cls
     * @param <T>
     * @return
     */
    public <T extends ViewModel> T createViewModel(BaseMvvmFragment fragment, Class<T> cls) {
        return ViewModelProviders.of(fragment.requireActivity()).get(cls);
    }

    @Override
    public void onStop() {
        super.onStop();
        NetUtils.INSTANCE.setShowToastWhenNoInternet(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NetworkUtils.unregisterNetworkStatusChangedListener(this);
    }
}
