-- User_Rights table for individual page access control
CREATE TABLE IF NOT EXISTS User_Rights (
    user_rights_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    page_name TEXT NOT NULL,
    granted_at TEXT DEFAULT CURRENT_TIMESTAMP,
    granted_by TEXT NOT NULL,
    FOREIGN KEY (username) REFERENCES User(username),
    UNIQUE(username, page_name)
);
