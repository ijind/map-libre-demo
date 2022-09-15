package ai.txai.setting.view

import ai.txai.common.mvvm.BaseMvvmFragment
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.common.widget.popupview.CustomerServiceView
import ai.txai.setting.R
import ai.txai.setting.adapter.ServiceHelperAdapter
import ai.txai.setting.databinding.SettingServiceHelperLayoutBinding
import ai.txai.setting.viewmodel.SettingViewModel
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.NetworkUtils

class ServiceHelperFragment:
    BaseMvvmFragment<SettingServiceHelperLayoutBinding, SettingViewModel>() {

    private lateinit var customerView: CustomerServiceView

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SettingServiceHelperLayoutBinding {
        return SettingServiceHelperLayoutBinding.inflate(inflater, container, false)
    }

    override fun initViewObservable() {
        customerView = CustomerServiceView(requireActivity())
        binding.contentRecycler.layoutManager = LinearLayoutManager(context)
        val adapter = createAdapter(binding.contentRecycler)
        binding.contentRecycler.adapter = adapter
        showLoading("")
        viewModel.loadLegalArticles(SettingViewModel.SERVICE_AND_HELP, true)
        viewModel.getArticles().observe(this) {
            hideLoading()
            adapter.data = it
        }
        initView()
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        binding.statusView.visibility = View.GONE
        binding.statusView.loadSuccess()
        binding.settingServiceTitleTv.visibility = View.GONE
    }

    override fun onDisconnected() {
        binding.statusView.visibility = View.VISIBLE
        binding.statusView.loadError()
        binding.settingServiceTitleTv.visibility = View.VISIBLE
        binding.settingServiceTitleTv.setTextColor(Color.parseColor("#040818"))
    }

    private fun initView() {
        binding.settingServiceBackImg.setDebounceClickListener {
            activity?.finish()
        }
        binding.customerServiceLayout.setPositiveClickListener {
            customerView.showPop()
        }
    }

    private fun createAdapter(recyclerView: RecyclerView): ServiceHelperAdapter {
        val adapter = ServiceHelperAdapter(requireContext())
        adapter.setOnParallaxScroll { percentage, _, _ ->
            if (percentage >= 0.7) {
                val color = ColorUtils.setAlphaComponent(Color.parseColor("#040818"), percentage)
                binding.settingServiceTitleTv.setTextColor(color)
            } else {
                binding.settingServiceTitleTv.setTextColor(Color.parseColor("#00040818"))
            }
        }
        val header = layoutInflater.inflate(R.layout.common_recycler_header, recyclerView, false)
        val titleTv = header.findViewById<TextView>(R.id.title_tv)
        titleTv.setText(R.string.setting_service_helper_title)
        val lp = titleTv.layoutParams as ViewGroup.MarginLayoutParams
        lp.leftMargin = AndroidUtils.dip2px(requireContext(), 20f)
        titleTv.layoutParams = lp

        val spaceView = header.findViewById<View>(R.id.space_holder_view)
        spaceView.visibility = View.VISIBLE
        adapter.setParallaxHeader(header, recyclerView)
        return adapter
    }
}