# Smart Door Lock
This a project that I created for my Mobile Application Development course. Following is the project report on it.

## Overview

1.	User Needs (establish the need/originality of the app)

    Many a times it happens that we are working on our phones/laptops with our earphones plugged in listening to music or in an online meeting and the doorbell rings. We have to get up and open the door every time. This may happen multiple times throughout the day and it becomes a tiresome chore to leave your work every time to get up and do a mundane and easy task like opening the door. This project aims to fix this problem by using the power of IoT and Android.

2.  Problem Statement (application of the project)

    Design an Android app that will allow you to electronically unlock the door from the comfort of the phone always in your pocket. The phone should receive a notification every time someone rings the door bell and should give the option to the user to unlock the door from anywhere inside the local network of the house. The app may also make use of a camera attached to the door so that the user can see who is at the door before unlocking it remotely. To prevent unauthorized use the of the app/phone, the app should always ask for verification of the user (password or biometrics) before unlocking the door.

## Description

1.  List & explanation of (learned) tools/components used

    The list of tools/components that were used in the app which were already taught to us in class are:

    1)	ConstraintLayout – Used to design layout of Dashboard, selected for its ease of use
    2)	LinearLayout – Used to design splash screen, selected for its simplicity
    3)	Simple Button views – To perform action on click of View
    4)	ImageView – To show image of ESP8266
    5)	onClick listeners for Button views – To implement functionality on click of Button views
    6)	Android navigation functions (finish() and start new Activity using Intent)
    7)	ProgressBar – To show loading status while asynchronous task is being done in the background
    8)	SharedPreferences – To store FCM token in cache memory

2.	List & explanation of (self-learned) tools/components used

    The list of tools/components that were used in the app which we came across and learnt by ourselves are:

    1)	Volley – An HTTP library to make HTTP request such as GET, POST etc.
    2)	Firebase Cloud Functions – To execute some code on the cloud triggered by the ESP8266
    3)	Firebase Cloud Messaging – To send notifications from Firebase Cloud Functions to the Android app
    4)	WebView – To display camera feed from the ESP32 to the Android app
    5)	BroadcastReceiver – To listen for custom Notification action button clicks
    6)	Build – To get details of the current Android version running on the device
    7)	NotificationChannel – To create Notification channel for Notifications
    8)	NotificationCompat.Builder – To create custom Notifications
    9)	BiometricManager – To check if device has support for Biometric Authentication
    10)	BiometricPrompt – To create Biometric Authentication Prompts
    11)	CoordinatorLayout- A super powered FrameLayout used to display Snackbars and AppBarLayout
    12)	AppBarLayout and CollapsingToolbarLayout – Used to display a Toolbar within Material Design guidelines
    13)	DrawerLayout and NavigationView – Used to display a Navigation drawer as given in Material Design
    14)	MaterialButton – To display buttons with icon support

## Standardized Workable App

1.	User Manual (step-by-Step screenshot and brief about the working of app)

    -	On opening the app, the user will be greeted by a splash screen with an embedded gif and a ProgressBar. Here, the app tries to communicate with the ESP8266 and sends the application’s FCM token to it. It does this to firstly make sure that the ESP8266 is online and secondly to send the FCM token so that the ESP8266 can forward it to the Firebase Cloud Function.

        Connection successful:
        
        <img src="https://imgur.com/8dD73u8.png" width="360" height="740">

        Trying to connect:
        
        <img src="https://imgur.com/M6Tl5qq.png" width="360" height="740">

        Connection failed:
        
        <img src="https://imgur.com/hqrGYfQ.png" width="360" height="740">

        The splash screen duration is 1.5 seconds. In this duration it will do the tasks mentioned above. While the task is being done the ProgressBar will be displayed. As soon as the task finishes the ProgressBar disappears.

    -	The Dashboard activity opens after the splash screen ends. The activity consists of two MaterialButtons, a WebView and a menu button in the Toolbar which will open a Navigation drawer. The navigation drawer consists of a simple header layout and a single menu item with title as “About” on which when clicked shows an AlertDialog with some info about the developers.

        Navigation drawer:
        
        <img src="https://imgur.com/4nFxTSc.png" width="360" height="740">
        
        AlertDialog:
        
        <img src="https://imgur.com/cMQkAA7.png" width="360" height="740">

        The MaterialButtons act as toggle buttons ie. they toggle the door lock and the stream respectively. Once the user clicks on either button, he or she has to authenticate themselves with Biometrics (Face Recognition or Fingerprint or optionally screen unlock depending on the hardware of the phone) and the proper task will be executed after authentication is successful.
        After the task is completed a Snackbar will be displayed as a confirmation.

        Authentication Prompt:
        
        <img src="https://imgur.com/EKzxe7Y.png" width="360" height="740">

        Confirmation Snackbar:
        
        <img src="https://imgur.com/DQ6UWKK.png" width="360" height="740">

        Starting the video stream will enable the WebView which will show the camera output from the ESP32-CAM with black borders: (example)

    -	The push button on the breadboard connected to NodeMCU acts as a doorbell, pushing on which will send a notification to the user with two options presented when someone (hypothetically) rings the bell. Unlock the door or start stream (to check who it is before unlocking):
 
2.	Reference Material Design topics used for which components
    - MaterialButtons to display icons inside Button
    - DrawerLayout and NavigationView to display a Navigation drawer
    - AppBarLayout and CollapsingBarLayout to display Material toolbar
    - Material icons which can be found at material.io/icons
    (Links to every component is in the description part of the report)

## Modules
1.	Block diagram/description of modules of the app (Use Light Theme for viewing or simply drag the images into a new tab)

    Android app:
    
    <img src="https://imgur.com/ol9vlRY.png" width="946" height="932">

    Firebase Cloud Function:
    
    <img src="https://imgur.com/VkhYans.png" width="277" height="522">

    ESP8266 code description:

    The ESP8266 chip is hardcoded to setup to a specific WiFi network with a specific IP address so that it is easy for other devices to connect to it. Without a static IP address, the chip may connect to the network with different IP addresses each time which will make it hard for other devices to find the chip in the first place.
    An HTTP web server is started so that the controller can send and receive HTTP requests.
    The controller is connected to a relay which will toggle the electronic door lock on or off and a push button which acts as a doorbell. Pushing on the doorbell will send a notification to the Android app saying that someone is at the door and give the user to unlock the door or start the camera.
    In the setup function of the code the controller connects to the WiFi network and starts the HTTP web server. In the loop part of the code the controller is set up to listen for HTTP requests and the state of the push button. When push button is HIGH, the chip sends a POST request to the Firebase Cloud Function to send the notification to the Android app.
    When the controller received a lock or unlock request it simply toggles the state of the relay pin connected to it, lock or unlocking the door lock appropriately.

2.	Future recommendations

    Due to time constraints, I could not implement facial recognition through the ESP32-CAM’s camera stream. We would like for it to be added in the future work.
    Also, I was not able to figure out how to make the devices communicate over the Internet rather than the local network. Enabling the project to work over the internet will allow the user to control his entire door lock security system from anywhere in the world (with a working internet connection) rather than only inside his local network ie the house or office etc.
