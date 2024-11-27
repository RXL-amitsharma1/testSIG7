package com.rxlogix.dto.reports.integration

import com.rxlogix.Constants
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.DrugTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.dto.ExecutedGlobalDateRangeInformation
import com.rxlogix.Constants
import com.rxlogix.enums.DrugTypeEnum
import com.rxlogix.signal.ProductTypeConfiguration

class ExecutedConfigurationDTO {
    String reportName
    String description
    String productSelection
    String productGroupSelection
    String studySelection
    String eventSelection
    String eventGroupSelection
    String ownerName
    String dateRangeType
    Date asOfVersionDate
    EvaluateCaseDateEnum evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean excludeNonValidCases = true
    boolean includeMedicallyConfirmedCases = false
    boolean limitPrimaryPath = false
    boolean suspectProduct = true
    Long pvrCumulativeCaseSeriesId
    Long pvrCaseSeriesId
    List<ExecutedTemplateQueryDTO> executedTemplateQueryDTOList = []
    List<String> sharedWithUsers = []
    List<String> sharedWithGroups = []
    String callbackURL
    ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false

    ExecutedConfigurationDTO(ExecutedConfiguration ec, Long templateId = 0, Long tempCaseSeriesId = 0, String reportName = '', Date prevExecStartDate = null, String type = "") {
        // TODO: Remove these where isChanged has been used after 5.2 release
        DateRangeTypeCaseEnum prevDateRangeType = ec.dateRangeType
        DateRangeEnum prevDateRangeEnum = ec.executedAlertDateRangeInformation.dateRangeEnum
        boolean isChanged = false
        if(ec.dateRangeType == DateRangeTypeCaseEnum.EVENT_RECEIPT_DATE){
            isChanged = true
            ec.dateRangeType = DateRangeTypeCaseEnum.EVENT_RECEIPT_DATE
        }
        this.reportName = reportName ?: ec.name
        description = ''
        productGroupSelection = ec.productGroupSelection
        productSelection = ec.productSelection
        studySelection = ec.studySelection
        eventSelection = ec.eventSelection
        eventGroupSelection = ec.eventGroupSelection
        ownerName = ec.owner.username
        dateRangeType = ec.dateRangeType.value()
        evaluateDateAs = ec.evaluateDateAs
        asOfVersionDate = ec.asOfVersionDate
        excludeFollowUp = ec.excludeFollowUp
        excludeNonValidCases = false
        includeLockedVersion = ec.includeLockedVersion
        suspectProduct = ec.suspectProduct
        includeMedicallyConfirmedCases = ec.includeMedicallyConfirmedCases
        limitPrimaryPath = ec.limitPrimaryPath
        executedTemplateQueryDTOList = reportName ? [new ExecutedTemplateQueryDTO(templateId, reportName, ec, type)] : populateTemplateQueries(ec, prevExecStartDate)
        pvrCumulativeCaseSeriesId = !reportName ? tempCaseSeriesId : 0
        pvrCaseSeriesId = reportName ? tempCaseSeriesId : ec.pvrCaseSeriesId
        if(isChanged){
            ec.dateRangeType = prevDateRangeType
            ec.executedAlertDateRangeInformation.dateRangeEnum = prevDateRangeEnum
        }
        List roleType = []
        List drugTypes = ec?.drugType?.split(',')
        for(String drugTypeId in drugTypes){
            try{
                ProductTypeConfiguration productTypeConfiguration = ProductTypeConfiguration.get(drugTypeId as Long)
                if (productTypeConfiguration)
                    roleType += productTypeConfiguration.roleType
            } catch(Exception ex) {
                println "Error occurred in fetching ProductTypeConfigurations from DB: ${ex.getMessage()}"
            }
        }
        if(ec.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && roleType?.unique() != ['Suspect']){
            suspectProduct = false
        }
    }

    List<ExecutedTemplateQueryDTO> populateTemplateQueries(ExecutedConfiguration ec,Date prevExecStartDate) {
        List<ExecutedTemplateQueryDTO> executedTemplateQueryDTOList = []
        ec.executedTemplateQueries.sort {[it.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute, it.id]}.each { ExecutedTemplateQuery executedTemplateQuery ->
            ExecutedTemplateQueryDTO templateQueryDTO = new ExecutedTemplateQueryDTO(executedTemplateQuery, ec.executedAlertDateRangeInformation,ec.missedCases,prevExecStartDate, ec.alertCaseSeriesId)
            executedTemplateQueryDTOList.add(templateQueryDTO)
        }
        executedTemplateQueryDTOList
    }
}
