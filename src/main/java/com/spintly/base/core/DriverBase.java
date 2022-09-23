package com.spintly.base.core;
import com.spintly.base.managers.TestAssert;
import com.spintly.base.managers.TestVerify;
import com.spintly.base.support.exception.ExceptionHandler;
import com.spintly.base.support.logger.LogUtility;

public class DriverBase {
    public TestAssert testStepAssert;
    public TestVerify testStepVerify;
    public VariableContext variableContext;
    public static LogUtility logger;
    public ExceptionHandler exception;

    public DriverBase() {
        testStepAssert = new TestAssert();
        testStepVerify = new TestVerify();
        variableContext = VariableContext.getObject();
        logger = new LogUtility(DriverBase.class);
        exception = new ExceptionHandler();
    }
}
