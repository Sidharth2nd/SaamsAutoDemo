package com.spintly.api.services;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverContext;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import com.spintly.base.utilities.RandomDataGenerator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PartnerService extends DriverContext {
    private static LogUtility logger = new LogUtility(AppService.class);
    ApiUtility apiUtility = new ApiUtility();

    public void addAccessPoint(String serialNumber) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String shortName = RandomDataGenerator.randomAlphabetic(3);
        String name = "AutoAp" + shortName;
        String accessPoint = getAccessPoints(serialNumber);
        if (accessPoint != "") {
            deleteAccessPoint(accessPoint);
            variableContext.setScenarioContext("COUNT", 1);

        } else {
            variableContext.setScenarioContext("COUNT", 0);
        }
        String jsonString = "{\"accessPointType\":\"doorsAndGates\",\"siteId\":843,\"name\":\"" + name + "\",\"installationType\":\"swingDoor\",\"devices\":[{\"serialNumber\":\"" + serialNumber + "\",\"type\":\"entry\"}],\"input_exit_dtls\":false,\"input_ds\":false,\"forAttendance\":false,\"forAccess\":true,\"doorSensor\":{\"direction\":\"entry\",\"enabled\":false,\"type\":\"NO\"}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v3/partners/1/organisations/" + orgID + "/accessPoints");
        ApiHelper.genericResponseValidation(response, "API - ADD ACCESSPOINT " + name);
        ResultManager.pass("I add access points", "I added access points with serial number "+ serialNumber , false);

    }

    public void addAccessPointUnderNewSite(String serialNumber) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String shortName = RandomDataGenerator.randomAlphabetic(3);
        String name = "AutoAp" + shortName;
        String accessPoint = getAccessPoints(serialNumber);
        if (accessPoint != "") {
            deleteAccessPoint(accessPoint);
            variableContext.setScenarioContext("COUNT", 1);

        } else {
            variableContext.setScenarioContext("COUNT", 0);
        }

        String siteID = (String) variableContext.getScenarioContext("SITEID");

        String jsonString = "{\"accessPointType\":\"doorsAndGates\",\"siteId\":"+siteID+",\"name\":\"" + name + "\",\"installationType\":\"swingDoor\",\"devices\":[{\"serialNumber\":\"" + serialNumber + "\",\"type\":\"entry\"}],\"input_exit_dtls\":false,\"input_ds\":false,\"forAttendance\":false,\"forAccess\":true,\"doorSensor\":{\"direction\":\"entry\",\"enabled\":false,\"type\":\"NO\"}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v3/partners/1/organisations/" + orgID + "/accessPoints");
        ApiHelper.genericResponseValidation(response, "API - ADD ACCESSPOINT " + name);
        ResultManager.pass("I add access points", "I added access points with serial number "+ serialNumber , false);

    }

    public void deleteAccessPoint(String accessPoint) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v2/partners/1/organisations/" + orgID + "/accessPoints/" + accessPoint);
        ApiHelper.genericResponseValidation(response, "API - DELETE ACCESSPOINT ");
        ResultManager.pass("I delete access points", "I deleted access point "+ accessPoint , false);

    }

    public String getAccessPoints(String serialNumber) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v3/partners/1/organisations/" + orgID + "/accessPoints");
        JsonPath jsonPathEvaluator = jsonPathEvaluator = response.jsonPath();
        ArrayList<LinkedHashMap<String, String>> accessPoints = jsonPathEvaluator.get("message.accessPoints");
        int i = 0;
        ResultManager.pass("I get access points", "I got access point " , false);
        for (LinkedHashMap accessPoint : accessPoints) {
            ArrayList devices = (ArrayList) accessPoint.get("devices");
            if (((LinkedHashMap) (devices.get(i))).get("serialNumber").equals(serialNumber)) {
                return accessPoint.get("id").toString();
            }
            i++;
        }

        return "";
    }

    public void addSite(){
        String token = apiUtility.getidTokenFromLocalStorage();

        variableContext.setScenarioContext("PREVIOUSTOKEN",token);
        String orgID = (String) variableContext.getScenarioContext("ORGID");

        String name = "site"+RandomDataGenerator.getData("{RANDOM_STRING}",5);
        String location  = "loc"+RandomDataGenerator.getData("{RANDOM_STRING}",5);

        String jsonString = "{\"name\":\""+name+"\",\"location\":\""+location+"\"}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v2/partners/1/organisations/" + orgID + "/sites");

        ApiHelper.genericResponseValidation(response, "API - CREATE SITE IN PARTNER");

        JsonPath jsonPathEvaluator = response.jsonPath();
        String siteId = jsonPathEvaluator.get("message.siteId").toString();

        variableContext.setScenarioContext("SITEID",siteId);
        variableContext.setScenarioContext("SITENAME",name);
        variableContext.setScenarioContext("SITELOCATION",location);
        variableContext.setScenarioContext("DELETESITE","TRUE");
    }

    public void deleteSite(){
        String token = apiUtility.getidTokenFromLocalStorage();
        String previousToken = (String) variableContext.getScenarioContext("PREVIOUSTOKEN");

        if(!(previousToken.equalsIgnoreCase(""))){
            token = previousToken;
        }

        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String siteID = (String) variableContext.getScenarioContext("SITEID");

        String path = PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v2/partners/1/organisations/" + orgID + "/sites/"+siteID;
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v2/partners/1/organisations/" + orgID + "/sites/"+siteID);

        ApiHelper.genericResponseValidation(response, "API - DELETE SITE IN PARTNER");
    }

    public void deleteNewSite(){
         String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("NEWORGID");
        String siteID = (String) variableContext.getScenarioContext("NEWSITEID");

        String previousToken = (String) variableContext.getScenarioContext("PREVIOUSTOKEN");

        if(!(previousToken.equalsIgnoreCase(""))){
            token = previousToken;
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v2/partners/1/organisations/" + orgID + "/sites/"+siteID);

        ApiHelper.genericResponseValidation(response, "API - DELETE NEW SITE FROM ORG IN PARTNER");
    }

    public void createNewOrganisation(){
        String token = apiUtility.getidTokenFromLocalStorage();
        variableContext.setScenarioContext("PREVIOUSTOKEN",token);

        String newOrgName = "ORG"+RandomDataGenerator.getData("{RANDOM_STRING}",5);

        String newSiteName = "site"+RandomDataGenerator.getData("{RANDOM_STRING}",5);
        String siteLocation  = "loc"+RandomDataGenerator.getData("{RANDOM_STRING}",5);

        String jsonString = "{\"name\":\""+newOrgName+"\",\"type\":\"office\",\"country\":\"IN\",\"integratorId\":1,\"admin\":{\"name\":\"Sidharth\",\"phoneNumber\":\"+919284174823\",\"email\":\"sidharth.k@spintly.com\"},\"site\":[{\"name\":\""+newSiteName+"\",\"location\":\""+siteLocation+"\"}]}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v2/partners/1/organisations");

        ApiHelper.genericResponseValidation(response, "API - CREATE ORG IN PARTNER");

        JsonPath jsonPathEvaluator = response.jsonPath();
        String newSiteId = jsonPathEvaluator.get("message.siteId").toString();
        String newOrgID = jsonPathEvaluator.get("message.organisationId").toString();

        variableContext.setScenarioContext("NEWORGNAME",newOrgName);
        variableContext.setScenarioContext("NEWSITENAME",newSiteName);
        variableContext.setScenarioContext("NEWORGID",newOrgID);
        variableContext.setScenarioContext("NEWSITEID",newSiteId);

        variableContext.setScenarioContext("DELETENEWORG","TRUE");
    }

    public void deleteNewOrg(){
        String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("NEWORGID");

        String previousToken = (String) variableContext.getScenarioContext("PREVIOUSTOKEN");

        if(!(previousToken.equalsIgnoreCase(""))){
            token = previousToken;
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("partner.api.url") + "/test/infrastructureManagement/v2/partners/1/organisations/"+orgID);

        ApiHelper.genericResponseValidation(response, "API - DELETE ORG IN PARTNER");
    }
}
