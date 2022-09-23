package com.spintly.api.services;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverContext;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class AppService extends DriverContext {
    private static LogUtility logger = new LogUtility(AppService.class);
    ApiUtility apiUtility = new ApiUtility();
    UserManagementService userManagementService = new UserManagementService();
    LeaveHolidayManagementService leaveHolidayManagementService = new LeaveHolidayManagementService();

    public void gpsCheckin(String phone, Long epoch) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        ;
        String orgID = apiUtility.getOrgnizationID();
        String latitude = PropertyUtility.getDataProperties("latitude");
        String longitude = PropertyUtility.getDataProperties("longitude");

        variableContext.setScenarioContext("CHECKINPHONE", phone);
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + orgID + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");


        jsonString = "{\"messageType\":\"info\",\"messageVersion\":1,\"messageData\":{\"from\":\"app\",\"module\":\"gps\",\"subModule\":\"gpsAttendance\",\"action\":\"CHECK-IN\",\"time\":" + epoch + ",\"data\":{\"response\":{\"message\":\"SUCCESS\"},\"additionalInfo\":{\"currentOrgUserId\":" + userID + ",\"currentOrgId\":" + orgID + "}},\"currentUserId\":220}}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("mobile.api.url") + "/test/v2/errorManagement/mobileLogs");

        jsonString = "{\"records\":[{\"orgUserId\":" + userID + ",\"latitude\":" + latitude + ",\"longitude\":" + longitude + ",\"mocked\":true,\"customParameter\":\"{\\\"direction\\\":\\\"entry\\\"}\",\"accessTime\":" + epoch + ",\"requestId\":\"" + java.util.UUID.randomUUID() + "\",\"apiCalledMobileTime\":" + System.currentTimeMillis() + ",\"delayed\":true}]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/gpsHistory");
        ApiHelper.genericResponseValidation(response, "API - APP CHECK-IN " + userID);

        ResultManager.pass("I checkin as a user ", "I checked in as an user "+ phone +" at epoch "+ epoch, false);

    }

    public void gpsCheckout(String phone, Long epoch) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        ;
        String orgID = apiUtility.getOrgnizationID();
        String latitude = PropertyUtility.getDataProperties("latitude");
        String longitude = PropertyUtility.getDataProperties("longitude");

        variableContext.setScenarioContext("CHECKOUTPHONE", phone);
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + orgID + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");


        jsonString = "{\"messageType\":\"info\",\"messageVersion\":1,\"messageData\":{\"from\":\"app\",\"module\":\"gps\",\"subModule\":\"gpsAttendance\",\"action\":\"CHECK-OUT\",\"time\":" + epoch + ",\"data\":{\"response\":{\"message\":\"SUCCESS\"},\"additionalInfo\":{\"currentOrgUserId\":" + userID + ",\"currentOrgId\":" + orgID + "}},\"currentUserId\":220}}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("mobile.api.url") + "/test/v2/errorManagement/mobileLogs");
        jsonString = "{\"records\":[{\"orgUserId\":" + userID + ",\"latitude\":" + latitude + ",\"longitude\":" + longitude + ",\"mocked\":true,\"customParameter\":\"{\\\"direction\\\":\\\"exit\\\"}\",\"accessTime\":" + epoch + ",\"requestId\":\"" + java.util.UUID.randomUUID() + "\",\"apiCalledMobileTime\":" + epoch + ",\"delayed\":true}]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/gpsHistory");
        ApiHelper.genericResponseValidation(response, "API - APP CHECK-OUT " + userID);
        ResultManager.pass("I checkout as a user ", "I checked out as an user "+phone , false);

    }

    public void applyFullDayLeave(String phone, String leaveType) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        ;
        String orgID = apiUtility.getOrgnizationID();
        String leaveCycleId = leaveHolidayManagementService.getLeaveCycle();
        String userId = userManagementService.getUserID(phone);

        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + orgID + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userLeave/organisations/" + orgID + "/users/" + userId + "/leaveCycles/" + leaveCycleId + "/leaves");
        ApiHelper.genericResponseValidation(response, "API - GET LEAVE ID" + phone);
        jsonPathEvaluator = response.jsonPath();
        String leaveId = jsonPathEvaluator.get("data.userLeaveData.leaves.userLeaveId[0]").toString();

        jsonString = "{\"leaveId\":" + leaveId + ",\"validFrom\":\"" + date + "\",\"validTill\":\"" + date + "\",\"isFullDay\":true,\"userRemark\":\"Personal Leave\"}\n";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + orgID + "/users/" + userId + "/leaves");
        ApiHelper.genericResponseValidation(response, "API - APPLY LEAVE for " + userID);

        ResultManager.pass("I apply "+leaveType+" leave ", "I applied "+leaveType+" leave for "+phone , false);

    }
    public void applyHalfDayLeave(String phone, String leaveType, Boolean isFirstHalf) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        ;
        String orgID = apiUtility.getOrgnizationID();
        String leaveCycleId = leaveHolidayManagementService.getLeaveCycle();
        String userId = userManagementService.getUserID(phone);

        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + orgID + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userLeave/organisations/" + orgID + "/users/" + userId + "/leaveCycles/" + leaveCycleId + "/leaves");
        ApiHelper.genericResponseValidation(response, "API - GET LEAVE ID" + phone);
        jsonPathEvaluator = response.jsonPath();
        String leaveId = jsonPathEvaluator.get("data.userLeaveData.leaves.userLeaveId[0]").toString();

        jsonString = "{\"leaveId\":" + leaveId + ",\"validFrom\":\"" + date + "\",\"validTill\":\"" + date + "\",\"isFullDay\":false,\"isFirstHalf\":\""+isFirstHalf+"\",\"userRemark\":\"Personal Leave\"}\n";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + orgID + "/users/" + userId + "/leaves");
        ApiHelper.genericResponseValidation(response, "API - APPLY LEAVE for " + userID);

        ResultManager.pass("I apply "+leaveType+" leave ", "I applied "+leaveType+" leave for "+phone , false);

    }


    public void gpsCheckinForAccessHistory() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        //String orgID = (String) variableContext.getScenarioContext("ORGID");

        String userID = (String) variableContext.getScenarioContext("NEWUSERID") ;
        String userName = (String) variableContext.getScenarioContext("NEWUSERNAME") ;

        Date now = new Date();
        String epoch = String.valueOf(now.getTime()).substring(0,10);

        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", java.util.Locale.ENGLISH); //dd/MM/yyyy

        String fullDate = sdfDate.format(now);

        String jsonString = "{\n" +
                "  \"records\": [\n" +
                "    {\n" +
                "      \"orgUserId\": "+userID+",\n" +
                "      \"latitude\": \"98.23\",\n" +
                "      \"longitude\": \"72.32\",\n" +
                "      \"customParameter\": \"{\\\"direction\\\": \\\"entry\\\"}\",\n" +
                "      \"accessTime\": "+epoch+",\n" +
                "      \"apiCalledMobileTime\": "+epoch+",\n" +
                "      \"mocked\": true,\n" +
                "      \"delayed\": true\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/gpsHistory");
        ApiHelper.genericResponseValidation(response, "API - APP CHECK-IN FOR ACCESS HISTORY ");

        variableContext.setScenarioContext("GPSCHECKINTIME",fullDate);
    }

    public void gpsCheckinForAccessHistoryPreviousDate() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        //String orgID = (String) variableContext.getScenarioContext("ORGID");

        String userID = (String) variableContext.getScenarioContext("NEWUSERID") ;
        String userName = (String) variableContext.getScenarioContext("NEWUSERNAME") ;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
        LocalDateTime yesterdayDate = LocalDateTime.now().minusDays(1);
        String yDate = dtf.format(yesterdayDate);

        variableContext.setScenarioContext("PREVIOUSACCESSDATE",yDate.split(" ")[0]);

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        Date now = sdfDate.parse(yDate);
        String epoch = String.valueOf(now.getTime()).substring(0,10);

//        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", java.util.Locale.ENGLISH); //dd/MM/yyyy

        String fullDate = sdfDate.format(now);

        String jsonString = "{\n" +
                "  \"records\": [\n" +
                "    {\n" +
                "      \"orgUserId\": "+userID+",\n" +
                "      \"latitude\": \"98.23\",\n" +
                "      \"longitude\": \"72.32\",\n" +
                "      \"customParameter\": \"{\\\"direction\\\": \\\"entry\\\"}\",\n" +
                "      \"accessTime\": "+epoch+",\n" +
                "      \"apiCalledMobileTime\": "+epoch+",\n" +
                "      \"mocked\": true,\n" +
                "      \"delayed\": true\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/gpsHistory");
        ApiHelper.genericResponseValidation(response, "API - APP CHECK-IN FOR ACCESS HISTORY ");

        variableContext.setScenarioContext("GPSCHECKINTIME",fullDate);
    }

    public void gpsCheckoutForAccessHistory() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        Date date = sdfDate.parse(strDate);
        String epoch = String.valueOf(date.getTime()).substring(0,10);


        String userID = (String) variableContext.getScenarioContext("NEWUSERID") ;
        String userName = (String) variableContext.getScenarioContext("NEWUSERNAME") ;

        String jsonString = "{\n" +
                "  \"records\": [\n" +
                "    {\n" +
                "      \"orgUserId\": "+userID+",\n" +
                "      \"latitude\": \"98.23\",\n" +
                "      \"longitude\": \"72.32\",\n" +
                "      \"customParameter\": \"{\\\"direction\\\": \\\"exit\\\"}\",\n" +
                "      \"accessTime\": "+epoch+",\n" +
                "      \"apiCalledMobileTime\": "+epoch+",\n" +
                "      \"mocked\": true,\n" +
                "      \"delayed\": true\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/gpsHistory");
        ApiHelper.genericResponseValidation(response, "API - APP CHECK-OUT FOR ACCESS HISTORY ");
    }

}
