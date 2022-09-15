package ai.txai.commonbiz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import ai.txai.common.widget.txaiButton.TxaiButton;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.databinding.BottomChoseSiteBinding;
import ai.txai.database.site.Site;

import androidx.annotation.Nullable;

/**
 * Time: 2/24/22
 * Author Hay
 */
public class ChoseSiteView extends LinearLayout implements IBottomView {
    BottomChoseSiteBinding binding;

    public ChoseSiteView(Context context) {
        this(context, null);
    }

    public ChoseSiteView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChoseSiteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = BottomChoseSiteBinding.inflate(LayoutInflater.from(context), this, false);
        addView(binding.getRoot());
    }

    public ChoseSiteView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void currentSite(Site site) {
        if (site == null) {
            binding.tvChooseSiteName.setText(R.string.biz_no_available_station);
            binding.tvChooseSiteDesc.setText("");
            binding.tvConfirm.setVisibleButtonEnable(false);
            return;
        }
        binding.tvConfirm.setVisibleButtonEnable(true);
        binding.tvChooseSiteName.setText(site.getName());
        binding.tvChooseSiteDesc.setText(site.getDescription());
    }

    public void toPickupModel() {
        binding.ivIcon.setImageResource(R.drawable.ic_pick_up_location);
        binding.tvChooseTitle.setText(R.string.biz_choose_pick_up_site);
        binding.tvConfirm.setPositiveText(R.string.biz_confirm_pick_up);
    }

    public void toDropOffModel() {
        binding.ivIcon.setImageResource(R.drawable.ic_drop_off_location);
        binding.tvChooseTitle.setText(R.string.biz_choose_drop_off_site);
        binding.tvConfirm.setPositiveText(R.string.biz_confirm_drop_off);
    }

    public void setSearchListener(OnClickListener listener) {
        binding.tvChooseSearch.setOnClickListener(listener);
    }

    public void setConfirmListener(OnClickListener listener) {
        binding.tvConfirm.setVisibleButtonClickListener(listener);
    }

}
