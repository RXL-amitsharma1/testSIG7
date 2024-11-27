package com.rxlogix

import com.rxlogix.mapping.EtlCaseTableStatus
import com.rxlogix.mapping.EtlScheduleResult
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class EtlCaseTableStatusController {

    @Secured(['ROLE_ADMIN'])
    def index() {
        def etlCaseTableStatusInstanceList = []
        def count = 0
        try {
            params.max = Constants.Search.MAX_SEARCH_RESULTS
            EtlCaseTableStatus.withTransaction {
                count = EtlCaseTableStatus.countByStageStartTimeIsNotNull()
            }
            EtlScheduleResult.withTransaction {
                etlCaseTableStatusInstanceList = new EtlCaseTableStatus().selectableQuery.list(params)
            }
        } catch (Exception ex) {
            log.error(ex.getMessage())
        }
        render view: "index", model: [etlCaseTableStatusInstanceList: etlCaseTableStatusInstanceList, etlCaseTableStatusInstanceTotal: count]
    }
}
