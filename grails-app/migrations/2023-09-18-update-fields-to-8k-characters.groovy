databaseChangeLog = {
    changeSet(author: "Rishabh Rajpurohit", id: "13072023051204-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ADVANCED_FILTER', columnName: 'DESCRIPTION')
        }
        sql("alter table ADVANCED_FILTER ALTER COLUMN DESCRIPTION TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104316-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_FILE_PROCESS_LOG', columnName: 'DESCRIPTION')
        }
        sql("alter table EVDAS_FILE_PROCESS_LOG ALTER COLUMN DESCRIPTION TYPE VARCHAR(4000 );")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104902-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_NOTIFICATION_MEMO', columnName: 'EMAIL_BODY')
        }
        sql("alter table SIGNAL_NOTIFICATION_MEMO ALTER COLUMN EMAIL_BODY TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104906-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RULE_INFORMATION', columnName: 'JUSTIFICATION_TEXT')
        }
        sql("alter table RULE_INFORMATION ALTER COLUMN JUSTIFICATION_TEXT TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104907-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'BUSINESS_CONFIGURATION', columnName: 'DESCRIPTION')
        }
        sql("alter table BUSINESS_CONFIGURATION ALTER COLUMN DESCRIPTION TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Rishabh Rajpurohit", id: "14072023104908-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'MEETING', columnName: 'MEETING_AGENDA')
        }
        sql("alter table MEETING ALTER COLUMN MEETING_AGENDA TYPE VARCHAR(8000 );")
    }
}