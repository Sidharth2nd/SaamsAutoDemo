package com.spintly;

import com.spintly.base.core.VariableContext;
import com.spintly.base.core.DriverContext;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.*;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.PageLoadStrategy;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@SuppressWarnings({"All"})
public class SuiteSetup extends EventFiringWebDriver {
    private static LogUtility logger = new LogUtility(SuiteSetup.class);
    private static WebDriver driver = null;
    private static final Thread CLOSE_THREAD = new Thread() {

        @Override
        public void run() {
            driver.quit();
        }
    };
    private static SuiteSetup setupManager;
    private static String TARGET_PLATFORM;

    static {
        TARGET_PLATFORM = PropertyUtility.getProperty("target.platform");
        logger.detail("PLATFORM : " + TARGET_PLATFORM);
        if (TARGET_PLATFORM.equalsIgnoreCase("WEB"))
            driver = createWebDriverInstance(PropertyUtility.getProperty("default.browser"));
        if (driver != null) {
            driver.manage().timeouts().setScriptTimeout(90, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(150, TimeUnit.SECONDS);
            driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
            DriverContext.getObject().setPrimaryInstanceKey("ORIGINAL");
            DriverContext.getObject().storeDriverInstance("ORIGINAL", driver);
            DriverContext.getObject().setDriver(driver);
        }
        Runtime.getRuntime().addShutdownHook(CLOSE_THREAD);
    }

    private SuiteSetup() {super(driver);}

    public static SuiteSetup getObject() {
        if (setupManager == null) {setupManager = new SuiteSetup();}
        return setupManager;
    }

    public static WebDriver getDriver() {
        if (DriverContext.getObject().getDriver() != null)
            return DriverContext.getObject().getDriver();
        else
            return null;
    }

    public static void setDriver(WebDriver newDriver) {
        DriverContext.getObject().setDriver(newDriver);
        driver = driver;
    }

    public static WebDriver createWebDriverInstance(String browser) {
        WebDriver driver = null;
        String chromeDriverPath = "src/main/resources/BrowserExecutables/chromedriver.exe";
        if (SystemUtils.IS_OS_MAC) {
            chromeDriverPath = "src/main/resources/BrowserExecutables/chromedriver";
        }else if(SystemUtils.IS_OS_LINUX){
            chromeDriverPath = "src/main/resources/BrowserExecutables/chromedriverLinux";
        }
        switch (browser.toUpperCase()) {
            case "CHROME":
                System.setProperty("webdriver.chrome.driver", FileUtility.getSuiteResource("", chromeDriverPath));
                ChromeOptions options = getChromeDesiredCapabilities();
                ChromeDriverService service = new ChromeDriverService.Builder()
                        .usingDriverExecutable(new File(FileUtility.getSuiteResource("", chromeDriverPath)))
                        .usingAnyFreePort()
                        .build();
                driver = new ChromeDriver(service, options);
                driver.manage().window().maximize();
                driver.manage().timeouts().setScriptTimeout(90, TimeUnit.SECONDS);
                driver.manage().timeouts().pageLoadTimeout(150, TimeUnit.SECONDS);
                driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
                break;
            default:
                logger.error("webdriver method for " + browser + "is not implemented ");
        }
        return driver;
    }

    private static ChromeOptions getChromeDesiredCapabilities() {
        ChromeOptions chromeOptions = new ChromeOptions();
        DesiredCapabilities d = new DesiredCapabilities();
        LoggingPreferences logPrefs = new LoggingPreferences();

        System.setProperty("webdriver.chrome.silentOutput", "true");
        Map<String, Object> prefs = new HashMap<String, Object>();

        prefs.put("download.default_directory", Paths.get("").toAbsolutePath() + File.separator + "Downloads");
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.addArguments("no-sandbox");
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        chromeOptions.addArguments("--disable-extensions");
        chromeOptions.addArguments("--disable-web-security");
        chromeOptions.addArguments("--test-type");
        chromeOptions.addArguments("--window-size=1920,1280");
//        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        chromeOptions.addArguments("ignore-certificate-errors");
        chromeOptions.addArguments("--allow-running-insecure-content");
        chromeOptions.addArguments("--disable-infobars");
        chromeOptions.addArguments("--log-level=3");

        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        //d.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        chromeOptions.setCapability( "goog:loggingPrefs", logPrefs );
        return chromeOptions;
    }

    public void useDriverInstance(String instanceKey) {
        try {
            DriverContext.getObject().useDriverInstance(instanceKey);
            logger.detail("Executing on Driver Instance : " + instanceKey);
        } catch (Exception ex) {
            logger.detail("Fetching Driver Instance Failed : " + instanceKey);
            VariableContext.getObject().setScenarioContext("FAILURE", "TRUE");
        }
    }

    public String getCurrentInstanceKey() {
        return DriverContext.getObject().getCurrentKey();
    }


    public void createNewWebdriverInstance(String key, String browser) {
        WebDriver newWebDriverInstance = createWebDriverInstance(browser);
        newWebDriverInstance.manage().timeouts().implicitlyWait(Integer.parseInt(PropertyUtility.getProperty("implicit.wait")), TimeUnit.SECONDS);
        newWebDriverInstance.manage().window().maximize();
        DriverContext.getObject().storeDriverInstance(key, newWebDriverInstance);
    }

    @Override
    public void close() {
        if (Thread.currentThread() != CLOSE_THREAD) {
            throw new UnsupportedOperationException(
                    "You shouldn't close this WebDriver. It's shared and will close when the JVM exits.");
        }
        super.close();
    }
}
