package com.rxlogix.signal

import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON

class UserViewAssignment {
    static auditable=false

    String hierarchy
    Long hierarchyKeyId
    Long workflowGroup

    Date dateCreated
    Date lastUpdated

    Long userAssigned
    Long groupAssigned
    String productClob
    String products

    Character isResolved
    Long tenantId

    static constraints = {
        hierarchy nullable: false
        hierarchyKeyId nullable: false
        workflowGroup nullable: true
        userAssigned nullable: true
        groupAssigned nullable: true
        products nullable: false
        isResolved nullable: false
        tenantId nullable: false
    }
    static mapping = {
        datasource "pva"
        version false
        id generator:'sequence', params:[sequence:'USER_VIEW_ASSIGNMENT_SEQ']
        products  sqlType: 'clob'
        productClob formula: 'dbms_lob.substr(PRODUCTS, dbms_lob.getlength(PRODUCTS))'
    }

    String getProductsClob(){
        if(this.products.length() == 8000) {
            return this.productClob
        }
        return this.products
    }
    Map toExportDto(Map groupsMap, Map usersMap, String timeZone) {
        String dateCreated = DateUtil.StringFromDate(this.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone)
        String lastUpdated = DateUtil.StringFromDate(this.lastUpdated,DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)
        Map map = [
                product      : (JSON.parse(getProductsClob()).name).join(", "),
                hierarchy    : this.hierarchy,
                assignments  : this.userAssigned ? (usersMap.get(this.userAssigned)?.fullName ?: "") : groupsMap.get(this.groupAssigned)?.name,
                userId       : this.userAssigned ? usersMap.get(this.userAssigned)?.username : "",
                workflowGroup: this.workflowGroup ? groupsMap.get(this.workflowGroup)?.name : "",
                dateCreated  : dateCreated,
                lastUpdated  : lastUpdated,
        ]
        map
    }
}
