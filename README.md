Installations Required:
Install INTELIJ IDEA if you are automation coder 

Install Maven 3.8.4

JDK/JRE 1.8.301

IDE preparation :
Pull code from Github repo and place in workspace
Open Project 

Go to File > Project settings and set Project SDK 1.8
Project Language Level 8 and above

Go to File > settings and Select Plugin
Install Following plugins : Cucumber for Groovy, Cucumber for Java, TestNG, Gherkin

Build Solution using Maven > Install

Right click on Pom.xml and Maven > Reload Project.

Build Solution using Maven > Compile

Create Configuration 
Create TESTNG configuration : RunSuite [Or you can right click on RunSuite and Run Run Suite , once configuration is seen in dropdown, add below parameters to configuration]
Parameters :
    test.Platform web
    test.Environment QA

In RunAutoSuite Class
update tags to glue and run subset of tests from whole pool of tests:

@CucumberOptions(features = "target/test-classes/features/web", monochrome = true, tags = "@web and @regression", plugin = {
"pretty", "html:target/cucumber-report/single",
"json:target/cucumber-report/single/cucumber.json",
"com.spintly.base.support.cucumberEvents.EventsHandler"},
glue = {"com.spintly.web.stepdefinitions", "com.spintly.api", "com.spintly.runner"}
)
where tags = "@web and @regression" need to specify based on type of testing you want to carry out.

To Run from Intelij IDEA: 
Click on Play icon beside the configuration.

To Run from command line or to integrate with batch command in CI/CD tool:
cd {workspace}

D:

"{maven path}\bin\mvn" install exec:java -e -Dtest.Device='device1,device2,device3,device4,device5' -Dtest.Category="demo" -Dtest.Platform="web" -Dtest.Environment="QA" -Dacceptance.test.parallel.count="5"  -Dcucumber.options="--glue com.spintly.web.stepdefinitions --glue com.spintly.api --glue com.spintly.runner --tags '@web and @demo'" 

Note: if total features in a project is X and you can set acceptance.test.parallel.count to X or lower.


Common errors: Current version of browser is not tested with xx version,
To resolve make sure, driver executable on system and browser version is same .
