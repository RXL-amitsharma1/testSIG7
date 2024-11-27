databaseChangeLog = {
    changeSet(author: "Rishabh Rajpurohit", id: "13072023051204-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ADVANCED_FILTER', columnName: 'DESCRIPTION')
        }
        sql("alter table ADVANCED_FILTER modify DESCRIPTION VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104316-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_FILE_PROCESS_LOG', columnName: 'DESCRIPTION')
        }
        sql("alter table EVDAS_FILE_PROCESS_LOG modify DESCRIPTION VARCHAR2(4000 CHAR);")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104902-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_NOTIFICATION_MEMO', columnName: 'EMAIL_BODY')
        }
        sql("alter table SIGNAL_NOTIFICATION_MEMO modify EMAIL_BODY VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Rishabh Goswami", id: "040820231049123-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ATTACHMENT_REFERENCE', columnName: 'NAME')
        }
        sql("alter table ATTACHMENT_REFERENCE modify NAME VARCHAR2(4000 CHAR);")
    }
}