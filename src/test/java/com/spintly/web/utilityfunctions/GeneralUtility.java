package com.spintly.web.utilityfunctions;

import com.spintly.api.utilityFunctions.ApiUtility;
import com.spintly.base.core.DriverBase;
import com.spintly.base.core.DriverContext;
import com.spintly.base.core.PageBase;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.RandomDataGenerator;

import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ExcelHelper;
import com.spintly.base.utilities.PdfHelper;
import com.spintly.web.pages.usermanagementpage.AllUsersPage;
//import com.spintly.web.pages.usermanagementpage.AllUsersPageAllUsersPage;
import com.spintly.web.support.*;
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


public class GeneralUtility extends DriverBase{

}


