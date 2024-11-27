package com.rxlogix


import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.time.DateUtils
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class OnDemandAlertDeletionService {

    def grailsApplication
    def singleOnDemandAlertService
    def aggregateOnDemandAlertService
    def evdasOnDemandAlertService

    void startDeletingOnDemandAlert() {
        try {
            log.info("Job for deleting the On Demand Alert Starts")
            Integer timespan = grailsApplication.config.signal.timespan.deletion.ondemand.alert
            deletingSingleOnDemandAlert(timespan)
            deletingAggregateOnDemandAlert(timespan)
            deletingEvdasOnDemandAlert(timespan)
        } catch (Throwable th) {
            log.error(th.getMessage(), th)
        }
    }

    void deletingSingleOnDemandAlert(Integer timespan) {
        List<Map> onDemandConfigurationList = Configuration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("dateCreated", "dateCreated")
            }
            eq("isDeleted", false)
            eq('adhocRun', true)
            eq("type", Constants.AlertConfigType.SINGLE_CASE_ALERT)
        } as List<Map>

        onDemandConfigurationList.each { Map configDataMap ->
            if ((DateUtils.truncate(configDataMap.dateCreated, Calendar.DAY_OF_MONTH)) <= (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH) - timespan)) {
                List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllByConfigId(configDataMap.id as Long)
                if(executedConfigurationList){
                    executedConfigurationList.each {
                        singleOnDemandAlertService.deleteOnDemandAlert(it)
                    }
                }
            }
        }
    }

    void deletingAggregateOnDemandAlert(Integer timespan) {
        List<Map> onDemandConfigurationList = Configuration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("dateCreated", "dateCreated")
            }
            eq("isDeleted", false)
            eq('adhocRun', true)
            eq("type", Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        } as List<Map>

        onDemandConfigurationList.each { Map configDataMap ->
            if ((DateUtils.truncate(configDataMap.dateCreated, Calendar.DAY_OF_MONTH)) <= (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH) - timespan)) {
                List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllByConfigId(configDataMap.id as Long)
                if(executedConfigurationList){
                    executedConfigurationList.each {
                        aggregateOnDemandAlertService.deleteOnDemandAlert(it)
                    }
                }
            }
        }
    }

    void deletingEvdasOnDemandAlert(Integer timespan) {
        List<Map> onDemandConfigurationList = EvdasConfiguration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("dateCreated", "dateCreated")
            }
            eq("isDeleted", false)
            eq('adhocRun', true)
        } as List<Map>

        onDemandConfigurationList.each { Map configDataMap ->
            if (DateUtils.truncate(configDataMap.dateCreated, Calendar.DAY_OF_MONTH)<= (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH) - timespan)) {
                List<ExecutedEvdasConfiguration> executedEvdasConfigurationList = ExecutedEvdasConfiguration.findAllByConfigId(configDataMap.id as Long)
                if(executedEvdasConfigurationList){
                    executedEvdasConfigurationList.each {
                        evdasOnDemandAlertService.deleteOnDemandAlert(it)
                    }
                }
            }
        }
    }
}
