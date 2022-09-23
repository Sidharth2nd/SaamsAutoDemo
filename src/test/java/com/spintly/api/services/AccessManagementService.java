package com.spintly.api.services;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverContext;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import cucumber.api.java.en.And;
import io.cucumber.datatable.DataTable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccessManagementService extends DriverContext {
    private static LogUtility logger = new LogUtility(AccessManagementService.class);
    ApiUtility apiUtility = new ApiUtility();

    public String getActiveAPsCount() throws ParseException {
        String jsonString = "{\"filters\":{\"sites\":null},\"pagination\":{\"perPage\":25,\"page\":1}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/accessPoint");
        ApiHelper.genericResponseValidation(response, "API - GET ACTIVE APS COUNT ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String count = jsonPathEvaluator.get("message.pagination.total").toString();
        variableContext.setScenarioContext("ACTIVEAPS", count);
        ResultManager.pass("I get ACTIVE APs count ", "I got ACTIVE APs count as "+ count, false);
        return count;
    }

    public String getAccessPointsCount() throws ParseException {
        String jsonString = "{\"filters\":{\"sites\":null},\"pagination\":{\"perPage\":25,\"page\":1}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/accessPoint");
        ApiHelper.genericResponseValidation(response, "API - GET ACCESS POINTS COUNT ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String count = jsonPathEvaluator.get("message.pagination.total").toString();
        variableContext.setScenarioContext("ACCESSPOINTS", count);
        ResultManager.pass("I get ACCESS POINTS count ", "I got ACCESS POINTS count as "+ count, false);
        return count;
    }

    public List<String> accessPointNamesWithFilters(String filter, String value){
        String token = apiUtility.getTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String jsonString = "";

        List<String> names = new ArrayList<>();

        switch (filter){
            case "type":
                if(value.equalsIgnoreCase("door")){
                    jsonString = "{\"filters\":{\"sites\":null,\"isDefault\":null,\"type\":\"door\"},\"pagination\":{\"perPage\":100,\"page\":1,\"current_page\":1,\"per_page\":100,\"currentPage\":1}}";
                    break;
                }else if(value.equalsIgnoreCase("clockin")){
                    jsonString = "{\"filters\":{\"sites\":null,\"isDefault\":null,\"type\":\"clockin\"},\"pagination\":{\"perPage\":100,\"page\":1,\"current_page\":1,\"per_page\":100,\"currentPage\":1}}";
                }
                break;
            case "MFA":
                if(value.equalsIgnoreCase("enabled")){
                    jsonString = "{\"filters\":{\"sites\":null,\"isDefault\":null,\"type\":null,\"mfa\":true},\"pagination\":{\"perPage\":100,\"page\":1,\"current_page\":1,\"per_page\":100,\"currentPage\":1}}";
                    break;
                }else if(value.equalsIgnoreCase("disabled")){
                    jsonString = "{\"filters\":{\"sites\":null,\"isDefault\":null,\"type\":null,\"mfa\":false},\"pagination\":{\"perPage\":100,\"page\":1,\"current_page\":1,\"per_page\":100,\"currentPage\":1}}";
                }
                break;
            case "Default Door":
                if(value.equalsIgnoreCase("true")){
                    jsonString = "{\"filters\":{\"sites\":null,\"isDefault\":true,\"type\":null,\"mfa\":null},\"pagination\":{\"perPage\":100,\"page\":1,\"current_page\":1,\"per_page\":100,\"currentPage\":1}}";
                    break;
                }else if(value.equalsIgnoreCase("false")){
                    jsonString = "{\"filters\":{\"sites\":null,\"isDefault\":false,\"type\":null,\"mfa\":null},\"pagination\":{\"perPage\":100,\"page\":1,\"current_page\":1,\"per_page\":100,\"currentPage\":1}}";
                }
                break;
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/organisationManagement/v3/organisations/"+orgID+"/accessPoints");

        ApiHelper.genericResponseValidation(response, "API - Return Access Point Names with Filters ");

        JsonPath jsonPathEvaluator = response.jsonPath();
        names = jsonPathEvaluator.get("message.accessPoints.name");

        return names.stream().sorted().collect(Collectors.toList());
    }

    public void accessPointConfiguration(DataTable data, boolean both){
        String token = apiUtility.getTokenFromLocalStorage();

        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

        String setting = dataMap.get("setting").trim();
        String value = dataMap.get("state").trim();

        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String jsonString = "";

        switch (setting){
            case "mfa":
                jsonString = "{\"mfa\": "+value+"}";
                variableContext.setScenarioContext("RestoreApConfigurationmfa","TRUE");
                break;
            case "default door":
                variableContext.setScenarioContext("RestoreApConfigurationdef","TRUE");
                jsonString = "{\"isDefault\": "+value+"}";
        }

        String path = PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/accessPoint/12/";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/accessPoint/12/");

        ApiHelper.genericResponseValidation(response, "API - Change Access Point Configuration for Access Point 1");

        if(both){
            Response response2 = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/accessPoint/16/");

            ApiHelper.genericResponseValidation(response2, "API - Change Access Point Configuration for Access point 2 ");

            variableContext.setScenarioContext("RestoreApConfigurationBoth","TRUE");
        }

    }

    public void restoreAccessPointConfiguration(String toRestore){
        String token = apiUtility.getTokenFromLocalStorage();

        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String jsonString = "{isDefault: false}";

        String both = (String) variableContext.getScenarioContext("RestoreApConfigurationBoth");

        switch (toRestore){
            case "RestoreApConfigurationmfa":
                jsonString = "{\"mfa\": false}";
                break;
            case "RestoreApConfigurationdef":
                jsonString = "{\"isDefault\": false}";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/accessPoint/12/");

        ApiHelper.genericResponseValidation(response, "API - Restore Access Point Configuration for 1st AP");

        if(both.equalsIgnoreCase("TRUE")){
            Response response2 = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/accessPoint/16/");

            ApiHelper.genericResponseValidation(response2, "API - Restore Access Point Configuration for 2nd AP");
        }

    }

    //Get current access settings
    public void getCurrentAccessSettings(){
        String token = apiUtility.getTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String payload = "";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/unlockSettings");

        ApiHelper.genericResponseValidation(response, "API - GET CURRENT ACCESS SETTINGS");

        payload = new JSONObject(response.asString()).getJSONObject("message").getJSONObject("unlockSettings").toString();

        variableContext.setScenarioContext("CURRENTACCESSSETTINGS",payload);
    }

    public void restoreAPName(){
        String token = apiUtility.getTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");

        String jsonString = "{\"name\":\"12Surat_Testing_DoNotDelete\"}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/accessPoint/16/");

        ApiHelper.genericResponseValidation(response, "API - RESTORED ACCESS POINT NAME TO 12Surat_Testing_DoNotDelete");

    }

    //Delete User Api
    public void deleteUserAPI(){
        String token = "eyJraWQiOiJtK2xxeUdNWEJPRDVSa2Q5MlwvaWtVcXIzdW5LNndPNU1GQnQzc1UxdUZyTT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMGY3MDNhMC0xZjY1LTQzZDAtYTYxYi03OTY0MWM3ZDkyY2YiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmFwLXNvdXRoLTEuYW1hem9uYXdzLmNvbVwvYXAtc291dGgtMV9yR3RvaDVpdWUiLCJwaG9uZV9udW1iZXJfdmVyaWZpZWQiOnRydWUsImNvZ25pdG86dXNlcm5hbWUiOiIxMGY3MDNhMC0xZjY1LTQzZDAtYTYxYi03OTY0MWM3ZDkyY2YiLCJvcmlnaW5fanRpIjoiZTEwMmY0ZDgtZjliYi00Y2VlLTkxZmYtMTRiODAyZDVlZGM4IiwiYXVkIjoiMWY5YnR0MWNranRqbmsxN2pvMmE1MDI4azQiLCJldmVudF9pZCI6IjI0MDA4MzM5LTE3NDgtNGRhMS1hMzYwLTAxMzg1OTdjOTBmZiIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNjU4MjA2MDUxLCJwaG9uZV9udW1iZXIiOiIrOTE3MDY2ODAyOTk1IiwiZXhwIjoxNjU4MjA5NjUxLCJpYXQiOjE2NTgyMDYwNTEsImp0aSI6ImQyM2Q5Mjc1LTFiOGYtNDhkNC1iYjJlLWEzOTZkZTExZWNiZSIsImVtYWlsIjoic2FidXJpYmFtYm9sa2FyQGdtYWlsLmNvbSJ9.8Ew8JsClYV-gBuD42FCiwf_pjcksCooVMUpt8STmxsPqzBRdZV9ZCBEmddRmpHrIyn82CFoqN1Byf_EWWVQ-sdFZbfmQhAZUCo_tFhkToy3Rj4rFgXTJNf49v2ZthLs8_bTprHXwAb_qAxLzyUR2l0Pbpl0ArOYVql8q9KK6fxY96cckLk2ZR8jSPZlrW_v42XuNuBYQMc7qLPTrJBiNovOg2r7XOibTDBo-6W0_RnXamFmZ8Thi3SfiSMK5BM4AaSNG9a4vJMZ80gkZ-NlBAPauc2nXbnSklh674d7_7xxJrpdIV7keGdJ7F9b9bk8Ov8n8Tby16HVkI1bHRQTX4w";

        String[] users = {"918530428550", "918888804606", "918806126817", "917588130974", "918975345483", "917378830611", "919764704079", "919971129800", "917558685652", "919423886767", "919604167936", "918806406430", "918805898339", "918147993315", "918208093533", "918007130513", "917875426473", "917020061557", "919663985134", "917972046516", "919880111799", "918657110990", "919959644644", "917875023790", "919145171151", "919370614324", "919763815974", "919403527748", "916360473487", "918007395578", "919884846585", "919921969658", "919359192032", "918975858287", "918668960229", "919922854404", "919623584551", "918208690748", "917066419875", "917263981070", "918208962564", "918459452380", "919900132780", "919545755781", "919886450273", "919860709109", "919158404160", "919545450310", "917798019868", "919096102624", "919359973372", "918007450809", "919699068359", "917066174563", "918766602665", "919545036796", "919637943984", "919004398188", "917875722802", "919503750329", "918412031641", "919686685328", "917218534625", "918550903953", "917020378589", "918637705687", "917038109344", "917066802995", "919309858071", "919307959308", "918880406714", "919168743629", "917030742719", "918275384744", "919886960605", "917601034609", "917350179497", "919663881983", "919011028872", "919765048647", "919158858107", "919011411974", "917066139650", "917588452365", "919890726906", "919764745509", "919405922118", "917722082259", "918788302574", "918805452817", "919642935500", "917083912359", "919975104884", "918975306150", "918637747238", "918888405922", "917796360315", "918888761535", "918329107319", "919426588774", "919764598824", "919561170742", "918806868161", "918553630035"};

        String[] users1={"918530428550", "918975345483", "919764704079", "919971129800", "919423886767", "919604167936", "918805898339", "918147993315", "918208093533", "917875426473", "917020061557", "919663985134", "917972046516", "919880111799", "918657110990", "919959644644", "917875023790", "919145171151", "919763815974", "919403527748", "916360473487", "918007395578", "919884846585", "919359192032", "919922854404", "919623584551", "918208690748", "917066419875", "917263981070", "918208962564", "919900132780", "919545755781", "919886450273", "919860709109", "919158404160", "919545450310", "917798019868", "919096102624", "919359973372", "918007450809", "919699068359", "917066174563", "919545036796", "919637943984", "919004398188", "917875722802", "919503750329", "919686685328", "917218534625", "918550903953", "917020378589", "918637705687", "917038109344", "917066802995", "919309858071", "918880406714", "919168743629", "919886960605", "917601034609", "917350179497", "919663881983", "919011028872", "919765048647", "919158858107", "919011411974", "917066139650", "917588452365", "919405922118", "917722082259", "918788302574", "919642935500", "917083912359", "919975104884", "918975306150", "918637747238", "918888761535", "918329107319", "919426588774", "919764598824", "918553630035"};

        for(int i =0; i< users.length;i++){
            String body = "{\n" +
                    "  \"phone\": \"+"+users1[i]+"\"\n" +
                    "}";

            Response response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(body)
                    .when().redirects().follow(false).
                    post(" https://nqfdy5tisa.execute-api.ap-south-1.amazonaws.com/prod/developerSupport/v1/userManagement/deleteCognitoUser");

//            System.out.println(users1[i]);
//            System.out.println(response.asString());
        }

    }



}
