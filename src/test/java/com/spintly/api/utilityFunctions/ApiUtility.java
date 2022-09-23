package com.spintly.api.utilityFunctions;


import com.spintly.base.core.DriverContext;
import com.spintly.base.support.properties.PropertyUtility;
import com.spintly.base.utilities.ApiHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Augmenter;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings({"All"})
public class ApiUtility {

    public String getTokenFromLocalStorage()
    {
        //WebStorage webStorage = (WebStorage) new Augmenter().augment(DriverContext.getObject().getDriver());
        WebStorage webStorage = (WebStorage) DriverContext.getObject().getDriver();
        LocalStorage localStorage = webStorage.getLocalStorage();
        String keyName = "", value ="";
        Iterator iterator = localStorage.keySet().iterator();
        while(iterator.hasNext()) {
            keyName = (String) iterator.next();
            if (keyName.contains("accessToken")) {
                value = localStorage.getItem(keyName);
                break;
            }
        }
        return value;
    }
    public String getidTokenFromLocalStorage()
    {
        //WebStorage webStorage = (WebStorage) new Augmenter().augment(DriverContext.getObject().getDriver());
        WebStorage webStorage = (WebStorage) DriverContext.getObject().getDriver();
        LocalStorage localStorage = webStorage.getLocalStorage();
        String keyName = "", value ="";
        Iterator iterator = localStorage.keySet().iterator();
        while(iterator.hasNext()) {
            keyName = (String) iterator.next();
            if (keyName.contains("idToken")) {
                value = localStorage.getItem(keyName);
                break;
            }
        }
        return value;
    }
    public int getDayNum(String day)
    {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] weekdays = dfs.getWeekdays();
        int index = 0;
        for (int i= 0; i< weekdays.length;i++) {
            if(weekdays[i].equalsIgnoreCase(day)) {
                if (weekdays[i].equalsIgnoreCase("sunday"))
                    return 6;
                else
                    return i-2;
            }
            index++;
        }
        return 0;
    }

    public String getOrgnizationID() throws ParseException {
        String token = getTokenFromLocalStorage();
        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url")+"/v2/organisationManagement/organisations");
        JsonPath jsonPathEvaluator = response.jsonPath();
        ArrayList<LinkedHashMap<String, String>> organizations = jsonPathEvaluator.get("message.organisations");
        for (LinkedHashMap organizaton : organizations) {
            String orgname = organizaton.get("name").toString();
            if (organizaton.get("name").equals(PropertyUtility.getDataProperties("organization.title"))) {
                return organizaton.get("orgId").toString();
            }
        }
        return "";
    }

    public String getOrgNameFromID(String id){
        String token = getTokenFromLocalStorage();
        List<String> orgIDs = new ArrayList<>();
        String orgName = "";

        Response response = ApiHelper.givenRequestSpecification()
                .header("authorization", token)
                .when().redirects().follow(false).
                get(PropertyUtility.getDataProperties("base.api.url")+"/v2/organisationManagement/organisations");
        JsonPath jsonPathEvaluator = response.jsonPath();

        orgIDs = jsonPathEvaluator.get("message.organisations.orgId");

        for(int i =0;i<orgIDs.size();i++){
            if(String.valueOf(orgIDs.get(i)).equalsIgnoreCase(id)){
                orgName = jsonPathEvaluator.get("message.organisations["+i+"].name");
                break;
            }
        }

        return orgName;
    }
}
