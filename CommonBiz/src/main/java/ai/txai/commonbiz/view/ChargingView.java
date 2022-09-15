package ai.txai.commonbiz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ai.txai.commonbiz.R;
import ai.txai.commonbiz.base.DragBottomView;
import ai.txai.database.site.Site;


/**
 * Time: 2/24/22
 * Author Hay
 */
public class ChargingView extends DragBottomView implements IBottomView {

    public ChargingView(Context context) {
        this(context, null);
    }

    public ChargingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChargingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void initView() {
        super.initView();
        binding.carStatusContainer.tvCancelText.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    protected String getTitleName() {
        return getContext().getString(R.string.biz_txai_carry_title);
    }

    @NonNull
    @Override
    protected String getDescription(@NonNull Site site) {
        return getContext().getString(R.string.biz_txai_carry_description);
    }
}
