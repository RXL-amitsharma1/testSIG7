package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.gorm.dirty.checking.DirtyCheck
import groovy.transform.ToString
import grails.converters.JSON
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat


@ToString(includes=['caseNumber', 'caseVersion', 'currentDisposition', 'currentPriority', 'currentAssignedTo'])
@DirtyCheck
class CaseHistory implements GroovyInterceptable,Serializable {

    def userService

    //Case history related fields
    Disposition currentDisposition
    User currentAssignedTo
    Group currentAssignedToGroup
    Priority currentPriority

    String change
    String caseNumber
    Integer caseVersion
    String productFamily
    Integer followUpNumber

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    String justification
    boolean isLatest
    Long configId
    Long execConfigId
    Long singleAlertId
    Long archivedSingleAlertId
    String tagName
    String subTagName
    String tagsUpdated
    Date dueDate
    boolean isUndo =false
    Date createdTimestamp

    static mapping = {
        table("CASE_HISTORY")
        singleAlertId index: 'single_alert_id_idx'
        justification column: "JUSTIFICATION", length: 9000
        currentPriority lazy: false
        currentDisposition lazy: false
        currentAssignedTo lazy: false
        currentAssignedToGroup lazy: false
        tagName length: 4000
        subTagName length: 4000
        tagsUpdated length: 4000
    }

    static constraints = {
        justification nullable: true, maxSize: 9000
        createdBy nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        modifiedBy nullable: true
        followUpNumber nullable: true
        change nullable: true
        currentAssignedTo nullable: true, blank: true
        currentAssignedToGroup nullable: true, blank: true
        configId nullable: true
        singleAlertId nullable: true
        archivedSingleAlertId nullable: true
        execConfigId nullable: true
        tagName nullable: true
        subTagName nullable: true
        tagsUpdated nullable: true
        dueDate nullable: true
        isUndo nullable: true
        createdTimestamp nullable: true
    }

    Map toDto(String userTimezone = "UTC", List configIds = [], String caseVisibility = '') {
        boolean isAccessibleToCurrentUser = true
        Date timeStamp = null
        if (Objects.nonNull(this.createdTimestamp) && StringUtils.isBlank(this.tagName)) {
            timeStamp = this.createdTimestamp
        } else {
            timeStamp = this.dateCreated
        }
        if(configIds && !(this.configId in configIds)){
            isAccessibleToCurrentUser = false
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
        [
                alertConfigId        : this.configId,
                executedAlertConfigId: this.execConfigId,
                alertName            : getAlertName(this.configId),
                id                   : this.id,
                alertTags            : fetchTags(this.tagName),
                alertSubTags         : fetchTags(this.subTagName),
                disposition          : this.currentDisposition.displayName?:'-',
                priority             : [value: this.currentPriority?.value, iconClass: this.currentPriority?.iconClass],
                assignedTo           : this.currentAssignedTo ? this.currentAssignedTo.fullName : this.currentAssignedToGroup.name,
                createdBy            : this.createdBy?.equalsIgnoreCase(Constants.Commons.SYSTEM) ? Constants.Commons.SYSTEM :
                                        (User.findByUsername(this.createdBy)?.fullName ?: this.createdBy),
                timestamp            : sdf.format(new Date(DateUtil.toDateStringWithTime(timeStamp, userTimezone))).toString(),
                productFamily        : this.productFamily,
                caseVersion          : this.caseVersion,
                caseNumber           : caseVisibility,
                updatedBy            : this.modifiedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM) ? Constants.Commons.SYSTEM :
                        (this.modifiedBy ? User.findByUsername(this.modifiedBy)?.fullName : User.findByUsername(this.createdBy)?.fullName),
                justification        : this.justification?:'-',
                followUpNumber       : this.followUpNumber.toString() ?: '-',
                change               : this.change ?: "",
                isAccessibleToCurrentUser: isAccessibleToCurrentUser
        ]
    }

    String getAlertName(Long configId){
        Configuration.findById(configId)?.name
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
