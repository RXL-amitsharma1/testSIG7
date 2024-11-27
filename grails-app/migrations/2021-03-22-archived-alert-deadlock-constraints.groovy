databaseChangeLog = {

    changeSet(author: "ankit (generated)", id: "16002395698766-19") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK21HX2FDB61YFLSA58Y8YKJUT8")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FK21HX2FDB61YFLSA58Y8YKJUT8")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-28") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK4jhhis76bsysx4xm6i6ioykpc")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK4jhhis76bsysx4xm6i6ioykpc")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-37") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKfwtfcguybm93tkxxgtbkf3yn0")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKfwtfcguybm93tkxxgtbkf3yn0")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-46") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKd9ws3gvh9jkokbmx8qej0nt7q")
        }
        dropForeignKeyConstraint(baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FKd9ws3gvh9jkokbmx8qej0nt7q")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-55") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKf8kqtv3nu1fgrdlcikfp4nfcm")
        }
        dropForeignKeyConstraint(baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKf8kqtv3nu1fgrdlcikfp4nfcm")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-64") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKopm5twbskg380tovcrs4r2gxs")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKopm5twbskg380tovcrs4r2gxs")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-73") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKk07u3gx8x9cfv56t566onjcfs")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FKk07u3gx8x9cfv56t566onjcfs")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-82") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK2jg2pehpoo65k30r8hp0ynaia")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK2jg2pehpoo65k30r8hp0ynaia")

    }

    changeSet(author: "ankit (generated)", id: "16002395698766-93") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_LITERATURE_EXEC_CONFIG_ID')
            }
        }
        createIndex(indexName: "IX_LITERATURE_EXEC_CONFIG_ID", tableName: "LITERATURE_ALERT") {
            column(name: "EX_LIT_SEARCH_CONFIG_ID")
        }
    }

    changeSet(author: "ankit (generated)", id: "16002395698766-104") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_single_alert_exconfig", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "EXEC_CONFIG_ID")
        }
    }
}