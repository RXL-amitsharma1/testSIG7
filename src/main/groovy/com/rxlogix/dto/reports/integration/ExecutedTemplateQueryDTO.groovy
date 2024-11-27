package com.rxlogix.dto.reports.integration

import com.rxlogix.config.*
import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.ParameterValueDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.QueryLevelEnum

class ExecutedTemplateQueryDTO {
    Long templateId
    Long queryId

    ExecutedDateRangeInfoDTO executedTemplateQueryDateRangeInfoDTO

    List templateValueLists = []
    List<QueryValueListDTO> executedQueryValueListDTOList = []

    String header = ''
    String title = ''
    String footer = ''

    boolean headerProductSelection = false
    boolean headerDateRange = false
    boolean blindProtected = false // Used for CIOMS I Template.
    boolean privacyProtected = false // Used for CIOMS I Template.

    QueryLevelEnum queryLevel = QueryLevelEnum.CASE

    ExecutedTemplateQueryDTO(ExecutedTemplateQuery exTemplateQuery, ExecutedAlertDateRangeInformation exAlertDateRangeInfo,boolean isMissedCase, Date prevExecStartDate, Long alertCaseSeriesId) {
        templateId = exTemplateQuery.executedTemplate
        queryId = exTemplateQuery.executedQuery
        header = exTemplateQuery.header?:exTemplateQuery.executedTemplateName
        title = exTemplateQuery.title ?: exTemplateQuery.executedConfiguration.name + ": " + exTemplateQuery.executedTemplateName
        footer = exTemplateQuery.footer
        headerProductSelection = exTemplateQuery.headerProductSelection
        headerDateRange = exTemplateQuery.headerDateRange
        blindProtected = exTemplateQuery.blindProtected
        privacyProtected = exTemplateQuery.privacyProtected
        executedTemplateQueryDateRangeInfoDTO = new ExecutedDateRangeInfoDTO(exTemplateQuery.executedDateRangeInformationForTemplateQuery, exAlertDateRangeInfo,isMissedCase,prevExecStartDate, alertCaseSeriesId)
        executedQueryValueListDTOList = populateQueryValueListDTO(exTemplateQuery.executedQueryValueLists)
        queryLevel = exTemplateQuery.queryLevel
    }

    ExecutedTemplateQueryDTO(Long template, String reportName, ExecutedConfiguration ec, String type = "") {
        templateId = template
        this.title = reportName ?: ec.name
        header = reportName?.split(':')[1]?.trim() ?: ec.name
        if (type == "CUM") {
            executedTemplateQueryDateRangeInfoDTO = new ExecutedDateRangeInfoDTO()
            executedTemplateQueryDateRangeInfoDTO.setCummulativeDateRangeInfoDTO(ec.executedAlertDateRangeInformation)
        } else {
            executedTemplateQueryDateRangeInfoDTO = new ExecutedDateRangeInfoDTO(ec.executedAlertDateRangeInformation)
        }
    }

    List<QueryValueListDTO> populateQueryValueListDTO(Set<ExecutedQueryValueList> executedQueryValueLists) {
        List<QueryValueListDTO> queryValueListDTOList = []
        List<ParameterValueDTO> parameterValueDTOList = []
        executedQueryValueLists.each { QueryValueList queryValueList ->
            parameterValueDTOList = []
            queryValueList.parameterValues.each { ParameterValue parameterValue ->
                if (parameterValue.hasProperty('reportField')) {
                    parameterValueDTOList << new ParameterValueDTO(key: parameterValue.key,
                            reportFieldName: parameterValue.reportField.name, operator: parameterValue.operator, value: parameterValue.value)
                } else {
                    parameterValueDTOList << new ParameterValueDTO(key: parameterValue.key, value: parameterValue.value)
                }
            }
            queryValueListDTOList << new QueryValueListDTO(queryId: queryValueList.query, parameterValues: parameterValueDTOList)
        }
        queryValueListDTOList
    }
}
