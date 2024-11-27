package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Disposition
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import groovy.transform.ToString

import java.text.SimpleDateFormat

import static com.rxlogix.util.DateUtil.toDateTimeString

@ToString(includes = ['currentDisposition', 'currentPriority', 'currentAssignedTo'])
class LiteratureHistory implements GroovyInterceptable, Serializable {

    def userService

    Disposition currentDisposition
    User currentAssignedTo
    Group currentAssignedToGroup
    Priority currentPriority

    String change
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    String justification
    String searchString
    Long articleId
    Long litConfigId
    Long litExecConfigId
    String tagName
    String subTagName
    String tagsUpdated
    boolean isLatest = false
    Boolean isUndo =false

    static mapping = {
        table("LITERATURE_HISTORY")
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
        change nullable: false
        currentAssignedTo nullable: true
        currentAssignedToGroup nullable: true
        searchString nullable: false
        articleId nullable: false
        litConfigId nullable: false
        litExecConfigId nullable: false
        tagName nullable: true
        subTagName nullable: true
        tagsUpdated nullable: true
        isUndo nullable:true
    }

    Map toDto() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
        [
                litConfigId    : this.litConfigId,
                litExecConfigId: this.litExecConfigId,
                alertName      : getAlertName(this.litConfigId),
                id             : this.id,
                alertTags      : fetchTags(this.tagName),
                alertSubTags   : fetchTags(this.subTagName),
                disposition    : this.currentDisposition.displayName,
                priority       : [value: this.currentPriority?.value, iconClass: this.currentPriority?.iconClass],
                assignedTo     : this.currentAssignedTo ? this.currentAssignedTo.fullName : this.currentAssignedToGroup.name,
                createdBy      : this.createdBy,
                timestamp      : sdf.format(new Date(DateUtil.toDateStringWithTime(this.dateCreated, userService.getUser()?.preference?.timeZone))).toString(),
                updatedBy      : this.modifiedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM) ? Constants.Commons.SYSTEM  :
                        (this.modifiedBy ? User.findByUsername(this.modifiedBy)?.fullName : User.findByUsername(this.createdBy)?.fullName),
                justification  : this.justification,
                change         : this.change ?: "",
                searchString   : this.searchString,
                articleId      : this.articleId

        ]
    }

    String getAlertName(Long litConfigId) {
        LiteratureConfiguration.findById(litConfigId)?.name
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
