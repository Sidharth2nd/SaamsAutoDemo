package com.spintly.base.core;

import org.openqa.selenium.WebDriver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DriverContext extends DriverBase {
    private static DriverContext driverContext;
    public static Map<String, WebDriver> driverArray = new ConcurrentHashMap<>();
    protected static WebDriver driver;
    private static String primaryInstanceKey;
    private static String currentKey="ORIGINAL";

    public static DriverContext getObject() {
        if (driverContext == null) {
            driverContext = new DriverContext();
        }
        return driverContext;
    }

    public WebDriver getDriver() {
        return driver;
    }
    public void setDriver(WebDriver newDriver) {
        driver=newDriver;
    }
    public void setPrimaryInstanceKey(String key){
        primaryInstanceKey =key;
    }
    public void storeDriverInstance(String key, WebDriver driver) {
        driverArray.put(key, driver);
    }
    public void closeDriverInstance(String key) {
        if (driverArray.containsKey(key)) {
            WebDriver driver = driverArray.get(key);
            driver.quit();
            driverArray.remove(key);
        } else {
            logger.error("DRIVER INSTANCE NOT FOUND");
        }
    }
    public void closeAllDriverInstanceExceptOriginal() {
        WebDriver driver;
        Set<String> keys = driverArray.keySet();
        for (String key : keys) {
            if (!key.equalsIgnoreCase(primaryInstanceKey)) {
                driver = driverArray.get(key);
                driver.quit();
                driverArray.remove(key);
            }
        }
    }

    public void useDriverInstance(String key) {
        if (driverArray.containsKey(key)) {
            setDriver(driverArray.get(key));
            currentKey=key;
        } else {
            logger.error("DRIVER INSTANCE NOT FOUND");
        }
    }

    public static String getCurrentKey() {
        return currentKey;
    }
}
