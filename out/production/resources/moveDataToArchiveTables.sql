create or replace PROCEDURE MOVE_ALERT_TO_ARCHIVE AS
type id_list_type is table of number index by PLS_INTEGER;
type typename_type is table of VARCHAR2(32767);

exec_id_list id_list_type;
config_id id_list_type;
typename typename_type;
v_code NUMBER;
lvc_sql VARCHAR2(32000);
lvc_exec_sql VARCHAR2(32000);
BEGIN

 BEGIN  -- block for Qualitative and Qauntitative
  DBMS_OUTPUT.PUT_LINE('Moving the data of Qualitative and Quantitative to Archive Table');

   --First disable all constraint of root table
   FOR c IN (SELECT c.owner, c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name in ('SINGLE_CASE_ALERT', 'AGG_ALERT') AND c.status = 'ENABLED' AND NOT (t.iot_type IS NOT NULL AND c.constraint_type = 'P') ORDER BY c.constraint_type DESC)
    LOOP
     dbms_utility.exec_ddl_statement('alter table "' || c.owner || '"."' || c.table_name || '" disable constraint ' || c.constraint_name || ' cascade');
    END LOOP;


  for conf_rec in (SELECT id, type FROM RCONFIG WHERE is_deleted = 0 and is_enabled = 1)
  loop
   DBMS_OUTPUT.PUT_LINE('ids ' || conf_rec.id);
   -- Move Qualitative alert records first
   if(conf_rec.type = 'Single Case Alert') then

   --Now select all exec_config_id from single_case_alert
    SELECT distinct exec_config_id bulk collect into exec_id_list FROM single_case_alert sca LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id) WHERE alert_configuration_id = conf_rec.id and rc.is_latest = 0 order by exec_config_id DESC;

   --collect exec ids in string and pass this string in next command 'in' section
    if(exec_id_list.count > 0) then
     for exec_rec in 1..exec_id_list.LAST
      loop

       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM user_tab_columns
       WHERE table_name = 'ARCHIVED_SINGLE_CASE_ALERT';

       lvc_exec_sql := 'INSERT into ARCHIVED_SINGLE_CASE_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM SINGLE_CASE_ALERT WHERE ALERT_CONFIGURATION_ID = '||conf_rec.id||' and EXEC_CONFIG_ID =' ||exec_id_list(exec_rec);
      execute immediate lvc_exec_sql;

       INSERT into VALIDATED_ARCHIVED_SCA(ARCHIVED_SCA_ID,VALIDATED_SIGNAL_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.VALIDATED_SIGNAL_ID
       FROM VALIDATED_SINGLE_ALERTS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

       INSERT into ARCHIVED_SCA_TAGS(SINGLE_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.PVS_ALERT_TAG_ID
       FROM SINGLE_CASE_ALERT_TAGS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

       INSERT into ARCHIVED_SCA_PT(ARCHIVED_SCA_ID,ARCHIVED_SCA_PT,PT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PT, vsca.PT_LIST_IDX
       FROM SINGLE_ALERT_PT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

       INSERT into ARCHIVED_SCA_CON_COMIT(ARCHIVED_SCA_ID,ALERT_CON_COMIT,CON_COMIT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.ALERT_CON_COMIT, vsca.CON_COMIT_LIST_IDX
       FROM SINGLE_ALERT_CON_COMIT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

       INSERT into ARCHIVED_SCA_SUSP_PROD(ARCHIVED_SCA_ID,SCA_PRODUCT_NAME,SUSPECT_PRODUCT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_PRODUCT_NAME, vsca.SUSPECT_PRODUCT_LIST_IDX
       FROM SINGLE_ALERT_SUSP_PROD vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

       INSERT into ARCHIVED_SCA_MED_ERR_PT_LIST(ARCHIVED_SCA_ID,SCA_MED_ERROR,MED_ERROR_PT_LIST_IDX) SELECT vsca.SINGLE_ALERT_ID, vsca.SCA_MED_ERROR, vsca.MED_ERROR_PT_LIST_IDX
       FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

       INSERT into ARCHIVED_SCA_ACTIONS(ARCHIVED_SCA_ID,ACTION_ID) SELECT vsca.SINGLE_CASE_ALERT_ID, vsca.ACTION_ID
       FROM SINGLE_ALERT_ACTIONS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
             WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

       --      Move the attachments to Archived Single Case Alert

        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join SINGLE_CASE_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec) ) conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedSingleCaseAlert';

       DELETE FROM VALIDATED_SINGLE_ALERTS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM VALIDATED_SINGLE_ALERTS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec)
        );

       DELETE FROM SINGLE_ALERT_TAGS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_TAGS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec)
        );

       DELETE FROM SINGLE_ALERT_PT WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_PT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec)
        );

       DELETE FROM SINGLE_ALERT_CON_COMIT WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_CON_COMIT vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec)
        );

       DELETE FROM SINGLE_ALERT_SUSP_PROD WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_SUSP_PROD vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec)
        );

       DELETE FROM SINGLE_ALERT_MED_ERR_PT_LIST WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_ALERT_MED_ERR_PT_LIST vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec)
        );

       DELETE FROM SINGLE_ALERT_ACTIONS WHERE (SINGLE_CASE_ALERT_ID) in (
       SELECT vsca.SINGLE_CASE_ALERT_ID
       FROM SINGLE_ALERT_ACTIONS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec));

        DELETE FROM single_case_alert WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);
      end loop;
    end if;  --if count greater than 1
    exec_id_list.delete();  -- empty list for fresh entry

   end if;  -- if type = single_case_alert

    if(conf_rec.type = 'Aggregate Case Alert') then

       --Now select all exec_configuration_id from agg_alert
        SELECT distinct exec_configuration_id bulk collect into exec_id_list FROM AGG_ALERT WHERE alert_configuration_id = conf_rec.id order by exec_configuration_id DESC;

       --collect exec ids in string and pass this string in next command 'in' section
        if(exec_id_list.count > 1) then
         for exec_rec in 2..exec_id_list.LAST
          loop

           SELECT listagg(column_name,',') within group (order by column_id) as cols
           INTO lvc_sql
           FROM user_tab_columns
           WHERE table_name = 'ARCHIVED_AGG_ALERT';

           lvc_exec_sql := 'INSERT into ARCHIVED_AGG_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = '||conf_rec.id||' and EXEC_CONFIGURATION_ID =' ||exec_id_list(exec_rec);
          execute immediate lvc_exec_sql;

           INSERT into VALIDATED_ARCHIVED_ACA(ARCHIVED_ACA_ID,VALIDATED_SIGNAL_ID) SELECT vaca.AGG_ALERT_ID, vaca.VALIDATED_SIGNAL_ID
           FROM VALIDATED_AGG_ALERTS vaca
            INNER JOIN AGG_ALERT aca
                ON vaca.AGG_ALERT_ID = aca.ID
            WHERE aca.ALERT_CONFIGURATION_ID = conf_rec.id and aca.EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);

           INSERT into ARCHIVED_AGG_CASE_ALERT_TAGS(AGG_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vaca.AGG_ALERT_ID, vaca.PVS_ALERT_TAG_ID
           FROM AGG_CASE_ALERT_TAGS vaca
            INNER JOIN AGG_ALERT aca
                ON vaca.AGG_ALERT_ID = aca.ID
            WHERE aca.ALERT_CONFIGURATION_ID = conf_rec.id and aca.EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);

           INSERT into ARCHIVED_ACA_ACTIONS(ARCHIVED_ACA_ID,ACTION_ID) SELECT vaca.AGG_ALERT_ID, vaca.ACTION_ID
           FROM AGG_ALERT_ACTIONS vaca
            INNER JOIN AGG_ALERT aca
                ON vaca.AGG_ALERT_ID = aca.ID
            WHERE aca.ALERT_CONFIGURATION_ID = conf_rec.id and aca.EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);

    --      Move the attachments to Archived Aggregate Case Alert

            MERGE INTO attachment_link al
            USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join AGG_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec) ) conf
            ON (al.reference_id = conf.reference_id)
            WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedAggregateCaseAlert';

            UPDATE product_event_history
            SET archived_agg_case_alert_id=AGG_CASE_ALERT_ID,
                AGG_CASE_ALERT_ID = null
            WHERE CONFIG_ID = conf_rec.id and EXEC_CONFIG_ID  = exec_id_list(exec_rec);

            DELETE FROM VALIDATED_AGG_ALERTS WHERE (AGG_ALERT_ID) in (
            SELECT vaca.AGG_ALERT_ID
            FROM VALIDATED_AGG_ALERTS vaca
            INNER JOIN AGG_ALERT aca
                ON vaca.AGG_ALERT_ID = aca.ID
            WHERE aca.ALERT_CONFIGURATION_ID = conf_rec.id and aca.EXEC_CONFIGURATION_ID = exec_id_list(exec_rec)
            );

            DELETE FROM AGG_ALERT_TAGS WHERE (AGG_ALERT_ID) in (
            SELECT vaca.AGG_ALERT_ID
            FROM AGG_ALERT_TAGS vaca
            INNER JOIN AGG_ALERT aca
                ON vaca.AGG_ALERT_ID = aca.ID
            WHERE aca.ALERT_CONFIGURATION_ID = conf_rec.id and aca.EXEC_CONFIGURATION_ID = exec_id_list(exec_rec)
            );

            DELETE FROM AGG_ALERT_ACTIONS WHERE (AGG_ALERT_ID) in (
            SELECT vaca.AGG_ALERT_ID
            FROM AGG_ALERT_ACTIONS vaca
            INNER JOIN AGG_ALERT aca
                ON vaca.AGG_ALERT_ID = aca.ID
            WHERE aca.ALERT_CONFIGURATION_ID = conf_rec.id and aca.EXEC_CONFIGURATION_ID = exec_id_list(exec_rec)
            );

            DELETE FROM AGG_ALERT WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);
          end loop;
        end if;  --if count greater than 1
        exec_id_list.delete();  -- empty list for fresh entry

       end if;  -- if type = Aggregate Case alert

  END loop;


  --Now enable all constraint of root table
   FOR c IN (SELECT c.owner, c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name in ('SINGLE_CASE_ALERT', 'AGG_ALERT') AND c.status = 'DISABLED' ORDER BY c.constraint_type)
    LOOP
     dbms_utility.exec_ddl_statement('alter table "' || c.owner || '"."' || c.table_name || '" enable constraint ' || c.constraint_name );
    END LOOP;

 END;  -- block for Qualiattive and Quantitative

BEGIN  -- block for EVDAS
  DBMS_OUTPUT.PUT_LINE('Moving the data of EVDAS to Archive Table');

   --First disable all constraint of root table
   FOR c IN (SELECT c.owner, c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name in ('EVDAS_ALERT') AND c.status = 'ENABLED' AND NOT (t.iot_type IS NOT NULL AND c.constraint_type = 'P') ORDER BY c.constraint_type DESC)
    LOOP
     dbms_utility.exec_ddl_statement('alter table "' || c.owner || '"."' || c.table_name || '" disable constraint ' || c.constraint_name || ' cascade');
    END LOOP;


  for conf_rec in (SELECT id FROM EVDAS_CONFIG WHERE is_deleted = 0 and is_enabled = 1)
  loop
   DBMS_OUTPUT.PUT_LINE('ids ' || conf_rec.id);

   --Now select all exec_config_id from EVDAS_ALERT
    SELECT distinct exec_configuration_id bulk collect into exec_id_list FROM EVDAS_ALERT WHERE alert_configuration_id = conf_rec.id order by exec_configuration_id DESC;

   --collect exec ids in string and pass this stirng in next command 'in' section
    if(exec_id_list.count > 1) then
     for exec_rec in 2..exec_id_list.LAST
      loop

       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM user_tab_columns
       WHERE table_name = 'ARCHIVED_EVDAS_ALERT';

       lvc_exec_sql := 'INSERT into ARCHIVED_EVDAS_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = '||conf_rec.id||' and EXEC_CONFIGURATION_ID =' ||exec_id_list(exec_rec);
      execute immediate lvc_exec_sql;

       INSERT into VALIDATED_ARCH_EVDAS_ALERTS(ARCHIVED_EVDAS_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT veva.EVDAS_ALERT_ID, veva.VALIDATED_SIGNAL_ID
       FROM VALIDATED_EVDAS_ALERTS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);

       INSERT into ARCHIVED_EVDAS_ALERT_ACTIONS(ARCHIVED_EVDAS_ALERT_ID,ACTION_ID) SELECT veva.EVDAS_ALERT_ID, veva.ACTION_ID
       FROM EVDAS_ALERT_ACTIONS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);

--      Move the attachments to Archived Evdas Alert

        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join EVDAS_ALERT t2 on t1.reference_id = t2.id WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec)) conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedEvdasAlert';

        MERGE INTO evdas_history eh
        USING (SELECT t1.id as history_id, t1.EVDAS_ALERT_ID as alert_id FROM evdas_history t1 left join EVDAS_ALERT t2 on t1.EVDAS_ALERT_ID = t2.id WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec)) conf
        ON (eh.id = conf.history_id)
        WHEN matched THEN UPDATE SET eh.ARCHIVED_EVDAS_ALERT_ID=conf.alert_id,eh.EVDAS_ALERT_ID = null;

        DELETE FROM VALIDATED_EVDAS_ALERTS WHERE (EVDAS_ALERT_ID) in
        (SELECT veva.EVDAS_ALERT_ID
        FROM VALIDATED_EVDAS_ALERTS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec));

        DELETE FROM EVDAS_ALERT_ACTIONS WHERE (EVDAS_ALERT_ID) in
        (SELECT veva.EVDAS_ALERT_ID
        FROM EVDAS_ALERT_ACTIONS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec));

        DELETE FROM EVDAS_ALERT WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);
      end loop;
    end if;  --if count greater than 1
    exec_id_list.delete();  -- empty list for fresh entry

  END loop;


  --Now enable all constraint of root table
   FOR c IN (SELECT c.owner, c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name in ('EVDAS_ALERT') AND c.status = 'DISABLED' ORDER BY c.constraint_type)
    LOOP
     dbms_utility.exec_ddl_statement('alter table "' || c.owner || '"."' || c.table_name || '" enable constraint ' || c.constraint_name );
    END LOOP;

 END;  -- block for EVDAS


 BEGIN  -- block for LITERATURE
  DBMS_OUTPUT.PUT_LINE('Moving the data of LITERATURE ALERT to Archive Table');

   --First disable all constraint of root table
   FOR c IN (SELECT c.owner, c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name in ('LITERATURE_ALERT') AND c.status = 'ENABLED' AND NOT (t.iot_type IS NOT NULL AND c.constraint_type = 'P') ORDER BY c.constraint_type DESC)
    LOOP
     dbms_utility.exec_ddl_statement('alter table "' || c.owner || '"."' || c.table_name || '" disable constraint ' || c.constraint_name || ' cascade');
    END LOOP;


  for conf_rec in (SELECT id FROM LITERATURE_CONFIG WHERE is_deleted = 0 and is_enabled = 1)
  loop
   DBMS_OUTPUT.PUT_LINE('ids ' || conf_rec.id);

   --Now select all exec_config_id from LITERATURE_ALERT
    SELECT distinct ex_lit_search_config_id bulk collect into exec_id_list FROM LITERATURE_ALERT WHERE lit_search_config_id = conf_rec.id order by ex_lit_search_config_id DESC;

   --collect exec ids in string and pass this stirng in next command 'in' section
    if(exec_id_list.count > 1) then
     for exec_rec in 2..exec_id_list.LAST
      loop

       SELECT listagg(column_name,',') within group (order by column_id) as cols
       INTO lvc_sql
       FROM user_tab_columns
       WHERE table_name = 'ARCHIVED_LITERATURE_ALERT';

       lvc_exec_sql := 'INSERT into ARCHIVED_LITERATURE_ALERT ('||lvc_sql||') SELECT '||lvc_sql||' FROM LITERATURE_ALERT WHERE lit_search_config_id = '||conf_rec.id||' and ex_lit_search_config_id =' ||exec_id_list(exec_rec);
      execute immediate lvc_exec_sql;

       INSERT into VALIDATED_ARCHIVED_LIT_ALERTS(ARCHIVED_LIT_ALERT_ID,VALIDATED_SIGNAL_ID) SELECT ala.LITERATURE_ALERT_ID, ala.VALIDATED_SIGNAL_ID
       FROM VALIDATED_LITERATURE_ALERTS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec);

       INSERT into ARCHIVED_LIT_CASE_ALERT_TAGS(ARCHIVED_LIT_ALERT_ID,PVS_ALERT_TAG_ID) SELECT ala.LITERATURE_ALERT_ID, ala.PVS_ALERT_TAG_ID
       FROM LITERATURE_CASE_ALERT_TAGS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec);

       INSERT into ARCHIVED_LIT_ALERT_ACTIONS(ARCHIVED_LIT_ALERT_ID,ACTION_ID) SELECT ala.LITERATURE_ALERT_ID, ala.ACTION_ID
       FROM LIT_ALERT_ACTIONS ala
        INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec);

--      Move the attachments to Archived Literature Alert
        MERGE INTO attachment_link al
        USING (SELECT t1.reference_id as reference_id FROM attachment_link t1 left join LITERATURE_ALERT t2 on t1.reference_id = t2.id WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec) ) conf
        ON (al.reference_id = conf.reference_id)
        WHEN matched THEN UPDATE SET al.reference_class='com.rxlogix.signal.ArchivedLiteratureAlert';

        DELETE FROM VALIDATED_LITERATURE_ALERTS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID
        FROM VALIDATED_LITERATURE_ALERTS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec)
        );

        DELETE FROM LITERATURE_ALERT_TAGS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID
        FROM LITERATURE_ALERT_TAGS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec)
        );

        DELETE FROM LIT_ALERT_ACTIONS WHERE (LITERATURE_ALERT_ID) in
        (SELECT ala.LITERATURE_ALERT_ID
        FROM LIT_ALERT_ACTIONS ala
                INNER JOIN LITERATURE_ALERT la
            ON ala.LITERATURE_ALERT_ID = la.ID
        WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec)
        );

        DELETE FROM LITERATURE_ALERT WHERE lit_search_config_id = conf_rec.id and ex_lit_search_config_id = exec_id_list(exec_rec);
      end loop;
    end if;  --if count greater than 1
    exec_id_list.delete();  -- empty list for fresh entry

  END loop;


  --Now enable all constraint of root table
   FOR c IN (SELECT c.owner, c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name in ('LITERATURE_ALERT') AND c.status = 'DISABLED' ORDER BY c.constraint_type)
    LOOP
     dbms_utility.exec_ddl_statement('alter table "' || c.owner || '"."' || c.table_name || '" enable constraint ' || c.constraint_name );
    END LOOP;
 END;  -- block for LITERATURE

 COMMIT;
 EXCEPTION
   WHEN others THEN
   v_code := SQLCODE;
   dbms_output.put_line('Error occoured while persisting the archived data. ! Error Code : ' || v_code || ' : ' || SQLERRM);
   rollback;

END MOVE_ALERT_TO_ARCHIVE;