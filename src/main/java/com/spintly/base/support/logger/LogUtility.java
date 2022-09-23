package com.spintly.base.support.logger;

import com.spintly.base.managers.ResultManager;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class LogUtility {

    public LogUtility(Object owner) {
        this.owner = owner;
        }

    public LogUtility() {
        this("<Null>");
    }

    public void debug(Object... varargs) {
        logger.debug(CLASS+DELIM+((Class) owner).getSimpleName() + DELIM + toString(varargs));
    }

    public String getLogFileName() {
    	FileAppender appender = (FileAppender)logger.getRootLogger().getAppender("file");
    	return appender.getFile();
    	}

    public void trace(Object... varargs) {
        logger.trace(CLASS+DELIM+((Class) owner).getSimpleName() + DELIM + toString(varargs));
    }

    public void detail(Object... varargs) {
        logger.info(CLASS+DELIM+((Class) owner).getSimpleName() + DELIM + toString(varargs));
    }

    public void warning(Object... varargs) {
        logger.warn(CLASS+DELIM+((Class) owner).getSimpleName() + DELIM + toString(varargs));
    }

    public void error(Object... varargs) {
        logger.error(CLASS+DELIM+((Class) owner).getSimpleName() + DELIM + toString(varargs));
        ResultManager.setStacktrace(toString(varargs));
    }

    public void handleError(String msg, Object... varargs) {
        Exception rootCause = null;
        if (varargs.length != 0) {
            if (varargs[0] instanceof Exception) {
                rootCause = (Exception) varargs[0];
            }
        }
        String text = "Error: " + msg + DELIM + toString(varargs);
        error(text);

        if (rootCause != null ) {
            error(msg, rootCause.getStackTrace());
        }
    }

    public void handleError(String msg) throws Exception {
        error("Error: " + msg + DELIM + "Re-throwing exception");
        throw new Exception() ;
    }

    private static final String DELIM = "|";
    private static final String CLASS = System.getProperty("runner.class").equalsIgnoreCase("RunSuite")?"": System.getProperty("runner.class");
    private static String toString(Object... varargs) {
        String s = "", delim = "";
        for (Object obj : varargs) {
            s += delim + obj.toString();
            delim = DELIM;
        }
        return s;
    }
    private Logger logger = Logger.getLogger(this.getClass());
    private Object owner;

}
