package com.wenha.cefrenglish.app

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wenha.cefrenglish.data.LessonRepository
import com.wenha.cefrenglish.data.NetworkLessonRepository
import com.wenha.cefrenglish.data.NetworkPlacementRepository
import com.wenha.cefrenglish.data.NetworkProgressRepository
import com.wenha.cefrenglish.data.PlacementRepository
import com.wenha.cefrenglish.data.ProgressRepository
import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.data.store.UserPrefsStore
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val userPrefsStore = UserPrefsStore(context)
    private val api: AppApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            ),
        )
        .build()
        .create(AppApi::class.java)

    val placementRepository: PlacementRepository = NetworkPlacementRepository(api, userPrefsStore)
    val lessonRepository: LessonRepository = NetworkLessonRepository(api)
    val progressRepository: ProgressRepository = NetworkProgressRepository(api)

    suspend fun getUserId(): String = userPrefsStore.getOrCreateUserId()

    companion object {
        const val BASE_URL = "http://10.0.2.2:8787/"
    }
}
