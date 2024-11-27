package com.rxlogix.config

import com.rxlogix.EmailNotification
import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.enums.DataSourceEnum
import com.rxlogix.json.JsonOutput
import com.rxlogix.mapping.MedDraHLGT
import com.rxlogix.mapping.MedDraHLT
import com.rxlogix.mapping.MedDraLLT
import com.rxlogix.mapping.MedDraPT
import com.rxlogix.mapping.MedDraSOC
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.product.view.LmProdDic200
import com.rxlogix.pvdictionary.product.view.LmProdDic201
import com.rxlogix.pvdictionary.product.view.LmProdDic202
import com.rxlogix.pvdictionary.product.view.LmProdDic203
import com.rxlogix.pvdictionary.product.view.LmProdDic204
import com.rxlogix.pvdictionary.product.view.LmProdDic205
import com.rxlogix.pvdictionary.product.view.LmProdDic206
import com.rxlogix.pvdictionary.product.view.LmProdDic207
import com.rxlogix.pvdictionary.product.view.LmProdDic208
import com.rxlogix.signal.AlertPreExecutionCheck
import com.rxlogix.signal.AutoAdjustmentRule
import com.rxlogix.signal.BusinessConfiguration
import com.rxlogix.signal.Justification
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.signal.ProductTypeConfiguration
import com.rxlogix.signal.RuleInformation
import com.rxlogix.signal.SignalWorkflowRule
import com.rxlogix.signal.SignalWorkflowState
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.signal.SystemConfig
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import com.rxlogix.UserDashboardCounts
import com.rxlogix.Constants
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.RolesEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.signal.SignalOutcome
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.converters.JSON
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovyx.net.http.Method
import net.sf.json.JSONObject
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.http.HttpStatus
import org.apache.commons.lang.SerializationUtils
import org.apache.commons.lang3.EnumUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.rxlogix.user.UserDepartment
import com.rxlogix.signal.SignalNotificationMemo
import com.rxlogix.CRUDService
import com.rxlogix.PriorityService
import com.rxlogix.Constants
import com.rxlogix.dto.PvaCategoryDTO
import static com.rxlogix.util.DateUtil.toDateString
import java.text.SimpleDateFormat
import com.rxlogix.util.ViewHelper

@Transactional
class ConfigManagementService {

    def reportIntegrationService
    def userService
    def userGroupService
    def cacheService
    def alertService
    def productDictionaryCacheService
    def groupService
    def emergingIssueService
    def dataObjectService
    def pvsProductDictionaryService
    def viewInstanceService
    def signalMemoReportService
    def CRUDService
    def priorityService
    def preCheckService
    def dataSource_pva
    def dataSource_eudra
    def dataSource_faers
    def dataSource_vaers
    def dataSource_vigibase
    def dataSource_jader
    def grailsApplication
    def aggregateCaseAlertService
    def signalDataSourceService
    def businessConfigurationService

    static final String  USER_HOME_DIR = System.properties.'user.home'
    static final int CELL_SIZE_LIMIT = 30000

    static responseFormats = ['json']
    static allowedMethods = [save: "POST"]


    Map<String, Map> columnsMapping = ["Product Substance Frequency"        : ["domainClass"     : SubstanceFrequency, "Dynamic_Key": ["name", "alertType"], "Frequency Label": "frequencyName", "Product Name": "name", "Start Date": "startDate", "End Date": "endDate",
                                                                       "Upload Frequency": "uploadFrequency", "Mining Frequency": "miningFrequency", "Alert Type": "alertType"],
                                       "Priority"                   : ["domainClass": Priority, "Value": "value", "Display Name": "displayName", "Description": "description", "Display": "display",
                                                                       "Default"    : "defaultPriority", "Icon": "iconClass", "Review Period (Days)": "reviewPeriod", "Order": "priorityOrder", "Disposition": "displayNameDisposition", "Review Period Disposition (Days)": "reviewPeriodDisposition"],
                                       "Dispositions"                : ["domainClass"         : Disposition, "Value": "value", "Display Name": "displayName", "Description": "description",
                                                                       "Validated Confirmed" : "validatedConfirmed", "Closed": "closed", "Review Completed": "reviewCompleted", "Display": "display",
                                                                       "Reset Review Process": "resetReviewProcess", "Abbreviation": "abbreviation", "Color": "colorCode", "Color Remarks": "colorCode",
                                                                       "Signal Status"       : "signalStatusForDueDate"],
                                       "Workflow Group"            : ["domainClass"          : Group, "Group Name": "name", "Description": "description", "Force Justification": "forceJustification", "Allowed Products": "allowedProd",
                                                                       "Allowed Products List": "allowedProductList", "Selected Datasource": "selectedDatasource", "Group Type": "groupType", "Auto Route Justification": "justificationText", "Users": "groupUsers",
                                                                       "Signal (Default)"     : "defaultSignalDisposition", "Auto Route Disposition": "autoRouteDisposition", "Individual Case (Default)": "defaultQualiDisposition",
                                                                       "Aggregate (Default)"  : "defaultQuantDisposition", "Adhoc Review (Default)": "defaultAdhocDisposition", "EVDAS (Default)": "defaultEvdasDisposition",
                                                                       "Literature (Default)" : "defaultLitDisposition", "Alert Level Disposition": "alertLevelDispositions"],
                                       "User Group"                : ["domainClass"          : Group, "Group Name": "name", "Description": "description", "Allowed Products": "allowedProd",
                                                                       "Allowed Products List": "allowedProductList", "Selected Datasource": "selectedDatasource", "Justification Text": "justificationText",
                                                                       "Roles"                : "roles", "Users": "groupUsers"],
                                       "Users"                      : ["domainClass"                      : User, "Username": "username", "User Full Name": "fullName", "Email": "email",
                                                                       "Wrong Attempts"                   : "badPasswordAttempts", "Account Enabled": "enabled", "Account Expired": "accountExpired", "Account Locked": "accountLocked",
                                                                       "Product Safety Lead Configuration": "safetyGroups", "Department": "userDepartments", "Roles": "roles",
                                                                       "Language"                         : "preference", "Timezone": "preference", "workflow Groups": "WORKFLOW_GROUP"],
                                       "Safety Lead"                : ["domainClass": SafetyGroup, "Safety Lead Configuration": "name", "Products Access": "allowedProd"],
                                       "Workflow Rules"             : ["domainClass"       : DispositionRule, "Workflow Rule Name": "name", "Incoming Disposition": "incomingDisposition",
                                                                       "Target Disposition": "targetDisposition", "Description": "description", "Approval Required": "approvalRequired",
                                                                       "Workflow Groups"   : "workflowGroups", "Allowed Groups": "allowedUserGroups", "Display": "display", "Notify": "notify", "Deleted": "isDeleted"],
                                       "Signal Outcome"             : ["domainClass": SignalOutcome, "Signal Outcome": "name", "Deleted": "isDeleted"],
                                       "Risk Topic Category"        : ["domainClass": TopicCategory, "Topic Category": "name"],
                                       "Business Rule"              : ["domainClass": BusinessConfiguration, "Rule Name": "ruleName", "Data Source": "dataSource", "Rule Type(Product/Global)": "isGlobalRule", "Description": "description", "Enable": "enabled", "Products": "productSelection", "Product Group Selection": "productGroupSelection", "Product Dictionary Selection": "productDictionarySelection", "Multi-Ingredient": "isMultiIngredient"],
                                       "Sub Rule"                   : ["domainClass": RuleInformation, "Business Configuration Name": "businessConfiguration", "Sub Rule Name": "ruleName", "Alert Type": "isSingleCaseAlertType", "First Time Rule": "isFirstTimeRule", "Break After Rule": "isBreakAfterRule", "Disposition": "disposition",
                                                                       "Enable"     : "enabled", "Rules": "ruleJSON", "Justification Text": "justificationText", "Format": "format", "Tags": "tags", "Signal": "signal"],
                                       "Signal Workflow List"       : ["domainClass"         : SignalWorkflowState, "Value": "value", "Display Name": "displayName",
                                                                       "Allowed Dispositions": "allowedDispositions", "Default": "defaultDisplay", "Calculate Due in": "dueInDisplay"],
                                       "Signal Workflow Rules"      : ["domainClass"   : SignalWorkflowRule, "Rule Name": "ruleName", "Description": "description", "From State": "fromState", "To State": "toState",
                                                                       "Allowed Groups": "allowedGroups", "Display": "display"],
                                       "Justification Template"     : ["domainClass"   : Justification, "Template Name": "name", "Justification Text": "justification",
                                                                       "Alert Workflow": "alertWorkflow", "Signal Workflow": "signalWorkflow", "Alert Priority": "alertPriority", "Signal Priority": "signalPriority",
                                                                       "Case Addition" : "caseAddition", "Allowed Dispositions": "dispositions"],
                                       "Action Type"                : ["domainClass": ActionType, "Name": "value", "Display Name": "displayName", "Description": "description"],
                                       "Action Configuration"       : ["domainClass" : ActionConfiguration, "Name": "value", "Display Name": "displayName", "Description": "description",
                                                                       "Notify Email": "isEmailEnabled"],
                                       "Business Rule (View Only)"       : ["domainClass"    : RuleInformation, "Sub Rule Name": "ruleName", "Alert Type": "isSingleCaseAlertType",
                                                                       "First Time Rule": "isFirstTimeRule", "Break After Rule": "isBreakAfterRule",
                                                                       "Rule Name"      : "ruleName", "Category": "category", "Select Attribute": "attribute",
                                                                       "Select Operator": "operator", "Threshold": "threshold", "Operator": "keyword",
                                                                       "Data Source"    : "dataSource", "Description": "description", "Rule Type": "isGlobalRule", "Split Signals to PT Level" : "splitSignalToPt", "Associate PEC’s to Closed Signals" : "assClosedSignal", "Associate Multiple Signals to PEC’s" : "assMultSignal"],
                                       "Important Events"           : ["domainClass": EmergingIssue, "Dynamic_Key": ["productSelection", "eventName"], "Product Term": "productSelection", "Event Term": "eventName", "IME": "ime", "DME": "dme", "Special Monitoring": "specialMonitoring", "Stop List": "emergingIssue", "Multi-Ingredient": "isMultiIngredient"],
                                       "Advanced Filter"         : ["domainClass": AdvancedFilter, "Dynamic_Key": ["name", "user"], "Name": "name", "Description": "description", "Alert Type": "alertType",
                                                                       "Shared With Users": "shareWithUser", "Shared With Groups": "shareWithGroup", "JSON": "JSONQuery", "Criteria": "criteria", "Owner": "createdBy"],
                                       "Advanced Filter (View Only)"           : ["domainClass": AdvancedFilter, "Dynamic_Key": ["name", "user"], "Name": "name", "Description": "description", "Alert Type": "alertType", "Field": "field",
                                                                       "Operator"   : "keyword", "Select Operator": "op", "Value": "value", "Shared With Users": "shareWithUser", "Shared With Groups": "shareWithGroup", "Owner": "createdBy"],
                                       "Views"                      : ["domainClass"       : ViewInstance, "Dynamic_Key": ["name", "alertType", "user"], "View Name": "name", "View Type": "alertType", "Advanced Filter": "advancedFilter", "Shared With Users": "shareWithUser",
                                                                       "Shared With Groups": "shareWithGroup", "Sorting": "sorting", "Default": "defaultValue", "User": "user", "Columns List": "columnSeq"],
                                       "Email Configuration"        : ["domainClass"  : EmailNotification, "Name": "moduleName", "Key": "key", "Enable": "isEnabled",
                                                                       "Default Value": "defaultValue"],
                                       "Signal Memo Configuration"  : ["domainClass"  : SignalNotificationMemo, "Config Name": "configName", "Signal Source": "signalSource", "Trigger Variable": "triggerVariable",
                                                                       "Trigger Value": "triggerValue", "Email Subject": "emailSubject", "Email Body": "emailBody", "Email Address": "emailAddress"],
                                       "Comment Template"           : ["domainClass": CommentTemplate, "Name": "name", "Template Content": "comments", "Date Modified": "lastUpdated", "Last Updated By": "modifiedBy"],
                                       "Product Type Configuration" : ["domainClass": ProductTypeConfiguration, "Dynamic_Key": ["name", "productType", "roleType"], "Name": "name", "Product Type": "productType", "Role": "roleType"],
                                       "Miscellaneous"              : ["End of Review Milestone Date Auto Population Configuration": "enableEndOfMilestone", "Display Due In": "displayDueIn", "Due In Endpoint": "selectedEndPoints",
                                                                       "Date Closed Based On Disposition/Workflow"                          : "dateClosedDisposition", "Enable Signal Assessment Charts": Constants.ENABLE_SIGNAL_CHARTS, "Product Group Update Pre-check Status": Constants.PRODUCT_GROUP_UPDATE,
                                                                       "Export Always"                                             : "exportAlways", "Prompt User": "promptUser", "Disable Alerts On PVR Inaccessibility": "isPvrCheckEnabled", "Alerts Configured On Version As Of": "isVersionAsOfCheckEnabled",
                                                                       "ETL Failure Check"                                         : "isEtlFailureCheckEnabled", "ETL In Progress Check": "isEtlInProgressCheckEnabled", "Individual Case Alert": "Single Case Alert", "Aggregate Alert": "Aggregate Case Alert"],
                                       "Category"                   : ["domainClass"      : PvaCategoryDTO, "Category": "value", "Created By": "createdBy", "Date Created": "dateCreated", "Display": "display", "Deleted": "isDeleted",
                                                                       "Is Master Data"   : "isMasterData", "Last Updated": "lastUpdated", "Modified By": "modifiedBy",
                                                                       "Referenced Entity": "referencedEntity", "Code Name": "codeName", "Sub-Categories": "parentValue"]
    ]

    List<String> sheetNamesWithLargeData = ["Business Rule", "Comment Template", "Important Events", "Workflow Group", "User Group", "Safety Lead", "Views"]
    List<String> systemConfigFieldNames = ["displayDueIn", "selectedEndPoints", "dateClosedDisposition"]
    List<String> alertConfigFieldNames = ["isPvrCheckEnabled", "isVersionAsOfCheckEnabled", "isEtlFailureCheckEnabled", "isEtlInProgressCheckEnabled"]
    List<String> pvsAppConfigFieldNames = [Constants.ENABLE_SIGNAL_CHARTS, Constants.PRODUCT_GROUP_UPDATE]
    List<String> caseNarrativeConfigFieldNames = ["exportAlways", "promptUser"]
    List<String> adjustmentRuleFields = ["Single Case Alert", "Aggregate Case Alert"]
    Map<String, String> adjustmentTypeEnumStringValues = ["ALERT_PER_SKIPPED_EXECUTION": "Auto-Adjust Date and Execute alert for every skipped execution",
                                                          "SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION": "Auto-Adjust Date and execute a single alert for all skipped execution",
                                                          "MANUAL_AUTO_ADJUSTMENT": "Disable Alert Execution and Enable based on Manual Intervention"]
    List<String> usersHeaderRolesRow =[]
    List<String> excludedRoles = ["Super Administrator"]
    List alertFields = []
    List<Map> fieldList = []
    Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
    Map rptToUiLabelMap = [:]

    void exportCriteriaSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Criteria Sheet")
        int rowNumber = 0
        Row row = sheet.createRow(rowNumber++)
        Cell cell = row.createCell(0)
        cell.setCellValue("Criteria Sheet")
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        cell.setCellStyle(applyColumnHeaderStyle(workbook))
        User user = userService.getUser() ?: User.findByFullName("System")
        String generatedBy = user.fullName
        String dateCreated = DateUtil.toDateStringWithTimeInAmPmFormat(user) + userService.getGmtOffset(user.preference.timeZone)
        Map<String, String> criteriaSheetData = ["Report Generated By": generatedBy, "Run Date and Time": dateCreated, "Environment": Holders.config.grails.serverURL, "Application Name" : "PVS"]
        criteriaSheetData.each { k, v ->
            row = sheet.createRow(rowNumber++)
            cell = row.createCell(0)
            cell.setCellValue(k)
            cell = row.createCell(1)
            cell.setCellValue(v)
            sheet.setColumnWidth(0,30 * 256)
            sheet.setColumnWidth(1,30 * 256)
        }
    }

    void exportTechnicalConfiguration(XSSFWorkbook workbook) {
        String serverUrl = Holders.config.pvadmin.api.url
        String apiPath = Holders.config.pvadmin.api.fetchTech.url
        Map<String, String> queryParams = ["appName": "PVS"]
        Map<String, Object> response = reportIntegrationService.get(serverUrl, apiPath, queryParams, "PVAdmin")
        if (response.status == HttpStatus.OK) {
            List<Object[]> techConfigData = response.data
            createExcelWorkBookForData(Holders.config.tech.export.headerData, techConfigData, workbook);
        }
    }

    void createExcelWorkBookForData(List<String> headerData, List<Object[]> tableData, XSSFWorkbook workbook) throws IOException {
        log.info("EXCEL DATA EXPORTER STARTED FOR TECH CONFIG....")
        XSSFSheet sheet = workbook.createSheet("Technical Configurations")
        int rowNumber = 0
        int cellNumber = 0
        Row row = sheet.createRow(rowNumber++)
        for (String key : headerData) {
            sheet.setColumnWidth(cellNumber, 25 * 256)
            Cell cell = row.createCell(cellNumber++)
            cell.setCellValue(key)
            cell.setCellStyle(applyColumnHeaderStyle(workbook))
        }
        for (Object[] objectRow : tableData) {
            Row row1 = sheet.createRow(rowNumber++)
            row1.setHeight((short) 1000)
            for (int i = 1; i < objectRow.length; i++) {
                Cell cell = row1.createCell(i - 1)
                cell.setCellValue(objectRow[i] != null ? objectRow[i].toString() : "")
            }
        }
        CellRangeAddress region = new CellRangeAddress(1, sheet.getLastRowNum(), 0, (headerData.size() -1))
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet)
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet)
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet)
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet)
        log.info("EXCEL DATA EXPORTER IS COMPLETE FOR TECH CONFIG.");
    }

    void exportBusinessConfiguration(XSSFWorkbook workbook) {
        log.info("Going to generate business configuration...")
        LinkedHashMap exportMap = Holders.config.business.config.export.map
        populateAlertFields()
        populateFieldList()
        if (!exportMap) {
            log.info("Export business map configuration is not present")
        }
        exportMap.each { String sheetName, def exportKeys ->
            if (exportKeys.export) {
                XSSFSheet sheet = workbook.createSheet(sheetName.replaceAll('\\/', " "))
                switch (true) {
                    case sheetName == "Sub Rule":
                        Map<String, Integer> columnNameToCount = populateBusinessRuleData(sheet, sheetName, exportKeys)
                        populateHeaderRow(sheet, exportKeys, workbook, columnNameToCount)
                        break
                    case sheetName == "Business Rule (View Only)":
                        populateBusinessRuleDataHeaderRowForExport(sheet, exportKeys, workbook)
                        populateBusinessRuleDataForExport(sheet, sheetName, exportKeys)
                        break
                    case ["Advanced Filter", "Advanced Filter (View Only)"].contains(sheetName):
                        populateHeaderRow(sheet, exportKeys, workbook)
                        populateAdvancedFilterDataForExport(sheet, sheetName, exportKeys)
                        break
                    case sheetName == "System Health Widget":
                        populateHeaderRow(sheet, exportKeys, workbook)
                        populateSystemHealthWidget(sheet)
                        break
                    case sheetName == "Miscellaneous":
                        populateControlPanelConfigurationHeaderRowForExport(sheet, workbook, ["Dynamic_Key", "Section", "Property", "Value"])
                        populateControlPanelConfigurationDataForExport(sheet, sheetName, exportKeys, workbook)
                        break
                    case sheetName == "Category":
                        populateHeaderRow(sheet, exportKeys, workbook)
                        populateCategorySheetData(sheet , sheetName, exportKeys)
                        break
                    case sheetNamesWithLargeData.contains(sheetName):
                        Map<String, Integer> columnNameToCount = populateDataSplittingColumns(sheet, sheetName, exportKeys)
                        populateHeaderRow(sheet, exportKeys, workbook, columnNameToCount)
                        break
                    default:
                        populateHeaderRow(sheet, exportKeys, workbook)
                        populateData(sheet, sheetName, exportKeys)
                        break
                }
            }
        }
        log.info("Generated business configuration for available sheets.")
    }

    void populateCategorySheetData(XSSFSheet sheet, String sheetName, exportKeys) {
        int cellNumber = 0
        int rowNumber = 1
        def map = columnsMapping[sheetName]
        List allCategory = []
        Sql sql = new Sql(dataSource_pva)
        sql.eachRow("select * from code_value where code_list_id = ${Holders.config.mart.codeValue.tags.value} and is_master_data=1 and is_deleted =0", []) { row ->
            Map rowData = [id: row.id, value: row.value as String, parentValue: row.parent_id, display: row.display, isDeleted: row.is_deleted, isMasterData : row.is_master_data]
            allCategory << rowData
        }
        sql?.close()
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        allCategory.each { domainObject ->
            Row row = sheet.createRow(rowNumber++)
            cellNumber = 0
            Boolean isCategory = true
            allKeys.unique().each { requiredColumn ->
                switch (true) {
                    case (requiredColumn == "Category"):
                        String fieldName = map."$requiredColumn"
                        Map tempCategory = allCategory.find { it.id == domainObject.id}
                        if (tempCategory && tempCategory.parentValue == null) {
                            Cell cell = row.createCell(cellNumber++)
                            String cellValueResult = tempCategory.value as String
                            cell.setCellValue(cellValueResult)
                            Cell cell2 = row.createCell(cellNumber++)
                            String cellValueResult2 = allCategory.findAll { it.parentValue == tempCategory.id }*.value.toString().replaceAll("[\\[\\]]", "")
                            cell2.setCellValue(cellValueResult2)
                        } else {
                            isCategory = false
                            rowNumber--;
                        }
                        break
                    case ((requiredColumn == "Is Master Data" || requiredColumn == "Deleted" || requiredColumn == "Display") && isCategory == true):
                        Cell cell = row.createCell(cellNumber++)
                        String fieldName = map."$requiredColumn"
                        String cellValueResult
                        if (domainObject[fieldName] instanceof Boolean) {
                            cellValueResult = domainObject[fieldName] ? "Yes" : "No"
                        } else {
                            cellValueResult = domainObject[fieldName] as String
                        }
                        cell.setCellValue(cellValueResult)
                        break
                }
            }
        }
    }

    void populateControlPanelConfigurationHeaderRowForExport(XSSFSheet sheet, XSSFWorkbook workbook, List<String> headerValues) {
        int cellNumber = 0
        Row headerRow = sheet.createRow(0)
        headerValues.forEach {headerColumnValue ->
            sheet.setColumnWidth(cellNumber, 25 * 256)
            Cell cell = headerRow.createCell(cellNumber++)
            cell.setCellValue(headerColumnValue)
            cell.setCellStyle(applyColumnHeaderStyle(workbook))
        }
    }

    void populateControlPanelConfigurationDataForExport(XSSFSheet sheet, String sheetName, exportKeys, XSSFWorkbook workbook) {
        int rowNumber = 1
        def columnsMappingMap = columnsMapping[sheetName]
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        SystemConfig systemConfig = SystemConfig.first()
        AlertPreExecutionCheck alertPreExecutionCheck = AlertPreExecutionCheck.first()
        CaseNarrativeConfiguration caseNarrativeConfiguration = CaseNarrativeConfiguration.getInstance()

        allKeys.forEach { key ->
            String fieldName = columnsMappingMap."$key"
            if (fieldName) {
                int cellNumber = 0;
                Row row = sheet.createRow(rowNumber++)
                if(key in ["End of Review Milestone Date Auto Population Configuration", "Display Due In", "Due In Endpoint",
                           "Date Closed Based On Disposition", "Enable Signal Assessment Charts"]){
                    row.createCell(cellNumber++).setCellValue("Signal Configurations"+"_"+key)
                } else if(key == "Product Group Update Pre-check Status"){
                    row.createCell(cellNumber++).setCellValue("Product Group Update Configurations"+"_"+key)
                } else if(key in ["Export Always", "Prompt User"]){
                    row.createCell(cellNumber++).setCellValue("Case Narrative Export"+"_"+key)
                } else{
                    row.createCell(cellNumber++).setCellValue("Alert Administration"+"_"+key)
                }
                if(key in ["End of Review Milestone Date Auto Population Configuration", "Display Due In", "Due In Endpoint",
                           "Date Closed Based On Disposition", "Enable Signal Assessment Charts"]){
                    row.createCell(cellNumber++).setCellValue("Signal Configurations")
                } else if(key == "Product Group Update Pre-check Status"){
                    row.createCell(cellNumber++).setCellValue("Product Group Update Configurations")
                } else if(key in ["Export Always", "Prompt User"]){
                    row.createCell(cellNumber++).setCellValue("Case Narrative Export")
                } else{
                    row.createCell(cellNumber++).setCellValue("Alert Administration")
                }
                switch (true) {
                    case fieldName == "enableEndOfMilestone":
                        row.createCell(cellNumber++).setCellValue(key)
                        String cellValue = getCellValueResult(systemConfig, fieldName)
                        row.createCell(cellNumber).setCellValue(cellValue)
                        if(cellValue == "Yes") {
                            createDispositionEndOfReviewMilestoneConfiguration(workbook)
                        }
                        break
                    case systemConfigFieldNames.contains(fieldName):
                        row.createCell(cellNumber++).setCellValue(key)
                        if(fieldName == "dateClosedDisposition" && systemConfig.enableSignalWorkflow == true){
                            fieldName = (systemConfig?.isDisposition) ? "dateClosedDispositionWorkflow" : "dateClosedWorkflow"
                        }
                        String cellValue = getCellValueResult(systemConfig, fieldName)
                        row.createCell(cellNumber).setCellValue(cellValue)
                        break
                    case alertConfigFieldNames.contains(fieldName):
                        row.createCell(cellNumber++).setCellValue(key)
                        String cellValue = getCellValueResult(alertPreExecutionCheck, fieldName)
                        row.createCell(cellNumber).setCellValue(cellValue)
                        break
                    case pvsAppConfigFieldNames.contains(fieldName):
                        row.createCell(cellNumber++).setCellValue(key)
                        PvsAppConfiguration domain = PvsAppConfiguration.findByKey(fieldName)
                        row.createCell(cellNumber).setCellValue(domain.booleanValue ? 'Yes' : 'No')
                        break
                    case caseNarrativeConfigFieldNames.contains(fieldName):
                        row.createCell(cellNumber++).setCellValue(key)
                        String cellValue = getCellValueResult(caseNarrativeConfiguration, fieldName)
                        row.createCell(cellNumber).setCellValue(cellValue)
                        break
                    case adjustmentRuleFields.contains(fieldName):
                        row.createCell(cellNumber++).setCellValue(key)
                        AutoAdjustmentRule autoAdjustmentRule = AutoAdjustmentRule.findByAlertType(fieldName)
                        def cellValueResult = autoAdjustmentRule.isEnabled ? adjustmentTypeEnumStringValues."$autoAdjustmentRule.adjustmentTypeEnum" : 'No'
                        row.createCell(cellNumber).setCellValue(cellValueResult)
                        break
                }
            }
        }
    }

    String getCellValueResult(domainObject, String fieldName) {
        if (domainObject[fieldName] instanceof List || domainObject[fieldName] instanceof Set) {
            return domainObject[fieldName]?.id ? domainObject[fieldName].sort { it.id }.join(', ') : domainObject[fieldName].sort().join(', ')
        } else if (domainObject[fieldName] instanceof Boolean) {
            return domainObject[fieldName] ? "Yes" : "No"
        } else {
            return domainObject[fieldName] as String
        }
    }

    void createDispositionEndOfReviewMilestoneConfiguration(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Disposition End Of Review Milestone Configuration")
        populateControlPanelConfigurationHeaderRowForExport(sheet, workbook, ["Signal Disposition", "Signal Status"])
        int rowNumber = 1
        List<Disposition> dispositions  = Disposition.findAllSignalStatusForDueDateIsNotNull()
        dispositions.forEach {disposition ->
            int cellNumber = 0;
            Row row = sheet.createRow(rowNumber++)
            row.createCell(cellNumber++).setCellValue(disposition.displayName)
            row.createCell(cellNumber).setCellValue(disposition.signalStatusForDueDate)
        }
    }

    void populateSystemHealthWidget(XSSFSheet sheet) {

        Map prechecks = ["DB Checks/Memory Status": "DB_CHECKS", "DB Connections": "DB_CONNECTIONS", "URL Configurations": "URL_CONFIG", "Directories Configuration": "DIRECTORY_CONFIG",
                         "Integrated Module"      : "INTEGRATED_MODULE", "Safety DB": "SAFETY_SOURCE", "EVDAS": "EVDAS", "FAERS": "FAERS", "VAERS": "VAERS", "VigiBase": "VIGIBASE"]

        Map dataSources = [
                (Constants.SystemPrecheck.SAFETY)  : dataSource_pva,
                (Constants.SystemPrecheck.FAERS)   : dataSource_faers,
                (Constants.SystemPrecheck.VIGIBASE): dataSource_vigibase,
                (Constants.SystemPrecheck.VAERS)   : dataSource_vaers,
                (Constants.SystemPrecheck.EVDAS)   : dataSource_eudra,
                (Constants.SystemPrecheck.JADER)   : dataSource_jader
        ]

        List<Map> result = []
        dataSources.each { dbType, dataSource ->
            if (dbType == Constants.SystemPrecheck.SAFETY)
                result.addAll(preCheckService.getDbStatus(dbType, dataSource, true, true))
            result.addAll(preCheckService.getDbStatus(dbType, dataSource, false, false))
        }
        Map groupedData = result.groupBy { it?.entityKey }
        int rowIndex = 1
        prechecks.each { precheckLabel, precheck ->
            List precheckList = groupedData[precheck]
            if (precheckList) {
                precheckList.each { precheckData ->
                    int cellNumber = 0
                    Row row = sheet.createRow(rowIndex++)
                    Cell dynamicKey = row.createCell(cellNumber++)
                    Cell labelCell = row.createCell(cellNumber++)
                    Cell precheckCell = row.createCell(cellNumber++)
                    Cell mandatoryCell = row.createCell(cellNumber++)
                    Cell warningCell = row.createCell(cellNumber++)
                    String preCheckVal = precheckData?.precheckName == 'RAM Details' ? 'Memory Details' : precheckData?.precheckName
                    dynamicKey.setCellValue(precheckLabel + "_" + preCheckVal)
                    labelCell.setCellValue(precheckLabel)
                    precheckCell.setCellValue(preCheckVal)
                    mandatoryCell.setCellValue(precheckData?.optional ? "No" : "Yes")
                    warningCell.setCellValue(precheckData?.warning ? "Yes" : "No")
                }
            }
        }
    }

    void populateBusinessRuleDataHeaderRowForExport(XSSFSheet sheet, def exportKeys, XSSFWorkbook workbook) {
        int cellNumber = 0
        Row headerRow = sheet.createRow(0)
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        allKeys.unique().each { String headerColumnValue ->
            sheet.setColumnWidth(cellNumber, 20 * 200)
            Cell cell = headerRow.createCell(cellNumber++)
            cell.setCellValue(headerColumnValue.toString())
            cell.setCellStyle(applyColumnHeaderStyle(workbook))
        }
    }


    void populateBusinessRuleDataForExport(XSSFSheet sheet, sheetName, exportKeys) {
        int rowNumber = 1
        Map columnsMappingMap = columnsMapping[sheetName]
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        BusinessConfiguration.findAll().each { BusinessConfiguration businessConfigObject ->
            Set<RuleInformation> ruleInfoObjectsList = businessConfigObject.ruleInformations
            ruleInfoObjectsList.each { RuleInformation ruleInfoObject ->
                Row row = sheet.createRow(rowNumber++)
                int cellNumber = 0
                Map expressionObj = JSON.parse(ruleInfoObject.ruleJSON).all.containerGroups[0]
                Stack expressionsStack = generateCriteria(expressionObj, new Stack<>())
                allKeys.unique().each { requiredColumn ->
                    switch (true) {
                        case (requiredColumn == "Rule Name" || columnsMappingMap."${requiredColumn}" == "description"):
                            Cell cell = row.createCell(cellNumber++)
                            String cellValueResult = getCellValueResult(businessConfigObject, columnsMappingMap."${requiredColumn}")
                            cell.setCellValue(cellValueResult)
                            break
                        case (columnsMappingMap."${requiredColumn}" == "isGlobalRule"):
                            Cell cell = row.createCell(cellNumber++)
                            cell.setCellValue(businessConfigObject[columnsMappingMap."${requiredColumn}"] ? "Global" : "Product")
                            break
                        case (columnsMappingMap."${requiredColumn}" == "isSingleCaseAlertType"):
                            Cell cell = row.createCell(cellNumber++)
                            cell.setCellValue(ruleInfoObject[columnsMappingMap."$requiredColumn"] ? 'Individual Case Alert' : 'Aggregate Alert')
                            break
                        case (columnsMappingMap."${requiredColumn}" == "isFirstTimeRule" || columnsMappingMap."${requiredColumn}" == "isBreakAfterRule" || requiredColumn == "Sub Rule Name"):
                            Cell cell = row.createCell(cellNumber++)
                            String cellValueResult = ""
                            String fieldName = columnsMappingMap."$requiredColumn"
                            if (fieldName) {
                                cellValueResult = getCellValueResult(ruleInfoObject, fieldName)
                            }
                            cell.setCellValue(cellValueResult)
                            break
                        case (columnsMappingMap."${requiredColumn}" == "dataSource"):
                            Cell cell = row.createCell(cellNumber++)
                            String cellValueResult = ""
                            String fieldName = columnsMappingMap."$requiredColumn"
                            if (fieldName) {
                                cellValueResult = getCellValueResult(businessConfigObject, fieldName)
                            }
                            cellValueResult = DataSourceEnum."${cellValueResult.toUpperCase()}"?.value() ?: " "
                            cell.setCellValue(cellValueResult)
                            break
                    }
                }

                rowNumber = populateCriteriaDataForExport(expressionsStack, sheet, rowNumber, columnsMappingMap, allKeys, sheetName)
            }
        }
    }

    int populateCriteriaDataForExport(Stack expressionsStack, XSSFSheet sheet, int rowNumber, Map columnsMappingMap, List<String> allKeys,String sheetName) {
        Row headerRow = sheet.getRow(0)
        Map category = ["ALGORITHM" : "Algorithm", "COUNTS" : "Counts", "QUERY_CRITERIA" : "Query Criteria", "REVIEW_STATE" : "Last Review Disposition", "SIGNAL_REVIEW_STATE" : "Last Signal Review Disposition", "LAST_REVIEW_DURATION" : "Last Review Duration"]
        expressionsStack.each { stack ->
            Row row = sheet.createRow(rowNumber++)
            int columnIndex = findCellIndexFromRowByCellValue(headerRow, "Operator")

            if (stack.keyword) {
                Cell cell = row.createCell(columnIndex++)
                def cellValueResult = stack.keyword
                cell.setCellValue((cellValueResult instanceof List || cellValueResult instanceof Set) ? cellValueResult.sort().join(', ') : cellValueResult as String)
            }
            if (stack.expression) {
                int cellNumber = 0
                allKeys.unique().each {String columnKey ->
                    Cell cell = row.createCell(cellNumber++)
                    def cellValueResult
                    if (sheetName == "Advanced Filter (View Only)" && columnKey == 'Field'){
                        cellValueResult = fieldList.find { it -> it.name == stack.expression[columnsMappingMap."$columnKey"]}?.display ?: stack.expression[columnsMappingMap."$columnKey"]
                    } else if(sheetName == "Business Rule (View Only)" && (columnKey == 'Select Attribute' || columnKey == 'Threshold')){
                        cellValueResult = alertFields.find { it -> it.name == stack.expression[columnsMappingMap."$columnKey"] }?.display as String ?: stack.expression[columnsMappingMap."$columnKey"]
                        if (stack.expression["category"] == "QUERY_CRITERIA" && columnKey == 'Select Attribute') {
                            cellValueResult = stack.expression["queryName"]
                        }
                        if(columnKey == 'Threshold' && (cellValueResult == 'null' || cellValueResult == 'NULL')){
                            cellValueResult = ""
                        }
                    } else if(sheetName == "Business Rule (View Only)" && columnKey == 'Category'){
                        cellValueResult = category[stack.expression[columnsMappingMap."$columnKey"]] ?: stack.expression[columnsMappingMap."$columnKey"]
                    } else if(sheetName == "Business Rule (View Only)" && columnKey in ["Split Signals to PT Level", "Associate PEC’s to Closed Signals", "Associate Multiple Signals to PEC’s"] && stack.expression["category"] == "SIGNAL_REVIEW_STATE"){
                        cellValueResult = stack.expression[columnsMappingMap."$columnKey"] == 'true' ? "Yes" : "No"
                    } else{
                        cellValueResult = stack.expression[columnsMappingMap."$columnKey"]
                    }
                    cell.setCellValue((cellValueResult instanceof List || cellValueResult instanceof Set) ? cellValueResult.sort().join(', ') : cellValueResult as String)
                }
            }
            if (stack.closingBracket) {
                Cell cell = row.createCell(columnIndex++)
                def cellValueResult = stack.closingBracket
                cell.setCellValue((cellValueResult instanceof List || cellValueResult instanceof Set) ? cellValueResult.sort().join(', ') : cellValueResult as String)
            }
        }
        return rowNumber;
    }

    int findCellIndexFromRowByCellValue(Row row, String cellValue) {
        for (Cell cell : row) {
            if (cell.getStringCellValue().equals(cellValue)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    def generateCriteria(Map expressionObj, Stack<String> stk) {
        if (expressionObj) {
            if (expressionObj.containsKey('keyword')) {
                def map = ["keyword": "( " + expressionObj.keyword]
                stk.push(map)

                for (int i = 0; i < expressionObj.expressions.size(); i++) {
                    generateCriteria(expressionObj.expressions[i], stk)
                }
                stk.push("closingBracket": ")")
            } else {
                if (expressionObj.containsKey('expressions')) {
                    for (int i = 0; i < expressionObj.expressions.size(); i++) {
                        generateCriteria(expressionObj.expressions[i], stk)
                    }
                } else {
                    stk.push("expression": expressionObj)
                }
            }
            return stk
        }
        return stk
    }

    void populateAdvancedFilterDataForExport(XSSFSheet sheet, sheetName, exportKeys) {
        int rowNumber = 1
        Map columnsMappingMap = columnsMapping[sheetName]
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        columnsMappingMap.domainClass.list().each { domainObject ->
            Row row = sheet.createRow(rowNumber++)
            int cellNumber = 0
            Stack<String> stk = new Stack<>()
            JSONObject object = JSON.parse(domainObject.JSONQuery)
            Map expressionObj = object.all.containerGroups[0]
            Stack finalStack = generateCriteria(expressionObj, stk)
            allKeys.unique().each { requiredColumn ->
                switch (true) {
                    case ["Name", "Description", "JSON", "Criteria", "Owner"].contains(requiredColumn):
                        Cell cell = row.createCell(cellNumber++)
                        if(requiredColumn == "Owner"){
                            cell.setCellValue(domainObject["user"].username)
                        } else{
                            cell.setCellValue(domainObject[columnsMappingMap."$requiredColumn"])
                        }
                        break
                    case (requiredColumn == "Alert Type"):
                        Cell cell = row.createCell(cellNumber++)
                        String cellValue = domainObject[columnsMappingMap."$requiredColumn"]
                        if(cellValue.contains("Single Case")){
                            cellValue = cellValue.replace("Single","Individual")
                        }
                        else if(cellValue.contains("Aggregate Case")){
                            cellValue = cellValue.replace("Aggregate Case","Aggregate")
                        }
                        if(cellValue.toLowerCase().contains("on demand")){
                            cellValue = cellValue.toLowerCase().replace("on demand", "")
                            cellValue = cellValue.replace("-","- ")
                            cellValue = "Ad-hoc " + cellValue
                        }
                        cell.setCellValue(cellValue.toUpperCase())
                        break
                    case (requiredColumn == "Shared With Groups"):
                        Cell cell = row.createCell(cellNumber++)
                        try {
                            def cellValueResult = (domainObject[columnsMappingMap."$requiredColumn"]*.name).findAll { it != null }.sort().join(', ')
                            cell.setCellValue(cellValueResult)
                        }
                        catch (Exception ex) {
                            cell.setCellValue("")
                        }
                        break
                    case (requiredColumn == "Shared With Users") :
                        Cell cell = row.createCell(cellNumber++)
                        try {
                            def cellValueResult = (domainObject[columnsMappingMap."$requiredColumn"]*.username).findAll { it != null }.sort().join(', ')
                            cell.setCellValue(cellValueResult)
                        } catch (Exception ex) {
                            cell.setCellValue("")
                        }
                        break
                    case (requiredColumn == "Dynamic_Key") :
                        Cell cell = row.createCell(cellNumber++)
                        List<String> fieldNamesForKey = columnsMappingMap."$requiredColumn"
                        String cellValueResult = getStringValueForDynamicKey(domainObject, fieldNamesForKey)
                        cell.setCellValue(cellValueResult)
                        break
                }
            }
            if (sheetName == "Advanced Filter (View Only)") rowNumber = populateCriteriaDataForExport(finalStack, sheet, rowNumber, columnsMappingMap, allKeys, sheetName)
        }
    }

    String getStringValueForDynamicKey(domainObject, List<String> fieldNamesForKey) {
        String cellValueResult = ""
        fieldNamesForKey.forEach {fieldName ->
            String value = domainObject[fieldName]
            if (value) {
                cellValueResult = cellValueResult.equals("") ? value : cellValueResult + "_" + value
            }
        }
        return cellValueResult
    }

    List<String> getAllRoles(){
        Role.list().collect { it.toString().trim() }.sort()
    }

    void populateHeaderRow(XSSFSheet sheet, def exportKeys, XSSFWorkbook workbook, Map<String, Integer> columnNameToCount = null) {
        int cellNumber = 0
        Row headerRow = sheet.createRow(0)
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        allKeys.unique()
                .collect { columnName ->
                    def count = columnNameToCount?.get(columnName) ?: 1

                    [columnName] + (2..<(count + 1)).collect { columnName + ' ' + it }
                }
                .flatten()
                .each { String headerColumnValue ->
                    if (headerColumnValue == "Roles") {
                        usersHeaderRolesRow = getAllRoles()  - excludedRoles
                        usersHeaderRolesRow.each { role ->
                            sheet.setColumnWidth(cellNumber, 25 * 256)
                            Cell cell = headerRow.createCell(cellNumber++)
                            cell.setCellValue(role)
                            cell.setCellStyle(applyColumnHeaderStyle(workbook))
                        }
                    } else if (headerColumnValue in ["IME", "DME", "Special Monitoring", "Stop List"]) {
                        sheet.setColumnWidth(cellNumber, 25 * 256)
                        Cell cell = headerRow.createCell(cellNumber++)
                        if (headerColumnValue == "IME") {
                            cell.setCellValue(Holders.config.importantEvents.ime.label)
                        } else if (headerColumnValue == "DME") {
                            cell.setCellValue(Holders.config.importantEvents.dme.label)
                        } else if (headerColumnValue == "Special Monitoring") {
                            cell.setCellValue(Holders.config.importantEvents.specialMonitoring.label)
                        } else if (headerColumnValue == "Stop List") {
                            cell.setCellValue(Holders.config.importantEvents.stopList.label)
                        }
                        cell.setCellStyle(applyColumnHeaderStyle(workbook))
                    } else {
                        sheet.setColumnWidth(cellNumber, 25 * 256)
                        Cell cell = headerRow.createCell(cellNumber++)
                        cell.setCellValue(headerColumnValue.toString())
                        cell.setCellStyle(applyColumnHeaderStyle(workbook))
                    }
                }
    }

    Map<String, Integer> populateBusinessRuleData(XSSFSheet sheet, String sheetName, def exportKeys) {
        int cellNumber = 0
        int rowNumber = 1
        def columnsMappingMap = columnsMapping[sheetName]
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        List<List<Object>> sheetData = []
        Map<String, Integer> columnNameToCount = [:]

        BusinessConfiguration.findAll().each { it ->
            RuleInformation.findAll().each { ruleObject ->
                if (ruleObject.businessConfigurationId == it.id) {
                    List<Object> rowData = []
                    cellNumber = 0
                    allKeys.unique().each { requiredColumn ->
                        def columnValue

                        switch (true) {
                            case (columnsMappingMap."${requiredColumn}" == "businessConfiguration") :
                                columnValue = it.ruleName.toString()
                                break
                            case (columnsMappingMap."${requiredColumn}" == "isSingleCaseAlertType"):
                                columnValue = ruleObject[columnsMappingMap."$requiredColumn"] ? 'Individual Case Alert' : 'Aggregate Alert'
                                break
                            default:
                                String fieldName = columnsMappingMap."$requiredColumn"
                                if (fieldName) {
                                    columnValue = getCellValueResult(ruleObject, fieldName)
                                }
                                break
                        }

                        if (columnValue && columnValue.length() > CELL_SIZE_LIMIT) {
                            columnValue = columnValue.toList().collate(CELL_SIZE_LIMIT)*.join()

                            columnNameToCount.compute(requiredColumn) { key, count ->
                                Integer.max(columnValue.size(), count ?: 1)
                            }
                        }
                        rowData << columnValue
                    }
                    sheetData << rowData
                }
            }
        }
        fillSheet(sheetData, sheet, rowNumber, columnNameToCount, allKeys)

        columnNameToCount
    }

    XSSFCellStyle applyColumnHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    boolean includeRecordInExport(String sheetName, def domainObject) {
        if (domainObject && domainObject instanceof Group && domainObject.groupType) {
            String groupType = domainObject.groupType.value
            return sheetName && sheetName.contains(groupType)
        }
        return true
    }

    void populateData(XSSFSheet sheet, String sheetName, def exportKeys) {
        int cellNumber = 0
        int rowNumber = 1
        def map = columnsMapping[sheetName]
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        map?.domainClass?.findAll().each { domainObject ->
            boolean includeRecord = includeRecordInExport(sheetName, domainObject)
            if(includeRecord) {
                Row row = sheet.createRow(rowNumber++)
                cellNumber = 0
                allKeys.unique().each { requiredColumn ->
                    switch(true) {
                        case (requiredColumn == "Dynamic_Key") :
                            Cell cell = row.createCell(cellNumber++)
                            List<String> fieldNamesForKey = map."$requiredColumn"
                            String cellValueResult = getStringValueForDynamicKey(domainObject, fieldNamesForKey)
                            cell.setCellValue(cellValueResult)
                            break
                        case (requiredColumn == "Alert Workflow" || requiredColumn == "Signal Workflow" || requiredColumn == "Alert Priority" ||
                                requiredColumn == "Signal Priority" || requiredColumn == "Case Addition"):
                            def data = JSON.parse(domainObject["feature"])
                            Cell cell = row.createCell(cellNumber++)
                            String value = data[map."$requiredColumn"]
                            cell.setCellValue(value == "on" ? "Yes" : "No")
                            break
                        case (map."${requiredColumn}" == "incomingDisposition" || map."${requiredColumn}" == "targetDisposition"):
                            Cell cell = row.createCell(cellNumber++)
                            String disposition = Disposition.findById(domainObject[map."$requiredColumn"].id).displayName
                            cell.setCellValue(disposition)
                            break
                        case (map."${requiredColumn}" == "workflowGroups" || map."${requiredColumn}" == "allowedUserGroups"):
                            Cell cell = row.createCell(cellNumber++)
                            def groups = domainObject[map."$requiredColumn"] ? Group.findAllByNameInList(domainObject[map."$requiredColumn"]) : ""
                            if (groups) {
                                cell.setCellValue(groups*.name.sort().join(', '))

                            } else {
                                cell.setCellValue("")
                            }
                            break
                        case(requiredColumn == "Roles"):
                            Set<String> userRoles = domainObject?.getAuthorities(false)*.toString()
                            usersHeaderRolesRow.each { role ->
                                if (userRoles?.contains(role)) {
                                    Cell cell = row.createCell(cellNumber++)
                                    cell.setCellValue("Yes")
                                } else {
                                    Cell cell = row.createCell(cellNumber++)
                                    cell.setCellValue("No")
                                }
                            }
                            break
                        case (requiredColumn == "workflow Groups"):
                            Cell cell = row.createCell(cellNumber++)
                            cell.setCellValue(domainObject.getWorkflowGroup()*.name?.sort()?.join(','))
                            break
                        case (requiredColumn == "Language" || requiredColumn == "Timezone") :
                            Cell cell = row.createCell(cellNumber++)
                            cell.setCellValue(requiredColumn == "Language" ? domainObject[map."$requiredColumn"]?.locale?.displayName : ViewHelper.getMessageByTimeZoneId(domainObject[map."$requiredColumn"]?.timeZone))
                            break
                        case (requiredColumn == "Emails"):
                            Cell cell = row.createCell(cellNumber++)
                            def cellValueResult = (domainObject["mailUsers"]*.fullName + domainObject["mailGroups"]*.name).findAll { it != null }.sort().join(', ')
                            cell.setCellValue(cellValueResult)
                            break
                        case (requiredColumn == "Email Address"):
                            Cell cell = row.createCell(cellNumber++)
                            String cellValueResult = ""
                            boolean individualMails = false;
                            if (map."$requiredColumn" && domainObject[map."$requiredColumn"]){
                                cellValueResult = domainObject[map."$requiredColumn"] as String
                                individualMails = true;
                            }
                            if ((domainObject["mailGroups"] + domainObject["mailUsers"])) {
                                if (individualMails) cellValueResult += ", "
                                cellValueResult += (domainObject["mailGroups"] + domainObject["mailUsers"]).findAll { it != null }.join(', ')
                            }
                            cell.setCellValue(cellValueResult)
                            break
                        case (["Disposition", "Review Period Disposition (Days)"].contains(requiredColumn)):
                            Cell cell = row.createCell(cellNumber++)
                            List<String> cellValueResult = domainObject.dispositionConfigs?.sort{it.id}?.collect {
                                requiredColumn == "Disposition" ? it?.disposition?.displayName : it?.reviewPeriod?.toString()
                            }
                            cell.setCellValue(cellValueResult?.join(','))
                            break
                        case (requiredColumn == "Users"):
                            Cell cell = row.createCell(cellNumber++)
                            String cellValueResult = UserGroupMapping.findAllByGroup(domainObject).collect{it?.user?.username}.sort().join(", ")
                            cell.setCellValue(cellValueResult)
                            break
                        case (sheetName == "Product Substance Frequency" && (requiredColumn == "Start Date" || requiredColumn == "End Date")):
                            Cell cell = row.createCell(cellNumber++)
                            def cellValueResult = ""
                            String fieldName = map."$requiredColumn"
                            if (fieldName) {
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                Date date = inputFormat.parse(getCellValueResult(domainObject, fieldName))
                                cellValueResult = toDateString(date)
                            }
                            cell.setCellValue(cellValueResult)
                            break
                        case requiredColumn == "Alert Type":
                            Cell cell = row.createCell(cellNumber++)
                            def cellValueResult = domainObject[map."$requiredColumn"]
                            if(cellValueResult.contains("Single Case")){
                                cellValueResult = cellValueResult.replace("Single","Individual")
                            }
                            else if(cellValueResult.contains("Aggregate Case")){
                                cellValueResult = cellValueResult.replace("Aggregate Case","Aggregate")
                            }
                            cell.setCellValue(cellValueResult)
                            break
                        default :
                            Cell cell = row.createCell(cellNumber++)
                            String cellValueResult = ""
                            String fieldName = map."$requiredColumn"
                            if (fieldName) {
                                cellValueResult = getCellValueResult(domainObject, fieldName)
                            }
                            cell.setCellValue(cellValueResult)
                    }
                }
            }
        }
    }

    Map<String, Integer> populateDataSplittingColumns(XSSFSheet sheet, String sheetName, def exportKeys) {
        Map<String, Integer> columnNameToCount = [:]
        int rowNumber = 1
        def map = columnsMapping[sheetName]
        List<String> allKeys = exportKeys["primaryKeys"] + exportKeys["include"]
        allKeys = allKeys.unique()
        List<List<Object>> sheetData = []
        List domainObjectList = []
        domainObjectList = map?.domainClass?.findAll()
        if(sheetName == "Views"){
            domainObjectList = ViewInstance.findAll{name != "System View"}
        }
        domainObjectList?.each { domainObject ->
            boolean includeRecord = includeRecordInExport(sheetName, domainObject)
            if(includeRecord) {
                List<Object> rowData = []
                allKeys.each { requiredColumn ->
                    def columnValue

                    switch(true) {
                        case (requiredColumn == "Users"):
                            columnValue = UserGroupMapping.findAllByGroup(domainObject).collect{it?.user?.username}.sort().join(", ")
                            break
                        case (map."${requiredColumn}" == "groupType"):
                            String group = domainObject[map."$requiredColumn"]
                            columnValue = group == "USER_GROUP" ? "User Group" : "Workflow Group"
                            break
                        case (map."${requiredColumn}" == "defaultSignalDisposition" || map."${requiredColumn}" == "autoRouteDisposition" || map."${requiredColumn}" == "defaultQualiDisposition"
                                || map."${requiredColumn}" == "defaultQuantDisposition" || map."${requiredColumn}" == "defaultAdhocDisposition" || map."${requiredColumn}" == "defaultEvdasDisposition"
                                || map."${requiredColumn}" == "defaultLitDisposition") :
                            columnValue = Disposition.findById(domainObject[map."$requiredColumn"]?.id)?.displayName
                            break
                        case (map."$requiredColumn" == "alertLevelDispositions"):
                            columnValue = domainObject["alertDispositions"]?.sort{it.id}.join(', ')
                            break
                        case(requiredColumn == "Roles"):
                            usersHeaderRolesRow = getAllRoles()  - excludedRoles
                            Set<String> userRoles = domainObject?.getAuthorities()*.toString()
                            usersHeaderRolesRow.each { role ->
                                if (userRoles?.contains(role)) {
                                    rowData << "Yes"
                                } else {
                                    rowData << "No"
                                }
                            }
                            break
                        case (requiredColumn == "Columns List") :
                            JsonSlurper jsonSlurper = new JsonSlurper()
                            def columnSequence = jsonSlurper.parseText(domainObject.columnSeq)
                            List columnList = []
                            columnSequence.each { key, value ->
                                if(value.containerView == 1)
                                    columnList.add(value.label)
                            }
                            columnValue = columnList.join(', ') as String
                            break
                        case (requiredColumn == "Advanced Filter") :
                            String advName = AdvancedFilter.findById(domainObject[map."$requiredColumn"]?.id)?.name
                            columnValue = advName as String
                            break
                        case (sheetName == "Views" && (requiredColumn == "Shared With Users" || requiredColumn == "Shared With Groups")):
                            try {
                                columnValue = domainObject[map."${requiredColumn}"].sort{it.id}.join(', ')
                            } catch (Exception ex) {
                                columnValue = ""
                            }
                            break
                        case (requiredColumn == "Event Term"):
                            def eventList
                            def eventMap = [:]
                            if (domainObject[map."$requiredColumn"]) {
                                eventMap = emergingIssueService.prepareEventMap(domainObject[map."$requiredColumn"], eventList)
                            } else {
                                eventMap.put("Event Group", getProdEventGroupNameFieldFromJson(domainObject["eventGroupSelection"]))
                            }
                            StringBuilder str = new StringBuilder()
                            eventMap.each { key, value ->
                                if (value instanceof String) {
                                    str.append(key + " : " + value + "\n")
                                } else {
                                    str.append(key + " : " + value.join(', ') + "\n")
                                }
                            }
                            str.setLength(str.length() - 1)

                            columnValue = str.toString()
                            break
                        case (requiredColumn == "Product Term"):
                            def productMap = [:]
                            StringBuilder str = new StringBuilder()
                            if (domainObject[map."$requiredColumn"]) {
                                productMap = emergingIssueService.prepareProductMap(domainObject[map."$requiredColumn"], domainObject["dataSourceDict"])
                            } else if (domainObject["productGroupSelection"]) {
                                productMap.put("Product Group", domainObject["products"])
                            } else {
                                str.append(" \n")
                            }
                            productMap.each { key, value ->
                                if (value instanceof String) {
                                    str.append(key + " : " + value + "\n")
                                } else {
                                    str.append(key + " : " + value.join(', ') + "\n")
                                }
                            }
                            str.setLength(str.length() - 1)

                            columnValue = str.toString()
                            break
                        case (requiredColumn == "Dynamic_Key") :
                            List<String> fieldNamesForKey = map."$requiredColumn"
                            String cellValueResult = getStringValueForDynamicKey(domainObject, fieldNamesForKey)
                            if (sheetName == "Important Events") {
                                columnValue = DigestUtils.sha256Hex(cellValueResult)
                            } else {
                                columnValue = cellValueResult
                            }
                            break
                        case (sheetName == "Business Rule" && requiredColumn == "Rule Type(Product/Global)"):
                            String cellValueResult = ""
                            String fieldName = map."$requiredColumn"
                            if (fieldName) {
                                cellValueResult = getCellValueResult(domainObject, fieldName)
                            }
                            columnValue = cellValueResult=="Yes" ? "Global" : "Product"
                            break
                        case (sheetName == "Business Rule" && requiredColumn == "Data Source"):
                            String cellValueResult = ""
                            String fieldName = map."$requiredColumn"
                            if (fieldName) {
                                cellValueResult = getCellValueResult(domainObject, fieldName)
                            }
                            String temp =  cellValueResult.toUpperCase()
                            columnValue = DataSourceEnum."${temp}".value() ?: " "
                            break
                        case (sheetName == "Business Rule" && requiredColumn == "Include WHO Drugs"):
                            if(domainObject["productSelection"]) {
                                JsonSlurper jsonSlurper = new JsonSlurper()
                                def productSelection = jsonSlurper.parseText(domainObject["productSelection"])
                                columnValue = productSelection["100"]? "Yes" : "No"
                            } else if(domainObject["productGroupSelection"]) {
                                JsonSlurper jsonSlurper = new JsonSlurper()
                                def productGroupSelection = jsonSlurper.parseText(domainObject["productGroupSelection"])
                                columnValue = "No"
                                productGroupSelection.each{it ->
                                    if(it?.'includeWHODrugs'){
                                        columnValue = "Yes"
                                    }
                                }
                            } else {
                                columnValue = "No"
                            }
                        case (requiredColumn == "View Type"):
                            String cellValueResult = domainObject[map."$requiredColumn"]
                            if(cellValueResult.contains("Single Case")){
                                cellValueResult = cellValueResult.replace("Single","Individual")
                            }
                            else if(cellValueResult.contains("Aggregate Case")){
                                cellValueResult = cellValueResult.replace("Aggregate Case","Aggregate")
                            }
                            columnValue = cellValueResult
                            break
                        default :
                            String cellValueResult = ""
                            String fieldName = map."$requiredColumn"
                            if (fieldName) {
                                cellValueResult = getCellValueResult(domainObject, fieldName)
                            }

                            columnValue = cellValueResult ?: ""
                    }

                    if (columnValue && columnValue.length() > CELL_SIZE_LIMIT) {
                        columnValue = columnValue.toList().collate(CELL_SIZE_LIMIT)*.join()

                        columnNameToCount.compute(requiredColumn) { key, count ->
                            Integer.max(columnValue.size(), count ?: 1)
                        }
                    }
                    if (!(requiredColumn == "Roles")) rowData << columnValue
                }

                sheetData << rowData
            }
        }

        fillSheet(sheetData, sheet, rowNumber, columnNameToCount, allKeys)

        columnNameToCount
    }

    void fillSheet(List<List<Object>> sheetData, XSSFSheet sheet, int rowNumber, Map<String, Integer> columnNameToCount, List<String> allKeys) {
        sheetData.each { rowData ->
            Row row = sheet.createRow(rowNumber++)

            int cellNumber = 0
            rowData.eachWithIndex { columnData, index ->
                String columnName = allKeys[index]
                def columnsCount = columnNameToCount[columnName] ?: 1

                0.upto(columnsCount - 1) {
                    Cell cell = row.createCell(cellNumber++)

                    if (columnData instanceof List) {
                        cell.setCellValue(columnData[it] as String)
                    } else if (it == 0) {
                        cell.setCellValue(columnData as String)
                    }
                }
            }
        }
    }

    //TODO the performance need to be handled in later release as per the tight timeline constraints the code is saving the instance per records at a time
    void parseExcelAndPopulateDB(String baseDirectoryPath, Boolean onStartup = false) {
        log.info("Reading excel file from location - ${USER_HOME_DIR + baseDirectoryPath}")
        def exportMap = Holders.config.business.config.export.map
        BusinessConfiguration businessConfigObject = null
        FileInputStream fis = new FileInputStream(USER_HOME_DIR + baseDirectoryPath)
        Workbook workbook = new XSSFWorkbook(fis)
        int totalSheets = workbook.getNumberOfSheets()
        List excludedSheetNames = ["Criteria Sheet", "Users", "Business Rule (View Only)", "Advanced Filter (View Only)", "Views", "Email Configuration"]
        for (int sheetCount = 0; sheetCount < totalSheets; sheetCount++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(sheetCount)
            String currentSheetName = sheet.getSheetName()
            if (sheet != null && !excludedSheetNames.contains(currentSheetName) && !currentSheetName.contains("Technical ")) {
                log.info("Parsing of sheet $currentSheetName started.")
                //This has been hard-coded as export of excel would be done by our system & header row would always be at first row.
                Row headerRow = sheet.getRow(0)
                List<Map<String, Object>> excelData = new ArrayList<>()
                int rowCount = sheet.getPhysicalNumberOfRows()
                int numberOfCells = headerRow.getLastCellNum()
                def columnsMappingMap = columnsMapping[currentSheetName]
                String primaryKey = exportMap[currentSheetName]?.primaryKeys?.getAt(0)
                Set currentData = fetchPrimaryKeyData(currentSheetName, primaryKey)
                for (int i = 1; i < rowCount; i++) {
                    try {
                        Row row = sheet.getRow(i)
                        Map<String, Object> currentRowDataMap = new HashMap<>()
                        Map<String, String> featureMap = new HashMap<>()
                        if (row != null && columnsMappingMap[headerRow.getCell(0)?.getStringCellValue()?.trim()] == columnsMapping[currentSheetName]?."$primaryKey"
                                && !(currentData?.contains(row.getCell(0)?.getStringCellValue()?.trim()))) {
                            for (int j = 0; j < numberOfCells; j++) {
                                Cell cell = row.getCell(j)
                                if (cell != null) {
                                    String headerStringValue =  headerRow.getCell(j).getStringCellValue().trim()
                                    String headerCell = columnsMappingMap[headerStringValue]
                                    String cellValue = row.getCell(j).getStringCellValue().trim()
                                    if(sheetNamesWithLargeData.contains(currentSheetName)) {
                                        String nextHeaderValue = headerRow.getCell(j+1)?.getStringCellValue()?.trim()?.replace(headerStringValue, "")
                                        while(nextHeaderValue && StringUtils.isNumeric(nextHeaderValue.trim())) {
                                            cellValue = cellValue + row.getCell(j+1)?.getStringCellValue()?.trim()
                                            j++
                                            nextHeaderValue = headerRow.getCell(j+1)?.getStringCellValue()?.trim()?.replace(headerStringValue, "")
                                        }
                                    }
                                    switch (true) {
                                        case (headerCell == "targetDisposition" || headerCell == "incomingDisposition" || headerCell == "defaultSignalDisposition" ||
                                                headerCell == "autoRouteDisposition" || headerCell == "defaultQualiDisposition" || headerCell == "defaultQuantDisposition" ||
                                                headerCell == "defaultAdhocDisposition" || headerCell == "defaultEvdasDisposition" || headerCell == "defaultLitDisposition"):
                                            Disposition disposition = Disposition.findByDisplayName(cellValue)
                                            currentRowDataMap.put(headerCell, disposition?.id)
                                            break
                                        case (headerCell == "groupType"):
                                            currentRowDataMap.put(headerCell, cellValue == "User Group" ? GroupType.USER_GROUP : GroupType.WORKFLOW_GROUP)
                                            break
                                        case (headerCell == "isEmailEnabled"):
                                            Boolean isEmailEnabled = cellValue as Boolean
                                            currentRowDataMap.put(headerCell, isEmailEnabled)
                                            break
                                        case (headerCell == "user"):
                                            currentRowDataMap.put(headerCell, User.findByFullName(cellValue).id)
                                            break
                                        case (headerCell == "allowedGroups" || headerCell == "allowedUserGroups" || headerCell == "workflowGroups"):
                                            if (cellValue) {
                                                List<Group> groups = Group.findAllByNameInList(cellValue.split(',').collect { it.trim() })
                                                currentRowDataMap.put(headerCell, groups)
                                            }
                                            break
                                        case (headerCell == "allowedDispositions" || headerCell == "dispositions" || headerCell == "alertDispositions"):
                                            List<Disposition> allowedDispositions = Disposition.findAllByDisplayNameInList(cellValue.split(',').collect { it.trim() })
                                            currentRowDataMap.put(headerCell, allowedDispositions)
                                            break
                                        case (headerCell == "alertWorkflow" || headerCell == "signalWorkflow" || headerCell == "alertPriority" ||
                                                headerCell == "signalPriority" || headerCell == "caseAddition"):
                                            String featureValue = cellValue == "Yes" ? "on" : ""
                                            featureMap.put(headerCell, featureValue)
                                            currentRowDataMap.put("feature", new JsonBuilder(featureMap).toPrettyString())
                                            break
                                        case (headerCell == "allowedProd"):
                                            List<String> allowedProdList = cellValue.split('#%#').collect { it }
                                            currentRowDataMap.put("allowedProductList", allowedProdList)
                                            break
                                        case ((currentSheetName == 'Sub Rule')):
                                            if (headerCell.equals("businessConfiguration")) {
                                                businessConfigObject = BusinessConfiguration.findByRuleName(cellValue);
                                                if (businessConfigObject != null) {
                                                    currentRowDataMap.put(headerCell, businessConfigObject);
                                                    currentRowDataMap.put("modifiedBy", Constants.SYSTEM_USER);
                                                    currentRowDataMap.put("createdBy", Constants.SYSTEM_USER);
                                                }
                                            } else if (businessConfigObject != null && !headerCell.equals("businessConfiguration")) {
                                                switch (headerCell) {
                                                    case "isSingleCaseAlertType":
                                                        currentRowDataMap.put(headerCell, cellValue.equals("Single Case Alert") ? true : false);
                                                        break;
                                                    default:
                                                        currentRowDataMap.put(headerCell, cellValue)
                                                        break;
                                                }
                                            } else {
                                                currentRowDataMap.put(headerCell, cellValue)
                                            }
                                            break
                                        case (headerCell == "eventName"):
                                            populateIMPData(row, currentRowDataMap, j)
                                            break
                                        case (headerCell == "productSelection"):
                                            populateProductRelatedDataForIMP(row, currentRowDataMap, j)
                                            break
                                        default:
                                            def value = cellValue
                                            if(cellValue == "Yes") {
                                                value = true as Boolean
                                            } else if(cellValue == "No") {
                                                value = false as Boolean
                                            }
                                            currentRowDataMap.put(headerCell, value)
                                            currentRowDataMap.put("modifiedBy", Constants.SYSTEM_USER)
                                            currentRowDataMap.put("createdBy", Constants.SYSTEM_USER)
                                    }
                                }
                            }
                        }
                        if (currentRowDataMap) {
                            excelData.add(currentRowDataMap)
                        }
                    } catch (Exception ex) {
                        log.info("Failed while reading data from row")
                        ex.printStackTrace()
                    }
                }
                if (excelData) {
                    log.info("Parsing of sheet $currentSheetName ended, going to save data")
                    switch (currentSheetName) {
                        case 'Signal Memo Configuration':
                            saveSignalMemoConfiguration(excelData, currentSheetName)
                            break
                        case 'Priority':
                            savePriority(excelData, currentSheetName)
                            break
                        default:
                            saveExcelDataInDB(excelData, currentSheetName)
                    }
                    refreshCache(sheet, columnsMappingMap, currentData, onStartup)
                }
                else{
                    log.info("Parsing of sheet $currentSheetName ended, no data to save")
                }
            } else if (sheet != null && currentSheetName == "Users") {
                populateUsersDataIntoDB(sheet)
            } else if (currentSheetName == "Views") {
                populateViewsIntoDB(sheet, currentSheetName)
            } else if (currentSheetName == "Email Configuration") {
                populateEmailConfig(sheet, currentSheetName)
            }
        }

        //remove file from read
        File file = new File(USER_HOME_DIR + baseDirectoryPath)
        if (file.exists()) {
            file.delete()
            log.info("File removed from read.")
        }
    }

    void populateEmailConfig(XSSFSheet sheet, String currentSheetName) {
        log.info("Parsing of sheet $currentSheetName started.")
        def exportMap = Holders.config.business.config.export.map
        Row headerRow = sheet.getRow(0)
        List<Map<String, Object>> excelData = new ArrayList<>()
        int rowCount = sheet.getPhysicalNumberOfRows()
        int numberOfCells = headerRow.getLastCellNum()
        def columnsMappingMap = columnsMapping[currentSheetName]
        String primaryKey = exportMap[currentSheetName]?.primaryKeys?.getAt(0)
        for (int i = 1; i < rowCount; i++) {
            Map<String, Object> currentRowDataMap = new HashMap<>()
            Row row = sheet.getRow(i)
            if (row != null && columnsMappingMap[headerRow.getCell(0)?.getStringCellValue()?.trim()] == columnsMapping[currentSheetName]?."$primaryKey") {
                for (int j = 0; j < numberOfCells; j++) {
                    Cell cell = row.getCell(j)
                    if (cell != null) {
                        String headerCell = columnsMappingMap[headerRow.getCell(j).getStringCellValue().trim()]
                        def value
                        if(row.getCell(j)?.getStringCellValue()?.trim().toUpperCase() == "YES") {
                            value = true as Boolean
                        } else if(row.getCell(j)?.getStringCellValue()?.trim().toUpperCase()  == "NO") {
                            value = false as Boolean
                        } else {
                            value = row.getCell(j)?.getStringCellValue()?.trim()
                        }
                        currentRowDataMap.put(headerCell, value)
                    }
                }
            }
            if(currentRowDataMap) {
                excelData.add(currentRowDataMap)
            }
        }
        updateEmailConfigInDB(excelData)
    }

    void updateEmailConfigInDB(List excelData) {
        excelData.each { Map excelRow ->
            def data = EmailNotification.findByKey(excelRow.get("key") as String)
            if (data != null && (excelRow["isEnabled"] != data?.isEnabled || excelRow["defaultValue"] != data?.defaultValue) ) {
                data.isEnabled = excelRow.isEnabled
                data.defaultValue = excelRow.defaultValue
                data.save(flush: true)
                cacheService.setEmaiNotificationCache(excelRow.key as String, excelRow.isEnabled as Boolean)
            } else if (data == null) {
                new EmailNotification(key: excelRow.key, moduleName: excelRow.moduleName, isEnabled: excelRow.isEnabled,
                        defaultValue: excelRow.defaultValue).save(flush: true, failOnError: true)
                cacheService.setEmaiNotificationCache(excelRow.key as String, excelRow.isEnabled as Boolean)
                cacheService.getCache(cacheService.CACHE_NAME_EMAIL_NOTIFICATION_MODULES).put(excelRow.key as String, excelRow.moduleName as String)
            }
        }
    }

    void savePriority(List excelData, String sheetName){
        List<AuditTrailChild> successRecords = new ArrayList<>()
        List<AuditTrailChild> failedRecords = new ArrayList<>()

        excelData.each{data->
            try{
                Priority priorityInstance = new Priority(data)
                List dispositionDisplayNames = data["displayNameDisposition"]?.tokenize(',').collect{it.trim()}
                List dispositionReviewPeriod = data["reviewPeriodDisposition"]?.tokenize(',').collect{it.trim()}

                if (dispositionDisplayNames.size() == dispositionReviewPeriod.size()) {
                    List dispositions = (0..<dispositionDisplayNames.size()).collect { i ->
                        [
                                displayName: dispositionDisplayNames[i],
                                reviewPeriod: dispositionReviewPeriod[i],
                                order: i + 1
                        ]
                    }
                    data.put("dispositions", JsonOutput.toJson(dispositions))
                    priorityService.savePriority(priorityInstance)
                    priorityService.saveDispositionConfig(data , priorityInstance)
                    AuditTrailChild auditTrailChild = new AuditTrailChild()
                    auditTrailChild.newValue = data
                    auditTrailChild.propertyName = sheetName
                    successRecords.add(auditTrailChild)
                }
                else{
                    log.info("Failed while saving row data")
                    AuditTrailChild auditTrailChild = new AuditTrailChild()
                    auditTrailChild.newValue = data
                    auditTrailChild.propertyName = sheetName
                    failedRecords.add(auditTrailChild)
                }
            }
            catch(Exception ex){
                log.info("Failed while saving row data")
                ex.printStackTrace()
                AuditTrailChild auditTrailChild = new AuditTrailChild()
                auditTrailChild.newValue = data
                auditTrailChild.propertyName = sheetName
                failedRecords.add(auditTrailChild)
            }
        }

        AuditTrail auditTrailObjectForSuccessRecords = new AuditTrail(applicationName: "PV Signal", category: AuditTrail.Category.INSERT.toString(), entityName: sheetName + " successfully imported records", username: "System", fullname: "System")
        auditTrailObjectForSuccessRecords.save(flush: true, failOnError: true)
        for (AuditTrailChild auditTrailChildObjectForSuccessRecords : successRecords) {
            auditTrailChildObjectForSuccessRecords.auditTrail = auditTrailObjectForSuccessRecords
            auditTrailChildObjectForSuccessRecords.save(flush: true)
        }
        AuditTrail auditTrailObjectForFailedRecords = new AuditTrail(applicationName: "PV Signal", category: AuditTrail.Category.INSERT.toString(), entityName: sheetName + " failed records", username: "System", fullname: "System")
        auditTrailObjectForFailedRecords.save(flush: true, failOnError: true)
        for (AuditTrailChild auditTrailChildObjectForFailedRecords : failedRecords) {
            auditTrailChildObjectForFailedRecords.auditTrail = auditTrailObjectForFailedRecords
            auditTrailChildObjectForFailedRecords.save(flush: true)
        }
    }

    void saveSignalMemoConfiguration(List excelData, String sheetName){
        List<AuditTrailChild> successRecords = new ArrayList<>()
        List<AuditTrailChild> failedRecords = new ArrayList<>()

        excelData.each{data->
            def users = ''
            data["mailAddresses"]?.tokenize(',').each{
                User user = User.findByFullName(it.trim())
                Group group = Group.findByName(it.trim())
                if(group)
                    users += "UserGroup_${group?.id},"
                else if(user)
                    users += "User_${user?.id},"
                else{
                    users += "NULL,"
                }
            }
            if(data["mailAddresses"]!=null){
                users = users.substring(0, users.length()-1)
            }
            data["mailAddresses"] = users

            boolean isSuccess = signalMemoReportService.saveSignalMemoConfig(data)
            if(isSuccess){
                AuditTrailChild auditTrailChild = new AuditTrailChild()
                auditTrailChild.newValue = data
                auditTrailChild.propertyName = sheetName
                successRecords.add(auditTrailChild)
            }
            else{
                log.info("Failed while saving row data")
                AuditTrailChild auditTrailChild = new AuditTrailChild()
                auditTrailChild.newValue = data
                auditTrailChild.propertyName = sheetName
                failedRecords.add(auditTrailChild)
            }
        }

        AuditTrail auditTrailObjectForSuccessRecords = new AuditTrail(applicationName: "PV Signal", category: AuditTrail.Category.INSERT.toString(), entityName: sheetName + " successfully imported records", username: "System", fullname: "System")
        auditTrailObjectForSuccessRecords.save(flush: true, failOnError: true)
        for (AuditTrailChild auditTrailChildObjectForSuccessRecords : successRecords) {
            auditTrailChildObjectForSuccessRecords.auditTrail = auditTrailObjectForSuccessRecords
            auditTrailChildObjectForSuccessRecords.save(flush: true)
        }
        AuditTrail auditTrailObjectForFailedRecords = new AuditTrail(applicationName: "PV Signal", category: AuditTrail.Category.INSERT.toString(), entityName: sheetName + " failed records", username: "System", fullname: "System")
        auditTrailObjectForFailedRecords.save(flush: true, failOnError: true)
        for (AuditTrailChild auditTrailChildObjectForFailedRecords : failedRecords) {
            auditTrailChildObjectForFailedRecords.auditTrail = auditTrailObjectForFailedRecords
            auditTrailChildObjectForFailedRecords.save(flush: true)
        }
    }

    def populateTechConfig() {
        String url = Holders.config.pvadmin.api.url
        String path = Holders.config.pvadmin.api.import.url
        Map data = ["appName": "PVS", "callbackUrl": Holders.config.grails.serverURL + "/hazelcastNotification/publishHazelCastNotification"]
        def response = reportIntegrationService.postData(url, path, data, Method.POST, "PVAdmin")
        log.info("Response from admin : " + response)
        if (response.status != HttpStatus.OK) {
            throw new Exception("An error Occurred in PV-Admin")
        }
    }

    void populateViewsIntoDB(XSSFSheet sheet, String currentSheetName) {
        log.info("Populating $currentSheetName data into DB started")
        def exportMap = Holders.config.business.config.export.map
        Row headerRow = sheet.getRow(0)
        int rowCount = sheet.getPhysicalNumberOfRows()
        int cellCount = headerRow.getLastCellNum()
        Map columnsMappingMap = columnsMapping[currentSheetName]
        String primaryKey = exportMap[currentSheetName]?.primaryKeys?.getAt(0)
        Set currentData = fetchPrimaryKeyData(currentSheetName, primaryKey)
        for (int i = 1; i < rowCount; i++) {
            Row currentRow = sheet.getRow(i)
            if (currentRow != null && columnsMappingMap[headerRow.getCell(0)?.getStringCellValue()?.trim()] == columnsMapping[currentSheetName]?."$primaryKey"
                    && !(currentData?.contains(currentRow.getCell(0)?.getStringCellValue()?.trim()))) {
                Map viDetailMap = [:]
                ViewInstance vi = new ViewInstance()
                int viewTypeColumnIndex = -1
                List clonedList = new ArrayList()
                List listFromConfig = []
                String columnListValue
                for (int j = 0; j < cellCount; j++) {
                    String currentHeaderCellValue = headerRow.getCell(j)?.getStringCellValue()?.trim()
                    String cellMappingValue = columnsMappingMap[currentHeaderCellValue]
                    String cellValue = currentRow.getCell(j)?.getStringCellValue()?.trim()
                    String nextHeaderValue = headerRow.getCell(j+1)?.getStringCellValue()?.trim()?.replace(currentHeaderCellValue, "")
                    while(nextHeaderValue && StringUtils.isNumeric(nextHeaderValue.trim())) {
                        cellValue = cellValue + currentRow.getCell(j+1)?.getStringCellValue()?.trim()
                        j++
                        nextHeaderValue = headerRow.getCell(j+1)?.getStringCellValue()?.trim()?.replace(currentHeaderCellValue, "")
                    }
                    if(cellValue != null && currentHeaderCellValue != "Dynamic_Key") {
                        if (cellMappingValue != "shareWithUser" && cellMappingValue != "shareWithGroup" && cellMappingValue != "user"
                                && cellMappingValue != "columnSeq" && cellMappingValue != "fixedColumns" && cellMappingValue != "alertType"
                                && cellMappingValue != "advancedFilter") {
                            viDetailMap.put(cellMappingValue, cellValue)
                            vi."${cellMappingValue}" = cellValue
                        } else if (cellMappingValue == "columnSeq") {
                            List columnsListFromExcel = cellValue.split(',').collect { it.trim() }
                            int columnIndex = -1
                            for (Cell cell1 : headerRow) {
                                if (cell1.getStringCellValue().equals("View Type")) {
                                    columnIndex = cell1.getColumnIndex()
                                }
                            }
                            columnListValue = Holders.config.configurations.viewInstances.find { it.alertType == currentRow.getCell(viewTypeColumnIndex).getStringCellValue() }?.columnList
                            if (columnListValue) {
                                listFromConfig = Holders.getConfig().configurations."${columnListValue}"
                                viDetailMap.put("columnList", columnListValue)
                            }
                            listFromConfig.each {
                                Map columnMap = new HashMap(it as Map)
                                clonedList.add(columnMap)
                            }
                            listFromConfig.each {
                                if (columnsListFromExcel.contains(it.label)) {
                                    it.containerView = 1
                                } else if (it.containerView == 1 && (columnListValue == "agaColumnOrderList" || columnListValue == "agaGroupBySMQColumnOrderList")) {
                                    if (it.label.contains("(F)")) {
                                        it.containerView = 3
                                    } else if (it.label.contains("(E)")) {
                                        it.containerView = 4
                                    } else if (it.label.contains("(VA)")) {
                                        it.containerView = 5
                                    } else if (it.label.contains("(VB)")) {
                                        it.containerView = 6
                                    } else {
                                        it.containerView = 2
                                    }
                                } else if (it.containerView == 1 && columnListValue != "agaColumnOrderList" && columnListValue != "agaGroupBySMQColumnOrderList") {
                                    it.containerView = 3
                                }
                            }
                        } else if (cellMappingValue == "user") {
                            viDetailMap.put(cellMappingValue, User.findByFullName(cellValue)?.id)
                            vi.user = User.findByFullName(cellValue)
                        } else if (cellMappingValue == "shareWithGroup") {
                            List<Group> groups = Group.findAllByNameInList(cellValue.split(',').collect { it.trim() })
                            vi.shareWithGroup = groups
                        } else if (cellMappingValue == "shareWithUser") {
                            List<User> userList = User.findAllByFullNameInList(cellValue.split(',').collect { it.trim() })
                            vi.shareWithUser = userList
                        } else if (cellMappingValue == "alertType") {
                            viDetailMap.put(cellMappingValue, cellValue)
                            vi."${cellMappingValue}" = cellValue
                            viewTypeColumnIndex = headerRow.getCell(j).getColumnIndex()
                        } else if (cellMappingValue == "advancedFilter") {
                            Long advName = AdvancedFilter.findByName(cellValue)?.id
                            viDetailMap.put(cellMappingValue, advName)
                        }
                    }
                }
                Integer fixedColumnOriginal = Holders.config.configurations.viewInstances.find { it.alertType == currentRow.getCell(viewTypeColumnIndex).getStringCellValue() }?.fixedColumns as Integer
                if(fixedColumnOriginal) {
                    viDetailMap.put("fixedColumns", fixedColumnOriginal)
                    Map columnOrderMap = viewInstanceService.addOrUpdateColumnMap(viDetailMap)
                    vi.columnSeq = JsonOutput.toJson(columnOrderMap)
                }
                listFromConfig = []
                clonedList.each {
                    Map columnMap = new HashMap(it)
                    listFromConfig.add(columnMap)
                }
                if(columnListValue) {
                    Holders.getConfig().configurations."${columnListValue}" = listFromConfig
                }
                if(vi.columnSeq) {
                    vi.save(failOnError: true)
                }
            }
        }
        log.info("Populating $currentSheetName data completed")
    }

    void populateUsersDataIntoDB(XSSFSheet sheet) {
        Row headerRow = sheet.getRow(0)
        List<Map<String, Object>> excelData = new ArrayList<>()
        int rowCount = sheet.getPhysicalNumberOfRows()
        int numberOfCells = headerRow.getLastCellNum()
        List<String> availableRolesList = getAllRoles();
        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i)
            Map<String, Object> userDataMap = new HashMap<>()
            List rolesList = []
            if (row != null) {
                for (int j = 0; j < numberOfCells; j++) {
                    Cell cell = row.getCell(j)
                    if (cell != null) {
                        String cellValue = cell.getStringCellValue()
                        String headerVal = headerRow.getCell(j)?.getStringCellValue()?.trim()
                        if (cellValue?.toUpperCase() == 'YES' && availableRolesList.contains(headerVal)) {
                            rolesList.add(headerRow.getCell(j)?.getStringCellValue()?.trim())
                        }
                        else if(!(cellValue?.toUpperCase() == 'NO' && availableRolesList.contains(headerVal))){
                            if (headerRow.getCell(j).getStringCellValue().trim() != 'Timezone' && headerRow.getCell(j).getStringCellValue().trim() != 'Language') {
                                userDataMap.put(columnsMapping.Users[headerRow.getCell(j).getStringCellValue()], cellValue.trim())
                            } else {
                                userDataMap.put(headerRow.getCell(j)?.getStringCellValue()?.toUpperCase(), cellValue.trim())
                            }
                        }
                    }
                }
                if (rolesList) {
                    userDataMap.put("ROLES",rolesList.join(','))
                }
                excelData.add(userDataMap)
            }
        }
        List fetchAllRoles = Role.list()
        List fetchAllSafetyGroups = SafetyGroup.list()
        List workflowGroups = Group.findAllByGroupType(GroupType.WORKFLOW_GROUP)
        excelData.each { user ->
            try {
                Set groupsToBeAdded = []
                String fullName = user["fullName"] ? user["fullName"].trim() : null
                if (!(User.findByUsername(user["username"]) || User.findByEmail(user["email"])) && fullName) {
                    List<Role> rolesToBeAdded = []
                    String finalTimeZone
                    if (!user.Timezone) {
                        finalTimeZone = "UTC"
                    } else {
                        TimeZoneEnum.values().each {
                            String getTimeZone = message(code: it.getI18nKey(), args: [it.gmtOffset])
                            if (getTimeZone.equals(user.Timezone)) {
                                finalTimeZone = it.timezoneId
                            }
                        }
                    }
                    groupsToBeAdded.add(Group.findByName('Default'))
                    if (user.ROLES) {
                        List roles = user.ROLES.tokenize(',')
                        roles.each { role ->
                            String roleName = role.toString().trim().replaceAll(" ", "_").replaceAll("-", "_")
                            if (EnumUtils.isValidEnum(RolesEnum, roleName)) {
                                String roleValue = RolesEnum.valueOf(roleName).value
                                rolesToBeAdded.add(fetchAllRoles.find { it.authority == roleValue })
                            }
                        }
                    }
                    if (!rolesToBeAdded.isEmpty() && !groupsToBeAdded.isEmpty()) {
                        Map userAdded = addUser(user as Map, finalTimeZone, rolesToBeAdded, fetchAllSafetyGroups, groupsToBeAdded)
                    } else {
                        log.info(user?.username + " is not saved. Roles or workflow group missing.")
                    }
                }
            } catch (Exception ex) {
                log.info("An Error Occurred when importing user : " + user.username)
                ex.printStackTrace()
            }
        }
        log.info("Users import complete.")
    }

    void saveExcelDataInDB(List excelData, String currentSheetName) {
        List<AuditTrailChild> successRecords = new ArrayList<>()
        List<AuditTrailChild> failedRecords = new ArrayList<>()
        saveData(currentSheetName, excelData, successRecords, failedRecords)
        AuditTrail auditTrailObjectForSuccessRecords = new AuditTrail(applicationName: "PV Signal", category: AuditTrail.Category.INSERT.toString(), entityName: currentSheetName + " successfully imported records", username: "System", fullname: "System")
        auditTrailObjectForSuccessRecords.save(flush: true, failOnError: true)
        for (AuditTrailChild auditTrailChildObjectForSuccessRecords : successRecords) {
            auditTrailChildObjectForSuccessRecords.auditTrail = auditTrailObjectForSuccessRecords
            auditTrailChildObjectForSuccessRecords.save(flush: true)
        }
        AuditTrail auditTrailObjectForFailedRecords = new AuditTrail(applicationName: "PV Signal", category: AuditTrail.Category.INSERT.toString(), entityName: currentSheetName + " failed records", username: "System", fullname: "System")
        auditTrailObjectForFailedRecords.save(flush: true, failOnError: true)
        for (AuditTrailChild auditTrailChildObjectForFailedRecords : failedRecords) {
            auditTrailChildObjectForFailedRecords.auditTrail = auditTrailObjectForFailedRecords
            auditTrailChildObjectForFailedRecords.save(flush: true)
        }
    }

    def populateProductRelatedDataForIMP(Row row, Map currentRowDataMap, int j) {
        Map<String, String> productLevelNameList = [:]
        Map product = [:]
        List productGroupList = []
        List<String> dataSourceListForPG = []
        String productTermValue = row.getCell(j).getStringCellValue()
        productTermValue.split('\n').each {
            List<String> termValueList = it.split(':') as List<String>
            if (termValueList && termValueList[0]?.trim() && termValueList[1]?.trim()) {
                if (termValueList[0].trim() == "Product Group") {
                    List<String> dataSourcesList = ["Safety DB", "EVDAS", "FAERS", "VIGIBASE", "VAERS"]
                    Map dataSourcesMap = ["Safety DB": "pva", "EVDAS": "eudra", "FAERS": "faers", "VIGIBASE": "vigibase", "VAERS": "vaers"]
                    termValueList[1].split(", ").each {
                        String dataSource
                        String pg = it
                        dataSourcesList.each { db ->
                            if (pg.contains(db)) {
                                pg = pg.replaceAll("," + db, "")
                                pg = pg.replaceAll(db, "")
                                if (!dataSource) {
                                    dataSource = dataSourcesMap[db]
                                }
                            }
                        }
                        if (pg.trim()) {
                            Map productGrpMultiIng = pvsProductDictionaryService.fetchProductGroup(PVDictionaryConfig.PRODUCT_GRP_TYPE, pg.replaceAll("\\(\\)", "").trim(), dataSource, 1, 30, user?.username, true, true)
                            if (productGrpMultiIng) {
                                productGroupList.add([name: productGrpMultiIng.name, id: productGrpMultiIng.id?.toString()])
                                if (productGrpMultiIng.text) {
                                    List dataSourceResponse = productGrpMultiIng.text.split(Constants.PG_RESPONSE_DATASOURSE_SEPERATOR)
                                    if (dataSourceResponse.size() > 1) {
                                        dataSourceListForPG.add(dataSourceResponse[1])
                                    }
                                }
                            }
                            Map productGrpMultiNonIng = pvsProductDictionaryService.fetchProductGroup(PVDictionaryConfig.PRODUCT_GRP_TYPE, pg.replaceAll("\\(\\)", "").trim(), dataSource, 1, 30, user?.username, true, false)
                            if (productGrpMultiNonIng) {
                                productGroupList.add([name: productGrpMultiNonIng.name, id: productGrpMultiNonIng.id?.toString()])
                                if (productGrpMultiNonIng.text) {
                                    List dataSourceResponse = productGrpMultiNonIng.text.split(Constants.PG_RESPONSE_DATASOURSE_SEPERATOR)
                                    if (dataSourceResponse.size() > 1) {
                                        dataSourceListForPG.add(dataSourceResponse[1])
                                    }
                                }
                            }
                        }
                    }
                } else {
                    productLevelNameList.put(termValueList[0]?.trim(), termValueList[1]?.trim())
                }
            }
        }
        if (productLevelNameList && !productGroupList) {
            Boolean isPvcm = dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
            Boolean isCustomDictionary = Holders.config.custom.caseInfoMap.Enabled
            product = getProductSelection(productLevelNameList, isPvcm, isCustomDictionary)
        }
        String dataSourceDict = product.containsKey("dataSource") ? product["dataSource"] : (dataSourceListForPG ? 'pva;' + dataSourceListForPG.join(";") : null)
        currentRowDataMap.put("productSelection", product.containsKey("productSelection") && !productGroupList ? product["productSelection"] : null)
        String prodgroupJSON = productGroupList ? productGroupList as JSON : null
        currentRowDataMap.put("productGroupSelection", prodgroupJSON)
        currentRowDataMap.put("dataSourceDict", dataSourceDict)
        List products = []
        if (product["productSelection"]) {
            Map productMap = emergingIssueService.prepareProductMap(product["productSelection"], dataSourceDict)
            products += productMap.keySet()
            products += productMap.values().flatten()
        }
        if (prodgroupJSON) {
            products.add(emergingIssueService.getGroupNameFieldFromJsonProduct(prodgroupJSON, dataSourceDict))
        }
        currentRowDataMap.put("products", products.join(','))
    }

    def populateIMPData(Row row, Map currentRowDataMap, int j) {
        List<String> eventTermList = row.getCell(j).getStringCellValue().split('\n').collect { it }
        Map levelEventTermMap = [:]
        Map eventSelectionLabelMap = ["SOC": 1, "HLGT": 2, "HLT": 3, "PT": 4, "LLT": 5, "Synonyms": 6]
        Map eventSelectionMap = [1: [], 2: [], 3: [], 4: [], 5: [], 6: []]
        Map eventTermDomainMapping = ["SOC": MedDraSOC, "HLGT": MedDraHLGT, "HLT": MedDraHLT, "PT": MedDraPT, "LLT": MedDraLLT]
        List eventGroupsList = []
        eventTermList.each { it ->
            List<String> events = it.split(':')
            events[0] = events[0].trim()
            events[1] = events[1].trim()
            if (events?.size() == 2 && events[0] && events[1]) {
                if (events[0] == "Event Group") {
                    eventSelectionMap = null
                    events[1].split(" #%# ").each {
                        if (it.trim()) {
                            Map eventGroup = pvsProductDictionaryService.fetchProductGroup(PVDictionaryConfig.EVENT_GRP_TYPE, it.trim(), 'pva', 1, 30, user?.username, true, false)
                            if (eventGroup) {
                                eventGroupsList.add([name: eventGroup.name, id: eventGroup.id?.toString()])
                            }
                        }
                    }
                } else {
                    levelEventTermMap.put(events[0], events[1])
                    List eventsList = []
                    eventTermDomainMapping[events[0]]."pva".withTransaction {
                        List<String> eventsSearchList = events[1].split(', ')
                        eventTermDomainMapping[events[0]]."pva".findAllByNameInList(eventsSearchList).each {
                            eventsList.add(["name": it.name, "id": it.id?.toString()])
                        }
                    }
                    eventSelectionMap.put(eventSelectionLabelMap[events[0]], eventsList)
                }
            }
        }
        String finalJsonString = eventSelectionMap && eventSelectionMap?.values()?.flatten() && !eventGroupsList ? eventSelectionMap as JSON : null
        currentRowDataMap.put("eventName", finalJsonString)
        String eventGroupJSON = eventGroupsList ? eventGroupsList as JSON : null
        currentRowDataMap.put("eventGroupSelection", eventGroupJSON)
        List events = []
        if (finalJsonString) {
            Map eventMap = emergingIssueService.prepareEventMap(finalJsonString, [])
            events += eventMap.keySet()
            events += eventMap.values().flatten()
        }
        if (eventGroupJSON) {
            events.add(emergingIssueService.getGroupNameFieldFromJson(eventGroupJSON))
        }
        currentRowDataMap.put("events", events.join(','))
    }

    def refreshCache(def sheet, def sheetName, def currentData, def onStartup) {
        if(!onStartup) {
            String currentSheetName = sheet.getSheetName()
            log.info("Caching started for sheet $currentSheetName")
            def exportMap = Holders.config.business.config.export.map
            switch (currentSheetName) {
                case 'Priority':
                    cacheService.clearPriorityCache()
                    cacheService.preparePriorityCache()
                    break
                case 'Dispositions':
                    cacheService.clearDispositionCache()
                    cacheService.prepareDispositionCache()
                    break
                case 'Safety Lead':
                    if (alertService.isProductSecurity()) {
                        SafetyGroup.list().each { safetyGroupInstance ->
                            if (!(currentData.contains(safetyGroupInstance[sheetName."$primaryKey"]))) {
                                productDictionaryCacheService.updateProductDictionaryCache(safetyGroupInstance, safetyGroupInstance.allowedProductList, false)
                                cacheService.setProductGroupCache(safetyGroupInstance)
                                List<Long> safetyGroupList = []
                                safetyGroupList.add(safetyGroupInstance.id)
                                cacheService.updateProductsCacheForSafetyGroup(safetyGroupList)
                            }
                        }
                    }
                    break
                case 'Groups' :
                    String primaryKey = exportMap["Groups"]?.primaryKeys?.getAt(0)
                    if (alertService.isProductSecurity()) {
                        List<Long> groupList = []
                        Group.list().each { group ->
                            if (!(currentData.contains(group[sheetName?."$primaryKey"]))) {
                                groupService.saveGroupInfoInGroupsWebappTable(group)
                                log.info("Updating Product Dictionary Cache for Group : " + group?.name)
                                productDictionaryCacheService.updateProductDictionaryCache(group, group.allowedProductList, true)
                                log.info("Product Dictionary Cache Completed for group: " + group?.name)
                                cacheService.setGroupCache(group)
                                if (group.groupType != GroupType.WORKFLOW_GROUP) {
                                    groupList.add(group.id)
                                }
                            }
                        }
                        if (groupList) {
                            cacheService.updateProductsCacheForGroup(groupList)
                        }
                    } else {
                        Group.list().each { group ->
                            if (!(currentData.contains(group[sheetName?."$primaryKey"]))) {
                                groupService.saveGroupInfoInGroupsWebappTable(group)
                                log.info("Group '" + group.name + "' saved in mart.")
                                cacheService.getCache(cacheService.CACHE_NAME_PV_GROUP).put(group.id, group)
                            }
                        }
                    }
                    break
                case 'Business Rule':
                    cacheService.prepareBusinessConfigCacheForSelectionType()
                    break
                case 'Sub Rule':
                    BusinessConfiguration.list().each { businessConfigObject ->
                        if (businessConfigObject.enabled) {
                            cacheService.setRuleInformationCache(businessConfigObject)
                        }
                    }
                    break
            }
        }
    }

    Set fetchPrimaryKeyData(String sheetName, String primaryKey) {
        Class domain = columnsMapping[sheetName]?.domainClass
        Set currentData = []
        if (primaryKey) {
            if(primaryKey == "Dynamic_Key") {
                List<String> fieldNamesForKey = columnsMapping[sheetName]?."$primaryKey"
                 domain.findAll().forEach { domainObject ->
                    String key = getStringValueForDynamicKey(domainObject, fieldNamesForKey)
                    if(sheetName == "Important Events") {
                        key = DigestUtils.sha256Hex(key)
                    }
                    currentData.add(key)
                }
            } else {
                currentData = domain.list().collect { domainObject ->
                    domainObject[columnsMapping[sheetName]?."$primaryKey"]
                }
            }
        }
        return currentData
    }
    void saveData(def sheetName, def excelData, def successRecords, def failedRecords) {
        Class domain = columnsMapping[sheetName].domainClass
        //TODO the performance need to be handled in later release as per the tight timeline constraints the code is saving the instance per records at a time
        excelData.each {
            try {
                def domainInstance = domain.newInstance(it)
                if (sheetName == "Safety Lead") {
                    domainInstance.allowedProductList = it.allowedProductList
                }
                domainInstance.save(flush: true)
                AuditTrailChild auditTrailChild = new AuditTrailChild()
                auditTrailChild.newValue = it
                auditTrailChild.propertyName = sheetName
                successRecords.add(auditTrailChild)
            } catch (Exception exception) {
                log.info("Failed while saving row data")
                AuditTrailChild auditTrailChild = new AuditTrailChild()
                auditTrailChild.newValue = it
                auditTrailChild.propertyName = sheetName
                failedRecords.add(auditTrailChild)
                exception.printStackTrace()
            }
        }
    }

    void bindDepartment(User userInstance, def userDeptString){
        if(Objects.nonNull(userDeptString)){
            List<String> userDeptList = userDeptString.tokenize(',').collect{it.trim()}
            userInstance?.userDepartments?.clear()
            userDeptList.each{
                UserDepartment userDept = UserDepartment.findByDepartmentName(it)
                if (!userDept) {
                    userDept = new UserDepartment(departmentName: it)
                    userDept = (UserDepartment) CRUDService.saveWithAuditLog(userDept)
                }
                userInstance.addToUserDepartments(userDept)
            }
        }
    }

    Map addUser(Map user, String timeZone, List rolesToBeAdded, List<SafetyGroup> fetchAllSafetyGroups, Set<Group> groupsToBeAdded) {
        Map resultMap = [status: false]
        List safetyGroupsToBeAdded = []
        try {
            if (user.SAFETY_GROUPS) {
                List safetyGroups = user.SAFETY_GROUPS.tokenize(',')
                safetyGroups.each { safety_group ->
                    SafetyGroup isGroup = fetchAllSafetyGroups.find { it -> it.name == safety_group }
                    if (isGroup) {
                        safetyGroupsToBeAdded.add(isGroup)
                    }
                }
            }
            String createdBy = userService.getUser()?.username ?: "System"
            String modifiedBy = userService.getUser()?.username ?: "System"
            Preference preference = new Preference(locale: user.LANGUAGE == 'Japanese' ? 'ja' : 'en', timeZone: timeZone, dashboardConfig: null, createdBy: createdBy, modifiedBy: modifiedBy)
            if(user?.username) {

                User userToAdd = new User(username: user?.username, fullName: user.fullName, email: user.email, enabled: user.enabled, accountExpired: user.accountExpired,
                        accountLocked: user.accountLocked, preference: preference, groups: groupsToBeAdded,
                        createdBy: createdBy, modifiedBy: modifiedBy, safetyGroups: safetyGroupsToBeAdded)
                bindDepartment(userToAdd, user.userDepartments)
                userToAdd.save(flush:true, failOnError: true)
                userService.saveUserInfoInPvUserWebappTable(userToAdd)
                userGroupService.createUserGroupMappingsForUser(userToAdd, userToAdd.groups.collect { it.id })
                rolesToBeAdded?.each { role ->
                    new UserRole(user: userToAdd, role: role).save(flush: true, failOnError: true)
                }
                cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(userToAdd)
                new UserDashboardCounts(userId: userToAdd.id).save(flush: true, failOnError: true)
                userService.updateUserGroupCountsInBackground(userToAdd, [])
                resultMap.status = true
            }
        } catch (ValidationException ve) {
            log.error(ve.getMessage())
            resultMap.status = false
        }
        return resultMap
    }

    Boolean checkLastUpdatedTimeSync(Integer retryCount) {
        log.info("is success : " + Holders.config.refresh.tech.success)
        if(retryCount < Holders.config.refresh.configuration.retry.count){
            if (Holders.config.refresh.tech.success  == false) {
                Thread.sleep(10000)
                retryCount++
                return checkLastUpdatedTimeSync(retryCount)
            } else {
                return true
            }
        } else {
            return false
        }

    }


    Map getProductSelection(Map<String, String> productSelectionMap, Boolean isPvcm, Boolean isCustomDictionary) {
        Map product = [:]
        Map productDomainMap
        Map productLabelMap
        Map resultMap = [:]
        List<String> dataSourcesList = ["Safety DB", "EVDAS", "FAERS", "VIGIBASE", "VAERS"]
        Map dataSourcesMap = ["Safety DB": "pva", "EVDAS": "eudra", "FAERS": "faers", "VIGIBASE": "vigibase", "VAERS": "vaers"]
        if (isPvcm) { //to test in the FDA env and PVCM integrated environment
            product = ["1": [], "2": [], "3": [], "4": []]
            productLabelMap = ["Substance": "1", "Product Name": "2", "Product - Dosage Forms": "3", "Trade Name": "4"]
            productDomainMap = ["Substance": LmProdDic200, "Product Name": LmProdDic201, "Product - Dosage Forms": LmProdDic202, "Trade Name": LmProdDic203]
        } else {
            if (isCustomDictionary) {
                productDomainMap = ["1": LmProdDic200, "2": LmProdDic201, "3": LmProdDic202, "4": LmProdDic203, "5": LmProdDic204, "6": LmProdDic205, "7": LmProdDic206, "8": LmProdDic207, "9": LmProdDic208]
                def customDicList = Holders.config.custom.dictionary.list
                customDicList.keySet().each {
                    product.put(it, [])
                    productLabelMap.put(customDicList[it], it)
                }
            } else {
                product = ["1": [], "2": [], "3": [], "4": []]
                productLabelMap = ["Ingredient": "1", "Family": "2", "Product Name": "3", "Trade Name": "4"]
                productDomainMap = ["Ingredient": LmProdDic200, "Family": LmProdDic201, "Product Name": LmProdDic202, "Trade Name": LmProdDic203]
            }
        }
        String productsDataSource
        productSelectionMap.each { label, prodListString ->
            String dataSource
            dataSourcesList.each {
                if (prodListString.contains(it)) {
                    if (!dataSource) {
                        dataSource = it
                    } else {
                        List productList = prodListString.split(', ')
                        prodListString = productList.findResults { it.contains(dataSource) ? it : null }.join(', ')
                    }
                }
            }

            if (!productsDataSource) {
                productsDataSource = dataSource
                resultMap.put("dataSource", dataSourcesMap[dataSource])
            }
            prodListString = prodListString.replaceAll(productsDataSource, "").replaceAll("\\(\\)", "")
            if (productsDataSource == dataSource) {
                List<String> prodString = prodListString.split(', ')
                String queryListString = prodString.findResults { it.trim() ? "'${it.trim()}'" : null }.join(',')
                product[productLabelMap[label]] = productDomainMap.get(label)."${dataSourcesMap[dataSource]}".createCriteria().list() {
                    sqlRestriction("COL_2 in (${queryListString})")
                    projections {
                        distinct('viewId')
                        property('name')
                    }
                }.collect { [name: it[1], id: it[0]] }
            }
        }

        String productJsonString = product as JSON
        resultMap.put("productSelection", productJsonString)

        return resultMap
    }

    def getProdEventGroupNameFieldFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = emergingIssueService.parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                prdName = jsonObj.collect {
                    it.name.substring(0, it.name.lastIndexOf('(') - 1)
                }.join(" #%# ")
            }
        }
        prdName
    }

    void currentConfigurationsBackup() {
        log.info("Creating backup of current system configuration.")
        String filepath = Holders.config.pvadmin.api.backup.file.path
        XSSFWorkbook workbook = new XSSFWorkbook()
        String appURL = Holders.config.grails.serverURL
        String environmentName = new URL(appURL).host
        Date date = new Date()
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MMM-dd HH:mm" )
        String fileName = USER_HOME_DIR + filepath + "${environmentName}_" + sdf.format(date) + "GMT.xlsx"
        try {
            exportCriteriaSheet(workbook)
            exportBusinessConfiguration(workbook)
            exportTechnicalConfiguration(workbook)
            File backupDir = new File(USER_HOME_DIR + filepath)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            FileOutputStream outputStream = new FileOutputStream(fileName)
            workbook.write(outputStream)
            log.info("Backup complete : " + fileName)
        } catch(Exception ex) {
            log.info("Backup Failed")
            ex.printStackTrace()
        } finally {
            workbook.close()
        }
    }

    void populateFieldList(){
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        try {
            String query = SignalQueryHelper.rpt_to_ui_label_table()
            sql.rows(query).each {
                String label = it.CUSTOM_UI_LABEL?:it.UI_LABEL
                rptToUiLabelMap.put(String.valueOf(it.RPT_FIELD), String.valueOf(label))
            }
        } finally {
            sql.close()
        }
        List dataSources = ["safetyDb", "faers", "evdas", "vaers", "vigibase"]
        fieldList += grailsApplication.config.signal.scaVaersColumnList.clone() as List<Map>
        fieldList += grailsApplication.config.signal.scaVigibaseColumnList.clone() as List<Map>
        fieldList += grailsApplication.config.signal.scaJaderColumnList.clone() as List<Map>
        fieldList += grailsApplication.config.signal.scaFaersColumnList.clone() as List<Map>
        fieldList += grailsApplication.config.signal.scaColumnListAdhoc.clone() as List<Map>
        fieldList += grailsApplication.config.signal.scaColumnList.clone() as List<Map>
        fieldList += grailsApplication.config.signal.evdasColumnList.clone() as List<Map>
        fieldList += grailsApplication.config.signal.evdasOnDemandColumnList.clone() as List<Map>
        dataSources.each{ data ->
            fieldList += grailsApplication.config.signal.agaColumnList."${data}".clone() as List<Map>
            if(data in ["safetyDb", "faers"]){
                fieldList += grailsApplication.config.signal.agaOnDemandColumnList."${data}".clone() as List<Map>
            }
        }
        fieldList += grailsApplication.config.signal.scaOnDemandColumnList.clone() as List<Map>
        fieldList += aggregateCaseAlertService.fieldListAdvanceFilter("",false,"review",'AGGREGATE_CASE_ALERT')
        fieldList.each {
            if(rptToSignalFieldMap.get(it.name)!=null && rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))!=null){
                it.display = rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))?:it.display
            }
            if(it.get("name")=='suspectProductList' && rptToUiLabelMap.get('masterSuspProdAgg')!=null){
                it.display = rptToUiLabelMap.get('masterSuspProdAgg')?:it.display
            }
            else if(it.get("name")=='allPtList' && rptToUiLabelMap.get('masterPrefTermAll')!=null){
                it.display = rptToUiLabelMap.get('masterPrefTermAll')?:it.display
            }
            else if(it.get("name")=='ptList' && rptToUiLabelMap.get('masterPrefTermSurAll')!=null){
                it.display = rptToUiLabelMap.get('masterPrefTermSurAll')?:it.display
            }
            else if(it.get("name")=='primSuspProdList' && rptToUiLabelMap.get('masterPrimProdName')!=null){
                it.display = rptToUiLabelMap.get('masterPrimProdName')?:it.display
            }
            else if(it.get("name")=='conComitList' && rptToUiLabelMap.get('masterConcomitProdList')!=null){
                it.display = rptToUiLabelMap.get('masterConcomitProdList')?:it.display
            }
        }
    }

    void populateAlertFields(){
        List v = BusinessConfiguration.list()*.dataSource
        v.unique().each{ String it ->
            Map temp = businessConfigurationService.getSelectBoxValues(BusinessConfiguration.findByDataSource(it))
            if(it == Constants.DataSource.JADER){
                temp = businessConfigurationService.fetchSelectBoxValuesJader(BusinessConfiguration.findByDataSource(it))
            }
            alertFields += temp.categories.collect{[name:it.value,display:it.text]}
        }
    }
}
