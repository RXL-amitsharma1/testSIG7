databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "19976543213-354") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE DROP CONSTRAINT IF EXISTS event_selection_json;
        ALTER TABLE EMERGING_ISSUE 
        ADD CONSTRAINT event_selection_json 
        CHECK (EVENT_SELECTION IS NULL OR EVENT_SELECTION::jsonb IS NOT NULL);
    ''')
    }


    changeSet(author: "Gaurav (generated)", id: "19976543213-355") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE DROP CONSTRAINT IF EXISTS product_selection_json;
        ALTER TABLE EMERGING_ISSUE
        ADD CONSTRAINT product_selection_json
        CHECK (PRODUCT_SELECTION IS NULL OR PRODUCT_SELECTION::jsonb IS NOT NULL);
    ''')
    }

    changeSet(author: "Gaurav (generated)", id: "19976543213-356") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_GROUP_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE DROP CONSTRAINT IF EXISTS event_group_selection_json;
        ALTER TABLE EMERGING_ISSUE
        ADD CONSTRAINT event_group_selection_json
        CHECK (EVENT_GROUP_SELECTION IS NULL OR EVENT_GROUP_SELECTION::jsonb IS NOT NULL);
    ''')
    }

    changeSet(author: "Gaurav (generated)", id: "19976543213-357") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_GROUP_SELECTION')
        }
        sql('''
        ALTER TABLE EMERGING_ISSUE DROP CONSTRAINT IF EXISTS product_group_selection_json;
        ALTER TABLE EMERGING_ISSUE
        ADD CONSTRAINT product_group_selection_json
        CHECK (PRODUCT_GROUP_SELECTION IS NULL OR PRODUCT_GROUP_SELECTION::json IS NOT NULL);
    ''')
    }


}