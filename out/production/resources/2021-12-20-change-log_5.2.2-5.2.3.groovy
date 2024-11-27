databaseChangeLog =

        {
            changeSet(author: "rishabh-goswami", id: "0912201905111999-07") {

                preConditions(onFail: 'MARK_RAN') {
                    sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'JUSTIFICATION' AND TABLE_NAME = 'JUSTIFICATION';")
                }
                addColumn(tableName: "JUSTIFICATION") {
                    column(name: "JUSTIFICATION_2", type: "VARCHAR2(4000 CHAR)")
                }

                sql("update JUSTIFICATION set JUSTIFICATION_2 = JUSTIFICATION;")
                sql("alter table JUSTIFICATION drop column JUSTIFICATION;")
                sql("alter table JUSTIFICATION rename column JUSTIFICATION_2 to JUSTIFICATION;")
            }
        }