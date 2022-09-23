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
import java.util.*;

import static io.restassured.RestAssured.given;

@SuppressWarnings({"All"})
public class ShiftManagementService extends DriverContext {
    private static LogUtility logger = new LogUtility(ShiftManagementService.class);
    ApiUtility apiUtility = new ApiUtility();
    UserManagementService userManagementService = new UserManagementService();

    public void createShift(DataTable data) throws ParseException {
        Map<String, String> dataMap = data.transpose().asMap(String.class, String.class);
        String random = RandomDataGenerator.getData("{RANDOM_NUM}", 5).toString();
        String alpha = RandomDataGenerator.randomAlphabetic(3);
        String shiftName = PropertyUtility.getDataProperties("shift.api.initial") + random + alpha;
        String shiftShortName = PropertyUtility.getDataProperties("shift.short.api.initial") + random + alpha;
        String fullWorkingDays = dataMap.get("Full Working Days").trim().toLowerCase().replace(",", "\",\"").replace(" ", "");
        String shiftFrom = dataMap.get("Shift From").trim();
        String shiftTo = dataMap.get("Shift To").trim();
        String partialWorkingDays = dataMap.get("Partial Working Days").trim().toLowerCase().replace(",", "\",\"").replace(" ", "");
        String partialShiftFrom = dataMap.get("Partial Shift From").trim();
        String partialShiftTo = dataMap.get("Partial Shift To").trim();
        String weekoffSettingsByDay[] = dataMap.get("Week Off Settings").trim().toLowerCase().split(":");
        String breakName = dataMap.get("Break1 Name").trim();
        String breakFrom = dataMap.get("Break1 From").trim();
        String breakTo = dataMap.get("Break1 To").trim();
        String minimumHoursEnabled = "true";
        try {
             minimumHoursEnabled = dataMap.get("Minimum Hours Enabled").trim();
        }catch(Exception ex){minimumHoursEnabled = "true";}
        Date date = null;
        String hours = "";
        int minutes = 0;
        String[] meridian = null;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm a");
        if (!breakFrom.equals("")) {
            date = format.parse(breakFrom);
            hours = String.valueOf(date.getHours());
            minutes = date.getMinutes();
            meridian = breakFrom.split(" ");
            variableContext.setScenarioContext("BREAK1FROM", hours + ":" + (minutes < 9 ? "0" + minutes : minutes) + " " + meridian[1]);
        }
        if (!breakTo.equals("")) {
            date = format.parse(breakTo);
            hours = String.valueOf(date.getHours());
            minutes = date.getMinutes();
            meridian = breakTo.split(" ");
            variableContext.setScenarioContext("BREAK1TO", hours + ":" + (minutes < 9 ? "0" + minutes : minutes) + " " + meridian[1]);
        }
        variableContext.setScenarioContext("BREAK1", "TRUE");
        variableContext.setScenarioContext("BREAK1NAME", breakName);
        String break2Name = dataMap.get("Break2 Name").trim();
        String break2From = dataMap.get("Break2 From").trim();
        String break2To = dataMap.get("Break2 To").trim();
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm a");
        Date date1 = null;
        String hours1 = "";
        int minutes1 = 0;
        String[] meridian1 = null;

        if (!break2From.equals("")) {
            date1 = format1.parse(break2From);
            hours1 = String.valueOf(date1.getHours());
            minutes1 = date1.getMinutes();
            meridian1 = break2From.split(" ");
            variableContext.setScenarioContext("BREAK2FROM", hours1 + ":" + (minutes1 < 9 ? "0" + minutes1 : minutes1) + " " + meridian1[1]);
        }
        if (!break2To.equals("")) {
            date1 = format1.parse(break2To);
            hours1 = String.valueOf(date1.getHours());
            minutes1 = date1.getMinutes();
            meridian1 = break2To.split(" ");
            variableContext.setScenarioContext("BREAK2TO", hours1 + ":" + (minutes1 < 9 ? "0" + minutes1 : minutes1) + " " + meridian1[1]);
        }
        variableContext.setScenarioContext("BREAK2", "TRUE");
        variableContext.setScenarioContext("BREAK2NAME", break2Name);


        String wo = "";
        if (!weekoffSettingsByDay[0].equalsIgnoreCase("NA")) {
            for (int i = 0; i < weekoffSettingsByDay.length; i++) {
                String[] forDay = weekoffSettingsByDay[i].split("-");
                String[] weeks = forDay[1].split(",");
                String weekString = "";
                for (int j = 0; j < weeks.length; j++) {
                    variableContext.setScenarioContext("WO" + i + j, weeks[j].trim().toLowerCase());
                    variableContext.setScenarioContext("DAY" + i + j, forDay[0].trim().toLowerCase());
                    if (j == weeks.length - 1)
                        weekString = weekString + "week " + weeks[j];
                    else
                        weekString = weekString + "week " + weeks[j] + "\n";
                }
                variableContext.setScenarioContext("WOWEEKSCOUNT" + i, weeks.length);
                variableContext.setScenarioContext("SELECTEDWEEKS" + i, weekString);

                int num = apiUtility.getDayNum(forDay[0]);
                String weekarray = forDay[1];
                if (i == weekoffSettingsByDay.length - 1)
                    wo = wo + "{\"" + num + "\":[" + weekarray + "]}";
                else
                    wo = wo + "{\"" + num + "\":[" + weekarray + "]},";
            }

            variableContext.setScenarioContext("WODAYSCOUNT", weekoffSettingsByDay.length);
        }
        String shiftFrom24HourFormat = new SimpleDateFormat("hh:mm a").parse(shiftFrom).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(shiftFrom).getMinutes());

        String shiftTo24HourFormat = new SimpleDateFormat("hh:mm a").parse(shiftTo).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(shiftTo).getMinutes());
        String break1From24HourFormat = new SimpleDateFormat("hh:mm a").parse(breakFrom).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(breakFrom).getMinutes());
        String break1To24HourFormat = new SimpleDateFormat("hh:mm a").parse(breakTo).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(breakTo).getMinutes());
        String break2From24HourFormat = new SimpleDateFormat("hh:mm a").parse(break2From).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(break2From).getMinutes());

        String break2To24HourFormat = new SimpleDateFormat("hh:mm a").parse(break2To).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(break2To).getMinutes());
        String shiftStartTimeForToday = (new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " " + shiftFrom24HourFormat);
        Date shiftDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(shiftStartTimeForToday);
        variableContext.setScenarioContext("SHIFTSTARTEPOCH", shiftDate.getTime() / 1000);
        String shiftEndTimeForToday = (new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " " + shiftTo24HourFormat);
        shiftDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(shiftEndTimeForToday);
        variableContext.setScenarioContext("SHIFTENDEPOCH", shiftDate.getTime() / 1000);

        long duration = new SimpleDateFormat("hh:mm").parse(shiftTo24HourFormat).getTime() - new SimpleDateFormat("hh:mm").parse(shiftFrom24HourFormat).getTime();
        variableContext.setScenarioContext("SHIFTDURATION", duration);

        String partialJsonString = "";
        if (!partialWorkingDays.equalsIgnoreCase("NA")) {
            String partialShiftFrom24HourFormat = new SimpleDateFormat("hh:mm a").parse(partialShiftFrom).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(partialShiftFrom).getMinutes());
            ;
            String partialShiftTo24HourFormat = new SimpleDateFormat("hh:mm a").parse(partialShiftTo).getHours() + ":" + String.format("%02d", new SimpleDateFormat("hh:mm a").parse(partialShiftTo).getMinutes());
            partialJsonString = "\"partialDayEnabled\":true,\"partialDayStartTime\":\"" + partialShiftFrom24HourFormat + "\",\"partialDayEndTime\":\"" + partialShiftTo24HourFormat + "\",\"partialDaysOfWeek\":[\"" + partialWorkingDays + "\"]";
        } else {
            partialJsonString = "\"partialDayEnabled\":false,\"partialDayStartTime\":\"" + shiftFrom24HourFormat + "\",\"partialDayEndTime\":\"" + shiftFrom24HourFormat + "\",\"partialDaysOfWeek\":[]";

        }
        String OTToBeCalculated = dataMap.get("OT to be calculated").trim();
        String OTCalculationToBeginMinBeforeShiftStarts = dataMap.get("OT calculation to begin Min before shift starts").trim();
        String OTCalculationToBeginMinAfterShiftEnds = dataMap.get("OT calculation to begin Min after shift ends").trim();
        String[] MinOTDuration = dataMap.get("Min OT duration").trim().split(":");
        String[] MaxOTDuration = dataMap.get("Max OT duration").trim().split(":");
        String GraceTimeForMinOT = dataMap.get("Grace time for min OT").trim();
        variableContext.setScenarioContext("OT", "TRUE");
        variableContext.setScenarioContext("OTTOBECALC", OTToBeCalculated);
        variableContext.setScenarioContext("OTBEFORE", OTCalculationToBeginMinBeforeShiftStarts);
        variableContext.setScenarioContext("OTAFTER", OTCalculationToBeginMinAfterShiftEnds);
        variableContext.setScenarioContext("MINOTHR", MinOTDuration[0]);
        variableContext.setScenarioContext("MINOTMIN", MinOTDuration[1]);
        variableContext.setScenarioContext("MAXOTHR", MaxOTDuration[0]);
        variableContext.setScenarioContext("MAXOTMIN", MaxOTDuration[1]);
        variableContext.setScenarioContext("GRACE", GraceTimeForMinOT);
        int otType = 1;
        boolean isOvernight = false;
        switch (OTToBeCalculated) {
            case "Before and after shift":
                otType = 1;
                break;
            case "Only after shift":
                otType = 2;
                break;
        }
        String fullDayMinutes = "510";
        String halfDayMinutes = "255";
        String fullDayMinutesPartial = "300";
        variableContext.setScenarioContext("FULLDAYMIN", fullDayMinutes);
        variableContext.setScenarioContext("HALFDAYMIN", halfDayMinutes);
        variableContext.setScenarioContext("FULLDAYMINPARTIAL", fullDayMinutesPartial);

        String otJsonString = "\"otSettings\":{\"enabled\":true,\"otType\":" + otType + ",\"otGraceMinutes\":" + GraceTimeForMinOT + ",\"minOtMinutes\":" + (Integer.parseInt(MinOTDuration[0]) * 60 + Integer.parseInt(MinOTDuration[1])) + ",\"maxOtMinutes\":" + (Integer.parseInt(MaxOTDuration[0]) * 60 + Integer.parseInt(MaxOTDuration[1])) + ",\"minutesBeforeShiftStart\":" + OTCalculationToBeginMinBeforeShiftStarts + ",\"minutesAfterShiftEnd\":" + OTCalculationToBeginMinAfterShiftEnds + "},\"isOvernightShift\":" + isOvernight + "";
        String minPartialWorkJsonString = "\"minimumPartialWorkHourSettings\":{\"enabled\":true,\"fullDayMinutes\":"+fullDayMinutesPartial+"}";

        String minimumStandardWorkHourSettings = "\"minimumStandardWorkHourSettings\":{\"enabled\":true,\"halfDayMinutes\":"+halfDayMinutes+",\"fullDayMinutes\":"+fullDayMinutes+"}";
        if(minimumHoursEnabled.equalsIgnoreCase("false"))
        {
            minimumStandardWorkHourSettings = "\"minimumStandardWorkHourSettings\":{\"enabled\":false,\"halfDayMinutes\":0,\"fullDayMinutes\":0}";
        }
        String weekOffSettingJsonString = "\"weekOffs\":[" + wo + "]";
        String breaksString = "\"breaks\":[{\"breakName\":\"" + breakName + "\",\"fromTime\":\"" + break1From24HourFormat + "\",\"toTime\":\"" + break1To24HourFormat + "\"},{\"breakName\":\"" + break2Name + "\",\"fromTime\":\"" + break2From24HourFormat + "\",\"toTime\":\"" + break2To24HourFormat + "\"}]";
        String jsonString = "{\"shiftName\":\"" + shiftName + "\",\"shortName\":\"" + shiftShortName + "\",\"startTime\":\"" + shiftFrom24HourFormat + "\",\"endTime\":\"" + shiftTo24HourFormat + "\",\"standardDaysOfWeek\":[\"" + fullWorkingDays + "\"]," + partialJsonString + ",\"lateEntryGraceMinutes\":0,\"earlyExitGraceMinutes\":0," + weekOffSettingJsonString + ",\"directBreakDeduction\":true," + breaksString + "," + minimumStandardWorkHourSettings + "," + minPartialWorkJsonString + "," + otJsonString + "}";
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/shift/");

        ApiHelper.genericResponseValidation(response, "API - CREATE SHIFT : " + shiftName);
        variableContext.setScenarioContext("SHIFTNAME", shiftName);
        variableContext.setScenarioContext("SHIFTSHORTNAME", shiftShortName);
        ;
        variableContext.setScenarioContext("SELECTEDFULLWORKINGDAYS", fullWorkingDays.toLowerCase().replace("\"", "").replace(",", "\n"));
        format = new SimpleDateFormat("HH:mm a");
        date = format.parse(shiftFrom);

        hours = String.valueOf(date.getHours());
        minutes = date.getMinutes();
        meridian = shiftFrom.split(" ");
        variableContext.setScenarioContext("SHIFTFROM", hours + ":" + (minutes < 9 ? "0" + minutes : minutes) + " " + meridian[1]);
        date = format.parse(shiftTo);
        hours = String.valueOf(date.getHours());
        minutes = date.getMinutes();
        meridian = shiftTo.split(" ");
        variableContext.setScenarioContext("SHIFTTO", hours + ":" + (minutes < 9 ? "0" + minutes : minutes) + " " + meridian[1]);
        ResultManager.pass("I create new shift ", "Created new shift with details : "+shiftName +" | "+shiftShortName, false);

    }

    public String getAllShiftsCount() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/shifts");
        ApiHelper.genericResponseValidation(response, "API - GET ALL SHIFTS COUNT");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String size = String.valueOf(jsonPathEvaluator.getList("message.shifts.id").size());
        variableContext.setScenarioContext("ALLSHIFTSCOUNT", size);
        ResultManager.pass("I get all shifts count ", "I got shifts count", false);
        return size;
    }

    public String getAllActiveShiftsCount() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/dashboard-data/forAdmin");
        ApiHelper.genericResponseValidation(response, "API - GET ALL SHIFTS COUNT ");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String activeShiftsCount = jsonPathEvaluator.get("message.shiftSummaryData.activeShiftCount").toString();
        variableContext.setScenarioContext("ALLACTIVESHIFTSCOUNT", activeShiftsCount);
        ResultManager.pass("I get all active shifts count ", "I got active shifts count", false);
        return String.valueOf(activeShiftsCount);

    }

    public List<String> getAllShiftsName() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/shifts");
        ApiHelper.genericResponseValidation(response, "API - GET ALL SHIFTS");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> shifts = jsonPathEvaluator.getList("message.shifts.shiftName");
        variableContext.setScenarioContext("ALLSHIFTNAMES", shifts);
        ResultManager.pass("I get all shift names ", "I got all shift names", false);

        return shifts;
    }

    public List<String> getAllActiveShiftsName() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/dashboard-data/forAdmin");
        ApiHelper.genericResponseValidation(response, "API - GET ALL ACTIVE SHIFTS");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<HashMap<String, Object>> allShifts = jsonPathEvaluator.getList("message.todayAttendanceData.shift");
        List<String> activeshifts = new ArrayList<>();
        for (int i = 0; i < allShifts.size(); i++) {
            if (allShifts.get(i).get("active").equals(true))
                activeshifts.add(allShifts.get(i).get("shiftName").toString());
        }
        variableContext.setScenarioContext("ACTIVESHIFTS", activeshifts);
        ResultManager.pass("I get all active shift names ", "I got all active shift names", false);

        return activeshifts;
    }

    public List<String> getAllInactiveShiftsName() throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/dashboard-data/forAdmin");
        ApiHelper.genericResponseValidation(response, "API - GET ALL INACTIVE SHIFTS");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<HashMap<String, Object>> allShifts = jsonPathEvaluator.getList("message.todayAttendanceData.shift");
        List<String> inactiveshifts = new ArrayList<>();
        for (int i = 0; i < allShifts.size(); i++) {
            if (!allShifts.get(i).get("active").equals(true))
                inactiveshifts.add(allShifts.get(i).get("shiftName").toString());
        }
        variableContext.setScenarioContext("INACTIVESHIFTS", inactiveshifts);
        ResultManager.pass("I get all inactive shift names ", "I got all inactive shift names", false);

        return inactiveshifts;
    }

    public String getShiftShortName(String shiftName) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/shifts");
        ApiHelper.genericResponseValidation(response, "API - GET SHIFT SHORTNAME");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<HashMap<String, Object>> allShifts = jsonPathEvaluator.getList("message.shifts");
        String shortName = "";
        for (int i = 0; i < allShifts.size(); i++) {
            if (allShifts.get(i).get("shiftName").equals(shiftName)) {
                shortName = allShifts.get(i).get("shortName").toString();
                break;
            }
        }
        variableContext.setScenarioContext("SHIFTSHORTNAME", shortName);
        return shortName;
    }

    public String getShiftId(String shiftName) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/shifts");
        ApiHelper.genericResponseValidation(response, "API - GET SHIFT SHORTNAME");
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<HashMap<String, Object>> allShifts = jsonPathEvaluator.getList("message.shifts");
        String id = "";
        for (int i = 0; i < allShifts.size(); i++) {
            if (allShifts.get(i).get("shiftName").equals(shiftName)) {
                id = allShifts.get(i).get("id").toString();
                break;
            }
        }
        variableContext.setScenarioContext("SHIFTID", id);
        return id;
    }

    public void assignBulkShifts(String shiftName, String userPhone, int prevDaysFromCurrent) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String shiftID = null;
        if(!shiftName.equalsIgnoreCase("WO"))
         shiftID = getShiftId(shiftName);
        String userID = userManagementService.getUserID(userPhone);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dayStart = new Date(System.currentTimeMillis() - 24 * prevDaysFromCurrent * 60 * 60 * 1000);
        Date dayEnd = new Date();
        String jsonString = "{\"shift\":{\"applicableFrom\":\"" + dateFormat.format(dayStart) + "\",\"applicableUntil\":\"" + dateFormat.format(dayEnd) + "\",\"shiftId\":" + shiftID + ",\"userIds\":[" + userID + "],\"overrideHoliday\":false}}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/assignShifts");

        ApiHelper.genericResponseValidation(response, "API - ASSIGN SHIFT FOR " + prevDaysFromCurrent + " DAYS PRIOR");
        ResultManager.pass("I assign shift for user ", "I assigned shift for user for "+ prevDaysFromCurrent+" days prior to current date", false);
    }
    public void assignWeekoff(String shiftName, String userPhone) throws ParseException {
        String token = apiUtility.getTokenFromLocalStorage();
        String shiftID = null;
        if(!shiftName.equalsIgnoreCase("WO"))
            shiftID = getShiftId(shiftName);
        String userID = userManagementService.getUserID(userPhone);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dayStart = new Date();
        String jsonString = "\n" +
                "{\"rosterData\":[{\"shiftId\":null,\"weekOff\":true,\"day\":\""+dateFormat.format(dayStart)+"\",\"overrideHoliday\":null,\"approvedLeaves\":null,\"compulsoryHoliday\":null,\"approvedHoliday\":null,\"leaves\":null,\"usedLeaves\":null,\"changed\":true,\"userId\":"+userID+",\"dateString\":\""+dateFormat.format(dayStart)+"\"}]}";
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .body(jsonString)
                .when().redirects().follow(false).
                post(PropertyUtility.getDataProperties("base.api.url") + "/v2/attendanceManagement/organisations/" + apiUtility.getOrgnizationID() + "/assignShiftsRooster");

        ApiHelper.genericResponseValidation(response, "API - ASSIGN WO FOR TODAY");
        ResultManager.pass("I assign shift for user ", "I assigned shift for user for for today", false);
    }

}
