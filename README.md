# Classroom Android App

ClassRoom is an app which will allow teachers to create virtual classroom for their students where they can upload syllabus, notes, subject related information and other notices online. 
Students will be notified for the updates and can check that information anytime anywhere using the app.

## Configuration
1. Create fingerprint on local PC:
```
keytool -exportcert -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore
```
2. Add fingerprint on [firebase consolse](https://console.firebase.google.com/u/0/project/classroom-1548585532344/settings/general/android:com.classroom.classroom1)


3. Download google.json and place in `app/src/google-services.json`


## Screenshots
<span>
  <image src ="screenshots/Screenshot_1552993424.png" height="414">
  <image src ="screenshots/Screenshot_1552994422.png" height="414">
  <image src ="screenshots/Screenshot_1552994771.png" height="414">
  <image src ="screenshots/Screenshot_1552994918.png" height="414">
  <image src ="screenshots/Screenshot_1552994951.png" height="414">
  <image src ="screenshots/Screenshot_1552994965.png" height="414">
</span>
  

## Built with
- [Firebase](https://firebase.google.com/)
