package com.rxlogix

import com.rxlogix.Constants
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional

import java.text.SimpleDateFormat

@Transactional(readOnly = true)
class LiteratureHistoryService implements AlertUtil {

    def CRUDService
    UserService userService
    CacheService cacheService
    LiteratureActivityService literatureActivityService


    /**
     * Method that saves the case history based on the article history map.
     * It consults any existing history and sets the values from it to the
     * to-be-saved history.
     * @param articleHistoryMap
     * @return
     */
    @Transactional
    def saveLiteratureArticleHistory(Map literatureHistoryMap,Boolean isArchived = false) {
        String change = literatureHistoryMap.change
        LiteratureHistory existingLiteratureHistory = getLatestLiteratureHistory(literatureHistoryMap.articleId, literatureHistoryMap.litExecConfigId)
        LiteratureHistory literatureHistory = new LiteratureHistory(literatureHistoryMap)
        if (existingLiteratureHistory) {
            if (literatureHistory.change != Constants.Commons.BLANK_STRING) {
                literatureHistory.litConfigId = literatureHistoryMap.litConfigId
                literatureHistory.articleId = literatureHistoryMap.articleId
                literatureHistory.litExecConfigId = literatureHistoryMap.litExecConfigId
                if (change == Constants.HistoryType.DISPOSITION) {
                    literatureHistory.currentDisposition = literatureHistoryMap.currentDisposition
                } else if (change == Constants.HistoryType.PRIORITY) {
                    literatureHistory.currentPriority = literatureHistoryMap.currentPriority
                } else if (change == Constants.HistoryType.ASSIGNED_TO) {
                    literatureHistory.currentAssignedTo = literatureHistoryMap.currentAssignedTo
                    literatureHistory.currentAssignedToGroup = literatureHistoryMap.currentAssignedToGroup
                } else if (change == Constants.HistoryType.ALERT_TAGS) {
                    literatureHistory.tagName = literatureHistoryMap.tagName
                }
                literatureHistory.change = change
            }
            existingLiteratureHistory.isLatest = false
            CRUDService.updateWithoutAuditLog(existingLiteratureHistory)
        }

        literatureHistory.justification = literatureHistoryMap.justification
        literatureHistory.isLatest = !isArchived
        literatureHistory.setLastUpdated(new Date())
        CRUDService.saveWithoutAuditLog(literatureHistory)
    }

    LiteratureHistory getLatestLiteratureHistory(Long articleId, Long litExecConfigId) {
        LiteratureHistory.findByArticleIdAndLitExecConfigIdAndIsLatest(articleId, litExecConfigId, true)
    }

    Map getLiteratureHistoryMap(LiteratureHistory literatureHistory, def literatureAlert, String historyType, String justification, Boolean isUndo = false) {
        [
                "litConfigId"           : literatureAlert.litSearchConfig.id,
                "currentDisposition"    : literatureHistory ? literatureHistory.currentDisposition : literatureAlert.disposition,
                "currentPriority"       : literatureAlert.priority,
                "articleId"             : literatureAlert.articleId,
                "searchString"          : literatureAlert.searchString,
                "currentAssignedTo"     : literatureHistory ? literatureHistory.currentAssignedTo : literatureAlert.assignedTo,
                "currentAssignedToGroup": literatureAlert.assignedToGroup,
                "litExecConfigId"       : literatureAlert.exLitSearchConfig.id,
                "justification"         : justification ?: Constants.Commons.BLANK_STRING,
                "tagName"               : '',
                "change"                : historyType,
                "isUndo"                : isUndo
        ]
    }

    /**
     * Method return the Tags attached to Literature Alert
     * @param LiteratureAlert
     * @return tagList
     */
    String getAlertTagNames(def alert) {
        List tagNames = alert?.alertTags?.name?.unique()
        List tagList = []
        Map tagObj = [:]
        tagNames.each { tagName ->
            tagObj = ["name": tagName]
            tagList.add(tagObj as JSON)
        }
        tagList as String
    }

    /**
     * Method return the List of the Literature History for the given articleId and Litertaure Executed Config
     * @param ArticleId and LitExConfigId
     * @return LiteratureHistories
     */
    List<Map> listLiteratureHistory(Long articleId, Long litConfigId) {
        List<Map> literatureHistoryList = []
        List<LiteratureHistory> history = getLiteratureHistory(articleId, litConfigId)
        history?.collect {
            literatureHistoryList.add(it.toDto())
        }
        return literatureHistoryList
    }

    /**
     * Method return the List of the Literature History for the given articleId and exclude the alerts have given Litertaure Executed Config
     * @param ArticleId and LitExConfigId
     * @return LiteratureHistories
     */
    List<Map> getArticleHistoriesInOtherAlerts(Long articleId, Long litConfigId) {
        List<Map> literatureHistoryList = []
        List<LiteratureHistory> history = getLiteratureHistoryForOtherAlerts(articleId, litConfigId)
        history?.collect {
            literatureHistoryList.add(it.toDto())
        }
        return literatureHistoryList
    }

    List<LiteratureHistory> getLiteratureHistory(Long articleId, Long litConfigId) {
        List<LiteratureHistory> literatureHistories = LiteratureHistory.findAllByArticleIdAndLitConfigIdAndChangeNotEqual(articleId, litConfigId , Constants.HistoryType.ASSIGNED_TO)?.sort { -it.id }
        return removeAllPrivateHistory(literatureHistories)
    }

    List<LiteratureHistory> getLiteratureHistoryForOtherAlerts(Long articleId, Long litConfigId) {
        List<String> sharedConfigurations = userService.getUserLitConfigurations()
        List<Long> configIds = sharedConfigurations.collect{
            LiteratureConfiguration.findByName(it).id}
        List<LiteratureHistory> literatureHistories = LiteratureHistory.createCriteria().list{
            eq('articleId' , articleId)
            ne('litConfigId' , litConfigId)
            ne('change' , Constants.HistoryType.ASSIGNED_TO)
            or {
                configIds.collate(1000).each {
                    'in'('litConfigId' , it)
                }
            }
            order("id","desc")
        }
        return removeAllPrivateHistory(literatureHistories)
    }

    /**
     * Method that update the justification for the given Literature History id .
     * @param historyId and newJustification
     * @return
     */
    void updateJustification(Long id, String newJustification) {
        LiteratureHistory history = LiteratureHistory.get(id)
        if (history.justification?.trim() != newJustification?.trim()) {
            String oldJustification = history.justification
            history.justification = newJustification?.trim()
            try {
                CRUDService.saveWithoutAuditLog(history)
                createActivityForJustificationChange(history, oldJustification)
            }
            catch (Exception e) {
                log.error(e.getMessage(), e)
            }
        }
    }

    Map getHistoryMap(LiteratureHistory history) {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List allTagNames = history.tagName? JSON.parse(history.tagName) : []
        String currentUsername = userService.getUser()?.username
        List tagNames = allTagNames.findAll{
            (it.privateUser == null || (it.privateUser!=null && it.privateUser == currentUsername))
        }.each{
            if(it.type == Constants.Commons.CASE_SERIES_TAG)
                it.name = it.name + ' (A)'
            else if(it.type == Constants.Commons.PRIVATE_TAG_GLOBAL)
                it.name = it.name + ' (P)'
            else if(it.type == Constants.Commons.PRIVATE_TAG_ALERT)
                it.name = it.name + ' (A)(P)'
        }
        List allSubTagNames = history.subTagName? JSON.parse(history.subTagName) : []
        List subTagNames = allSubTagNames.findAll{
            (it.privateUser == null || (it.privateUser!=null && it.privateUser == currentUsername))
        }
        [
                alertName    : history.getAlertName(history.litConfigId),
                disposition  : history.currentDisposition?.displayName,
                priority     : history.currentPriority?.displayName,
                justification: history.justification,
                timestamp    : new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM).format(new Date(DateUtil.toDateStringWithTime(history.dateCreated, userTimezone))).toString(),
                updatedBy    : history.modifiedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM) ? Constants.Commons.SYSTEM :
                        (history.modifiedBy ? User.findByUsername(history.modifiedBy)?.fullName : User.findByUsername(history.createdBy)?.fullName),
                alertTags    : tagNames?.name,
                alertSubTags : subTagNames?.name
        ]
    }

    /**
     * Method that save the activity for the justification and LiteratureHistory .
     * @param LiteratureHistory and oldJustification
     * @return
     */
    void createActivityForJustificationChange(LiteratureHistory history, String oldJustification) {
        LiteratureAlert alert = LiteratureAlert.findByArticleIdAndExLitSearchConfig(history.articleId as int, ExecutedLiteratureConfiguration.get(history.litExecConfigId))
        if (alert) {
            User currentUser = userService.getUser()
            String textInfo = history.change == Constants.HistoryType.DISPOSITION ? "Disposition (${history?.currentDisposition?.displayName})"
                    : history.change == Constants.HistoryType.PRIORITY ? "Priority (${history?.currentPriority?.displayName})" : ''
            String details = "Justification for ${textInfo} changed from '$oldJustification' to '${history.justification}'"
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.JustificationChange)
            String productName = getNameFieldFromJson(alert.litSearchConfig.productSelection)
            String eventName = getNameFieldFromJson(alert.litSearchConfig.eventSelection)
            literatureActivityService.createLiteratureActivity(alert.exLitSearchConfig, activityType, currentUser, details, "",
                    productName, eventName, alert.assignedTo, alert.searchString, alert.articleId, alert.assignedToGroup)
        }
    }

    List<LiteratureHistory> removeAllPrivateHistory(List<LiteratureHistory> literatureHistories) {
        List<LiteratureHistory> newHistories = literatureHistories.findAll{
            String tags = it.tagsUpdated
            if(tags && tags != '[]') {
                List allTagNames = JSON.parse(tags)
                String currentUsername = userService.getUser().username
                String privateUserNameTag
                Integer count = 0
                allTagNames.each{tag->
                    if(tag.privateUser) {
                        count++
                        privateUserNameTag = tag.privateUser
                    }
                }
                count != allTagNames.size() || currentUsername == privateUserNameTag

            } else {
                true
            }
        }
        return newHistories
    }

}
