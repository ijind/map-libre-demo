package ai.txai.commonbiz.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.base.DragBottomView;
import ai.txai.database.site.Site;


/**
 * Time: 2/24/22
 * Author Hay
 */
public class ArrivedView extends DragBottomView implements IBottomView {

    public ArrivedView(Context context) {
        this(context, null);
    }

    public ArrivedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArrivedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ArrivedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    @Override
    protected String getTitleName() {
        return getContext().getString(R.string.biz_txai_arrived_title);
    }

    @NonNull
    @Override
    protected String getDescription(@NonNull Site site) {
        return getContext().getString(R.string.biz_txai_arrived_description, site.getName());
    }
}
