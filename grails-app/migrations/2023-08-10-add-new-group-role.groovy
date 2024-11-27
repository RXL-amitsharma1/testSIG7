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
        sql( "alter table GROUPS ALTER COLUMN NAME TYPE VARCHAR(255 );" )
    }


    changeSet(author: "Krishan Joshi (generated)", id: "16940906286300-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'GROUPS', columnName: 'NAME')
        }
        sql('''
        ALTER TABLE GROUPS
        DROP CONSTRAINT uc_groupsname_col;
    ''')
    }

    changeSet(author: "Krishna (generated)", id: "1694590699331-06") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ROLE', columnName: 'AUTHORITY_DISPLAY')
            }
        }
        addColumn(tableName: "ROLE") {
            column(name: "AUTHORITY_DISPLAY", type: "VARCHAR(255 )") {
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
            column(name: "GROUP_ROLES", type: "VARCHAR(8000 )") {
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
        sql( "alter table GROUPS ALTER COLUMN NAME TYPE VARCHAR(550 );" )
    }


    changeSet( author: "Krishan Joshi (generated)", id: "1692852425643-12" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'GROUPS', columnName: 'DESCRIPTION' )
        }
        sql( "alter table GROUPS ALTER COLUMN DESCRIPTION TYPE VARCHAR(8000 );" )
    }

    changeSet( author: "Krishan Joshi (generated)", id: "1692852425643-13" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'GROUPS', columnName: 'JUSTIFICATION_TEXT' )
        }
        sql( "alter table GROUPS ALTER COLUMN JUSTIFICATION_TEXT TYPE VARCHAR(8000 );" )
    }


    changeSet(author: "Krishna Joshi(generated)", id: "1694697038175-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'IS_DEFAULT')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "IS_DEFAULT", type: "boolean") {
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
