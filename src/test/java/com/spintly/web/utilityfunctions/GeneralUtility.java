package com.spintly.web.utilityfunctions;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverBase;
import com.spintly.base.core.DriverContext;
import com.spintly.base.core.PageBase;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.RandomDataGenerator;
import com.spintly.web.pages.attendance.ReportsPage;
import com.spintly.web.pages.organisationsettings.MobileBasedAccess;

import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ExcelHelper;
import com.spintly.base.utilities.PdfHelper;
import com.spintly.web.pages.shiftmanagement.CreateShiftPage;
import com.spintly.web.pages.users.AddSingleUserPage;
import com.spintly.web.pages.users.AllUsersPage;
import com.spintly.web.pages.users.EditMultipleUsersPage;
import com.spintly.web.support.*;
import com.spintly.web.pages.dashboard.DashboardPage;
import com.spintly.web.pages.login.LoginPage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import java.awt.*;
import java.io.*;
import java.sql.DriverManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import com.spintly.base.core.PageBase;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;


public class GeneralUtility extends DriverBase {
    WebDriverActions action = new WebDriverActions();
    DashboardPage dashboardPage = new DashboardPage();
    LoginPage loginPage = new LoginPage();
    CreateShiftPage createShiftPage = new CreateShiftPage();
    ReportsPage reportsPage = new ReportsPage();
    AllUsersPage aup = new AllUsersPage();
    AddSingleUserPage addSingleUserPage = new AddSingleUserPage();
    MobileBasedAccess mobileBasedAccess = new MobileBasedAccess();
    EditMultipleUsersPage editMultipleUsersPage = new EditMultipleUsersPage();
    RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    PageBase pageBase = new PageBase();
    LocalDateTime now = LocalDateTime.now();
    ApiUtility apiUtility = new ApiUtility();

    PdfHelper pdfHelper = new PdfHelper();
    ExcelHelper excelHelper = new ExcelHelper();

    public void Login(String phone, String password) {
        action.sendKeys(loginPage.Textbox_Phone(), phone);
        action.sendKeys(loginPage.Textbox_Password(), password);
        action.click(loginPage.Button_Login());
        testStepAssert.isElementDisplayed(dashboardPage.Dropdown_Organization(), "I should successfully login", "Logged in successfully", "Error in login in to system");
    }

    public void LoginVMS(String phone, String password) {
        action.sendKeys(loginPage.Textbox_Phone(), phone);
        action.sendKeys(loginPage.Textbox_Password(), password);
        action.click(loginPage.Button_Login());
    }
    int x = 10000;
        int y = 2000;
   

    public void partnerLogin(String phone, String password) {
        action.sendKeys(loginPage.Textbox_PartnerPhone(), phone);
        action.sendKeys(loginPage.Textbox_PartnerPassword(), password);
        action.click(loginPage.Button_PartnerLogin());
        testStepAssert.isElementDisplayed(dashboardPage.Label_Total_Active_Customers(), "I should successfully login to partner site", "Logged in successfully to partner site", "Error in login in to partner site ");
    }

    public void navigateMenu(String menu) throws InterruptedException {
        switch (menu) {
            case "Attendance > Shift Management":
                action.click(dashboardPage.Label_Attendance());
                action.click(dashboardPage.Label_ShiftManagement());
                Thread.sleep(x);
                break;
            case "Dashboard":
                if (action.getCurrentURL().contains("/dashboard/")) {
                    action.click(dashboardPage.Label_Dashboard());
                } else
                    action.refreshPage();
                break;
            case "Attendance":
                action.click(dashboardPage.Label_Attendance());
                action.click(dashboardPage.Label_Reports());
                Thread.sleep(x);
                break;
            case "Attendance > Reports":
                action.click(dashboardPage.Label_Attendance());
                action.click(dashboardPage.Label_Reports());
                Thread.sleep(x);
                break;
            case "All Users":
                action.click(dashboardPage.Label_AllUsers());
                Thread.sleep(x);
                break;
            case "Add Single User":
                action.click(dashboardPage.Label_AllUsers());
                action.click(aup.addUserButton());
                action.click(aup.addSingleUser());
                Thread.sleep(x);
                break;
            case "Mobile based Access":
                action.click(dashboardPage.settings());
                action.click(dashboardPage.settingsOption(menu));
                Thread.sleep(x);
                break;
            case "Add multiple users":
                action.click(dashboardPage.Label_AllUsers());
                action.click(aup.addUserButton());
                action.click(aup.addMultipleUsers());
                Thread.sleep(x);
                break;
            case "Visitor Management":
                action.click(dashboardPage.visitorManagementTab());
                break;
            case "Access Management":
                action.click(dashboardPage.accessManagementTab());
                break;
        }
        dashboardPage.waitForPageLoad();
    }

    public void selectTime(WebElement element, String time) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm a");
        Date date = format.parse(time);
        String hours = String.valueOf(date.getHours());
        int minutes = date.getMinutes();
        String[] meridian = time.split(" ");
        action.click(element);
        Actions ac = new Actions(DriverContext.getObject().getDriver());
        ac.moveToElement(createShiftPage.Clock_Hour(hours)).clickAndHold().moveToElement(createShiftPage.Clock_Hour(hours)).release().perform();
        if (minutes % 5 == 0) {
            String mins = "";
            if (minutes <= 9)
                mins = "0" + minutes;
            else
                mins = String.valueOf(minutes);
            ac.moveToElement(createShiftPage.Clock_Minutes(mins)).clickAndHold().moveToElement(createShiftPage.Clock_Minutes(mins)).release().perform();
        } else {
            int degree = minutes * 6;
            JavascriptExecutor js = (JavascriptExecutor) DriverContext.getObject().getDriver();
            js.executeScript("arguments[0].setAttribute('style', 'height: 40%; transform: rotateZ(" + degree + "deg)')", createShiftPage.Clock_Pointer());
            ac.moveToElement(createShiftPage.Clock_PointerArrow()).click().release().perform();
        }
        action.click(createShiftPage.Clock_Meridian(meridian[1]));

    }


    //Function to Select a user
    public void selectAUser(String username) throws InterruptedException {
        //Search for the user using the name search box
        action.clearAndSend(aup.nameSearchBox(), username);

        Thread.sleep(3000);

        action.click(aup.userLink(username));

    }

    //Function to search a user in all users
    public boolean searchAUser(String username) throws InterruptedException {
        boolean present = false;
        //Search for the user using the name search box
        action.sendKeys(aup.nameSearchBox(), username);

        Thread.sleep(1500);

        present = action.isElementPresent(aup.userLink(username));

        return present;
    }

    //Select particular organisation
    public void selectParticularOrganisation(String org) throws InterruptedException,ParseException {
        switch (org) {
            case "Custom Attributes":
                PropertyUtility.changeDefaultOrganization("AutomationCA");
                PropertyUtility.changeOrgRoleID(org);
                variableContext.setScenarioContext("ORGID", apiUtility.getOrgnizationID());
                variableContext.setScenarioContext("CHANGEORG","CustomAttributes");
                if (!action.getText(dashboardPage.Dropdown_Organization()).trim().equalsIgnoreCase(PropertyUtility.getDataProperties("organization.title"))) {
                    String orgz = PropertyUtility.getDataProperties("organization.title");
                    action.click(dashboardPage.Dropdown_Organization());
                    action.clickViaJavascript(dashboardPage.List_Organization(PropertyUtility.getDataProperties("organization.title")));
                }
                break;
            case "no access points":
                variableContext.setScenarioContext("CHANGEORG","NOAP");
                PropertyUtility.changeDefaultOrganization("PriyankaNewOrg");
                if (!action.getText(dashboardPage.Dropdown_Organization()).trim().equalsIgnoreCase(PropertyUtility.getDataProperties("organization.title"))) {
                    action.click(dashboardPage.Dropdown_Organization());
                    action.click(dashboardPage.List_Organization(PropertyUtility.getDataProperties("organization.title")));
                }
                break;
            case "Access Management":
                String name = apiUtility.getOrgNameFromID("5");
                PropertyUtility.changeDefaultOrganization(name);
                //PropertyUtility.changeOrgRoleID(org);
                variableContext.setScenarioContext("ORGID", apiUtility.getOrgnizationID());
                variableContext.setScenarioContext("CHANGEORG","AccessManagement");
                if (!action.getText(dashboardPage.Dropdown_Organization()).trim().equalsIgnoreCase(PropertyUtility.getDataProperties("organization.title"))) {
                    action.click(dashboardPage.Dropdown_Organization());
                    action.clickViaJavascript(dashboardPage.List_Organization(PropertyUtility.getDataProperties("organization.title")));
                }
                break;
        }
    }



    //User CheckBox Action
    public void checkBoxAction(String username, String option) {
        //Search for the user using the name search box
        action.sendKeys(aup.nameSearchBox(), username);

        //Select the user from the list of users
        List<WebElement> users = aup.listOfUsernames();
        List<WebElement> checkboxes = aup.listOfCheckBoxes();

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getText().equalsIgnoreCase(username)) {
                action.click(checkboxes.get(i));
            }
        }

        switch (option) {
            case "delete":
                action.click(aup.deleteUserButton());
                action.click(aup.confirmActionButton());
                break;
            case "deactivate":
                action.click(aup.deactivateUsersButton());
                action.click(aup.confirmActionButton());
                break;
            case "edit":
                action.click(aup.editUserFromCheckBox());
                break;
        }
    }

    //Function to go to Add Single User Page
    public void addSingleUserPage() {
        action.click(aup.addUserButton());
        action.click(aup.addSingleUser());
    }

    //Function to go Add Multiple Users Page
    public void addMultipleUsersPage() {
        action.click(aup.addUserButton());
        action.click(aup.addMultipleUsers());
    }

    //Assign AccessPoints
    public void assignAccessPoints(String AccessPointsList[]) {

        for (int i = 0; i < AccessPointsList.length; i++) {
            action.click(addSingleUserPage.accessCheckBox(AccessPointsList[i]));
        }

        action.click(addSingleUserPage.assignAccessPoints());
    }

    //Unassign Access Points
    public void unassignAccessPoints(String AccessPointsList[]) {

        for (int i = 0; i < AccessPointsList.length; i++) {
            action.click(addSingleUserPage.accessCheckBox(AccessPointsList[i]));
        }

        action.click(addSingleUserPage.unassignAccessPoints());
    }


    //Get the month name
    public String monthName(String number) {
        String month = "";
        switch (number) {
            case "01":
                month = "Jan";
                break;
            case "02":
                month = "Feb";
                break;
            case "03":
                month = "Mar";
                break;
            case "04":
                month = "Apr";
                break;
            case "05":
                month = "May";
                break;
            case "06":
                month = "Jun";
                break;
            case "07":
                month = "Jul";
                break;
            case "08":
                month = "Aug";
                break;
            case "09":
                month = "Sep";
                break;
            case "10":
                month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;
        }
        return month;
    }

    //Select access expiry date
    public void selectAccessExpiryDate(String date, String month, String year) {
        //Click on the access expiry calendar button
        action.click(addSingleUserPage.accessExpiryButton());

        //Click on the month next button once
        action.click(addSingleUserPage.accessExpiryNextMonthFirst());

        //Keep Clicking on the next month button till you find the month
        while (true) {
            action.click(addSingleUserPage.accessExpiryNextMonthSec());
            if (addSingleUserPage.calendarMonthLabel().getText().split(" ")[0].substring(0, 3).equalsIgnoreCase(month)) {
                break;
            }
        }

        //Select the date
        action.click(addSingleUserPage.date(date));

        //Select the month
        action.click(addSingleUserPage.month(month));

        //Select the year
        action.click(addSingleUserPage.year(year));
    }

    //Select access expiry date
    public void selectAccessExpiryDateBulkEdit(String date, String month, String year) {
        //Click on the access expiry calendar button
        action.click(editMultipleUsersPage.accessExpiryAccessBulkEdit());

        //Click on the month next button once
        action.click(addSingleUserPage.accessExpiryNextMonthFirst());

        //Keep Clicking on the next month button till you find the month
        while (true) {
            action.click(addSingleUserPage.accessExpiryNextMonthSec());
            if (addSingleUserPage.calendarMonthLabel().getText().split(" ")[0].substring(0, 3).equalsIgnoreCase(month)) {
                break;
            }
        }

        //Select the date
        action.clickViaJavascript(addSingleUserPage.date(date));

        //Select the month
        action.click(addSingleUserPage.month(month));

        //Select the year
        action.click(addSingleUserPage.year(year));
    }

    public void editExpiryDate(String date, String month, String year) {
        //Click on the access expiry calendar button
        action.click(addSingleUserPage.accessExpiryButton());

        //Keep Clicking on the next month button till you find the month
        while (true) {
            action.click(addSingleUserPage.accessExpiryNextMonthSec());
            if (addSingleUserPage.calendarMonthLabel().getText().split(" ")[0].substring(0, 3).equalsIgnoreCase(month)) {
                break;
            }
        }

        //Select the date
        action.click(addSingleUserPage.date(date));

        //Select the month
        action.click(addSingleUserPage.month(month));

        //Select the year
        action.click(addSingleUserPage.year(year));
    }

    //Select joining date
    public void selectJoiningDate(String date, String month, String year) throws InterruptedException {
        action.click(addSingleUserPage.joiningDate());

        //Keep Clicking on the next month button till you find the month
        while (true) {
            action.click(addSingleUserPage.joiningDatePreviousMonth());
            if (action.getText(addSingleUserPage.calendarMonthLabel()).split(" ")[0].substring(0, 3).equalsIgnoreCase(month)) {
                break;
            }
        }

        //Select the date
        action.click(addSingleUserPage.date(date));

        //Click on the calendar button again
        addSingleUserPage.joiningDate().click();

        //Click on the year button
        action.click(addSingleUserPage.yearButtonJoiningDate());

        //Select the year
        action.click(addSingleUserPage.year(year));

        //Select the date again
        action.click(addSingleUserPage.date(date));
    }

    //Select joining date
    public void selectJoiningDateBulkEdit(String date, String month, String year) throws InterruptedException {
        action.click(editMultipleUsersPage.joiningDateButtonBulkEdit());

        //Keep Clicking on the next month button till you find the month
        while (true) {
            action.click(addSingleUserPage.joiningDatePreviousMonth());
            if (action.getText(addSingleUserPage.calendarMonthLabel()).split(" ")[0].substring(0, 3).equalsIgnoreCase(month)) {
                break;
            }
        }

        //Select the date
        action.click(addSingleUserPage.date(date));

        //Click on the calendar button again
        addSingleUserPage.joiningDate().click();

        //Click on the year button
        action.click(addSingleUserPage.yearButtonJoiningDate());

        //Select the year
        action.click(addSingleUserPage.year(year));

        //Select the date again
        action.click(addSingleUserPage.date(date));
    }


    public void
    setupExcelForUpload(String type) throws IOException  {
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\ExcelFiles\\base.xlsx";

        FileInputStream fis = new FileInputStream(path);
        File file;
        FileOutputStream out;
        String userkeyword = "";


        // XSSFWorkbook object to control the excel sheet
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        XSSFSheet sheet = workbook.getSheetAt(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        switch (type) {
            case "adding of users":
                userkeyword = "BulkAdd";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "test" + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));

                }
                break;
            case "uploading of excel file":
                //do nothing to upload the base excel file as it is without any changes
                break;
            case "changing of column headers":
                //changing the headers
                XSSFRow rowHeader = sheet.getRow(0);
                Cell empcodeHeader = rowHeader.getCell(0);
                Cell fullNameHeader = rowHeader.getCell(1);
                empcodeHeader.setCellValue("Full Name");
                fullNameHeader.setCellValue("Employee Code");

                userkeyword = "changeHdrs";

                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "test" + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);
                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));

                }
                break;
            case "duplicate column headers":
                //changing the headers
                XSSFRow duplicateRowHeader = sheet.getRow(0);
                Cell empcodeHeaderDuplicate = duplicateRowHeader.getCell(0);
                Cell fullNameHeaderDuplicate = duplicateRowHeader.getCell(1);
                empcodeHeaderDuplicate.setCellValue("Full Name");
                fullNameHeaderDuplicate.setCellValue("Full Name");
                break;

            case "if name is mandatory":
                for (int i = 1; i < 3; i++) {
                    String name = "";
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "test" + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);
                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));

                }
                break;

            case "if phone is mandatory":
                userkeyword = "noPhone";

                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "test" + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
                    String phone = "";
                    if (i == 1) {
                        phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");
                    } else {
                        phone = "";
                    }

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));

                }
                break;

            case "if duplicate phone number is allowed":
                userkeyword = "duplPhone";
                String phoneDuplicate = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "test" + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phoneDuplicate);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phoneDuplicate);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));

                }
                break;

            case "if email can be empty":
                userkeyword = "emptemail";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");
                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));

                }
                break;
            case "if country code can be empty":
                userkeyword = "emptCC";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");
                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell countryCode = row.getCell(2);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    countryCode.setCellValue("");
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;
            case "if employee code can be empty":
                userkeyword = "emptempCode";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");
                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell countryCode = row.getCell(2);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue("");
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;
            case "if spintly user role is assigned when role is empty":
                userkeyword = "emptRole";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");
                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell countryCode = row.getCell(2);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell role = row.getCell(6);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    role.setCellValue("");
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;
            case "deactivated users can be added":
                userkeyword = "deactusr";
                for (int i = 1; i < 3; i++) {
                    String name = (String) variableContext.getScenarioContext("NEWUSERNAME" + (i - 1));
                    String phone = (String) variableContext.getScenarioContext("NEWUSERPHONE" + (i - 1));
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");

                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell countryCode = row.getCell(2);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell role = row.getCell(6);
                    Cell accessExpiry = row.getCell(7);


                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    empcode.setCellValue(employeeCode);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;
            //
            case "if existing users can be added":
                userkeyword = "addexst";
                for (int i = 1; i < 3; i++) {
                    String name = (String) variableContext.getScenarioContext("NEWUSERNAME" + (i - 1));
                    String phone = (String) variableContext.getScenarioContext("NEWUSERPHONE" + (i - 1));
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");

                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell countryCode = row.getCell(2);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell role = row.getCell(6);
                    Cell accessExpiry = row.getCell(7);


                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    empcode.setCellValue(employeeCode);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;

            case "updating of dashboard user count":
                userkeyword = "dashboard";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "test" + randomDataGenerator.getData("{RANDOM_STRING}") + "@gmail.com";
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;

            case "if user can be added with existing email":
                userkeyword = "exstemail";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = (String) variableContext.getScenarioContext("NEWEMAIL");
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    email.setCellValue(emailID);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;
            case "if user can be added invalid email":
                userkeyword = "invalidemail";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "abc@" + randomDataGenerator.getData("{RANDOM_STRING}");
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}");

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    email.setCellValue(emailID);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;
            case "if user can be added invalid phone":
                userkeyword = "invalidphn";
                for (int i = 1; i < 3; i++) {
                    String name = userkeyword + randomDataGenerator.getData("{RANDOM_STRING}");
                    String employeeCode = randomDataGenerator.getData("{RANDOM_STRING}");
                    String emailID = "abc@" + randomDataGenerator.getData("{RANDOM_STRING}");
                    String phone = randomDataGenerator.getData("{RANDOM_PHONE_NUM}").substring(6, 10);

                    variableContext.setScenarioContext("NEWUSERPHONE" + i, phone);
                    variableContext.setScenarioContext("NEWUSERNAME" + i, name);


                    XSSFRow row = sheet.getRow(i);
                    Cell empcode = row.getCell(0);
                    Cell fullName = row.getCell(1);
                    Cell contactNumber = row.getCell(3);
                    Cell email = row.getCell(4);
                    Cell accessExpiry = row.getCell(7);


                    empcode.setCellValue(employeeCode);
                    fullName.setCellValue(name);
                    contactNumber.setCellValue(phone);
                    email.setCellValue(emailID);
                    accessExpiry.setCellValue(now.plusDays(20).format(formatter));
                }
                break;
        }

        String savePath = System.getProperty("user.dir") + "\\src\\main\\resources\\ExcelFiles\\" + type.replace(" ", "_") + ".xlsx";
        out = new FileOutputStream(savePath);
        workbook.write(out);
        out.close();

        variableContext.setScenarioContext("USERKEYWORD", userkeyword);
        variableContext.setScenarioContext("FILEKEYWORD", type.replace(" ", "_"));
        variableContext.setScenarioContext("EXCELSHEETPATH", savePath);
        variableContext.setScenarioContext("EXCELFILECLEANUPREQUIRED", "TRUE");
    }

    public String[] getPDFData(String filename) throws IOException, InterruptedException {
        Thread.sleep(20000);
        File dir = new File(Paths.get("").toAbsolutePath() + File.separator + "Downloads");
        File[] files = dir.listFiles();
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        String selectedFile = "";
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.toString().contains(filename) && file.toString().contains(".pdf")) {
                selectedFile = file.toString();
                logger.detail("LATEST SELECTED PDF FILE " + selectedFile);
                break;
            }
        }
        if(selectedFile=="")
            testStepAssert.isFail(filename + " PDF not found in downloads");
        return pdfHelper.extractPDF(selectedFile);
    }

    public List<Object> getExcelData(String filename) throws IOException, InterruptedException {
        Thread.sleep(20000);
        File dir = new File(Paths.get("").toAbsolutePath() + File.separator + "Downloads");
        File[] files = dir.listFiles();
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        String selectedFile = "";
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.toString().contains(filename) && file.toString().contains(".xlsx")) {
                selectedFile = file.toString();
                logger.detail("LATEST SELECTED EXCEL FILE " + selectedFile);
                break;
            }
        }
        if(selectedFile=="")
        testStepAssert.isFail(filename + " excel not found in downloads");
        return excelHelper.extractExcel(selectedFile);

    }


    public String getStatusCodeForBulkPermissionAssign(String urlGiven,int reqNumber) throws IOException {
        LogEntries les = DriverContext.getObject().getDriver().manage().logs().get(LogType.PERFORMANCE);

        ArrayList abc = new ArrayList();

        for (LogEntry le : les) {
            String check = le.toString();
            if(check.contains(urlGiven) && check.contains("Network.responseReceived")){
                abc.add(le.toJson().get("message").toString());
            }
        }

        JSONObject jsonObject = new JSONObject(abc.get(reqNumber).toString());

        String code = jsonObject.getJSONObject("message").getJSONObject("params").getJSONObject("response").get("status").toString();
        return code;
    }
}


