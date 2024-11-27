create or replace PROCEDURE MOVE_MASTER_ALERT_FROM_ARCHIVE(current_master_id IN NUMBER, previous_master_id IN NUMBER) AS
  type typename_type is table of VARCHAR2(32767);
  typename typename_type;
  v_code NUMBER;
  lvc_sql VARCHAR2(32000);
  lvc_exec_sql VARCHAR2(32000);
BEGIN
  BEGIN

    DELETE
    FROM product_event_history
    WHERE (EXEC_CONFIG_ID) in (select er.ID FROM EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id);

    UPDATE product_event_history
    SET AGG_CASE_ALERT_ID= archived_agg_case_alert_id,
        archived_agg_case_alert_id = null
    WHERE (EXEC_CONFIG_ID) in (select er.ID FROM EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id);



    DELETE
    FROM VALIDATED_AGG_ALERTS
    WHERE (AGG_ALERT_ID) in (
      SELECT vaca.AGG_ALERT_ID
      FROM VALIDATED_AGG_ALERTS vaca
             left join AGG_ALERT aa ON vaca.AGG_ALERT_ID = aa.ID
             left join EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
      where er.MASTER_EX_CONFIG_ID = current_master_id
    );

    DELETE
    FROM AGG_ALERT_TAGS
    WHERE (AGG_ALERT_ID) in (
      SELECT vaca.AGG_ALERT_ID
      FROM AGG_ALERT_TAGS vaca
             left join AGG_ALERT aa ON vaca.AGG_ALERT_ID = aa.ID
             left join EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
      where er.MASTER_EX_CONFIG_ID = current_master_id
    );

    DELETE
    FROM AGG_GLOBAL_TAGS
    WHERE PVS_GLOBAL_TAG_ID in (select agt.PVS_GLOBAL_TAG_ID
                                from AGG_GLOBAL_TAGS agt
                                       inner join PVS_GLOBAL_TAG pgt on agt.PVS_GLOBAL_TAG_ID = pgt.ID and
                                                                        pgt.EXEC_CONFIG_ID in (select er.ID
                                                                                               from EX_RCONFIG er
                                                                                               where er.MASTER_EX_CONFIG_ID = current_master_id
                                                                        ) and
                                                                        pgt.DOMAIN = 'Aggregate Case Alert'
                                  and agt.CREATION_DATE >
                                      (select DATE_CREATED
                                       from EX_RCONFIG er1
                                       where er1.MASTER_EX_CONFIG_ID = current_master_id));

    DELETE
    FROM AGG_CASE_ALERT_TAGS
    WHERE PVS_ALERT_TAG_ID in (
      select ID
      from PVS_ALERT_TAG
      WHERE exec_config_id in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id
      )
    );


    DELETE
    FROM PVS_GLOBAL_TAG
    where EXEC_CONFIG_ID in (select er.ID
                             from EX_RCONFIG er
                             where er.MASTER_EX_CONFIG_ID = current_master_id
    )
      and DOMAIN = 'Aggregate Case Alert'
      and CREATED_AT >
          (select er1.DATE_CREATED from EX_RCONFIG er1 where er1.MASTER_EX_CONFIG_ID = current_master_id);

    DELETE
    FROM ARCHIVED_AGG_CASE_ALERT_TAGS
    WHERE AGG_ALERT_ID in (select ALERT_ID
                           FROM PVS_ALERT_TAG pat
                           WHERE pat.EXEC_CONFIG_ID in
                                 (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id)
    );

    DELETE
    FROM PVS_ALERT_TAG
    where ALERT_ID in (
      select sca.ID
      from AGG_ALERT sca
      where sca.EXEC_CONFIGURATION_ID in
            (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id)
    )
      AND DOMAIN = 'Aggregate Case Alert';
    DELETE
    FROM AGG_ALERT_ACTIONS
    WHERE (AGG_ALERT_ID) in (
      SELECT vaca.AGG_ALERT_ID
      FROM AGG_ALERT_ACTIONS vaca
             left join AGG_ALERT aa ON vaca.AGG_ALERT_ID = aa.ID
             left join EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
      where er.MASTER_EX_CONFIG_ID = current_master_id
    );

    DELETE
    FROM SINGLE_CASE_ALERT
    where AGG_EXECUTION_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id) and IS_CASE_SERIES = 1;


    DELETE FROM ACTIONS WHERE EXEC_CONFIG_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id) AND ALERT_TYPE='Aggregate Case Alert';
    DELETE FROM ALERT_COMMENT where EX_CONFIG_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id)  and ALERT_TYPE=alert_type;
    DELETE FROM ALERT_COMMENT_HISTORY where EXEC_CONFIG_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id);
    DELETE FROM EX_TEMPLT_QRS_EX_QUERY_VALUES WHERE EX_TEMPLT_QUERY_ID in (select id from EX_TEMPLT_QUERY  WHERE EX_RCONFIG_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id));
    DELETE FROM EX_TEMPLT_QUERY WHERE EX_RCONFIG_ID  in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id);
    DELETE
    FROM EX_RCONFIG_ACTIVITIES
    where EX_CONFIG_ACTIVITIES_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id
    );
    DELETE
    FROM CASE_HISTORY
    where EXEC_CONFIG_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id
    );

    DELETE FROM EX_RCONFIGS_PROD_GRP where EXCONFIG_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id
    );

    DELETE FROM EX_ALERT_QUERY_VALUES where EX_ALERT_QUERY_ID in(     select er.ID
                                                                      from  EX_RCONFIG er
                                                                      where er.MASTER_EX_CONFIG_ID = current_master_id
    );

    DELETE FROM EX_FG_ALERT_QUERY_VALUES where EX_FG_QUERY_VALUE_ID in(     select er.ID
                                                                            from  EX_RCONFIG er
                                                                            where er.MASTER_EX_CONFIG_ID = current_master_id);
    DELETE  FROM ACTIONS WHERE EXEC_CONFIG_ID in (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = current_master_id
    ) AND ALERT_TYPE='Aggregate Case Alert';


    DELETE
    FROM AGG_ALERT
    WHERE (ID) in (select aa.ID
                   from AGG_ALERT aa
                          left join EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
                   where er.MASTER_EX_CONFIG_ID = current_master_id
    );



    ---- Deletion done -------
    SELECT listagg(column_name, ',') within group (order by column_id) as cols INTO lvc_sql
    FROM user_tab_columns
    WHERE table_name = 'AGG_ALERT';

    lvc_exec_sql := 'INSERT into AGG_ALERT (' || lvc_sql || ') SELECT ' || lvc_sql ||
                    ' FROM ARCHIVED_AGG_ALERT WHERE EXEC_CONFIGURATION_ID IN (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID =' ||previous_master_id || ')';
    execute immediate lvc_exec_sql;


    INSERT into VALIDATED_AGG_ALERTS (AGG_ALERT_ID, VALIDATED_SIGNAL_ID)
    SELECT vaca.ARCHIVED_ACA_ID, vaca.VALIDATED_SIGNAL_ID
    FROM VALIDATED_ARCHIVED_ACA vaca
           left join AGG_ALERT aa ON vaca.ARCHIVED_ACA_ID = aa.ID
           left join EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
    where er.MASTER_EX_CONFIG_ID = previous_master_id;


    INSERT into AGG_CASE_ALERT_TAGS (AGG_ALERT_ID, PVS_ALERT_TAG_ID)
    SELECT vaca.AGG_ALERT_ID, vaca.PVS_ALERT_TAG_ID
    FROM ARCHIVED_AGG_CASE_ALERT_TAGS vaca
           left join AGG_ALERT aa ON vaca.AGG_ALERT_ID = aa.ID
           left join EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
    where er.MASTER_EX_CONFIG_ID = previous_master_id;

    INSERT into AGG_ALERT_ACTIONS (AGG_ALERT_ID, ACTION_ID)
    SELECT vaca.ARCHIVED_ACA_ID, vaca.ACTION_ID
    FROM ARCHIVED_ACA_ACTIONS vaca
           left join AGG_ALERT aa ON vaca.ARCHIVED_ACA_ID = aa.ID
           left join EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID = er.ID
    where er.MASTER_EX_CONFIG_ID = previous_master_id;


    DELETE
    FROM ARCHIVED_AGG_CASE_ALERT_TAGS
    WHERE AGG_ALERT_ID in (select ALERT_ID
                           FROM PVS_ALERT_TAG pat
                           WHERE pat.EXEC_CONFIG_ID in
                                 (select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = previous_master_id)
    );



    update EX_RCONFIG
    set IS_LATEST=1
    where MASTER_EX_CONFIG_ID = previous_master_id;


    DELETE EX_STATUS where EXECUTED_CONFIG_ID in (
      select er.ID
      from  EX_RCONFIG er
      where er.MASTER_EX_CONFIG_ID = current_master_id and er.TYPE ='Aggregate Case Alert'
    );

    DELETE
    FROM EX_RCONFIG
    where MASTER_EX_CONFIG_ID = current_master_id;


    DELETE FROM  VALIDATED_ARCHIVED_ACA WHERE ARCHIVED_ACA_ID in( select aa.ID from AGG_ALERT aa left JOIN  EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID=er.ID
                                                                  where er.MASTER_EX_CONFIG_ID = previous_master_id);
    DELETE FROM  ARCHIVED_AGG_CASE_ALERT_TAGS WHERE AGG_ALERT_ID in( select aa.ID from AGG_ALERT aa left JOIN  EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID=er.ID
                                                                     where er.MASTER_EX_CONFIG_ID = previous_master_id);
    DELETE FROM  ARCHIVED_ACA_ACTIONS WHERE ARCHIVED_ACA_ID in( select aa.ID from AGG_ALERT aa left JOIN  EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID=er.ID
                                                                where er.MASTER_EX_CONFIG_ID = previous_master_id);
    DELETE FROM  ARCHIVED_IMP_EVENT_LIST WHERE AGG_ALERT_ID in( select aa.ID from AGG_ALERT aa left JOIN  EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID=er.ID
                                                                where er.MASTER_EX_CONFIG_ID = previous_master_id);
    DELETE FROM  AR_ALERT_COMMENT_HISTORY_MAP WHERE AGG_ALERT_ID in( select aa.ID from AGG_ALERT aa left JOIN  EX_RCONFIG er on aa.EXEC_CONFIGURATION_ID=er.ID
                                                                     where er.MASTER_EX_CONFIG_ID = previous_master_id);





    --Move the attachments to Archived Aggregate Case Alert
    MERGE INTO attachment_link al
    USING (SELECT t1.reference_id as reference_id
           FROM attachment_link t1
                  left join ARCHIVED_AGG_ALERT t2 on t1.reference_id = t2.id
           WHERE EXEC_CONFIGURATION_ID in (
             select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID = previous_master_id
           )) conf
    ON (al.reference_id = conf.reference_id)
    WHEN matched THEN
      UPDATE SET al.reference_class='com.rxlogix.signal.AggregateCaseAlert';

    DELETE
    FROM ARCHIVED_AGG_ALERT
    WHERE EXEC_CONFIGURATION_ID in (
      select er.ID from EX_RCONFIG er where er.MASTER_EX_CONFIG_ID in(previous_master_id,current_master_id)
    );

  END;

  COMMIT;
EXCEPTION
  WHEN others THEN
    v_code := SQLCODE;
    dbms_output.put_line(
          'Error occoured while persisting the archived data. ! Error Code : ' || v_code || ' : ' || SQLERRM);
    rollback;

END MOVE_MASTER_ALERT_FROM_ARCHIVE;