import com.rxlogix.CRUDService
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.PriorityDispositionConfig
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Preference
import grails.converters.JSON
import org.joda.time.DateTime
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.util.Holders
import com.rxlogix.Constants


databaseChangeLog = {

    changeSet(author: "suraj (generated)", id: "1630661135578-09") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'REFERENCE_DETAILS')
            }
        }
        createTable(tableName: "REFERENCE_DETAILS") {


            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "REFERENCES_PK")
            }

            column(name: "attachment_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(4000 CHAR)")
            column(name: "is_deleted", type: "number(1,0)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "suraj (generated)", id: "1630847171273-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ATTACHMENT_REFERENCE')
            }
        }
        createTable(tableName: "ATTACHMENT_REFERENCE") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "attachment_reference_PK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "content_type", type: "varchar2(255 char)")

            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "ext", type: "varchar2(255 char)")


            column(name: "length", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "poster_class", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "poster_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "saved_name", type: "varchar2(255 char)")
            column(name: "reference_type", type: "varchar2(255 char)")
            column(name: "reference_link", type: "varchar2(4000 char)")
            column(name: "input_name", type: "varchar2(4000 char)") {
                constraints(nullable: "false")
            }
            column(name: "attachment_type", type: "varchar2(4000 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "suraj (generated)", id: "1631092000423-177") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_REFERENCES')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_REFERENCES") {
            column(name: "REFERENCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USER_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")

        }

    }
    //1631166037538
    changeSet(author: "suraj (generated)", id: "1631166037538-83") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'user_reference_fk_constraint')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USER_ID", baseTableName: "SHARE_WITH_USER_REFERENCES", constraintName: "user_reference_fk_constraint", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "suraj (generated)", id: "1631092000423-189") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_REFERENCES')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_REFERENCES") {
            column(name: "REFERENCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")

        }

    }
    changeSet(author: "suraj (generated)", id: "1631166461614-83") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'group_reference_fk_constraint')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUP_ID", baseTableName: "SHARE_WITH_GROUP_REFERENCES", constraintName: "group_reference_fk_constraint", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }
    //1631601335557
    changeSet(author: "suraj (generated)", id: "1631601335557-180") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'REFERENCE_DELETED_FOR_USER')
            }
        }
        createTable(tableName: "REFERENCE_DELETED_FOR_USER") {
            column(name: "REFERENCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DELETED_USER_ID", type: "NUMBER(19, 0)")
            column(name: "deleted_by_user_idx", type: "NUMBER(10, 0)")

        }

    }
    changeSet(author: "suraj (generated)", id: "1631601839645-170") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'ref_del_fk_constraint')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "DELETED_USER_ID", baseTableName: "REFERENCE_DELETED_FOR_USER", constraintName: "ref_del_fk_constraint", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }


/*   changeSet(author: "suraj (generated)", id: "1632334132323-150") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REFERENCE_DETAILS', columnName: 'description')
        }
        sql("alter table REFERENCE_DETAILS modify description VARCHAR2(4000 CHAR);")
    }*/

    hangeSet(author: "rahul (generated)", id: "1630661135578-14") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REFERENCE_DETAILS', columnName: 'description')
            not{
                sqlCheck(expectedResult:'8000', "SELECT CHAR_LENGTH FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'REFERENCE_DETAILS' AND COLUMN_NAME = 'DESCRIPTION';")
            }
        }
        sql("alter table REFERENCE_DETAILS modify description VARCHAR2(8000 CHAR);")
    }

    changeSet(author: "uddesh (generated)", id: "1632334132323-151") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REFERENCE_DETAILS', columnName: 'last_updated')
            }
        }
        addColumn(tableName: "REFERENCE_DETAILS") {
            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "uddesh (generated)", id: "1632334132323-152") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ATTACHMENT_REFERENCE', columnName: 'last_updated')
            }
        }
        addColumn(tableName: "ATTACHMENT_REFERENCE") {
            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "suraj (generated)", id: "1631688158043-150") {

        grailsChange {
            change {
                try {
                    List<Role> roles = Role.createCriteria().list {
                        'in'('authority',['ROLE_SHARE_ALL','ROLE_SHARE_GROUP'])
                    }
                    roles.each { role ->
                        if (role!=null) {
                            if("ROLE_SHARE_ALL".equalsIgnoreCase(role.authority)){
                                role.description = 'Share alerts, signals or references from the dashboard widget with all the users'
                                ctx.CRUDService.update(role)
                            }
                            if("ROLE_SHARE_GROUP".equalsIgnoreCase(role.authority)){
                                role.description = 'Share alerts, signals or references with the user group(s)'
                                ctx.CRUDService.update(role)
                            }

                        }
                    }
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error updating temp_column_seq #############")
                }
            }
        }
    }

    changeSet(author: "mohit (generated)", id: "1631688158043-153") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'ACTION_TYPES' AND column_name = 'DESCRIPTION' ;")
        }
        sql("ALTER TABLE ACTION_TYPES MODIFY DESCRIPTION VARCHAR2(255 CHAR) NULL;")
    }

    changeSet(author: "uddesh (generated)", id: "1632334132323-154") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REFERENCE_DETAILS', columnName: 'modified_by')
            }
        }
        addColumn(tableName: "REFERENCE_DETAILS") {
            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "shivam (generated)", id: "20220106131958-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REFERENCE_DETAILS', columnName: 'fav_icon_url')
            }
        }
        addColumn(tableName: "REFERENCE_DETAILS") {
            column(name: "fav_icon_url", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20220106131958-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'USER_REFERENCES_MAPPING')
            }
        }
        createTable(tableName: "USER_REFERENCES_MAPPING") {


            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reference_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "priority", type: "number(19,0)"){
                constraints(nullable: "false")
            }
            column(name: "is_deleted", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "is_pinned", type: "number(1,0)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "shivam (generated)", id: "20220106131958-5") {
        grailsChange {
            change {
                try {
                    List<Preference> prefrences = Preference.createCriteria().list {
                        isNotNull("dashboardConfig")
                    }
                    prefrences.each { reference ->
                        if (reference!=null) {
                            Map widgetConfig = Holders.config.signal.dashboard.widgets.config
                            reference.dashboardConfig = JsonOutput.toJson(widgetConfig)
                            ctx.CRUDService.update(reference)
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace()
                    println(ex)
                    println("##################### Error updating dashboardConfig #############")
                }
            }
        }
    }

}
