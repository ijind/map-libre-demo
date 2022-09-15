package ai.txai.lostfound.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.router.provider.BizProvider;
import ai.txai.common.router.ProviderManager;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.widget.popupview.ContactUsView;
import ai.txai.commonview.NoLineClickSpan;
import ai.txai.commonview.items.NoLineItem;
import ai.txai.database.order.Order;
import ai.txai.lostfound.R;
import ai.txai.lostfound.activity.LostFoundMainActivity;
import ai.txai.lostfound.bean.LostDetailEntry;
import ai.txai.lostfound.constant.Constant;
import ai.txai.lostfound.databinding.LfProgressDetailBinding;
import ai.txai.lostfound.viewmodel.LostDetailViewModel;

/**
 * Time: 28/04/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_LOST_PROGRESS)
public class LostProgressFragment extends BaseScrollFragment<LfProgressDetailBinding, LostDetailViewModel> {
    public static final String EXTRA_LOST_ID = "extra.lost.id";
    private ContactUsView customerView;

    @Override
    protected int getCustomTitle() {
        return R.string.lf_lost_progress_detail;
    }

    @Override
    protected LfProgressDetailBinding initItemBinding(ViewGroup parent) {
        return LfProgressDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        backgroundColor(ai.txai.common.R.color.commonview_grey_f6);
        viewModel.getDetail().observe(this, new Observer<LostDetailEntry>() {
            @Override
            public void onChanged(LostDetailEntry entry) {
                if (entry == null) {
                    return;
                }
                viewModel.requestOrderInfo(entry.orderId);
                switch (entry.status) {
                    case Constant.PENDING:
                        itemBinding.layoutProgress.line1.setVisibility(View.GONE);
                        itemBinding.layoutProgress.line2.setVisibility(View.GONE);
                        itemBinding.layoutProgress.ctvProcess.setVisibility(View.GONE);
                        itemBinding.layoutProgress.tvProcessTime.setVisibility(View.GONE);
                        itemBinding.layoutProgress.ivProcess.setVisibility(View.GONE);
                        itemBinding.layoutProgress.ctvComplete.setVisibility(View.GONE);
                        itemBinding.layoutProgress.tvCompleteTime.setVisibility(View.GONE);
                        itemBinding.layoutProgress.ivComplete.setVisibility(View.GONE);

                        itemBinding.layoutProgress.ctvPending.setTextColor(getContext().getResources().getColor(R.color.commonview_orange_00));
                        itemBinding.layoutProgress.ivPending.setImageResource(R.mipmap.ic_process_ing);
                        break;
                    case Constant.PROCESSING:
                        itemBinding.layoutProgress.line2.setVisibility(View.GONE);
                        itemBinding.layoutProgress.ctvComplete.setVisibility(View.GONE);
                        itemBinding.layoutProgress.tvCompleteTime.setVisibility(View.GONE);
                        itemBinding.layoutProgress.ivComplete.setVisibility(View.GONE);
                        itemBinding.layoutProgress.ctvProcess.setTextColor(getContext().getResources().getColor(R.color.commonview_orange_00));
                        itemBinding.layoutProgress.ivProcess.setImageResource(R.mipmap.ic_process_ing);
                        break;
                    case Constant.COMPLETE:
                        itemBinding.layoutProgress.ctvComplete.setTextColor(getContext().getResources().getColor(R.color.commonview_orange_00));
                        itemBinding.layoutProgress.ivComplete.setImageResource(R.mipmap.ic_process_ing);
                        break;
                }

                itemBinding.layoutProgress.tvPendingTime.setText(AndroidUtils.INSTANCE.buildDate(entry.createdAt));
                itemBinding.layoutProgress.tvProcessTime.setText(AndroidUtils.INSTANCE.buildDate(entry.handleStart));
                itemBinding.layoutProgress.tvCompleteTime.setText(AndroidUtils.INSTANCE.buildDate(entry.completedAt));

                itemBinding.layoutSubmit.tvItemTypeContent.setText(entry.itemTypeName);
                itemBinding.layoutSubmit.tvItemDescriptionContent.setText(entry.summary);
                itemBinding.layoutSubmit.tvContactContent.setText(String.format("+%s %s", entry.callingCode, entry.phone));
                itemBinding.layoutSubmit.tvEmailContent.setText(entry.email);

                NoLineItem contactUsItem = new NoLineItem(R.color.commonview_orange_00, getString(R.string.lf_contact_us), new NoLineClickSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        showCustomer();
                    }
                });

                NoLineItem resubmitItem = new NoLineItem(R.color.commonview_orange_00, getString(R.string.lf_resubmit), new NoLineClickSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        Bundle extras = new Bundle();
                        extras.putString(LostFoundMainActivity.EXTRA_ORDER_ID, entry.orderId);
                        ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_FRAGMENT_LOST_REPORT, extras);
                    }
                });
                NoLineClickSpan.setClickableSpan(itemBinding.tvDetailsExtraNotice, getContext(), getString(R.string.lf_detail_notice), contactUsItem, resubmitItem);
            }
        });

        viewModel.getOrder().observe(this, new Observer<Order>() {
            @Override
            public void onChanged(Order order) {
                itemBinding.layoutOrderInfo.tvTime.setText(AndroidUtils.INSTANCE.buildDate(order.getCreateTime()));
                if (order.payOrderInfo != null) {
                    itemBinding.layoutOrderInfo.tvAmount.setText(getString(R.string.lf_aed_with_amount,
                            AndroidUtils.INSTANCE.buildAmount(order.payOrderInfo.paidFare)));
                }

                BizProvider bizProvider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_BIZ);
                if (bizProvider != null) {
                    bizProvider.updateVehicleModel(itemBinding.layoutOrderInfo.tvTaxiName, order.getVehicleModelId());
                    bizProvider.updateSiteName(itemBinding.layoutOrderInfo.tvPickUpName, order.getPickUpId());
                    bizProvider.updateSiteName(itemBinding.layoutOrderInfo.tvDropOffName, order.getDropOffId());
                }
            }
        });
    }

    private void showCustomer() {
        if (customerView == null) {
            customerView = new ContactUsView(getActivity());
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
