databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1598698970939-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'user_view_order')
            }
        }
        createTable(tableName: "user_view_order") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "user_view_orderPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "view_instance_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "view_order", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "EVENT_GROUP_SELECTION", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "EVENT_GROUP_SELECTION", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "PRODUCT_GROUP_SELECTION", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'pvs_alert_tag', columnName: 'auto_tagged')
            }
        }
        addColumn(tableName: "pvs_alert_tag") {
            column(name: "auto_tagged", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'pvs_global_tag', columnName: 'auto_tagged')
            }
        }
        addColumn(tableName: "pvs_global_tag") {
            column(name: "auto_tagged", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb05age_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb05age_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'eb05age_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "eb05age_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb05gender_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb05gender_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'eb05gender_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "eb05gender_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb95age_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb95age_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'eb95age_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "eb95age_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb95gender_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb95gender_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'eb95gender_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "eb95gender_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ebgm_age_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ebgm_age_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ebgm_age_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ebgm_age_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ebgm_gender_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ebgm_gender_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ebgm_gender_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ebgm_gender_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'evdas_columns')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "evdas_columns", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'evdas_columns')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "evdas_columns", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'evdas_Date_Range')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "evdas_date_range", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERTS', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "ALERTS") {
            column(name: "event_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "event_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "event_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-26") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "event_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "event_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-28") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "event_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-29") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'EVENT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "event_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-30") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'pvs_alert_tag', columnName: 'exec_config_id')
            }
        }
        addColumn(tableName: "pvs_alert_tag") {
            column(name: "exec_config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-31") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'pvs_global_tag', columnName: 'exec_config_id')
            }
        }
        addColumn(tableName: "pvs_global_tag") {
            column(name: "exec_config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-32") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'faers_Case_Series_Id')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "faers_case_series_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-33") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'faers_columns')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "faers_columns", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-34") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'faers_columns')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "faers_columns", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-35") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'faers_Cum_Case_Series_Id')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "faers_cum_case_series_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-36") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'FAERS_DATE_RANGE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "faers_date_range", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-37") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'integrated_configuration_id')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "integrated_configuration_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-38") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'integrated_configuration_id')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "integrated_configuration_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-39") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'pvs_alert_tag', columnName: 'is_retained')
            }
        }
        addColumn(tableName: "pvs_alert_tag") {
            column(name: "is_retained", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-40") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'pvs_global_tag', columnName: 'is_retained')
            }
        }
        addColumn(tableName: "pvs_global_tag") {
            column(name: "is_retained", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-41") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERTS', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "ALERTS") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-42") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'BUSINESS_CONFIGURATION', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-43") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-44") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-45") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-46") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-48") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'PRODUCT_GROUP_SELECTION')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-49") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'prr_str05faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_str05faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-50") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'prr_str05faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "prr_str05faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-51") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'prr_str95faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_str95faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-52") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'prr_str95faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "prr_str95faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-53") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'prr_str_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_str_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-54") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'prr_str_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "prr_str_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-55") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_str05faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_str05faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-56") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_str05faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_str05faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-57") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_str95faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_str95faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-58") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_str95faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_str95faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-59") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_str_faers')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_str_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-60") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_str_faers')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_str_faers", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id:'1598698970939-61') {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', '''
                      select count(*) from agg_alert aga JOIN EX_RCONFIG erc ON (erc.id = aga.EXEC_CONFIGURATION_ID)
                                             where erc.selected_data_source = 'faers' AND ROWNUM = 1
                      ''')
        }
        sql("""
            merge into agg_alert t using ( 
                                    Select  '{ "executedAlertConfigurationId": ' || EXEC_CONFIGURATION_ID || ',"productId" : ' || PRODUCT_ID ||  ',"ptCode" : "' || PT_CODE ||  '","smqCode" : ' || COALESCE(SMQ_CODE,'null') ||
                                            ',"newCountFaers" : ' || NEW_COUNT || ',"cummCountFaers" : ' || CUMM_COUNT || ',"newSeriousCountFaers" : ' || NEW_SERIOUS_COUNT || ',"cumSeriousCountFaers" : ' || CUM_SERIOUS_COUNT
                                             ||   ',"ebgmFaers" : ' || CASE WHEN ebgm > 0 and ebgm < 1 then '0' || to_char(ebgm) else to_char(ebgm) END || ',"eb95Faers" : ' || CASE WHEN eb95 > 0 and eb95 < 1 then '0' || to_char(eb95) else to_char(eb95) END ||
                                             ',"eb05Faers" : ' || CASE WHEN eb05 > 0 and eb05 < 1 then '0' || to_char(eb05) else to_char(eb05) END || ',"newSponCountFaers" : ' || NEW_SPON_COUNT || ',"cumSponCountFaers" : ' || CUM_SPON_COUNT || 
                                             ',"prrValueFaers" : ' || CASE WHEN prr_value > 0 and prr_value < 1 then '0' || to_char(prr_value) else to_char(prr_value) END ||
                                             ',"prr95Faers" : ' || CASE WHEN PRR95 > 0 and PRR95 < 1 then '0' || to_char(PRR95) else to_char(PRR95) END ||
                                             ',"prr05Faers" : ' || CASE WHEN prr05 > 0 and prr05 < 1 then '0' || to_char(prr05) else to_char(prr05) END ||
                                             ',"rorValueFaers" : ' || CASE WHEN ror_value > 0 and ror_value < 1 then '0' || to_char(ror_value) else to_char(ror_value) END ||
                                             ',"ror95Faers" : ' || CASE WHEN ROR95 > 0 and ROR95 < 1 then '0' || to_char(ROR95) else to_char(ROR95) END ||
                                             ',"ror05Faers" : ' || CASE WHEN ROR05 > 0 and ROR05 < 1 then '0' || to_char(ROR05) else to_char(ROR05) END ||
                                             ',"chiSquareFaers" : ' || CASE WHEN CHI_SQUARE > 0 and CHI_SQUARE < 1 then '0' || to_char(CHI_SQUARE) else to_char(CHI_SQUARE) END ||
                                             ',"prrMhFaers" : "' || PRR_MH || '","rorMhFaers" : "' || ROR_MH || '","newStudyCountFaers" : ' || NEW_STUDY_COUNT || ',"cumStudyCountFaers" : ' || CUM_STUDY_COUNT || ',"newInteractingCountFaers" : ' || NEW_INTERACTING_COUNT || ',"cummInteractingCountFaers" : ' 
                                             || CUMM_INTERACTING_COUNT || ',"newFatalCountFaers" : ' || NEW_FATAL_COUNT || ',"cumFatalCountFaers" : ' || CUM_FATAL_COUNT || ',"listedFaers" : "' || LISTED || '","impEventsFaers" : "' || COALESCE(IMP_EVENTS,'') || 
                                             '","positiveDechallengeFaers" : "' || POSITIVE_DECHALLENGE || '","positiveRechallengeFaers" : "' || POSITIVE_RECHALLENGE || '","pregenencyFaers" : "' || PREGENENCY || '","relatedFaers" : "' || RELATED || '","trendTypeFaers" : "' 
                                             || TREND_TYPE || '","freqPriorityFaers" : "' || FREQ_PRIORITY || '","newPediatricCountFaers" : ' || NEW_PEDIATRIC_COUNT ||  ', "cummPediatricCountFaers": ' || CUMM_PEDIATRIC_COUNT || ' } ' as faers_columns,
                                             aga.id, eb05age, eb95age, ebgm_age, eb05gender, eb95gender, ebgm_gender, prr_str, prr_str05, prr_str95, ror_str, ror_str05, ror_str95   
                                             from agg_alert aga JOIN EX_RCONFIG erc ON (erc.id = aga.EXEC_CONFIGURATION_ID)
                                             where erc.selected_data_source = 'faers'
                                            ) s 
                                             on (t.id = s.id) 
                                             when matched then update set t.faers_columns = s.faers_columns,
                                             t.eb05age_faers = dbms_lob.substr(s.eb05age,dbms_lob.getlength(s.eb05age),1),
                                             t.eb95age_faers = dbms_lob.substr(s.eb95age,dbms_lob.getlength(s.eb95age),1),
                                             t.ebgm_age_faers = dbms_lob.substr(s.ebgm_age,dbms_lob.getlength(s.ebgm_age),1),
                                             t.eb05gender_faers = dbms_lob.substr(s.eb05gender,dbms_lob.getlength(s.eb05gender),1),
                                             t.eb95gender_faers = dbms_lob.substr(s.eb95gender,dbms_lob.getlength(s.eb95gender),1),
                                             t.ebgm_gender_faers = dbms_lob.substr(s.ebgm_gender,dbms_lob.getlength(s.ebgm_gender),1),
                                             t.prr_str_faers = dbms_lob.substr(s.prr_str,dbms_lob.getlength(s.prr_str),1),
                                             t.prr_str05faers = dbms_lob.substr(s.prr_str05,dbms_lob.getlength(s.prr_str05),1),
                                             t.prr_str95faers = dbms_lob.substr(s.prr_str95,dbms_lob.getlength(s.prr_str95),1),
                                             t.ror_str_faers = dbms_lob.substr(s.ror_str,dbms_lob.getlength(s.ror_str),1),
                                             t.ror_str05faers = dbms_lob.substr(s.ror_str05,dbms_lob.getlength(s.ror_str05),1),
                                             t.ror_str95faers = dbms_lob.substr(s.ROR_STR95,dbms_lob.getlength(s.ror_str95),1);
           """)
    }

    changeSet(author: "amrendra (generated)", id:'1598698970939-62') {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', '''
                      select count(*) from agg_alert aga JOIN EX_RCONFIG erc ON (erc.id = aga.EXEC_CONFIGURATION_ID)
                                             where erc.selected_data_source = 'faers' AND ROWNUM = 1
                      ''')
        }
        sql("""
                 UPDATE agg_alert set CUM_FATAL_COUNT =  -1 , CUM_SERIOUS_COUNT = -1, CUM_SPON_COUNT = -1, CUM_STUDY_COUNT = -1,          
                 CUMM_COUNT = - 1, CUMM_INTERACTING_COUNT = -1, CUMM_PEDIATRIC_COUNT = -1, NEW_COUNT = -1, NEW_FATAL_COUNT = -1,
                 NEW_INTERACTING_COUNT = -1, NEW_PEDIATRIC_COUNT = -1, NEW_SERIOUS_COUNT  = -1, NEW_SPON_COUNT  = -1, NEW_STUDY_COUNT = -1,
                 ebgm = -1 , eb95 = -1, eb05 = -1, ror_Value = -1, ror05 = -1, ror95 = -1, ror_Mh = -1, prr_Value = -1,
                 prr05 = -1, prr95 = -1, prr_Mh = -1, chi_square = -1, POSITIVE_DECHALLENGE = '-',
                 POSITIVE_RECHALLENGE = '-', RELATED = '-', PREGENENCY = '-', listed = '-', imp_Events = '-', TREND_TYPE = null, FREQ_PRIORITY = null,
                 eb05age = null, eb95age = null, ebgm_age = null, eb05gender = null,eb95gender = null, ebgm_gender = null, prr_str = null, prr_str05 = null,
                 prr_str95 = null, ror_str = null, ror_str05 = null, ror_str95 = null
                 where EXEC_CONFIGURATION_ID IN (SELECT ID FROM EX_RCONFIG WHERE selected_data_source = 'faers')
        """)
    }

    changeSet(author: "amrendra (generated)", id:'1598698970939-63') {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', '''
                      select count(*) from ARCHIVED_AGG_ALERT aga JOIN EX_RCONFIG erc ON (erc.id = aga.EXEC_CONFIGURATION_ID)
                                             where erc.selected_data_source = 'faers' AND ROWNUM = 1
                      ''')
        }
        sql("""
            merge into ARCHIVED_AGG_ALERT t using ( 
                                    Select  '{ "executedAlertConfigurationId": ' || EXEC_CONFIGURATION_ID || ',"productId" : ' || PRODUCT_ID ||  ',"ptCode" : "' || PT_CODE ||  '","smqCode" : ' || COALESCE(SMQ_CODE,'null') ||
                                            ',"newCountFaers" : ' || NEW_COUNT || ',"cummCountFaers" : ' || CUMM_COUNT || ',"newSeriousCountFaers" : ' || NEW_SERIOUS_COUNT || ',"cumSeriousCountFaers" : ' || CUM_SERIOUS_COUNT
                                             ||   ',"ebgmFaers" : ' || CASE WHEN ebgm > 0 and ebgm < 1 then '0' || to_char(ebgm) else to_char(ebgm) END || ',"eb95Faers" : ' || CASE WHEN eb95 > 0 and eb95 < 1 then '0' || to_char(eb95) else to_char(eb95) END ||
                                             ',"eb05Faers" : ' || CASE WHEN eb05 > 0 and eb05 < 1 then '0' || to_char(eb05) else to_char(eb05) END || ',"newSponCountFaers" : ' || NEW_SPON_COUNT || ',"cumSponCountFaers" : ' || CUM_SPON_COUNT || 
                                             ',"prrValueFaers" : ' || CASE WHEN prr_value > 0 and prr_value < 1 then '0' || to_char(prr_value) else to_char(prr_value) END ||
                                             ',"prr95Faers" : ' || CASE WHEN PRR95 > 0 and PRR95 < 1 then '0' || to_char(PRR95) else to_char(PRR95) END ||
                                             ',"prr05Faers" : ' || CASE WHEN prr05 > 0 and prr05 < 1 then '0' || to_char(prr05) else to_char(prr05) END ||
                                             ',"rorValueFaers" : ' || CASE WHEN ror_value > 0 and ror_value < 1 then '0' || to_char(ror_value) else to_char(ror_value) END ||
                                             ',"ror95Faers" : ' || CASE WHEN ROR95 > 0 and ROR95 < 1 then '0' || to_char(ROR95) else to_char(ROR95) END ||
                                             ',"ror05Faers" : ' || CASE WHEN ROR05 > 0 and ROR05 < 1 then '0' || to_char(ROR05) else to_char(ROR05) END ||
                                             ',"chiSquareFaers" : ' || CASE WHEN CHI_SQUARE > 0 and CHI_SQUARE < 1 then '0' || to_char(CHI_SQUARE) else to_char(CHI_SQUARE) END ||
                                             ',"prrMhFaers" : "' || PRR_MH || '","rorMhFaers" : "' || ROR_MH || '","newStudyCountFaers" : ' || NEW_STUDY_COUNT || ',"cumStudyCountFaers" : ' || CUM_STUDY_COUNT || ',"newInteractingCountFaers" : ' || NEW_INTERACTING_COUNT || ',"cummInteractingCountFaers" : ' 
                                             || CUMM_INTERACTING_COUNT || ',"newFatalCountFaers" : ' || NEW_FATAL_COUNT || ',"cumFatalCountFaers" : ' || CUM_FATAL_COUNT || ',"listedFaers" : "' || LISTED || '","impEventsFaers" : "' || COALESCE(IMP_EVENTS,'') || 
                                             '","positiveDechallengeFaers" : "' || POSITIVE_DECHALLENGE || '","positiveRechallengeFaers" : "' || POSITIVE_RECHALLENGE || '","pregenencyFaers" : "' || PREGENENCY || '","relatedFaers" : "' || RELATED || '","trendTypeFaers" : "' 
                                             || TREND_TYPE || '","freqPriorityFaers" : "' || FREQ_PRIORITY || '","newPediatricCountFaers" : ' || NEW_PEDIATRIC_COUNT ||  ', "cummPediatricCountFaers": ' || CUMM_PEDIATRIC_COUNT || ' } ' as faers_columns,
                                             aga.id, eb05age, eb95age, ebgm_age, eb05gender, eb95gender, ebgm_gender, prr_str, prr_str05, prr_str95, ror_str, ror_str05, ror_str95
                                             from ARCHIVED_AGG_ALERT aga JOIN EX_RCONFIG erc ON (erc.id = aga.EXEC_CONFIGURATION_ID)
                                             where erc.selected_data_source = 'faers'
                                            ) s 
                                             on (t.id = s.id) 
                                             when matched then update set t.faers_columns = s.faers_columns,
                                             t.eb05age_faers = dbms_lob.substr(s.eb05age,dbms_lob.getlength(s.eb05age),1),
                                             t.eb95age_faers = dbms_lob.substr(s.eb95age,dbms_lob.getlength(s.eb95age),1),
                                             t.ebgm_age_faers = dbms_lob.substr(s.ebgm_age,dbms_lob.getlength(s.ebgm_age),1),
                                             t.eb05gender_faers = dbms_lob.substr(s.eb05gender,dbms_lob.getlength(s.eb05gender),1),
                                             t.eb95gender_faers = dbms_lob.substr(s.eb95gender,dbms_lob.getlength(s.eb95gender),1),
                                             t.ebgm_gender_faers = dbms_lob.substr(s.ebgm_gender,dbms_lob.getlength(s.ebgm_gender),1),
                                             t.prr_str_faers = dbms_lob.substr(s.prr_str,dbms_lob.getlength(s.prr_str),1),
                                             t.prr_str05faers = dbms_lob.substr(s.prr_str05,dbms_lob.getlength(s.prr_str05),1),
                                             t.prr_str95faers = dbms_lob.substr(s.prr_str95,dbms_lob.getlength(s.prr_str95),1),
                                             t.ror_str_faers = dbms_lob.substr(s.ror_str,dbms_lob.getlength(s.ror_str),1),
                                             t.ror_str05faers = dbms_lob.substr(s.ror_str05,dbms_lob.getlength(s.ror_str05),1),
                                             t.ror_str95faers = dbms_lob.substr(s.ror_str95,dbms_lob.getlength(s.ror_str95),1);
            """)
    }

    changeSet(author: "amrendra (generated)", id:'1598698970939-64') {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', '''
                      select count(*) from ARCHIVED_AGG_ALERT aga JOIN EX_RCONFIG erc ON (erc.id = aga.EXEC_CONFIGURATION_ID)
                                             where erc.selected_data_source = 'faers' AND ROWNUM = 1
                      ''')
        }
        sql("""
                 UPDATE ARCHIVED_AGG_ALERT set CUM_FATAL_COUNT =  -1 , CUM_SERIOUS_COUNT = -1, CUM_SPON_COUNT = -1, CUM_STUDY_COUNT = -1,          
                 CUMM_COUNT = - 1, CUMM_INTERACTING_COUNT = -1, CUMM_PEDIATRIC_COUNT = -1, NEW_COUNT = -1, NEW_FATAL_COUNT = -1,
                 NEW_INTERACTING_COUNT = -1, NEW_PEDIATRIC_COUNT = -1, NEW_SERIOUS_COUNT  = -1, NEW_SPON_COUNT  = -1, NEW_STUDY_COUNT = -1,
                 ebgm = -1 , eb95 = -1, eb05 = -1, ror_Value = -1, ror05 = -1, ror95 = -1, ror_Mh = -1, prr_Value = -1,
                 prr05 = -1, prr95 = -1, prr_Mh = -1, chi_square = -1, POSITIVE_DECHALLENGE = '-',
                 POSITIVE_RECHALLENGE = '-', RELATED = '-', PREGENENCY = '-', listed = '-', imp_Events = '-', TREND_TYPE = null, FREQ_PRIORITY = null,
                 eb05age = null, eb95age = null, ebgm_age = null, eb05gender = null,eb95gender = null, ebgm_gender = null, prr_str = null, prr_str05 = null,
                 prr_str95 = null, ror_str = null, ror_str05 = null, ror_str95 = null
                 where EXEC_CONFIGURATION_ID IN (SELECT ID FROM EX_RCONFIG WHERE selected_data_source = 'faers')
        """)
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-65") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKj3x5c42aklughf7f6acvddvk5")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKj3x5c42aklughf7f6acvddvk5")
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-66") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK2jg2pehpoo65k30r8hp0ynaia")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK2jg2pehpoo65k30r8hp0ynaia")
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-67") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK5lld0tglw6bwtcm3q9v72afnw")
        }
        dropForeignKeyConstraint(baseTableName: "LITERATURE_CASE_ALERT_TAGS", constraintName: "FK5lld0tglw6bwtcm3q9v72afnw")
    }

    changeSet(author: "amrendra (generated)", id: "1598698970939-68") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK5395n1fvj46kencfit7o01ycm")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK5395n1fvj46kencfit7o01ycm")
    }

    changeSet(author: "ankit (generated)", id: "1598698970939-70") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FORMAT')
        }
        sql("alter table ARCHIVED_AGG_ALERT modify FORMAT VARCHAR2(8000 CHAR);")
    }
}

