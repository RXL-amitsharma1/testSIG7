package unit.com.rxlogix

import com.rxlogix.DynamicReportService
import com.rxlogix.action.ActionService
import com.rxlogix.config.ActionController
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */

@Ignore
class ActionControllerSpec extends Specification implements ControllerUnitTest<ActionController> {

    @ConfineMetaClassChanges([ActionController])
    def "test for export action details"() {

        given:
        Boolean renderReportOutputTypeCalled = false
        File tempFile = new File("abc.pdf")
        def actionServiceMocked = Mock(ActionService)
        actionServiceMocked.listActionsForAlert(_, _) >> [['name': 'test'], ['name': 'test1']]

        def dynamicReportServiceMocked = Mock(DynamicReportService)
        dynamicReportServiceMocked.createActionsReport(_, _, _) >> tempFile.createNewFile()

        controller.actionService = actionServiceMocked
        controller.dynamicReportService = dynamicReportServiceMocked
        ActionController.metaClass.renderReportOutputType = { File reportFile ->
            renderReportOutputTypeCalled = true
        }

        when:
        controller.exportActionsReport()

        then:
        1 * controller.actionService.listActionsForAlert(_, _)
        1 * controller.dynamicReportService.createActionsReport(_, _, _)
        renderReportOutputTypeCalled
        tempFile.delete()
    }
}