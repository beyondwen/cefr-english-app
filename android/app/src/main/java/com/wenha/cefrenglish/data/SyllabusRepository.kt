package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.domain.CourseSyllabus
import com.wenha.cefrenglish.domain.SyllabusModule

interface SyllabusRepository {
    suspend fun fetchSyllabus(level: String): CourseSyllabus
}

class NetworkSyllabusRepository(private val api: AppApi) : SyllabusRepository {
    override suspend fun fetchSyllabus(level: String): CourseSyllabus {
        val response = api.fetchSyllabus(level)
        return CourseSyllabus(
            level = response.level,
            title = response.title,
            description = response.description,
            modules = response.modules.map {
                SyllabusModule(
                    title = it.title,
                    goal = it.goal,
                    lessons = it.lessons,
                )
            },
        )
    }
}
