package com.rxlogix.jobs

import com.rxlogix.config.PvsAppConfiguration
import grails.util.Holders

class SpotfireStatusJob {
    def spotfireService
    def concurrent = false
    static triggers = {
        simple repeatInterval: 20000l // execute job once in 20s seconds
    }
    def execute() {
        String serverHostName = "hostname".execute().text.trim()
        PvsAppConfiguration serverHostnameConfig = PvsAppConfiguration.findByKey('serverHostname')
        if(Holders.config.signal.spotfire.enabled && Holders.config.signal.boot.status == true && serverHostName == serverHostnameConfig.stringValue){
            spotfireService.getStatusOfSpotfireFileGeneration()
        }
    }
}
