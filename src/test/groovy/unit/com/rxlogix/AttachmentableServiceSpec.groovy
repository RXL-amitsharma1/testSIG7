package unit.com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.attachments.AttachmentableService
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import groovy.transform.SourceURI
import com.rxlogix.exception.FileFormatException
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths


class AttachmentableServiceSpec extends Specification implements ServiceUnitTest<AttachmentableService> {

    ValidatedSignal validatedSignal
    User user
    Priority priority
    Disposition defaultSignalDisposition
    Group wfGroup
    File uploadFolder
    File file
    String directory

    def setup() {
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultSignalDisposition,
                defaultQuantDisposition: defaultSignalDisposition,
                defaultAdhocDisposition: defaultSignalDisposition,
                defaultEvdasDisposition: defaultSignalDisposition,
                defaultLitDisposition: defaultSignalDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: defaultSignalDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush:true)
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)
        priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        validatedSignal = new ValidatedSignal(name: "test_name", products: "test_products", endDate: new Date(),
                assignedTo: user, assignmentType: 'USER', modifiedBy: user.username, priority: priority,
                disposition: defaultSignalDisposition, createdBy: user.username, startDate: new Date(), id: 1,
                genericComment: "Test notes", workflowGroup: wfGroup, sharedGroups: wfGroup, productDictionarySelection:
                "productDictionarySelection1")
        validatedSignal.save(flush: true)

        uploadFolder = new File(config.grails.attachmentable.uploadDir)
        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        directory = scriptLocation.toString().replace("AttachmentableServiceSpec.groovy", "testingFiles/Details.html")
        file=new File(directory)
    }

    void "test checkExtension() with exception"() {
        given:
        String fileName = "abc.txt.csv"

        when:
        service.checkExtension(fileName)

        then:
        noExceptionThrown()
    }

    void "test checkExtension() without exception"() {
        given:
        String fileName = "abc.csv"

        when:
        service.checkExtension(fileName)

        then:
        noExceptionThrown()

    }

    void "test checkType() with exception"() {
        given:
        File file = new File(directory)

        when:
        service.checkType(file)

        then:
        thrown FileFormatException

    }

    void "test uploadAssessmentReport"(){
        when:
        service.uploadAssessmentReport(user,validatedSignal,file,"file.pdf")
        then:
        noExceptionThrown()

    }

}
