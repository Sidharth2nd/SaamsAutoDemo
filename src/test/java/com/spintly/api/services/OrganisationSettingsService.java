package com.spintly.api.services;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverContext;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import com.spintly.base.utilities.RandomDataGenerator;
import io.cucumber.datatable.DataTable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrganisationSettingsService extends DriverContext {
    private static LogUtility logger = new LogUtility(ShiftManagementService.class);
    ApiUtility apiUtility = new ApiUtility();
    RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    UserManagementService userManagementService = new UserManagementService();

    public void restoreGeneralSettings() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage ();
        String jsonString = "{\"name\":\"AutomationCA\",\"email\":\"arundhati.k@spintly.com\",\"phone\":\"+917722082259\",\"location\":\"Goa\",\"id\":776}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/details");

        ApiHelper.genericResponseValidation(response, "API - RESTORE GENERAL SETTINGS");
    }

    public List<String> listOfCustomAttributes() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> attributes = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");


        ApiHelper.genericResponseValidation(response, "API - GET LIST OF CUSTOM ATTRIBUTES IN ORGANISATION");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> values = jsonPathEvaluator.get("message.attributes");

        attributes = jsonPathEvaluator.get("message.attributes.attributeName");


        return attributes;
    }

//https://test.api.spintly.com/v2/organisationManagement/organisations/703/unlockSettings

    public void restoreMobileBasesAccessSettings() throws ParseException, InterruptedException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String payload = (String) variableContext.getScenarioContext("CURRENTACCESSSETTINGS");
        String jsonString = "";

        if(payload.equalsIgnoreCase("")){
            jsonString = "{\"remoteAccess\":false,\"clickToAccess\":true,\"clickToAccessRange\":\"4\",\"proximityAccessRange\":\"4\",\"proximityAccess\":true,\"deviceLock\":true,\"mobile\":true,\"tapToAccess\":true,\"card\":true,\"fingerprint\":true,\"mfa\":true}";
        }else{
            jsonString = payload;
        }

        String orgID = (String) variableContext.getScenarioContext("ORGID");

        Thread.sleep(20000);

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/unlockSettings");

        ApiHelper.genericResponseValidation(response, "API - RESTORE MOBILE ACCESS SETTINGS");
    }


    public void editMobileBasesAccessSettings(DataTable data) throws ParseException, InterruptedException {
        String token = apiUtility.getidTokenFromLocalStorage();
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        String orgID = (String) variableContext.getScenarioContext("ORGID");

        String remoteAccess = dataMap.get("remoteAccess").trim();
        String clickToAccess = dataMap.get("clickToAccess").trim();
        String clickToAccessRange = dataMap.get("clickToAccessRange").trim();
        String proximityAccessRange = dataMap.get("proximityAccessRange").trim();
        String proximityAccess = dataMap.get("proximityAccess").trim();
        String deviceLock = dataMap.get("deviceLock").trim();
        String mobile = dataMap.get("tapToAccess").trim();
        String tapToAccess = dataMap.get("tapToAccess").trim();
        String card = dataMap.get("card").trim();
        String fingerprint = dataMap.get("fingerprint").trim();
        String mfa = dataMap.get("mfa").trim();

        Thread.sleep(20000);

        String jsonString = "{\"remoteAccess\":"+remoteAccess+",\"clickToAccess\":"+clickToAccess+",\"clickToAccessRange\":\"4\",\"proximityAccessRange\":\"4\",\"proximityAccess\":"+proximityAccess+",\"deviceLock\":"+deviceLock+",\"mobile\":"+mobile+",\"tapToAccess\":"+tapToAccess+",\"card\":"+card+",\"fingerprint\":"+fingerprint+",\"mfa\":"+mfa+"}";

        System.out.println("response.asString()");

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/organisationManagement/organisations/" + orgID + "/unlockSettings");

//        System.out.println(response.asString());
//        System.out.println(response.getTime());

        ApiHelper.genericResponseValidation(response, "API - EDIT MOBILE BASED ACCESS SETTINGS");
    }

    public void editAttendanceSettings(DataTable data) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

        String regularizationSettings = dataMap.get("regularizationSettings").trim();


        String jsonString = "{\"regularizationSettings\":"+regularizationSettings+",\"halfDayFullDaySettings\":{\"id\":1149,\"halfDayFullDayEnabled\":false,\"minHalfDayInMinutes\":0,\"minFullDayInMinutes\":0}}";

        String path  = PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/attendanceManagement/organisations/703/attendance-data/regularization-settings";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post("https://test.api.spintly.com" + "/v2/attendanceManagement/organisations/"+apiUtility.getOrgnizationID()+"/attendance-data/regularization-settings");

        ApiHelper.genericResponseValidation(response, "API - EDIT ATTENDANCE SETTINGS");
    }

    public void restoreAttendanceSettings() throws ParseException {
        //{"regularizationSettings":false,"halfDayFullDaySettings":{"id":1149,"halfDayFullDayEnabled":false,"minHalfDayInMinutes":0,"minFullDayInMinutes":0}}
        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"regularizationSettings\":true,\"halfDayFullDaySettings\":{\"id\":1149,\"halfDayFullDayEnabled\":false,\"minHalfDayInMinutes\":0,\"minFullDayInMinutes\":0}}";

        String path  = PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/attendanceManagement/organisations/703/attendance-data/regularization-settings";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post("https://test.api.spintly.com" + "/v2/attendanceManagement/organisations/"+apiUtility.getOrgnizationID()+"/attendance-data/regularization-settings");

        ApiHelper.genericResponseValidation(response, "API - RESTORE ATTENDANCE SETTINGS");
    }
}
