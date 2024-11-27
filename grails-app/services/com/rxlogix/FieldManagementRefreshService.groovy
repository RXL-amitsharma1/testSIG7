package com.rxlogix

import grails.util.Holders


class FieldManagementRefreshService {

    def cacheService
    def dataSource_pva

    def processLabelMessage(List data) {
        try {
            log.info("Going to refresh ICR label cache.")
            Boolean cacheUpdateStatus = false
            Integer retryCount = Holders.config.hazelcast.publish.retry.count ?: 10
            while (!cacheUpdateStatus && retryCount > 0) {
                println "cache update status : " + cacheUpdateStatus
                cacheUpdateStatus = cacheService.updateUiLabelCacheForSafety(data)
                if (!cacheUpdateStatus) {
                    Thread.sleep(60000)
                    log.info("waiting 1 minute before retrying cache refresh...")
                }
                retryCount--
            }
            log.info("ICR Label cache refresh complete.")
        } catch (Exception exception) {
            log.error("ERROR: " + exception.printStackTrace())
        }

    }

}