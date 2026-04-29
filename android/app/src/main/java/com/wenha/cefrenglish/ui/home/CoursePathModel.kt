package com.wenha.cefrenglish.ui.home

import com.wenha.cefrenglish.domain.CourseSyllabus
import com.wenha.cefrenglish.domain.SyllabusModule

enum class CoursePathStatus {
    Completed,
    Current,
    Available,
    Locked,
}

data class CoursePathItem(
    val index: Int,
    val module: SyllabusModule,
    val status: CoursePathStatus,
    val lessons: List<CoursePathLessonItem>,
)

data class CoursePathLessonItem(
    val index: Int,
    val title: String,
    val status: CoursePathStatus,
)

fun buildCoursePathItems(
    syllabus: CourseSyllabus,
    currentLevel: String,
    currentLessonId: String?,
): List<CoursePathItem> {
    if (syllabus.level != currentLevel) {
        return syllabus.modules.mapIndexed { index, module ->
            CoursePathItem(
                index = index + 1,
                module = module,
                status = CoursePathStatus.Available,
                lessons = module.lessons.mapIndexed { lessonIndex, lesson ->
                    CoursePathLessonItem(
                        index = lessonIndex + 1,
                        title = lesson,
                        status = CoursePathStatus.Available,
                    )
                },
            )
        }
    }

    val currentLessonNumber = currentLessonId
        ?.substringAfter("${syllabus.level}-", missingDelimiterValue = "")
        ?.takeIf { it.isNotBlank() }
        ?.toIntOrNull()
        ?: 1
    var firstLessonInModule = 1
    var currentModuleIndex = 0
    val moduleFirstLessonNumbers = mutableListOf<Int>()
    syllabus.modules.forEachIndexed { index, module ->
        moduleFirstLessonNumbers.add(firstLessonInModule)
        val lessonCount = module.lessons.size.coerceAtLeast(1)
        val lastLessonInModule = firstLessonInModule + lessonCount - 1
        if (currentLessonNumber in firstLessonInModule..lastLessonInModule) {
            currentModuleIndex = index
        }
        firstLessonInModule += lessonCount
    }

    return syllabus.modules.mapIndexed { index, module ->
        val status = when {
            index < currentModuleIndex -> CoursePathStatus.Completed
            index == currentModuleIndex -> CoursePathStatus.Current
            index == currentModuleIndex + 1 -> CoursePathStatus.Available
            else -> CoursePathStatus.Locked
        }
        CoursePathItem(
            index = index + 1,
            module = module,
            status = status,
            lessons = module.lessons.mapIndexed { lessonIndex, lesson ->
                val lessonNumber = moduleFirstLessonNumbers[index] + lessonIndex
                CoursePathLessonItem(
                    index = lessonIndex + 1,
                    title = lesson,
                    status = when {
                        lessonNumber < currentLessonNumber -> CoursePathStatus.Completed
                        lessonNumber == currentLessonNumber -> CoursePathStatus.Current
                        lessonNumber == currentLessonNumber + 1 -> CoursePathStatus.Available
                        else -> CoursePathStatus.Locked
                    },
                )
            },
        )
    }
}
