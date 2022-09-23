package com.spintly.base.core;


import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.support.cucumberEvents.StepsStore;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.spintly.base.managers.ResultManager.error;


public class PageBase extends DriverBase {
    private long DRIVER_WAIT_TIME;

    public PageBase() {
        DRIVER_WAIT_TIME = Long.parseLong(PropertyUtility.getProperty("WaitTime"));
    }

    public enum LocatorType {
        Id,
        Name,
        ClassName,
        LinkText,
        PartialLinkText,
        CssSelector,
        TagName,
        XPath
    }

    public void waitForPageLoad() {
        new WebDriverWait(DriverContext.getObject().getDriver(), DRIVER_WAIT_TIME).until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }

    public List<WebElement> findElements(String identifier, LocatorType locatorType) {
        WebDriver driver = DriverContext.getObject().getDriver();

        List<WebElement> elements = null;
        switch (locatorType) {
            case Id: {
                WaitUntilElementIsDisplayed(By.id(identifier));
                elements = driver.findElements(By.id(identifier));
                break;
            }
            case Name: {
                WaitUntilElementIsDisplayed(By.name(identifier));
                elements = driver.findElements(By.name(identifier));
                break;
            }
            case ClassName: {
                WaitUntilElementIsDisplayed(By.className(identifier));
                elements = driver.findElements(By.className(identifier));
                break;
            }
            case XPath: {
                WaitUntilElementIsDisplayed(By.xpath(identifier));
                elements = driver.findElements(By.xpath(identifier));
                break;
            }
            case LinkText: {
                WaitUntilElementIsDisplayed(By.linkText(identifier));
                elements = driver.findElements(By.linkText(identifier));
                break;
            }
            case PartialLinkText: {
                WaitUntilElementIsDisplayed(By.partialLinkText(identifier));
                elements = driver.findElements(By.partialLinkText(identifier));
                break;
            }
            case CssSelector: {
                WaitUntilElementIsDisplayed(By.cssSelector(identifier));
                elements = driver.findElements(By.cssSelector(identifier));
                break;
            }
            case TagName: {
                WaitUntilElementIsDisplayed(By.tagName(identifier));
                elements = driver.findElements(By.tagName(identifier));
                break;
            }
        }
        return elements;
    }

    public WebElement findElement(String identifier, LocatorType locatorType, boolean... ignoreException) {
        WebDriver driver = DriverContext.getObject().getDriver();
        updateWaitTime(ignoreException);
        WebElement element = null;
        boolean retry = false;
        do {
            //retry = false;
            try {
                switch (locatorType) {
                    case Id: {
                        WaitUntilElementIsDisplayed(By.id(identifier));
                        element = driver.findElement(By.id(identifier));
                        break;
                    }
                    case Name: {
                        WaitUntilElementIsDisplayed(By.name(identifier));
                        element = driver.findElement(By.name(identifier));
                        break;
                    }
                    case ClassName: {
                        WaitUntilElementIsDisplayed(By.className(identifier));
                        element = driver.findElement(By.className(identifier));
                        break;
                    }
                    case XPath: {
                        WaitUntilElementIsDisplayed(By.xpath(identifier));
                        element = driver.findElement(By.xpath(identifier));
                        break;
                    }
                    case LinkText: {
                        WaitUntilElementIsDisplayed(By.linkText(identifier));
                        element = driver.findElement(By.linkText(identifier));
                        break;
                    }
                    case PartialLinkText: {
                        WaitUntilElementIsDisplayed(By.partialLinkText(identifier));
                        element = driver.findElement(By.partialLinkText(identifier));
                        break;
                    }
                    case CssSelector: {
                        WaitUntilElementIsDisplayed(By.cssSelector(identifier));
                        element = driver.findElement(By.cssSelector(identifier));
                        break;
                    }
                    case TagName: {
                        WaitUntilElementIsDisplayed(By.tagName(identifier));
                        element = driver.findElement(By.tagName(identifier));
                        break;
                    }
                }
            } catch (StaleElementReferenceException e) {
                if (retry) {
                    error("Element with [Locator : " + identifier + " ] by type [ " + locatorType + " ] should be displayed", "Element with [Locator : " + identifier + " ] by type [ " + locatorType + " ] is not displayed. Please refer error logs for more details.",
                            e.getMessage(), true);
                    break;
                }
                retry = true;
            } catch (NoSuchElementException e) {
                if (ignoreException.length > 0) {
                    if (ignoreException[0] == true) {
                        //ignore exception
                    } else {
                        variableContext.setScenarioContext("ERROR", "Element with [Locator : " + identifier + " ] by type [ " + locatorType + " ] is not displayed. Please refer error logs for more details.");
                        variableContext.setScenarioContext("STEP", StepsStore.get());
                        //throw new NoSuchElementException(identifier);
                        testStepAssert.isFail("Failed due to web element not found in HTML DOM " + identifier);
                    }
                } else {
                    variableContext.setScenarioContext("ERROR", "Element with [Locator : " + identifier + " ] by type [ " + locatorType + " ] is not displayed. Please refer error logs for more details.");
                    variableContext.setScenarioContext("STEP", StepsStore.get());
                    //throw new NoSuchElementException(identifier);
                    testStepAssert.isFail("Failed due to web element not found in HTML DOM " + identifier);
                }
            } finally {
                updateWaitTime(Long.parseLong(PropertyUtility.getProperty("WaitTime")));

            }
        } while (retry);
        return element;
    }

    public void open(String PageUrl) {
        DriverContext.getObject().getDriver().navigate().to(PageUrl);
    }

    public String get() {
        return DriverContext.getObject().getDriver().getCurrentUrl();
    }

    private void updateWaitTime(boolean... ignoreException) {
        if (ignoreException.length > 0)
            if (ignoreException[0] == true)
                DRIVER_WAIT_TIME = 1L;

    }

    private void updateWaitTime(Long waitTime) {
        DRIVER_WAIT_TIME = waitTime;
    }

    private void WaitUntilElementIsDisplayed(By locator) {
        try {
            WebDriver driver = DriverContext.getObject().getDriver();
            WebDriverWait wait = new WebDriverWait(driver, DRIVER_WAIT_TIME);
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception ex) {
        }
    }

    public void WaitUntilElementIsDisplayed(WebElement element) {
        try {
            WebDriver driver = DriverContext.getObject().getDriver();
            WebDriverWait wait = new WebDriverWait(driver, DRIVER_WAIT_TIME);
            wait.until(ExpectedConditions.visibilityOf(element));

        } catch (Exception ex) {
        }
    }


}
