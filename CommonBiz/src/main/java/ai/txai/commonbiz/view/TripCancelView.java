package ai.txai.commonbiz.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import ai.txai.common.utils.AndroidUtils;
import ai.txai.commonbiz.databinding.BottomMainTripCancelBinding;


/**
 * Time: 2/24/22
 * Author Hay
 */
public class TripCancelView extends LinearLayout implements IBottomView {
    BottomMainTripCancelBinding binding;

    public TripCancelView(Context context) {
        this(context, null);
    }

    public TripCancelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TripCancelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = BottomMainTripCancelBinding.inflate(LayoutInflater.from(context), this, false);
        addView(binding.getRoot());
    }

    public TripCancelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void updateTime(long time) {
        binding.tvTime.setText(AndroidUtils.INSTANCE.buildDate(time));
    }

    public void cancelReason(String memo) {
        if (TextUtils.isEmpty(memo)) {
            binding.tvCancelReason.setVisibility(GONE);
        } else {
            binding.tvCancelReason.setText(memo);
            binding.tvCancelReason.setVisibility(VISIBLE);
        }
    }
}
