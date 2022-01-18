# Twilio Verify Android

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.twilio/twilio-verify-android/badge.svg?style=svg) ](https://maven-badges.herokuapp.com/maven-central/com.twilio/twilio-verify-android)
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
* [Clear local storage](#ClearLocalStorage)
* [Contributing](#Contributing)
* [License](#License)

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

Ensure that you have `mavenCentral` listed in your project's buildscript repositories section:
```groovy
buildscript {
    repositories {
        mavenCentral()
        ...                
    }
}
```
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

* Configure a [Push Credential](https://www.twilio.com/docs/verify/quickstarts/push-android#create-a-push-credential) for the sample app, using the same [Firebase project](#FirebaseConfiguration) you configured
* Configure a [Verify Service](https://www.twilio.com/docs/verify/quickstarts/push-android#create-a-verify-service-and-add-the-push-credential), using the Push Credential for the sample app
* Go to: https://www.twilio.com/code-exchange/verify-push-backend
* Use the `Quick Deploy to Twilio` option
  - You should log in to your Twilio account
  - Enter the Verify Service Sid you created above, you can find it [here](https://www.twilio.com/console/verify/services)
  - Deploy the application
  - Press `Go to live application`
  - You will see the start page. Copy the url and replace `index.html` with `access-token`.(e.g. https://verify-push-backend-xxxxx.twil.io/access-token). This will be your `Access Token generation URL`

<a name='UsingSampleApp'></a>

## Using the sample app

### Adding a factor
* Press Create factor in the factor list (main view)
* Enter the identity to use. This value should be an UUID that identifies the user to prevent PII information use
* Enter the Access token URL (Access token generation URL, including the path, e.g. https://verify-push-backend-xxxxx.twil.io/access-token)
* Decide if you want to enable push notifications for challenges associated to this factor. If you disable this option, push notifications will not be sent and you will get the factor's pending challenges only in the factor screen (pressing the factor)
* Press Create factor
* Copy the factor Sid

### Sending and updating a challenge
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

#### Silently approve challenges

You can silently approve challenges when your app already knows that the user is trying to complete an action (actively logging in, making a transaction, etc.) on the same device as the registered device that is being challenged.

You can enable the option "Silently approve challenges" for a factor. After enabling it, every challenge received as a push notification when the app is in foreground for that factor will be silently approved, so user interaction is not required. The option will be saved for the session, so the selection will not be persisted.

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
Network | 60401 | Exception while calling the API
Mapping | 60402 | Exception while mapping an entity
Storage | 60403 | Exception while storing/loading an entity
Input | 60404 | Exception while loading input
Key Storage | 60405 | Exception while storing/loading key pairs
Initialization | 60406 | Exception while initializing an object
Authentication Token | 60407 | Exception while generating token

### Getting Verify API errors
You can control Verify API error codes listed [here](https://www.twilio.com/docs/api/errors) by following the next example:

```
twilioVerify.createFactor(factorPayload, { factor ->
  // Success
}, { exception ->
  (exception.cause as? NetworkException)?.failureResponse?.apiError?.let {
    // Gets Verify API error response
    Log.d(TAG, "Code: ${it.code} - ${it.message}")
  }
})
```

Check an example [here](https://github.com/twilio/twilio-verify-android/blob/main/sample/src/main/java/com/twilio/verify/sample/view/factors/create/CreateFactorFragment.kt#L147)

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

<a name='ClearLocalStorage'></a>

## Clear local storage
You can clear local storage calling the `TwilioVerify.clearLocalStorage` method:
```
twilioVerify.clearLocalStorage {
  // Operation finished
}
```
Note: Calling this method will not delete factors in **Verify Push API**, so you need to delete them from your backend to prevent invalid/deleted factors when getting factors for an identity.

<a name='Contributing'></a>

## Contributing
This project wolcomes contributions. Please check out our [Contributing guide](./CONTRIBUTING.md) to learn more on how to get started.

<a name='License'></a>

## License
[Apache © Twilio Inc.](./LICENSE)

