package com.rxlogix.config

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class AlertDeletionDataServiceSpec extends Specification {

    AlertDeletionDataService alertDeletionDataService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new AlertDeletionData(...).save(flush: true, failOnError: true)
        //new AlertDeletionData(...).save(flush: true, failOnError: true)
        //AlertDeletionData alertDeletionData = new AlertDeletionData(...).save(flush: true, failOnError: true)
        //new AlertDeletionData(...).save(flush: true, failOnError: true)
        //new AlertDeletionData(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //alertDeletionData.id
    }

    void "test get"() {
        setupData()

        expect:
        alertDeletionDataService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<AlertDeletionData> alertDeletionDataList = alertDeletionDataService.list(max: 2, offset: 2)

        then:
        alertDeletionDataList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        alertDeletionDataService.count() == 5
    }

    void "test delete"() {
        Long alertDeletionDataId = setupData()

        expect:
        alertDeletionDataService.count() == 5

        when:
        alertDeletionDataService.delete(alertDeletionDataId)
        sessionFactory.currentSession.flush()

        then:
        alertDeletionDataService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        AlertDeletionData alertDeletionData = new AlertDeletionData()
        alertDeletionDataService.save(alertDeletionData)

        then:
        alertDeletionData.id != null
    }
}
