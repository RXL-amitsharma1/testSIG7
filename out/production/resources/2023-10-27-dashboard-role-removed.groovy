databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1695734014790-10") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'ROLE_SIGNAL_DASHBOARD', "SELECT AUTHORITY FROM role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD';")
        }
        sql("delete from PVUSERS_ROLES where role_id in (select id FROM role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD');")
        sql("delete from USER_GROUP_ROLE where role_id in (select id FROM role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD');")
        sql("delete from role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD';")
    }
    changeSet(author: "Hritik (generated)", id: "1698825606-1") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'DESCRIPTION' AND TABLE_NAME = 'BUSINESS_CONFIGURATION';")
        }
        sql("alter table BUSINESS_CONFIGURATION modify DESCRIPTION VARCHAR2(8000 CHAR);")
    }
}