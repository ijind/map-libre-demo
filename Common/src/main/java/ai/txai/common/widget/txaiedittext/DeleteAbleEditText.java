package ai.txai.common.widget.txaiedittext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SizeUtils;

import ai.txai.common.R;
import ai.txai.commonview.TextWatcherAdapter;

public class DeleteAbleEditText extends CustomEditText {

    private Drawable endDrawable;

    public DeleteAbleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DeleteAbleEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        setDrawableClickListener(new DrawableClickListener() {
            @Override
            public void onClick(DrawablePosition target) {
                if (target == DrawablePosition.RIGHT) {
                    setText("");
                }
            }
        });

        addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                super.afterTextChanged(s);
                visibleDeleteDrawable(!s.toString().isEmpty());
            }
        });

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int length = getText().length();
                    visibleDeleteDrawable(length != 0);
                } else {
                    visibleDeleteDrawable(false);
                }
            }
        });

    }

    private void visibleDeleteDrawable(boolean visible) {
        if (visible && endDrawable != null) {
            return;
        }
        if (!visible && endDrawable == null) {
            return;
        }
        if (visible) {
            endDrawable = getResources().getDrawable(R.mipmap.commonview_delete_ic);
            endDrawable.setBounds(0, 0, endDrawable.getMinimumWidth(), endDrawable.getMinimumHeight());
            setCompoundDrawablePadding(SizeUtils.dp2px(16));
            setCompoundDrawables(null, null, endDrawable, null);
        } else {
            endDrawable = null;
            setCompoundDrawables(null, null, null, null);
        }
    }
}