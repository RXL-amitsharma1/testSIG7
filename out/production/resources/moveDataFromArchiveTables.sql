create or replace PROCEDURE MOVE_ALERT_FROM_ARCHIVE(current_exe_id IN NUMBER, pre_exe_id IN NUMBER,
                                                    alert_config_id IN NUMBER, alert_type VARCHAR2) AS
  type typename_type is table of VARCHAR2(32767);
  typename typename_type;
  v_code NUMBER;
  lvc_sql VARCHAR2(32000);
  lvc_exec_sql VARCHAR2(32000);
BEGIN

  BEGIN


    if (alert_type = 'Single Case Alert') then
      DELETE
      FROM VALIDATED_SINGLE_ALERTS
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM VALIDATED_SINGLE_ALERTS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );

      DELETE
      FROM SINGLE_ALERT_PT
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_PT vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );

      DELETE
      FROM SINGLE_ALERT_CON_COMIT
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_CON_COMIT vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );

      DELETE
      FROM SINGLE_ALERT_SUSP_PROD
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_SUSP_PROD vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );

      DELETE
      FROM SINGLE_ALERT_MED_ERR_PT_LIST
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );

      DELETE
      FROM SINGLE_ALERT_ACTIONS
      WHERE (SINGLE_CASE_ALERT_ID) in (
        SELECT vsca.SINGLE_CASE_ALERT_ID
        FROM SINGLE_ALERT_ACTIONS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id);

      DELETE FROM ALERT_COMMENT where EX_CONFIG_ID = current_exe_id and ALERT_TYPE = alert_type;


      DELETE
      FROM SINGLE_SIGNAL_CONCEPTS
      WHERE (SINGLE_CASE_ALERT_ID) in (
        SELECT vsca.SINGLE_CASE_ALERT_ID
        FROM SINGLE_SIGNAL_CONCEPTS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_TOPIC_CONCEPTS
      WHERE (SINGLE_CASE_ALERT_ID) in (
        SELECT vsca.SINGLE_CASE_ALERT_ID
        FROM SINGLE_TOPIC_CONCEPTS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM TOPIC_SINGLE_ALERTS
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM TOPIC_SINGLE_ALERTS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );


      DELETE
      FROM SINGLE_GLOBAL_TAG_MAPPING
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_GLOBAL_TAG_MAPPING vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );

      DELETE
      FROM SINGLE_ALERT_INDICATION_LIST
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_INDICATION_LIST vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_CAUSE_OF_DEATH
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_CAUSE_OF_DEATH vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_PAT_MED_HIST
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_PAT_MED_HIST vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_PAT_HIST_DRUGS
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_PAT_HIST_DRUGS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_BATCH_LOT_NO
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_BATCH_LOT_NO vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_CASE_CLASSIFI
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_CASE_CLASSIFI vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_THERAPY_DATES
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_THERAPY_DATES vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_DOSE_DETAILS
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_DOSE_DETAILS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_PRIM_SUSP
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_PRIM_SUSP vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_PRIM_PAI
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_PRIM_PAI vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_ALL_PAI
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_ALL_PAI vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_ALL_PT
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_ALL_PT vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_GENERIC_NAME
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_GENERIC_NAME vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_ALLPT_OUT_COME
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_ALLPT_OUT_COME vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM SINGLE_ALERT_CROSS_REFERENCE_IND
      WHERE (SINGLE_ALERT_ID) in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_CROSS_REFERENCE_IND vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = current_exe_id
      );



      DELETE
      FROM SINGLE_GLOBAL_TAGS
      WHERE PVS_GLOBAL_TAG_ID in (select agt.PVS_GLOBAL_TAG_ID
                                  from SINGLE_GLOBAL_TAGS agt
                                         inner join PVS_GLOBAL_TAG pgt on agt.PVS_GLOBAL_TAG_ID = pgt.ID and
                                                                          pgt.EXEC_CONFIG_ID = current_exe_id and
                                                                          pgt.DOMAIN = 'Single Case Alert'
                                    and agt.CREATION_DATE >
                                        (select DATE_CREATED from EX_RCONFIG where ID =current_exe_id )
      );


      DELETE
      FROM SINGLE_CASE_ALERT_TAGS
      WHERE PVS_ALERT_TAG_ID in (
        select ID
        from PVS_ALERT_TAG
        WHERE exec_config_id = current_exe_id
      );

      DELETE
      FROM PVS_GLOBAL_TAG
      where EXEC_CONFIG_ID = current_exe_id
        and DOMAIN = 'Single Case Alert'
        and CREATED_AT >
            (select DATE_CREATED from EX_RCONFIG where ID = current_exe_id);




      DELETE
      from ARCHIVED_SCA_TAGS
      where SINGLE_ALERT_ID in (
        select ALERT_ID
        FROM PVS_ALERT_TAG pat
        WHERE  pat.EXEC_CONFIG_ID = current_exe_id
      );

      DELETE
      FROM PVS_ALERT_TAG
      where EXEC_CONFIG_ID = current_exe_id
        AND DOMAIN = alert_type;



      DELETE  FROM ACTIONS WHERE EXEC_CONFIG_ID=current_exe_id AND ALERT_TYPE=alert_type;
      DELETE FROM EX_TEMPLT_QRS_EX_QUERY_VALUES WHERE EX_TEMPLT_QUERY_ID in (select id from EX_TEMPLT_QUERY  WHERE EX_RCONFIG_ID = current_exe_id);
      DELETE FROM EX_TEMPLT_QUERY WHERE EX_RCONFIG_ID=current_exe_id;
      DELETE FROM ex_rconfig_activities where EX_CONFIG_ACTIVITIES_ID = current_exe_id;
      DELETE FROM EX_RCONFIGS_PROD_GRP where EXCONFIG_ID = current_exe_id;
      DELETE FROM EX_ALERT_QUERY_VALUES where EX_ALERT_QUERY_ID = current_exe_id;
      DELETE FROM EX_FG_ALERT_QUERY_VALUES where EX_FG_QUERY_VALUE_ID = current_exe_id;
      DELETE FROM CASE_HISTORY where EXEC_CONFIG_ID = current_exe_id;


      DELETE FROM single_case_alert WHERE ALERT_CONFIGURATION_ID = alert_config_id and EXEC_CONFIG_ID = current_exe_id;
      DBMS_OUTPUT.PUT_LINE('Table deletion has been finished for current_exe_id: ' || current_exe_id);

      DBMS_OUTPUT.PUT_LINE('Archiving started for pre_exe_id: ' || pre_exe_id);
      SELECT listagg(column_name, ',') within group (order by column_id) as cols INTO lvc_sql
      FROM user_tab_columns
      WHERE table_name = 'SINGLE_CASE_ALERT';

      lvc_exec_sql := 'INSERT into SINGLE_CASE_ALERT (' || lvc_sql || ') SELECT ' || lvc_sql ||
                      ' FROM ARCHIVED_SINGLE_CASE_ALERT WHERE ALERT_CONFIGURATION_ID = ' || alert_config_id ||
                      ' and EXEC_CONFIG_ID =' || pre_exe_id;
      execute immediate lvc_exec_sql;

      UPDATE case_history
      SET archived_single_alert_id=single_alert_id,single_alert_id = null
      WHERE CONFIG_ID = alert_config_id and EXEC_CONFIG_ID  = pre_exe_id;

      INSERT into VALIDATED_SINGLE_ALERTS (SINGLE_ALERT_ID, VALIDATED_SIGNAL_ID)
      SELECT vsca.ARCHIVED_SCA_ID, vsca.VALIDATED_SIGNAL_ID
      FROM VALIDATED_ARCHIVED_SCA vsca
             INNER JOIN SINGLE_CASE_ALERT sca
                        ON vsca.ARCHIVED_SCA_ID = sca.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;

      INSERT into SINGLE_CASE_ALERT_TAGS (SINGLE_ALERT_ID, PVS_ALERT_TAG_ID)
      SELECT vsca.SINGLE_ALERT_ID, vsca.PVS_ALERT_TAG_ID
      FROM ARCHIVED_SCA_TAGS vsca
             INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                        ON vsca.SINGLE_ALERT_ID = sca.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;

      INSERT into SINGLE_ALERT_PT (SINGLE_ALERT_ID, SCA_PT, PT_LIST_IDX)
      SELECT vsca.ARCHIVED_SCA_ID, vsca.ARCHIVED_SCA_PT, vsca.PT_LIST_IDX
      FROM ARCHIVED_SCA_PT vsca
             INNER JOIN SINGLE_CASE_ALERT sca
                        ON vsca.ARCHIVED_SCA_ID = sca.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;

      INSERT into SINGLE_ALERT_CON_COMIT (SINGLE_ALERT_ID, ALERT_CON_COMIT, CON_COMIT_LIST_IDX)
      SELECT vsca.ARCHIVED_SCA_ID, vsca.ALERT_CON_COMIT, vsca.CON_COMIT_LIST_IDX
      FROM ARCHIVED_SCA_CON_COMIT vsca
             INNER JOIN SINGLE_CASE_ALERT sca
                        ON vsca.ARCHIVED_SCA_ID = sca.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;

      INSERT into SINGLE_ALERT_SUSP_PROD (SINGLE_ALERT_ID, SCA_PRODUCT_NAME, SUSPECT_PRODUCT_LIST_IDX)
      SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_PRODUCT_NAME, vsca.SUSPECT_PRODUCT_LIST_IDX
      FROM ARCHIVED_SCA_SUSP_PROD vsca
             INNER JOIN SINGLE_CASE_ALERT sca
                        ON vsca.ARCHIVED_SCA_ID = sca.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;

      INSERT into SINGLE_ALERT_MED_ERR_PT_LIST (SINGLE_ALERT_ID, SCA_MED_ERROR, MED_ERROR_PT_LIST_IDX)
      SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_MED_ERROR, vsca.MED_ERROR_PT_LIST_IDX
      FROM ARCHIVED_SCA_MED_ERR_PT_LIST vsca
             INNER JOIN SINGLE_CASE_ALERT sca
                        ON vsca.ARCHIVED_SCA_ID = sca.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;

      INSERT into SINGLE_ALERT_ACTIONS (SINGLE_CASE_ALERT_ID, ACTION_ID)
      SELECT vsca.ARCHIVED_SCA_ID, vsca.ACTION_ID
      FROM ARCHIVED_SCA_ACTIONS vsca
             INNER JOIN SINGLE_CASE_ALERT sca
                        ON vsca.ARCHIVED_SCA_ID = sca.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;
      DBMS_OUTPUT.PUT_LINE('Archiving finished for pre_exe_id : ' || pre_exe_id);
      DBMS_OUTPUT.PUT_LINE('Deleting previous archived');
      DELETE
      FROM VALIDATED_ARCHIVED_SCA
      WHERE ARCHIVED_SCA_ID in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM VALIDATED_SINGLE_ALERTS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id
      );

      DELETE
      FROM ARCHIVED_SCA_PT
      WHERE ARCHIVED_SCA_ID in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_PT vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id
      );

      DELETE
      FROM ARCHIVED_SCA_CON_COMIT
      WHERE ARCHIVED_SCA_ID in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_CON_COMIT vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id
      );

      DELETE
      FROM ARCHIVED_SCA_SUSP_PROD
      WHERE ARCHIVED_SCA_ID in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_SUSP_PROD vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id
      );

      DELETE
      FROM ARCHIVED_SCA_MED_ERR_PT_LIST
      WHERE ARCHIVED_SCA_ID in (
        SELECT vsca.SINGLE_ALERT_ID
        FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id
      );


      DELETE
      FROM ARCHIVED_SCA_ACTIONS
      WHERE ARCHIVED_SCA_ID in (
        SELECT vsca.SINGLE_CASE_ALERT_ID
        FROM SINGLE_ALERT_ACTIONS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id);


      DELETE
      FROM AR_SIN_ALERT_INDICATION_LIST
      WHERE ARCHIVED_SCA_ID in (
        SELECT vsca.SINGLE_CASE_ALERT_ID
        FROM SINGLE_ALERT_ACTIONS vsca
               INNER JOIN SINGLE_CASE_ALERT sca
                          ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id
      );

      DELETE
      FROM AR_SIN_ALERT_INDICATION_LIST
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_INDICATION_LIST sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_CAUSE_OF_DEATH
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_INDICATION_LIST sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_PAT_MED_HIST
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_PAT_MED_HIST sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_PAT_HIST_DRUGS
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_PAT_HIST_DRUGS sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_BATCH_LOT_NO
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_BATCH_LOT_NO sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_CASE_CLASSIFI
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_CASE_CLASSIFI sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_THERAPY_DATES
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_THERAPY_DATES sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_DOSE_DETAILS
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_DOSE_DETAILS sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM AR_SIN_ALERT_PRIM_SUSP
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_PRIM_SUSP sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id);

      DELETE
      FROM AR_SIN_ALERT_PRIM_PAI
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_PRIM_PAI sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id);

      DELETE
      FROM AR_SIN_ALERT_ALL_PAI
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_ALL_PAI sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id);

      DELETE
      FROM AR_SIN_ALERT_ALL_PT
      WHERE ARCHIVED_SCA_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_ALL_PT sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id);

      DELETE
      FROM AR_SINGLE_ALERT_GENERIC_NAME
      WHERE SINGLE_ALERT_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_GENERIC_NAME sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id);

      DELETE
      FROM AR_SINGLE_ALERT_ALLPT_OUT_COME
      WHERE SINGLE_ALERT_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_ALLPT_OUT_COME sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE
      FROM ARCHIVED_SINGLE_ALERT_CROSS_REFERENCE_IND
      WHERE SINGLE_ALERT_ID in (
        select sail.SINGLE_ALERT_ID
        from SINGLE_ALERT_CROSS_REFERENCE_IND sail
               inner join SINGLE_CASE_ALERT sca on sail.SINGLE_ALERT_ID = sca.ID
        where sca.EXEC_CONFIG_ID = pre_exe_id
          and sca.ALERT_CONFIGURATION_ID = alert_config_id
      );

      DELETE from ARCHIVED_SCA_TAGS where SINGLE_ALERT_ID in(
        select ID
        FROM archived_single_case_alert
        WHERE ALERT_CONFIGURATION_ID = alert_config_id
          and EXEC_CONFIG_ID = pre_exe_id
      );


      update EX_RCONFIG set IS_LATEST=1 where ID = pre_exe_id;

      DELETE EX_STATUS where EXECUTED_CONFIG_ID = current_exe_id and type = alert_type;

      DELETE FROM EX_RCONFIG where ID = current_exe_id;

      DBMS_OUTPUT.PUT_LINE('Previous archived data deleted');


      MERGE INTO attachment_link al
      USING (SELECT t1.reference_id as reference_id
             FROM attachment_link t1
                    left join ARCHIVED_SINGLE_CASE_ALERT t2 on t1.reference_id = t2.id
             WHERE ALERT_CONFIGURATION_ID = alert_config_id
               and EXEC_CONFIG_ID = pre_exe_id) conf
      ON (al.reference_id = conf.reference_id)
      WHEN matched THEN
        UPDATE SET al.reference_class='com.rxlogix.signal.SingleCaseAlert';

      DELETE
      FROM archived_single_case_alert
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIG_ID = pre_exe_id;
    end if;
    if (alert_type = 'Aggregate Case Alert') then


      DELETE
      FROM product_event_history
      WHERE CONFIG_ID = alert_config_id and EXEC_CONFIG_ID = current_exe_id;

      DELETE
      FROM VALIDATED_AGG_ALERTS
      WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM VALIDATED_AGG_ALERTS vaca
               INNER JOIN AGG_ALERT aca
                          ON vaca.AGG_ALERT_ID = aca.ID
        WHERE aca.ALERT_CONFIGURATION_ID = alert_config_id
          and aca.EXEC_CONFIGURATION_ID = current_exe_id
      );

      DELETE
      FROM AGG_ALERT_ACTIONS
      WHERE (AGG_ALERT_ID) in (
        SELECT vaca.AGG_ALERT_ID
        FROM AGG_ALERT_ACTIONS vaca
               INNER JOIN AGG_ALERT aca
                          ON vaca.AGG_ALERT_ID = aca.ID
        WHERE aca.ALERT_CONFIGURATION_ID = alert_config_id
          and aca.EXEC_CONFIGURATION_ID = current_exe_id
      );


      DELETE FROM ACTIONS WHERE EXEC_CONFIG_ID = current_exe_id AND ALERT_TYPE = alert_type;
      DELETE FROM ALERT_COMMENT where EX_CONFIG_ID = current_exe_id and ALERT_TYPE = alert_type;
      DELETE FROM ALERT_COMMENT_HISTORY where EXEC_CONFIG_ID = current_exe_id;
      DELETE FROM EX_TEMPLT_QRS_EX_QUERY_VALUES WHERE EX_TEMPLT_QUERY_ID in (select id from EX_TEMPLT_QUERY  WHERE EX_RCONFIG_ID = current_exe_id);
      DELETE FROM EX_TEMPLT_QUERY WHERE EX_RCONFIG_ID = current_exe_id;
      DELETE FROM ex_rconfig_activities where EX_CONFIG_ACTIVITIES_ID = current_exe_id;
      DELETE FROM EX_RCONFIGS_PROD_GRP where EXCONFIG_ID = current_exe_id;
      DELETE FROM EX_ALERT_QUERY_VALUES where EX_ALERT_QUERY_ID = current_exe_id;
      DELETE FROM EX_FG_ALERT_QUERY_VALUES where EX_FG_QUERY_VALUE_ID = current_exe_id;
      DELETE FROM CASE_HISTORY where EXEC_CONFIG_ID = current_exe_id;
      DELETE FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = alert_config_id and EXEC_CONFIGURATION_ID = current_exe_id;


      DELETE
      FROM AGG_GLOBAL_TAGS
      WHERE PVS_GLOBAL_TAG_ID in (select agt.PVS_GLOBAL_TAG_ID
                                  from AGG_GLOBAL_TAGS agt
                                         inner join PVS_GLOBAL_TAG pgt on agt.PVS_GLOBAL_TAG_ID = pgt.ID and
                                                                          pgt.EXEC_CONFIG_ID = current_exe_id and
                                                                          pgt.DOMAIN = 'Aggregate Case Alert'
                                    and agt.CREATION_DATE >
                                        (select DATE_CREATED from EX_RCONFIG where ID = current_exe_id)
      );

      DELETE
      FROM AGG_CASE_ALERT_TAGS
      WHERE PVS_ALERT_TAG_ID in (
        select ID
        from PVS_ALERT_TAG
        WHERE exec_config_id = current_exe_id
      );

      DELETE
      FROM PVS_GLOBAL_TAG
      where EXEC_CONFIG_ID = current_exe_id
        and DOMAIN = 'Aggregate Case Alert'
        and CREATED_AT >
            (select DATE_CREATED from EX_RCONFIG where ID = current_exe_id);

      DELETE
      FROM ARCHIVED_AGG_CASE_ALERT_TAGS
      WHERE AGG_ALERT_ID in (select ALERT_ID
                             FROM PVS_ALERT_TAG pat
                             WHERE pat.EXEC_CONFIG_ID = current_exe_id
      );
      DELETE
      FROM PVS_ALERT_TAG
      where EXEC_CONFIG_ID = current_exe_id
        AND DOMAIN = 'Aggregate Case Alert';

      DELETE FROM SINGLE_CASE_ALERT where AGG_EXECUTION_ID = current_exe_id and IS_CASE_SERIES = 1;
      ---- Deletion done -------
      SELECT listagg(column_name, ',') within group (order by column_id) as cols INTO lvc_sql
      FROM user_tab_columns
      WHERE table_name = 'AGG_ALERT';

      lvc_exec_sql := 'INSERT into AGG_ALERT (' || lvc_sql || ') SELECT ' || lvc_sql ||
                      ' FROM ARCHIVED_AGG_ALERT WHERE ALERT_CONFIGURATION_ID = ' || alert_config_id ||
                      ' and EXEC_CONFIGURATION_ID =' || pre_exe_id;
      execute immediate lvc_exec_sql;

      UPDATE product_event_history
      SET AGG_CASE_ALERT_ID = archived_agg_case_alert_id, archived_agg_case_alert_id = null
      WHERE CONFIG_ID = alert_config_id and EXEC_CONFIG_ID = pre_exe_id;

      INSERT into VALIDATED_AGG_ALERTS (AGG_ALERT_ID, VALIDATED_SIGNAL_ID)
      SELECT vaca.ARCHIVED_ACA_ID, vaca.VALIDATED_SIGNAL_ID
      FROM VALIDATED_ARCHIVED_ACA vaca
             INNER JOIN AGG_ALERT aca
                        ON vaca.ARCHIVED_ACA_ID = aca.ID
      WHERE aca.ALERT_CONFIGURATION_ID = alert_config_id
        and aca.EXEC_CONFIGURATION_ID = pre_exe_id;

      INSERT into AGG_CASE_ALERT_TAGS (AGG_ALERT_ID, PVS_ALERT_TAG_ID)
      SELECT vaca.AGG_ALERT_ID, vaca.PVS_ALERT_TAG_ID
      FROM ARCHIVED_AGG_CASE_ALERT_TAGS vaca
             INNER JOIN ARCHIVED_AGG_ALERT aca
                        ON vaca.AGG_ALERT_ID = aca.ID
      WHERE aca.ALERT_CONFIGURATION_ID = alert_config_id
        and aca.EXEC_CONFIGURATION_ID = pre_exe_id;

      INSERT into AGG_ALERT_ACTIONS (AGG_ALERT_ID, ACTION_ID)
      SELECT vaca.ARCHIVED_ACA_ID, vaca.ACTION_ID
      FROM ARCHIVED_ACA_ACTIONS vaca
             INNER JOIN AGG_ALERT aca
                        ON vaca.ARCHIVED_ACA_ID = aca.ID
      WHERE aca.ALERT_CONFIGURATION_ID = alert_config_id
        and aca.EXEC_CONFIGURATION_ID = pre_exe_id;

      DELETE
      FROM ARCHIVED_AGG_CASE_ALERT_TAGS
      WHERE AGG_ALERT_ID in (select ALERT_ID
                             FROM PVS_ALERT_TAG pat
                             WHERE pat.EXEC_CONFIG_ID = pre_exe_id
      );

      update EX_RCONFIG set IS_LATEST=1 where ID = pre_exe_id;
      DELETE EX_STATUS where EXECUTED_CONFIG_ID = current_exe_id and type = alert_type;
      DELETE FROM EX_RCONFIG where ID = current_exe_id;

      DELETE
      FROM VALIDATED_ARCHIVED_ACA
      WHERE ARCHIVED_ACA_ID in (select aa.ID
                                from AGG_ALERT aa
                                       left JOIN EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
                                where er.ID = pre_exe_id);

      DELETE
      FROM ARCHIVED_ACA_ACTIONS
      WHERE ARCHIVED_ACA_ID in (select aa.ID
                                from AGG_ALERT aa
                                       left JOIN EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
                                where er.ID = pre_exe_id);
      DELETE
      FROM ARCHIVED_IMP_EVENT_LIST
      WHERE AGG_ALERT_ID in (select aa.ID
                             from AGG_ALERT aa
                                    left JOIN EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
                             where er.ID = pre_exe_id);
      DELETE
      FROM AR_ALERT_COMMENT_HISTORY_MAP
      WHERE AGG_ALERT_ID in (select aa.ID
                             from AGG_ALERT aa
                                    left JOIN EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
                             where er.ID = pre_exe_id);



      --      Move the attachments to Archived Aggregate Case Alert

      MERGE INTO attachment_link al
      USING (SELECT t1.reference_id as reference_id
             FROM attachment_link t1
                    left join ARCHIVED_AGG_ALERT t2 on t1.reference_id = t2.id
             WHERE ALERT_CONFIGURATION_ID = alert_config_id
               and EXEC_CONFIGURATION_ID = pre_exe_id) conf
      ON (al.reference_id = conf.reference_id)
      WHEN matched THEN
        UPDATE SET al.reference_class='com.rxlogix.signal.AggregateCaseAlert';



      DELETE
      FROM ARCHIVED_AGG_ALERT
      WHERE ALERT_CONFIGURATION_ID = alert_config_id and EXEC_CONFIGURATION_ID = pre_exe_id;

    end if;

  END;


  if (alert_type = 'Literature Search Alert') then
    BEGIN
      -- block for LITERATURE
      DBMS_OUTPUT.PUT_LINE('Moving the data of LITERATURE ALERT to Archive Table');

      DELETE
      FROM LITERATURE_HISTORY
      WHERE LIT_CONFIG_ID = alert_config_id and LIT_EXEC_CONFIG_ID = current_exe_id;

      DELETE
      FROM VALIDATED_LITERATURE_ALERTS
      WHERE (LITERATURE_ALERT_ID) in
            (SELECT ala.LITERATURE_ALERT_ID
             FROM VALIDATED_LITERATURE_ALERTS ala
                    INNER JOIN LITERATURE_ALERT la
                               ON ala.LITERATURE_ALERT_ID = la.ID
             WHERE lit_search_config_id = alert_config_id
               and ex_lit_search_config_id = current_exe_id
            );

      DELETE
      FROM LITERATURE_ALERT_TAGS
      WHERE (LITERATURE_ALERT_ID) in
            (SELECT ala.LITERATURE_ALERT_ID
             FROM LITERATURE_ALERT_TAGS ala
                    INNER JOIN LITERATURE_ALERT la
                               ON ala.LITERATURE_ALERT_ID = la.ID
             WHERE lit_search_config_id = alert_config_id
               and ex_lit_search_config_id = current_exe_id
            );


      DELETE
      FROM LIT_ALERT_ACTIONS
      WHERE (LITERATURE_ALERT_ID) in
            (SELECT ala.LITERATURE_ALERT_ID
             FROM LIT_ALERT_ACTIONS ala
                    INNER JOIN LITERATURE_ALERT la
                               ON ala.LITERATURE_ALERT_ID = la.ID
             WHERE lit_search_config_id = alert_config_id
               and ex_lit_search_config_id = current_exe_id
            );

      DELETE FROM ALERT_COMMENT where EX_CONFIG_ID = current_exe_id and ALERT_TYPE = alert_type;

      DELETE
      FROM LITERATURE_CASE_ALERT_TAGS
      where (PVS_ALERT_TAG_ID) in(
        select ID
        from PVS_ALERT_TAG
        WHERE exec_config_id in(current_exe_id,pre_exe_id)
      );

      DELETE
      FROM PVS_ALERT_TAG
      where ALERT_ID in (
        select sca.ID from LITERATURE_ALERT sca where sca.EX_LIT_SEARCH_CONFIG_ID in (current_exe_id, pre_exe_id)
      )
        AND DOMAIN = 'Literature Alert';

      --Below code is commented because in 5.6 alert has not to be deleted for Literature. Please uncomment in case Literature is deleted from mart in future release.
      --       DELETE
      --       FROM LITERAURE_GLOBAL_TAGS
      --       WHERE PVS_GLOBAL_TAG_ID in (select agt.PVS_GLOBAL_TAG_ID
      --                                   from LITERAURE_GLOBAL_TAGS agt
      --                                          inner join PVS_GLOBAL_TAG pgt on agt.PVS_GLOBAL_TAG_ID = pgt.ID and
      --                                                                           pgt.EXEC_CONFIG_ID = current_exe_id and
      --                                                                           pgt.DOMAIN = 'Literature Alert'
      --                                     and agt.CREATION_DATE >
      --                                         (select DATE_CREATED from EX_LITERATURE_CONFIG where ID = current_exe_id)
      --       );
      --
      --       DELETE
      --       FROM PVS_GLOBAL_TAG
      --       where EXEC_CONFIG_ID = current_exe_id
      --         and DOMAIN = 'Literature Alert'
      --         and CREATED_AT >
      --             (select DATE_CREATED from EX_LITERATURE_CONFIG where ID = current_exe_id);
      DELETE  FROM ACTIONS WHERE EXEC_CONFIG_ID=current_exe_id AND ALERT_TYPE=alert_type;

      DELETE
      FROM LITERATURE_ALERT
      WHERE lit_search_config_id = alert_config_id and ex_lit_search_config_id = current_exe_id;
      DELETE FROM literature_activity where EXECUTED_CONFIGURATION_ID = current_exe_id;


      SELECT listagg(column_name, ',') within group (order by column_id) as cols INTO lvc_sql
      FROM user_tab_columns
      WHERE table_name = 'LITERATURE_ALERT';


      lvc_exec_sql := 'INSERT into LITERATURE_ALERT (' || lvc_sql || ') SELECT ' || lvc_sql ||
                      ' FROM ARCHIVED_LITERATURE_ALERT WHERE lit_search_config_id = ' || alert_config_id ||
                      ' and ex_lit_search_config_id =' || pre_exe_id;
      execute immediate lvc_exec_sql;


      INSERT into VALIDATED_LITERATURE_ALERTS(LITERATURE_ALERT_ID, VALIDATED_SIGNAL_ID)
      SELECT ala.ARCHIVED_LIT_ALERT_ID, ala.VALIDATED_SIGNAL_ID
      FROM VALIDATED_ARCHIVED_LIT_ALERTS ala
             INNER JOIN LITERATURE_ALERT la
                        ON ala.ARCHIVED_LIT_ALERT_ID = la.ID
      WHERE lit_search_config_id = alert_config_id
        and ex_lit_search_config_id = pre_exe_id;

      INSERT into LITERATURE_CASE_ALERT_TAGS(LITERATURE_ALERT_ID, PVS_ALERT_TAG_ID)
      SELECT ala.ARCHIVED_LIT_ALERT_ID, ala.PVS_ALERT_TAG_ID
      FROM ARCHIVED_LIT_CASE_ALERT_TAGS ala
             INNER JOIN ARCHIVED_LITERATURE_ALERT la
                        ON ala.ARCHIVED_LIT_ALERT_ID = la.ID
      WHERE lit_search_config_id = alert_config_id
        and ex_lit_search_config_id = pre_exe_id;

      INSERT into LIT_ALERT_ACTIONS(LITERATURE_ALERT_ID, ACTION_ID)
      SELECT ala.ARCHIVED_LIT_ALERT_ID, ala.ACTION_ID
      FROM ARCHIVED_LIT_ALERT_ACTIONS ala
             INNER JOIN LITERATURE_ALERT la
                        ON ala.ARCHIVED_LIT_ALERT_ID = la.ID
      WHERE lit_search_config_id = alert_config_id
        and ex_lit_search_config_id = pre_exe_id;

      DELETE
      FROM PVS_ALERT_TAG
      where ALERT_ID in (
        select sca.ID from LITERATURE_ALERT sca where sca.EX_LIT_SEARCH_CONFIG_ID in (current_exe_id, pre_exe_id)
      )
        AND DOMAIN = 'Literature Alert';

      DELETE FROM ACTIONS WHERE EXEC_CONFIG_ID = current_exe_id AND ALERT_TYPE = alert_type;
      update EX_LITERATURE_CONFIG set IS_LATEST=1 WHERE config_id = alert_config_id and ID = pre_exe_id;
      DELETE EX_STATUS where EXECUTED_CONFIG_ID = current_exe_id and type = alert_type;
      DELETE FROM EX_LITERATURE_CONFIG WHERE config_id = alert_config_id and ID = current_exe_id;


      DELETE
      FROM VALIDATED_ARCHIVED_LIT_ALERTS
      where ARCHIVED_LIT_ALERT_ID in (select la.ID
                                      from LITERATURE_ALERT la
                                             left join LITERATURE_CONFIG lc
                                                       on la.EX_LIT_SEARCH_CONFIG_ID = lc.ID
                                      where lc.id = pre_exe_id);

      DELETE
      FROM ARCHIVED_LIT_ALERT_ACTIONS
      where ARCHIVED_LIT_ALERT_ID in (select la.ID
                                      from LITERATURE_ALERT la
                                             left join LITERATURE_CONFIG lc
                                                       on la.EX_LIT_SEARCH_CONFIG_ID = lc.ID
                                      where lc.id = pre_exe_id);


      DELETE
      FROM ARCHIVED_LIT_CASE_ALERT_TAGS
      WHERE ARCHIVED_LIT_ALERT_ID in (select ID from literature_alert WHERE ex_lit_search_config_id = pre_exe_id);

      --      Move the attachments to Archived Literature Alert
      MERGE INTO attachment_link al
      USING (SELECT t1.reference_id as reference_id
             FROM attachment_link t1
                    left join ARCHIVED_LITERATURE_ALERT t2 on t1.reference_id = t2.id
             WHERE lit_search_config_id = alert_config_id
               and ex_lit_search_config_id = pre_exe_id) conf
      ON (al.reference_id = conf.reference_id)
      WHEN matched THEN
        UPDATE SET al.reference_class='com.rxlogix.config.LiteratureAlert';
      DELETE
      FROM ARCHIVED_LITERATURE_ALERT
      WHERE lit_search_config_id = alert_config_id and ex_lit_search_config_id = pre_exe_id;



    END; -- block for LITERATURE
  end if;

  if (alert_type = 'EVDAS Alert') then
    BEGIN
      -- block for LITERATURE
      DBMS_OUTPUT.PUT_LINE('Moving the data of EVDAS ALERT to Archive Table');

      ------delete from current alert ---------
      DELETE
      FROM evdas_history t2
      WHERE t2.id in (SELECT t1.id
                      FROM evdas_history t1
                               left join EVDAS_ALERT t2 on t1.EVDAS_ALERT_ID = t2.id
                      WHERE ALERT_CONFIGURATION_ID = alert_config_id
                        and EXEC_CONFIGURATION_ID = current_exe_id);

      DELETE
      FROM VALIDATED_EVDAS_ALERTS
      WHERE (EVDAS_ALERT_ID) in
            (SELECT veva.EVDAS_ALERT_ID
             FROM VALIDATED_EVDAS_ALERTS veva
                    INNER JOIN EVDAS_ALERT eva
                               ON veva.EVDAS_ALERT_ID = eva.ID
             WHERE ALERT_CONFIGURATION_ID = alert_config_id
               and EXEC_CONFIGURATION_ID = current_exe_id);

      DELETE
      FROM EVDAS_ALERT_ACTIONS
      WHERE (EVDAS_ALERT_ID) in
            (SELECT veva.EVDAS_ALERT_ID
             FROM EVDAS_ALERT_ACTIONS veva
                    INNER JOIN EVDAS_ALERT eva
                               ON veva.EVDAS_ALERT_ID = eva.ID
             WHERE ALERT_CONFIGURATION_ID = alert_config_id
               and EXEC_CONFIGURATION_ID = current_exe_id);
      DELETE  FROM ACTIONS WHERE EXEC_CONFIG_ID=current_exe_id AND ALERT_TYPE=alert_type;
      DELETE FROM ALERT_COMMENT where EX_CONFIG_ID = current_exe_id and ALERT_TYPE = alert_type;
      DELETE FROM EX_EVDAS_CONFIG_ACTIVITIES WHERE EX_EVDAS_CONFIG_ID = current_exe_id;
      DELETE FROM EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = alert_config_id and EXEC_CONFIGURATION_ID = current_exe_id;


      SELECT listagg(column_name, ',') within group (order by column_id) as cols INTO lvc_sql
      FROM user_tab_columns
      WHERE table_name = 'EVDAS_ALERT';


      ----------current alert deletion done -----------------
      ------ insert from archived alert ------------
      lvc_exec_sql := 'INSERT into EVDAS_ALERT (' || lvc_sql || ') SELECT ' || lvc_sql ||
                      ' FROM ARCHIVED_EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = ' || alert_config_id ||
                      ' and EXEC_CONFIGURATION_ID =' || pre_exe_id;
      execute immediate lvc_exec_sql;

      INSERT into VALIDATED_EVDAS_ALERTS(EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID)
      SELECT veva.ARCHIVED_EVDAS_ALERT_ID, veva.VALIDATED_SIGNAL_ID
      FROM VALIDATED_ARCH_EVDAS_ALERTS veva
             INNER JOIN EVDAS_ALERT eva
                        ON veva.ARCHIVED_EVDAS_ALERT_ID = eva.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIGURATION_ID = pre_exe_id;

      INSERT into EVDAS_ALERT_ACTIONS(EVDAS_ALERT_ID, ACTION_ID)
      SELECT veva.ARCHIVED_EVDAS_ALERT_ID, veva.ACTION_ID
      FROM ARCHIVED_EVDAS_ALERT_ACTIONS veva
             INNER JOIN EVDAS_ALERT eva
                        ON veva.ARCHIVED_EVDAS_ALERT_ID = eva.ID
      WHERE ALERT_CONFIGURATION_ID = alert_config_id
        and EXEC_CONFIGURATION_ID = pre_exe_id;


      ---------------------------------------------------------------------------
      ---------------delete from archived alert-------------------
      DELETE
      FROM VALIDATED_ARCH_EVDAS_ALERTS
      WHERE (ARCHIVED_EVDAS_ALERT_ID) in
            (SELECT veva.ARCHIVED_EVDAS_ALERT_ID
             FROM VALIDATED_ARCH_EVDAS_ALERTS veva
                    INNER JOIN ARCHIVED_EVDAS_ALERT eva
                               ON veva.ARCHIVED_EVDAS_ALERT_ID = eva.ID
             WHERE ALERT_CONFIGURATION_ID = alert_config_id
               and EXEC_CONFIGURATION_ID = pre_exe_id);

      DELETE
      FROM ARCHIVED_EVDAS_ALERT_ACTIONS
      WHERE (ARCHIVED_EVDAS_ALERT_ID) in
            (SELECT veva.ARCHIVED_EVDAS_ALERT_ID
             FROM ARCHIVED_EVDAS_ALERT_ACTIONS veva
                    INNER JOIN ARCHIVED_EVDAS_ALERT eva
                               ON veva.ARCHIVED_EVDAS_ALERT_ID = eva.ID
             WHERE ALERT_CONFIGURATION_ID = alert_config_id
               and EXEC_CONFIGURATION_ID = pre_exe_id);

      DELETE FROM ACTIONS WHERE EXEC_CONFIG_ID = current_exe_id AND ALERT_TYPE = alert_type;
      update EX_EVDAS_CONFIG set IS_LATEST=1 WHERE CONFIG_ID = alert_config_id and ID = pre_exe_id;
      DELETE EX_STATUS where EXECUTED_CONFIG_ID = current_exe_id and type = alert_type;
      DELETE FROM EX_EVDAS_CONFIG WHERE CONFIG_ID = alert_config_id and ID = current_exe_id;


      DELETE
      FROM VALIDATED_ARCH_EVDAS_ALERTS vae
      WHERE vae.ARCHIVED_EVDAS_ALERT_ID in (
        SELECT ea.ID
        from EVDAS_ALERT ea
               left join EX_EVDAS_CONFIG ee on ea.ID = ee.ID
        where ee.id = pre_exe_id
      );

      DELETE
      FROM ARCHIVED_EVDAS_ALERT_ACTIONS vae
      WHERE vae.ARCHIVED_EVDAS_ALERT_ID in (
        SELECT ea.ID
        from EVDAS_ALERT ea
               left join EX_EVDAS_CONFIG ee on ea.ID = ee.ID
        where ee.id = pre_exe_id
      );

      DELETE
      FROM ARCHIVED_EA_IMP_EVENT_LIST vae
      WHERE vae.ARCHIVED_EVDAS_ALERT_ID in (
        SELECT ea.ID
        from EVDAS_ALERT ea
               left join EX_EVDAS_CONFIG ee on ea.ID = ee.ID
        where ee.id = pre_exe_id
      );



      --      Move the attachments to Archived Evdas Alert
      MERGE INTO attachment_link al
      USING (SELECT t1.reference_id as reference_id
             FROM attachment_link t1
                    left join ARCHIVED_EVDAS_ALERT t2 on t1.reference_id = t2.id
             WHERE ALERT_CONFIGURATION_ID = alert_config_id
               and EXEC_CONFIGURATION_ID = pre_exe_id) conf
      ON (al.reference_id = conf.reference_id)
      WHEN matched THEN
        UPDATE SET al.reference_class='com.rxlogix.config.EvdasAlert';

      MERGE INTO evdas_history eh
      USING (SELECT t1.id as history_id, t1.EVDAS_ALERT_ID as alert_id
             FROM evdas_history t1
                    left join ARCHIVED_EVDAS_ALERT t2 on t1.EVDAS_ALERT_ID = t2.id
             WHERE ALERT_CONFIGURATION_ID = config_id
               and EXEC_CONFIGURATION_ID = pre_exe_id) conf
      ON (eh.id = conf.history_id)
      WHEN matched THEN
        UPDATE SET eh.EVDAS_ALERT_ID=config_id, eh.ARCHIVED_EVDAS_ALERT_ID = null;

      DELETE
      FROM ARCHIVED_EVDAS_ALERT
      WHERE ALERT_CONFIGURATION_ID = alert_config_id and EXEC_CONFIGURATION_ID = pre_exe_id;


    END; -- block for LITERATURE

  end if;
  COMMIT;
EXCEPTION
  WHEN others THEN
    v_code := SQLCODE;
    dbms_output.put_line(
          'Error occoured while persisting the archived data. ! Error Code : ' || v_code || ' : ' || SQLERRM);
    rollback;

END MOVE_ALERT_FROM_ARCHIVE;
