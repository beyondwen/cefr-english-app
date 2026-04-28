package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.SyllabusRepository
import com.wenha.cefrenglish.domain.CourseSyllabus
import com.wenha.cefrenglish.domain.SyllabusModule

class FakeSyllabusRepository : SyllabusRepository {
    override suspend fun fetchSyllabus(level: String): CourseSyllabus =
        CourseSyllabus(
            level = level,
            title = "$level syllabus",
            description = "Test syllabus",
            modules = listOf(SyllabusModule("Module 1", "Goal", listOf("Lesson 1"))),
        )

    override suspend fun regenerateSyllabus(level: String): CourseSyllabus = fetchSyllabus(level)
}
