# Introduction #

The key document for a maven release is:
https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-5.Prerequisites


# Details #

Basically all I need to do is:

```
mvn clean deploy
mvn release:clean
mvn release:prepare
mvn release:perform

Then, Go to https://oss.sonatype.org
Login to the Nexus UI.
Go to Staging Repositories page.
Select a staging repository.
Click the Close button.
THEN Click the Release button and it should also appear in maven central etc
```