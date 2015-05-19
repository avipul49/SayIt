# SayIt
This application is build upon following library 
http://cmusphinx.sourceforge.net/wiki/tutorialandroid

and google speech recognition api http://developer.android.com/reference/android/speech/SpeechRecognizer.html

cmusphinx uses device's co-processor to continuously listen for a keyword. Once the keyword is found we launch google's speech recognition api for following commands. 

## Usage
Application waits for user to say "Ding ding start". Once it is started camera and location commands can be issued. User can now end the session by saying "ding ding stop listening"

### Camera actions
Once user says **camera**, camera is launched and grammar for camera is loaded. The application now looks for **click photo** or **done taking photo** and acts accordingly. 

### Location actions
Once user says **location**,  grammar for location is loaded. The application now looks for **where am i** or **exit** and acts accordingly. 

**Go to <insert location>** can be used to launch navigation app to get route from current location to provided location.

