create or replace PROCEDURE MOVE_Evdas_Config_id
AS
  v_code          NUMBER;
  config_id_value NUMBER;
  alert_count     NUMBER;
  config_count    NUMBER;
BEGIN
  FOR exec_conf_rec IN (SELECT * FROM EX_EVDAS_CONFIG WHERE CONFIG_ID IS NULL)
  LOOP
     SELECT COUNT(1) INTO alert_count from EVDAS_ALERT WHERE exec_configuration_id = exec_conf_rec.id and rownum = 1;
     IF alert_count > 0 THEN -- First of all we are going to check if there is any data in literature alert.
        SELECT alert_configuration_id INTO config_id_value FROM EVDAS_ALERT WHERE exec_configuration_id = exec_conf_rec.id and rownum = 1;
        UPDATE EX_EVDAS_CONFIG SET config_id = config_id_value WHERE id = exec_conf_rec.id;
     ELSE -- If there is no data then we'll try to fetch the alert config id based on alert name and owner.
        SELECT COUNT(1) INTO config_count from EVDAS_CONFIG where name = exec_conf_rec.name and OWNER_ID = exec_conf_rec.OWNER_ID;
        IF config_count > 0 THEN -- It will try to determine if we have any config with same name and owner.
            SELECT ID INTO config_id_value FROM EVDAS_CONFIG where name = exec_conf_rec.name and OWNER_ID = exec_conf_rec.OWNER_ID and rownum = 1;
            UPDATE EX_EVDAS_CONFIG SET config_id = config_id_value WHERE id = exec_conf_rec.id;
        ELSE
            UPDATE EX_EVDAS_CONFIG SET config_id = 0 WHERE id = exec_conf_rec.id;
        END IF;
     END IF;
     alert_count := 0;
     config_count := 0;
     config_id_value := 0;
  END LOOP;
  COMMIT;
EXCEPTION
WHEN OTHERS THEN
  v_code := SQLCODE;
  dbms_output.put_line('Error occoured while updating the configid  ! Error Code : ' || v_code || ' : ' || SQLERRM);
  ROLLBACK;
END MOVE_Evdas_Config_id;