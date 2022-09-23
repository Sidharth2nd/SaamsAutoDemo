package com.spintly.base.managers;

import com.spintly.base.core.VariableContext;
import com.spintly.base.support.cucumberEvents.StepsStore;
import com.spintly.base.support.logger.LogUtility;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class TestVerify {
	public static LogUtility logger = new LogUtility(TestVerify.class);;

	public void isTrue(boolean value, String expectedText, String errorMessage) {
		try {
			Assert.assertTrue(value, expectedText);
			ResultManager.pass( expectedText, "Success : " + expectedText, true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(), expectedText, errorMessage, true);
		}
	}

	public void isTrue(boolean value, String expectedText) {
		try {
			Assert.assertTrue(value, expectedText);
			ResultManager.pass( expectedText, "Success : " + expectedText, true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(), expectedText, "Failed : " + expectedText, true);
		}
	}

	public void isTrue(boolean value, String expectedText,String sucessMessage, String errorMessage) {
		try {
			Assert.assertTrue(value, expectedText);
			ResultManager.pass( expectedText, sucessMessage, true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(), expectedText, errorMessage, true);
		}

	}

	public void isEquals(String actualValue,String expectedValue, String expectedText, String errorMessage) {
		try {
			Assert.assertEquals(expectedValue, actualValue);
			ResultManager.pass( expectedText, "Success : " + expectedText, true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(),expectedText, errorMessage, true);
		}
		logger.detail("Actual Value : "+actualValue+" | Expected Value : "+expectedValue);

	}

	public void isEquals(String actualValue,String expectedValue, String expectedText,String sucessMessage, String errorMessage) {
		try {
			Assert.assertEquals(expectedValue, actualValue);
			ResultManager.pass( expectedText, sucessMessage, true);
		} catch (AssertionError e) {
			//mark test case fail and continue test
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(),expectedText, errorMessage, true);
		}
		logger.detail("Actual Value : "+actualValue+" | Expected Value : "+expectedValue);
	}

	public void contains(String actualValue,String expectedValue, String expectedText,String sucessMessage, String errorMessage) {
		try {
			Assert.assertTrue(actualValue.contains( expectedValue));
			ResultManager.pass( expectedText, sucessMessage, true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(),expectedText, errorMessage+" Actual Value "+actualValue+" | Expected Value "+expectedValue, true);
		}
		logger.detail("Actual Value : "+actualValue+" | Expected Value : "+expectedValue);

	}

	public void isEquals(String actualValue,String expectedValue) {
		try {
			Assert.assertEquals(expectedValue, actualValue);
			ResultManager.pass( expectedValue+" should be displayed", actualValue+" is correctly displayed", true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(),expectedValue+" should be displayed", expectedValue+ " is not displayed. Actual value : "+actualValue, true);
		}
		logger.detail("Actual Value : "+actualValue+" | Expected Value : "+expectedValue);
	}

	public void isNotEquals(String actualValue, String expectedValue) {
		try {
			Assert.assertNotEquals(expectedValue, actualValue);
			ResultManager.pass( expectedValue+" should not be displayed", actualValue+" is displayed", true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(), expectedValue+" should not be displayed", expectedValue+ " is matching with Actual : "+actualValue, true);
		}
		logger.detail("Actual Value : "+actualValue+" | Expected Value : "+expectedValue);
	}

	public void isEquals(Integer actualValue, Integer expectedValue) {
		try {
			Assert.assertEquals(expectedValue, actualValue);
			ResultManager.pass( expectedValue+" should be displayed", actualValue+" is correctly displayed", true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(), expectedValue+" should be displayed", expectedValue+ " is not displayed. Actual : "+actualValue, true);
		}
		logger.detail("Actual Value : "+actualValue+" | Expected Value : "+expectedValue);
	}

	public void isFalse(boolean value,String expectedText,String sucessMessage, String errorMessage) {
		try {
			Assert.assertFalse(value, expectedText);
			ResultManager.pass( expectedText, sucessMessage, true);
		} catch (AssertionError e) {
			VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS","TRUE");
			ResultManager.failureStep( StepsStore.get(), expectedText, errorMessage, true);
		}

	}

	public void isElementEnabled(WebElement element, String expectedText, String successMessage, String errorMessage) {
		Boolean isEnabled;
		try {
			isEnabled= element.isEnabled();
		} catch (Exception e) {
			isEnabled= false;
		}
		isTrue(isEnabled,expectedText,successMessage, errorMessage);
	}

	public void isElementEnabled(WebElement element, String expectedText) {
		Boolean isEnabled;
		try {
			isEnabled= element.isEnabled();
		} catch (Exception e) {
			isEnabled= false;
		}
		isTrue(isEnabled,expectedText);
	}

	public void isElementNotEnabled(WebElement element, String expectedText, String successMessage, String errorMessage) {
		Boolean isEnabled;
		try {
			isEnabled= element.isEnabled();
		} catch (Exception e) {
			isEnabled= false;
		}
		isFalse(isEnabled,expectedText,successMessage, errorMessage);
	}

	public void isElementSelected(WebElement element,String expectedMessage,String successMessage, String errorMessage) {
		Boolean isSelected;
		try {
			isSelected= element.isSelected();
		} catch (Exception e) {
			isSelected= false;
		}
		isTrue(isSelected,expectedMessage,successMessage, errorMessage);
	}

	public void isElementNotSelected(WebElement element,String expectedMessage,String successMessage, String errorMessage) {
		Boolean isSelected;
		try {
			isSelected= element.isSelected();
		} catch (Exception e) {
			isSelected= false;
		}
		isFalse(isSelected,expectedMessage,successMessage, errorMessage);
	}

	public void isElementDisplayed(WebElement element,String expectedMessage,String successMessage, String errorMessage) {
		Boolean isDisplayed;
		try {
			isDisplayed= element.isDisplayed();
		} catch (Exception e) {
			isDisplayed= false;
		}
		isTrue(isDisplayed,expectedMessage,successMessage+" :", errorMessage);
	}

	public void isElementTextEquals(WebElement element,String expectedValue,String expectedMessage,String successMessage, String errorMessage) {
		isEquals(element.getText(),expectedValue,expectedMessage,successMessage,errorMessage);
	}

	public void isElementTextEquals(WebElement element,String expectedValue) {
		String elementText=element.getText();
		logger.detail("Element Text : "+elementText);
		isEquals(elementText,expectedValue);
	}

	public void isElementNotDisplayed(WebElement element,String expectedMessage,String successMessage, String errorMessage) {
		Boolean isDisplayed;
		try {
			isDisplayed= element.isDisplayed();
		} catch (Exception e) {
			isDisplayed= false;
		}
		isFalse(isDisplayed,expectedMessage,successMessage, errorMessage);
	}


}
