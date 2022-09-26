$(document).ready(function() {var formatter = new CucumberHTML.DOMFormatter($('.cucumber-report'));formatter.uri("target/test-classes/features/web/usermanagement/AllUsers.feature");
formatter.feature({
  "name": "Adding a user",
  "description": "",
  "keyword": "Feature",
  "tags": [
    {
      "name": "@web"
    }
  ]
});
formatter.scenario({
  "name": "Creating a user",
  "description": "",
  "keyword": "Scenario",
  "tags": [
    {
      "name": "@web"
    },
    {
      "name": "@demo1"
    }
  ]
});
formatter.before({
  "status": "passed"
});
formatter.step({
  "name": "I am on the Spintly web portal",
  "keyword": "Given "
});
formatter.match({
  "location": "AllUsersSteps.i_am_on_the_spintly_web_portal()"
});
formatter.result({
  "status": "passed"
});
formatter.afterstep({
  "status": "passed"
});
formatter.step({
  "name": "I log in",
  "keyword": "When "
});
formatter.match({
  "location": "AllUsersSteps.i_log_in()"
});
formatter.result({
  "status": "passed"
});
formatter.afterstep({
  "status": "passed"
});
formatter.after({
  "status": "passed"
});
});