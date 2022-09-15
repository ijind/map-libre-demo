package ai.txai.common.base;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SizeUtils;

import ai.txai.common.R;
import ai.txai.common.base.helper.ScrollBindingHelper;
import ai.txai.common.databinding.CommonFragmentScrollBinding;
import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseMvvmFragment;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.commonview.observablescrollview.ObservableScrollViewCallbacks;
import ai.txai.commonview.observablescrollview.ScrollState;
import ai.txai.commonview.observablescrollview.ScrollUtils;

public abstract class BaseScrollFragment<VB extends ViewBinding, VM extends BaseViewModel> extends BaseMvvmFragment<CommonFragmentScrollBinding, VM> implements ObservableScrollViewCallbacks {
    protected VB itemBinding;

    @Override
    protected CommonFragmentScrollBinding initViewBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return CommonFragmentScrollBinding.inflate(inflater, container, false);
    }

    @Override
    public void initViewObservable() {
        ScrollBindingHelper.backImgClickListener(binding, () -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        });
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
