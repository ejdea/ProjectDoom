# ProjectDoom
CS467: Capstone | Oregon State University | Summer 2020
<br>
Authors: Martin Edmunds, Lee Taylor Rice, Edmund Dea

Development Environment Setup
1)	Install Git (https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)

2)	Android Studio (https://developer.android.com/studio/install)

3)	Install Unity and Unity Hub (https://unity3d.com/get-unity/download)

4)	Clone Project Doom 
a.	git clone git@github.com:ejdea/ProjectDoom.git 

5)	Open Android Studio
a.	Click on “Open an Existing Project”.  Android will prompt “Open File or Project”.  
b.	Enter in the path to the ProjectDoom\DoodleMaze in the Project Doom repository
c.	Android Studio will open the Android component of Project Doom
d.	Select OK
e.	Connect a physical device with Android Debug Bridge enabled or setup an emulator in Android Studio under Tools  AVD Manager  Create Virtual Device
f.	Select a Target device from the Toolbar (shown right in Figure 1)
g.	To deploy the app to a device, click the green Play icon to build in the toolbar

6)	Open Unity Hub
a.	Click Add. Unity will prompt “Select a project to open”.
b.	Enter in the path to the ProjectDoom\UnityComponent in the Project Doom repository.
c.	Click Select Folder

7)	Add Firebase to the Unity project
a.	Download the Firebase Unity SDK (https://firebase.google.com/download/unity)
b.	Extract firebase_unity_sdk\dotnet4\FirebaseAuth.unitypackage
c.	Extract firebase_unity_sdk\dotnet4\FirebaseFirestore.unitypackage
d.	In Unity, click on Assets  Import Package  Custom Package  Select “FirebaseAuth.unitypackage”  Select Open  Wait until Unity finishes extracting the package  Unity will prompt “Import Unity Package”  Click Import
e.	In Unity, click on Assets  Import Package  Custom Package  Select “FirebaseFirestore.unitypackage”  Select Open  Wait until Unity finishes extracting the package  Unity will prompt “Import Unity Package”  Click Import
f.	In Unity, click on Assets  Project Settings  Player  Publishing Settings  Under the Minify sub-category, set Release to “Gradle (Experimental)”

8)	Build and run Unity
a.	Connect a physical device via USB cable to your desktop computer
b.	Click on File  Build Settings  Select Android  Click on Switch Platform (if necessary)
c.	Click on File  Build Settings  Build and Run.  Alternatively, click on File  Build and Run.
