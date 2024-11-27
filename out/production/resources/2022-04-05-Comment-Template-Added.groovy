databaseChangeLog = {

    changeSet(author: "uddesh teke(generated)", id: "1497858211919-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'COMMENT_TEMPLATE')
            }
        }
        createTable(tableName: "COMMENT_TEMPLATE") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "COMMENT_TEMPLAPK")
            }

            column(name: "COMMENTS", type: "clob") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }


            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "uddesh teke(generated)", id: "1497858311919-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_COMMENT_HISTORY')
            }
        }
        createTable(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_COMMENT_HISTOPK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENTS", type: "clob") {
                constraints(nullable: "true")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PERIOD", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "1497858411919-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_COMMENT_HISTORY_MAP')
            }
        }
        createTable(tableName: "ALERT_COMMENT_HISTORY_MAP") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_HISTORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "1497858411919-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_ALERT_COMMENT_HISTORY_MAP')
            }
        }
        createTable(tableName: "AR_ALERT_COMMENT_HISTORY_MAP") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_HISTORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "1497858511919-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'COMMENT_TEMPLATE_ID')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "COMMENT_TEMPLATE_ID", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1654860615-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_FORM', columnName: 'EXCEL_GENERATED')
            }
        }
        addColumn(tableName: "CASE_FORM") {
            column(name: "EXCEL_GENERATED", type: "number(1, 0)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1654860615-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'CASE_FORM', columnName: 'EXCEL_GENERATED')
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' UPDATE CASE_FORM SET EXCEL_GENERATED = 0 ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while mirating excel generated in case form. #############")
                }
            }
        }
    }
    changeSet(author: "rahul (generated)", id: "1654860615-30") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EVDAS_CONFIG')
        }
        grailsChange {
            change {
                try {
                    List<Map> configurationList = []
                    sql.eachRow("""
                     select id,event_group_selection from evdas_config
                        where  event_group_selection is not null """) { row ->
                        configurationList.add(id: row[0],eventGroupSelection: row[1])
                    }
                    println configurationList
                    sql.withBatch(100, "UPDATE EX_EVDAS_CONFIG SET event_group_selection = :eventGroupSelection WHERE CONFIG_ID = :id and event_group_selection is null", { preparedStatement ->
                        configurationList.each {
                            preparedStatement.addBatch(id: it.id, eventGroupSelection: it.eventGroupSelection)
                        }
                    })
                }catch(Exception ex){
                    println("##################### Error occurred while migrating eventGroupSelection from Evdas Confifuration to Executed Evdas Configuration #############")
                }
            }
        }
    }

    changeSet(author: "Yogesh (generated)", id: "1656917316914-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ATTACHMENT_DESCRIPTION', columnName: 'DESCRIPTION')
        }
        sql("alter table ATTACHMENT_DESCRIPTION modify DESCRIPTION VARCHAR2(8000 CHAR);")
    }


    changeSet(author: "uddesh teke (generated)", id: "1497828411919-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'PRODUCT_NAME')
            }
        }
        addColumn(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "PRODUCT_NAME", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "1497828411919-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'EVENT_NAME')
            }
        }
        addColumn(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "EVENT_NAME", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "1497828411919-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'CONFIG_ID')
            }
        }
        addColumn(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "1497828411919-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'PRODUCT_ID')
            }
        }
        addColumn(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "PRODUCT_ID", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "1497828411919-7") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'ALERT_NAME')
        }

        sql("ALTER TABLE ALERT_COMMENT_HISTORY MODIFY (ALERT_NAME NULL);")
    }

    changeSet(author: "uddesh teke (generated)", id: "1497828411919-8") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'PERIOD')
        }
        sql("ALTER TABLE ALERT_COMMENT_HISTORY MODIFY (PERIOD NULL);")
    }

    changeSet(author: "uddesh teke (generated)", id: "1497828411919-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'AGG_ALERT_ID')
        }
        sql("ALTER TABLE ALERT_COMMENT_HISTORY MODIFY (AGG_ALERT_ID NULL);")
    }

    changeSet(author: "uddesh teke (generated)", id: "1497828411919-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'alert_comment_sequence')
            }
        }
        createSequence(sequenceName: "alert_comment_sequence")
    }

    changeSet(author: "uddesh teke (generated)", id: "1573631364906-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'alert_comment_history_sequence')
            }
        }
        createSequence(sequenceName: "alert_comment_history_sequence")
    }


    changeSet(author: "Uddesh Teke (generated)", id: "157301162494322-2") {
        try {
            sql('''
            DELETE FROM ALERT_COMMENT_HISTORY;
            INSERT INTO ALERT_COMMENT_HISTORY   (ID, VERSION, COMMENTS, CREATED_BY, DATE_CREATED, LAST_UPDATED, MODIFIED_BY, ALERT_NAME, AGG_ALERT_ID, CONFIG_ID, PRODUCT_NAME, EVENT_NAME, PRODUCT_ID)
            SELECT  alert_comment_history_sequence.nextval, 0, COMMENTS, CREATED_BY, DATE_CREATED, LAST_UPDATED, MODIFIED_BY, ALERT_NAME, 
                                        CASE_ID, CONFIG_ID, PRODUCT_NAME, EVENT_NAME, PRODUCT_ID
            FROM ALERT_COMMENT
            COMMIT;
            ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "15730162494321-4") {
        try {
            sql('''
            INSERT INTO ALERT_COMMENT   (ID, VERSION, ALERT_TYPE, ARTICLE_ID, CASE_NUMBER, CONFIG_ID, CREATED_BY, DATE_CREATED, EVENT_NAME, LAST_UPDATED, MODIFIED_BY,
                                        PRODUCT_FAMILY, PRODUCT_ID, PRODUCT_NAME, PT_CODE, CASE_ID, DATA_SOURCE, FOLLOW_UP_NUM, SYNC_FLAG, VERSION_NUM, EX_CONFIG_ID, 
                                        ALERT_NAME, COMMENTS, COMMENT_TEMPLATE_ID)
            SELECT alert_comment_sequence.nextval, 0, ALERT_COMMENT.ALERT_TYPE, ALERT_COMMENT.ARTICLE_ID, ALERT_COMMENT.CASE_NUMBER, ALERT_COMMENT.CONFIG_ID,
                                        ALERT_COMMENT.CREATED_BY, ALERT_COMMENT.DATE_CREATED, ALERT_COMMENT.EVENT_NAME, ALERT_COMMENT.LAST_UPDATED, ALERT_COMMENT.MODIFIED_BY,
                                        ALERT_COMMENT.PRODUCT_FAMILY, ALERT_COMMENT.PRODUCT_ID, ALERT_COMMENT.PRODUCT_NAME, ALERT_COMMENT.PT_CODE, ALERT_COMMENT.CASE_ID,
                                        ALERT_COMMENT.DATA_SOURCE, ALERT_COMMENT.FOLLOW_UP_NUM, ALERT_COMMENT.SYNC_FLAG, ALERT_COMMENT.VERSION_NUM, EX_RCONFIG.ID,
                                        ALERT_COMMENT.ALERT_NAME, ALERT_COMMENT.COMMENTS, ALERT_COMMENT.COMMENT_TEMPLATE_ID
            FROM ALERT_COMMENT
            INNER JOIN
            EX_RCONFIG ON
                EX_RCONFIG.CONFIG_ID = ALERT_COMMENT.CONFIG_ID;
            COMMIT;
            ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "15730162494324-5") {
        try {
            sql('''
            INSERT INTO SHARE_WITH_USER_REFERENCES (REFERENCE_ID, SHARE_WITH_USER_ID, SHARE_WITH_USER_IDX)
            select RD.ID, PU.id, (select count(*) from SHARE_WITH_USER_REFERENCES where REFERENCE_ID=RD.ID)+1 from
                 REFERENCE_DETAILS RD
                    INNER JOIN PVUSER PU ON PU.FULL_NAME = RD.CREATED_BY
                        WHERE (SELECT COUNT(*) FROM USER_REFERENCES_MAPPING WHERE USER_ID=PU.ID AND REFERENCE_ID=RD.ID  AND IS_DELETED=0) > 0
                        AND
                        (SELECT COUNT(*) FROM SHARE_WITH_USER_REFERENCES WHERE SHARE_WITH_USER_ID=PU.ID AND REFERENCE_ID=RD.ID)=0
                        AND
                        RD.IS_DELETED=0;
            COMMIT;
            ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

}
