package com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection

import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryObject {

    @Singleton
    @Provides
    fun provideRepository(): Repository {
        return Repository()
    }
}