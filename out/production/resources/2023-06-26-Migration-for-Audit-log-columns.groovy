import com.rxlogix.AggregateCaseAlertService
import com.rxlogix.UserRoleService
import com.rxlogix.UserService
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "yogesh (generated)", id: "16789593232383565-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'USER_IP_ADDRESS')
            }
        }
        addColumn(tableName: "audit_log") {
            column(name: "USER_IP_ADDRESS", type: "varchar2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1683184030211-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'TRANSACTION_ID')
            }
        }
        sql("alter table AUDIT_LOG  add TRANSACTION_ID VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1683184030211-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'IS_FIRST_ENTRY_IN_TRANSACTION')
            }
        }
        sql("alter table AUDIT_LOG  add IS_FIRST_ENTRY_IN_TRANSACTION NUMBER(1);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1683184030211-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'MODULE_NAME')
            }
        }
        sql("alter table AUDIT_LOG  add MODULE_NAME VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1683184030211-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'DEVICE')
            }
        }
        sql("alter table AUDIT_LOG  add DEVICE VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1683184030211-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'BROWSER')
            }
        }
        sql("alter table AUDIT_LOG  add BROWSER VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1683184030211-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'TIME_ZONE')
            }
        }
        sql("alter table AUDIT_LOG  add TIME_ZONE VARCHAR2(255);")
    }
    changeSet(author: "Uddesh Teke (generated)", id: "1683184030211-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'DESCRIPTION')
            }
        }
        sql("alter table AUDIT_LOG  add DESCRIPTION VARCHAR2(255);")
    }
    changeSet(author: "Uddesh Teke (generated)", id: "1683184030212-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'SECTION_CHILD_MODULE')
            }
        }
        sql("alter table AUDIT_LOG  add SECTION_CHILD_MODULE NUMBER(1) default 0;")
        sql("update AUDIT_LOG  set SECTION_CHILD_MODULE = 0 where SECTION_CHILD_MODULE is null;")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1683184030212-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'TEMPLT_QUERY', columnName: 'PARAMETER_VALUE_AUDIT_STRING')
            }
        }
        sql("alter table TEMPLT_QUERY  add PARAMETER_VALUE_AUDIT_STRING varchar2(32000);")
    }
    changeSet(author: "Yogesh (generated)", id: "1689569106455-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMS', columnName: 'SIGNAL_ID')
            }
        }
        sql("alter table SIGNAL_RMMS  add SIGNAL_ID NUMBER(19);")
    }

    changeSet(author: "Yogesh (generated)", id: "1689569106455-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'USER_ROLES_STRING')
            }
        }
        sql("alter table PVUSER  add USER_ROLES_STRING varchar2(32000);")
    }



    changeSet(author: "Yogesh (generated)", id: "1689569106455-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'SIGNAL_PIN_CONFIGS')
            }
        }
        sql("alter table PVUSER  add SIGNAL_PIN_CONFIGS varchar2(8000);")
    }

    changeSet(author: "Yogesh (generated)", id: "1689569106455-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'USER_PIN_CONFIGS')
            }
        }
        sql("alter table PVUSER  add USER_PIN_CONFIGS varchar2(8000);")
    }

    changeSet(author: "yogesh (generated)", id: "1689569106455-7") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVUSER', columnName: 'USER_PIN_CONFIGS')
        }
        grailsChange {
            change {
                try {
                    List<User> userList=User.list()
                    userList.each{
                        UserService userService = ctx.userService
                        it.userPinConfigs=userService.getUserPinConfigs(it)
                        it.save(flush:true)
                    }

                } catch (Exception e) {
                    println("########## Some error occurred while saving user roles string in PVUSER table#############")
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1689569106457-11") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVUSER', columnName: 'USER_ROLES_STRING')
        }
        grailsChange {
            change {
                try {
                    List<User> userList=User.list()
                    UserService userService = ctx.userService
                    userService.saveUserRolesString(userList)

                } catch (Exception e) {
                    println("########## Some error occurred while saving user roles string in PVUSER table#############")
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "Uddesh (generated)", id: "16895691064313-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AUDIT_LOG', columnName: 'DESCRIPTION')
        }
        sql("alter table AUDIT_LOG add TMPDESCRIPTION CLOB;")
        sql("UPDATE AUDIT_LOG SET TMPDESCRIPTION=DESCRIPTION;")
        sql("ALTER TABLE AUDIT_LOG DROP COLUMN DESCRIPTION;")
        sql("ALTER TABLE AUDIT_LOG RENAME COLUMN TMPDESCRIPTION TO DESCRIPTION;")
    }

    changeSet(author: "Uddesh (generated)", id: "16895691064313-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AUDIT_LOG', columnName: 'ENTITY_VALUE')
        }
        sql("alter table AUDIT_LOG add TMP_ENTITY_VALUE CLOB;")
        sql("UPDATE AUDIT_LOG SET TMP_ENTITY_VALUE=ENTITY_VALUE;")
        sql("ALTER TABLE AUDIT_LOG DROP COLUMN ENTITY_VALUE;")
        sql("ALTER TABLE AUDIT_LOG RENAME COLUMN TMP_ENTITY_VALUE TO ENTITY_VALUE;")
    }


}