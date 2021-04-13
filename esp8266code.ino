#include <ESP8266WebServer.h>
#include <ESP8266HTTPClient.h>

// WiFi Definitions
const char* ssid = "Kandoi";
const char* password = "kandoinetwork";

IPAddress local_ip(192,168,0,100);
IPAddress gateway(192,168,0,1);
IPAddress subnet(255,255,255,0);
IPAddress primaryDNS(8,8,8,8);
IPAddress secondaryDNS(8,8,4,4);

const int relay=5;
const int button=16;

int buttonState = 0;
const char* notifUrl = "http://us-central1-iot-mad-door-lock.cloudfunctions.net/sendNotification";

String input;

HTTPClient http;

ESP8266WebServer server(80);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);  
  pinMode(relay, OUTPUT);
  pinMode(button, INPUT_PULLDOWN_16);

  if (!WiFi.config(local_ip, gateway, subnet, primaryDNS, secondaryDNS)) {
  Serial.println("STA Failed to configure");
  }
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected");
  Serial.println(WiFi.localIP());
  delay(100);

  server.on("/", HTTP_POST ,onConnect);
  server.on("/lock",doLock);
  server.on("/unlock",doUnlock);
  server.onNotFound(handle_NotFound);
  
  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  buttonState = digitalRead(button);
  
  // put your main code here, to run repeatedly:
  server.handleClient();

  if(buttonState == HIGH) 
  {
    if(input == "")
      Serial.println("No token present, cannot send notification");
    else
    {
      http.begin(notifUrl);
      http.addHeader("Content-Type", "application/json"); 
      Serial.println(input);
      int httpResponseCode = http.POST(input);
      if(httpResponseCode>0)
      {
        String response = http.getString();  //Get the response to the request
        Serial.println(httpResponseCode);   //Print return code
        Serial.println(response);           //Print request answer
      }
      else 
      {
        Serial.print("Error on sending POST: ");
        Serial.println(httpResponseCode);
      }
      http.end();
    }
    delay(1000);
  }
}

void onConnect()
{
  if (server.hasArg("plain") == false) { //Check if body received
    server.send(200, "text/plain", "Body not received");
    return;
  }
  else {
    input = "";
    input += server.arg("plain");
    Serial.println(input);
    server.send(200, "text/plain", "Successfully received token"); 
  }
}

void handle_NotFound()
{
  server.send(404, "text/plain", "Invalid or unknown request");
}

void doLock()
{
  digitalWrite(relay, HIGH);
  server.send(200, "text/plain", "Locked");
}

void doUnlock()
{
  digitalWrite(relay, LOW);
  server.send(200, "text/plain", "Unlocked"); 
}
