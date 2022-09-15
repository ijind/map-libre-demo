package ai.txai.commonbiz.view

import ai.txai.common.utils.FormatUtils.buildTimeGap
import ai.txai.common.utils.AndroidUtils.buildAmount
import android.widget.LinearLayout
import ai.txai.commonbiz.entries.RequestRideItem
import android.view.LayoutInflater
import ai.txai.database.vehicle.VehicleModel
import ai.txai.commonbiz.bean.TripPlanResponse
import android.view.ViewGroup
import ai.txai.commonbiz.R
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.commonbiz.databinding.BizItemSelectVehicleModelBinding
import ai.txai.commonbiz.databinding.BottomMainRequestRideBinding
import android.content.Context
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

/**
 * Time: 2/24/22
 * Author Hay
 */
class RequestRideView : LinearLayout, IBottomView {
    val itemsCache: MutableList<RequestRideItem> = ArrayList()
    private lateinit var binding: BottomMainRequestRideBinding
    private var adapter: MyAdapter? = null
    var onItemClickListener: OnItemClickListener? = null
    private var defaultPosition = -1

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        binding = BottomMainRequestRideBinding.inflate(LayoutInflater.from(context), this, false)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(getContext())
        adapter = MyAdapter(getContext())
        binding.recyclerView.adapter = adapter
        addView(binding.root)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.buttonRequestCar.setVisibleButtonClickListener { l?.onClick(binding.buttonRequestCar) }
    }

    fun updateModel(responses: List<VehicleModel>) {
        itemsCache.clear()
        for (i in responses.indices) {
            val resp = responses[i]
            val item = RequestRideItem()
            item.vehicleModel = resp
            item.setvModelId(resp.id + "")
            itemsCache.add(item)
        }
    }

    fun defaultTrip(position: Int) {
        this.defaultPosition = position
    }

    fun updateTrips(tripPlans: Map<String?, TripPlanResponse>): RequestRideItem? {
        for (value in tripPlans.values) {
            for (i in itemsCache.indices) {
                val requestRideItem = itemsCache[i]
                if (TextUtils.equals(value.vehicleTypeId, requestRideItem.getvModelId())) {
                    requestRideItem.tripPlan = value
                }
            }
        }
        val items: MutableList<RequestRideItem> = ArrayList()
        for (i in itemsCache.indices) {
            val requestRideItem = itemsCache[i]
            if (requestRideItem.tripPlan != null) {
                items.add(requestRideItem)
            }
        }
        if (items.isNotEmpty()) {
            adapter!!.mItems.clear()
            adapter!!.mItems.addAll(items)
            val defaultPosition = 0
            defaultTrip(defaultPosition)
            adapter!!.notifyDataSetChanged()
            return items[defaultPosition]
        }
        return null
    }

    protected inner class MyAdapter(private val mContext: Context) :
        RecyclerView.Adapter<MyHolder>() {
        val mItems: MutableList<RequestRideItem> = ArrayList()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            val binding = BizItemSelectVehicleModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MyHolder(binding)
        }

        override fun onBindViewHolder(holder: MyHolder, position: Int) {
            val item = mItems[position]
            holder.bindData(item, position, mItems.size == 1)
        }

        override fun getItemCount(): Int {
            return mItems.size
        }
    }

    inner class MyHolder(private val itemBinding: BizItemSelectVehicleModelBinding) :
        RecyclerView.ViewHolder(
            itemBinding.root
        ) {
        fun bindData(item: RequestRideItem, pos: Int, hasSingleItem: Boolean) {
            itemBinding.hasDiscountLayout.tvDiscountAmount.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            if (hasSingleItem) {
                itemBinding.ivSelectedBox.visibility = GONE
            } else {
                itemBinding.ivSelectedBox.visibility = VISIBLE
            }
            if (pos == defaultPosition) {
                post {
                    onItemClickListener?.onItemSelected(itemBinding.root, item)
                }
                itemBinding.ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_selected)
                itemBinding.root.setBackgroundResource(R.drawable.biz_bottom_trip_selected_bg)
                itemBinding.hasDiscountLayout.ivInfo.visibility = VISIBLE
                itemBinding.noDiscountLayout.ivInfo.visibility = VISIBLE
            } else {
                itemBinding.ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_default)
                itemBinding.root.setBackgroundResource(R.drawable.biz_bottom_trip_unselected_bg)
                itemBinding.hasDiscountLayout.ivInfo.visibility = INVISIBLE
                itemBinding.noDiscountLayout.ivInfo.visibility = INVISIBLE
            }
            val vehicleModel = item.vehicleModel
            if (vehicleModel != null) {
                itemBinding.nameCar.text = vehicleModel.name
                itemBinding.tvCountSeats.text = vehicleModel.maxPassengerNum.toString() + ""
                itemBinding.tvDiscountType.text = vehicleModel.description
            }
            val tripPlan = item.tripPlan
            if (tripPlan != null) {
                if (tripPlan.estimateTripInfo != null) {
                    itemBinding.tvEstMinutes.text = buildTimeGap(tripPlan.estimateTripInfo.eta)
                }
                if (tripPlan.fareInfo != null) {
                    val actPay = tripPlan.fareInfo.totalFare - tripPlan.fareInfo.discountFare
                    if (tripPlan.fareInfo.discountFare == 0.0) {
                        itemBinding.hasDiscountLayout.root.visibility = GONE
                        itemBinding.noDiscountLayout.root.visibility = VISIBLE
                        itemBinding.noDiscountLayout.tvAmount.text = buildAmount(actPay)
                        itemBinding.noDiscountLayout.ivInfo.setOnClickListener {
                            if (onItemClickListener != null) {
                                onItemClickListener!!.onItemInfoClick(itemBinding.root, item)
                            }
                        }
                    } else {
                        itemBinding.hasDiscountLayout.root.visibility = VISIBLE
                        itemBinding.noDiscountLayout.root.visibility = GONE
                        if (actPay == 0.0) {
                            itemBinding.hasDiscountLayout.tvDiscountLevel.visibility = VISIBLE
                        } else {
                            itemBinding.hasDiscountLayout.tvDiscountLevel.visibility = GONE
                        }
                        itemBinding.hasDiscountLayout.tvDiscountAmount.text = context.getString(
                            R.string.biz_aed_with_amount,
                            buildAmount(tripPlan.fareInfo.discountFare)
                        )
                        itemBinding.hasDiscountLayout.tvAmount.text = buildAmount(actPay)
                        itemBinding.hasDiscountLayout.ivInfo.setOnClickListener {
                            if (onItemClickListener != null) {
                                onItemClickListener!!.onItemInfoClick(itemBinding.root, item)
                            }
                        }
                    }
                }
            }
            itemBinding.root.setOnClickListener { v: View? ->
                defaultPosition = pos
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    fun reSelectItem() {
        if (defaultPosition >= 0 && adapter!!.mItems.size > defaultPosition) {
            onItemClickListener?.onItemSelected(null, adapter!!.mItems[defaultPosition])
        }
    }

    interface OnItemClickListener {
        fun onItemSelected(v: View?, item: RequestRideItem?)
        fun onItemInfoClick(v: View?, item: RequestRideItem?)
    }
}