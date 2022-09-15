package ai.txai.commonbiz.base

import ai.txai.common.widget.DragView
import ai.txai.commonbiz.R
import ai.txai.commonbiz.databinding.BizItemPrecautionsBinding
import ai.txai.commonbiz.databinding.BizScrollPrecautionsLayoutBinding
import ai.txai.commonbiz.utils.ViewHelper
import ai.txai.commonview.NoLineClickSpan
import ai.txai.commonview.items.NoLineItem
import ai.txai.database.site.Site
import ai.txai.database.vehicle.Vehicle
import ai.txai.database.vehicle.VehicleModel
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.blankj.utilcode.util.ScreenUtils

abstract class DragBottomView: LinearLayout {
    lateinit var binding: BizScrollPrecautionsLayoutBinding

    private val tvNoticeList = ArrayList<TextView>()
    private val tvNumberList = ArrayList<TextView>()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
            super(context, attrs, defStyleAttr) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes)

    open fun initView() {
        binding = BizScrollPrecautionsLayoutBinding.
        inflate(LayoutInflater.from(context), this, false)
        binding.carStatusContainer.tvCancelTitle.text = getTitleName()
        initScrollView()
        addView(binding.root)
        binding.precautionsContainer.precautionsLayout.viewTreeObserver
            .addOnGlobalLayoutListener {
                setViewBackground()
                updateItemViewMargin()
            }
    }

    private fun initScrollView() {
        val container = binding.precautionsContainer
        container.tvOk.visibility = View.GONE
        updateItemView(1, R.string.biz_precautions_item_1, container.llNotice1)
        updateItemView(2, R.string.biz_precautions_item_2, container.llNotice2)
        updateItemView(3, R.string.biz_precautions_item_3, container.llNotice3)
        updateItemView(4, R.string.biz_precautions_item_4, container.llNotice4)
        updateItemView(5, R.string.biz_precautions_item_5, container.llNotice5)
        updateItemView(6, R.string.biz_precautions_item_6, container.llNotice6)
    }

    private fun updateItemView(number: Int, res: Int, notice: BizItemPrecautionsBinding) {
        notice.number.text = number.toString()
        notice.notice.setText(res)
        tvNoticeList.add(notice.notice)
        tvNumberList.add(notice.number)
    }

    private fun setViewBackground() {
        val drawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.commonview_all_corner_shape_10, null
        )
        binding.carStatusContainer.carStatusLayout.background = drawable
        binding.precautionsContainer.precautionsLayout.background = drawable
    }

    private fun updateItemViewMargin() {
        for (i in tvNoticeList.indices) {
            val tvNotice = tvNoticeList[i]
            val tvNumber = tvNumberList[i]
            if (tvNotice.lineCount >= 2) {
                val lp = tvNumber.layoutParams as MarginLayoutParams
                lp.topMargin = (tvNotice.measuredHeight - tvNumber.measuredHeight) / 2
                tvNumber.layoutParams = lp
            }
        }
        tvNoticeList.clear()
        tvNumberList.clear()
    }

    fun setCancelOnClickListener(listener: OnClickListener?) {
        listener?.let { binding.carStatusContainer.tvCancelText.setNegativeClickListener(it) }
    }

    fun setOnDragStatusListener(listener: DragView.OnViewStatusChangeListener) {
        binding.dragTripView.setViewStatusChangeListener(listener)
    }

    fun updateVehicle(value: Vehicle?) {
        if (value == null) {
            return
        }
        binding.carStatusContainer.carName.text = value.brand
        binding.carStatusContainer.carColor.text = value.color
        binding.carStatusContainer.carNumber.text = value.plateNo
    }

    fun updateTo(site: Site) {
        val item = NoLineItem(R.color.commonview_orange_00, site.name, null)
        NoLineClickSpan.setClickableSpan(
            binding.carStatusContainer.description, context, getDescription(site), item)
    }

    fun updateVehicleModelName(modelId: String?) {
        modelId?.let {
            ViewHelper.updateVehicleModelLabel(binding.carStatusContainer.tvTaxiName, it)
        }
    }

    fun defaultHeight(): Int {
        return binding.dragTripView.defaultHeight().toInt()
    }

    protected abstract fun getTitleName(): String

    protected abstract fun getDescription(site: Site): String

    interface OnDragStatusListener {
        fun onAlphaChanged(y: Float)

        fun onTranslationChange(translationY: Float)
    }
}