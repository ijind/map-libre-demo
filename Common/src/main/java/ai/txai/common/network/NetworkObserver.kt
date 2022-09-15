package ai.txai.common.network

import com.blankj.utilcode.util.NetworkUtils

interface NetworkObserver: NetworkUtils.OnNetworkStatusChangedListener {

    fun onRefresh()

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
    }

    override fun onDisconnected() {
    }
}