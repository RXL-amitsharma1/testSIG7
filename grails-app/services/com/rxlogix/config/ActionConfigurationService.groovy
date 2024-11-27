package com.rxlogix.config

import grails.gorm.transactions.Transactional

@Transactional
class ActionConfigurationService {

    def saveActionConfiguration(ActionConfiguration ac) {
        ac.validate()
        ac.save(failOnError: true, flush: true)
    }

    def deleteActionConfiguration(ActionConfiguration ac) {
        ac.delete()
    }
}
