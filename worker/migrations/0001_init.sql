CREATE TABLE users (
  user_id TEXT PRIMARY KEY,
  current_level TEXT NOT NULL,
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
  updated_at TEXT NOT NULL
);
