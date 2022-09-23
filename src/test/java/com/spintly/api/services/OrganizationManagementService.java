package com.spintly.api.services;

import com.google.gson.Gson;
import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverContext;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

public class OrganizationManagementService extends DriverContext {
    private static LogUtility logger = new LogUtility(OrganizationManagementService.class);
    ApiUtility apiUtility = new ApiUtility();

    public String getAttendanceSettings() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/attendance-settings");
        ApiHelper.genericResponseValidation(response, "API - GET ATTENDANCE SETTINGS ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        variableContext.setScenarioContext("TODAYSVISITORS", jsonPathEvaluator.get("message"));
        Gson gson = new Gson();
        return gson.toJson(jsonPathEvaluator.get("message"), LinkedHashMap.class);
    }

    public void enableRegularization() throws ParseException {
        JSONObject json = new JSONObject(getAttendanceSettings());
        json.remove("regularizationSettings");
        json.put("regularizationSettings", true);
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(json.toString())
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/attendance-data/regularization-settings");
        ApiHelper.genericResponseValidation(response, "API - ENABLE REGULARIZATION");
        ResultManager.pass("I enable regularization", "I enabled regularization" , false);

    }

    public void disableRegularization() throws ParseException {
        //String jsonString = getAttendanceSettings();
        JSONObject json = new JSONObject(getAttendanceSettings());
        json.remove("regularizationSettings");
        json.put("regularizationSettings", false);
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(json.toString())
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/attendance-data/regularization-settings");
        ApiHelper.genericResponseValidation(response, "API - DISABLE REGULARIZATION");
        ResultManager.pass("I disable regularization", "I disabled regularization" , false);

    }
}
