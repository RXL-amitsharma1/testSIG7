import com.rxlogix.GroupService
databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "1997654321337-347") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_eventname_PEH')
            }
        }
        createIndex(indexName: "idx_eventname_PEH", tableName: "product_event_history") {
            column(name: "event_name")
        }
    }

    changeSet(author: "yogesh (generated)", id: "1714653924159-5") {
        //This migration is written to resolve conflicts of groups user due to migration not written at the time of column addition
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'GROUPS', columnName: 'GROUP_USERS')
        }
        grailsChange {
            change {
                try {
                    GroupService groupService = ctx.getBean("groupService")
                    groupService.updateGroupUsersString()
                } catch (Exception e) {
                    println("########## Some error occurred while saving group usera string in GROUPS table#############")
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "Yogesh (generated)", id: "1714653924159-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_audit_transxn_entity')
            }
        }
        createIndex(indexName: "idx_audit_transxn_entity", tableName: "AUDIT_LOG") {
            column(name: "TRANSACTION_ID")
            column(name: "ENTITY_NAME")
            column(name: "ENTITY_ID")
        }
    }


    changeSet(author: "Yogesh (generated)", id: "1714653924159-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'audit_date_create_idx')
            }
        }
        createIndex(indexName: "audit_date_create_idx", tableName: "AUDIT_LOG") {
            column(name: "DATE_CREATED")
        }
    }
    changeSet(author: "Amrendra (generated)", id: "1714653924159-11") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'update_time')
            }
        }
        addColumn(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "update_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }


}