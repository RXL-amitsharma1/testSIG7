package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.SignalReport
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.sdicons.json.validator.impl.predicates.False
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.http.util.TextUtils
import org.grails.web.json.JSONArray

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class TemplateController {
    def springSecurityService
    def userService
    def templateService
    def CRUDService
    def dynamicReportService
    def reportIntegrationService
    def sqlService
    def dataSource_pva
    def pvsProductDictionaryService
    def cacheService
    def signalAuditLogService
    def alertFieldService

    //todo: move this to Constants interface.
    public static String CUSTOM_SQL_VALUE_REGEX_CONSTANT = /:([a-zA-Z][a-zA-Z0-9_-]*)/
    public static String COLUMN_LABEL_REGEX_CONSTANT = /[0-9a-zA-Z-_ ]+/

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def index() {
        def dateRange = Constants.Commons.BLANK_STRING
        ExecutedConfiguration ec = ExecutedConfiguration.findById(params.configId)
        Configuration configuration = Configuration.get(ec?.configId)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = ec?.executedAlertDateRangeInformation
        dateRange = DateUtil.toDateString(executedAlertDateRangeInformation?.dateRangeStartAbsolute) +
                " to " +
                DateUtil.toDateString(executedAlertDateRangeInformation?.dateRangeEndAbsolute)
        def latestVersion = ExecutionStatus.findAllByConfigIdAndExecutionStatusAndType(configuration.id, ReportExecutionStatus.COMPLETED, ec.type).size()
        if(params.aggExecutionId && !TextUtils.isEmpty(params.aggExecutionId)){
            ec =  ExecutedConfiguration.findById(params.aggExecutionId)
            configuration = Configuration.get(ec?.configId)
            latestVersion = ExecutionStatus.findAllByConfigIdAndExecutionStatusAndType(configuration?.id, ReportExecutionStatus.COMPLETED,configuration?.type ).size()
        }
        List productNameList = []
        String products = ""
        if(ec?.productGroupSelection ){
            productNameList.addAll(ec.getGroupNameFieldFromJson(ec.productGroupSelection))
        }
        if (ec?.productSelection){
            productNameList.addAll(ec.getAllProductNameFieldFromJson(ec.productSelection)?.split(","))
        }
        if(ec.adhocRun && ec.dataMiningVariable){
            String miningVariable = ec.dataMiningVariable
            String productSelection = (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(ec) ? getCacheService().getUpperHierarchyProductDictionaryCache(ec.id) : getNameFieldFromJson(ec.productSelection))?:getGroupNameFieldFromJson(ec.productGroupSelection)
            if(productSelection){
                products = miningVariable + "(" + productSelection + ")"
            } else{
                products = miningVariable
            }
        }
        if(ec.adhocRun){
            latestVersion = 1
        }
        if(configuration.aggAlertId){
            configuration.numOfExecutions = 1
        }
        [alertName: params.aggExecutionId?ExecutedConfiguration.findById(params.aggExecutionId).name:configuration.name,
         version: (params.version && params.version!="null")?params.version:latestVersion?:configuration.numOfExecutions,
         description: configuration.description,
         dateRange: dateRange,
         selectedCases : params.selectedCases?:null,
         viewId: params.viewId,
         tempViewId: params.tempViewId,
         isFilterRequest: params.isFilterRequest,
         filters: params.filters?:null,
         filterList: params.filterList?:null,
         advancedFilterId: params.advancedFilterId?:null,
         productNameList:(ec.adhocRun && ec.dataMiningVariable)?products: productNameList.sort().join(", "), isAggScreen: params.isAggScreen?:false,
         hasReviewerAccess:SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_REVIEWER, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION,ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER, ROLE_SINGLE_CASE_VIEWER, ROLE_VIEW_ALL")]
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def view(Long id) {
        String url = Holders.config.pvreports.template.view.uri + "/" + id
        redirect(url: url)
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def viewExecutedTemplate(ReportTemplate executedTemplateInstance) {
        if (!executedTemplateInstance) {
            notFound()
            return
        }
        render(view: "view", model: [editable: false, currentTemplate: ReportTemplate.get(executedTemplateInstance.originalTemplateId),
                                     template: executedTemplateInstance, isExecuted: true, title: message(code: "app.label.viewExecutedTemplate")])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def create() {
        User currentUser = userService.getUser()
        ReportTemplate reportTemplateInstance = getReportTemplateInstance()
        //todo:  get rid of editable and fromCreate map entries
        render(view: "create", model: [reportTemplateInstance: reportTemplateInstance, fromCreate: true, editable: true, isAdmin: currentUser.admin])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def save(){
        ReportTemplate reportTemplateInstance = getReportTemplateInstance()
        reportTemplateInstance.owner = userService.getUser()

        populateModel(reportTemplateInstance)

        //todo:  Until we move this to the domain object constraints, we'll go ahead and attempt the update regardless of outcome. - morett
        //todo:  This will collect all validation errors at once vs. piecemeal. - morett
        reportTemplateInstance = preValidateTemplate(reportTemplateInstance, params)

        try {
            reportTemplateInstance = (ReportTemplate) CRUDService.save(reportTemplateInstance)
        } catch (ValidationException ve) {
            //todo:  get rid of editable and fromCreate map entries - morett
            //todo:  we should be able to send the reportTemplateInstance prepopulated with the data that the container needs.  -morett
            render view: "create", model: [reportTemplateInstance: reportTemplateInstance, editable: true, fromCreate: true]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.template'), reportTemplateInstance.name])
                redirect(action: "view", id: reportTemplateInstance.id)
            }
            '*' { respond reportTemplateInstance, [status: CREATED] }
        }

    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def edit(ReportTemplate reportTemplateInstance) {
        if (!reportTemplateInstance) {
            notFound()
            return
        }
        User currentUser = userService.getUser()

        if (reportTemplateInstance.isEditableBy(currentUser)) {
            int templateUsed = reportTemplateInstance.countUsage()
            if (templateUsed > 0) {
                flash.warn = """${message(code: "app.template.usage.reports", args: [templateUsed])}
                            <linkQuery>${createLink(controller: 'template', action: 'checkUsage', id: params.id)}"""
            }
            render(view: "edit", model: [editable: true, template: reportTemplateInstance, isAdmin: currentUser.admin])
        } else {
            flash.warn = message(code: "app.template.edit.permission", args: [reportTemplateInstance.name])
            redirect(view: "index")
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def update() {
        ReportTemplate reportTemplateInstance = ReportTemplate.get(params.id)
        if (reportTemplateInstance.isEditableBy(userService.getUser())) {

            populateModel(reportTemplateInstance)

            //todo:  Until we move this to the domain object constraints, we'll go ahead and attempt the update regardless of outcome. - morett
            //todo:  This will collect all validation errors at once vs. piecemeal. - morett
            reportTemplateInstance = preValidateTemplate(reportTemplateInstance, params)

            try {
                reportTemplateInstance = (ReportTemplate) CRUDService.update(reportTemplateInstance)
            } catch (ValidationException ve) {
                render(view: "edit", model: [editable: true, template: reportTemplateInstance])
                return
            }

            flash.message = message(code: "app.template.update.success", args: [reportTemplateInstance.name])
            redirect(action: "view", id: reportTemplateInstance.id)

        } else {
            flash.warn = message(code: "app.template.delete.permission", args: [reportTemplateInstance.name])
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def delete() {
        User currentUser = userService.getUser()
        ReportTemplate template = ReportTemplate.get(params.id)
        if (template.isEditableBy(currentUser)) {
            int templateUsed = template.countUsage()
            if (templateUsed > 0) {
                flash.warn = """${message(code: "app.template.delete.usage", args: [template.name, templateUsed])}
                            <linkQuery>${createLink(controller: 'template', action: 'checkUsage', id: params.id)}"""
            } else {
                ReportTemplate deleteTemplate = templateService.deleteTemplate(template)
                if (deleteTemplate.hasErrors()) {
                    chain(action: "index", model: [error: deleteTemplate])
                }
                flash.message = message(code: "app.template.delete.success", args: [template.name])
            }
        } else {
            flash.warn = message(code: "app.template.delete.permission", args: [template.name])
        }
        redirect(view: "index")
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def copy(ReportTemplate reportTemplateInstance) {
        if (!reportTemplateInstance) {
            notFound()
            return
        }

        User currentUser = userService.getUser()
        if (!reportTemplateInstance.isViewableBy(currentUser)) {
            redirect(view: "index")
            flash.warn = message(code: "app.userPermission.message", args: [reportTemplateInstance.name, message(code: "app.label.template.lower")])
        } else {
            ReportTemplate saveTemplate = templateService.copyTemplate(reportTemplateInstance, currentUser)
            if (saveTemplate.hasErrors()) {
                chain(action: "index", model: [error: saveTemplate])
            } else {
                flash.message = message(code: "app.copy.success", args: [reportTemplateInstance.name])
                redirect(action: "view", id: saveTemplate.id)
            }
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def load() {}

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def saveJSONTemplates() {
        if (params?.JSONTemplates) {
            def listOfTemplatesJSON = "[${params?.JSONTemplates}]"
            List<ReportTemplate> templates = []
            JSONArray listOfTemplates = JSON.parse(listOfTemplatesJSON)

            listOfTemplates.each {
                List<Tag> parsedTags = null
                ReportFieldInfoList parsedColumns = null

                // CASE LINE LISTING
                ReportFieldInfoList parsedGrouping = null
                ReportFieldInfoList parsedRowCols = null

                // DATA TABULATION
                ReportFieldInfoList parsedRows = null
                List<DataTabulationMeasure> savedMeasures = []

                // CUSTOM SQL & NON CASE
                Set<CustomSQLValue> parsedCustomSQLValues = []

                if (it.tags) {
                    parsedTags = it.tags.collect { current -> Tag.findByName(current.name) }
                }

                if (it?.columnList) {
                    parsedColumns = new ReportFieldInfoList()
                    it.columnList.each {
                        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                        reportFieldInfo.reportField = ReportField.findByName(it.reportFieldName)
                        if (it.sortEnumValue) {
                            reportFieldInfo.sort = Sort.valueOfName(it.sortEnumValue)
                        }
                        parsedColumns.addToReportFieldInfoList(reportFieldInfo)
                    }
                    parsedColumns.save(failOnError: true)
                }

                if (it?.groupingList) {
                    parsedGrouping = new ReportFieldInfoList()
                    it.groupingList.each {
                        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                        reportFieldInfo.reportField = ReportField.findByName(it.reportFieldName)
                        if (it.sortEnumValue) {
                            reportFieldInfo.sort = Sort.valueOfName(it.sortEnumValue)
                        }
                        parsedGrouping.addToReportFieldInfoList(reportFieldInfo)
                    }
                    parsedGrouping.save(failOnError: true)
                }

                if (it?.rowColumnList) {
                    parsedRowCols = new ReportFieldInfoList()
                    it.rowColumnList.each {
                        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                        reportFieldInfo.reportField = ReportField.findByName(it.reportFieldName)
                        parsedRowCols.addToReportFieldInfoList(reportFieldInfo)
                    }
                    parsedRowCols.save(failOnError: true)
                }

                if (it?.rowList) {
                    parsedRows = new ReportFieldInfoList()
                    it.rowList.each {
                        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                        reportFieldInfo.reportField = ReportField.findByName(it.reportFieldName)
                        parsedRows.addToReportFieldInfoList(reportFieldInfo)
                    }
                    parsedRows.save(failOnError: true)
                }


                if (it?.measures) {
                    it?.measures.each { measure ->
                        savedMeasures.add(new DataTabulationMeasure(name: measure?.name, dateRangeCount: CountTypeEnum.valueOf(measure.dateRangeCount.name),
                                customPeriodFrom: measure?.customPeriodFrom, customPeriodTo: measure?.customPeriodTo,
                                percentageOption: PercentageOptionEnum.valueOf(measure?.percentageOption?.name),
                                showSubtotalRowAfterGroups: measure.showSubtotalRowAfterGroups,
                                showTotalRowOnly: measure?.showTotalRowOnly, showTotalAsColumn: measure?.showTotalAsColumn,
                                type: MeasureTypeEnum.valueOf(measure?.type?.name)))
                    }

                }

                List<DataTabulationColumnMeasure> savedColumnMeasureList = []
                if (it?.columnMeasureList) {
                    it.columnMeasureList.each { columnMeasure ->
                        DataTabulationColumnMeasure dtColumnMeasure = new DataTabulationColumnMeasure()
                        columnMeasure.measures.each { measure ->
                            dtColumnMeasure.addToMeasures(new DataTabulationMeasure(name: measure?.name, dateRangeCount: CountTypeEnum.valueOf(measure.dateRangeCount.name),
                                    customPeriodFrom: measure?.customPeriodFrom, customPeriodTo: measure?.customPeriodTo,
                                    percentageOption: PercentageOptionEnum.valueOf(measure?.percentageOption?.name),
                                    showSubtotalRowAfterGroups: measure.showSubtotalRowAfterGroups,
                                    showTotalRowOnly: measure?.showTotalRowOnly, showTotalAsColumn: measure?.showTotalAsColumn,
                                    type: MeasureTypeEnum.valueOf(measure?.type?.name)))
                        }

                        ReportFieldInfoList dtColumns = new ReportFieldInfoList()
                        columnMeasure.columnList.each {
                            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                            reportFieldInfo.reportField = ReportField.findByName(it.reportFieldName)
                            if (it.sortEnumValue) {
                                reportFieldInfo.sort = Sort.valueOfName(it.sortEnumValue)
                            }
                            dtColumns.addToReportFieldInfoList(reportFieldInfo)
                        }
                        dtColumns.save(failOnError: true)
                        dtColumnMeasure.columnList = dtColumns

                        savedColumnMeasureList.add(dtColumnMeasure)
                    }
                }

                if (it?.customSQLValues) {
                    it?.customSQLValues.each { customSQLValue ->
                        parsedCustomSQLValues.add(new CustomSQLValue(key: customSQLValue.key, value: customSQLValue.value).save())
                    }
                }

                ReportTemplate temp

                if (it.templateType.name == TemplateTypeEnum.CASE_LINE.name()) {
                    LinkedHashMap bindingMap = getBindingMapForLoading(it)

                    bindingMap.putAt("columnList", parsedColumns)
                    bindingMap.putAt("groupingList", parsedGrouping)
                    bindingMap.putAt("rowColumnList", parsedRowCols)

                    bindingMap.putAt("tags", parsedTags)

                    bindingMap.putAt("pageBreakByGroup", it.pageBreakByGroup)
                    bindingMap.putAt("columnShowTotal", it.columnShowTotal)

                    temp = new CaseLineListingTemplate(bindingMap)

                } else if (it.templateType.name == TemplateTypeEnum.DATA_TAB.name()) {
                    LinkedHashMap bindingMap = getBindingMapForLoading(it)

                    bindingMap.putAt("columnList", parsedColumns)
                    bindingMap.putAt("rowList", parsedRows)

                    bindingMap.putAt("tags", parsedTags)

                    bindingMap.putAt("showTotalIntervalCases", it?.showTotalIntervalCases)
                    bindingMap.putAt("showTotalCumulativeCases", it?.showTotalCumulativeCases)

                    temp = new DataTabulationTemplate(bindingMap)
                    savedMeasures.each {
                        temp.addToMeasures(it)
                    }

                    savedColumnMeasureList.each {
                        temp.addToColumnMeasureList(it)
                    }

                } else if (it.templateType.name == TemplateTypeEnum.NON_CASE.name()) {
                    LinkedHashMap bindingMap = getBindingMapForLoading(it)

                    bindingMap.putAt("tags", parsedTags)

                    bindingMap.putAt("nonCaseSql", it?.nonCaseSql)
                    bindingMap.putAt("columnNamesList", it?.columnNamesList)
                    bindingMap.putAt("customSQLValues", parsedCustomSQLValues)

                    temp = new NonCaseSQLTemplate(bindingMap)

                } else {
                    LinkedHashMap bindingMap = getBindingMapForLoading(it)

                    bindingMap.putAt("tags", parsedTags)

                    bindingMap.putAt("customSQLTemplateSelectFrom", it.customSQLTemplateSelectFrom)
                    bindingMap.putAt("customSQLTemplateWhere", it.customSQLTemplateWhere)
                    bindingMap.putAt("columnNamesList", it.columnNamesList)

                    bindingMap.putAt("customSQLValues", parsedCustomSQLValues)

                    temp = new CustomSQLTemplate(bindingMap)

                }

                temp.save(failOnError: true)
                templates.add(temp)
            }

            List success = []
            List failed = []
            templates.each {
                if (!it.hasErrors()) {
                    success.add(it.name)
                } else {
                    log.error("Failed to import $it")
                    failed.add(it.name)
                }
            }
            render "${message(code: "app.load.import.success")} $success \n${message(code: "app.load.import.fail")} $failed"
        } else {
            render message(code: "app.load.import.noData")
        }
    }

    def checkUsage() {
        ReportTemplate template = ReportTemplate.get(params.id)
        List<Configuration> usages = templateService.getUsages(template)
        render(view: "checkUsage", model: [usages: usages, template: template.name])
    }

    private populateModel(ReportTemplate reportTemplateInstance) {

        //todo:  some binding is being done in preValidateTemplate(); move some of that here where possible - morett

        bindData(reportTemplateInstance, params, ["tags"])
        setCommonFields(reportTemplateInstance)
    }

    private getBindingMap(def reportFieldInfo) {
        def bindingMap = [
                reportField            : ReportField.findByName(reportFieldInfo.reportFieldName),
                argusName              : reportFieldInfo.argusName,
                renameValue            : reportFieldInfo.renameValue,
                customExpression       : reportFieldInfo.customExpression,
                datasheet              : reportFieldInfo.datasheet,
                stackId                : reportFieldInfo.stackId ?: -1,
                sortLevel              : reportFieldInfo.sortLevel ?: -1,
                sort                   : reportFieldInfo.sort ? Sort.valueOfName(reportFieldInfo.sort) : null,
                commaSeparatedValue    : reportFieldInfo.commaSeparatedValue ?: false,
                suppressRepeatingValues: reportFieldInfo.suppressRepeatingValues ?: false,
                blindedValue           : reportFieldInfo.blindedValue ?: false
        ]
        bindingMap
    }

    private getMeasureBindingMap(int colMeasIndex, int measIndex) {
        def bindingMap = [
                type             : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-type"),
                name             : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-name"),
                dateRangeCount   : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-dateRangeCount"),
                percentageOption : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-percentageOption"),
                customExpression : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-customExpression"),
                showTotal        : params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-showTotal") ?: false
        ]
        bindingMap
    }

    private ReportTemplate getReportTemplateInstance() {
        ReportTemplate reportTemplateInstance = null
        String templateType = params.templateType
        if (templateType == TemplateTypeEnum.CASE_LINE.name()) {
            reportTemplateInstance = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE)
        } else if (templateType == TemplateTypeEnum.DATA_TAB.name()) {
            reportTemplateInstance = new DataTabulationTemplate(templateType: TemplateTypeEnum.DATA_TAB)
        } else if (templateType == TemplateTypeEnum.CUSTOM_SQL.name()) {
            reportTemplateInstance = new CustomSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL)
        } else if (templateType == TemplateTypeEnum.NON_CASE.name()) {
            reportTemplateInstance = new NonCaseSQLTemplate(templateType: TemplateTypeEnum.NON_CASE)
        }

        reportTemplateInstance
    }

    private void setCommonFields(ReportTemplate template) {
        addTags(template)

        if (template.templateType == TemplateTypeEnum.CASE_LINE) {
            template = (CaseLineListingTemplate) template

            template.columnList = createRFInfoList(params.columns)
            template.groupingList = createRFInfoList(params.grouping)
            template.rowColumnList = createRFInfoList(params.rowCols)

        } else if (template.templateType == TemplateTypeEnum.DATA_TAB) {
            template = (DataTabulationTemplate) template
            template.rowList = createRFInfoList(params.rows)
            template.columnMeasureList = null

            if (params.("validColMeasIndex") != "") {
                List validColMeasIndex = params.("validColMeasIndex").split(',')
                validColMeasIndex.each { index ->
                    int colMeasIndex = index.toInteger()
                    DataTabulationColumnMeasure columnMeasure = new DataTabulationColumnMeasure()

                    // create column list
                    String columns = params.("columns" + colMeasIndex)
                    columnMeasure.columnList = createRFInfoList(columns)

                    List validMeasureIndex = params.("colMeas" + colMeasIndex + "-validMeasureIndex").split(',')
                    validMeasureIndex.each {
                        if (it != "") {
                            int measIndex = it.toInteger()
                            LinkedHashMap bindingMap = getMeasureBindingMap(colMeasIndex, measIndex)
                            DataTabulationMeasure measure = new DataTabulationMeasure(bindingMap)

                            if (measure.dateRangeCount == CountTypeEnum.CUSTOM_PERIOD_COUNT) {
                                // save date range with timezone
                                String preferredTimeZone = userService.getUser().preference.timeZone
                                String dateFrom = params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-customPeriodFrom")
                                String dateTo = params.("colMeas" + colMeasIndex + "-meas" + measIndex + "-customPeriodTo")
                                measure.customPeriodFrom = DateUtil.parseDateWithTimeZone(dateFrom, null,
                                        Constants.DateFormat.WITH_TZ, preferredTimeZone)
                                measure.customPeriodTo = DateUtil.parseDateWithTimeZone(null, dateTo,
                                        Constants.DateFormat.WITH_TZ, preferredTimeZone)
                            }

                            measure.save()
                            columnMeasure.addToMeasures(measure)
                        }
                    }
                    columnMeasure.save()
                    template.addToColumnMeasureList(columnMeasure)
                }
            }
        } else if (template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            template = (CustomSQLTemplate) template

            if (!template.customSQLTemplateWhere) {
                template.customSQLTemplateWhere = ''
            }
        }
    }

    //todo: combine with version in QueryController - morett
    private void addTags(ReportTemplate template) {
        template.tags?.clear()
        if (params.tags) {
            if (params.tags.class == String) {
                params.tags = [params.tags]
            }
            List updatedTags = params.tags

            updatedTags.unique().each {
                Tag tag = Tag.findByName(it)
                if (!tag) {
                    tag = new Tag(name: it, createdBy: userService.getUser(), dateCreated: new Date())
                }

                template.addToTags(tag)
            }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.template'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    def customSQLValuesForTemplate() {
        def result = []
        if (params.templateId) {
            NonCaseSQLTemplate.get(params.templateId)?.customSQLValues?.each {
                result += [value: it.value, key: it.key]
            }
            CustomSQLTemplate.get(params.templateId)?.customSQLValues?.each {
                result += [value: it.value, key: it.key]
            }
        }
        render result as JSON
    }

    private createRFInfoList(String fieldsInfo) {
        ReportFieldInfoList rfList = null
        if (fieldsInfo) {
            rfList = new ReportFieldInfoList()
            List infoList = new JsonSlurper().parseText(fieldsInfo)
            infoList.each {
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(getBindingMap(it))
                rfList.addToReportFieldInfoList(reportFieldInfo)
            }
            rfList.save(failOnError: true)
        }
        return rfList
    }

    //todo: this must be moved to a domain object - morett
    private ReportTemplate preValidateTemplate(ReportTemplate template, def params) {
        if (template.validate(['customSQLTemplateSelectFrom', 'customSQLTemplateWhere', 'nonCaseSql'])) {
            if (template.templateType != TemplateTypeEnum.CASE_LINE || template.templateType != TemplateTypeEnum.DATA_TAB) {

                Sql sql = new Sql(dataSource_pva)

                try {
                    String toValidate

                    if (template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
                        template.customSQLTemplateSelectFrom = params.customSQLTemplateSelectFrom
                        template.customSQLTemplateWhere = params.customSQLTemplateWhere
                        toValidate = "$params.customSQLTemplateSelectFrom where 1=1"
                        if (params.customSQLTemplateWhere) {
                            toValidate += " ${params.customSQLTemplateWhere.trim()}"
                        }

                        template = saveCustomSQLValues(template)

                        if (!template.hasBlanks) {
                            def fieldName = "customSQLTemplateSelectFrom"
                            template = sqlService.testSQLExecution(template, fieldName, toValidate, sql, "com.rxlogix.config.CustomSQLTemplate.sql.incorrect")
                            if (!template.hasErrors()) {
                                template = saveColumnNamesList(template, fieldName, toValidate, sql)
                            }
                        } else {
                            template.columnNamesList = "[]"
                        }
                    } else if (template.templateType == TemplateTypeEnum.NON_CASE) {
                        template.nonCaseSql = params.nonCaseSql
                        toValidate = "$params.nonCaseSql"

                        template = saveCustomSQLValues(template)

                        if (!template.hasBlanks) {
                            def fieldName = "nonCaseSql"
                            template = sqlService.testSQLExecution(template, fieldName, toValidate, sql, "com.rxlogix.config.NonCaseSQLTemplate.sql.incorrect")
                            if (!template.hasErrors()) {
                                template = saveColumnNamesList(template, fieldName, toValidate, sql)
                            }
                        } else {
                            template.columnNamesList = "[]"
                        }
                    }

                } catch (SQLException e) {
                    //todo:   if there's an exception, we need to do something other than log the error.  -morett
                    //todo:   put the SQLException into ValidationException to be sent back to user/screen. - morett
                    log.error("Error while validating query!")
                } finally {
                    sql.close()
                }
            }
        }

        return template
    }

    //todo: there is a near duplicate method in queryService; combine and parameterize - morett
    private ReportTemplate saveCustomSQLValues(ReportTemplate template) {
        template.customSQLValues?.clear()
        String base = null
        if (template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            base = template.customSQLTemplateSelectFrom + " " + template.customSQLTemplateWhere
        } else if (template.templateType == TemplateTypeEnum.NON_CASE) {
            base = template.nonCaseSql
        }

        List<String> keys = base?.findAll(CUSTOM_SQL_VALUE_REGEX_CONSTANT)

        keys?.unique()?.each {
            CustomSQLValue toAdd = new CustomSQLValue(key: it, value: "")
            template.addToCustomSQLValues(toAdd)
        }

        template.hasBlanks = keys?.size() > 0
        return template
    }

    private ReportTemplate saveColumnNamesList(def template, String fieldName, String toValidate, Sql sql) {
        //todo:  this entire method feels like binding, but it's not.  It's binding + more validations.  - morett

        try {
            List<String> columnNamesList = []
            List<String> rejectedColumnNamesList = []

            boolean invalidColumnName = false
            sql.query(toValidate) { ResultSet rs ->
                // Columns are added even if there are no rows in the ResultSet
                ResultSetMetaData rsmd = rs.metaData
                for (int i = 1; i <= rsmd.columnCount; i++) {
                    String currentCol = rsmd.getColumnLabel(i)
                    if (currentCol ==~ COLUMN_LABEL_REGEX_CONSTANT) {
                        columnNamesList.add(currentCol)
                    } else {
                        rejectedColumnNamesList.add(currentCol)
                        invalidColumnName = true
                    }
                }
            }

            if (invalidColumnName) {
                log.info("Rejected columns: $rejectedColumnNamesList")
                template.getErrors().rejectValue(fieldName, "com.rxlogix.config.template.columnNameInvalid")
                return template
            }

            template.columnNamesList = columnNamesList.toListString()
        } catch (SQLException e) {
            log.error("Failed to save the new column list names")
            template.getErrors().rejectValue(fieldName, "com.rxlogix.config.template.columnNameInvalid")
            return template
        }

        return template
    }

    private getBindingMapForLoading(def it) {
        def bindingMap = [
                category             : it.category,
                name                 : it?.name,
                description          : it?.description,
                isPublic             : it.isPublic,
                createdBy            : it.modifiedBy,
                modifiedBy           : it.modifiedBy,
                owner                : it.owner,

                templateType         : TemplateTypeEnum.valueOf(it.templateType.name),
                reassessListedness   : it?.reassessListedness?ReassessListednessEnum.valueOf(it?.reassessListedness?.name):null,
        ]
        bindingMap
    }

    def getGeneratedReports() {
        def configId = params.configId
        String products
        String timeZone = userService.getUser()?.preference?.timeZone
        def executedConfiguration = ExecutedConfiguration.findById(configId)
        if(params.aggExecutionId && params.aggExecutionId!="undefined"){
            executedConfiguration = ExecutedConfiguration.get(params.aggExecutionId)
        }
        String nonSeriousCountType = Holders.config.signal.new.serious.column.header - "New "
        Map countName = [NEW: 'New', CUMM: 'Cum']
        Map countType = [SPONT_FLAG: 'Spon', SERIOUS_FLAG: 'Serious', FATAL_FLAG: 'Fatal', STUDY_FLAG: 'Study', CUMM_FLAG: 'Count', PEDI_FLAG: 'Paed', INTERACTING_FLAG: "Interacting",NON_SERIOUS_FLAG: nonSeriousCountType,GERI_FLAG:'Geria']
        Boolean isSafetyIntegratedAlert = false // Added for PVS-61089
        if(executedConfiguration.selectedDatasource.contains('pva') && (executedConfiguration.selectedDatasource.contains('eudra') || executedConfiguration.selectedDatasource.contains('faers') || executedConfiguration.selectedDatasource.contains('vaers') || executedConfiguration.selectedDatasource.contains('vigibase') || executedConfiguration.selectedDatasource.contains( "evdas" ))){
            isSafetyIntegratedAlert = true
        }

        List dbType = new ArrayList()
        if ( executedConfiguration.selectedDatasource.contains( "pva" ) ) {
            dbType.add( Constants.SystemPrecheck.SAFETY )
        }
        if ( executedConfiguration.selectedDatasource.contains( "faers" ) && !isSafetyIntegratedAlert) {
            dbType.add( Constants.SystemPrecheck.FAERS )
        }
        if ( executedConfiguration.selectedDatasource.contains( "vaers" ) && !isSafetyIntegratedAlert) {
            dbType.add( Constants.SystemPrecheck.VAERS )
        }
        if ( executedConfiguration.selectedDatasource.contains( "vigibase" ) && !isSafetyIntegratedAlert) {
            dbType.add( Constants.SystemPrecheck.VIGIBASE )
        }
        if ( (executedConfiguration.selectedDatasource.contains( "evdas" ) || executedConfiguration.selectedDatasource.contains( "eudra" )) && !isSafetyIntegratedAlert ) {
            dbType.add( Constants.SystemPrecheck.EVDAS )
        }

        Map labelConfig = alertFieldService.getAlertFields( 'AGGREGATE_CASE_ALERT' ).findAll {
            it.dbType in dbType
        }.collectEntries {
            b -> [ b.keyId, b.display ]
        }

        def signalReportList
        if (executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            def alert
            if(executedConfiguration.adhocRun){
                alert = AggregateOnDemandAlert.get(params.alertId)
            } else {
                alert = executedConfiguration.isLatest ? AggregateCaseAlert.get(params.alertId) : ArchivedAggregateCaseAlert.get(params.alertId)
            }
            if(executedConfiguration.adhocRun && executedConfiguration.dataMiningVariable){
                String miningVariable = executedConfiguration.dataMiningVariable
                String productSelection = (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(executedConfiguration) ? getCacheService().getUpperHierarchyProductDictionaryCache(executedConfiguration.id) : getNameFieldFromJson(executedConfiguration.productSelection))?:getGroupNameFieldFromJson(executedConfiguration.productGroupSelection)
                if(productSelection){
                    products = miningVariable + "(" + productSelection + ")"
                } else{
                    products = miningVariable
                }
            } else {
                products = alert.productName
            }

            signalReportList = SignalReport.findAllByExecutedAlertIdAndReportExecutionStatus(executedConfiguration?.id, ReportExecutionStatus.COMPLETED).collect {
                String countString = labelConfig.get( it.typeFlag )?.toString()
                boolean containsSlash = countString?.contains( "/" )
                String[] countTypeArray = countString?.split( "/" )
                [
                        id           : it.id,
                        generatedBy  : User.findByUsername(it.modifiedBy) ? User.findByUsername(it.modifiedBy).fullName : it.modifiedBy,
                        generatedDate: DateUtil.toDateStringWithTime(it.lastUpdated, timeZone),
                        reportName   : it.reportName,
                        productName  : products,
                        eventName    : executedConfiguration.adhocRun ? AggregateOnDemandAlert.get(it.alertId).pt : executedConfiguration.isLatest ? AggregateCaseAlert.get(it.alertId).pt:ArchivedAggregateCaseAlert.get(it.alertId).pt,
                        reportUrl    : reportIntegrationService.getExecutedReportUrl(it?.reportId),
                        countType    :"${ it.typeFlag != null ? (it.type?.equals( "NEW" ) ? (containsSlash ? countTypeArray[ 0 ] : countString) : (containsSlash ? countTypeArray[ 1 ] : countString)) : '' }",
                        isWordFileAvailable : it.wordReport ? true : false
                ]
            }
            if(params.preferredTerm && params.preferredTerm!="undefined"){
                signalReportList = signalReportList.findAll{it.eventName==params.preferredTerm}
            }
            if(params.eventName && params.eventName!="undefined"){
                signalReportList = signalReportList.findAll{it.eventName==params.eventName}
            }
        } else {
            if(executedConfiguration?.productGroupSelection){
                products = executedConfiguration.getGroupNameFieldFromJson(executedConfiguration?.productGroupSelection)
            }else if(executedConfiguration?.productSelection){
                products = executedConfiguration.getProductNameList()?.join(',')
            }
            signalReportList = SignalReport.findAllByExecutedAlertIdAndReportExecutionStatus(executedConfiguration?.id, ReportExecutionStatus.COMPLETED).collect {
                String countString = labelConfig.get( it.typeFlag )?.toString()
                boolean containsSlash = countString?.contains( "/" )
                String[] countTypeArray = countString?.split( "/" )
                [
                        id           : it.id,
                        generatedBy  : User.findByUsername(it.modifiedBy) ? User.findByUsername(it.modifiedBy).fullName : it.modifiedBy,
                        generatedDate: DateUtil.toDateStringWithTime(it.lastUpdated, timeZone),
                        reportName   : it.reportName,
                        productName  : products,
                        eventName    : "",
                        reportUrl    : reportIntegrationService.getExecutedReportUrl(it?.reportId),
                        countType    : params.isAggScreen ? "${ it.typeFlag != null ? (it.type?.equals( "NEW" ) ? (containsSlash ? countTypeArray[ 0 ] : countString) : (containsSlash ? countTypeArray[ 1 ] : countString))  : '' }" : Constants.AlertType.INDIVIDUAL_CASE_SERIES,
                        isWordFileAvailable : it.wordReport ? true : false
                ]
            }

            if(params.preferredTerm && params.preferredTerm!="undefined"){
                signalReportList = signalReportList.findAll{it.eventName==params.preferredTerm}
            }
            if(params.eventName && params.eventName!="undefined"){
                signalReportList = signalReportList.findAll{it.eventName==params.eventName}
            }
        }
        render signalReportList as JSON
    }

    def getNameFieldFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.find {k,v->
                    v.find { it.containsKey('name') || it.containsKey('genericName')}
                }?.value.findAll{
                    it.containsKey('name') || it.containsKey('genericName')
                }.collect {it.name ? it.name : it.genericName }
                prdName = prdVal ? prdVal.sort().join(',') : ""
            }
        }
        prdName
    }

    def parseJsonString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }

    def getGroupNameFieldFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                prdName = jsonObj.collect {
                    it.name.substring(0, it.name.lastIndexOf('(') - 1)
                }.join(",")
            }
        }
        prdName
    }
    def downloadSignalReport() {
        def reportId = params.id
        def reportType = params.outputFormat
        def report = SignalReport.get(reportId)
        def reportBytes = null
        if (Constants.SignalReportOutputType.PDF == reportType) {
            reportBytes = report.pdfReport
        } else if (Constants.SignalReportOutputType.DOCX == reportType) {
            reportBytes = report.wordReport
        } else if (Constants.SignalReportOutputType.XLSX == reportType) {
            reportBytes = report.excelReport
        }
        String moduleName = ExecutedConfiguration.get(report.executedAlertId).isLatest ? "Individual Case Review: Report" : "Individual Case Review: Archived Alert: Report"
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, ExecutedConfiguration.get(report.executedAlertId)?.getInstanceIdentifierForAuditLog(), moduleName, params, report.reportName)
        response.contentType = "${dynamicReportService.getContentType(reportType)}; charset=UTF-8"
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(report.reportName), "UTF-8")}.$reportType" + "\"")
        response.getOutputStream().write(reportBytes)
        response.outputStream.flush()
    }

    def templateList(String term, int page, int max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        Map resp = reportIntegrationService.getTemplateList(term, Math.max(page - 1, 0) * max, max)
        Integer totalCount = resp.totalCount
        render([list: resp?.templateList?.unique()?.collect {
            [id: it.id, text:  it.name + " " + (it?.description ? "(" + it.description + ")" : Constants.Commons.BLANK_STRING) + (it.owner ? " - Owner: " + it.owner : Constants.Commons.BLANK_STRING), name: it.name]
        }, totalCount:totalCount] as JSON)
    }

    def templateIdNameList() {
        Map resp = [:]
        if(params.templateIdList && params.list("templateIdList")) {
            List<String> templateIdList = params.list("templateIdList")
            resp = reportIntegrationService.getTemplateNameIdList(templateIdList)
        }else{
            resp.templateIdNameList = []
        }
        render([templateIdNameList: resp?.templateIdNameList] as JSON)
    }

    String getReportPt(Long alertId){
        String pt
        pt = AggregateCaseAlert.read(alertId).pt
        if(!pt)
            pt = ArchivedAggregateCaseAlert.read(alertId).pt
        return pt
    }
}
