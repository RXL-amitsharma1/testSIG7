databaseChangeLog =

        {
            changeSet(author: "rishabh-goswami", id: "0912201905111999-07") {

                preConditions(onFail: 'MARK_RAN') {
                    sqlCheck(expectedResult: '255', "SELECT character_maximum_length FROM information_schema.columns WHERE column_name = 'justification' AND table_name = 'justification'")
                }
                addColumn(tableName: "JUSTIFICATION") {
                    column(name: "JUSTIFICATION_2", type: "VARCHAR(4000 )")
                }

                sql("update JUSTIFICATION set JUSTIFICATION_2 = JUSTIFICATION;")
                sql("alter table JUSTIFICATION drop column JUSTIFICATION;")
                sql("alter table JUSTIFICATION rename column JUSTIFICATION_2 to JUSTIFICATION;")
            }
        }