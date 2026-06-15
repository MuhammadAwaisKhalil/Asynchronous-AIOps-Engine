import keras
import tensorflow as tf
import numpy as np
from keras.layers import TextVectorization
from keras.preprocessing.sequence import pad_sequences
import random  # FIX 1: Must import the random module


servers = ["prod-web-srv-", "db-primary-node-", "auth-cluster-pod-", "api-gateway-v2-"]
ips = ["192.168.1.50", "10.0.4.12", "172.16.254.1", "185.220.101.4"]
tables = ["orders", "users", "transactions", "inventory_items", "billing_profiles"]

training_logs = []
training_labels = []

# =========================================================================
# Category 0: DATABASE_DEADLOCK (SQL means structural engine failures)
# =========================================================================
db_templates = [
    "CRITICAL SQL Exception: Lock acquisition shared conflict on table '{table}'. Query aborted.",
    "SQL Transaction rolled back: Deadlock found when trying to get lock on table '{table}'",
    "SQL HikariPool-1 - Connection is not available, request timed out on node {node}",
    "PostgreSQL SQL Error 1213 (40001): Lock wait timeout exceeded on table '{table}'",
    "SQL org.postgresql.util.PSQLException: FATAL: remaining connection slots are reserved on {node}",
    "SQL OperationalError: database table '{table}' is locked due to concurrent write transaction",
    # --- NEW GENERALIZED VARIATIONS ---
    "Deadlock failure: aborted waiting for row resource clearance inside customer database pool.",
    "Internal database pool resource failure: query execution aborted waiting for lock clearance.",
    "Fatal storage engine error: transaction clearance timed out due to high index contention.",
    "Database driver dropped connection: socket timeout during extensive read/write block on {node}.",
    "Hibernate transaction coordinator failed to flush batch update to backend data store repository."
]


# =========================================================================
# Category 1: SECURITY_AUTH_BREACH (SQL means malicious input attacks)
# =========================================================================
security_templates = [
    "Security Breach Warning: Malicious SQL injection payload detected in input field on /api/v1/login.",
    "SQL AccessDeniedException: Unauthorized API access attempt intercepted from source IP {ip}",
    "SQL JWT signature validation failed. Token compromised or malicious manipulation on /api/v1/{table}",
    "Spring Security Alert: Bad credentials SQL execution limit exceeded for account user_{id}",
    "WAF Alert: Blocked request payload containing dangerous malicious SQL injection strings targeting table '{table}'",
    "Security Filter Chain Exception: Malicious SQL bypass attempted on restricted resource path /admin/{table}",
    # --- NEW GENERALIZED VARIATIONS ---
    "WAF dropped an exploit attempt: malicious code sequence discovered targeting backend endpoint parameters.",
    "Intrusion Detection System triggered: anomalous automated scanning sequence originating from client remote socket.",
    "JSON Web Token parsing dropped: invalid cryptographic payload or forged context footprint signature.",
    "Privilege escalation vector neutralized: account user_{id} attempted executing forbidden administrative tasks.",
    "Security firewall blocked inbound transport layer packet matching known cross-site scripting signatures."
]



# =========================================================================
# Generate Category 2: RESOURCE_EXHAUSTION (~200 examples)
# =========================================================================
resource_templates = [
    "java.lang.OutOfMemoryError: Java heap space full or completely exhausted on instance {node}",
    "Critical system runtime failure: Severe Thread death due to StackOverflowError inside runtime execution thread",
    "Garbage Collection overhead limit exceeded; CPU utilization core scheduler spiked to {cpu} percent",
    "Kubernetes Eviction API: Pod OOMKilled status triggered. Container resource memory limit reached on host {node}",
    "Docker Daemon: System-wide file descriptors limit reached (fs.file-max). Cannot open new network sockets on node",
    "Apache Tomcat Server: Severe worker thread pool exhaustion. MaxThreads active and waiting queue is 100% full",
    # --- NEW GENERALIZED VARIATIONS ---
    "Runtime Failure: Thread starvation imminent. Core CPU scheduler operating at maximum limits.",
    "System execution halted: execution context memory heap allocator reached absolute system physical boundary.",
    "Host compute layer non-responsive: IO bottleneck saturation detected on core operating system disk scheduler.",
    "JVM processing threshold broken: garbage collector running continuously, stalling master cluster node processing loop.",
    "Container environment terminated: operating system low memory subsystem triggered active kernel out-of-memory killer."
]
def format_log(template):
    return template.format(
        table=random.choice(tables),
        node=random.choice(servers) + str(random.randint(1, 5)),
        ip=random.choice(ips),
        id=random.randint(1000, 9999),
        cpu=random.randint(95, 100)
    )

training_logs = []
training_labels = []

for _ in range(250): # Increased to 250 each to give the expanded vocabulary more density
    training_logs.append(format_log(random.choice(db_templates)))
    training_labels.append(0)

for _ in range(250):
    training_logs.append(format_log(random.choice(security_templates)))
    training_labels.append(1)

for _ in range(250):
    training_logs.append(format_log(random.choice(resource_templates)))
    training_labels.append(2)


print(f"Dataset successfully built! Total logs: {len(training_logs)}")
VOCAB_SIZE=300
MAX_LEN = 25


tokenized  = TextVectorization(max_tokens=VOCAB_SIZE,output_mode='int',output_sequence_length=MAX_LEN)
tokenized.adapt(training_logs)



x_train = tf.convert_to_tensor(training_logs, dtype=tf.string)
y_train = keras.utils.to_categorical(np.array(training_labels),num_classes=3)


model = keras.models.Sequential([

    keras.layers.Input(shape=(1,), dtype=tf.string),

    tokenized,

    keras.layers.Embedding(VOCAB_SIZE, 32),

    keras.layers.Bidirectional(keras.layers.LSTM(16, return_sequences=False)),
    keras.layers.Dense(16, activation='relu'),
    keras.layers.Dense(3, activation='softmax')
])


model.compile(loss='categorical_crossentropy',optimizer='adam',metrics=['accuracy'])



model.fit(x_train,y_train,epochs=40,batch_size=32, verbose=1)

model.save("log_classifier_model.keras")
print("\n[SUCCESS] Model successfully compiled and saved as log_classifier_model.keras")
