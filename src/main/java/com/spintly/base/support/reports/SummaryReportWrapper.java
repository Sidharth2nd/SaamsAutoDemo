package com.spintly.base.support.reports;

import com.spintly.base.support.logger.LogUtility;
import com.spintly.base.support.properties.PropertyUtility;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

public class SummaryReportWrapper {
    static Path configFilePath;
    private static ArrayList<String> summaryData = new ArrayList<>();
    private static ArrayList<String> detailsArray = new ArrayList<>();
    private static ArrayList<String> failureSummaryData = new ArrayList<>();
    private static int passCount = 0, failCount = 0, inConclusiveCount = 0,skippedCount = 0;
    private static String logoFilePath = "";
    private static Date startTime, endTime;
    //public static LogUtility logger;

    public static void main(String[] args) throws IOException, ParseException {
        try {
            if (args.length > 0) {
                 String mainFolder = args[0];

                //logger.detail("Main Folder"+mainFolder);
                String platform = args[1];
                String category = args[2];
                String environment = args[3];

                configFilePath = Paths.get(mainFolder);
                CopyMiscFolder();
                List<String> listOfResultFile = getListOfResultFile();
                int testCount = 1;
                Boolean isFailed = false;
                for (String path : listOfResultFile) {
                    File in = new File(path);
                    String subFolder = in.getParentFile().getName();
                    //Logo image path
                    if (logoFilePath.equals("")) {
                        PropertyUtility.loadProperties();
                        String relativePath = PropertyUtility.getResultConfigProperties("MISC_DIRECTORY") + "/" + PropertyUtility.getResultConfigProperties("LOGO_FILENAME");
                        logoFilePath = subFolder + "/" + relativePath;
                    }

                    //Copy Screenshots Folder To main folder

                    //Parse HTML file and extract data
                    Document doc = Jsoup.parse(in, null);
                    Element table = doc.select("table[class='parent']").get(0); //select the first table.

                    Elements rows = table.select("tr");
                    int featureTotal = Integer.parseInt(doc.getElementById("pass").val().contains("--") ? "0" : doc.getElementById("pass").val())+Integer.parseInt(doc.getElementById("fail").val().contains("--") ? "0" : doc.getElementById("fail").val())+ Integer.parseInt(doc.getElementById("inconclusive").val().contains("--") ? "0" : doc.getElementById("inconclusive").val())+Integer.parseInt(doc.getElementById("skipped").val().contains("--") ? "0" : doc.getElementById("skipped").val());
                    int featurePass = Integer.parseInt(doc.getElementById("pass").val().contains("--") ? "0" : doc.getElementById("pass").val());
                    int featureFail = Integer.parseInt(doc.getElementById("fail").val().contains("--") ? "0" : doc.getElementById("fail").val());
                    int featureInconclusive = Integer.parseInt(doc.getElementById("inconclusive").val().contains("--") ? "0" : doc.getElementById("inconclusive").val());
                    int featureSkipped = Integer.parseInt(doc.getElementById("skipped").val().contains("--") ? "0" : doc.getElementById("skipped").val());

                    String featureSummary = "[TOTAL : "+featureTotal+" | PASS : "+ featurePass +" | FAIL : "+ featureFail + " | INCONCLUSIVE : " + featureInconclusive + " | SKIPPED : " + featureSkipped+ "]";
                    String str = "<tr>\n" +
                            "                <td colspan=\"6\">FEATURE : "+in.getName().toString().replace(".html", "")+" "+featureSummary+"</td>\n" +
                            "                <td colspan=\"2\" style=\"width: 6px;\"><a href=\""+subFolder + "/" + in.getName()+"\"> EXECUTION REPORT : "+in.getName()+"</a></td>\n" +
                            "            </tr>";
//                    summaryData.add("<tr> </tr>");
//                    summaryData.add(" <td colspan=3 style='text-align:left;'> FEATURE : " + in.getName().toString().replace(".html", "") +" "+ featureSummary+" </td>");
//                    summaryData.add(" <td colspan=3><a href=" + subFolder + "/" + in.getName() + "> EXECUTION REPORT : " + in.getName() + "</td>");
//                    summaryData.add("<tr> </tr>");

                    summaryData.add(str);
                    passCount = passCount + featurePass;
                    failCount = failCount + featureFail;
                    inConclusiveCount = inConclusiveCount + featureInconclusive;
                    skippedCount = skippedCount + featureSkipped;

                    for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
                        Element row = rows.get(i);
                        Elements cols = row.select("td");
                        cols.remove(0);
                        //cols.remove((cols.size()-1));
                        String data = cols.toString();
                        //data = data.replace(cols.get(2).toString(), "");
                        if (i == 1) {
                            String startTime = cols.get(2).text();
                            storeStartTime(startTime);
                        }
                        if (i == rows.size() - 1) {
                            String startTime = cols.get(3).text();
                            storeEndTime(startTime);
                        }
                        summaryData.add("<tr><td class=\"casenumber\">" + testCount + ".</td>");
                        summaryData.add(data+"</tr>");
                        testCount++;
                    }

                    Element detailsDiv = doc.select("div[class='parent-div-test-steps']").get(0);
                    Elements divChildren = detailsDiv.children();

                    for (Element elem : divChildren){
                        detailsArray.add(elem.toString());
                    }

                    if(doc.select("table").size()>2) {
                        Element table2 = doc.select("table").get(2); //select the third hidden table.
                    }
                }
                createResultFileFromSummaryTemplate(platform, category, environment);
                System.out.println("Generated index.html");
                createCountTemplate(platform, category, environment);
                System.out.println("Generated summarycount.html");
                Boolean isParallel = false;
                try{
                    isParallel = System.getProperty("Parallel").toString().equalsIgnoreCase("true");
                }
                catch(Exception e)
                {
                    isParallel = true;
                }
                if(isParallel)
                newName(configFilePath," FinalReport_"+ DateTime.now().toString("ddMMYYYYHHSSms"));
            } else {
                System.err.println("Pass Main folder name of parallel test  as argument");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    static  Path  newName(Path oldName, String newNameString) throws IOException {
        return Files.move(oldName, oldName.resolveSibling(newNameString));
    }

    public static void storeStartTime(String time) {
        try {
            if(time!="") {
                DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyyy", Locale.ENGLISH);
                Date date = format.parse(time);
                if (startTime == null)
                    startTime = date;

                if (startTime.after(date)) {
                    startTime = date;
                }
            }
        } catch (Exception e) {
        }
    }

    public static void storeEndTime(String time) {
        try {
            if(time!="") {
                DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyyy", Locale.ENGLISH);
                Date date = format.parse(time);
                if (endTime == null)
                    endTime = date;

                if (endTime.before(date)) {
                    endTime = date;
                }
            }
        } catch (Exception e) {

        }
    }

    public static List<String> getListOfResultFile() throws IOException {
        List<String> listOfResultFile = Files.walk(configFilePath)
                .filter(s -> s.toString().endsWith(".html")).map((p) -> p.getParent() + "/" + p.getFileName())
                .filter(s -> !s.toString().contains("htmlpublisher-wrapper"))
                .filter(s -> !s.toString().contains("index"))
                .sorted()
                .collect(toList());
        System.out.println(listOfResultFile);
        return listOfResultFile;
    }

    public static List<String> getScreenshots() throws IOException {
        List<String> listOfScreenshotFile = Files.walk(configFilePath)
                .filter(s -> s.toString().endsWith(".jpg")).map((p) -> p.getParent() + "/" + p.getFileName())
                .sorted()
                .collect(toList());
        System.out.println(listOfScreenshotFile);
        return listOfScreenshotFile;
    }

    public static List<String> getLogos() throws IOException {
        List<String> listOfScreenshotFile = Files.walk(configFilePath)
                .filter(s -> s.toString().endsWith(".PNG")).map((p) -> p.getParent() + "/" + p.getFileName())
                .sorted()
                .collect(toList());
        System.out.println(listOfScreenshotFile);
        return listOfScreenshotFile;
    }

    public static void CopyScreenshotsToDirectory() throws IOException {
        try {
            List<String> listOfScreenshotFile = getScreenshots();
            Path targetDirectory = Paths.get(configFilePath+"/Screenshot");
            File directory = new File(String.valueOf(targetDirectory));
            if(!directory.exists()) {
                directory.mkdir();
            }
            for(String file : listOfScreenshotFile) {
                Files.copy(Paths.get(file),
                        (new File(directory +"/"+ Paths.get(file).getFileName().toString())).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void CopyMiscFolder() throws IOException {
        try {
            List<String> listOfScreenshotFile = getLogos();
            Path targetDirectory = Paths.get(configFilePath+"/Misc");
            File directory = new File(String.valueOf(targetDirectory));
            if(!directory.exists()) {
                directory.mkdir();
            }
            for(String file : listOfScreenshotFile) {
                Files.copy(Paths.get(file),
                        (new File(directory +"/"+ Paths.get(file).getFileName().toString())).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                break; //Only single
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void createResultFileFromSummaryTemplate(String platform, String category, String environment) {
        Boolean isParallel = false;
  try{
       isParallel = System.getProperty("Parallel").toString().equalsIgnoreCase("true");
  }
  catch(Exception e)
  {
      isParallel = true;
  }
        try {
            File result;
            if(isParallel)
             result = new File(configFilePath  + "/" + PropertyUtility.getResultConfigProperties("MERGED_SUMMARY_FILE"));
           else
               result = new File(System.getProperty("RESULTMAINDIR") +"/" + PropertyUtility.getResultConfigProperties("MERGED_SUMMARY_FILE"));

            BufferedReader br = new BufferedReader(new InputStreamReader(ReportDesigner.class.getResourceAsStream("/" + "Templates/newsummarytemplate.html")));
            String s;
            String totalStr = "";
            String listString = String.join("", summaryData);
            String detailsString = String.join("",detailsArray);

            if (startTime == null) {
                startTime = new Date();
                endTime = new Date();
            }
            while ((s = br.readLine()) != null) {
                totalStr += s;
            }
            totalStr = totalStr.replaceAll("<!--LOGO.PATH-->", logoFilePath);
            totalStr = totalStr.replaceAll("<!--PLATFORM-->",  platform.toUpperCase());
            totalStr = totalStr.replaceAll("<!--SUMARRY-->", listString);
            totalStr = totalStr.replaceAll("<!--DETAILS-->", detailsString);
            int total = passCount+failCount+inConclusiveCount+skippedCount;
            totalStr = totalStr.replaceAll("<!--TOTAL.COUNT-->",  total + "");
            totalStr = totalStr.replaceAll("<!--PASSED.COUNT-->", passCount + "");
            totalStr = totalStr.replaceAll("<!--FAILED.COUNT-->", failCount + "");
            totalStr = totalStr.replaceAll("<!--INCONCLUSIVE.COUNT-->", inConclusiveCount + "");
            totalStr = totalStr.replaceAll("<!--SKIPPED.COUNT-->", skippedCount + "");

            totalStr = totalStr.replaceAll("<!--START.TIME-->", startTime + "");
            totalStr = totalStr.replaceAll("<!--END.TIME-->", endTime + "");
            totalStr = totalStr.replaceAll("<!--TOTAL.TIME-->", calculateDuration(endTime,startTime) + "");
            totalStr = totalStr.replaceAll("<!--CATEGORY-->", (category==null)?"":category.toUpperCase());
            totalStr = totalStr.replaceAll("<!--ENVIRONMENT-->", (environment==null)?"": environment.toUpperCase());
            //totalStr = totalStr.replaceAll("<!--LINK.TO.FAILURE-->", (failCount==0)? "":  "<a href='./failureSummary.html'> Link to Failure Test Summary Report</a>");

            FileWriter fw = new FileWriter(result);
            fw.write(totalStr);
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void createCountTemplate(String platform, String category, String environment) {
        Boolean isParallel = false;
        try{
            isParallel = System.getProperty("Parallel").toString().equalsIgnoreCase("true");
        }
        catch(Exception e)
        {
            isParallel = true;
        }
        try {
            File result;
            if(isParallel)
                result = new File(configFilePath  + "/" + PropertyUtility.getResultConfigProperties("MERGED_COUNT_FILE"));
            else
                result = new File(System.getProperty("RESULTMAINDIR")  + "/" + PropertyUtility.getResultConfigProperties("MERGED_COUNT_FILE"));

            BufferedReader br = new BufferedReader(new InputStreamReader(ReportDesigner.class.getResourceAsStream("/" + "Templates/newresultcounttemplate.html")));
            String s;
            String totalStr = "";
            String listString = String.join("", summaryData);

            //if start time is null due to any reason then set it to current time
            if (startTime == null) {
                startTime = new Date();
                endTime = new Date();
            }
            while ((s = br.readLine()) != null) {
                totalStr += s;
            }
            int total = passCount+failCount+inConclusiveCount+skippedCount;
            totalStr = totalStr.replaceAll("<!--LOGO.PATH-->", logoFilePath);
            totalStr = totalStr.replaceAll("<!--TOTAL.COUNT-->",  total + "");
            totalStr = totalStr.replaceAll("<!--PASSED.COUNT-->", passCount + "");
            totalStr = totalStr.replaceAll("<!--FAILED.COUNT-->", failCount + "");
            totalStr = totalStr.replaceAll("<!--INCONCLUSIVE.COUNT-->", inConclusiveCount + "");
            totalStr = totalStr.replaceAll("<!--SKIPPED.COUNT-->", skippedCount + "");
            totalStr = totalStr.replaceAll("<!--START.TIME-->", startTime + "");
            totalStr = totalStr.replaceAll("<!--END.TIME-->", endTime + "");
            totalStr = totalStr.replaceAll("<!--TOTAL.TIME-->", calculateDuration(endTime,startTime) + "");

            FileWriter fw = new FileWriter(result);
            fw.write(totalStr);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String calculateDuration(Date d2, Date d1) {
        long diff = d2.getTime() - d1.getTime();

        long diffSeconds = diff   / 1000 % 60;
        long diffMinutes = diff  / (60 * 1000) % 60;
        long diffHours   = diff / (60 * 60 * 1000) % 24;
        try {
            String diffTime = cal(String.valueOf(diffHours)) + ":" + cal(String.valueOf(diffMinutes)) + ":"
                    + cal(String.valueOf(diffSeconds));
            return diffTime;
        } catch (Exception e) {
            System.out.println("Error in calculating duration in HTML report"+ e);
            return "";
        }
    }

    private static String cal(String time) {
        while (time.length() != 2)
            time = "0" + time;
        return time;
    }




}
