package unit.com.rxlogix

import com.rxlogix.ApplicationSettingsService
import com.rxlogix.EvdasCaseListingImportService
import com.rxlogix.EvdasDataImportService
import com.rxlogix.config.EvdasApplicationSettings
import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class EvdasCaseListingImportServiceSpec extends Specification implements ServiceUnitTest<EvdasCaseListingImportService> {

    User userInstance
    File readFolder
    File uploadFolder
    File processSuccessDir
    File processFailDir

    def setup() {
        userInstance = new User(username: "Test User")
        userInstance.save(validate: false)


        readFolder = new File(config.signal.evdas.case.line.listing.import.folder.read as String)
        uploadFolder = new File(config.signal.evdas.case.line.listing.import.folder.upload as String)
        processSuccessDir = new File(config.signal.evdas.case.line.listing.import.folder.success as String)
        processFailDir = new File(config.signal.evdas.case.line.listing.import.folder.fail as String)

        5.times {
            new EvdasFileProcessLog(status: EvdasFileProcessState.IN_PROCESS, dataType: 'Case Listing', fileName: "someFileName${it}").save(validate: false)
        }
    }

    def "test the creation of base folders"() {
        given:

        when:
        service.checkAndCreateBaseDirs()

        then:
        assert readFolder.exists()
        assert uploadFolder.exists()
        assert processSuccessDir.exists()
        assert processFailDir.exists()
    }

    @Unroll
    def "test the creation of substance folders"() {
        given:
        File readFolderSubstance = new File(readFolder.absolutePath + "/" + substanceName)
        File uploadFolderSubstance = new File(uploadFolder.absolutePath + "/" + substanceName)
        File processSuccessDirSubstance = new File(processSuccessDir.absolutePath + "/" + substanceName)
        File processFailDirSubstance = new File(processFailDir.absolutePath + "/" + substanceName)

        when:
        service.checkAndCreateBaseDirs()

        then:
        assert readFolderSubstance.exists()
        assert uploadFolderSubstance.exists()
        assert processSuccessDirSubstance.exists()
        assert processFailDirSubstance.exists()

        where:
        number | substanceName
        1      | "Substance1"
        2      | "Substance2"
        3      | "Substance3"
        4      | "Substance4"
        5      | "Substance5"
    }

    def "test initiate data import flow"() {
        given:
        def mockedService = Spy(EvdasCaseListingImportService) {
            // stub a call on the same object
            readAllFilesFromSourceFolder() >> []
            filterFilesForDataImport(_) >> [:]
            filterSourceFolder(_, _, _, _, _) >> null
        }
        mockedService.transactionManager = getTransactionManager()

        when:
        mockedService.initiateDataImport()

        then:
        1 * mockedService.readAllFilesFromSourceFolder()
    }

    @Ignore
    def "test process Waiting Files flow"() {
        given:
        def mockedService = Spy(EvdasCaseListingImportService)
        mockedService.transactionManager = getTransactionManager()

        when:
        service.processWaitingFiles()

        then:
        assert EvdasFileProcessLog.count == 5
        1 * mockedService.startProcessing(_) >> null
    }

    @Ignore
    def "test startProcessing flow"() {
        given:
        def waitingFileList = EvdasFileProcessLog.findAllByStatusAndDataType(EvdasFileProcessState.IN_PROCESS, 'Case Listing', [sort: "dateCreated", order: "asc"])
        def mockedService = Spy(EvdasCaseListingImportService)
        mockedService.transactionManager = getTransactionManager()

        when:
        mockedService.startProcessing(waitingFileList)

        then:
        5 * mockedService.processFile(_) >> [processable: [], discarded: [], totalRecords: []]
        5 * mockedService.persistDataInDatabase(_) >> true
        5 * mockedService.moveFile(_, _) >> null
        5 * mockedService.updateLog(_, _, _) >> null
    }

    @Ignore
    def "test Filter source folder flow"() {
        given:
        def mockedService = Spy(EvdasCaseListingImportService)
        mockedService.transactionManager = getTransactionManager()
        def filteredFileList = [:]
        def fileAndDescriptionMap = [:]
        def isManual = false
        def dataType = "Case Listing"


        when:
        mockedService.filterSourceFolder(userInstance, filteredFileList, fileAndDescriptionMap, isManual, dataType)

        then:
        2 * mockedService.saveLogs(_, _, _, _, _, _ , _) >> null
        1 * mockedService.moveFile(_, _) >> null
    }

    def "test read all files from source folder flow"() {
        given:
        5.times {
            new File(readFolder, "file${it}.txt").createNewFile()
        }

        when:
        def ls = service.readAllFilesFromSourceFolder()

        then:
        assert ls.size() == 5

        cleanup:
        5.times {
            new File(readFolder, "file${it}.txt").delete()
        }
    }

    @Unroll
    def "test for validating file names"() {
        when:
        def actualResponse = service.validateFileName(fileName)

        then:
        assert actualResponse == expectedResponse

        where:
        fileName   | expectedResponse
        'abc.txt'  | false
        'abc.xls'  | true
        'abc.xlsx' | true
        'abc.xlx'  | false
        'abc.xlsm' | false
        'abc.xlm'  | false
    }

    def "test for getCsvCaseListingData"() {
        given:
        String sampleCsvFile = "${config.signal.evdas.case.line.listing.import.folder.read}/file.csv"
        Map columnTypeMap = config.signal.evdas.case.line.listing.import.COLUMN_TYPE_MAP
        columnTypeMap["3"] = "HYPERLINK"
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(sampleCsvFile))
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)
        csvPrinter.printRecord("EU Local Number", "EV Gateway Receipt Date", "Reaction List PT (Duration – Outcome - Seriousness Criteria)", 'ICSR Form')
        csvPrinter.printRecord("EU-EC-10005377590", "2020-01-31 00:00:00", "Diabetes mellitus <BR> Acne", '<a target="_blank"  href="https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10022972">New EVPM Link</a>')
        csvPrinter.printRecord("EU-EC-10005379521", "2020-01-22 00:00:00", "Acne Diabetes", '<a target="_blank"  href="https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10049105">New EVPM Link</a>')
        csvPrinter.flush()
        File file = new File(sampleCsvFile)

        when:
        def map = service.getCsvCaseListingData(file, columnTypeMap)

        then:
        List processable = map.processable
        processable[0]["EU LOCAL NUMBER"] == "EU-EC-10005377590"
        processable[0]["EV GATEWAY RECEIPT DATE"] == "31/01/2020"
        processable[0]["Reaction List PT (Duration – Outcome - Seriousness Criteria)".toUpperCase()] == "Diabetes mellitus  Acne"
        processable[0]["ICSR FORM"] == 'https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10022972'

        processable[1]["EU LOCAL NUMBER"] == "EU-EC-10005379521"
        processable[1]["EV GATEWAY RECEIPT DATE"] == "22/01/2020"
        processable[1]["Reaction List PT (Duration – Outcome - Seriousness Criteria)".toUpperCase()] == "Acne Diabetes"
        processable[1]["ICSR FORM"] == 'https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10049105'

        cleanup:
        new File(sampleCsvFile).delete()
    }

}