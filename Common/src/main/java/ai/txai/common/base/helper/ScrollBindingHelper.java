package ai.txai.common.base.helper;

import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.IntRange;
import androidx.annotation.StringRes;

import ai.txai.common.R;
import ai.txai.common.databinding.CommonFragmentScrollBinding;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.ClickDebounceUtilsKt;

/**
 * Time: 20/06/2022
 * Author Hay
 */
public class ScrollBindingHelper {

    public static void backImgClickListener(CommonFragmentScrollBinding binding, Runnable clickRunnable) {
        binding.settingBackImg.setOnClickListener(view -> clickRunnable.run());
    }

    public static void setTitle(CommonFragmentScrollBinding binding, @StringRes int titleRes) {
        binding.toolbarTitleTv.setText(titleRes);
        binding.bigTitle.titleTv.setText(titleRes);
    }

    public static void visibleBottomLayout(CommonFragmentScrollBinding binding, boolean visible) {
        binding.llBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void initButtonStyle(CommonFragmentScrollBinding binding, @IntRange(from = 1, to = 3) int style) {
        binding.llBottom.updateButtonStyle(style);
    }

    public static void negativeClickListener(CommonFragmentScrollBinding binding, @StringRes int res, View.OnClickListener listener) {
        visibleBottomLayout(binding, true);
        binding.llBottom.setNegativeText(res);
        binding.llBottom.setNegativeVisibility(true);
        binding.llBottom.setNegativeClickListener(listener);
    }

    public static void positiveClickListener(CommonFragmentScrollBinding binding, @StringRes int res, View.OnClickListener listener) {
        visibleBottomLayout(binding, true);
        binding.llBottom.setPositiveText(res);
        binding.llBottom.setPositiveVisibility(true);
        binding.llBottom.setPositiveClickListener(listener);
    }

    public static void backgroundColor(CommonFragmentScrollBinding binding, @ColorRes int colorRes) {
        binding.getRoot().setBackgroundResource(colorRes);
        titleBarBackgroundColor(binding, colorRes);
    }

    public static void titleBarBackgroundColor(CommonFragmentScrollBinding binding, @ColorRes int colorRes) {
        binding.titleLayout.setBackgroundResource(colorRes);
    }

    public static void positiveEnableClick(CommonFragmentScrollBinding binding, boolean enable) {
        binding.llBottom.setPositiveEnable(enable);
    }

    public static void bottomShadowVisible(CommonFragmentScrollBinding binding, boolean visible) {
        binding.bottomShadow.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void operationEnableClick(CommonFragmentScrollBinding binding, boolean enable) {
        binding.operationTv.setEnabled(enable);
        if (enable) {
            binding.operationTv.setTextColor(AndroidUtils.INSTANCE.getApplicationContext().getResources().getColor(R.color.commonview_orange_00));
        } else {
            binding.operationTv.setTextColor(AndroidUtils.INSTANCE.getApplicationContext().getResources().getColor(R.color.commonview_grey_c3));
        }
    }

    public static void operationClickListener(CommonFragmentScrollBinding binding, @StringRes int res, View.OnClickListener listener) {
        binding.operationTv.setText(res);
        binding.operationTv.setVisibility(View.VISIBLE);
        ClickDebounceUtilsKt.setDebounceClickListener(binding.operationTv, listener);
    }

    public static void operationVisible(CommonFragmentScrollBinding binding, boolean visible) {
        binding.operationTv.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
