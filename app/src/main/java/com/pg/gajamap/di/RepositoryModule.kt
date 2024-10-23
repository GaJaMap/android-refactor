package com.pg.gajamap.di

import com.pg.gajamap.data.repository.ClientRepository
import com.pg.gajamap.data.repository.ClientRepositoryImpl
import com.pg.gajamap.data.repository.GroupRepository
import com.pg.gajamap.data.repository.GroupRepositoryImpl
import com.pg.gajamap.data.repository.KakaoRepository
import com.pg.gajamap.data.repository.KakaoRepositoryImpl
import com.pg.gajamap.data.repository.MemoRepository
import com.pg.gajamap.data.repository.MemoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun GroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl,
    ): GroupRepository

    @Singleton
    @Binds
    abstract fun ClientRepository(
        clientRepositoryImpl: ClientRepositoryImpl,
    ): ClientRepository

    @Singleton
    @Binds
    abstract fun KakaoRepository(
        kakaoRepositoryImpl: KakaoRepositoryImpl,
    ): KakaoRepository

    @Singleton
    @Binds
    abstract fun MemoRepository(
        memoRepositoryImpl: MemoRepositoryImpl,
    ): MemoRepository

}