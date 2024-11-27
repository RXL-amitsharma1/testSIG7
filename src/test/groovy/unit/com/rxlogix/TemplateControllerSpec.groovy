package unit.com.rxlogix

import com.rxlogix.Constants
import com.rxlogix.TemplateController
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SignalReport
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SignalReport, User])
@Ignore
class TemplateControllerSpec extends Specification implements ControllerUnitTest<TemplateController> {

    List<SignalReport> signalReportList = []
    AggregateCaseAlert aggregateCaseAlertInstance
    ExecutedConfiguration executedConfigurationInstance

    def setup() {
        aggregateCaseAlertInstance = new AggregateCaseAlert(productName: "Test Product", pt: "Test Event").save(validate: false)
        signalReportList << new SignalReport(reportName: "Test Report", type: 'CUMM', typeFlag: 'SERIOUS_FLAG', alertId: aggregateCaseAlertInstance.id).save(validate: false)
        signalReportList << new SignalReport(reportName: "Test Report2", type: 'NEW', typeFlag: 'SPONT_FLAG', alertId: aggregateCaseAlertInstance.id).save(validate: false)
        executedConfigurationInstance = new ExecutedConfiguration(type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT).save(validate: false)
    }

    @Unroll
    def "test getGeneratedReports method"() {
        given:
        SignalReport.metaClass.static.findAllByReportNameAndAlertIdAndIsGenerating = { String reportName, Long alertId, Boolean isGenerating ->
            return signalReportList
        }
        User.metaClass.static.findByUsername = { String username ->
            return new User(fullName: "Test User", modifiedBy: "Test User", username: "test-user")
        }
        when:
        params.alertId = aggregateCaseAlertInstance.id
        params.configId = executedConfigurationInstance.id
        controller.getGeneratedReports()

        then:
        response.json.size() == signalReportList.size()
        response.json.getAt(0).countType == "Cumm Serious"
        response.json.getAt(1).countType == "New Spon"

    }


}
