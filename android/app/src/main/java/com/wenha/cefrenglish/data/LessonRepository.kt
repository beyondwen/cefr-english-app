package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.data.api.LessonSubmitRequestDto
import com.wenha.cefrenglish.data.api.TodayLessonRegenerateRequestDto
import com.wenha.cefrenglish.domain.DailyLesson
import com.wenha.cefrenglish.domain.DialogueLine
import com.wenha.cefrenglish.domain.KeySentence
import com.wenha.cefrenglish.domain.LessonQuestion
import com.wenha.cefrenglish.domain.LessonSubmissionResult
import com.wenha.cefrenglish.domain.VocabularyItem
import com.wenha.cefrenglish.domain.WritingReview
import com.wenha.cefrenglish.domain.WritingRuleChecks

interface LessonRepository {
    suspend fun getTodayLesson(userId: String): DailyLesson
    suspend fun getSyllabusLesson(userId: String, level: String, moduleIndex: Int): DailyLesson
    suspend fun regenerateTodayLesson(userId: String): DailyLesson
    suspend fun submitLesson(
        userId: String,
        lessonInstanceId: String,
        prompt: String,
        level: String,
        readingAnswers: List<String>,
        grammarAnswers: List<String>,
        writingSubmission: String,
    ): LessonSubmissionResult
}

class NetworkLessonRepository(private val api: AppApi) : LessonRepository {
    override suspend fun getTodayLesson(userId: String): DailyLesson {
        val response = api.getTodayLesson(userId)
        return response.toDomain()
    }

    override suspend fun getSyllabusLesson(userId: String, level: String, moduleIndex: Int): DailyLesson {
        val response = api.getSyllabusLesson(userId, level, moduleIndex)
        return response.toDomain()
    }

    override suspend fun regenerateTodayLesson(userId: String): DailyLesson {
        val response = api.regenerateTodayLesson(TodayLessonRegenerateRequestDto(userId))
        return response.toDomain()
    }

    override suspend fun submitLesson(
        userId: String,
        lessonInstanceId: String,
        prompt: String,
        level: String,
        readingAnswers: List<String>,
        grammarAnswers: List<String>,
        writingSubmission: String,
    ): LessonSubmissionResult {
        val response = api.submitLesson(
            LessonSubmitRequestDto(
                userId = userId,
                lessonInstanceId = lessonInstanceId,
                prompt = prompt,
                level = level,
                readingAnswers = readingAnswers,
                grammarAnswers = grammarAnswers,
                writingSubmission = writingSubmission,
            ),
        )
        return LessonSubmissionResult(
            completed = response.completed,
            currentLessonId = response.currentLessonId,
            nextLessonId = response.nextLessonId,
            todayCompleted = response.todayCompleted,
            missingRequirements = response.missingRequirements,
            ruleChecks = WritingRuleChecks(
                notBlank = response.ruleChecks.notBlank,
                minSentencesOk = response.ruleChecks.minSentencesOk,
                onTopicLikely = response.ruleChecks.onTopicLikely,
            ),
            feedback = WritingReview(
                grammar = response.feedback.grammar,
                vocabulary = response.feedback.vocabulary,
                coherence = response.feedback.coherence,
                suggestions = response.feedback.suggestions,
                rewrite = response.feedback.rewrite,
            ),
        )
    }
}

private fun com.wenha.cefrenglish.data.api.DailyLessonDto.toDomain(): DailyLesson =
    DailyLesson(
        lessonInstanceId = lessonInstanceId,
        templateId = templateId,
        level = level,
        theme = theme,
        objectives = objectives,
        warmupQuestions = warmupQuestions,
        vocabulary = vocabulary.map {
            VocabularyItem(
                word = it.word,
                meaning = it.meaning,
                example = it.example,
            )
        },
        keySentences = keySentences.map {
            KeySentence(
                pattern = it.pattern,
                meaning = it.meaning,
                examples = it.examples,
            )
        },
        dialogue = dialogue.map {
            DialogueLine(
                speaker = it.speaker,
                line = it.line,
            )
        },
        speakingPractice = speakingPractice,
        listeningPractice = listeningPractice,
        reviewTasks = reviewTasks,
        readingText = readingText,
        readingQuestions = readingQuestions.map {
            LessonQuestion(
                questionId = it.questionId,
                prompt = it.prompt,
                choices = it.choices,
                answer = it.answer,
            )
        },
        grammarExplanation = grammarExplanation,
        grammarQuestions = grammarQuestions.map {
            LessonQuestion(
                questionId = it.questionId,
                prompt = it.prompt,
                choices = it.choices,
                answer = it.answer,
            )
        },
        writingPrompt = writingPrompt,
        writingRubric = writingRubric,
        generationVersion = generationVersion,
    )
