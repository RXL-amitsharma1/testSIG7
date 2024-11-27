import com.rxlogix.Constants
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1614160008436-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'case_id')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "case_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'data_source')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "data_source", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'follow_up_num')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "follow_up_num", type: "number(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-55") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'sync_flag')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "sync_flag", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'version_num')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "version_num", type: "number(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-60") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'ex_config_id')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "ex_config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-61") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'alert_name')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "alert_name", type: "varchar2(200 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-7") {
        preConditions(onFail: 'MARK_RAN') {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'COMMENTS')
        }

        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "COMMENTS_COPY", type: "CLOB")
        }

        sql("update ALERT_COMMENT set COMMENTS_COPY = COMMENTS;")

        dropColumn(tableName: "ALERT_COMMENT", columnName: "COMMENTS")

        renameColumn(tableName: "ALERT_COMMENT", oldColumnName: "COMMENTS_COPY", newColumnName: "COMMENTS")

    }

    changeSet(author: "amrendra (generated)", id: "1614160008436-8-4") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT', columnName: 'data_source')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''UPDATE alert_comment ac
                    SET (data_source, alert_name) = (SELECT config.selected_data_source, config.name
                         FROM rconfig config
                        WHERE ac.config_id = config.id 
                        )
                     WHERE EXISTS (
                        SELECT 1
                          FROM rconfig config
                         WHERE ac.config_id = config.id
                        and ac.alert_type = 'Single Case Alert'
                        and (ac.sync_flag is null or ac.sync_flag = 0)
                        )  ''')

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating alert_comment table. #############")
                }

            }
        }
    }

}
