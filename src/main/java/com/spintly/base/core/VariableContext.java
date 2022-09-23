package com.spintly.base.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableContext {

    private Map<String, Object> featureContext;
    private  Map<String, Object> scenarioContext;

    private static VariableContext contextObject;

    public VariableContext(){
        featureContext = new HashMap<>();
        scenarioContext = new HashMap<>();
    }

    public static VariableContext getObject(){
        if(contextObject==null){contextObject= new VariableContext();}
        return contextObject;
    }

    public void setScenarioContext(String key, Object value) {
        scenarioContext.put(key.toString(), value);
    }

    public Object getScenarioContext(String key){
        Object value;
        if(scenarioContext.containsKey(key)) {
            if(scenarioContext.get(key.toString()) instanceof List)
             value = scenarioContext.get(key.toString());
            else
                value = scenarioContext.get(key.toString()).toString();
            return value;
        }
        else {
            return "";
        }
             
    }

    //Get scenario object withouth converting to string
    public Object getScenarioContextObject(String key){
        if(scenarioContext.containsKey(key)) {
            Object value = scenarioContext.get(key.toString());
            return value;
        }
        else {
            return "";
        }

    }

    public Boolean isScenarioContextContains(String key){
        return scenarioContext.containsKey(key.toString());
    }

    public  void clearScenarioContext(){
        scenarioContext = new HashMap<>();

    }

    public void setFeatureContextContext(String key, Object value) {
        featureContext.put(key.toString(), value);
    }

    public Object getFeatureContextContext(String key){
        if(featureContext.containsKey(key))
            return featureContext.get(key.toString());
        else
            return "";
    }

    public Boolean isFeatureContextContains(String key){
        return featureContext.containsKey(key.toString());
    }

    public String toString(){
        return "Scenario Context:"+scenarioContext.toString() +"Feature Context:"+featureContext.toString();
    }
    

}
