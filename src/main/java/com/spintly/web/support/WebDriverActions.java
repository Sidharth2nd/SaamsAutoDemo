package com.spintly.web.support;

import com.spintly.base.core.DriverContext;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;

import io.restassured.path.json.JsonPath;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v96.network.Network;
import org.openqa.selenium.devtools.v96.network.model.Response;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.WebDriver;


import java.io.IOException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static com.spintly.base.managers.ResultManager.error;
import static com.spintly.base.managers.ResultManager.pass;

public class WebDriverActions {
    private static LogUtility logger = new LogUtility(WebDriverActions.class);
    private long DRIVER_WAIT_TIME;


    public void clearAndSend(WebElement element, String text) {
        try {
            new WebDriverWait(DriverContext.getObject().getDriver(), DRIVER_WAIT_TIME).until(ExpectedConditions.visibilityOf(element));
            Thread.sleep(2000);
            element.clear();
            element.sendKeys(text);
            String field = getElementDetails(element);
            logger.detail("Clear and Send  \"" + text + "\" in element -> " + field);
            pass("\" " + text + "\" should be sent in " + field + "after clearing", "\" " + text + "\" is sent in " + field + " after clearing the field");
        } catch (Exception e) {
            String field = getElementDetails(element);
            error("\"" + text + "\" should be sent in " + field, "Unable to send \"" + text + "\" in element -> " + field,
                    e.getMessage(), true);
        }
    }



    public void sendKeys(WebElement element, String text) {
        try {
            element.sendKeys(text);
            String field = getElementDetails(element);
            logger.detail("Send  \"" + text + "\" in element -> " + field);
            pass("\"" + text + "\" should be sent in " + field, "\"" + text + "\" is sent in " + field);

        } catch (ElementNotInteractableException ex) {
            new WebDriverWait(DriverContext.getObject().getDriver(), DRIVER_WAIT_TIME).until(ExpectedConditions.visibilityOf(element));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            element.sendKeys(text);
            String field = getElementDetails(element);
            logger.detail("Retry | Send  " + text + " in element -> " + field);
            pass("\"" + text + "\" should be sent in " + field, "\"" + text + "\" is sent in " + field);
        } catch (Exception e) {
            error("Step should be successful", "Unable to send \"" + text + "\" in element -> " + getElementDetails(element),
                    e.getMessage(), true);
        }
    }

    public void clear(WebElement element) {
        try {
            logger.detail("Clear  element -> " + getElementDetails(element));
            new WebDriverWait(DriverContext.getObject().getDriver(), DRIVER_WAIT_TIME).until(ExpectedConditions.elementToBeClickable(element));
            element.clear();
            String field = getElementDetails(element);
            logger.detail("Retry | Clear element -> " + field);
            pass(field + " should be cleared ", field + " is cleared ");
        } catch (Exception e) {
            error("Step should be successful", "Unable to clear element -> " + getElementDetails(element),
                    e.getMessage(), true);
        }
    }

    public void click(WebElement element) {
        DRIVER_WAIT_TIME = Long.parseLong(PropertyUtility.getProperty("WaitTime"));
        try {
            new WebDriverWait(DriverContext.getObject().getDriver(), DRIVER_WAIT_TIME).until(ExpectedConditions.elementToBeClickable(element));
            new Actions(DriverContext.getObject().getDriver()).moveToElement(element).perform();
            element.click();
            String field = getElementDetails(element);
            logger.detail("Click on element by locator" + field);
            pass("I should click on " + field + " button", "I have clicked on " + field + " button");

        } catch (StaleElementReferenceException ex) {
            //Retry
            try {
                Thread.sleep(5000);
                element.click();
                String field = getElementDetails(element);
                logger.detail("Click on element by locator [Attempt 2]" + field);
                pass("I should click on " + field + " button", "I have clicked on " + field + " button");

            } catch (Exception ex1) {
                logger.error("Error performing step", ExceptionUtils.getStackTrace(ex1));
                error("Step should be successful", "Unable to click on element -> " + getElementDetails(element),
                        ex1.getMessage(), true);
            }
        } catch (WebDriverException ex) {
            //Chrome Retry if unable to click because of overlapping (Chrome NativeEvents is always on (Clicks via Co-ordinates))
            clickViaJavascript(element);
        }

    }

    public void clickViaJavascript(WebElement element) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();
            executor.executeScript("arguments[0].click();", element);
            String field = getElementDetails(element);
            logger.detail("JS Click on element by locator" + field);
            pass("I should click on " + field + " button", "I have clicked on " + field + " button");

        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to click on element -> " + getElementDetails(element),
                    e.getMessage(), true);
        }
    }

    public void windowToForeground(){
        //DriverContext.getObject().getDriver().manage().window().fullscreen();
        JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();
        executor.executeScript("window.focus();");
    }

    public void setActiveDropdownValue(WebElement element) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();
            executor.executeScript("arguments[0].setAttribute('aria-activedescendent', 'combo-box-demo-option-0');", element);
            String field = getElementDetails(element);
            logger.detail("set active DropdownValue on element by locator " + field);
            pass("I should set activeDropdownValue  on " + field + " button", "I have set DropdownValue on " + field + " button");

        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to set active Dropdown Value on element -> " + getElementDetails(element),
                    e.getMessage(), true);
        }
    }

    public boolean waitForJStoLoad() {

        WebDriverWait wait = new WebDriverWait(DriverContext.getObject().getDriver(), 30);
        ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
                } catch (Exception e) {
                    return true;
                }
            }
        };

        ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState")
                        .toString().equals("complete");
            }
        };
        return wait.until(jQueryLoad) && wait.until(jsLoad);
    }

    public String getText(WebElement element) throws InterruptedException {
        try {
            Long DRIVER_WAIT_TIME = Long.parseLong(PropertyUtility.getProperty("WaitTime"));
            waitForJStoLoad();
            String text = element.getText();
            new Actions(DriverContext.getObject().getDriver()).moveToElement(element).perform();
            String field = getElementDetails(element);
            logger.detail("Text value is \"" + text + "\" for element -> " + field);
            return text;
        } catch (StaleElementReferenceException ex) {
            Thread.sleep(5000);
            try {
            String text = element.getText();
            logger.detail(" SECOND ATTEMPT : Text value is \"" + text + "\" for element -> " + getElementDetails(element));
            return text;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void moveToElement(WebElement element) {
        try {
            waitForJStoLoad();
            new Actions(DriverContext.getObject().getDriver()).moveToElement(element).perform();
        } catch (Exception e) {
        }
    }



    public void setAttribute(WebElement element, String attName, String attValue) {
        JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();
        executor.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);",element,attName,attValue);
    }

//    URL url = new URL(urlGiven);
//    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
//        huc.setRequestMethod("HEAD");
//        huc.connect();
//    int code = huc.getResponseCode();


//    ChromeDriver driver = (ChromeDriver) DriverContext.getObject().getDriver();
//    DevTools devTools = driver.getDevTools();
//              devTools.createSession();
//
//              devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
//
//              devTools.addListener(Network.responseReceived(), response ->{
//        Response res = response.getResponse();
//        System.out.println(response.getResponse().getUrl());
//        System.out.println(response.getResponse().getStatus());
//    });



    public String getCookieString(){
        JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();

        String cookiesString = (String) executor.executeScript("return document.cookie");
        return cookiesString;
    }

    public String getAttribute(WebElement element, String attribute) {
        try {

            Long DRIVER_WAIT_TIME = Long.parseLong(PropertyUtility.getProperty("WaitTime"));
            Thread.sleep(3000);
            waitForJStoLoad();
            String value = element.getAttribute(attribute);
            new Actions(DriverContext.getObject().getDriver()).moveToElement(element).perform();
            logger.detail("Attribute value is \"" + value + "\" for element -> " + getElementDetails(element));
            return value;
        } catch (StaleElementReferenceException ex) {
            String value = element.getAttribute("value");
            logger.detail("Attribute value is \"" + value + "\" for element -> " + getElementDetails(element));
            return value;
        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to get value from element -> " + getElementDetails(element),
                    e.getMessage(), true);
            return null;
        }
    }

    public String getAttributeValue(WebElement element) {
        try {

            Long DRIVER_WAIT_TIME = Long.parseLong(PropertyUtility.getProperty("WaitTime"));
            Thread.sleep(3000);
            waitForJStoLoad();
            String value = element.getAttribute("value");
            new Actions(DriverContext.getObject().getDriver()).moveToElement(element).perform();
            logger.detail("Attribute value is \"" + value + "\" for element -> " + getElementDetails(element));
            return value;
        } catch (StaleElementReferenceException ex) {
            String value = element.getAttribute("value");
            logger.detail("Attribute value is \"" + value + "\" for element -> " + getElementDetails(element));
            return value;
        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to get value from element -> " + getElementDetails(element),
                    e.getMessage(), true);
            return null;
        }
    }

    public void scrolldownByJavaScript() {
        JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();
        executor.executeScript("window.scrollBy(0,200)", "");
    }

    public void scrollRightByJavaScript() {
        JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();
        executor.executeScript("window.scrollBy(200,0)", "");
    }

    public void clearByKeyboardAction(WebElement element) {
        try {
            Thread.sleep(2000);
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(Keys.DELETE);
            element.sendKeys(Keys.BACK_SPACE);
            element.clear();
            logger.detail(" Keyboard action to Clear element | " + getElementDetails(element));

        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to clear element -> " + getElementDetails(element),
                    e.getMessage(), true);
        }
    }

    public void clearByJavaScript(WebElement element) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) DriverContext.getObject().getDriver();
            executor.executeScript("arguments[0].value = '';", element);
            element.clear();
            logger.detail(" JS Clear on element | " + getElementDetails(element));

        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to clear element -> " + getElementDetails(element),
                    e.getMessage(), true);
        }
    }

    public void hitBrowserBackButton() {
        DriverContext.getObject().getDriver().navigate().back();
    }

    public void navigateTo(String url) {
        try {
            DriverContext.getObject().getDriver().navigate().to(url);
            pass("User should navigate to \"" + url + "\" url", "Navigated to " + url + " url");
        } catch (Exception e) {
            error("User should navigate to \"" + url + "\" url", "Unable to navigate to url " + url,
                    e.getMessage(), true);
        }


    }

    public void refreshPage() throws InterruptedException {
        DriverContext.getObject().getDriver().navigate().refresh();
        Thread.sleep(5000);
        pass("User should refresh the page", "User refreshes the page");

    }

    public String getWindowHandle() {

        return DriverContext.getObject().getDriver().getWindowHandle();

    }

    public String getCurrentURL() {
        String s = DriverContext.getObject().getDriver().getCurrentUrl();
        return s;
    }

    public String getPagesource() {
        String s = DriverContext.getObject().getDriver().getPageSource();
        return s;
    }

    public void selectElementByText(WebElement element, String text) {
        try {
            Long DRIVER_WAIT_TIME = Long.parseLong(PropertyUtility.getProperty("WaitTime"));
            new WebDriverWait(DriverContext.getObject().getDriver(), DRIVER_WAIT_TIME).until(ExpectedConditions.elementToBeClickable(element));
            new Select(element).selectByVisibleText(text);
            String field = getElementDetails(element);
            logger.detail("Select By Text \"" + text, "\" for element -> " + field);
            pass("I should select " + text + " from dropdown " + field, "I have selected " + text + " from dropdown " + field);

        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to select element by text of " + getElementDetails(element) + " | Text: " + text,
                    e.getMessage(), true);
        }
    }

    public static String getFirstSelectedOption(WebElement element) {
        return new Select(element).getFirstSelectedOption().getText();
    }

    public void deleteAllCookies() {
        DriverContext.getObject().getDriver().manage().deleteAllCookies();
    }

    public boolean invisibilityOfElementLocated(WebElement element) {
        try {
            return new WebDriverWait(DriverContext.getObject().getDriver(), Long.parseLong(PropertyUtility.getProperty("WaitTime"))).until(ExpectedConditions.invisibilityOf(element));
        } catch (Exception ex) {
            return true;
        }

    }

    private String getElementDetails(WebElement element) {
        return "\"" + element.toString().split("->")[1].replaceFirst("(?s)(.*)\\]", "$1" + "") + "\"";
    }

    public void switchToFrame(String value) {

        WebDriver driver = DriverContext.getObject().getDriver();
        driver.switchTo().frame(value);
    }

    public void switchToNewTabFromParent(String parentHandle) {
        Set<String> handles = DriverContext.getObject().getDriver().getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(parentHandle)) {
                DriverContext.getObject().getDriver().switchTo().window(handle);
                System.out.println("Parent : " + parentHandle);
                System.out.println("Switched to : " + handle);
                break;
            }
        }
    }

    public void switchToParentTab(String parentHandle) {
        Set<String> handles = DriverContext.getObject().getDriver().getWindowHandles();
        for (String handle : handles) {
            if (handle.equals(parentHandle)) {
                DriverContext.getObject().getDriver().switchTo().window(handle);
                System.out.println("Parent : " + parentHandle);
                System.out.println("Switched to : " + handle);
                break;
            }
        }
    }

    public void switchToMainFrame() {
        WebDriver driver = DriverContext.getObject().getDriver();
        driver.switchTo().defaultContent();

    }

    public void close() {
        WebDriver driver = DriverContext.getObject().getDriver();
        driver.close();

    }

    public void openNewTab() {
        ((JavascriptExecutor) DriverContext.getObject().getDriver()).executeScript("window.open('about:blank','_blank');");
        String AdminsubWindowHandler = null;

        Set<String> handles = DriverContext.getObject().getDriver().getWindowHandles();
        Iterator<String> iterator = handles.iterator();
        while (iterator.hasNext()) {
            AdminsubWindowHandler = iterator.next();
        }

        DriverContext.getObject().getDriver().switchTo().window(AdminsubWindowHandler);
    }

    public boolean isElementPresent(WebElement element) {
        try {
            boolean isdisplayed = element.isDisplayed();
            return isdisplayed;
        } catch (Exception Ex) {
            return false;
        }
    }

    public boolean isElementSelected(WebElement element) {
        try {
            boolean isSelected = element.isSelected();
            return isSelected;
        } catch (Exception Ex) {
            return false;
        }
    }

    public boolean isElementEnabled(WebElement element) {
        try {
            boolean isEnabled = element.isEnabled();
            return isEnabled;
        } catch (Exception Ex) {
            return false;
        }
    }

    public String getCSSValue(WebElement element, String property){
        try {
            Long DRIVER_WAIT_TIME = Long.parseLong(PropertyUtility.getProperty("WaitTime"));
            Thread.sleep(3000);
            waitForJStoLoad();
            String value = element.getCssValue(property);
            new Actions(DriverContext.getObject().getDriver()).moveToElement(element).perform();
            logger.detail("Property value is \"" + value + "\" for element -> " + getElementDetails(element));
            return value;
        } catch (StaleElementReferenceException ex) {
            String value = element.getAttribute("value");
            logger.detail("Property value is \"" + value + "\" for element -> " + getElementDetails(element));
            return value;
        } catch (Exception e) {
            logger.error("Error performing step", ExceptionUtils.getStackTrace(e));
            error("Step should be successful", "Unable to get value from element -> " + getElementDetails(element),
                    e.getMessage(), true);
            return null;
        }
    }

}
