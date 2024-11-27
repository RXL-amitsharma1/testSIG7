package unit.com.rxlogix

import com.rxlogix.EvdasCaseListingImportService
import com.rxlogix.EvdasDataController
import com.rxlogix.EvdasDataImportService
import com.rxlogix.EvdasDataService
import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Ignore
class EvdasDataControllerSpec extends Specification implements ControllerUnitTest<EvdasDataController> {

    User userInstance
    EvdasFileProcessLog evdasFileProcessLogInstance

    def setup() {
        userInstance = new User(username: "Test User")
        userInstance.save(validate: false)

        evdasFileProcessLogInstance = new EvdasFileProcessLog(fileName: "Some File Name")
        evdasFileProcessLogInstance.save(validate: false)
    }

    void "test the index action returns the correct model"() {
        given:
        List<String> substanceNames = ['Substance1', 'Substance2', 'Substance3', 'Substance4']
        controller.evdasCaseListingImportService = [listAllSubstanceName: { substanceNames }]

        when: 'The index action is executed'
        def model = controller.index()

        then:
        model.substanceNames
        model.substanceNames.size() == 4
        model.substanceNames.find { it == 'Substance1' }
    }

    @Unroll
    void "test the eRMR/caseListing file upload flow"() {
        given:
        params.description = description
        params.dataType = dataType
        params.optDuplicate = optDuplicate
        params.substanceName = substanceName

        def file = new GrailsMockMultipartFile('file', 'someData'.bytes)
        request.addFile(file)
        controller.userService = [getUser: { return userInstance }]

        def evdasCaseListingImportServiceMocked = Mock(EvdasCaseListingImportService)
        evdasCaseListingImportServiceMocked.filterFilesForDataImport(_) >> ['pass': ['someFileName'], 'fail': ['someFileName']]
        evdasCaseListingImportServiceMocked.filterSourceFolder(_, _, _, _, _) >> null
        controller.evdasCaseListingImportService = evdasCaseListingImportServiceMocked

        def evdasDataImportServiceMocked = Mock(EvdasDataImportService)
        evdasDataImportServiceMocked.filterFilesForDataImport(_, _) >> ['pass': ['someFileName'], 'fail': ['someFileName']]
        evdasDataImportServiceMocked.filterSourceFolder(_, _, _, _, _, _) >> null
        controller.evdasDataImportService = evdasDataImportServiceMocked

        when:
        controller.upload(description, dataType, optDuplicate, substanceName)

        then:
        caseListingServiceMethodCount * controller.evdasCaseListingImportService.filterFilesForDataImport(_)
        caseListingServiceMethodCount * controller.evdasCaseListingImportService.filterSourceFolder(_, _, _, _, _)
        ermrServiceMethodCount * controller.evdasDataImportService.filterFilesForDataImport(_, _)
        ermrServiceMethodCount * controller.evdasDataImportService.filterSourceFolder(_, _, _, _, _, _)


        where:
        description          | dataType       | optDuplicate | substanceName          | caseListingServiceMethodCount | ermrServiceMethodCount
        "Random Description" | "Case Listing" | null         | "Random SubstanceName" | 1                             | 0
        "Random Description" | "eRMR"         | 0            | "Random SubstanceName" | 0                             | 1
    }

    void "test the fetch evdas data action"() {
        given:
        def evdasDataServiceMocked = Mock(EvdasDataService)
        evdasDataServiceMocked.fetchFileProcessLog() >> [['prop1': 'value1']]
        controller.evdasDataService = evdasDataServiceMocked

        when:
        controller.fetchEvdasData()

        then:
        1 * controller.evdasDataService.fetchFileProcessLog()
    }
}