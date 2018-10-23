# Runtastic-Activity-Parser

A simple parser to convert activities from runtastic to a basic GPX format.

## Building
This is a Java Project build with maven.
Build the project with \
 ``` mvn package ```

## Export Runtastic Data
The module allows you to parse runtastic activities. First you have to download all your own data of runtastic. 
Follow the steps below:
1. Login to [runtastic.com](https://www.runtastic.com)
2. Navigate to your profile > Settings. In the settings menu you can 
find a point called "Export". There you can request all your personal data
stored by runtastic. After few minutes you will get a mail with a download link.
3. Downloading the package of your whole personal data. The package contains 
a folder 'Sport-sessions'. This folder includes all your activities in seperated JSON files. 

## Parse JSON Files to GPX
To parse the activity JSON files to GPX files run the build jar like following: \
``` java -jar runtastic-activity-parser /User/bob/Sport-sessions /User/bob/output ``` \
The first argument is the source path and the second the target path.
   