package ai.txai.common.widget.popupview

import ai.txai.common.R
import ai.txai.common.api.ApiConfig
import ai.txai.common.api.ApiServerManager
import ai.txai.common.databinding.CommonChangeEnvItemBinding
import ai.txai.common.manager.LifeCycleManager
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ARouterUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.common.widget.txaiButton.TxaiButton
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition

class ChangeEnvView(activity: Context) : BottomPopupView(activity) {
    private lateinit var adapter: MyAdapter
    private var baseUrl = ApiConfig.baseUrl
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .dismissOnTouchOutside(false)
        .popupPosition(PopupPosition.Bottom)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.common_change_env_layout
    }

    override fun onCreate() {
        super.onCreate()
        initView(context)
    }

    private fun initView(context: Context) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(getContext())
        adapter = MyAdapter(getContext())
        recyclerView.adapter = adapter

        val envBtn = findViewById<TxaiButton>(R.id.env_btn)
        envBtn.setPositiveClickListener {
            if (baseUrl == ApiConfig.baseUrl) {
                ToastUtils.show("Not change Url", false)
            } else {
                ApiConfig.baseUrl = baseUrl
                ApiServerManager.clearServerCache()
                val path = ARouterConstants.PATH_ACTIVITY_LOGIN
                val flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                LifeCycleManager.getCurActivity()
                    ?.let { ARouterUtils.navigation(it, flags, path, Bundle()) }
            }
        }

        envBtn.setNegativeClickListener {
            if (baseUrl == ApiConfig.baseUrl) {
                dismissPop()
            }
        }

        val items = ArrayList<Pair<String, String>>()
        items.add(Pair("Develop", ApiConfig.DEV_URL))
        items.add(Pair("Test", ApiConfig.TEST_URL))
        updateArea(items)
    }

    fun updateArea(items: List<Pair<String, String>>) {
        adapter.mItems.clear()
        adapter.mItems.addAll(items)
        adapter.notifyDataSetChanged()
    }

    inner class MyAdapter(private val context: Context) : RecyclerView.Adapter<MyHolder>() {
        val mItems: MutableList<Pair<String, String>> = mutableListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.common_change_env_item, parent, false)
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
        fun bindData(item: Pair<String, String>, pos: Int, size: Int) {
            var bind = CommonChangeEnvItemBinding.bind(itemView)
            bind.ctvUrlName.text = item.first
            bind.tvUrlContent.text = item.second

            if (item.second == baseUrl) {
                bind.ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_selected)
            } else {
                bind.ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_default)
            }

            itemView.setOnClickListener {
                baseUrl = item.second
                adapter.notifyDataSetChanged()
            }
        }

    }

    fun showPop() {
        if(popupView.isDismiss) {
            popupView.show()
        }
    }

    fun dismissPop() {
        if(!popupView.isDismiss) {
            popupView.dismiss()
        }
    }
}