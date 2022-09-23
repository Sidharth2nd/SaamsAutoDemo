package com.spintly.base.support.reports;
import com.spintly.base.core.DriverBase;
import com.spintly.base.core.VariableContext;
import static com.spintly.base.managers.ResultManager.failureStep;
import com.spintly.base.support.cucumberEvents.StepsStore;
import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.RandomDataGenerator;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

public class ReportDesigner extends DriverBase {
	private String detailsFolderPath,screenshotFolder,miscFolder,logFolder;
	private Writer bufWriter1, fileWriter;
	private ArrayList<String> detailsArray = new ArrayList<String>();
	private ArrayList<String> summaryArray = new ArrayList<String>();
	private ArrayList<String> stackTraceArray = new ArrayList<String>();
	ArrayList<String> failureArray = new ArrayList<String>();
	private final static String logoPath =PropertyUtility.getResultConfigProperties("MISC_DIRECTORY")+"/"+PropertyUtility.getResultConfigProperties("LOGO_FILENAME");
	private final static String downloadPath =PropertyUtility.getResultConfigProperties("MISC_DIRECTORY")+"/"+ PropertyUtility.getResultConfigProperties("DOWNLOAD_LOGO");

	private static LogUtility logger = new LogUtility(ReportDesigner.class);
	private int testCases = 1;
	private int failed = 0;
	private int passed = 0;
	private int inconclusive=0;
	private int skipped =0;
	private int testStepCount=0;
	private Date  testFinish,suiteFinish, startTime = null;
	private Date testStepStart, testStepEnd;
	private String tcName;
	private String featureName;
	private String reason;
	private String tags;
	private boolean isTcVerifyFailed;
	String uniqueId = "";
	String mainFolderName = "";

	private static Date suiteStartTime, suiteEndTime;

	public ReportDesigner(String detailsFolderPath, String screenshotFolder, String miscFolder, String logFolder){
		this.detailsFolderPath=detailsFolderPath;
		this.screenshotFolder=screenshotFolder;
		this.miscFolder=miscFolder;
		this.logFolder=logFolder;
		this.mainFolderName = System.getProperty("MAINFOLDER");
	}

	public void start() throws Exception {
		storeStartTime(new Date().toString());
	}

	public void end() {
		try{
			createResultFileFromTemplate();
		} catch (Exception e) {
		}
	}
	public void createResultFileFromTemplate(){
		try {
			suiteFinish = new Date();
			storeEndTime(suiteFinish.toString());

			File result;
			BufferedReader br ;
			br = new BufferedReader(new InputStreamReader(ReportDesigner.class.getResourceAsStream("/" + "Templates/newresulttemplate.html")));

			if(System.getProperty("Parallel").toString().equals("true")) {
				result = new File(detailsFolderPath + this.featureName.replace(".feature", "").replace(" ", "") + ".html");

			}else {
				result = new File(detailsFolderPath + "combinedfeatureReport.html");
			}String s;
			String totalStr = "";

			while ((s = br.readLine()) != null) {
				totalStr += s;
			}
			totalStr = totalStr.replaceAll("<!--LOGO.PATH-->",logoPath);
			String session = (String) variableContext.getScenarioContext("SESSION");
			if(session =="")
				totalStr = totalStr.replaceAll("<!--FEATURE.NAME-->",this.featureName);
			else
				totalStr = totalStr.replaceAll("<!--FEATURE.NAME-->",this.featureName+" | Session ID : "+session);
			totalStr = totalStr.replaceAll("<!--SUMARRY-->", Matcher.quoteReplacement(getLogDetails(summaryArray)));
			totalStr = totalStr.replaceAll("<!--DETAILS-->", Matcher.quoteReplacement(getLogDetails(detailsArray)));
			totalStr = totalStr.replaceAll("<!--PASSED.COUNT-->",passed+"");
			totalStr = totalStr.replaceAll("<!--FAILED.COUNT-->",failed+"");
			totalStr = totalStr.replaceAll("<!--INCONCLUSIVE.COUNT-->",inconclusive+"");
			totalStr = totalStr.replaceAll("<!--SKIPPED.COUNT-->",skipped+"");
			totalStr = totalStr.replaceAll("<!--START.TIME-->", suiteStartTime + "");
			totalStr = totalStr.replaceAll("<!--END.TIME-->", suiteEndTime + "");
			totalStr = totalStr.replaceAll("<!--TOTAL.TIME-->", calculateDuration(suiteEndTime,suiteStartTime) + "");
			totalStr = totalStr.replaceAll("<!--FAILURE.DETAILS-->",Matcher.quoteReplacement(getLogFailureData(failureArray)));

			FileWriter fw = new FileWriter(result);
			fw.write(totalStr);
			fw.close();
		} catch (IOException e) {
		}
	}
	public void startTestCase(String tcName , String featureName , String tags) {
		this.tcName = tcName.replace(",","");
		this.featureName = featureName;
		this.startTime = new Date();
		//storeStartTime(this.startTime.toString());
		this.isTcVerifyFailed=false;
		this.testStepCount=0;
		this.reason="";
		this.tags= tags;
		addTestCaseEntryInDetailsTable(tcName, featureName);
		StepsStore.resetNumberOfSteps();
	}

	public void addTestCaseEntryInDetailsTable(String name, String featureName) {
		uniqueId = RandomDataGenerator.getData("{RANDOM_STRING}",10);
		name= name.replace(",","");

		String str = "<div class=\"table"+uniqueId+" testcaselistname\" style=\"display: none;\">\n" +
				"            <p class=\"tagname\">"+name+"</p>\n" +
				"            <p class=\"taglist\">Tags : "+tags+"</p>\n" +
				"        </div>\n" +
				"        <div class=\"scrolling-div table"+uniqueId+"\">\n" +
				"            <table class=\"step-details table"+uniqueId+"\" style=\"display: none;\">\n" +
				"                <thead>\n" +
				"                    <tr>\n" +
				"                        <th>Sr. No</th>\n" +
				"                        <th>Test Step</th>\n" +
				"                        <th>Step Result</th>\n" +
				"                        <th>Expected Result</th>\n" +
				"                        <th class=\""+uniqueId+"\">\n" +
				"                            Actual Result\n" +
				"                            <button id=\""+uniqueId+"\" type=\"button\" class=\"btn btn-primary cancelbutton\" onclick=\"closeSteps(this)\">Close</button>\n" +
				"                        </th>\n" +
				"                    </tr>\n" +
				"                </thead>\n" +
				"                <tbody>";

		//String str = "<tr class='header'><td colspan='5' align='left' style='font-weight: bold'>Scenario : "+ name +" - Tags : "+tags+"</td></tr>"; ;
		detailsArray.add(str);
		stackTraceArray.clear();
		this.reason="";
		logger.detail("SCENARIO : "+testCases+" OF FEATURE : "+ featureName);
	}

	public void addTestData(Map<String, String> eventData) {

		testStepStart = testStepEnd == null ? startTime : testStepEnd;
		testStepEnd = new Date();
		int stepCount= testStepCount+1;
		String reason = screenDumpLink((String) eventData.get("actual"), eventData);
		String str = "<tr>";

		str = str + "<td>"+stepCount+".</td>";
		str = str + "<td>"+eventData.get("name").toString()+"</td>";
		if (eventData.get("type").toString() == "PASSED") {
			str = str + "<td style='background-color:#54b33c94;'>" + eventData.get("type").toString() + "</td>";
		}
		else if (eventData.get("type").toString() == "WARNING") {
			str = str + "<td style='background-color:orange;'>" + eventData.get("type").toString() + "</td>";
		}
		else {
			str = str + "<td style='background-color:red;'>" + eventData.get("type").toString() + "</td>";
		}

		str = str + "<td>" + eventData.get("expected").toString() + "</td>";
		str = str + "<td>" + reason + "</td>";
		str = str + "</tr>";

		detailsArray.add(str);
		if (eventData.get("type").toString() != "PASSED") {
			detailsArray.addAll(stackTraceArray);
			this.reason = reason;
			stackTraceArray.clear();
		}
		testStepCount++;
	}

	public void addStackTrace(Map<String, String> eventData) {
		if(eventData.get("actual").toString()!= "") {
			String str = "<tr><td + rightSpan + ></td>";
			str = str + "<td colspan=8 align='left'>Error Log : <div class='maincontent'><div class='content'>" + eventData.get("actual").toString() + "</div><div class='txtcol'><a>Show More</a></div></div></td>";
			stackTraceArray.add(str);
		}
		else {stackTraceArray.add("");}
	}

	private String screenDumpLink(String message, Map<String, String> eventData) {
		String sDumpFile;
		try {
			sDumpFile = (String) (eventData.get("screenDump"));

		} catch (Exception e) {

			return message;
		}

		if (sDumpFile == null) {
			return message;
		}


		String path = sDumpFile.replace('\\', '/');
		logger.detail("Screenshot Path :  "+ path );
		String str  = "<div style=\"color:red; font-weight: bold\" class=\""+uniqueId+"\">\n" +
				"                                <img alt=\""+this.mainFolderName+"\"class=\"screenshots\" src=\""+downloadPath+"\" onclick=\"showScreeshot(this)\"> \"View screenshot here\"\n" +
				"                            </div>\n" +
				"                            <div class=\"modal"+uniqueId+"\" style=\"display: none; position: fixed; z-index: 1; padding-top: 100px; left: 0; top: 0; width: 100%; height: 100%; overflow: auto; background-color: rgba(0,0,0,0.9);\">\n" +
				"                                <button id=\""+uniqueId+"\" class=\"close\" onclick=\"closeScreenshot(this)\">x</button>\n" +
				"                                <img class=\"modal-content screenshots\" src=\""+path+"\" alt=\""+this.mainFolderName+"\" style=\"width:1000px\">\n" +
				"                                <div class=\"caption\">"+path+"</div>\n" +
				"                            </div>";

		return message +str;
	}

	public void endTestCase(boolean isFailed, boolean isSkipped) {
		String str1;
		testCases++;
//		int caseCount = 0;
//		caseCount = caseCount +1;
		testFinish = new Date();
		String str = "";
		String status = "";
		if(isSkipped)
		{
			skipped++;
			status = "<td class=\"status\">\n" +
					"                    <div class=\"indicator\" style=\"background-color: skyblue;\">Skipped</div>\n" +
					"                </td>";
			str = "<td>" + this.startTime + "</td> <td>" + this.testFinish + "</td> <td>" + calculateDuration(this.testFinish, this.startTime);
			str1 = "<tr><td class=\"casenumber\">"+(testCases-1)+".</td>"+"<td>\n" +
				"                    <p>"+tcName+"</p>\n" +
				"                    <p>Tags : "+tags+"</p>\n" +
				"                </td>" + status + str+"</td><td class=\""+uniqueId+"\"><button type=\"button\" class=\"btn btn-primary\" onclick=\"showSteps(this)\">Show Steps</button></td></tr>";
			summaryArray.add(str1);
		}
		else {
			//check testng assert and local flag as well
			if (!isFailed && !isTcVerifyFailed) {
				status = "<td class=\"status\">\n" +
						"                    <div class=\"indicator\" style=\"background-color: #54b33c94;\">Pass</div>\n" +
						"                </td>";
				passed++;
			} else {
				try {
					if (this.reason.equalsIgnoreCase("")) {
						String cause = (String) variableContext.getScenarioContext("ERROR");
						String step = (String) variableContext.getScenarioContext("STEP");
						this.reason = cause;
						if (cause != "")
							failureStep(step, "Step Should be successful", (String) variableContext.getScenarioContext("ERROR"), true);
					}
				} catch (Exception ex) {
				}
				String reason = this.reason;
				String st = "<tr><td + rightspan+ ><td colspan='7' style='text-align: left;'>NOTE: Some steps are SKIPPED or FAILED. Please refer to logs for more details</td></tr>";
				if (reason == "") {
					VariableContext.getObject().setScenarioContext("FAILURE", "TRUE");
					status = "<td class=\"status\">\n" +
							"                    <div class=\"indicator\" style=\"background-color: skyblue;\">Inconclusive</div>\n" +
							"                </td>";
				} else if (((String) VariableContext.getObject().getScenarioContext("PASS_WITH_OBSERVATIONS")).equals("TRUE")) {
					passed++;
					status = "<td class=\"status\">\n" +
							"                    <div class=\"indicator\" style=\"background-color: orange;\">Observations</div>\n" +
							"                </td>";
				} else {
					failed++;
					status = "<td class=\"status\">\n" +
							"                    <div class=\"indicator\" style=\"background-color: red;color:white;font-weight:700;\">Fail</div>\n" +
							"                </td>";
				}
				detailsArray.add(st);
				String str2 = "<td>*</td><td align='left'>" + tcName + "</td>" + status + "<td align='left'>" + reason + "</td>";
				failureArray.add(str2);
				failureArray.addAll(stackTraceArray);

			}
			str =  "<td>" + this.startTime
					+ "</td><td>" + this.testFinish + "</td><td>" + calculateDuration(this.testFinish, this.startTime);

			str1 = "<tr><td class=\"casenumber\">"+(testCases-1)+".</td>"+"<td>\n" +
					"                    <p>"+tcName+"</p>\n" +
					"                    <p>Tags : "+tags+"</p>\n" +
					"                </td>" + status + str+"</td><td class=\""+uniqueId+"\"><button type=\"button\" class=\"btn btn-primary\" onclick=\"showSteps(this)\">Show Steps</button></td></tr>";
			summaryArray.add(str1);
		}

		String closeTable = "</tbody>\n" +
				"            </table>\n" +
				"        </div>";

		detailsArray.add(closeTable);
	}

	public void verificationFailed(Map<String, String> eventData){
		this.isTcVerifyFailed=true;
		endTestDataContainer(eventData);
		logger.trace("Marked test case :"+this.tcName +" failed as verification got failed"  );
	}

	private String calculateDuration(Date d2, Date d1) {
		long diff = d2.getTime() - d1.getTime();

		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		try {
			String diffTime = cal(String.valueOf(diffHours)) + ":" + cal(String.valueOf(diffMinutes)) + ":"
					+ cal(String.valueOf(diffSeconds));
			return diffTime;
		} catch (Exception e) {
			logger.handleError("Error in calculating duration in HTML report", e);
			return null;
		}

	}

	private String cal(String time) {
		while (time.length() != 2)
			time = "0" + time;
		return time;
	}

	private void closeDetailsFile() {
		try {
			bufWriter1.close();
		} catch (IOException e) {
			logger.handleError("Exception caught while closing details file ", e);
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				logger.handleError("Exception caught while closing details file ", e);
			} finally {
				fileWriter = bufWriter1 = null;
			}
		}
	}

	private void writeDetails(String lines) {
		try {
			bufWriter1.write(lines);
		} catch (IOException e) {
			logger.handleError("Exception caught while writing details in HTML : ", e);
		}
	}
	public boolean isScenarioFailed(){
		return this.isTcVerifyFailed;
	}

	public int inconclusive(){
		inconclusive++;

		return inconclusive;
	}
	public int skipped(){
		return skipped;
	}
	private String getLogDetails(ArrayList<String> strArray) {
		String strDetails = "";
		for (String str : strArray)
			strDetails+= str;
		final String cleansedString = StringUtils.normalizeSpace(strDetails);
		return strDetails;
	}
	private String getLogFailureData(ArrayList<String> strArray) {
		String strDetails = "";
		for (String str : strArray)
			strDetails+="<tr class='header'>" + str + "</tr>";
		final String cleansedString = StringUtils.normalizeSpace(strDetails);
		return strDetails;
	}

	public void endTestDataContainer(Map<String, String> eventData)
	{
	}

	public static void storeStartTime(String time) {
		try {
			if(time!="") {
				DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyyy", Locale.ENGLISH);
				Date date = format.parse(time);
				if (suiteStartTime == null)
					suiteStartTime = date;

				if (suiteStartTime.after(date)) {
					suiteStartTime = date;
				}
			}
		} catch (Exception e) {
		}
	}

	public static void storeEndTime(String time) {
		try {
			if(time!="") {
				DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyyy", Locale.ENGLISH);
				Date date = format.parse(time);
				if (suiteEndTime == null)
					suiteEndTime = date;

				if (suiteEndTime.before(date)) {
					suiteEndTime = date;
				}
			}
		} catch (Exception e) {

		}
	}
}
