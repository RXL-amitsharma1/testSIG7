databaseChangeLog = {

    changeSet(author: "Shivam (generated)", id: "1689922142218-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'CASE_NARRATIVE_CONFIG')
            }
        }

        createTable(tableName: "CASE_NARRATIVE_CONFIG") {
            column(name: "id", type: "number(19,0)") {
                constraints(primaryKey: "true", primaryKeyName: "CASE_NARRATIVE_CONFIGPK")
            }
            column(name: "EXPORT_ALWAYS", type: "boolean", defaultValue: "false") {
                constraints(nullable: "false")
            }
            column(name: "PROMPT_USER", type: "boolean", defaultValue: "false") {
                constraints(nullable: "false")
            }
        }
    }
}