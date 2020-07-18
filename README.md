# ProjectDoom
CS467: Capstone | Oregon State University | Summer 2020
<br>
Authors: Martin Edmunds, Lee Taylor Rice, Edmund Dea


To order to reduce the size of this repository, our team removed some larger files that will need to be installed from internet sources.  

Please use the following steps to restore those files:

1.) Complete the steps from the following page to install the OpenCV 3.4.3 library: https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c

2.) Install the following Firebase SDKs in accordance with the steps here: https://firebase.google.com/docs/unity/setup#manual_installation
   a. You can download the Firebase Unity SDK here: https://firebase.google.com/download/unity
   b. Extract firebase_unity_sdk_6.15.2.zip
   c. There are 3 .unitypackage files in these directories:
      1. firebase_unity_sdk_6.15.2\firebase_unity_sdk\dotnet4\FirebaseAnalytics.unitypackage
      2. firebase_unity_sdk_6.15.2\firebase_unity_sdk\dotnet4\FirebaseAuth.unitypackage
      3. firebase_unity_sdk_6.15.2\firebase_unity_sdk\dotnet4\FirebaseStorage.unitypackage
   d. Import the 3 .unitypackage's to Unity
      1. Open Unity
      2. Click Assets -> Import Package -> Custom Package
      3. Select the respective .unitypackage that you need to import
      4. Unity will extract the .unitypackage. When it is done, Unity will list all the files in a popup. Then, click the Import button.

________________________________________________________

Alternative Steps:

1) Download 3 patches at https://drive.google.com/file/d/1JonYoQBXA2gMFOdisGGDx4Axc3uYO-na/view?usp=sharing
2) Extract the 3 patches to the root of the ProjectDoom directory
   a. ProjectDoom\0001-Uninstall-Git-LFS.patch
   b. ProjectDoom\0002-UnityComponent-Add-Firebase-Auth-Database-and-Storag.patch
   c. ProjectDoom\0003-DoodleMaze-Add-jniLibs.patch
3) Apply the 3 patches
   a. git am 0001-Uninstall-Git-LFS.patch
   b. git am 0002-UnityComponent-Add-Firebase-Auth-Database-and-Storag.patch
   c. git am 0003-DoodleMaze-Add-jniLibs.patch
