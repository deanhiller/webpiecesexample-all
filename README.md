# webpieces-example

BIG NOTE:
This project is a LEGACY project that every time we release webpieces that has breaking changes, we
MUST ALSO upgrade this project.  In fact, if we don't, the webpieces build doesn't work.

BETTER YET:
This means, the history of this projects documents the evolution of switching from which methods to which
methods when we decide to release with breaking changes.  This makes it easier on people that upgrade to 
check the git history and compare

Example project for using the webserver part of webpieces

./gradlew test - run all tests excluding tests with @Ignore like the selenium one
./gradlew assembleDist - creates a distribution as a zip and tar format

./gradlew tasks - lists all the available targets like the ones above
