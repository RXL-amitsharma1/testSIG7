databaseChangeLog = {

    changeSet(author: "sakshi (generated)", id: "1712927152814-001") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVS_ALERT_TAG', columnName: 'TAG_TEXT')
        }
        sql("alter table PVS_ALERT_TAG add TAG_TEXT1 VARCHAR2(1020 CHAR)")
        sql("update PVS_ALERT_TAG set TAG_TEXT1=TAG_TEXT")
        sql("alter table PVS_ALERT_TAG drop column  TAG_TEXT")
        sql("alter table PVS_ALERT_TAG rename column TAG_TEXT1 to TAG_TEXT")
    }
    changeSet(author: "sakshi (generated)", id: "1712927152814-002") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVS_ALERT_TAG', columnName: 'SUB_TAG_TEXT')
        }
        sql("alter table PVS_ALERT_TAG add SUB_TAG_TEXT1 VARCHAR2(1020 CHAR)")
        sql("update PVS_ALERT_TAG set SUB_TAG_TEXT1=SUB_TAG_TEXT")
        sql("alter table PVS_ALERT_TAG drop column  SUB_TAG_TEXT")
        sql("alter table PVS_ALERT_TAG rename column SUB_TAG_TEXT1 to SUB_TAG_TEXT")
    }
    changeSet(author: "sakshi (generated)", id: "1712927152814-003") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVS_GLOBAL_TAG', columnName: 'TAG_TEXT')
        }
        sql("alter table PVS_GLOBAL_TAG add TAG_TEXT1 VARCHAR2(1020 CHAR)")
        sql("update PVS_GLOBAL_TAG set TAG_TEXT1=TAG_TEXT")
        sql("alter table PVS_GLOBAL_TAG drop column  TAG_TEXT")
        sql("alter table PVS_GLOBAL_TAG rename column TAG_TEXT1 to TAG_TEXT")
    }
    changeSet(author: "sakshi (generated)", id: "1713156627026-004") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVS_GLOBAL_TAG', columnName: 'SUB_TAG_TEXT')
        }
        sql("alter table PVS_GLOBAL_TAG add SUB_TAG_TEXT1 VARCHAR2(1020 CHAR)")
        sql("update PVS_GLOBAL_TAG set SUB_TAG_TEXT1=SUB_TAG_TEXT")
        sql("alter table PVS_GLOBAL_TAG drop column  SUB_TAG_TEXT")
        sql("alter table PVS_GLOBAL_TAG rename column SUB_TAG_TEXT1 to SUB_TAG_TEXT")
    }

}
