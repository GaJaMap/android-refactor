package com.pg.gajamap.di

import com.pg.gajamap.data.service.AuthService
import com.pg.gajamap.data.service.ClientService
import com.pg.gajamap.data.service.GroupService
import com.pg.gajamap.data.service.KakaoService
import com.pg.gajamap.data.service.MemoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun provideAuthAPIService(@NetworkModule.GJM retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideGroupAPIService(@NetworkModule.GJM retrofit: Retrofit): GroupService {
        return retrofit.create(GroupService::class.java)
    }

    @Singleton
    @Provides
    fun provideClientAPIService(@NetworkModule.GJM retrofit: Retrofit): ClientService {
        return retrofit.create(ClientService::class.java)
    }

    @Singleton
    @Provides
    fun provideMemoAPIService(@NetworkModule.GJM retrofit: Retrofit): MemoService {
        return retrofit.create(MemoService::class.java)
    }

    @Singleton
    @Provides
    fun provideKakaoAPIService(@NetworkModule.KAKAO retrofit: Retrofit): KakaoService {
        return retrofit.create(KakaoService::class.java)
    }
}