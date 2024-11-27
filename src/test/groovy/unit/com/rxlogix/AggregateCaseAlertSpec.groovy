package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.user.User
import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class AggregateCaseAlertSpec extends Specification implements DomainUnitTest<AggregateCaseAlert> {

    User user
    ExecutedConfiguration executedConfiguration
    Configuration configuration
    Disposition disposition

    def setup() {

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal")
        disposition.save(failOnError: true)

        //Save the  user
        user = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.grailsApplication = grailsApplication
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = {"Fake Namer"}
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        executedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)

        executedConfiguration.save(failOnError: true)

        configuration = new Configuration(assignedTo: user, productSelection : "[TestProduct]", name: "test",
                owner: user, createdBy: user.username, modifiedBy: user.username, priority: new Priority(value: "High"))
        configuration.save(failOnError: true)


    }

    void "test the Aggregate Alert persistence"() {
        setup:
        def aca = new AggregateCaseAlert(alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: configuration.priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "PRODUCT_NAME0",
                productId: 12321312,
                soc:  "BODY_SYS1",
                pt: 'TEST',
                ptCode: 1421,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                newSponCount: 1,
                cumSponCount: 1,
                newSeriousCount: 1,
                cumSeriousCount: 1,
                newFatalCount: 1,
                cumFatalCount: 1,
                prrValue: 1,
                rorValue: 1,
                createdBy: configuration.assignedTo.username,
                modifiedBy: configuration.assignedTo.username,
                dateCreated: executedConfiguration.dateCreated,
                lastUpdated: executedConfiguration.dateCreated,
                eb05: new Double(1),
                eb95: new Double(1),
                ebgm: new Double(2),
                dueDate: new Date(),
                pregenency: "test",
                related: "test",
                positiveDechallenge: "test",
                listed: "test",
                positiveRechallenge: "test"
        )
        aca.save(failOnError: true)

        expect:
        aca.id != null
    }
}
