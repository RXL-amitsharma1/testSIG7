import com.rxlogix.BusinessRulesMigrationService

databaseChangeLog = {
    changeSet(author: "Rahul (generated)", id: "1712839673-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'RULE_MIGRATION_STATUS')
            }
        }
        createTable(tableName: "RULE_MIGRATION_STATUS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RULE_MIGRATIONPK")
            }
            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "entity_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "entity_class", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
            column(name: "datasource", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "is_migration_completed", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "error", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "initial_data", type: "clob") {
                constraints(nullable: "true")
            }
            column(name: "migrated_data", type: "clob") {
                constraints(nullable: "true")
            }
            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "last_updated", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "Yogesh Kumar (generated)", id: "1712839673-02") {
        grailsChange {
            change{
                try {
                    BusinessRulesMigrationService businessRulesMigrationService = ctx.businessRulesMigrationService
                    businessRulesMigrationService.insertDataForMigration()
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("######### Error occurred while Migrating ruleJSON  ###########")
                }
            }
        }
    }
}
