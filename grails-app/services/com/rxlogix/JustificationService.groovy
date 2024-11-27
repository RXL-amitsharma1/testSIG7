package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.signal.ActionJustification
import com.rxlogix.signal.Justification
import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class JustificationService {

    def userService
    def alertService

    void bindDispositions(Justification justification, def dispositions) {
        if (dispositions) {
            if (justification?.dispositions) {
                justification.dispositions.clear()
            }
            if (dispositions.getClass() == String) {
                dispositions = [dispositions]
            }

            dispositions?.collect { it }.each {
                Disposition disposition = Disposition.get(it)
                if (disposition) {
                    justification.addToDispositions(disposition)
                }
            }
        } else if (!dispositions && justification?.dispositions) {
            justification.dispositions.clear()
        }
    }

    List<String> fetchJustificationsForDisposition(Long dispositionId, Boolean signalWorkFlow) {
        Disposition disposition = Disposition.findById(dispositionId)
        List<String> justifications = []
        if (disposition) {
            Justification.list().each {
                if (signalWorkFlow) {
                    if (it?.dispositions?.contains(disposition) && it.feature.contains(JustificationFeatureEnum.signalWorkflow.toString() + '": "on"')) {
                        justifications.add(it.justification)
                    }
                } else {
                    if (it?.dispositions?.contains(disposition) && it.feature.contains(JustificationFeatureEnum.alertWorkflow.toString() + '": "on"')) {
                        justifications.add(it.justification)
                    }
                }
            }
        }
        justifications.sort({it.toUpperCase()})
    }

    List<Map> fetchJustificationsForDispositionForBR(Long dispositionId) {
        Disposition disposition = Disposition.findById(dispositionId)
        List<Map> justifications = []
        if (disposition) {
            Justification.list().each {
                if (it?.dispositions?.contains(disposition)) {
                    justifications.add([id: it.id, text: it.justification])
                }
            }
        }
        justifications?.sort({it.text.toUpperCase()})
    }

/**
 * This Method saves the value of justification while performing action on any type of configuration
 * or validated signal.
 * @param objectIds : Primary key of the objects being modified.
 * @param alertType : Alert type of configurations or Validated Signal
 * @param actionType : Type of action being performed
 * @param justification : Justification added by the end user from UI
 */
    def saveActionJustification(List<Long> objectIds, String alertType, String actionType, String justification, List<Map> instancesInfo = null) {
        String currentUserName = userService.getUser().fullName
        def domain = alertService.getDomainObjectByAlertType(alertType)
        String posterClassName = alertService.getPosterClassName(alertType)
        List<Map> instancesInfoMap = instancesInfo ?: prepareDataForPersistingJustification(objectIds, domain)
        Map data = ["INSTANCES_INFO": instancesInfoMap]
        ActionJustification actionJustification = new ActionJustification(actionType, justification, posterClassName, data, currentUserName)
        actionJustification.save(flush: true)
    }

    List<Map> prepareDataForPersistingJustification(List<Long> objectIds, def domain) {
        List<Map> data = domain.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property('id', 'id')
                property('name', 'name')
            }
            'in'("id", objectIds)
        } as List<Map>

        return data

    }

    List<Map> prepareDataForMasterConfigJustification(List<Long> objectIds) {
        List<Map> data = Configuration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property('id', 'id')
                property('name', 'name')
            }
            'in'("masterConfigId", objectIds)
        } as List<Map>

        return data

    }

}

