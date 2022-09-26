package com.spintly.web.stepdefinitions.usermanagement;

import com.spintly.base.core.DriverBase;
import com.spintly.web.pages.login.LogInPage;
import com.spintly.web.pages.usermanagementpage.AllUsersPage;
import com.spintly.web.support.WebDriverActions;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
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
    @Then("^I navigate to the dashboard page$")
    public void i_am_on_the_dashboard_page()  {
        actions.click(logInPage.cancel());

    }

    @Then("^I select the organisation$")
    public void i_select_the_organisation()  {
        actions.click(logInPage.selectOrg());
        actions.click(logInPage.mrinqTech());

    }

    @Then("^I click on all users$")
    public void i_click_on_all_users()  {
        actions.click(logInPage.allUser());

    }

    @Then("^I click on add user$")
    public void i_click_on_add_user()  {
        actions.click(logInPage.addUser());
        actions.click(logInPage.addSingleUser());

    }
    @And("^I add the details$")
    public void i_add_the_details()  {
        actions.sendKeys(logInPage.addName(), "rutva");
        actions.sendKeys(logInPage.addNumber(), "9912345560");
        actions.click(logInPage.nextButton());

    }

    @And("^I click on save changes$")
    public void i_click_on_save_changes()  {
        actions.click(logInPage.saveButton());

    }



}
