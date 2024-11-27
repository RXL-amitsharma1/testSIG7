databaseChangeLog = {

    changeSet(author: "chetansharma (generated)", id: "1527503261680-1") {
        createTable(tableName: "ADHOC_ALERT_COMMENTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-2") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "action_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-3") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "action_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-4") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "action_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-5") {
        addColumn(tableName: "WORK_FLOW_RULES") {
            column(defaultValueBoolean: "true", name: "display", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-6") {
        addColumn(tableName: "PVUSER") {
            column(name: "email", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-7") {
        addColumn(tableName: "PVUSER") {
            column(name: "full_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-8") {
        addColumn(tableName: "AGG_ALERT") {
            column(defaultValueBoolean: "true", name: "is_new", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-9") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(defaultValueBoolean: "true", name: "is_new", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-10") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "listedness", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-11") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "outcome", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-12") {
        addColumn(tableName: "ALERTS") {
            column(name: "product_dictionary_selection", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-13") {
        addColumn(tableName: "ALERTS") {
            column(name: "reason_for_delay", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-14") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "review_due_date", type: "timestamp")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-15") {
        addColumn(tableName: "PRODUCT_DICTIONARY_CACHE") {
            column(name: "safety_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-16") {
        createIndex(indexName: "INDEX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")
            column(name: "TOPIC_ID")
        }
        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, TOPIC_ID", forIndexName: "INDEX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-17") {
        createIndex(indexName: "INDEX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")
            column(name: "TOPIC_ID")
        }
        addPrimaryKey(columnNames: "AGG_ALERT_ID, TOPIC_ID", forIndexName: "INDEX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-18") {
        createIndex(indexName: "INDEX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")
            column(name: "TOPIC_ID")
        }
        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, TOPIC_ID", forIndexName: "INDEX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-42") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FK4xwtik25kkjo898vri20gjn8h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-43") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FK923jql3sdm5jk6ht1an9nn7ov", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-44") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FK9q2pi8025jxe8bbgtyldlp6tp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-45") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FKa3cqvwnj1j7q2yi5coqlp2br4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-46") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FKd74cpexfn1jrxsjhy23g3kqjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-47") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FKdk2ms0bbvlg9wt85bl3h172c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-48") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKf8kqtv3nu1fgrdlcikfp4nfcm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-49") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKhhjqmaurc06u0g2sd8pk0puw3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-50") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKhycn6c1tko90ojyv3m9q92b4b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-51") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKmx3gs9bdqe5we8t8dyfwlnu57", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-52") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKneg18gip2c5vrkf5o5d6hjvp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-53") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKnf31xnc5tocj5toxf64f4n5bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-54") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKqj7ub0xptnbuhouvowxamuj26", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-55") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FK31JAB223WUOIN37MIHJGMD5O3")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-56") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FK58RY6BYMEI63YCJU6MS99BUW5")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-57") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FK743ERTPL99BHH1SP7VD91ASMI")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-58") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_COMMENTS", constraintName: "FK7RAWB1UURSVBK5HKI32GO9IFO")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-59") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_ACTIVITIES", constraintName: "FK888DTFRH3HVMO8UTWVC6SFUYA")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-60") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKGG9JS6NTSJTYESWGIEDQ6RE87")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-61") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_ACTIVITIES", constraintName: "FKK00OQ8I0RB555MRS1UA1U1BMV")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-62") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKMIP3DEWJMMTBXME02DDF93BNI")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-63") {
        dropForeignKeyConstraint(baseTableName: "TOPIC_COMMENTS", constraintName: "FKON2N40JNXMX59N3CQMIG3YTC5")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-94") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "ACTIVITY_ID", tableName: "TOPIC_ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-96") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "COMMENT_ID", tableName: "TOPIC_COMMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-123") {
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "disposition_id", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-124") {
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "follow_up_exists", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-125") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "group_id", tableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-141") {
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "TOPIC_ID", tableName: "TOPIC_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-142") {
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "TOPIC_ID", tableName: "TOPIC_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-143") {
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "TOPIC_ID", tableName: "TOPIC_SINGLE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-144") {
        dropPrimaryKey(tableName: "VALIDATED_ADHOC_ALERTS")
        createIndex(indexName: "INDEX_SIGNAL_ADHOC_ALERTS", tableName: "VALIDATED_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")
            column(name: "VALIDATED_SIGNAL_ID")
        }
        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "INDEX_SIGNAL_ADHOC_ALERTS", tableName: "VALIDATED_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-145") {
        dropPrimaryKey(tableName: "VALIDATED_AGG_ALERTS")
        createIndex(indexName: "INDEX_SIGNAL_AGG_ALERTS", tableName: "VALIDATED_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")
            column(name: "VALIDATED_SIGNAL_ID")
        }
        addPrimaryKey(columnNames: "AGG_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "INDEX_SIGNAL_AGG_ALERTS", tableName: "VALIDATED_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-146") {
        dropPrimaryKey(tableName: "VALIDATED_EVDAS_ALERTS")
        createIndex(indexName: "INDEX_SIGNAL_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS", unique: "true") {
            column(name: "EVDAS_ALERT_ID")
            column(name: "VALIDATED_SIGNAL_ID")
        }
        addPrimaryKey(columnNames: "EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "INDEX_SIGNAL_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527503261680-147") {
        dropPrimaryKey(tableName: "VALIDATED_SINGLE_ALERTS")
        createIndex(indexName: "INDEX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")
            column(name: "VALIDATED_SIGNAL_ID")
        }
        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "INDEX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS")
    }
}
