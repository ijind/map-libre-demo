package ai.txai.commonbiz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

import ai.txai.commonbiz.R;
import ai.txai.commonbiz.databinding.BottomMainPickupBinding;
import ai.txai.database.site.Site;

/**
 * Time: 2/24/22
 * Author Hay
 */
public class PickUpView extends LinearLayout implements IBottomView {
    private static final String TAG = "PickupView";

    private BottomMainPickupBinding binding;

    public PickUpView(Context context) {
        this(context, null);
    }

    public PickUpView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickUpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = BottomMainPickupBinding.inflate(LayoutInflater.from(context), this, false);
        addView(binding.getRoot());
    }

    public PickUpView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setPickUpOnClickListener(@Nullable OnClickListener l) {
        binding.pickUpSelect.getRoot().setOnClickListener(l);
    }

    public void callPickupClick() {
        binding.pickUpSelect.getRoot().callOnClick();
    }

    public void setDropOffOnClickListener(@Nullable OnClickListener l) {
        binding.dropOffSelect.getRoot().setOnClickListener(l);
    }


    public void callDropOffClick() {
        binding.dropOffSelect.getRoot().callOnClick();
    }

    public void updatePickUpLocation(Site site) {
        if (site == null) {
            binding.pickUpSelect.pickUpTitle.setText(R.string.biz_choose_pick_up_site);
            binding.pickUpSelect.pickUpSite.setText("");
            return;
        }
        binding.pickUpSelect.pickUpTitle.setText(R.string.biz_pick_up_at);
        binding.pickUpSelect.pickUpSite.setText(site.getName());
    }

    public void updateDropOffLocation(Site site) {
        binding.dropOffSelect.dropOffTv.setText(site.getName());
    }
}
