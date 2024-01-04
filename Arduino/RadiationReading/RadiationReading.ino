// Constants
const int geigerCounterPin = 2;

// Variables
String msgPhone;
boolean state;

long count = 0;
long countPerMinute = 0;
float usvh = 0.0;
long prevTimeMeasure = 0;

void setup() {
  // Initialization
  Serial.begin(9600); // Communication rate of the Bluetooth Module
  msgPhone = "";
  state = false; // Initial state is false

  // Initializing pins
  pinMode(geigerCounterPin, INPUT);
  attachInterrupt(digitalPinToInterrupt(geigerCounterPin), tick, FALLING);
}

void loop() {
  
  // To read message received from Mobile phone
  if (Serial.available() > 0){ // Check if there is data coming
    msgPhone = Serial.readString(); // Read the message as String
    if (msgPhone == "1"){ // When pressed the start button on mobile
      state = 1;
    }
    else if(msgPhone == "0"){ // When pressed the stop button on mobile
      state = 0;
    }
  }

  if (millis() - prevTimeMeasure > 10000){ // Provide the count every 10 seconds
    countPerMinute = 6*count;
    prevTimeMeasure = millis();
    
    if(state){
      Serial.println(countPerMinute, DEC);
    }
    count = 0;
  }
}

void tick() {
  detachInterrupt(0);
  count++;
  while(digitalRead(geigerCounterPin) == 0){
  }
  attachInterrupt(0, tick, FALLING);
}