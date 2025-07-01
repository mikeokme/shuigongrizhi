package com.example.shuigongrizhi.utils

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.*

object LocationUtils {
    
    // 默认位置：徐州云龙区元和路9号
    private const val DEFAULT_LATITUDE = 34.2044
    private const val DEFAULT_LONGITUDE = 117.2857
    private const val DEFAULT_ADDRESS = "江苏省徐州市云龙区元和路9号"
    
    /**
     * 三级定位策略：
     * 1. 优先使用系统定位
     * 2. 系统定位失败时使用IP定位
     * 3. IP定位也失败时使用默认位置
     */
    suspend fun getBestLocation(context: Context): Pair<Double, Double> = withContext(Dispatchers.IO) {
        // 1. 优先用系统定位
        val systemLocation = getSystemLocation(context)
        if (systemLocation != null) {
            android.util.Log.d("LocationUtils", "使用系统定位: ${systemLocation.first}, ${systemLocation.second}")
            return@withContext systemLocation
        }
        
        // 2. 系统定位失败，尝试IP定位
        val ipLocation = getIpLocation()
        if (ipLocation != null) {
            android.util.Log.d("LocationUtils", "使用IP定位: ${ipLocation.first}, ${ipLocation.second}")
            return@withContext ipLocation
        }
        
        // 3. IP定位也失败，使用默认位置
        android.util.Log.d("LocationUtils", "使用默认位置: $DEFAULT_LATITUDE, $DEFAULT_LONGITUDE")
        return@withContext Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }
    
    /**
     * 获取系统定位
     */
    private suspend fun getSystemLocation(context: Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
            
            if (hasFine || hasCoarse) {
                val providers = lm.getProviders(true)
                var bestLocation: Location? = null
                
                for (provider in providers) {
                    val location = lm.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                        bestLocation = location
                    }
                }
                
                bestLocation?.let { 
                    return@withContext Pair(it.latitude, it.longitude)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("LocationUtils", "系统定位失败", e)
        }
        
        return@withContext null
    }
    
    /**
     * 获取IP定位
     */
    private suspend fun getIpLocation(): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val json = URL("https://ipapi.co/json/").readText()
            val obj = JSONObject(json)
            val lat = obj.optDouble("latitude")
            val lon = obj.optDouble("longitude")
            
            if (!lat.isNaN() && !lon.isNaN() && lat != 0.0 && lon != 0.0) {
                return@withContext Pair(lat, lon)
            }
        } catch (e: Exception) {
            android.util.Log.w("LocationUtils", "IP定位失败", e)
        }
        
        return@withContext null
    }
    
    /**
     * 获取默认位置信息
     */
    fun getDefaultLocation(): Pair<Double, Double> {
        return Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }
    
    /**
     * 获取默认地址
     */
    fun getDefaultAddress(): String {
        return DEFAULT_ADDRESS
    }
    
    /**
     * 根据经纬度获取地址信息
     */
    suspend fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val addressLine = address.getAddressLine(0)
                if (!addressLine.isNullOrBlank()) {
                    return@withContext addressLine
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("LocationUtils", "Geocoder获取地址失败", e)
        }
        
        // 如果是默认位置，返回默认地址
        if (latitude == DEFAULT_LATITUDE && longitude == DEFAULT_LONGITUDE) {
            return@withContext DEFAULT_ADDRESS
        }
        
        // 其他情况返回坐标格式
        return@withContext "纬度: ${String.format("%.6f", latitude)}, 经度: ${String.format("%.6f", longitude)}"
    }
    
    /**
     * 获取位置信息（包含地址）
     */
    suspend fun getLocationInfo(context: Context): LocationInfo = withContext(Dispatchers.IO) {
        val (latitude, longitude) = getBestLocation(context)
        val address = getAddressFromLatLng(context, latitude, longitude)
        
        return@withContext LocationInfo(
            latitude = latitude,
            longitude = longitude,
            address = address
        )
    }
    
    /**
     * 检查是否为默认位置
     */
    fun isDefaultLocation(latitude: Double, longitude: Double): Boolean {
        return latitude == DEFAULT_LATITUDE && longitude == DEFAULT_LONGITUDE
    }
    
    /**
     * 位置信息数据类
     */
    data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val address: String
    )
}