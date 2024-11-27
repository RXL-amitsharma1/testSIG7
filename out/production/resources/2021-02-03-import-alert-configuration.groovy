import com.rxlogix.config.Configuration

databaseChangeLog = {
    changeSet(author: "Nikhil (generated)", id: "198373278233-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_TEMPLATE_ALERT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_TEMPLATE_ALERT", type: "BOOLEAN"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nikhil (generated)", id: "198373278233-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'CONFIGURATION_TEMPLATE_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "CONFIGURATION_TEMPLATE_ID", type: "NUMBER(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Nikhil (generated)", id: "198373278233-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'IMPORT_CONFIGURATION_LOG')
            }
        }
        createTable(tableName: "IMPORT_CONFIGURATION_LOG") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "IMPORT_CONFIGURATION_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "import_file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
            column(name: "import_log_file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "imported_by_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "imported_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }
            column(name: "status", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "Nikhil (generated)", id: "198373278233-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'IMPORT_CONFIGURATION_LOG', columnName: 'import_conf_type')
            }
        }
        addColumn(tableName: "IMPORT_CONFIGURATION_LOG") {
            column(name: "import_conf_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "198373278233-26") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'RCONFIG')
        }
        grailsChange {
            change {
                List<Configuration> configurationList = Configuration.list()
                configurationList.each {
                    if(it.scheduleDateJSON && it.nextRunDate){
                        it.isEnabled = true
                        ctx.CRUDService.save(it)
                    }
                }
            }
        }
    }

}