package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil

import java.text.SimpleDateFormat

class EvdasFileProcessLog {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.BASIC_DATE)
    static def userService

    String fileName
    String description
    String substances
    String dataType
    EvdasFileProcessState status
    Integer duplicateDataHandling
    Boolean isManual
    Date recordStartDate
    Date recordEndDate
    String reason
    Integer totalRecords
    Integer processedRecords
    Date dateCreated = new Date()
    Date lastUpdated
    User updatedBy
    String savedName

    static mapping = {
        reason column: "REASON", type: "text", sqlType: "clob"
        substances column: "SUBSTANCES", length: 4000
    }

    static constraints = {
        fileName blank: false
        recordStartDate nullable: true
        recordEndDate nullable: true
        reason nullable: true, blank: false
        description nullable: true, blank: false
        totalRecords nullable: true
        processedRecords nullable: true
        substances nullable: true
        dataType nullable: true
        duplicateDataHandling nullable: true
        isManual nullable: true
        savedName nullable: true
    }


    def beforeValidate() {
        this.lastUpdated = new Date()
    }

    static List fetchFileProcessLogs(String timezone) {
        String userTimezone = userService?.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List fileProcessLogs = []
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
        findAll().each { fileProcessLog ->
            fileProcessLogs.add([
                    "documentId"      : fileProcessLog.id,
                    "dataType"        : fileProcessLog.dataType ?: "-",
                    "description"     : fileProcessLog.description ?: "-",
                    "documentName"    : fileProcessLog.fileName.split('/').last(),
                    "uploadTimeStamp" : sdf.format(new Date(DateUtil.toDateStringWithTime(fileProcessLog.dateCreated, userTimezone))).toString(),
                    "substanceName"   : fileProcessLog.substances ?: "-",
                    "dataRange"       : (fileProcessLog.recordStartDate && fileProcessLog.recordEndDate) ? "${DateUtil.toDateStringPattern(fileProcessLog.recordStartDate, Constants.DateFormat.STANDARD_DATE)} to ${DateUtil.toDateStringPattern(fileProcessLog.recordEndDate, Constants.DateFormat.STANDARD_DATE)}" : "-",
                    "uploadStatus"    : fileProcessLog.status.value(),
                    "errorLog"        : fileProcessLog.reason,
                    "totalRecords"    : fileProcessLog.totalRecords ?: "-",
                    "processedRecords": fileProcessLog.processedRecords ?: "-",
                    "uploadedBy"      : fileProcessLog.updatedBy.fullName
            ])
        }
        fileProcessLogs
    }
}
