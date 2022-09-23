package com.spintly.base.core;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.utilities.FileUtility;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.support.reports.ReportDesigner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ReportBase extends DriverBase{
	private static ReportDesigner reportDesigner;
	private String detailsFolderPath,screenshotFolder,miscFolder,logFolder;

	public void startSuiteFile(String resultFolderName) throws Exception {
		String resultFolder=GetResultFolderName(resultFolderName);
		this.detailsFolderPath=FileUtility.getSuiteResource(PropertyUtility.getResultConfigProperties("RESULT_DIRECTORY"),resultFolder  );
		System.setProperty("RESULTDIR",resultFolder);
		System.setProperty("RESULTMAINDIR",PropertyUtility.getResultConfigProperties("RESULT_DIRECTORY"));
		this.screenshotFolder=this.detailsFolderPath+PropertyUtility.getResultConfigProperties("SCREENSHOTS_DIRECTORY");
		this.miscFolder=this.detailsFolderPath+PropertyUtility.getResultConfigProperties("MISC_DIRECTORY");
		this.logFolder=this.detailsFolderPath+PropertyUtility.getResultConfigProperties("LOG_DIRECTORY");
		reportDesigner= new ReportDesigner(this.detailsFolderPath,this.screenshotFolder,this.miscFolder,this.logFolder);
		new ResultManager(this);
		createInitialFolder();
		reportDesigner.start();
	}

	private void createInitialFolder() throws Exception{
		try {
			FileUtility.makeFolder(this.detailsFolderPath);
			FileUtility.makeFolder(this.screenshotFolder);
			FileUtility.makeFolder(this.miscFolder);
			FileUtility.copyFile(FileUtility.getSuiteResource(PropertyUtility.getResultConfigProperties("LOGO_DIRECTORY"), PropertyUtility.getResultConfigProperties("LOGO_FILENAME")),this.miscFolder+"/"+PropertyUtility.getResultConfigProperties("LOGO_FILENAME"));
			FileUtility.copyFile(FileUtility.getSuiteResource(PropertyUtility.getResultConfigProperties("LOGO_DIRECTORY"), PropertyUtility.getResultConfigProperties("DOWNLOAD_LOGO")),this.miscFolder+"/"+PropertyUtility.getResultConfigProperties("DOWNLOAD_LOGO"));
		} catch (Exception e) {
				logger.handleError("Error while creating HTML report folder", e);
			}
	}
	public static String GetResultFolderName(String resultFolderName) {
		//logger.detail("-----------------------------------------------------------------------------------------------------------------------------------------Result Folder name "+ resultFolderName);
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date now = new Date();
		String parentFolder = resultFolderName.equals("")?"":resultFolderName+"/";
		String strDate = parentFolder+PropertyUtility.getResultConfigProperties("RESULT_FOLDER_INITIAL").trim() + sdfDate.format(now)+"_"+System.getProperty("runner.class")+"/";
		if(!(parentFolder.equalsIgnoreCase(""))){
			String mainFolder = strDate.substring(27);
			System.setProperty("MAINFOLDER",mainFolder);
		}else{
			System.setProperty("MAINFOLDER",strDate);
		}
		return strDate;
	}
	public String getTestResultFolderName(){
		return this.detailsFolderPath;
	}
	public String getTestScreenShotFolderName(){
		return this.screenshotFolder;
	}
	public void startTestCase(String tcName, String featureName, String tags) {
		reportDesigner.startTestCase(tcName, featureName, tags);
	}
	public void endTestCase(boolean isFailed, boolean isSkipped){
		reportDesigner.endTestCase(isFailed,isSkipped);
	}

	public void verificationFailed(Map<String, String> eventData){
		reportDesigner.verificationFailed(eventData);
	}
	public boolean isVerificationFailed(){
		return reportDesigner.isScenarioFailed();
	}
	public int inconclusive(){
		return reportDesigner.inconclusive();
	}
	public int skipped(){
		return reportDesigner.skipped();
	}
	public void addTestData(Map<String, String> eventData){
		reportDesigner.addTestData(eventData);
	}
	public void addStackTrace(Map<String, String> eventData){
		reportDesigner.addStackTrace(eventData);
	}

	public void endSuiteFile() {
		try {
			reportDesigner.end();
			copyLogFile();
		}
		catch (Exception ex) {}
	}

	public void copyLogFile(){
		FileUtility.makeFolder(this.logFolder);
		FileUtility.copyFile(FileUtility.getSuiteResource(logger.getLogFileName(), ""), this.logFolder + "/ConsoleLog.log");
	}
}
