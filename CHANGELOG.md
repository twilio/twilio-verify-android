# 0.9.0 (2025-11-20)

### Features
- Target Android API 35 ([449d3e2](https://github.com/twilio/twilio-verify-android/commit/449d3e272e62a47664f406e900f3521cf7955963))

### Building system
- Update gems ([596b19d](https://github.com/twilio/twilio-verify-android/commit/596b19d7efe5e050e5640e8921fb5b3b21734871))
- CI update ([93deb89](https://github.com/twilio/twilio-verify-android/commit/93deb89005319ee5e159aa9c7664ce4a9e9f815f))
- update dependencies and pipeline ([b6145fb](https://github.com/twilio/twilio-verify-android/commit/b6145fb1291d12c6ebb19cdb7187f4ee2c8a117c))
- fix publishing task ([4a317b4](https://github.com/twilio/twilio-verify-android/commit/4a317b4d452ac476fcf57a1e416db94fe5c6be67))
- fix publishing pipeline ([6c4d980](https://github.com/twilio/twilio-verify-android/commit/6c4d980608696110a0bd3f7ee8b2c1e8339da8b3))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 699.6KB         |
| x86_64          | 699.6KB         |
| armeabi-v7a     | 699.6KB         |
| arm64-v8a       | 699.6KB         |
| universal       | 699.6KB         |



# 0.8.0 (2022-06-22)

### Features
- Allow sending custom metadata when creating a factor ([71ca610](https://github.com/twilio/twilio-verify-android/commit/71ca61078be40f31799f4909fe25fa69883b540e))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 77.2KB          |
| x86_64          | 77.2KB          |
| armeabi-v7a     | 77.2KB          |
| arm64-v8a       | 77.2KB          |
| universal       | 77.2KB          |


# 0.7.0 (2022-02-24)

### Features
- Improve input error to provide reason of failure (#203) ([d2a9169](https://github.com/twilio/twilio-verify-android/commit/d2a9169940bc921cdef5dfbc765af2e311ed4001))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 76.6KB          |
| x86_64          | 76.6KB          |
| armeabi-v7a     | 76.6KB          |
| arm64-v8a       | 76.6KB          |
| universal       | 76.6KB          |


# 0.6.1 (2022-01-18)

### Bug fixes
- Improved network error to get Verify API error ([b48bdc2](https://github.com/twilio/twilio-verify-android/commit/b48bdc2f76918f48bf6ed90369fa75fc51d6ffd9))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 75.3KB          |
| x86_64          | 75.3KB          |
| armeabi-v7a     | 75.3KB          |
| arm64-v8a       | 75.3KB          |
| universal       | 75.3KB          |


# 0.6.0 (2022-01-03)

### Features
- Validate challenge status before trying to update it. Add validations for empty factor sid or challenge sid. Get decrypt information correctly when the provider is not valid. ([5a68181](https://github.com/twilio/twilio-verify-android/commit/5a681814ef97131fff2578acaa988b2e48d186bc))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 74.3KB          |
| x86_64          | 74.3KB          |
| armeabi-v7a     | 74.3KB          |
| arm64-v8a       | 74.3KB          |
| universal       | 74.3KB          |


# 0.5.0 (2021-11-26)

### Features
- Support notification platform none to allow not sending push token. Factors with notification platform none will not receive push notifications for challenges and polling should be implemented to get pending challenges ([4fd8967](https://github.com/twilio/twilio-verify-android/commit/4fd8967274783fc9088a9c5908df0b78c4e90148))

### Building system
- Update FTL devices ([2535df5](https://github.com/twilio/twilio-verify-android/commit/2535df51a0658d51eff3b347f8c84695b7af5eb5))

### Documentation
- Update documentation to use new sample backend ([5708592](https://github.com/twilio/twilio-verify-android/commit/5708592e02fd78cf0924d0d538e1287f3d63cf05))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 74KB            |
| x86_64          | 74KB            |
| armeabi-v7a     | 74KB            |
| arm64-v8a       | 74KB            |
| universal       | 74KB            |


# 0.4.0 (2021-09-13)

### Features
- Ordering for challenge list ([790fb17](https://github.com/twilio/twilio-verify-android/commit/790fb17a3326b2dc7b62dd9f12af49b43fd1287c))

### Building system
- Create tag after pushing new version (#182) ([93d1b4b](https://github.com/twilio/twilio-verify-android/commit/93d1b4b09a390b5da85cdbdea50cf3cff0553ace))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 73.4KB          |
| x86_64          | 73.4KB          |
| armeabi-v7a     | 73.4KB          |
| arm64-v8a       | 73.4KB          |
| universal       | 73.4KB          |


# 0.3.1 (2021-07-26)

### Bug fixes
- Update error codes (#164) ([308d053](https://github.com/twilio/twilio-verify-android/commit/308d053dab3f367513104cee285321a2d733097e))

### Building system
- Publishing to MavenCentral (#170) ([e638380](https://github.com/twilio/twilio-verify-android/commit/e63838079e94df526a4b78d7dff60a43b5e1ae99))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 72.9KB          |
| x86_64          | 72.9KB          |
| armeabi-v7a     | 72.9KB          |
| arm64-v8a       | 72.9KB          |
| universal       | 72.9KB          |


# 0.3.0 (2020-12-03)

### Features
- SDK Logging (#162) ([39f29b0](https://github.com/twilio/twilio-verify-android/commit/39f29b07b0943a78c90aade9251d7cc6d72f50ee))

### Size impact

| ABI             | APK Size Impact |
| --------------- | --------------- |
| x86             | 72.9KB          |
| x86_64          | 72.9KB          |
| armeabi-v7a     | 72.9KB          |
| arm64-v8a       | 72.9KB          |
| universal       | 72.9KB          |


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
