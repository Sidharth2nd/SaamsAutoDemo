package com.spintly.base.support.selenium;

import com.spintly.base.core.DriverBase;
import com.spintly.base.core.DriverContext;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class ScreenshotUtility extends DriverBase {

	public String screenshot(String path_screenshot) throws IOException  {
		try {
		File srcFile = ((TakesScreenshot) DriverContext.getObject().getDriver()).getScreenshotAs(OutputType.FILE);
	    File targetFile=new File(path_screenshot+"/" + UUID.randomUUID().toString() +".jpg");
			FileUtils.copyFile(srcFile,targetFile);
			return targetFile.getName();
		}catch (Exception e){
			logger.detail("Problem capturing screenshots");
			return "";
		}

	}
}
