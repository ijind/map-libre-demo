package ai.txai.lostfound.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollActivity;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.widget.popupview.ContactUsView;
import ai.txai.lostfound.R;
import ai.txai.lostfound.databinding.LfInfoFragmentBinding;
import ai.txai.lostfound.viewmodel.LostFoundViewModel;

/**
 * Time: 09/05/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_ACTIVITY_LF)
public class LostFoundMainActivity extends BaseScrollActivity<LfInfoFragmentBinding, LostFoundViewModel> {
    public static final String EXTRA_ORDER_ID = "extra.order.id";
    public static final String EXTRA_EXIST = "extra.exist";
    private ContactUsView customerView;

    private String orderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.loadLegalArticles();
    }

    @Override
    protected int getCustomTitle() {
        return R.string.lf_title;
    }

    @Override
    protected LfInfoFragmentBinding initItemBinding(ViewGroup parent) {
        return LfInfoFragmentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            orderId = extras.getString(EXTRA_ORDER_ID);
            boolean isFinish = extras.getBoolean(EXTRA_EXIST);
            if (isFinish) {
                finish();
            }
        }
        negativeClickListener(R.string.lf_contact_us, v -> showCustomer());

        positiveClickListener(R.string.lf_property_report, v -> ARouterUtils.navigation(LostFoundMainActivity.this, ARouterConstants.PATH_FRAGMENT_LOST_REPORT, extras));

        itemBinding.llProcessProgressQuery.setOnClickListener(v -> ARouterUtils.navigation(LostFoundMainActivity.this, ARouterConstants.PATH_FRAGMENT_LOST_LIST, extras));

        viewModel.getArticles().observe(this, articlesInfo -> {
            if (articlesInfo == null) return;

            final String title = articlesInfo.getTitle();
            final String content = articlesInfo.getContent();
            itemBinding.contactUsTitleTv.setText(title);
            itemBinding.contactUsContentTv.setText(content);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            boolean isFinish = extras.getBoolean(EXTRA_EXIST);
            if (isFinish) {
                finish();
            }
        }
    }

    private void showCustomer() {
        if (customerView == null) {
            customerView = new ContactUsView(this);
        }
        customerView.showPop();
    }

    @Override
    public void onDestroy() {
        if (customerView != null) {
            customerView.dismissPop();
        }
        super.onDestroy();
    }
}
