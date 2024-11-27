databaseChangeLog = {
    changeSet(author: "Uddesh Teke (generated)", id: "20240821173354-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_agg_on_demand_exconfig')
            }
        }
        createIndex(indexName: "idx_agg_on_demand_exconfig", tableName: "agg_on_demand_alert", unique: "false") {
            column(name: "exec_configuration_id")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_on_demand_exconfig')
            }
        }
        createIndex(indexName: "idx_single_on_demand_exconfig", tableName: "single_on_demand_alert", unique: "false") {
            column(name: "exec_config_id")
        }
    }
}