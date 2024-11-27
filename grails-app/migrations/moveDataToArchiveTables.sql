CREATE OR REPLACE FUNCTION move_alert_to_archive() RETURNS VOID AS
$$
DECLARE
    -- Type declarations for PostgreSQL
    exec_id_list BIGINT[] := ARRAY[]::BIGINT[];
    config_id BIGINT[];
    typename TEXT[];
    v_code INTEGER;
    lvc_sql TEXT;
    lvc_exec_sql TEXT;
    conf_rec RECORD;
    c RECORD;

BEGIN
    -- Block for Qualitative and Quantitative
    RAISE NOTICE 'Moving the data of Qualitative and Quantitative to Archive Table';

    -- First disable all constraints of root table
    FOR c IN
        SELECT conname AS constraint_name, conrelid::regclass AS table_name
        FROM pg_constraint
        WHERE conrelid::regclass IN ('single_case_alert'::regclass, 'agg_alert'::regclass)
          AND convalidated
        LOOP
            EXECUTE format('ALTER TABLE %I DISABLE TRIGGER ALL', c.table_name);
        END LOOP;

    FOR conf_rec IN
        SELECT id, type FROM rconfig WHERE is_deleted = 0 AND is_enabled = 1
        LOOP
            RAISE NOTICE 'ids %', conf_rec.id;

            -- Move Qualitative alert records first
            IF conf_rec.type = 'Single Case Alert' THEN
                -- Select all exec_config_id from single_case_alert
                EXECUTE format('
                SELECT DISTINCT exec_config_id FROM single_case_alert sca
                LEFT JOIN ex_rconfig rc ON rc.id = sca.exec_config_id
                WHERE alert_configuration_id = %s AND rc.is_latest = 0
                ORDER BY exec_config_id DESC
            ', conf_rec.id) INTO exec_id_list;

                IF array_length(exec_id_list, 1) > 0 THEN
                    FOR exec_rec IN 1..array_length(exec_id_list, 1)
                        LOOP
                            SELECT STRING_AGG(column_name, ',') AS cols
                            INTO lvc_sql
                            FROM information_schema.columns
                            WHERE table_name = 'archived_single_case_alert';

                            lvc_exec_sql := format(
                                    'INSERT INTO archived_single_case_alert (%s) SELECT %s FROM single_case_alert WHERE alert_configuration_id = %s AND exec_config_id = %s',
                                    lvc_sql, lvc_sql, conf_rec.id, exec_id_list[exec_rec]
                                );
                            EXECUTE lvc_exec_sql;

                            EXECUTE format('
                        INSERT INTO validated_archived_sca (archived_sca_id, validated_signal_id)
                        SELECT vsca.single_alert_id, vsca.validated_signal_id
                        FROM validated_single_alerts vsca
                        INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                        WHERE alert_configuration_id = %s AND exec_config_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_sca_tags (single_alert_id, pvs_alert_tag_id)
                        SELECT vsca.single_alert_id, vsca.pvs_alert_tag_id
                        FROM single_case_alert_tags vsca
                        INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                        WHERE alert_configuration_id = %s AND exec_config_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_sca_pt (archived_sca_id, archived_sca_pt, pt_list_idx)
                        SELECT vsca.single_alert_id, vsca.sca_pt, vsca.pt_list_idx
                        FROM single_alert_pt vsca
                        INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                        WHERE alert_configuration_id = %s AND exec_config_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_sca_con_comit (archived_sca_id, alert_con_comit, con_comit_list_idx)
                        SELECT vsca.single_alert_id, vsca.alert_con_comit, vsca.con_comit_list_idx
                        FROM single_alert_con_comit vsca
                        INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                        WHERE alert_configuration_id = %s AND exec_config_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_sca_susp_prod (archived_sca_id, sca_product_name, suspect_product_list_idx)
                        SELECT vsca.single_alert_id, vsca.sca_product_name, vsca.suspect_product_list_idx
                        FROM single_alert_susp_prod vsca
                        INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                        WHERE alert_configuration_id = %s AND exec_config_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_sca_med_err_pt_list (archived_sca_id, sca_med_error, med_error_pt_list_idx)
                        SELECT vsca.single_alert_id, vsca.sca_med_error, vsca.med_error_pt_list_idx
                        FROM single_alert_med_err_pt_list vsca
                        INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                        WHERE alert_configuration_id = %s AND exec_config_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

       INSERT into ARCHIVED_SCA_ACTIONS(ARCHIVED_SCA_ID,ACTION_ID,IS_RETAINED) SELECT vsca.SINGLE_CASE_ALERT_ID, vsca.ACTION_ID, vsca.IS_RETAINED
       FROM SINGLE_ALERT_ACTIONS vsca
        INNER JOIN SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_CASE_ALERT_ID = sca.ID
             WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIG_ID = exec_id_list(exec_rec);

                            -- Move the attachments to Archived Single Case Alert
                            EXECUTE format('
                        UPDATE attachment_link
                        SET reference_class = ''com.rxlogix.signal.ArchivedSingleCaseAlert''
                        WHERE reference_id IN (
                            SELECT t1.reference_id
                            FROM attachment_link t1
                            LEFT JOIN single_case_alert t2 ON t1.reference_id = t2.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM validated_single_alerts
                        WHERE single_alert_id IN (
                            SELECT vsca.single_alert_id
                            FROM validated_single_alerts vsca
                            INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM single_alert_tags
                        WHERE single_alert_id IN (
                            SELECT vsca.single_alert_id
                            FROM single_alert_tags vsca
                            INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM single_alert_pt
                        WHERE single_alert_id IN (
                            SELECT vsca.single_alert_id
                            FROM single_alert_pt vsca
                            INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM single_alert_con_comit
                        WHERE single_alert_id IN (
                            SELECT vsca.single_alert_id
                            FROM single_alert_con_comit vsca
                            INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM single_alert_susp_prod
                        WHERE single_alert_id IN (
                            SELECT vsca.single_alert_id
                            FROM single_alert_susp_prod vsca
                            INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM single_alert_med_err_pt_list
                        WHERE single_alert_id IN (
                            SELECT vsca.single_alert_id
                            FROM single_alert_med_err_pt_list vsca
                            INNER JOIN single_case_alert sca ON vsca.single_alert_id = sca.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM single_alert_actions
                        WHERE single_case_alert_id IN (
                            SELECT vsca.single_case_alert_id
                            FROM single_alert_actions vsca
                            INNER JOIN single_case_alert sca ON vsca.single_case_alert_id = sca.id
                            WHERE alert_configuration_id = %s AND exec_config_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

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

           INSERT into ARCHIVED_ACA_ACTIONS(ARCHIVED_ACA_ID,ACTION_ID,IS_RETAINED) SELECT vaca.AGG_ALERT_ID, vaca.ACTION_ID, vaca.IS_RETAINED
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

            -- Handle Aggregate Case Alert similarly

            IF conf_rec.type = 'Aggregate Case Alert' THEN
                -- Select all exec_configuration_id from agg_alert
                EXECUTE format('
                SELECT DISTINCT exec_configuration_id FROM agg_alert
                WHERE alert_configuration_id = %s
                ORDER BY exec_configuration_id DESC
            ', conf_rec.id) INTO exec_id_list;

                IF array_length(exec_id_list, 1) > 0 THEN
                    FOR exec_rec IN 1..array_length(exec_id_list, 1)
                        LOOP
                            SELECT STRING_AGG(column_name, ',') AS cols
                            INTO lvc_sql
                            FROM information_schema.columns
                            WHERE table_name = 'archived_agg_alert';

                            lvc_exec_sql := format(
                                    'INSERT INTO archived_agg_alert (%s) SELECT %s FROM agg_alert WHERE alert_configuration_id = %s AND exec_configuration_id = %s',
                                    lvc_sql, lvc_sql, conf_rec.id, exec_id_list[exec_rec]
                                );
                            EXECUTE lvc_exec_sql;

                            EXECUTE format('
                        INSERT INTO validated_archived_aca (archived_aca_id, validated_signal_id)
                        SELECT vaca.agg_alert_id, vaca.validated_signal_id
                        FROM validated_agg_alerts vaca
                        INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                        WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_aca_tags (agg_alert_id, pvs_alert_tag_id)
                        SELECT vaca.agg_alert_id, vaca.pvs_alert_tag_id
                        FROM agg_alert_tags vaca
                        INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                        WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

       INSERT into ARCHIVED_EVDAS_ALERT_ACTIONS(ARCHIVED_EVDAS_ALERT_ID,ACTION_ID,IS_RETAINED) SELECT veva.EVDAS_ALERT_ID, veva.ACTION_ID, veva.IS_RETAINED
       FROM EVDAS_ALERT_ACTIONS veva
        INNER JOIN EVDAS_ALERT eva
            ON veva.EVDAS_ALERT_ID = eva.ID
            WHERE ALERT_CONFIGURATION_ID = conf_rec.id and EXEC_CONFIGURATION_ID = exec_id_list(exec_rec);

                            EXECUTE format('
                        INSERT INTO archived_aca_con_comit (archived_aca_id, alert_con_comit, con_comit_list_idx)
                        SELECT vaca.agg_alert_id, vaca.alert_con_comit, vaca.con_comit_list_idx
                        FROM agg_alert_con_comit vaca
                        INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                        WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_aca_susp_prod (archived_aca_id, aca_product_name, suspect_product_list_idx)
                        SELECT vaca.agg_alert_id, vaca.aca_product_name, vaca.suspect_product_list_idx
                        FROM agg_alert_susp_prod vaca
                        INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                        WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_aca_med_err_pt_list (archived_aca_id, aca_med_error, med_error_pt_list_idx)
                        SELECT vaca.agg_alert_id, vaca.aca_med_error, vaca.med_error_pt_list_idx
                        FROM agg_alert_med_err_pt_list vaca
                        INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                        WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        INSERT INTO archived_aca_actions (archived_aca_id, action_id)
                        SELECT vaca.agg_alert_id, vaca.action_id
                        FROM agg_alert_actions vaca
                        INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                        WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            -- Move the attachments to Archived Aggregate Case Alert
                            EXECUTE format('
                        UPDATE attachment_link
                        SET reference_class = ''com.rxlogix.signal.ArchivedAggregateCaseAlert''
                        WHERE reference_id IN (
                            SELECT t1.reference_id
                            FROM attachment_link t1
                            LEFT JOIN agg_alert t2 ON t1.reference_id = t2.id
                            WHERE alert_configuration_id = %s AND exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM validated_agg_alerts
                        WHERE agg_alert_id IN (
                            SELECT vaca.agg_alert_id
                            FROM validated_agg_alerts vaca
                            INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                            WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM agg_alert_tags
                        WHERE agg_alert_id IN (
                            SELECT vaca.agg_alert_id
                            FROM agg_alert_tags vaca
                            INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                            WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM agg_alert_pt
                        WHERE agg_alert_id IN (
                            SELECT vaca.agg_alert_id
                            FROM agg_alert_pt vaca
                            INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                            WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM agg_alert_con_comit
                        WHERE agg_alert_id IN (
                            SELECT vaca.agg_alert_id
                            FROM agg_alert_con_comit vaca
                            INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                            WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM agg_alert_susp_prod
                        WHERE agg_alert_id IN (
                            SELECT vaca.agg_alert_id
                            FROM agg_alert_susp_prod vaca
                            INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                            WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM agg_alert_med_err_pt_list
                        WHERE agg_alert_id IN (
                            SELECT vaca.agg_alert_id
                            FROM agg_alert_med_err_pt_list vaca
                            INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                            WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM agg_alert_actions
                        WHERE agg_alert_id IN (
                            SELECT vaca.agg_alert_id
                            FROM agg_alert_actions vaca
                            INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id
                            WHERE aca.alert_configuration_id = %s AND aca.exec_configuration_id = %s
                        )
                    ', conf_rec.id, exec_id_list[exec_rec]);

                            EXECUTE format('
                        DELETE FROM agg_alert
                        WHERE alert_configuration_id = %s AND exec_configuration_id = %s
                    ', conf_rec.id, exec_id_list[exec_rec]);
                        END LOOP;
                END IF;
                exec_id_list := ARRAY[]::BIGINT[];
            END IF;

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

       INSERT into ARCHIVED_LIT_ALERT_ACTIONS(ARCHIVED_LIT_ALERT_ID,ACTION_ID,IS_RETAINED) SELECT ala.LITERATURE_ALERT_ID, ala.ACTION_ID, ala.IS_RETAINED
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
    WHEN OTHERS THEN
        v_code := SQLSTATE;
        RAISE NOTICE 'Error occurred while persisting the archived data. Error Code: % : %', v_code, SQLERRM;
        ROLLBACK;
END;
$$ LANGUAGE plpgsql;
