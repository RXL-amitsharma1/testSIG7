package com.rxlogix.jobs

import com.rxlogix.Constants
import grails.util.Holders

class MoveAggregateConfigToQueueJob {

    def aggregateExecutorService
    def dataObjectService
    static triggers = {
        simple startDelay: 300000l, repeatInterval: 30000l // execute job once in 20s seconds
    }

    def execute() {
        if (Holders.config.signal.boot.status && dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)){
//            aggregateExecutorService.initiateMovingAggConfigToQueue()
        }
    }
}
