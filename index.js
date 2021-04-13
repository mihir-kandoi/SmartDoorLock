const functions = require("firebase-functions");

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.

// The Firebase Admin SDK to access Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.https.onRequest((req, res) => {
  
  // Check for POST request
  if(req.method !== "POST"){
    res.status(400).send('Please send a POST request');
    return;
    }

    var message = {
        data: {
          title: 'Someone is at the door',
          body: 'Always check who it is before unlocking your door :)'
        },
        android: {
          priority: 'high',
        },
        token: req.body.token
      };

    // Send a message to the device corresponding to the provided
    // registration token.
    admin.messaging().send(message)
        .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
            res.status(200).send('Message sent successfully');
        })
        .catch((error) => {
            console.log('Error sending message:', error);
            res.status(500).send("Error sending message");
        });
});