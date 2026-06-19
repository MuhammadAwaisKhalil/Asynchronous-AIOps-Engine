import tensorflow as tf
from flask import Flask, request, jsonify
import log_errors
app = Flask(__name__)

model = tf.keras.models.load_model('log_classifier_model.keras')

CLASS_LABELS = {0:"DATABASE_DEADLOCK",1:"SECURITY_AUTH_BREACH",2:"RESOURCE_EXHAUTION"}

@app.route('/telemetry',methods=['POST'])
def process_telemetry():
    data = request.get_json()
    logID = data.get('logID')
    log = data.get('logText')

    print(logID,'  ',log)

    input_tensor = tf.convert_to_tensor([[log]],dtype=tf.string)

    predictions = model.predict(input_tensor, verbose=0)
    print(input_tensor.shape)

    predicted_idx = tf.argmax(predictions[0]).numpy()

    confidence = float(predictions[0][predicted_idx])

    aiResponse = CLASS_LABELS.get(predicted_idx,"UNKNOWN")

    log_errors.addLog(log_text=log,prediction=aiResponse)

    return aiResponse, 200

if __name__ == '__main__':
    app.run(port=8080, debug=False)

