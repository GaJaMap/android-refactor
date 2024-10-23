package com.pg.gajamap.di

import android.content.Context
import com.kakao.sdk.user.UserApiClient
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.service.AuthService
import com.pg.gajamap.data.service.KakaoAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object LoginModule {
    @Provides
    @ActivityScoped
    fun provideUserApiClient(): UserApiClient = UserApiClient.instance

    @Provides
    fun provideKakaoAuthService(
        @ActivityContext context: Context,
        client: UserApiClient,
        sharedPreferences: GJMSharedPreference,
        authService: AuthService,
    ) = KakaoAuthService(context, client, sharedPreferences, authService)
}