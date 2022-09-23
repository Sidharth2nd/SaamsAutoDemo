package com.spintly.runner;

import com.spintly.SuiteSetup;
import com.spintly.api.services.*;
import com.spintly.base.core.VariableContext;
import com.spintly.base.core.DriverContext;
import com.spintly.base.core.ReportBase;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.*;
import com.spintly.web.support.WebDriverActions;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;

import static com.spintly.api.services.UserManagementService.*;

public class Hooks {
    private static boolean isFirstTestCase;
    private static LogUtility logger = new LogUtility(Hooks.class);

    static {
        PropertyUtility.loadProperties();
        String autoHome = Hooks.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("/target/test-classes/", "");
        if (SystemUtils.IS_OS_WINDOWS)
            autoHome = autoHome.substring(0, 1).equals("/") ? autoHome.substring(1) : autoHome;
        FileUtility.autoHome = autoHome;
        PropertyConfigurator.configure(FileUtility.getSuiteResource("", "src/main/resources/Properties/System/log4j.properties"));
    }

    protected WebDriver driver;
    public ReportBase reportBase;
    private boolean isTestcaseFailed = false;

    public Hooks() {
        this.reportBase = new ReportBase();
    }

    public synchronized void start(String resultFolder) {
        try {

            this.reportBase.startSuiteFile(resultFolder);
            String device = System.getProperty("DEVICE") == null ? "Windows VM" : System.getProperty("DEVICE");
            logger.detail("********** Initializing Test on Device : "+device.toUpperCase()+" ************");
            SuiteSetup.getObject().getDriver();
        } catch (Exception e) {
            logger.error("Unable to launch Driver");
        }
    }


    @Before
    public void beforeTest(Scenario scenario) throws InterruptedException {
        ApiHelper apiHelper =new ApiHelper();
        apiHelper.checkInternetConnection();
        logger.detail("**********************************************************************************");
        String[] rawFeature = scenario.getId().split("features/")[1].split("/");
        String[] rawFeatureName = rawFeature[rawFeature.length - 1].split(":");
        String tags = scenario.getSourceTagNames().toString();
        logger.detail("FEATURE : " + rawFeatureName[0]+ " | Tags : " + scenario.getSourceTagNames()+"");
        logger.detail("STARTING SCENARIO : " + scenario.getName());
        this.reportBase.startTestCase(scenario.getName(), rawFeatureName[0], tags);
        SuiteSetup.getObject().getObject().useDriverInstance("ORIGINAL");
        new VariableContext().setScenarioContext("PASS_WITH_OBSERVATIONS","FALSE");
        new VariableContext().setScenarioContext("IS_PARTNER","FALSE");

    }

    @After
    public void afterTest(Scenario scenario) {
        boolean bit= false;
        try {
            if(((String) VariableContext.getObject().getScenarioContext("NEWUSERCLEANUPREQUIRED")).equals("TRUE"))
            {
              new UserManagementService().cleanupUser((String) VariableContext.getObject().getScenarioContext("NEWUSERPHONE"));
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETEDEACTIVATED")).equals("TRUE"))
            {
                if (!(VariableContext.getObject().getScenarioContext("NEWUSERPHONE0").equals(""))) {
                    for (int i = 0; i < 2; i++) {
                        new UserManagementService().deleteDeactivatedUser((String) VariableContext.getObject().getScenarioContext("NEWUSERPHONE" + i));
                    }
                } else {
                    new UserManagementService().deleteDeactivatedUser((String) VariableContext.getObject().getScenarioContext("NEWUSERPHONE"));
                }
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETEREACTIVATEDUSER")).equals("TRUE"))
            {
                new UserManagementService().deleteUser((String) VariableContext.getObject().getScenarioContext("NEWUSERPHONE"));
            }
            if(((String) VariableContext.getObject().getScenarioContext("NEWMULTUSERCLEANUPREQUIRED")).equals("TRUE"))
            {
                for(int i=0;i<2;i++){
                    new UserManagementService().deleteUser((String) VariableContext.getObject().getScenarioContext("NEWUSERPHONE"+i));
                }
            }
            if(((String) VariableContext.getObject().getScenarioContext("EXCELUSERCLEANUPREQUIRED")).equals("TRUE"))
            {
                for(int i=1;i<3;i++){
                    new UserManagementService().deleteUser((String) VariableContext.getObject().getScenarioContext("NEWUSERPHONE"+i));
                }

            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETENEWLEAVECYCLE")).equals("TRUE"))
            {
                new LeaveHolidayManagementService().deleteLeaveCycle((String) VariableContext.getObject().getScenarioContext("NEWLEAVECYCLENAME"));

                if(!((String) VariableContext.getObject().getScenarioContext("SECONDLEAVECYCLENAME")).equals("")){
                    new LeaveHolidayManagementService().deleteLeaveCycle((String) VariableContext.getObject().getScenarioContext("SECONDLEAVECYCLENAME"));
                }
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETEEDITEDLEAVECYCLE")).equals("TRUE"))
            {
                new LeaveHolidayManagementService().deleteLeaveCycle((String) VariableContext.getObject().getScenarioContext("EDITEDLEAVECYCLENAME"));
            }
            if(((String) VariableContext.getObject().getScenarioContext("EXCELFILECLEANUPREQUIRED")).equals("TRUE"))
            {
                String path = (String) VariableContext.getObject().getScenarioContext("EXCELSHEETPATH");
                File file = new File(path);
                file.delete();

            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETELEAVEPOLICY")).equals("TRUE"))
            {
                new LeaveHolidayManagementService().deleteLeavePolicy();
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETEHOLIDAYFROMPOLICY")).equals("TRUE"))
            {
                new LeaveHolidayManagementService().deleteHolidayFromHolidayPolicy("exisiting");
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETEREPORTEE")).equals("TRUE"))
            {
                new UserManagementService().deleteReportees();
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETENEWHOLIDAYPOLICY")).equals("TRUE")){
                new LeaveHolidayManagementService().deleteHolidayPolicy();
            }
            //Delete users that were added through UI
            if(((String) VariableContext.getObject().getScenarioContext("DELETEVALIDUSER")).equals("TRUE")){
                new UserManagementService().deleteUser((String) VariableContext.getObject().getScenarioContext("validNumber"));
            }
            //Restore Default Holiday Policy Settings
            if(((String) VariableContext.getObject().getScenarioContext("RESTOREDEFAULTHOLIDAYPOLICY")).equals("TRUE")){
                new LeaveHolidayManagementService().restoreDefaultHolidayPolicy();
            }
            //Delete Custom attributes added through API
            if(((String) VariableContext.getObject().getScenarioContext("DeleteCA")).equals("TRUE")){
                new UserManagementService().deleteCustomAttribute((String) VariableContext.getObject().getScenarioContext("NewCA"));
            }
            //Add deleted fixed Custom attributes back again
            if(((String) VariableContext.getObject().getScenarioContext("AddFixedCA")).equals("TRUE")){
                new UserManagementService().addFixedCustomAttribute();
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETEACCESSHISTORYEXCEL")).equals("TRUE")){
                String selectedFile = (String) VariableContext.getObject().getScenarioContext("ACCESSHISTORYFILEPATH");
                new File(selectedFile).delete();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("LEAVEID")).equals(""))
            {
                new LeaveHolidayManagementService().cleanupLeave();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("RESTOREGENERALSETTINGS")).equals(""))
            {
                new OrganisationSettingsService().restoreGeneralSettings();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("RESTOREMOBILE")).equals(""))
            {
                new OrganisationSettingsService().restoreMobileBasesAccessSettings();
            }
            if(((String) VariableContext.getObject().getScenarioContext("RESTOREAPNAME")).equals("TRUE"))
            {
                new AccessManagementService().restoreAPName();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("RESTOREATTENDANCE")).equals(""))
            {
                new OrganisationSettingsService().restoreAttendanceSettings();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("DELETESITE")).equals(""))
            {
                new PartnerService().deleteSite();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("RESTOREVMS")).equals(""))
            {
                new VisitorManagementService().restoreVMSSettings();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("DELETEPURPOSE")).equals(""))
            {
                new VisitorManagementService().deletePurpose();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("DELETENEWORG")).equals(""))
            {
                new PartnerService().deleteNewSite();
                new PartnerService().deleteNewOrg();
            }
            if(((String) VariableContext.getObject().getScenarioContext("DELETEVISITOR")).equals("TRUE"))
            {
                new VisitorManagementService().cancelVisitorSchedule();
            }
            if(((String) VariableContext.getObject().getScenarioContext("SECONDDELETEVISITOR")).equals("TRUE"))
            {
                new VisitorManagementService().cancelSecondVisitorSchedule();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("RESTOREKIOSK")).equals(""))
            {
                new VisitorManagementService().restoreKioskSettings();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("DELETEKIOSK")).equals(""))
            {
                new VisitorManagementService().deleteKiosk();
            }
            if(!((String) VariableContext.getObject().getScenarioContext("DELETESECONDKIOSK")).equals(""))
            {
                new VisitorManagementService().deleteSecondKiosk();
            }//RestoreApConfiguration
            if(((String) VariableContext.getObject().getScenarioContext("RestoreApConfigurationmfa")).equals("TRUE"))
            {
                new AccessManagementService().restoreAccessPointConfiguration("RestoreApConfigurationmfa");
            }
            if(((String) VariableContext.getObject().getScenarioContext("RestoreApConfigurationdef")).equals("TRUE"))
            {
                new AccessManagementService().restoreAccessPointConfiguration("RestoreApConfigurationdef");
            }
            DriverContext.getObject().closeAllDriverInstanceExceptOriginal();
            SuiteSetup.getObject().useDriverInstance("ORIGINAL");
            if(scenario.getStatus().toString().equals("UNDEFINED"))
            {
                this.reportBase.endTestCase(true, false);
                logger.detail("SKIPPED TEST SCENARIO : " + scenario.getName() + " | Inconclusive Count : " + this.reportBase.inconclusive());

            }
            else {
                this.reportBase.endTestCase(scenario.isFailed(), false);

                if (!scenario.isFailed() || !this.reportBase.isVerificationFailed()) {
                    String Failure = (String) VariableContext.getObject().getScenarioContext("FAILURE");
                    if (Failure.equals("TRUE")) {
                        logger.detail("SKIPPED TEST SCENARIO : " + scenario.getName() + " | Inconclusive Count : " + this.reportBase.inconclusive());
                        bit = true;
                    } else if (((String) VariableContext.getObject().getScenarioContext("PASS_WITH_OBSERVATIONS")).equals("TRUE")) {
                        logger.detail("TEST SCENARIO WITH OBSERVATIONS : " + scenario.getName());
                        bit = true;
                    } else {
                        logger.detail("PASSING TEST SCENARIO : " + scenario.getName());
                        bit = true;
                    }
                    VariableContext.getObject().setScenarioContext("FAILURE", "FALSE");
                    VariableContext.getObject().setScenarioContext("PASS_WITH_OBSERVATIONS", "FALSE");

                } else if (scenario.isFailed() || this.reportBase.isVerificationFailed()) {
                    try {
                        bit = true;
                        logger.detail("FAILED TEST SCENARIO : " + scenario.getName());
                        logger.debug("PAGE SOURCE :" + StringUtils.normalizeSpace(DriverContext.getObject().getDriver().getPageSource()));

                    } catch (Exception e) {
                    }
                }
                if (PropertyUtility.targetPlatform.equalsIgnoreCase("WEB")) {
                   JavascriptExecutor js = (JavascriptExecutor) DriverContext.getObject().getDriver();
                   js.executeScript("Object.keys(localStorage).filter(x => x.startsWith('CognitoIdentityServiceProvider')).forEach(x => localStorage.removeItem(x))");
                }
                VariableContext.getObject().clearScenarioContext();
            }
        } catch (Exception e) {
            if(bit==false) {
                this.reportBase.endTestCase(scenario.isFailed(), true);
                if (PropertyUtility.targetPlatform.equalsIgnoreCase("WEB")) {
                    JavascriptExecutor js = (JavascriptExecutor) DriverContext.getObject().getDriver();
                    js.executeScript("Object.keys(localStorage).filter(x => x.startsWith('CognitoIdentityServiceProvider')).forEach(x => localStorage.removeItem(x))");
                }
                logger.detail("SKIPPED TEST SCENARIO : " + scenario.getName() + " | Skipped Count : " + this.reportBase.skipped());
            }

        }
    }

    public void tearDown() throws IOException {
        try {
            this.reportBase.endSuiteFile();
        }
        catch (Exception ex) { }
    }

    @AfterStep
    public void afterStep() {
        WebDriverActions action = new WebDriverActions();
        action.waitForJStoLoad();
    }
}
