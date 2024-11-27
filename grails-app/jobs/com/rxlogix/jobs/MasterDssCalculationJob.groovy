package com.rxlogix.jobs

import com.rxlogix.config.MasterConfigStatus

class MasterDssCalculationJob {

    def dssExecutorService
    def concurrent = true
    def masterExecutorService
    def hazelcastService

    static triggers = {
        simple repeatInterval: 20000l // execute job once in 20s seconds
    }

    def execute() {

        List<MasterConfigStatus> masterExec = dssExecutorService.getNextMasterForDSS()
        if(masterExec && dssExecutorService.getDssExecutionQueueSize() < 1) {
            log.info("Found Master id ${masterExec.masterExecId} for DSS calculation")
            String uuid = hazelcastService.hazelcastInstance.cluster.localMember.uuid
            MasterConfigStatus execToRun = masterExec[0]
            Map flagMap = [:]
            execToRun.dssExecuting = true
            masterExecutorService.updateMasterConfigStatus("dss_executing", [execToRun.masterExecId], uuid)

            dssExecutorService.fetchAlertData(execToRun.masterExecId, execToRun.dataSource, flagMap, uuid, execToRun.nodeName, execToRun.faersMasterExId, execToRun.vaersMasterExId, execToRun.vigibaseMasterExId)
        }

    }

}
