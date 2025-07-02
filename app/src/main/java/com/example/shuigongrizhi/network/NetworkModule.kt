package com.example.shuigongrizhi.network

import com.example.shuigongrizhi.BuildConfig
import com.example.shuigongrizhi.core.Constants
import com.example.shuigongrizhi.core.Logger
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
// import javax.inject.Qualifier
// import javax.inject.Singleton

// @Qualifier
// @Retention(AnnotationRetention.BINARY)
annotation class WeatherRetrofit

// @Module
// @InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // @Provides
    // @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Logger.network(message)
        }.apply {
            level = if (BuildConfig.DEBUG_MODE) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    // @Provides
    // @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.Network.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.Network.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.Network.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request()
                Logger.network("发起网络请求: ${request.method} ${request.url}")
                val response = chain.proceed(request)
                Logger.network("网络响应: ${response.code} ${request.url}")
                response
            }
            .build()
    }
    
    // @Provides
    // @Singleton
    // @WeatherRetrofit
    fun provideWeatherRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.example.shuigongrizhi.config.ApiConfig.CAIYUN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // @Provides
    // @Singleton
    fun provideWeatherService(
        /* @WeatherRetrofit */ retrofit: Retrofit
    ): WeatherService {
        return retrofit.create(WeatherService::class.java)
    }
}