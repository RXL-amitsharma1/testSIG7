package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.CaseColumnJoinMapping
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.sql.SQLException

@Transactional
class TemplateService {
    def userService
    def customMessageService
    def seedDataService

    String chartDefaultOptions
    def copyTemplate(ReportTemplate originalTemplate, User owner) {
        ReportTemplate newTemplate
        if (originalTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
            newTemplate = new CaseLineListingTemplate(originalTemplate.properties)
        } else if (originalTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            newTemplate = new DataTabulationTemplate(originalTemplate.properties)
        } else if (originalTemplate.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            newTemplate = new CustomSQLTemplate(originalTemplate.properties)
            newTemplate.customSQLValues = []
            originalTemplate.customSQLValues.each {
                newTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        } else if (originalTemplate.templateType == TemplateTypeEnum.NON_CASE) {
            newTemplate = new NonCaseSQLTemplate(originalTemplate.properties)
            newTemplate.customSQLValues = []
            originalTemplate.customSQLValues.each {
                newTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        }

        newTemplate.name = generateUniqueName(originalTemplate)
        newTemplate.createdBy = owner.username
        newTemplate.isPublic = false
        newTemplate.save()
        return newTemplate
    }

    String generateUniqueName(ReportTemplate template) {
        String newName = "Copy of $template.name"

        if (ReportTemplate.findByNameAndOwnerAndIsDeleted(newName, template.owner, false)) {
            int count = 1
            newName = "Copy of $template.name ($count)"
            while (ReportTemplate.findByNameAndOwnerAndIsDeleted(newName, template.owner, false)) {
                newName = "Copy of $template.name (${count++})"
            }
        }

        return newName
    }

    def deleteTemplate(ReportTemplate template) {
        template.isDeleted = true
        template.save()
        return template
    }


    //todo:  this is not used/never called - morett
    boolean validateCustomExpression(String customExpression, Sql sql) {
        log.info(customExpression)
        try {
            sql.execute(customExpression + " limit 1")
            log.info("Successfully validated custom expressions")
            return true
        } catch (SQLException e) {
            log.info("Failed to validate custom expressions")
            return false
        }
    }

    // TODO: Validate syntax of custom expression
    //todo:  this is not used/never called - morett
    String generateSQLForCustomExpression(ReportTemplate template) {
        def selectedFields = template.getAllSelectedFieldsInfo().reportField

        // get custom expression list
//        JSONObject customExpressionObject = template.getCustomExpressions()
//        def customExpressionSequence = customExpressionObject ? customExpressionObject.keySet() : []
        def customExpressionSequence = template.getAllSelectedFieldsInfo().customExpression

        // from clause creation
        def tableName
        def tempTableNames = []
        String caseTableFromClause = ""

        //find table list
        selectedFields.each { ReportField rf ->
            if (tableName != rf.sourceColumn.tableName.tableName) {
                tempTableNames.add(rf.sourceColumn.tableName.tableName)
                tableName = rf.sourceColumn.tableName.tableName
            }
        }
        // construct SQL after finding relation between case tables
        def Integer loopCounter = 0
        boolean recursiveFlag = true

        while (recursiveFlag && loopCounter < 5) {
            def relTableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def relCaseTableRelation = relTableJoinMapping.list {
                inList("tableName.tableName", tempTableNames)
                order("mapTableName.tableName", "asc")
            }
            recursiveFlag = false
            relCaseTableRelation.each { CaseColumnJoinMapping rf ->

                if (tableName == rf.mapTableName.tableName) {
                    if (!tempTableNames.contains(rf.mapTableName.tableName)) {
                        tempTableNames.add(rf.mapTableName.tableName)
                        recursiveFlag = true
                    }
                }
                tableName = rf.mapTableName.tableName

            }
            loopCounter++
        }

        loopCounter = 0

        // sort tables in join order
        def tableJoinOrder = SourceTableMaster.createCriteria()
        def tableList = tableJoinOrder.list {
            inList("tableName", tempTableNames)
            order("caseJoinOrder", "asc")
        }
        tableList.each { SourceTableMaster tabRec ->
            def tableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def caseTableRelation = tableJoinMapping.list {
                inList("mapTableName.tableName", tempTableNames)
                eq("tableName.tableName", tabRec.tableName)
                order("mapColumnName", "asc")
            }
            if (loopCounter > 0) {
                caseTableFromClause += (tabRec.caseJoinType == "O" ? " Left " : "") + " join "
            }
            caseTableFromClause += tabRec.tableName + " " + tabRec.tableAlias
            if (caseTableRelation.size() > 0) {
                caseTableFromClause += " on ("
            }
            def int iterations = 0
            caseTableRelation.each { CaseColumnJoinMapping rf ->
                if (iterations > 0) {
                    caseTableFromClause += " AND "
                }
                iterations++
                caseTableFromClause += rf.mapTableName.tableAlias + "." + rf.mapColumnName + " = " + rf.tableName.tableAlias + "." + rf.columnName
                if (rf.tableName.versionedData == "V" && rf.mapTableName.versionedData == "V") {
                    caseTableFromClause += " and " + rf.mapTableName.tableAlias + ".version_num = " + rf.tableName.tableAlias + ".version_num"
                }
            }
            if (caseTableRelation.size() > 0) {
                caseTableFromClause += " ) "
            }
            loopCounter++
        }

        def tempString
        loopCounter = 0
        String lmTableAlias = ""
        String lmTableJoins = ""
        String selectClause = ""

        selectedFields.each { ReportField rf ->
            if (rf?.sourceColumn?.lmDecodeColumn) {
                if (rf?.sourceColumn?.lmTableName?.tableName && rf?.sourceColumn?.tableName?.tableAlias) {
                    lmTableAlias = rf.sourceColumn.tableName.tableAlias + loopCounter // unique alias for LM table
                    lmTableJoins += (rf.sourceColumn.lmJoinType == "O" ? " Left " : "") + " Join " + rf.sourceColumn.lmTableName.tableName + " "
                    lmTableJoins += lmTableAlias + " on (" + rf.sourceColumn.tableName.tableAlias + "."
                    lmTableJoins += rf.sourceColumn.columnName + " = " + lmTableAlias + "." + rf.sourceColumn.lmJoinColumn + " ) "

                    tempString = lmTableAlias + "." + rf.sourceColumn.lmDecodeColumn
                }
            } else {
                tempString = rf.sourceColumn.tableName.tableAlias + "." + rf.sourceColumn.columnName
            }
            if (customExpressionSequence.contains(loopCounter)) {
                selectClause += customExpressionObject[loopCounter] + ","
            } else {
                selectClause += tempString + " AS " + rf.sourceColumn.columnName + loopCounter + ","
            }
            loopCounter++
        }
        String SQL = "select ${selectClause.getAt(0..selectClause.length() - 2)} from ${caseTableFromClause} $lmTableJoins"
        return SQL
    }



    List<ReportTemplate> getTemplateList() {
        def templatesList = []
        User currentUser = userService.getUser()

        if (currentUser.isAdmin()) {
            templatesList += CaseLineListingTemplate.findAllByIsDeletedAndOriginalTemplateId(false, 0) + DataTabulationTemplate.findAllByIsDeletedAndOriginalTemplateId(false, 0) + CustomSQLTemplate.findAllByIsDeletedAndOriginalTemplateId(false, 0) + NonCaseSQLTemplate.findAllByIsDeletedAndOriginalTemplateId(false, 0)
        } else {
            templatesList += ReportTemplate.where {
                (isPublic == true || createdBy == currentUser.username) && isDeleted == false && originalTemplateId == 0
            }
        }

        return templatesList.flatten().sort()
    }

    List getReportFieldsForCLL() {
        def fieldGroups = ReportFieldGroup.getAll()
        List fieldSelection = []
        fieldGroups.each {
            fieldSelection.add(text: it.name, children: ReportField.findAllByFieldGroupAndTemplateCLLSelectable(it, true))
        }
        return fieldSelection
    }

    List getReportFieldsForDTRow() {
        def fieldGroups = ReportFieldGroup.getAll()
        List fieldSelection = []
        fieldGroups.each {
            fieldSelection.add(text: it.name, children: ReportField.findAllByFieldGroupAndTemplateDTRowSelectable(it, true))
        }
        return fieldSelection
    }

    List getReportFieldsForDTColumn() {
        def fieldGroups = ReportFieldGroup.getAll()
        List fieldSelection = []
        fieldGroups.each {
            fieldSelection.add(text: it.name, children: ReportField.findAllByFieldGroupAndTemplateDTColumnSelectable(it, true))
        }
        return fieldSelection
    }

    @Transactional(readOnly = true)
    def createMeasureListFromJson(DataTabulationTemplate template, String measureJSON) {
        def measures = null
        if (measureJSON) {
            measures = []
            JSON.parse(measureJSON).each {
                DataTabulationMeasure measure = new DataTabulationMeasure(name: it.name)
                measure.type = MeasureTypeEnum.valueOf(it.type)
                measure.dateRangeCount = CountTypeEnum.valueOf(it.count)
                measure.percentageOption = PercentageOptionEnum.valueOf(it.percentage)
                if (measure.dateRangeCount == CountTypeEnum.CUSTOM_PERIOD_COUNT) {
                    // save date range with timezone
                    String preferredTimeZone = userService.getUser().preference.timeZone
                    measure.customPeriodFrom = DateUtil.parseDateWithTimeZone(it.customPeriodFrom, null,
                            Constants.DateFormat.WITH_TZ, preferredTimeZone)
                    measure.customPeriodTo = DateUtil.parseDateWithTimeZone(null, it.customPeriodTo,
                            Constants.DateFormat.WITH_TZ, preferredTimeZone)
                }
                measure.customExpression = it.customExpression
                measure.showTotal = it.showTotal
                measures.add(measure)
            }
        }
        return measures
    }

    // For data tabulation rows
    List<String> getAllSelectedFieldNames(ReportTemplate reportTemplate) {
        List<String> rowNames = getSelectedFieldNames(reportTemplate.rowList.reportFieldInfoList)
        return rowNames // Don't show column names for now.
    }

    private List<String> getSelectedFieldNames(List<ReportFieldInfo> reportFieldInfoList) {
        List<String> names = []

        reportFieldInfoList.each {
            if (it.renameValue) {
                names.add(it.renameValue)
            } else {
                names.add(customMessageService.getMessage("app.reportField." + it.reportField.name))
            }
        }
        return names
    }

    //TODO: Need to check the usage of this method and refactored the code -PS
    @Transactional(readOnly = true)
    List<Configuration> getUsages(ReportTemplate template) {
        return []
    }

    // For view page; dev user only
    JSON getTemplateAsJSON(ReportTemplate reportTemplate) {
        HashMap templateMap = new HashMap(reportTemplate.properties)
        templateMap.remove("templateService")
        templateMap.remove("auditLogService")

        if (reportTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
            templateMap.columnList = reportTemplate.columnList.reportFieldInfoList.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.groupingList = reportTemplate.groupingList?.reportFieldInfoList?.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.rowColumnList = reportTemplate.rowColumnList?.reportFieldInfoList?.collect {
                return getRFIPropertiesMap(it)
            }
        } else if (reportTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            templateMap.rowList = reportTemplate.rowList.reportFieldInfoList.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.columnMeasureList = reportTemplate.columnMeasureList.collect {
                return getColMeasMap(it)
            }
        }

        return templateMap as JSON
    }

    HashMap getRFIPropertiesMap(ReportFieldInfo reportFieldInfo) {
        HashMap rfiListMap = new HashMap(reportFieldInfo.properties)
        rfiListMap.reportFieldName = reportFieldInfo.reportField.name
        rfiListMap.remove("reportFieldInfoList")
        rfiListMap.sortEnumValue = reportFieldInfo.sort?.value()
        rfiListMap.remove("sort")
        return rfiListMap
    }

    HashMap getColMeasMap(DataTabulationColumnMeasure columnMeasure) {
        HashMap columnMeasureMap = new HashMap(columnMeasure.properties)

        columnMeasureMap.columnList = columnMeasure.columnList?.reportFieldInfoList?.collect {
            return getRFIPropertiesMap(it)
        }

        return columnMeasureMap
    }

    // For gsp
    String getJSONStringRF(ReportFieldInfoList list) {
        JSONArray JSONColumns = new JSONArray()
        if (list) {
            list.reportFieldInfoList.each {
                ReportField rf = it.reportField

                JSONObject column = new JSONObject([id: rf.id, stackId: it.stackId, renameValue: it.renameValue,
                                                    text: customMessageService.getMessage("app.reportField." + rf.name),
                                                    customExpression: it.customExpression, datasheet: it.datasheet,
                                                    sortLevel: it.sortLevel, sort: it.sort?.value(), argusName: it.argusName,
                                                    commaSeparatedValue: it.commaSeparatedValue,
                                                    suppressRepeatingValues: it.suppressRepeatingValues,
                                                    blindedValue: it.blindedValue, reportFieldName: it.reportField.name])
                JSONColumns.add(column)
            }
        }
        return JSONColumns.toString()
    }

    def getChartDefaultOptions() {
        if (!chartDefaultOptions) {
            chartDefaultOptions = seedDataService.getInputStreamForMetadata("chart_default_options.json").text
        }
        return chartDefaultOptions
    }
}
