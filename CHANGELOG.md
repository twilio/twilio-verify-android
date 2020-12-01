# 0.2.0 (2020-11-03)

### Features
- Provide method in TwilioVerify to clear local storage ([9a58f1f](https://github.com/twilio/twilio-verify-android/commit/9a58f1f8fb14a8d25ee4450ee2b6dfb889f4486a))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 68.7KB          |
| x86_64          | 68.7KB          |
| armeabi-v7a     | 68.7KB          |
| arm64-v8a       | 68.7KB          |
| universal       | 68.7KB          |


# 0.1.0 (2020-10-13)

### Features
- Use encrypted storage for factors (#145) ([6ea7c95](https://github.com/twilio/twilio-verify-android/commit/6ea7c9585ade4b771341ae62e1fbc4b22a0ec3c2))

### Bug fixes
- Delete factor should delete it locally for deleted factors from API (#141) ([11bc7c7](https://github.com/twilio/twilio-verify-android/commit/11bc7c7aa27f4ed6f768382720f35128c31fc051))
- Support new challenge format (#150) ([56a5900](https://github.com/twilio/twilio-verify-android/commit/56a5900d223b8d2b5fa0190d48072f36e119eea4))

### Code refactoring
- Create factor body params ([9adec83](https://github.com/twilio/twilio-verify-android/commit/9adec83a84435cb4df18318ce8f7232f6ee7ccab))
- Update factor body params ([e3dc5ae](https://github.com/twilio/twilio-verify-android/commit/e3dc5aec0b960be38ba9180405f5909a3267e201))

### Building system
- Added github issue templates and code of conduct ([24cee84](https://github.com/twilio/twilio-verify-android/commit/24cee842db8136fd0f2371ab6829fae4372a0ba2))

### Documentation
- Add SDK API docs link, update factor's push token and delete factor sections in readme ([5cf2770](https://github.com/twilio/twilio-verify-android/commit/5cf2770b9957d9950f5e7d953c1b17fd727c44d3))
- SDK size as part of CHANGELOG.md ([19298ea](https://github.com/twilio/twilio-verify-android/commit/19298eacc6108c5d3fe9a1c8027704117f680680))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 68KB            |
| x86_64          | 68KB            |
| armeabi-v7a     | 68KB            |
| arm64-v8a       | 68KB            |
| universal       | 68KB            |


# 0.0.3

### Documentation
- Source files added to the library

# 0.0.2

### Code refactoring
- Rename `enrollmentJwe` to `accessToken` in `PushFactorPayload`
- Rename `entityIdentity` to `identity` in `Factor`

# 0.0.1

### Features
- Version 0.0.1
