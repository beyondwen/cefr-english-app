CREATE TABLE IF NOT EXISTS review_progress (
  user_id TEXT NOT NULL,
  review_id TEXT NOT NULL,
  status TEXT NOT NULL,
  mastery_score INTEGER NOT NULL DEFAULT 0,
  completed_at TEXT,
  updated_at TEXT NOT NULL,
  PRIMARY KEY (user_id, review_id)
);
