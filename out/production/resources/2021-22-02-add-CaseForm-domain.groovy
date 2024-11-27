databaseChangeLog = {
    changeSet(author: "rxlogix (generated)", id: "1613989815343-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'case_form')
            }
        }
        createTable(tableName: "case_form") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_ids", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "case_numbers", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "executed_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_num", type: "CLOB")

            column(name: "form_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_duplicate", type: "CLOB")

            column(name: "version_num", type: "CLOB")

            column(name: "advanced_filter_name", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "true")
            }

            column(name: "view_instance_name", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "true")
            }

            column(name: "is_full_case_series", type: "number(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxlogix (generated)", id: "1613989815343-3") {
        createSequence(sequenceName: "case_form_sequence")
    }

    changeSet(author: "rxlogix (generated)", id: "1613989815343-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'case_form', columnName: 'saved_name')
            }
        }
        addColumn(tableName: "case_form") {
            column(name: "saved_name", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1613989815344-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'case_form', columnName: 'case_ids')
        }
        sql("alter table case_form modify (case_ids NULL);")
    }

    changeSet(author: "nitesh (generated)", id: "1613989815344-4") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'case_form', columnName: 'case_numbers')
        }
        sql("alter table case_form modify (case_numbers NULL);")
    }

    changeSet(author: "rishabh (generated)", id: "11617101827367-188") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(1) from ex_rconfig where is_case_series = 1")
            }
        }
        grailsChange {
            change {
                try {
                    ctx.getBean("alertService").updateIsTempCaseSeries(true,true,null,null)
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating EX_rconfig. #############")
                }
            }
        }

    }

    changeSet(author: "rishabh (generated)", id: "11617101827367-189") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(1) from ex_rconfig")
            }
        }
        grailsChange {
            change {
                try {
                    ctx.getBean("alertService").updateIsTempCaseSeries(false,false,null,null)
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating EX_rconfig. #############")
                }
            }
        }
    }


}
