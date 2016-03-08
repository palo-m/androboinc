# Building

The project can be built directly by Gradle (useful for machine-builds) or imported to Android Studio (convenient for editing).


## A. Using Gradle

1. Install Android SDK

   See https://developer.android.com/sdk/installing/index.html?pkg=tools
   Don't forget to make sure that the [system requirements](https://developer.android.com/sdk/index.html#Requirements) are met.

2. Clone the repository

   ```
   git clone https://github.com/palo-m/androboinc.git
   cd androboinc
   ```

3. Set up the path to the installed SDK

   Assuming SDK was installed to `/opt/android-sdk-linux` it can be accomplished by:
   ```
   echo 'sdk.dir=/opt/android-sdk-linux' > local.properties
   ```

4. Build the debug binary

   ```
   ./gradlew assembleDebug
   ```

   After successful build, the apk is located in AndroBOINC/build/outputs/apk/.
   It can be loaded to emulator or to real Android device and run.


## B. Using Android Studio

1. Install Android Studio

   See https://developer.android.com/sdk/index.html

2. Import project into Android Studio

  * Start Android Studio
  * Choose `Check out project from Version Control`, then select `Git` from drop-down menu
  * Fill `https://github.com/palo-m/androboinc.git` in `Git Repository URL`
  * Select `Clone` button
  * Open the new project in Studio and wait for gradle initialization

3. Build the debug binary

  * Select `Build > Make Project`
  * If all went fine, select `Build > Build APK`
  * The apk is located in AndroBOINC/build/outputs/apk/


# Usage

See [Wiki](https://github.com/palo-m/androboinc/wiki)


