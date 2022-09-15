package ai.txai.commonbiz.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.base.DragBottomView;
import ai.txai.database.site.Site;


/**
 * Time: 2/24/22
 * Author Hay
 */
public class ArrivingView extends DragBottomView implements IBottomView {
    public ArrivingView(Context context) {
        this(context, null);
    }

    public ArrivingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArrivingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ArrivingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    @Override
    protected String getTitleName() {
        return getContext().getString(R.string.biz_txai_arriving_title);
    }

    @NonNull
    @Override
    protected String getDescription(@NonNull Site site) {
        return getContext().getString(R.string.biz_txai_arriving_description, site.getName());
    }
}
