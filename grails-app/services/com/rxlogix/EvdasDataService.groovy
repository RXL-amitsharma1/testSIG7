package com.rxlogix

import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.enums.EvdasFileProcessState
import grails.gorm.transactions.Transactional
import com.rxlogix.Constants.DateFormat
import grails.util.Holders

import java.text.SimpleDateFormat

@Transactional
class EvdasDataService {

    def userService
    SignalAuditLogService signalAuditLogService

    List fetchFileProcessLog() {
        EvdasFileProcessLog.fetchFileProcessLogs(userService?.getUser().preference.timeZone)
    }

    File fetchDownloadableFile(EvdasFileProcessLog fileProcessLog) {
        def config = Holders.config
        String sourceDirPath
        String fileName = fileProcessLog.savedName == null ? fileProcessLog.fileName : fileProcessLog.savedName
        String auditFileName = fileProcessLog.fileName.substring(fileName.lastIndexOf("/") + 1)
        List criteriaSheet = [["label":"Document","value":auditFileName],["label":"File Type","value":fileProcessLog.dataType],["label":"Substance(s)","value":fileProcessLog.substances]]
        signalAuditLogService.createAuditForExport(criteriaSheet,fileProcessLog.getDataType(), "EVDAS Data Upload", [:],auditFileName)
        if (fileProcessLog.dataType == 'eRMR') {
            if (fileProcessLog.status == EvdasFileProcessState.SUCCESS) {
                sourceDirPath = config.signal.evdas.data.import.folder.success
            } else {
                sourceDirPath = config.signal.evdas.data.import.folder.fail
            }

            new File("$sourceDirPath/$fileName")

        } else if (fileProcessLog.dataType == 'Case Listing') {
            def fileToMovePathBreakdown = fileName.split('/')
            fileToMovePathBreakdown[-3] = (fileProcessLog.status == EvdasFileProcessState.SUCCESS) ? 'success' : 'fail'
            new File(fileToMovePathBreakdown.join('/'))
        }
    }
}
