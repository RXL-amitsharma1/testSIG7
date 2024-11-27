package signal

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.util.DateUtil
import groovy.json.JsonBuilder
import grails.util.Holders
import org.apache.commons.lang.StringEscapeUtils
import org.hibernate.Session

import javax.servlet.http.HttpSession
import java.util.stream.Collectors

class PvSignalTagLib {

    def userService
    def alertService
    def sessionFactory
    def cacheService

    def defaultPvSignalDateFormat = { attrs ->
        def timeZone = userService.getUser().preference.timeZone
        out << DateUtil.toDateStringWithTimeInAmPmFormat(attrs.date,timeZone)
    }


    def renderShortFormattedDate = { attrs ->
        if (!attrs.date) {
            out << ""
            return
        }
        attrs.formatName = "default.date.format.short"
        out << formatDate(attrs)
    }

    def renderShortDateFormat = { attrs ->
        out << g.formatDate([format: "dd-MMM-yyyy", date: attrs.date])
    }

    def initializeAssignToElement = { attrs ->
        def bean = attrs.bean ? attrs.bean : null
        boolean isLabel = !attrs.containsKey("isLabel")
        boolean isHide = attrs.containsKey("isHide")
        boolean isTags = attrs.containsKey("isTags")
        boolean isClone = attrs.containsKey("isClone")? attrs.isClone: false
        def shareWithId = attrs.assignedToId ?: "assignedTo"

        String assignToData = ''
        def assignToObj
        String prefix
        User user = userService.user
        List blindedUsersList = cacheService.getUserBlindingCache()
        if(isClone && attrs.currentUser){
            assignToObj = attrs.currentUser
            prefix = Constants.USER_TOKEN
        } else if (bean) {
            if (bean.assignedTo) {
                assignToObj = bean.assignedTo
                prefix = Constants.USER_TOKEN
            } else {
                assignToObj = bean.assignedToGroup
                prefix = Constants.USER_GROUP_TOKEN
            }
        }
        if (assignToObj) {
            assignToData = new JsonBuilder([id: prefix + assignToObj?.id, name: assignToObj?.name?.encodeAsHTML(), blinded: blindedUsersList?.contains(assignToObj?.id)]).toString()
        }

        String output = ""
        if(isLabel){
            output += """
                       <label>${g.message(code: 'app.label.action.item.assigned.to', default: 'Assigned To')}</label><span class="required-indicator">*</span>
                      """
        }
        output+= """
                        <script type="text/javascript">
                        """
        if(assignToData){
            output+= "var assignToData = ${assignToData};"
        }

        output+="""
                            sharedWithListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
                            sharedWithValuesUrl = "${createLink(controller: 'util', action: 'sharedWithValues')}";
                            \$(document).ready(function () {
                               bindShareWith(\$('#$shareWithId'), sharedWithListUrl, sharedWithValuesUrl,${assignToData ?: null},${isTags});
                 """
        if(isHide){
            output+="""
                    \$('#$shareWithId').next(".select2-container").hide();
                  """
        }
        output+="""
                            });
                        </script>
                        <select id="$shareWithId" name="assignedToValue" class="form-control assignedToSelect select2"></select>
                        """
        out << output
    }

    def initializeEmailForSignalMemo = { attrs ->
        def bean = attrs.bean ? attrs.bean : null
        boolean isLabel = !attrs.containsKey("isLabel")
        boolean isHide = attrs.containsKey("isHide")
        boolean isTags = attrs.containsKey("isTags")
        def shareWithId = attrs.assignedToId ?: "assignedTo"

        String sharedWithData = ''
        if (bean) {
            def sharedWithUsers =(bean.getMailGroupList()?.collect{[id: Constants.USER_GROUP_TOKEN + it.id, name: it.name]} +
                    bean.getMailUserList()?.collect{[id: Constants.USER_TOKEN + it.id, name: it.fullName]})

            if(sharedWithUsers){
                sharedWithData = new JsonBuilder(sharedWithUsers).toString()
            }
        }

        String output = ""
        if(isLabel){
            output += """
                       <label>${g.message(code: 'app.label.action.item.assigned.to', default: 'Assigned To')}</label><span class="required-indicator">*</span>
                      """
        }
        output+= """
                        <script type="text/javascript">
                        """
        if(sharedWithData){
            output+= "var sharedWithData = ${sharedWithData};"
        }

        output+="""
                            sharedWithListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
                            \$(document).ready(function () {
                               bindShareWithForSignalMemo(\$('#$shareWithId'), sharedWithListUrl, ${sharedWithData ?: null},${isTags});
                 """
        if(isHide){
            output+="""
                    \$('#$shareWithId').next(".select2-container").hide();
                  """
        }
        output+="""
                            });
                        </script>
                        <select id="$shareWithId" name="assignedToValue" class="form-control assignedToSelect select2"></select>
                        """
        out << output
    }

//    NOTE: initializeShareWithElement and initializeSharedWithElement can both be used as both are essentially same, second one was added as same element cannot be used on same page
    def initializeShareWithElement = { attrs ->
        def bean = attrs.bean ? attrs.bean : null
        boolean isLabel = attrs.containsKey("isLabel")?attrs.isLabel:true
        def shareWithId = attrs.shareWithId ?: "sharedWith"
        def isWorkflowEnabled = attrs.isWorkflowEnabled ?: "true"
        boolean isClone = attrs.containsKey("isClone")? attrs.isClone: false
        String sharedWithData = ''
        User user = userService.user
        List blindedUsersList = cacheService.getUserBlindingCache()
        List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
        List readOnlyUsers = []
        List readOnlyGroups = []
        if( (bean instanceof Configuration) && bean?.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT ){
            readOnlyUsers = alertService.getReadOnlyUsers(bean as Configuration)
            readOnlyGroups = alertService.getReadOnlyGroups(bean as Configuration)
        }
        if (isClone && attrs.currentUser) {
            sharedWithData = new JsonBuilder([[id: Constants.USER_TOKEN + attrs.currentUser.id, name: attrs.currentUser.fullName, blinded: blindedUsersList?.contains(attrs.currentUser.id)]]).toString()
        } else if (bean) {
            def sharedWithValue = (bean.getShareWithGroups()?.collect {
                [id: Constants.USER_GROUP_TOKEN + it.id + "${readOnlyGroups?.contains(it.id) ? '_readOnly' : ''}", name: it.name, blinded: blindedUserGroupsList?.contains(it.id)]
            } +
                    bean.getShareWithUsers()?.collect {
                        [id: Constants.USER_TOKEN + it.id + "${readOnlyUsers?.contains(it.id) ? '_readOnly' : ''}", name: it.fullName, blinded: blindedUsersList?.contains(it.id)]
                    })

            if (!sharedWithValue && !bean.id) {
                sharedWithValue = ([[id: Constants.USER_TOKEN + user.id, name: user.fullName, blinded: blindedUsersList?.contains(user.id)]])
            }
            if (sharedWithValue) {
                sharedWithData = new JsonBuilder(sharedWithValue).toString()
            }
        }
        String output
        if(isLabel){
            output = """
                        <label>${g.message(code: 'shared.with', default: 'Share With')}</label>
                        <script type="text/javascript">
                        """
        } else {
            output = """
                        <script type="text/javascript">
                        """
        }
        if(sharedWithData){
            output+= "var sharedWithData = ${sharedWithData};"
        }else{
            output+= "var sharedWithData = ''"
        }
        output += """
             
                            \$(document).ready(function () {
                                sharedWithUrl = "${createLink(controller: 'user', action: 'searchShareWithUserGroupList')}";
                                bindShareWith2WithData(\$('#${shareWithId}'), sharedWithUrl, sharedWithData, ${isWorkflowEnabled});
                            });
                        </script>
                        <select id="${shareWithId}" name="${shareWithId}" class="sharedWithControl form-control select2"></select>
                        """
        out << output
    }

    def initializeShareWithElementProductSelection = { attrs ->
        def shareWithId = attrs.shareWithId ?: "selectUserOrGroup"

        String output = ""

        output+= """
                        <script type="text/javascript">
                        """
        output+= "var assignToData = '';"

        output+="""
                            sharedWithListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
                            \$(document).ready(function () {
                            buildReportingDestinationsSelectBox(\$('#$shareWithId'), sharedWithListUrl, assignToData, \$("#primaryAssignment"), true);
                 """
        output+="""
                            });
                        </script>
                        <select id="${shareWithId}" name="${shareWithId}" class="sharedWithControl form-control select2"></select>
                        """
        out << output
    }

    //    NOTE: initializeShareWithElement and initializeSharedWithElement can both be used as both are essentially same, second one was added as same element cannot be used on same page
    def initializeSharedWithElement = { attrs ->
        def bean = attrs.bean ? attrs.bean : null
        def shareWithId = attrs.shareWithId ?: "sharedWith"
        def isWorkflowEnabled = attrs.isWorkflowEnabled ?: "true"
        String sharedWithData1 = ''
        if (bean) {
            def sharedWithValue1 =(bean.getShareWithGroups()?.collect{[id: Constants.USER_GROUP_TOKEN + it.id, name: it.name]} +
                    bean.getShareWithUsers()?.collect{[id: Constants.USER_TOKEN + it.id, name: it.fullName]})

            if(!sharedWithValue1 && !bean.id)
                sharedWithValue1 = ([[id: Constants.USER_TOKEN + userService.user.id, name: userService.user.fullName]])

            if(sharedWithValue1){
                sharedWithData1 = new JsonBuilder(sharedWithValue1).toString()
            }
        }

        String output = """
                        <label>${g.message(code: 'shared.with', default: 'Share With')}</label>
                        <script type="text/javascript">
                        """
        if(sharedWithData1){
            output+= "var sharedWithData1 = ${sharedWithData1};"
        }else{
            output+= "var sharedWithData1 = ''"
        }
        output += """
             
                            \$(document).ready(function () {
                                sharedWithUrl = "${createLink(controller: 'user', action: 'searchShareWithUserGroupList')}";
                                bindShareWith2WithData(\$('#${shareWithId}'), sharedWithUrl, sharedWithData1, ${isWorkflowEnabled});
                            });
                        </script>
                        <select id="${shareWithId}" name="${shareWithId}" class="sharedWithControl form-control select2"></select>
                        """
        out << output
    }

    def initializeUsersAndGroupsElement = { attrs ->
        HttpSession session = request.getSession()
        String savedValues = ""
        if(attrs?.callingScreen == Constants.Commons.DASHBOARD && attrs.isFromAdhoc){
            savedValues = session.getAttribute("adhocDashboard")
        }else if(attrs?.callingScreen == Constants.Commons.DASHBOARD && attrs.isFromSignal){
            savedValues = session.getAttribute("signalFilterFromDashboard")
        }else{
            savedValues = session.getAttribute(attrs.alertType)
        }
        List<String> retainedValues = (savedValues=="null" || savedValues==null || savedValues=="") ? [] : savedValues?.substring(1,savedValues.length()-1).replaceAll("\"", "").split(",");
        def alertsFilterId = attrs.shareWithId ?: "alertsFilter"
        def isWorkflowEnabled = attrs.isWorkflowEnabled ?: "true"
        String sharedWithData = ''
        def sharedWithValue = []
        List blindedUsers = cacheService.getUserBlindingCache()
        List blindedGroups = cacheService.getUserGroupBlindingCache()
        if(retainedValues) {
            retainedValues.each {
                def createRowData
                if (it.contains("User_")) {
                    createRowData = [id: it, name: User.findById(it.substring(5) as Long).fullName, blinded: blindedUsers?.contains(it.substring(5) as Long)]
                } else if (it.contains("UserGroup_")) {
                    createRowData = [id: it, name: Group.findById(it.substring(10) as Long, [null: null]).name, blinded: blindedGroups?.contains(it.substring(10) as Long)]
                }
                else if(it.contains("Mine_")) {
                    createRowData = [id:it, name: Constants.FilterOptions.OWNER]
                }
                else if(it.contains("AssignToMe_")) {
                    createRowData = [id:it, name: Constants.FilterOptions.ASSIGNED_TO_ME]
                }
                else if(it.contains("SharedWithMe_")) {
                    createRowData = [id:it, name: Constants.FilterOptions.SHARED_WITH_ME]
                }


                sharedWithValue.add(createRowData)
            }
        }
        sharedWithData = new JsonBuilder(sharedWithValue).toString()
        String output = """
                        <script type="text/javascript">
                        """
        if(sharedWithData){
            output+= "var sharedWithData = ${sharedWithData};"
        }else{
            output+= "var sharedWithData = '';"
        }
        output+= "var alertType='${attrs.alertType}';"
        output += """
             
                            \$(document).ready(function () {
                                fetchUsersAndGroupsUrl = "${createLink(controller: 'user', action: 'searchUsersAndGroupsForFilterAlertsAndSignals')}";
                                bindShareWith2WithData(\$('#${alertsFilterId}'), fetchUsersAndGroupsUrl,${sharedWithData},  ${isWorkflowEnabled}, ${false}, alertType);
                            });
                        </script>
                        <select id="${alertsFilterId}" name="${alertsFilterId}" class="sharedWithControl form-control select2"></select>
                        """
        out << output
    }

    def showAssignedToName = { attrs ->
        def bean = attrs.bean ? attrs.bean : null
        String assignedToName
        if(!bean.hasProperty("isAutoAssignedTo")){
            assignedToName  = bean.assignedTo ? bean.assignedTo.fullName : bean.assignedToGroup.name
        } else if(bean.hasProperty("isAutoAssignedTo") && bean.isAutoAssignedTo) {
            assignedToName = Constants.AssignmentType.AUTO_ASSIGN
        } else {
            assignedToName  = bean.assignedTo ? bean.assignedTo.fullName : bean.assignedToGroup.name
        }
        out << StringEscapeUtils.escapeHtml(assignedToName)
    }

    def showSharedWithName = { attrs ->
        def bean = attrs.bean ? attrs.bean : null
        def configDomain
        List sharedWithNamesList = []
        List sharedWithGroupNames = []
        if (bean instanceof ExecutedConfiguration) {
            Configuration configuration = alertService.getAlertConfigObjectByType(bean)
            configDomain = configuration
        } else if (bean instanceof ExecutedEvdasConfiguration) {
            EvdasConfiguration configuration = alertService.getAlertConfigObject(bean)
            configDomain = configuration
        } else {
            configDomain = bean
        }
        sharedWithNamesList = configDomain.shareWithUser?.each {
            it.fullName
        }
        sharedWithGroupNames = configDomain.shareWithGroup?.each {
            it.name
        }
        if(configDomain.hasProperty("autoShareWithUser") && configDomain.hasProperty("autoShareWithGroup")){
            if (configDomain.autoShareWithUser || configDomain.autoShareWithGroup) {
                sharedWithGroupNames.add("Auto Assign")
            }
        }

        String sharedWithUsers = """
            ${sharedWithNamesList?.join('</br>')?: ""}
            ${sharedWithNamesList?'</br>':""}
            ${sharedWithGroupNames?.join('</br>') ?: ""}
        """
        out << sharedWithUsers
    }

    def showSharedWithNameWithReadOnly = { attrs ->
        def bean = attrs.bean ? attrs.bean : null
        def configDomain
        List sharedWithNamesList = []
        List sharedWithGroupNames = []
        List getReadOnlyUsers = []
        List getReadOnlyGroups = []
        String sharedWithUsers
        if (bean instanceof ExecutedConfiguration) {
            if(bean.isLatest){
                sharedWithNamesList = alertService.getReadOnlyUsersForEx(bean)?.collect {
                    User.get(it.id).fullName + "${it.readOnly? ' (R)' : '' }"
                }
                sharedWithGroupNames = alertService.getReadOnlyGroupsForEx(bean)?.collect {
                    Group.get(it.id).name + "${it.readOnly? ' (R)' : '' }"
                }
                sharedWithUsers = """
            ${sharedWithNamesList?.join('</br>')?: ""}
            ${sharedWithNamesList?'</br>':""}
            ${sharedWithGroupNames?.join('</br>') ?: ""}
        """
            }
            else{
                Configuration configuration = alertService.getAlertConfigObjectByType(bean)
                configDomain = configuration
                getReadOnlyUsers = alertService.getReadOnlyUsers(configDomain)
                getReadOnlyGroups = alertService.getReadOnlyGroups(configDomain)
                sharedWithNamesList = configDomain.shareWithUser?.collect {
                    it.fullName + "${getReadOnlyUsers?.contains(it.id)? ' (R)' : '' }";
                }
                sharedWithGroupNames = configDomain.shareWithGroup?.collect {
                    it.name + "${getReadOnlyGroups?.contains(it.id)? ' (R)' : '' }";
                }
                if(configDomain.hasProperty("autoShareWithUser") && configDomain.hasProperty("autoShareWithGroup")){
                    if (configDomain.autoShareWithUser || configDomain.autoShareWithGroup) {
                        sharedWithGroupNames.add("Auto Assign")
                    }
                }

                sharedWithUsers = """
            ${sharedWithNamesList?.join('</br>')?: ""}
            ${sharedWithNamesList?'</br>':""}
            ${sharedWithGroupNames?.join('</br>') ?: ""}
        """
            }
        } else if (bean instanceof ExecutedEvdasConfiguration) {
            EvdasConfiguration configuration = alertService.getAlertConfigObject(bean)
            configDomain = configuration
            sharedWithNamesList = configDomain.shareWithUser?.collect {
                it.fullName + "${getReadOnlyUsers?.contains(it.id)? ' (R)' : '' }";
            }
            sharedWithGroupNames = configDomain.shareWithGroup?.collect {
                it.name + "${getReadOnlyGroups?.contains(it.id)? ' (R)' : '' }";
            }
            if(configDomain.hasProperty("autoShareWithUser") && configDomain.hasProperty("autoShareWithUser")){
                if (configDomain.autoShareWithUser || configDomain.autoShareWithGroup) {
                    sharedWithGroupNames.add("Auto Assign")
                }
            }

            sharedWithUsers = """
            ${sharedWithNamesList?.join('</br>')?: ""}
            ${sharedWithNamesList?'</br>':""}
            ${sharedWithGroupNames?.join('</br>') ?: ""}
        """
        } else {
            configDomain = bean
            getReadOnlyUsers = alertService.getReadOnlyUsers(configDomain as Configuration)
            getReadOnlyGroups = alertService.getReadOnlyGroups(configDomain as Configuration)
            sharedWithNamesList = configDomain.shareWithUser?.collect {
                it.fullName + "${getReadOnlyUsers?.contains(it.id)? ' (R)' : '' }";
            }
            sharedWithGroupNames = configDomain.shareWithGroup?.collect {
                it.name + "${getReadOnlyGroups?.contains(it.id)? ' (R)' : '' }";
            }
            if(configDomain.hasProperty("autoShareWithUser") && configDomain.hasProperty("autoShareWithUser")){
                if (configDomain.autoShareWithUser || configDomain.autoShareWithGroup) {
                    sharedWithGroupNames.add("Auto Assign")
                }
            }

            sharedWithUsers = """
            ${sharedWithNamesList?.join('</br>')?: ""}
            ${sharedWithNamesList?'</br>':""}
            ${sharedWithGroupNames?.join('</br>') ?: ""}
        """
        }
        out << sharedWithUsers
    }

    def getCurrentUser = { attrs ->
        out << userService.getUser()
    }

    def getCurrentUserFullName = { attrs ->
        out << userService.getUserFromCacheByUsername(userService.getCurrentUserName())?.fullName
    }
    def renderUserLastLoginDate = { attrs ->
        def user = userService.getUser()
        if (!user || !user.lastToLastLogin) {
            out << message(code: 'user.neverLoggedIn.before.label')
            return
        }
        attrs.date = user.lastToLastLogin
        attrs.timeZone = user.preference?.timeZone
        attrs.formatName = 'user.lastLogin.date.format'
        out << (formatDate(attrs) + " (${message(code: 'app.timezone.TZ.GMT')} ${TimeZoneEnum.values().find { it.timezoneId == attrs.timeZone }?.gmtOffset})")
    }
    def getCurrentUserTimezone = { attrs ->
        out << userService.getCurrentUserPreference()?.timeZone
    }

    def getCurrentUserLanguage = { attrs ->
        out << userService.getCurrentUserPreference()?.locale?.language
    }

    String getCurrentUserName ={ attrs ->
        out << userService.getCurrentUserName()
    }

    def getMaxUploadLimit = { attrs ->
        out << Holders.config.grails.controllers.upload.maxFileSize
    }

    def getCurrentUserInboxId = { attrs ->
        out << userService.getCurrentUserId()
    }

    def renderLongFormattedDate = { attrs ->
        if (!attrs.date) {
            out << ""
            return
        }
        attrs.formatName = attrs.showTimeZone ? "default.date.format.long.tz" : "default.date.format.long"
        out << formatDate(attrs)
    }

    def renderFormattedComment = {attrs ->
        out << attrs.comment?.replaceAll(Constants.Alias.NEXT_LINE, Constants.Alias.NEW_LINE)
    }
}