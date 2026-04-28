import type { CourseSyllabus, PlacementTest } from '../domain/types'

export type WritingReview = {
  grammar: string
  vocabulary: string
  coherence: string
  suggestions: string[]
  rewrite: string
}

export type AiProvider = {
  generateLesson(input: {
    level: string
    lessonId: string
    grammarFocus: string
    writingTask: string
    weaknesses?: string[]
    theme?: string
  }): Promise<{
    objectives?: string[]
    warmupQuestions?: string[]
    vocabulary?: Array<{
      word: string
      meaning: string
      example: string
    }>
    keySentences?: Array<{
      pattern: string
      meaning: string
      examples: string[]
    }>
    dialogue?: Array<{
      speaker: string
      line: string
    }>
    speakingPractice?: string[]
    listeningPractice?: string[]
    reviewTasks?: string[]
    readingText: string
    readingQuestions: string[]
    grammarExplanation: string
    grammarQuestions: string[]
    writingPrompt: string
  }>
  generateSyllabus(input: { level: CourseSyllabus['level'] }): Promise<CourseSyllabus>
  generatePlacementTest(): Promise<PlacementTest>
  generateWritingReview(input: { level: string; prompt: string; submission: string }): Promise<WritingReview>
}

type OpenAiChatResponse = {
  choices?: Array<{
    message?: {
      content?: string
      reasoning_content?: string
      reasoning?: string
    }
  }>
}

type LongCatConfig = {
  apiKey?: string
  baseUrl?: string
  model?: string
  LONGCAT_API_KEY?: string
  LONGCAT_BASE_URL?: string
  LONGCAT_MODEL?: string
  ALLOW_FAKE_AI_FALLBACK?: string
}

const defaultLongCatBaseUrl = 'https://api.longcat.chat/openai'
const defaultLongCatModel = 'LongCat-Flash-Thinking-2601'

const syllabusShapeByLevel: Record<CourseSyllabus['level'], { unitCount: number; lessonsPerUnit: string; focus: string }> = {
  A1: {
    unitCount: 12,
    lessonsPerUnit: '每个单元 3 节课，总计约 36 节课',
    focus: '零基础到入门，必须覆盖发音、字母、数字、be动词、一般现在时、名词单复数、基础介词、can、简单过去时、购物问路点餐等真实生活场景',
  },
  A2: {
    unitCount: 12,
    lessonsPerUnit: '每个单元 3-4 节课，总计约 40 节课',
    focus: '日常交流扩展，覆盖过去经历、未来计划、比较、建议、邀请、旅行、健康、邮件和简单观点表达',
  },
  B1: {
    unitCount: 10,
    lessonsPerUnit: '每个单元 4 节课，总计约 40 节课',
    focus: '独立表达，覆盖叙事、观点、工作学习、问题解决、条件句、现在完成时和较完整段落写作',
  },
  B2: {
    unitCount: 10,
    lessonsPerUnit: '每个单元 4 节课，总计约 40 节课',
    focus: '中高级流利度，覆盖论证、让步、转述、商务沟通、媒体社会话题和结构化短文',
  },
  C1: {
    unitCount: 8,
    lessonsPerUnit: '每个单元 4-5 节课，总计约 36 节课',
    focus: '高级精准表达，覆盖语域控制、学术表达、复杂观点、专业沟通和文本润色',
  },
  C2: {
    unitCount: 8,
    lessonsPerUnit: '每个单元 4-5 节课，总计约 36 节课',
    focus: '精通级掌控，覆盖修辞、风格转换、专业阅读、专家摘要、编辑写作和高阶表达策略',
  },
}

const stripCodeFence = (value: string): string =>
  value
    .trim()
    .replace(/^```(?:json)?\s*/i, '')
    .replace(/\s*```$/i, '')
    .trim()

const parseJsonObject = <T>(content: string): T => {
  const cleaned = stripCodeFence(content)
  const start = cleaned.indexOf('{')
  const end = cleaned.lastIndexOf('}')
  if (start < 0 || end < start) throw new Error('LongCat response did not contain a JSON object')
  return JSON.parse(cleaned.slice(start, end + 1)) as T
}

const asStringArray = (value: unknown): string[] =>
  Array.isArray(value) ? value.map((item) => String(item)).filter(Boolean) : []

const asVocabulary = (value: unknown): Array<{ word: string; meaning: string; example: string }> =>
  Array.isArray(value)
    ? value.map((item) => {
        const parsed = item as { word?: unknown; meaning?: unknown; example?: unknown }
        return {
          word: String(parsed.word ?? ''),
          meaning: String(parsed.meaning ?? ''),
          example: String(parsed.example ?? ''),
        }
      }).filter((item) => item.word && item.meaning)
    : []

const asKeySentences = (value: unknown): Array<{ pattern: string; meaning: string; examples: string[] }> =>
  Array.isArray(value)
    ? value.map((item) => {
        const parsed = item as { pattern?: unknown; meaning?: unknown; examples?: unknown }
        return {
          pattern: String(parsed.pattern ?? ''),
          meaning: String(parsed.meaning ?? ''),
          examples: asStringArray(parsed.examples).slice(0, 3),
        }
      }).filter((item) => item.pattern && item.meaning)
    : []

const asDialogue = (value: unknown): Array<{ speaker: string; line: string }> =>
  Array.isArray(value)
    ? value.map((item) => {
        const parsed = item as { speaker?: unknown; line?: unknown }
        return {
          speaker: String(parsed.speaker ?? ''),
          line: String(parsed.line ?? ''),
        }
      }).filter((item) => item.line)
    : []

const normalizeLesson = (value: unknown): Awaited<ReturnType<AiProvider['generateLesson']>> => {
  const item = value as Partial<Awaited<ReturnType<AiProvider['generateLesson']>>>
  return {
    objectives: asStringArray(item.objectives).slice(0, 4),
    warmupQuestions: asStringArray(item.warmupQuestions).slice(0, 3),
    vocabulary: asVocabulary(item.vocabulary).slice(0, 10),
    keySentences: asKeySentences(item.keySentences).slice(0, 5),
    dialogue: asDialogue(item.dialogue).slice(0, 10),
    speakingPractice: asStringArray(item.speakingPractice).slice(0, 5),
    listeningPractice: asStringArray(item.listeningPractice).slice(0, 5),
    reviewTasks: asStringArray(item.reviewTasks).slice(0, 5),
    readingText: String(item.readingText ?? ''),
    readingQuestions: asStringArray(item.readingQuestions).slice(0, 4),
    grammarExplanation: String(item.grammarExplanation ?? ''),
    grammarQuestions: asStringArray(item.grammarQuestions).slice(0, 4),
    writingPrompt: String(item.writingPrompt ?? ''),
  }
}

const normalizeReview = (value: unknown): WritingReview => {
  const item = value as Partial<WritingReview>
  return {
    grammar: String(item.grammar ?? ''),
    vocabulary: String(item.vocabulary ?? ''),
    coherence: String(item.coherence ?? ''),
    suggestions: asStringArray(item.suggestions).slice(0, 5),
    rewrite: String(item.rewrite ?? ''),
  }
}

const normalizeSyllabus = (level: CourseSyllabus['level'], value: unknown): CourseSyllabus => {
  const item = value as Partial<CourseSyllabus>
  const modules = Array.isArray(item.modules)
    ? item.modules.map((module) => {
        const parsed = module as { title?: unknown; goal?: unknown; lessons?: unknown }
        return {
          title: String(parsed.title ?? ''),
          goal: String(parsed.goal ?? ''),
          lessons: asStringArray(parsed.lessons).slice(0, 5),
        }
      }).filter((module) => module.title && module.goal && module.lessons.length > 0)
    : []

  if (!item.title || !item.description || modules.length === 0) {
    throw new Error('LongCat syllabus response was incomplete')
  }

  return {
    level,
    title: String(item.title),
    description: String(item.description),
    modules,
  }
}

const normalizePlacementQuestions = (value: unknown): PlacementTest['readingQuestions'] =>
  Array.isArray(value)
    ? value
        .map((item) => {
          const parsed = item as { prompt?: unknown; options?: unknown; correctIndex?: unknown }
          const options = asStringArray(parsed.options).slice(0, 4)
          return {
            prompt: String(parsed.prompt ?? ''),
            options,
            correctIndex: Number(parsed.correctIndex),
          }
        })
        .filter((item) => item.prompt && item.options.length >= 3 && Number.isInteger(item.correctIndex) && item.correctIndex >= 0 && item.correctIndex < item.options.length)
    : []

const normalizePlacementTest = (value: unknown): PlacementTest => {
  const item = value as Partial<PlacementTest>
  const readingQuestions = normalizePlacementQuestions(item.readingQuestions).slice(0, 5)
  const grammarQuestions = normalizePlacementQuestions(item.grammarQuestions).slice(0, 5)
  const writingPrompt = String(item.writingPrompt ?? '')
  const minWritingWords = Number(item.minWritingWords ?? 30)

  if (!item.readingPassage || readingQuestions.length !== 5 || grammarQuestions.length !== 5 || !writingPrompt || minWritingWords < 20) {
    throw new Error('LongCat placement test response was incomplete')
  }

  return {
    readingPassage: String(item.readingPassage),
    readingQuestions,
    grammarQuestions,
    writingPrompt,
    minWritingWords,
  }
}

const chatCompletion = async (config: Required<LongCatConfig>, messages: Array<{ role: 'system' | 'user'; content: string }>): Promise<string> => {
  const endpoint = `${config.baseUrl.replace(/\/$/, '')}/v1/chat/completions`
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${config.apiKey}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      model: config.model,
      messages,
      stream: false,
      temperature: 0.7,
    }),
  })

  if (!response.ok) {
    const detail = await response.text()
    throw new Error(`LongCat request failed: ${response.status} ${detail.slice(0, 200)}`)
  }

  const data = (await response.json()) as OpenAiChatResponse
  const message = data.choices?.[0]?.message
  const content = message?.content?.trim() || message?.reasoning_content?.trim() || message?.reasoning?.trim()
  if (!content) throw new Error('LongCat response was empty')
  return content
}

export const fakeAiProvider: AiProvider = {
  async generateLesson({ lessonId, grammarFocus, writingTask, theme, weaknesses }) {
    const weaknessText = weaknesses?.length ? `Focus more on ${weaknesses.join(', ')}.` : 'Balanced practice.'
    return {
      objectives: ['理解本课目标表达', '能在真实场景中使用核心句型'],
      warmupQuestions: ['你在这个场景里通常会说什么？'],
      vocabulary: [
        { word: 'hello', meaning: '你好', example: 'Hello, I am Anna.' },
      ],
      keySentences: [
        { pattern: 'I am ...', meaning: '我……', examples: ['I am a student.', 'I am from China.'] },
      ],
      dialogue: [
        { speaker: 'A', line: 'Hello, I am Anna.' },
        { speaker: 'B', line: 'Nice to meet you.' },
      ],
      speakingPractice: ['跟读核心句型 3 遍。'],
      listeningPractice: ['听老师读对话，圈出你听到的关键词。'],
      readingText: `Sample reading for ${lessonId} about ${theme ?? 'daily life'}.`,
      readingQuestions: ['What is the main idea?', 'Which detail is correct?'],
      grammarExplanation: `Focus on ${grammarFocus}. ${weaknessText}`,
      grammarQuestions: ['Choose the correct form.', 'Rewrite the sentence correctly.'],
      writingPrompt: `Write a ${writingTask} response about ${theme ?? 'daily life'}.`,
    }
  },
  async generateWritingReview() {
    return {
      grammar: 'Good use of basic present simple.',
      vocabulary: 'Add more topic words.',
      coherence: 'The text is easy to follow.',
      suggestions: ['Link the two sentences with and.'],
      rewrite: 'I wake up early and I walk to school.',
    }
  },
  async generateSyllabus({ level }) {
    return {
      level,
      title: `${level} 课程大纲`,
      description: '基于 CEFR 能力目标生成的课程路径。',
      modules: [
        {
          title: '核心能力入门',
          goal: '建立本等级需要的基础表达能力。',
          lessons: ['核心语法', '主题词汇', '短文阅读', '情景写作'],
        },
      ],
    }
  },
  async generatePlacementTest() {
    return fallbackPlacementTest
  },
}

const fallbackPlacementTest: PlacementTest = {
  readingPassage:
    "Emma works in a small hotel. She usually starts work at seven o'clock in the morning. Yesterday the hotel was busy because many guests arrived for a music festival. Emma helped three guests find their rooms, answered phone calls, and wrote a short email to a manager. After work, she was tired, but she felt happy because the guests thanked her.",
  readingQuestions: [
    { prompt: 'Where does Emma work?', options: ['In a school', 'In a hotel', 'In a supermarket'], correctIndex: 1 },
    { prompt: 'What time does Emma usually start work?', options: ["At seven o'clock", "At nine o'clock", "At twelve o'clock"], correctIndex: 0 },
    { prompt: 'Why was the hotel busy yesterday?', options: ['There was a music festival', 'It was raining', 'Emma had a meeting'], correctIndex: 0 },
    { prompt: 'What did Emma write?', options: ['A story', 'A short email', 'A shopping list'], correctIndex: 1 },
    { prompt: 'How did Emma feel after work?', options: ['Angry', 'Tired but happy', 'Bored'], correctIndex: 1 },
  ],
  grammarQuestions: [
    { prompt: 'I _____ from China.', options: ['am', 'is', 'are'], correctIndex: 0 },
    { prompt: 'She _____ coffee every morning.', options: ['drink', 'drinks', 'drinking'], correctIndex: 1 },
    { prompt: 'We went to the park _____.', options: ['yesterday', 'tomorrow', 'every day'], correctIndex: 0 },
    { prompt: 'This bag is _____ than that one.', options: ['heavy', 'heavier', 'heaviest'], correctIndex: 1 },
    { prompt: 'I have lived here _____ 2022.', options: ['for', 'since', 'at'], correctIndex: 1 },
  ],
  writingPrompt: '介绍你昨天做了什么，以及今天想学习什么。',
  minWritingWords: 30,
}

export const createLongCatProvider = (config: LongCatConfig): AiProvider | null => {
  const apiKey = config.apiKey ?? config.LONGCAT_API_KEY
  if (!apiKey) return null

  const resolved = {
    apiKey,
    baseUrl: config.baseUrl ?? config.LONGCAT_BASE_URL ?? defaultLongCatBaseUrl,
    model: config.model ?? config.LONGCAT_MODEL ?? defaultLongCatModel,
  }

  return {
    async generateSyllabus(input) {
      const shape = syllabusShapeByLevel[input.level]
      const content = await chatCompletion(resolved, [
        {
          role: 'system',
          content:
            '你是一个专业 CEFR 英语课程架构师。请为中文母语学习者生成完整课程大纲。只返回严格 JSON，不要 Markdown，不要代码块。字段必须是 level, title, description, modules。modules 是数组，每项必须有 title, goal, lessons。课程必须可直接用于 App 展示，不要生成泛泛的目录，要像真实课程表。',
        },
        {
          role: 'user',
          content: [
            `请生成 ${input.level} 等级英语课程大纲。`,
            `单元数量：必须生成 ${shape.unitCount} 个单元。`,
            `课时数量：${shape.lessonsPerUnit}。每个 lessons 数组写具体课时标题，不要只写“阅读/语法/写作”。`,
            `能力重点：${shape.focus}。`,
            'title 为中文标题；description 为一句中文说明；每个单元 goal 为一句中文学习目标。',
            '按从易到难排列；课时标题要具体到语法点、词汇主题、对话场景或写作任务。',
          ].join('\n'),
        },
      ])

      return normalizeSyllabus(input.level, parseJsonObject(content))
    },

    async generateLesson(input) {
      const prompt = [
        `CEFR等级：${input.level}`,
        `课程ID：${input.lessonId}`,
        `章节主题：${input.theme ?? '日常英语'}`,
        `语法/知识点：${input.grammarFocus}`,
        `写作任务目标：${input.writingTask}`,
        `薄弱项：${input.weaknesses?.join(', ') || '无'}`,
      ].join('\n')

      const content = await chatCompletion(resolved, [
        {
          role: 'system',
          content:
            '你是一个专业英语课程设计老师。请为中文母语学习者生成一节真正可学习的英语课。只返回严格 JSON，不要 Markdown，不要代码块。字段必须是 objectives, warmupQuestions, vocabulary, keySentences, dialogue, speakingPractice, listeningPractice, readingText, readingQuestions, grammarExplanation, grammarQuestions, writingPrompt, reviewTasks。readingText、dialogue、例句用英文；解释、问题和任务用中文。A1-A2 必须使用简单短句和高频词。',
        },
        {
          role: 'user',
          content: `${prompt}\n\nJSON要求：objectives 2-4条；warmupQuestions 2条；vocabulary 6-10个词，每个含 word/meaning/example；keySentences 3-5个句型，每个含 pattern/meaning/examples；dialogue 6-10轮短对话；speakingPractice 3-5条跟读或替换练习；listeningPractice 2-4条听辨/听写任务；readingText 为80-180词英文短文；readingQuestions 为2-4个中文阅读问题；grammarExplanation 为120-220字中文语法讲解，要讲清楚怎么用；grammarQuestions 为3-5个中文语法练习；writingPrompt 为一个贴合章节的中文写作题；reviewTasks 3-5条课后复习任务。`,
        },
      ])

      return normalizeLesson(parseJsonObject(content))
    },

    async generatePlacementTest() {
      const content = await chatCompletion(resolved, [
        {
          role: 'system',
          content:
            '你是 CEFR 英语分级测试命题老师。只返回严格 JSON，不要 Markdown，不要代码块。字段必须是 readingPassage, readingQuestions, grammarQuestions, writingPrompt, minWritingWords。题目面向中文母语成人学习者，但题干和选项用英文。correctIndex 从 0 开始。',
        },
        {
          role: 'user',
          content:
            '生成一套 A1-A2 起点评估题：readingPassage 为 90-130 词英文短文；readingQuestions 恰好 5 题，每题 3 个选项；grammarQuestions 恰好 5 题，每题 3 个选项，覆盖 be 动词、一般现在时、过去时间、比较级、现在完成时；writingPrompt 用中文给出一个短写作任务；minWritingWords 为 30。',
        },
      ])

      return normalizePlacementTest(parseJsonObject(content))
    },

    async generateWritingReview(input) {
      const content = await chatCompletion(resolved, [
        {
          role: 'system',
          content:
            '你是英语写作批改老师。只返回严格 JSON，不要 Markdown，不要代码块。字段必须是 grammar, vocabulary, coherence, suggestions, rewrite。反馈使用中文，rewrite 使用英文。',
        },
        {
          role: 'user',
          content: `CEFR等级：${input.level}\n写作题目：${input.prompt}\n学生提交：${input.submission}`,
        },
      ])

      return normalizeReview(parseJsonObject(content))
    },
  }
}

export const createAiProvider = (config: LongCatConfig): AiProvider => {
  const longCatProvider = createLongCatProvider(config)
  if (!longCatProvider) return fakeAiProvider
  const allowFakeFallback = config.ALLOW_FAKE_AI_FALLBACK === 'true'

  return {
    async generateLesson(input) {
      try {
        return await longCatProvider.generateLesson(input)
      } catch (error) {
        console.error('LongCat lesson generation failed', error)
        if (!allowFakeFallback) throw error
        return fakeAiProvider.generateLesson(input)
      }
    },
    async generateSyllabus(input) {
      try {
        return await longCatProvider.generateSyllabus(input)
      } catch (error) {
        console.error('LongCat syllabus generation failed', error)
        if (!allowFakeFallback) throw error
        return fakeAiProvider.generateSyllabus(input)
      }
    },
    async generatePlacementTest() {
      try {
        return await longCatProvider.generatePlacementTest()
      } catch (error) {
        console.error('LongCat placement test generation failed', error)
        return fakeAiProvider.generatePlacementTest()
      }
    },
    async generateWritingReview(input) {
      try {
        return await longCatProvider.generateWritingReview(input)
      } catch (error) {
        console.error('LongCat writing review failed', error)
        if (!allowFakeFallback) throw error
        return fakeAiProvider.generateWritingReview(input)
      }
    },
  }
}
