package com.rxlogix

import com.rxlogix.config.AlertDeletionData
import com.rxlogix.config.Configuration
import com.rxlogix.config.DeletionStatus
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.user.Group
import com.rxlogix.user.User


class ResourceSecurityInterceptor {

    def userService

    ResourceSecurityInterceptor(){
        match controller: ~/aggregateCaseAlert|singleCaseAlert|literatureAlert|evdasAlert|singleOnDemandAlert|aggregateOnDemandAlert|evdasOnDemandAlert/,
                action: ~/view|edit|copy|delete|details|adhocDetails|dssScores/
    }

    boolean before() {
        if(params.action == "dssScores" || params.action == "details" || params.action == "adhocDetails" || ((params.controller == "singleOnDemandAlert" || params.controller == "aggregateOnDemandAlert" || params.controller == "evdasOnDemandAlert" ) && params.action == "delete")) {
            if ((params.controller == "singleOnDemandAlert" || params.controller == "aggregateOnDemandAlert" || params.controller == "evdasOnDemandAlert" ) && params.action == "delete") {
             params.id =  checkForDetails(params, controllerName) ? params.id : -1
            } else {
                params.configId = checkForDetails(params, controllerName) ? params.configId : -1
            }
        }
        else if(params.id){
            def domain = Configuration
            String alertType
            switch (controllerName){
                case 'literatureAlert':
                    domain = LiteratureConfiguration
                    alertType=Constants.AlertConfigType.LITERATURE_SEARCH_ALERT
                    break
                case 'evdasAlert':
                    domain = EvdasConfiguration
                    alertType=Constants.AlertConfigType.EVDAS_ALERT
                    break
                case 'evdasOnDemandAlert':
                    domain = EvdasConfiguration
                    alertType=Constants.AlertConfigType.EVDAS_ALERT_DEMAND
                    break
            }
            def config = domain.findById(params.id)
            if(alertType==null || alertType.equals("")){
                alertType=config?.type
            }

            if(config){
                if(!isAuthorizedResource(config,null) || isDeleteInProgress(config.id, alertType)){
                    params.id = -1
                }
            }
        }

        return true
    }

    private boolean isAuthorizedResource(def config, def exConfig = null){
        User user = userService.getUser()
        Set<Group> groups = Group.findAllUserGroupByUser(user)
        Group getAssignedToGroup = groups.find { group -> group.id == config.assignedToGroup?.id }
        Boolean isAutoShare = false
        Boolean isValidAutoShare = false
        if(config?.hasProperty("autoShareWithUser") || config?.hasProperty("autoShareWithGroup")){
            isAutoShare = true
            isValidAutoShare = config.autoShareWithUser?.contains(user) || config?.autoShareWithGroup?.find{group -> groups?.contains(group)}
        }
        if((config?.owner != user && config?.assignedTo != user && !config?.assignedToGroup?.members?.contains(user) && !(isAutoShare && isValidAutoShare) && getAssignedToGroup == null && user.id != config?.assignedTo?.id)){
            return true
        }
        if(exConfig){
            if ((!(config.isDeleted) || !(exConfig.isDeleted)) && (config.owner == user || config.shareWithUser?.contains(user) || config.shareWithGroups?.find { group -> groups?.contains(group) } ||
                    config.assignedTo == user || config.assignedToGroup?.members?.contains(user) || (isAutoShare && isValidAutoShare) || getAssignedToGroup != null || user.id == config.assignedTo?.id))
                return true
        }
        else if (!(config.isDeleted) && (config.owner == user || config.shareWithUser?.contains(user) || config.shareWithGroups?.find { group -> groups?.contains(group) } ||
                config.assignedTo == user || config.assignedToGroup?.members?.contains(user) || (isAutoShare && isValidAutoShare) || getAssignedToGroup != null || user.id == config.assignedTo?.id))
            return true
        return false
    }

    private boolean isDeleteInProgress(Long configId, String alertType) {
        def deletion = AlertDeletionData.createCriteria().get {
            eq("configId",configId)
            eq("alertType",alertType)
            eq("deletionStatus", DeletionStatus.READY_TO_DELETE)
            eq("deletionCompleted",false)
        }
        return deletion != null
    }

    private Boolean checkForDetails(def params, def controllerName){
        def domain = ExecutedConfiguration
        def configDomain = Configuration
        String alertType
        switch (controllerName){
            case 'literatureAlert':
                domain = ExecutedLiteratureConfiguration
                configDomain = LiteratureConfiguration
                alertType=Constants.AlertConfigType.LITERATURE_SEARCH_ALERT
                break
            case 'evdasAlert':
                domain = ExecutedEvdasConfiguration
                configDomain = EvdasConfiguration
                alertType=Constants.AlertConfigType.EVDAS_ALERT
                break
            case 'evdasOnDemandAlert':
                domain = ExecutedEvdasConfiguration
                configDomain = EvdasConfiguration
                alertType=Constants.AlertConfigType.EVDAS_ALERT_DEMAND
                break
        }
        def exConfig
        if(params.id){
             exConfig = domain.findById(params?.id)
        }else{
            exConfig = domain.findById(params?.configId)
        }
        if(exConfig){
            boolean isCaseSeries = exConfig instanceof ExecutedConfiguration && exConfig.isCaseSeries
            if (isCaseSeries) {
                return true
            }
            def config = configDomain.get(exConfig.configId)
            if(alertType==null || alertType.equals("")){
            alertType=config?.type
            }
            if(!isAuthorizedResource(config,exConfig)){
                return false
            } else if(isDeleteInProgress(config.id, alertType)){
                return true
            }
        }else{
            return false
        }
        return true
    }
}
