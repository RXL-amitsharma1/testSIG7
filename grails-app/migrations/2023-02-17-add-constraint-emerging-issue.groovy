databaseChangeLog = {
    changeSet(author: "uddesh teke(generated)", id: "14454519312-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE 
        ADD CONSTRAINT event_selection_json 
        CHECK (EVENT_SELECTION::jsonb IS NOT NULL);
    ''')
    }


    changeSet(author: "uddesh teke(generated)", id: "14454519312-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE
        ADD CONSTRAINT product_selection_json
        CHECK (PRODUCT_SELECTION::jsonb IS NOT NULL);
    ''')
    }

    changeSet(author: "uddesh teke(generated)", id: "14454519312-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_GROUP_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE
        ADD CONSTRAINT event_group_selection_json
        CHECK (EVENT_GROUP_SELECTION::jsonb IS NOT NULL);
    ''')
    }

    changeSet(author: "uddesh teke(generated)", id: "14454519312-4") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_GROUP_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE
        ADD CONSTRAINT product_group_selection_json
        CHECK (PRODUCT_GROUP_SELECTION::jsonb IS NOT NULL);
    ''')
    }

    changeSet(author: "uddesh teke(generated)", id: "14454533858-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVUSER', columnName: 'USER_TYPE')
        }
//      NON-LDAP user should not have null password
        sql('''
        ALTER TABLE PVUSER
        ADD CONSTRAINT PASSWORD_CHECK_NON_LDAP
        CHECK ((USER_TYPE = 'LDAP') OR (PASSWORD IS NOT NULL));
    ''')    }

}