# webpieces-example
  
Generated from project https://github.com/deanhiller/webpieces/releases

Legacy project used for testing backwards compatibility of webpieces project

This project not meant to be checked out by someone not developing on webpieces BUT
if you do, you will need to run ./runAllTesting.sh in webpieces first to generate
a local release that this project will use (OR you will need to change the version
numbers in the build.gradle - two locations)

./gradlew test - run all tests excluding tests with @Ignore like the selenium one
./gradlew assembleDist - creates a distribution as a zip and tar format

./gradlew tasks - lists all the available targets like the ones above
