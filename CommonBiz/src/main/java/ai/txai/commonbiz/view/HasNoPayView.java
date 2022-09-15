package ai.txai.commonbiz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import ai.txai.commonbiz.databinding.BottomMainHasNoPayBinding;


/**
 * Time: 2/24/22
 * Author Hay
 */
public class HasNoPayView extends LinearLayout implements IBottomView {
    BottomMainHasNoPayBinding binding;

    public HasNoPayView(Context context) {
        this(context, null);
    }

    public HasNoPayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HasNoPayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = BottomMainHasNoPayBinding.inflate(LayoutInflater.from(context), this, false);
        addView(binding.getRoot());
    }

    public HasNoPayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void added() {
    }

    @Override
    public void beforeRemove() {
    }
}
