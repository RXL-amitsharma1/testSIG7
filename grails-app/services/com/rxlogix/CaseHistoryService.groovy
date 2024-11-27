package com.rxlogix

import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.CaseHistoryDTO
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification

import java.text.SimpleDateFormat

@Transactional
class CaseHistoryService {

    def CRUDService
    def userService
    def activityService
    def singleCaseAlertService
    def dataSource_pva
    def sessionFactory
    def cacheService
    def dataObjectService
    def alertService
    /**
     * Method that saves the case history based on the case history map.
     * It consults any existing history and sets the values from it to the
     * to-be-saved history.
     * @param caseHistoryMap
     * @return
     */
    def saveCaseHistory(Map caseHistoryMap) {
        CaseHistory caseHistory = bindCaseHistoryData(caseHistoryMap)
        if (caseHistoryMap.createdBySystem) {
            CRUDService.instanceSaveWithSystemUser(caseHistory)
        }else {
            CRUDService.saveWithoutAuditLog(caseHistory)
        }
    }

    def saveCaseHistoryForBusinessConfig(Map caseHistoryMap) {
        String caseNumber = caseHistoryMap.caseNumber
        String change = caseHistoryMap.change
        Long configId = caseHistoryMap.configId
        Long singleCaseAlertId = caseHistoryMap.singleAlertId
        Long executedConfigurationId = caseHistoryMap.execConfigId
        String tagNames = caseHistoryMap.tagName
        String subTagName = caseHistoryMap.subTagName
        CaseHistory existingCaseHistory = dataObjectService.getExistingCaseHistory(configId,caseNumber)
        CaseHistory caseHistory = new CaseHistory(caseHistoryMap)
        if (existingCaseHistory) {
            if (caseHistoryMap.change != Constants.Commons.BLANK_STRING) {
                caseHistory.properties = existingCaseHistory.properties
                caseHistory.configId = configId
                caseHistory.singleAlertId = singleCaseAlertId
                caseHistory.execConfigId = executedConfigurationId
                if (change == Constants.HistoryType.DISPOSITION) {
                    caseHistory.currentDisposition = caseHistoryMap.currentDisposition
                    caseHistory.dueDate = caseHistoryMap.dueDate
                } else if (change == Constants.HistoryType.PRIORITY) {
                    caseHistory.currentPriority = caseHistoryMap.currentPriority
                } else if (change == Constants.HistoryType.ASSIGNED_TO) {
                    caseHistory.currentAssignedTo = caseHistoryMap.currentAssignedTo
                    caseHistory.currentAssignedToGroup = caseHistoryMap.currentAssignedToGroup
                } else if (change == Constants.HistoryType.ALERT_TAGS) {
                    caseHistory.tagName = tagNames
                    caseHistory.subTagName = subTagName
                } else if (change == Constants.HistoryType.SUB_TAGS) {
                    caseHistory.subTagName = subTagName
                }
                if (caseHistoryMap.followUpNumber) {
                    caseHistory.followUpNumber = caseHistoryMap.followUpNumber
                }
                caseHistory.change = change
            }
            dataObjectService.setExistingCaseHistoryToMap(configId, existingCaseHistory.id)
        }
        caseHistory.justification = caseHistoryMap.justification
        caseHistory.currentDisposition = caseHistoryMap.currentDisposition ?: caseHistory.currentDisposition
        caseHistory.currentPriority = caseHistoryMap.currentPriority ?: caseHistory.currentPriority
        caseHistory.createdBy = caseHistoryMap.createdBy ?: caseHistory.createdBy
        caseHistory.modifiedBy = caseHistoryMap.modifiedBy ?: caseHistory.modifiedBy
        caseHistory.isLatest = true
        dataObjectService.setCaseHistoryToMap(configId, caseHistory)
    }

    CaseHistory createCaseHistory(Map caseHistoryMap) {
        String caseNumber = caseHistoryMap.caseNumber
        String change = caseHistoryMap.change
        Long configId = caseHistoryMap.configId
        Long singleCaseAlertId = caseHistoryMap.singleAlertId
        Long executedConfigurationId = caseHistoryMap.execConfigId
        String tagNames = caseHistoryMap.tagName
        CaseHistory existingCaseHistory = getLatestCaseHistory(caseNumber, configId)
        CaseHistory caseHistory = new CaseHistory(caseHistoryMap)
        if (existingCaseHistory) {
            if (caseHistoryMap.change != Constants.Commons.BLANK_STRING) {
                caseHistory.properties = existingCaseHistory.properties
                caseHistory.configId = configId
                caseHistory.singleAlertId = singleCaseAlertId
                caseHistory.execConfigId = executedConfigurationId
                if (change == Constants.HistoryType.DISPOSITION) {
                    caseHistory.currentDisposition = caseHistoryMap.currentDisposition
                } else if (change == Constants.HistoryType.PRIORITY) {
                    caseHistory.currentPriority = caseHistoryMap.currentPriority
                } else if (change == Constants.HistoryType.ASSIGNED_TO) {
                    caseHistory.currentAssignedTo = caseHistoryMap.currentAssignedTo
                    caseHistory.currentAssignedToGroup = caseHistoryMap.currentAssignedToGroup
                } else if (change == Constants.HistoryType.ALERT_TAGS) {
                    caseHistory.tagName = tagNames
                }
                if (caseHistoryMap.followUpNumber) {
                    caseHistory.followUpNumber = caseHistoryMap.followUpNumber
                }
                caseHistory.change = change
            }
            existingCaseHistory.isLatest = false
        }

        caseHistory.justification = caseHistoryMap.justification
        caseHistory.isLatest = true

        caseHistory
    }

    CaseHistory getLatestCaseHistory(String caseNumber, Long configId) {
        List<CaseHistory> caseHistory = CaseHistory.createCriteria().list {
            eq('caseNumber', caseNumber)
            eq('configId', configId)
            eq('isLatest', true)
            maxResults(1)
            order("dateCreated", "desc")
        }
        if (caseHistory) {
            return caseHistory[0]
        }
        return null
    }

    def getSecondLatestCaseHistory(String caseNumber, Configuration configuration) {
        //Since we are passing isLatest as false thus it will give list of all (but non latest) case history elements.
        //The first element would be second latest case history.
        List<CaseHistory> list = CaseHistory.findAllByCaseNumberAndConfigurationAndIsLatestAndChange(caseNumber,
                configuration, false, Constants.HistoryType.DISPOSITION)?.sort {
            -it.id
        }
        return list[0]
    }

    List<CaseHistory> listCaseHistory(String caseNumber, Long configId, Boolean isArchived = false, Long exeConfigId = 0) {
        List<CaseHistory> caseHistoriesWithoutError = []
        List<CaseHistory> caseHistories = CaseHistory.createCriteria().list {
            eq('caseNumber', caseNumber)
            eq('configId', configId)
            if (exeConfigId > 0) {
                le('execConfigId', exeConfigId)
            }
            ne('change', Constants.HistoryType.ASSIGNED_TO)
            order("lastUpdated", "desc")
        }
        caseHistories.each {
            if (ExecutionStatus.findByExecutedConfigId(it?.execConfigId)?.executionStatus != ReportExecutionStatus.ERROR) {
                caseHistoriesWithoutError.add(it)
            }
        }
        return removeAllPrivateHistory(caseHistoriesWithoutError)
    }

    def listSuspectCaseHistory(caseNumber, Long configId) {
        List<CaseHistory> caseHistories = CaseHistory.findAllByCaseNumberAndConfigIdNotEqualAndChangeNotEqual(caseNumber, configId, Constants.HistoryType.ASSIGNED_TO)
        List<CaseHistory> caseHistoriesWithoutError = []
        List failedExecConfigIds = ExecutionStatus.createCriteria().list {
            projections {
                property("executedConfigId", "executedConfigId")
            }
            eq("configId", configId)
            eq("executionStatus", ReportExecutionStatus.ERROR)
        }
        caseHistories.each {
            if (!failedExecConfigIds.contains(it.execConfigId)) {
                caseHistoriesWithoutError.add(it)
            }
        }
        removeAllPrivateHistory(caseHistoriesWithoutError)
    }

    void createActivityForJustificationChange(CaseHistory caseHistory, String oldJustification) {
        def alert
        if (caseHistory.singleAlertId)
            alert = SingleCaseAlert.get(caseHistory.singleAlertId)
        else if (caseHistory.archivedSingleAlertId)
            alert = ArchivedSingleCaseAlert.get(caseHistory.archivedSingleAlertId)
        if(alert){
            User currentUser = userService.getUser()
            String textInfo = caseHistory.change == Constants.HistoryType.DISPOSITION ? "Disposition (${caseHistory?.currentDisposition?.displayName})" : caseHistory.change == Constants.HistoryType.PRIORITY ? "Priority (${caseHistory?.currentPriority?.displayName})" : ''
            String details = "Justification for ${textInfo} changed from '$oldJustification' to '${caseHistory.justification}'"
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.JustificationChange)
            activityService.createActivity(alert.executedAlertConfiguration, activityType, currentUser, details, "", ['For Case Number': alert.caseNumber],
                    alert.productName, alert.pt, alert.assignedTo, alert.caseNumber, alert.assignedToGroup)
        }
    }

    /**
     * This method converts a CaseHistory object into caseHistoryMap useful for displaying case history
     * @param caseHistory - Passed case history object
     * @param caseVisibility - This is the value of case number which will be displayed on UI,
     * Possible values for caseVisibility: Blinded, Redacted or the case number itself.
     * @return - A transformed map of case history object will be returned.
     */
    Map getCaseHistoryMap(CaseHistory caseHistory, String caseVisibility = '') {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List allTagNames = caseHistory.tagName? JSON.parse(caseHistory.tagName) : []
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
        }?.unique()
        List allSubTagNames = caseHistory.subTagName? JSON.parse(caseHistory.subTagName) : []
        List subTagNames = allSubTagNames.findAll{
            (it.privateUser == null || (it.privateUser!=null && it.privateUser == currentUsername))
        }
        String dataSource = Configuration.findById(caseHistory.configId)?.selectedDatasource
        String followUpString = "";
        if( !(dataSource == "faers" || dataSource == "vaers" || dataSource == "vigibase")){
            followUpString =  '(' + caseHistory.followUpNumber + ')';
        }
        String caseNumber = (caseVisibility in [Constants.BlindingStatus.BLINDED, Constants.BlindingStatus.REDACTED]) ? caseVisibility : caseVisibility + followUpString
        [
                alertName    : Configuration.findById(caseHistory.configId)?.name,
                disposition  : caseHistory.currentDisposition?.displayName,
                priority     : caseHistory.currentPriority?.displayName,
                justification: caseHistory.justification,
                timestamp    : new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM).format(new Date(DateUtil.toDateStringWithTime(caseHistory.dateCreated, userTimezone))).toString(),
                updatedBy    : caseHistory.modifiedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM) ? Constants.Commons.SYSTEM : (caseHistory.modifiedBy ? User.findByUsername(caseHistory.modifiedBy)?.fullName
                        : User.findByUsername(caseHistory.createdBy)?.fullName),
                alertTags    : tagNames?.name,
                alertSubTags : subTagNames?.name,
                caseNumber   : caseNumber
        ]
    }

    List<Map> getDefaultHistoryMap() {
        [[alertName: null, disposition: null, justification: null, priority: null, alertTags: null, updatedBy: null, timestamp: null]]
    }

//TODO if common is working then remove it
    void batchPersistCaseHistory(List<CaseHistory> caseHistoryList) {
        CaseHistory.withTransaction {
            def batch = []
            for (CaseHistory caseHistory : caseHistoryList) {
                batch += caseHistory
                Session session = sessionFactory.currentSession
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (CaseHistory caseHistoryObj in batch) {
                        caseHistoryObj.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (CaseHistory caseHistoryObj in batch) {
                        caseHistoryObj.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
        }
    }

    void bulkUpdateCaseHistoryByHQL(List<Long> caseHistoryIdList) {
        caseHistoryIdList.collate(1000).each{
            CaseHistory.executeUpdate("Update CaseHistory set isLatest = false where id in (:caseHistoryIdList)", [caseHistoryIdList: it])
        }
    }

    List<Map> existingCaseHistoryMap(List<Long> singleCaseAlertIdList){
        CaseHistory.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("caseNumber", "caseNumber")
                property("configId", "configId")
            }
            eq("isLatest", true)
            singleCaseAlertIdList.collate(1000).each {
                'in'("singleAlertId",it)
            }
        } as List<Map>
    }

    List<CaseHistory> getAlertHistoryByConfigIdList(List<Long> execConfigIdList, List<String> caseNumberList) {
        CaseHistory.createCriteria().list {
            'in'('execConfigId', execConfigIdList)
            'in'('caseNumber', caseNumberList)
            order("lastUpdated", "desc")
        } as List<CaseHistory>
    }

    List<CaseHistoryDTO> getAlertHistoryByConfigIdListForExport(String execConfigIds, String caseNumbers) {
        Session session = sessionFactory.currentSession
        String sql_statement = SignalQueryHelper.case_history_sql(execConfigIds, caseNumbers)
        alertService.getTransformedResultList(CaseHistoryDTO.class.name, sql_statement, session)
    }

    List<CaseHistory> removeAllPrivateHistory(List<CaseHistory> caseHistories) {
        List<CaseHistory> newCaseHistories = caseHistories.findAll{
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
        return newCaseHistories
    }

    CaseHistory bindCaseHistoryData(Map caseHistoryMap) {
        String caseNumber = caseHistoryMap.caseNumber
        String change = caseHistoryMap.change
        Long configId = caseHistoryMap.configId
        Integer caseVersion = caseHistoryMap.caseVersion
        Long singleCaseAlertId = caseHistoryMap.singleAlertId
        Long executedConfigurationId = caseHistoryMap.execConfigId
        String tagNames = caseHistoryMap.tagName
        String subTagNames = caseHistoryMap.subTagName
        String createdByUser = caseHistoryMap.createdBy
        String modifiedByUser = caseHistoryMap.modifiedBy
        CaseHistory existingCaseHistory = getLatestCaseHistory(caseNumber, configId)
        CaseHistory caseHistory = new CaseHistory(caseHistoryMap)
        if (existingCaseHistory) {
            if (caseHistoryMap.change != Constants.Commons.BLANK_STRING) {
                caseHistory.properties = existingCaseHistory.properties
                caseHistory.configId = configId
                caseHistory.singleAlertId = singleCaseAlertId
                caseHistory.execConfigId = executedConfigurationId
                if (change == Constants.HistoryType.DISPOSITION) {
                    caseHistory.currentDisposition = caseHistoryMap.currentDisposition
                    caseHistory.dueDate = caseHistoryMap.dueDate
                } else if (change == Constants.HistoryType.PRIORITY) {
                    caseHistory.currentPriority = caseHistoryMap.currentPriority
                } else if (change == Constants.HistoryType.ASSIGNED_TO) {
                    caseHistory.currentAssignedTo = caseHistoryMap.currentAssignedTo
                    caseHistory.currentAssignedToGroup = caseHistoryMap.currentAssignedToGroup
                } else if (change == Constants.HistoryType.ALERT_TAGS) {
                    caseHistory.tagName = tagNames
                    caseHistory.subTagName = subTagNames
                }
                if (caseHistoryMap.followUpNumber) {
                    caseHistory.followUpNumber = caseHistoryMap.followUpNumber
                }
                caseHistory.change = change
                caseHistory.caseVersion = caseVersion
            }
            existingCaseHistory.isLatest = false
            existingCaseHistory.save(failOnError: true, flush: true)
        }
        caseHistory.justification = caseHistoryMap.justification
        caseHistory.isLatest = true
        caseHistory.setLastUpdated(new Date())
        caseHistory.createdBy = createdByUser ? createdByUser : caseHistory.createdBy
        caseHistory.modifiedBy = modifiedByUser ? modifiedByUser : caseHistory.modifiedBy
        return caseHistory
    }


    void saveCaseHistoryForCaseReset(Map caseHistoryMap) {
        Long configId = caseHistoryMap.configId
        CaseHistory caseHistory = bindCaseHistoryData(caseHistoryMap)
        dataObjectService.setCaseHistoryToMap(configId, caseHistory)
    }

}
