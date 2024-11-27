package com.rxlogix.config

class MasterExecutedConfiguration {

    String name
    String productHierarchy
    Integer lastX
    String dateRangeType
    String asOfVersionDate
    Date startDate
    Date endDate
    String scheduler
    String configTemplate
    Boolean isEnabled
    Boolean executing
    Long masterConfigId
    Date dateCreated
    Date lastUpdated
    String datasource
    Long integratedExId
    Integer runCount = 0

    static constraints = {
        //isResume nullable: true
        masterConfigId nullable: true
        executing nullable: true
        isEnabled nullable: true
        configTemplate nullable: true
        scheduler nullable: true
        startDate nullable: true
        endDate nullable: true
        asOfVersionDate nullable: true
        dateRangeType nullable: true
        lastX nullable: true
        productHierarchy nullable: true
        datasource nullable: true
        integratedExId nullable: true
        runCount nullable: true
    }

    static mapping = {
        id generator:'sequence', params:[sequence:'exec_config_sequence']
    }
}
