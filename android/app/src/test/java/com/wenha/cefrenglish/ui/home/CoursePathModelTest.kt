package com.wenha.cefrenglish.ui.home

import com.wenha.cefrenglish.domain.CourseSyllabus
import com.wenha.cefrenglish.domain.SyllabusModule
import org.junit.Assert.assertEquals
import org.junit.Test

class CoursePathModelTest {
    @Test
    fun buildCoursePath_marksCompletedCurrentAvailableAndLockedModules() {
        val syllabus = CourseSyllabus(
            level = "A1",
            title = "A1",
            description = "A1 path",
            modules = listOf(
                SyllabusModule("Module 1", "Goal 1", listOf("L1", "L2", "L3")),
                SyllabusModule("Module 2", "Goal 2", listOf("L4", "L5", "L6")),
                SyllabusModule("Module 3", "Goal 3", listOf("L7", "L8", "L9")),
                SyllabusModule("Module 4", "Goal 4", listOf("L10", "L11", "L12")),
            ),
        )

        val items = buildCoursePathItems(
            syllabus = syllabus,
            currentLevel = "A1",
            currentLessonId = "A1-05",
        )

        assertEquals(CoursePathStatus.Completed, items[0].status)
        assertEquals(CoursePathStatus.Current, items[1].status)
        assertEquals(CoursePathStatus.Available, items[2].status)
        assertEquals(CoursePathStatus.Locked, items[3].status)
    }

    @Test
    fun buildCoursePath_treatsOtherLevelsAsAvailablePreview() {
        val syllabus = CourseSyllabus(
            level = "B1",
            title = "B1",
            description = "B1 path",
            modules = listOf(
                SyllabusModule("Module 1", "Goal 1", listOf("L1")),
                SyllabusModule("Module 2", "Goal 2", listOf("L2")),
            ),
        )

        val items = buildCoursePathItems(
            syllabus = syllabus,
            currentLevel = "A1",
            currentLessonId = "A1-01",
        )

        assertEquals(listOf(CoursePathStatus.Available, CoursePathStatus.Available), items.map { it.status })
    }

    @Test
    fun buildCoursePath_marksLessonsAroundCurrentLesson() {
        val syllabus = CourseSyllabus(
            level = "A1",
            title = "A1",
            description = "A1 path",
            modules = listOf(
                SyllabusModule("Module 1", "Goal 1", listOf("L1", "L2", "L3")),
                SyllabusModule("Module 2", "Goal 2", listOf("L4", "L5", "L6")),
                SyllabusModule("Module 3", "Goal 3", listOf("L7", "L8", "L9")),
            ),
        )

        val items = buildCoursePathItems(
            syllabus = syllabus,
            currentLevel = "A1",
            currentLessonId = "A1-05",
        )

        assertEquals(
            listOf(CoursePathStatus.Completed, CoursePathStatus.Completed, CoursePathStatus.Completed),
            items[0].lessons.map { it.status },
        )
        assertEquals(
            listOf(CoursePathStatus.Completed, CoursePathStatus.Current, CoursePathStatus.Available),
            items[1].lessons.map { it.status },
        )
        assertEquals(
            listOf(CoursePathStatus.Locked, CoursePathStatus.Locked, CoursePathStatus.Locked),
            items[2].lessons.map { it.status },
        )
    }
}
