package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.CaseColumnJoinMapping
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.UserType
import com.rxlogix.mapping.LmProduct
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource
import java.sql.SQLException

class SeedDataService {
    static String USERNAME = "Application"
    static transactional = false

    String argusTableMaster = "argusTableMaster.csv"
    String argusColumnMaster = "argusColumnMaster.csv"
    String caseColumnJoinMapping = "caseColumnJoinMapping.csv"
    String reportFieldGroup = "reportFieldGroup.csv"

    String metadataLocation = "metadata"
    String templates = "templates.json"
    String externalized = "externalized_data.cfg"
    def grailsApplication
    def userService
    def genericNameCacheService
    def reportFieldService
    def signalDataSourceService
    def importService
    def sessionFactory
    def cacheService
    def dataSource_eudra

    @Autowired
    DataSource dataSource_pva

    def CRUDService

    def seed() {

        seedRoles()

        seedUsers()

        seedSystemUser()

        seedCategoriesAndTags()

        seedMetadata()

        if(Holders.config.signal.evdas.enabled == true) {
            seedEvdasMetadata()
        }

        cacheReportFields()
        try {
            seedEvaluationTypeAndReferenceType()
        } catch (Throwable t) {
            log.error("Cannot read evaluation information. This might or might not be an issue")
        }

        if (!EtlSchedule.count()) {
            seedETLSchedule()
        }
        seedGenericNameCache()
    }

    def cacheReportFields() {
        log.info("Now caching the report fields.")
        //Write Query parameters to the temporary file
        cacheService.prepareRptToUiLabelInfoPvr()
        def values = reportFieldService.retrieveValuesFromDatabase("en")
        if (values != null && values.keySet().size() > 0) {
            reportFieldService.serializeValues(values)
        }
        log.info("Caching of report fields is completed.")
    }

    def seedEvaluationTypeAndReferenceType() {
        if (!EvaluationReferenceType.count()) {
            new EvaluationReferenceType(name: "Publication", display: true, description: "This is a Reference Type.").save(flush: true)
            new EvaluationReferenceType(name: "Social Media", display: true, description: "This is a Reference Type.").save(flush: true)
            new EvaluationReferenceType(name: "Inquiry", display: true, description: "This is a Reference Type.").save(flush: true)
        }
    }

    // ====================  Seed users, roles and groups ==========================
    protected void seedUsers() {
        Group workflowGroup = Group.findAllByGroupType(GroupType.WORKFLOW_GROUP).first()
        (readPvsExtConfig().grails.pvsignal.users as List).each {
            List<Group> groupsList = [workflowGroup]
            it?.userGroups?.each{ group ->
                groupsList.add(Group.findByNameIlike(group))
            }
            Preference tempPreference = new Preference(locale: new Locale('en'), timeZone: it.timeZone ?: DateTimeZone.UTC.ID, createdBy: USERNAME, modifiedBy: USERNAME)
            userService.createUser(it.username as String, tempPreference, it.roles as List<String>, USERNAME, groupsList, UserType.valueOf(it.type ?: 'LDAP'),it?.email,it?.fullName)
        }
    }

    void seedSystemUser() {
        log.info("Seeding the System User.")
        if (!User.findByUsername(Constants.SYSTEM_USER)) {
            Group workflowGroup = Group.findByGroupType(GroupType.WORKFLOW_GROUP)
            Preference pref = new Preference(locale: new Locale(Constants.Commons.LOCALE_EN), timeZone: DateTimeZone.UTC.ID, createdBy: USERNAME, modifiedBy: USERNAME)
            User sysUser = new User(username: Constants.SYSTEM_USER, preference: pref, createdBy: USERNAME, modifiedBy: USERNAME, fullName: Constants.SYSTEM_USER, email: null, enabled: false,).save(failOnError: true)
            UserRole.create(sysUser, Role.findByAuthority(Constants.Commons.ROLE_DEV))
            //TODO: Need to change two save call as this is required because userRole cannt be created on unsaved user
            sysUser.userRolesString=UserRole.findAllByUser(sysUser)?.role.toString()
            sysUser.save(flush:true)
        }
    }

    @Transactional
    void seedRoles() {
        log.info("Roles seeding started.")
        def roles = [
                [authority  : 'ROLE_ADMIN',
                 description: "Administrator; can perform all operations "],

                [authority  : 'ROLE_DEV',
                 description: "Developer; has access to developer oriented functionality"],

                //System Configuration -----------------------------------------------------------------
                [authority  : 'ROLE_CONFIGURATION_CRUD',
                 description: "Create, read, update, and delete System Configurations"],
                [authority  : 'ROLE_CONFIGURATION_VIEW',
                 description: "View System Configurations only"],

                //Single Case

                [authority  : 'ROLE_SINGLE_CASE_CONFIGURATION',
                 description: "Perform Individual Case  configuration and review activities."],

                [authority  : 'ROLE_SINGLE_CASE_REVIEWER',
                 description: "View, update, Individual Case Review alerts"],

                [authority  : 'ROLE_SINGLE_CASE_VIEWER',
                 description: "View Individual Case alerts"],

                //Aggregate Case

                [authority  : 'ROLE_AGGREGATE_CASE_CONFIGURATION',
                 description: "Perform Aggregate alerts configuration and review"],

                [authority  : 'ROLE_AGGREGATE_CASE_REVIEWER',
                 description: "View, update, Aggregate Alerts"],

                [authority  : 'ROLE_AGGREGATE_CASE_VIEWER',
                 description: "View Aggregate Alerts"],

                //Literature Alert

                [authority  : 'ROLE_LITERATURE_CASE_CONFIGURATION',
                 description: "Perform Literature Alerts Configuration"],

                [authority  : 'ROLE_LITERATURE_CASE_REVIEWER',
                 description: "View, update Literature Alerts"],

                [authority  : 'ROLE_LITERATURE_CASE_VIEWER',
                 description: "View Literature Alerts"],

                //Data Mining
                [authority  : 'ROLE_DATA_MINING',
                 description: "Data Mining and on demand option will be enabled"],

                //Signal Management

                [authority  : 'ROLE_SIGNAL_MANAGEMENT_CONFIGURATION',
                 description: "Create signals from Signal Management"],

                [authority  : 'ROLE_SIGNAL_MANAGEMENT_REVIEWER',
                 description: "View, update Signals from Signal Management"],

                [authority  : 'ROLE_SIGNAL_MANAGEMENT_VIEWER',
                 description: "View signals from Signal Management"],

                //View all
                [authority  : 'ROLE_VIEW_ALL',
                 description: "View Individual Case, Aggregate Review, EVDAS Review, Literature Review Signal review from Signal Management section"],

                //Ad-hoc Case
                [authority  : 'ROLE_AD_HOC_CRUD',
                 description: "Create, read, update, and delete Ad-hoc Alerts"],

                //Queries ------------------------------------------------------------------------
                [authority  : 'ROLE_QUERY_ADVANCED',
                 description: "Advanced querying features like set builder and custom sql"],
                [authority  : 'ROLE_QUERY_CRUD',
                 description: "Create, read, update and delete Queries"],

                //View, Modify Alerts

                // Shared With related roles
                [authority  : 'ROLE_SHARE_ALL',
                 description: "Share alerts, signals or references from the dashboard widget with all the users"],

                [authority  : 'ROLE_SHARE_GROUP',
                 description: "Share alerts, signals or references with the user group(s)"],

                [authority  : 'ROLE_PRODUCTIVITY_AND_COMPLIANCE',
                 description: "Role to Access Alerts Operational Metrics"],

                [authority  : 'ROLE_MANAGE_PRODUCT_ASSIGNMENTS',
                 description: "Create, update and delete Product Assignments"],

                [authority  : 'ROLE_CONFIGURE_TEMPLATE_ALERT',
                 description: "Ability to configure an alert as alert template."],

                [authority  : 'ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT',
                 description: "Add, Update, delete  categories/Subcategories  from alert detail sections"],

                [authority  : 'ROLE_OPERATIONAL_METRICS',
                 description: "Role to Access Signal Operational metrics"],

                [authority  : 'ROLE_EXECUTE_SHARED_ALERTS',
                 description: "Allow user to execute the alerts shared with them even if user is not the owner of alert or an administrator."],

                [authority  : 'ROLE_REPORTING',
                 description: 'To enable the Reporting option under Analysis section.'],

                [authority  :  'ROLE_DATA_MINING_ADVANCED_RUNS',
                 description:  'Allow user to execute hierarchy alerts.'],
        ]
        if (Holders.config.signal.evdas.enabled == true) {
            //Evdas Case
             roles.add(authority: 'ROLE_EVDAS_CASE_CONFIGURATION',description:  'Perform EVDAS Alerts configuration and review')
             roles.add(authority: 'ROLE_EVDAS_CASE_REVIEWER',description:  'View, update EVDAS Alerts')
             roles.add(authority: 'ROLE_EVDAS_CASE_VIEWER',description:  'View EVDAS Alerts')
            if(Holders.config.evdasDataDownload.enabled){
                roles.add(authority: 'ROLE_EVDAS_AUTO_DOWNLOAD', description: 'To access Evdas Auto Download Utility')
            } else {
                Role evdasAutoDownloadRole = Role.findByAuthority('ROLE_EVDAS_AUTO_DOWNLOAD')
                if(evdasAutoDownloadRole){
                    log.info("Deleting Roles From all the Users")
                    String roleDeleteQuery = SignalQueryHelper.delete_roles_from_user(evdasAutoDownloadRole.id)
                    SQLQuery sql = null
                    Session session = sessionFactory.currentSession
                    try {
                        sql = session.createSQLQuery(roleDeleteQuery)
                        sql.executeUpdate()
                        evdasAutoDownloadRole.delete()
                        log.info("Successfully deleted Roles from all the users.")
                    } catch (Throwable throwable) {
                        throw throwable
                    } finally {
                        session.flush()
                        session.clear()
                    }
                }
            }
        }
        if (Holders.config.signal.faers.enabled == true) {
             roles.add(authority: 'ROLE_FAERS_CONFIGURATION',description:  'Aggregate Alerts Configuration for FAERS data source')
        }
        if (Holders.config.signal.vaers.enabled == true) {
            roles.add(authority: 'ROLE_VAERS_CONFIGURATION',description:  'Aggregate Alerts Configuration for VAERS data source')
        }
        if (Holders.config.signal.vigibase.enabled == true) {
            roles.add(authority: 'ROLE_VIGIBASE_CONFIGURATION',description:  'Perform VigiBase alerts configuration and review')
        }
        if (Holders.config.signal.jader.enabled == true) {
            roles.add(authority: 'ROLE_JADER_CONFIGURATION',description:  'Perform JADER alerts configuration and review')
        }
        if (Holders.config.signal.spotfire.enabled == true) {
            //Data Analysis
            roles.add(authority: 'ROLE_DATA_ANALYSIS', description: 'Access to Data Analysis information')
        }

        roles.collect {
            def role
            if(!Role.findByAuthority(it.authority)) {
                role = new Role(authority: it.authority, description: it.description,
                        createdBy: USERNAME, modifiedBy: USERNAME).save(failOnError: true, flush: true)
            }else{
             role = Role.findByAuthority(it.authority)
            }
        }
        roles
        log.info("Roles seeding done.")
    }

    // ========================== Seed Metadata ======================
    void seedMetadata() {
        seedSourceTableMasterMetaTable()
        seedCaseColumnJoinMetaTable()
        seedSourceColumnMasterMetaTable()
        seedReportFieldGroupsMetaTable()
        seedReportFieldsMetaTable()
    }

    void seedEvdasMetadata() {
        seedSourceTableMasterMetaTableEVDAS()
        seedSourceColumnMasterMetaTableEVDAS()
        seedReportFieldGroupsMetaTableEVDAS()
        seedReportFieldsMetaTableEVDAS()
    }

    def seedSourceTableMasterMetaTable() {
        Sql sql = new Sql(dataSource_pva)
        log.info("Seeding Source Table Master")

        try {
            sql.rows("SELECT * from SOURCE_TABLE_MASTER").collect {
                def colData = SourceTableMaster.findByTableName(it.TABLE_NAME)
                def newVal = new SourceTableMaster(tableName: it.TABLE_NAME, tableAlias: it?.TABLE_ALIAS, tableType: it.TABLE_TYPE,
                        caseJoinOrder: it.CASE_JOIN_ORDER, caseJoinType: it.CASE_JOIN_EQUI_OUTER, versionedData: it.VERSIONED_DATA,
                        isEudraField: false, hasEnterpriseId: it.HAS_ENTERPRISE_ID, isDeleted: it.IS_DELETED?.asBoolean())

                if (colData) {
                    if (!colData.equals(newVal)) {
                        colData.tableName = it.TABLE_NAME
                        colData.tableAlias = it.TABLE_ALIAS
                        colData.tableType = it.TABLE_TYPE
                        colData.caseJoinOrder = it.CASE_JOIN_ORDER
                        colData.versionedData = it.VERSIONED_DATA
                        colData.caseJoinType = it.CASE_JOIN_EQUI_OUTER
                        colData.hasEnterpriseId = it.HAS_ENTERPRISE_ID
                        colData.isEudraField = false
                        colData.isDeleted = it.IS_DELETED?.asBoolean()
                        CRUDService.updateWithoutAuditLog(colData)
                    }
                } else {
                    newVal.save(flush: true)
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage())
            log.info("Exception caught in seeding Source Table Master")
        } finally {
            sql.close()
        }
        log.info("Seeding Source Table Master Completed.")
    }

    def seedSourceTableMasterMetaTableEVDAS() {
        Sql sql = new Sql(dataSource_eudra)
        log.info("Seeding Source Table Master EVDAS")

        try {
            sql.rows("SELECT * from SOURCE_TABLE_MASTER").collect {
                def colData = SourceTableMaster.findByTableName(it.TABLE_NAME)
                def newVal = new SourceTableMaster(tableName: it.TABLE_NAME, tableAlias: it?.TABLE_ALIAS, tableType: it.TABLE_TYPE,
                        caseJoinOrder: it.CASE_JOIN_ORDER, caseJoinType: it.CASE_JOIN_EQUI_OUTER, versionedData: it.VERSIONED_DATA,
                        isEudraField: true, hasEnterpriseId: it.HAS_ENTERPRISE_ID, isDeleted: it.IS_DELETED?.asBoolean())

                if (colData) {
                    if (!colData.equals(newVal) && !(colData.tableName)?.equals(newVal.tableName)) {
                        colData.tableName = it.TABLE_NAME
                        colData.tableAlias = it.TABLE_ALIAS
                        colData.tableType = it.TABLE_TYPE
                        colData.caseJoinOrder = it.CASE_JOIN_ORDER
                        colData.versionedData = it.VERSIONED_DATA
                        colData.caseJoinType = it.CASE_JOIN_EQUI_OUTER
                        colData.hasEnterpriseId = it.HAS_ENTERPRISE_ID
                        colData.isDeleted = it.IS_DELETED?.asBoolean()
                        colData.isEudraField = true
                        CRUDService.updateWithoutAuditLog(colData)
                    }
                } else {
                    newVal.save(flush: true)
                }
            }
        } catch (Throwable e) {
            log.info("Exception caught in seeding Source Table Master EVDAS")
        } finally {
            sql.close()
        }
        log.info("Seeding Source Table Master EVDAS Completed.")
    }

    def seedCaseColumnJoinMetaTable() {
        log.info("Seeding Case Column Meta Table")
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.rows("SELECT * from CASE_COLUMN_JOIN_MAPPING").collect {
                def colData = CaseColumnJoinMapping.findByTableNameAndColumnNameAndMapColumnNameAndMapTableName(SourceTableMaster.get(it.TABLE_NAME_ATM_ID), it.COLUMN_NAME,
                        it.MAP_COLUMN_NAME, SourceTableMaster.get(it.MAP_TABLE_NAME_ATM_ID))
                def newVal = new CaseColumnJoinMapping(tableName: SourceTableMaster.get(it.TABLE_NAME_ATM_ID), columnName: it.COLUMN_NAME,
                        mapTableName: SourceTableMaster.get(it.MAP_TABLE_NAME_ATM_ID), mapColumnName: it.MAP_COLUMN_NAME, isDeleted: it.IS_DELETED?.asBoolean())

                if (colData) {
                    if (!colData.equals(newVal)) {
                        colData.tableName = SourceTableMaster.get(it.TABLE_NAME_ATM_ID)
                        colData.columnName = it.COLUMN_NAME
                        colData.mapTableName = SourceTableMaster.get(it.MAP_TABLE_NAME_ATM_ID)
                        colData.mapColumnName = it.MAP_COLUMN_NAME
                        colData.isDeleted = it.IS_DELETED?.asBoolean()
                        CRUDService.updateWithoutAuditLog(colData)
                    }
                } else {
                    newVal.save(flush: true)
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage())
            log.info("Exception caught in seeding case Column join Mapping")
        } finally {
            sql.close()
        }
        log.info("Seeding Case Column Meta Table Completed.")
    }

    void seedSourceColumnMasterMetaTable() {
        log.info("Seeding Source Column Master Meta Table")
        Sql sql = new Sql(dataSource_pva)
        List<SourceColumnMaster> srcColMasterList = []
        List<SourceColumnMaster> existSrcColMasterList = SourceColumnMaster.list()
        try {

            sql.rows("SELECT * from SOURCE_COLUMN_MASTER").collect {

                String reportItem = it.REPORT_ITEM
                SourceColumnMaster colData = existSrcColMasterList.find{
                    it.reportItem == reportItem
                }

                SourceColumnMaster newVal = new SourceColumnMaster(
                        tableName: SourceTableMaster.get(it.TABLE_NAME_ATM_ID),
                        columnName: it.COLUMN_NAME,
                        primaryKey: it.PRIMARY_KEY_ID,
                        lmTableName: SourceTableMaster.get(it.LM_TABLE_NAME_ATM_ID),
                        lmJoinColumn: it.LM_JOIN_COLUMN,
                        lmDecodeColumn: it.LM_DECODE_COLUMN,
                        columnType: it.COLUMN_TYPE,
                        reportItem: it.REPORT_ITEM,
                        lmJoinType: it.LM_JOIN_EQUI_OUTER,
                        isDeleted: it.IS_DELETED?.asBoolean(),
                        isEudraField: false,
                        concatField: it.CONCATENATED_FIELD)

                if (colData) {
                    if (!colData.equals(newVal)) {
                        colData.tableName = SourceTableMaster.get(it.TABLE_NAME_ATM_ID)
                        colData.columnName = it.COLUMN_NAME
                        colData.primaryKey = it.PRIMARY_KEY_ID
                        colData.lmTableName = SourceTableMaster.get(it.LM_TABLE_NAME_ATM_ID)
                        colData.lmJoinColumn = it.LM_JOIN_COLUMN
                        colData.lmDecodeColumn = it.LM_DECODE_COLUMN
                        colData.columnType = it.COLUMN_TYPE
                        colData.reportItem = it.REPORT_ITEM
                        colData.lmJoinType = it.LM_JOIN_EQUI_OUTER
                        colData.concatField = it.CONCATENATED_FIELD
                        colData.isDeleted = it.IS_DELETED?.asBoolean()
                        colData.isEudraField = false
                        srcColMasterList.add(colData)
                    }
                } else {
                    srcColMasterList.add(newVal)
                }
                if (srcColMasterList.size() > 1000) {
                    Session session = sessionFactory.currentSession
                    SourceColumnMaster.withTransaction {
                        for (SourceColumnMaster srcColMasterInstance in srcColMasterList) {
                            srcColMasterInstance.save()
                        }
                    }
                    session.flush()
                    session.clear()
                    srcColMasterList.clear()
                }
            }

            if (srcColMasterList.size() > 0) {
                Session session = sessionFactory.currentSession
                SourceColumnMaster.withTransaction {
                    for (SourceColumnMaster srcColMasterInstance in srcColMasterList) {
                        srcColMasterInstance.save()
                    }
                }
                session.flush()
                session.clear()
                srcColMasterList.clear()
            }

        } catch (Throwable e) {
            log.error(e.getMessage())
            log.info("Exception caught in seeding Source Column Master")
        } finally {
            sql.close()
        }
        log.info("Seeding Source Column Master Meta Table Completed.")
    }

    void seedSourceColumnMasterMetaTableEVDAS() {
        log.info("Seeding Source Column Master Meta Table EVDAS")
        Sql sql = new Sql(dataSource_eudra)
        try {
            sql.rows("SELECT * from SOURCE_COLUMN_MASTER").collect {
                def colData = SourceColumnMaster.get(it.REPORT_ITEM)
                def newVal = new SourceColumnMaster(
                        tableName: SourceTableMaster.get(it.TABLE_NAME_ATM_ID),
                        columnName: it.COLUMN_NAME,
                        primaryKey: it.PRIMARY_KEY_ID,
                        lmTableName: SourceTableMaster.get(it.LM_TABLE_NAME_ATM_ID),
                        lmJoinColumn: it.LM_JOIN_COLUMN,
                        lmDecodeColumn: it.LM_DECODE_COLUMN,
                        columnType: it.COLUMN_TYPE,
                        reportItem: it.REPORT_ITEM,
                        lmJoinType: it.LM_JOIN_EQUI_OUTER,
                        isDeleted: it.IS_DELETED?.asBoolean(),
                        isEudraField: true,
                        concatField: it.CONCATENATED_FIELD)

                if (colData) {
                    if (!colData.equals(newVal) && !(colData.columnName)?.equals(newVal.columnName)  &&
                            !(colData.tableName)?.equals(newVal.tableName)) {
                        colData.tableName = SourceTableMaster.get(it.TABLE_NAME_ATM_ID)
                        colData.columnName = it.COLUMN_NAME
                        colData.primaryKey = it.PRIMARY_KEY_ID
                        colData.lmTableName = SourceTableMaster.get(it.LM_TABLE_NAME_ATM_ID)
                        colData.lmJoinColumn = it.LM_JOIN_COLUMN
                        colData.lmDecodeColumn = it.LM_DECODE_COLUMN
                        colData.columnType = it.COLUMN_TYPE
                        colData.reportItem = it.REPORT_ITEM
                        colData.lmJoinType = it.LM_JOIN_EQUI_OUTER
                        colData.concatField = it.CONCATENATED_FIELD
                        colData.isDeleted = it.IS_DELETED?.asBoolean()
                        colData.isEudraField = true
                        CRUDService.updateWithoutAuditLog(colData)
                    }
                } else {
                    newVal.save(flush: true)
                }
            }

        } catch (Throwable e) {
            log.info("Exception caught in seeding Source Column Master EVDAS")
        } finally {
            sql.close()
        }
        log.info("Seeding Source Column Master Meta Table EVDAS Completed.")
    }

    def seedReportFieldGroupsMetaTable() {
        log.info("Seeding report field group Table")
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.rows("SELECT * from RPT_FIELD_GROUP").collect {
                def colData = ReportFieldGroup.findByName(it.NAME)
                def newVal = new ReportFieldGroup(
                        name: it.NAME,
                        isDeleted: it.IS_DELETED?.asBoolean(),
                        isEudraField: false
                )

                if (colData) {
                    if (!colData.equals(newVal)) {
                        colData.name = it.NAME
                        colData.isDeleted = it.IS_DELETED?.asBoolean()
                        colData.isEudraField = false
                        CRUDService.updateWithoutAuditLog(colData)
                    }
                } else {
                    newVal.save(flush: true)
                }
            }
        } catch (SQLException e) {
            log.info("Exception caught in seeding Report Field Group")
        } finally {
            sql.close()
        }
        log.info("Seeding report field group Table Completed.")
    }

    def seedReportFieldGroupsMetaTableEVDAS() {
        log.info("Seeding report field group Table EVDAS")
        Sql sql = new Sql(dataSource_eudra)
        try {
            sql.rows("SELECT * from RPT_FIELD_GROUP").collect {
                def colData = ReportFieldGroup.findByName(it.NAME)
                def newVal = new ReportFieldGroup(
                        name: it.NAME,
                        isDeleted: it.IS_DELETED?.asBoolean(),
                        isEudraField: true
                )

                if (colData) {
                    if (!colData.equals(newVal) && !(colData.name).equals(newVal.name)) {
                        colData.name = it.NAME
                        colData.isDeleted = it.IS_DELETED?.asBoolean()
                        colData.isEudraField = true
                        CRUDService.updateWithoutAuditLog(colData)
                    }
                } else {
                    newVal.save(flush: true)
                }
            }
        } catch (SQLException e) {
            log.info("Exception caught in seeding Report Field Group EVDAS")
        } finally {
            sql.close()
        }
        log.info("Seeding report field group EVDAS Table Completed.")
    }

    def seedReportFieldsMetaTable() {

        log.info("Seeding ReportField meta table")
        Sql sql = new Sql(dataSource_pva)
        List<ReportField> rptFieldList = []
        List<ReportField> existingRptField = ReportField.list()
        try {
            sql.rows("SELECT * from RPT_FIELD").each {

                String name = it.NAME
                ReportField existingRecord = existingRptField.find {
                    it.name == name
                }

                ReportField newRecord = new ReportField(
                        name: name,
                        description: it.DESCRIPTION,
                        transform: it.TRANSFORM,
                        fieldGroup: ReportFieldGroup.get(it.RPT_FIELD_GRPNAME),
                        sourceColumn: SourceColumnMaster.get(it.SOURCE_COLUMN_MASTER_ID),
                        dataType: Class.forName(it.DATA_TYPE),
                        isText: it.IS_TEXT?.asBoolean(),
                        listDomainClass: it.LIST_DOMAIN_CLASS ? Class.forName((it.LIST_DOMAIN_CLASS).trim()) : null,
                        lmSQL: it.LMSQL,
                        querySelectable: it.QUERY_SELECTABLE?.asBoolean(),
                        templateCLLSelectable: it.TEMPLT_CLL_SELECTABLE?.asBoolean(),
                        templateDTRowSelectable: it.TEMPLT_DTROW_SELECTABLE?.asBoolean(),
                        templateDTColumnSelectable: it.TEMPLT_DTCOL_SELECTABLE?.asBoolean(),
                        isDeleted: it.IS_DELETED?.asBoolean(),
                        dateFormat: it.DATE_FORMAT,
                        dictionaryType: it.DIC_TYPE,
                        dictionaryLevel: it.DIC_LEVEL,
                        isAutocomplete: it.ISAUTOCOMPLETE ? it.ISAUTOCOMPLETE : 0,
                        isEudraField: false
                )

                if (existingRecord) {

                    //check to see if it needs to be updated
                    if (!existingRecord.equals(newRecord)) {
                        existingRecord.name = name
                        existingRecord.description = it.DESCRIPTION
                        existingRecord.transform = it.TRANSFORM
                        existingRecord.fieldGroup = ReportFieldGroup.get(it.RPT_FIELD_GRPNAME)
                        existingRecord.sourceColumn = SourceColumnMaster.get(it.SOURCE_COLUMN_MASTER_ID)
                        existingRecord.dataType = Class.forName(it.DATA_TYPE)
                        existingRecord.isText = it.IS_TEXT?.asBoolean()
                        existingRecord.listDomainClass = it.LIST_DOMAIN_CLASS ? Class.forName((it.LIST_DOMAIN_CLASS).trim()) : null
                        existingRecord.lmSQL = it.LMSQL
                        existingRecord.querySelectable = it.QUERY_SELECTABLE?.asBoolean()
                        existingRecord.templateCLLSelectable = it.TEMPLT_CLL_SELECTABLE?.asBoolean()
                        existingRecord.templateDTRowSelectable = it.TEMPLT_DTROW_SELECTABLE?.asBoolean()
                        existingRecord.templateDTColumnSelectable = it.TEMPLT_DTCOL_SELECTABLE?.asBoolean()
                        existingRecord.isDeleted = it.IS_DELETED?.asBoolean()
                        existingRecord.dateFormat = it.DATE_FORMAT
                        existingRecord.dictionaryType = it.DIC_TYPE
                        existingRecord.dictionaryLevel = it.DIC_LEVEL
                        existingRecord.isAutocomplete = it.ISAUTOCOMPLETE ? it.ISAUTOCOMPLETE : 0
                        existingRecord.isEudraField = false
                        rptFieldList.add(existingRecord)
                    }
                } else {
                    rptFieldList.add(newRecord)
                }
            }
            batchPersistForDomain(rptFieldList, ReportField)
            log.info("Seeding ReportField meta table Completed.")
        } catch (SQLException e) {
            log.error(e.getMessage())
            log.info("Exception caught in seeding ReportField")
        } finally {
            sql.close()
        }
    }

    def seedReportFieldsMetaTableEVDAS() {

        log.info("Seeding ReportField meta table EVDAS")
        Sql sql = new Sql(dataSource_eudra)

        try {
            List<ReportField> rptFieldList = []
            List<ReportField> existingRptField = ReportField.list()
            sql.rows("SELECT * from RPT_FIELD").collect {

                String name = it.NAME
                ReportField existingRecord = existingRptField.find {
                    it.name == name
                }

                def newRecord = new ReportField (
                        name: it.NAME,
                        description: it.DESCRIPTION,
                        transform: it.TRANSFORM,
                        fieldGroup: ReportFieldGroup.get(it.RPT_FIELD_GRPNAME),
                        sourceColumn: SourceColumnMaster.get(it.SOURCE_COLUMN_MASTER_ID),
                        dataType: Class.forName(it.DATA_TYPE),
                        isText: it.IS_TEXT?.asBoolean(),
                        listDomainClass: it.LIST_DOMAIN_CLASS ? Class.forName((it.LIST_DOMAIN_CLASS).trim()) : null,
                        lmSQL: it.LMSQL,
                        querySelectable: it.QUERY_SELECTABLE?.asBoolean(),
                        templateCLLSelectable: it.TEMPLT_CLL_SELECTABLE?.asBoolean(),
                        templateDTRowSelectable: it.TEMPLT_DTROW_SELECTABLE?.asBoolean(),
                        templateDTColumnSelectable: it.TEMPLT_DTCOL_SELECTABLE?.asBoolean(),
                        isDeleted: it.IS_DELETED?.asBoolean(),
                        dateFormat: it.DATE_FORMAT,
                        dictionaryType: it.DIC_TYPE,
                        dictionaryLevel: it.DIC_LEVEL,
                        isAutocomplete: it.ISAUTOCOMPLETE ? it.ISAUTOCOMPLETE : 0,
                        isEudraField: true
                )

                if (existingRecord) {

                    //check to see if it needs to be updated
                    if (!existingRecord.equals(newRecord)) {
                        existingRecord.name = it.NAME
                        existingRecord.description = it.DESCRIPTION
                        existingRecord.transform = it.TRANSFORM
                        existingRecord.fieldGroup = ReportFieldGroup.get(it.RPT_FIELD_GRPNAME)
                        existingRecord.sourceColumn = SourceColumnMaster.get(it.SOURCE_COLUMN_MASTER_ID)
                        existingRecord.dataType = Class.forName(it.DATA_TYPE)
                        existingRecord.isText = it.IS_TEXT?.asBoolean()
                        existingRecord.listDomainClass = it.LIST_DOMAIN_CLASS ? Class.forName((it.LIST_DOMAIN_CLASS).trim()) : null
                        existingRecord.lmSQL = it.LMSQL
                        existingRecord.querySelectable = it.QUERY_SELECTABLE?.asBoolean()
                        existingRecord.templateCLLSelectable = it.TEMPLT_CLL_SELECTABLE?.asBoolean()
                        existingRecord.templateDTRowSelectable = it.TEMPLT_DTROW_SELECTABLE?.asBoolean()
                        existingRecord.templateDTColumnSelectable = it.TEMPLT_DTCOL_SELECTABLE?.asBoolean()
                        existingRecord.isDeleted = it.IS_DELETED?.asBoolean()
                        existingRecord.dateFormat = it.DATE_FORMAT
                        existingRecord.dictionaryType = it.DIC_TYPE
                        existingRecord.dictionaryLevel = it.DIC_LEVEL
                        existingRecord.isAutocomplete = it.ISAUTOCOMPLETE ? it.ISAUTOCOMPLETE : 0
                        existingRecord.isEudraField = true
                        rptFieldList.add(existingRecord)
                    }

                } else {
                    rptFieldList.add(newRecord)
                }
            }
            batchPersistForDomain(rptFieldList, ReportField)

            log.info("Seeding ReportField meta table Completed EVDAS")
        } catch (SQLException e) {
            log.error(e.getMessage())
            log.info("Exception caught in seeding ReportField EVDAS")
        } finally {
            sql.close()
        }
    }

    def readPvsExtConfig() {
        ConfigSlurper config = new ConfigSlurper()
        config.parse(getURLForMetadata("$externalized"))
    }

    def InputStream getInputStreamForMetadata(String dataFilePath) {
        def rcs = grailsApplication.getMainContext().getResource(
                "$Holders.config.grails.pvsignal.config.root/$dataFilePath")
        if (rcs.exists()) {
            rcs.getInputStream()
        } else
            new FileInputStream(grailsApplication.getMainContext().getResource(
                    "classpath:$metadataLocation/${dataFilePath}")?.file)
    }

    def URL getURLForMetadata(String dataFilePath) {
        def rcs = grailsApplication.getMainContext().getResource (
                "$Holders.config.grails.pvsignal.config.root/$dataFilePath")

        if (rcs.exists())
            rcs.getURL()
        else {
            grailsApplication.getMainContext().getResource("classpath:$metadataLocation/$dataFilePath")?.getURL()
        }
    }

    // should the "builders" be put into the domain classes, force their maintenance/upkeep there?

    protected void seedETLSchedule() {
        EtlSchedule eTLSchedule =
                new EtlSchedule(scheduleName: "ETL", startDateTime: "2015-03-31T03:23+02:00", repeatInterval: "FREQ=DAILY;INTERVAL=1;",
                        isInitial: false, isDisabled: true, createdBy: "bootstrap", modifiedBy: "bootstrap")
        eTLSchedule.save(failOnError: true, flush: true)
    }

    // ==========================================  private methods ====================================================

    private JSONObject loadJSONFile(InputStream datStream) {
        return new JsonSlurper().parseText(datStream.text) as JSONObject
    }

    def private seedAlertDocument() {
        def documentJSON = [[
                                    "chronicleId"   : "rxlogix1234",
                                    "documentLink"  : "http://www.rxlogix.com",
                                    "startDate"     : DateUtil.parseDate("2015/04/04"),
                                    "documentStatus": "Pending",
                                    "author"        : "Chetan Sharma",
                                    "statusDate"    : DateUtil.parseDate("2015/10/10"),
                                    "documentType"  : 'Validation'
                            ], [
                                    "chronicleId"   : "rxlogix2345",
                                    "documentLink"  : "http://www.rxlogix.com",
                                    "startDate"     : DateUtil.parseDate("2015/10/10"),
                                    "documentStatus": "Completed",
                                    "author"        : "Ajey Singh",
                                    "statusDate"    : DateUtil.parseDate("2015/11/11"),
                                    "documentType"  : 'Validation'
                            ], [
                                    "chronicleId"   : "rxlogix6543",
                                    "documentLink"  : "http://www.rxlogix.com",
                                    "startDate"     : DateUtil.parseDate("2015/09/09"),
                                    "documentStatus": "Pending",
                                    "author"        : "Tushar Saxena",
                                    "statusDate"    : DateUtil.parseDate("2015/11/11"),
                                    "documentType"  : 'Validation'
                            ], [
                                    "chronicleId"   : "rxlogix9876",
                                    "documentLink"  : "http://www.rxlogix.com",
                                    "startDate"     : DateUtil.parseDate("2015/08/08"),
                                    "documentStatus": "Completed",
                                    "author"        : "Ankit Kumar",
                                    "statusDate"    : DateUtil.parseDate("2015/10/10"),
                                    "documentType"  : 'Validation'
                            ]]

        documentJSON.each {
            def args = it as Map
            def ad = new AlertDocument(args)
            ad.save(flush: true)
        }
    }

    def seedGenericNameCache() {
        LmProduct.withTransaction {
            genericNameCacheService.set(LmProduct.list())
        }
    }

    @Transactional
    def seedCategoriesAndTags() {
        (readPvsExtConfig().grails.pvsignal.categories as List).each {
            if (!Category.findByName(it.name)) {
                new Category(name: it.name, defaultName: it.defaultName).save()
            }
        }
        List qualityTags = [[name: PvqTypeEnum.SAMPLING.value()],
                            [name: PvqTypeEnum.CASE_QUALITY.value()],
                            [name: PvqTypeEnum.SUBMISSION_QUALITY.value()]]
        qualityTags.each {
            if (!Tag.findByName(it.name)) {
                new Tag(name: it.name).save()
            }
        }
    }

    void batchPersistForDomain(List domainList, Class domainClz) throws Exception {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        domainClz.withTransaction {
            List batch = []
            domainList.eachWithIndex { def domain, Integer index ->
                batch += domain
                domain.save(validate: false)
                if (index && index.mod(batchSize) == 0) {
                    Session session = sessionFactory.currentSession
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }

        log.info("Data is persisted.")
    }

}
