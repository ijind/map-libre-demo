package ai.txai.commonbiz.main;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ThreadUtils;

import ai.txai.common.countdown.OnCountDownTickListener;
import ai.txai.common.dialog.DialogCreator;
import ai.txai.common.dialog.TwoSelectDialog;
import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.bean.PendingCountDown;
import ai.txai.commonbiz.bean.WaitingQueueBean;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.onetrip.TripModel;
import ai.txai.commonbiz.view.PendingView;
import ai.txai.commonbiz.viewmodel.PendingViewModel;
import ai.txai.database.enums.TripState;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.push.payload.notify.DispatchWaitingNotify;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_PENDING)
public class PendingActivity extends TripDetailsActivity<PendingView, PendingViewModel> {
    @Override
    public void initObservable() {
        viewModel.getQueueNotify().observe(this, new Observer<DispatchWaitingNotify>() {
            @Override
            public void onChanged(DispatchWaitingNotify notify) {
                bottomView.updateOrderInfo(notify);
            }
        });

        bottomView.startAnimation();
        bottomView.setCancelClick(v -> {
            toMakeSureCancel(TripState.Pending, null);
        });
        Order order = viewModel.getOrder();
        if (order != null) {
            bottomView.updateVehicleModelName(order.getVehicleModelId());
            BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
                @Override
                public void onLoaded(Site... site) {
                    final Site currentPickUpSite = site[0];
                    if (currentPickUpSite == null) {
                        return;
                    }

                    viewModel.getWaitingRefresh().observe(PendingActivity.this, new Observer<WaitingQueueBean>() {
                        @Override
                        public void onChanged(WaitingQueueBean waitingQueueBean) {
                            if (waitingQueueBean == null) {
                                return;
                            }
                            queueCountDown(currentPickUpSite, waitingQueueBean.estimateWaitingTime);
                        }
                    });


                    mapBoxService.ringPoint(currentPickUpSite.getPoint());
                    centerPoints(currentPickUpSite.getPoint());
                    mapBoxService.drawPickUpNameNoOffset(currentPickUpSite);
                    WaitingQueueBean queueBean = TripModel.getInstance().getTripStateMachine().getQueueBean();
                    if (queueBean != null) {
                        bottomView.updateQueue(queueBean.waitingInx, queueBean.waitingCnt);
                        PendingCountDown pendingCountDown = viewModel.getTripModel().getTripStateMachine().getPendingCountDown();
                        if (pendingCountDown == null) {
                            queueCountDown(currentPickUpSite, queueBean.estimateWaitingTime);
                        } else {
                            long currentTimeMillis = System.currentTimeMillis();
                            long left = pendingCountDown.duration - (currentTimeMillis - pendingCountDown.start) / 1000;
                            if (left <= 0) {
                                continueWaiting();
                            } else {
                                queueCountDown(currentPickUpSite, (int) left);
                            }
                        }
                    }

                }
            }, order.getPickUpId());

        }
    }

    private void queueCountDown(Site pickup, int time) {
        viewModel.getTripModel().getTripStateMachine().setPendingCountDown(PendingCountDown.newInstance(System.currentTimeMillis(), time));
        mapBoxService.drawCountdown(pickup, time, new OnCountDownTickListener() {
            @Override
            public void onTick(long time) {

            }

            @Override
            public void onFinish() {
                viewModel.getTripModel().getTripStateMachine().setPendingCountDown(null);
                continueWaiting();
            }
        });
    }

    private void continueWaiting() {
        TripState currentTripState = viewModel.getTripModel().getTripStateMachine().getCurrentTripState();
        LOG.i(TAG, "queueCountDown: finish %s", currentTripState);
        if (TripState.Pending == currentTripState) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogCreator.showTwoSelectDialog(PendingActivity.this, getString(R.string.biz_waiting_timeout_notice),
                            getString(R.string.biz_second_btn_cancel),
                            getString(R.string.biz_second_btn_keep_calling),
                            new TwoSelectDialog.OnClickListener() {

                                @Override
                                public void onNegative() {
                                    viewModel.cancelOrder();
                                }

                                @Override
                                public void onPositive() {
                                    viewModel.requestWaitTime();
                                }
                            }
                    );
                }
            });
        }
    }

    @Override
    protected void clickedCurrentLocation() {
        BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
            @Override
            public void onLoaded(Site... site) {
                final Site currentPickUpSite = site[0];
                if (currentPickUpSite == null) {
                    return;
                }
                centerPoints(currentPickUpSite.getPoint());
            }
        }, viewModel.getOrder().getPickUpId());
    }

    @Override
    public void onDestroy() {
        bottomView.stopAnimation();
        super.onDestroy();
    }
}
