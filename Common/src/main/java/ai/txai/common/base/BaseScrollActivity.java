package ai.txai.common.base;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SizeUtils;

import ai.txai.common.base.helper.ScrollBindingHelper;
import ai.txai.common.databinding.CommonFragmentScrollBinding;
import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseMvvmActivity;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.network.NetUtils;
import ai.txai.common.utils.ToastUtils;
import ai.txai.commonview.observablescrollview.ObservableScrollViewCallbacks;
import ai.txai.commonview.observablescrollview.ScrollState;
import ai.txai.commonview.observablescrollview.ScrollUtils;

public abstract class BaseScrollActivity<VB extends ViewBinding, VM extends BaseViewModel> extends BaseMvvmActivity<CommonFragmentScrollBinding, VM> implements ObservableScrollViewCallbacks {
    protected VB itemBinding;

    @Override
    protected CommonFragmentScrollBinding initViewBinding() {
        return CommonFragmentScrollBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initViewObservable() {
        ScrollBindingHelper.backImgClickListener(binding, () -> onBackPressed());
        int title = getCustomTitle();
        if (title > 0) {
            ScrollBindingHelper.setTitle(binding, title);
        }

        binding.scroll.addScrollViewCallbacks(this);
        itemBinding = initItemBinding(binding.body);
        binding.body.addView(itemBinding.getRoot());
        ScrollUtils.addOnGlobalLayoutListener(binding.toolbarTitleTv, new Runnable() {
            @Override
            public void run() {
                updateFlexibleSpaceText(binding.scroll.getCurrentScrollY());
            }
        });
    }

    protected abstract int getCustomTitle();

    protected abstract VB initItemBinding(ViewGroup parent);

    private void updateFlexibleSpaceText(final int scrollY) {
        float scale = scrollY * 1f / SizeUtils.dp2px(33.5F);
        LOG.i("Scroll", "updateFlexibleSpaceText %s-%s-%s", scale, scrollY, SizeUtils.dp2px(33.5F));
        if (scale >= 1) {
            binding.toolbarTitleTv.setTextColor(Color.parseColor("#040818"));
        } else if (scale <= 0) {
            binding.toolbarTitleTv.setTextColor(Color.parseColor("#00040818"));
        } else {
            int color = ColorUtils.setAlphaComponent(Color.parseColor("#040818"), scale);
            binding.toolbarTitleTv.setTextColor(color);
        }
    }

    @Override
    public void onConnected(@Nullable NetworkUtils.NetworkType networkType) {
        binding.statusView.setVisibility(View.GONE);
        binding.statusView.loadSuccess();
        binding.toolbarTitleTv.setVisibility(View.GONE);
    }

    @Override
    public void onDisconnected() {
        binding.statusView.setVisibility(View.VISIBLE);
        binding.statusView.loadError();
        binding.toolbarTitleTv.setVisibility(View.VISIBLE);
        binding.toolbarTitleTv.setTextColor(Color.parseColor("#040818"));
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateFlexibleSpaceText(scrollY);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    protected void visibleBottomLayout(boolean visible) {
        ScrollBindingHelper.visibleBottomLayout(binding, visible);
    }

    protected void negativeClickListener(@StringRes int res, View.OnClickListener listener) {
        ScrollBindingHelper.negativeClickListener(binding, res, listener);
    }

    protected void positiveClickListener(@StringRes int res, View.OnClickListener listener) {
        ScrollBindingHelper.positiveClickListener(binding, res, listener);
    }

    protected void positiveEnableClick(boolean enable) {
        ScrollBindingHelper.positiveEnableClick(binding, enable);
    }

    protected void bottomShadowVisible(boolean visible) {
        ScrollBindingHelper.bottomShadowVisible(binding, visible);
    }

    protected void backgroundColor(@ColorRes int colorRes) {
        ScrollBindingHelper.backgroundColor(binding, colorRes);
    }

    protected void operationClickListener(@StringRes int res, View.OnClickListener listener) {
        ScrollBindingHelper.operationClickListener(binding, res, listener);
    }

    protected void operationClickEnable(boolean enable) {
        ScrollBindingHelper.operationEnableClick(binding, enable);
    }
}
