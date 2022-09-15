package ai.txai.commonbiz.main;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.router.ARouterConstants;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.view.TripCancelView;
import ai.txai.commonbiz.viewmodel.CompleteViewModel;
import ai.txai.database.location.Point;
import ai.txai.database.site.Site;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_CANCEL)
public class CancelActivity extends TripDetailsActivity<TripCancelView, CompleteViewModel> {

    @Override
    public void initObservable() {
        viewModel.getOrderDetail().observe(this, order -> {
            if (order.getCreateTime() > 0) {
                bottomView.updateTime(order.getCreateTime());
            }
            bottomView.cancelReason(order.getMemo());
            BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
                @Override
                public void onLoaded(Site... site) {
                    Site pickUpSite = site[0];
                    Site dropOffSite = site[1];
                    List<Point> points = new ArrayList<>();
                    if (pickUpSite != null) {
                        mapBoxService.drawPickUpSmall(pickUpSite.getPoint());
                        mapBoxService.drawPickUpName(pickUpSite);
                        points.add(pickUpSite.getPoint());
                    }
                    if (dropOffSite != null) {
                        mapBoxService.drawDropOffSmall(dropOffSite.getPoint());
                        mapBoxService.drawDropOffName(dropOffSite);
                        points.add(dropOffSite.getPoint());
                    }
                    if (!points.isEmpty()) {
                        centerPoints(points);
                    }
                }
            }, order.getPickUpId(), order.getDropOffId());
        });
    }

    @Override
    protected void clickedCurrentLocation() {
        viewModel.getOrderDetail().postValue(viewModel.getOrder());
    }
}
