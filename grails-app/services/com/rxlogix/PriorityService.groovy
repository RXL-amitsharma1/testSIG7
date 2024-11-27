package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.config.PriorityDispositionConfig
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import grails.converters.JSON
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class PriorityService {
    def cacheService

    /**
     * Method : savePriority. This method saves the priority in the transactional block.
     * It also ensures that if default priority value is coming as true, it modifies the
     * same value for others to false.
     * @param priorityInstance
     * @return
     */
    def savePriority(Priority priorityInstance) {
        if (priorityInstance.defaultPriority) {
            def curDefPriorities = Priority.findAllByDefaultPriority(true)

            if (curDefPriorities) {
                curDefPriorities.each {
                    if (it != priorityInstance) {
                        it.defaultPriority = false
                        it.save()
                    }
                }
            }
        }
        priorityInstance.save()

        cacheService.updatePriorityCache(priorityInstance)

        if (priorityInstance.hasErrors() || !priorityInstance.save()) {
            throw new ValidationException("Validation error", priorityInstance.errors)
        }
    }

    def listPriorityOrder() {
        Priority.createCriteria().list {
            projections {
                property 'id'
                property 'value'
                property 'priorityOrder'
                property 'iconClass'
                property 'reviewPeriod'
            }
            eq('display', true)
        }
    }

    List<Map> listPriorityAdvancedFilter(){
        Map<Long,Priority> priorityMap = cacheService.getPriorityCacheMap()
        List<Map> priorityList = []
        priorityMap.each {
            if(it.value.display){
                priorityList.add([id:it.key,text:it.value.displayName])
            }

        }
        priorityList
    }

    void saveDispositionConfig(Map params , Priority priorityInstance) {

        List dispositionList = JSON.parse(params.dispositions)

        List<PriorityDispositionConfig> priorityDispositionConfigList = PriorityDispositionConfig.createCriteria().list {
            'priority' {
                'eq'("id", priorityInstance?.id)
            }
        }
        dispositionList.each { dispositionMap ->
            PriorityDispositionConfig config = priorityDispositionConfigList.find {
                it.disposition == Disposition.findByDisplayName(dispositionMap.displayName)
            }
            if (config) {
                config.reviewPeriod = Integer.parseInt(dispositionMap.reviewPeriod)
                config.save()
            } else {
                config = new PriorityDispositionConfig()
                config.priority = priorityInstance
                config.disposition = Disposition.findByDisplayName(dispositionMap.displayName)
                config.reviewPeriod = Integer.parseInt(dispositionMap.reviewPeriod)
                config.dispositionOrder = dispositionMap.order
                priorityInstance.addToDispositionConfigs(config)
                config.save()
            }
        }
        priorityDispositionConfigList.each {config->
            if (!dispositionList.find{it.displayName == config.disposition.displayName}) {
                priorityInstance.removeFromDispositionConfigs(config)
                config?.delete()
            }
        }
    }

   List fetchDispositionConfigList(Priority priorityInstance) {
        List<PriorityDispositionConfig> dispositionConfigList = new ArrayList(priorityInstance.dispositionConfigs)
        List configs = []
        dispositionConfigList.groupBy { it.dispositionOrder }.each { key, value ->
            List list = []
            value.each {
                list.add([displayName: it.disposition.displayName, reviewPeriod: it.reviewPeriod])
            }
            configs.add(list)
        }
        return configs
   }

    String fetchDispositionConfigListForAudit(Priority priorityInstance) {
        List<PriorityDispositionConfig> dispositionConfigList = new ArrayList(priorityInstance.dispositionConfigs)
        List configs = []
        dispositionConfigList.groupBy { it.dispositionOrder }.each { key, value ->
            value.each {
                configs.add("$it.disposition.displayName:$it.reviewPeriod")
            }
        }
        return configs.join(',')
    }
}
