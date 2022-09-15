package ai.txai.commonbiz.view

import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.provider.LostFoundProvider
import ai.txai.common.router.ProviderManager
import ai.txai.common.utils.AndroidUtils.buildAmount
import ai.txai.common.utils.AndroidUtils.buildDate
import ai.txai.common.utils.DoubleOperate
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.commonbiz.R
import ai.txai.commonbiz.databinding.BottomMainTripCompleteBinding
import ai.txai.commonbiz.utils.ViewHelper
import ai.txai.database.order.Order
import ai.txai.database.site.Site
import ai.txai.database.vehicle.Vehicle
import ai.txai.database.vehicle.VehicleModel
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout

/**
 * Time: 2/24/22
 * Author Hay
 */
class TripCompleteView : LinearLayout, IBottomView {
    var binding: BottomMainTripCompleteBinding? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        binding = BottomMainTripCompleteBinding.inflate(LayoutInflater.from(context), this, false)
        binding!!.tvCancelTitle.setText(R.string.biz_trip_complete_title)
        binding!!.tvDiscount.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
        addView(binding!!.root)

        val provide = ProviderManager.provide<LostFoundProvider>(ARouterConstants.PATH_SERVICE_LOST_FOUND)
        if (provide == null || !provide.enable()) {
            binding!!.tvHelperTitle.visibility = GONE
            binding!!.clHelperEntrance.visibility = GONE
        }
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    fun updateVehicle(value: Vehicle?) {
        if (value == null) {
            return
        }
        binding!!.carName.text = value.brand
        binding!!.carColor.text = value.color
        binding!!.carNumber.text = value.plateNo
    }

    fun updateTo(site: Site?) {
        if (site == null) {
            return
        }
        binding!!.tvDropOffName.text = site.name
    }

    fun updateFrom(site: Site) {
        binding!!.tvPickUpName.text = site.name
    }

    fun updateDispatchStatus(value: Order?) {
        if (value == null) {
            return
        }
        val discount = value.discountFare
        if (discount == 0.0) {
            binding!!.tvDiscount.visibility = GONE
            binding!!.tvDiscountLevel.visibility = GONE
        } else {
            binding!!.tvDiscount.visibility = VISIBLE
            binding!!.tvDiscountLevel.visibility = VISIBLE
        }
        binding!!.tvAmount.text =
            buildAmount(value.dueFare)
        binding!!.tvDiscount.text = context.getString(
            R.string.biz_aed_with_amount,
            buildAmount(discount)
        )
        binding!!.tvTime.text = buildDate(value.createTime)
    }

    fun setExpandClickListener(listener: OnClickListener?) {
        binding!!.tvReceiptDetails.setDebounceClickListener { listener?.onClick(binding!!.tvReceiptDetails) }
        binding!!.ivMore.setDebounceClickListener { listener?.onClick(binding!!.tvReceiptDetails) }
    }

    fun updateVehicleModelName(vehicleModel: String?) {
        vehicleModel?.let {
            ViewHelper.updateVehicleModelLabel(binding!!.tvTaxiName, vehicleModel)
        }
    }

    fun setLostAndFoundClickListener(listener: OnClickListener?) {
        binding!!.clHelperEntrance.setDebounceClickListener { listener?.onClick(binding!!.clHelperEntrance) }
    }
}