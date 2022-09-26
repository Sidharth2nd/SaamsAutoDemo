package com.spintly.web.stepdefinitions.usermanagement;

import com.spintly.base.core.DriverBase;
import com.spintly.web.pages.login.LogInPage;
import com.spintly.web.pages.usermanagementpage.AllUsersPage;
import com.spintly.web.support.WebDriverActions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

public class AllUsersSteps extends DriverBase {
    WebDriverActions actions = new WebDriverActions();
    AllUsersPage allUsersPage = new AllUsersPage();
    LogInPage logInPage = new LogInPage();

    @Given("^I am on the Spintly web portal$")
    public void i_am_on_the_spintly_web_portal()  {
        actions.navigateTo("http://test.smart-access.spintly.com/login");

    }

    @When("^I log in$")
    public void i_log_in()  {
        actions.sendKeys(logInPage.phoneNumber(), "7722082259");
        actions.sendKeys(logInPage.password(), "sidharth123");
        actions.click(logInPage.logIn());

    }

}
