package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.data.api.CourseSyllabusDto
import com.wenha.cefrenglish.data.api.RegenerateSyllabusRequestDto
import com.wenha.cefrenglish.domain.CourseSyllabus
import com.wenha.cefrenglish.domain.SyllabusModule

interface SyllabusRepository {
    suspend fun fetchSyllabus(level: String): CourseSyllabus
    suspend fun regenerateSyllabus(level: String): CourseSyllabus
}

class NetworkSyllabusRepository(private val api: AppApi) : SyllabusRepository {
    override suspend fun fetchSyllabus(level: String): CourseSyllabus {
        return api.fetchSyllabus(level).toDomain()
    }

    override suspend fun regenerateSyllabus(level: String): CourseSyllabus {
        return api.regenerateSyllabus(RegenerateSyllabusRequestDto(level)).toDomain()
    }
}

private fun CourseSyllabusDto.toDomain(): CourseSyllabus =
    CourseSyllabus(
        level = level,
        title = title,
        description = description,
        modules = modules.map {
            SyllabusModule(
                title = it.title,
                goal = it.goal,
                lessons = it.lessons,
            )
        },
    )
