package com.rxlogix

import com.rxlogix.config.CaseLineListingTemplate
import com.rxlogix.config.Disposition
import com.rxlogix.config.PVSState
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.config.ReportFieldInfoList
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.signal.AutoAdjustmentRule
import com.rxlogix.signal.GlobalCaseCommentMapping
import com.rxlogix.signal.SignalOutcome
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import com.rxlogix.Constants
import groovy.transform.Synchronized
import org.apache.commons.lang.StringUtils
import org.hibernate.Session
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Transactional
class RefreshConfigurationService implements AppRefreshableConfigs {

    def graphFaersDataService
    def cacheService
    def dataSource_pva
    def dataObjectService
    def pvsGlobalTagService
    def alertCommentService
    def sessionFactory
    def seedDataService
    def validatedSignalService
    def viewInstanceService
    def dataSource_eudra
    def grailsApplication
    def dataSource_faers
    def dataSource_vaers
    def dataSource_vigibase
    def dataSource_jader
    def alertAttributesService

    @Override
    void refreshConfigData(List<String> updatedKeysList) {
        log.info("refresh config started")
        ExecutorService executorService
        try {
            executorService = Executors.newFixedThreadPool(10)
            updatedKeysList.each { configKey ->
                executorService.execute({ ->
                    switch (configKey) {
                        case 'signal.faers.graphDB.seedData':
                            refreshGraphFaersDataService()
                            break
                        case 'pvsignal.product.based.security':
                            refreshCacheForProductBasedSecurity()
                            break
                        case 'pvsignal.workflow.default.value':
                            try {
                                PVSState.withTransaction {
                                    cacheService.prepareDefaultWorkflow()
                                }
                            } catch (Exception exception) {
                                log.info("Failed to prepare default workflow config")
                                exception.printStackTrace()
                            }
                            break
                        case 'pvsignal.disposition.default.value':
                            try {
                                Disposition.withTransaction {
                                    cacheService.prepareDefaultDisposition()
                                }
                            } catch (Exception ex) {
                                log.info("failed to prepare default config")
                                ex.printStackTrace()

                            }
                            break
                        case 'signal.datasource.list':
                            populateValueInDataSourceMap()
                            break
                        case 'signal.categories.migration.enabled': //to test
                            if (Holders.config.signal.categories.migration.enabled == true) {
                                pvsGlobalTagService.runCategoriesMigrationInDB()
                                pvsGlobalTagService.importAggregateGlobalTagsWithKeyId()
                                pvsGlobalTagService.importSingleGlobalTagsWithKeyId()
                                log.info("Categories migration is Complete.")
                            } else {
                                log.info("Categories migration is disabled.")
                            }
                            break
                        case 'signal.ror.flag.column.name':
                            updateRorStatus()
                            break
                        case 'global.comment.migration.flag':
                            syncGlobalCaseComments()
                            break
                        case 'alertManagement.autoAdjustmentAlertTypes':
                            initializeAlertAutoAdjustmentRules()
                            break
                        case 'prepare.possible.values.cache':
                            log.info('Refreshing advance filter possible values cache...')
                            cacheService.prepareAdvancedFilterPossibleValues()
                            log.info('Advanced filter possible values cache refresh Complete.')
                            break
                        case 'sqlString.nonCahcheSelectable.enabled':
                            seedDataService.cacheReportFields()
                            break
                        case 'validatedSignal.shareWith.enabled':
                            validatedSignalService.shareWithAllUsers()
                            break
                        case 'refresh.view.instance':
                               viewInstanceService.updateAllViewInstances()
                            break
                        case 'disposition.signal.outcome.mapping.enabled':
                            mapSignalOutcomeAndDisposition()
                            break
                        case 'signal.evdas.enabled':
                            refreshEvdasConfig()
                            break
                        case 'signal.spotfire.enabled':
                            refreshSpotfireConfig()
                            break
                        case 'signal.faers.enabled':
                            refreshFaersConfig()
                            break
                        case 'signal.vaers.enabled':
                            refreshVaersConfig()
                            break
                        case 'signal.jader.enabled':
                            refreshJaderConfig()
                            break
                        case 'signal.vigibase.enabled':
                            refreshVigibaseConfig()
                            break
                        case 'signal.evdas.case.line.listing.import.folder.base':
                            def ctx = Holders.grailsApplication.mainContext
                            def evdasCaseListingImportService = ctx.getBean("evdasCaseListingImportService")
                            evdasCaseListingImportService.checkAndCreateBaseDirs()
                            break
                        case 'signal.evdas.case.line.listing.default.substance.list':
                            def ctx = Holders.grailsApplication.mainContext
                            def evdasCaseListingImportService = ctx.getBean("evdasCaseListingImportService")
                            evdasCaseListingImportService.checkAndCreateSubstanceFolders()
                            break
                        case 'signal.evdas.data.import.folder.base':
                            def ctx = Holders.grailsApplication.mainContext
                            def evdasDataImportService = ctx.getBean("evdasDataImportService")
                            evdasDataImportService.checkAndCreateBaseDirs()
                            break
                        case 'signal.summary.dynamic.fields':
                            AuditLogConfigUtil.populateLabelConfigForAudit("signalUDLabels")
                            break
                        case 'adhoc.alert.configuration.dynamic.fields':
                            cacheService.prepareAdhocConfiguration()
                            viewInstanceService.updateAllViewInstances()
                            AuditLogConfigUtil.populateLabelConfigForAudit("adhocAlertLabels")
                            break
                        case 'alert.attributes.default.values':
                            refreshAdHocAlertAttributes()
                            break

                    }

                } as Runnable)
            }
        } catch (Exception exception) {
            log.info("Failed while refreshing config")
            exception.printStackTrace()
        } finally {
            executorService.shutdown()
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        }
    }

    void refreshGraphFaersDataService() {
        if (Holders.config.pvsignal.multiple.datasource.toBoolean() == true &&
                Holders.config.pvsignal.supported.datasource.call().contains(Constants.DataSource.FAERS) &&
                Holders.config.signal.faers.graphDB.seedData == true) {
            log.info("Faers DB Seeding Started.")
            graphFaersDataService.seed()
            log.info("Faers DB Seeding Completed.")
        } else {
            log.info("Seeding on FAERS flag is false thus seeding will be skipped.")
        }
    }

    void refreshCacheForProductBasedSecurity() {
        if (Holders.config.pvsignal.product.based.security) {
            log.info("Product based security is enabled. refreshing cache now....")
            cacheService.prepareProductIdsAndNameCacheForAllUsers()
            cacheService.prepareSafetyGroupProductNameCacheForAllUsers()
            cacheService.prepareProductsUsed()
            cacheService.prepareGroupCache()
            log.info("Product based security is enabled. refreshing cache completed.")
        } else {
            cacheService.getCache(cacheService.CACHE_NAME_PV_USER_PRODUCTS_IDS).clear()
            cacheService.getCache(cacheService.CACHE_NAME_PV_USER_PRODUCTS_NAMES).clear()
            cacheService.getCache(cacheService.CACHE_NAME_PV_SAFETY_GROUP_PRODUCTS_NAMES).clear()
            cacheService.getSetCache(cacheService.CACHE_NAME_PRODUCTS_USED).clear()
            cacheService.getCache(cacheService.CACHE_NAME_PV_GROUP_ALLOWED_PRODUCT).clear()
            cacheService.getCache(cacheService.CACHE_NAME_PV_GROUP_ALLOWED_ID).clear()
            log.info("Product based security is disabled. Cache is cleared.")
        }
    }

    void populateValueInDataSourceMap() {
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            List list = sql.rows("select ETL_VALUE from pvr_etl_constants where etl_key='SOURCE_TYPE'")
            if (list) {
                List<String> dataSourceList = Holders.config.signal.datasource.list as List<String>
                String sourceName = list.get(0)?.ETL_VALUE
                boolean value = dataSourceList.contains(sourceName.toUpperCase())
                if (sourceName?.equals(Constants.DbDataSource.PVCM)) {
                    Holders.config.is.pvcm.env = value
                    dataObjectService.setDataSourceMap(Constants.DbDataSource.PVCM, value)
                } else {
                    dataObjectService.setDataSourceMap(Constants.DbDataSource.IS_ARISG_PVIP, value)
                }
            }
        } catch (Exception e) {
            log.error("Some error occurred while fetching value for datasource")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
    }


    private void updateRorStatus() {
        log.info("updating ROR status cache")

        String rorStatement = "select JSON_VALUE(CONFIG_VALUE,'\$.KEY_VALUE') as KEY_VALUE from VW_ADMIN_APP_CONFIG where CONFIG_KEY='ROR_INTERVAL' and APPLICATION_NAME = 'PVA-DB'"
        log.info("rorStatement======================")
        log.info(""+rorStatement)
        String rorColumn = Holders.config.signal.ror.flag.column.name

        boolean isRor = true
        Sql sqlObj = null
        try {
            sqlObj = new Sql(dataSource_pva)

            sqlObj.eachRow(rorStatement) { GroovyResultSet resultSet ->
                String rorColumnFlagVal = resultSet.getString(rorColumn)
                log.info("rorColumnFlagVal===============================")
                log.info(""+rorColumnFlagVal)
                if (rorColumnFlagVal == '1') {
                    isRor = false
                }
            }


        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sqlObj?.close()
            log.info("update ROR status cache Complete")
        }
        cacheService.saveRorCache(isRor)
    }

    void syncGlobalCaseComments() {
        try {
            if (Holders.config.global.comment.migration.flag) {
                Integer globalCommentCount = 0
                GlobalCaseCommentMapping."pva".withTransaction {
                    globalCommentCount = GlobalCaseCommentMapping."pva".count()
                }
                if (globalCommentCount <= 0) {
                    alertCommentService.insertExistingCommentInDb(Constants.DataSource.PVA)
                    if (Holders.config.signal.faers.enabled == true) {
                        alertCommentService.insertExistingCommentInDb(Constants.DataSource.FAERS)
                    }
                    alertCommentService.runMigrationInDB(Constants.DataSource.PVA)
                    if (Holders.config.signal.faers.enabled == true) {
                        alertCommentService.runMigrationInDB(Constants.DataSource.FAERS)
                    }
                }
                log.debug("Global Comment initial migration is Completed!")
            } else {
                log.debug("Global Comment initial migration is disabled!")
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    void initializeAlertAutoAdjustmentRules() {
        List<String> alertTypes = Holders.config.alertManagement.autoAdjustmentAlertTypes

        try {
            alertTypes.each { alertType ->
                if (!AutoAdjustmentRule.findByAlertType(alertType)) {
                    AutoAdjustmentRule autoAdjustmentRule = new AutoAdjustmentRule(alertType, Constants.SYSTEM_USER, Constants.SYSTEM_USER)
                    autoAdjustmentRule.save(flush: true)
                    log.debug("Auto adjustment rule created for alert type ${alertType}!")
                }
            }
        } catch (Exception ex) {
            log.error("Error occurred while initializing auto adjustment rules.", ex.printStackTrace())
        }
    }

    def mapSignalOutcomeAndDisposition() {
        List<SignalOutcome> outcomes = SignalOutcome.findAll();
        outcomes.each {
            it.dispositionId = null;
        }
        SignalOutcome.saveAll(outcomes)
        if (Holders.config.disposition.signal.outcome.mapping.enabled) {
            log.info("Mapping signal outcome with disposition...")
            Map mapping = grailsApplication.config.freshBuild.disposition.mapped.signalOutcome
            mapping.each { key, val ->
                Disposition disposition = Disposition.findByValue(key);
                val.value.each {
                    SignalOutcome signalOutcome = SignalOutcome.findByName(it)
                    signalOutcome?.dispositionId = disposition?.id
                    signalOutcome?.save(flush: true);
                }
            }
        }
    }

    void refreshEvdasConfig() {
        if (Holders.config.signal.evdas.enabled == true) {
            if (Holders.config.dataSources.eudra.username != Holders.config.dataSources.pva.username) {
                try {
                    def ctx = Holders.grailsApplication.mainContext
                    def evdasDataImportService = ctx.getBean("evdasDataImportService")
                    evdasDataImportService.checkAndCreateBaseDirs()
                    def evdasCaseListingImportService = ctx.getBean("evdasCaseListingImportService")
                    evdasCaseListingImportService.checkAndCreateBaseDirs()
                    setRoleHierarchy()
                    seedEvdasRoles()
                    seedDataService.seedEvdasMetadata()
                    log.info("EVDAS enabled successfully.")
                } catch (Exception ex) {
                    log.info(" Failed to enable EVDAS")
                    Holders.config.signal.evdas.enabled == false
                    ex.printStackTrace()
                }
            } else {
                Holders.config.signal.evdas.enabled == false
                log.info("EVDAS DB details not available.")
            }
        }
    }

    def setRoleHierarchy() {
        grailsApplication.mainContext.roleHierarchy.hierarchy = """
         ROLE_DEV > ROLE_ADMIN
         ROLE_ADMIN > ROLE_CONFIGURATION_CRUD
         ROLE_ADMIN > ROLE_MANAGE_PRODUCT_ASSIGNMENTS
         ROLE_CONFIGURE_TEMPLATE_ALERT < ROLE_CONFIGURATION_CRUD
         ROLE_CONFIGURE_TEMPLATE_ALERT < ROLE_SHARE_GROUP
         ROLE_CONFIGURE_TEMPLATE_ALERT < ROLE_SHARE_ALL
         ROLE_ADMIN > ROLE_CONFIGURE_TEMPLATE_ALERT
         ROLE_CONFIGURATION_CRUD > ROLE_CONFIGURATION_VIEW
         ROLE_ADMIN > ROLE_QUERY_ADVANCED
         ROLE_QUERY_ADVANCED > ROLE_QUERY_CRUD
         ROLE_QUERY_CRUD > ROLE_QUERY_VIEW
         ROLE_ADMIN > ROLE_TEMPLATE_ADVANCED
         ROLE_TEMPLATE_ADVANCED > ROLE_TEMPLATE_CRUD
         ROLE_TEMPLATE_CRUD > ROLE_TEMPLATE_VIEW
         ROLE_ADMIN > ROLE_SINGLE_CASE_CONFIGURATION
         ROLE_SINGLE_CASE_CONFIGURATION > ROLE_SINGLE_CASE_REVIEWER
         ROLE_SINGLE_CASE_REVIEWER > ROLE_SINGLE_CASE_VIEWER
         ROLE_ADMIN > ROLE_AGGREGATE_CASE_CONFIGURATION
         ROLE_AGGREGATE_CASE_CONFIGURATION > ROLE_AGGREGATE_CASE_REVIEWER
         ROLE_AGGREGATE_CASE_REVIEWER > ROLE_AGGREGATE_CASE_VIEWER
         ROLE_ADMIN > ROLE_LITERATURE_CASE_CONFIGURATION
         ROLE_LITERATURE_CASE_CONFIGURATION > ROLE_LITERATURE_CASE_REVIEWER
         ROLE_LITERATURE_CASE_REVIEWER > ROLE_LITERATURE_CASE_VIEWER
         ROLE_ADMIN > ROLE_SIGNAL_MANAGEMENT_CONFIGURATION
         ROLE_SIGNAL_MANAGEMENT_CONFIGURATION > ROLE_SIGNAL_MANAGEMENT_REVIEWER
         ROLE_SIGNAL_MANAGEMENT_REVIEWER > ROLE_SIGNAL_MANAGEMENT_VIEWER
         
         ${Holders.config.signal.evdas.enabled ? '''ROLE_ADMIN > ROLE_EVDAS_CASE_CONFIGURATION
                ROLE_EVDAS_CASE_CONFIGURATION > ROLE_EVDAS_CASE_REVIEWER
                ROLE_EVDAS_CASE_REVIEWER > ROLE_EVDAS_CASE_VIEWER      
                ''' : ''}
         ${Holders.config.signal.faers.enabled ? '''
                ROLE_ADMIN > ROLE_FAERS_CONFIGURATION
                ''' : ''}

         ${Holders.config.signal.vaers.enabled ? '''
                ROLE_ADMIN > ROLE_VAERS_CONFIGURATION
                ''' : ''}
         
         ${Holders.config.signal.vigibase.enabled ? '''
                ROLE_ADMIN > ROLE_VIGIBASE_CONFIGURATION
                ''' : ''}
                ''' : ''}
         ROLE_ADMIN > ROLE_AD_HOC_CRUD
         ${Holders.config.signal.spotfire.enabled ? 'ROLE_ADMIN > ROLE_DATA_ANALYSIS' : ''}
         ${Holders.config.signal.spotfire.enabled ? 'ROLE_ADMIN > ROLE_PRODUCTIVITY_AND_COMPLIANCE' : ''}
         ROLE_ADMIN > ROLE_DATA_MINING
         ROLE_ADMIN > ROLE_TASK_CRUD
         ROLE_TASK_CRUD > ROLE_TASK_VIEW
         ROLE_ADMIN > ROLE_COGNOS_CRUD
         ROLE_COGNOS_CRUD > ROLE_COGNOS_VIEW
         ROLE_ADMIN > ROLE_SHARE_GROUP
         ROLE_ADMIN > ROLE_SHARE_ALL
         ROLE_ADMIN > ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT
         ROLE_ADMIN > ROLE_OPERATIONAL_METRICS
         ROLE_ADMIN > ROLE_EXECUTE_SHARED_ALERTS
         ROLE_ADMIN > ROLE_REPORTING
       """
    }

    void seedEvdasRoles() {
        def roles = [
                [authority: 'ROLE_EVDAS_CASE_CONFIGURATION', description: 'Perform EVDAS Alerts configuration and review'],
                [authority: 'ROLE_EVDAS_CASE_REVIEWER', description: 'View, update EVDAS Alerts'],
                [authority: 'ROLE_EVDAS_CASE_VIEWER', description: 'View EVDAS Alerts']
        ]
        roles.collect {
            def role
            if (!Role.findByAuthority(it.authority)) {
                role = new Role(authority: it.authority, description: it.description,
                        createdBy: "Application", modifiedBy: "Application").save(failOnError: true, flush: true)
            }
        }
    }

    void refreshSpotfireConfig() {
        if (Holders.config.signal.spotfire.enabled == true) {
            if (Holders.config.dataSources.spotfire.username != Holders.config.dataSources.pva.username) {
                try {
                    setRoleHierarchy()
                    Role role
                    if (!Role.findByAuthority('ROLE_DATA_ANALYSIS')) {
                        role = new Role(authority: 'ROLE_DATA_ANALYSIS', description: 'Access to Data Analysis information',
                                createdBy: "Application", modifiedBy: "Application").save(failOnError: true, flush: true)
                    }
                    log.info("Spotfire enabled successfully.")
                } catch (Exception ex) {
                    log.info(" Failed to enable Spotfire")
                    Holders.config.signal.spotfire.enabled = false
                    ex.printStackTrace()
                }
            } else {
                Holders.config.signal.spotfire.enabled = false
                log.info("Spotfire DB details not available.")
            }
        }
    }

    void refreshFaersConfig() {
        if (Holders.config.signal.faers.enabled == true) {
            if (Holders.config.dataSources.faers.username != Holders.config.dataSources.pva.username) {
                try {
                    updateFaersScoreConfiguration()
                    setRoleHierarchy()
                    Role role
                    if (!Role.findByAuthority('ROLE_FAERS_CONFIGURATION')) { //seedRoles
                        role = new Role(authority: 'ROLE_FAERS_CONFIGURATION', description: 'Aggregate Alerts Configuration for FAERS data source',
                                createdBy: "Application", modifiedBy: "Application").save(failOnError: true, flush: true)
                    }
                    prepareFaersSubGroupColumnCache()
                    prepareFaersMiningVariables()
                    log.info("Successfully enabled FAERS.")
                } catch (Exception ex) {
                    Holders.config.signal.faers.enabled = false
                    log.info("Failed to enable FAERS")
                    ex.printStackTrace()
                }
            } else {
                Holders.config.signal.faers.enabled = false
                log.info("FAERS Db details not available.")
            }
        }
    }

    void prepareFaersSubGroupColumnCache() {
        def sql_faers
        try {
            sql_faers = new Sql(dataSource_faers)
            sql_faers.eachRow(SignalQueryHelper.ebgm_sub_group_view_sql("FAERS-DB")) { vwNameRow ->
                if (vwNameRow && vwNameRow.DSP_VIEW_NAME) {
                    String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                    Map<String, String> idLabelMap = new LinkedHashMap<>()
                    sql_faers.rows(query).each {
                        idLabelMap.put(String.valueOf(it.ID), it.LABEL)
                    }
                    cacheService.getCache(cacheService.CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).put(vwNameRow.PVS_STR_COLUMN + "_" + Constants.DataSource.DATASOURCE_FAERS, idLabelMap)
                }
            }
            log.info("Caching faers subgroup complete.")
        } catch (Exception ex) {
            log.info("failed while caching faers subgroup")
            ex.printStackTrace()
        } finally {
            sql_faers?.close()
        }
    }

    void prepareFaersMiningVariables() {
        def sql_faers
        try {
            sql_faers = new Sql(dataSource_faers)
            Map<String, String> idLabelMap = new LinkedHashMap<>()
            Map<String, String> meddraMap = new LinkedHashMap<>()
            idLabelMap = new LinkedHashMap<>()
            meddraMap = new LinkedHashMap<>()
            sql_faers.rows(SignalQueryHelper.find_meddra_field_sql()).each {
                meddraMap.put(String.valueOf(it.key_id), ['label': it.ui_label, 'use_case': it.use_case])
            }
            sql_faers.rows(SignalQueryHelper.batch_variables_sql()).each {
                List data = []
                boolean isMeddra = false;
                if (meddraMap.containsKey(String.valueOf(it.key_id))) {
                    isMeddra = true;
                }
                if (it.isautocomplete == 1) {
                    data = cacheService.getDmvDataList(sql_faers, it.use_case, it.isautocomplete == 1 ? true : false)
                }
                idLabelMap.put(String.valueOf(it.key_id), ['label': it.ui_label, 'use_case': it.use_case, "isMeddra": isMeddra, "isOob": it.paste_import_option == 1 ? false : true, 'isautocomplete': it.isautocomplete == 1 ? true : false, 'data': data, 'dic_level': it.dic_level, 'dic_type': it.dic_type, 'validatable': it.validatable == 1 ? true : false])
            }
            cacheService.getCache(cacheService.CACHE_DATA_MINING_VARIABLES).put('faers', idLabelMap)
            log.info("Faers data mining variable cache refresh completed.")
        } catch (Exception ex) {
            log.info("Faers data mining variable cache refresh failed.")
        } finally {
            sql_faers?.close()
        }
    }

    void updateFaersScoreConfiguration() {
        log.info("Updating the score configurations ..")
        def config = Holders.getConfig()
        final String sqlToExecute = """
            select case when CONFIG_KEY = 'PQR_Ebgm' then 'EBGM' WHEN CONFIG_KEY = 'PQR_PRR' THEN 'PRR' END as key,JSON_VALUE(config_value,'\$.HIDE') as hide from VW_ADMIN_APP_CONFIG where CONFIG_KEY in ('PQR_Ebgm', 'PQR_PRR') and application_name = 'FAERS-DB'
        """
        Sql sql
        try {
            sql = new Sql(dataSource_faers)
            sql.eachRow(sqlToExecute, []) { sqlRow ->
                if (sqlRow.key == "EBGM") {
                    config.statistics.faers.enable.ebgm = !sqlRow.hide
                }
                if (sqlRow.key == "PRR") {
                    config.statistics.faers.enable.prr = !sqlRow.hide
                    config.statistics.faers.enable.ror = !sqlRow.hide
                }
            }
            log.info("Updating the score completed.")
        } catch (Exception ex) {
            log.info("Updating the score failed.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void refreshVaersConfig() {
        if (Holders.config.signal.vaers.enabled) {
            if (Holders.config.dataSources.vaers.username != Holders.config.dataSources.pva.username) {
                try {
                    updateVaersScoreConfiguration()
                    setRoleHierarchy()
                    Role role
                    if (!Role.findByAuthority('ROLE_VAERS_CONFIGURATION')) { //seedRoles
                        role = new Role(authority: 'ROLE_VAERS_CONFIGURATION', description: 'Aggregate Alerts Configuration for VAERS data source',
                                createdBy: "Application", modifiedBy: "Application").save(failOnError: true, flush: true)
                    }
                    log.info("VAERS config enabled successfully.")
                } catch (Exception ex) {
                    log.info("VAERS config enabled failed.")
                    ex.printStackTrace()
                    Holders.config.signal.vaers.enabled = false
                }
            } else {
                Holders.config.signal.vaers.enabled = false
                log.info("VAERS db config is not available.")
            }
        }
    }
    void refreshJaderConfig() {
        if (Holders.config.signal.jader.enabled) {
            if (Holders.config.dataSources.jader.username != Holders.config.dataSources.pva.username) {
                try {
                    updateJaderScoreConfiguration()
                    setRoleHierarchy()
                    Role role
                    if (!Role.findByAuthority('ROLE_JADER_CONFIGURATION')) { //seedRoles
                        role = new Role(authority: 'ROLE_JADER_CONFIGURATION', description: 'Aggregate Alerts Configuration for JADER data source',
                                createdBy: "Application", modifiedBy: "Application").save(failOnError: true, flush: true)
                    }
                    log.info("JADER config enabled successfully.")
                } catch (Exception ex) {
                    log.info("JADER config enabled failed.")
                    ex.printStackTrace()
                    Holders.config.signal.jader.enabled = false
                }
            } else {
                Holders.config.signal.jader.enabled = false
                log.info("JADER db config is not available.")
            }
        }
    }

    void updateVaersScoreConfiguration() {
        log.info("Updating the vaers score configurations ..")
        def config = Holders.getConfig()
        final String sqlToExecute = """
            select case when CONFIG_KEY = 'PQR_Ebgm' then 'EBGM' WHEN CONFIG_KEY = 'PQR_PRR' THEN 'PRR' END as key,JSON_VALUE(config_value,'\$.HIDE') as hide from VW_ADMIN_APP_CONFIG where CONFIG_KEY in ('PQR_Ebgm', 'PQR_PRR') and application_name = 'VAERS-DB';
        """
        Sql sql
        try {
            sql = new Sql(dataSource_vaers)
            sql.eachRow(sqlToExecute, []) { sqlRow ->
                if (sqlRow.key == "EBGM") {
                    config.statistics.vaers.enable.ebgm = !sqlRow.hide
                }
                if (sqlRow.key == "PRR") {
                    config.statistics.vaers.enable.prr = !sqlRow.hide
                    config.statistics.vaers.enable.ror = !sqlRow.hide
                }
            }
            log.info("Updating the vaers score completed.")
        } catch (Exception ex) {
            log.info("Updating the vaers score failed.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }
    void updateJaderScoreConfiguration() {
        log.info("Updating the jader score configurations ..")
        def config = Holders.getConfig()
        final String sqlToExecute = """
            select case when CONFIG_KEY = 'PQR_Ebgm' then 'EBGM' WHEN CONFIG_KEY = 'PQR_PRR' THEN 'PRR' END as key,JSON_VALUE(config_value,'\$.HIDE') as hide from VW_ADMIN_APP_CONFIG where CONFIG_KEY in ('PQR_Ebgm', 'PQR_PRR') and application_name = 'JADER-DB';
        """
        Sql sql
        try {
            sql = new Sql(dataSource_jader)
            sql.eachRow(sqlToExecute, []) { sqlRow ->
                if (sqlRow.key == "EBGM") {
                    config.statistics.jader.enable.ebgm = !sqlRow.hide
                }
                if (sqlRow.key == "PRR") {
                    config.statistics.jader.enable.prr = !sqlRow.hide
                    config.statistics.jader.enable.ror = !sqlRow.hide
                }
            }
            log.info("Updating the jader score completed.")
        } catch (Exception ex) {
            log.info("Updating the jader score failed.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void refreshVigibaseConfig() {
        if (Holders.config.signal.vigibase.enabled) {
            if (Holders.config.dataSources.vigibase.username != Holders.config.dataSources.pva.username) {
                try {
                    updateVigibaseScoreConfiguration()
                    setRoleHierarchy()
                    Role role
                    if (!Role.findByAuthority('ROLE_VIGIBASE_CONFIGURATION')) { //seedRoles
                        role = new Role(authority: 'ROLE_VIGIBASE_CONFIGURATION', description: 'Perform VigiBase alerts configuration and review',
                                createdBy: "Application", modifiedBy: "Application").save(failOnError: true, flush: true)
                    }
                    log.info("Vigibase config update completed.")
                } catch (Exception ex) {
                    log.info("Failed to refresh vigibase config")
                    Holders.config.signal.vigibase.enabled = false
                    ex.printStackTrace()
                }
            } else {
                log.info("Vigibase db Config not available.")
                Holders.config.signal.vigibase.enabled = false
            }
        }
    }

    void updateVigibaseScoreConfiguration() {
        log.info("Updating the vigibase score configurations ..")
        def config = Holders.getConfig()
        final String sqlToExecute = """
            select case when CONFIG_KEY = 'PQR_Ebgm' then 'EBGM' WHEN CONFIG_KEY = 'PQR_PRR' THEN 'PRR' END as key,JSON_VALUE(config_value,'\$.HIDE') as hide from VW_ADMIN_APP_CONFIG where CONFIG_KEY in ('PQR_Ebgm', 'PQR_PRR') and application_name = 'VIGIBASE-DB';
        """
        Sql sql
        try {
            sql = new Sql(dataSource_vigibase)
            sql.eachRow(sqlToExecute, []) { sqlRow ->
                if (sqlRow.key == "EBGM") {
                    config.statistics.vigibase.enable.ebgm = !sqlRow.hide
                }
                if (sqlRow.key == "PRR") {
                    config.statistics.vigibase.enable.prr = !sqlRow.hide
                    config.statistics.vigibase.enable.ror = !sqlRow.hide
                }
            }
            log.info("Updating the vigibase score completed.")
        } catch (Exception ex) {
            log.info("Updating the vigibase score failed.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }

    }

    void updateOnetimeDependentConfigurations() {
        // onetime configurations
        def config = Holders.getConfig()
        def reportsConfig = config.pvreports

        //advanced filter rpt field config
        config.advancedFilter.rpt.field.map = ["country": "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled ? 'csiderived_country' : 'masterCountryId'}", "submitter": "csisender_organization"]

        //reports URL config
        reportsConfig.query.view.uri = "${-> grails.util.Holders?.config?.pvreports?.web?.url}/reports/query/view"
        reportsConfig.reports.view.uri = "${-> grails.util.Holders?.config?.pvreports?.web?.url}/reports/report/viewMultiTemplateReport"
        reportsConfig.query.list.uri = "${-> grails.util.Holders?.config?.pvreports?.web?.url}/reports/query/index"
        reportsConfig.query.create.uri = "${-> grails.util.Holders?.config?.pvreports?.web?.url}/reports/query/create"
        reportsConfig.query.load.uri = "${-> grails.util.Holders?.config?.pvreports?.web?.url}/reports/query/load"
        reportsConfig.template.view.uri = "${-> grails.util.Holders?.config?.pvreports?.web?.url}/reports/template/view"
        reportsConfig.ciomsI.export.uri = "${-> grails.util.Holders?.config?.pvreports?.web?.url}/reports/report/exportSingleCIOMS?caseNumber="
        log.info("Conditional configurations updated!")
    }

    def refreshAdHocAlertAttributes() {
        alertAttributesService.slurp(null)
        alertAttributesService.attributesMap
        List evaluationMethods = grailsApplication.config.configurations.evaluationMethods
        List dataSources = grailsApplication.config.configurations.datasources
        List modifiedEvaluationMethods = []
        List modifiedDataSourceMethods = []
        evaluationMethods.each {
            modifiedEvaluationMethods << [value: it]
        }
        dataSources.each {
            modifiedDataSourceMethods << [value: it.dataSource]
        }
        alertAttributesService.attributesMap.evaluationMethods = alertAttributesService.attributesMap["evaluationMethods"] ?: modifiedEvaluationMethods
        alertAttributesService.attributesMap.initialDataSource = alertAttributesService.attributesMap["initialDataSource"] ?: modifiedDataSourceMethods
    }

}
