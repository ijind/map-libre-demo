package ai.txai.commonbiz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.Locale;

import ai.txai.commonbiz.R;
import ai.txai.commonbiz.bean.OrderResponse;
import ai.txai.commonbiz.databinding.BottomMainWaitingSendCarBinding;
import ai.txai.commonbiz.utils.ViewHelper;
import ai.txai.database.vehicle.VehicleModel;
import ai.txai.push.payload.notify.DispatchWaitingNotify;


/**
 * Time: 2/24/22
 * Author Hay
 */
public class PendingView extends LinearLayout implements IBottomView {
    BottomMainWaitingSendCarBinding binding;

    public PendingView(Context context) {
        this(context, null);
    }

    public PendingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PendingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = BottomMainWaitingSendCarBinding.inflate(LayoutInflater.from(context), this, false);
        binding.tvCancelTitle.setText(R.string.biz_waiting_notice);
        addView(binding.getRoot());

    }

    public PendingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void startAnimation() {
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(-1);
        rotate.setDuration(2000);
        binding.ivProcessing.startAnimation(rotate);
    }

    public void stopAnimation() {
        binding.ivProcessing.clearAnimation();
    }

    @Override
    public void added() {
        startAnimation();
    }

    @Override
    public void beforeRemove() {
        stopAnimation();
    }


    public void setCancelClick(@Nullable OnClickListener l) {
        if (l == null) {
            return;
        }

        binding.tvCancelText.setNegativeClickListener(l);
    }

    public void updateOrderInfo(OrderResponse response) {
        binding.tvQueueWaitingIndex.setText(String.valueOf(response.waitingQueue.waitingInx));
        binding.tvQueueWaitingSize.setText(String.format(Locale.US, "/%d", response.waitingQueue.waitingCnt));
    }

    public void updateOrderInfo(DispatchWaitingNotify notify) {
        binding.tvQueueWaitingIndex.setText(String.valueOf(notify.waitingInx));
        binding.tvQueueWaitingSize.setText(String.format(Locale.US, "/%d", notify.waitingCnt));
    }

    public void updateQueue(int waitingInx, int waitingCnt) {
        binding.tvQueueWaitingIndex.setText(String.valueOf(waitingInx));
        binding.tvQueueWaitingSize.setText(String.format(Locale.US, "/%d", waitingCnt));
    }

    public void updateVehicleModelName(String modelId) {
        ViewHelper.updateVehicleModelLabel(binding.tvTaxiName, modelId);
    }
}
