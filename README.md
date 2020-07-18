# ProjectDoom
CS467: Capstone | Oregon State University | Summer 2020
<br>
Authors: Martin Edmunds, Lee Taylor Rice, Edmund Dea


To order to reduce the size of this repository, our team removed some larger files that will need to be installed from internet sources.  

Please use the following steps to restore those files:

1. ) Complete the steps from the following page to install the OpenCV 3.4.3 library: https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c

2. ) Install the following Firebase SDKs in accordance with the steps here: https://firebase.google.com/docs/unity/setup#manual_installation
 a) FirebaseAnalytics.unitypackage
 b) FirebaseAuth.unitypackage
 c) FirebaseStorage.unitypackage

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
