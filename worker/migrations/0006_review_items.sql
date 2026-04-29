CREATE TABLE IF NOT EXISTS review_items (
  user_id TEXT NOT NULL,
  review_id TEXT NOT NULL,
  skill TEXT NOT NULL,
  title TEXT NOT NULL,
  task TEXT NOT NULL,
  steps_json TEXT NOT NULL,
  due_date TEXT NOT NULL,
  source_lesson_instance_id TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  PRIMARY KEY (user_id, review_id)
);
