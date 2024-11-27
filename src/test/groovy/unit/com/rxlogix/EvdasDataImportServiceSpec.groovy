package unit.com.rxlogix

import com.rxlogix.ApplicationSettingsService
import com.rxlogix.EvdasDataImportService
import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTimeZone
import spock.lang.Specification
import spock.lang.Unroll
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Paths
import spock.lang.Ignore
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Ignore
class EvdasDataImportServiceSpec extends Specification implements ServiceUnitTest<EvdasDataImportService> {

    User userInstance
    File readFolder
    File uploadFolder
    File processSuccessDir
    File processFailDir

    def setup() {
        service.transactionManager = getTransactionManager()

        userInstance = new User(username: "Test User")
        userInstance.save(validate: false)


        readFolder = new File(config.signal.evdas.data.import.folder.read as String)
        uploadFolder = new File(config.signal.evdas.data.import.folder.upload as String)
        processSuccessDir = new File(config.signal.evdas.data.import.folder.success as String)
        processFailDir = new File(config.signal.evdas.data.import.folder.fail as String)

        5.times {
            new EvdasFileProcessLog(status: EvdasFileProcessState.IN_PROCESS, dataType: 'eRMR', fileName: "someFileName${it}").save(validate: false)
        }

        new SubstanceFrequency(name: "Substance1", startDate: Date.parse('dd-MMM-yyyy', '01-Feb-2016'), endDate: Date.parse('dd-MMM-yyyy', '29-Feb-2016'), uploadFrequency: 'Monthly', miningFrequency: 'Monthly', frequencyName: "Monthly", alertType: "EVDAS Alert").save(validate: false)
        new SubstanceFrequency(name: "Substance2", startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2016'), endDate: Date.parse('dd-MMM-yyyy', '31-Mar-2016'), uploadFrequency: 'Quarterly', miningFrequency: 'Monthly', frequencyName: "Monthly", alertType: "EVDAS Alert").save(validate: false)
    }

    def "test the creation of base folders"() {
        when:
        service.checkAndCreateBaseDirs()

        then:
        assert readFolder.exists()
        assert uploadFolder.exists()
        assert processSuccessDir.exists()
        assert processFailDir.exists()
    }

    def "test initiate data import flow"() {
        given:
        def mockedService = Spy(EvdasDataImportService) {
            readAllFilesFromSourceFolder() >> []
            filterFilesForDataImport(_, _) >> [:]
            filterSourceFolder(_, _, _, _, _, _) >> null
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
        def mockedService = Spy(EvdasDataImportService)
        mockedService.transactionManager = getTransactionManager()

        when:
        mockedService.processWaitingFiles()

        then:
        assert EvdasFileProcessLog.count == 5
        1 * mockedService.startProcessing(_) >> null
    }


    def "test startProcessing flow"() {
        given:
        def waitingFileList = EvdasFileProcessLog.findAllByStatusAndDataType(EvdasFileProcessState.IN_PROCESS, 'eRMR', [sort: "dateCreated", order: "asc"])
        def mockedService = Spy(EvdasDataImportService)
        mockedService.transactionManager = getTransactionManager()

        when:
        mockedService.startProcessing(waitingFileList)

        then:
        5 * mockedService.processFile(_) >> [processable: [], discarded: [], totalRecords: []]
        5 * mockedService.persistDataInDatabase(_, _, _,_) >> [:]
        5 * mockedService.moveFile(_, _, _) >> null
        5 * mockedService.updateLog(_, _, _, _) >> null
    }


    def "test Filter source folder flow"() {
        given:
        def mockedService = Spy(EvdasDataImportService)
        mockedService.transactionManager = getTransactionManager()
        def filteredFileList = [:]
        def fileAndDescriptionMap = [:]
        def isManual = false
        def dataType = "eRMR"
        def optDuplicate = 1

        when:
        mockedService.filterSourceFolder(userInstance, filteredFileList, fileAndDescriptionMap, isManual, dataType, optDuplicate)

        then:
        2 * mockedService.saveLogs(_, _, _, _, _, _, _, _) >> null
        1 * mockedService.moveFile(_, _, _) >> null
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
        def actualResponse = service.validateFileName(new File(fileName))

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

    @Unroll
    @Ignore
    def "test for valid substance and date frequency"() {
        given:
        def startDateTemp = DateUtil.stringToDate(startDate, 'dd/MM/yyyy', DateTimeZone.UTC.ID)
        def endDateTemp = DateUtil.stringToDate(endDate, 'dd/MM/yyyy', DateTimeZone.UTC.ID)

        when:
        def actualResponse = service.isValidSubstanceAndDateRange(substanceName, [startDateTemp, endDateTemp])

        then:
        assert actualResponse == expectedResponse

        where:
        substanceName | startDate    | endDate      | expectedResponse
        "Substance1"  | "01/03/2017" | "31/03/2017" | true
        "Substance2"  | "01/04/2017" | "30/06/2017" | true
        "Substance1"  | "02/03/2017" | "01/04/2017" | false
        "Substance2"  | "01/03/2017" | "31/07/2017" | false
    }

    def "test for getCsvData"() {
        given:
        Map baseColumnMapping = ["ACTIVE SUBSTANCE": "STRING", "SOC": "STRING", "PT": "STRING", "NEW EVPM": "NUMBER", "NEW EEA": "NUMBER", "NEW EVPM LINK": "STRING"]
        String sampleCsvFile = "${config.signal.evdas.data.import.folder.read}/file.csv"
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(sampleCsvFile))
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)
        csvPrinter.printRecord("Active Substance", "SOC", "PT", 'New EVPM', 'New EVPM', "New EEA")
        csvPrinter.printRecord("IPILIMUMAB", "Blood and lymphatic system disorders", "Iron deficiency anaemia", "1", '<a target="_blank"  href="https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10022972">New EVPM Link</a>', "2")
        csvPrinter.printRecord("IPILIMUMAB", "Cardiac disorders", "Microcytic anaemia", "2", '<a target="_blank"  href="https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10049105">New EVPM Link</a>', "3")
        csvPrinter.flush()
        File file = new File(sampleCsvFile)

        when:
        def map = service.getCsvData(file, baseColumnMapping)

        then:
        List processable = map.processable
        processable[0][0] == "IPILIMUMAB"
        processable[0][1] == "Blood and lymphatic system disorders"
        processable[0][2] == "Iron deficiency anaemia"
        processable[0][3] == 1.0
        processable[0][4] == 2.0
        processable[0][5] == 'https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10022972'

        processable[1][0] == "IPILIMUMAB"
        processable[1][1] == "Cardiac disorders"
        processable[1][2] == "Microcytic anaemia"
        processable[1][3] == 2.0
        processable[1][4] == 3.0
        processable[1][5] == 'https://BI.EMA.EUROPA.EU/analytics/saw.dll?go&path=%2Fshared%2FMAH%20Pharmacovigilance%20Query%20Library%2FReport%2FLine%20listing%2FLine%20listing%20New%20EVPM%20Pre-run&Options=fdr&Action=Navigate&P0=2&P1=eq&P2=%22Line%20Listing%20Objects%22.%22Active%20Substance%20(High%20Level)%20ID%22&P3=1+57378&P4=eq&P5=%22Line%20Listing%20Objects%22.%22Reaction%20PT%20ID%22&P6=1+10049105'

        cleanup:
        new File(sampleCsvFile).delete()

    }
}