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
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104906-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RULE_INFORMATION', columnName: 'JUSTIFICATION_TEXT')
        }
        sql("alter table RULE_INFORMATION modify JUSTIFICATION_TEXT VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104907-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'BUSINESS_CONFIGURATION', columnName: 'DESCRIPTION')
        }
        sql("alter table BUSINESS_CONFIGURATION modify DESCRIPTION VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104908-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'MEETING', columnName: 'MEETING_AGENDA')
        }
        sql("alter table MEETING modify MEETING_AGENDA VARCHAR2(8000 CHAR);")
    }
}