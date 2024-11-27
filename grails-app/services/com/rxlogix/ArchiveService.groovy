package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.EmbaseLiteratureAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.util.AttachmentableUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SQLQuery
import org.hibernate.Session
import com.rxlogix.signal.PvsAlertTag
import grails.util.Holders

@Transactional
class ArchiveService {
    def signalDataSourceService
    def sessionFactory
    AlertService alertService
    PvsGlobalTagService pvsGlobalTagService
    UserService userService
    CacheService cacheService
    PvsAlertTagService pvsAlertTagService
    ProductAssignmentImportService productAssignmentImportService
    def dataSource

    void moveDatatoArchive(def executedConfiguration, def domain, List<Long> prevExecConfigIds) {
        String queryString = ""
        Long start = System.currentTimeMillis()
        log.info "Running Archive Alert Method for archiving alert"
        Long executedConfigId = prevExecConfigIds ? prevExecConfigIds[0] : null
        if (executedConfigId) {
            moveAttachementToArchive(domain, executedConfiguration.configId,executedConfigId)
            Sql sql = null
            queryString = archiveSqlQuery(domain, executedConfiguration.configId, executedConfigId)
            Session session = sessionFactory.currentSession
            try {
                sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
                //Added code for pii policy
                if(domain == SingleCaseAlert) {
                    DbUtil.executePIIProcCall(sql, Constants.PII_OWNER, Constants.PII_ENCRYPTION_KEY)
                }
                //end code for pii policy
                sql.execute(queryString)
                log.info("Archiving old alert data for (C: ${executedConfiguration.configId} and EC: ${executedConfigId}) completed successfully in (${(System.currentTimeMillis() - start) / 1000}secs)")
            } catch (Throwable throwable) {
                log.error("Error while archiving old alert data for (C: ${executedConfiguration.configId} and EC: ${executedConfigId}) \n ${throwable.printStackTrace()}")
                throw throwable
            } finally {
                sql?.close()
                session.flush()
                session.clear()
            }
            moveAlertTagDataFromArchive(domain, executedConfigId, executedConfiguration)
        } else
            log.info("Not found any old alert to archive for Configuration: ${executedConfiguration.configId}.")
    }

    String archiveSqlQuery(def domain, Long configId, Long executedConfigId) {
        String queryString = ""
        switch (domain) {
            case SingleCaseAlert:
                queryString = SignalQueryHelper.sca_archived_sql(configId, executedConfigId)
                break
            case AggregateCaseAlert:
                queryString = SignalQueryHelper.aca_archived_sql(configId, executedConfigId)
                break
            case EvdasAlert:
                queryString = SignalQueryHelper.evdas_archived_sql(configId, executedConfigId)
                break
            case LiteratureAlert:
                queryString = SignalQueryHelper.literature_archived_sql(configId, executedConfigId)
                break
            case EmbaseLiteratureAlert:
                queryString = SignalQueryHelper.embase_literature_archived_sql(configId, executedConfigId)
                break
        }
        queryString
    }

    Long oldExecutedAlertId(def domain, Long configId, Long executedConfigId) {
        List<Long> lastExecutedIds = []
        if (domain == LiteratureAlert) {
            lastExecutedIds = domain.createCriteria().listDistinct {
                projections {
                    'exLitSearchConfig' {
                        property("id")
                    }
                }
                'litSearchConfig' {
                    eq("id", configId)
                }
                'exLitSearchConfig' {
                    ne('id', executedConfigId)
                }
                order("exLitSearchConfig", "desc")
            }
        } else {
            lastExecutedIds = domain.createCriteria().listDistinct {
                projections {
                    'executedAlertConfiguration' {
                        property("id")
                    }
                }
                'alertConfiguration' {
                    eq("id", configId)
                }
                'executedAlertConfiguration' {
                    ne('id', executedConfigId)
                }
                order("executedAlertConfiguration", "desc")
            }
        }
        lastExecutedIds ? lastExecutedIds.first() : 0
    }
    void moveAttachementToArchive(def domain, Long configId, Long executedConfigId){
        String sql_statement = ""
        String referenceClass = ""
        switch (domain) {
            case SingleCaseAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join SINGLE_CASE_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${executedConfigId} and t1.reference_class='com.rxlogix.signal.SingleCaseAlert'
                    """
                referenceClass = 'com.rxlogix.signal.ArchivedSingleCaseAlert'
                break
            case AggregateCaseAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join AGG_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${executedConfigId} and t1.reference_class='com.rxlogix.signal.AggregateCaseAlert'
                    """
                referenceClass = 'com.rxlogix.signal.ArchivedAggregateCaseAlert'
                break
            case EvdasAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join EVDAS_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${executedConfigId} and t1.reference_class='com.rxlogix.config.EvdasAlert'
                    """
                referenceClass = 'com.rxlogix.config.ArchivedEvdasAlert'
                break
            case LiteratureAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join LITERATURE_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${executedConfigId} and t1.reference_class='com.rxlogix.config.LiteratureAlert'
                    """
                referenceClass = 'com.rxlogix.config.ArchivedLiteratureAlert'
                break
            case EmbaseLiteratureAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join EMBASE_LITERATURE_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${executedConfigId} and t1.reference_class='com.rxlogix.config.EmbaseLiteratureAlert'
                    """
                referenceClass = 'com.rxlogix.config.ArchivedEmbaseLiteratureAlert'
                break
        }
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        try{
            sqlQuery.list().each { row ->
                AttachmentLink attachmentLink = AttachmentLink.get(row[0])
                List<Attachment> attachmentList = Attachment.findAllByLnk(attachmentLink)
                attachmentList.each { attachment ->
                    File file = AttachmentableUtil.getFile(Holders.config, attachment)
                    def newDestination = AttachmentableUtil.getDir(Holders.config, referenceClass, attachmentLink.referenceId, false)
                    if (file.exists()) {
                        productAssignmentImportService.moveFile(file, newDestination as String)
                    } else {
                        log.info("file not found: " + file.path)
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error Occurred while persisting Attachments", e)
            throw e
        } finally {
            session.flush()
            session.clear()
        }
    }

    void moveAttachementFromArchive(def domain, Long configId, Long executedConfigId,Sql sql){
        String sql_statement = ""
        String referenceClass = ""
        switch (domain) {
            case SingleCaseAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join ARCHIVED_SINGLE_CASE_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${executedConfigId} and t1.reference_class='com.rxlogix.signal.ArchivedSingleCaseAlert'
                    """
                referenceClass = 'com.rxlogix.signal.SingleCaseAlert'
                break
            case AggregateCaseAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join ARCHIVED_AGG_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${executedConfigId} and t1.reference_class='com.rxlogix.signal.ArchivedAggregateCaseAlert'
                    """
                referenceClass = 'com.rxlogix.signal.AggregateCaseAlert'
                break
            case EvdasAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join ARCHIVED_EVDAS_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIGURATION_ID = ${executedConfigId} and t1.reference_class='com.rxlogix.config.ArchivedEvdasAlert'
                    """
                referenceClass = 'com.rxlogix.config.EvdasAlert'
                break
            case LiteratureAlert:
                sql_statement  = """
                    SELECT t1.id,t1.reference_id,t1.reference_class
                    FROM attachment_link t1
                    LEFT join ARCHIVED_LITERATURE_ALERT t2
                    ON t1.reference_id = t2.id
                    WHERE lit_search_config_id = ${configId} and ex_lit_search_config_id = ${executedConfigId} and t1.reference_class='com.rxlogix.config.ArchivedLiteratureAlert'
                    """
                referenceClass = 'com.rxlogix.config.LiteratureAlert'
                break
        }
        try{
            sql.rows(sql_statement).each { row ->
                AttachmentLink attachmentLink = AttachmentLink.get(row[0])
                List<Attachment> attachmentList = Attachment.findAllByLnk(attachmentLink)
                attachmentList.each { attachment ->
                    File file = AttachmentableUtil.getFile(Holders.config, attachment)
                    def newDestination = AttachmentableUtil.getDir(Holders.config, referenceClass, attachmentLink.referenceId, false)
                    if (file.exists()) {
                       productAssignmentImportService.moveFile(file, newDestination as String)
                    } else {
                        log.info("file not found: " + file.path)
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error Occurred while persisting Attachments", e)
            throw e
        }
    }

    void moveAlertTagDataFromArchive(def domain, Long oldExecConfigID, def executedConfiguration) {
        log.info("Persisting Alert Categories")
        String sql_statement = ""
        String insertMappingQuery = ""
        Integer tagIndex = null
        boolean isRetained
        String dataSource = (Constants.DataSource.PVA).toUpperCase()
        Long executedConfigId = executedConfiguration?.id
        Session session = sessionFactory.openSession()
        List pvsTagList = []
        def config
        List<Long> prevExecConfigIds
        try {
            switch (domain) {
                case SingleCaseAlert:
                    config = Configuration.get(executedConfiguration?.configId)
                    prevExecConfigIds = alertService.fetchPrevExecConfigId(executedConfiguration, config, false, true)
                    sql_statement = SignalQueryHelper.sca_archivedAlertCat_sql(prevExecConfigIds, executedConfigId)
                    insertMappingQuery = "INSERT INTO SINGLE_CASE_ALERT_TAGS(PVS_ALERT_TAG_ID,SINGLE_ALERT_ID) VALUES(?,?)"
                    tagIndex = Holders.config.category.singleCase.alertSpecific
                    break
                case AggregateCaseAlert:
                    sql_statement = SignalQueryHelper.aca_archivedAlertCat_sql(oldExecConfigID, executedConfigId)
                    insertMappingQuery = "INSERT INTO AGG_CASE_ALERT_TAGS(PVS_ALERT_TAG_ID,AGG_ALERT_ID) VALUES(?,?)"
                    tagIndex = Holders.config.category.aggregateCase.alertSpecific
                    dataSource = executedConfiguration?.selectedDatasource?.toUpperCase()
                    break
                case EvdasAlert:
                    return
                    break
                case LiteratureAlert:
                    prevExecConfigIds = alertService.getLiteraturePrevExConfigIds(executedConfiguration, executedConfiguration.configId)
                    sql_statement = SignalQueryHelper.literature_archivedAlertCat_sql(prevExecConfigIds, executedConfigId)
                    insertMappingQuery = "INSERT INTO LITERATURE_CASE_ALERT_TAGS(PVS_ALERT_TAG_ID,LITERATURE_ALERT_ID) VALUES(?,?)"
                    tagIndex = Holders.config.category.literature.alertSpecific
                    break
                case EmbaseLiteratureAlert:
                    prevExecConfigIds = alertService.getLiteraturePrevExConfigIds(executedConfiguration, executedConfiguration.configId)
                    sql_statement = SignalQueryHelper.embaseLiterature_archivedAlertCat_sql(prevExecConfigIds, executedConfigId)
                    insertMappingQuery = "INSERT INTO EMBASE_LITERATURE_CASE_ALERT_TAGS(PVS_ALERT_TAG_ID,EMBASE_LITERATURE_ALERT_ID) VALUES(?,?)"
                    tagIndex = Holders.config.category.literature.alertSpecific
                    break
            }
            List<CategoryDTO> categoryDTOList = new ArrayList()
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)

            List alertIdAndAlertCategoryList = new ArrayList()

            List<Map> tagList = sqlQuery.list()
            List<Long> tagIds = tagList.collect {it[1] as Long}
            List<PvsAlertTag> alertTagList = tagIds ? PvsAlertTag.createCriteria().list {
                'or' {
                    tagIds.collate(1000).each { it
                        'in'("id", it)
                    }
                }
            } : []
            tagList.each { row ->
                PvsAlertTag pvsAlertTag = alertTagList.find { it -> it.id == row[1]}
                PvsAlertTag newPvsAlertTag = pvsAlertTag.clone()
                isRetained = false
                if (pvsAlertTag.autoTagged && domain == AggregateCaseAlert) {
                    Disposition disposition = cacheService.getDispositionByValue(Long.parseLong(row[2].toString()))
                    isRetained = !disposition.reviewCompleted
                }
                if (isRetained) {
                    newPvsAlertTag.setIsRetained(true)
                    String tagText = newPvsAlertTag.tagText
                    newPvsAlertTag.tagText = tagText.charAt(tagText.length() - 1) == Constants.Commons.RETAINED_CATEGORY ? tagText : tagText + Constants.Commons.RETAINED_CATEGORY
                } else if (domain != LiteratureAlert && pvsAlertTag.autoTagged) {
                    newPvsAlertTag = null
                }
                if (newPvsAlertTag) {
                    newPvsAlertTag.alertId = row[0]
                    newPvsAlertTag.id = null
                    pvsTagList.add(newPvsAlertTag)
                    CategoryDTO categoryDTO = pvsGlobalTagService.fetchCategoryDto(dataSource, newPvsAlertTag.tagText,
                            newPvsAlertTag.tagId, null, tagIndex, newPvsAlertTag.subTagText,
                            newPvsAlertTag.subTagId, userService.getUserByUsername(newPvsAlertTag?.privateUser)?.id, newPvsAlertTag.autoTagged,
                            isRetained, isRetained? oldExecConfigID:executedConfigId, newPvsAlertTag.createdAt, newPvsAlertTag.modifiedAt,
                            newPvsAlertTag?.createdBy, newPvsAlertTag?.modifiedBy, newPvsAlertTag?.priority)
                    if (domain == SingleCaseAlert) {
                        SingleCaseAlert singleCaseAlert = SingleCaseAlert.get(newPvsAlertTag.alertId)
                        categoryDTO.setFactGrpCol2(singleCaseAlert?.executedAlertConfiguration?.pvrCaseSeriesId?.toString())
                        categoryDTO.setFactGrpCol3(singleCaseAlert.caseId.toString())
                        categoryDTO.setFactGrpCol4(singleCaseAlert?.caseVersion?.toString())
                    } else {
                        categoryDTO.setFactGrpCol2(newPvsAlertTag.alertId.toString())
                        categoryDTO.setFactGrpCol3(executedConfigId as String)
                    }
                    categoryDTOList.add(categoryDTO)
                }
            }
            alertIdAndAlertCategoryList = pvsAlertTagService.batchPersistAlertTags(pvsTagList)

            alertIdAndAlertCategoryList = alertIdAndAlertCategoryList?.unique {
                [it.col2, it.col1]
            }

            if (categoryDTOList){
                categoryDTOList.collate(20000).each{
                    CategoryUtil.saveCategories(it)
                }
            }
            cacheService.prepareCommonTagCache()
            alertService.batchPersistForMapping(session, alertIdAndAlertCategoryList, insertMappingQuery)
        } catch (Exception e) {
            log.error("Error Occurred while persisting Alert Level Categories $sql_statement", e)
            throw e
        } finally {
            session.flush()
            session.clear()
            session.close()
        }
        log.info("Alert Categories are saved across the system.")
    }

}