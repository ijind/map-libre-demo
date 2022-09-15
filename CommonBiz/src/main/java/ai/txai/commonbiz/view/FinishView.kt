package ai.txai.commonbiz.view

import ai.txai.common.utils.AndroidUtils.buildAmount
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.commonbiz.R
import ai.txai.commonbiz.databinding.BottomMainArrivedDestinationBinding
import ai.txai.commonbiz.utils.ViewHelper
import ai.txai.database.order.Order
import ai.txai.database.vehicle.Vehicle
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout

/**
 * Time: 2/24/22
 * Author Hay
 */
class FinishView : LinearLayout, IBottomView {
    var binding: BottomMainArrivedDestinationBinding? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        binding =
            BottomMainArrivedDestinationBinding.inflate(LayoutInflater.from(context), this, false)
        binding!!.tvDiscount.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
        binding!!.tvCancelTitle.setText(R.string.biz_txai_finish_title)
        addView(binding!!.root)
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

    fun updateVehicleModelName(vehicleModel: String?) {
        vehicleModel?.let {
            ViewHelper.updateVehicleModelLabel(binding!!.tvTaxiName, vehicleModel)
        }
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
    }

    fun setExpandClickListener(listener: OnClickListener?) {
        binding!!.tvReceiptDetails.setDebounceClickListener { listener?.onClick(binding!!.tvReceiptDetails) }
    }

    fun setPaymentMethodClickListener(listener: OnClickListener?) {
        binding!!.llPaymentMethod.setDebounceClickListener { listener?.onClick(binding!!.llPaymentMethod) }
    }

    fun setPaymentNowClickListener(listener: OnClickListener?) {
        binding!!.tvPayNow.setVisibleButtonClickListener { listener?.onClick(binding!!.tvPayNow) }
    }

    fun updatePaymentMethod(resId: Int, name: String, isCard: Boolean) {
        binding!!.ivPaymentLogo.setImageResource(resId)
        binding!!.ivPaymentLogo.visibility = VISIBLE
        binding!!.tvPaymentMethodContent.text = name
        binding!!.tvPaymentMethodContent.hint = ""
        binding!!.llCardPots.visibility = if (isCard) VISIBLE else GONE
    }
}