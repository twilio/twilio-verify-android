# Twilio Verify Android

## Table of Contents

* [About](#About)
* [Dependencies](#Dependencies)
* [Requirements](#Requirements)
* [Terms](#Terms)
* [Installation](#Installation)
* [Running the Sample app](#SampleApp)
* [Running the sample backend](#SampleBackend)
* [Usage](#Usage)

<a name='About'></a>

## About
Verify Push enables developers to implement secure push authentication without giving up privacy and control. This project provides a SDK to create and use verify push

<a name='Dependencies'></a>

## Dependencies

None

<a name='Requirements'></a>

## Requirements
* Android Studio 3.4 or higher
* Java 1.8.0
* Android 6.0 (23) SDK or higher
* Gradle 5.4.1
* Kotlin 1.3.61

<a name='Terms'></a>

## Terms

| Term         | Definition |
| :----------- | :---------- |
| Service      | Scope the resources. It contains the configurations for each factor |
| Entity       | Represents anything that can be authenticated in a developer’s application. Like a User |
| Factor       | It is an established method for sending authentication Challenges. Like SMS, Phone Call, Push |
| Challenge    | It is a verification attempt sent to an Entity |

<a name='Installation'></a>

## Installation

### Add library
In the build.gradle file, add the library

```implementation "com.twilio:verify:1.0"```

### Add firebase configuration
If you want to receive challenges as push notifications, you should add a firebase configuration to your project
* Add a project in Firebase to use cloud messaging for an application ID
* Add the google-services.json file to your project
More info [here](https://firebase.google.com/docs/android/setup#console)

<a name='SampleApp'></a>

## Running the Sample app

### To run the Sample App:
* Clone the repo
* Follow the steps from [Firebase configuration](#FirebaseConfiguration)
* Get your account Sid and Auth Token from [API credentials](https://www.twilio.com/console/project/settings)
* Replace the `ACCOUNT_SID` (account Sid) and `AUTH_TOKEN` (Auth Token) values in the sample's build.gradle file with the values for your account, for [release build type](sample/build.gradle#L28)
* Run the `sample` module using `release` as build variant

<a name='FirebaseConfiguration'></a>

### Firebase configuration

In order to run the sample app, you have to create a project and application in Firebase
* Add a project in Firebase to use cloud messaging for an application ID (you can use `com.twilio.verify.sampleapp`)
* Move the google-services.json file you downloaded from Firebase console into the root of `sample` directory.

<a name='SampleBackend'></a>

## Running the Sample app

### Create a Notify service
You will need a notify service to send push notifications to your app
* Go to [Push Credentials](https://www.twilio.com/console/notify/credentials)
* Enter a friendly name
* Select as type `FCM push credentials`
* Enter the `FCM Secret`. The value is the app's `Server key`. You can find it in your app's settings, Cloud messaging
* Go to [Notify Services](https://www.twilio.com/console/notify/services)
* Create a `Notify service` for the app
* For `FCM CREDENTIAL SID` select the created FCM credential
* Save changes

### Create a verify service
* Get your account Sid and Auth Token from [API credentials](https://www.twilio.com/console/project/settings)
* Create a verify service calling the endpoint:
```
curl --location --request POST 'https://authy.twilio.com/v1/Services' \
--form 'FriendlyName=<The service name>' \
--form 'Push={
  "notify_service_sid": "<The notify service Sid>"
}
--user <Account Sid>:<Auth token> 
'
```

### Running the sample backend
* Clone this repo: https://github.com/twilio/verify-push-sample-backend
* Run the steps in the README file
    * To run the application, you'll need to gather your Twilio account credentials and configure them in a file named `.env`. To create this file from an example template, do the following in your Terminal:
    ```cp .env.example .env```
    * Open the .env file in your favorite text editor and configure the following values:

        | Config Value               | Description                                                                                 |
        | :------------------------- | :------------------------------------------------------------------------------------------ |
        |`TWILIO_ACCOUNT_SID`        | The account Sid from [API credentials](https://www.twilio.com/console/project/settings)     |
        |`TWILIO_AUTH_TOKEN`         | The auth token from [API credentials](https://www.twilio.com/console/project/settings)      |
        |`TWILIO_API_KEY`            | Create an API key here: [API Keys](https://www.twilio.com/console/project/api-keys)         |
        |`TWILIO_API_SECRET`         | Get the API secret from the created API key.                                                |
        |`TWILIO_VERIFY_SERVICE_SID` | Your verify service                                                                         |
    * Run the sample backend
        * If you don’t have yarn, install it following this: [Install yarn](https://classic.yarnpkg.com/en/docs/install#mac-stable)
        * Install the dependencies in the project folder running: ```yarn install```
        * Run the sample backend running: ```yarn start```

<a name='Usage'></a>

## Usage

### Adding a factor
* Open http://localhost:3000/ in your browser to validate the sample app is running
* Create a publicly accessible URL using a tool like [ngrok](https://ngrok.com/) to send HTTP/HTTPS traffic to a server running on your localhost
```ngrok https 3000```
* Enter the entity identity to use. This value should be an UUID that identifies the user to prevent PII information use
* Enter the public URL generated in JWT url
* Press Create factor
* Copy the factor Sid

### Sending a challenge
* Go to Create Push Challenge link
* Enter the entity identity you used in factor creation
* Enter the factor Sid you added
* Enter a message. You will see the message in the push notification and in the challenge view
* Enter details to the challenge. You will see them in the challenge view. You can add more details using the Add more details button
* Press Create challenge button
* You will receive a push notification showing the challenge message in your device. 
* The app will show the challenge info below the factor information, in a `Challenge` section
* Approve or deny the challenge
* After the challenge is updated, you will see the challenge status in the backend's `Create Push Challenge` view

