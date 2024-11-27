databaseChangeLog = {
    changeSet(author: "Farhan (generated)", id: "14816215872894-1") {
        addColumn(tableName: "PVUSER") {
            column(name: "SCIM_ID", type: "varchar2(255 char)")
        }
    }
}