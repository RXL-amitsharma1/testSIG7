package com.rxlogix.dto.caseSeries.integration

import com.rxlogix.enums.EvaluateCaseDateEnum

class ExecutedCaseSeriesDTO {
    String seriesName
    String description
    String dateRangeType
    Date asOfVersionDate
    EvaluateCaseDateEnum evaluateDateAs
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean includeAllStudyDrugsCases = false
    boolean excludeNonValidCases = true
    boolean isTemporary = false
    boolean suspectProduct = false
    String productSelection
    String studySelection
    String eventSelection
    String ownerName
    Long globalQueryId
    ExecutedDateRangeInfoDTO executedCaseSeriesDateRangeInformation
    List<QueryValueListDTO> executedGlobalQueryValueLists
    String callbackURL
    List<String> sharedWithUsers
    List<String> sharedWithGroups
    Boolean isMultiIngredient = false
    Boolean includeWHODrugs = false
}
