package com.rxlogix.evdas.upload.automation;


import com.rxlogix.evdas.upload.automation.Base.Base;
import com.rxlogix.evdas.upload.automation.UIPages.Analytics;
import com.rxlogix.evdas.upload.automation.UIPages.LoginPage;
import com.rxlogix.evdas.upload.automation.Utils.AppConstants;
import groovy.util.logging.Slf4j;
import org.junit.Test;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class RegressionFlow extends Base {

    static Logger log = LoggerFactory.getLogger(RegressionFlow.class);

    @Test
    public void completeFlow(Map<String, String> evdasDataAutomationMap, ExternalConfig externalConfig) throws IOException, InterruptedException {
        try {
            log.info("Came in Script execution --------------------");
            launchApp(externalConfig);
            Analytics analytics;
            LoginPage login = PageFactory.initElements(driver, LoginPage.class);
            login = login.loginToApplication(evdasDataAutomationMap.get("username"), evdasDataAutomationMap.get("password"));
            log.info("Login Completed --------------------");

            if (evdasDataAutomationMap.get("fileType").equals(AppConstants.FILE_TYPE_ERMR)) {
                login = login.clickERMR();
                login = login.setTypeOfErmr();
                login = login.setActiveSubstance(evdasDataAutomationMap.get("substance"));
                login = login.setMedraReactionTerms();
                analytics = login.generateReport();
                analytics = analytics.clickExport();
                analytics = analytics.export();
                analytics.download(evdasDataAutomationMap.get("fileName"), externalConfig);
                log.info("Download Finished --------------------");

            } else {
                login = login.clickcaseListing();
            }

        } catch (Exception e) {
            log.info("In");
            log.error("Some exception occured while processing automation script for sender : ");
            e.printStackTrace();
        } finally {

//			driver.quit();
        }
    }

}
