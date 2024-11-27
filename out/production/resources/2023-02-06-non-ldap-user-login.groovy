import com.rxlogix.enums.UserType
import com.rxlogix.user.User
import grails.util.Holders
import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "Bhupender", id: "2023012314500102-1") {
        addColumn(tableName: "PVUSER") {
            column(name: "USER_TYPE", type: "varchar2(255 char)")
        }
    }
    changeSet(author: "Bhupender", id: "2023012314500102-2") {
        createTable(tableName: "pvuser_password_digests") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "password_digests_string", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "Bhupender", id: "2023012314500102-3") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_password_digests", constraintName: "FKPVUSER_PASSWORDDIGEST", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER")
    }

    changeSet(author: "Bhupender", id: "2023012314500102-4") {
        addColumn(tableName: "PVUSER") {
            column(name: "PASSWORD_MODIFIED_TIME", type: "TIMESTAMP")
        }
    }

    changeSet(author: "Bhupender", id: "2023012314500102-5") {
        addColumn(tableName: "PVUSER") {
            column(name: "PASSWORD", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "Bhupender", id: "2023012314500102-07") {
        grailsChange {
            change {
                Sql sql=null
                try {
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE PVUSER SET USER_TYPE = 'LDAP' WHERE USERNAME != 'pvs_user'")
                }catch(Exception ex){
                    println "#### Error while updating User for LDAP configuration"
                    println (ex.getMessage())
                }finally{
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "Bhupender", id: "2023012314500102-08") {
        addNotNullConstraint(tableName: "PVUSER" , columnName: "USER_TYPE")
    }

}