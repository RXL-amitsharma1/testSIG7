create or replace PROCEDURE MOVE_TAGS_ARCHIVE AS
type id_list_type is table of number index by PLS_INTEGER;

exec_id_list id_list_type;
tag_archive_list id_list_type;
v_code NUMBER;
BEGIN

 BEGIN
  DBMS_OUTPUT.PUT_LINE('Moving the tags data of Qualitative  Archive Table');


    SELECT distinct exec_config_id bulk collect into exec_id_list FROM ARCHIVED_SINGLE_CASE_ALERT sca  order by exec_config_id DESC;

    if(exec_id_list.count > 0) then
     for exec_rec in 1..exec_id_list.LAST
      loop

       INSERT into ARCHIVED_SCA_TAGS(SINGLE_ALERT_ID,PVS_ALERT_TAG_ID) SELECT vsca.SINGLE_ALERT_ID, vsca.PVS_ALERT_TAG_ID
       FROM SINGLE_CASE_ALERT_TAGS vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
        where vsca.PVS_ALERT_TAG_ID not in(select PVS_ALERT_TAG_ID from ARCHIVED_SCA_TAGS) AND  EXEC_CONFIG_ID = exec_id_list(exec_rec);



        DELETE FROM SINGLE_CASE_ALERT_TAGS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM SINGLE_CASE_ALERT_TAGS vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE vsca.PVS_ALERT_TAG_ID not in(select PVS_ALERT_TAG_ID from ARCHIVED_SCA_TAGS) AND EXEC_CONFIG_ID = exec_id_list(exec_rec)
        );


      end loop;
    end if;  --if count greater than 1


 END;  -- block for Qualiattive and Quantitative

 COMMIT;
 EXCEPTION
   WHEN others THEN
   v_code := SQLCODE;
   dbms_output.put_line('Error occoured while persisting the archived data. ! Error Code : ' || v_code || ' : ' || SQLERRM);
   rollback;

END MOVE_TAGS_ARCHIVE;