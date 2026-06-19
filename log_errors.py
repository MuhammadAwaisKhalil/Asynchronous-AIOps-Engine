import sqlite3
import pandas as pd
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

def addLog(log_text,prediction):
    conn = sqlite3.connect('sysHistory.db')
    cursor = conn.cursor()
    cursor.execute('INSERT INTO Logs (log_text,prediction) VALUES (?,?)',(log_text,prediction))
    conn.commit()
    conn.close()

def getData():
    conn = sqlite3.connect('sysHistory.db')
    df = pd.read_sql_query('SELECT * FROM Logs ORDER BY id DESC',conn)
    conn.close()
    return df


