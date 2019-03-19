Project Detail: [Google Drive Folder](https://drive.google.com/drive/folders/1NYKdhtDSegwmDQCyhrtBdMgvRjnFViso)

UI Design: [Marvel Mockup](https://marvelapp.com/4bcb2j0/screen/44397618)

Synopsis: [Google Doc](https://docs.google.com/document/d/1g0thaW45Mb7ABqZ0b1zWyRPY2QJeZRYqvlmNdb9AybA/edit)

Important steps:

1. Create fingerprint on local PC:
```
keytool -exportcert -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore
```
2. Add fingerprint on [firebase consolse](https://console.firebase.google.com/u/0/project/classroom-1548585532344/settings/general/android:com.classroom.classroom1)


3. Download google.json and place in `app/src/google-services.json`