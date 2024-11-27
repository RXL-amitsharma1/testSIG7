databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "15991974231017-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_DOCUMENT', columnName: 'link_text')
            }
        }

        addColumn(tableName: "ALERT_DOCUMENT") {
            column(name: "link_text", type: "varchar2(4000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
}