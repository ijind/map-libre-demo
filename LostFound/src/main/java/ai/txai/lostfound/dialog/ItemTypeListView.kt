package ai.txai.lostfound.dialog

import ai.txai.common.utils.setDebounceClickListener
import ai.txai.lostfound.R
import ai.txai.lostfound.bean.ItemTypeEntry
import ai.txai.lostfound.databinding.LfItemTypeSelectBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ThreadUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition

class ItemTypeListView(activity: Context, val listener: OnItemClickListener) :
    BottomPopupView(activity) {
    private lateinit var adapter: MyAdapter

    var defaultPosition = -1
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .popupPosition(PopupPosition.Bottom)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.lf_bottom_dialog_item_select
    }

    override fun onCreate() {
        super.onCreate()
        initView(context)
    }

    private fun cancelClickListener() {
        var cancelView = findViewById<View>(R.id.iv_cancel)
        cancelView.setDebounceClickListener {
            dismissPop()
        }
    }

    private fun initView(context: Context) {
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(getContext())
        adapter = MyAdapter(getContext())
        recyclerView.adapter = adapter

        val title = resources.getString(R.string.lf_lost_item_type)
        val titleInclude = findViewById<View>(R.id.item_include)
        val titleView = titleInclude.findViewById<TextView>(R.id.tv_title)
        titleView.text = title

        cancelClickListener()
    }

    fun updateItemTypes(areaResponses: List<ItemTypeEntry>) {
        adapter.mItems.clear()
        adapter.mItems.addAll(areaResponses)
        adapter.notifyDataSetChanged()
    }

    inner class MyAdapter(private val context: Context) : RecyclerView.Adapter<MyHolder>() {
        val mItems: MutableList<ItemTypeEntry> = mutableListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.lf_item_type_select, parent, false)
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

    inner class MyHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bindData(item: ItemTypeEntry, pos: Int, size: Int) {
            var itemBinding = LfItemTypeSelectBinding.bind(itemView)
            itemBinding.tvItemTypeName.text = item.name
            if (pos == defaultPosition) {
                itemBinding.ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_selected)
            } else {
                itemBinding.ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_default)
            }
            itemView.setOnClickListener {
                defaultPosition = pos
                listener.onItemSelected(item)
                adapter.notifyDataSetChanged()
                ThreadUtils.runOnUiThreadDelayed({ dismissPop() }, 10)
            }
            if (pos == size - 1) {
                itemBinding.itemTypeLine.visibility = View.GONE
            }
        }
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }

    interface OnItemClickListener {
        fun onItemSelected(item: ItemTypeEntry?)
    }
}