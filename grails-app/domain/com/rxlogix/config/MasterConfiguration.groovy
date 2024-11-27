package com.rxlogix.config

import com.rxlogix.user.User
import grails.plugins.orm.auditable.AuditEntityIdentifier

class MasterConfiguration {

    static auditable = false
    @AuditEntityIdentifier
    String name
    String productHierarchy
    Integer lastX
    String dateRangeType
    String asOfVersionDate
    Date startDate
    Date endDate
    String scheduler
    String configTemplate
    Date dateCreated
    Date lastUpdated
    Boolean isEnabled
    Boolean executing
    Boolean isResume
    Integer numOfExecutions
    Date nextRunDate
    User owner
    String scheduleDateJSON
    String datasource
    Long integratedId
    Boolean isAutoPaused = false
    Boolean autoPausedEmailTriggered
    String alertDisableReason
    Boolean isMultiIngredient=false
    Boolean includeWHODrugs = false

    static constraints = {
        name maxSize: 255
        scheduleDateJSON nullable: true
        nextRunDate nullable: true
        numOfExecutions nullable: true
        isResume nullable: true
        executing nullable: true
        isEnabled nullable: true
        configTemplate nullable: true
        scheduler nullable: true
        startDate nullable: true
        endDate nullable: true
        asOfVersionDate nullable: true
        dateRangeType nullable: true
        lastX nullable: true
        productHierarchy nullable: true
        owner nullable: true
        datasource nullable: true
        integratedId nullable: true
        isAutoPaused nullable: true
        autoPausedEmailTriggered nullable: true
        alertDisableReason nullable: true
        isMultiIngredient nullable: true
        includeWHODrugs nullable: true
    }

    static mapping = {
        scheduleDateJSON column: 'SCHEDULE_DATEJSON'
        autoPausedEmailTriggered column: 'AUTO_PAUSED_EMAIL_SENT'
        id generator:'sequence', params:[sequence:'config_sequence']
        includeWHODrugs column: 'INCLUDE_WHO_DRUGS'
    }

    static MasterConfiguration getNextConfigurationToExecute(List<Long> currentlyRunningIds) {
        List<MasterConfiguration> configurationList = MasterConfiguration.createCriteria().list {
            and {
                eq('isEnabled', true)
                eq('executing', false)
                lte('nextRunDate', new Date())
                ne('isAutoPaused', true)

                // next run date check
                if (currentlyRunningIds) {
                    not {
                        'in'('id', currentlyRunningIds)
                    }
                }
            }
            order('nextRunDate', 'asc')
        }
        if (configurationList) {
            List<MasterConfiguration> passedMasterConfigs = []
            List<Configuration> allConfigs = Configuration.findAllByMasterConfigIdInListAndNextRunDateIsNotNull(configurationList*.id)
            configurationList.each {masterConfig ->
                List configs = allConfigs.findAll {it.masterConfigId == masterConfig.id && it.nextRunDate == masterConfig.nextRunDate}
                if(configs)
                    passedMasterConfigs << masterConfig
            }
            if(passedMasterConfigs)
                return passedMasterConfigs.sort {it.nextRunDate}.last()
        }
        return null
    }

    static List<MasterConfiguration> fetchPausedMasterConfigurations(List<Long> currentlyRunningIds) {
        List<MasterConfiguration> configurationList = MasterConfiguration.createCriteria().list {
            and {
                eq('isEnabled', true)
                eq('executing', false)
                lte('nextRunDate', new Date())
                eq('isAutoPaused', true)

                // next run date check
                if (currentlyRunningIds) {
                    not {
                        'in'('id', currentlyRunningIds)
                    }
                }
            }
            order('nextRunDate', 'asc')
        }
        return configurationList
    }

}
