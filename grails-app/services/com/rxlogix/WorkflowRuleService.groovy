package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional
import net.sf.dynamicreports.report.builder.group.Groups
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class WorkflowRuleService {

    def userService
    def cacheService
    def utilService

    Boolean saveWorkflowRule(DispositionRule dispositionRule) {
         dispositionRule.save(flush:true)
    }

    Map fetchDispositionIncomingOutgoingMap() {
        User user = cacheService.getUserByUserNameIlike(userService.getCurrentUserName())
        Set<Group> groups = utilService.fetchUserListIdForGroup( user, GroupType.USER_GROUP )
        List<Map> dispositionRules = groups ? DispositionRule.createCriteria().listDistinct {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections{
                property("approvalRequired","approvalRequired")
                'incomingDisposition'{
                    property("displayName","incomingDispDisplayName")
                }
                'targetDisposition'{
                    property("id","id")
                    property("displayName","displayName")
                    property("abbreviation","abbreviation")
                    property("colorCode","colorCode")
                    property("validatedConfirmed","validatedConfirmed")
                    property("closed", "closed")
                    property("reviewCompleted", "reviewCompleted")
                }
            }

            eq('display', true)
            eq('isDeleted', false)
            'workflowGroups' {
                eq('id', user.workflowGroup?.id)
            }

            'allowedUserGroups' {
                inList('id', groups)
            }
        } as List<Map> : []

        dispositionRules.unique {
            [it.incomingDispDisplayName,it.id]
        }

        dispositionRules.groupBy {
            it.incomingDispDisplayName
        }.collectEntries { key, val ->
            [(key): val.collect {
                [displayName: it.displayName, abbreviation: it.abbreviation, colorCode: it.colorCode, id: it.id, validatedConfirmed: it.validatedConfirmed, isApprovalRequired : it.approvalRequired, dispositionClosedStatus: it.closed, isReviewed : it.reviewCompleted]
            }]
        }
    }

    Map fetchDispositionData() {
        def dispositionData = Disposition.list()
        Map mapValues = [:]
        dispositionData.each{
            mapValues.put(it.displayName, ["validatedConfirmed":it.validatedConfirmed, "reviewCompleted":it.reviewCompleted,
                                   "closed": it.closed])
        }
        mapValues
    }

}
