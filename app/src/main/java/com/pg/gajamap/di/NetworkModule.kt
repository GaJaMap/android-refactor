package com.pg.gajamap.di

import android.app.Application
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.manager.AuthInterceptor
import com.pg.gajamap.data.manager.CookieManager
import com.pg.gajamap.util.Constants.GJM_BASE_URL
import com.pg.gajamap.util.Constants.KAKAO_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GJM

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KAKAO

    @Provides
    @Singleton
    fun provideAuthInterceptor(storage: GJMSharedPreference, context: Application): Interceptor {
        return AuthInterceptor(storage, context)
    }
    
    @Provides
    @Singleton
    fun provideCookieManager(storage: GJMSharedPreference): CookieJar {
        return CookieManager(storage)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        interceptor: AuthInterceptor,
        cookieManager: CookieManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .cookieJar(cookieManager)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()
    }

    @GJM
    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(GJM_BASE_URL)
            .build()
    }

    @KAKAO
    @Singleton
    @Provides
    fun provideKakaoOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()
    }

    @KAKAO
    @Singleton
    @Provides
    fun provideKakaoRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(KAKAO_BASE_URL)
            .build()
    }
}