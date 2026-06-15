import sqlite3
def initializeDatabase():
    conn = sqlite3.connect('sysHistory.db')
    cursor = conn.cursor()
    cursor.execute('''CREATE TABLE IF NOT EXISTS Logs(
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                   log_text TEXT,
                   prediction TEXT)''')
    conn.commit()
    conn.close()
initializeDatabase()