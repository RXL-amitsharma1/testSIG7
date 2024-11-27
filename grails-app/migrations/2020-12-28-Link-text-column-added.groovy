databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "15991974231017-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_DOCUMENT', columnName: 'link_text')
            }
        }

        addColumn(tableName: "ALERT_DOCUMENT") {
            column(name: "link_text", type: "VARCHAR(4000 )") {
                constraints(nullable: "true")
            }
        }
    }
}