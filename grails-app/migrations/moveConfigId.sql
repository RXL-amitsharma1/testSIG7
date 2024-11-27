create or replace PROCEDURE MOVE_Config_id AS
v_code NUMBER;
config_id_value NUMBER;
alert_count     NUMBER;
config_count    NUMBER;
BEGIN
for ex_conf_rec in (SELECT * FROM EX_RCONFIG where config_id is null)
loop
IF (ex_conf_rec.TYPE = 'Aggregate Case Alert') THEN
    SELECT count(1) INTO alert_count FROM
    agg_alert where exec_configuration_id = ex_conf_rec.id and ROWNUM =1;
    IF(alert_count > 0) THEN
        SELECT ALERT_CONFIGURATION_ID INTO config_id_value
        FROM agg_alert
        where exec_configuration_id = ex_conf_rec.id  and ROWNUM =1;
        DBMS_OUTPUT.PUT_LINE(config_id_value);
        update ex_rconfig set config_id = config_id_value where id = ex_conf_rec.id;
    ELSE
        select COUNT(1) INTO config_count
        from RCONFIG
        WHERE name = ex_conf_rec.name and PVUSER_ID = ex_conf_rec.PVUSER_ID and type = 'Aggregate Case Alert';
        IF(config_count > 0) THEN -- It will try to determine if we have any config with same name,type and owner.
          select id INTO config_id_value from RCONFIG
          WHERE name = ex_conf_rec.name and PVUSER_ID = ex_conf_rec.PVUSER_ID
          and type = 'Aggregate Case Alert' and rownum = 1;
          DBMS_OUTPUT.PUT_LINE(config_id_value);
          UPDATE ex_rconfig SET config_id = config_id_value WHERE id = ex_conf_rec.id;
        ELSE
          update ex_rconfig set config_id = 0 where id = ex_conf_rec.id;
        END IF;
    END IF;
    alert_count := 0;
    config_count := 0;
    config_id_value := 0;
ELSE
    SELECT count(1) INTO alert_count FROM
    single_case_alert where exec_config_id = ex_conf_rec.id and ROWNUM =1;
    IF(alert_count > 0) THEN
        SELECT ALERT_CONFIGURATION_ID INTO config_id_value
        FROM single_case_alert
        where exec_config_id = ex_conf_rec.id  and ROWNUM =1;
        DBMS_OUTPUT.PUT_LINE(config_id_value);
        update ex_rconfig set config_id = config_id_value where id = ex_conf_rec.id;
    ELSE
        select COUNT(1) INTO config_count
        from RCONFIG
        WHERE name = ex_conf_rec.name and PVUSER_ID = ex_conf_rec.PVUSER_ID and type = 'Single Case Alert';
        IF(config_count > 0) THEN -- It will try to determine if we have any config with same name, type and owner.
          select id INTO config_id_value from RCONFIG
          WHERE name = ex_conf_rec.name and PVUSER_ID = ex_conf_rec.PVUSER_ID
          and type = 'Single Case Alert' and rownum = 1;
          DBMS_OUTPUT.PUT_LINE(config_id_value);
          UPDATE ex_rconfig SET config_id = config_id_value WHERE id = ex_conf_rec.id;
        ELSE
          update ex_rconfig set config_id = 0 where id = ex_conf_rec.id;
        END IF;
    END IF;
    alert_count := 0;
    config_count := 0;
END IF;
end loop;

COMMIT;
EXCEPTION
WHEN others THEN
v_code := SQLCODE;
dbms_output.put_line('Error occoured while updating the configid  ! Error Code : ' || v_code || ' : ' || SQLERRM);
rollback;
END MOVE_Config_id;