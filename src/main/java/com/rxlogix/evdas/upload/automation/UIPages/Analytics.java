package com.rxlogix.evdas.upload.automation.UIPages;

import com.rxlogix.evdas.upload.automation.Base.Base;
import com.rxlogix.evdas.upload.automation.ExternalConfig;
import com.rxlogix.evdas.upload.automation.RegressionFlow;
import com.rxlogix.evdas.upload.automation.Utils.CommonMethods;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Analytics extends Base {
    static Logger log = LoggerFactory.getLogger(RegressionFlow.class);

    @FindBy(xpath = "//*[@title='Export to different format']")
    public WebElement exportReport;

    @FindBy(xpath = "//*[contains(text(),'Excel')]")
    public WebElement export;

    @FindBy(xpath = "//*[contains(text(),'Excel 2007+')]")
    public WebElement export2007;


    public Analytics(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public Analytics clickExport() {
        CommonMethods.waitForProcessing(5000L);
        String currentTab = driver.getWindowHandle();
        for (String tab : driver.getWindowHandles()) {
            if (!tab.equals(currentTab)) {
                driver.switchTo().window(tab);
            }
        }
        exportReport.click();
        return PageFactory.initElements(driver, Analytics.class);
    }


    public Analytics export() {
        CommonMethods.waitForProcessing(4000L);
        Actions action = new Actions(driver);
        action.moveToElement(export).build().perform();
        return PageFactory.initElements(driver, Analytics.class);
    }

    public Analytics download(String fileName, ExternalConfig externalConfig) {
        CommonMethods.waitForProcessing(4000L);
        export2007.click();
        Boolean fileDownloadSuccessful = false;
        log.info("Start the download --------------------");
        String excelFilePath = externalConfig.getExcelDownloadLocation() + "/" + "electronic Reaction Monitoring Report - fixed reference period.xlsx";
        File file = new File(excelFilePath);
        String tempFilePath = externalConfig.getExcelDownloadLocation() + "/" + "electronic Reaction Monitoring Report - fixed reference period.xlsx.part";
        File tempFile = new File(tempFilePath);

        for (int k = 0; k < 80; k++) {
            CommonMethods.waitForProcessing(15000L);
            if (file.exists() && !tempFile.exists()) {
                fileDownloadSuccessful = true;
                break;
            }
        }

        if (fileDownloadSuccessful) {
            String newFileName = externalConfig.getEvdasDirectoryLocation() + "/" + fileName;

            if (file.renameTo(new File(newFileName))) {
                file.delete();
            }
        }

        return PageFactory.initElements(driver, Analytics.class);
    }

}
