package com.rxlogix.jobs

import com.rxlogix.CommonTagService
import com.rxlogix.config.PvsAppConfiguration
import grails.util.Holders
class AutoAlertJob {

    def autoAlertService
    CommonTagService commonTagService
    def CRUDService
    def concurrent = false

    static triggers = {
        cron startDelay: 300000l, cronExpression: Holders.config.auto.alert.job.cron.exp // execute job once in 8 mins after the delay of 5 mins.
    }

    def execute() {
        String serverHostName = "hostname".execute().text.trim()
        log.info('Hostname of the server : ' + serverHostName)
        PvsAppConfiguration serverHostnameConfig = PvsAppConfiguration.findByKey('serverHostname')
        if(serverHostnameConfig?.stringValue == serverHostName){
            PvsAppConfiguration autoAlertJobConfig = PvsAppConfiguration.findByKey('autoAlertJobRunning')
            if(!autoAlertJobConfig?.booleanValue){
                autoAlertJobConfig.skipAudit = true
                autoAlertJobConfig.booleanValue = true
                autoAlertJobConfig.save(flush:true)
                try{
                    log.info("Auto Alert Job Started.")
                    autoAlertService.addCasesToAlert()
                    commonTagService.syncETLCasesWithCategories()
                    log.info("Auto Alert Job Ended.")
                } catch (Exception ex){
                    ex.printStackTrace()
                } finally {
                    autoAlertJobConfig.booleanValue = false
                    autoAlertJobConfig.skipAudit = true
                    autoAlertJobConfig.save(flush:true)
                }

            }else{
                log.info("Auto Alert Job Already In Progress")
            }
        } else {
            log.info('Auto Alert Job Started on the server with Hostname:' + serverHostName)
        }
    }

}
