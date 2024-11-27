databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "19976543213-346") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'archived_agg_alert', columnName: 'Trend_Flag')
        }
        sql("ALTER TABLE archived_agg_alert RENAME COLUMN \"Trend_Flag\" TO TREND_FLAG;")
    }

    changeSet(author: "Gaurav (generated)", id: "19976543213-347") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'agg_alert', columnName: 'Trend_Flag')
        }
        sql("ALTER TABLE agg_alert RENAME COLUMN \"Trend_Flag\" TO TREND_FLAG;")
    }
}