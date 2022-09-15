package ai.txai.commonbiz.search;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.maps.plugin.gestures.OnMoveListener;

import java.util.List;

import ai.txai.common.animation.AnimationUtils;
import ai.txai.common.json.GsonManager;
import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.FormatUtils;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.bean.AreaResponse;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.main.TripDetailsActivity;
import ai.txai.commonbiz.view.ChoseSiteView;
import ai.txai.commonbiz.viewmodel.SearchInMapViewModel;
import ai.txai.database.location.Point;
import ai.txai.database.site.Site;
import ai.txai.mapsdk.utils.MapBoxUtils;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_SEARCH_IN_MAP)
public class SearchInMapActivity extends TripDetailsActivity<ChoseSiteView, SearchInMapViewModel> {
    public static final int REQUEST_CODE = 1002;
    private boolean firstPage = true;
    private boolean isPickUp = false;
    private Point currentPoint;
    private String pickupId;
    private String dropOffId;
    private Site selectedSite;

    private ObjectAnimator animator;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void initData(Bundle args) {
        super.initData(args);
        firstPage = args.getBoolean(SearchActivity.EXTRA_FIRST_PAGE, true);
        isPickUp = args.getBoolean(SearchActivity.EXTRA_IS_PICK_UP);
        String strPoint = args.getString(SearchActivity.EXTRA_CURRENT_POINT);
        currentPoint = GsonManager.fromJsonObject(strPoint, Point.class);
        pickupId = args.getString(SearchActivity.EXTRA_PICK_UP_SITE);
        dropOffId = args.getString(SearchActivity.EXTRA_DROP_OFF_SITE);
    }

    @Override
    public void initObservable() {
        mapBoxService.addOnMoveListener(this);
        viewModel.getMapSelectSite().observe(this, new Observer<>() {
            @Override
            public void onChanged(Site site) {
                bottomView.currentSite(site);
                selectedSite = site;
                if (site != null) {
                    mapBoxService.centerWithAnimation(site.getPoint(), new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            startJumpAnimation();
                            mapBoxService.gestureFocalPoint(site.getPoint());
                        }
                    });
                }
            }
        });
        viewModel.getAllSites().observe(this, new Observer<List<Site>>() {
            @Override
            public void onChanged(List<Site> sites) {
                mapBoxService.drawSites(sites);
            }
        });
        binding.ivSelectedSiteSign.setVisibility(View.VISIBLE);
        if (isPickUp) {
            binding.ivSelectedSiteSign.setImageResource(R.mipmap.ic_point_pick_up);
        } else {
            binding.ivSelectedSiteSign.setImageResource(R.mipmap.ic_point_drop_off);
        }
        initBottomView();

        String selectId = isPickUp ? pickupId : dropOffId;
        BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
            @Override
            public void onLoaded(Site... site) {
                selectedSite = site[0];
                if (selectedSite != null) {
                    mapBoxService.center(selectedSite.getPoint(), new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startJumpAnimation();
                            mapBoxService.gestureFocalPoint(selectedSite.getPoint());
                        }
                    });
                } else if (currentPoint != null) {
                    mapBoxService.center(currentPoint, new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mapBoxService.gestureFocalPoint(currentPoint);
                        }
                    });
                }
                bottomView.currentSite(selectedSite);
            }
        }, selectId);

        binding.currentLocation.setVisibility(View.VISIBLE);
    }

    private void startJumpAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        if (animator == null) {
            animator = AnimationUtils.INSTANCE.newJumpAnim(binding.ivSelectedSiteSign);
        }
        animator.start();
    }

    private void initBottomView() {
        LOG.d(TAG, "select model change %s", isPickUp);
        bottomView.setSearchListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstPage) {
                    Bundle bundle;
                    Bundle args = getIntent().getExtras();
                    if (args != null) {
                        bundle = new Bundle(args);
                    } else {
                        bundle = new Bundle();
                    }
                    bundle.putBoolean(SearchActivity.EXTRA_FIRST_PAGE, false);
                    ARouterUtils.navigationWithBundleForResult(SearchInMapActivity.this, ARouterConstants.PATH_ACTIVITY_SEARCH, bundle, SearchActivity.REQUEST_CODE);
                } else {
                    finish();
                }
            }
        });
        bottomView.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishSelected();
            }
        });


        if (isPickUp) {
            bottomView.toPickupModel();
        } else {
            bottomView.toDropOffModel();
        }
    }

    private void finishSelected() {
        Site site = selectedSite;
        if (site != null) {
            Intent intent = new Intent();
            if ((isPickUp && TextUtils.equals(site.getId(), dropOffId))
                    || (!isPickUp && TextUtils.equals(site.getId(), pickupId))) {
                showToast(getString(R.string.biz_pickup_and_dropoff_cannot_same), false);
                return;
            }

            AreaResponse area = BizData.getInstance().getArea(site.getAreaId());
            String s = AndroidUtils.INSTANCE.buildDateHour(System.currentTimeMillis());
            if (FormatUtils.INSTANCE.compareHour(s, area.opStartTime) > 0
                    && FormatUtils.INSTANCE.compareHour(s, area.opEndTime) < 0) {
                intent.putExtra(SearchActivity.EXTRA_CONTENT, site.getId());
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                showToast(getString(R.string.biz_station_out_of_hours), false);
            }
        }
    }

    @Override
    public boolean onMove(@NonNull MoveGestureDetector detector) {
        LOG.i(TAG, "onMove %s", detector.getFocalPoint());
        com.mapbox.geojson.Point centerPoint = mapBoxService.getCenterPoint();
        Site site = MapBoxUtils.nearlySite(centerPoint, viewModel.getAllSites().getValue());
        bottomView.currentSite(site);
        return false;
    }

    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector detector) {
        super.onMoveBegin(detector);
    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector detector) {
        LOG.i(TAG, "onMoveEnd %s", detector.getFocalPoint());
        com.mapbox.geojson.Point centerPoint = mapBoxService.getCenterPoint();
        Site site = MapBoxUtils.nearlySite(centerPoint, viewModel.getAllSites().getValue());
        viewModel.getMapSelectSite().postValue(site);
    }

    @Override
    protected void focusCurrentPoint(Point point) {
        super.focusCurrentPoint(point);
        viewModel.defaultLocation(point);
    }

    protected void clickedCurrentLocation() {
        enableLocation();
        mapBoxService.toCurrentPoint(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                com.mapbox.geojson.Point centerPoint = mapBoxService.getCenterPoint();
                focusCurrentPoint(new Point(centerPoint.longitude(), centerPoint.latitude()));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == SearchActivity.REQUEST_CODE) {
                String content = data.getStringExtra(SearchActivity.EXTRA_CONTENT);
                finishForResult(content);
            }
        }
    }

    private void finishForResult(String content) {
        Intent intent = new Intent();
        intent.putExtra(SearchActivity.EXTRA_CONTENT, content);
        intent.putExtra(SearchActivity.EXTRA_IS_PICK_UP, isPickUp);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
