package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.WorkflowRule
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.Alert
import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier

class Group implements Serializable {

    static auditable = ['ignore':['isActive','isDefault','dateCreated','lastUpdated','createdBy','modifiedBy']]
    @AuditEntityIdentifier
    String name
    String description
    Boolean isActive = true
    Boolean forceJustification = true
    Boolean isDefault = false
    String allowedProd

    List<String> allowedProductList = []
    String selectedDatasource

    String toString() { "${name}" }

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    GroupType groupType
    String scimId
    String groupRoles
    String groupUsers

    String justificationText
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    Disposition defaultQualiDisposition
    Disposition defaultQuantDisposition
    Disposition defaultAdhocDisposition
    Disposition defaultEvdasDisposition
    Disposition defaultLitDisposition


    static hasMany = [members: User, workflowRules: WorkflowRule, alertDispositions: Disposition]
    static belongsTo = [User, WorkflowRule, Alert, Configuration]

    static constraints = {
        name blank: false, maxSize: 550, validator: { value, object ->
            return MiscUtil.validator(value, "Group Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        description nullable: true, maxSize: 8000, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
        groupType nullable: false
        createdBy nullable: false, maxSize: 100
        modifiedBy nullable: false, maxSize: 100
        allowedProd nullable: true
        selectedDatasource nullable: true
        forceJustification nullable: true
        isDefault nullable: true
        alertDispositions nullable: true
        autoRouteDisposition nullable: true
        scimId nullable: true
        groupRoles nullable: true
        groupUsers nullable: true

        defaultQualiDisposition nullable: true, validator: { val, obj ->
            def result = true
            if (obj.groupType == GroupType.WORKFLOW_GROUP && !obj.defaultQualiDisposition) {
                result = 'com.rxlogix.signal.group.default.Qualitative.disposition.required'
            }
            return result
        }
        defaultQuantDisposition nullable: true, validator: { val, obj ->
            def result = true
            if (obj.groupType == GroupType.WORKFLOW_GROUP && !obj.defaultQuantDisposition) {
                result = 'com.rxlogix.signal.group.default.Quantitative.disposition.required'
            }
            return result
        }
        defaultAdhocDisposition nullable: true, validator: { val, obj ->
            def result = true
            if (obj.groupType == GroupType.WORKFLOW_GROUP && !obj.defaultAdhocDisposition) {
                result = 'com.rxlogix.signal.group.default.Adhoc.disposition.required'
            }
            return result
        }
        defaultEvdasDisposition nullable: true, validator: { val, obj ->
            def result = true
            if (obj.groupType == GroupType.WORKFLOW_GROUP && !obj.defaultEvdasDisposition) {
                result = 'com.rxlogix.signal.group.default.EVDAS.disposition.required'
            }
            return result
        }
        defaultLitDisposition nullable: true, validator: { val, obj ->
            def result = true
            if (obj.groupType == GroupType.WORKFLOW_GROUP && !obj.defaultLitDisposition) {
                result = 'com.rxlogix.signal.group.default.Literature.disposition.required'
            }
            return result
        }
        justificationText nullable: true, maxSize: 8000, validator: { val, obj ->
            def result = true
            if (obj.autoRouteDisposition && !obj.justificationText) {
                result = 'com.rxlogix.signal.group.justification.required'
            }
            return result
        }
        defaultSignalDisposition nullable: true, validator: { val, obj ->
            def result = true
            if(obj.groupType == GroupType.WORKFLOW_GROUP && !obj.defaultSignalDisposition){
                result = 'com.rxlogix.signal.group.default.signal.disposition.required'
            }
            return result
        }

    }

    static mapping = {
        table "GROUPS"
        members joinTable: [name: 'USER_GROUPS']
        workflowRules joinTable: [name: 'WORKFLOWRULES_GROUPS']
        configurations joinTable: [name: 'CONFIGURATION_GROUPS']
        allowedProd type: 'text', sqlType: 'clob'
        alertDispositions joinTable: [name: "GRP_ALERT_DISP", column: "DISPOSITION_ID", key: "GRP_ALERT_DISP_ID"]
        sort "name"
    }

    static transients = ['allowedProductList', 'allowedLmProductList', 'allowedProductDisplayName']

    def beforeValidate() { syncProductsString() }

    def afterLoad() { syncProductList(this.allowedProd) }

    def setAllowedProductList(productList) {
        this.allowedProductList = productList
        syncProductsString()
    }

    Set<Role> getAuthorities() {
        Set<Role> roles = UserGroupRole.findAllByUserGroup( this ).collect { it.role } as Set
        roles?.sort { a, b -> a.authorityDisplay?.toLowerCase() <=> b.authorityDisplay?.toLowerCase() }
    }

    def getAllowedLmProductList() {
        this.allowedProductList
    }

    def getAllowedProductDisplayName() {
        if (allowedProd) {
            if (allowedProd.size() > 50)
                "${allowedProd.take(50)} ..."
            else
                allowedProd
        } else {
            ''
        }
    }

    private def syncProductList(ids) {
        if (ids) {
            allowedProductList = ids.split(',').collect { it }
        } else {
            allowedProductList = []
        }
    }

    private def syncProductsString() {
        if (allowedProductList) {
            allowedProd = allowedProductList.sort().join(',')
        } else {
            allowedProd = null
        }
    }

    def getModuleNameForMultiUseDomains() {
        this.groupType == GroupType.WORKFLOW_GROUP ? "Workflow Group Management" : "User Group Management"
    }

    static Set<Group> findAllUserGroupByUser(User user) {
        return user.groups
    }

    Map toMap() {
        [
                id      : this.id,
                fullName: this.name,
                name    : this.name
        ]
    }

    Map getAssignedToMap() {
        [
                id  : this.id,
                name: this.name
        ]
    }
}
