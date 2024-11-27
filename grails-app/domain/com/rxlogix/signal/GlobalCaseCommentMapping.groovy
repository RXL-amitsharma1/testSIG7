package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.util.Holders

import static com.rxlogix.util.DateUtil.toDateTimeString

class GlobalCaseCommentMapping {
    static auditable = true

    String id
    Integer tenantId
    Long caseId
    Integer versionNum
    Integer commentSeqNum
    Integer followUpNum
    String comments
    String module
    String subModule
    String alertName
    Long configId
    Long exConfigId
    String createdBy
    Date dateCreated
    String modifiedBy
    Date lastUpdated
    String eventName
    String productFamily
    String productName
    Long productId
    Long ptCode
    String caseNumber


    static mapping = {
        table("comment_case_g")
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        version false
        comments column: "comment_txt", type: "text", sqlType: "clob"
        exConfigId column: "execution_id"
        dateCreated column: "created_date"
        modifiedBy column: "updated_by"
        lastUpdated column: "updated_date"
        id column: 'ROWID'
    }

    static constraints = {

    }

    def toDto(currentUser, timeZone = "UTC") {
        [
                id                 : this.id,
                configId           : this.configId,
                comments           : this.comments,
                productName        : this.productName,
                eventName          : this.eventName,
                caseNumber         : this.caseNumber,
                productFamily      : this.productFamily,
                alertType          : Constants.AlertConfigType.SINGLE_CASE_ALERT,
                dateCreated        : toDateTimeString(this.dateCreated),
                dateUpdated        : DateUtil.toDateStringWithTime(this.lastUpdated, timeZone),
                createdBy          : User.findByUsername(this.createdBy)?.fullName ?: "-",
                editable           : currentUser == this.createdBy,
                modifiedBy         : User.findByUsername(this.modifiedBy)?.fullName ?: "-",
                exConfigId         : this.exConfigId,
                caseId             : this.caseId,
                versionNum         : this.versionNum,
                syncFlag           : true,
                followUpNum        : this.followUpNum,
                dataSource         : ""
        ]
    }
}
