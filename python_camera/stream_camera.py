import cv2
from flask import Flask, Response, render_template_string, jsonify
import threading
import sys
import random

app = Flask(__name__)
camera = cv2.VideoCapture(1) # 0 - iphone, 1 - macOS


# Load the face detection model
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

# Global variable to hold the latest frame
latest_frame = None
frame_lock = threading.Lock()

# LED state
led_state = "on"

def process_frames():
    global latest_frame
    while True:
        success, frame = camera.read()
        if not success:
            break

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(gray, 1.1, 4)
        for (x, y, w, h) in faces:
            cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)

        with frame_lock:
            latest_frame = frame.copy()

def gen_frames():
    while True:
        with frame_lock:
            if latest_frame is None:
                continue
            ret, buffer = cv2.imencode('.jpg', latest_frame)
            frame_bytes = buffer.tobytes()

        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')

@app.route('/video_feed')
def video_feed():
    # Updated CSS: align-items: center will vertically center the image in the 100vh container
    return render_template_string('''
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
              body {
                margin: 0;
                padding: 0;
                background-color: black;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
                width: 100vw;
                overflow: hidden;
              }
              img {
                max-width: 100%;
                max-height: 100%;
                width: auto;
                height: auto;
                display: block;
                object-fit: contain;
              }
            </style>
          </head>
          <body>
            <img src="{{ url_for('stream') }}" />
          </body>
        </html>
    ''')

@app.route('/stream')
def stream():
    return Response(gen_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/led_on')
def led_on():
    global led_state
    led_state = "on"
    return "LED turned on"

@app.route('/led_off')
def led_off():
    global led_state
    led_state = "off"
    return "LED turned off"

@app.route('/led_status')
def get_led_status():
    global led_state
    return led_state

@app.route('/sensors')
def get_sensors():
    # Simulating sensor data
    data = {
        "temperature": round(random.uniform(20.0, 30.0), 1),
        "humidity": round(random.uniform(40.0, 60.0), 1)
    }
    return jsonify(data)

@app.route('/sensors_view')
def sensors_view():
    return render_template_string('''
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta http-equiv="refresh" content="5">
            <style>
              body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f0f0f0; }
              .card { background: white; padding: 2rem; border-radius: 1rem; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center; }
              h1 { color: #333; }
              .val { font-size: 3rem; font-weight: bold; color: #007bff; }
            </style>
          </head>
          <body>
            <div class="card">
                <h1>Current Environment</h1>
                <div class="val" id="temp">--°C</div>
                <div class="val" id="hum">--%</div>
            </div>
            <script>
                fetch('/sensors').then(r => r.json()).then(data => {
                    document.getElementById('temp').innerText = data.temperature + '°C';
                    document.getElementById('hum').innerText = data.humidity + '%';
                });
            </script>
          </body>
        </html>
    ''')

def run_flask():
    app.run(host='0.0.0.0', port=5001, threaded=True, use_reloader=False)

if __name__ == '__main__':
    threading.Thread(target=process_frames, daemon=True).start()

    debug_mode = "--debug" in sys.argv

    if debug_mode:
        print("Debug mode active: Showing local window.")
        threading.Thread(target=run_flask, daemon=True).start()

        while True:
            with frame_lock:
                if latest_frame is not None:
                    cv2.imshow('Camera Debug - Face Detection', latest_frame)

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

        camera.release()
        cv2.destroyAllWindows()
    else:
        print("Running in server-only mode. Use --debug to see local video.")
        run_flask()
