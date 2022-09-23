package com.spintly.base.managers;

import com.spintly.base.core.DriverBase;
import com.spintly.base.core.ReportBase;
import com.spintly.base.enums.EnumCollection.ResultType;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.support.selenium.ScreenshotUtility;
import com.spintly.base.support.cucumberEvents.StepsStore;
import org.testng.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResultManager extends DriverBase {
    private static ReportBase reportBase;
    private static String screenShotFolder;
    private String detailsPath;

    public ResultManager(ReportBase reportDesigner) {
        this.reportBase = reportDesigner;
        this.detailsPath = reportDesigner.getTestResultFolderName();
    }

    public static void log(String expected, String actual, Boolean... screenDump) {
        String name = StepsStore.get();
        reportBase.addTestData(getDataMap(name, expected, actual, ResultType.DONE.toString(), false));
        logger.trace("LOG| Step : " + name + ", Expected is : " + expected + " and Actual is : " + actual);
    }

    public static void pass(String expected, String actual, Boolean... screenDump) {
        String name = StepsStore.get();
        reportBase.addTestData(getDataMap(name, expected, actual, ResultType.PASSED.toString(), false));
        logger.detail("TEST STEP - PASS| Actual Result : " + actual);
    }

    public static void fail(String expected, String actual, Boolean... screenDump) {
        String name = StepsStore.get();
        reportBase.addTestData(getDataMap(name, expected, actual, ResultType.FAILED.toString(), screenDump));
        logger.error("FAIL| Step : " + name + " Actual is : " + actual);
        reportBase.verificationFailed(getDataMap(name, expected, actual, ResultType.FAILED.toString()));
        Assert.fail("Step : " + name + " Actual is : " + actual); // Added for failure
    }

    public static void failureStep(String step, String expected, String actual, Boolean... screenDump) {
        String name = step;
        reportBase.addTestData(getDataMap(name, expected, actual, ResultType.WARNING.toString(), screenDump));
        logger.error("FAIL| Step : " + name + ", Expected is : " + expected + " and Actual is : " + actual);
        reportBase.verificationFailed(getDataMap(name, expected, actual, ResultType.WARNING.toString()));
    }

    public static void error(String expected, String actual, String stacktrace, Boolean... screenDump) {
        String name = StepsStore.get();
        reportBase.addTestData(getDataMap(name, expected, actual, ResultType.ERROR.toString(), screenDump));
        logger.error("ERROR| Step : " + name + ", Expected is : " + expected + " and Actual is : " + actual +" | Stacktrace "+ stacktrace);
        reportBase.verificationFailed(getDataMap(name, expected, actual, ResultType.ERROR.toString()));
        Assert.fail("Step : " + name + ", Expected is : " + expected + " and Actual is : " + actual);
    }

    public static void warning(String expected, String actual, Boolean... screenDump) {
        String name = StepsStore.get();
        reportBase.addTestData(getDataMap(name, expected, actual, ResultType.WARNING.toString(), false));
        logger.warning("WARNING|Step : " + name + ", Expected is : " + expected + " and Actual is : " + actual);
    }

    public static Map<String, String> getDataMap(String name, String expected, String actual, String logType, Boolean... screenDump) {
        ScreenshotUtility screenshotManager = new ScreenshotUtility();
        Map<String, String> data = new HashMap<String, String>();
        try {
            if (screenDump.length > 0) {
                if (screenDump[0] == true)
                    data.put("screenDump", PropertyUtility.getResultConfigProperties("SCREENSHOTS_DIRECTORY") + "/" + screenshotManager.screenshot(reportBase.getTestScreenShotFolderName()));
            }
        } catch (IOException e) {
            logger.error("Error while capturing/copying screenshot" + e.getMessage());
        }
        data.put("name", name);
        data.put("type", logType);
        data.put("expected", expected);
        data.put("actual", actual);
        return data;
    }

    public static String getScreenShotFolder() {
        return reportBase.getTestScreenShotFolderName();
    }

    public static void setStacktrace(String stackTrace)
    {
        int len = stackTrace.length();
        if (len>1500)
            len =1500;
        stackTrace = stackTrace.substring(0,len)+" ...";
        reportBase.addStackTrace(getDataMap("", "", stackTrace,"", false));
    }
}
