package ai.txai.commonbiz.utils;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import java.util.List;

import ai.txai.commonbiz.data.BizData;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleModel;

/**
 * Time: 2/24/22
 * Author Hay
 */
public class ViewHelper {
    public static void updateVehicleModelLabel(TextView textView, VehicleModel vehicleModel) {
        if (textView == null || vehicleModel == null) {
            return;
        }
        textView.setText(vehicleModel.getName());
        if (!TextUtils.isEmpty(vehicleModel.getColor())) {
            Drawable background = textView.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gd = (GradientDrawable) background;
                gd.setColor(Color.parseColor(vehicleModel.getColor()));
            }
        }
    }

    public static void updateVehicleModelLabel(TextView textView, String id) {
        if (textView == null) {
            return;
        }
        VehicleModel vehicleModel = BizData.getInstance().getVehicleModel(id);
        if (vehicleModel != null) {
            updateVehicleModelLabel(textView, vehicleModel);
        } else {
            textView.setText(id);
        }
        BizData.getInstance().requestVehicleModel(new BizData.VehicleModelChangeListener() {
            @Override
            public void finishLoaded(List<VehicleModel> vehicleModels) {
                VehicleModel vehicleModel = BizData.getInstance().getVehicleModel(id);
                if (vehicleModel != null) {
                    updateVehicleModelLabel(textView, vehicleModel);
                } else {
                    textView.setText(id);
                }
            }
        });
    }


    public static void updateSiteLabel(TextView textView, String id) {
        if (textView == null) {
            return;
        }
        Site site = BizData.getInstance().getSite(id);
        if (site != null) {
            textView.setText(site.getName());
        } else {
            textView.setText(id);
        }
        BizData.getInstance().requestSites(new BizData.SiteChangeListener() {
            @Override
            public void onLoaded(List<Site> siteList) {
                Site site = BizData.getInstance().getSite(id);
                if (site != null) {
                    textView.setText(site.getName());
                } else {
                    textView.setText(id);
                }
            }

            @Override
            public void onFailed(String msg) {

            }
        });
    }
}
