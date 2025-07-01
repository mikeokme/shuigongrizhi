package com.example.shuigongrizhi.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络状态监听器
 * 提供实时的网络连接状态监控
 */
@Singleton
class NetworkMonitor @Inject constructor(
    private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * 网络状态Flow
     */
    val networkState: Flow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Logger.network("网络连接可用: $network")
                trySend(getCurrentNetworkState())
            }
            
            override fun onLost(network: Network) {
                Logger.network("网络连接丢失: $network")
                trySend(getCurrentNetworkState())
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Logger.network("网络能力变化: $network")
                trySend(getCurrentNetworkState())
            }
        }
        
        // 注册网络回调
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
        }
        
        // 发送初始状态
        trySend(getCurrentNetworkState())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * 获取当前网络状态
     */
    fun getCurrentNetworkState(): NetworkState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
            
            when {
                capabilities == null -> NetworkState.Disconnected
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    NetworkState.Connected(NetworkType.WIFI)
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    NetworkState.Connected(NetworkType.CELLULAR)
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    NetworkState.Connected(NetworkType.ETHERNET)
                }
                else -> NetworkState.Connected(NetworkType.OTHER)
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo?.isConnected == true) {
                val type = when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
                    ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
                    else -> NetworkType.OTHER
                }
                NetworkState.Connected(type)
            } else {
                NetworkState.Disconnected
            }
        }
    }
    
    /**
     * 检查网络是否可用
     */
    fun isNetworkAvailable(): Boolean {
        return getCurrentNetworkState() is NetworkState.Connected
    }
    
    /**
     * 获取网络类型
     */
    fun getNetworkType(): NetworkType? {
        val state = getCurrentNetworkState()
        return if (state is NetworkState.Connected) state.type else null
    }
    
    /**
     * 检查是否为计费网络
     */
    fun isMeteredNetwork(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.isActiveNetworkMetered
        } else {
            // 对于旧版本，假设移动网络为计费网络
            getNetworkType() == NetworkType.CELLULAR
        }
    }
    
    /**
     * 获取网络强度（仅WiFi）
     */
    fun getWifiSignalStrength(): Int? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork
            val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
            capabilities?.signalStrength
        } else {
            null
        }
    }
}

/**
 * 网络状态密封类
 */
sealed class NetworkState {
    object Disconnected : NetworkState()
    data class Connected(val type: NetworkType) : NetworkState()
    
    val isConnected: Boolean
        get() = this is Connected
    
    val isDisconnected: Boolean
        get() = this is Disconnected
    
    fun getDisplayName(): String {
        return when (this) {
            is Disconnected -> "无网络连接"
            is Connected -> when (type) {
                NetworkType.WIFI -> "WiFi"
                NetworkType.CELLULAR -> "移动网络"
                NetworkType.ETHERNET -> "以太网"
                NetworkType.OTHER -> "其他网络"
            }
        }
    }
}

/**
 * 网络类型枚举
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER
}

/**
 * 网络状态扩展函数
 */
fun NetworkState.isFastConnection(): Boolean {
    return when (this) {
        is NetworkState.Disconnected -> false
        is NetworkState.Connected -> when (type) {
            NetworkType.WIFI, NetworkType.ETHERNET -> true
            NetworkType.CELLULAR -> true // 现代移动网络通常足够快
            NetworkType.OTHER -> false
        }
    }
}

fun NetworkState.isSlowConnection(): Boolean {
    return !isFastConnection() && isConnected
}

fun NetworkState.shouldShowDataWarning(): Boolean {
    return this is NetworkState.Connected && type == NetworkType.CELLULAR
}