CREATE TABLE users (
  user_id TEXT PRIMARY KEY,
  current_level TEXT NOT NULL,
  recent_weaknesses_json TEXT NOT NULL DEFAULT '[]',
  theme_rotation_state TEXT NOT NULL DEFAULT 'life',
  current_template_id TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE TABLE placement_results (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL,
  level TEXT NOT NULL,
  weaknesses_json TEXT NOT NULL,
  raw_score_json TEXT NOT NULL,
  created_at TEXT NOT NULL
);

CREATE TABLE lessons (
  lesson_id TEXT NOT NULL,
  user_id TEXT NOT NULL,
  level TEXT NOT NULL,
  unit_index INTEGER NOT NULL,
  status TEXT NOT NULL,
  lesson_json TEXT NOT NULL,
  generated_by TEXT NOT NULL,
  generated_at TEXT NOT NULL,
  version TEXT NOT NULL,
  PRIMARY KEY (lesson_id, user_id)
);

CREATE TABLE lesson_instances (
  lesson_instance_id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  template_id TEXT NOT NULL,
  level TEXT NOT NULL,
  generation_version INTEGER NOT NULL,
  status TEXT NOT NULL,
  lesson_json TEXT NOT NULL,
  generated_at TEXT NOT NULL,
  completed_at TEXT
);

CREATE TABLE lesson_submissions (
  submission_id INTEGER PRIMARY KEY AUTOINCREMENT,
  lesson_instance_id TEXT NOT NULL,
  user_id TEXT NOT NULL,
  reading_answers_json TEXT NOT NULL,
  grammar_answers_json TEXT NOT NULL,
  writing_submission TEXT NOT NULL,
  review_json TEXT NOT NULL,
  submitted_at TEXT NOT NULL
);

CREATE TABLE writing_reviews (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL,
  lesson_id TEXT NOT NULL,
  prompt TEXT NOT NULL,
  submission TEXT NOT NULL,
  review_json TEXT NOT NULL,
  created_at TEXT NOT NULL
);

CREATE TABLE progress (
  user_id TEXT PRIMARY KEY,
  level TEXT NOT NULL,
  completed_lesson_ids_json TEXT NOT NULL,
  current_template_id TEXT,
  current_lesson_instance_id TEXT,
  today_completed INTEGER NOT NULL DEFAULT 0,
  updated_at TEXT NOT NULL
);
