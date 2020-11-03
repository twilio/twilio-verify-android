# Twilio Verify Android

[![Download](https://api.bintray.com/packages/twilio/releases/twilio-verify-android/images/download.svg) ](https://bintray.com/twilio/releases/twilio-verify-android/_latestVersion)
[![CircleCI](https://circleci.com/gh/twilio/twilio-verify-android.svg?style=shield&circle-token=e5c76e91c300be6dcdd3db05a57bb4f01304415e)](https://circleci.com/gh/twilio/twilio-verify-android)
[![codecov](https://codecov.io/gh/twilio/twilio-verify-android/branch/main/graph/badge.svg?token=o1ZcrAfoc0)](https://codecov.io/gh/twilio/twilio-verify-android)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![License](https://img.shields.io/badge/License-Apache%202-blue.svg?logo=law)](https://github.com/twilio/twilio-verify-android/blob/main/LICENSE)

## Table of Contents

* [About](#About)
* [Dependencies](#Dependencies)
* [Requirements](#Requirements)
* [Documentation](#Documentation)
* [Installation](#Installation)
* [Usage](#Usage)
* [Running the Sample app](#SampleApp)
* [Running the sample backend](#SampleBackend)
* [Using the sample app](#UsingSampleApp)
* [Logging](#Logging)
* [Errors](#Errors)
* [Update factor's push token](#UpdatePushToken)
* [Delete a factor](#DeleteFactor)

<a name='About'></a>

## About
Twilio Verify Push SDK helps you verify users by adding a low-friction, secure, cost-effective, "push verification" factor into your own mobile application. This fully managed API service allows you to seamlessly verify users in-app via a secure channel, without the risks, hassles or costs of One-Time Passcodes (OTPs).
This project provides an SDK to implement Verify Push for your Android app.

<a name='Dependencies'></a>

## Dependencies

None

<a name='Requirements'></a>

## Requirements
* Android Studio 4.0 or higher
* Java 8
* Android 6.0 (23) SDK or higher
* Gradle 6.3
* Kotlin 1.3.72

<a name='Documentation'></a>

## Documentation
[SDK API docs](https://twilio.github.io/twilio-verify-android/latest/verify/index.html)

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
* Get the Access Token generation URL from your backend [(Running the Sample backend)](#SampleBackend). You will use it for creating a factor
* Run the `sample` module using `release` as build variant

<a name='FirebaseConfiguration'></a>

### Firebase configuration

In order to run the sample app, you have to create a project and application in Firebase
* Add a project in Firebase to use cloud messaging for an application ID (you can use `com.twilio.verify.sample`)
* Move the google-services.json file you downloaded from Firebase console into the root of `sample` directory.

<a name='SampleBackend'></a>

## Running the Sample backend

* Clone this repo: https://github.com/twilio/verify-push-sample-backend
* Configure a [Push Credential](https://www.twilio.com/docs/verify/quickstarts/push-android#create-a-push-credential) for the sample app, using the same [Firebase project](#FirebaseConfiguration) you configured
* Configure a [Verify Service](https://www.twilio.com/docs/verify/quickstarts/push-android#create-a-verify-service-and-add-the-push-credential), using the Push Credential for the sample app
* Run the steps in the [README file](https://github.com/twilio/verify-push-sample-backend/blob/master/README.md)

<a name='UsingSampleApp'></a>

## Using the sample app

### Adding a factor
* Press Create factor in the factor list (main view)
* Enter the identity to use. This value should be an UUID that identifies the user to prevent PII information use
* Enter the Access token URL (Access token generation URL, including the path, e.g. https://yourapp.ngrok.io/accessTokens)
* Press Create factor
* Copy the factor Sid

### Sending a challenge
* Go to Create Push Challenge page (/challenge path in your sample backend)
* Enter the `identity` you used in factor creation
* Enter the `Factor Sid` you added
* Enter a `message`. You will see the message in the push notification and in the challenge view
* Enter details to the challenge. You will see them in the challenge view. You can add more details using the `Add more Details` button
* Press `Create challenge` button
* You will receive a push notification showing the challenge message in your device.
* The app will show the challenge info below the factor information, in a `Challenge` section
* Approve or deny the challenge
* After the challenge is updated, you will see the challenge status in the backend's `Create Push Challenge` view

<a name='Logging'></a>

## Logging
By default, logging is disabled. To enable it you can either set your own logging services by implementing [LoggerService](https://github.com/twilio/twilio-verify-android/blob/main/verify/src/main/java/com/twilio/verify/logger/LoggerService.kt) and calling `addLoggingService` (note that you can add as many logging services as you like) or enable the default logger service by calling `enableDefaultLoggingService`. Your multiple implementations and the default one can work at the same time, but you may just want to have it enabled during the development process, it's risky to have it turned on when releasing your app.

### Setting Log Level
You may want to log only certain processes that are happening in the SDK, or you just want to log it all, for that the SDK allows you to set a log level.
* Error: reports behaviors that shouldn't be happening.
* Info: warns specific information of what is being done.
* Debug: detailed information.
* Networking: specific data for the networking work, such as request body, headers, response code, response body.
* All: Error, Info, Debug and Networking are enabled.

### Usage
To start logging, enable the default logging service or/and pass your custom implementations
```
TwilioVerify.Builder(applicationContext).apply {
  if (BuildConfig.DEBUG) {
    enableDefaultLoggingService(LogLevel.Debug)
    addLoggingService(MyOwnLoggerService1())
    addLoggingService(MyOwnLoggerService2())
  }
}.build()
```

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

<a name='UpdatePushToken'></a>

## Update factor's push token
You can update the factor's push token in case it changed, calling the `TwilioVerify.updateFactor` method:
```
val updateFactorPayload = UpdatePushFactorPayload(factorSid, newPushtoken)
twilioVerify.updateFactor(updateFactorPayload, { factor ->
  // Success
}, { exception ->
  // Error
})
```

Firebase provides a method to be notified when the push token is updated. See [FirebasePushService](https://github.com/twilio/twilio-verify-android/blob/main/sample/src/main/java/com/twilio/verify/sample/push/FirebasePushService.kt#L42) in the sample app. You should update the push token for all factors.

<a name='DeleteFactor'></a>

## Delete a factor
You can delete a factor calling the `TwilioVerify.deleteFactor` method:
```
twilioVerify.deleteFactor(factorSid, {
  // Success
}, { exception ->
  // Error
})
```
