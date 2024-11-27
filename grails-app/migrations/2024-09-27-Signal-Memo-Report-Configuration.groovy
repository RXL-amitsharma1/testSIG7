databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1728457713439-001") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'SIGNAL_MEMO_FLAG_FOR_UNDO_DIS')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "SIGNAL_MEMO_FLAG_FOR_UNDO_DIS", type: "boolean", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "hemlata (generated)", id: "1729678648980-001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'DISPOSITION_IDS_MEMO')
            }
        }
        createTable(tableName: "DISPOSITION_IDS_MEMO") {
            column(name: "SIGNAL_NOTIFICATION_MEMO_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_IDS", type: "NUMBER(19, 0)")

            column(name: "dispositions_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "Hemlata", id: "1728377664312-001") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_EMAIL_LOG', columnName: 'BODY')
        }
        sql("ALTER TABLE SIGNAL_EMAIL_LOG ALTER COLUMN BODY TYPE TEXT;")
    }
    changeSet(author: "hemlata (generated)", id: "1729847442257-01") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'signal_rmms' and column_name = 'status';")
        }
        sql('ALTER TABLE signal_rmms ALTER COLUMN status TYPE VARCHAR(255);')
        sql('ALTER TABLE signal_rmms ALTER COLUMN status DROP NOT NULL;')
    }

    changeSet(author: "hemlata (generated)", id: "1729847442257-02") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'signal_rmms' AND column_name = 'due_date';")
        }
        sql('ALTER TABLE signal_rmms ALTER COLUMN due_date TYPE TIMESTAMP(6);')
        sql('ALTER TABLE signal_rmms ALTER COLUMN due_date DROP NOT NULL;')
    }
}