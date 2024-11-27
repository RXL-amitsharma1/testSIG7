databaseChangeLog = {

    changeSet(author: "Amrendra (generated)", id: "1698745898110-11") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'spotfire_notification_query', columnName: 'signal_parameters')
        }

        addColumn(tableName: "spotfire_notification_query") {
            column(name: "signal_parameters_copy", type: "CLOB")
        }

        sql("update spotfire_notification_query set signal_parameters_copy = signal_parameters;")

        dropColumn(tableName: "spotfire_notification_query", columnName: "signal_parameters")

        renameColumn(tableName: "spotfire_notification_query", oldColumnName: "signal_parameters_copy", newColumnName: "signal_parameters")

    }
    changeSet(author: "rahul (generated)", id: "1701686299-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName:"NAME")
        }
        modifyDataType(columnName: "NAME", newDataType: "varchar2(255 char)", tableName: "EX_LITERATURE_CONFIG")
    }

}