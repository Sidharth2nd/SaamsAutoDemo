package com.spintly.api.services;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverContext;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import com.spintly.base.utilities.RandomDataGenerator;
import io.cucumber.datatable.DataTable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebElement;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class VisitorManagementService extends DriverContext {
    private static LogUtility logger = new LogUtility(VisitorManagementService.class);
    ApiUtility apiUtility = new ApiUtility();
    UserManagementService userManagementService = new UserManagementService();

    public String getTodaysVisitorsCount() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(new Date());
        String jsonString = "{\"filters\":{\"visitors\":[],\"users\":[],\"startDate\":\"" + strDate + " 00:00:00 +05:30\",\"endDate\":\"" + strDate + " 23:59:59 +05:30\",\"meetingPurpose\":null,\"status\":\"Scheduled\",\"kiosks\":[],\"covidStatus\":null},\"pagination\":{\"per_page\":25,\"perPage\":25,\"page\":1}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/visitors/history");
        ApiHelper.genericResponseValidation(response, "API - GET TODAYS VISITOR COUNT SCHEDULED");
        JsonPath jsonPathEvaluator = response.jsonPath();

        int count1 = jsonPathEvaluator.get("message.pagination.total");
         jsonString = "{\"filters\":{\"visitors\":[],\"users\":[],\"startDate\":\"" + strDate + " 00:00:00 +05:30\",\"endDate\":\"" + strDate + " 23:59:59 +05:30\",\"meetingPurpose\":null,\"status\":\"Cancelled\",\"kiosks\":[],\"covidStatus\":null},\"pagination\":{\"per_page\":25,\"perPage\":25,\"page\":1}}";

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") +  "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/visitors/history");
        ApiHelper.genericResponseValidation(response, "API - GET TODAYS VISITOR COUNT CANCELLED");
        jsonPathEvaluator = response.jsonPath();
        int count2 = jsonPathEvaluator.get("message.pagination.total");

        String value = String.valueOf(count1-count2);
        ResultManager.pass("I get todays visitors count ", "I got todays visitors count as " +value , false);

        variableContext.setScenarioContext("TODAYSVISITORS", value);

        return value;
    }

    public void addVisitor() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String   phone = PropertyUtility.getDataProperties("admin.username");
        String   userId = userManagementService.getUserID(phone);

        String orgId = (String) variableContext.getScenarioContext("ORGID");

        String visitorPhone = RandomDataGenerator.getData("{RANDOM_PHONE_NUM}");
        String purpose = "Delivery";
        String visitorName = "James API" + RandomDataGenerator.randomAlphabetic(5);
        String email = PropertyUtility.getDataProperties("common.email.id");
        String meetingDuration = "240";
        String dateTime = "";
        String additionalInfo = "additional info";
        LocalDateTime now = LocalDateTime.now().plusMinutes(5);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTime = dtf1.format(now) + " +05:30";
        String jsonString = "{\"visitorName\":\"" + visitorName + "\",\"email\":\"" + email + "\",\"phone\":\"" + visitorPhone + "\",\"meetingPurpose\":\"" + purpose + "\",\"meetingDuration\":" + meetingDuration + ",\"accessModes\":{\"qr\":false,\"card\":false},\"permissionsToAdd\":[],\"additionalInfo\":\""+additionalInfo+"\",\"visitDate\":\"" + dateTime + "\"}\n";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/users/" + userId + "/visitors/schedule");

        ApiHelper.genericResponseValidation(response, "API - ADD VISITOR ");
        variableContext.setScenarioContext("VISITORPHONE", phone);
        variableContext.setScenarioContext("VISITORNAME", visitorName);
        variableContext.setScenarioContext("VISITORID", getExpectedVisitors());
        variableContext.setScenarioContext("MEETINGID",getMeetingId());
        variableContext.setScenarioContext("VISITDATE",dateTime);
        variableContext.setScenarioContext("VISITOREMAIL",email);
        variableContext.setScenarioContext("USERID",userId);
        ResultManager.pass("I add visitor ", "I added visitor "+visitorPhone  , false);

        variableContext.setScenarioContext("DELETEVISITOR","TRUE");
    }

    public void addSecondVisitor(String fd) throws ParseException {
        String phone = "";
        String userId = "";
        String token = apiUtility.getTokenFromLocalStorage();

        if(fd.equalsIgnoreCase("FD")) {
            phone = PropertyUtility.getDataProperties("frontdesk.username");
            userId = userManagementService.getUserID(phone);
            variableContext.setScenarioContext("FD","TRUE");
        }else {
            phone = PropertyUtility.getDataProperties("admin.username");
            userId = userManagementService.getUserID(phone);
        }

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        String visitorPhone = RandomDataGenerator.getData("{RANDOM_PHONE_NUM}");
        String purpose = "Maintenance";
        String visitorName = "James API" + RandomDataGenerator.randomAlphabetic(5);
        String email = PropertyUtility.getDataProperties("common.email.id");
        String meetingDuration = "240";
        String dateTime = "";

        LocalDateTime now = LocalDateTime.now().plusDays(2);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTime = dtf1.format(now) + " +05:30";

        String jsonString = "{\"visitorName\":\"" + visitorName + "\",\"email\":\"" + email + "\",\"phone\":\"" + visitorPhone + "\",\"meetingPurpose\":\"" + purpose + "\",\"meetingDuration\":" + meetingDuration + ",\"accessModes\":{\"qr\":false,\"card\":false},\"permissionsToAdd\":[],\"additionalInfo\":\"\",\"visitDate\":\"" + dateTime + "\"}\n";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/users/" + userId + "/visitors/schedule");
        ApiHelper.genericResponseValidation(response, "API - ADD VISITOR ");
        variableContext.setScenarioContext("SECONDVISITORPHONE", phone);
        variableContext.setScenarioContext("SECONDVISITORNAME", visitorName);
        variableContext.setScenarioContext("SECONDVISITORID", getSecondVisitorID());
        variableContext.setScenarioContext("SECONDMEETINGID",getSecondMeetingId());
        variableContext.setScenarioContext("SECONDVISITDATE",dateTime);
        ResultManager.pass("I add visitor ", "I added visitor "+visitorPhone  , false);

        variableContext.setScenarioContext("SECONDDELETEVISITOR","TRUE");
    }

    public String getExpectedVisitors() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(new Date());
        String jsonString = "{\"filters\":{\"startDate\":\"" + strDate + " 00:00:00 +05:30\"},\"pagination\":{\"per_page\":100,\"perPage\":100,\"page\":1,\"current_page\":1,\"currentPage\":1}}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/visitors/expected");

        ApiHelper.genericResponseValidation(response, "API - GET ALL VISITORS ");

        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println(response.asString());
        ArrayList<LinkedHashMap> visitors = jsonPathEvaluator.get("message.visitorsHistory");
        String id = "";
        String visitorName = (String) variableContext.getScenarioContext("VISITORNAME");
        for (int i = 0; i < visitors.size(); i++) {
            LinkedHashMap map = (LinkedHashMap) visitors.get(i);
            if (map.get("visitorName").equals(visitorName)) {
                id = String.valueOf(map.get("visitorId"));
            }
        }
        return id;
    }

    public String getSecondVisitorID() throws ParseException {
        String token =  apiUtility.getTokenFromLocalStorage();

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(new Date());
        String jsonString = "{\"filters\":{\"startDate\":\"" + strDate + " 00:00:00 +05:30\"},\"pagination\":{\"per_page\":100,\"perPage\":100,\"page\":1,\"current_page\":1,\"currentPage\":1}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/visitors/expected");
        ApiHelper.genericResponseValidation(response, "API - GET ALL VISITORS ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println(response.asString());
        ArrayList<LinkedHashMap> visitors = jsonPathEvaluator.get("message.visitorsHistory");
        String id = "";
        String visitorName = (String) variableContext.getScenarioContext("SECONDVISITORNAME");
        for (int i = 0; i < visitors.size(); i++) {
            LinkedHashMap map = (LinkedHashMap) visitors.get(i);
            if (map.get("visitorName").equals(visitorName)) {
                id = String.valueOf(map.get("visitorId"));
            }
        }
        return id;
    }

    public String getMeetingId() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(new Date());
        String jsonString = "{\"filters\":{\"startDate\":\"" + strDate + " 00:00:00 +05:30\"},\"pagination\":{\"per_page\":100,\"perPage\":100,\"page\":1,\"current_page\":1,\"currentPage\":1}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/visitors/expected");
        ApiHelper.genericResponseValidation(response, "API - GET ALL VISITORS ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println(response.asString());
        ArrayList<LinkedHashMap> visitors = jsonPathEvaluator.get("message.visitorsHistory");
        String id = "";
        String visitorName = (String) variableContext.getScenarioContext("VISITORNAME");
        for (int i = 0; i < visitors.size(); i++) {
            LinkedHashMap map = (LinkedHashMap) visitors.get(i);
            if (map.get("visitorName").equals(visitorName)) {
                id = String.valueOf(map.get("meetingId"));
            }
        }
        return id;
    }

    public String getSecondMeetingId() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(new Date());
        String jsonString = "{\"filters\":{\"startDate\":\"" + strDate + " 00:00:00 +05:30\"},\"pagination\":{\"per_page\":100,\"perPage\":100,\"page\":1,\"current_page\":1,\"currentPage\":1}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/visitors/expected");
        ApiHelper.genericResponseValidation(response, "API - GET ALL VISITORS ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println(response.asString());
        ArrayList<LinkedHashMap> visitors = jsonPathEvaluator.get("message.visitorsHistory");
        String id = "";
        String visitorName = (String) variableContext.getScenarioContext("SECONDVISITORNAME");
        for (int i = 0; i < visitors.size(); i++) {
            LinkedHashMap map = (LinkedHashMap) visitors.get(i);
            if (map.get("visitorName").equals(visitorName)) {
                id = String.valueOf(map.get("meetingId"));
            }
        }
        return id;
    }

    public void cancelSecondVisitorSchedule() throws ParseException {
        String token = "";

        String loggedIN = (String) variableContext.getScenarioContext("FD");
        if(loggedIN.equalsIgnoreCase("TRUE")){

            token = (String) variableContext.getScenarioContext("FDTOKEN");
        }else {
            token = apiUtility.getTokenFromLocalStorage();
        }

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        String id = (String) variableContext.getScenarioContext("SECONDMEETINGID");
        String userId = userManagementService.getUserID((String) variableContext.getScenarioContext("SECONDVISITORPHONE"));
        String visitDate = (String) variableContext.getScenarioContext("SECONDVISITDATE");
        String comment = "Reason is  "+RandomDataGenerator.randomAlphabetic(5);
        String jsonString = "{\n" +
                "  \"status\": \"rejected\",\n" +
                "  \"comment\": \"meetings cancelled\",\n" +
                "  \"visitDate\": \""+visitDate+"\",\n" +
                "  \"meetingPurpose\": \"Delivery\",\n" +
                "  \"hostAdditionalInfo\": \"string\"\n" +
                "}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/users/" + userId + "/visitors/" + id + "/updateRequest");

        ApiHelper.genericResponseValidation(response, "API - DELETE SECOND VISITOR ");
        ResultManager.pass("I cancel visitor schedule", "I canceled second visitor's schedule "+userId  , false);
    }

    public void cancelVisitorSchedule() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        String id = (String) variableContext.getScenarioContext("MEETINGID");
        String userId = userManagementService.getUserID((String) variableContext.getScenarioContext("VISITORPHONE"));
        String visitDate = (String) variableContext.getScenarioContext("VISITDATE");
        String comment = "Reason is  "+RandomDataGenerator.randomAlphabetic(5);
        String jsonString = "{\n" +
                "  \"status\": \"cancelled\",\n" +
                "  \"comment\": \"meetings cancelled\"\n" +
                "}";

        String path = PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/users/" + userId + "/visitors/" + id + "/updateRequest";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userId + "/visitors/" + id + "/updateRequest");
        System.out.println(response.asString());
        ApiHelper.genericResponseValidation(response, "API - CANCEL VISITOR ");
        ResultManager.pass("I cancel visitor schedule", "I canceled visitor schedule "+userId  , false);

        variableContext.setScenarioContext("DELETEVISITOR","FALSE");
    }

    public void rejectVisitorSchedule() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String orgId = (String) variableContext.getScenarioContext("ORGID");
        String id = (String) variableContext.getScenarioContext("MEETINGID");
        String userId = userManagementService.getUserID((String) variableContext.getScenarioContext("VISITORPHONE"));
        String visitDate = (String) variableContext.getScenarioContext("VISITDATE");
        String comment = "Reason is  "+RandomDataGenerator.randomAlphabetic(5);
        String jsonString = "{\n" +
                "  \"status\": \"rejected\",\n" +
                "  \"comment\": \"meetings cancelled\",\n" +
                "  \"visitDate\": \""+visitDate+"\",\n" +
                "  \"meetingPurpose\": \"Delivery\",\n" +
                "  \"hostAdditionalInfo\": \"string\"\n" +
                "}";

        String path = PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + orgId + "/users/" + userId + "/visitors/" + id + "/updateRequest";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userId + "/visitors/" + id + "/updateRequest");
        System.out.println(response.asString());
        ApiHelper.genericResponseValidation(response, "API - REJECT VISITOR ");
        ResultManager.pass("I reject visitor schedule", "I rejected visitor schedule "+userId  , false);

        variableContext.setScenarioContext("DELETEVISITOR","FALSE");
    }

    public void approveVisitorSchedule() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String id = (String) variableContext.getScenarioContext("MEETINGID");
        String userId = userManagementService.getUserID((String) variableContext.getScenarioContext("VISITORPHONE"));
        String visitDate = (String) variableContext.getScenarioContext("VISITDATE");
        String comment = "Reason is  "+RandomDataGenerator.randomAlphabetic(5);

        String jsonString =  "";

        String fd = (String) variableContext.getScenarioContext("LOGGEDIN");

        if(fd.equalsIgnoreCase("FD")){

            jsonString = "{\n" +
                    "  \"status\": \"approved\",\n" +
                    "  \"comment\": \"meetings cancelled\",\n" +
                    "  \"hostAdditionalInfo\": \"something\"\n" +
                    "}";

        }else{

            jsonString = "{\n" +
                    "  \"status\": \"approved\",\n" +
                    "  \"comment\": \"meetings cancelled\",\n" +
                    "  \"visitDate\": \""+visitDate+"\",\n" +
                    "  \"meetingPurpose\": \"Delivery\",\n" +
                    "  \"hostAdditionalInfo\": \"something\"\n" +
                    "}";
        }

        //String path = PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userId + "/visitors/" + id + "/updateRequest";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userId + "/visitors/" + id + "/updateRequest");

        //System.out.println(response.asString());
        ApiHelper.genericResponseValidation(response, "API - APPROVE VISITOR ");
        ResultManager.pass("I approve visitor schedule", "I approved visitor schedule "+userId  , false);

        variableContext.setScenarioContext("DELETEVISITOR","FALSE");
    }

    public void restoreVMSSettings() throws ParseException {
        //https://test.api.spintly.com/v2/organisationManagement/organisations/703/visitors/settings
        String token = apiUtility.getTokenFromLocalStorage();

        String jsonString = "{\"allowAllUsers\":false,\"selectedUsers\":[277342362,277325154],\"emailEnabled\":true,\"smsEnabled\":false,\"covidEnabled\":false,\"photoIdEnabled\":true,\"defaultTAndCEnabled\":false,\"meetingPurposes\":[{\"id\":105,\"meetingPurpose\":\"Maintenance\",\"checked\":true},{\"id\":287,\"meetingPurpose\":\"Delivery\",\"checked\":true}],\"visitorPhotoEnabled\":true,\"visitorNameEnabled\":true,\"visitorPhoneEnabled\":true,\"visitorEmailEnabled\":true,\"visitorEmailRequired\":true,\"purposeOfMeetingEnabled\":true,\"meetingDurationEnabled\":true,\"tAndCFormEnabled\":true,\"autoApproveVisit\":false,\"personToMeetEnabled\":true,\"additionalInfoEnabled\":false,\"hostAdditionalInfo\":true,\"visitorAdditionalInfo\":false,\"printVisitorDetailsEnabled\":true}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/visitors/settings");

        ApiHelper.genericResponseValidation(response, "API - RESTORE VMS SETTINGS ");
    }


    public void editVMSSettings(DataTable data) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

        String photoIdEnabled = dataMap.get("photoIdEnabled").trim();
        String meetingDurationEnabled = dataMap.get("meetingDurationEnabled").trim();
        String visitorPhotoEnabled = dataMap.get("visitorPhotoEnabled").trim();
        String additionalInfoEnabled = dataMap.get("additionalInfoEnabled").trim();
        String meetingPurpose = dataMap.get("meetingPurpose").trim();
        String purposeOfMeetingEnabled = dataMap.get("purposeOfMeetingEnabled").trim();
        String visitorEmailRequired = dataMap.get("visitorEmailRequired").trim();
        String autoApproveVisit = dataMap.get("autoApproveVisit").trim();
        String emailEnabled = dataMap.get("emailEnabled").trim();
        String smsEnabled = dataMap.get("smsEnabled").trim();
        String tAndCFormEnabled = dataMap.get("tAndCFormEnabled").trim();
        String defaultTAndCEnabled = dataMap.get("defaultTAndCEnabled").trim();
        String covidEnabled = dataMap.get("covidEnabled").trim();
        String allowAllUsers = dataMap.get("allowAllUsers").trim();
        String visitorAdditionalInfo = dataMap.get("visitorAdditionalInfo").trim();
        String printVisitorDetailsEnabled = dataMap.get("printVisitorDetailsEnabled").trim();

        String jsonString = "{\"allowAllUsers\":"+allowAllUsers+",\"selectedUsers\":[277342362,277325154],\"emailEnabled\":"+emailEnabled+",\"smsEnabled\":"+smsEnabled+",\"covidEnabled\":"+covidEnabled+",\"photoIdEnabled\":"+photoIdEnabled+",\"defaultTAndCEnabled\":"+defaultTAndCEnabled+",\"meetingPurposes\":[{\"id\":105,\"meetingPurpose\":\"Maintenance\",\"checked\":"+meetingPurpose+"},{\"id\":287,\"meetingPurpose\":\"Delivery\",\"checked\":true}],\"visitorPhotoEnabled\":"+visitorPhotoEnabled+",\"visitorNameEnabled\":true,\"visitorPhoneEnabled\":true,\"visitorEmailEnabled\":true,\"visitorEmailRequired\":"+visitorEmailRequired+",\"purposeOfMeetingEnabled\":"+purposeOfMeetingEnabled+",\"meetingDurationEnabled\":"+meetingDurationEnabled+",\"tAndCFormEnabled\":"+tAndCFormEnabled+",\"autoApproveVisit\":"+autoApproveVisit+",\"personToMeetEnabled\":true,\"additionalInfoEnabled\":"+additionalInfoEnabled+",\"hostAdditionalInfo\":true,\"visitorAdditionalInfo\":"+visitorAdditionalInfo+",\"printVisitorDetailsEnabled\":"+printVisitorDetailsEnabled+"}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/visitors/settings");

        ApiHelper.genericResponseValidation(response, "API - EDIT VMS SETTINGS ");

        variableContext.setScenarioContext("RESTOREVMS","TRUE");
    }

    public String getPurposeID() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        int id = 0;
        String purpose = (String) variableContext.getScenarioContext("PURPOSENAME");
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/visitors/settings");

        ApiHelper.genericResponseValidation(response, "API - GET VMS PURPOSE ID");

        JsonPath jsonPathEvaluator = response.jsonPath();
        //meetingPurposes[0].meetingPurpose
        List<String> purposes = jsonPathEvaluator.get("message.meetingPurposes.meetingPurpose");

        for(int i=0;i<purposes.size();i++){
            String temp = jsonPathEvaluator.get("message.meetingPurposes["+i+"].meetingPurpose");

            if(temp.equalsIgnoreCase(purpose)){
               id = jsonPathEvaluator.get("message.meetingPurposes["+i+"].id");
               break;
            }
        }

        return String.valueOf(id);
    }

    //Delete Meeting purpose
    public void deletePurpose() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String purpose = (String) variableContext.getScenarioContext("PURPOSENAME");
        String jsonString = "{\"meetingPurpose\":\""+purpose+"\",\"destroy\":true}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/meetingPurpose/"+getPurposeID()+"/update");

        ApiHelper.genericResponseValidation(response, "API - DELETE VMS PURPOSE");
    }

    public void addMeetingPurpose(String checked) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String name = "Purpose "+ RandomDataGenerator.getData("{RANDOM_STRING}",4);

        if(checked.equalsIgnoreCase("checked")){
            checked = "true";
        }else {
            checked = "false";
        }

        String jsonString = "{\"meetingPurpose\":\""+name+"\",\"checked\":"+checked+"}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/addMeetingPurpose");

        ApiHelper.genericResponseValidation(response, "API - ADD VMS PURPOSE");

        variableContext.setScenarioContext("PURPOSENAME",name);
        variableContext.setScenarioContext("DELETEPURPOSE","TRUE");
    }


    //Restore Kiosk Settings
    public void restoreKioskSettings() throws ParseException {
        //https://test.api.spintly.com/v2/organisationManagement/organisations/703/accessAssignment
        String token = apiUtility.getTokenFromLocalStorage();

        String jsonString = "{\"accessAssignmentEnabled\":true,\"accessModes\":{\"qr\":true,\"card\":true},\"autoAccessExpiryEnabled\":true,\"accessPoints\":[{\"id\":\"1263\",\"name\":\"test_door2\",\"checked\":false,\"qr\":null},{\"id\":\"1267\",\"name\":\"Test FP1\",\"checked\":false,\"qr\":null}]}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/accessAssignment");

        ApiHelper.genericResponseValidation(response, "API - RESTORE VMS KIOSK SETTINGS");
    }

    public void editKioskSettings(DataTable data) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

        String accessAssignmentEnabled = dataMap.get("accessAssignmentEnabled").trim();
        String qr = dataMap.get("qr").trim();
        String card = dataMap.get("card").trim();
        String autoAccessExpiryEnabled = dataMap.get("autoAccessExpiryEnabled").trim();
        String checked  = dataMap.get("checked").trim();

        String second ="false";

        if(checked.equalsIgnoreCase("both")){
            checked = "true";
            second  = "true";
        }

        String jsonString = "{\"accessAssignmentEnabled\":"+accessAssignmentEnabled+",\"accessModes\":{\"qr\":"+qr+",\"card\":"+card+"},\"autoAccessExpiryEnabled\":"+autoAccessExpiryEnabled+",\"accessPoints\":[{\"id\":\"1263\",\"name\":\"test_door2\",\"checked\":"+checked+",\"qr\":null},{\"id\":\"1267\",\"name\":\"Test FP1\",\"checked\":"+second+",\"qr\":null}]}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/accessAssignment");

        ApiHelper.genericResponseValidation(response, "API - EDIT VMS KIOSK SETTINGS");

        variableContext.setScenarioContext("RESTOREKIOSK","TRUE");
    }


    //Create a kiosk
    public void createKiosk() throws ParseException{
        String token = apiUtility.getTokenFromLocalStorage();

        String name = "Kiosk "+ RandomDataGenerator.getData("{RANDOM_STRING}",3);
        String jsonString = "{\"kioskName\":\""+name+"\"}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/kiosk");

        ApiHelper.genericResponseValidation(response, "API - ADD KIOSK VMS");
        variableContext.setScenarioContext("NEWKIOSKNAME",name);
        variableContext.setScenarioContext("KIOSKID",getKioskID(name));
        variableContext.setScenarioContext("DELETEKIOSK","TRUE");
    }

    //Create a kiosk
    public void createSecondKiosk() throws ParseException{
        String token = apiUtility.getTokenFromLocalStorage();

        String name = "Kiosk "+ RandomDataGenerator.getData("{RANDOM_STRING}",3);
        String jsonString = "{\"kioskName\":\""+name+"\"}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/" + apiUtility.getOrgnizationID() + "/kiosk");

        ApiHelper.genericResponseValidation(response, "API - ADD KIOSK VMS");
        variableContext.setScenarioContext("SECONDKIOSKNAME",name);
        variableContext.setScenarioContext("DELETESECONDKIOSK","TRUE");
    }

    //Delete Kiosk
    public void deleteKiosk() throws  ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String name = (String) variableContext.getScenarioContext("NEWKIOSKNAME");

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/"+apiUtility.getOrgnizationID()+"/kiosks/"+getKioskID(name));

        ApiHelper.genericResponseValidation(response, "API - DELETE KIOSK VMS");

    }

    //Delete Kiosk
    public void deleteSecondKiosk() throws  ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String name = (String) variableContext.getScenarioContext("SECONDKIOSKNAME");

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/"+apiUtility.getOrgnizationID()+"/kiosks/"+getKioskID(name));

        ApiHelper.genericResponseValidation(response, "API - DELETE SECOND KIOSK VMS");

    }

    public String getKioskID(String kioskName) throws  ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String name = (String) variableContext.getScenarioContext("NEWKIOSKNAME");

        int id = 0;

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/"+apiUtility.getOrgnizationID()+"/kiosks");

        ApiHelper.genericResponseValidation(response, "API - GET VMS KIOSK ID");

        JsonPath jsonPathEvaluator = response.jsonPath();
        //meetingPurposes[0].meetingPurpose
        List<String> purposes = jsonPathEvaluator.get("message.kiosks.kioskName");

        for(int i=0;i<purposes.size();i++){
            String temp = jsonPathEvaluator.get("message.kiosks["+i+"].kioskName");

            if(temp.equalsIgnoreCase(kioskName)){
                id = jsonPathEvaluator.get("message.kiosks["+i+"].id");
                break;
            }
        }

        return String.valueOf(id);
    }

    public void visitorCheckin() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String visitorPhone = (String) variableContext.getScenarioContext("VISITORPHONE");
        String visitorName = (String) variableContext.getScenarioContext("VISITORNAME");
        String visitorID = (String) variableContext.getScenarioContext("VISITORID");
        String meetingidID = (String) variableContext.getScenarioContext("MEETINGID");
        String visitdate  = (String) variableContext.getScenarioContext("VISITDATE");

        String kioskID  = (String) variableContext.getScenarioContext("KIOSKID");
        String userID = (String) variableContext.getScenarioContext("USERID");
        String visitorEmail = (String) variableContext.getScenarioContext("VISITOREMAIL");


        String jsonString = "{\"visitorName\": \""+visitorName+"\",\"phone\": \""+visitorPhone+"\",\"email\": \""+visitorEmail+"\",\"meetingPurpose\": \"Delivery\",\"visitDate\": \""+visitdate+"\",\"userId\": "+userID+",\"visitorPhoto\": \"empty\",\"photoId\":\"empty\",\"signature\":\"empty\",\"meetingDuration\": 240,\"covidData\":{\"fever\": false,\"cough\": false,\"breathlesness\": false,\"soreThroat\":false,\"cameInContact\":false},\"tAndCFormChecked\": true,\"kioskId\": "+kioskID+",\"hostAdditionalInfo\": \"something\"}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/"+apiUtility.getOrgnizationID()+"/visitors/"+visitorID+"/meeting/"+meetingidID+"/add/mobileApp");


        ApiHelper.genericResponseValidation(response, "API - VMS VISITOR CHECKIN");

        variableContext.setScenarioContext("DELETEVISITOR","FALSE");
    }

    public void webCheckin() throws ParseException {
        String visitorPhone = (String) variableContext.getScenarioContext("VISITORPHONE");
        String visitorName = (String) variableContext.getScenarioContext("VISITORNAME");
        String visitorID = (String) variableContext.getScenarioContext("VISITORID");
        String meetingidID = (String) variableContext.getScenarioContext("MEETINGID");
        String visitdate  = (String) variableContext.getScenarioContext("VISITDATE");

        String kioskID  = (String) variableContext.getScenarioContext("KIOSKID");
        String userID = (String) variableContext.getScenarioContext("USERID");
        String visitorEmail = (String) variableContext.getScenarioContext("VISITOREMAIL");

        String jsonString = "{\"visitorName\": \""+visitorName+"\",\"phone\": \""+visitorPhone+"\",\"email\": \""+visitorEmail+"\",\"meetingPurpose\": \"Delivery\",\"visitDate\": \""+visitdate+"\",\"userId\": "+userID+",\"visitorPhoto\": \"empty\",\"photoId\":\"empty\",\"meetingDuration\": 240,\"covidData\":{\"fever\": false,\"cough\": false,\"breathlesness\": false,\"soreThroat\":false,\"cameInContact\":false},\"tAndCFormChecked\": true,\"visitorAdditionalInfo\": \"something\"}";

        Response response = ApiHelper.givenRequestSpecification()
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/"+apiUtility.getOrgnizationID()+"/visitors/"+visitorID+"/meeting/"+meetingidID+"/add");

        ApiHelper.genericResponseValidation(response, "API - VMS VISITOR WEB CHECKIN");

        variableContext.setScenarioContext("DELETEVISITOR","FALSE");
    }

    public List<String> kiosksInOrganisation() throws ParseException{
        //v2/visitorManagement/organisations/703/kiosks
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> kiosks = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/"+apiUtility.getOrgnizationID()+"/kiosks");

        ApiHelper.genericResponseValidation(response, "API - LIST OF KIOSKS IN ORGANISATION");

        JsonPath jsonPathEvaluator = response.jsonPath();

        kiosks = jsonPathEvaluator.get("message.kiosks.kioskName");

        return kiosks.stream().sorted().collect(Collectors.toList());
    }

    public void visitorCheckout() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        String visitorID = (String) variableContext.getScenarioContext("VISITORID");
        String meetingidID = (String) variableContext.getScenarioContext("MEETINGID");
        String userID = (String) variableContext.getScenarioContext("USERID");

        String dateTime = "";

        LocalDateTime now = LocalDateTime.now().plusMinutes(5);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTime = dtf1.format(now) + " +05:30";

        String jsonString = "{\n" +
                "  \"checkoutTime\": \""+dateTime+"\"\n" +
                "}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/visitorManagement/organisations/"+apiUtility.getOrgnizationID()+"/visitors/"+visitorID+"/meeting/"+meetingidID+"/checkOut/mobileApp");


        ApiHelper.genericResponseValidation(response, "API - VMS VISITOR CHECKOUT");
    }


}
