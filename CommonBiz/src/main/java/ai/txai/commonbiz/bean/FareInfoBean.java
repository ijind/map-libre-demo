package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class FareInfoBean {
    @SerializedName("base_fare")
    public double baseFare;
    @SerializedName("base_mileage")
    public double baseMileage;
    @SerializedName("per_mileage_surcharge")
    public double perMileageSurcharge;
    @SerializedName("mileage_surcharge")
    public double mileageSurcharge;
    @SerializedName("total_fare")
    public double totalFare;
    @SerializedName("discount_fare")
    public double discountFare;
    @SerializedName("free_order")
    public boolean freeOrder;
    @SerializedName("total_mileage")
    public double totalMileage;

    public ai.txai.database.order.bean.FareInfoBean toDBFareInfo() {
        ai.txai.database.order.bean.FareInfoBean fareInfoBean = new ai.txai.database.order.bean.FareInfoBean();
        fareInfoBean.baseFare = baseFare;
        fareInfoBean.baseMileage = baseMileage;
        fareInfoBean.perMileageSurcharge = perMileageSurcharge;
        fareInfoBean.mileageSurcharge = mileageSurcharge;
        fareInfoBean.totalFare = totalFare;
        fareInfoBean.discountFare = discountFare;
        fareInfoBean.freeOrder = freeOrder;
        fareInfoBean.totalMileage = totalMileage;
        return fareInfoBean;
    }
}
