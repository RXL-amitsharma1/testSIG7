package com.rxlogix.config

import com.rxlogix.util.DbUtil

class MasterConfigStatus {

    Long masterExecId
    Boolean dssFlag=false
    Boolean ebgmFlag=false
    Boolean prrFlag=false
    Boolean isMiningDone=true
    Boolean isCountDone=false
    Boolean isEbgmDone=false
    Boolean isPrrDone=false
    Boolean isDssDone=false
    Boolean dssToRun=false
    Boolean dssExecuting=false
    Boolean dataPersisted=false
    Boolean allDbDone=false
    Boolean isDbError=false
    Boolean errorMsg=false
    String dataSource
    String nodeName
    String nodeUuid
    Long integratedExecId
    Boolean isEventGroup=false
    Date dateCreated
    Date lastUpdated
    String faersExIds
    Long faersMasterExId
    String evdasExIds
    Long evdasMasterExId
    Boolean allChildDone=false
    String vaersExIds
    Long vaersMasterExId
    String vigibaseExIds
    Long vigibaseMasterExId


    static mapping = {
        faersExIds sqlType: DbUtil.longStringType
        evdasExIds sqlType: DbUtil.longStringType
        vaersExIds sqlType: DbUtil.longStringType
        vigibaseExIds sqlType: DbUtil.longStringType
    }



    static constraints = {
        dataSource nullable: true
        integratedExecId nullable: true
        faersExIds nullable: true
        faersMasterExId nullable: true
        evdasExIds nullable: true
        evdasMasterExId nullable: true
        allChildDone nullable: true
        vaersExIds nullable: true
        vaersMasterExId nullable: true
        vigibaseExIds nullable: true
        vigibaseMasterExId nullable: true
    }
}
