databaseChangeLog = {

    // This file is to support the our temp 4.5.3 release which was not formally rolled out. Some of the existing customer's environments have
    // builds on 4.5.3 and to migrate those to 4.6 this file can be used in externalized configuration file.

    changeSet(author: "amrendra (generated)", id: "1573900006619-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'EMAIL_NOTIFICATION')
            }
        }
        createTable(tableName: "EMAIL_NOTIFICATION") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EMAIL_NOTIFICATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DEFAULT_VALUE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "KEY", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MODULE_NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_ADHOC')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_EVDAS_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_LITR_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_ADHOC')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_EVDAS_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_LITR_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'is_duplicate')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "is_duplicate", type: "number(1, 0)", defaultValue :"0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'is_duplicate')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "is_duplicate", type: "number(1, 0)", defaultValue :"0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'submitter')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "submitter", type: "varchar2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'submitter')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "submitter", type: "varchar2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: "IX_AGG_ON_DEMAND_ALERTPK")
            }
        }
        createIndex(indexName: "IX_AGG_ON_DEMAND_ALERTPK", tableName: "AGG_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "AGG_ON_DEMAND_ALERTPK", forIndexName: "IX_AGG_ON_DEMAND_ALERTPK", tableName: "AGG_ON_DEMAND_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: "IX_EVDAS_ON_DEMAND_ALERTPK")
            }
        }
        createIndex(indexName: "IX_EVDAS_ON_DEMAND_ALERTPK", tableName: "EVDAS_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_ON_DEMAND_ALERTPK", forIndexName: "IX_EVDAS_ON_DEMAND_ALERTPK", tableName: "EVDAS_ON_DEMAND_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: "IX_SINGLE_ON_DEMAND_ALERTPK")
            }
        }
        createIndex(indexName: "IX_SINGLE_ON_DEMAND_ALERTPK", tableName: "SINGLE_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SINGLE_ON_DEMAND_ALERTPK", forIndexName: "IX_SINGLE_ON_DEMAND_ALERTPK", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: "IX_RPT_FIELD_GROUPPK")
            }
        }
        createIndex(indexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP", unique: "true") {
            column(name: "NAME")
        }

        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELD_GROUPNAME_COL", forIndexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK7vrrx66aotafbdx6uc56kbqvu")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_ADHOC", constraintName: "FK7vrrx66aotafbdx6uc56kbqvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK86n78knlffb2f3yg8c9jhhlqj")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_LITR_CONFIG", constraintName: "FK86n78knlffb2f3yg8c9jhhlqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK88ek3tuwyqh526ly1ij7f84as")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_CONFIG", constraintName: "FK88ek3tuwyqh526ly1ij7f84as", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKfk7kmeqebxjcxsgq733o3uh0d")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_LITR_CONFIG", constraintName: "FKfk7kmeqebxjcxsgq733o3uh0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKj293s15eb37wlr6gn0yjjpprh")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_ADHOC", constraintName: "FKj293s15eb37wlr6gn0yjjpprh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKjwr7h59vtbhc7yycaiixb3d30")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_EVDAS_CONFIG", constraintName: "FKjwr7h59vtbhc7yycaiixb3d30", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKob80eqdx25087jypm0j5rehhh")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_EVDAS_CONFIG", constraintName: "FKob80eqdx25087jypm0j5rehhh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-26") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKpcpkomkegxwykoq2am8py9ayt")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_CONFIG", constraintName: "FKpcpkomkegxwykoq2am8py9ayt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "akshay", id: "22194823845834-46") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'patient_age')
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "patient_age_copy", type: "DOUBLE precision") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_CASE_ALERT set patient_age_copy = patient_age;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "patient_age")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "patient_age_copy", newColumnName: "patient_age")
    }

    changeSet(author: "akshay", id: "22194823845834-47") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'patient_age')
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "patient_age_copy", type: "DOUBLE precision") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_ON_DEMAND_ALERT set patient_age_copy = patient_age;")

        dropColumn(tableName: "SINGLE_ON_DEMAND_ALERT", columnName: "patient_age")

        renameColumn(tableName: "SINGLE_ON_DEMAND_ALERT", oldColumnName: "patient_age_copy", newColumnName: "patient_age")
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-1") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
                 insert into groups(id,version,CREATED_BY,DATE_CREATED,GROUP_TYPE,IS_ACTIVE,LAST_UPDATED,MODIFIED_BY,NAME)
                 values((select max(ID)+1 from GROUPS),0,'System',Systimestamp,'USER_GROUP',1,Systimestamp,'System','All Users');
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249432-2") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', '''
                      select count(*) from pvuser where id not in (
                      SELECT usr.id
                      FROM pvuser usr
                      JOIN USER_GROUP_S ug ON (usr.ID = ug.USER_ID)
                      JOIN groups grp ON grp.ID = ug.GROUP_ID
                      where grp.id = (select ID from GROUPS WHERE NAME = 'All Users')) AND ROWNUM = 1
                      ''')
        }
        try {
            sql('''
                INSERT INTO USER_GROUP_S(GROUP_ID,USER_ID)
                   (select (select ID from GROUPS WHERE NAME = 'All Users'),  id from pvuser where id not in (
                      SELECT usr.id
                      FROM pvuser usr
                      JOIN USER_GROUP_S ug ON (usr.ID = ug.USER_ID)
                      JOIN groups grp ON grp.ID = ug.GROUP_ID
                      where grp.id = (select ID from GROUPS WHERE NAME = 'All Users')));
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-3") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
               INSERT INTO SHARE_WITH_GROUP_CONFIG(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,CONFIG_ID)
                (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from RCONFIG);
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-4") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
               INSERT INTO SHARE_WITH_GROUP_EVDAS_CONFIG(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,CONFIG_ID)
               (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from EVDAS_CONFIG);  
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-5") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
               INSERT INTO SHARE_WITH_GROUP_ADHOC(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,CONFIG_ID)
               (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from ALERTS);  
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-59") {
        preConditions(onFail: 'MARK_RAN') {
            notNullConstraintExists(columnName: "sent_to", tableName: "EMAIL_LOG")
        }
        dropNotNullConstraint(columnDataType: "varchar(4000 CHAR)", columnName: "sent_to", tableName: "EMAIL_LOG")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-61") {
        preConditions(onFail: 'MARK_RAN') {
            notNullConstraintExists(columnName: "subject", tableName: "EMAIL_LOG")
        }
        dropNotNullConstraint(columnDataType: "varchar(4000 CHAR)", columnName: "subject", tableName: "EMAIL_LOG")
    }
}
