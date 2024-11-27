databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1695734014790-10") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD';")
        }
        sql("delete from PVUSERS_ROLES where role_id in (select id FROM role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD');")
        sql("delete from USER_GROUP_ROLE where role_id in (select id FROM role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD');")
        sql("delete from role WHERE AUTHORITY = 'ROLE_SIGNAL_DASHBOARD';")
    }
    changeSet(author: "Hritik (generated)", id: "1698825606-1") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'business_configuration' AND column_name = 'description';")
        }
        sql("alter table BUSINESS_CONFIGURATION ALTER COLUMN DESCRIPTION TYPE VARCHAR(8000 );")
    }
}