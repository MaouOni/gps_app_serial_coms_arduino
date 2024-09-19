#include <TinyGPS.h>
#include <SoftwareSerial.h>

static const int RXPin = 3, TXPin = 4;
static const uint32_t GPSBaud = 9600;

TinyGPS gps;
SoftwareSerial ss(RXPin, TXPin);

void setup() {
  Serial.begin(115200);
  ss.begin(GPSBaud);
  Serial.println("GPS Module Test");
}

void loop() {
  static unsigned long lastSentTime = 0;
  const unsigned long interval = 1000; // 1 second interval

  while (ss.available() > 0) {
    int c = ss.read();
    if (gps.encode(c)) {
      float latitude, longitude;
      unsigned long age;
      gps.f_get_position(&latitude, &longitude, &age);

      if (latitude != TinyGPS::GPS_INVALID_F_ANGLE && longitude != TinyGPS::GPS_INVALID_F_ANGLE) {
        unsigned long currentTime = millis();
        if (currentTime - lastSentTime >= interval) {
          String data = String(latitude, 6) + "," + String(longitude, 6);
          Serial.println(data);
          lastSentTime = currentTime;
        }
      }
    }
  }
}
