package com.rxlogix.evdas.upload.automation;

public class ExternalConfig {
    String driverPath;
    String emaUrl;
    String excelDownloadLocation;
    String evdasDirectoryLocation;


    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    public String getEmaUrl() {
        return emaUrl;
    }

    public void setEmaUrl(String emaUrl) {
        this.emaUrl = emaUrl;
    }

    public String getExcelDownloadLocation() {
        return excelDownloadLocation;
    }

    public void setExcelDownloadLocation(String excelDownloadLocation) {
        this.excelDownloadLocation = excelDownloadLocation;
    }

    public String getEvdasDirectoryLocation() {
        return evdasDirectoryLocation;
    }

    public void setEvdasDirectoryLocation(String evdasDirectoryLocation) {
        this.evdasDirectoryLocation = evdasDirectoryLocation;
    }
}
