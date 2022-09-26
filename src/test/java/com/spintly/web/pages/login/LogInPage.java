package com.spintly.web.pages.login;

import com.spintly.base.core.PageBase;
import org.openqa.selenium.WebElement;

public class LogInPage extends PageBase {
    public WebElement phoneNumber(){
        return findElement("//input[@placeholder= 'Enter Phone Number']", LocatorType.XPath);
    }

    public WebElement password(){
        return findElement("//input[@type ='password']", LocatorType.XPath);
    }
    public WebElement logIn(){
        return findElement("(//span[contains (., 'Login')])[2]", LocatorType.XPath);
    }

    public WebElement cancel(){
        return findElement("button[title='Close']", LocatorType.CssSelector);
    }
    public WebElement selectOrg(){
        return findElement("div[class = 'flex']", LocatorType.CssSelector);
    }
    public WebElement mrinqTech(){
        return findElement("//li[contains (text(), 'Mrinq Technologies LLP V2')]", LocatorType.XPath);
    }
    public WebElement allUser(){
        return findElement("//span[contains (text(), 'All Users')]", LocatorType.XPath);
    }
    public WebElement addUser(){
        return findElement("//span[contains (., 'Add User')]", LocatorType.XPath);
    }
    public WebElement addSingleUser(){
        return findElement("//span[contains (., 'Add Single User')]", LocatorType.XPath);
    }
    public WebElement addName(){
        return findElement("input[name='name']", LocatorType.CssSelector);
    }
    public WebElement addNumber(){
        return findElement("//input[@placeholder='Enter Phone Number']", LocatorType.XPath);
    }
    public WebElement nextButton(){
        return findElement("(//span[contains (., 'Next')])[1]", LocatorType.XPath);
    }
    public WebElement saveButton(){
        return findElement("(//span[contains (., 'Save Changes')])", LocatorType.XPath);
    }





}
