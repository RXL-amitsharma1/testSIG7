package com.rxlogix.evdas.upload.automation.Base;

import com.rxlogix.evdas.upload.automation.ExternalConfig;
import com.rxlogix.evdas.upload.automation.RegressionFlow;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Base {
    static Logger log = LoggerFactory.getLogger(RegressionFlow.class);
    public static WebDriver driver;

    @BeforeClass
    public static void launchApp(ExternalConfig externalConfig) throws IOException, InterruptedException {
        try {
            log.info("Launching Browser Headless");
            System.setProperty("webdriver.gecko.driver", externalConfig.getDriverPath());
            FirefoxProfile profile = new FirefoxProfile();
            profile.setAcceptUntrustedCertificates(true);
            profile.setAssumeUntrustedCertificateIssuer(true);
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.helperApps.alwaysAsk.force", false);
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.download.dir", externalConfig.getExcelDownloadLocation());
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            FirefoxOptions options = new FirefoxOptions().setProfile(profile);
            options.setHeadless(true);
            driver = new FirefoxDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.get(externalConfig.getEmaUrl());
            log.info("Browser Launched");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @AfterClass
    public void End() {
        {
            // driver.quit();
        }

    }
}

