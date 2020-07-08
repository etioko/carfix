const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.pushNotificationData  = functions.database.ref('/Treatments/{id}/exitDate')
    .onWrite((change, context) => {
   
      // Grab the current value of what was written to the Realtime Database.
      const original = change.after.val();
	  console.log('params', context);
	  console.log('Push Nutification', context.params.id, original);
	  
	  const key = context.params.id;

	  // Create a notification
	const payload = {
    notification: {
        title: "עדכון",
        body: "זמן סיום הטיפול התעדכן",
    },
    data: {
        data_type: "direct_message",
    }
};	
	
	//Create an options object that contains the time to live for the notification and the priority
    const options = {
      priority: 'high',
      timeToLive: 24 * 60 * 60
    };
	
	console.log('Admin:', admin);
	
	  return admin.database().ref('Treatments/' + key)
      .once('value')
      .then(snapshot => {
        const token = snapshot.val().tokenClient;
		console.log('Token: ', token);
		
        return admin.messaging().sendToDevice(token, payloadת)
		.then(function(response)
		{
			console.log('Successfully sent message:', response);
			return true;
		})
      }).catch(error => {
        console.log('Error sending message:', error);
        return false;
      });
	});
	