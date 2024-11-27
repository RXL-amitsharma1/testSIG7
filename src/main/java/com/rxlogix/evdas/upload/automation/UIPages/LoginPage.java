
package com.rxlogix.evdas.upload.automation.UIPages;

import com.rxlogix.evdas.upload.automation.Base.Base;
import com.rxlogix.evdas.upload.automation.RegressionFlow;
import com.rxlogix.evdas.upload.automation.Utils.AppConstants;
import com.rxlogix.evdas.upload.automation.Utils.CommonMethods;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class LoginPage extends Base {

    static Logger log = LoggerFactory.getLogger(RegressionFlow.class);

    @FindBy(id = "username")
    public WebElement userName;

    @FindBy(id = "password")
    public WebElement password;

    @FindBy(xpath = "//input[@type='submit']")
    public WebElement loginButton;

    @FindBy(id = "dashboard_page_1_tab")
    public WebElement eRMR;

    @FindBy(id = "dashboard_page_2_tab")
    public WebElement caseListing;

    @FindBy(css = ".promptTextField.textFieldHelper")
    public WebElement activeSubstance;

    @FindBy(xpath = "//*[contains(text(),'eRMR – ad hoc reference period')]")
    public WebElement eRMRAdhocPeriod;

    @FindBy(xpath = "//*[contains(text(),'eRMR – fixed reference period')]")
    public WebElement eRMRFixedPeriod;

    @FindBy(css = ".promptTextField.promptTextFieldReadOnly")
    public WebElement referencePeriodStartDate;

    @FindBy(css = ".promptTextField.promptTextFieldReadOnly")
    public WebElement referencePeriodEndDate;

    @FindBy(xpath = "//input[@value='Active Substance (High Level)']")
    public WebElement searchActive;

    @FindBy(xpath = "//input[@value='Worldwide Case Number']")
    public WebElement searchWorlwideCaseNo;

    @FindBy(xpath = "//input[@value='EU Local Number']")
    public WebElement searchEULocalNo;

    @FindBy(xpath = "//input[@value='none']")
    public WebElement none;

    @FindBy(xpath = "//input[@value='MedDRA reaction PT']")
    public WebElement medraPT;

    @FindBy(xpath = "//input[@value='MedDRA reaction HLT']")
    public WebElement medraHLT;

    @FindBy(xpath = "//input[@value='MedDRA reaction HLGT']")
    public WebElement medraHLGT;

    @FindBy(xpath = "//input[@value='MedDRA reaction SOC']")
    public WebElement medraSOC;

    @FindBy(xpath = "//input[@value='MedDRA SMQ Level 1 Broad']")
    public WebElement medraSMQBroad;

    @FindBy(xpath = "//input[@value='MedDRA SMQ Level 1 Narrow']")
    public WebElement medraSMQNarrow;

    @FindBy(xpath = "//a[@name='SectionElements']")
    public WebElement reportLink;

    @FindBy(xpath = "//*[contains(text(),'allows')]")
    public WebElement clickOutside;


    public int fileCount = 0;

    public String downloadStatus = AppConstants.FILE_DOWNLOAD_STATUS_SUCCESS;

    //This setter method set file count and download status for a request
    private void updateFileStatus(String fileCount, String downloadStatus) {
        this.fileCount = Integer.parseInt(fileCount);
        this.downloadStatus = downloadStatus;
    }

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    //login into EV Web using username and password
    public LoginPage loginToApplication(String username, String passwrd) throws IOException, InterruptedException {
        CommonMethods.waitForProcessing(2000L);
        userName.click();
        userName.sendKeys(username);
        password.click();
        password.sendKeys(passwrd);
        loginButton.click();
        userName.click();
        userName.sendKeys(username);
        password.click();
        password.sendKeys(passwrd);
        loginButton.click();
        return PageFactory.initElements(driver, LoginPage.class);
    }

    public LoginPage clickERMR() {
        CommonMethods.waitForProcessing(8000L);
        eRMR.click();
        return PageFactory.initElements(driver, LoginPage.class);
    }

    public LoginPage clickcaseListing() {
        CommonMethods.waitForProcessing(2000L);
        caseListing.click();
        return PageFactory.initElements(driver, LoginPage.class);
    }

    public LoginPage setTypeOfErmr() {
        CommonMethods.waitForProcessing(6000L);
        eRMRFixedPeriod.click();
        return PageFactory.initElements(driver, LoginPage.class);
    }

    public LoginPage setActiveSubstance(String substance) {
        log.info("Entering in Active Substance --------------------");
        CommonMethods.waitForProcessing(2000L);
        activeSubstance.clear();
        activeSubstance.sendKeys(substance.substring(0, substance.length() - 1));
        CommonMethods.waitForProcessing(4000L);
        String dropDownXPath = "//div[@title='" + substance + "']";
        WebDriverWait wait = new WebDriverWait(driver, 15000L);
        WebElement dropDown = driver.findElement(By.xpath(dropDownXPath));
        dropDown.click();
        return PageFactory.initElements(driver, LoginPage.class);
    }

    public LoginPage setMedraReactionTerms() {
        CommonMethods.waitForProcessing(4000L);
        none.click();
        return PageFactory.initElements(driver, LoginPage.class);
    }

    public Analytics generateReport() {
        CommonMethods.waitForProcessing(2000L);
        log.info("Generating Report --------------------");
        reportLink.click();
        return PageFactory.initElements(driver, Analytics.class);
    }

}

