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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.stream.Stream;


public class LeaveHolidayManagementService extends DriverContext {
    private static LogUtility logger = new LogUtility(ShiftManagementService.class);
    ApiUtility apiUtility = new ApiUtility();
    RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    UserManagementService userManagementService = new UserManagementService();

    public void assignLeavePolicy(String userID, String approver, Boolean... isPaid) throws ParseException {
        Boolean isPaidValue = true;
        if (isPaid.length == 0)
            isPaidValue = true;
        else
            isPaidValue = isPaid[0];
        String shortName = RandomDataGenerator.randomAlphabetic(3);
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"name\":\"AL" + userID + shortName + "\",\"shortName\":\"" + shortName + "\",\"paid\":" + isPaidValue + ",\"accrual\":\"cycle\",\"allowCarryForward\":true,\"allowEncashment\":true,\"precedence\":\"COE\",\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true,\"backDatedAllowedDays\":7,\"maxCF\":20,\"applyDaysBefore\":0,\"minAllowed\":0.5,\"maxAllowed\":1,\"maxLeavesInMonth\":4,\"updatePolicies\":\"none\"}\n";
        variableContext.setScenarioContext("LEAVENAME", "AL" + userID + shortName);
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/");

        ApiHelper.genericResponseValidation(response, "API - Add Leave - Monthly Type " + "AL" + userID + shortName);

        JsonPath jsonPathEvaluator = response.jsonPath();
        String leaveId = jsonPathEvaluator.get("data.leaveId").toString();
        variableContext.setScenarioContext("LEAVEID", leaveId);
        variableContext.setScenarioContext("LEAVESHORTNAME", shortName);


        jsonString = "{\"name\":\"LPN" + userID + shortName + "\",\"leaveTypes\":[{\"cycleLimit\":100,\"accrualLimit\":100,\"carryForwardLimit\":100,\"encashmentLimit\":100,\"allowCarryForward\":true,\"allowEncashment\":true,\"accrual\":\"cycle\",\"leaveId\":" + leaveId + ",\"allowedOnProbation\":true,\"applyDaysBefore\":0,\"backDatedAllowedDays\":7,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"maxAllowed\":1,\"maxCF\":20,\"minAllowed\":0.5,\"name\":\"" + "AL" + userID + shortName + "\",\"paid\":true,\"precedence\":\"COE\",\"probationProrate\":true,\"shortName\":\"" + shortName + "\",\"weekOffBetLeaves\":\"leave\"}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOHalfDayHours\":0,\"cOFullDayHours\":0},\"compOff\":{\"allowCF\":true,\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true},\"leaveApplicationSettings\":{\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}],\"approvalRule\":\"" + approver + "\"},\"assignedUsers\":[" + userID + "]}\n" +
                "\n";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycle() + "/leavePolicies");
        ApiHelper.genericResponseValidation(response, "API - Assign Leave Policy for User ID " + userID);
        ResultManager.pass("I assign Leave Policy for User", "I assigned Leave Policy for User ID " + userID, false);

        variableContext.setScenarioContext("LEAVEPOLICYNAME","LPN" + userID);
        variableContext.setScenarioContext("DELETELEAVEPOLICY","TRUE");

    }

    public void assignHolidayPolicy(String userID) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveCycle = getLeaveCycle();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());
        String jsonString = "{\"holidayPolicy\":{\"name\":\"New holiday policy" + userID + "\",\"cycleId\":" + leaveCycle + ",\"country\":\"IN\",\"holidays\":[{\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true,\"country\":\"IN\"},{\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false,\"country\":\"IN\"}],\"assignedUserIds\":[" + userID + "],\"discretionaryLimit\":1}}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/holidayPolicy");
        ApiHelper.genericResponseValidation(response, "API - Assign Holiday Policy for User ID " + userID);
        ResultManager.pass("I assign Holiday Policy for User", "I assigned Holiday Policy for User ID " + userID, false);

        variableContext.setScenarioContext("DELETENEWHOLIDAYPOLICY", "TRUE");
        variableContext.setScenarioContext("NEWHOLIDAYPOLICYNAME","New holiday policy" + userID);
    }

    public void assignHolidayPolicyToMultipleUsers() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveCycle = getLeaveCycle();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());

        String user1 = (String) variableContext.getScenarioContext("NEWUSERID0");
        String user2 = (String) variableContext.getScenarioContext("NEWUSERID1");

        String userId = user1+","+user2;
        String name = "New holiday policy" + RandomDataGenerator.getData("{RANDOM_STRING}");
        String jsonString = "{\"holidayPolicy\":{\"name\":\""+name+"\",\"cycleId\":" + leaveCycle + ",\"country\":\"IN\",\"holidays\":[{\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true,\"country\":\"IN\"},{\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false,\"country\":\"IN\"}],\"assignedUserIds\":[" + userId + "],\"discretionaryLimit\":1}}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/holidayPolicy");
        ApiHelper.genericResponseValidation(response, "API - Assign Holiday Policy for Multiple Users ");
        ResultManager.pass("I assign Holiday Policy for User", "I assigned Holiday Policy for Multiple Users ", false);

        variableContext.setScenarioContext("DELETENEWHOLIDAYPOLICY", "TRUE");
        variableContext.setScenarioContext("NEWHOLIDAYPOLICYNAME",name);
    }

    public void assignHolidayPolicyWithCHYestdayAndDHToday(String userID) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveCycle = getLeaveCycle();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Date oneDayBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        String ld = dateFormat.format(oneDayBefore);
        String ld2 = dateFormat.format(date);
        String shortName = RandomDataGenerator.randomAlphabetic(3);

        variableContext.setScenarioContext("CH", date.getDate() - 1);
        variableContext.setScenarioContext("DH", date.getDate());
        String jsonString = "{\"holidayPolicy\":{\"name\":\"NHP" + userID + shortName + "\",\"cycleId\":" + leaveCycle + ",\"country\":\"IN\",\"holidays\":[{\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true,\"country\":\"IN\"},{\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false,\"country\":\"IN\"}],\"assignedUserIds\":[" + userID + "],\"discretionaryLimit\":1}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/holidayPolicy");
        ApiHelper.genericResponseValidation(response, "API - Assign Holiday Policy for User ID " + userID);
        variableContext.setScenarioContext("NEWUSERID", userID);
        ResultManager.pass("I assign Holiday Policy for User", "I assigned Holiday Policy for User ID " + userID , false);

    }
    public void assignHolidayPolicyWithCHForToday(String userID) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveCycle = getLeaveCycle();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String ld2 = dateFormat.format(date);
        String shortName = RandomDataGenerator.randomAlphabetic(3);

        variableContext.setScenarioContext("CH", date.getDate());
        String jsonString = "{\"holidayPolicy\":{\"name\":\"NHP" + userID + shortName + "\",\"cycleId\":" + leaveCycle + ",\"country\":\"IN\",\"holidays\":[{\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":false,\"country\":\"IN\"}],\"assignedUserIds\":[" + userID + "],\"discretionaryLimit\":1}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/holidayPolicy");
        ApiHelper.genericResponseValidation(response, "API - Assign Holiday Policy for User ID " + userID);
        variableContext.setScenarioContext("NEWUSERID", userID);
        ResultManager.pass("I assign Holiday Policy for User", "I assigned Holiday Policy for User ID " + userID, false);

    }

    public void applydiscretionaryHoliday(String userID) throws ParseException {
        String leaveCycle = getLeaveCycle();

        String token = apiUtility.getidTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body("")
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userHoliday/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID + "/leaveCycles/" + leaveCycle + "/holidays");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String holidayID = jsonPathEvaluator.get("data.userHolidayData.policy.holidays[0].policyHolidayId").toString();

        ApiHelper.genericResponseValidation(response, "API - Get Holidays for User ID " + userID);

        String jsonString = "{\"holidayId\":" + holidayID + "}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userHoliday/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID + "/leaveCycles/" + leaveCycle + "/holidays/" + holidayID);
        ApiHelper.genericResponseValidation(response, "API - Assign Discretionary holiday for User ID " + userID);
        ResultManager.pass("I apply Discretionary Holiday for User", "I apply Discretionary Holiday for User ID " + userID, false);

    }

    public void applydiscretionaryHolidayasAUser(String userID) throws ParseException {
        String leaveCycle = getLeaveCycle();

        String token = apiUtility.getidTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body("")
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userHoliday/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID + "/leaveCycles/" + leaveCycle + "/holidays");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String holidayID = jsonPathEvaluator.get("data.userHolidayData.policy.holidays[0].policyHolidayId").toString();
        String date = jsonPathEvaluator.get("data.userHolidayData.policy.holidays[0].date").toString();
        SimpleDateFormat format2 = new SimpleDateFormat("YYYY-MM-dd");
        SimpleDateFormat format1 = new SimpleDateFormat("YYYY-MM-dd'T'HH:MM:ssXXX");
        Date dateToPass = format1.parse(date);

        ApiHelper.genericResponseValidation(response, "API - Get Holidays for User ID " + userID);

        String jsonString = "{\"holidayId\":" + holidayID + ",\"date\":\"" + format2.format(dateToPass) + "\"}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID + "/leaves/discretionaryHolidays");
        ApiHelper.genericResponseValidation(response, "API - Assign Discretionary holiday for User ID " + userID);
        ResultManager.pass("I apply Discretionary Holiday for User", "I apply Discretionary Holiday for User ID " + userID, false);

    }

    public String getLeaveCycle() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"filter\":{},\"fields\":[\"id\",\"name\",\"startDate\",\"endDate\"]}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles");
        ApiHelper.genericResponseValidation(response, "API - Get Current Leave Cycle ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String leaveCycles = jsonPathEvaluator.getJsonObject("data.currentCycleId").toString();
        return leaveCycles;
    }

    public void deleteLeave() throws ParseException {

        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"updatePolicies\":\"none\"}";
        String id = (String) variableContext.getScenarioContext("LEAVEID");
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/" + id + "/delete");

        ApiHelper.genericResponseValidation(response, "API - Delete Leave type " + id);

/*
        //Delete leavePolicies
        String policyid = (String) variableContext.getScenarioContext("LEAVEID");
         response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/"+getLeaveCycle()+"/leavePolicies/"+policyid);
        ApiHelper.genericResponseValidation(response, "API - Delete Leave polivy " + id);
*/
    }

   public void cleanupLeave() throws ParseException {

        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"updatePolicies\":\"currentAndFuture\"}";
        String id = (String) variableContext.getScenarioContext("LEAVEID");
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/" + id + "/delete");
    }


    //Get all the leave cycles in the organisation
    public List<String> getAllLeaveCycles() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"filter\":{},\"fields\":[\"id\",\"name\",\"startDate\",\"endDate\"]}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles");
        ApiHelper.genericResponseValidation(response, "API - Get All Leave Cycles In Organisation ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> leaveCycles = jsonPathEvaluator.get("data.leaveCycles.name");
        return leaveCycles.stream().sorted().collect(Collectors.toList());
    }

    //Create Leave Cycle
    public void createLeaveCycle(DataTable Data) throws ParseException {
        Map<String, String> dataMap = Data.transpose().asMap(String.class, String.class);
        String token = apiUtility.getidTokenFromLocalStorage();

        String name = dataMap.get("name") + randomDataGenerator.getData("{RANDOM_STRING}");

        String type = dataMap.get("type").trim();
        String startDate = dataMap.get("startDate").trim();
        String endDate = dataMap.get("endDate").trim();
        String eocReminderDate = dataMap.get("eocReminderDate").trim();
        String year = dataMap.get("year").trim();

        String jsonString = "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\",\"eocReminderDate\":\"" + eocReminderDate + "\",\"year\":" + year + "}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/");
        ApiHelper.genericResponseValidation(response, "API - CREATE LEAVE CYCLE ");

        variableContext.setScenarioContext("NEWLEAVECYCLENAME", name);
        variableContext.setScenarioContext("DELETENEWLEAVECYCLE", "TRUE");
    }

    //Create Leave Cycle
    public void createSecondLeaveCycle(DataTable Data) throws ParseException {
        Map<String, String> dataMap = Data.transpose().asMap(String.class, String.class);
        String token = apiUtility.getidTokenFromLocalStorage();

        String name = dataMap.get("name") + randomDataGenerator.getData("{RANDOM_STRING}");

        String type = dataMap.get("type").trim();
        String startDate = dataMap.get("startDate").trim();
        String endDate = dataMap.get("endDate").trim();
        String eocReminderDate = dataMap.get("eocReminderDate").trim();
        String year = dataMap.get("year").trim();

        String jsonString = "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\",\"eocReminderDate\":\"" + eocReminderDate + "\",\"year\":" + year + "}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/");
        ApiHelper.genericResponseValidation(response, "API - CREATE SECOND LEAVE CYCLE ");

        variableContext.setScenarioContext("SECONDLEAVECYCLENAME", name);
        variableContext.setScenarioContext("DELETESECONDLEAVECYCLE", "TRUE");
    }

    //Get Leave Cycle ID
    public int getLeaveCycleID(String cyclename) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"filter\":{},\"fields\":[\"id\",\"name\",\"startDate\",\"endDate\",\"leavePolicies\"]}";
        int id = 0;

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles");

        ApiHelper.genericResponseValidation(response, "API - GET CYCLE ID ");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<Object> leaveCycles = jsonPathEvaluator.get("data.leaveCycles");

        for (int i = 0; i < leaveCycles.size(); i++) {
            String name = jsonPathEvaluator.get("data.leaveCycles[" + i + "].name");
            if (name.equalsIgnoreCase(cyclename)) {
                id = jsonPathEvaluator.get("data.leaveCycles[" + i + "].id");
                break;
            }
        }
        return id;
    }

    //Delete leave cycle
    public void deleteLeaveCycle(String leaveCycleName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID(leaveCycleName));
        ApiHelper.genericResponseValidation(response, "API - DELETE LEAVE CYCLE ");
    }

    //Edit Leave Policy
    public void editLeavePolicy(DataTable Data) throws ParseException {
        Map<String, String> dataMap = Data.transpose().asMap(String.class, String.class);
        String token = apiUtility.getidTokenFromLocalStorage();

        String name = dataMap.get("name") + randomDataGenerator.getData("{RANDOM_STRING}");
        ;
        String type = dataMap.get("type");
        String startDate = dataMap.get("startDate");
        String endDate = dataMap.get("endDate");
        String eocReminderDate = dataMap.get("eocReminderDate");
        int year = Integer.parseInt(dataMap.get("year"));

        String jsonString = "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\",\"eocReminderDate\":\"" + eocReminderDate + "\",\"year\":" + year + "}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID((String) variableContext.getScenarioContext("NEWLEAVECYCLENAME")));
        ApiHelper.genericResponseValidation(response, "API - EDIT LEAVE CYCLE ");

        variableContext.setScenarioContext("EDITEDLEAVECYCLENAME", name);
        variableContext.setScenarioContext("DELETENEWLEAVECYCLE", "FALSE");
        variableContext.setScenarioContext("DELETEEDITEDLEAVECYCLE", "TRUE");
    }

    public void assignLeavePolicyToSpecificUser(String userName, String policyName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String phone = (String) variableContext.getScenarioContext("NEWUSERPHONE");
        int userID = Integer.parseInt(userManagementService.getUserID(phone));

        List<Integer> userIds = getUserIDAssignedToAPolicy(policyName);
        userIds.add(userID);

        String jsonString = "{\"name\":\"DoNotDelete\",\"leaveTypes\":[{\"id\":4443,\"name\":\"DoNotDelete1\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":508,\"clubbing\":true,\"shortName\":\"d1\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"id\":4442,\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"id\":4441,\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOFullDayHours\":0,\"cOHalfDayHours\":0},\"compOff\":{\"allowCF\":true,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true},\"leaveApplicationSettings\":{\"approvalRule\":\"RMOrAdmin\",\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}]},\"assignedUsers\":" + userIds + ",\"cycleId\":3926}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID(policyName, "DoNotDeleteLC"));
        ApiHelper.genericResponseValidation(response, "API - Assign Custom Leave Policy for User ID " + userID);

        variableContext.setScenarioContext("ASSIGNEDPOLICYPAYLOAD", jsonString);
    }

    //Get the user ID assigned to a particular leave policy
    public List<Integer> getUserIDAssignedToAPolicy(String policyName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID(policyName, "DoNotDeleteLC"));
        ApiHelper.genericResponseValidation(response, "API - GET USER ID ASSIGNED TO A POLICY ");

        JsonPath jsonPathEvaluator = response.jsonPath();

        List<Integer> userIDs = jsonPathEvaluator.get("data.leavePolicy.assignedUsers.id");

        return userIDs;
    }

    public int getLeavePolicyID(String policyName, String cycleName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"filter\":{},\"fields\":[\"id\",\"name\",\"assignedUsers\"]}";
        int id = 0;

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/");

        ApiHelper.genericResponseValidation(response, "API - GET POLICY ID ");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<Object> leavePolicies = jsonPathEvaluator.get("data.leavePolicies.name");


        for (int i = 0; i < leavePolicies.size(); i++) {
            String leavePolicyName = jsonPathEvaluator.get("data.leavePolicies[" + i + "].name");
            if (leavePolicyName.equalsIgnoreCase(policyName)) {
                id = jsonPathEvaluator.get("data.leavePolicies[" + i + "].id");
                break;
            }
        }
        return id;
    }

    public void deleteAddedUserFromPolicy(String policyName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String phone = (String) variableContext.getScenarioContext("NEWUSERPHONE");
        int userID = Integer.parseInt(userManagementService.getUserID(phone));

        List<Integer> userIds = getUserIDAssignedToAPolicy(policyName);

        for (int i = 0; i < userIds.size(); i++) {
            if (userIds.get(i).equals(userID)) {
                userIds.remove(i);
                break;
            }
        }

        String jsonString = "{\"name\":\"DoNotDelete\",\"leaveTypes\":[{\"id\":4443,\"name\":\"DoNotDelete1\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":508,\"clubbing\":true,\"shortName\":\"d1\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"id\":4442,\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"id\":4441,\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOFullDayHours\":0,\"cOHalfDayHours\":0},\"compOff\":{\"allowCF\":true,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true},\"leaveApplicationSettings\":{\"approvalRule\":\"RMOrAdmin\",\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}]},\"assignedUsers\":" + userIds + ",\"cycleId\":3926}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID(policyName, "DoNotDeleteLC"));
        ApiHelper.genericResponseValidation(response, "API - DELETE USER FROM LEAVE POLICY" + userID);
    }

    //Leaves in a particular leave policy
    public List<String> leavesInALeavePolicy(String cycleName, String policyName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID(policyName, cycleName));
        ApiHelper.genericResponseValidation(response, "API - Fetch leaves in a policy");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> leaveInTheCycle = jsonPathEvaluator.get("data.leavePolicy.leaveTypes.name");

        return leaveInTheCycle.stream().sorted().collect(Collectors.toList());
    }

    //Function to create a leave
    public void createLeave() throws ParseException {
        String shortName = RandomDataGenerator.randomAlphabetic(3);
        String leaveName = "newLeave" + RandomDataGenerator.randomAlphabetic(4);
        String userID = (String) variableContext.getScenarioContext("NEWUSERID");
        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"name\":\"" + leaveName + "\",\"shortName\":\"" + shortName + "\",\"paid\":true,\"accrual\":\"cycle\",\"allowCarryForward\":true,\"allowEncashment\":true,\"precedence\":\"COE\",\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true,\"backDatedAllowedDays\":7,\"maxCF\":20,\"applyDaysBefore\":0,\"minAllowed\":0.5,\"maxAllowed\":1,\"maxLeavesInMonth\":4,\"updatePolicies\":\"none\"}\n";
        variableContext.setScenarioContext("LEAVENAME", leaveName);
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/");

        ApiHelper.genericResponseValidation(response, "API - Add Leave - Cycle Type ");


        JsonPath jsonPathEvaluator = response.jsonPath();
        String leaveId = jsonPathEvaluator.get("data.leaveId").toString();
        variableContext.setScenarioContext("LEAVEID", leaveId);
        variableContext.setScenarioContext("LEAVESHORTNAME", shortName);
        variableContext.setScenarioContext("DELETENEWLEAVE", "TRUE");

        String addLeavePayload = "{\"name\":\"" + leaveName + "\",\"shortName\":\"" + shortName + "\",\"paid\":true,\"accrual\":\"monthlyStart\",\"allowCarryForward\":true,\"allowEncashment\":true,\"precedence\":\"COE\",\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true,\"backDatedAllowedDays\":7,\"maxCF\":20,\"applyDaysBefore\":0,\"minAllowed\":0.5,\"maxAllowed\":1,\"leaveId\":" + leaveId + ",\"id\":0,\"cycleLimit\": 10,\"accrualLimit\": 10,\"encashmentLimit\": 0,\"carryForwardLimit\": 0}";

        variableContext.setScenarioContext("ADDLEAVEPAYLOAD", addLeavePayload);
    }

    //function to add leave to leave Policy
    public void addLeaveToPolicy() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        //fetch the exisitng leaves in the policy
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID("DoNotDelete", "DoNotDeleteLC"));

        ApiHelper.genericResponseValidation(response, "API - FETCH ALL LEAVES IN THE POLICY");

        String existingLeaves = response.getBody().asString().split("\\[")[1].split("]")[0];

        String newLeavePayload = (String) variableContext.getScenarioContext("ADDLEAVEPAYLOAD");

        String leavesPayload = "";
        if (!(existingLeaves == "")) {
            leavesPayload = existingLeaves + "," + newLeavePayload;
        } else {
            leavesPayload = newLeavePayload;
        }

        String assignedPolicyPayload = (String) variableContext.getScenarioContext("ASSIGNEDPOLICYPAYLOAD");

        if (assignedPolicyPayload == "") {
            List<Integer> userIds = getUserIDAssignedToAPolicy("DoNotDelete");
            assignedPolicyPayload = "{\"name\":\"DoNotDelete\",\"leaveTypes\":[{\"id\":4443,\"name\":\"DoNotDelete1\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":508,\"clubbing\":true,\"shortName\":\"d1\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"id\":4442,\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"id\":4441,\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":null,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOFullDayHours\":0,\"cOHalfDayHours\":0},\"compOff\":{\"allowCF\":true,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true},\"leaveApplicationSettings\":{\"approvalRule\":\"RMOrAdmin\",\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}]},\"assignedUsers\":" + userIds + ",\"cycleId\":3926}";
        }

        String first = assignedPolicyPayload.split("\\[")[0];
        String second = assignedPolicyPayload.split("\"overtime\"")[1];
        String finalPayload = first + "[" + leavesPayload + "],\"overtime\"" + second;

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(finalPayload)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID("DoNotDelete", "DoNotDeleteLC"));
        ApiHelper.genericResponseValidation(response, "API - ADD LEAVE TO LEAVE POLICY");

    }

    //Function to delete a new leave
    public void deleteNewLeave() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveID = (String) variableContext.getScenarioContext("LEAVEID");

        String jsonString = "{\"updatePolicies\":\"none\"}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/" + leaveID + "/delete");

        ApiHelper.genericResponseValidation(response, "API - DELETE LEAVE");
    }

    //Delete leave from leave policy
    public void deleteLeaveFromPolicy() throws ParseException {
        String assignedPolicyPayload = (String) variableContext.getScenarioContext("ASSIGNEDPOLICYPAYLOAD");
        String token = apiUtility.getidTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(assignedPolicyPayload)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID("DoNotDelete", "DoNotDeleteLC"));
        ApiHelper.genericResponseValidation(response, "API - DELETED LEAVE FROM LEAVE POLICY");
    }

    //Create policy with custom approval rules
    public void assignLeavePolicyCustomApprovalRules(String userID, String approvalType) throws ParseException {

        String shortName = RandomDataGenerator.randomAlphabetic(3);
        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"name\":\"CL" + userID + "\",\"shortName\":\"" + shortName + "\",\"paid\":true,\"accrual\":\"monthlyStart\",\"allowCarryForward\":true,\"allowEncashment\":true,\"precedence\":\"COE\",\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true,\"backDatedAllowedDays\":7,\"maxCF\":20,\"applyDaysBefore\":0,\"minAllowed\":0.5,\"maxAllowed\":1,\"maxLeavesInMonth\":4,\"updatePolicies\":\"none\"}\n";
        variableContext.setScenarioContext("LEAVENAME", "CL" + userID);
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/");

        ApiHelper.genericResponseValidation(response, "API - ADD LEAVE FOR CUSTOM APPROVAL RULE");

        JsonPath jsonPathEvaluator = response.jsonPath();
        String leaveId = jsonPathEvaluator.get("data.leaveId").toString();
        variableContext.setScenarioContext("LEAVEID", leaveId);
        variableContext.setScenarioContext("LEAVESHORTNAME", shortName);

        String approval = "";

        switch (approvalType) {
            case "approved by only the reporting manager":
                approval = "RMOnly";
                break;
            case "approved by either the reporting manager or admin":
                approval = "RMOrAdmin";
                break;
            case "approved by the reporting manager and admin":
                approval = "RMAndAdmin";
                break;
        }

        jsonString = "{\"name\":\"custLeavePolicy" + userID + "\",\"leaveTypes\":[{\"cycleLimit\":20,\"accrualLimit\":20,\"carryForwardLimit\":20,\"encashmentLimit\":20,\"allowCarryForward\":true,\"allowEncashment\":true,\"accrual\":\"cycle\",\"leaveId\":" + leaveId + ",\"allowedOnProbation\":true,\"applyDaysBefore\":0,\"backDatedAllowedDays\":7,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"maxAllowed\":1,\"maxCF\":20,\"minAllowed\":0.5,\"name\":\"" + "CL" + userID + "\",\"paid\":true,\"precedence\":\"COE\",\"probationProrate\":true,\"shortName\":\"" + shortName + "\",\"weekOffBetLeaves\":\"leave\"}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOHalfDayHours\":0,\"cOFullDayHours\":0},\"compOff\":{\"allowCF\":true,\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true},\"leaveApplicationSettings\":{\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}],\"approvalRule\":\"" + approval + "\"},\"assignedUsers\":[" + userID + "]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies");
        ApiHelper.genericResponseValidation(response, "API - ASSIGNED CUSTOM APPROVAL LEAVE POLICY for User ID " + userID);

        variableContext.setScenarioContext("LEAVEPOLICYNAME", "custLeavePolicy" + userID);
        variableContext.setScenarioContext("DELETELEAVEPOLICY", "TRUE");

        Calendar calOne = Calendar.getInstance();
        int dayOfYear = calOne.get(Calendar.DAY_OF_YEAR);
        int year = calOne.get(Calendar.YEAR);
        Calendar calTwo = new GregorianCalendar(year, 11, 31);
        int day = calTwo.get(Calendar.DAY_OF_YEAR);
        System.out.println("Days in current year: " + day);
        int total_days = day - dayOfYear;
        System.out.println("Total " + total_days + " days remaining in current year");

        double ratio = total_days / 365.0;
        int balance = (int) Math.round(ratio * 20);

        variableContext.setScenarioContext("LEAVEBALANCE", String.valueOf(balance));
    }

    //Delete leave policy
    public void deleteLeavePolicy() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String leavePolicyName = (String) variableContext.getScenarioContext("LEAVEPOLICYNAME");
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies/" + getLeavePolicyID(leavePolicyName, "DoNotDeleteLC"));
        ApiHelper.genericResponseValidation(response, "API - DELETE LEAVE POLICY");
    }

    //Create leave policy with min leave application limit
    public void createPolicyWithMinLeaveApplicationLimit(String userID, String limit) throws ParseException {
        String shortName = RandomDataGenerator.randomAlphabetic(3);
        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"name\":\"CL" + userID + "\",\"shortName\":\"" + shortName + "\",\"paid\":true,\"accrual\":\"monthlyStart\",\"allowCarryForward\":true,\"allowEncashment\":true,\"precedence\":\"COE\",\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true,\"backDatedAllowedDays\":7,\"maxCF\":20,\"applyDaysBefore\":0,\"minAllowed\":" + limit + ",\"maxAllowed\":1,\"maxLeavesInMonth\":4,\"updatePolicies\":\"none\"}\n";
        variableContext.setScenarioContext("LEAVENAME", "CL" + userID);
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/");

        ApiHelper.genericResponseValidation(response, "API - ADD LEAVE FOR CUSTOM APPLICATION LIMIT");

        JsonPath jsonPathEvaluator = response.jsonPath();
        String leaveId = jsonPathEvaluator.get("data.leaveId").toString();
        variableContext.setScenarioContext("LEAVEID", leaveId);
        variableContext.setScenarioContext("LEAVESHORTNAME", shortName);

        jsonString = "{\"name\":\"custLeavePolicy" + userID + "\",\"leaveTypes\":[{\"cycleLimit\":20,\"accrualLimit\":20,\"carryForwardLimit\":20,\"encashmentLimit\":20,\"allowCarryForward\":true,\"allowEncashment\":true,\"accrual\":\"cycle\",\"leaveId\":" + leaveId + ",\"allowedOnProbation\":true,\"applyDaysBefore\":0,\"backDatedAllowedDays\":7,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"maxAllowed\":1,\"maxCF\":20,\"minAllowed\":" + limit + ",\"name\":\"" + "CL" + userID + "\",\"paid\":true,\"precedence\":\"COE\",\"probationProrate\":true,\"shortName\":\"" + shortName + "\",\"weekOffBetLeaves\":\"leave\"}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOHalfDayHours\":0,\"cOFullDayHours\":0},\"compOff\":{\"allowCF\":true,\"holidayBetLeaves\":\"leave\",\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true,\"probationProrate\":true,\"clubbing\":true},\"leaveApplicationSettings\":{\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}],\"approvalRule\":\"RMOrAdmin\"},\"assignedUsers\":[" + userID + "]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/leavePolicies");
        ApiHelper.genericResponseValidation(response, "API - ASSIGNED CUSTOM MINIMUM NUMBER OF LEAVE APPLICATION for User ID " + userID);

        variableContext.setScenarioContext("LEAVEPOLICYNAME", "custLeavePolicy" + userID);
        variableContext.setScenarioContext("DELETELEAVEPOLICY", "TRUE");
    }


    public void assignHolidayPolicyCustomCycle(String userID, String cycleName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveCycle = getLeaveCycle();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());
        variableContext.setScenarioContext("DHDATE", ld2);
        String jsonString = "{\"holidayPolicy\":{\"name\":\"New holiday policy" + userID + "\",\"cycleId\":" + getLeaveCycleID(cycleName) + ",\"country\":\"IN\",\"holidays\":[{\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true,\"country\":\"IN\"},{\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false,\"country\":\"IN\"}],\"assignedUserIds\":[" + userID + "],\"discretionaryLimit\":1}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/holidayPolicy");
        ApiHelper.genericResponseValidation(response, "API - Assign Holiday Policy for User ID " + userID);

        variableContext.setScenarioContext("NEWHOLIDAYPOLICYNAME", "New holiday policy" + userID);
        variableContext.setScenarioContext("DELETENEWHOLIDAYPOLICY", "TRUE");
    }

    public void createHolidayPolicy() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveCycle = getLeaveCycle();
        String cycleName = "DoNotDeleteLC";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();

        String name = "Holiday"+randomDataGenerator.getData("{RANDOM_STRING}");

        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());
        variableContext.setScenarioContext("DHDATE", ld2);

        String jsonString = "{\"holidayPolicy\":{\"name\":\""+name+"\",\"cycleId\":" + getLeaveCycleID(cycleName) + ",\"country\":\"IN\",\"holidays\":[{\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true,\"country\":\"IN\"},{\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false,\"country\":\"IN\"}],\"assignedUserIds\":[],\"discretionaryLimit\":1}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/holidayPolicy");
        ApiHelper.genericResponseValidation(response, "CREATE HOLIDAY POLICY");

        variableContext.setScenarioContext("NEWHOLIDAYPOLICYNAME", name);
        variableContext.setScenarioContext("DELETENEWHOLIDAYPOLICY", "TRUE");
    }


    //Get Holiday Policy Id
    public int getHolidayPolicyID(String policyName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"filter\":{},\"fields\":[\"id\",\"name\",\"cycleId\",\"cycleName\",\"country\"]}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/holidayPolicies");
        ApiHelper.genericResponseValidation(response, "API - GET HOLIDAY POLICY ID");

        int id = 0;
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> policyIDs = jsonPathEvaluator.get("data.holidayPolicies.name");

        for (int i = 0; i < policyIDs.size(); i++) {
            if (policyIDs.get(i).equalsIgnoreCase(policyName)) {
                id = jsonPathEvaluator.get("data.holidayPolicies[" + i + "].id");
                break;
            }
        }
        return id;
    }

    //User IDs assigned to a holiday policy
    public List<Integer> UserIDsInAHolidayPolicy() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"filter\":{}}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/holidayPolicies/" + getHolidayPolicyID("DoNotDeleteHP"));
        ApiHelper.genericResponseValidation(response, "API - GET USER IDS ASSIGNED TO HOLIDAY POLICY");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<Integer> userIDs = jsonPathEvaluator.get("data.assignedUsers.id");

        return userIDs;
    }

    //Assign User To Holiday Policy
    public void unassignUserFromHolidayPolicy(String userID) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        String holidayPolicyName = (String) variableContext.getScenarioContext("NEWHOLIDAYPOLICYNAME");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());

        String jsonString = "{\"holidayPolicy\":{\"name\":\"New holiday policy" + userID + "\",\"cycleId\":" + getLeaveCycleID("DoNotDeleteLC") + ",\"country\":\"IN\",\"holidays\":[{\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true,\"country\":\"IN\"},{\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false,\"country\":\"IN\"}],\"assignedUserIds\":[],\"discretionaryLimit\":1,\"id\":" + getHolidayPolicyID(holidayPolicyName) + "}}";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/holidayPolicies/" + getHolidayPolicyID(holidayPolicyName));
        ApiHelper.genericResponseValidation(response, "API - UNASSIGN USER ID FROM EXISTING HOLIDAY POLICY");
    }

    //Delete Holiday Policy
    public void deleteHolidayPolicy() throws ParseException {
        String holidayPolicyName = (String) variableContext.getScenarioContext("NEWHOLIDAYPOLICYNAME");
        String token = apiUtility.getidTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/holidayPolicies/" + getHolidayPolicyID(holidayPolicyName));
        ApiHelper.genericResponseValidation(response, "API -DELETE HOLIDAY POLICY");
    }

    //Add Holiday in holiday policy
    public void addHolidayInHolidayPolicy(String existing) throws ParseException {
        String holidayPolicyName = (String) variableContext.getScenarioContext("NEWHOLIDAYPOLICYNAME");
        String userID = (String) variableContext.getScenarioContext("NEWUSERID");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));

        LocalDate ld3 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld3 = ld3.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));

        variableContext.setScenarioContext("Added", ld3.getDayOfMonth());
        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());

        String token = apiUtility.getidTokenFromLocalStorage();

        String holidayName = "new" + RandomDataGenerator.randomAlphabetic(3);

        variableContext.setScenarioContext("NEWHOLIDAYNAME", holidayName);

        String addedHoliday = "{\"policyHolidayId\":0,\"holidayId\":0,\"holidayName\":\"" + holidayName + "\",\"date\":\"" + ld3.toString() + "T06:50:00.000Z\",\"discretionary\":true}";

        String jsonString = "";

        if (!(existing == "")) {
            variableContext.setScenarioContext("DELETEHOLIDAYFROMPOLICY", "TRUE");
            holidayPolicyName = "DoNotDeleteHP";
            jsonString = "{\"holidayPolicy\":{\"name\":\"DoNotDeleteHP\",\"cycleId\":" + getLeaveCycleID("DoNotDeleteLC") + ",\"country\":\"IN\",\"holidays\":[{\"policyHolidayId\":" + getPolicyHolidayID("Republic Day") + ",\"holidayId\":847,\"holidayName\":\"Republic Day\",\"date\":\"2022-01-26T00:00:00.000Z\",\"discretionary\":true},{\"policyHolidayId\":" + getPolicyHolidayID("Christmas") + ",\"holidayId\":3071,\"holidayName\":\"Christmas\",\"date\":\"2022-12-25T00:00:00.000Z\",\"discretionary\":false},{\"policyHolidayId\":" + getPolicyHolidayID("New Year's Eve") + ",\"holidayId\":3073,\"holidayName\":\"New Year's Eve\",\"date\":\"2022-12-31T00:00:00.000Z\",\"discretionary\":true}," + addedHoliday + "],\"assignedUserIds\":[277308417,277313986,277317164,277325186],\"discretionaryLimit\":3,\"id\":" + getHolidayPolicyID(holidayPolicyName) + "}}";
        } else {
            jsonString = "{\"holidayPolicy\":{\"name\":\"New holiday policy" + userID + "\",\"cycleId\":" + getLeaveCycleID("DoNotDeleteLC") + ",\"country\":\"IN\",\"holidays\":[{\"policyHolidayId\":" + getPolicyHolidayID("DH001") + ",\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true},{\"policyHolidayId\":" + getPolicyHolidayID("CH001") + ",\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false}," + addedHoliday + "],\"assignedUserIds\":[" + userID + "],\"discretionaryLimit\":1,\"id\":" + getHolidayPolicyID(holidayPolicyName) + "}}";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/holidayPolicies/" + getHolidayPolicyID(holidayPolicyName));
        ApiHelper.genericResponseValidation(response, "API - ADD HOLIDAY TO HOLIDAY POLICY");

    }

    //Add Holiday in holiday policy
    public void addCompulsoryHolidayInHolidayPolicy(String existing) throws ParseException {
        String holidayPolicyName = (String) variableContext.getScenarioContext("NEWHOLIDAYPOLICYNAME");
        String userID = (String) variableContext.getScenarioContext("NEWUSERID");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));

        LocalDate ld3 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld3 = ld3.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));

        variableContext.setScenarioContext("Added", ld3.getDayOfMonth());
        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());

        String token = apiUtility.getidTokenFromLocalStorage();

        String holidayName = "new" + RandomDataGenerator.randomAlphabetic(3);

        variableContext.setScenarioContext("NEWHOLIDAYNAME", holidayName);

        String addedHoliday = "{\"policyHolidayId\":0,\"holidayId\":0,\"holidayName\":\"" + holidayName + "\",\"date\":\"" + ld3.toString() + "T06:50:00.000Z\",\"discretionary\":false}";

        String jsonString = "";

        if (!(existing == "")) {
            variableContext.setScenarioContext("DELETEHOLIDAYFROMPOLICY", "TRUE");
            holidayPolicyName = "DoNotDeleteHP";
            jsonString = "{\"holidayPolicy\":{\"name\":\"DoNotDeleteHP\",\"cycleId\":" + getLeaveCycleID("DoNotDeleteLC") + ",\"country\":\"IN\",\"holidays\":[{\"policyHolidayId\":" + getPolicyHolidayID("Republic Day") + ",\"holidayId\":847,\"holidayName\":\"Republic Day\",\"date\":\"2022-01-26T00:00:00.000Z\",\"discretionary\":true},{\"policyHolidayId\":" + getPolicyHolidayID("Christmas") + ",\"holidayId\":3071,\"holidayName\":\"Christmas\",\"date\":\"2022-12-25T00:00:00.000Z\",\"discretionary\":false},{\"policyHolidayId\":" + getPolicyHolidayID("New Year's Eve") + ",\"holidayId\":3073,\"holidayName\":\"New Year's Eve\",\"date\":\"2022-12-31T00:00:00.000Z\",\"discretionary\":true}," + addedHoliday + "],\"assignedUserIds\":[277308417,277313986,277317164,277325186],\"discretionaryLimit\":3,\"id\":" + getHolidayPolicyID(holidayPolicyName) + "}}";
        } else {
            jsonString = "{\"holidayPolicy\":{\"name\":\"New holiday policy" + userID + "\",\"cycleId\":" + getLeaveCycleID("DoNotDeleteLC") + ",\"country\":\"IN\",\"holidays\":[{\"policyHolidayId\":" + getPolicyHolidayID("DH001") + ",\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true},{\"policyHolidayId\":" + getPolicyHolidayID("CH001") + ",\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false}," + addedHoliday + "],\"assignedUserIds\":[" + userID + "],\"discretionaryLimit\":1,\"id\":" + getHolidayPolicyID(holidayPolicyName) + "}}";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/holidayPolicies/" + getHolidayPolicyID(holidayPolicyName));
        ApiHelper.genericResponseValidation(response, "API - ADD HOLIDAY TO HOLIDAY POLICY");

    }

    //Delete holiday from holiday policy
    public void deleteHolidayFromHolidayPolicy(String existing) throws ParseException {
        String holidayPolicyName = (String) variableContext.getScenarioContext("NEWHOLIDAYPOLICYNAME");
        String userID = (String) variableContext.getScenarioContext("NEWUSERID");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDate ld = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld = ld.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        LocalDate ld2 = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ld2 = ld2.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));

        variableContext.setScenarioContext("CH", ld.getDayOfMonth());
        variableContext.setScenarioContext("DH", ld2.getDayOfMonth());

        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "";

        if (!(existing == "")) {
            holidayPolicyName = "DoNotDeleteHP";
            jsonString = "{\"holidayPolicy\":{\"name\":\"DoNotDeleteHP\",\"cycleId\":" + getLeaveCycleID("DoNotDeleteLC") + ",\"country\":\"IN\",\"holidays\":[{\"policyHolidayId\":" + getPolicyHolidayID("Republic Day") + ",\"holidayId\":847,\"holidayName\":\"Republic Day\",\"date\":\"2022-01-26T00:00:00.000Z\",\"discretionary\":true},{\"policyHolidayId\":" + getPolicyHolidayID("Christmas") + ",\"holidayId\":3071,\"holidayName\":\"Christmas\",\"date\":\"2022-12-25T00:00:00.000Z\",\"discretionary\":false},{\"policyHolidayId\":" + getPolicyHolidayID("New Year's Eve") + ",\"holidayId\":3073,\"holidayName\":\"New Year's Eve\",\"date\":\"2022-12-31T00:00:00.000Z\",\"discretionary\":true}],\"assignedUserIds\":[277308417,277313986,277317164,277325186],\"discretionaryLimit\":3,\"id\":" + getHolidayPolicyID(holidayPolicyName) + "}}";
        } else {
            jsonString = "{\"holidayPolicy\":{\"name\":\"New holiday policy" + userID + "\",\"cycleId\":" + getLeaveCycleID("DoNotDeleteLC") + ",\"country\":\"IN\",\"holidays\":[{\"policyHolidayId\":" + getPolicyHolidayID("DH001") + ",\"holidayId\":0,\"holidayName\":\"DH001\",\"date\":\"" + ld2.toString() + "T06:50:00.000Z\",\"discretionary\":true},{\"policyHolidayId\":" + getPolicyHolidayID("CH001") + ",\"holidayId\":0,\"holidayName\":\"CH001\",\"date\":\"" + ld.toString() + "T06:50:00.000Z\",\"discretionary\":false}],\"assignedUserIds\":[" + userID + "],\"discretionaryLimit\":1,\"id\":" + getHolidayPolicyID(holidayPolicyName) + "}}";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID("DoNotDeleteLC") + "/holidayPolicies/" + getHolidayPolicyID(holidayPolicyName));
        ApiHelper.genericResponseValidation(response, "API - DELETED HOLIDAY FROM HOLIDAY POLICY");
    }

    //Get policy holiday ID
    public int getPolicyHolidayID(String name) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String userID = (String) variableContext.getScenarioContext("NEWUSERID");
        String holidayPolicyName = (String) variableContext.getScenarioContext("NEWHOLIDAYPOLICYNAME");

        if (holidayPolicyName.equalsIgnoreCase("")) {
            holidayPolicyName = "DoNotDeleteHP";
        }

        String leaveCycleName = "DoNotDeleteLC";

        String jsonString = "{\"filter\":{}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/holidays/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID(leaveCycleName) + "/holidayPolicies/" + getHolidayPolicyID(holidayPolicyName));
        ApiHelper.genericResponseValidation(response, "API - GET POLICY HOLIDAY ID");

        int id = 0;
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> policyHolidayIDs = jsonPathEvaluator.get("data.policy_holidays.name");

        for (int i = 0; i < policyHolidayIDs.size(); i++) {
            if (policyHolidayIDs.get(i).equalsIgnoreCase(name)) {
                id = jsonPathEvaluator.get("data.policy_holidays[" + i + "].id");
                break;
            }
        }
        return id;
    }

    //Leaves in the organisation
    public List<String> leavesInTheOrganisation() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        String jsonString = "{\"filter\":{}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes");
        ApiHelper.genericResponseValidation(response, "API - GET LEAVES IN THE ORGANISATIONS");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> leaves = jsonPathEvaluator.get("data.leaveTypes.name");

        return leaves.stream().sorted().collect(Collectors.toList());
    }

    //Leave names and short names map
    public Map<String, String> leaveNamesAndShortNamesMap() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        Map<String, String> h = new LinkedHashMap<String, String>();

        String jsonString = "{\"filter\":{}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes");
        ApiHelper.genericResponseValidation(response, "API - GET LEAVES AND SHORT NAMES MAP IN THE ORGANISATIONS");

        JsonPath jsonPathEvaluator = response.jsonPath();

        List<String> leaves = jsonPathEvaluator.get("data.leaveTypes.name");

        for (int i = 0; i < leaves.size(); i++) {
            String key = jsonPathEvaluator.get("data.leaveTypes[" + i + "].name");
            String value = jsonPathEvaluator.get("data.leaveTypes[" + i + "].shortName");

            h.put(key.toLowerCase(), value.toLowerCase());
        }

        return h;
    }

    //Get Leave ID
    public String leaveID(String leaveName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        int id = 0;
        String jsonString = "{\"filter\":{}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes");
        ApiHelper.genericResponseValidation(response, "API - GET LEAVE ID");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> leaves = jsonPathEvaluator.get("data.leaveTypes.name");

        for (int i = 0; i < leaves.size(); i++) {
            String name = jsonPathEvaluator.get("data.leaveTypes[" + i + "].name");
            if (name.equalsIgnoreCase(leaveName)) {
                id = jsonPathEvaluator.get("data.leaveTypes[" + i + "].id");
                break;
            }
        }

        return String.valueOf(id);

    }

    //Function to create a custom leave to be edited
    public void createLeaveToBeEdited(DataTable data) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

        String shortName = RandomDataGenerator.randomAlphabetic(3);
        String name = "newLeave" + RandomDataGenerator.randomAlphabetic(4);
        String paid = dataMap.get("paid").trim();
        String accrual = dataMap.get("accrual");
        String allowCarryForward = dataMap.get("allowCarryForward");
        String allowEncashment = dataMap.get("allowEncashment");
        String precedence = dataMap.get("precedence");
        String holidayBetLeaves = dataMap.get("holidayBetLeaves");
        String weekOffBetLeaves = dataMap.get("weekOffBetLeaves");
        String allowedOnProbation = dataMap.get("allowedOnProbation");
        String probationProrate = dataMap.get("probationProrate");
        String clubbing = dataMap.get("clubbing");
        String backDatedAllowedDays = dataMap.get("backDatedAllowedDays");
        String maxCF = dataMap.get("maxCF");
        String applyDaysBefore = dataMap.get("applyDaysBefore");
        String minAllowed = dataMap.get("minAllowed");
        String maxAllowed = dataMap.get("maxAllowed");
        String maxLeavesInMonth = dataMap.get("maxLeavesInMonth");
        String updatePolicies = dataMap.get("updatePolicies");


        String userID = (String) variableContext.getScenarioContext("NEWUSERID");
        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"name\":\"" + name + "\",\"shortName\":\"" + shortName + "\",\"paid\":" + paid + ",\"accrual\":\"" + accrual + "\",\"allowCarryForward\":" + allowCarryForward + ",\"allowEncashment\":" + allowEncashment + ",\"precedence\":\"" + precedence + "\",\"holidayBetLeaves\":\"" + holidayBetLeaves + "\",\"weekOffBetLeaves\":\"" + weekOffBetLeaves + "\",\"allowedOnProbation\":" + allowedOnProbation + ",\"probationProrate\":" + probationProrate + ",\"clubbing\":" + clubbing + ",\"backDatedAllowedDays\":" + backDatedAllowedDays + ",\"maxCF\":" + maxCF + ",\"applyDaysBefore\":" + applyDaysBefore + ",\"minAllowed\":" + minAllowed + ",\"maxAllowed\":" + maxAllowed + ",\"maxLeavesInMonth\":" + maxLeavesInMonth + ",\"updatePolicies\":\"" + updatePolicies + "\"}\n";
        variableContext.setScenarioContext("LEAVENAME", name);
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveType/organisations/" + apiUtility.getOrgnizationID() + "/leaveTypes/");

        ApiHelper.genericResponseValidation(response, "API - Add Leave - Cycle Type ");


        JsonPath jsonPathEvaluator = response.jsonPath();
        String leaveId = jsonPathEvaluator.get("data.leaveId").toString();
        variableContext.setScenarioContext("LEAVEID", leaveId);
        variableContext.setScenarioContext("LEAVESHORTNAME", shortName);
        variableContext.setScenarioContext("DELETENEWLEAVE", "TRUE");

        String addedLeavePayload = "";
        if(accrual.equalsIgnoreCase("cycle")){
            addedLeavePayload = "{\"name\":\"" + name + "\",\"shortName\":\"" + shortName + "\",\"paid\":" + paid + ",\"accrual\":\"" + accrual + "\",\"allowCarryForward\":" + allowCarryForward + ",\"allowEncashment\":" + allowEncashment + ",\"precedence\":\"" + precedence + "\",\"holidayBetLeaves\":\"" + holidayBetLeaves + "\",\"weekOffBetLeaves\":\"" + weekOffBetLeaves + "\",\"allowedOnProbation\":" + allowedOnProbation + ",\"probationProrate\":" + probationProrate + ",\"clubbing\":" + clubbing + ",\"backDatedAllowedDays\":" + backDatedAllowedDays + ",\"maxCF\":" + maxCF + ",\"applyDaysBefore\":" + applyDaysBefore + ",\"minAllowed\":" + minAllowed + ",\"maxAllowed\":" + maxAllowed + ",\"leaveId\":" + leaveId + ",\"cycleLimit\": 10,\"accrualLimit\": 10,\"encashmentLimit\": 10,\"carryForwardLimit\": 10}";
        }else {
            addedLeavePayload = "{\"name\":\"" + name + "\",\"shortName\":\"" + shortName + "\",\"paid\":" + paid + ",\"accrual\":\"" + accrual + "\",\"allowCarryForward\":" + allowCarryForward + ",\"allowEncashment\":" + allowEncashment + ",\"precedence\":\"" + precedence + "\",\"holidayBetLeaves\":\"" + holidayBetLeaves + "\",\"weekOffBetLeaves\":\"" + weekOffBetLeaves + "\",\"allowedOnProbation\":" + allowedOnProbation + ",\"probationProrate\":" + probationProrate + ",\"clubbing\":" + clubbing + ",\"backDatedAllowedDays\":" + backDatedAllowedDays + ",\"maxCF\":" + maxCF + ",\"applyDaysBefore\":" + applyDaysBefore + ",\"minAllowed\":" + minAllowed + ",\"maxAllowed\":" + maxAllowed + ",\"leaveId\":" + leaveId + ",\"cycleLimit\": 10,\"accrualLimit\": \"05\",\"encashmentLimit\": 10,\"carryForwardLimit\": 10}";
        }

        variableContext.setScenarioContext("ADDLEAVEPAYLOAD",addedLeavePayload);
    }


    //Leave Cycles Map
    public Map<String, List<String>> leaveCyclesMap() throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();

        Map<String, List<String>> h = new LinkedHashMap<String, List<String>>();

        String jsonString = "{\"filter\":{},\"fields\":[\"id\",\"name\",\"startDate\",\"endDate\",\"leavePolicies\"]}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leaveCycle/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles");
        ApiHelper.genericResponseValidation(response, "API - GET LEAVES CYCLE DETAILS MAP IN THE ORGANISATIONS");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<Object> leaveCycles = jsonPathEvaluator.get("data.leaveCycles");

        for (int i = 0; i < leaveCycles.size(); i++) {
            List<String> leaveCycleDetails = new ArrayList<>();
            String policyName = "";
            String startDate = "";
            String endDate = "";

            String range = "";
            String policyCount = "";

            policyName = jsonPathEvaluator.get("data.leaveCycles[" + i + "].name");

            startDate = jsonPathEvaluator.get("data.leaveCycles[" + i + "].startDate").toString().substring(0, 12);
            endDate = jsonPathEvaluator.get("data.leaveCycles[" + i + "].endDate").toString().substring(0, 12);

            for (int j = 0; j < 2; j++) {
                String dateType = "";
                if (j == 0) {
                    dateType = startDate;
                } else {
                    dateType = endDate;
                }


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH);
                Date myDate = sdf.parse(dateType);
                sdf.applyPattern("d MMM yyyy");
                String sMyDate = sdf.format(myDate);

                String components[] = sMyDate.split(" ");


                if (j == 0) {
                    range = components[1] + " " + components[0] + ", " + components[2];
                } else {
                    range = range + " to " + components[1] + " " + components[0] + ", " + components[2];
                }

            }

            leaveCycleDetails.add(range);
            List<Object> leavePolicies = jsonPathEvaluator.get("data.leaveCycles[" + i + "].leave_policies");
            if (leavePolicies == null) {
                policyCount = "0";
            } else {
                policyCount = String.valueOf(leavePolicies.size());
            }

            leaveCycleDetails.add(policyCount);

            h.put(policyName, leaveCycleDetails);
        }

        //System.out.println(h);
        return h;
    }


    public void assignSecondLeavePolicyToSpecificUserForEditing(DataTable Data) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        Map<String, String> dataMap = Data.transpose().asMap(String.class, String.class);
        List<Integer> userIds = new ArrayList<>();
        List<Integer> admins = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        List<Integer> usersAssignedToPolicy = new ArrayList<>();

        List<String> phones = new ArrayList<>();

        String cycleName = (String) variableContext.getScenarioContext("SECONDLEAVECYCLENAME");

        if(cycleName.equalsIgnoreCase("")){
            cycleName = "DoNotDeleteLC";
        }

        if(!(variableContext.getScenarioContext("NEWUSERPHONE").equals(""))){
            String phone = (String) variableContext.getScenarioContext("NEWUSERPHONE");
            int userID = Integer.parseInt(userManagementService.getUserID(phone));
            userIds.add(userID);
        }

        String name = dataMap.get("name").trim() + randomDataGenerator.getData("{RANDOM_STRING}");
        String payOTHours = dataMap.get("payOTHours").trim();
        String convertOTtoCO = dataMap.get("convertOTtoCO").trim();
        String cOHalfDayHours = dataMap.get("cOHalfDayHours").trim();
        String cOFullDayHours = dataMap.get("cOFullDayHours").trim();
        String allowCF = dataMap.get("allowCF").trim();
        String clubbing = dataMap.get("clubbing").trim();
        String holidayBetLeaves = dataMap.get("holidayBetLeaves").trim();
        String weekOffBetLeaves = dataMap.get("weekOffBetLeaves").trim();
        String allowedOnProbation = dataMap.get("allowedOnProbation").trim();
        String probationProrate = dataMap.get("probationProrate").trim();
        String approvalRule = dataMap.get("approvalRule").trim();
        String notifyAdmin = dataMap.get("notifyAdmin").trim();
        String notifyUsers = dataMap.get("notifyUsers").trim();
        String inherit = dataMap.get("inherit").trim();
        String AssignUsers = dataMap.get("AssignUsers").trim();

        if(notifyAdmin.equalsIgnoreCase("true")){
            phones.add("9420767761");//LuzviDoNotDelete
            phones.add("7722082259");//Sidharth
            for (int i=0;i<2;i++) {
                int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                admins.add(userID);
            }
        }

        if(notifyUsers.equalsIgnoreCase("true")){
            phones.add("7885256626");//LarylDoNotDelete
            phones.add("8105935806");//KrishnaDoNotDelete
            for (int i=0;i<2;i++) {
                int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                users.add(userID);
            }
        }

        if(AssignUsers.equalsIgnoreCase("true")){
            if(!(variableContext.getScenarioContext("NEWUSERNAME1").equals(""))){
                for (int i=0;i<2;i++) {
                    String userPhone = (String) variableContext.getScenarioContext("NEWUSERPHONE"+i);
                    int userID = Integer.parseInt(userManagementService.getUserID(userPhone));
                    usersAssignedToPolicy.add(userID);
                }
            }else {
                phones.add("7889955562");//checkDoNotDelete
                phones.add("9518589192");//ManaliDoNotDelete
                for (int i=0;i<2;i++) {
                    int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                    usersAssignedToPolicy.add(userID);
                }
            }
        }

        List<Integer> finalListUserIDs =  Stream.concat(usersAssignedToPolicy.stream(), userIds.stream()).collect(Collectors.toList());


        String jsonString ="";

        if(inherit.equalsIgnoreCase("true")){
            jsonString ="{\"name\":\"DoNotDelete\",\"leaveTypes\":[{\"name\":\"DoNotDelete1\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":508,\"clubbing\":true,\"shortName\":\"d1\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOFullDayHours\":0,\"cOHalfDayHours\":0},\"compOff\":{\"allowCF\":true,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true},\"leaveApplicationSettings\":{\"approvalRule\":\"RMOrAdmin\",\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}]},\"assignedUsers\":[277308417,277313986,277316843,277316844,277316895,277317164,277325186,277326512,277330725]}";
        }else{
            String leavePayload = (String) variableContext.getScenarioContext("ADDLEAVEPAYLOAD");

            if(leavePayload.equalsIgnoreCase("")){
                leavePayload = "{\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}";
            }

            jsonString = "{\"name\":\""+name+"\",\"leaveTypes\":["+leavePayload+"],\"overtime\":{\"payOTHours\":"+payOTHours+",\"convertOTtoCO\":"+convertOTtoCO+",\"cOFullDayHours\":"+cOHalfDayHours+",\"cOHalfDayHours\":"+cOFullDayHours+"},\"compOff\":{\"allowCF\":"+allowCF+",\"clubbing\":"+clubbing+",\"holidayBetLeaves\":\""+holidayBetLeaves+"\",\"probationProrate\":"+probationProrate+",\"weekOffBetLeaves\":\""+weekOffBetLeaves+"\",\"allowedOnProbation\":"+allowedOnProbation+"},\"leaveApplicationSettings\":{\"approvalRule\":\""+approvalRule+"\",\"notifyUserEmails\":[{\"admins\":"+admins+",\"users\":"+users+"}]},\"assignedUsers\":" + finalListUserIDs + "}";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID(cycleName) + "/leavePolicies");
        ApiHelper.genericResponseValidation(response, "API - CREATE SECOND POLICY FOR EDITING");

        variableContext.setScenarioContext("SECONDPOLICYTOEDIT",name);
    }


    public void assignLeavePolicyToSpecificUserForEditing(DataTable Data) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        Map<String, String> dataMap = Data.transpose().asMap(String.class, String.class);
        List<Integer> userIds = new ArrayList<>();
        List<Integer> admins = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        List<Integer> usersAssignedToPolicy = new ArrayList<>();

        List<String> phones = new ArrayList<>();

        String cycleName = (String) variableContext.getScenarioContext("NEWLEAVECYCLENAME");

        if(!(variableContext.getScenarioContext("NEWUSERPHONE").equals(""))){
            String phone = (String) variableContext.getScenarioContext("NEWUSERPHONE");
            int userID = Integer.parseInt(userManagementService.getUserID(phone));
            userIds.add(userID);
        }

        String name = dataMap.get("name").trim() + randomDataGenerator.getData("{RANDOM_STRING}");
        String payOTHours = dataMap.get("payOTHours").trim();
        String convertOTtoCO = dataMap.get("convertOTtoCO").trim();
        String cOHalfDayHours = dataMap.get("cOHalfDayHours").trim();
        String cOFullDayHours = dataMap.get("cOFullDayHours").trim();
        String allowCF = dataMap.get("allowCF").trim();
        String clubbing = dataMap.get("clubbing").trim();
        String holidayBetLeaves = dataMap.get("holidayBetLeaves").trim();
        String weekOffBetLeaves = dataMap.get("weekOffBetLeaves").trim();
        String allowedOnProbation = dataMap.get("allowedOnProbation").trim();
        String probationProrate = dataMap.get("probationProrate").trim();
        String approvalRule = dataMap.get("approvalRule").trim();
        String notifyAdmin = dataMap.get("notifyAdmin").trim();
        String notifyUsers = dataMap.get("notifyUsers").trim();
        String inherit = dataMap.get("inherit").trim();
        String AssignUsers = dataMap.get("AssignUsers").trim();

        if(notifyAdmin.equalsIgnoreCase("true")){
            phones.add("9420767761");//LuzviDoNotDelte
            phones.add("7722082259");//Sidharth
            for (int i=0;i<2;i++) {
                int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                admins.add(userID);
            }
        }

        if(notifyUsers.equalsIgnoreCase("true")){
            phones.add("7885256626");//LarylDoNotDelete
            phones.add("8105935806");//KrishnaDoNotDelete
            for (int i=0;i<2;i++) {
                int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                users.add(userID);
            }
        }

        if(AssignUsers.equalsIgnoreCase("true")){
            if(!(variableContext.getScenarioContext("NEWUSERNAME1").equals(""))){
                for (int i=0;i<2;i++) {
                    String userPhone = (String) variableContext.getScenarioContext("NEWUSERPHONE"+i);
                    String userIDString = userManagementService.getUserID(userPhone);
                    int userID = Integer.parseInt(userIDString);
                    usersAssignedToPolicy.add(userID);
                }
            }else {
                phones.add("7889955562");//checkDoNotDelete
                phones.add("9518589192");//ManaliDoNotDelete
                for (int i=0;i<2;i++) {
                    String userIDString = userManagementService.getUserID(phones.get(i));
                    int userID = Integer.parseInt(userIDString);
                    usersAssignedToPolicy.add(userID);
                }
            }
        }

        List<Integer> finalListUserIDs =  Stream.concat(usersAssignedToPolicy.stream(), userIds.stream()).collect(Collectors.toList());


        String jsonString ="";

        if(inherit.equalsIgnoreCase("true")){
            jsonString ="{\"name\":\"DoNotDelete\",\"leaveTypes\":[{\"name\":\"DoNotDelete1\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":508,\"clubbing\":true,\"shortName\":\"d1\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOFullDayHours\":0,\"cOHalfDayHours\":0},\"compOff\":{\"allowCF\":true,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true},\"leaveApplicationSettings\":{\"approvalRule\":\"RMOrAdmin\",\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}]},\"assignedUsers\":[277308417,277313986,277316843,277316844,277316895,277317164,277325186,277326512,277330725]}";
        }else{
            String leavePayload = (String) variableContext.getScenarioContext("ADDLEAVEPAYLOAD");

            if(leavePayload.equalsIgnoreCase("")){
                leavePayload = "{\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":15,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":15,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":15,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":15,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":15,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}";
            }

            jsonString = "{\"name\":\""+name+"\",\"leaveTypes\":["+leavePayload+"],\"overtime\":{\"payOTHours\":"+payOTHours+",\"convertOTtoCO\":"+convertOTtoCO+",\"cOFullDayHours\":"+cOHalfDayHours+",\"cOHalfDayHours\":"+cOFullDayHours+"},\"compOff\":{\"allowCF\":"+allowCF+",\"clubbing\":"+clubbing+",\"holidayBetLeaves\":\""+holidayBetLeaves+"\",\"probationProrate\":"+probationProrate+",\"weekOffBetLeaves\":\""+weekOffBetLeaves+"\",\"allowedOnProbation\":"+allowedOnProbation+"},\"leaveApplicationSettings\":{\"approvalRule\":\""+approvalRule+"\",\"notifyUserEmails\":[{\"admins\":"+admins+",\"users\":"+users+"}]},\"assignedUsers\":" + finalListUserIDs + "}";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID(cycleName) + "/leavePolicies");
        ApiHelper.genericResponseValidation(response, "API - CREATE POLICY FOR EDITING");

        variableContext.setScenarioContext("POLICYTOEDIT",name);
    }


    public void assignLeavePolicyToSpecificUserForEditingCurrentPolicy(DataTable Data) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        Map<String, String> dataMap = Data.transpose().asMap(String.class, String.class);
        List<Integer> userIds = new ArrayList<>();
        List<Integer> admins = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        List<Integer> usersAssignedToPolicy = new ArrayList<>();

        List<String> phones = new ArrayList<>();

        String cycleName = "DoNotDeleteLC";

        if(!(variableContext.getScenarioContext("NEWUSERPHONE").equals(""))){
            String phone = (String) variableContext.getScenarioContext("NEWUSERPHONE");
            int userID = Integer.parseInt(userManagementService.getUserID(phone));
            userIds.add(userID);
        }

        String name = dataMap.get("name").trim() + randomDataGenerator.getData("{RANDOM_STRING}");
        String payOTHours = dataMap.get("payOTHours").trim();
        String convertOTtoCO = dataMap.get("convertOTtoCO").trim();
        String cOHalfDayHours = dataMap.get("cOHalfDayHours").trim();
        String cOFullDayHours = dataMap.get("cOFullDayHours").trim();
        String allowCF = dataMap.get("allowCF").trim();
        String clubbing = dataMap.get("clubbing").trim();
        String holidayBetLeaves = dataMap.get("holidayBetLeaves").trim();
        String weekOffBetLeaves = dataMap.get("weekOffBetLeaves").trim();
        String allowedOnProbation = dataMap.get("allowedOnProbation").trim();
        String probationProrate = dataMap.get("probationProrate").trim();
        String approvalRule = dataMap.get("approvalRule").trim();
        String notifyAdmin = dataMap.get("notifyAdmin").trim();
        String notifyUsers = dataMap.get("notifyUsers").trim();
        String inherit = dataMap.get("inherit").trim();
        String AssignUsers = dataMap.get("AssignUsers").trim();

        if(notifyAdmin.equalsIgnoreCase("true")){
            phones.add("9420767761");//LuzviDoNotDelete
            phones.add("7722082259");//Sidharth
            for (int i=0;i<2;i++) {
                int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                admins.add(userID);
            }
        }

        if(notifyUsers.equalsIgnoreCase("true")){
            phones.add("7885256626");//LarylDoNotDelete
            phones.add("8105935806");//KrishnaDoNotDelete
            for (int i=0;i<2;i++) {
                int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                users.add(userID);
            }
        }

        if(AssignUsers.equalsIgnoreCase("true")){
            if(!(variableContext.getScenarioContext("NEWUSERNAME1").equals(""))){
                for (int i=0;i<2;i++) {
                    String userPhone = (String) variableContext.getScenarioContext("NEWUSERPHONE"+i);
                    int userID = Integer.parseInt(userManagementService.getUserID(userPhone));
                    usersAssignedToPolicy.add(userID);
                }
            }else {
                phones.add("7889955562");//checkDoNotDelete
                phones.add("9518589192");//ManaliDoNotDelete
                for (int i=0;i<2;i++) {
                    int userID = Integer.parseInt(userManagementService.getUserID(phones.get(i)));
                    usersAssignedToPolicy.add(userID);
                }
            }
        }

        List<Integer> finalListUserIDs =  Stream.concat(usersAssignedToPolicy.stream(), userIds.stream()).collect(Collectors.toList());


        String jsonString ="";

        if(inherit.equalsIgnoreCase("true")){
            jsonString ="{\"name\":\"DoNotDelete\",\"leaveTypes\":[{\"name\":\"DoNotDelete1\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":508,\"clubbing\":true,\"shortName\":\"d1\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}],\"overtime\":{\"payOTHours\":true,\"convertOTtoCO\":true,\"cOFullDayHours\":0,\"cOHalfDayHours\":0},\"compOff\":{\"allowCF\":true,\"clubbing\":true,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowedOnProbation\":true},\"leaveApplicationSettings\":{\"approvalRule\":\"RMOrAdmin\",\"notifyUserEmails\":[{\"admins\":[],\"users\":[]}]},\"assignedUsers\":[277308417,277313986,277316843,277316844,277316895,277317164,277325186,277326512,277330725]}";
        }else{
            String leavePayload = (String) variableContext.getScenarioContext("ADDLEAVEPAYLOAD");

            if(leavePayload.equalsIgnoreCase("")){
                leavePayload = "{\"name\":\"DoNotDelete3\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":510,\"clubbing\":true,\"shortName\":\"d3\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7},{\"name\":\"DoNotDelete2\",\"paid\":true,\"maxCF\":20,\"accrual\":\"cycle\",\"leaveId\":509,\"clubbing\":true,\"shortName\":\"d2\",\"cycleLimit\":20,\"maxAllowed\":1,\"minAllowed\":0.5,\"precedence\":\"COE\",\"accrualLimit\":20,\"allowEncashment\":true,\"applyDaysBefore\":0,\"encashmentLimit\":20,\"holidayBetLeaves\":\"leave\",\"probationProrate\":true,\"weekOffBetLeaves\":\"leave\",\"allowCarryForward\":true,\"carryForwardLimit\":20,\"allowedOnProbation\":true,\"backDatedAllowedDays\":7}";
            }

            jsonString = "{\"name\":\""+name+"\",\"leaveTypes\":["+leavePayload+"],\"overtime\":{\"payOTHours\":"+payOTHours+",\"convertOTtoCO\":"+convertOTtoCO+",\"cOFullDayHours\":"+cOHalfDayHours+",\"cOHalfDayHours\":"+cOFullDayHours+"},\"compOff\":{\"allowCF\":"+allowCF+",\"clubbing\":"+clubbing+",\"holidayBetLeaves\":\""+holidayBetLeaves+"\",\"probationProrate\":"+probationProrate+",\"weekOffBetLeaves\":\""+weekOffBetLeaves+"\",\"allowedOnProbation\":"+allowedOnProbation+"},\"leaveApplicationSettings\":{\"approvalRule\":\""+approvalRule+"\",\"notifyUserEmails\":[{\"admins\":"+admins+",\"users\":"+users+"}]},\"assignedUsers\":" + finalListUserIDs + "}";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                put(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/leavePolicy/organisations/" + apiUtility.getOrgnizationID() + "/leaveCycles/" + getLeaveCycleID(cycleName) + "/leavePolicies");
        ApiHelper.genericResponseValidation(response, "API - CREATE POLICY FOR EDITING");

        variableContext.setScenarioContext("POLICYTOEDIT",name);
        variableContext.setScenarioContext("LEAVEPOLICYNAME", name);
        variableContext.setScenarioContext("DELETELEAVEPOLICY","TRUE");
    }

    public String getUserLeaveId(String phone,String leaveName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        int userLeaveId = 0;

        String leaveCycleName = (String) variableContext.getScenarioContext("NEWLEAVECYCLENAME");

        if(leaveCycleName.equalsIgnoreCase("")){
            leaveCycleName = "DoNotDeleteLC";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userLeave/organisations/" + apiUtility.getOrgnizationID() + "/users/"+userManagementService.getUserID(phone)+"/leaveCycles/"+getLeaveCycleID(leaveCycleName)+ "/leaves");

        ApiHelper.genericResponseValidation(response, "API - GET USER LEAVE ID");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<Object> userLeaveData = jsonPathEvaluator.get("data.userLeaveData.leaves");

        for(int i=0; i< userLeaveData.size();i++){
            String name = jsonPathEvaluator.get("data.userLeaveData.leaves["+i+"].leaveName");
            if(name.equalsIgnoreCase(leaveName)){
                userLeaveId = jsonPathEvaluator.get("data.userLeaveData.leaves["+i+"].userLeaveId");
                break;
            }
        }

        return String.valueOf(userLeaveId);
    }

    public String getUserAppliedLeaveId(String phone,String leaveName) throws ParseException {
        String token = apiUtility.getidTokenFromLocalStorage();
        int userAppliedLeaveId = 0;

        String leaveCycleName = (String) variableContext.getScenarioContext("NEWLEAVECYCLENAME");

        if(leaveCycleName.equalsIgnoreCase("")){
            leaveCycleName = "DoNotDeleteLC";
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userLeave/organisations/" + apiUtility.getOrgnizationID() + "/users/"+userManagementService.getUserID(phone)+"/leaveCycles/"+getLeaveCycleID(leaveCycleName)+ "/leaves");

        ApiHelper.genericResponseValidation(response, "API - GET USER LEAVE ID");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<Object> userLeaveData = jsonPathEvaluator.get("data.userLeaveData.appliedLeaves");

        for(int i=0; i< userLeaveData.size();i++){
            String comment = jsonPathEvaluator.get("data.userLeaveData.appliedLeaves["+i+"].comment");
            String commentToBeMatched = (String) variableContext.getScenarioContext("LEAVECOMMENT");
            if(comment.contains(commentToBeMatched)){
                userAppliedLeaveId = jsonPathEvaluator.get("data.userLeaveData.appliedLeaves["+i+"].userAppliedLeaveId");
                break;
            }
        }

        return String.valueOf(userAppliedLeaveId);
    }


   public void applyLeaveForAUser(String userLeaveID,String date, String phone,String leaveName)  throws ParseException {
       String token = apiUtility.getidTokenFromLocalStorage();


       String leaveCycleName = (String) variableContext.getScenarioContext("NEWLEAVECYCLENAME");
       String comment = randomDataGenerator.getData("{RANDOM_STRING}");

       variableContext.setScenarioContext("LEAVECOMMENT",comment);

       if(leaveCycleName.equalsIgnoreCase("")){
           leaveCycleName = "DoNotDeleteLC";
       }

       String policyName = (String) variableContext.getScenarioContext("POLICYTOEDIT");

       String secondPolicy = (String) variableContext.getScenarioContext("SECONDPOLICYTOEDIT");

       if(!(variableContext.getScenarioContext("APPLYTOSECONDPOLICY").equals(""))){
           policyName = secondPolicy;
       }

       String half = null;

       if(!(variableContext.getScenarioContext("APPLYFORSECONDHALF").equals(""))){
           half = "First Half";
       }else{
           half = "Second Half";
       }

       String jsonString = "{\"leaveId\":"+userLeaveID+",\"validFrom\":\""+date+"\",\"userRemark\":\""+comment+"\",\"validTill\":\""+date+"\",\"duration\":\"Half Day\",\"dayType\":\""+half+"\"}";

       Response response = ApiHelper.givenRequestSpecification()
               .header("authorization", token)
               .body(jsonString)
               .when().redirects().follow(false).
               post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userLeave/organisations/"+apiUtility.getOrgnizationID()+"/users/"+userManagementService.getUserID(phone)+"/leaveCycles/"+getLeaveCycleID(leaveCycleName)+"/leavePolicies/"+getLeavePolicyID(policyName,leaveCycleName)+"/leaves/"+userLeaveID);

       ApiHelper.genericResponseValidation(response, "API - POST - APPLY LEAVE FOR A USER");
   }

    public void processALeave(String phone, String process,String comment,String leaveName) throws ParseException {
        //https://test.saams.api.spintly.com/v2/leaveManagement/userLeave/organisations/776/users/277313986/leaveCycles/5481/leaves/2064/process

        String token = apiUtility.getidTokenFromLocalStorage();
        String leaveCycleName = (String) variableContext.getScenarioContext("NEWLEAVECYCLENAME");

        if(leaveCycleName.equalsIgnoreCase("")){
            leaveCycleName = "DoNotDeleteLC";
        }

        String toDo = null;

        if(process.equalsIgnoreCase("withdrawing")){
            toDo = "cancelled";
        }else if(process.equalsIgnoreCase("approving")){
            toDo = "approved";
        }else{
            toDo = "rejected";
        }

        String jsonString = "{\"type\": \""+toDo+"\",\"comment\": \"jkdsncj\"}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") + "/v2/leaveManagement/userLeave/organisations/"+apiUtility.getOrgnizationID()+"/users/"+userManagementService.getUserID(phone)+"/leaveCycles/"+getLeaveCycleID(leaveCycleName)+"/leaves/"+getUserAppliedLeaveId(phone,leaveName)+"/process");


        ApiHelper.genericResponseValidation(response, "API - POST - PROCESS LEAVE FOR A USER : "+process);
    }

    public List<String> listOfHolidayPolicies(){
        String token = apiUtility.getidTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");

        List<String> names = new ArrayList<>();
        //v2/leaveManagement/holidays/organisations/776/holidayPolicies

        String jsonString = "{\"filter\":{},\"fields\":[\"id\",\"name\",\"cycleId\",\"cycleName\",\"country\"]}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.saams.api.url") +"/v2/leaveManagement/holidays/organisations/"+orgID+"/holidayPolicies");

        ApiHelper.genericResponseValidation(response, "API - GET LIST OF HOLIDAY POLICIES: ");

        JsonPath jsonPathEvaluator = response.jsonPath();

        names = jsonPathEvaluator.get("data.holidayPolicies.name");

        return names;
    }

    public void restoreDefaultHolidayPolicy(){
        String token = apiUtility.getidTokenFromLocalStorage();
        String jsonString = "{\"holidayPolicy\":{\"name\":\"DoNotDeleteHP\",\"cycleId\":3926,\"country\":\"IN\",\"holidays\":[{\"policyHolidayId\":1704,\"holidayId\":847,\"holidayName\":\"Republic Day\",\"date\":\"2022-01-26T00:00:00.000Z\",\"discretionary\":true},{\"policyHolidayId\":1629,\"holidayId\":3071,\"holidayName\":\"Christmas\",\"date\":\"2022-12-25T00:00:00.000Z\",\"discretionary\":false},{\"policyHolidayId\":1630,\"holidayId\":3073,\"holidayName\":\"New Year's Eve\",\"date\":\"2022-12-31T00:00:00.000Z\",\"discretionary\":true}],\"assignedUserIds\":[277308417,277313986,277317164,277325186],\"discretionaryLimit\":3,\"id\":234}}";

        String orgID = (String) variableContext.getScenarioContext("ORGID");
        //v2/leaveManagement/holidays/organisations/776/leaveCycles/3926/holidayPolicies/234

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.saams.api.url") +"/v2/leaveManagement/holidays/organisations/"+orgID+"/leaveCycles/3926/holidayPolicies/234");

        ApiHelper.genericResponseValidation(response, "API - RESTORE DEFAULT HOLIDAY POLICY SETTINGS: ");
    }

}
