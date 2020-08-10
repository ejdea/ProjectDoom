# ProjectDoom
CS467: Capstone | Oregon State University | Summer 2020
<br>
Authors: Martin Edmunds, Lee Taylor Rice, Edmund Dea
<br />
<br />
Project Doom is an Android mobile game titled Doodle Maze. This app converts a hand drawn maze into a playable Unity game. The user must draw the maze on white, unlined paper and use either a black/blue pen or a dark pencil.
<br />
<br />
Development Environment Setup  <br />
1)	Install Git (https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)  <br />

2)	Android Studio (https://developer.android.com/studio/install)  <br />

3)	Install Unity and Unity Hub (https://unity3d.com/get-unity/download)  <br />

4)	Clone Project Doom  <br />
a.	git clone git@github.com:ejdea/ProjectDoom.git  <br />

5)	Open Android Studio  <br />
a.	Click on “Open an Existing Project”.  Android will prompt “Open File or Project”.  <br />
b.	Enter in the path to the ProjectDoom\DoodleMaze in the Project Doom repository  <br />
c.	Android Studio will open the Android component of Project Doom  <br />
d.	Select OK  <br />
e.	Connect a physical device with Android Debug Bridge enabled or setup an emulator in Android Studio under Tools  AVD Manager  Create Virtual Device  <br />
f.	Select a Target device from the Toolbar (shown right in Figure 1)  <br />
g.	To deploy the app to a device, click the green Play icon to build in the toolbar  <br />

6)	Open Unity Hub  <br />
a.	Click Add. Unity will prompt “Select a project to open”.  <br />
b.	Enter in the path to the ProjectDoom\UnityComponent in the Project Doom repository.  <br />
c.	Click Select Folder  <br />

7)	Add Firebase to the Unity project  <br />
a.	Download the Firebase Unity SDK (https://firebase.google.com/download/unity)  <br />
b.	Extract firebase_unity_sdk\dotnet4\FirebaseAuth.unitypackage  <br />
c.	Extract firebase_unity_sdk\dotnet4\FirebaseFirestore.unitypackage  <br />
d.	In Unity, click on Assets -> Import Package -> Custom Package -> Select “FirebaseAuth.unitypackage” -> Select Open -> Wait until Unity finishes extracting the package -> Unity will prompt “Import Unity Package” -> Click Import  <br />
e.	In Unity, click on Assets -> Import Package -> Custom Package -> Select “FirebaseFirestore.unitypackage” -> Select Open -> Wait until Unity finishes extracting the package -> Unity will prompt “Import Unity Package” -> Click Import  <br />
f.	In Unity, click on Assets -> Project Settings -> Player -> Publishing Settings -> Under the Minify sub-category, set Release to “Gradle (Experimental)”  <br />

8)	Build and run Unity  <br />
a.	Connect a physical device via USB cable to your desktop computer  <br />
b.	Click on File -> Build Settings -> Select Android -> Click on Switch Platform (if necessary)  <br />
c.	Click on File -> Build Settings -> Build and Run.  Alternatively, click on File -> Build and Run.  <br />
