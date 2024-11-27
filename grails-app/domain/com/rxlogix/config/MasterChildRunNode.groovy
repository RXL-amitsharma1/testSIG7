package com.rxlogix.config

class MasterChildRunNode {

    Long childExecId
    Long masterExecId
    String nodeName
    String nodeUuid
    Boolean isExecuting=false
    Boolean isResume=false
    Boolean fileGenerated=false
    Boolean isSaveDone=false
    Date dateCreated
    Date lastUpdated
    Long faersId
    Long evdasId
    Long vaersId
    Long vigibaseId
    Long exEvdasId

    static constraints = {
        nodeName nullable: false
        faersId nullable: true
        evdasId nullable: true
        vaersId nullable: true
        vigibaseId nullable: true
        exEvdasId nullable: true
    }
}
