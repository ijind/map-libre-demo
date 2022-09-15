package ai.txai.commonbiz.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.lxj.xpopup.core.CenterPopupView;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.dialog.DialogCreator;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ProviderManager;
import ai.txai.common.router.provider.PaymentProvider;
import ai.txai.common.utils.DoubleOperate;
import ai.txai.common.widget.popupview.CustomLoadingPopupView;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.fragment.ExpendDetailsFragment;
import ai.txai.commonbiz.onetrip.TripStateMachine;
import ai.txai.commonbiz.view.FinishView;
import ai.txai.commonbiz.viewmodel.FinishViewModel;
import ai.txai.database.location.Point;
import ai.txai.database.order.Order;
import ai.txai.database.order.bean.FareInfoBean;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.Vehicle;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_FINISH)
public class FinishedActivity extends TripDetailsActivity<FinishView, FinishViewModel> {
    public static final String EXTRA_PAY_STATUS = "extra.pay.status";
    public static final String EXTRA_PAY_MEMO = "extra.pay.memo";
    public static final String EXTRA_PAY_FAILURE_TYPE = "extra.pay.failure.type";
    private CenterPopupView currentDialog;

    @Override
    protected void onResume() {
        super.onResume();
        PaymentProvider provider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_PAYMENT);
        if (provider == null) {
            return;
        }
        if (provider.isPaying(viewModel.getOrderId())) {
            if (currentDialog != null && currentDialog.isShow()) {
                return;
            }
            showLoading(getString(R.string.biz_order_payment_processing), new CustomLoadingPopupView.TimeoutListener() {
                @Override
                public void onTimeout() {
                    if (currentDialog != null) {
                        currentDialog.dismiss();
                    }
                    currentDialog = DialogCreator.showConfirmDialog(FinishedActivity.this, R.mipmap.payment_ic_failed,
                            getString(R.string.biz_order_payment_request_timeout),
                            getString(R.string.biz_order_payment_request_timeout_notice), null);

                }
            });
            postDelay(() -> {
                viewModel.requestPayStatus();
            }, 5000);
        }
    }

    @Override
    public void initObservable() {
        viewModel.getVehicleDetail().observe(this, new Observer<Vehicle>() {
            @Override
            public void onChanged(Vehicle vehicleDetailResponse) {
                bottomView.updateVehicle(vehicleDetailResponse);
            }
        });

        bottomView.setExpandClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(ExpendDetailsFragment.EXTRA_ORDER_ID, viewModel.getOrderId());
                viewModel.router(ARouterConstants.PATH_FRAGMENT_EXPEND_DETAILS, bundle);
            }
        });

        viewModel.getOrderDetail().observe(this, new Observer<Order>() {
            @Override
            public void onChanged(Order order) {
                if (order != null) {
                    bottomView.updateDispatchStatus(order);
                    final String vehicleNo = order.getVehicleNo();
                    final String vehicleModelId = order.getVehicleModelId();
                    viewModel.requestVehicleDetails(vehicleNo);
                    bottomView.updateVehicleModelName(vehicleModelId);
                    List<Point> estimateRouter = order.getEstimateRouter();

                    BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
                        @Override
                        public void onLoaded(Site... site) {
                            Site pickUpSite = site[0];
                            Site dropOffSite = site[1];
                            if (pickUpSite != null) {
                                mapBoxService.drawPickUpSmall(pickUpSite.getPoint());
                                mapBoxService.drawPickUpName(pickUpSite);
                            }
                            if (dropOffSite != null) {
                                mapBoxService.drawDropOffSmall(dropOffSite.getPoint());
                                mapBoxService.drawDropOffName(dropOffSite);
                            }

                            if (estimateRouter != null) {
                                mapBoxService.drawRouter(estimateRouter, Color.parseColor("#B9BEC3"));
                                centerPoints(estimateRouter);
                            } else {
                                List<Point> points = new ArrayList<>();
                                if (pickUpSite != null) {
                                    points.add(pickUpSite.getPoint());
                                }
                                if (dropOffSite != null) {
                                    points.add(dropOffSite.getPoint());
                                }
                                centerPoints(points);
                            }

                        }
                    }, order.getPickUpId(), order.getDropOffId());
                }
            }
        });

        bottomView.setPaymentMethodClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Order value = viewModel.getOrderDetail().getValue();
                if (value == null) {
                    return;
                }
                PaymentProvider provider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_PAYMENT);
                if (provider == null) {
                    return;
                }
                FareInfoBean fareInfo = value.getFareInfo();
                double actualPay = DoubleOperate.subtract(fareInfo.totalFare, fareInfo.discountFare);
                provider.selectCard(FinishedActivity.this, actualPay, value.getId());
            }
        });

        bottomView.setPaymentNowClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Order value = viewModel.getOrderDetail().getValue();
                if (value == null) {
                    return;
                }
                PaymentProvider provider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_PAYMENT);
                if (provider == null) {
                    return;
                }
                FareInfoBean fareInfo = value.getFareInfo();
                double actualPay = DoubleOperate.subtract(fareInfo.totalFare, fareInfo.discountFare);
                provider.payNow(FinishedActivity.this, actualPay, value.getId());
            }
        });

        viewModel.getMethod().observe(this, payMethod -> {
            if (viewModel.hasDefaultMethod()) {
                bottomView.updatePaymentMethod(payMethod.logoRes, payMethod.name, payMethod.isCard);
            }
        });

        showPayResult(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        showPayResult(intent);
    }

    private void showPayResult(Intent intent) {
        Bundle extras = intent.getExtras();
        String payStatus = extras.getString(EXTRA_PAY_STATUS);
        String payMemo = extras.getString(EXTRA_PAY_MEMO);
        String failureType = extras.getString(EXTRA_PAY_FAILURE_TYPE);

        showToast(payStatus, payMemo, failureType);
    }

    private boolean showToast(String payStatus, String payMemo, String failureType) {
        if (TextUtils.equals(payStatus, TripStateMachine.Cancelled)) {
            hideLoading();
            showToast(getString(R.string.biz_order_payment_cancelled), false);
            return true;
        } else if (TextUtils.equals(payStatus, TripStateMachine.Paid_Failure)) {
            hideLoading();
            if (TextUtils.equals(failureType, TripStateMachine.Paid_Failure_Expired)) {
                if (currentDialog != null) {
                    currentDialog.dismiss();
                }
                currentDialog = DialogCreator.showConfirmDialog(this, R.mipmap.payment_ic_failed,
                        getString(R.string.biz_order_payment_failed_expire),
                        getString(R.string.biz_order_payment_expire_try_later), null);
            } else {
                if (currentDialog != null) {
                    currentDialog.dismiss();
                }
                currentDialog = DialogCreator.showConfirmDialog(this, R.mipmap.payment_ic_failed,
                        getString(R.string.biz_order_payment_failed),
                        payMemo, null);
            }
            return true;
        } else if (TextUtils.equals(payStatus, TripStateMachine.Paid_Success)) {
            hideLoading();
            return true;
        } else if (TextUtils.equals(payStatus, TripStateMachine.Paid_In_Progress)) {
            postDelay(() -> {
                viewModel.requestPayStatus();
            }, 5000);
        }
        return false;
    }

    @Override
    protected void clickedCurrentLocation() {
        viewModel.getOrderDetail().postValue(viewModel.getOrder());
    }
}
