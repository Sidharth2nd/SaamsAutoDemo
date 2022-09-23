package com.spintly.base.utilities;
import com.spintly.base.core.DriverBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileUtility extends DriverBase {
	public static String autoHome ;

	public static boolean copyFile(String sPath, String dPath){
		Path FROM = Paths.get(sPath);
		Path TO = Paths.get(dPath);
		CopyOption[] options = new CopyOption[]{
		  StandardCopyOption.REPLACE_EXISTING,
		  StandardCopyOption.COPY_ATTRIBUTES
		};
		try {
			Files.copy(FROM, TO, options);
			return true;
		} catch (IOException e) {
			logger.error("Exception caught in coping file :"+e.getMessage());
			return false;
		}
	}

	public static boolean makeFolder(String sFolderPath) {
		if (!new File(sFolderPath).isDirectory())
			return new File(sFolderPath).mkdirs();
		else
			return true;
	}

	private static String createPath(String home, String relPath, String relName) {
		if (!relPath.equals(""))
			home = home + "/" + relPath;
		if (!relName.equals(""))
			home = home + "/" + relName;
		return home;
	}
	public static String getSuiteResource(String relPath, String relName) {
		return createPath(autoHome, relPath, relName);
	}
	private FileUtility() {}

}
