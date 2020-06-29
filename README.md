# Twilio Verify Android

## Table of Contents

* [About](#About)
* [Dependencies](#Dependencies)
* [Requirements](#Requirements)
* [Installation](#Installation)
* [Usage](#Usage)
* [Running the Sample app](#SampleApp)
* [Running the sample backend](#SampleBackend)
* [Using the sample app](#UsingSampleApp)
* [Errors](#Errors)

<a name='About'></a>

## About
Twilio Verify Push SDK helps you verify users by adding a low-friction, secure, cost-effective, "push verification" factor into your own mobile application. This fully managed API service allows you to seamlessly verify users in-app via a secure channel, without the risks, hassles or costs of One-Time Passcodes (OTPs).
This project provides an SDK to implement Verify Push for your Android app.

<a name='Dependencies'></a>

## Dependencies

None

<a name='Requirements'></a>

## Requirements
* Android Studio 3.6 or higher
* Java 8
* Android 6.0 (23) SDK or higher
* Gradle 5.6.4
* Kotlin 1.3.72 (For Kotlin projects)

<a name='Installation'></a>

## Installation

### Add library
In the build.gradle file, add the library

```implementation 'com.twilio:twilio-verify-android:(insert latest version)'```

### Add firebase configuration
If you want to receive challenges as push notifications, you should add a firebase configuration to your project
* Add a project in Firebase to use cloud messaging for an application ID
* Add the google-services.json file to your project

More info [here](https://firebase.google.com/docs/android/setup#console)

<a name='Usage'></a>

## Usage

See [Verify Push Quickstart](https://www.twilio.com/docs/verify/quickstarts/push-android) for a step-by-step guide to using this SDK in a basic Verify Push implementation.

<a name='SampleApp'></a>

## Running the Sample app

### To run the Sample App:
* Clone the repo
* Follow the steps from [Firebase configuration](#FirebaseConfiguration)
* Get the Enrollment JWE generation URL from your backend [(Running the Sample backend)](#SampleBackend). You will use it for creating a factor
* Run the `sample` module using `release` as build variant

<a name='FirebaseConfiguration'></a>

### Firebase configuration

In order to run the sample app, you have to create a project and application in Firebase
* Add a project in Firebase to use cloud messaging for an application ID (you can use `com.twilio.verify.sample`)
* Move the google-services.json file you downloaded from Firebase console into the root of `sample` directory.

<a name='SampleBackend'></a>

## Running the Sample backend

### Create a Notify service
You will need a notify service to send push notifications to your app
* Go to [Push Credentials](https://www.twilio.com/console/notify/credentials)
* Click the `Add (+)` button 
* Enter a friendly name
* Select `FCM push credentials` as type
* Enter the `FCM Secret`. The value is the app's `Server key`. You can find it in your app's Firebase project settings, Cloud messaging
* Click the `Create` button
* Go to [Notify Services](https://www.twilio.com/console/notify/services)
* Create a `Notify service` for the app
* For `FCM CREDENTIAL SID`, select the created FCM credential
* Save changes
* Copy the notify service sid

### Create a verify service
* Get your account Sid and Auth Token from [API credentials](https://www.twilio.com/console/project/settings)
* Create a verify service calling the endpoint:
```
curl -X POST https://verify.twilio.com/v2/Services \
--form 'FriendlyName=Your service name' \
--form 'Push={
    "notify_service_sid": "IS00000000000000000000000000000000"
  }' \
-u ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX:your_auth_token
```
* Copy the verify service Sid, it is the `sid` field in the response

### To run the sample backend
* Clone this repo: https://github.com/twilio/verify-push-sample-backend
* Run the steps in the [README file](https://github.com/twilio/verify-push-sample-backend/blob/master/README.md)
    * Use your previously created verify service Sid for `TWILIO_VERIFY_SERVICE_SID`

<a name='UsingSampleApp'></a>

## Using the sample app

### Adding a factor
* Press Create factor in the factor list (main view)
* Enter the entity identity to use. This value should be an UUID that identifies the user to prevent PII information use
* Enter the enrollment URL (Enrollment JWE generation URL, including the path)
* Press Create factor
* Copy the factor Sid

### Sending a challenge
* Go to Create Push Challenge page (/challenge path in your sample backend)
* Enter the entity `identity` you used in factor creation
* Enter the `Factor Sid` you added
* Enter a `message`. You will see the message in the push notification and in the challenge view
* Enter details to the challenge. You will see them in the challenge view. You can add more details using the `Add more Details` button
* Press `Create challenge` button
* You will receive a push notification showing the challenge message in your device. 
* The app will show the challenge info below the factor information, in a `Challenge` section
* Approve or deny the challenge
* After the challenge is updated, you will see the challenge status in the backend's `Create Push Challenge` view

<a name='Errors'></a>

## Errors
Types | Code | Description
---------- | ----------- | -----------
Network | 68001 | Exception while calling the API
Mapping | 68002 | Exception while mapping an entity
Storage | 68003 | Exception while storing/loading an entity
Input | 68004 | Exception while loading input
Key Storage | 68005 | Exception while storing/loading key pairs
Initialization | 68006 | Exception while initializing an object
Authentication Token | 68007 | Exception while generating token