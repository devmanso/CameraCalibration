## How to run and compile this:

## Step 1, getting the necessary files:

* go to https://opencv.org/releases/
* download 4.6.0 release
* run the exe/binary and follow the prompts

## Step 2, gradle build:

* in the terminal run "./gradlew shadowJar" (or you can configure this in IntelliJ)
* after the build, this will generate a build and .gradle folder, which are needed for the next part
* we will need to place a .dll (or .so file depending on your OS)
* go back to where you extracted your OpenCV files, and go to: build->java->x64->opencv_java460.dll
* (note: if you have an x86 system, go to the x86 directory instead
* copy-paste or move this file into our project's build->libs folder

## Step 3, running the program:

* now that we have our dll file and jar file in the same directory run this command in the terminal
* java -jar .\cvcalib-1.0-SNAPSHOT-all.jar
