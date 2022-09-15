package ai.txai.commonbiz.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.maps.plugin.gestures.OnMoveListener;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.activity.DispatcherActivity;
import ai.txai.common.animation.AnimationUtils;
import ai.txai.common.json.GsonManager;
import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseMvvmActivity;
import ai.txai.common.permission.PermissionSettings;
import ai.txai.common.permission.PermissionUtils;
import ai.txai.common.permission.dialog.PermissionDialog;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.scheme.SchemeDispatchManager;
import ai.txai.common.utils.ClickDebounceUtilsKt;
import ai.txai.common.widget.popupview.CustomerServiceView;
import ai.txai.commonbiz.BuildConfig;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.bean.AreaResponse;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.databinding.BizActivityMainV2Binding;
import ai.txai.commonbiz.mapbox.impl.MapBoxServiceImpl;
import ai.txai.commonbiz.mapbox.service.MapService;
import ai.txai.commonbiz.onetrip.TripModel;
import ai.txai.commonbiz.search.SearchActivity;
import ai.txai.commonbiz.view.AreaListView;
import ai.txai.commonbiz.view.PersonalCenterView;
import ai.txai.commonbiz.view.PickUpView;
import ai.txai.commonbiz.viewmodel.RequestRideViewModel;
import ai.txai.database.enums.TripState;
import ai.txai.database.location.Point;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleIndicator;
import ai.txai.mapsdk.utils.MapBoxUtils;
import ai.txai.push.util.PushUtils;
import kotlin.Pair;

/**
 * Time: 30/03/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_ACTIVITY_V2_MAIN)
public class MainV2Activity extends BaseMvvmActivity<BizActivityMainV2Binding, MainV2ViewModel> implements OnMoveListener {

    public static final String TAG = "MainV2Activity";

    private MainBottomViewHelper bottomViewManager = new MainBottomViewHelper();

    private MapService mapBoxService;
    private AreaListView areaListView;
    private CustomerServiceView customerView;
    private PersonalCenterView personalView;

    private PickUpView pickUpView;

    /**
     * 是否在显示区域信息
     */
    private boolean showingArea = true;

    private ObjectAnimator animator;

    /**
     * 是否自动确认地址， false 表示手动选择的地址
     */
    private boolean autoSelect = true;

    @Override
    protected BizActivityMainV2Binding initViewBinding() {
        return BizActivityMainV2Binding.inflate(getLayoutInflater());
    }

    private void enableLocation(boolean auto) {
        PermissionUtils.requestFineLocationPermission(this, new PermissionUtils.RequestResult() {
            @Override
            public void onGranted(List<String> permissions) {
                LOG.i("PermissionUtils", "onGrant");
                mapBoxService.enableUserLocation(true);
                viewModel.setGrantPermission(true);
                if (!auto) {
                    processSelect();
                    mapBoxService.toCurrentPoint(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            postDelay(() -> viewModel.foreDefaultLocation(), 1000);
                        }
                    });
                } else {
                    delay3sLocation();
                }
            }

            @Override
            public void onDenied(List<String> permissions) {
                viewModel.setGrantPermission(false);
                if (auto) {
                    delay3sLocation();
                }
            }

            @Override
            public void onDeniedNotAsk(PermissionSettings permissionSettings) {
                viewModel.setGrantPermission(false);
                if (!auto) {
                    PermissionDialog.show(MainV2Activity.this, permissionSettings);
                } else {
                    delay3sLocation();
                }
            }

            private void delay3sLocation() {
                ThreadUtils.runOnUiThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Point point = mapBoxService.toCurrentPoint(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                postDelay(viewModel.getDefaultRunnable(), 1000);
                            }
                        });
                        if (point == null) {
                            viewModel.getDefaultRunnable().run();
                        }
                    }
                }, 3000);
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableLocation(true);

        if (BuildConfig.DEBUG) {
            binding.tvPushState.setVisibility(View.VISIBLE);
        } else {
            binding.tvPushState.setVisibility(View.GONE);
        }

        viewModel.checkUpdateVersion();
        SchemeDispatchManager.getInstance().dispatch(this);

        binding.llHomeContainer.dialogContainer.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.llHomeContainer.dialogContainer.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        final int width = binding.llHomeContainer.dialogContainer.getWidth();
                        final ViewGroup.LayoutParams lp = binding.llHomeContainer.dialogShadow.getLayoutParams();
                        lp.width = width + SizeUtils.dp2px(15);
                        binding.llHomeContainer.dialogShadow.setLayoutParams(lp);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.refreshVehicle();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SchemeDispatchManager.getInstance().dispatch(this);
    }

    public void initMapboxFragment() {
        mapBoxService = new MapBoxServiceImpl();
        View mapView = mapBoxService.init(this);
        binding.llHomeContainer.mapViewContainer.addView(mapView);
        mapBoxService.addOnIndicatorPositionChangeListener(viewModel);
        mapBoxService.addOnMoveListener(this);
    }

    @Override
    public void initViewObservable() {

        binding.llHomeContainer.dialogContainer.setOnClickListener(view -> {
        });
        initMapboxFragment();

        ClickDebounceUtilsKt.setDebounceClickListener(binding.llHomeContainer.personCenter, v -> showPersonalCenterView());

        ClickDebounceUtilsKt.setDebounceClickListener(binding.llHomeContainer.question, v -> showAreaList());

        ClickDebounceUtilsKt.setDebounceClickListener(binding.llHomeContainer.customerService, v -> showCustomer());

        ClickDebounceUtilsKt.setDebounceClickListener(binding.llHomeContainer.currentLocation, v -> enableLocation(false));

        viewModel.getAreaPoints().observe(this, new Observer<List<List<Point>>>() {
            @Override
            public void onChanged(List<List<Point>> lists) {
                mapBoxService.drawFillWithLine(lists.get(0));
                Point point = MapBoxUtils.centerPoint(lists.get(0));
                double zoom = MapBoxUtils.getZoom(MapBoxUtils.transferToMapboxPoints(lists.get(0)));
                mapBoxService.zoom(zoom);
                mapBoxService.center(point, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mapBoxService.gestureFocalPoint(point);
                    }
                });
            }
        });

        viewModel.getSelectedSite().observe(this, pair -> {
            if (pair == null) {
                return;
            }
            showingArea = false;
            Boolean isDefault = pair.getFirst();//是否获取默认位置
            Site site = pair.getSecond();

            if (site != null) {
                LOG.d(TAG, GsonManager.getGson().toJson(site));
                if (autoSelect) {
                    autoSelect = false;
                    mapBoxService.hideFillWithLine();
                    List<Site> value = viewModel.getAllSites().getValue();
                    if (value != null && !value.isEmpty()) {
                        List<Point> pointList = new ArrayList<>();
                        for (Site tmp : value) {
                            pointList.add(tmp.getPoint());
                        }
                        double zoom = MapBoxUtils.getZoom(MapBoxUtils.transferToMapboxPoints(pointList));
                        mapBoxService.zoom(zoom);
                    } else {
                        mapBoxService.zoom(14.0);
                    }
                }
                mapBoxService.centerWithAnimation(site.getPoint(), new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        jumpDefaultSiteSing();
                        mapBoxService.gestureFocalPoint(site.getPoint());
                    }
                });
                availableSiteNotice(site);
            } else {
                if (isDefault) {
                    mapBoxService.toCurrentPoint(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            jumpDefaultSiteSing();

                            com.mapbox.geojson.Point centerPoint = mapBoxService.getCenterPoint();
                            mapBoxService.gestureFocalPoint(new Point(centerPoint.longitude(), centerPoint.latitude()));
                        }
                    });
                }
                unavailableSiteNotice();
                jumpDefaultSiteSing();
            }
            if (pickUpView != null) {
                pickUpView.updatePickUpLocation(site);
            }
        });

        viewModel.getUserBean().observe(this, user -> {
            if (personalView != null) {
                personalView.updateUserInfo(user);
            }
        });

        viewModel.getAllSites().observe(this, new Observer<List<Site>>() {
            @Override
            public void onChanged(List<Site> sites) {
                mapBoxService.drawSites(sites);
            }
        });

        viewModel.getAvailableVehicle().observe(this, new Observer<List<VehicleIndicator>>() {
            @Override
            public void onChanged(List<VehicleIndicator> vehicles) {
                mapBoxService.drawVehicles(vehicles);

//                postDelay(new Runnable() {
//                    @Override
//                    public void run() {
//                        viewModel.refreshVehicle();
//                    }
//                }, 1000);
            }
        });

        viewModel.getHasInProgressOrder().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean has) {
                int locationBottom = SizeUtils.dp2px(10);
                if (has) {
                    locationBottom = SizeUtils.dp2px(3);
                    binding.llHomeContainer.clProgressContainer.setVisibility(View.VISIBLE);
                } else {
                    binding.llHomeContainer.clProgressContainer.setVisibility(View.GONE);
                }
                updateLocationMargin(locationBottom);
            }
        });

        viewModel.getPushState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.tvPushState.setText("Push State:" + PushUtils.fromCode(integer));
            }
        });

        switchToPickupView();
    }

    private void jumpDefaultSiteSing() {
        if (animator != null) {
            animator.cancel();
        } else {
            animator = AnimationUtils.INSTANCE.newJumpAnim(binding.llHomeContainer.llDefaultSiteSign.getRoot(), -30f);
        }
        animator.start();
    }

    private void updateLocationMargin(int bottom) {
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                binding.llHomeContainer.currentLocation.getLayoutParams();
        lp.bottomMargin = bottom;
        binding.llHomeContainer.currentLocation.setLayoutParams(lp);
    }

    private void unavailableSiteNotice() {
        binding.llHomeContainer.llDefaultSiteSign.siteName.setText(R.string.biz_no_available_station);
        binding.llHomeContainer.llDefaultSiteSign.ivMore.setVisibility(View.GONE);
        binding.llHomeContainer.llDefaultSiteSign.getRoot().setOnClickListener(null);

        binding.llHomeContainer.ivSelectedSiteSign.setVisibility(View.GONE);
        binding.llHomeContainer.llDefaultSiteSign.getRoot().setVisibility(View.VISIBLE);
    }

    private void availableSiteNotice(Site site) {
        binding.llHomeContainer.llDefaultSiteSign.siteName.setText(site.getName());
        binding.llHomeContainer.llDefaultSiteSign.ivMore.setVisibility(View.VISIBLE);
        binding.llHomeContainer.ivSelectedSiteSign.setVisibility(View.GONE);
        binding.llHomeContainer.llDefaultSiteSign.getRoot().setVisibility(View.VISIBLE);
        ClickDebounceUtilsKt.setDebounceClickListener(binding.llHomeContainer.llDefaultSiteSign.getRoot(), v -> pickUpView.callPickupClick());
    }

    private void processSelect() {
        binding.llHomeContainer.ivSelectedSiteSign.setVisibility(View.VISIBLE);
        binding.llHomeContainer.llDefaultSiteSign.getRoot().setVisibility(View.GONE);
    }

    private void toCurrentStateUI(TripState tripState, Order order) {
        Bundle args = new Bundle();
        if (order != null) {
            args.putString(TripDetailsActivity.EXTRA_ORDER_ID, order.getId());
        }
        switch (tripState) {
            case Pending:
                ARouterUtils.navigation(MainV2Activity.this, ARouterConstants.PATH_ACTIVITY_PENDING, args);
                break;
            case Dispatched:
            case Arriving:
                ARouterUtils.navigation(MainV2Activity.this, ARouterConstants.PATH_ACTIVITY_ARRIVING, args);
                break;
            case Arrived:
                ARouterUtils.navigation(MainV2Activity.this, ARouterConstants.PATH_ACTIVITY_ARRIVED, args);
                break;
            case Charging:
                ARouterUtils.navigation(MainV2Activity.this, ARouterConstants.PATH_ACTIVITY_CHARGING, args);
                break;
            case Finished:
                ARouterUtils.navigation(MainV2Activity.this, ARouterConstants.PATH_ACTIVITY_FINISH, args);
                break;
            case Completed:
                ARouterUtils.navigation(MainV2Activity.this, ARouterConstants.PATH_ACTIVITY_COMPLETE, args);
                break;
        }
    }


    private void showAreaList() {
        if (areaListView == null) {
            areaListView = new AreaListView(this);
        }
        areaListView.showPop();
        List<AreaResponse> value = viewModel.getAreas().getValue();
        if (value != null) {
            areaListView.post(() -> areaListView.updateArea(value));
        }
    }

    private void showCustomer() {
        if (customerView == null) {
            customerView = new CustomerServiceView(this);
        }
        customerView.showPop();
    }

    private void showPersonalCenterView() {
        if (personalView == null) {
            personalView = new PersonalCenterView(this);
        }
        personalView.showPop();
    }

    private void gotoSelectPickupActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SearchActivity.EXTRA_IS_PICK_UP, true);

        com.mapbox.geojson.Point point = TripModel.getInstance().getCurrentPoint();

        if (point != null) {
            bundle.putString(SearchActivity.EXTRA_CURRENT_POINT, GsonManager.GsonString(new Point(point.longitude(), point.latitude())));
        }
        Site value = viewModel.getSelectedSite().getValue().getSecond();
        if (value != null) {
            bundle.putString(SearchActivity.EXTRA_PICK_UP_SITE, value.getId());
        }
        DispatcherActivity.startActivity(ARouterConstants.PATH_ACTIVITY_SEARCH, bundle, this, new DispatcherActivity.ICallback() {
            @Override
            public void onSuccess(String content) {
                BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
                    @Override
                    public void onLoaded(Site... site) {
                        viewModel.getSelectedSite().postValue(new Pair<>(false, site[0]));
                    }
                }, content);
            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void gotoSelectDropOffActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SearchActivity.EXTRA_IS_PICK_UP, false);
        com.mapbox.geojson.Point point = TripModel.getInstance().getCurrentPoint();
        if (point != null) {
            bundle.putString(SearchActivity.EXTRA_CURRENT_POINT, GsonManager.GsonString(new Point(point.longitude(), point.latitude())));
        }
        Site value = viewModel.getSelectedSite().getValue().getSecond();
        if (value != null) {
            bundle.putString(SearchActivity.EXTRA_PICK_UP_SITE, value.getId());
        }
        Site dropOffSite = viewModel.getDropOffSite();
        if (dropOffSite != null) {
            bundle.putString(SearchActivity.EXTRA_DROP_OFF_SITE, dropOffSite.getId());
        }

        DispatcherActivity.startActivity(ARouterConstants.PATH_ACTIVITY_SEARCH, bundle, this, new DispatcherActivity.ICallback() {
            @Override
            public void onSuccess(String content) {
                BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
                    @Override
                    public void onLoaded(Site... site) {
                        gotoRequestView(site[0]);
                    }
                }, content);
            }

            @Override
            public void onFailed() {

            }
        });
    }

    public View switchTo(Class<PickUpView> tClass) {
        View view = bottomViewManager.getView(this, tClass);
        if (view != null) {
            replaceDialog(view);
            view.post(() -> mapBoxService.logoBottom(view.getHeight()));
        } else {
            Log.e(TAG, "not support this tag - " + tClass);
        }
        return view;
    }

    private void switchToPickupView() {
        pickUpView = (PickUpView) switchTo(PickUpView.class);
        pickUpView.setPickUpOnClickListener(v -> {
            if (checkCanStartNewTrip()) return;

            if (!showingArea) {
                gotoSelectPickupActivity();
            }
        });
        pickUpView.setDropOffOnClickListener(v -> {
            if (checkCanStartNewTrip()) return;
            Pair<Boolean, Site> pair = viewModel.getSelectedSite().getValue();
            Site pickupSite = null;
            if (pair != null) {
                pickupSite = pair.getSecond();
            }
            if (!showingArea && pickupSite != null) {
                gotoSelectDropOffActivity();
            }
        });

        ClickDebounceUtilsKt.setDebounceClickListener(binding.llHomeContainer.clProgressContainer, v -> {
            if (TripModel.getInstance().getTripStateMachine().inProgress()) {
                toCurrentStateUI(TripModel.getInstance().getTripStateMachine().getCurrentTripState(), TripModel.getInstance().getTripStateMachine().getOrder());
            } else {
                binding.llHomeContainer.clProgressContainer.setVisibility(View.GONE);
                int locationBottom = SizeUtils.dp2px(10);
                updateLocationMargin(locationBottom);
            }
        });
    }

    private boolean checkCanStartNewTrip() {
        List<Site> value = viewModel.getAllSites().getValue();
        if (value == null || value.isEmpty()) {
            showToast(getString(R.string.biz_no_available_station), false);
            return true;
        }

        if (viewModel.getHasInProgressOrder().getValue() == Boolean.TRUE) {
            showToast(getString(R.string.biz_have_a_trip_in_progress), false);
            return true;
        }

        return false;
    }


    private void replaceDialog(View view) {
        binding.llHomeContainer.dialogContainer.addView(view);
    }

    private void gotoRequestView(Site site) {
        viewModel.setDropOffSite(site);

        Bundle args = new Bundle();
        args.putString(RequestRideViewModel.EXTRA_PICK_UP_SITE, GsonManager.GsonString(viewModel.getSelectedSite().getValue().getSecond()));
        args.putString(RequestRideViewModel.EXTRA_DROP_OFF_SITE, GsonManager.GsonString(site));
        ARouterUtils.navigation(this, ARouterConstants.PATH_ACTIVITY_REQUEST_RIDE, args);
    }

    @Override
    protected void onDestroy() {
        mapBoxService.onFinish();
        if (personalView != null) {
            personalView.onDestroy();
        }
        if (customerView != null) {
            customerView.onDestroy();
        }
        if (areaListView != null) {
            areaListView.onDestroy();
        }
        if (animator != null) {
            animator.cancel();
        }
        super.onDestroy();
    }

    @Override
    public boolean onMove(@NonNull MoveGestureDetector detector) {
        return false;
    }

    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector detector) {
        if (!showingArea) {
            processSelect();
            pickUpView.updatePickUpLocation(null);
        }
    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector detector) {
        if (showingArea) {
            return;
        }

        com.mapbox.geojson.Point centerPoint = mapBoxService.getCenterPoint();
        Site site = MapBoxUtils.nearlySite(centerPoint, viewModel.getAllSites().getValue());
        viewModel.getSelectedSite().postValue(new Pair<>(false, site));
    }
}
