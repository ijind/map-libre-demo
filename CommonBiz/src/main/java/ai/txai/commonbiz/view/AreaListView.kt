package ai.txai.commonbiz.view

import ai.txai.common.glide.GlideUtils.loadImage
import ai.txai.common.glide.ImageOptions
import ai.txai.common.utils.AndroidUtils
import ai.txai.commonbiz.R
import ai.txai.commonbiz.bean.AreaResponse
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition

class AreaListView(activity: Context) : BottomPopupView(activity) {
    private lateinit var adapter : MyAdapter

    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .popupPosition(PopupPosition.Bottom)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.bottom_service_detail
    }

    override fun onCreate() {
        super.onCreate()
        initView(context)
    }

    override fun getMaxHeight(): Int {
        val defaultHeight = 533f
        return AndroidUtils.dip2px(context, defaultHeight)
    }

    private fun initView(context: Context) {
        initTitle()
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(getContext())
        adapter = MyAdapter(getContext())
        recyclerView.adapter = adapter
        findViewById<View>(R.id.iv_cancel).setOnClickListener {
            dismissPop()
        }
    }

    private fun initTitle() {
        val tvCancelTitle = findViewById<TextView>(R.id.tv_title)
        tvCancelTitle.setText(R.string.biz_service_detail_title)
        tvCancelTitle.viewTreeObserver.addOnGlobalLayoutListener(object:
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                tvCancelTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val lp = tvCancelTitle.layoutParams as ConstraintLayout.LayoutParams
                lp.endToEnd = R.id.bottom_pop_title_layout
                lp.marginStart = SizeUtils.dp2px(16f)
                tvCancelTitle.layoutParams = lp
            }
        })
    }

    fun updateArea(areaResponses: List<AreaResponse>) {
        adapter.mItems.clear()
        adapter.mItems.addAll(areaResponses)
        adapter.notifyDataSetChanged()
    }

    internal class MyAdapter(private val context: Context) : RecyclerView.Adapter<MyHolder>() {
        val mItems: MutableList<AreaResponse> = mutableListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.biz_item_area_service_detail, parent, false)
            return MyHolder(view)
        }

        override fun onBindViewHolder(holder: MyHolder, position: Int) {
            val item = mItems[position]
            holder.bindData(item, position, mItems.size)
        }

        override fun getItemCount(): Int {
            return mItems.size
        }
    }

    internal class MyHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindData(item: AreaResponse, pos: Int, size: Int) {
            var tvAreaDesc = itemView.findViewById<TextView>(R.id.tv_area_desc)
            var tvHoursDesc = itemView.findViewById<TextView>(R.id.tv_hours_desc)
            val bottomLine = itemView.findViewById<View>(R.id.bottom_line)
            val ivAreaBig = itemView.findViewById<ImageView>(R.id.iv_area_big)
            tvAreaDesc.text = item.name
            tvHoursDesc.text = String.format(
                "%s - %s",
                item.opStartTime,
                item.opEndTime
            )

            val options = ImageOptions.Builder()
                .setImageUrl(item.picture)
                .setPlaceHolderResId(0)
                .build()
            ivAreaBig.loadImage(AndroidUtils.getApplicationContext(), options)
            if (pos == size - 1) { bottomLine.visibility = View.GONE}
        }

    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }
}