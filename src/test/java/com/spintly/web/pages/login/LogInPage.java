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




}
