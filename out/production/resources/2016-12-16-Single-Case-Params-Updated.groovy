databaseChangeLog = {

    changeSet(author: "chetansharma (generated)", id: "1481899396701-1") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "product_name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1481899396701-2") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "pt", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
    }

}
