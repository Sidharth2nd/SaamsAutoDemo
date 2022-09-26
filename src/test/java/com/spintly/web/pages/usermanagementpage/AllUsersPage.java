package com.spintly.web.pages.usermanagementpage;

import com.spintly.base.core.PageBase;
import org.openqa.selenium.WebElement;

public class AllUsersPage extends PageBase {
    public WebElement userLink(String name){
        return findElement(String.format("//a[text() = '%s']", name), LocatorType.XPath);
    }
    public WebElement activeUsersButton(){
        return findElement("//button[text() = 'Active Users']", LocatorType.XPath);
    }
    public WebElement deactivatedButton(){
        return findElement("//button[text()= 'Deactivated Users']", LocatorType.XPath);
    }
    public WebElement addUserButton(){
        return findElement("//span[contains(., 'Add User')]", LocatorType.XPath);
    }
    public WebElement addSingleUserButton() {
        return findElement("//span[contains(., 'Add Single User')]", LocatorType.XPath);
    }
}
