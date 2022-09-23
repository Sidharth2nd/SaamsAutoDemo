package com.spintly.base.managers;

import com.spintly.base.support.logger.LogUtility;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class TestAssert {
    public static LogUtility logger = new LogUtility(TestAssert.class);
    ;


    public void isTrue(boolean value, String expectedText, String errorMessage) {
        try {
            Assert.assertTrue(value, expectedText);
            ResultManager.pass(expectedText, "Success : " + expectedText, true);
        } catch (AssertionError e) {
            ResultManager.error(expectedText, errorMessage, e.getMessage(), true);

        }
    }

    public void isFail(String errorMessage) {
        ResultManager.fail("Step should be successful", errorMessage, true);
        Assert.assertFalse(true, errorMessage);
    }

    public void isTrue(boolean value, String expectedText, String successMessage, String errorMessage) {

        try {
            Assert.assertTrue(value, expectedText);
            ResultManager.pass(expectedText, successMessage, true);
        } catch (AssertionError e) {
            ResultManager.error(expectedText, errorMessage, e.getMessage(), true);
        }
    }

    public void isEquals(String actualValue, Object expectedValue, String expectedText, String sucessMessage, String errorMessage) {
        try {
            Assert.assertEquals(expectedValue, actualValue);
            ResultManager.pass(expectedText, sucessMessage, true);

        } catch (AssertionError e) {
            // logger.detail("Actual Value : "+actualValue+" Expected Value : "+expectedValue);
            ResultManager.error(expectedText, errorMessage, e.getMessage(), true);
        }
        logger.detail("Actual Value : " + actualValue + " | Expected Value : " + expectedValue);
    }

    public void isFalse(boolean value, String expectedText, String errorMessage) {
        try {
            Assert.assertFalse(value, expectedText);
            ResultManager.pass(expectedText, "Success : " + expectedText, true);
        } catch (AssertionError e) {
            ResultManager.error(expectedText, errorMessage, e.getMessage(), true);
        }

    }

    public void isFalse(boolean value, String expectedText, String successMessage, String errorMessage) {
        try {
            Assert.assertFalse(value, expectedText);
            ResultManager.pass(expectedText, successMessage, true);
        } catch (AssertionError e) {
            ResultManager.error(expectedText, errorMessage, e.getMessage(), true);
        }

    }

    public void isElementEnabled(WebElement element, String expectedText, String successMessage, String errorMessage) {
        Boolean isEnabled;
        try {
            isEnabled = element.isEnabled();
        } catch (Exception e) {
            isEnabled = false;
        }
        isTrue(isEnabled, expectedText, successMessage, errorMessage);
    }

    public void isElementSelected(WebElement element, String expectedMessage, String successMessage, String errorMessage) {
        Boolean isSelected;
        try {
            isSelected = element.isSelected();
        } catch (Exception e) {
            isSelected = false;
        }
        isTrue(isSelected, expectedMessage, successMessage, errorMessage);
    }

    public void isElementTextEquals(WebElement element, String expectedText, String expectedMessage, String successMessage, String errorMessage) {
        String actualText = element.getText();
        logger.detail("Element Text : " + actualText);
        isTrue(actualText.equals(expectedText), expectedMessage, successMessage, errorMessage);
    }

    public void isElementDisplayed(WebElement element, String expectedMessage, String successMessage, String errorMessage) {
        Boolean isDisplayed;
        try {
            isDisplayed = element.isDisplayed();
        } catch (Exception e) {
            isDisplayed = false;
        }
        String field = getElementDetails(element);
        isTrue(isDisplayed, expectedMessage, successMessage, errorMessage);
    }

    public void isNotElementDisplayed(WebElement element, String expectedMessage, String successMessage, String errorMessage) {
        Boolean isDisplayed;
        try {
            isDisplayed = element.isDisplayed();
        } catch (Exception e) {
            isDisplayed = false;
        }
        isFalse(isDisplayed, expectedMessage, successMessage, errorMessage);
    }


    public void isElementValueEquals(WebElement element, String expectedText, String expectedMessage, String successMessage, String errorMessage) {
        isTrue(element.getAttribute("value").equals(expectedText), expectedMessage, successMessage, errorMessage);
    }

    public void isElementNameEquals(WebElement element, String expectedText, String expectedMessage, String successMessage, String errorMessage) {
        isTrue(element.getAttribute("name").equals(expectedText), expectedMessage, successMessage, errorMessage);
    }

    public void isElementNotEnabled(WebElement element, String expectedText, String successMessage, String errorMessage) {
        Boolean isEnabled;
        try {
            isEnabled = element.isEnabled();
        } catch (Exception e) {
            isEnabled = false;
        }
        isFalse(isEnabled, expectedText, successMessage, errorMessage);
    }

    private String getElementDetails(WebElement element) {
        return element.toString().split("->")[1].replaceFirst("(?s)(.*)\\]", "$1" + "");
    }
}
