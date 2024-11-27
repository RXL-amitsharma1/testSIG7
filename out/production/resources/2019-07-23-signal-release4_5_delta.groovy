import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "anshul (generated)", id: "1563960320761-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'DISPO_RULES_TOPIC_CATEGORY')
            }
        }
        createTable(tableName: "DISPO_RULES_TOPIC_CATEGORY") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_LINKED_SIGNALS')
            }
        }
        createTable(tableName: "SIGNAL_LINKED_SIGNALS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LINKED_SIGNAL_ID", type: "NUMBER(19, 0)")

            column(name: "linked_signals_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_OUTCOME')
            }
        }
        createTable(tableName: "SIGNAL_OUTCOME") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_OUTCOMEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_SIG_STATUS_HISTORY')
            }
        }
        createTable(tableName: "SIGNAL_SIG_STATUS_HISTORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIG_STATUS_HISTORY_ID", type: "NUMBER(19, 0)")

            column(name: "signal_status_histories_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_STATUS_HISTORY')
            }
        }
        createTable(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_STATUS_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_updated", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "performed_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "signal_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "status_comment", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_CASE_ALL_TAG')
            }
        }
        createTable(tableName: "SINGLE_CASE_ALL_TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SINGLE_CASE_ALL_TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_series_id", type: "NUMBER(19, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "owner", type: "VARCHAR2(255 CHAR)")

            column(name: "tag_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tag_text", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_GLOBAL_TAG_MAPPING')
            }
        }
        createTable(tableName: "SINGLE_GLOBAL_TAG_MAPPING") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_GLOBAL_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'TOPIC_CATEGORY')
            }
        }
        createTable(tableName: "TOPIC_CATEGORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TOPIC_CATEGORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'VALIDATED_SIGNAL_OUTCOMES')
            }
        }
        createTable(tableName: "VALIDATED_SIGNAL_OUTCOMES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_OUTCOME_ID", type: "NUMBER(19, 0)")

            column(name: "signal_outcomes_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'VAL_SIGNAL_TOPIC_CATEGORY')
            }
        }
        createTable(tableName: "VAL_SIGNAL_TOPIC_CATEGORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'apply_alert_stop_list')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "apply_alert_stop_list", type: "number(1, 0)") {
                constraints(nullable: "true")
            }
        }
        sql('''update RCONFIG set apply_alert_stop_list = EXCLUDE_NON_VALID_CASES''')
        addNotNullConstraint(columnDataType: "number(1,0)", tableName: "RCONFIG", columnName: "apply_alert_stop_list")
    }

    changeSet(author: "kunal (generated)", id: "1563960320761-131") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'apply_alert_stop_list')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "apply_alert_stop_list", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
        sql('''update EX_RCONFIG set apply_alert_stop_list = EXCLUDE_NON_VALID_CASES''')
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "apply_alert_stop_list", tableName: "EX_RCONFIG")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'attachment', columnName: 'attachment_type')
            }
        }
        addColumn(tableName: "attachment") {
            column(name: "attachment_type", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
        sql("update attachment set attachment_type = 'Attachment';")
        addNotNullConstraint(tableName: "attachment", columnName: "attachment_type")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'case_id')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "case_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'chi_square')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "chi_square", type: "double precision", defaultValue: '0') {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'cum_fatal_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "cum_fatal_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'cum_serious_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "cum_serious_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'cum_spon_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "cum_spon_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'cum_study_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "cum_study_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'cumm_count')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "cumm_count", type: "number(10, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set cumm_count = 0;")
        addNotNullConstraint(columnDataType: "number(10,0)", columnName: "cumm_count", tableName: "AGG_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'cumm_interacting_count')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "cumm_interacting_count", type: "number(10, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set cumm_interacting_count = 0;")
        addNotNullConstraint(columnDataType: "number(10,0)", columnName: "cumm_interacting_count", tableName: "AGG_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'cumm_pediatric_count')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "cumm_pediatric_count", type: "number(10, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set cumm_pediatric_count = 0;")
        addNotNullConstraint(columnDataType: "number(10,0)", columnName: "cumm_pediatric_count", tableName: "AGG_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'due_date')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "due_date", type: "timestamp")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb05str')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb05str", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb95str')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb95str", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ebgm_str')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ebgm_str", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-26") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'new_count')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "new_count", type: "number(10, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set new_count = 0;")
        addNotNullConstraint(columnDataType: "number(10,0)", columnName: "new_count", tableName: "AGG_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_eea')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_eea", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-28") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_ev')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_ev", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-29") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_fatal')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_fatal", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-30") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'new_fatal_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "new_fatal_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-31") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_geria')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_geria", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-32") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_hcp')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_hcp", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-33") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'new_interacting_count')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "new_interacting_count", type: "number(10, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set new_interacting_count = 0;")
        addNotNullConstraint(columnDataType: "number(10,0)", columnName: "new_interacting_count", tableName: "AGG_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-34") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_lit')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_lit", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-35") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_med_err')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_med_err", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-36") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_obs')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_obs", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-37") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_paed')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_paed", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-38") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'new_pediatric_count')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "new_pediatric_count", type: "number(10, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set new_pediatric_count = 0;")
        addNotNullConstraint(columnDataType: "number(10,0)", columnName: "new_pediatric_count", tableName: "AGG_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-39") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_rc')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_rc", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-40") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_serious')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_serious", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-41") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'new_serious_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "new_serious_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-42") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'new_spon_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "new_spon_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-43") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'new_spont')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "new_spont", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-44") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'new_study_count')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "new_study_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-45") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'positive_rechallenge')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "positive_rechallenge", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-46") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'product_id')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "product_id", type: "number(10, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_CASE_ALERT set PRODUCT_ID = 0;")
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "PRODUCT_ID", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'attachment', columnName: 'reference_link')
            }
        }
        addColumn(tableName: "attachment") {
            column(name: "reference_link", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-48") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'smq_code')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "smq_code", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-49") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_eea')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_eea", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-50") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_geria')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_geria", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-51") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_hcp')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_hcp", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-52") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_med_err')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_med_err", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-53") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_obs')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_obs", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-54") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_paed')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_paed", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-55") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_rc')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_rc", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-56") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_spont')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_spont", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-57") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_spont_asia')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_spont_asia", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-58") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_spont_europe')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_spont_europe", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-59") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_spont_japan')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_spont_japan", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-60") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_spont_rest')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_spont_rest", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-61") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'tot_spontnamerica')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "tot_spontnamerica", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-62") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'total_ev')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "total_ev", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-63") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'total_fatal')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "total_fatal", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-64") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'total_lit')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "total_lit", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-65") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'total_serious')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "total_serious", type: "number(10, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-66") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIVITIES', columnName: 'DETAILS')
        }
        addColumn(tableName: "ACTIVITIES") {
            column(name: "DETAILS_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update ACTIVITIES set DETAILS_COPY = DETAILS;")

        dropColumn(tableName: "ACTIVITIES", columnName: "DETAILS")


        renameColumn(tableName: "ACTIVITIES", oldColumnName: "DETAILS_COPY", newColumnName: "DETAILS")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-67") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WkFL_RUL_DISPOSITIONS', columnName: 'allowed_dispositions_id')
            }
        }
        addColumn(tableName: "WkFL_RUL_DISPOSITIONS") {
            column(name: "allowed_dispositions_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-124") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'CASE_HISTORY', columnName: 'TAG_NAME')
        }
        modifyDataType(tableName: "CASE_HISTORY", columnName: "TAG_NAME", newDataType: "varchar2(4000 CHAR)")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-68") {
        createIndex(indexName: "IX_EXEVDAS_CONFIG_ACTIVITIESPK", tableName: "EX_EVDAS_CONFIG_ACTIVITIES", unique: "true") {
            column(name: "EX_EVDAS_CONFIG_ID")

            column(name: "ACTIVITY_ID")
        }

        addPrimaryKey(columnNames: "EX_EVDAS_CONFIG_ID, ACTIVITY_ID", constraintName: "EX_EVDAS_CONFIG_ACTIVITIESPK", forIndexName: "IX_EXEVDAS_CONFIG_ACTIVITIESPK", tableName: "EX_EVDAS_CONFIG_ACTIVITIES")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-70") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK1lqlnx26rn2mthsyqoajgfwtr')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "TOPIC_CATEGORY_ID", baseTableName: "DISPO_RULES_TOPIC_CATEGORY", constraintName: "FK1lqlnx26rn2mthsyqoajgfwtr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-71") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK1pciu4e4nwseetvpojr0hrh3q')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SINGLE_GLOBAL_ID", baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FK1pciu4e4nwseetvpojr0hrh3q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALL_TAG")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-72") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK86r3dj8pbqyfn9p1d3cphcxwd')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VAL_SIGNAL_TOPIC_CATEGORY", constraintName: "FK86r3dj8pbqyfn9p1d3cphcxwd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-73") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKaj0rnca6k5rfil9h32x5m6pbu')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "TOPIC_CATEGORY_ID", baseTableName: "VAL_SIGNAL_TOPIC_CATEGORY", constraintName: "FKaj0rnca6k5rfil9h32x5m6pbu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-74") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKg049m72c3c0vmmyyg47jky5w5')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKg049m72c3c0vmmyyg47jky5w5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-76") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKj5pmuktmewe6h1pbfljto0p4u')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_TOPIC_CATEGORY", constraintName: "FKj5pmuktmewe6h1pbfljto0p4u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-77") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKjpwbsduge17rl18csfbwj2rog')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SIG_STATUS_HISTORY_ID", baseTableName: "SIGNAL_SIG_STATUS_HISTORY", constraintName: "FKjpwbsduge17rl18csfbwj2rog", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STATUS_HISTORY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-78") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKk07u3gx8x9cfv56t566onjcfs')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FKk07u3gx8x9cfv56t566onjcfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-79") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKre3rort06h65skghf2j23ad7p')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_OUTCOME_ID", baseTableName: "VALIDATED_SIGNAL_OUTCOMES", constraintName: "FKre3rort06h65skghf2j23ad7p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_OUTCOME")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-80") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKt7saax9k5yl609iwcsu4x1gr7')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "LINKED_SIGNAL_ID", baseTableName: "SIGNAL_LINKED_SIGNALS", constraintName: "FKt7saax9k5yl609iwcsu4x1gr7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-81") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FK1H2UCYIPG6Q9PLBMKPQS9ML5X')
        }
        dropForeignKeyConstraint(baseTableName: "WKFL_RUL_DISPOSITIONS", constraintName: "FK1H2UCYIPG6Q9PLBMKPQS9ML5X")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-82") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FK5XQN3NLELIFK9HPIH50RD7B3S')
        }
        dropForeignKeyConstraint(baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FK5XQN3NLELIFK9HPIH50RD7B3S")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-83") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FK8J3M9M71XSSNSTCR7SADEG87F')
        }
        dropForeignKeyConstraint(baseTableName: "DISPO_RULES_SIGNAL_CATEGORY", constraintName: "FK8J3M9M71XSSNSTCR7SADEG87F")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-84") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FK9H7PD11WVCCR6LF5YGE61LAYR')
        }
        dropForeignKeyConstraint(baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FK9H7PD11WVCCR6LF5YGE61LAYR")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-85") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKESLQOUXQ7266LCK0OUERMOO0B')
        }
        dropForeignKeyConstraint(baseTableName: "DISPO_RULES_SIGNAL_CATEGORY", constraintName: "FKESLQOUXQ7266LCK0OUERMOO0B")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-86") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKFMLDOJGPELY7WLUG6E2L6Q4OO')
        }
        dropForeignKeyConstraint(baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FKFMLDOJGPELY7WLUG6E2L6Q4OO")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-87") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'DISPO_RULES_SIGNAL_CATEGORY')
        }
        dropTable(tableName: "DISPO_RULES_SIGNAL_CATEGORY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-88") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_CLL_TEMPLT')
        }
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-89") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_DTAB_TEMPLT')
        }
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-90") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_CLL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-91") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_CUSTOM_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-92") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_DTAB_TEMPLT')
        }
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-93") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_NCASE_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-94") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_QUERY_EXP')
        }
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-95") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_QUERY_VALUE')
        }
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-96") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_SQL_VALUE')
        }
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-97") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-98") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_NONCASE_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-99") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_PARAM')
        }
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-100") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_QUERY_EXP_VALUE')
        }
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-101") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_QUERY_VALUE')
        }
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-102") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_RPT_TEMPLT')
        }
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-103") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-104") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-105") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_VALUE')
        }
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-106") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-107") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_VALUE')
        }
        dropTable(tableName: "HT_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-108") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SIGNAL_CATEGORY')
        }
        dropTable(tableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-109") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VALIDATED_SIGNAL_CATEGORY')
        }
        dropTable(tableName: "VALIDATED_SIGNAL_CATEGORY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-110") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VALID_MINING_FREQUENCY')
        }
        dropTable(tableName: "VALID_MINING_FREQUENCY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-111") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VALID_UPLOAD_FREQUENCY')
        }
        dropTable(tableName: "VALID_UPLOAD_FREQUENCY")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-112") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'WKFL_RUL_DISPOSITIONS', columnName: 'WORKFLOW_RULE_ID')
        }
        dropColumn(columnName: "WORKFLOW_RULE_ID", tableName: "WKFL_RUL_DISPOSITIONS")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-121") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'WkFL_RUL_DISPOSITIONS', columnName: 'disposition_id')
        }
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "disposition_id", tableName: "WkFL_RUL_DISPOSITIONS")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-123") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_EVDAS_CONFIG_ACTIVITIES', columnName: 'ACTIVITY_ID')
        }
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "ACTIVITY_ID", tableName: "EX_EVDAS_CONFIG_ACTIVITIES")
    }

    changeSet(author: "prashantsahi (generated)", id: "1563960320761-138") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIVITIES', columnName: 'ATTRIBUTES')
        }
        addColumn(tableName: "ACTIVITIES") {
            column(name: "ATTRIBUTES_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update ACTIVITIES set ATTRIBUTES_COPY = ATTRIBUTES;")

        dropColumn(tableName: "ACTIVITIES", columnName: "ATTRIBUTES")

        renameColumn(tableName: "ACTIVITIES", oldColumnName: "ATTRIBUTES_COPY", newColumnName: "ATTRIBUTES")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-126") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VIEW_INSTANCE')
        }
        grailsChange {
            change {
                try {
                    def viewInstanceService = ctx.getBean('viewInstanceService')
                    viewInstanceService.updateAllViewInstances()
                }
                catch (Exception ex) {
                    println("########  Some error occurred while running changelog script View-Instance-Update-From-Service ##########")
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-130") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ADVANCED_FILTER', columnName: 'user_id')
            }
        }
        addColumn(tableName: "ADVANCED_FILTER") {
            column(name: "user_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }

        grailsChange {
            change {
                try {
                    List<ViewInstance> vwList = ViewInstance.findAllByAdvancedFilterIsNotNull()
                    Map<Long, Long> viewFilterMapping = new HashMap<>()
                    AdvancedFilter advanceFilter
                    vwList.each { vwInstance ->
                        AdvancedFilter advFilter = vwInstance.advancedFilter
                        AdvancedFilter.withNewTransaction {
                            advanceFilter = new AdvancedFilter()
                            advanceFilter.alertType = advFilter.alertType
                            advanceFilter.criteria = advFilter.criteria
                            advanceFilter.createdBy = advFilter.createdBy
                            advanceFilter.dateCreated = advFilter.dateCreated
                            advanceFilter.description = advFilter.description
                            advanceFilter.JSONQuery = advFilter.getJSONQuery()
                            advanceFilter.lastUpdated = advFilter.lastUpdated
                            advanceFilter.modifiedBy = advFilter.modifiedBy
                            advanceFilter.name = advFilter.name
                            advanceFilter.user = User.get(vwInstance.user.id)
                            advanceFilter.save(failOnError: true, flush: true)
                            viewFilterMapping.put(vwInstance.getId(), advanceFilter.getId())
                        }
                    }

                    ViewInstance vw
                    AdvancedFilter af
                    for (Map.Entry<String, String> entry : viewFilterMapping.entrySet()) {
                        ViewInstance.withNewTransaction {
                            vw = ViewInstance.get(entry.getKey())
                            af = AdvancedFilter.get(entry.getValue())
                            vw.advancedFilter = af;
                            vw.save(failOnError: true, flush: true)
                        }
                    }

                } catch (Exception ex) {
                    println "##### Error Occurred while inserting advanced filter for all users' views in liquibase change-set ####"
                    ex.printStackTrace()
                }
            }
        }

        grailsChange {
            def grailsApplication = Holders.grailsApplication
            change {
                try {
                    AdvancedFilter filter
                    AdvancedFilter filter1
                    grailsApplication.config.configurations.advancedFilterAddActivity.each {
                        filter = AdvancedFilter.findByNameAndAlertType(it.name, it.alertType)
                        if (filter) {
                            User.list().each { user ->
                                try {
                                    AdvancedFilter.withNewTransaction {
                                        filter1 = new AdvancedFilter()
                                        filter1.alertType = filter.alertType
                                        filter1.criteria = filter.criteria
                                        filter1.createdBy = filter.createdBy
                                        filter1.dateCreated = filter.dateCreated
                                        filter1.description = filter.description
                                        filter1.JSONQuery = filter.getJSONQuery()
                                        filter1.lastUpdated = filter.lastUpdated
                                        filter1.modifiedBy = filter.modifiedBy
                                        filter1.name = filter.name
                                        filter1.user = user
                                        filter1.save(failOnError: true, flush: true)
                                    }
                                }
                                catch (Exception) {
                                    println("#### ${filter.name} couldn't inserted for alert ${filter.alertType} and user ${user.name} ####")
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    println "##### Error Occurred while inserting advanced filter for public filters in liquibase change-set ####"
                    ex.printStackTrace()
                }
            }
        }

        sql("DELETE FROM ADVANCED_FILTER WHERE user_id IS NULL; commit;")

        addNotNullConstraint(tableName: "ADVANCED_FILTER", columnName: "USER_ID", columnDataType: "number(19,0)")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-75") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKhrs1not8xdn6i0qofrejeridc')
        }
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "ADVANCED_FILTER", constraintName: "FKhrs1not8xdn6i0qofrejeridc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "anshul (generated)", id: "1563960320761-131") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM ACTIVITY_TYPE where value in ('ReferenceRemoved','ReferenceAdded');")
        }
        grailsChange {
            change {
                try {
                    ActivityType activityType
                    [ActivityTypeValue.ReferenceRemoved, ActivityTypeValue.ReferenceAdded].each {
                        activityType = new ActivityType(value: it)
                        activityType.save()
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while adding the ReferenceRemoved and ReferenceAdded ActivityType for liquibase change-set 1560333130414-7 ####"
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-132") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SINGLE_GLOBAL_TAG_MAPPING')
        }
        sql('''TRUNCATE TABLE SINGLE_GLOBAL_TAG_MAPPING ; DELETE FROM SINGLE_CASE_ALL_TAG;''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-133") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ADHOC_ALERT_ACTIONS')
            }
        }
        createTable(tableName: "ADHOC_ALERT_ACTIONS") {
            column(name: "ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-134") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AGG_ALERT_ACTIONS')
            }
        }
        createTable(tableName: "AGG_ALERT_ACTIONS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-135") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'EVDAS_ALERT_ACTIONS')
            }
        }
        createTable(tableName: "EVDAS_ALERT_ACTIONS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-136") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'LIT_ALERT_ACTIONS')
            }
        }
        createTable(tableName: "LIT_ALERT_ACTIONS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-137") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_ACTIONS')
            }
        }
        createTable(tableName: "SINGLE_ALERT_ACTIONS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-138") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK1spm3adg2morgiavbiypkcowf')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK1spm3adg2morgiavbiypkcowf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-139") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK6b0na7al8svxgc22f6rmb8fyr')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ALERT_ID", baseTableName: "ADHOC_ALERT_ACTIONS", constraintName: "FK6b0na7al8svxgc22f6rmb8fyr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-140") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKbwscav6fgh7pp95mn2p7joyr6')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LIT_ALERT_ACTIONS", constraintName: "FKbwscav6fgh7pp95mn2p7joyr6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-141") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKdaa6kaucnowbyqhb0salrcwsl')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FKdaa6kaucnowbyqhb0salrcwsl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-142") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKfajq9c8l8as3u0p8h65m522ua')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_ALERT_ACTIONS", constraintName: "FKfajq9c8l8as3u0p8h65m522ua", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-143") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKhhg10vfuqjejwgwe05cngrij5')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "LIT_ALERT_ACTIONS", constraintName: "FKhhg10vfuqjejwgwe05cngrij5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-144") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKiua3cjol1cxe2hpwy3vcbf5g')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FKiua3cjol1cxe2hpwy3vcbf5g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-145") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKjnj34noqmqxdh4sp62ty6f4vb')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "EVDAS_ALERT_ACTIONS", constraintName: "FKjnj34noqmqxdh4sp62ty6f4vb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-146") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKqf474sj5dkn95xc433napf95b')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ADHOC_ALERT_ACTIONS", constraintName: "FKqf474sj5dkn95xc433napf95b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-147") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKshd0nte1n4d4v5cp5dav55ic0')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FKshd0nte1n4d4v5cp5dav55ic0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-148") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKO8VF0YXSU6JV9D9XDDV9OWYXO')
        }
        dropForeignKeyConstraint(baseTableName: "ACTIONS", constraintName: "FKO8VF0YXSU6JV9D9XDDV9OWYXO")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-149") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKOTW99RDOEYDIDVA8EQNGEOIM1')
        }
        dropForeignKeyConstraint(baseTableName: "ACTIONS", constraintName: "FKOTW99RDOEYDIDVA8EQNGEOIM1")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-150") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKP8D85NI1HCK589SQ6QBXTK2UR')
        }
        dropForeignKeyConstraint(baseTableName: "ACTIONS", constraintName: "FKP8D85NI1HCK589SQ6QBXTK2UR")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-151") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKQCRTH3PEHAA07ED4HF83ABYP4')
        }
        dropForeignKeyConstraint(baseTableName: "ACTIONS", constraintName: "FKQCRTH3PEHAA07ED4HF83ABYP4")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-152") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKQQMEB3P8BX7819MCUWEUN42QU')
        }
        dropForeignKeyConstraint(baseTableName: "ACTIONS", constraintName: "FKQQMEB3P8BX7819MCUWEUN42QU")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-153") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM EVDAS_ALERT_ACTIONS;")
        }
        sql(''' INSERT INTO EVDAS_ALERT_ACTIONS(ACTION_ID,EVDAS_ALERT_ID)
                     SELECT ID,EVDAS_ALERT_ID FROM ACTIONS WHERE EVDAS_ALERT_ID IS NOT NULL;
                     COMMIT;  ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-154") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM AGG_ALERT_ACTIONS;")
        }
        sql(''' INSERT INTO AGG_ALERT_ACTIONS(ACTION_ID,AGG_ALERT_ID)
                     SELECT ID,AGG_ALERT_ID FROM ACTIONS WHERE AGG_ALERT_ID IS NOT NULL;
                     COMMIT;  ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-155") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM SINGLE_ALERT_ACTIONS;")
        }
        sql(''' INSERT INTO SINGLE_ALERT_ACTIONS(ACTION_ID,SINGLE_CASE_ALERT_ID)
                     SELECT ID,SINGLE_CASE_ALERT_ID FROM ACTIONS WHERE SINGLE_CASE_ALERT_ID IS NOT NULL;
                     COMMIT;  ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-156") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM LIT_ALERT_ACTIONS;")
        }
        sql(''' INSERT INTO LIT_ALERT_ACTIONS(ACTION_ID,LITERATURE_ALERT_ID)
                     SELECT ID,LITERATURE_ALERT_ID FROM ACTIONS WHERE LITERATURE_ALERT_ID IS NOT NULL;
                     COMMIT;  ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-157") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM ADHOC_ALERT_ACTIONS;")
        }
        sql(''' INSERT INTO ADHOC_ALERT_ACTIONS(ACTION_ID,ALERT_ID)
                     SELECT ID,ALERT_ID FROM ACTIONS WHERE ALERT_ID IS NOT NULL;
                     COMMIT;  ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-158") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'AGG_ALERT_ID')
        }
        dropColumn(columnName: "AGG_ALERT_ID", tableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-159") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'ALERT_ID')
        }
        dropColumn(columnName: "ALERT_ID", tableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-160") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'EVDAS_ALERT_ID')
        }
        dropColumn(columnName: "EVDAS_ALERT_ID", tableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-161") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'LITERATURE_ALERT_ID')
        }
        dropColumn(columnName: "LITERATURE_ALERT_ID", tableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-162") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'SINGLE_CASE_ALERT_ID')
        }
        dropColumn(columnName: "SINGLE_CASE_ALERT_ID", tableName: "ACTIONS")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-163") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'type')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "type", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-168") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM EX_STATUS WHERE TYPE IS NULL AND ROWNUM = 1;")
        }
        sql('''
           update EX_STATUS set type = 'Aggregate Case Alert'
           where config_id in (Select id from RCONFIG WHERE TYPE = 'Aggregate Case Alert');
           commit;
        ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-173") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM EX_STATUS WHERE TYPE IS NULL AND ROWNUM = 1;")
        }
        sql('''
           update EX_STATUS set type = 'Single Case Alert'
           where config_id in (Select id from RCONFIG WHERE TYPE = 'Single Case Alert') AND TYPE IS NULL;
           commit;
        ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-174") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM EX_STATUS WHERE TYPE IS NULL AND ROWNUM = 1;")
        }
        sql('''
           update EX_STATUS set type = 'EVDAS Alert'
           where config_id in (Select id from EVDAS_CONFIG) AND TYPE IS NULL;
           commit;
        ''')
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-175") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM EX_STATUS WHERE TYPE IS NULL AND ROWNUM = 1;")
        }
        sql('''
           update EX_STATUS set type = 'Literature Search Alert'
           where config_id in (Select id from LITERATURE_CONFIG) AND TYPE IS NULL;
           commit;
        ''')
    }

    changeSet(author: "sandeep (generated)", id: "1563960320761-176") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ADVANCED_FILTER', columnName:"DESCRIPTION")
         }
        modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(4000 char)", tableName: "ADVANCED_FILTER")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-177") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMAIL_LOG', columnName:"SUBJECT")
        }
        modifyDataType(columnName: "SUBJECT", newDataType: "varchar2(4000 char)", tableName: "EMAIL_LOG")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-178") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMAIL_LOG', columnName:"SENT_TO")
        }
        modifyDataType(columnName: "SENT_TO", newDataType: "varchar2(4000 char)", tableName: "EMAIL_LOG")
    }


    changeSet(author: "anshul (generated)", id: "1563960320761-179") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERTS', columnName: 'COMMENT_SIGNAL_STATUS')
        }
        modifyDataType(tableName: "ALERTS", columnName: "COMMENT_SIGNAL_STATUS", newDataType: "varchar2(4000 CHAR)")
    }

    changeSet(author: "ankit (generated)", id: "1563960320761-180") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'INBOX_LOG', columnName:"SUBJECT")
        }
        modifyDataType(columnName: "SUBJECT", newDataType: "varchar2(4000 char)", tableName: "INBOX_LOG")
    }
}
