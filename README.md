# SayIt
This application is build apone following library 
http://cmusphinx.sourceforge.net/wiki/tutorialandroid

The library provides functionalities to get the audio input from the microphone and translate it into text. The translation into text is done following a grammer which we can provide. Additionlly, grammer can be changed at runtime. 

This is more like changin context. In this application when user says camera, grammer for camera is loaded and only if user says something that matches with camera grammer some action will be taken. Once user exits the camera. It goes back to menu grammer, which looks for "camera" or "location" command.



## Usage
After launching application, user needs to wait while decoder is loaded. Once loaded listening can be started by pressing a button the screen (Same button can be used to stop the application). Once the application is running, it waits for user to say **please start**. User can now issue **camera**, **location** or **stop listening** commands.

### Camera actions
Once user says **camera** camera is launched and grammer for camera is loaded. The application now looks for **click photo** or **done taking photo** and acts accordingly. 

### Location actions
Once user says **location**  grammer for location is loaded. The application now looks for **where am i** or **exit** and acts accordingly. 



