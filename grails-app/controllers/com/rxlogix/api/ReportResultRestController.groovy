package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportResultStatus
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.user.User
import com.rxlogix.user.Group
import grails.converters.JSON
import grails.rest.RestfulController
import groovy.json.JsonBuilder

class ReportResultRestController extends RestfulController {
    def springSecurityService
    def cacheService
    def alertService

    ReportResultRestController() {
        super(ReportResult)
    }

    def index() {
        User currentUser = springSecurityService.currentUser
        List<ExecutedConfiguration> reports = getCompletedReport(ExecutedConfiguration.viewableByUserAndNew(currentUser).list())
        respond getExecutedConfigMaps(reports, currentUser), [formats:['json']]
    }

    def archived() {
        User currentUser = springSecurityService.currentUser
        List<ExecutedConfiguration> reports = getCompletedReport(ExecutedConfiguration.viewableByUserAndReviewed(currentUser).list())
        respond getExecutedConfigMaps(reports, currentUser), [formats:['json']]
    }

    def UnderReview() {
        User currentUser = springSecurityService.currentUser
        List<ExecutedConfiguration> reports = getCompletedReport(ExecutedConfiguration.viewableByUserAndNotReviewed(currentUser).list())
        respond getExecutedConfigMaps(reports, currentUser), [formats:['json']]
    }

    // Once the Execution status hierarchy changes this needs to be deleted
    private List<ExecutedConfiguration> getCompletedReport(List<ExecutedConfiguration> executedConfigurations) {
        List<ExecutedConfiguration> results = []
        executedConfigurations.each {
            if (it.getExecutionStatus()==ReportExecutionStatus.COMPLETED.value()) {
                results.add(it)
            }
        }
        return results
    }

    private getExecutedConfigMaps(List<ExecutedConfiguration> executedConfiguration, User currentUser) {
        def configsMap = []
        executedConfiguration.each {
            ReportResultStatus status = ReportResultStatus.NEW
            it.sharedWith.each { sw ->
                if (sw.user == currentUser) {
                    status = sw.status
                }
            }
            configsMap += [name:it.name, description:it.description, owner:it.owner.fullName, dateCreated: it.dateCreated,
                           id:it.id, version:it.numOfExecutions, status: status]
        }
        return configsMap
    }

    def getSharedWithUsers() {
        if (params.id) {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(params.id)
            if (executedConfiguration) {
                respond executedConfiguration.assignedTo, [formats:['json']]
            } else {
                log.info("Not valid config id.")
            }
        }
    }

    def getSharedWithUserAndGroups() {

        if (params.id) {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(params.id)
            if (executedConfiguration) {
                List blindedUsersList = cacheService.getUserBlindingCache()
                List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
                Configuration config = Configuration.get(executedConfiguration.configId)
                List<Map> users = config.getShareWithUsers()?.collect{[id: Constants.USER_TOKEN + it.id, name: it.fullName, blinded: blindedUsersList?.contains(it.id)]}
                List<Map>  groups= config.getShareWithGroups()?.collect{[id: Constants.USER_GROUP_TOKEN + it.id, name: it.name, blinded: blindedUserGroupsList?.contains(it.id)]}
                if (config.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    users.clear()
                    alertService.getReadOnlyUsersForEx(executedConfiguration)?.each { entry ->
                        users.add([
                                id: Constants.USER_TOKEN + entry.id + "${entry.readOnly ? '_readOnly' : ''}",
                                name: User.get(entry.id).fullName,
                                blinded: blindedUsersList?.contains(entry.id)
                        ])
                    }
                    groups.clear()
                    alertService.getReadOnlyGroupsForEx(executedConfiguration)?.each { entry ->
                        groups.add([
                                id: Constants.USER_GROUP_TOKEN + entry.id + "${entry.readOnly ? '_readOnly' : ''}",
                                name: Group.get(entry.id).name,
                                blinded: blindedUserGroupsList?.contains(entry.id)
                        ])
                    }
                }
                Map result =[users: users, groups: groups, all: users + groups]
                if(config.autoShareWithGroup || config.autoShareWithUser){
                    result.all.add([id:"AUTO_ASSIGN",name:"Auto Assign"])
                }
                render result as JSON
            } else {
                log.info("Not valid config id.")
            }
        }
    }

    def getEmailToUsers() {
        if (params.id) {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(params.id)
            if (executedConfiguration) {
                respond executedConfiguration.assignedTo, [formats: ['json']]
            } else {
                // no such result
            }
        } else {
            // no valid id
        }
    }

}
