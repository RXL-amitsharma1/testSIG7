package com.rxlogix.dto

import com.rxlogix.enums.SignalChartsEnum

class SignalChartsDTO {

    List<String> dateRange = []
    Integer groupingCode
    String caseList
    String productSelection
    String productGroupSelection
    String eventSelection
    String eventGroupSelection
    Long signalId
    SignalChartsEnum chartType
    Map assessmentDetail
    Boolean isMultiIngredient
}
