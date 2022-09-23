package com.spintly.base.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spintly.base.core.DriverBase;
import com.spintly.base.managers.ResultManager;
import com.spintly.base.managers.TestAssert;
import com.spintly.base.support.logger.LogUtility;
import io.restassured.config.DecoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.net.URL;
import java.net.URLConnection;

import static io.restassured.RestAssured.given;

public class ApiHelper extends DriverBase {
    private static Gson gson;
    private static LogUtility logger = new LogUtility(ApiHelper.class);

    public static Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gson(gsonBuilder);
        return gson;
    }

    public static Gson gson(GsonBuilder gsonBuilder) {
        gson = gsonBuilder.create();
        return gson;
    }
    public static RequestSpecification givenRequestSpecification() {
        return given()
        .config(RestAssuredConfig.config().decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8")).and().sslConfig(new SSLConfig().relaxedHTTPSValidation()))//.log().body()
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Content-Type", "application/json")
                .header("Accept-Encoding", "gzip, deflate")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");
    }

    public static void genericResponseValidation(Response response, String RequestText) {
        JsonPath jsonPathEvaluator;
        try {
            jsonPathEvaluator = response.jsonPath();
            String outcome = jsonPathEvaluator.get("type");

            if (outcome.equalsIgnoreCase("SUCCESS")) {
                logger.detail(RequestText + " | Response - Success ");
            }
            else
            {   logger.detail(RequestText + " | Response - Failure ");
                logger.detail(response.then().log().body());
                ResultManager.warning("Step should be successful", "ERROR in API CALL for "+ RequestText +" | "+ response.then().log().body(), true);
            }
            response.then().statusCode(200);
        }
        catch (AssertionError ex)
        {

        }


    }

    public void checkInternetConnection() throws InterruptedException {
        while(true){
            boolean go = true;
            try{
                System.out.println("trying internet");
                URL u = new URL("https://www.google.com");
                URLConnection conn = u.openConnection();
                conn.connect();
            }catch (Exception e){
                go = false;
                Thread.sleep(20000);
                continue;
            }
            if(go){
                System.out.println("Internet connection established");
                break;
            }
        }
    }
}