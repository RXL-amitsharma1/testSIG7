package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.SingleCaseAlert

import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.commons.lang.WordUtils
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification

import java.text.SimpleDateFormat

import static com.rxlogix.util.MiscUtil.calcDueDate

/**
 * Handles logic behind deletion of alert and associated data
 */
@Transactional
class AlertDeletionDataService {

    ArrayList<Long> currentlyRunningIds = []
    def CRUDService
    def dataSource
    def reportIntegrationService
    def alertService
    def userService
    def aggregateCaseAlertService
    def spotfireService
    def alertAdministrationService
    def archiveService
    def dataSource_faers
    def dataSource_pva
    def sessionFactory
    def cacheService
    def singleCaseAlertService


    List<Long> getExecutedConfigurationId(Long configId, Boolean isLatest = true) {
        Configuration configuration = Configuration.get(configId as Long)
        List<Long> exConfigIds = []
        if (configuration) {
            exConfigIds = ExecutedConfiguration.createCriteria().list {
                projections {
                    property('id')
                }
                eq('configId', configId as Long)
                order('dateCreated', 'desc')
                if (isLatest) {
                    maxResults(1)
                }
            } as List<Long>

        }
        return exConfigIds
    }

    List<Long> getExecutedEvdasConfigurationId(Long configId, Boolean isLatest = true) {
        EvdasConfiguration configuration = EvdasConfiguration.get(configId as Long)
        List<Long> exConfigIds = []
        if (configuration) {
            exConfigIds = ExecutedEvdasConfiguration.createCriteria().list {
                projections {
                    property('id')
                }
                eq('configId', configId as Long)
                order('dateCreated', 'desc')
                if (isLatest) {
                    maxResults(1)
                }
            } as List<Long>
        }
        return exConfigIds
    }

    List<Long> getExecutedLiteratureConfigId(Long configId, Boolean isLatest = true) {
        LiteratureConfiguration configuration = LiteratureConfiguration.get(configId as Long)
        List<Long> exConfigIds = []
        if (configuration) {
            exConfigIds = ExecutedLiteratureConfiguration.createCriteria().list {
                projections {
                    property('id')
                }
                eq('configId', configId as Long)
                order('dateCreated', 'desc')
                if (isLatest) {
                    maxResults(1)
                }
            } as List<Long>
        }
        return exConfigIds
    }

    List<Long> getExecutedConfigurationIds(Long configId, Boolean isLatest = true) {
        Configuration configuration = Configuration.get(configId as Long)
        List<Long> exConfigIds = []
        if (configuration) {
            exConfigIds = ExecutedConfiguration.createCriteria().list {
                projections {
                    property('id')
                }
                eq('configId', configId as Long)
                order('dateCreated', 'desc')
                if (isLatest) {
                    maxResults(1)
                }
            } as List<Long>
            configuration.deletionInProgress = true
            configuration.deletionStatus = DeletionStatus.READY_TO_DELETE
            CRUDService.update(configuration)
        }
        return exConfigIds
    }

    List<Long> getEvdasExConfigIds(Long configId, Boolean isLatest = true) {
        EvdasConfiguration configuration = EvdasConfiguration.get(configId as Long)
        List<Long> exConfigIds = []
        if (configuration) {
            exConfigIds = ExecutedEvdasConfiguration.createCriteria().list {
                projections {
                    property('id')
                }
                eq('configId', configId as Long)
                order('dateCreated', 'desc')
                if (isLatest) {
                    maxResults(1)
                }
            } as List<Long>
            configuration.deletionInProgress = true
            configuration.deletionStatus = DeletionStatus.READY_TO_DELETE
            CRUDService.update(configuration)
        }
        return exConfigIds
    }

    List<Long> getExecutedLitConfigIds(Long configId, Boolean isLatest = true) {
        LiteratureConfiguration configuration = LiteratureConfiguration.get(configId as Long)
        List<Long> exConfigIds = []
        if (configuration) {
            exConfigIds = ExecutedLiteratureConfiguration.createCriteria().list {
                projections {
                    property('id')
                }
                eq('configId', configId as Long)
                order('dateCreated', 'desc')
                if (isLatest) {
                    maxResults(1)
                }
            } as List<Long>
            configuration.deletionInProgress = true
            configuration.deletionStatus = DeletionStatus.READY_TO_DELETE
            CRUDService.update(configuration)
        }
        return exConfigIds
    }

    List<Long> getExecutedConfigIds(String alertType, Long configId, Boolean isLatest = true) {
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            return getExecutedConfigurationIds(configId, isLatest)
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            return getEvdasExConfigIds(configId, isLatest)
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            return getExecutedLitConfigIds(configId, isLatest)
        }
        return []
    }

    void saveData(String alertType, String justification, List<Long> exConfigIds, Long configId, Boolean isMaster = false){
        try{
            AlertDeletionData alertDeletionData = new AlertDeletionData(exConfigIds.join(','), configId, alertType, justification, isMaster)
            alertDeletionData.save(flush: true)
            exConfigIds.each { exConfigId ->
                AlertDeleteEntry alertDeleteEntry = new AlertDeleteEntry(exConfigId, alertDeletionData)
                alertDeleteEntry.save(flush: true)
            }
        } catch(Exception ex){
            log.error("Error occoured while saving AlertDeletionData", ex)
            ex.printStackTrace()
        }
    }

    String getExConfigIds(Long masterId, Boolean isLatest = true) {
        Sql sql
        List ids = []
        try {
            sql = new Sql(dataSource)
            String query = SignalQueryHelper.getExConfigIdsForMasterAlertQuery(masterId, isLatest)
            ids = sql.rows(query)
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        return ids.collect { it.id }.join(',')
    }

/**
 * This method saves AlertDeletionData for Master Configurations. First fetches configuration for master configuration, then
 * fetches executed configuration for those configuration and saves the data
 * @param alertType -- Only "Aggregate Case Alert" would be supported for master alerts
 * @param justification
 * @param configIdList -- Ids of master configuration
 * @param isLatest
 */
    void saveMasterAlertDeletionData(String alertType, String justification, List<Long> configIdList, Boolean isLatest = true) {
        log.info("inside saveMasterAlertDeletionData...")
        if(alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
            configIdList.each{ masterId ->
                Configuration configuration = Configuration.createCriteria().get{
                    eq('masterConfigId', masterId as Long)
                    order('lastUpdated', 'desc')
                    maxResults(1)
                } as Configuration
                ExecutedConfiguration executedConfiguration = ExecutedConfiguration.createCriteria().get{
                    eq('configId', configuration.id)
                    order('lastUpdated', 'desc')
                    maxResults(1)
                } as ExecutedConfiguration
                String exIds = getExConfigIds(masterId as Long, isLatest)
                if(configuration && executedConfiguration){
                    AlertDeletionData alertDeletionData = new AlertDeletionData(exIds, configuration.id, alertType, justification, true)
                    alertDeletionData.save(flush: true)

                    AlertDeleteEntry alertDeleteEntry = new AlertDeleteEntry(executedConfiguration.id, alertDeletionData)
                    alertDeleteEntry.save(flush: true)
                }
            }
            log.info("successfully saved master" + alertType + " deletion data for: " + configIdList.size() + " alerts")
        } else {
            log.info("saveMasterAlertDeletionData Not Supported for ${alertType}!!!")
        }
    }

/**
 * This method saves AlertDeletionData for Configurations. First fetches executed configuration for the
 * configurations and then saves the data
 * @param alertType
 * @param justification
 * @param configIdList
 * @param isLatest
 */
    void saveAlertDeletionData(String alertType, String justification, List<Long> configIdList, Boolean isLatest = true) {
        log.info("inside saveAlertDeletionData...")
        try {
            if (configIdList) {
                configIdList.each { configId ->
                    List<Long> exConfigIds = getExecutedConfigIds(alertType, configId as Long, isLatest)
                    if(exConfigIds.size()>0) {
                        saveData(alertType, justification, exConfigIds, configId as Long, false)
                    } else {
                        log.info("No Executed Configuration found to delete for configuration: ${configId}, hence not saving!!!")
                    }
                }
            } else {
                log.info("Found 0 alert deletion data to save...")
            }
            log.info("successfully saved " + alertType + " deletion data for: " + configIdList.size() + " alerts")
        } catch (Exception ex) {
            log.error("Failed to save deletion data with error: " + ex.printStackTrace())
        }
    }

/**
 * Get case series IDs associated with executed configuration
 * @param exConfigId
 * @param isCaseSeries
 * @return
 */
    String getReportsData(Long exConfigId, Boolean isCaseSeries=false) {
        Sql sql
        List ids = []
        String query
        try {
            sql = new Sql(dataSource)
            if(isCaseSeries){
                query = SignalQueryHelper.getCaseSeriesIds(exConfigId)
            } else {
                query = SignalQueryHelper.getReportId(exConfigId)
            }
            ids = sql.rows(query)
        } catch (Exception ex) {
             ex.printStackTrace()
        } finally {
            sql.close()
        }
        return ids.collect { it.id }.join(',')
    }

/**
 * Soft deletes generated case series and reports from PV Reports for Single and Aggregate Alert
 * @param caseSeriesIds
 * @param reportIds
 * @return
 */
    Boolean deleteCaseSeriesFromReports(String caseSeriesIds, String reportIds) {
        //todo: check if pvr is up, then call the API
        log.info("inside deleteAssociatedReportsForAlerts!!!")
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.api.delete.configuration
        Map query = [ids: caseSeriesIds, reportId: reportIds]
        def response = reportIntegrationService.get(url, path, query)
        if (response.status == 200) {
            log.info("successfully completed deleteAssociatedReportsForAlerts for caseSeriesIds: ${caseSeriesIds} and report Ids: ${reportIds}" )
            log.info("Status message from pvr: " + response.data)
            return true
        }
        log.info("failed to deleteAssociatedReportsForAlerts for caseSeriesIds: ${caseSeriesIds} and report Ids: ${reportIds}")
        return false
    }

    List<Long> getCaseIds(Long exConfigId) {
        log.info("inside getCaseNumbers for exConfigId: " + exConfigId)
        List<Long> caseIds = SingleCaseAlert.createCriteria().list {
            projections {
                property('caseId')
            }
            eq('executedAlertConfiguration.id', exConfigId)
        } as List<Long>
        log.info("Got " + caseIds.size() + " Cases to insert in GTTs")
        return caseIds
    }

    List<Map> getAggregateAlertData(Long exConfigId) {
        log.info("inside getAggregateAlertData for exConfigId: " + exConfigId)
        List<Map> aggAlertData = AggregateCaseAlert.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("productId", "productId")
                property("ptCode", "ptCode")
                property("smqCode", "smqCode")
                property("prodHierarchyId", "prodHierarchyId")
                property("eventHierarchyId", "eventHierarchyId")
            }
            eq('executedAlertConfiguration.id', exConfigId)
        } as List<Map>
        log.info("Got " + aggAlertData.size() + " PECs to insert in GTTs")
        return aggAlertData
    }

    Boolean deleteAggAlertCat(Sql sql, Long exConfigId, ExecutedConfiguration ec) {
        try {
            log.info("inside deleteAggAlertCat for id: " + exConfigId)
            StringBuilder insertStatement = new StringBuilder()
            List<Map> aggAlertData = getAggregateAlertData(exConfigId)
            insertStatement.append("Begin execute immediate('delete from GTT_P_CAT_FACT_PE_G_DEL'); ")
            aggAlertData.each { data ->
                insertStatement.append("INSERT INTO GTT_P_CAT_FACT_PE_G_DEL(APP_PRODUCT_ID, APP_EVENT_ID, APP_SMQ, APP_EVT_HIERARCHY_ID, APP_PROD_HIERARCHY_ID) VALUES ('${data.productId}', '${data.ptCode}', '${data.smqCode}', '${data.eventHierarchyId}', '${data.prodHierarchyId}'); ")
            }
            insertStatement.append("END;")
            sql.execute(insertStatement.toString())
            log.info("GTT_P_CAT_FACT_PE_G_DEL insertion completed!!!")
            log.info("Current alert parameters : ")
            log.info("Execution id : " + ec.id)
            log.info("selectedDatasource : " + ec.selectedDatasource)
            log.info("pvrCaseSeriesId : " + ec.pvrCaseSeriesId)
            log.info("dateCreated : " + ec.dateCreated.toString())
            sql.call("{call pkg_category_delete.P_CAT_FACT_PE_G_DELETE(?,?)}", [ec.selectedDatasource, ec.dateCreated.toString()])
            sql.call("{call pkg_category_delete.P_CAT_FACT_PE_A_DELETE(?,?,?)}", [ec.selectedDatasource, ec.dateCreated.toString(), exConfigId])
            List<Long> prevExecConfigId = alertService.fetchPrevExecConfigId(ec, Configuration.get(ec.configId), false, true) as List<Long>
            Long previousExConfigId = (prevExecConfigId.size() > 0 ? prevExecConfigId[0] : 0) as Long
            ExecutedConfiguration previousExConfig = ExecutedConfiguration.get(previousExConfigId)
            if(previousExConfig) {
                log.info("Previous alert parameters : ")
                log.info("Previous Execution id : " + previousExConfig.id)
                log.info("selectedDatasource : " + previousExConfig.selectedDatasource)
                log.info("pvrCaseSeriesId : " + previousExConfig.pvrCaseSeriesId)
                log.info("dateCreated : " + previousExConfig.dateCreated.toString())
                sql.call("{call pkg_category_delete.P_CAT_FACT_PE_G_DELETE(?,?)}", [ec.selectedDatasource, ec.dateCreated.toString()])
                sql.call("{call pkg_category_delete.P_CAT_FACT_PE_A_DELETE(?,?,?)}", [ec.selectedDatasource, ec.dateCreated.toString(), previousExConfig?.id])
            }
            log.info("pkg_category_delete call completed for Aggregate Case Alert!!!")
        } catch (Exception exception) {
            log.error("Error occoured while deleteAggAlertCat: " + exception.printStackTrace(), exception)
            exception.printStackTrace()
            return false
        }
        return true
    }

    void deleteGlobalComments(Sql sql, Date dateCreated, List<Long> caseIds) {
        try {
            log.info("deleteGlobalComments method calling...")
            StringBuilder insertStatement = new StringBuilder()
            insertStatement.append("Begin execute immediate('delete from GTT_COMMENT_CASE_G_DEL');")
            caseIds.each { caseId ->
                insertStatement.append("INSERT INTO GTT_COMMENT_CASE_G_DEL(APP_TENANT_ID, APP_CASE_ID) VALUES (${1}, ${caseId});")
            }
            insertStatement.append("END;")
            sql.execute(insertStatement.toString())
            log.info("GTT_COMMENT_CASE_G_DEL insertion completed!!!")
            sql.call("{call P_COMMENT_CASE_G_DELETE(?)}", [dateCreated.toString()])
            log.info("Called P_COMMENT_CASE_G_DELETE successfully!!!")
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    Boolean deleteSingleAlertCat(Sql sql, Long exConfigId, ExecutedConfiguration ec) {
        try {
            log.info("inside deleteSingleAlertCat for id: " + exConfigId)
            StringBuilder insertStatement = new StringBuilder()
            List<Long> caseIds = getCaseIds(exConfigId)
            insertStatement.append("Begin execute immediate('delete from GTT_P_CAT_FACT_CASE_G_DEL');")
            caseIds.each { caseId ->
                insertStatement.append("INSERT INTO GTT_P_CAT_FACT_CASE_G_DEL(APP_TENANT_ID, APP_CASE_ID) VALUES (${1},${caseId});")
            }
            insertStatement.append("END;")
            sql.execute(insertStatement.toString())
            log.info("GTT_P_CAT_FACT_CASE_G_DEL insertion completed!!!")
            log.info("Current alert parameters : ")
            log.info("Execution id : " + ec.id)
            log.info("selectedDatasource : " + ec.selectedDatasource)
            log.info("pvrCaseSeriesId : " + ec.pvrCaseSeriesId)
            log.info("dateCreated : " + ec.dateCreated.toString())
            sql.call("{call pkg_category_delete.P_CAT_FACT_CASE_G_DELETE(?,?)}", [ec.selectedDatasource, ec.dateCreated.toString()])
            sql.call("{call pkg_category_delete.P_CAT_FACT_CASE_A_DELETE(?,?,?)}", [ec.selectedDatasource, ec.dateCreated.toString(), ec.pvrCaseSeriesId])
            log.info("P_CAT_FACT_CASE_G_DELETE & P_CAT_FACT_CASE_A_DELETE call finished.")
            List<Long> prevExecConfigId = alertService.fetchPrevExecConfigId(ec, Configuration.get(ec.configId), false, true) as List<Long>
            Long previousExConfigId = (prevExecConfigId.size() > 0 ? prevExecConfigId[0] : 0) as Long
            log.info("previousExConfigId is : "+previousExConfigId)
            ExecutedConfiguration previousExConfig = ExecutedConfiguration.get(previousExConfigId)
            log.info("previousExConfig is : "+previousExConfig)
            if (previousExConfig) {
                log.info("Previous alert parameters : ")
                log.info("Previous Execution id : " + previousExConfig.id)
                log.info("selectedDatasource : " + previousExConfig.selectedDatasource)
                log.info("pvrCaseSeriesId : " + previousExConfig.pvrCaseSeriesId)
                log.info("dateCreated : " + previousExConfig.dateCreated.toString())
                sql.call("{call pkg_category_delete.P_CAT_FACT_CASE_G_DELETE(?,?)}", [ec.selectedDatasource, ec.dateCreated.toString()])
                sql.call("{call pkg_category_delete.P_CAT_FACT_CASE_A_DELETE(?,?,?)}", [ec.selectedDatasource, ec.dateCreated.toString(), previousExConfig?.pvrCaseSeriesId])
            }
            log.info("pkg_category_delete call completed for Single Case Alert!!!")
            deleteGlobalComments(sql, ec.dateCreated, caseIds)
            if (previousExConfig) {
                deleteGlobalComments(sql, previousExConfig.dateCreated, caseIds)
            }

        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Error occoured while deleteSingleAlertCat: " + ex.printStackTrace())
            return false
        }
        return true
    }

    Boolean invokeMartDataDeletion(Long exConfigId, String alertType, String masterExecutionIds, Boolean isMaster = false) {
        ExecutedConfiguration ec = ExecutedConfiguration.get(exConfigId)
        Sql sql
        if (ec.selectedDatasource.startsWith(Constants.DataSource.FAERS)) {
            sql = new Sql(dataSource_faers)
        } else {
            sql = new Sql(dataSource_pva)
        }
        Boolean isCatDeleted = true
        if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            if(isMaster){
                List<Long> ids = masterExecutionIds.split(',') as List<Long>
                ids.each{ id->
                    ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(id as Long)
                    isCatDeleted = deleteAggAlertCat(sql, id as Long, executedConfiguration)
                }
            } else {
                isCatDeleted = deleteAggAlertCat(sql, exConfigId, ec)
            }
            try {
                long startTime = System.currentTimeMillis()
                if(isMaster){
                     sql.call("{call p_drop_objects_alert_all_data(?,?)}", [ec.masterExConfigId, 1])
                } else{
                    sql.call("{call p_drop_objects_alert_all_data(?,?)}", [exConfigId, 0])
                }
                long endTime = System.currentTimeMillis()
                log.info("Time taken for p_drop_objects_alert_all_data: "+ (endTime-startTime)/1000 +" sec")
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("Error occoured while calling p_drop_objects_alert_all_data: " + ex.printStackTrace())
                return false
            }
        } else if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            isCatDeleted = deleteSingleAlertCat(sql, exConfigId, ec)
        }
        sql?.close()
        return isCatDeleted
    }

    boolean deleteSpotfireReportsForAlerts(Long exConfigId) {
        List<String> fileNames = spotfireService.getSpotfireNames(ExecutedConfiguration.get(exConfigId)).values().collect { "'" + it?.replace("+"," ") + "'" }
        if (fileNames.size() > 0) {
            List<String> descriptions = spotfireService.getReportFilesDescriptionByName(fileNames.join(','))
            if (descriptions.size() > 0) {
                return spotfireService.deleteSfReports(descriptions)
            }
        }
        return true
    }

    Boolean migrateAlertDataForDeletionForMaster(Long currentMasterId, Long previousMasterId) {
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            long startTime = System.currentTimeMillis()
            sql.call("{call MOVE_MASTER_ALERT_FROM_ARCHIVE(?,?)}", [currentMasterId, previousMasterId])
            long endTime = System.currentTimeMillis()
            log.info("MOVE_MASTER_ALERT_FROM_ARCHIVE call completed in time: "+ (endTime-startTime)/1000 +" sec")
        } catch (Exception ex) {
            log.error("Error occoured while deleting master alert data", ex)
            return false
        } finally {
            sql?.close()
        }
        return true
    }

    Boolean migrateAlertDataForDeletion(Long exConfigId, String alertType, Long prevExConfigId, Long configId,def domain) {
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            archiveService.moveAttachementFromArchive(domain, configId,prevExConfigId,sql)
            log.info("Before calling MOVE_ALERT_FROM_ARCHIVE procedure.")
            log.info("exConfigId : "+exConfigId+" prevExConfigId : "+prevExConfigId+" configId : "+configId+" alertType : "+alertType)
            sql.call("{call MOVE_ALERT_FROM_ARCHIVE(?,?,?,?)}", [exConfigId, prevExConfigId, configId, alertType])
            log.info("MOVE_ALERT_FROM_ARCHIVE call completed for " + alertType + " with exConfigId: " + exConfigId)
        } catch (Exception ex) {
            log.error("Error occoured while deleting alert data for: " + alertType + " with exConfigId: " + exConfigId, ex)
            return false
        } finally {
            sql?.close()
        }
        return true
    }

    void deleteLiteratureAlert(AlertDeletionData alertDeletionData) {
        Long currentConfigId = alertDeletionData.configId
        currentlyRunningIds.add(currentConfigId)
        Integer alertsToDelete
        try {
            LiteratureConfiguration configuration = LiteratureConfiguration.get(currentConfigId)
            configuration.setIsEnabled(false)
            if (configuration.nextRunDate != null) {
                log.info("Unscheduling LiteratureConfiguration: " + currentConfigId)
                configuration.nextRunDate = null
            }
            configuration.deletionStatus = DeletionStatus.DELETION_IN_PROGRESS
            CRUDService.update(configuration)
            List<AlertDeleteEntry> alertDeleteEntryList = alertDeletionData.getAlertDeleteEntries()?.toList()?.findAll { it.alertDeletionStatus == AlertDeletionStatus.CREATED }
            alertsToDelete = alertDeleteEntryList.size()
            alertDeleteEntryList.each { alert ->
                alert.alertDeletionStatus = AlertDeletionStatus.DELETION_IN_PROGRESS
                ExecutedLiteratureConfiguration ec = ExecutedLiteratureConfiguration.get(alert.exConfigId)
                if (ec == null) {
                    log.info("ExecutedLiteratureConfiguration not found, removing: " + currentConfigId)
                    currentlyRunningIds.remove(currentConfigId)
                    return
                }
                log.info("Now deleting ExecutedLiteratureConfiguration: " + alert.exConfigId)
                alert.dbCompleted = true
                Long latestExecutedConfigId = getExecutedLiteratureConfigId(currentConfigId, true)[0]
                ec = ExecutedLiteratureConfiguration.get(latestExecutedConfigId)
                List<Long> prevExecConfigIds = alertService.getLiteraturePrevExConfigIds(ec, currentConfigId)
                alert.dataMigrationCompleted = migrateAlertDataForDeletion(alert.exConfigId, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT, (prevExecConfigIds.size() > 0 ? prevExecConfigIds[0] : 0) as Long, currentConfigId, LiteratureAlert)
                alert.pvrCompleted = true
                alert.spotfireCompleted = true

                if (!alert.dbCompleted || !alert.spotfireCompleted || !alert.pvrCompleted || !alert.dataMigrationCompleted) {
                    alert.isDeletionCompleted = false
                    alert.alertDeletionStatus = AlertDeletionStatus.ERROR
                } else {
                    alert.isDeletionCompleted = true
                    alert.alertDeletionStatus = AlertDeletionStatus.DELETED
                }
                CRUDService.updateWithoutAuditLog(alert)
                if (configuration.numOfExecutions > 1) {
                    configuration.numOfExecutions -= 1
                } else {
                    configuration.numOfExecutions = 0
                }
                if(alert.isDeletionCompleted){
                    saveAuditTrailForDeletion(ec, "Literature Alert Configuration", alertDeletionData.justification)
                }
                log.info("Completed deleting ExecutedLiteratureConfiguration: " + alert.exConfigId)
                ExecutedLiteratureConfiguration executedLiteratureConfiguration
                List executedLiteratureConfigurationList =    ExecutedLiteratureConfiguration.findAllByConfigIdAndIsLatest(configuration.id, true)
                if(executedLiteratureConfigurationList) {
                    executedLiteratureConfiguration = executedLiteratureConfigurationList?.first()
                    executedLiteratureConfigurationList.each {
                        if (it.id > executedLiteratureConfiguration.id) {
                            executedLiteratureConfiguration = it
                        }
                    }
                }
                if(executedLiteratureConfiguration != null){
                    configuration?.dateRangeInformation?.dateRangeStartAbsolute = executedLiteratureConfiguration?.dateRangeInformation?.dateRangeStartAbsolute
                    configuration?.dateRangeInformation?.dateRangeEndAbsolute = executedLiteratureConfiguration?.dateRangeInformation?.dateRangeEndAbsolute
                    configuration.save(failOnError: true, flush: true)
                }

            }
            Integer alertsFailedToDelete = alertDeleteEntryList.findAll { it.alertDeletionStatus == AlertDeletionStatus.ERROR }.size()
            if (alertsToDelete == alertsFailedToDelete) {
                alertDeletionData.deletionStatus = DeletionStatus.ERROR
                configuration.deletionStatus = DeletionStatus.ERROR
                alertDeletionData.deletionCompleted = false
            } else if (alertDeleteEntryList.findAll { it.alertDeletionStatus == AlertDeletionStatus.ERROR }.size() > 0) {
                alertDeletionData.deletionStatus = DeletionStatus.PARTIALLY_DELETED
                configuration.deletionStatus = DeletionStatus.PARTIALLY_DELETED
            } else {
                alertDeletionData.deletionStatus = DeletionStatus.DELETED
                configuration.deletionStatus = DeletionStatus.DELETED
            }
            configuration.deletionInProgress = false
            alertDeletionData.deletionCompleted = true
            CRUDService.updateWithoutAuditLog(alertDeletionData)
            CRUDService.update(configuration)
            currentlyRunningIds.remove(currentConfigId)
        } catch (Exception ex) {
            currentlyRunningIds.remove(currentConfigId)
            ex.printStackTrace()
        }
    }

    void deleteEvdasAlert(AlertDeletionData alertDeletionData) {
        Long currentConfigId = alertDeletionData.configId
        currentlyRunningIds.add(currentConfigId)
        Integer alertsToDelete
        try {
            EvdasConfiguration configuration = EvdasConfiguration.get(currentConfigId)
            configuration.skipAudit=true
            configuration.setIsEnabled(false)
            if (configuration.nextRunDate != null) {
                log.info("Unscheduling EvdasConfiguration: " + currentConfigId)
                configuration.nextRunDate = null
            }
            configuration.deletionStatus = DeletionStatus.DELETION_IN_PROGRESS
            CRUDService.updateWithoutAuditLog(configuration)
            List<AlertDeleteEntry> alertDeleteEntryList = alertDeletionData.getAlertDeleteEntries()?.toList()?.findAll { it.alertDeletionStatus == AlertDeletionStatus.CREATED }
            alertsToDelete = alertDeleteEntryList.size()
                alertDeleteEntryList.sort{
                it.dateCreated
            }.each { alert ->
                alert.alertDeletionStatus = AlertDeletionStatus.DELETION_IN_PROGRESS
                ExecutedEvdasConfiguration ec = ExecutedEvdasConfiguration.get(alert.exConfigId)
                if (ec == null) {
                    log.info("ExecutedEvdasConfiguration not found, removing: " + currentConfigId)
                    currentlyRunningIds.remove(currentConfigId)
                    return
                }
                log.info("Now deleting ExecutedEvdasConfiguration: " + alert.exConfigId)
                alert.dbCompleted = true
                    Long latestExecutedConfigId = getExecutedEvdasConfigurationId(currentConfigId, true)[0]
                    ec = ExecutedEvdasConfiguration.get(latestExecutedConfigId)
                    List<Long> prevExecConfigIds = alertService.getEvdasPrevExConfigIds(ec, currentConfigId)
                    alert.dataMigrationCompleted = migrateAlertDataForDeletion(alert.exConfigId, Constants.AlertConfigType.EVDAS_ALERT, (prevExecConfigIds.size() > 0 ? prevExecConfigIds[0] : 0) as Long, currentConfigId, EvdasAlert)
                alert.pvrCompleted = true
                alert.spotfireCompleted = true

                if (!alert.dbCompleted || !alert.spotfireCompleted || !alert.pvrCompleted || !alert.dataMigrationCompleted) {
                    alert.alertDeletionStatus = AlertDeletionStatus.ERROR
                    alert.isDeletionCompleted = false
                } else {
                    alert.isDeletionCompleted = true
                    alert.alertDeletionStatus = AlertDeletionStatus.DELETED
                }
                if(alert.isDeletionCompleted){
                    saveAuditTrailForDeletion(ec, "EVDAS Alert Configuration", alertDeletionData.justification)
                }
                CRUDService.updateWithoutAuditLog(alert)
                if (configuration.numOfExecutions > 1) {
                    configuration.numOfExecutions -= 1
                } else {
                    configuration.numOfExecutions = 0
                }
                log.info("Completed deleting ExecutedEvdasConfiguration: " + alert.exConfigId)
                    ExecutedEvdasConfiguration executedEvdasConfiguration = ExecutedEvdasConfiguration.findByConfigIdAndIsLatest(configuration.id, true)
                    if(executedEvdasConfiguration != null) {
                        configuration?.dateRangeInformation?.dateRangeStartAbsolute = executedEvdasConfiguration?.dateRangeInformation?.dateRangeStartAbsolute
                        configuration?.dateRangeInformation?.dateRangeEndAbsolute = executedEvdasConfiguration?.dateRangeInformation?.dateRangeEndAbsolute
                        configuration.skipAudit=true
                        configuration.save(failOnError: true, flush: true)
                    }
            }
            Integer alertsFailedToDelete = alertDeleteEntryList.findAll { it.alertDeletionStatus == AlertDeletionStatus.ERROR }.size()
            if (alertsToDelete == alertsFailedToDelete) {
                alertDeletionData.deletionStatus = DeletionStatus.ERROR
                configuration.deletionStatus = DeletionStatus.ERROR
                alertDeletionData.deletionCompleted = false
            } else if (alertDeleteEntryList.findAll { it.alertDeletionStatus == AlertDeletionStatus.ERROR }.size() > 0) {
                alertDeletionData.deletionStatus = DeletionStatus.PARTIALLY_DELETED
                configuration.deletionStatus = DeletionStatus.PARTIALLY_DELETED
            } else {
                alertDeletionData.deletionStatus = DeletionStatus.DELETED
                configuration.deletionStatus = DeletionStatus.DELETED
            }
            configuration.deletionInProgress = false
            alertDeletionData.deletionCompleted = true
            CRUDService.updateWithoutAuditLog(alertDeletionData)
            configuration.skipAudit=true
            CRUDService.update(configuration)
            currentlyRunningIds.remove(currentConfigId)
        } catch (Exception ex) {
            currentlyRunningIds.remove(currentConfigId)
            ex.printStackTrace()
        }
    }
    void updateConfigurationsForMaster(String exConfigIds, Boolean isCompleted = false){
        SQLQuery sql = null
        String sqlStatement = null
        try{
            Session session = sessionFactory.currentSession
            if(isCompleted){
                sqlStatement = SignalQueryHelper.configurationDeletionCompleted(exConfigIds)
            } else{
                sqlStatement= SignalQueryHelper.updateConfigurationDeletionStatus(exConfigIds)
            }
            sql = session.createSQLQuery(sqlStatement)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex){
            log.error("Error occoured while updateConfigurationsForMaster", ex)
        }
    }

    void deleteSingleAndAggregateAlerts(AlertDeletionData alertDeletionData) {
        Long currentConfigId = alertDeletionData.configId
        currentlyRunningIds.add(currentConfigId)
        Integer alertsToDelete
        try {
            if(alertDeletionData.isMaster){
                updateConfigurationsForMaster(alertDeletionData.exConfigId)
            }
            Configuration configuration = Configuration.get(currentConfigId)
            //unschedule configuration, then delete alert
            configuration.setIsEnabled(false)
            if (configuration.nextRunDate != null) {
                log.info("Unscheduling Configuration: " + currentConfigId)
                configuration.nextRunDate = null
            }
            configuration.deletionStatus = DeletionStatus.DELETION_IN_PROGRESS
            configuration.skipAudit = true
            CRUDService.updateWithAuditLog(configuration)

            List<AlertDeleteEntry> alertDeleteEntryList = alertDeletionData.getAlertDeleteEntries()?.toList()?.findAll { it.alertDeletionStatus == AlertDeletionStatus.CREATED }?.sort{-it.exConfigId}
            alertsToDelete = alertDeleteEntryList.size()
            alertDeleteEntryList.each { alert ->
                alert.alertDeletionStatus = AlertDeletionStatus.DELETION_IN_PROGRESS
                ExecutedConfiguration ec = ExecutedConfiguration.get(alert.exConfigId)
                if (ec == null) {
                    log.info("ExecutedConfiguration not found, removing: " + currentConfigId)
                    currentlyRunningIds.remove(currentConfigId)
                    return
                }
                log.info("Now deleting ExecutedConfiguration: " + alert.exConfigId)
                if (alertDeletionData.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                    String caseSeriesIds = getReportsData(alert.exConfigId, true)
                    String reportIds = getReportsData(alert.exConfigId, false)
                    alert.pvrCompleted = deleteCaseSeriesFromReports(caseSeriesIds, reportIds)
                }
                else if(alertDeletionData.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
                    String reportIds = getReportsData(alert.exConfigId, false)
                    alert.pvrCompleted = deleteCaseSeriesFromReports(ec?.pvrCaseSeriesId as String, reportIds)
                }
                alert.spotfireCompleted = deleteSpotfireReportsForAlerts(alert.exConfigId)
                Boolean dbCompleted = invokeMartDataDeletion(alert.exConfigId, alertDeletionData.alertType, alertDeletionData.exConfigId ,alertDeletionData.isMaster)
                alert.dbCompleted = dbCompleted
                if (alertDeletionData.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    Long latestExecutedConfigId=getExecutedConfigurationId(currentConfigId,true)[0]
                    ec=ExecutedConfiguration.get(latestExecutedConfigId)
                    List<Long> prevExecConfigId = alertService.fetchPrevExecConfigId(ec, configuration, false, true) as List<Long>
                    alert.dataMigrationCompleted = migrateAlertDataForDeletion(alert.exConfigId, Constants.AlertConfigType.SINGLE_CASE_ALERT, prevExecConfigId.size() > 0 ? prevExecConfigId[0] : 0, currentConfigId, SingleCaseAlert)
                    if (prevExecConfigId.size() > 0) {
                        ExecutedConfiguration exConfigAfterMigration = ExecutedConfiguration.findById(prevExecConfigId[0])
                        setValuesForMigratedAlertData(configuration, exConfigAfterMigration)
                        revertIsLatestForCaseHistory(prevExecConfigId, currentConfigId)
                    }
                } else if (alertDeletionData.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    List<Long> prevExecConfigId = []
                    if(alertDeletionData.isMaster){
                        Long latestExecutedConfigId=getExecutedConfigurationId(currentConfigId,true)[0]
                        ec = ExecutedConfiguration.get(latestExecutedConfigId)
                        prevExecConfigId = aggregateCaseAlertService.fetchPreviousExecConfigs(ec, currentConfigId)*.id?.sort {
                            -it
                        }
                        ExecutedConfiguration prevExConfig = null
                        if(prevExecConfigId.size() >0){
                            prevExConfig = ExecutedConfiguration.get(prevExecConfigId[0])
                        }
                        ExecutedConfiguration currExConfig = ExecutedConfiguration.get(alert.exConfigId)
                        alert.dataMigrationCompleted = migrateAlertDataForDeletionForMaster(currExConfig.masterExConfigId, prevExConfig?.masterExConfigId?:0)
                    } else {
                        prevExecConfigId = aggregateCaseAlertService.fetchPreviousExecConfigs(ec, currentConfigId)*.id?.sort { -it }
                        alert.dataMigrationCompleted = migrateAlertDataForDeletion(alert.exConfigId, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, prevExecConfigId.size() > 0 ? prevExecConfigId[0] : 0, currentConfigId, AggregateCaseAlert)
                    }
                }


                if (!alert.dbCompleted || !alert.spotfireCompleted || !alert.pvrCompleted || !alert.dataMigrationCompleted) {
                    alert.alertDeletionStatus = AlertDeletionStatus.ERROR
                    alert.isDeletionCompleted = false
                } else {
                    alert.isDeletionCompleted = true
                    alert.alertDeletionStatus = AlertDeletionStatus.DELETED
                }
                if(alert.isDeletionCompleted){
                    saveAuditTrailForDeletion(ec, ec.getModuleNameForMultiUseDomains(),alertDeletionData?.justification)
                }
                CRUDService.updateWithoutAuditLog(alert)
                if (configuration.numOfExecutions > 1) {
                    configuration.numOfExecutions -= 1
                } else {
                    configuration.numOfExecutions = 0
                }
                log.info("Completed deleting ExecutedConfiguration: " + alert.exConfigId)
                ExecutedConfiguration executedConfiguration = ExecutedConfiguration.createCriteria().get{
                        eq('configId',currentConfigId)
                    maxResults(1)
                    order("lastUpdated","desc")
                } as ExecutedConfiguration

                if(executedConfiguration != null) {
                    configuration?.alertDateRangeInformation?.dateRangeStartAbsolute = executedConfiguration?.executedAlertDateRangeInformation?.dateRangeStartAbsolute
                    configuration?.alertDateRangeInformation?.dateRangeEndAbsolute = executedConfiguration?.executedAlertDateRangeInformation?.dateRangeEndAbsolute
                    if(configuration && executedConfiguration) {
                        configuration.revertConfigurationToPreviousVersion(executedConfiguration)
                    }
                    configuration.skipAudit = true
                    configuration.save(failOnError: true, flush: true)
                }
            }
            Integer alertsFailedToDelete = alertDeleteEntryList.findAll { it.alertDeletionStatus == AlertDeletionStatus.ERROR }.size()
            if (alertsToDelete == alertsFailedToDelete) {
                alertDeletionData.deletionStatus = DeletionStatus.ERROR
                configuration.deletionStatus = DeletionStatus.ERROR
                alertDeletionData.deletionCompleted = false
            } else if (alertsFailedToDelete > 0) {
                alertDeletionData.deletionStatus = DeletionStatus.PARTIALLY_DELETED
                configuration.deletionStatus = DeletionStatus.PARTIALLY_DELETED
                alertDeletionData.deletionCompleted = false
            } else {
                alertDeletionData.deletionStatus = DeletionStatus.DELETED
                configuration.deletionStatus = DeletionStatus.DELETED
                alertDeletionData.deletionCompleted = true
            }
            if(alertDeletionData.isMaster){
                updateConfigurationsForMaster(alertDeletionData.exConfigId, true)
            }
            configuration.deletionInProgress = false
            configuration.skipAudit = true
            alertDeletionData.deletionCompleted = true
            CRUDService.updateWithoutAuditLog(alertDeletionData)
            CRUDService.updateWithAuditLog(configuration)
            currentlyRunningIds.remove(currentConfigId)
        } catch (Exception ex) {
            currentlyRunningIds.remove(currentConfigId)
            ex.printStackTrace()
        }
    }

    def saveAuditTrailForDeletion(def ec, String alertType = '',String justification='') {
        String fullClassName =ec.getClass().name
        String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        try {
            Session session = sessionFactory.currentSession
            AuditTrail auditTrail = new AuditTrail()
            auditTrail.category = AuditTrail.Category.DELETE.toString()
            auditTrail.applicationName = "PV Signal"
            auditTrail.entityId = ec?.id
            auditTrail.entityName = WordUtils.uncapitalize(simpleClassName)
            auditTrail.moduleName = alertType
            auditTrail.entityValue = "Name: ${ec.getInstanceIdentifierForAuditLog()} , Justification: ${justification}"
            auditTrail.username = userService.getUser()?.getUsername() ?: "System"
            auditTrail.fullname = userService.getUser()?.getFullName() ?: ""
            auditTrail.save()
            AuditTrailChild auditTrailChild = null
            auditTrailChild = new AuditTrailChild()
            auditTrailChild.oldValue = 'No'
            auditTrailChild.propertyName = 'isDeleted'
            auditTrailChild.newValue = 'Yes'
            auditTrailChild.auditTrail = auditTrail
            auditTrailChild.save()
        } catch(Exception ve) {
            log.error(ve.toString())
        }
    }

    void executeAlertDeletion(List<Long> currentlyRunningIdList) {

        AlertDeletionData alertDeletionData = AlertDeletionData.getNextEntryToDelete(currentlyRunningIdList)

        if (alertDeletionData != null) {
            log.info("Found " + alertDeletionData.alertType + " with confid id : " + alertDeletionData.configId + " to delete!!!")
            alertDeletionData.deletionStatus = DeletionStatus.DELETION_IN_PROGRESS
            CRUDService.updateWithoutAuditLog(alertDeletionData)
            String alertType = alertDeletionData.alertType

            if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                deleteSingleAndAggregateAlerts(alertDeletionData)
            } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                deleteEvdasAlert(alertDeletionData)
            } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                deleteLiteratureAlert(alertDeletionData)
            }
        }
    }

    void setValuesForMigratedAlertData(Configuration configuration, ExecutedConfiguration executedConfiguration) {
        log.info("Setting due date for cases in current latest execution for ex config id : " + executedConfiguration.id)
        List alertDueDateList = []
        List<SingleCaseAlert> singleCaseAlertList = SingleCaseAlert.findAllByAlertConfigurationAndExecutedAlertConfiguration(configuration, executedConfiguration)
        singleCaseAlertList.each { SingleCaseAlert sca ->
            calcDueDate(sca, configuration.priority, sca.disposition, false,
                    cacheService.getDispositionConfigsByPriority(sca.priority.id))
            alertDueDateList << [id           : sca.id, dueDate: sca.dueDate,
                                 dispositionId: sca.dispositionId, reviewDate: sca.reviewDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(sca.reviewDate) : null, caseNumber: sca.caseNumber, configId: configuration.id, execConfigId: executedConfiguration.id]
        }
        singleCaseAlertService.persistDispositionDueDate(alertDueDateList, SingleCaseAlert)
    }

    void revertIsLatestForCaseHistory(List<Long> exConfigIdList, Long configId) {
        try {
            List<Map> prevSingleCaseAlertList = alertService.fetchPrevPeriodSCAlerts(SingleCaseAlert, exConfigIdList)
            List<Map> archivedSCAlertList = alertService.fetchPrevPeriodSCAlerts(ArchivedSingleCaseAlert, exConfigIdList)
            List<CaseHistory> existingCaseHistoryList = CaseHistory.createCriteria().list {
                eq("configId", configId)
                order("id", "desc")
            } as List<CaseHistory>
            List<Map> totalCasesList = prevSingleCaseAlertList + archivedSCAlertList
            List<Long> caseHistoryUpdate = []
            totalCasesList.each { Map alertMap ->
                CaseHistory latestCaseHistory = existingCaseHistoryList.find { it.caseNumber == alertMap.caseNumber }
                if (!latestCaseHistory?.isLatest) {
                    caseHistoryUpdate.add(latestCaseHistory?.id)
                }
            }
            if (caseHistoryUpdate) {
                caseHistoryUpdate.collate(1000).each {
                    CaseHistory.executeUpdate("Update CaseHistory set isLatest = true where id in (:caseHistoryIdList)", [caseHistoryIdList: it])
                }
            }
        } catch (Exception ex) {
            log.error("Failed to revert isLatest for existing case history " + ex.printStackTrace())
        }
    }
}
