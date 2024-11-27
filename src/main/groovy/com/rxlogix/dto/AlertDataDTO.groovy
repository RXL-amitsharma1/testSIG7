package com.rxlogix.dto

class AlertDataDTO {

    List<String> allowedProductsToUser
    List<String> dispositionFilters
    List<String> visibleColumnsList = []
    Set<String> uniqueDispositions = []
    List<Long> groupIdList = []
    List<Long> execConfigIdList = []
    def domainName
    Map params
    Long userId
    Long execConfigId
    Long configId
    Map filterMap
    Map orderColumnMap
    String timeZone
    Boolean cumulative
    Boolean isFromExport
    def executedConfiguration
    Boolean isFullCaseList
    Integer length
    Integer start
    Boolean isProjection
    Long workflowGroupId
    List<String> clipBoardCases
    Boolean isCaseFormProjection
    Boolean isFaers
    List advancedFilterDispositions = []
    List advancedFilterDispName = []
    Boolean isVaers
    Long masterExConfigId
    Boolean isVigibase
    Boolean isJader
    Boolean exportCaseNarrative
    Map analysisStatusJson
}
