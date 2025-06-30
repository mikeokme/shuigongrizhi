package com.example.shuigongrizhi.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object LocationUtils {
    suspend fun getBestLocation(context: Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        // 1. 优先用系统定位
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            val providers = lm.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = lm.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            bestLocation?.let { return@withContext Pair(it.latitude, it.longitude) }
        }
        // 2. 用IP定位
        try {
            val json = URL("https://ipapi.co/json/").readText()
            val obj = JSONObject(json)
            val lat = obj.optDouble("latitude")
            val lon = obj.optDouble("longitude")
            if (!lat.isNaN() && !lon.isNaN()) return@withContext Pair(lat, lon)
        } catch (_: Exception) {}
        // 3. 都失败
        null
    }
} 