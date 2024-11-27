package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import groovy.transform.ToString
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat

import static com.rxlogix.util.DateUtil.toDateString

@ToString(includes=['productName', 'eventName', 'disposition', 'priority', 'assignedTo'])
class ProductEventHistory implements GroovyInterceptable, Serializable {
    def userService

    //Workflow management related params.
    Disposition disposition
    User assignedTo
    Group assignedToGroup
    Priority priority

    //Statistics related params.
    String prrValue
    String rorValue

    Double ebgm
    Double eb05
    Double eb95

    Integer newStudyCount
    Integer cumStudyCount
    Integer newSponCount
    Integer cumSponCount
    Integer newSeriousCount
    Integer cumSeriousCount
    Integer newFatalCount
    Integer cumFatalCount
    String positiveRechallenge

    String change
    String productName
    String eventName
    Integer newCount
    BigInteger productId
    Long eventId

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    String tagName
    String subTagName
    String tagsUpdated

    boolean isLatest
    Date asOfDate
    Date executionDate
    String justification

    Long configId
    Long execConfigId
    Long aggCaseAlertId
    Long archivedAggCaseAlertId
    Date dueDate
    Boolean isUndo =false
    Date createdTimestamp

    static constraints = {
        justification nullable: true , maxSize: 9000
        createdBy nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        modifiedBy nullable: true
        executionDate nullable: true
        change nullable: true
        newCount nullable: true
        configId nullable: true
        execConfigId nullable: true
        aggCaseAlertId nullable: true
        archivedAggCaseAlertId nullable: true
        tagName nullable: true, maxSize: 4000
        assignedTo nullable: true
        assignedToGroup nullable: true
        newStudyCount nullable: true
        cumStudyCount nullable: true
        newSponCount nullable: true
        cumSponCount nullable: true
        newSeriousCount nullable: true
        cumSeriousCount nullable: true
        newFatalCount nullable: true
        cumFatalCount nullable: true
        positiveRechallenge nullable: true
        subTagName nullable: true, maxSize: 4000
        tagsUpdated nullable: true, maxSize: 4000
        dueDate nullable: true
        productId nullable: true
        eventId nullable: true
        isUndo nullable:true
        createdTimestamp nullable: true
    }

    static mapping = {
        priority lazy: false
        disposition lazy: false
        assignedTo lazy: false
        assignedToGroup lazy: false
        justification column: "JUSTIFICATION", length: 9000
    }

    static transients = ['alertName']

    def toDto(String userTimezone = "UTC", List configIds = []) {
        boolean isAccessibleToCurrentUser = true
        if(configIds && !(this.configId in configIds)){
            isAccessibleToCurrentUser = false
        }
        Date timeStamp = null
        if (Objects.nonNull(this.createdTimestamp) && StringUtils.isBlank(this.tagName)) {
            timeStamp = this.createdTimestamp
        } else {
            timeStamp = this.dateCreated
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)

        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(this.execConfigId)
        [
                alertConfigId        : this.configId,
                executedAlertConfigId: this.execConfigId,
                alertName            : alertName,
                alertTags            : fetchTags(this.tagName),
                alertSubTags         : fetchTags(this.subTagName),
                id                   : this.id,
                disposition          : this.disposition?.displayName,
                priority             : [value: this.priority?.value, iconClass: this.priority?.iconClass],
                assignedTo           : this.assignedTo ? this.assignedTo?.fullName : this.assignedToGroup?.name,
                createdBy            : this.createdBy?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:this.createdBy,
                updatedBy            : this.modifiedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:this.modifiedBy,
                ebgm                 : this.ebgm,
                eb05                 : this.eb05,
                eb95                 : this.eb95,
                rorValue             : this.rorValue,
                prrValue             : this.prrValue,
                newCount             : this.newCount,
                isLatest             : this.isLatest,
                asOfDate             : DateUtil.toDateStringWithTime(this.asOfDate, userTimezone),
                productName          : this.productName,
                eventName            : this.eventName,
                executionDate        : DateUtil.toDateStringWithTime(this.executionDate, userTimezone),
                timestamp            : sdf.format(new Date(DateUtil.toDateStringWithTime(timeStamp, userTimezone))).toString(),
                justification        : this.justification,
                change               : this.change ?: "",
                reviewPeriod         : DateUtil.toDateString(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeStartAbsolute) +
                        " - " + DateUtil.toDateString(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeEndAbsolute),
                isAccessibleToCurrentUser: isAccessibleToCurrentUser,
                isArchivedAlert : !executedConfiguration?.isLatest
        ]
    }

    def getAlertName(){
        Configuration.get(this.configId)?.name
    }

    String fetchTags(String tags){
        if(tags) {
            List allTagNames = JSON.parse(tags)
            String currentUsername = userService.getUser().username
            List tagNames = allTagNames.findAll {
                (it.privateUser == null || (it.privateUser != null && it.privateUser == currentUsername))
            }
            return tagNames as JSON
        }
        return null

    }
}
