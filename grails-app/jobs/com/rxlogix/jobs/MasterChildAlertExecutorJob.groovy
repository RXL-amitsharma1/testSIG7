package com.rxlogix.jobs

import com.rxlogix.Constants
import com.rxlogix.config.AlertType
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.MasterConfigStatus
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.config.MasterExecutedConfiguration
import com.rxlogix.config.ReportExecutionStatus
import grails.util.Holders

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class MasterChildAlertExecutorJob {

    def masterExecutorService
    def concurrent = true
    def childExecutorService
    def dssExecutorService
    def dataObjectService
    def hazelcastService
    def alertService
    def cacheService
    static triggers = {
        simple repeatInterval: 10000l // execute job once in 10s seconds
    }

    def execute() {
        String localUuid = hazelcastService.hazelcastInstance.cluster.localMember.uuid
        List<MasterConfigStatus> masterExec = childExecutorService.getNextMasterForPersist()
        Long masterToRun = masterExec ? masterExec[0].masterExecId : null
        Boolean isResume = false
        MasterConfiguration masterConfiguration = null
        if(!masterToRun){
            masterConfiguration = MasterConfiguration.findByIsResumeAndNextRunDateLessThanEquals(true, new Date()) //master config
            masterToRun = ExecutionStatus.findByConfigIdAndExecutionStatusAndIsMaster(masterConfiguration?.id, ReportExecutionStatus.SCHEDULED, true)?.executedConfigId
            isResume = true
        }
        if (masterToRun) {
            log.info("Found Master id ${masterToRun} for saving child")
            if (childExecutorService.manageMasterChildExecutionQueue(false, true, false) < 1) {
                childExecutorService.manageMasterChildExecutionQueue(true, false, false).add(masterToRun)
            }
            try {
                if (childExecutorService.manageRunningChildExecutionQueue(false, true, false) < 4) {
                    List<Long> childAlertList
                    List<Map> childAlertMap
                    if(isResume){
                        log.info("Found Master id ${masterToRun} for saving child : Resume")
                        List configIdList = Configuration.findAllByMasterConfigIdAndNextRunDate(masterConfiguration.id, masterConfiguration.nextRunDate)*.id
                        if(!configIdList.isEmpty()){
                            childAlertList = childExecutorService.getResumeChildAlertToSave(masterToRun, configIdList).childAlerts
                            childAlertMap = childExecutorService.getResumeChildAlertToSave(masterToRun, configIdList).childAlertMap
                        }
                    } else {
                        childAlertList = childExecutorService.getChildAlertToSave(masterToRun).childAlerts
                        childAlertMap = childExecutorService.getChildAlertToSave(masterToRun).childAlertMap
                    }
                    Long childAlert = childAlertList ? childAlertList[0] : null
                    log.info("Found Child id for saving :  ${childAlert}")

                    List<ExecutionStatus> executionStatusList = []
                    ExecutionStatus executionStatus = null
                    if (childAlert) {
                        childExecutorService.manageRunningChildExecutionQueue(true, false, false).add(childAlert)
                        executionStatusList = ExecutionStatus.findAllByExecutedConfigIdInList([childAlert])
                        if(isResume){
                            executionStatus= ExecutionStatus.findByExecutedConfigIdAndExecutionStatus(childAlert,ReportExecutionStatus.SCHEDULED)
                            if(executionStatus){
                                executionStatus.executionStatus = ReportExecutionStatus.GENERATING
                                executionStatus.save(flush:true)
                            }
                        }
                        try {
                            childExecutorService.persistChildAlert(childAlert, masterToRun, executionStatusList, isResume, childAlertMap)
                            childExecutorService.saveChildDone(childAlert, masterToRun)
                            childExecutorService.updateCriteriaCountsOnExecConfig(childAlert)
                        } catch (Exception ex) {
                            executionStatus?.executionStatus = ReportExecutionStatus.ERROR
                            executionStatus?.save(flush:true)
                            log.error(ex.printStackTrace())
                        } finally {
                            childExecutorService.manageRunningChildExecutionQueue(true, false, false).remove(childAlert)
                        }
                    } else {
                        log.info("No more child alert to run.")
                        childExecutorService.manageMasterChildExecutionQueue(true, false, false).remove(masterToRun)
                        childExecutorService.saveMasterDone(masterToRun, localUuid)
                        masterExecutorService.updateChildDoneStatus(localUuid, masterToRun)
                        if (masterConfiguration && masterConfiguration.isResume){   //set master is resume false
                            cacheService.clearPartialAlertCache(masterConfiguration.id)
                        }
                    }
                } else {
                    log.info("Child alert running for: " + childExecutorService.manageRunningChildExecutionQueue(true, false, false))
                }
            } catch (Exception ex) {
                log.error("Master child alerts failed while persisting: ", ex)
            } finally {
                childExecutorService.manageMasterChildExecutionQueue(true, false, false).remove(masterToRun)
                if(masterConfiguration)
                    cacheService.clearPartialAlertCache(masterConfiguration?.id)

            }

        } else if(childExecutorService.manageRunningChildExecutionQueue(true, false, false)) {
            log.info("Child alerts are in progress for " + childExecutorService.manageRunningChildExecutionQueue(true, false, false))
        }
    }

}