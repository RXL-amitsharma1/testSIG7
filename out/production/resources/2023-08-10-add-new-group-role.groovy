databaseChangeLog = {
    changeSet( author: "Krishan Joshi(generated)", id: "1692852425643-1" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            not {
                tableExists( tableName: 'USER_GROUP_ROLE' )
            }
        }
        createTable( tableName: "USER_GROUP_ROLE" ) {
            column( name: "role_id", type: "NUMBER(19, 0)" ) {
                constraints( nullable: "false" )
            }

            column( name: "user_group_id", type: "NUMBER(19, 0)" ) {
                constraints( nullable: "false" )
            }
        }
    }

    changeSet( author: "Krishan Joshi (generated)", id: "1692852425643-2" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'GROUPS', columnName: 'NAME' )
        }
        sql( "alter table GROUPS modify NAME VARCHAR2(255 CHAR);" )
    }



    changeSet( author: "Krishan Joshi (generated)", id: "16940906286300-3" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'GROUPS', columnName: 'NAME' )
        }
        sql( "alter table GROUPS DROP UNIQUE (NAME);" )
    }

    changeSet(author: "Krishna (generated)", id: "1694590699331-06") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ROLE', columnName: 'AUTHORITY_DISPLAY')
            }
        }
        addColumn(tableName: "ROLE") {
            column(name: "AUTHORITY_DISPLAY", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "Krishna Joshi(generated)", id: "1694697038175-07") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'GROUP_ROLES')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "GROUP_ROLES", type: "VARCHAR2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishna Joshi(generated)", id: "1694697038175-09") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'GROUP_USERS')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "GROUP_USERS", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet( author: "Krishan Joshi (generated)", id: "1692852425643-10" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'GROUPS', columnName: 'NAME' )
        }
        sql( "alter table GROUPS modify NAME VARCHAR2(550 CHAR);" )
    }


    changeSet( author: "Krishan Joshi (generated)", id: "1692852425643-12" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'GROUPS', columnName: 'DESCRIPTION' )
        }
        sql( "alter table GROUPS modify DESCRIPTION VARCHAR2(8000 CHAR);" )
    }

    changeSet( author: "Krishan Joshi (generated)", id: "1692852425643-13" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'GROUPS', columnName: 'JUSTIFICATION_TEXT' )
        }
        sql( "alter table GROUPS modify JUSTIFICATION_TEXT VARCHAR2(8000 CHAR);" )
    }


    changeSet(author: "Krishna Joshi(generated)", id: "1694697038175-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'IS_DEFAULT')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "IS_DEFAULT", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet( author: "Krishna Joshi(generated)", id: "1694697038175-11" ) {
        grailsChange {
            change {
                ctx.userGroupService.updateGroupDefaultParameter(  )
            }
        }
    }

}
