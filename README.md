# SayIt
This application is build upon following library 
http://cmusphinx.sourceforge.net/wiki/tutorialandroid

The library provides functionalities to get the audio input from the microphone and translate it into text. The translation into text is done according to a grammar which we can provide. Additionally, grammar can be changed at runtime. 

This is more like changing context. In this application when user says camera, grammar for camera is loaded and only if user says something that matches with camera grammar, some action will be taken. Once user exits the camera. It goes back to menu grammar, which looks for "camera" or "location" command.



## Usage
After launching application, user needs to wait while decoder is loaded. Once loaded, listening can be started by pressing a button on the screen (Same button can be used to stop the application). Once the application is running, it waits for user to say **please start**. User can now issue **camera**, **location** or **stop listening** commands.

Stopping the application by pressing a button on the screen will stop the application completely. That means **please start** command wont work until the application is started again from the UI.

### Camera actions
Once user says **camera**, camera is launched and grammar for camera is loaded. The application now looks for **click photo** or **done taking photo** and acts accordingly. 

### Location actions
Once user says **location**,  grammar for location is loaded. The application now looks for **where am i** or **exit** and acts accordingly. 


