databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "1677128577357-111") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_text1')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_text1", type: "VARCHAR2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Amrendra (generated)", id: "1677128577357-121") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_text2')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_text2", type: "VARCHAR2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Amrendra (generated)", id: "1677128577357-131") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_date1')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_date1", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Amrendra (generated)", id: "1677128577357-141") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_date2')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_date2", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Amrendra (generated)", id: "1677128577357-151") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_dropdown1')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_dropdown1", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Amrendra (generated)", id: "1677128577357-161") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_dropdown2')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_dropdown2", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1677128577357-171") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'dd_value1')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "dd_value1", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Amrendra (generated)", id: "1677128577357-181") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'dd_value2')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "dd_value2", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }



    changeSet(author: "Amrendra (generated)", id: "1677128577357-221") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'dynamic_fields')
            }
        }
        createTable(tableName: "dynamic_fields") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "dynamic_fieldPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "field_name", type: "VARCHAR2(255)") {
                constraints(nullable: "false")
            }

            column(name: "field_label", type: "VARCHAR2(255)") {
                constraints(nullable: "false")
            }

            column(name: "field_type", type: "VARCHAR2(255)") {
                constraints(nullable: "false")
            }

            column(name: "sequence", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1677128577357-241") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'dynamic_dropdown_values')
            }
        }
        createTable(tableName: "dynamic_dropdown_values") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "dynamic_ddvaluePK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "field_name", type: "VARCHAR2(255)") {
                constraints(nullable: "false")
            }

            column(name: "field_key", type: "VARCHAR2(255)") {
                constraints(nullable: "false")
            }

            column(name: "field_value", type: "VARCHAR2(255)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1677128577357-271") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'rconfig', columnName: 'is_standalone')
            }
        }
        addColumn(tableName: "rconfig") {
            column(name: "is_standalone", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1677128577357-281") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ex_rconfig', columnName: 'is_standalone')
            }
        }
        addColumn(tableName: "ex_rconfig") {
            column(name: "is_standalone", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }



}
