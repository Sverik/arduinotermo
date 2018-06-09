/*
File: RestClient.ino
Note: works only with Arduino Uno WiFi Developer Edition.

http://www.arduino.org/learning/tutorials/boards-tutorials/restserver-and-restclient
*/

#include <Wire.h>
#include <UnoWiFiDevEd.h>
#include <DHT.h>;

#define DHTPIN 2     // what pin we're connected to
#define DHTTYPE DHT22   // DHT 22  (AM2302)
DHT dht(DHTPIN, DHTTYPE); //// Initialize DHT sensor for normal 16mhz Arduino

float hum;  //Stores humidity value
float temp; //Stores temperature value
unsigned long tempInterval = 60000; // 60 * 1000
unsigned long time = 0;
unsigned long nextTempTime = 0;
unsigned long bubbleMinInterval = 4000; // 4 * 1000
unsigned long lastBubbleTime = 0;

int sensorPin = A0;    // select the input pin for the potentiometer
int sensorValue = 0;  // variable to store the value coming from the sensor

 char* connector = "rest";
 char* server = "192.168.1.227";
 char* method = "POST";
 String resource;
void setup() {

 Serial.begin(9600);
 Ciao.begin();
 dht.begin();

 delay(5000);
}

void loop() {
  time = millis();

  // post temp, if it is time
  if (nextTempTime < time) {
    Serial.print("temptime: ");
    Serial.println(time);
    Serial.print("tempInterval: ");
    Serial.println(tempInterval);
    nextTempTime = time + tempInterval;
    Serial.print("nextTempTime: ");
    Serial.println(nextTempTime);
    recordTemp();
  }

  // check bubble
  sensorValue = analogRead(sensorPin);
  if (sensorValue > 512 && lastBubbleTime + bubbleMinInterval < time) {
    lastBubbleTime = time;
    recordBubble();
  }

  delay (25);
}

void doRequest(char* conn, char* server, String command, char* method){
 CiaoData data = Ciao.write(conn, server, command, method);
 if (!data.isEmpty()){
 Ciao.println( "State: " + String (data.get(1)) );
 Ciao.println( "Response: " + String (data.get(2)) );
 Serial.println( "State: " + String (data.get(1)) );
 Serial.println( "Response: " + String (data.get(2)) );
 }
 else{
 Ciao.println ("Write Error");
 Serial.println ("Write Error");
 }
}

void recordBubble() {
  Serial.println("Bubble!");
 
 resource = "/bubble";
 
 doRequest(connector, server, resource, method);
}

void recordTemp() {
  hum = dht.readHumidity();
  temp= dht.readTemperature();
  //Print temp and humidity values to serial monitor
  Serial.print("Humidity: ");
  Serial.print(hum);
  Serial.print(" %, Temp: ");
  Serial.print(temp);
  Serial.println(" Celsius");
 
 resource = "/env?temp=";
 resource.concat(temp);
 resource.concat("&hum=");
 resource.concat(hum);
 Serial.println(resource);
 
 doRequest(connector, server, resource, method);
}

