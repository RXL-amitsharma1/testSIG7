package unit.com.rxlogix

import com.rxlogix.Constants
import com.rxlogix.DataAnalysisController
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class DataAnalysisControllerSpec extends Specification implements ControllerUnitTest<DataAnalysisController> {

    ExecutedConfiguration executedConfiguration
    User user

    def setup() {

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.grailsApplication = grailsApplication
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"

        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        executedConfiguration = new ExecutedConfiguration(id:2L,name: "test",
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
                type: Constants.AlertConfigType.SINGLE_CASE_ALERT,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)

        executedConfiguration.save(failOnError: true)
    }

    void "test for the create method"() {

        setup:
        controller.userService = [getUser : { user }]
        controller.spotfireService = [getHashedValue: { "username" }]
        controller.params.selectedCaseSeries = ['1', '2', '3']

        when:
        def model = controller.create()

        then:
        response.status == 200
        assert model.selectedCaseSeries  == ['1', '2', '3']
        assert model.user_name == "username"
        assert model.executedConfigs == [executedConfiguration]
        assert model.selectedCaseSeries != ['1', '2', '4']
        assert model.user_name != "username1"
        assert model.executedConfigs != []
        assert model.lmProductFamilies == []
    }
}