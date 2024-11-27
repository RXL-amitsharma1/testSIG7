import com.rxlogix.PvsAlertTagService
import com.rxlogix.PvsGlobalTagService

databaseChangeLog = {
        changeSet(author: "amrendra (generated)", id: "1610473325439-1") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK5395n1fvj46kencfit7o01ycm")
        }
        dropForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK5395n1fvj46kencfit7o01ycm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1610473325439-2") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK36oexbhattm8op3hnya3tcgoy")
        }
        dropForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FK36oexbhattm8op3hnya3tcgoy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "akshat (generated)", id: "1610473325439-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'global_case', columnName: 'version_num')
            }
        }
        addColumn(tableName: "global_case") {
            column(name: "version_num", type: "number(10, 0)")
        }
    }

    changeSet(author: "akshat (generated)", id: "1610473325439-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM GLOBAL_CASE where VERSION_NUM IS NULL;")
            }
        }
        grailsChange {
            change {
                try {
                    sql.execute("DELETE FROM PVS_GLOBAL_TAG WHERE DOMAIN = 'Single Case Alert'")
                    sql.execute("DELETE FROM SINGLE_GLOBAL_TAGS")
                    sql.execute('''INSERT INTO GLOBAL_CASE(globalcaseid,VERSION , CASE_ID, VERSION_NUM)
                                        select hibernate_sequence.nextval ,VERSION,case_id,case_Version from (
                                        (SELECT 0 as VERSION, CASE_ID, CASE_VERSION FROM SINGLE_CASE_ALERT UNION SELECT 0 , CASE_ID, CASE_VERSION FROM ARCHIVED_SINGLE_CASE_ALERT ))  ''')
                    sql.execute('''MERGE INTO SINGLE_CASE_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        GLOBAL_CASE
                                )
                                b ON ( b.CASE_ID = a.CASE_ID AND b.VERSION_NUM = a.CASE_VERSION)
                                WHEN MATCHED THEN UPDATE SET a.GLOBAL_IDENTITY_ID = GLOBALCASEID   ''')
                    sql.execute('''MERGE INTO ARCHIVED_SINGLE_CASE_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        GLOBAL_CASE
                                )
                                b ON ( b.CASE_ID = a.CASE_ID AND b.VERSION_NUM = a.CASE_VERSION)
                                WHEN MATCHED THEN UPDATE SET a.GLOBAL_IDENTITY_ID = GLOBALCASEID   ''')
                    sql.execute('''ALTER TABLE SINGLE_CASE_ALERT DISABLE CONstraint FK8ic0iqi8eynbxkkwxroc6io1r ''')
                    sql.execute('''ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT DISABLE CONstraint FKqaxchl4ofg9634tb244aw63e5 ''')
                    sql.execute("DELETE FROM GLOBAL_CASE where version_NUM is NULL")
                    sql.execute('''ALTER TABLE SINGLE_CASE_ALERT ENABLE CONstraint FK8ic0iqi8eynbxkkwxroc6io1r ''')
                    sql.execute('''ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT ENABLE CONstraint FKqaxchl4ofg9634tb244aw63e5 ''')

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating GLOBAL_CASE table. #############")
                }

            }
        }
    }

    changeSet(author: "akshat (generated)", id: "1610473325439-5") {
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.migrateGlobalCase()
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating Global Case table. #############")
                }
            }
        }
    }

    changeSet(author: "akshat (generated)", id: "1610473325439-6") {
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.importSingleGlobalTags()
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating PvsGlobalTag table For SingleCaseAlert. #############")
                }
            }
        }
    }

    changeSet(author: "akshat (generated)", id: "1610473325439-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM PVS_ALERT_TAG WHERE DOMAIN = 'Single Case Alert';")
            }
        }
        grailsChange {
            change {
                try {
                    sql.execute("DELETE FROM SINGLE_CASE_ALERT_TAGS")
                    sql.execute("DELETE FROM ARCHIVED_SCA_TAGS")
                    sql.execute("DELETE FROM PVS_ALERT_TAG WHERE DOMAIN = 'Single Case Alert'")

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PVS_ALERT_TAG table. #############")
                }

            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1610473325439-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_single_alert_exconfig", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "EXEC_CONFIG_ID")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1610473325439-9") {
        grailsChange {
            change {
                try {
                    PvsAlertTagService pvsAlertTagService = ctx.pvsAlertTagService
                    pvsAlertTagService.importSingleAlertTags(false)
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating SingleAlertTags table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1610473325439-10") {
        grailsChange {
            change {
                try {
                    PvsAlertTagService pvsAlertTagService = ctx.pvsAlertTagService
                    pvsAlertTagService.importSingleAlertTags(true)
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating Archived SingleAlertTags table. #############")
                }
            }
        }
    }
}