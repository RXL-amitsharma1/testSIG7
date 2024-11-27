
DECLARE
    ln_count   NUMBER;
    lvc_sql    VARCHAR2(32000);
BEGIN
    SELECT
        COUNT(*)
    INTO ln_count
    FROM
        user_tables
    WHERE
        table_name = 'CAT_AGG_ALERT';

    IF ln_count = 1 THEN
        lvc_sql := 'DROP TABLE cat_agg_alert PURGE';
        EXECUTE IMMEDIATE lvc_sql;
    END IF;
    lvc_sql := '
    CREATE TABLE cat_agg_alert
        AS
            SELECT DISTINCT
                alertab.exec_configuration_id,
                CASE
                    WHEN config.group_by_smq = 1
                         AND config.event_group_selection IS NOT NULL THEN
                        600
                    ELSE
                        11
                END AS new_event_hierarchy_id
            FROM
                agg_alert    alertab,
                ex_rconfig   config
            WHERE
                alertab.smq_code IS NULL
                AND alertab.exec_configuration_id = config.id
        '
    ;
    EXECUTE IMMEDIATE lvc_sql;
    lvc_sql := '
    CREATE INDEX idx_cat_agg_alert ON
        cat_agg_alert (
            exec_configuration_id
        )    
    '
    ;
    EXECUTE IMMEDIATE lvc_sql;
    lvc_sql := '
    UPDATE agg_alert tgt
    SET
        tgt.event_hierarchy_id = (
            SELECT DISTINCT
                new_event_hierarchy_id
            FROM
                cat_agg_alert alertab
            WHERE
                alertab.exec_configuration_id = tgt.exec_configuration_id
        )
    WHERE
        tgt.smq_code IS NULL
    '
    ;
    EXECUTE IMMEDIATE lvc_sql;
    COMMIT;
    lvc_sql := 'DROP TABLE cat_agg_alert PURGE';
    EXECUTE IMMEDIATE lvc_sql;
END;
/