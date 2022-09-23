package com.spintly.api.services;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverContext;
import com.spintly.base.core.VariableContext;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import com.spintly.base.utilities.RandomDataGenerator;
import io.cucumber.datatable.DataTable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"All"})
public class UserManagementService extends DriverContext {
    private static LogUtility logger = new LogUtility(ShiftManagementService.class);
    ApiUtility apiUtility = new ApiUtility();
    RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    private int reporteeCount = 1;

    public void createUser(@NotNull DataTable data) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plusDays(30);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String email = dataMap.get("Email").trim();
        String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
        String gps = dataMap.get("GPS Enabled").trim();
        String mobile = dataMap.get("Mobile Enabled").trim();
        String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
        String probabationPeriod = dataMap.get("Probation Period Days").trim();
        String joining = "";
        String joiningDate = "";
        try {
            joining = dataMap.get("Joining").trim();
        } catch (Exception ex) {
        }
        switch (joining.toLowerCase()) {
            case "yesterday":
                joiningDate = dtf.format(now.plusDays(-1));
                break;
            case "daybeforeyesterday":
                joiningDate = dtf.format(now.plusDays(-2));
                break;
            case "firstdayofmonth":
                joiningDate = dtf.format(now.plusDays(-(now.getDayOfMonth() - 1)));
                break;
            case "today":
            case "":
            default:
                joiningDate = dtf.format(now);
                break;
        }

        String gender = dataMap.get("Gender").trim();
        String accessPoints = PropertyUtility.getDataProperties("accesspoints");
        if (accessPoints == "")
            accessPoints = "\"accessPoints\":[" + "" + "],";
        else
            accessPoints = "\"accessPoints\":[" + accessPoints + "],";


        String terms = getCustomAttributes();
        String role = PropertyUtility.getDataProperties("role.endUser");
        variableContext.setScenarioContext("Role", dataMap.get("Role").trim());
        switch (dataMap.get("Role").trim().toLowerCase()) {
            case "spintly user":
                role = PropertyUtility.getDataProperties("role.endUser");
                break;
            case "manager":
                role = role + "," + PropertyUtility.getDataProperties("role.manager");
                break;
            case "administrator":
                role = role + "," + PropertyUtility.getDataProperties("role.admin");
                break;
            case "front desk person / security":
                role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                break;
        }

        String reportingTo = getUserID(dataMap.get("Reporting Phone"));
        String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";
        String name = "Tom APIUser" + randomDataGenerator.getData("{RANDOM_STRING}");
        String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

        String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + ",\"createdAt\":\"" + joiningDate + "\"}]}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
        ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);
        String userID = getUserID(phone);
        variableContext.setScenarioContext("NEWUSERPHONE", phone);
        variableContext.setScenarioContext("NEWUSERNAME", name);
        variableContext.setScenarioContext("NEWUSERCODE", employeeCode);
        variableContext.setScenarioContext("NEWUSERID", userID);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "TRUE");
        variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
        variableContext.setScenarioContext("REPORTINGID", reportingTo);
        variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));

        ResultManager.pass("I create new user ", "Created new user with details : "+name +" | "+ phone , false);
    }

    public void createUsersForAccessManagement(DataTable data) throws ParseException {
        String terms = getAttributeForAccessManagement();

        for(int i = 0; i< 2; i++){
            Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime accessExpiry = now.plusDays(30);
            DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String email = dataMap.get("Email").trim();
            String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
            String gps = dataMap.get("GPS Enabled").trim();
            String mobile = dataMap.get("Mobile Enabled").trim();
            String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
            String probabationPeriod = dataMap.get("Probation Period Days").trim();
            String ap = dataMap.get("ap").trim();
            String joining = "";
            String joiningDate = "";
            try {
                joining = dataMap.get("Joining").trim();
            } catch (Exception ex) {
            }
            switch (joining.toLowerCase()) {
                case "yesterday":
                    joiningDate = dtf.format(now.plusDays(-1));
                    break;
                case "daybeforeyesterday":
                    joiningDate = dtf.format(now.plusDays(-2));
                    break;
                case "firstdayofmonth":
                    joiningDate = dtf.format(now.plusDays(-(now.getDayOfMonth() - 1)));
                    break;
                case "today":
                case "":
                default:
                    joiningDate = dtf.format(now);
                    break;
            }

            String gender = dataMap.get("Gender").trim();
            String accessPoints = PropertyUtility.getDataProperties("accesspoints");
            if (accessPoints == "")
                accessPoints = "\"accessPoints\":[" + "" + "],";
            else if(!(ap.equalsIgnoreCase(""))){
                accessPoints = "\"accessPoints\":[" + ap + "],";
            }
            else
                accessPoints = "\"accessPoints\":[" + accessPoints + "],";

            String role = "6";
            variableContext.setScenarioContext("Role", dataMap.get("Role").trim());
            switch (dataMap.get("Role").trim().toLowerCase()) {
                case "spintly user":
                    role = "6";
                    break;
                case "manager":
                    role = role + "," + "5";
                    break;
                case "administrator":
                    role = role + "," + "4";
                    break;
                case "front desk person / security":
                    role = role + "," + "2011";
                    break;
            }

            String reportingTo = getUserID(dataMap.get("Reporting Phone"));
            String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";
            String name = "Tom APIUser" + randomDataGenerator.getData("{RANDOM_STRING}");
            String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

            String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + ",\"createdAt\":\"" + joiningDate + "\"}]}";
            String token = apiUtility.getTokenFromLocalStorage();
            Response response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
            ApiHelper.genericResponseValidation(response, "API - CREATE USER For Access Management Organisation " + email + " - " + phone);
            String userID = getUserID(phone);
            variableContext.setScenarioContext("NEWUSERPHONE"+i, phone);
            variableContext.setScenarioContext("NEWUSERNAME"+i, name);
            variableContext.setScenarioContext("NEWUSERCODE"+i, employeeCode);
            variableContext.setScenarioContext("NEWUSERID"+i, userID);
            variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
            variableContext.setScenarioContext("NEWMULTUSERCLEANUPREQUIRED", "TRUE");
            variableContext.setScenarioContext("REPORTINGID", reportingTo);
            variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));

            ResultManager.pass("I create new user for Access Management Org ", "Created new user with details : "+name +" | "+ phone , false);
        }
    }

    public void createSingleUserForAccessManagement(DataTable data) throws ParseException {
            String terms = getAttributeForAccessManagement();

            Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime accessExpiry = now.plusDays(30);
            DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String email = dataMap.get("Email").trim();
            String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
            String gps = dataMap.get("GPS Enabled").trim();
            String mobile = dataMap.get("Mobile Enabled").trim();
            String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
            String probabationPeriod = dataMap.get("Probation Period Days").trim();
            String ap = dataMap.get("ap").trim();
            String joining = "";
            String joiningDate = "";
            try {
                joining = dataMap.get("Joining").trim();
            } catch (Exception ex) {
            }
            switch (joining.toLowerCase()) {
                case "yesterday":
                    joiningDate = dtf.format(now.plusDays(-1));
                    break;
                case "daybeforeyesterday":
                    joiningDate = dtf.format(now.plusDays(-2));
                    break;
                case "firstdayofmonth":
                    joiningDate = dtf.format(now.plusDays(-(now.getDayOfMonth() - 1)));
                    break;
                case "today":
                case "":
                default:
                    joiningDate = dtf.format(now);
                    break;
            }

            String gender = dataMap.get("Gender").trim();
            String accessPoints = PropertyUtility.getDataProperties("accesspoints");
            if (accessPoints == "")
                accessPoints = "\"accessPoints\":[" + "" + "],";
            else if(!(ap.equalsIgnoreCase(""))){
                accessPoints = "\"accessPoints\":[" + ap + "],";
            }
            else
                accessPoints = "\"accessPoints\":[" + accessPoints + "],";


            String role = "6";
            variableContext.setScenarioContext("Role", dataMap.get("Role").trim());
            switch (dataMap.get("Role").trim().toLowerCase()) {
                case "spintly user":
                    role = "6";
                    break;
                case "manager":
                    role = role + "," + "5";
                    break;
                case "administrator":
                    role = role + "," + "4";
                    break;
                case "front desk person / security":
                    role = role + "," + "2011";
                    break;
            }

            String reportingTo = getUserID(dataMap.get("Reporting Phone"));
            String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";
            String name = "Tom APIUser" + randomDataGenerator.getData("{RANDOM_STRING}");
            String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

            String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + ",\"createdAt\":\"" + joiningDate + "\"}]}";
            String token = apiUtility.getTokenFromLocalStorage();
            Response response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
            ApiHelper.genericResponseValidation(response, "API - CREATE USER For Access Management Organisation " + email + " - " + phone);
            String userID = getUserID(phone);
            variableContext.setScenarioContext("NEWUSERPHONE", phone);
            variableContext.setScenarioContext("NEWUSERNAME", name);
            variableContext.setScenarioContext("NEWUSERCODE", employeeCode);
            variableContext.setScenarioContext("NEWUSERID", userID);
            variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
            variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "TRUE");
            variableContext.setScenarioContext("REPORTINGID", reportingTo);
            variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));

            ResultManager.pass("I create new user for Access Management Org ", "Created new user with details : "+name +" | "+ phone , false);

        }

    public String getAttributeForAccessManagement() throws ParseException{
        String token = apiUtility.getTokenFromLocalStorage();
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/attributes");
        ApiHelper.genericResponseValidation(response, "API - GET ATTRIBUTE FOR ACCESS MANAGEMENT");

        JsonPath jsonPathEvaluator = response.jsonPath();

        String name = jsonPathEvaluator.get("message.attributes[0].attributeName").toString();
        String value = jsonPathEvaluator.getString("message.attributes[0].terms[0].name");
        variableContext.setScenarioContext("ACCESSATTRIBUTE",name);
        variableContext.setScenarioContext("ACCESSATTRIBUTEVALUE",value);
        List<String> names = jsonPathEvaluator.getList("message.attributes.attributeName");

        variableContext.setScenarioContext("LISTOFATTRIBUTENAMES",names);

        return jsonPathEvaluator.get("message.attributes[0].terms[0].id").toString();
    }

    public void createUserWithoutCustomAttributes(DataTable data) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plusDays(30);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String email = dataMap.get("Email").trim();
        String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
        String gps = dataMap.get("GPS Enabled").trim();
        String mobile = dataMap.get("Mobile Enabled").trim();
        String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
        String probabationPeriod = dataMap.get("Probation Period Days").trim();
        String joining = "";
        String joiningDate = "";
        try {
            joining = dataMap.get("Joining").trim();
        } catch (Exception ex) {
        }
        switch (joining.toLowerCase()) {
            case "yesterday":
                joiningDate = dtf.format(now.plusDays(-1));
                break;
            case "daybeforeyesterday":
                joiningDate = dtf.format(now.plusDays(-2));
                break;
            case "firstdayofmonth":
                joiningDate = dtf.format(now.plusDays(-(now.getDayOfMonth() - 1)));
                break;
            case "today":
            case "":
            default:
                joiningDate = dtf.format(now);
                break;
        }

        String gender = dataMap.get("Gender").trim();
        String accessPoints = PropertyUtility.getDataProperties("accesspoints");
        if (accessPoints == "")
            accessPoints = "\"accessPoints\":[" + "" + "],";
        else
            accessPoints = "\"accessPoints\":[" + accessPoints + "],";


        String terms = "[]";
        String role = PropertyUtility.getDataProperties("role.endUser");
        variableContext.setScenarioContext("Role", dataMap.get("Role").trim());
        switch (dataMap.get("Role").trim().toLowerCase()) {
            case "spintly user":
                role = PropertyUtility.getDataProperties("role.endUser");
                break;
            case "manager":
                role = role + "," + PropertyUtility.getDataProperties("role.manager");
                break;
            case "administrator":
                role = role + "," + PropertyUtility.getDataProperties("role.admin");
                break;
            case "front desk person / security":
                role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                break;
        }

        String reportingTo = getUserID(dataMap.get("Reporting Phone"));
        String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";
        String name = "Tom APIUser" + randomDataGenerator.getData("{RANDOM_STRING}");
        String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

        String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":" + terms + "," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + ",\"createdAt\":\"" + joiningDate + "\"}]}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
        ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);
        String userID = getUserID(phone);
        variableContext.setScenarioContext("NEWUSERPHONE", phone);
        variableContext.setScenarioContext("NEWUSERNAME", name);
        variableContext.setScenarioContext("NEWUSERCODE", employeeCode);
        variableContext.setScenarioContext("NEWUSERID", userID);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "TRUE");
        variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
        variableContext.setScenarioContext("REPORTINGID", reportingTo);
        variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));

        ResultManager.pass("I create new user ", "Created new user with details : "+name +" | "+ phone , false);
    }


    public void createAccessExpiredUser(DataTable data) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.minusDays(30);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String email = dataMap.get("Email").trim();
        String employeeCode = "";
        String gps = dataMap.get("GPS Enabled").trim();
        String mobile = dataMap.get("Mobile Enabled").trim();
        String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
        String probabationPeriod = dataMap.get("Probation Period Days").trim();
        String joiningDate = dtf.format(now);
        String gender = dataMap.get("Gender").trim();
        String accessPoints = PropertyUtility.getDataProperties("accesspoints");
        if (accessPoints == "")
            accessPoints = "\"accessPoints\":[" + "" + "],";

        accessPoints = "\"accessPoints\":[" + accessPoints + "],";

        String terms = getCustomAttributes();
        String role = PropertyUtility.getDataProperties("role.endUser");
        variableContext.setScenarioContext("Role", dataMap.get("Role").trim());
        switch (dataMap.get("Role").trim().toLowerCase()) {
            case "spintly user":
                role = PropertyUtility.getDataProperties("role.endUser");
                break;
            case "manager":
                role = role + "," + PropertyUtility.getDataProperties("role.manager");
                break;
            case "administrator":
                role = role + "," + PropertyUtility.getDataProperties("role.admin");
                break;
            case "front desk person / security":
                role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                break;
        }

        String reportingTo = getUserID(dataMap.get("Reporting Phone"));
        String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";
        String name = "Tom APIUser" + randomDataGenerator.getData("{RANDOM_STRING}");
        String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

        String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + "}]}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
        ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);
        String userID = getUserID(phone);
        variableContext.setScenarioContext("NEWUSERPHONE", phone);
        variableContext.setScenarioContext("NEWUSERNAME", name);
        variableContext.setScenarioContext("NEWUSERID", userID);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "TRUE");
        variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
        variableContext.setScenarioContext("REPORTINGID", reportingTo);
        variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));

    }

    public void createUserForEditing(DataTable data) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plusDays(30);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";

        String email = dataMap.get("Email").trim();
        String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
        String gps = dataMap.get("GPS Enabled").trim();
        String mobile = dataMap.get("Mobile Enabled").trim();
        String probabationPeriod = "";
        String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
        probabationPeriod = dataMap.get("Probation Period Days").trim();
        String joiningDate = dtf.format(now);
        String gender = dataMap.get("Gender").trim();

        String accessPoints = dataMap.get("Access Points").trim();

        if (accessPoints.equalsIgnoreCase("none")) {

            accessPoints = "\"accessPoints\":[" + "" + "],";

        } else if (accessPoints.equalsIgnoreCase("all")) {

            List<Integer> accessPointIDs = this.accessPointsID();
            String idString = "";
            for (int i = 0; i < accessPointIDs.size(); i++) {

                if (i == accessPointIDs.size() - 1) {
                    idString = idString + String.valueOf(accessPointIDs.get(i));
                } else {
                    idString = idString + String.valueOf(accessPointIDs.get(i)) + ",";
                }
            }

            accessPoints = "\"accessPoints\":[" + idString + "],";
        } else {

            accessPoints = "\"accessPoints\":[" + accessPoints + "],";
        }


        String terms = getCustomAttributes();
        String role = PropertyUtility.getDataProperties("role.endUser");
        variableContext.setScenarioContext("Role", dataMap.get("Role").trim());
        switch (dataMap.get("Role").trim().toLowerCase()) {
            case "spintly user":
                role = PropertyUtility.getDataProperties("role.endUser");
                break;
            case "manager":
                role = role + "," + PropertyUtility.getDataProperties("role.manager");
                break;
            case "administrator":
                role = role + "," + PropertyUtility.getDataProperties("role.admin");
                break;
            case "front desk person / security":
                role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                break;
        }

        String reportingTo = getUserID(dataMap.get("Reporting Phone"));

        String name = "MultUsr" + randomDataGenerator.getData("{RANDOM_STRING}");
        String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

        String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + "}]}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
        ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);
        String userID = getUserID(phone);
        variableContext.setScenarioContext("NEWEMAIL", email);
        variableContext.setScenarioContext("NEWUSERPHONE", phone);
        variableContext.setScenarioContext("NEWUSERNAME", name);
        variableContext.setScenarioContext("NEWUSERID", userID);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "TRUE");
        variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
        variableContext.setScenarioContext("REPORTINGID", reportingTo);
        variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));

    }

    public void createReportees(DataTable data, String reportingManagerPhone) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plusDays(30);
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String email = dataMap.get("Email").trim();
        String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
        String gps = dataMap.get("GPS Enabled").trim();
        String mobile = dataMap.get("Mobile Enabled").trim();
        String probabationPeriod = "";
        String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
        probabationPeriod = dataMap.get("Probation Period Days").trim();
        String joiningDate = dtf.format(now);
        String gender = dataMap.get("Gender").trim();

        String accessPoints = dataMap.get("Access Points").trim();

        if (accessPoints.equalsIgnoreCase("none")) {

            accessPoints = "\"accessPoints\":[" + "" + "],";

        } else if (accessPoints.equalsIgnoreCase("all")) {

            List<Integer> accessPointIDs = this.accessPointsID();
            String idString = "";
            for (int i = 0; i < accessPointIDs.size(); i++) {

                if (i == accessPointIDs.size() - 1) {
                    idString = idString + String.valueOf(accessPointIDs.get(i));
                } else {
                    idString = idString + String.valueOf(accessPointIDs.get(i)) + ",";
                }
            }

            accessPoints = "\"accessPoints\":[" + idString + "],";
        } else {

            accessPoints = "\"accessPoints\":[" + accessPoints + "],";
        }

        String terms = getCustomAttributes();
        String role = PropertyUtility.getDataProperties("role.endUser");
        variableContext.setScenarioContext("Role", dataMap.get("Role").trim());
        switch (dataMap.get("Role").trim().toLowerCase()) {
            case "spintly user":
                role = PropertyUtility.getDataProperties("role.endUser");
                break;
            case "manager":
                role = role + "," + PropertyUtility.getDataProperties("role.manager");
                break;
            case "administrator":
                role = role + "," + PropertyUtility.getDataProperties("role.admin");
                break;
            case "front desk person / security":
                role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                break;
        }

        String reportingTo = getUserID(reportingManagerPhone);
        String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";
        String name = "Tom APIUser" + randomDataGenerator.getData("{RANDOM_STRING}");
        String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

        String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + "}]}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
        ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);
        String userID = getUserID(phone);

        variableContext.setScenarioContext("REPORTEE" + reporteeCount, name);

        variableContext.setScenarioContext("REPORTEEID" + reporteeCount, userID);

        variableContext.setScenarioContext("REPORTEEPHONE" + reporteeCount, phone);

        variableContext.setScenarioContext("DELETEREPORTEE", "TRUE");
        reporteeCount++;
    }

    //Delete reportees
    public void deleteReportees() throws ParseException {
        for (int i = 0; i < reporteeCount; i++) {
            String phone = (String) variableContext.getScenarioContext("REPORTEEPHONE" + reporteeCount);
            String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
            String token = apiUtility.getTokenFromLocalStorage();
            Response response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
            ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
            JsonPath jsonPathEvaluator = response.jsonPath();
            String userID = jsonPathEvaluator.get("message.users.id").toString();
            jsonString = "{\"replaceManager\":[],\"userIds\":[" + userID + "]}";
            response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/delete");
            ApiHelper.genericResponseValidation(response, "API - DELETE REPORTEE " + phone);
        }

    }


    public String getCustomAttributes() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");
        ApiHelper.genericResponseValidation(response, "API - GET Attributes ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        ArrayList<ArrayList> array = jsonPathEvaluator.get("message.attributes.terms.id");
        ArrayList deparment = array.get(2);
        ArrayList location = array.get(1);
        ArrayList site = array.get(0);
        String customAttribute = deparment.get(0) + "," + location.get(0) + "," + site.get(0);
        array = jsonPathEvaluator.get("message.attributes.terms.name");
        deparment = array.get(2);
        location = array.get(1);
        site = array.get(0);
        variableContext.setScenarioContext("DEPT", deparment.get(0));
        variableContext.setScenarioContext("LOC", location.get(0));
        variableContext.setScenarioContext("SITE", site.get(0));
        return customAttribute;
    }

    public String getUserID(String phone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String orgID = (String) variableContext.getScenarioContext("ORGID");
        String token = "";

        String loggedIN = (String) variableContext.getScenarioContext("LOGGEDIN");

        if(loggedIN.equalsIgnoreCase("FD")){
            token = (String) variableContext.getScenarioContext("ADMINTOKEN");
        }else {
            token = apiUtility.getTokenFromLocalStorage();
        }

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" +orgID+"/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        return jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");

    }

    public String getUserName(String phone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        variableContext.setScenarioContext("USERNAME", jsonPathEvaluator.get("users.name"));
        return jsonPathEvaluator.get("message.users.name").toString().replace("[", "").replace("]", "");

    }

    public String getActiveUsersCount() throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"createdOn\":null,\"userType\":[\"active\"],\"accessExpiresAt\":null,\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET ACTIVE USERS COUNT ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        variableContext.setScenarioContext("ACTIVEUSERS", jsonPathEvaluator.get("message.pagination.total"));
        ResultManager.pass("I get active users count ", "I got active users count", false);

        return jsonPathEvaluator.get("message.pagination.total").toString();
    }

    public String getDeactiveUsersCount() throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"createdOn\":null,\"userType\":[\"inactive\"],\"accessExpiresAt\":null,\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET ACTIVE USERS COUNT ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        variableContext.setScenarioContext("DEACTIVEUSERS", jsonPathEvaluator.get("message.pagination.total"));
        ResultManager.pass("I get inactive users count ", "I got inactive users count", false);
        return jsonPathEvaluator.get("message.pagination.total").toString();
    }

    public void deleteUser(String phone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString();
        jsonString = "{\"replaceManager\":[],\"userIds\":[" + userID + "]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/delete");

        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "FALSE");

        ApiHelper.genericResponseValidation(response, "API - DELETE ACTIVE USER " + phone);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "FALSE");
        ResultManager.pass("I delete user ", "I delete user " + phone, false);

    }

    public void cleanupUser(String phone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString();
        jsonString = "{\"replaceManager\":[],\"userIds\":[" + userID + "]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/delete");
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "FALSE");
    }

//    public String countOfUsersInOrg(String type){
//        String orgID = (String) variableContext.getScenarioContext("ORGID");
//        String token = apiUtility.getTokenFromLocalStorage();
//        String jsonString = "{}";
//
//        //v2/organisationManagement/organisations/5/statistics
//        Response response = ApiHelper.givenRequestSpecification()
//                .header("authorization", token)
//                .body(jsonString)
//                .when().redirects().follow(false).
//                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/"+orgID+"/statistics");
//
//
//        JsonPath jsonPathEvaluator = response.jsonPath();
//
//        String message = "";
//        switch (type){
//
//            case "active":
//                message = jsonPathEvaluator.get("message.statistics.activeUsers").toString();
//                break;
//            case "deactivated":
//                int total = jsonPathEvaluator.get("message.statistics.totalUsers");
//                int active = jsonPathEvaluator.get("message.statistics.activeUsers");
//                message = String.valueOf(total-active);
//                break;
//        }
//        return message;
//    }

    public void deactivateUser(String phone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString();
        jsonString = "{\"userIds\":[" + userID + "],\"replaceManager\":[]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/deactivate");
        ApiHelper.genericResponseValidation(response, "API - DEACTIVATE USER " + phone);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "FALSE");
        variableContext.setScenarioContext("DELETEDEACTIVATED", "TRUE");
        ResultManager.pass("I deactivate user ", "I deactivated user " + phone, false);
    }

    //Function to delete deactivated user
    public void deleteDeactivatedUser(String phone) throws ParseException {

        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":100},\"filters\":{\"userType\":[\"inactive\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET DEACTIVATED USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();

        String userID = jsonPathEvaluator.get("message.users.id").toString();

        jsonString = "{\"replaceManager\":[],\"userIds\":[" + userID + "]}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/delete");
        ApiHelper.genericResponseValidation(response, "API - DELETE DEACTIVATED USER " + phone);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "FALSE");
        //variableContext.setScenarioContext("DELETEDEACTIVATED", "TRUE");
    }


    public void setRoleForUser(DataTable data) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        String role = dataMap.get("Role").trim();
        String phone = dataMap.get("Phone").trim();

        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID);
        ApiHelper.genericResponseValidation(response, "API - GET USER DATA of phone " + phone);
        jsonPathEvaluator = response.jsonPath();
        String message = jsonPathEvaluator.get("message").toString().replace("=", ":");

        JSONObject jObject = new JSONObject(message);
        jObject.getJSONObject("roles").remove("name");
        jObject.getJSONObject("roles").remove("id");
        jObject.getJSONObject("reportingTo").remove("name");
        jObject.getJSONObject("reportingTo").remove("id");
        JSONArray jObject1 = jObject.getJSONArray("attributes");
        String termID = "";
        for (int i = 0; i < jObject1.length(); i++) {
            JSONArray terms = jObject1.getJSONObject(i).getJSONArray("terms");
            for (int j = 0; j < terms.length(); j++) {
                if (j == terms.length())
                    termID = termID + jObject.getJSONObject("id");
                else
                    termID = termID + jObject.getJSONObject("id") + ",";

            }

        }
        System.out.println(termID);

        jsonString = "{\"user\":{\"id\":" + userID + ",\"accessorId\":19083,\"name\":\"Krish\",\"email\":\"krishna.hoderker.fl@gmail.com\",\"phone\":\"+919284174823\",\"roles\":[5,6,2011],\"reportees\":[],\"reportingTo\":277317210,\"createdAt\":\"2022-01-27T14:55:11.669Z\",\"isSignedUp\":true,\"cardAssigned\":false,\"cardId\":null,\"accessExpiresAt\":null,\"accessExpired\":false,\"approveDeviceLock\":false,\"attributes\":[{\"id\":223,\"attributeName\":\"Department\",\"terms\":[{\"id\":999,\"name\":\"Firmware\"}]},{\"id\":401,\"attributeName\":\"location\",\"terms\":[{\"id\":2004,\"name\":\"Goa\"}]},{\"id\":438,\"attributeName\":\"Sites\",\"terms\":[{\"id\":2102,\"name\":\"Lake Plaza\"}]}],\"deactivatedOn\":null,\"employeeCode\":null,\"gps\":false,\"probationPeriod\":0,\"gender\":\"male\",\"joiningDate\":\"2043-01-27\",\"terms\":[999,2004,2102],\"mobile\":false}}";
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/deactivate");
        ApiHelper.genericResponseValidation(response, "API - Set Role USER " + phone);
        variableContext.setScenarioContext("NEWUSERCLEANUPREQUIRED", "FALSE");
    }

    //Service to grab exisitng valid phone number
    public String getValidPhoneNumber() throws ParseException {
        String perPage = getActiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"createdOn\":null,\"userType\":[\"active\"],\"accessExpiresAt\":null,\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        String phone = "";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Phone Numbers");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> phoneNumbers = jsonPathEvaluator.get("message.users.phone");

        for (int i = 0; i < phoneNumbers.size(); i++) {
            // Variable to store the first digit and country code of the Number
            String firstDigit = phoneNumbers.get(i).substring(0, 4);

            // Checking if valid phone number conditions are met
            if (phoneNumbers.get(i) != null && phoneNumbers.get(i).length() == 13 && ((firstDigit.equalsIgnoreCase("+919") || firstDigit.equalsIgnoreCase("+918") || firstDigit.equalsIgnoreCase("+917")))) {
                // Getting the first valid number and breaking out of the loop
                phone = phoneNumbers.get(i).substring(3, 13);
                break;
            }
        }

        variableContext.setScenarioContext("validExistingPhoneNumber", phone);
        return phone;
    }


    //Service to grab existing valid employee code
    public String getValidEmployeeCode() throws ParseException {
        String perPage = getActiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"createdOn\":null,\"userType\":[\"active\"],\"accessExpiresAt\":null,\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        String code = "";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Phone Numbers");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> employeeCode = jsonPathEvaluator.get("message.users.employeeCode");

        for (int i = 0; i < employeeCode.size(); i++) {
            //checking if valid employee code conditions are met
            if (employeeCode.get(i) != null && employeeCode.get(i).length() > 0 && !(employeeCode.get(i).equalsIgnoreCase("-"))) {
                //Getting the first valid employee code and breaking out of the loop
                code = employeeCode.get(i);
                break;
            }
        }

        variableContext.setScenarioContext("validEmpCode", code);
        return code;
    }


    //Find all managers in the organisation
    public List<String> managersInOrganization() throws ParseException {
        String perPage = getActiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"isSignedUp\":null,\"roles\":[\"386\"]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> managers = new ArrayList<>();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Managers");
        JsonPath jsonPathEvaluator = response.jsonPath();

        managers = jsonPathEvaluator.get("message.users.name");
        return managers.stream().map(String::toLowerCase).sorted().collect(Collectors.toList());
    }

    //Return all active users in the organisation
    public List<String> getAllActiveUsersInOrg() throws ParseException {
        String perPage = getActiveUsersCount();
        //For Page 1
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"createdOn\":null,\"userType\":[\"active\"],\"accessExpiresAt\":null,\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> users = new ArrayList<>();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Active users");
        JsonPath jsonPathEvaluator = response.jsonPath();

        users = jsonPathEvaluator.get("message.users.name");

        return users.stream().sorted().map(String::toLowerCase).collect(Collectors.toList());
    }

    //Return all valid access users in the organisation
    public List<String> getAllValidAccessUsersInOrg() throws ParseException {
        //String perPage = getActiveUsersCount();
        //For Page 1
        String jsonString = "{\"pagination\":{\"page\":-1,\"perPage\":-1},\"filters\":{\"createdOn\":null,\"userType\":[\"active\"],\"accessExpiresAt\":null,\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> users = new ArrayList<>();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Valid Access users");
        JsonPath jsonPathEvaluator = response.jsonPath();

        ArrayList<LinkedHashMap<String, String>> userDetails = jsonPathEvaluator.get("message.users");

        users = userDetails.stream()
                .filter(x -> String.valueOf(x.get("accessExpired")).equalsIgnoreCase("false"))
                .map(x -> x.get("name"))
                .sorted()
                .map(String::toLowerCase)
                .collect(Collectors.toList());


//        return users.stream().sorted().map(String::toLowerCase).collect(Collectors.toList());
        return users;
    }

    //Return all deactivated users in the organisation
    public List<String> getAllDeactivatedUsersInOrg() throws ParseException {
        String perPage = getDeactiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"createdOn\":null,\"userType\":[\"inactive\"],\"accessExpiresAt\":null,\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Deactivated Users");
        JsonPath jsonPathEvaluator = response.jsonPath();

        List<String> users1 = jsonPathEvaluator.get("message.users.name");

        return users1.stream().map(String::toLowerCase).sorted().collect(Collectors.toList());
    }

    //Get List of signup complete users
    public List<String> getSignupCompleteUsers() throws ParseException {
        String perPage = getActiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"isSignedUp\":true}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> signedUp = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch SignupComplete Users");
        JsonPath jsonPathEvaluator = response.jsonPath();

        //For Page 1
        signedUp = jsonPathEvaluator.get("message.users.name");

        return signedUp.stream().collect(Collectors.toList());
    }

    public List<String> deactivatedManagersInOrganization() throws ParseException {
        String perPage = getDeactiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"inactive\"],\"terms\":[],\"isSignedUp\":null,\"roles\":[\"386\"]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> deactivatedManagers = new ArrayList<>();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Deactivated Managers");
        JsonPath jsonPathEvaluator = response.jsonPath();

        deactivatedManagers = jsonPathEvaluator.get("message.users.name");

        return deactivatedManagers.stream().map(String::toLowerCase).sorted().collect(Collectors.toList());
    }


    public Map<String, List<String>> customAttributesMap() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Map<String, List<String>> h = new LinkedHashMap<String, List<String>>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");
        ApiHelper.genericResponseValidation(response, "API - GET Custom Attributes Map ");
        JsonPath jsonPathEvaluator = response.jsonPath();

        String responseBody = response.getBody().asString();
        System.out.println(responseBody);

        List<Object> attributes = jsonPathEvaluator.get("message.attributes");

        for (int i = 0; i < attributes.size(); i++) {
            List<String> values = jsonPathEvaluator.get("message.attributes[" + i + "].terms.name");

            String name = jsonPathEvaluator.get("message.attributes[" + i + "].attributeName");
            System.out.println(values);

            h.put(name, values.stream().sorted().collect(Collectors.toList()));
        }

        return h;
    }

    //Add Custom attribute
    public void addCustomAttribute() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String jsonString = "[{\"attributeName\":\"testCA\",\"terms\":[\"sid1\",\"sid2\"]}]";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");

        ApiHelper.genericResponseValidation(response, "API - Add Custom attribute ");
        variableContext.setScenarioContext("NewCA", "testCA");

    }

    //Delete custom Attribute
    public void deleteCustomAttribute(String ca) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes/" + getCustomAttributeID(ca));
        ApiHelper.genericResponseValidation(response, "API - DELETE Custom Attributes");
        JsonPath jsonPathEvaluator = response.jsonPath();
    }

    //Add the deleted fixed Custom attribute back again
    public void addFixedCustomAttribute() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String jsonString = "[{\"attributeName\":\"Site\",\"terms\":[\"Lake Plaza\",\"Ocean Plaza\"]}]";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");

        ApiHelper.genericResponseValidation(response, "API - Add Custom attribute ");
    }

    //Delete a fixed custom Attribute attribute everytime
    public void deleteFixedCustomAttribute() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                delete(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes/" + getCustomAttributeID("Site"));
        ApiHelper.genericResponseValidation(response, "API - DELETE Fixed Custom Attribute");
        JsonPath jsonPathEvaluator = response.jsonPath();

        variableContext.setScenarioContext("FixedCAName", "Site");
        variableContext.setScenarioContext("AddFixedCA", "TRUE");
    }

    //Get custom attribute id
    public String getCustomAttributeID(String attributeName) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String id = "abcd";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");

        ApiHelper.genericResponseValidation(response, "API - GET Custom Attribute ID ");
        JsonPath jsonPathEvaluator = response.jsonPath();

        ArrayList<LinkedHashMap<String, String>> attributes = jsonPathEvaluator.get("message.attributes");
        for (LinkedHashMap attribute : attributes)
            if (attribute.get("attributeName").equals(attributeName)) {
                id = attribute.get("id").toString();
                break;
            }
        return id;
    }

    public String getCustomAttributeValueID(String attributeName, String valueName) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String id = "";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");

        ApiHelper.genericResponseValidation(response, "API - GET Custom Attribute ID ");
        JsonPath jsonPathEvaluator = response.jsonPath();

        List<Object> attributes = jsonPathEvaluator.get("message.attributes");
        for(int i=0;i<attributes.size();i++){
            String attName = jsonPathEvaluator.get("message.attributes["+i+"].attributeName");
            if(attName.equalsIgnoreCase(attributeName)){
                List<Object> values = jsonPathEvaluator.get("message.attributes["+i+"].terms");
                for(int j=0;j<values.size();j++){
                    String val = jsonPathEvaluator.get("message.attributes["+i+"].terms["+j+"].name");
                    if(val.equalsIgnoreCase(valueName)){
                        int idToReturn = jsonPathEvaluator.get("message.attributes["+i+"].terms["+j+"].id");

                        return String.valueOf(idToReturn);
                    }
                }
            }
        }
        return "";
    }

    //Return custom attribute values
    public List<String> returnCAValues(String name) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> values = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes");

        ApiHelper.genericResponseValidation(response, "API - GET Custom Attribute Values ");
        JsonPath jsonPathEvaluator = response.jsonPath();

        List<Object> attributes = jsonPathEvaluator.get("message.attributes");

        for (int i = 0; i < attributes.size(); i++) {
            if (jsonPathEvaluator.get("message.attributes[" + i + "].attributeName").toString().equalsIgnoreCase(name)) {
                values = jsonPathEvaluator.get("message.attributes[" + i + "].terms.name");
            }
        }
        return values;
    }


    //Return List of access points names in an organisation
    public List<String> accessPointsInOrg() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String jsonString = "{\"filters\":{\"sites\":null},\"pagination\":{\"perPage\":100,\"page\":1}}";
        List<String> accessPoints = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/accessPoint");


        ApiHelper.genericResponseValidation(response, "API - GET Access Points in organization ");
        JsonPath jsonPathEvaluator = response.jsonPath();

        accessPoints = jsonPathEvaluator.get("message.accessPoints.name");
        return accessPoints;
    }

    //Return List of access points IDs in an organisation
    public List<Integer> accessPointsID() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String jsonString = "{\"filters\":{\"sites\":null},\"pagination\":{\"perPage\":100,\"page\":1}}";
        List<Integer> accessPointIDs = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/accessPoint");

        ApiHelper.genericResponseValidation(response, "API - GET Access Points in organization ");
        JsonPath jsonPathEvaluator = response.jsonPath();

        accessPointIDs = jsonPathEvaluator.get("message.accessPoints.id");
        return accessPointIDs;
    }

    //Create users for bulk edit
    public void createUsersForBulkEditing(DataTable data, String tc, String toSkip) throws ParseException {
        Map<String, List<String>> h = new LinkedHashMap<String, List<String>>();

        for (int j = 0; j < 2; j++) {
            // Main list that stores all the user details
            List<String> userDetailsList = new ArrayList<String>();

            Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime accessExpiry = now.plusDays(30);
            DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            String email = dataMap.get("Email").trim() + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
            if (!(toSkip.equalsIgnoreCase("email"))) {
                userDetailsList.add(email.toLowerCase());
            }

            String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
            if (!(toSkip.equalsIgnoreCase("empcode"))) {
                userDetailsList.add(employeeCode.toLowerCase());
            }

            String gps = dataMap.get("GPS Enabled").trim();
            if (!(toSkip.equalsIgnoreCase("gps"))) {
                if (gps.equalsIgnoreCase("true")) {
                    userDetailsList.add("true");
                    userDetailsList.add("false");
                } else if (gps.equalsIgnoreCase("false")) {
                    userDetailsList.add("false");
                    userDetailsList.add("true");
                }
            }

            String mobile = dataMap.get("Mobile Enabled").trim();
            if (!(toSkip.equalsIgnoreCase("mobile"))) {
                if (mobile.equalsIgnoreCase("true")) {
                    userDetailsList.add("true");
                    userDetailsList.add("false");
                } else if (mobile.equalsIgnoreCase("false")) {
                    userDetailsList.add("false");
                    userDetailsList.add("true");
                }
            }

            String probabationPeriod = "";
            probabationPeriod = dataMap.get("Probation Period Days").trim();

            String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
            if (!(toSkip.equalsIgnoreCase("probation"))) {
                if (probationPeriodEnabled.equalsIgnoreCase("true")) {
                    userDetailsList.add("false");
                    userDetailsList.add("true");
                    userDetailsList.add(probabationPeriod);
                } else if (probationPeriodEnabled.equalsIgnoreCase("false")) {
                    userDetailsList.add("true");
                    userDetailsList.add("false");
                }
            }

            String joiningDate = dtf.format(now);
            if (!(toSkip.equalsIgnoreCase("joindate"))) {
                userDetailsList.add(joiningDate);
            }

            String gender = dataMap.get("Gender").trim();
            if (!(toSkip.equalsIgnoreCase("gender"))) {
                userDetailsList.add(gender.toLowerCase());
            }

            String accessPoints = dataMap.get("Access Points").trim();

            if (accessPoints.equalsIgnoreCase("none")) {

                accessPoints = "\"accessPoints\":[" + "" + "],";

            } else if (accessPoints.equalsIgnoreCase("all")) {

                List<Integer> accessPointIDs = this.accessPointsID();
                String idString = "";
                for (int i = 0; i < accessPointIDs.size(); i++) {

                    if (i == accessPointIDs.size() - 1) {
                        idString = idString + String.valueOf(accessPointIDs.get(i));
                    } else {
                        idString = idString + String.valueOf(accessPointIDs.get(i)) + ",";
                    }
                }

                accessPoints = "\"accessPoints\":[" + idString + "],";
            } else {

                accessPoints = "\"accessPoints\":[" + accessPoints + "],";
            }


            String terms = getCustomAttributes();
            if (!(toSkip.equalsIgnoreCase("customattributes"))) {
                userDetailsList.add("software");
                userDetailsList.add("bangalore");
                userDetailsList.add("lake plaza");
            }

            //String role = PropertyUtility.getDataProperties("role.endUser");

            String role = null;

            variableContext.setScenarioContext("Role", dataMap.get("Role").trim());

            String organisation = (String) variableContext.getScenarioContext("CHANGEORG");

            role = PropertyUtility.getDataProperties("role.endUser");
            switch (dataMap.get("Role").trim().toLowerCase()) {
                case "spintly user":
                    role = PropertyUtility.getDataProperties("role.endUser");
                    if (!(toSkip.equalsIgnoreCase("roles"))) {
                        userDetailsList.add("false");
                        userDetailsList.add("false");
                        userDetailsList.add("true");
                        userDetailsList.add("false");
                    }
                    break;
                case "manager":
                    role = role + "," + PropertyUtility.getDataProperties("role.manager");
                    if (!(toSkip.equalsIgnoreCase("roles"))) {
                        userDetailsList.add("false");
                        userDetailsList.add("true");
                        userDetailsList.add("true");
                        userDetailsList.add("false");
                    }
                    break;
                case "administrator":
                    role = role + "," + PropertyUtility.getDataProperties("role.admin");
                    if (!(toSkip.equalsIgnoreCase("roles"))) {
                        userDetailsList.add("true");
                        userDetailsList.add("false");
                        userDetailsList.add("true");
                        userDetailsList.add("false");
                    }
                    break;
                case "front desk person / security":
                    role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                    if (!(toSkip.equalsIgnoreCase("roles"))) {
                        userDetailsList.add("false");
                        userDetailsList.add("false");
                        userDetailsList.add("true");
                        userDetailsList.add("true");
                    }
                    break;
            }

            String reportingTo = getUserID(dataMap.get("Reporting Phone"));
            if (!(toSkip.equalsIgnoreCase("reportingmanager"))) {
                userDetailsList.add("krishna manager");
            }

            String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";
            if (!(toSkip.equalsIgnoreCase("accessexpirydate"))) {
                userDetailsList.add(accessExpiresAt.split(" ")[0].trim());
            }

            String name = "MultUsr" + tc + randomDataGenerator.getData("{RANDOM_STRING}");
            if (!(toSkip.equalsIgnoreCase("name"))) {
                userDetailsList.add(name.toLowerCase());
            }

            String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");
            if (!(toSkip.equalsIgnoreCase("phone"))) {
                userDetailsList.add(phone);
            }

            String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + "}]}";
            String token = apiUtility.getTokenFromLocalStorage();
            Response response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
            ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);

            String userID = getUserID(phone);

            variableContext.setScenarioContext("NEWEMAIL" + j, email);
            variableContext.setScenarioContext("NEWUSERPHONE" + j, phone);
            variableContext.setScenarioContext("NEWUSERNAME" + j, name);
            variableContext.setScenarioContext("NEWUSERID" + j, userID);
            variableContext.setScenarioContext("NEWMULTUSERCLEANUPREQUIRED", "TRUE");
            variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
            variableContext.setScenarioContext("REPORTINGID", reportingTo);
            variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));
            variableContext.setScenarioContext("USERKEYWORD", tc);

            h.put(name, userDetailsList);

        }

        System.out.println(h);

        variableContext.setScenarioContext("MULTUSERSMAP", h);

        /*
        -email
        -employeeCode
        -gps enable
        -gps disable
        -mobile enable
        -mobile disable
        -probation no
        -probation yes
        -join date
        -gender
        -CA dept
        -CA location
        -CA site
        -role spintly user
        -role manager
        -role admin
        -role frontdesk
        -Reporting manager
        -accessexpiry
        -name
        -phone

        */
    }


    public void
    createUsersWithCustomAttributes(DataTable data,String tc) throws ParseException {
        for (int j = 0; j < 2; j++) {
            // Main list that stores all the user details
            List<String> userDetailsList = new ArrayList<String>();

            Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime accessExpiry = now.plusDays(30);
            DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            String email = dataMap.get("Email").trim() + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
            String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
            String gps = dataMap.get("GPS Enabled").trim();
            String mobile = dataMap.get("Mobile Enabled").trim();
            String probabationPeriod = dataMap.get("Probation Period Days").trim();
            String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
            String joiningDate = dtf.format(now);
            String gender = dataMap.get("Gender").trim();
            String accessPoints = dataMap.get("Access Points").trim();

            if (accessPoints.equalsIgnoreCase("none")) {

                accessPoints = "\"accessPoints\":[" + "" + "],";

            } else if (accessPoints.equalsIgnoreCase("all")) {

                List<Integer> accessPointIDs = this.accessPointsID();
                String idString = "";
                for (int i = 0; i < accessPointIDs.size(); i++) {

                    if (i == accessPointIDs.size() - 1) {
                        idString = idString + String.valueOf(accessPointIDs.get(i));
                    } else {
                        idString = idString + String.valueOf(accessPointIDs.get(i)) + ",";
                    }
                }

                accessPoints = "\"accessPoints\":[" + idString + "],";
            } else {

                accessPoints = "\"accessPoints\":[" + accessPoints + "],";
            }


            String terms = getCustomAttributeValueID("Site","Lake Plaza");
            //getCustomAttributeValueID("Site","Lake Plaza");

            if(terms.equalsIgnoreCase("")){
                String newCaName = (String) variableContext.getScenarioContext("NewCA");
                terms = getCustomAttributeValueID(newCaName,"sid1");
            }

            if(terms.equalsIgnoreCase("newOrg")){
                String org = (String) variableContext.getScenarioContext("CHANGEORG");
                if(org.equalsIgnoreCase("CustomAttributes")){
                    terms = "2090";
                }
            }

            String role = PropertyUtility.getDataProperties("role.endUser");

            switch (dataMap.get("Role").trim().toLowerCase()) {
                case "spintly user":
                    role = PropertyUtility.getDataProperties("role.endUser");
                    break;
                case "manager":
                    role = role + "," + PropertyUtility.getDataProperties("role.manager");
                    break;
                case "administrator":
                    role = role + "," + PropertyUtility.getDataProperties("role.admin");
                    break;
                case "front desk person / security":
                    role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                    break;
            }

            String reportingTo = getUserID(dataMap.get("Reporting Phone"));
            String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";


            String name = tc+"MultUsr"+ randomDataGenerator.getData("{RANDOM_STRING}");
            String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

            String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":[" + terms + "]," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + "}]}";
            String token = apiUtility.getTokenFromLocalStorage();
            Response response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
            ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);

            String userID = getUserID(phone);

            variableContext.setScenarioContext("NEWEMAIL" + j, email);
            variableContext.setScenarioContext("NEWUSERPHONE" + j, phone);
            variableContext.setScenarioContext("NEWUSERNAME" + j, name);
            variableContext.setScenarioContext("NEWUSERID" + j, userID);
            variableContext.setScenarioContext("NEWMULTUSERCLEANUPREQUIRED", "TRUE");
            variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
            variableContext.setScenarioContext("REPORTINGID", reportingTo);
            variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));
            variableContext.setScenarioContext("USERKEYWORD", tc);
        }
    }

    public void createUsersWithoutCustomAttributes(DataTable data,String tc) throws ParseException {
        for (int j = 0; j < 2; j++) {
            // Main list that stores all the user details
            List<String> userDetailsList = new ArrayList<String>();

            Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime accessExpiry = now.plusDays(30);
            DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            String email = dataMap.get("Email").trim() + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
            String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
            String gps = dataMap.get("GPS Enabled").trim();
            String mobile = dataMap.get("Mobile Enabled").trim();
            String probabationPeriod = dataMap.get("Probation Period Days").trim();
            String probationPeriodEnabled = dataMap.get("Probation Period Enabled").trim();
            String joiningDate = dtf.format(now);
            String gender = dataMap.get("Gender").trim();
            String accessPoints = dataMap.get("Access Points").trim();

            if (accessPoints.equalsIgnoreCase("none")) {

                accessPoints = "\"accessPoints\":[" + "" + "],";

            } else if (accessPoints.equalsIgnoreCase("all")) {

                List<Integer> accessPointIDs = this.accessPointsID();
                String idString = "";
                for (int i = 0; i < accessPointIDs.size(); i++) {

                    if (i == accessPointIDs.size() - 1) {
                        idString = idString + String.valueOf(accessPointIDs.get(i));
                    } else {
                        idString = idString + String.valueOf(accessPointIDs.get(i)) + ",";
                    }
                }

                accessPoints = "\"accessPoints\":[" + idString + "],";
            } else {

                accessPoints = "\"accessPoints\":[" + accessPoints + "],";
            }


            String terms = "[]";

            String role = PropertyUtility.getDataProperties("role.endUser");

            switch (dataMap.get("Role").trim().toLowerCase()) {
                case "spintly user":
                    role = PropertyUtility.getDataProperties("role.endUser");
                    break;
                case "manager":
                    role = role + "," + PropertyUtility.getDataProperties("role.manager");
                    break;
                case "administrator":
                    role = role + "," + PropertyUtility.getDataProperties("role.admin");
                    break;
                case "front desk person / security":
                    role = role + "," + PropertyUtility.getDataProperties("role.frontdesk");
                    break;
            }

            String reportingTo = getUserID(dataMap.get("Reporting Phone"));
            String accessExpiresAt = dtf1.format(accessExpiry) + " +05:30";


            String name = tc+"MultUsr"+ randomDataGenerator.getData("{RANDOM_STRING}");
            String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

            String jsonString = "{\"users\":[{\"accessExpiresAt\":\"" + accessExpiresAt + "\",\"email\":\"" + email + "\",\"employeeCode\":\"" + employeeCode + "\",\"gps\":" + gps + ",\"name\":\"" + name + "\",\"phone\":\"+91" + phone + "\",\"reportingTo\":" + reportingTo + ",\"roles\":[" + role + "],\"terms\":" + terms + "," + accessPoints + "\"gender\":\"" + gender + "\",\"joiningDate\":\"" + joiningDate + "\",\"probationPeriod\":" + probabationPeriod + ",\"probationPeriodEnabled\":" + probationPeriodEnabled + ",\"mobile\":" + mobile + "}]}";
            String token = apiUtility.getTokenFromLocalStorage();
            Response response = ApiHelper.givenRequestSpecification()
                    .header("authorization", token)
                    .body(jsonString)
                    .when().redirects().follow(false).
                    post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/");
            ApiHelper.genericResponseValidation(response, "API - CREATE USER " + email + " - " + phone);

            String userID = getUserID(phone);

            variableContext.setScenarioContext("NEWEMAIL" + j, email);
            variableContext.setScenarioContext("NEWUSERPHONE" + j, phone);
            variableContext.setScenarioContext("NEWUSERNAME" + j, name);
            variableContext.setScenarioContext("NEWUSERID" + j, userID);
            variableContext.setScenarioContext("NEWMULTUSERCLEANUPREQUIRED", "TRUE");
            variableContext.setScenarioContext("NEWUSERCREATEDDAY", now.getDayOfMonth());
            variableContext.setScenarioContext("REPORTINGID", reportingTo);
            variableContext.setScenarioContext("REPORTINGNAME", getUserName(dataMap.get("Reporting Phone")));
            variableContext.setScenarioContext("USERKEYWORD", tc);
        }
    }

    public void reactivateUser(String phone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"inactive\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET INACTIVE USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");
        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID);
        ApiHelper.genericResponseValidation(response, "API - GET USER DETAILS" + userID);
        jsonPathEvaluator = response.jsonPath();
        String userEmail = jsonPathEvaluator.get("message.user.email").toString();
        String userPhone = jsonPathEvaluator.get("message.user.phone").toString();
        String userName = jsonPathEvaluator.get("message.user.name").toString();
        String reportingID = jsonPathEvaluator.get("message.user.reportingTo.id").toString();
        String roles = jsonPathEvaluator.get("message.user.roles.id").toString();
        Boolean mobile = jsonPathEvaluator.get("message.user.mobile");
        String probationPeriod = jsonPathEvaluator.get("message.user.probationPeriod").toString();
        String gender = jsonPathEvaluator.get("message.user.gender").toString();
        Boolean gps = jsonPathEvaluator.get("message.user.gps");
        String joiningDate = jsonPathEvaluator.get("message.user.joiningDate").toString();

        jsonString = "{\"user\":\n" +
                "{\"accessExpiresAt\":null,\n" +
                "\"email\":\"" + userEmail + "\",\n" +
                "\"employeeCode\":\"\",\n" +
                "\"gps\":" + gps + ",\n" +
                "\"name\":\"" + userName + "\",\n" +
                "\"phone\":\"" + userPhone + "\",\n" +
                "\"reportingTo\":\"" + reportingID + "\",\n" +
                "\"roles\":[" + roles + "],\n" +
                "\"terms\":[],\n" +
                "\"accessPoints\":[],\n" +
                "\"gender\":\"" + gender + "\",\n" +
                "\"joiningDate\":\"" + joiningDate + "\",\n" +
                "\"probationPeriod\":" + probationPeriod + ",\n" +
                "\"probationPeriodEnabled\":false,\n" +
                "\"id\":" + userID + ",\n" +
                "\"mobile\":" + mobile + "}}";

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID + "/activate");

        ApiHelper.genericResponseValidation(response, "API - REACTIVATE USER " + phone);
        ResultManager.pass("I reactivate user ", "I reactivated user " + phone, false);

    }

    public void editUserName(String phone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID);
        ApiHelper.genericResponseValidation(response, "API - GET USER DETAILS" + userID);
        jsonPathEvaluator = response.jsonPath();
        String userEmail = jsonPathEvaluator.get("message.user.email").toString();
        String userPhone = jsonPathEvaluator.get("message.user.phone").toString();
        String userName = "Edited APIUser" + randomDataGenerator.getData("{RANDOM_STRING}");
        String reportingID = jsonPathEvaluator.get("message.user.reportingTo.id").toString();
        String roles = jsonPathEvaluator.get("message.user.roles.id").toString();
        Boolean mobile = jsonPathEvaluator.get("message.user.mobile");
        String probationPeriod = jsonPathEvaluator.get("message.user.probationPeriod").toString();
        String gender = jsonPathEvaluator.get("message.user.gender").toString();
        Boolean gps = jsonPathEvaluator.get("message.user.gps");
        String joiningDate = jsonPathEvaluator.get("message.user.joiningDate").toString();

        jsonString = "{\"user\":\n" +
                "{\"accessExpiresAt\":null,\n" +
                "\"email\":\"" + userEmail + "\",\n" +
                "\"employeeCode\":\"\",\n" +
                "\"gps\":" + gps + ",\n" +
                "\"name\":\"" + userName + "\",\n" +
                "\"phone\":\"" + userPhone + "\",\n" +
                "\"reportingTo\":" + reportingID + ",\n" +
                "\"roles\":[" + roles + "],\n" +
                "\"terms\":[],\n" +
                "\"accessPoints\":[],\n" +
                "\"gender\":\"" + gender + "\",\n" +
                "\"joiningDate\":\"" + joiningDate + "\",\n" +
                "\"probationPeriod\":" + probationPeriod + ",\n" +
                "\"probationPeriodEnabled\":false,\n" +
                "\"id\":" + userID + ",\n" +
                "\"mobile\":" + mobile + "}}";
        variableContext.setScenarioContext("NEWUSERNAME", userName);

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID + "");

        ApiHelper.genericResponseValidation(response, "API - EDIT USER NAME of " + phone + " to " + userName);
        ResultManager.pass("I edit username ", "I edit username to " + userName, false);

    }

    public void editReportingManager(String phone,String reportingPhone) throws ParseException {
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":25},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"name\":\"\",\"phone\":\"" + phone + "\",\"s\":{\"name\":\"\",\"phone\":\"" + phone + "\"}}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET USER ID of phone " + phone);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String userID = jsonPathEvaluator.get("message.users.id").toString().replace("[", "").replace("]", "");

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID);
        ApiHelper.genericResponseValidation(response, "API - GET USER DETAILS" + userID);
        jsonPathEvaluator = response.jsonPath();
        String userEmail = jsonPathEvaluator.get("message.user.email").toString();
        String userPhone = jsonPathEvaluator.get("message.user.phone").toString();
        String userName = jsonPathEvaluator.get("message.user.name").toString();
        String reportingID = getUserID(reportingPhone);
        String roles = jsonPathEvaluator.get("message.user.roles.id").toString();
        Boolean mobile = jsonPathEvaluator.get("message.user.mobile");
        String probationPeriod = jsonPathEvaluator.get("message.user.probationPeriod").toString();
        String gender = jsonPathEvaluator.get("message.user.gender").toString();
        Boolean gps = jsonPathEvaluator.get("message.user.gps");
        String joiningDate = jsonPathEvaluator.get("message.user.joiningDate").toString();

        jsonString = "{\"user\":\n" +
                "{\"accessExpiresAt\":null,\n" +
                "\"email\":\"" + userEmail + "\",\n" +
                "\"employeeCode\":\"\",\n" +
                "\"gps\":" + gps + ",\n" +
                "\"name\":\"" + userName + "\",\n" +
                "\"phone\":\"" + userPhone + "\",\n" +
                "\"reportingTo\":" + reportingID + ",\n" +
                "\"roles\":" + roles + ",\n" +
                "\"terms\":[],\n" +
                "\"accessPoints\":[],\n" +
                "\"gender\":\"" + gender + "\",\n" +
                "\"joiningDate\":\"" + joiningDate + "\",\n" +
                "\"probationPeriod\":" + probationPeriod + ",\n" +
                "\"probationPeriodEnabled\":false,\n" +
                "\"id\":" + userID + ",\n" +
                "\"mobile\":" + mobile + "}}";
        variableContext.setScenarioContext("NEWUSERNAME", userName);

        response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                patch(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/" + userID + "");

        ApiHelper.genericResponseValidation(response, "API - EDIT REPORTING MANAGER OF " + phone + " to " + reportingID);

    }

    public ArrayList<String> getActiveUsers() throws ParseException {
        String perPage = getActiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\"],\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET ACTIVE USERS ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        ArrayList<String> array = jsonPathEvaluator.get("message.users.name");
        ResultManager.pass("I get active users ", "I got active users", false);

        return array;
    }

    public ArrayList<String> getAllUsers() throws ParseException {
        String perPage = getActiveUsersCount();
        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\",\"inactive\"],\"terms\":[]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");
        ApiHelper.genericResponseValidation(response, "API - GET ALL USERS ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        ArrayList<String> array = jsonPathEvaluator.get("message.users.name");
        ResultManager.pass("I get all users ", "I got all users", false);

        return array;
    }

    //Find all admins in the organisation
    public List<String> adminsInOrganization() throws ParseException {
        String perPage = getActiveUsersCount();

        String roleCode = "";
        String org = (String) variableContext.getScenarioContext("CHANGEORG");
        if(org.equalsIgnoreCase("CustomAttributes")){
            roleCode = "259";
        }else if(org.equalsIgnoreCase("AccessManagement")){
            roleCode = "4";
        }else{
            roleCode = "385";
        }


        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"isSignedUp\":null,\"roles\":[\""+roleCode+"\"]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> admins = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch ADMINS");
        JsonPath jsonPathEvaluator = response.jsonPath();

        admins = jsonPathEvaluator.get("message.users.name");

        return admins.stream().sorted().collect(Collectors.toList());
    }

    //Find all managers in the organisation
    public List<String> managersInOrganizationLeavePolicy() throws ParseException {
        String perPage = getActiveUsersCount();

        String roleCode = "";
        String org = (String) variableContext.getScenarioContext("CHANGEORG");
        if(org.equalsIgnoreCase("CustomAttributes")){
            roleCode = "260";
        }else if(org.equalsIgnoreCase("AccessManagement")){
            roleCode = "5";
        }
        else{
            roleCode = "386";
        }

        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"isSignedUp\":null,\"roles\":[\""+roleCode+"\"]}}";
        String token = apiUtility.getTokenFromLocalStorage();

        List<String> admins = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - Fetch Managers");
        JsonPath jsonPathEvaluator = response.jsonPath();

        //For Page 1
        admins = jsonPathEvaluator.get("message.users.name");

        return admins.stream().sorted().collect(Collectors.toList());
    }

    //Find all frontdesk in the organisation
    public List<String> frontdeskPersonsInOrganization() throws ParseException {
        String perPage = getActiveUsersCount();

        String roleCode = "";
        String org = (String) variableContext.getScenarioContext("CHANGEORG");
        if(org.equalsIgnoreCase("CustomAttributes")){
            roleCode = "3017";
        }else if(org.equalsIgnoreCase("AccessManagement")){
           roleCode = "2011";
        }
        else{
            roleCode = "388";
        }

        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"isSignedUp\":null,\"roles\":[\""+roleCode+"\"]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> admins = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - FETCH FRONTDESK");
        JsonPath jsonPathEvaluator = response.jsonPath();

        //For Page 1
        admins = jsonPathEvaluator.get("message.users.name");

        return admins.stream().sorted().collect(Collectors.toList());
    }

    //Find all Spintly in the organisation
    public List<String> spintlyUsersInOrganization() throws ParseException {
        String perPage = getActiveUsersCount();

        String roleCode = "";
        String org = (String) variableContext.getScenarioContext("CHANGEORG");
        if(org.equalsIgnoreCase("CustomAttributes")){
            roleCode = "261";
        }else if(org.equalsIgnoreCase("AccessManagement")){
            roleCode = "6";
        }
        else{
            roleCode = "387";
        }

        String jsonString = "{\"pagination\":{\"page\":1,\"perPage\":"+perPage+"},\"filters\":{\"userType\":[\"active\"],\"terms\":[],\"isSignedUp\":null,\"roles\":[\""+roleCode+"\"]}}";
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> admins = new ArrayList<>();

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/users/list");

        ApiHelper.genericResponseValidation(response, "API - FETCH SPINTLY USERS");
        JsonPath jsonPathEvaluator = response.jsonPath();


        admins = jsonPathEvaluator.get("message.users.name");

        return admins.stream().sorted().collect(Collectors.toList());
    }

    public List<String> getUsersAssignedToACustomAttribute(int attributeID, int termID) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        List<String> users = new ArrayList<>();

        //https://test.api.spintly.com/v2/organisationManagement/organisations/776/attributes/451/allUsers
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/organisationManagement/organisations/" + apiUtility.getOrgnizationID() + "/attributes/"+attributeID+"/allUsers");

        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> users1 = jsonPathEvaluator.get("message.users");

        for(int i=0;i<users1.size();i++){
            //jsonPathEvaluator.get("message.users["+i+"].terms[0]");
            if(!(jsonPathEvaluator.get("message.users["+i+"].terms[0]") == null)) {
                int termInt = jsonPathEvaluator.get("message.users["+i+"].terms[0]");
                if(termInt == termID) {
                    String user = jsonPathEvaluator.get("message.users["+i+"].name");
                    users.add(user);
                    //break;
                }
            }
        }

        return users.stream().collect(Collectors.toList());
    }
}

