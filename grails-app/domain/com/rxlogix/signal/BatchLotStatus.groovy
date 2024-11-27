package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Action
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.util.Holders

import java.sql.Timestamp

class BatchLotStatus implements Serializable {
    Long id
    Long version
    String batchId
    Date batchDate
    Long count
    Long validRecordCount
    Long invalidRecordCount
    Date uploadedAt
    String addedBy
    Boolean isApiProcessed
    Boolean isEtlProcessed
    Date etlStartDate
    String etlStatus
    String dateRange

    List<BatchLotData> clientDatas = []

    static mapping = {
        datasource "pva"
        table name: "PVS_BS_APP_BATCH_LOT_STATUS"
        uploadedAt column: "UPLOADED_DATE"
        id generator: 'sequence', params: [sequence: 'BATCH_LOT_STATUS_SEQ']
        clientDatas joinTable: [name: "PVS_BS_APP_BATCH_LOT_DATA", key: "BATCH_LOT_ID",  column: "ID"]
    }
    static constraints = {
        batchId nullable: true
        batchDate nullable: true
        count nullable: true
        validRecordCount nullable: true
        invalidRecordCount nullable: true
        uploadedAt nullable: true
        addedBy nullable: true
        isApiProcessed nullable: true
        isEtlProcessed nullable: true
        etlStartDate nullable: true
        etlStatus nullable: true
        dateRange nullable: true
    }

    BatchLotStatus() {
    }

    BatchLotStatus(String batchId, Date batchDate, Long count, Long validRecordCount, Long invalidRecordCount, Date uploadedAt, String addedBy, List<BatchLotData> clientDatas) {
        this.batchId = batchId
        this.batchDate = batchDate
        this.count = count
        this.validRecordCount = validRecordCount
        this.invalidRecordCount = invalidRecordCount
        this.uploadedAt = uploadedAt
        this.addedBy = addedBy
        this.clientDatas = clientDatas
    }
    BatchLotStatus(Long id, Long version, String batchId, Date batchDate, Long count, Long validRecordCount,
                   Long invalidRecordCount, Date uploadedAt, String addedBy, Boolean isApiProcessed,
                   Boolean isEtlProcessed, Date etlStartDate, String etlStatus, String dateRange, List<BatchLotData> clientDatas) {
        this.id = id
        this.version = version
        this.batchId = batchId
        this.batchDate = batchDate
        this.count = count
        this.validRecordCount = validRecordCount
        this.invalidRecordCount = invalidRecordCount
        this.uploadedAt = uploadedAt
        this.addedBy = addedBy
        this.isApiProcessed = isApiProcessed
        this.isEtlProcessed = isEtlProcessed
        this.etlStartDate = etlStartDate
        this.etlStatus = etlStatus
        this.dateRange=dateRange
        this.clientDatas = clientDatas
    }

    BatchLotStatus(BigDecimal id, String batchId, Timestamp batchDate, BigDecimal count, BigDecimal validRecordCount,
                   BigDecimal invalidRecordCount, Timestamp uploadedAt, String addedBy, BigDecimal isApiProcessed) {
        this.id = id
        this.batchId = batchId
        this.batchDate = batchDate
        this.count = count
        this.validRecordCount = validRecordCount
        this.invalidRecordCount = invalidRecordCount
        this.uploadedAt = uploadedAt
        this.addedBy = addedBy
        this.isApiProcessed = isApiProcessed
    }

    Map toExportDto(String timeZone) {
        Map map = [
                id                  : (null==this.id)?"":this.id.toString(),
                batchId             : this.batchId,
                batchDate           : this.batchDate,
                count               : this.count,
                validRecordCount    : this.validRecordCount,
                invalidRecordCount  : this.invalidRecordCount,
                uploadedAt          : this.uploadedAt,
                addedBy             : this.addedBy,
                isApiProcessed      : this.isApiProcessed,
                isEtlProcessed      : this.isEtlProcessed,
                etlStartDate        : this.etlStartDate,
                childDatas          : [
                ]
        ]
        map
    }
}
