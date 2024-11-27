package unit.com.rxlogix.util


import spock.lang.Specification
import com.rxlogix.util.SpotfireUtil

class SpotfireUtilSpec extends Specification {
    def server
    Integer port
    def protocol
    String automationUsername
    String automationPassword
    String ntlmAcct
    String ntlmPass

    def setup() {
        server = '10.100.6.8'
        port = 443
        protocol = 'https'
        automationUsername = 'admin'
        automationPassword = 'admin'
        ntlmAcct = 'spotfiretest$@rxlogix.com'
        ntlmPass = 'rxlogix1!'
    }

    def "test for generateAutomationXml"() {
        setup:
        File folder = new File("test-folder")
        folder.mkdir()
        (0..3).each { new File(folder, "job-${it}.xml").write("test")}

        SpotfireUtil.generateAutomationXml(folder, "no-test")

        expect:
        File theFile = new File(folder, 'job-5.xml')
        theFile.exists()
        cleanup:
        folder.deleteDir()
    }
}
