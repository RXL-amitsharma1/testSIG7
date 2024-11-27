package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil

import java.text.SimpleDateFormat

class LiteratureActivity {
    def userService

    static auditable = false
    Long id

    ActivityType type
    String details
    User performedBy
    Date timestamp
    Boolean display = Boolean.TRUE
    String justification
    Integer articleId
    String productName
    String eventName
    User assignedTo
    Group assignedToGroup
    String searchString
    String guestAttendeeEmail
    String privateUserName

    static mapping = {
        table("LITERATURE_ACTIVITY")
        details column: "DETAILS", sqlType: 'clob'
        justification column: "JUSTIFICATION", length: 9000
        productName column: "PRODUCT_NAME", sqlType: DbUtil.longStringType
        eventName column: "EVENT_NAME", sqlType: DbUtil.longStringType
    }

    static constraints = {
        justification nullable: true, maxSize: 9000
        productName nullable: true
        eventName nullable: true
        assignedTo nullable: true, blank: true
        assignedToGroup nullable: true, blank: true
        searchString nullable: true, blank: true
        guestAttendeeEmail nullable: true, blank: true
        privateUserName nullable: true
    }

    static belongsTo = [performedBy: User,executedConfiguration:ExecutedLiteratureConfiguration]


    def toDto() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
        [
                articleId        : this.articleId,
                activity_id      : id,
                type             : (type.value.toString() == "LiteratureAlertAssociated")? "SignalAdded" : type.value.toString(),
                details          : details,
                performedBy      : performedBy.getFullName()?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM: performedBy.getFullName(),
                timestamp        : sdf.format(new Date(DateUtil.toDateStringWithTime(this.timestamp, userService.getCurrentUserPreference().timeZone))),
                justification    : justification,
                productName      : this.productName,
                eventName        : this.eventName,
                searchString     : this.searchString,
                currentAssignment: assignedTo ? assignedTo?.getFullName() : assignedToGroup ? assignedToGroup?.name : guestAttendeeEmail
        ]
    }

}
