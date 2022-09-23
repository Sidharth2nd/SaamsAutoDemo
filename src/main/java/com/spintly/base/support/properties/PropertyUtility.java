package com.spintly.base.support.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtility {
    private static final String FILE_LOCATION_PROPERTY_FILE = "/Properties/User/resourcesFilePaths.properties";
    private static final String CONFIG_PROPERY_FILE = "/Properties/User/config.properties";
    private static final String RESULT_CONFIG_PROPERTY_FILE = "/Properties/System/resultConfig.properties";
    private static String DATA_PROPERTY_FILE = "" ;
    private static String LOGIN_PROPERTY_FILE = "" ;

    private static Properties properties;
    private static Properties fileLocations;
    private static Properties data;
    private static Properties resultConfig;
    private static Properties loginData;

    public static  String targetPlatform="",environment="";

    public static void loadProperties() {
        properties = new Properties();
        System.out.println(CONFIG_PROPERY_FILE);
        try (InputStream inputStream = PropertyUtility.class.getResourceAsStream(CONFIG_PROPERY_FILE)) {
            properties.load(inputStream);
            if(!environment.equals("") && !targetPlatform.equals("")){properties.setProperty("target.platform", targetPlatform);properties.setProperty("environment", environment);}
        } catch (IOException e) {
            System.err.println(e);
        }
        fileLocations = new Properties();
        try (InputStream inputStream = PropertyUtility.class.getResourceAsStream(FILE_LOCATION_PROPERTY_FILE)) {
            fileLocations.load(inputStream);
        } catch (IOException e) {
            System.err.println(e);
        }
        LOGIN_PROPERTY_FILE=fileLocations.getProperty("login.properties.file").replace("{ENVT}",properties.getProperty("environment").toUpperCase());
        loginData = new Properties();
        try (InputStream inputStream = PropertyUtility.class.getResourceAsStream(LOGIN_PROPERTY_FILE)) {
            loginData.load(inputStream);
        } catch (IOException e) {
            System.err.println(e);
        }
        DATA_PROPERTY_FILE=fileLocations.getProperty("data.properties.file").replace("{ENVT}",properties.getProperty("environment").toUpperCase());
        data = new Properties();
        try (InputStream inputStream = PropertyUtility.class.getResourceAsStream(DATA_PROPERTY_FILE)) {
            data.load(inputStream);
        } catch (IOException e) {
            System.err.println(e);
        }
        data.putAll(loginData);
        resultConfig = new Properties();
        try (InputStream inputStream = PropertyUtility.class.getResourceAsStream(RESULT_CONFIG_PROPERTY_FILE)) {
            resultConfig.load(inputStream);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static String getProperty(String key) {
        if ((key == null) || key.isEmpty()) {
            return "";
        } else {
            return properties.getProperty(key);
        }
    }

    public static String getFileLocations(String key) {
        if ((key == null) || key.isEmpty()) {
            return "";
        } else {
            return fileLocations.getProperty(key);
        }
    }

    public static String getDataProperties(String key) {
        if ((key == null) || key.isEmpty()) {
            return "";
        } else {
            return data.getProperty(key);
        }
    }

    public static String getResultConfigProperties(String key) {
        if ((key == null) || key.isEmpty()) {
            return "";
        } else {
            return resultConfig.getProperty(key);
        }
    }

    //Change the default org property
    public static void changeDefaultOrganization(String org){
        data.setProperty("organization.title",org);
    }

    //Change org role ID
    public static void changeOrgRoleID(String org){
        switch (org){
            case "Custom Attributes":
                data.setProperty("role.endUser","259");
                data.setProperty("role.manager","260");
                data.setProperty("role.endUser","261");
                data.setProperty("role.frontdesk","262");
                break;
        }
    }

    //Set access point values in data.prop file
    public static void setAccessPointsId(String ID){
        data.setProperty("accesspoints",ID);
    }

}
