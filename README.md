# dhis2-android-trackercapture
Mobile Helping Babies Survive trackercapture powered by DHIS 2

# Testing
To run with our database, you can use the following:
- Server: http://gw174.iu.xsede.org
- Username: admin
- Password: district

# Initialize the submodules
To initialize the SDK as a submodule, from the command line you can clone as:
git clone --recursive
or run 
git submodule update --init
on existing local repository

To unininitialize and clean submodules, run git submodule deinit --all

# How to Download and Set up in Android Studio
Stepwise explanation on how to set it up: https://docs.google.com/document/d/141uX2IKA7NRouaYDAPUhJu29WRmiw7UxwNtXSj_iOVA/edit?usp=sharing 

To successfully build and run this project, the dhis2-android-sdk is required.

The dhis2-android-sdk project https://github.com/dhis2/dhis2-android-sdk folder should be in the same root folder as the dhis2-android-trackercapture.

# Running the App
 
Open Android Studio, select "Open an existing Android Studio project", and select the build.gradle in dhis2-android-trackercapture/
