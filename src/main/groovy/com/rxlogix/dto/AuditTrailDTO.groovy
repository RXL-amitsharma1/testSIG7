package com.rxlogix.dto

class AuditTrailDTO {

    String username
    String fullname
    String entityName
    String moduleName
    String description
    String entityId
    String applicationName

    String category
    String entityValue
    String transactionId
    Boolean sectionChildModule = false
    List<AuditTrailChildDTO> auditTrailChildDTOS = []

     void setAuditTrailChildDTOList(AuditTrailChildDTO auditTrailChildDTO){
        if(this.auditTrailChildDTOS)
            this.auditTrailChildDTOS.add(auditTrailChildDTO)
        else{
            this.auditTrailChildDTOS = [auditTrailChildDTO]

        }

    }
    Map toMap(){
        ["username":this.username,"fullname":this.fullname,"entityName":this.entityName,"moduleName":this.moduleName,
        "description":this.description,"entityId":this.entityId,"applicationName":this.applicationName,"category":this.category,
        "entityValue":this.entityValue,"transactionId":this.transactionId,"sectionChildModule":this.sectionChildModule,"auditTrailChildDTOS":this.auditTrailChildDTOS]
    }


}
