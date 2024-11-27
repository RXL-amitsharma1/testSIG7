import com.rxlogix.Constants
import com.rxlogix.config.ExecutedConfiguration

databaseChangeLog = {
    changeSet(author: "vishwas (generated)", id: "13976543213-360") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SHARE_WITH_USER_CONFIG', columnName: 'READ_ONLY')
            }
        }
        addColumn(tableName: "SHARE_WITH_USER_CONFIG") {
            column(name: "READ_ONLY", type: "BOOLEAN"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "vishwas (generated)", id: "13976533213-360") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SHARE_WITH_GROUP_CONFIG', columnName: 'READ_ONLY')
            }
        }
        addColumn(tableName: "SHARE_WITH_GROUP_CONFIG") {
            column(name: "READ_ONLY", type: "BOOLEAN"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "vishwas (generated)", id: "13986340213-360") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_EXCONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_EXCONFIG") {
            column(name: "EXCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USER_IDX", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "READ_ONLY", type: "boolean") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "vishwas (generated)", id: "13891340316-360") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_EXCONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_EXCONFIG") {
            column(name: "EXCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUP_IDX", type: "number(19,0)") {
                constraints(nullable: "true")
            }

            column(name: "READ_ONLY", type: "boolean") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "vishwas (generated)", id: "13809943316-360") {
        grailsChange {
            change {
                ExecutedConfiguration.list().findAll{it.isLatest==true && it.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}.each{
                    ctx.alertService.persistShareWithToExecutedConfig(it)
                }
            }
        }
    }
}



