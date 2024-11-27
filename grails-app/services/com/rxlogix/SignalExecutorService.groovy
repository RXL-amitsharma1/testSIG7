package com.rxlogix

import grails.util.Holders

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/* This service will create the single instance of thread pools which will be used throughout the lifecycle of application.
   Earlier new thread pool is created for every request which require parallel programming due to that resource consumption is high.
*/
class SignalExecutorService {

    ExecutorService qualAlertExecPool
    ExecutorService quantAlertExecPool
    ExecutorService quantListPool
    ExecutorService evdasListPool
    ExecutorService qualListPool
    ExecutorService literatureListPool
    ExecutorService caseDetailPool
    ExecutorService caseSeriesPool

    //TODO This should be driven from number of processors
    Integer getPoolSize() {
        return 6
    }

    ExecutorService threadPoolForQualAlertExec() {
        if (!qualAlertExecPool) {
            qualAlertExecPool = Executors.newFixedThreadPool(getPoolSize())
        }
        qualAlertExecPool
    }

    ExecutorService threadPoolForQuantAlertExec() {
        if (!quantAlertExecPool) {
            quantAlertExecPool = Executors.newFixedThreadPool(getPoolSize())
        }
        quantAlertExecPool
    }

    ExecutorService threadPoolForQuantListExec() {
        if (!quantListPool) {
            quantListPool = Executors.newFixedThreadPool(getPoolSize())
        }
        quantListPool
    }

    ExecutorService threadPoolForEvdasListExec() {
        if (!evdasListPool) {
            evdasListPool = Executors.newFixedThreadPool(getPoolSize())
        }
        evdasListPool
    }

    ExecutorService threadPoolForQualListExec() {
        if (!qualListPool) {
            qualListPool = Executors.newFixedThreadPool(getPoolSize())
        }
        qualListPool
    }

    ExecutorService threadPoolForLitListExec() {
        if (!literatureListPool) {
            literatureListPool = Executors.newFixedThreadPool(getPoolSize())
        }
        literatureListPool
    }
    ExecutorService threadPoolForCaseDetail() {
        if (!caseDetailPool) {
            caseDetailPool = Executors.newFixedThreadPool(Holders.config.signal.casedetail.thread.pool)
        }
        caseDetailPool
    }

    ExecutorService threadPoolForCaseSeriesExec() {
        if (!caseSeriesPool) {
            caseSeriesPool = Executors.newFixedThreadPool(getPoolSize())
        }
        caseSeriesPool
    }

}
