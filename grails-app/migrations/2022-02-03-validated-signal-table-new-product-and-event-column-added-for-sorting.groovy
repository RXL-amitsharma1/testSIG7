import com.rxlogix.ViewInstanceService
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.AlertService
import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "Hritik Chaudhary (generated)", id: "1675329550-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'PRODUCTS_AND_GROUP_COMBINATION')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "PRODUCTS_AND_GROUP_COMBINATION", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Hritik Chaudhary (generated)", id: "1675329550-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'EVENTS_AND_GROUP_COMBINATION')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "EVENTS_AND_GROUP_COMBINATION", type: "clob") {
                constraints(nullable: "true")
            }
        }

    }

 changeSet(author: "Yogesh (generated)", id: "1675425900210-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_sin_global_id')
            }
        }
        createIndex(indexName: "idx_sin_global_id", tableName: "single_case_alert", unique: "false") {
            column(name: "global_identity_id")
        }
    }

}