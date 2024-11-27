import com.rxlogix.PvsAlertTagService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.UserDashboardCounts
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.SignalQueryHelper
import groovy.json.JsonBuilder
import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "1598798990939-77") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FORMAT')
        }
        sql("alter table ARCHIVED_AGG_ALERT modify FORMAT VARCHAR2(8000 CHAR);")
    }

    changeSet(author: "akshat (generated)", id: "1600239568834-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'global_case', columnName: 'version_num')
            }
        }
        addColumn(tableName: "global_case") {
            column(name: "version_num", type: "number(10, 0)")
        }
    }

    changeSet(author: "akshat (generated)", id: "1600239568834-5") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKRBK26CKL20Y61BNM3FLKO1MMK')
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FKRBK26CKL20Y61BNM3FLKO1MMK")
    }

    changeSet(author: "Nikhil (generated)", id: "1573900009384759-36") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EVDAS_ALERT' AND column_name = 'LISTEDNESS' ;")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(1)", columnName: "listedness", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "Nikhil (generated)", id: "157390948609456-92") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns" +
                    " WHERE table_name = 'ARCHIVED_EVDAS_ALERT' AND column_name = 'LISTEDNESS' ;")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(1)", columnName: "listedness", tableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "ankit (generated)", id: "1600239568836-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'user_dashboard_counts')
            }
        }
        createTable(tableName: "user_dashboard_counts") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_disp_case_counts", type: "varchar2(4000 CHAR)")
            column(name: "group_disp_case_counts", type: "varchar2(4000 CHAR)")
            column(name: "user_due_date_case_counts", type: "varchar2(8000 CHAR)")
            column(name: "group_due_date_case_counts", type: "varchar2(8000 CHAR)")
            column(name: "user_disppecounts", type: "varchar2(4000 CHAR)")
            column(name: "group_disppecounts", type: "varchar2(4000 CHAR)")
            column(name: "user_due_datepecounts", type: "varchar2(8000 CHAR)")
            column(name: "group_due_datepecounts", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1600239568836-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_user_dashboard_countsPK')
            }
        }
        createIndex(indexName: "IX_user_dashboard_countsPK", tableName: "user_dashboard_counts", unique: "true") {
            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "user_id", constraintName: "user_dashboard_countsPK", forIndexName: "IX_user_dashboard_countsPK", tableName: "user_dashboard_counts")
    }


    changeSet(author: "ankit (generated)", id: "1600239568836-3") {
        grailsChange {
            change {
                try {
                    if (UserDashboardCounts.count()) {
                        return
                    }
                    Sql sql = new Sql(ctx.getBean("dataSource"))
                    List<Map> userDispCaseCountList = []
                    Map<Long, Integer> userDispCaseCountsMap = [:]
                    List<Map> groupDispCaseCountList = []
                    Map<Long, Map> groupDispCaseCountsMap = [:]
                    List<Map> userDueDateCaseCountsList = []
                    Map<String, Integer> userDueDateCaseCountsMap = [:]
                    List<Map> dueDateGroupCaseCountList = []
                    Map<Long, Map> dueDateGroupCaseCountsMap = [:]

                    List<Map> userDispPECountList = []
                    Map<Long, Integer> userDispPECountsMap = [:]
                    List<Map> groupDispPECountList = []
                    Map<Long, Map> groupDispPECountsMap = [:]
                    List<Map> userDueDatePECountsList = []
                    Map<String, Integer> userDueDatePECountsMap = [:]
                    List<Map> dueDateGroupPECountList = []
                    Map<Long, Map> dueDateGroupPECountsMap = [:]


                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(true), []) { row ->
                        userDispCaseCountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(false), []) { row ->
                        groupDispCaseCountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(true), []) { row ->
                        userDueDateCaseCountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(false), []) { row ->
                        dueDateGroupCaseCountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(true), []) { row ->
                        userDispPECountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(false), []) { row ->
                        groupDispPECountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(true), []) { row ->
                        userDueDatePECountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(false), []) { row ->
                        dueDateGroupPECountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    User.list().each { user ->
                        Group workflowgroup = user.workflowGroup
                        List<Long> groupIdList = user.groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.id
                        groupIdList.each { id ->
                            Map dispCountMap = [:]
                            Map dueDateCountMap = [:]
                            Map dispPECountMap = [:]
                            Map dueDatePECountMap = [:]

                            groupDispCaseCountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dispCountMap.put(it.dispositionId, it.count)
                            }

                            if (dispCountMap) {
                                groupDispCaseCountsMap.put(id, dispCountMap)
                            }

                            dueDateGroupCaseCountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dueDateCountMap.put(it.due_date, it.count)
                            }

                            if (dueDateCountMap) {
                                dueDateGroupCaseCountsMap.put(id, dueDateCountMap)
                            }

                            groupDispPECountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dispPECountMap.put(it.dispositionId, it.count)
                            }

                            if (dispPECountMap) {
                                groupDispPECountsMap.put(id, dispPECountMap)
                            }

                            dueDateGroupPECountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dueDatePECountMap.put(it.due_date, it.count)
                            }

                            if (dueDatePECountMap) {
                                dueDateGroupPECountsMap.put(id, dueDatePECountMap)
                            }
                        }

                        userDispCaseCountList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDispCaseCountsMap.put(it.dispositionId, it.count)
                        }

                        userDueDateCaseCountsList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDueDateCaseCountsMap.put(it.due_date, it.count)
                        }

                        userDispPECountList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDispPECountsMap.put(it.dispositionId, it.count)
                        }

                        userDueDatePECountsList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDueDatePECountsMap.put(it.due_date, it.count)
                        }

                        sql.execute("""INSERT INTO 
                                                  user_dashboard_counts (user_id, user_disp_case_counts, group_disp_case_counts, user_due_date_case_counts, group_due_date_case_counts, 
                                                  user_disppecounts, group_disppecounts, user_due_datepecounts,group_due_datepecounts)
                                                  VALUES (${user.id}, ${userDispCaseCountsMap ? new JsonBuilder(userDispCaseCountsMap).toPrettyString() : null},
                                                          ${groupDispCaseCountsMap ? new JsonBuilder(groupDispCaseCountsMap).toPrettyString() : null},
                                                          ${userDueDateCaseCountsMap ? new JsonBuilder(userDueDateCaseCountsMap).toPrettyString() : null},
                                                          ${dueDateGroupCaseCountsMap ? new JsonBuilder(dueDateGroupCaseCountsMap).toPrettyString() : null},
                                                          ${userDispPECountsMap ? new JsonBuilder(userDispPECountsMap).toPrettyString() : null},
                                                          ${groupDispPECountsMap ? new JsonBuilder(groupDispPECountsMap).toPrettyString() : null},
                                                          ${userDueDatePECountsMap ? new JsonBuilder(userDueDatePECountsMap).toPrettyString() : null},
                                                          ${dueDateGroupPECountsMap ? new JsonBuilder(dueDateGroupPECountsMap).toPrettyString() : null})
                                           """)
                        userDispCaseCountsMap.clear()
                        groupDispCaseCountsMap.clear()
                        userDueDateCaseCountsMap.clear()
                        dueDateGroupCaseCountsMap.clear()
                        userDispPECountsMap.clear()
                        groupDispPECountsMap.clear()
                        userDueDatePECountsMap.clear()
                        dueDateGroupPECountsMap.clear()
                    }
                }
                catch (Exception ex) {
                    println "##### Error Occurred while updating the UserDashboardCounts ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1600239568836-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'disp_counts')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "disp_counts", type: "varchar2(4000 CHAR)") {
                constraints(nullable: "true")
            }
        }

        grailsChange {
            change {
                List<Map> execConfigDispCountList = []
                sql.eachRow("""
                     select exec_config_id,disposition_id,count(id) from single_case_alert 
                        where  exec_config_id in (select id from ex_rconfig)
                               and exec_config_id is not null
                        group by exec_config_id,disposition_id""") { row ->
                    execConfigDispCountList.add(execConfigId: row[0], dispositionId: row[1], count: row[2])
                }

                sql.eachRow("""
                     select exec_configuration_id,disposition_id,count(id) from agg_alert 
                        where  exec_configuration_id in (select id from ex_rconfig)
                               and exec_configuration_id is not null
                        group by exec_configuration_id,disposition_id""") { row ->
                    execConfigDispCountList.add(execConfigId: row[0], dispositionId: row[1], count: row[2])
                }

                Map execConfigDispCountMap = execConfigDispCountList.groupBy({
                    it.execConfigId
                }).collectEntries { key, val -> [(key): val.collectEntries { [it.dispositionId, it.count] }] }
                sql.withBatch(100, "UPDATE EX_RCONFIG SET disp_counts = :dispCounts WHERE ID = :id", { preparedStatement ->
                    execConfigDispCountMap.each { key, value ->
                        preparedStatement.addBatch(id: key, dispCounts: value ? new JsonBuilder(value).toPrettyString() : null)
                    }
                })
                confirm "Successfully Updated Disp_count values in EX_RCONFIG Table."
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1600239568836-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_pvs_global_tag_global_id')
            }
        }
        createIndex(indexName: "IX_pvs_global_tag_global_id", tableName: "pvs_global_tag") {
            column(name: "global_id")
            column(name: "domain")
        }
    }

    changeSet(author: "ankit (generated)", id: "1600239568836-6") {
        preConditions(onFail: 'MARK_RAN') {
            indexExists(indexName: 'IDX_SINGLE_ALERT_ISNEW')
        }
        dropIndex(indexName: "IDX_SINGLE_ALERT_ISNEW", tableName: "single_case_alert")
    }

    changeSet(author: "ankit (generated)", id: "1600239568836-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'new_counts')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "new_counts", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }

        grailsChange {
            change {
                List<Map> execConfigNewCountList = []
                sql.eachRow("""
                     select exec_config_id,count(id) from single_case_alert 
                        where  exec_config_id in (select id from ex_rconfig) AND is_new = 1
                        group by exec_config_id""") { row ->
                    execConfigNewCountList.add(execConfigId: row[0], count: row[1])
                }

                sql.withBatch(100, "UPDATE EX_RCONFIG SET new_counts = :newCounts WHERE ID = :id", { preparedStatement ->
                    execConfigNewCountList.each {
                        preparedStatement.addBatch(id: it.execConfigId, newCounts: it.count)
                    }
                })
                confirm "Successfully Updated new_count values in EX_RCONFIG Table."
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1600239568836-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'single_alert_id_idx')
            }
        }
        createIndex(indexName: "single_alert_id_idx", tableName: "CASE_HISTORY") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1600239568836-9") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'MASTER_PREF_TERM_ALL')
        }

        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update SINGLE_CASE_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }
    changeSet(author: "amrendra (generated)", id: "1600239568834-13") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK5395n1fvj46kencfit7o01ycm")
        }
        dropForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK5395n1fvj46kencfit7o01ycm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1600239568834-14") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK36oexbhattm8op3hnya3tcgoy")
        }
        dropForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FK36oexbhattm8op3hnya3tcgoy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }


    changeSet(author: "akshat (generated)", id: "1600239568834-2") {
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

    changeSet(author: "akshat (generated)", id: "1600239568834-3") {
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

    changeSet(author: "akshat (generated)", id: "1600239568834-4") {
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

    changeSet(author: "akshat (generated)", id: "1600239568834-10") {
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

    changeSet(author: "amrendra (generated)", id: "1600239568836-11") {
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

    changeSet(author: "amrendra (generated)", id: "1600239568836-12") {
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