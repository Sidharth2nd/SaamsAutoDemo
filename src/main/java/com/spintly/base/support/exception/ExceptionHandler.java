package com.spintly.base.support.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import static com.spintly.base.managers.ResultManager.error;

public class ExceptionHandler  {
    public void log(Exception e, String... actual)
    {
        String actualMessage = "";
        actualMessage= actual.length==0? "RUNTIME ERROR : Please check logs for more details" :actual[0];
        error("Step should be successful", actualMessage,ExceptionUtils.getStackTrace(e), true);
    }
}
