package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.commandObjects.BatchLotCO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.signal.BatchLotData
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.ViewHelper
import grails.util.Holder
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang.StringUtils
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.springframework.util.CollectionUtils

import java.sql.ResultSet
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat

class BatchRestService {

    static transactional = false
    def dataSource
    def signalDataSourceService
    def sessionFactory_pva
    def sessionFactory
    def dataSource_pva
    def dynamicReportService
    def emailNotificationService
    def signalAuditLogService
    def userService
    def cacheService

    Map getBatchLotStatusList(DataTableSearchRequest searchRequest, params) {
        User user = null
        List<Long> groupIds = null
        String timeZone = null
        List<BatchLotStatus> batchLotStatuses = getBatchLotStatuses(searchRequest, params, false);
        List<Map> batchLotStatusMap = createBatchLotStatusDTO(batchLotStatuses, timeZone);
        Integer filteredCount = getBatchLotStatusCountsBySQL(user, searchRequest, searchRequest?.searchParam?.search?.getValue()?.toLowerCase(), params, groupIds, false)
        int totalCount
        BatchLotStatus."pva".withTransaction {
            totalCount=BatchLotStatus.createCriteria().get { projections { count "id" } } as int
        }
        if(!CollectionUtils.isEmpty(batchLotStatusMap)) {
            batchLotStatusMap.each {
                bls ->
                Set<String> validationMessages = BatchLotData.createCriteria().list {projections {property ('validationError')}
                    eq("batchLotId", bls.get("id"))
                }
                if(!CollectionUtils.isEmpty(validationMessages)) {
                    validationMessages.removeAll([null])
                    Set validationMessagesSet = []
                    validationMessages.join("\n").split("\n").each { ln ->
                      if(!StringUtils.isEmpty(ln)) {
                        if (ln.indexOf("MISMATCH DATA") != -1) {
                            ln = ln.replace("MISMATCH DATA", "")
                        }
                        if (ln.indexOf("DUPLICATE DATA") != -1) {
                            ln = ln.replace("DUPLICATE DATA", "")
                        }
                        if (ln.indexOf(" must be date in yyyyMMdd format") != -1) {
                            ln = ln.replace(" must be date in yyyyMMdd format", " must be in YYYYMMDD format")
                        }
                        if (ln.indexOf("\n\n") != -1) {
                            ln = ln.replace("\n\n", "\n")
                        }
                        validationMessagesSet.add(ln)
                      }
                    }
                    validationMessagesSet.removeAll([null])
                    bls.put("info",validationMessagesSet.join("\n"))
                } else {
                    bls.put("info","No records are processed!")
                }
            }
        }
        [aaData: batchLotStatusMap, recordsTotal: totalCount, recordsFiltered: filteredCount]
    }

    List getLastETLBatchLots() {
        List<BatchLotStatus> batchLotStatuses = []
        BatchLotStatus."pva".withTransaction {
            Date lastETLDate = BatchLotStatus.createCriteria().get { projections { max "etlStartDate" } } as Date
            if (lastETLDate != null) {
                batchLotStatuses = BatchLotStatus.createCriteria().list { eq("etlStartDate", lastETLDate) };
            }
        }
        batchLotStatuses
    }

    List getLastSuccessfullETLBatchLots() {
        List<BatchLotStatus> batchLotStatuses = []
        BatchLotStatus."pva".withTransaction {
            Date lastETLDate = BatchLotStatus.createCriteria().get {
                projections { max "etlStartDate" }
                or{eq("etlStatus","COMPLETED")
                    eq("etlStatus","FAILED")}
            } as Date
            if (lastETLDate != null) {
                batchLotStatuses = BatchLotStatus.createCriteria().list {
                    eq("etlStartDate", lastETLDate)
                    or{eq("etlStatus","COMPLETED")
                        eq("etlStatus","FAILED")}
                };
            }
        }
        batchLotStatuses
    }

    BatchLotStatus getBatchLotStatusById(Long batchLotId) {
        BatchLotStatus batchLotStatus
        BatchLotStatus."pva".withTransaction {
            List<BatchLotStatus> batchLotStatuses = BatchLotStatus.createCriteria().list { eq("id", batchLotId) };
            if(batchLotStatuses!=null && batchLotStatuses.size()>0) {
                batchLotStatus= batchLotStatuses.get(0)
            }
        }
        batchLotStatus
    }

    Long getRemainingBatchLotCountForETL() {
        Long batchLotCountForETL = 0
        Long startedBatchLotETLCount = BatchLotStatus.createCriteria().get {
            projections { count "id" }
            eq("etlStatus" , "STARTED")
        } as Long
        if(startedBatchLotETLCount==0) {
            batchLotCountForETL = BatchLotStatus.createCriteria().get {
                projections { count "id" }
                isNull("etlStatus")
            } as Long
        }
        batchLotCountForETL
    }

    def isPVDETLCompleted() {
        String PVDETLCompleted = "NO"
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.eachRow(SignalQueryHelper.pvd_etl_status_completed()) { ResultSet resultSetObj ->
                Integer eltValue = resultSetObj.getInt("etl_value")
                if (eltValue!=null) {
                    PVDETLCompleted = "YES"
                }
            }
        } catch (Exception ex) {
            println(ex.stackTrace)
        } finally {
            sql?.close()
        }
        PVDETLCompleted
    }
    def runEtlForRemainingApiBatchLot() {
        log.info("runEtlForRemainingApiBatchLot STARTED")
        List batchLotList = BatchLotStatus.createCriteria().list {
            projections {
                property("id")
            }
            isNull("etlStatus")
        } as List
        log.info("FETCHED ALL BATCH IDs")
        String updatedRecordCount = "0"
        BatchLotStatus."pva".withTransaction {
            Session session = sessionFactory_pva.currentSession
            SQLQuery sqlQuery = session.createSQLQuery(SignalQueryHelper.batch_lot_status_update_to_started())
            updatedRecordCount = String.valueOf(sqlQuery.executeUpdate())
            session.flush()
            session.clear()
        }
        log.info("STATUS STARTED")

        User user = userService.getUser()
        ResponseDTO responseDTO =  null;
        batchLotList.each { batchId ->
            String urlString = Holders.config.pvcc.api.url+Holders.config.signal.batchSignal.pvcc.etl.url+batchId
            def postmanPost = new URL(urlString)
            def postConnection = postmanPost.openConnection()
            postConnection.setRequestProperty("apiToken",user?.preference?.apiToken)
            postConnection.setRequestProperty("userName",user?.username)
            postConnection.requestMethod = 'POST'
            if(postConnection.responseCode == 200) {
                log.info(urlString+" -> SUCCESS...")
            } else {
                log.info(urlString+" -> ERROR...")
            }
        }
        sendMailOnETLCompletion(batchLotList , user)
        updatedRecordCount
    }

    List getBatchLotStatuses(DataTableSearchRequest searchRequest, params, boolean isExport) {
        String orderByProperty = searchRequest?.orderBy() == 'id' ? Constants.Commons.BULK_API_BATCH_UPLOADED_DATE : searchRequest?.orderBy()
        String orderDirection = searchRequest?.orderBy() == 'id' ? "desc" : searchRequest?.searchParam?.orderDir()
        String searchAlertQuery = prepareValidatedBatchLotStatusSQLQuery(searchRequest, params, null, null, orderByProperty, orderDirection, isExport)
        List<BatchLotStatus> batchLotStatusList = getResultList(BatchLotStatus.class, searchAlertQuery)
        return batchLotStatusList;
    }

    String getBatchDatasByLotId(Long batchLotId) {
        List<BatchLotData> batchLotDataList = BatchLotData.createCriteria().list { eq("batchLotId", batchLotId) };
        getCSVString(batchLotDataList, getBatchDataColumnNameMap());

    }

    boolean getLastBatchLotETLNotSuccessfulCount() {
        boolean isETLNotSuccessfullyCompleted = false
        BatchLotStatus.withTransaction {
            try {
                Session session_pva = sessionFactory_pva.currentSession
                SQLQuery sqlQuery = session_pva.createSQLQuery(SignalQueryHelper.batch_lot_etl_not_successful_count())
                session_pva.flush()
                session_pva.clear()
                if (sqlQuery.list().get(0) == 0) {
                    isETLNotSuccessfullyCompleted = true
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
        isETLNotSuccessfullyCompleted
    }

    Date getLastBatchLotETLSuccessfulDate() {
        Date lastEtlSuccessDate = null
        BatchLotStatus.withTransaction {
            try {
                Session session_pva = sessionFactory_pva.currentSession
                SQLQuery sqlQuery = session_pva.createSQLQuery(SignalQueryHelper.batch_lot_etl_last_successful_date())
                session_pva.flush()
                session_pva.clear()
                lastEtlSuccessDate = sqlQuery.list().get(0)
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
        lastEtlSuccessDate
    }

    List getExcelDataByLotId(Long batchLotId) {
        List<BatchLotData> batchLotDataList = BatchLotData.createCriteria().list { eq("batchLotId", batchLotId) };

        boolean isETLCompleted = false
        BatchLotStatus.withTransaction {
            try {
                Session session_pva = sessionFactory_pva.currentSession
                SQLQuery sqlQuery = session_pva.createSQLQuery(SignalQueryHelper.batch_lot_etl_status_by_id(batchLotId))
                session_pva.flush()
                session_pva.clear()
                if (sqlQuery.list().get(0) > 0) {
                    isETLCompleted = true
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }

        getBatchExcelData(batchLotDataList, getBatchDataColumnNameMap(), isETLCompleted);
    }

    List getExcelDataForETLByLotId(Long batchLotId) {
        List<BatchLotData> batchLotDataList = BatchLotData.createCriteria().list { eq("batchLotId", batchLotId) };

        boolean isETLCompleted = false
        BatchLotStatus.withTransaction {
            try {
                Session session_pva = sessionFactory_pva.currentSession
                SQLQuery sqlQuery = session_pva.createSQLQuery(SignalQueryHelper.batch_lot_etl_status_by_id(batchLotId))
                session_pva.flush()
                session_pva.clear()
                if (sqlQuery.list().get(0) > 0) {
                    isETLCompleted = true
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
        getBatchExcelDataForETL(batchLotDataList, getBatchDataColumnNameMap(), isETLCompleted);
    }

    String getBatchDatasForETLByLotId(Long batchLotId) {
        List<BatchLotData> batchLotDataList = BatchLotData.createCriteria().list { eq("batchLotId", batchLotId) };
        getCSVString(batchLotDataList, getBatchDataForETLColumnNameMap());

    }
    List<BatchLotStatus> getBatchLotStatusByLotIds(List<Long> batchLotIds) {
        List<BatchLotStatus> batchLotStatusList = BatchLotStatus.createCriteria().list {
            'in'('id',batchLotIds)
        }
        batchLotStatusList
    }

    List<BatchLotData> getBatchLotDateByBatchLotId(Long batchLotId) {
        List<BatchLotData> batchLotDataList = BatchLotData.createCriteria().list {
            'eq'('batchLotId',batchLotId)
        }
        batchLotDataList
    }

    List exportBatchLot(DataTableSearchRequest searchRequest, params) {
        List<BatchLotStatus> batchLotStatuses = []
        DataTableSearchRequest.DataTableSearchParam request = searchRequest?.searchParam
        String orderByProperty = StringUtils.defaultIfBlank(request.columns[request.order[0].column].name, 'id')
        String orderDirection = request.order[0].dir
        String searchAlertQuery = prepareValidatedBatchLotStatusSQLQuery(searchRequest, params, null, null, orderByProperty, orderDirection)
        List<BatchLotStatus> batchLotStatusList = getResultList(BatchLotStatus.class, searchAlertQuery)
        batchLotStatuses
    }

    private int getBatchLotStatusCounts(User user, DataTableSearchRequest searchRequest, String searchKey, params, List<Long> groupIds, boolean isTotalCount) {
        int count
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append(SignalQueryHelper.batch_lot_status_count())
        validatedBatchStatusSearchFilters(params, groupIds, searchAlertQuery, user, searchKey, searchRequest, isTotalCount)
        BatchLotStatus."pva".withTransaction {
            count = BatchLotStatus."pva".executeQuery(searchAlertQuery.toString())[0] as int
        }
        return count
    }

    private int getBatchLotStatusCountsBySQL(User user, DataTableSearchRequest searchRequest, String searchKey, params, List<Long> groupIds, boolean isTotalCount) {
        int count
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append(SignalQueryHelper.batch_lot_status_count_sql())
        validatedBatchStatusSearchFiltersBySqlQry(params, groupIds, searchAlertQuery, user, searchKey, searchRequest, isTotalCount)
        BatchLotStatus.withTransaction {
            try {
                Session session_pva = sessionFactory_pva.currentSession
                SQLQuery sqlQuery = session_pva.createSQLQuery(searchAlertQuery.toString())
                session_pva.flush()
                session_pva.clear()
                count = sqlQuery.list().get(0)
            } catch(Exception ex) {
                println("Exception: "+ex.toString())
                ex.printStackTrace()
            }
        }
        return count
    }


    String prepareValidatedBatchLotStatusHQL(DataTableSearchRequest searchRequest, params, List<Long> groupIds, User user,
                                             String orderByProperty, String orderDirection) {

        String searchKey = searchRequest?.searchParam?.search?.getValue()?.toLowerCase()
        StringBuilder searchAlertQuery = new StringBuilder()
        if (searchRequest) {
            searchAlertQuery.append(SignalQueryHelper.batch_lot_status())
        } else {
            searchAlertQuery.append(SignalQueryHelper.batch_lot_status_with_columns())
        }
        validatedBatchStatusSearchFilters(params, groupIds, searchAlertQuery, user, searchKey, searchRequest)

        if (StringUtils.upperCase(orderByProperty) in [Constants.Commons.EVENTS, Constants.Commons.PRODUCTS]) {
            searchAlertQuery.append(" ORDER BY dbms_lob.substr(vs.${orderByProperty}, dbms_lob.getlength(vs.${orderByProperty}), 1) ${orderDirection} ")
        } else {
            String orderDir = orderByProperty == Constants.Commons.LAST_UPDATED ? 'desc' : orderDirection;
            searchAlertQuery.append(" ORDER BY vs.${orderByProperty} ${orderDir} ")
        }
        searchAlertQuery.toString()
    }

    String prepareValidatedBatchLotStatusSQLQuery(DataTableSearchRequest searchRequest, params, List<Long> groupIds, User user,
                                                  String orderByProperty, String orderDirection, boolean isExport) {
        String searchKey = searchRequest?.searchParam?.search?.getValue()?.toLowerCase()
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append(" select * from ( ")
        if (searchRequest) {
            searchAlertQuery.append(SignalQueryHelper.batch_lot_status_sql())
        } else {
            searchAlertQuery.append(SignalQueryHelper.batch_lot_status_with_columns_sql())
        }
        validatedBatchStatusSearchFiltersBySqlQry(params, groupIds, searchAlertQuery, user, searchKey, searchRequest)
        searchAlertQuery.append(" ) kk ")
        if (StringUtils.upperCase(orderByProperty) in [Constants.Commons.EVENTS, Constants.Commons.PRODUCTS]) {
            searchAlertQuery.append(" ORDER BY dbms_lob.substr(kk.${getOderByColumnName(orderByProperty)}, dbms_lob.getlength(kk.${orderByProperty}), 1) ${orderDirection} ")
        } else {
            String orderDir = orderByProperty == Constants.Commons.LAST_UPDATED ? 'desc' : orderDirection;
            searchAlertQuery.append(" ORDER BY kk.${getOderByColumnName(orderByProperty)} ${orderDir} ")
        }
        if(isExport!=true) {
            searchAlertQuery.append(" OFFSET ${searchRequest.searchParam.start} ROWS FETCH NEXT ${searchRequest.searchParam.length} ROWS ONLY ")
        }
        log.info("last searchAlertQuery:"+searchAlertQuery)
        searchAlertQuery.toString()
    }

    String getOderByColumnName(String propertyName) {
        String orderBy = "UPLOADED_DATE"
        Map propertyVsColumnMap = ["id":"ID","version":"version","batchId":"batchId","count":"count","batchDate":"dateRange",
                                   "validRecordCount":"validRecordCount","invalidRecordCount":"invalidRecordCount",
                                   "uploadedAt":"uploadedAt","addedBy":"addedBy","isApiProcessed":"isApiProcessed"]
        if(propertyVsColumnMap.get(propertyName)!=null) {
            orderBy = propertyVsColumnMap.get(propertyName)
        }
        orderBy
    }

    private void validatedBatchStatusSearchFilters(params, List<Long> groupIds, StringBuilder searchAlertQuery, User user,
                                                   String searchKey, DataTableSearchRequest searchRequest, boolean isTotalCount = false) {
        if (!isTotalCount && searchKey) {
            searchAlertQuery.append(" and ( lower(vs.batchId) like lower('%${searchKey}%')  or ")
            searchAlertQuery.append("  lower(vs.addedBy) like lower('%${searchKey}%')  or ")
            searchAlertQuery.append("  lower(vs.dateRange) like lower('%${searchKey}%') or ")
            if(!searchKey.trim().equals("/")) {
                searchAlertQuery.append("  concat( vs.validRecordCount, '/' , vs.count ) like lower('%${searchKey}%')  or ")
            }
            searchAlertQuery.append("  lower(to_char((vs.uploadedAt),'DD-MON-YYYY HH:MI:ss AM')) like lower('%${searchKey}%') ) ")
        }
        if (!isTotalCount) {
            searchRequest?.searchParam?.columns.each {
                String searchValue = it?.search?.value?.toLowerCase()
                if(searchValue.equals("_")) {
                    searchValue="____"
                }
                if (searchValue) {
                    if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ID.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like lower('%${searchValue}%')   )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ADDED_BY.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like lower('%${searchValue}%')  )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_DATE.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.dateRange) like lower('%${searchValue}%') )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_UPLOADED_DATE.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(to_char((vs.${it.name}),'DD-MON-YYYY HH:MI:ss AM')) like lower('%${searchValue}%') )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_VALID_RECORD_COUNT.toLowerCase()) {
                        if(!searchValue.trim().equals("/")) {
                            searchAlertQuery.append(" and ( concat( vs.validRecordCount, '/' , vs.count ) like lower('%${searchValue}%')  )")
                        }
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_COUNT.toLowerCase()) {
                        searchAlertQuery.append(" and ( vs.${it.name} = ${searchValue} )")
                    }
                }
            }
        }
    }

    private void validatedBatchStatusSearchFiltersBySqlQry(params, List<Long> groupIds, StringBuilder searchAlertQuery, User user,
                                                           String searchKey, DataTableSearchRequest searchRequest, boolean isTotalCount = false) {
        if (!isTotalCount && searchKey) {
            searchKey = searchKey.toLowerCase()
            String esc_char = ""
            if (searchKey.contains('_')) {
                searchKey = searchKey.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchKey.contains('%')) {
                searchKey = searchKey.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                searchAlertQuery.append(" and ( lower(vs.batch_id) like ('%${searchKey.replaceAll("'", "''")}%') escape '${esc_char}' or ")
                searchAlertQuery.append("  lower(vs.added_By) like ('%${searchKey.replaceAll("'", "''")}%') escape '${esc_char}'  or ")
                searchAlertQuery.append("  concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like ('%${searchKey.replaceAll("'", "''")}%')  escape '${esc_char}' or ")
                searchAlertQuery.append("  lower(vs.date_Range) like ('%${searchKey.replaceAll("'", "''")}%') escape '${esc_char}' or ")
                searchAlertQuery.append("  lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like ('%${searchKey.replaceAll("'", "''")}%') escape '${esc_char}' ) ")
            } else {
                searchAlertQuery.append(" and ( lower(vs.batch_id) like '%${searchKey}%'  or ")
                searchAlertQuery.append("  lower(vs.added_By) like '%${searchKey}%'  or ")
                searchAlertQuery.append("  concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like '%${searchKey}%'  or ")
                searchAlertQuery.append("  lower(vs.date_Range) like '%${searchKey}%' or ")
                searchAlertQuery.append("  lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchKey}%' ) ")
            }
        }
        if (!isTotalCount) {
            searchRequest?.searchParam?.columns.each {
                String searchValue = it?.search?.value?.toLowerCase()
                String esc_char = ""
                if (searchValue.contains('_')) {
                    searchValue = searchValue.replaceAll("\\_", "!_%")
                    esc_char = "!"
                } else if (searchValue.contains('%')) {
                    searchValue = searchValue.replaceAll("\\%", "!%%")
                    esc_char = "!"
                }
                if (searchValue) {
                    if (esc_char) {
                        if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ID.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.batch_id) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}'  )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ADDED_BY.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.added_By) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_DATE.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.date_Range) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_UPLOADED_DATE.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_VALID_RECORD_COUNT.toLowerCase()) {
                            if(!searchValue.trim().equals("/")) {
                                searchAlertQuery.append(" and ( concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}' )")
                            }
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_COUNT.toLowerCase()) {
                            searchAlertQuery.append(" and ( vs.count = ${searchValue.replaceAll("'", "''")} escape '${esc_char}' ) ) ")
                        }
                    } else {
                        if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ID.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.batch_id) like '%${searchValue}%'   )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ADDED_BY.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.added_By) like '%${searchValue}%'  )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_DATE.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.date_Range) like '%${searchValue}%' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_UPLOADED_DATE.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchValue}%' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_VALID_RECORD_COUNT.toLowerCase()) {
                            if(!searchValue.trim().equals("/")) {
                                searchAlertQuery.append(" and ( concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like '%${searchValue}%'  )")
                            }
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_COUNT.toLowerCase()) {
                            searchAlertQuery.append(" and ( vs.count = ${searchValue} )")
                        }
                    }
                }
            }
        }
        println("searchAlertQuery:"+searchAlertQuery)
    }

    List<BatchLotStatus> getResultList(Class className, String sql){
        List<BatchLotStatus> batchLotStatusList = null
        BatchLotStatus.withTransaction {
            try {
                Session session_pva = sessionFactory_pva.currentSession
                SQLQuery sqlQuery = session_pva.createSQLQuery(sql)
                session_pva.flush()
                session_pva.clear()
                List resutlList = sqlQuery.list()
                if(resutlList!=null) {
                    batchLotStatusList = []
                    BatchLotStatus bl = null
                    resutlList.each { row ->
                        bl = new BatchLotStatus();
                        bl.setId(row[0]==null?null:((BigDecimal)row[0]).longValue())
                        bl.setBatchId(row[1]==null?null:String.valueOf(row[1]))
                        bl.setDateRange(row[2]==null?null:String.valueOf(row[2]))
                        bl.setCount(row[3]==null?null:((BigDecimal)row[3]).longValue())
                        bl.setValidRecordCount(row[4]==null?null:((BigDecimal)row[4]).longValue())
                        bl.setInvalidRecordCount(row[5]==null?null:((BigDecimal)row[5]).longValue())
                        bl.setUploadedAt(row[6]==null?null:new Date(((Timestamp )row[6]).getTime()))
                        bl.setAddedBy(row[7]==null?null:String.valueOf(row[7]))
                        bl.setIsApiProcessed(row[8]==null?null:((BigDecimal)row[5]).longValue()==0?false:true)
                        bl.setEtlStatus(row[9]==null?null:String.valueOf(row[9]))
                        batchLotStatusList.add(bl)
                    }
                }
            } catch(Exception ex) {
                println("Exception: "+ex.toString())
                ex.printStackTrace()
            }
        }
        batchLotStatusList
    }
    String getBatchDateRange(Set<String> dateRangeSet) {
        StringBuffer allDateRange = new StringBuffer()
        if(!CollectionUtils.isEmpty(dateRangeSet)) {
            dateRangeSet.each {dr ->
                    if(allDateRange.toString().length()>0) {
                        allDateRange.append(", ")
                    }
                    if(dr.split(" - ")) {
                        if(!StringUtils.isEmpty(dr.split(" - ")[0])) {
                            allDateRange.append(getFormatedDateRangeDate(dr.split(" - ")[0]))
                        }
                        allDateRange.append(" to ")
                        if(dr.split(" - ").length==2 && !StringUtils.isEmpty(dr.split(" - ")[1])) {
                            allDateRange.append(getFormatedDateRangeDate(dr.split(" - ")[1]))
                        }
                    }

            }
        }
        allDateRange
    }
    String getFormatedDateRangeDate(String dateRangeDate) {
        String formattedDate=""
        try {
            formattedDate=getDateInStringFormat(DateUtil.StringToDate(dateRangeDate,"yyyyMMdd"),DateUtil.DATEPICKER_FORMAT)
        } catch(Exception ex) {
            print("getFormatedDateRangeDate:"+ex.toString())
        }
        formattedDate==null?"":formattedDate
    }

    List<Map> createBatchLotStatusDTO(List<BatchLotStatus> validatedSignals, String timeZone, boolean isDashboard = false) {
        List<Map> validatedSignalsDTO = []
        Map blMap = null;
        if (validatedSignals.size()) {
            for (BatchLotStatus bls : validatedSignals) {
                blMap = new HashMap();

                blMap.put("id", bls.getId());
                blMap.put("version", bls.getVersion());
                blMap.put("batchId", bls.getBatchId());
                blMap.put("batchDate", getDateInStringFormat(bls.getBatchDate(),DateUtil.DATEPICKER_FORMAT));
                blMap.put("dateRange", bls.getDateRange());
                blMap.put("count", bls.getCount());
                blMap.put("validRecordCount", bls.getValidRecordCount());
                blMap.put("invalidRecordCount", bls.getInvalidRecordCount());
                blMap.put("uploadedAt", getDateInStringFormat(bls.getUploadedAt(),DateUtil.DATEPICKER_FORMAT_AM_PM_3));
                blMap.put("addedBy", bls.getAddedBy());
                blMap.put("apiStatus", "");
                blMap.put("etlStatus", bls.getEtlStatus());
                validatedSignalsDTO.add(blMap);
            }
        }
        validatedSignalsDTO
    }

    def getDateInStringFormat(Date date, String format) {
        String formattedDate = null
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            if(date!=null) {
                formattedDate = dateFormat.format(date)
            }
        }catch (Exception ex) {
            log.error(date+"->"+ex.toString())
        }
        formattedDate
    }

    def sendMailOnBatchLotUpdate(BatchLotStatus batchLotStatus, User user, String errorString) {
        if(!org.springframework.util.StringUtils.isEmpty(Holders.config.signal.batchSignal.api.upload.toAddresses)) {
            List<String> toAddresses = Holders.config.signal.batchSignal.api.upload.toAddresses.split(",")
            List uploads = null
            if(batchLotStatus.getBatchId()!=null) {
                List summaryList = getBatchLotETLSummaryData(getBatchLotStatusByLotIds([batchLotStatus.getId()]))
                List<BatchLotData> batchLotDataList = getExcelDataByLotId(batchLotStatus.getId())//BatchLotData.createCriteria().list { eq("batchLotId", batchLotStatus.getId()) };
                uploads = fetchAttachments(batchLotStatus.getBatchId(),batchLotDataList, summaryList);
            }
            emailNotificationService.mailApiUploadStatus(batchLotStatus, user.getEmail(), toAddresses, uploads , errorString)
        } else {
            log.error("API Mail can not be sent because batchSignal.api.upload.toAddresses properting is missing...")
        }
    }

    def sendMailOnETLCompletion(List batchLotList , User user) {
        List<BatchLotStatus> batchLotStatusList = getBatchLotStatusByLotIds(batchLotList)
        batchLotStatusList.each { batchLotStatus ->
            String errorString = getDistinctErrorMessage(batchLotStatus, getBatchLotDateByBatchLotId(batchLotStatus.getId()))
            if(!org.springframework.util.StringUtils.isEmpty(Holders.config.signal.batchSignal.api.upload.toAddresses)) {
                List<String> toAddresses = Holders.config.signal.batchSignal.api.upload.toAddresses.split(",")
                List uploads = null
                if(batchLotStatus.getBatchId()!=null) {
                    List summaryList = getBatchLotETLImportSummaryData(getBatchLotStatusByLotIds([batchLotStatus.getId()]))
                    List<BatchLotData> batchLotDataList = getExcelDataForETLByLotId(batchLotStatus.getId())
                    uploads = fetchAttachments(batchLotStatus.getBatchId(),batchLotDataList, summaryList);
                    summaryList.each { sl ->
                        if("Total number of Records".equals(sl.get("label"))) {
                            batchLotStatus.setCount(Long.valueOf(sl.get("value")))
                        }else if("Total number of Records Imported".equals(sl.get("label"))) {
                            batchLotStatus.setValidRecordCount(Long.valueOf(sl.get("value")))
                        }
                    }
                }
                emailNotificationService.mailBatchLotETLStatus(batchLotStatus, user.getEmail(), toAddresses, uploads , errorString)
            } else {
                log.error("API Mail can not be sent because batchSignal.api.upload.toAddresses properting is missing...")
            }
            saveETLAuditTrail(batchLotStatus, user)
        }
    }

    def fetchAttachments(String id, String csv) {
        List uploads = []
        uploads << [name:id+".csv" , file:csv.bytes]
        uploads
    }
    def fetchAttachments(String id, List<Map> dataMap,List summaryList ) {
        List uploads = []
        uploads << [name:id+".XLSX" , file:renderExcelReportFile(id, dataMap, summaryList, getBatchDataColumnNameMap())]
        uploads
    }
    File renderExcelReportFile(String name, List data, List summaryList, Map columns) {
        Map reportParams = new LinkedHashMap()
        reportParams.outputFormat = "XLSX"
        reportParams.name = name
        reportParams.columns = columns
        reportParams.summaryList = summaryList
        summaryList.each {hm ->
            if(hm.get("label")=="Date") {
                reportParams.etlLogExport = "YES"
            }
        }
        dynamicReportService.createEtlBatchReport(new JRMapCollectionDataSource(data), reportParams)
    }

    def fetchAttachments_bkp(String id, String csv) {
        List uploads = []
        File uploadedAttachment = new File(Holders.config.grails.attachmentable.uploadDir + '/' + id+".csv")
        uploadedAttachment.createNewFile()
        OutputStream os = new FileOutputStream(uploadedAttachment)
        os.write(csv.bytes)
        os.close()
        uploads << [name:uploadedAttachment.name , file:uploadedAttachment.bytes]
        uploads
    }

    def saveBatchClientRecord(BatchLotCO batchLotCO, String username, Long batchLotId) {
        User user = userService.getUserByUsername(username);
        batchLotCO.setApiUsername(user.getFullName());
        BatchLotStatus batchLotStatus = saveBatchLotStatusAndData(batchLotCO, username, batchLotId)
        String errorString = getDistinctErrorMessage(batchLotStatus, batchLotCO.getClientDatas())
        sendMailOnBatchLotUpdate(batchLotStatus, user, errorString)
        saveAuditTrail(batchLotCO, batchLotStatus.getId(), batchLotStatus.getUploadedAt(), null,user, batchLotStatus.getValidRecordCount() )
        batchLotStatus
    }

    private String getDistinctErrorMessage(BatchLotStatus batchLotStatus, List<BatchLotData> clientDatas) {
        String errorString = null;
        if (batchLotStatus.getValidRecordCount() == null
                || batchLotStatus.getValidRecordCount() == 0
                || batchLotStatus.getValidRecordCount() < batchLotStatus.getCount()) {
            Set<String> errorDetailsSet = null
            clientDatas.each { batchLotData ->
                if (batchLotData.getValidationError() != null && batchLotData.getValidationError().trim().length() > 0) {
                    if (errorDetailsSet == null) {
                        errorDetailsSet = new HashSet<String>();
                    }
                    batchLotData.getValidationError().split("\n").each { validationErr ->
                        errorDetailsSet.add(validationErr)
                    }
                }
            }
            if (errorDetailsSet != null && errorDetailsSet.size() > 0) {
                errorDetailsSet.each { errString ->
                    if(errString.indexOf("MISMATCH DATA")!=-1) {
                        errString = errString.replace("MISMATCH DATA","Invalid Mapping")
                    }
                    if(errString.indexOf("DUPLICATE DATA")!=-1) {
                        errString = errString.replace("DUPLICATE DATA","ETL failed because of duplicate data")
                    }

                    if (errorString == null) {
                        errorString = errString
                    } else {
                        errorString = errorString + "<br/>" + errString
                    }
                }
            }
        }
        errorString
    }

    def saveBatchLotStatusAndAudit(BatchLotCO batchLotCO, String username, String batchLotClientDataString) {
        User user = userService.getUserByUsername(username);
        batchLotCO.setApiUsername(user.getFullName());
        BatchLotStatus batchLotStatus = saveBatchLotStatusAndData(batchLotCO, username, null)
        saveAuditTrail(batchLotCO, batchLotStatus.getId(), batchLotStatus.getUploadedAt(), batchLotClientDataString, user, batchLotStatus.getValidRecordCount())
        batchLotStatus
    }

    def saveBatchLotStatusAndData(BatchLotCO batchLotCO, String username, Long batchLotId) {
        List <BatchLotData> clientDatas = batchLotCO.getClientDatas()
        int importedDataCount = 0
        int invalidDataCount = 0

        clientDatas.each {
            it.setValidationError(validateBatchLotData(it));
            if(it.getValidationError()!=null && it.getValidationError().size()>0) {
                invalidDataCount = invalidDataCount+1
            } else {
                importedDataCount = importedDataCount+1
            }
        }

        BatchLotStatus batchLotStatus = null
        BatchLotStatus."pva".withTransaction {

            if(batchLotId!=null) {
                batchLotStatus = getBatchLotStatusById(batchLotId);
                batchLotStatus.setDateRange(getDateRanges(batchLotCO));
                batchLotStatus.setValidRecordCount(importedDataCount)
                batchLotStatus.setInvalidRecordCount(invalidDataCount)
                batchLotStatus.setIsApiProcessed(true)
            } else {
                batchLotStatus = new BatchLotStatus(batchLotCO.getBatchId(),batchLotCO.getBatchDate(),
                        batchLotCO.getCount(), importedDataCount, invalidDataCount, new Date(), batchLotCO.getApiUsername(), null);
                batchLotStatus.setIsApiProcessed(false)
            }
            log.info("batchLotStatus batchId:"+batchLotStatus.getBatchId())
            batchLotStatus.save(validate: false)
            Session session_pva = sessionFactory_pva.currentSession
            session_pva.flush()
            session_pva.clear()
        }
        clientDatas.each {
            it.setBatchLotId(batchLotStatus.getId())
        }

        def batch = []

        for(def clientData : clientDatas) {
            batch += clientData
            clientData.setBatchId(batchLotStatus.getBatchId())
            clientData.setBatchDate(batchLotStatus.getBatchDate())
            if (batch.size() > Holders.config.signal.batch.size) {
                BatchLotData."pva".withTransaction {
                    Session session_pva = sessionFactory_pva.currentSession
                    for (def cd in batch) {
                        cd.save(validate: false)
                    }
                    session_pva.flush()
                    session_pva.clear()
                    batch.clear()
                }
            }
        }

        if (batch) {
            try {

                int tmpSaveData = 0;
                BatchLotData."pva".withTransaction {
                    Session session_pva = sessionFactory_pva.currentSession
                    for (def cd in batch) {
                        cd.save(validate: false)
                    }
                    session_pva.flush()
                    session_pva.clear()
                }
            } catch (Throwable th ) {
                th.printStackTrace()
            }
        }
        batchLotStatus
    }

    String getDateRanges(BatchLotCO batchLotCO) {
        Set<String> dateRanges = []
        batchLotCO.clientDatas.each { bld ->
            if(bld.startDate!=null || bld.endDate!=null) {
                dateRanges.add(bld.startDate+" - "+bld.endDate)
            }
        }
        getBatchDateRange(dateRanges)
    }

    String objectToString(BatchLotCO batchLotCO) {
        StringBuffer sb = new StringBuffer();
        sb.append("[")
        eachWithIndex { item, index ->
            batchLotCO.getClientDatas().eachWithIndex { blData, bldIndx->
                sb.append((sb.toString().length()==0?"":",")+"{")
                Map colMap = getBatchDataColumnMapping()
                colMap.each {
                    try {
                        sb.append(it.key + ":" + blData.getProperty(it.key) + ",")
                    } catch (Exception exp) { }
                }
                sb.append("}")
            }
        }
        sb.append("]")
        sb.toString()
    }
    String objectToString(BatchLotData clientData) {
        StringBuffer sb = new StringBuffer();
        sb.append("{")
        sb.append((sb.toString().length()==0?"":",")+"{")
        Map colMap = getBatchDataColumnMapping()
        colMap.each {
            try {
                sb.append(it.key + ":" + clientData.getProperty(it.key) + ",")
            } catch (Exception exp) { }
        }
        sb.append("}")
        sb.toString()
    }

    def validateBatchLotData(BatchLotData batchLotData) {
        String errorString = "";
        if(batchLotData.getId()==null
                && batchLotData.getBatchLotId() == null && batchLotData.getVersion() == null && batchLotData.getProductId() == null
                && batchLotData.getProduct() == null && batchLotData.getDescription() == null && batchLotData.getBulkBatch() == null
                && batchLotData.getBulkBatchDate() == null && batchLotData.getFillBatch() == null && batchLotData.getFillBatchName() == null
                && batchLotData.getFillExpiry() == null && batchLotData.getFillUnits() == null && batchLotData.getPackageBatch() == null
                && batchLotData.getPackageCountry() == null && batchLotData.getPackageUnit() == null && batchLotData.getPackageReleaseDate() == null
                && batchLotData.getShippingBatch() == null && batchLotData.getComponentBatch() == null && batchLotData.getDataPeriod() == null
                && batchLotData.getUdField1() == null && batchLotData.getUdField2() == null && batchLotData.getUdField3() == null
                && batchLotData.getUdField4() == null && batchLotData.getUdField5() == null && batchLotData.getUdField6() == null
                && batchLotData.getUdField7() == null && batchLotData.getUdField8() == null && batchLotData.getUdField9() == null
                && batchLotData.getUdField10() == null  ) {
            errorString="Data row must not be empty ";
        } else {
            errorString=addValidationError(errorString, batchLotData.getProductId(), "productId",false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getProduct(), "product",  false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getDescription(), "Description", false, 4000, "STRING");
            errorString=addValidationError(errorString, batchLotData.getBulkBatch(), "BulkBatch", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getBulkBatchDate(), "BulkBatchDate", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getFillBatch(), "FillBatch", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getFillBatchName(), "FillBatchName", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getFillExpiry(), "FillExpiry", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getFillUnits(), "FillUnits", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getPackageBatch(), "PackageBatch", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getPackageCountry(), "PackageCountry", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getPackageUnit(), "PackageUnit", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getPackageReleaseDate(), "PackageReleaseDate", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getShippingBatch(), "ShippingBatch", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getComponentBatch(), "ComponentBatch", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getDataPeriod(), "DataPeriod", false, 250, "DATE");
            errorString=addValidationError(errorString, batchLotData.getStartDate(), "StartDate", false, 250, "DATE");
            errorString=addValidationError(errorString, batchLotData.getEndDate(), "EndDate", false, 250, "DATE");
            errorString=addValidationError(errorString, batchLotData.getProductGroupName(), "ProductGroupName", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField1(), "UdField1", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField2(), "UdField2", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField3(), "UdField3", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField4(), "UdField4", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField5(), "UdField5", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField6(), "UdField6", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField7(), "UdField7", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField8(), "UdField8", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField9(), "UdField9", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getUdField10(), "UdField10", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getProductHierarchy(), "ProductHierarchy", false, 250, "STRING");
            errorString=addValidationError(errorString, batchLotData.getProductHierarchyId(), "ProductHierarchyId", false, 250, "STRING");
        }
        batchLotData.setValidationError(errorString)
        return errorString;
    }

    def addValidationError(String errorString, String columnValue, String columnName, boolean isMendatory, int dataLength, String dataType) {
        if(isMendatory == true && columnValue ==null) {
            if(errorString == null) {
                errorString = columnName+" must be mandatory \n"
            } else {
                errorString = errorString + columnName+" must be mandatory \n"
            }
        } else if( columnValue != null && dataType=="STRING" && columnValue.length()>dataLength) {
            if(errorString == null) {
                errorString = columnName+" must be less than "+dataLength+" charectors \n"
            } else {
                errorString = errorString + columnName+" must be less than "+dataLength+" charectors \n"
            }
        } else if( columnValue != null && dataType=="INTEGER" ) {
            if(errorString == null) {
                errorString = columnName+" must be integer \n"
            } else {
                errorString = errorString + columnName+" must be integer \n"
            }
        } else if( columnValue != null && dataType=="DATE" ) {
            try {
                DateUtil.StringToDate(columnValue,"yyyyMMdd");
            } catch(Exception ex) {
                ex.getStackTrace()
                if(errorString == null) {
                    errorString = columnName+" must be in YYYYMMDD format \n"
                } else {
                    errorString = errorString + columnName+" must be in YYYYMMDD format \n"
                }
            }
        }
        return errorString
    }

    Map getBatchDataColumnMapping() {
        LinkedHashMap map = Holders.config.signal.batchSignal.columns.mapping
        map
    }
    def getBatchLotDatas(List<Object> objectList) {
        Map batchDataColumnMapping = getBatchDataColumnMapping()
        List<BatchLotData> batchLotDataList = null
        BatchLotData batchLotData = null
        if(objectList!=null && objectList.size()>0) {
            batchLotDataList = new ArrayList<>()
            objectList.eachWithIndex { item, index ->
                batchLotData = new BatchLotData();
                for (Map.Entry<String, Object> entry : batchDataColumnMapping.entrySet()) {
                    batchLotData.setProperty(entry.getValue(), item.get(entry.getKey()));
                }
                batchLotDataList.add(batchLotData)
            }
        }
        batchLotDataList
    }
    def getBatchLotCSVData(List<BatchLotStatus> batchLotStatusList) {
        StringBuffer sb = new StringBuffer();
        sb.append("\"Batch No.\",\"Batch Date\",\"Total Number of records Imported\",\"Total Number of records failed\",\"Total Number of records\",\"Imported Date\",\"Added By\",\"Api Processed\",\"Etl Processed\"\n");
        for (bl in batchLotStatusList) {
            sb.append( (bl.getBatchId()==null?"":"\""+bl.getBatchId())+"\",\"" +
                    (bl.getBatchDate()==null?"":bl.getBatchDate())+"\",\"" +
                    (bl.getValidRecordCount()==null?"":bl.getValidRecordCount())+"\",\"" +
                    (bl.getInvalidRecordCount()==null?"":bl.getInvalidRecordCount())+"\",\"" +
                    (bl.getCount()==null?"":bl.getCount())+"\",\"" +
                    (bl.getUploadedAt()==null?"":bl.getUploadedAt())+"\",\"" +
                    (bl.getAddedBy()==null?"":bl.getAddedBy())+"\",\"" +
                    (bl.getIsApiProcessed()==null?"": ( bl.getIsApiProcessed()==true?"Passed":"Failed" ) )+"\",\"" +
                    (bl.getIsEtlProcessed()==null?"":bl.getIsEtlProcessed())+"\"\n");
        }
        sb.toString()
    }

    List<Map> getBatchLotExcelData(List<BatchLotStatus> batchLotStatusList) {
        List<Map> mapList = []
        String finalStatus = null
        batchLotStatusList.each{bl->
            finalStatus = "";
            if(bl.getValidRecordCount()==null || bl.getValidRecordCount()==0) {
                finalStatus = "Failed"
            } else if(bl.getValidRecordCount()<bl.getCount()) {
                finalStatus = "Failed"
            } else if(bl.getValidRecordCount()==bl.getCount()) {
                finalStatus = "Success"
            }
            mapList.add([batchId: bl.getBatchId(), batchDate: bl.getDateRange(),
                         validCount: ((bl.getValidRecordCount()==null?0:bl.getValidRecordCount())+"/"+(bl.getCount()==null?0:bl.getCount())) as String,
                         invalidCounts: bl.getInvalidRecordCount() as String,
                         count: bl.getCount() as String, uploadedAt: getDateInStringFormat(bl.getUploadedAt(),DateUtil.DATEPICKER_FORMAT_AM_PM_3),
                         addedBy: bl.getAddedBy(), apiProcessed: bl.getCount()==bl.getValidRecordCount()?"Passed":(bl.getValidRecordCount()>0?"Partial Failed":"Failed"),
                         etlProcessed: finalStatus as String])
        }
        mapList
    }
    List<Map> getBatchLotETLSummaryData(List<BatchLotStatus> batchLotStatusList) {
        Long validRecordCount = 0
        Long invalidRecordCount = 0
        Long count = 0
        Set<String> uploadedAtSet = []
        batchLotStatusList.each{it ->
            uploadedAtSet.add(getDateInStringFormat(it.getUploadedAt(),DateUtil.DATEPICKER_FORMAT))
            validRecordCount = it.validRecordCount + validRecordCount
            invalidRecordCount = it.invalidRecordCount + invalidRecordCount
            count = (it.count==null?0:it.count)+ count
        }
        String uploadedAt = uploadedAtSet.join(", ")
        List mapList = [
                ['label': "Imported Date", 'value': uploadedAt],
                ['label': "Total number of Records", 'value': count],
                ['label': "Total number of Records Imported", 'value': validRecordCount],
                ['label': "Total number of Records Failed", 'value': invalidRecordCount]]
        return mapList
    }

    List<Map> getBatchLotETLImportSummaryData(List<BatchLotStatus> batchLotStatusList) {
        Long validRecordCount = 0
        Long invalidRecordCount = 0
        Long notProcessedRecordCount = 0
        Long count = 0
        Set<String> uploadedAtSet = []
        batchLotStatusList.each{it ->
            uploadedAtSet.add(getDateInStringFormat(it.getUploadedAt(),DateUtil.DATEPICKER_FORMAT))
            Long batchLotId = Long.valueOf(it.id)
            List<BatchLotData> batchLotDataList = BatchLotData.createCriteria().list { eq("batchLotId", batchLotId) };
            batchLotDataList.each { dl ->
                if(dl.getValidationError()!=null && ( dl.getValidationError().toUpperCase().indexOf("DUPLICATE DATA") != -1
                        || dl.getValidationError().toUpperCase().indexOf("MISMATCH DATA") != -1 )
                ) {
                    notProcessedRecordCount = notProcessedRecordCount + 1;
                } else if( !StringUtils.isEmpty(dl.getValidationError()) ) {
                    invalidRecordCount = invalidRecordCount+1;
                } else if( "FAILED".equalsIgnoreCase(it.getEtlStatus()) ) {
                    invalidRecordCount = invalidRecordCount+1;
                } else {
                    validRecordCount = validRecordCount+1;
                }
                count=count+1
            }
        }
        String uploadedAt = uploadedAtSet.join(", ")
        User user = userService.user
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getLongDateFormatForLocale(user?.preference?.locale, true))
        sdf.setTimeZone(TimeZone.getTimeZone(ViewHelper.getTimeZone(user)))

        List mapList = [
                ['label': "Date", 'value': (sdf.format(new Date()) + userService.getGmtOffset(user.preference.timeZone))],
                ['label': "Total number of Records", 'value': count],
                ['label': "Total number of Records Imported", 'value': validRecordCount],
                ['label': "Total number of Records Failed", 'value': invalidRecordCount],
                ['label': "Total number of Records not Processed", 'value': notProcessedRecordCount]]
        return mapList
    }

    List<Map> getBatchLotETLExcelData(List<BatchLotStatus> batchLotStatusList) {
        List<Map> mapList = []
        String etlStatus = ""
        String dataInfo = ""
        batchLotStatusList.each{bl->
            bl.getClientDatas().each { dl->
                if("COMPLETED".equals(bl.getEtlStatus()) && "ERROR".equals(dl.getEtlStatus()) ) {
                    etlStatus = "Not processed"
                } else if( !StringUtils.isEmpty(dl.getValidationError()) ) {
                    etlStatus = "Failed"
                } else if("COMPLETED".equals(bl.getEtlStatus()) && !"ERROR".equals(dl.getEtlStatus()) ) {
                    etlStatus = "Success"
                } else if("STARTED".equals(bl.getEtlStatus()) && !"ERROR".equals(dl.getEtlStatus())) {
                    etlStatus = "Scheduled"
                } else if("FAILED".equals(bl.getEtlStatus()) && dl.getEtlStatus()==null) {
                    etlStatus = "ETL_Failed"
                } else {
                    etlStatus = "Failed"
                }
                dataInfo = dl.getValidationError()
                if(dataInfo==null) {
                    dataInfo=""
                }
                if("Not processed".equals(etlStatus) &&
                        (dataInfo.indexOf("MISMATCH DATA")!=-1 || dataInfo.indexOf("DUPLICATE DATA")!=-1) ) {
                    dataInfo = "Invalid Mapping"
                }else if("Failed".equals(etlStatus)) {
                    if(dataInfo.indexOf("MISMATCH DATA")!=-1) {
                        dataInfo = dataInfo.replace("MISMATCH DATA","Invalid Mapping"/*"ETL failed because of invalid data"*/)
                    }
                    if(dataInfo.indexOf("DUPLICATE DATA")!=-1) {
                        dataInfo = dataInfo.replace("DUPLICATE DATA","Invalid Mapping"/*"ETL failed because of duplicate data"*/)
                    }
                } else if(etlStatus == "ETL_Failed" && dataInfo == "") {
                    etlStatus = "Failed"
                    dataInfo = "Record failed due to database error"
                } else if("Scheduled".equals(etlStatus)) {
                    dataInfo = "ETL is running"
                } else {
                    dataInfo = "Successfully Processed"
                }
                mapList.add([batchId: bl.getBatchId(), batchDate: bl.getDateRange(),
                             "product":dl.getProduct(), "description":dl.getDescription(), "bulkBatch":dl.getBulkBatch(),
                             "bulkBatchDate":dl.getBulkBatchDate(), "fillBatch":dl.getFillBatch(), "fillBatchName":dl.getFillBatchName(),
                             "fillExpiry":dl.getFillExpiry(), "fillUnits":dl.getFillUnits(), "packageBatch":dl.getPackageBatch(),
                             "packageCountry":dl.getPackageCountry(), "packageUnit":dl.getPackageUnit(), "packageReleaseDate":dl.getPackageReleaseDate(),
                             "shippingBatch":dl.getShippingBatch(), "componentBatch":dl.getComponentBatch(), "startDate":castToBatchLotDataDate(dl.getStartDate()),
                             "endDate":castToBatchLotDataDate(dl.getEndDate()),"productGroupName":dl.getProductGroupName(), "productHierarchy":dl.getProductHierarchy(),
                             "productHierarchyId":dl.getProductHierarchyId(),
                             "etlStatus":etlStatus,
                             "validationError":dataInfo
                ])
            }
        }

        Map columns = ["batchId":"Batch No.","batchDate":"Batch Date",
                       "productId":"Product Id", "product":"Product", "description":"Description", "bulkBatch":"Bulk Batch",
                       "bulkBatchDate":"Bulk Batch Date", "fillBatch":"Fill Batch", "fillBatchName":"Fill Batch Name",
                       "fillExpiry":"Fill Expiry", "fillUnits":"Fill Units", "packageBatch":"Package Batch",
                       "packageCountry":"Package Country", "packageUnit":"Package Unit", "packageReleaseDate":"Package Release Date",
                       "shippingBatch":"Shipping Batch", "componentBatch":"Component Batch", "startDate":"Start Date",
                       "endDate":"End Date", "etlStatus":"Import Result","validationError":"Error Detail"]

        mapList
    }

    private String castToBatchLotDataDate(String strDate) {
        String formattedDate = getFormatedDateRangeDate(strDate);
        if(formattedDate == "") {
            formattedDate = strDate;
        }
        formattedDate
    }

    List<Map> getBatchExcelData(List<BatchLotStatus> batchLotStatusList, LinkedHashMap columnNameMap, boolean isETLEcompleted) {
        List<Map> mapList = []
        String info=""
        String importResult=""
        batchLotStatusList.eachWithIndex { item, index ->
            Map row = [:]
            for (Map.Entry<String, Object> entry : columnNameMap.entrySet()) {
                if(entry.getKey().equals("etlStatus")) {
                    if(item.getProperty("validationError")==null
                            || item.getProperty("validationError").trim().equals("")
                            || item.getProperty("validationError").trim().equals("ETL Successfully completed")
                            || item.getProperty("validationError").trim().equals("API Successfully completed")
                            || item.getProperty("validationError").trim().equals("MISMATCH DATA")
                            || item.getProperty("validationError").trim().equals("DUPLICATE DATA")
                    ) {
                        importResult = "Success"
                    } else {
                        importResult="Failed"
                    }
                    row.put(entry.getKey(), importResult)
                } else if(entry.getKey().equals("validationError")) {
                    if(item.getProperty(entry.getKey())!=null && !item.getProperty(entry.getKey()).trim().equals("")) {
                        info = item.getProperty(entry.getKey())
                        if(info.trim().equals("MISMATCH DATA")) {
                            info=""
                        } else if(info.trim().equals("DUPLICATE DATA")) {
                            info=""
                        }
                        if(info.indexOf("MISMATCH DATA")!=-1) {
                            info = info.replace("MISMATCH DATA","")
                        }
                        if(info.indexOf("DUPLICATE DATA")!=-1) {
                            info = info.replace("DUPLICATE DATA","")
                        }
                        if(info.trim().equals("")) {
                            info = "Successfully Processed"
                        }
                    } else {
                        info = "Successfully Processed"
                    }
                    Set<String> infoSet = [];
                    info.split("\n").each { ln ->
                        infoSet.add(ln)
                    }
                    row.put(entry.getKey(), infoSet.join("\n"))
                } else {
                    String keyValue = item.getProperty(entry.getKey())==null?"":item.getProperty(entry.getKey());
                    List<String> dateColumns = Holders.config.signal.batchSignal.data.date.columns;
                    dateColumns.each { columnName ->
                        if(columnName.equals(entry.getKey())) {
                            keyValue = castToBatchLotDataDate(keyValue);
                        }
                    }
                    row.put(entry.getKey(), keyValue)
                }
            }
            mapList.add(row)
        }
        mapList
    }

    List<Map> getBatchExcelDataForETL(List<BatchLotStatus> batchLotStatusList, LinkedHashMap columnNameMap, boolean isETLEcompleted) {
        List batchLotStatusIdList = []
        batchLotStatusList.each {
            batchLotStatusIdList.add(it.batchLotId)
        }
        List<BatchLotStatus> batchLotStatusList2 = BatchLotStatus.createCriteria().list { 'in'("id", batchLotStatusIdList) }
        List<Map> mapList = []
        String info=""
        String importResult=""
        batchLotStatusList2.each { bl ->
            BatchLotData.createCriteria().list {'in'("batchLotId",bl.id)}.eachWithIndex { item, index ->
                Map row = [:]
                for (Map.Entry<String, Object> entry : columnNameMap.entrySet()){
                    if(entry.getKey().equals("etlStatus")) {
                        if("COMPLETED".equals(bl.getEtlStatus()) && "ERROR".equals(item.getEtlStatus()) ) {
                            importResult = "Not processed"
                        }
                        else if (!StringUtils.isEmpty(item.getValidationError())){
                            importResult = "Failed"
                        }
                        else if ("COMPLETED".equals(bl.getEtlStatus()) && !"ERROR".equals(item.getEtlStatus()))
                        {
                            importResult = "Success"
                        }
                        else if ("STARTED".equals(bl.getEtlStatus()) && !"ERROR".equals(item.getEtlStatus())){
                            importResult = "Scheduled"

                        }
                        else if ("FAILED".equals(bl.getEtlStatus()) && item.getEtlStatus()==null)
                        {
                            importResult = "ETL_Failed"
                        }
                        else {
                            importResult = "Failed"
                        }
                        row.put(entry.getKey(), importResult)
                    }
                    else if (entry.getKey().equals("validationError")){
                            info = item.getProperty(entry.getKey())
                            if("Not processed".equals(importResult) && (info.indexOf("MISMATCH DATA")!=-1 || info.indexOf("DUPLICATE DATA")!=-1)){
                                info = "Invalid Mapping"
                            }
                            else if ("Failed".equals(importResult)){
                                if(info.indexOf("MISMATCH DATA")!=-1){
                                    info = info.replace("MISMATCH DATA","Invalid Mapping")
                                }
                                if(info.indexOf("DUPLICATE DATA")!=-1) {
                                    info = info.replace("DUPLICATE DATA","Invalid Mapping")
                                }
                            }
                            else if(importResult == "ETL_Failed" && info == "") {
                                importResult = "Failed"
                                info = "Record failed due to database error"
                            }
                            else if("Scheduled".equals(importResult)) {
                                info = "ETL is running"
                            } else {
                                info = "Successfully Processed"
                            }
                        Set<String> infoSet = [];
                        info.split("\n").each { ln ->
                            infoSet.add(ln)
                        }
                        row.put(entry.getKey(), infoSet.join("\n"))
                        }
                    else {
                        String keyValue = item.getProperty(entry.getKey())==null?"":item.getProperty(entry.getKey());
                        List<String> dateColumns = Holders.config.signal.batchSignal.data.date.columns;
                        dateColumns.each { columnName ->
                            if(columnName.equals(entry.getKey())) {
                                keyValue = castToBatchLotDataDate(keyValue);
                            }
                        }
                        row.put(entry.getKey(), keyValue)
                    }
                }
                mapList.add(row)
            }
        }
        mapList
    }

    LinkedHashMap getBatchDataColumnNameMap() {
        LinkedHashMap map = Holders.config.signal.batchSignal.csv.columns
        map
    }
    LinkedHashMap getBatchDataForETLColumnNameMap() {
        LinkedHashMap map = Holders.config.signal.batchSignal.etl.csv.columns
        map
    }
    def getCSVString(List<BatchLotData> batchLotDataList, LinkedHashMap columnNameMap) {
        StringBuffer sb = new StringBuffer();
        String extraWhiteSpace = "";
        if(!CollectionUtils.isEmpty(batchLotDataList)) {
            Class clazz = BatchLotData.class
            columnNameMap.each{
                sb.append("\""+it.value+"\",")
            }
            sb.append("\n");
            batchLotDataList.eachWithIndex { item, index ->
                for (Map.Entry<String, Object> entry : columnNameMap.entrySet()) {
                    sb.append("\""+ (item.getProperty(entry.getKey())==null?"":item.getProperty(entry.getKey()) ) +"\",")
                }
                sb.append("\n");
                extraWhiteSpace=extraWhiteSpace+"  ";
            }
        }
        sb.toString()+extraWhiteSpace
    }
    String getValue(Object obj) {
        return obj==null?"":String.valueOf(obj);
    }
    def saveAuditTrail(BatchLotCO batchLotCO, Long batchLotId, Date uploadedAt, String batchLotClientDataString, User user, Long validRecordCount) {
        try {
            Session session = sessionFactory.currentSession
            BatchLotStatus batchLotStatus = getBatchLotStatusById(batchLotId);
            List auditChildrenForETL = getAuditChilrenForETL(batchLotStatus, user)
            signalAuditLogService.createAuditLog([
                    entityName : Constants.AuditLog.BATCH_LOT_ENTITY,
                    moduleName : Constants.AuditLog.AUDIT_CONTROL_PANEL,
                    category   : AuditTrail.Category.INSERT.toString(),
                    entityValue: "{ batchId:"+batchLotCO.getBatchId()+", batchDate: " +
                            DateUtil.toDateStringPattern(batchLotCO.getBatchDate(),DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                            (validRecordCount==null?"":", processed:"+validRecordCount) +
                            ", total count:"+batchLotCO.getCount()+
                            ",processedAt:" + DateUtil.toDateStringPattern(uploadedAt,DateUtil.DATEPICKER_FORMAT_AM_PM_4)+"}",
            ] as Map, auditChildrenForETL)

            AuditTrailChild auditTrailChild = null
            if(batchLotCO.getClientDatas()!=null && batchLotCO.getClientDatas().size()>0) {
                batchLotCO.getClientDatas().each {
                    auditTrailChild = new AuditTrailChild()
                    auditTrailChild.newValue = objectToString(it)
                    auditTrailChild.auditTrail = auditTrail
                    auditTrailChild.save()
                }
            } else if(batchLotClientDataString!=null) {
                auditTrailChild = new AuditTrailChild()
                auditTrailChild.newValue = batchLotClientDataString
                auditTrailChild.auditTrail = auditTrail
                auditTrailChild.save()
            }
            session.flush()
            session.clear()
        } catch(ValidationException ve) {
            log.error(ve.toString())
        }
    }
    def saveETLAuditTrail(BatchLotStatus batchLotStatus, User user) {
        try {
            Session session = sessionFactory.currentSession
            List auditChildrenForETL = getAuditChilrenForETL(batchLotStatus, user)
            signalAuditLogService.createAuditLog([
                    entityName : Constants.AuditLog.BATCH_LOT_ENTITY,
                    moduleName : Constants.AuditLog.AUDIT_CONTROL_PANEL,
                    category   : AuditTrail.Category.INSERT.toString(),
                    entityValue: Constants.AuditLog.BATCH_LOT_ENTITY_VALUE,
            ] as Map, auditChildrenForETL)
            session.flush()
            session.clear()
        } catch (Exception exception) {
            log.info("Error occurred while creating Audit log for Run ETL")
            log.error(exception.toString())
        }
    }

    List<Map> getAuditChilrenForETL(BatchLotStatus batchLotStatus,User user) {
        List<Map> auditChildList = []
        try {
            def childEntry = [:]
            childEntry = [
                    propertyName: "batchId",
                    newValue    : batchLotStatus.getBatchId() ?: ""]
            auditChildList << childEntry
            childEntry = [
                    propertyName: "processed",
                    newValue    : batchLotStatus.getValidRecordCount() ?: ""]
            auditChildList << childEntry
            childEntry = [
                    propertyName: "totalCount",
                    newValue    : batchLotStatus.getCount() ?: ""]
            auditChildList << childEntry
            return auditChildList
        } catch (Exception exception) {
            exception.printStackTrace()
            return []
        }
    }

    def runEtl(String batchId) {
        ResponseDTO responseDTO =  new ResponseDTO(code: 200, status: true)
        final Sql sql
        try {
            log.info("In def runEtl(String batchId) ")
            sql = new Sql(dataSource_pva)
            def procedure = "call pkg_pvs_bs_etl.p_pvs_bs_main(?)"
            log.info("p_pvs_bs_main called "+batchId)
            sql.call("{${procedure}}", [batchId]) { result ->
                log.info("Etl  procedure result. "+result)
            }
            log.info("p_pvs_bs_main COMPLETED "+batchId)
            responseDTO.status=true
        } catch (Exception ex) {
            ex.printStackTrace()
            responseDTO.status=false
            BatchLotStatus."pva".withTransaction {
                Session session = sessionFactory_pva.currentSession
                SQLQuery sqlQuery = session.createSQLQuery(SignalQueryHelper.batch_lot_status_update_to_failed(batchId))
                String.valueOf(sqlQuery.executeUpdate())
                session.flush()
                session.clear()
            }
        }
        responseDTO
    }

    void updateBatchLotValidCount(String strBatchLotId) {
        Long batchLotId = Long.valueOf(strBatchLotId)
        List<BatchLotData> batchLotDataList = BatchLotData.createCriteria().list { eq("batchLotId", batchLotId) };
        int count = 0;
        int validRecordCount = 0;
        int inValidRecordCount = 0;
        batchLotDataList.each { dl ->
            if( !StringUtils.isEmpty(dl.getValidationError()) ) {
                inValidRecordCount = inValidRecordCount+1;
            } else {
                validRecordCount = validRecordCount+1;
            }
            count=count+1
        }
        BatchLotStatus batchLotStatus = getBatchLotStatusById(batchLotId);
        batchLotStatus.setValidRecordCount(validRecordCount)
        batchLotStatus.setInvalidRecordCount(inValidRecordCount)
        String updatedRecordCount = "0"
        BatchLotStatus."pva".withTransaction {
            Session session = sessionFactory_pva.currentSession
            SQLQuery sqlQuery = session.createSQLQuery(SignalQueryHelper.update_batch_lot_status_count(batchLotStatus))
            updatedRecordCount = String.valueOf(sqlQuery.executeUpdate())
            session.flush()
            session.clear()
            log.info("updatedRecordCount="+updatedRecordCount+", Batch lot valid (id:"+batchLotStatus.getId()+") count is updated for valid count to "+batchLotStatus.getValidRecordCount()+" and for invalid count to "+batchLotStatus.getInvalidRecordCount())
        }
    }

    Integer getIntegerValue(String stringNum) {
        Integer intValue = null
        try {
            intValue = stringNum.toInteger()
        } catch (Exception ex) {
            log.error(ex.toString())
        }
        intValue
    }
}