package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
//@Mock([SingleCaseAlert, AggregateCaseAlert, LiteratureAlert, EvdasAlert, Configuration, ExecutedConfiguration, LiteratureConfiguration, ExecutedEvdasConfiguration, ExecutedLiteratureConfiguration, EvdasConfiguration, User])
//@TestFor(ArchiveService)
@Ignore
class ArchiveServiceSpec extends Specification implements ServiceUnitTest<ArchiveService> {

    Configuration qualConfiguration
    Configuration quantConfiguration
    LiteratureConfiguration literatureConfiguration
    EvdasConfiguration evdasConfiguration
    ExecutedConfiguration executedQualConfiguration
    ExecutedConfiguration executedQuantConfiguration
    ExecutedLiteratureConfiguration executedLiteratureConfiguration
    ExecutedEvdasConfiguration executedEvdasConfiguration
    User mockUser

    void setup() {

        //Prepare the mock user
        mockUser = new User(id: 1, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        mockUser.preference.createdBy = "createdBy"
        mockUser.preference.modifiedBy = "modifiedBy"
        mockUser.preference.locale = new Locale("en")
        mockUser.preference.isEmailEnabled = false
        mockUser.metaClass.getFullName = { 'Fake Name' }
        mockUser.metaClass.getEmail = { 'fake.email@fake.com' }
        mockUser.save(validate: false)

        qualConfiguration = new Configuration(
                id: 1,
                executing: false,
                name: "Qual Test",
                productSelection: "Test Product A",
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser,
                owner: mockUser,
                isEnabled: true,
                type: Constants.AlertConfigType.SINGLE_CASE_ALERT
        )
        AlertDateRangeInformation alertDateRangeInformation = new AlertDateRangeInformation()
        alertDateRangeInformation.dateRangeStartAbsolute = new Date()
        alertDateRangeInformation.dateRangeEndAbsolute = new Date()
        qualConfiguration.alertDateRangeInformation = alertDateRangeInformation
        qualConfiguration.save(validate: false)
        quantConfiguration = new Configuration(
                id: 2,
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "Quant Test",
                productSelection: "Test Product A",
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser,
                owner: mockUser,
                isEnabled: true,
                nextRunDate: new Date(),
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
        )

        AlertDateRangeInformation alertDateRangeInformation1 = new AlertDateRangeInformation()
        alertDateRangeInformation1.dateRangeStartAbsolute = null
        alertDateRangeInformation1.dateRangeEndAbsolute = null
        quantConfiguration.alertDateRangeInformation = alertDateRangeInformation1
        quantConfiguration.save(validate: false)

        literatureConfiguration = new LiteratureConfiguration(
                id: 3,
                name: "Literature Test",
                productSelection: "Test Product A",
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser,
                owner: mockUser,
        )
        literatureConfiguration.save(validate: false)

        evdasConfiguration = new EvdasConfiguration(
                id: 4,
                name: "Evdas Test",
                productSelection: "Test Product A",
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser,
                owner: mockUser,
        )
        evdasConfiguration.save(validate: false)

        ExecutedConfiguration executedQualConfiguration1 = new ExecutedConfiguration(id: 1, name: qualConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: 1)
        executedQualConfiguration1.save(validate: false)

        executedQualConfiguration = new ExecutedConfiguration(id: 2, name: qualConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: 1)
        executedQualConfiguration.save(validate: false)

        ExecutedConfiguration executedQuantConfiguration1 = new ExecutedConfiguration(id: 3, name: quantConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: 2)
        executedQuantConfiguration1.save(validate: false)

        executedQuantConfiguration = new ExecutedConfiguration(id: 4, name: quantConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: 2)
        executedQuantConfiguration.save(validate: false)

        ExecutedLiteratureConfiguration executedLiteratureConfiguration1 = new ExecutedLiteratureConfiguration(id: 5, name: literatureConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: literatureConfiguration.id)
        executedLiteratureConfiguration1.save(validate: false)

        executedLiteratureConfiguration = new ExecutedLiteratureConfiguration(id: 6, name: literatureConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: literatureConfiguration.id)
        executedLiteratureConfiguration.save(validate: false)

        ExecutedEvdasConfiguration executedEvdasConfiguration1 = new ExecutedEvdasConfiguration(id: 7, name: evdasConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: evdasConfiguration.id)
        executedEvdasConfiguration1.save(validate: false)

        executedEvdasConfiguration = new ExecutedEvdasConfiguration(id: 8, name: evdasConfiguration.name,
                owner: mockUser,
                createdBy: mockUser.username, modifiedBy: mockUser.username,
                assignedTo: mockUser, configId: evdasConfiguration.id)
        executedEvdasConfiguration.save(validate: false)

        SingleCaseAlert singleCaseAlert = new SingleCaseAlert(id: 1, alertConfiguration: qualConfiguration, executedAlertConfiguration: executedQualConfiguration1).save(validate: false)
        SingleCaseAlert singleCaseAlert1 = new SingleCaseAlert(id: 2, alertConfiguration: qualConfiguration, executedAlertConfiguration: executedQualConfiguration).save(validate: false)
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert(id: 3, alertConfiguration: quantConfiguration, executedAlertConfiguration: executedQuantConfiguration1).save(validate: false)
        AggregateCaseAlert aggregateCaseAlert1 = new AggregateCaseAlert(id: 4, alertConfiguration: quantConfiguration, executedAlertConfiguration: executedQuantConfiguration).save(validate: false)
        LiteratureAlert literatureAlert = new LiteratureAlert(id: 5, litSearchConfig: literatureConfiguration, exLitSearchConfig: executedLiteratureConfiguration1).save(validate: false)
        LiteratureAlert literatureAlert1 = new LiteratureAlert(id: 6, litSearchConfig: literatureConfiguration, exLitSearchConfig: executedLiteratureConfiguration).save(validate: false)
        EvdasAlert evdasAlert = new EvdasAlert(id: 7, alertConfiguration: evdasConfiguration, executedAlertConfiguration: executedEvdasConfiguration1).save(validate: false)
        EvdasAlert evdasAlert1 = new EvdasAlert(id: 8, alertConfiguration: evdasConfiguration, executedAlertConfiguration: executedEvdasConfiguration).save(validate: false)
    }

    static String qualArchiveSql() {
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
             SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM user_tab_cols
       WHERE table_name = 'ARCHIVED_SINGLE_CASE_ALERT';
       lvc_exec_sql := 'INSERT into ARCHIVED_SINGLE_CASE_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM SINGLE_CASE_ALERT WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1';
       execute immediate lvc_exec_sql;
      
       INSERT into VALIDATED_ARCHIVED_SCA(ARCHIVED_SCA_ID,VALIDATED_SIGNAL_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.VALIDATED_SIGNAL_ID
       FROM VALIDATED_SINGLE_ALERTS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
    
       INSERT into ARCHIVED_SCA_TAGS(SINGLE_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.PVS_ALERT_TAG_ID
       FROM SINGLE_CASE_ALERT_TAGS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
     
       INSERT into ARCHIVED_SCA_PT(ARCHIVED_SCA_ID,ARCHIVED_SCA_PT,PT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PT, vsca.PT_LIST_IDX
       FROM SINGLE_ALERT_PT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
     
     
       INSERT into ARCHIVED_SCA_CON_COMIT(ARCHIVED_SCA_ID,ALERT_CON_COMIT,CON_COMIT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.ALERT_CON_COMIT, vsca.CON_COMIT_LIST_IDX
       FROM SINGLE_ALERT_CON_COMIT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
     
       INSERT into ARCHIVED_SCA_SUSP_PROD(ARCHIVED_SCA_ID,SCA_PRODUCT_NAME,SUSPECT_PRODUCT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PRODUCT_NAME, vsca.SUSPECT_PRODUCT_LIST_IDX
       FROM SINGLE_ALERT_SUSP_PROD vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
      
       INSERT into ARCHIVED_SCA_MED_ERR_PT_LIST(ARCHIVED_SCA_ID,SCA_MED_ERROR,MED_ERROR_PT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_MED_ERROR, vsca.MED_ERROR_PT_LIST_IDX
       FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
     
 
       INSERT into ARCHIVED_SCA_ACTIONS(ARCHIVED_SCA_ID,ACTION_ID) SELECT vsca.SINGLE_CASE_ALERT_ID, vsca.ACTION_ID
       FROM SINGLE_ALERT_ACTIONS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
             WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
     
       --      Move the attachments to Archived Single Case Alert
    
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join SINGLE_CASE_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1) conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedSingleCaseAlert';
        UPDATE case_history
        SET archived_single_alert_id=single_alert_id,
            single_alert_id = null
        WHERE CONFIG_ID = 1 and EXEC_CONFIG_ID  = 1;
     
       DELETE FROM VALIDATED_SINGLE_ALERTS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM VALIDATED_SINGLE_ALERTS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1
        );
     
       DELETE FROM SINGLE_ALERT_TAGS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_TAGS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1
        );
      
       DELETE FROM SINGLE_ALERT_PT WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_PT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1
        );
       DELETE FROM SINGLE_ALERT_CON_COMIT WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_CON_COMIT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1
        );
       DELETE FROM SINGLE_ALERT_SUSP_PROD WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_SUSP_PROD vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1
        );
     
       DELETE FROM SINGLE_ALERT_MED_ERR_PT_LIST WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1
        );
 
 
       DELETE FROM SINGLE_GLOBAL_TAG_MAPPING WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_GLOBAL_TAG_MAPPING vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1);
     
       DELETE FROM SINGLE_ALERT_ACTIONS WHERE (SINGLE_CASE_ALERT_ID) in (
       SELECT vsca.SINGLE_CASE_ALERT_ID
       FROM SINGLE_ALERT_ACTIONS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1);
    
        DELETE FROM single_case_alert WHERE ALERT_CONFIGURATION_ID = 1 and EXEC_CONFIG_ID = 1;
      exception when others
      then
      raise_application_error(-20001,dbms_utility.format_error_backtrace);
        END;"""
    }

    static String quantArchiveSql() {
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM user_tab_cols
       WHERE table_name = 'ARCHIVED_AGG_ALERT';
       lvc_exec_sql := 'INSERT into ARCHIVED_AGG_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3';
       execute immediate lvc_exec_sql;
      
       INSERT into VALIDATED_ARCHIVED_ACA(ARCHIVED_ACA_ID,VALIDATED_SIGNAL_ID) SELECT vaca.AGG_ALERT_ID, vaca.VALIDATED_SIGNAL_ID
       FROM VALIDATED_AGG_ALERTS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3;
      
       INSERT into ARCHIVED_AGG_CASE_ALERT_TAGS(AGG_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vaca.AGG_ALERT_ID, vaca.PVS_ALERT_TAG_ID
       FROM AGG_CASE_ALERT_TAGS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3;
     
       INSERT into ARCHIVED_ACA_ACTIONS(ARCHIVED_ACA_ID,ACTION_ID) SELECT vaca.AGG_ALERT_ID, vaca.ACTION_ID
       FROM AGG_ALERT_ACTIONS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3;

--      Move the attachments to Archived Aggregate Case Alert
       
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join AGG_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3) conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedAggregateCaseAlert';
       
        UPDATE product_event_history
        SET archived_agg_case_alert_id=AGG_CASE_ALERT_ID,
            AGG_CASE_ALERT_ID = null
        WHERE CONFIG_ID = 2 and EXEC_CONFIG_ID = 3;
      
        DELETE FROM VALIDATED_AGG_ALERTS WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM VALIDATED_AGG_ALERTS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3
        );
      
        DELETE FROM AGG_ALERT_TAGS WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM AGG_ALERT_TAGS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3
        );
     
        DELETE FROM AGG_ALERT_ACTIONS WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM AGG_ALERT_ACTIONS vaca
        INNER JOIN AGG_ALERT aca
            ON vaca.AGG_ALERT_ID = aca.ID
        WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3
        );
      
        DELETE FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = 2 and EXEC_CONFIGURATION_ID = 3;
      exception when others
      then
      raise_application_error(-20001,dbms_utility.format_error_backtrace);
        END;"""
    }

    static String evdasArchiveSql() {
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM user_tab_cols
       WHERE table_name = 'ARCHIVED_EVDAS_ALERT';

       lvc_exec_sql := 'INSERT into ARCHIVED_EVDAS_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7';
      execute immediate lvc_exec_sql;

       INSERT into VALIDATED_ARCH_EVDAS_ALERTS(ARCHIVED_EVDAS_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT veva.EVDAS_ALERT_ID, veva.VALIDATED_SIGNAL_ID
       FROM VALIDATED_EVDAS_ALERTS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7;

       INSERT into ARCHIVED_EVDAS_ALERT_ACTIONS(ARCHIVED_EVDAS_ALERT_ID,ACTION_ID) SELECT veva.EVDAS_ALERT_ID, veva.ACTION_ID
       FROM EVDAS_ALERT_ACTIONS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7;

--      Move the attachments to Archived Evdas Alert

        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join EVDAS_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7) conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedEvdasAlert';

        MERGE INTO evdas_history eh
        USING (SELECT t1.id as history_id, t1.EVDAS_ALERT_ID as alert_id FROM evdas_history t1 left join EVDAS_ALERT t2 on t1.EVDAS_ALERT_ID = t2.id WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7) conf
        ON (eh.id = conf.history_id)
        WHEN matched THEN UPDATE SET eh.ARCHIVED_EVDAS_ALERT_ID=conf.alert_id,eh.EVDAS_ALERT_ID = null;

        DELETE FROM VALIDATED_EVDAS_ALERTS WHERE (EVDAS_ALERT_ID) in
        (SELECT veva.EVDAS_ALERT_ID
        FROM VALIDATED_EVDAS_ALERTS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7);

        DELETE FROM EVDAS_ALERT_ACTIONS WHERE (EVDAS_ALERT_ID) in
        (SELECT veva.EVDAS_ALERT_ID
        FROM EVDAS_ALERT_ACTIONS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7);

        DELETE FROM EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = 4 and EXEC_CONFIGURATION_ID = 7;
      exception when others
      then
      raise_application_error(-20001,dbms_utility.format_error_backtrace);
        END;"""
    }

    static String litArchiveSql() {
        """declare
       lvc_sql VARCHAR2(32000);
       lvc_exec_sql VARCHAR2(32000);
       BEGIN
       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM user_tab_cols
       WHERE table_name = 'ARCHIVED_LITERATURE_ALERT';

       lvc_exec_sql := 'INSERT into ARCHIVED_LITERATURE_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM LITERATURE_ALERT WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5';
       execute immediate lvc_exec_sql;

       INSERT into VALIDATED_ARCHIVED_LIT_ALERTS(ARCHIVED_LIT_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT ala.LITERATURE_ALERT_ID, ala.VALIDATED_SIGNAL_ID
       FROM VALIDATED_LITERATURE_ALERTS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5;

       INSERT into ARCHIVED_LIT_ALERT_TAGS(ARCHIVED_LIT_ALERT_ID,ALERT_TAG_ID) SELECT ala.LITERATURE_ALERT_ID, ala.ALERT_TAG_ID
       FROM LITERATURE_ALERT_TAGS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5;

       INSERT into ARCHIVED_LIT_ALERT_ACTIONS(ARCHIVED_LIT_ALERT_ID,ACTION_ID) SELECT ala.LITERATURE_ALERT_ID, ala.ACTION_ID
       FROM LIT_ALERT_ACTIONS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5;

--      Move the attachments to Archived Literature Alert
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join LITERATURE_ALERT t2 on t1.reference_id = t2.id WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5) conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedLiteratureAlert';

        DELETE FROM VALIDATED_LITERATURE_ALERTS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID FROM VALIDATED_LITERATURE_ALERTS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5
        );
        
        DELETE FROM LITERATURE_ALERT_TAGS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID FROM LITERATURE_ALERT_TAGS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5
        );

        DELETE FROM LIT_ALERT_ACTIONS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID FROM LIT_ALERT_ACTIONS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5
        );
        
        DELETE FROM LITERATURE_ALERT WHERE lit_search_config_id = 3 and ex_lit_search_config_id = 5;
      
      exception when others
      then
      raise_application_error(-20001,dbms_utility.format_error_backtrace);
      END;"""
    }

    void "testing method oldExecutedAlertId"() {
        when:
        Long archivedExecutedId = service.oldExecutedAlertId(domain, configId, executedConfigId)
        then:
        archivedExecutedId == result
        where:
        sno | domain             | configId | executedConfigId | result
        1   | SingleCaseAlert    | 1        | 2                | 0
        2   | AggregateCaseAlert | 2        | 4                | 0
        3   | LiteratureAlert    | 3        | 6                | 0
        4   | EvdasAlert         | 4        | 8                | 0
    }

    @Ignore
    void "testing archiveSqlQuery() method for Qualitative"() {
        given: "SingleCaseAlert domain, configurationId and executedConfigurationId"

        when:
        String resultSql = service.archiveSqlQuery(SingleCaseAlert, 1, 1)

        then:
        resultSql.equals(qualArchiveSql())
    }

    void "testing archiveSqlQuery() method for Quantitative"() {
        given: "AggregateCaseAlert domain, configurationId and executedConfigurationId"

        when:
        String resultSql = service.archiveSqlQuery(AggregateCaseAlert, 2, 3)

        then:
        resultSql.equals(quantArchiveSql())
    }

    @Ignore
    void "testing archiveSqlQuery() method for Litearure"() {
        given: "LiteratureAlert domain, configurationId and executedConfigurationId"

        when:
        String resultSql = service.archiveSqlQuery(LiteratureAlert, 3, 5)

        then:
        resultSql.equals(litArchiveSql())
    }

    @Ignore
    void "testing archiveSqlQuery() method for Evdas"() {
        given: "EvdasAlert, configurationId and executedConfigurationId"

        when:
        String resultSql = service.archiveSqlQuery(EvdasAlert, 4, 7)

        then:
        resultSql.equals(evdasArchiveSql())
    }

    void "testing archiveSqlQuery() method when domain is not SCA, ACA, Evdas ALert and Literature Alert"() {
        given: "Adhoc, configurationId and executedConfigurationId"

        when:
        String resultSql = service.archiveSqlQuery(AggregateOnDemandAlert, 4, 7)

        then:
        resultSql.equals('')
    }

}