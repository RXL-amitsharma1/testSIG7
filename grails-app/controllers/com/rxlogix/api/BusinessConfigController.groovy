package com.rxlogix.api

import com.rxlogix.Constants
import grails.converters.JSON
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletResponse
import grails.util.Holders

class BusinessConfigController {

    def configManagementService
    def userService

    ResponseEntity importBusinessConfig() {
        String token = request.getHeader(Constants.publicTokens.PVS_TOKEN)
        Map<String, String> responseBody = new HashMap<>()
        ResponseEntity responseEntity = new ResponseEntity<>(responseBody.put("message", "Success"), HttpStatus.OK)

        if (token != Holders.config.pvs.public.token) {
            log.info("Import business config: Invalid token.")
            responseBody.put("message", "Unauthorized Access")
            responseEntity = new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED)
        } else {
            try {
                String baseDirectoryPath = Holders.config.pvadmin.api.import.read.directory
                File baseFolder = new File(configManagementService.USER_HOME_DIR + baseDirectoryPath)
                if (!baseFolder.exists()) {
                    baseFolder.mkdirs()
                }
                configManagementService.currentConfigurationsBackup()
                configManagementService.parseExcelAndPopulateDB(baseDirectoryPath + Constants.ConfigManagement.BUSINESS_CONFIG_EXCEL)
                log.info("Config file import completed successfully.")
            } catch (Exception exception) {
                exception.printStackTrace()
                responseBody.put("message", exception.getMessage())
                responseEntity = new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR)
            }
            return responseEntity
        }
    }

    def exportBusinessConfig() {
        String token = request.getHeader(Constants.publicTokens.PVS_TOKEN);
        String filePath = params.filePath
        String userHome = System.properties.'user.home'
        ZipSecureFile.setMinInflateRatio(0.00001);
        Map resp = ["message": "Fail"]

        if (token != Holders.config.pvs.public.token) {
            log.info("Export business config: Invalid token.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        } else {
            if (!filePath) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
            } else {
                File configFile = new File(userHome + filePath)
                if (configFile.exists()) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(configFile)
                        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)
                        configManagementService.exportBusinessConfiguration(workbook)
                        fileInputStream.close()
                        FileOutputStream outputStream = new FileOutputStream(userHome + filePath)
                        workbook.write(outputStream)
                        outputStream.close() // Close the output stream
                        workbook.close() // Close the workbook
                        log.info("Generated file exported to path: " + filePath)
                        response.status = HttpServletResponse.SC_OK
                        resp.message = "Success"

                    } catch (Exception exception) {
                        log.error("An Error Occurred when generating business excel")
                        exception.printStackTrace()
                    }

                } else {
                    log.info("File not found at location : " + filePath)
                }
            }
        }
        render(resp as JSON)
    }
}
