databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "37283791301-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_sharewithuser_configid')
            }
        }
        createIndex(indexName: "idx_sharewithuser_configid", tableName: "SHARE_WITH_USER_CONFIG") {
            column(name: "config_id")
        }

    }

}