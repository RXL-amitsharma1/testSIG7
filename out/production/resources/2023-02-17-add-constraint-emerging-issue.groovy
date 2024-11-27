databaseChangeLog = {
    changeSet(author: "uddesh teke(generated)", id: "14454519312-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add constraint event_selection_json check ( EVENT_SELECTION is json );")
    }

    changeSet(author: "uddesh teke(generated)", id: "14454519312-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add constraint product_selection_json check ( PRODUCT_SELECTION is json );")
    }

    changeSet(author: "uddesh teke(generated)", id: "14454519312-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_GROUP_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add constraint event_group_selection_json check ( EVENT_GROUP_SELECTION is json );")
    }

    changeSet(author: "uddesh teke(generated)", id: "14454519312-4") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_GROUP_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add constraint product_group_selection_json check ( PRODUCT_GROUP_SELECTION is json );")
    }

    changeSet(author: "uddesh teke(generated)", id: "14454533858-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVUSER', columnName: 'USER_TYPE')
        }
//      NON-LDAP user should not have null password
        sql("alter table PVUSER add constraint PASSWORD_CHECK_NON_LDAP check ( (USER_TYPE = 'LDAP') OR (PASSWORD IS NOT NULL) );")
    }

}