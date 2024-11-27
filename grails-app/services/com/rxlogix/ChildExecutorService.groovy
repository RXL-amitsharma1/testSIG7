package com.rxlogix

import com.hazelcast.core.HazelcastInstance
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.MasterChildRunNode
import com.rxlogix.config.MasterConfigStatus
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.config.MasterExecutedConfiguration
import com.rxlogix.customException.ExecutionStatusException
import grails.gorm.transactions.Transactional
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.springframework.transaction.annotation.Propagation
import org.apache.commons.lang3.StringUtils

@Transactional
class ChildExecutorService {

    def hazelcastService
    def alertService
    def grailsApplication
    def aggregateCaseAlertService
    def reportExecutorService
    def masterExecutorService
    def sessionFactory

    HazelcastInstance hazelcastInstance

    public static String HAZELCAST_CURRENTLY_RUNNING_CHILD = "hazelcastCurrentlyRunningChild"
    public static String HAZELCAST_CURRENTLY_RUNNING_MASTER_CHILD = "hazelcastCurrentlyRunningMasterChild"

    List<Long> currentlyChildRunning = []

    void clearChildExecutionQueue() {
        currentlyChildRunning = []
    }

    List<Long> currentlyMasterChildRunning = []

    void clearMasterChildExecutionQueue() {
        currentlyChildRunning = []
    }

    Map getResumeChildAlertToSave(Long masterExecId = null, List<Long> configIdList=[]){
        List childIds = ExecutedConfiguration.findAllByMasterExConfigIdAndConfigIdInList(masterExecId,configIdList)*.id
        List<MasterChildRunNode> childAlertList = MasterChildRunNode.createCriteria().list {
            eq("masterExecId", masterExecId)
            if (childIds) {
                childIds.collate(1000).each {
                    'in'('childExecId', it)
                }
            }
            eq("isSaveDone", false)
            if (currentlyChildRunning) {
                not {
                    'in'('childExecId', currentlyChildRunning)
                }
            }
        }
        List<Map> childAlerts = childAlertList?.collect {
            [childExecId: it.childExecId, faersId: it.faersId, evdasId: it.evdasId, vaersId: it.vaersId, vigibaseId: it.vigibaseId]
        }
        [childAlerts: childAlertList*.childExecId, childAlertMap: childAlerts]
    }

    Map getChildAlertToSave(Long masterExecId) {
        
        List<MasterChildRunNode> childAlertList = MasterChildRunNode.createCriteria().list {
            List<Long> runningChilds = manageRunningChildExecutionQueue(true, false, false)
            eq("masterExecId", masterExecId)
            eq("nodeUuid", hazelcastService.hazelcastInstance.cluster.localMember.uuid)
            eq("fileGenerated", true)
            eq("isSaveDone", false)
            if (runningChilds) {
                not {
                    'in'('childExecId', runningChilds)
                }
            }
        }
        List<Map> childAlerts = childAlertList?.collect {
            [childExecId: it.childExecId, faersId: it.faersId, evdasId: it.evdasId, exEvdasId: it.exEvdasId, vaersId: it.vaersId, vigibaseId: it.vigibaseId]
        }
        [childAlerts: childAlertList*.childExecId, childAlertMap: childAlerts]
    }

    void persistChildAlert(Long childId, Long masterExecId, List executionStatusList, Boolean isResume=false, List<Map> childAlertMap) {
        String filePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}"
        Map<String, List<Map>> alertDataMap = [:]
        List<String> allFiles = []
        Map otherDataSourcesExecIds = [:]
        ExecutedConfiguration executedConfig = ExecutedConfiguration.findById(childId)
        MasterExecutedConfiguration masterExecutedConfiguration = MasterExecutedConfiguration.findById(masterExecId)
        executedConfig.selectedDatasource.split(',').each {
            Long dbExConfigId = executedConfig.id
            ExecutedConfiguration dbExConfig = executedConfig
            if(it.equals("faers") && !executedConfig.selectedDatasource.startsWith("faers")) {
                dbExConfigId = childAlertMap.find {it.childExecId == childId}.faersId
                dbExConfig = ExecutedConfiguration.findById(dbExConfigId)
            } else if(it.equals("vaers") && !executedConfig.selectedDatasource.startsWith("vaers")) {
                dbExConfigId = childAlertMap.find {it.childExecId == childId}.vaersId
                dbExConfig = ExecutedConfiguration.findById(dbExConfigId)
            } else if(it.equals("vigibase") && !executedConfig.selectedDatasource.startsWith("vigibase")) {
                dbExConfigId = childAlertMap.find {it.childExecId == childId}.vigibaseId
                dbExConfig = ExecutedConfiguration.findById(dbExConfigId)
            }
            if(it.equals("eudra")) {
                dbExConfigId = childAlertMap.find {it.childExecId == childId}.evdasId
            }
            String integratedFilePath = "${filePath}/${dbExConfigId}_${executedConfig.type}_${it}"
            List<Map> alertDataList = []
            try {
                allFiles << integratedFilePath
                alertDataList = alertService.loadAlertDataFromFile(integratedFilePath)
            } catch (Exception ex) {
                log.error("File not found: " + ex.printStackTrace())
            }

            if(it.equals("eudra")) {
                dbExConfigId = childAlertMap.find {it.childExecId == childId}.exEvdasId
            } else {
                List<Map> prrList = []
                List<Map> abcdList = []
                List<Map> rorList = []
                String prrFilePath = filePath + "/prr"
                String abcdFilePath = filePath + "/abcd"
                String rorFilePath = filePath + "/ror"
                try {
                    if(it == "pva"){
                        prrList = masterExecutorService.getAlertDataFromFile(dbExConfig, prrFilePath, it, allFiles)
                        rorList = masterExecutorService.getAlertDataFromFile(dbExConfig, rorFilePath, it, allFiles)
                        masterExecutorService.setEcPrrData(prrList)
                        masterExecutorService.setEcRorData(rorList)
                    }else{
                        prrList = masterExecutorService.getAlertDataFromFile(dbExConfig, prrFilePath, it, allFiles)
                        masterExecutorService.setEcPrrRorData(prrList)
                    }
                    abcdList = masterExecutorService.getAlertDataFromFile(dbExConfig, abcdFilePath, it, allFiles)
                    masterExecutorService.setEcAbcdData(abcdList)
                    abcdList=[]
                    prrList = []
                } catch (Throwable th) {
                    log.error("Error in saving prr data: " + th.printStackTrace())
                }
            }
            alertDataMap.put(it, alertDataList)
            alertDataList = []
            otherDataSourcesExecIds.put(it, dbExConfigId)

        }
        ExecutionStatus executionStatus = executionStatusList.find { it -> it.executedConfigId == executedConfig.id }
        try {
            Long integratedFaersMasterId = null
            Long integratedVigibaseMasterId = null
            aggregateCaseAlertService.createAlert(executedConfig.configId, executedConfig.id, alertDataMap, otherDataSourcesExecIds, integratedFaersMasterId, allFiles, integratedVigibaseMasterId, executionStatus)
            synchronized (MasterExecutorService.class){
                MasterConfiguration masterConfiguration = MasterConfiguration.findById(masterExecutedConfiguration.masterConfigId)
                //pvs-54057 update of execution status will happen before notification
                reportExecutorService.updateExecutionStatusLevel(executionStatus, null, true)
                masterExecutorService.setSuccessForConfiguration(executedConfig.configId, executedConfig.id, executionStatus.id, isResume, masterConfiguration)
            }
            masterExecutorService.deleteSuccessFiles(allFiles)
            masterExecutorService.deleteDssSuccessFile(executedConfig.id, masterExecId)
            System.gc()

        }
        catch (Throwable th) {
            ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(th))
            synchronized (MasterExecutorService.class) {
                MasterConfiguration masterConfiguration = MasterConfiguration.findById(masterExecutedConfiguration.masterConfigId)
                masterExecutorService.handleFailedExecution(ese, executedConfig.configId, executedConfig.id, executedConfig.type, executionStatus.id, isResume, masterConfiguration)
            }
        }
    }

    List<MasterConfigStatus> getNextMasterForPersist() {
        List<MasterConfigStatus> configs = MasterConfigStatus.createCriteria().list {
            eq("isMiningDone", true)
            eq("isDssDone", true)
            eq("allDbDone", true)
            eq("dataPersisted", false)
            eq("nodeUuid", hazelcastService.hazelcastInstance.cluster.localMember.uuid)
            order('id', 'asc')
        }
        configs?configs:[]
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    void saveChildDone(Long childAlert, Long masterExecId) {
        try {
            String updateQuery = "update master_child_run_node set is_save_done=true"
            updateQuery = updateQuery + " where child_exec_id= "+childAlert+" and master_exec_id in (" + masterExecId + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    void saveMasterDone(Long masterExecId, String nodeUuid) {
        try {
            String updateQuery = "update master_config_status set data_persisted=true"
            updateQuery = updateQuery + " where node_uuid='" +nodeUuid+ "' and master_exec_id in (" + masterExecId + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    def manageMasterChildExecutionQueue(boolean getList = false, boolean getSize = false, boolean clearList = true) {
        if (getList){
            def listData = (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_MASTER_CHILD) : currentlyMasterChildRunning
            return listData
        } else if(getSize) {
            def listSize = (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_MASTER_CHILD).size() : currentlyMasterChildRunning.size()
            return listSize
        } else if(clearList) {
            (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_MASTER_CHILD).clear() : currentlyMasterChildRunning.clear()
        }
    }

    def manageRunningChildExecutionQueue(boolean getList = false, boolean getSize = false, boolean clearList = true) {
        if (getList){
            def listData = (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_CHILD) : currentlyChildRunning
            return listData
        } else if(getSize) {
            def listSize = (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_CHILD).size() : currentlyChildRunning.size()
            return listSize
        } else if(clearList) {
            (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_CHILD).clear() : currentlyChildRunning.clear()
        }
    }

    void updateCriteriaCountsOnExecConfig(Long childAlert) {
        ExecutedConfiguration executedConfig = ExecutedConfiguration.findById(childAlert)
        log.info(executedConfig.criteriaCounts)
        if (executedConfig.masterExConfigId && StringUtils.isBlank(executedConfig.criteriaCounts)) {
            String selectedDataSource = executedConfig.selectedDatasource.split(',')[0]
            log.info("updateCriteriaCountsOnExecConfig : " + executedConfig.id + ", DataSource: " + selectedDataSource)
            reportExecutorService.populateCriteriaSheetCount(executedConfig, selectedDataSource)
            executedConfig.save(flush: true)
            log.info("Updated criteriaCount from executedConfig")
        }
    }

}
