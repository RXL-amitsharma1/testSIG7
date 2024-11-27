databaseChangeLog = {
    changeSet(author: "Rahul (generated)", id: "1720594702-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'agg_alert' AND column_name = 'product_id'")
            }
        }
        sql("alter table AGG_ALERT alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-02") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'archived_agg_alert' AND column_name = 'product_id'")
            }
        }
        sql("alter table ARCHIVED_AGG_ALERT alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-03") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'agg_on_demand_alert' AND column_name = 'product_id'")
            }
        }
        sql("alter table AGG_ON_DEMAND_ALERT alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-04") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'single_case_alert' AND column_name = 'product_id'")
            }
        }
        sql("alter table SINGLE_CASE_ALERT alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-05") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'archived_single_case_alert' AND column_name = 'product_id'")
            }
        }
        sql("alter table ARCHIVED_SINGLE_CASE_ALERT alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-06") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'single_on_demand_alert' AND column_name = 'product_id'")
            }
        }
        sql("alter table SINGLE_ON_DEMAND_ALERT alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-07") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'alert_comment' AND column_name = 'product_id'")
            }
        }
        sql("alter table ALERT_COMMENT alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-08") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'product_event_history' AND column_name = 'product_id'")
            }
        }
        sql("alter table PRODUCT_EVENT_HISTORY alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }
    changeSet(author: "Rahul (generated)", id: "1720594702-09") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'PRODUCT_ID')
            not {
                sqlCheck(expectedResult: 'numeric', "SELECT data_type FROM information_schema.columns WHERE table_name = 'alert_comment_history' AND column_name = 'product_id'")
            }
        }
        sql("alter table ALERT_COMMENT_HISTORY alter column PRODUCT_ID TYPE NUMERIC(38, 0);")
    }

}