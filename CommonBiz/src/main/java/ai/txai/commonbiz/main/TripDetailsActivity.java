package ai.txai.commonbiz.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SizeUtils;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.maps.plugin.gestures.OnMoveListener;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseMvvmActivity;
import ai.txai.common.permission.PermissionSettings;
import ai.txai.common.permission.PermissionUtils;
import ai.txai.common.permission.dialog.PermissionDialog;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.ClickDebounceUtilsKt;
import ai.txai.common.utils.ReflectionUtils;
import ai.txai.common.widget.DragView;
import ai.txai.common.widget.popupview.CustomerServiceView;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.base.DragBottomView;
import ai.txai.commonbiz.databinding.BizLayoutMapContainerBinding;
import ai.txai.commonbiz.mapbox.impl.MapBoxServiceImpl;
import ai.txai.commonbiz.mapbox.service.MapService;
import ai.txai.commonbiz.view.SecondConfirmCancelViewPop;
import ai.txai.database.enums.TripState;
import ai.txai.database.location.Point;
import ai.txai.database.vehicle.Vehicle;

/**
 * Time: 30/03/2022
 * Author Hay
 */
public abstract class TripDetailsActivity<B extends View, VM extends TripDetailsViewModel> extends BaseMvvmActivity<BizLayoutMapContainerBinding, VM> implements OnMoveListener {
    public static final String EXTRA_BACK_ACTION = "extra.back.action";
    public static final String EXTRA_ORDER_ID = "extra.order.id";
    public static final String TAG = "TripDetailsActivity";
    protected MainBottomViewHelper bottomViewManager = new MainBottomViewHelper();
    protected SecondConfirmCancelViewPop confirmCancelView;
    protected B bottomView;

    protected MapService mapBoxService;

    protected CustomerServiceView customerView;

    @Override
    protected BizLayoutMapContainerBinding initViewBinding() {
        return BizLayoutMapContainerBinding.inflate(getLayoutInflater());
    }

    public void initMapboxFragment() {
        mapBoxService = new MapBoxServiceImpl();
        View mapView = mapBoxService.init(this);
        mapBoxService.addOnMoveListener(this);
        binding.mapViewContainer.addView(mapView);
    }

    @Override
    public void initViewObservable() {
        binding.dialogContainer.setOnClickListener(view -> {
        });
        initMapboxFragment();
        bottomView = bottomView();
        replaceDialog(bottomView);
        binding.question.setVisibility(View.GONE);
        binding.personCenter.setImageResource(R.drawable.ic_back);
        binding.personCenter.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.customerService.setOnClickListener(v -> {
            Log.d(TAG, "customerService click");
            showCustomer();
        });
        binding.currentLocation.setVisibility(View.GONE);
        ClickDebounceUtilsKt.setDebounceClickListener(binding.currentLocation, view -> {
            binding.currentLocation.setVisibility(View.GONE);
            clickedCurrentLocation();
        });

        initData(getIntent().getExtras());
        initObservable();
    }

    protected void clickedCurrentLocation() {

    }

    protected void focusCurrentPoint(Point point) {

    }

    protected void enableLocation() {
        if (PermissionUtils.hasFineLocationPermission(this)) {
            if (mapBoxService != null) {
                mapBoxService.enableUserLocation(true);
            }
        }
    }


    protected void initData(Bundle args) {
    }

    protected final B bottomView() {
        return bottomViewManager.getView(this, getBottomViewClass());
    }

    public Class<B> getBottomViewClass() {
        return ReflectionUtils.getFirstGeneric(this, View.class);
    }

    protected abstract void initObservable();

    private void replaceDialog(View view) {
        binding.dialogContainer.removeAllViews();
        binding.dialogContainer.addView(view);

        view.post(() -> mapBoxService.logoBottom(bottomHeight()));
    }

    @Override
    public void onBackPressed() {
        Bundle args = getIntent().getExtras();
        boolean autoBack = args.getBoolean(EXTRA_BACK_ACTION);
        if (!autoBack) {
            backHome();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        mapBoxService.onFinish();
        super.onDestroy();
    }


    private void showCustomer() {
        if (customerView == null) {
            customerView = new CustomerServiceView(this);
        }
        customerView.showPop();
    }

    protected void backHome() {
        ARouterUtils.navigation(TripDetailsActivity.this, ARouterConstants.PATH_ACTIVITY_V2_MAIN);
    }

    protected int bottomHeight() {
        if (bottomView instanceof DragBottomView) {
            return ((DragBottomView) bottomView).defaultHeight();
        }
        return bottomView.getHeight();
    }

    protected final int mapTop() {
        return SizeUtils.dp2px(115);
    }

    protected final int mapLeftAndRight() {
        return SizeUtils.dp2px(100);
    }

    protected final void centerPoints(List<Point> points) {
        bottomView.post(() -> mapBoxService.center(points, mapTop(), mapLeftAndRight(), bottomHeight() + SizeUtils.dp2px(45), mapLeftAndRight(), null));
    }

    protected final void centerPoints(Point point) {
        if(point == null) {
            return;
        }
        List<Point> points = new ArrayList<>();
        points.add(point);
        centerPoints(points);
    }

    protected void toMakeSureCancel(TripState tripState, Vehicle vehicle) {
        if (confirmCancelView != null) {
            confirmCancelView.showPop();
            return;
        }
        final String plateNo;
        if (vehicle != null) {
            plateNo = vehicle.getPlateNo();
        } else {
            plateNo = "";
        }
        confirmCancelView = new SecondConfirmCancelViewPop(this);
        confirmCancelView.showPop();
        confirmCancelView.post(() -> {
            if (tripState == TripState.Pending) {
                confirmCancelView.setNotice(getString(R.string.biz_waiting_cancel_notice), getString(R.string.biz_waiting_cancel_notice_1));
                confirmCancelView.setPositive(getString(R.string.biz_second_btn_keep_calling));
            } else if (tripState == TripState.Arriving || tripState == TripState.Dispatched) {
                confirmCancelView.setNotice(getString(R.string.biz_arriving_cancel_notice, plateNo), getString(R.string.biz_arriving_cancel_notice_1));
                confirmCancelView.setPositive(getString(R.string.biz_second_btn_keep_waiting));
            } else if (tripState == TripState.Arrived) {
                if (vehicle != null) {
                    confirmCancelView.setNotice(getString(R.string.biz_arrived_cancel_notice, plateNo), getString(R.string.biz_arrived_cancel_notice_1));
                }
                confirmCancelView.setPositive(getString(R.string.biz_second_btn_keep_using));
            }
            confirmCancelView.setOnCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewModel.cancelOrder();
                }
            });
        });
    }

    private float dialogInitPosition = 0f;

    protected void initDragTripView() {
        binding.dialogContainer.setClickable(false);
        binding.dialogShadow.setVisibility(View.GONE);
        binding.dialogContainer.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            dialogInitPosition = binding.dialogContainer.getY();
        });

        final float defaultPosition = getResources().getDisplayMetrics().heightPixels * 0.57f;
        binding.currentLocation.setY(defaultPosition);
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) binding.currentLocation.
                getLayoutParams();
        lp.rightMargin = AndroidUtils.INSTANCE.dip2px(this, 12);
        binding.currentLocation.setLayoutParams(lp);

        if (bottomView instanceof DragBottomView) {
            ((DragBottomView) bottomView).setOnDragStatusListener(new DragView.
                    OnViewStatusChangeListener() {
                @Override
                public void onAlphaChanged(float y) {

                }

                @Override
                public void onTranslationChange(float translationY) {
                    final float dis = translationY + dialogInitPosition;
                    if (Math.abs(dis) <= 100) {
                        final float index = dis / 100;
                        if (dis < 0) {
                            LOG.d(TAG, "on trans index %s", index);
                        }
                    }

//                    if (translationY == 0) {
//                        final ObjectAnimator animator = ObjectAnimator.ofFloat(binding.dragViewMask,
//                                "alpha", 0f, 1f);
//                        animator.setDuration(300);
//                        animator.start();
//                    }
                    LOG.d(TAG, "on trans dis %s", dis);
                }
            });
        }
    }

    @Override
    public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
        return false;
    }

    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
        binding.currentLocation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

    }
}
