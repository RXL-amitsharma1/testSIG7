
package com.rxlogix.evdas.upload.automation.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Utils {

    static Properties properties;

    public static String loadPropertyFile(String propertyFilename, String Key) throws IOException {
        File file = new File(propertyFilename);

        FileInputStream fis = new FileInputStream(file);
        properties = new Properties();
        properties.load(fis);
        return properties.getProperty(Key);

    }

	    /*This method breaks the date into day,month,year*/

    public static Map<String, String> parseInputDate(String inputDate) {
        Map<String, String> resp = new HashMap<>();
        String[] dateValues = inputDate.split("-");
        resp.put("year", dateValues[0]);
        resp.put("month", findElementInList(dateValues[1]));
        resp.put("day", dateValues[2]);
        return resp;
    }
    /*This method gives the year difference of passed date from current date*/

    public static int getYearDifference(String inputDate) {
        String[] dateValues = inputDate.split("-");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int inputYear = Integer.parseInt(dateValues[0]);
        if (currentYear < inputYear) {
            return (inputYear - currentYear);
        } else {
            return (currentYear - inputYear);
        }
    }

    private static String findElementInList(String key) {
        Map<String, String> monthStringArray = new HashMap<String, String>();
        monthStringArray.put("JAN", "January");
        monthStringArray.put("FEB", "February");
        monthStringArray.put("MAR", "March");
        monthStringArray.put("APR", "April");
        monthStringArray.put("MAY", "May");
        monthStringArray.put("JUN", "June");
        monthStringArray.put("JUL", "July");
        monthStringArray.put("AUG", "August");
        monthStringArray.put("SEP", "September");
        monthStringArray.put("OCT", "October");
        monthStringArray.put("NOV", "November");
        monthStringArray.put("DEC", "December");
        return monthStringArray.get(key.toUpperCase());
    }
}

