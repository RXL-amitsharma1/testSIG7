databaseChangeLog = {
    changeSet(author: "rahul (generated)", id: "1678951326-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"RECHALLENGE")
        }
        modifyDataType(columnName: "RECHALLENGE", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-02") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"RECHALLENGE")
        }
        modifyDataType(columnName: "RECHALLENGE", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-03") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"RECHALLENGE")
        }
        modifyDataType(columnName: "RECHALLENGE", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-04") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"IS_SUSAR")
        }
        modifyDataType(columnName: "IS_SUSAR", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-05") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"IS_SUSAR")
        }
        modifyDataType(columnName: "IS_SUSAR", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-06") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"IS_SUSAR")
        }
        modifyDataType(columnName: "IS_SUSAR", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-07") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"MEDICALLY_CONFIRMED")
        }
        modifyDataType(columnName: "MEDICALLY_CONFIRMED", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-08") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"MEDICALLY_CONFIRMED")
        }
        modifyDataType(columnName: "MEDICALLY_CONFIRMED", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-09") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"MEDICALLY_CONFIRMED")
        }
        modifyDataType(columnName: "MEDICALLY_CONFIRMED", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-10") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"COMBO_FLAG")
        }
        modifyDataType(columnName: "COMBO_FLAG", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-11") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"COMBO_FLAG")
        }
        modifyDataType(columnName: "COMBO_FLAG", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-12") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"COMBO_FLAG")
        }
        modifyDataType(columnName: "COMBO_FLAG", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-13") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"DEATH")
        }
        modifyDataType(columnName: "DEATH", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-14") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"DEATH")
        }
        modifyDataType(columnName: "DEATH", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-15") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"DEATH")
        }
        modifyDataType(columnName: "DEATH", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-16") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"PREGNANCY")
        }
        modifyDataType(columnName: "PREGNANCY", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-17") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"PREGNANCY")
        }
        modifyDataType(columnName: "PREGNANCY", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-18") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"PREGNANCY")
        }
        modifyDataType(columnName: "PREGNANCY", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-19") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"PRODUCT_NAME")
        }
        modifyDataType(columnName: "PRODUCT_NAME", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-20") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"PRODUCT_NAME")
        }
        modifyDataType(columnName: "PRODUCT_NAME", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-21") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"PRODUCT_NAME")
        }
        modifyDataType(columnName: "PRODUCT_NAME", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-22") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"AGE")
        }
        modifyDataType(columnName: "AGE", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-23") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"AGE")
        }
        modifyDataType(columnName: "AGE", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-24") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"AGE")
        }
        modifyDataType(columnName: "AGE", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-25") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"COUNTRY")
        }
        modifyDataType(columnName: "COUNTRY", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-26") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"COUNTRY")
        }
        modifyDataType(columnName: "COUNTRY", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-27") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"COUNTRY")
        }
        modifyDataType(columnName: "COUNTRY", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-28") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"OUTCOME")
        }
        modifyDataType(columnName: "OUTCOME", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-29") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"OUTCOME")
        }
        modifyDataType(columnName: "OUTCOME", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-30") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"OUTCOME")
        }
        modifyDataType(columnName: "OUTCOME", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-31") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"EVENT_OUTCOME")
        }
        modifyDataType(columnName: "EVENT_OUTCOME", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-32") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"EVENT_OUTCOME")
        }
        modifyDataType(columnName: "EVENT_OUTCOME", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-33") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"EVENT_OUTCOME")
        }
        modifyDataType(columnName: "EVENT_OUTCOME", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-34") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"GENDER")
        }
        modifyDataType(columnName: "GENDER", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-35") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"GENDER")
        }
        modifyDataType(columnName: "GENDER", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-36") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"GENDER")
        }
        modifyDataType(columnName: "GENDER", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-37") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"REPORTERS_HCP_FLAG")
        }
        modifyDataType(columnName: "REPORTERS_HCP_FLAG", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-38") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"REPORTERS_HCP_FLAG")
        }
        modifyDataType(columnName: "REPORTERS_HCP_FLAG", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-39") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"REPORTERS_HCP_FLAG")
        }
        modifyDataType(columnName: "REPORTERS_HCP_FLAG", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-40") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"LISTEDNESS")
        }
        modifyDataType(columnName: "LISTEDNESS", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-41") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"LISTEDNESS")
        }
        modifyDataType(columnName: "LISTEDNESS", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-42") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"LISTEDNESS")
        }
        modifyDataType(columnName: "LISTEDNESS", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-43") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"CASE_REPORT_TYPE")
        }
        modifyDataType(columnName: "CASE_REPORT_TYPE", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-44") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"CASE_REPORT_TYPE")
        }
        modifyDataType(columnName: "CASE_REPORT_TYPE", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-45") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"CASE_REPORT_TYPE")
        }
        modifyDataType(columnName: "CASE_REPORT_TYPE", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-46") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"SERIOUS")
        }
        modifyDataType(columnName: "SERIOUS", newDataType: "varchar2(16000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-47") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"SERIOUS")
        }
        modifyDataType(columnName: "SERIOUS", newDataType: "varchar2(16000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-48") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"SERIOUS")
        }
        modifyDataType(columnName: "SERIOUS", newDataType: "varchar2(16000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-49") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"CASE_VERSION")
        }
        modifyDataType(columnName: "CASE_VERSION", newDataType: "NUMBER(19,0)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-50") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"CASE_VERSION")
        }
        modifyDataType(columnName: "CASE_VERSION", newDataType: "NUMBER(19,0)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-51") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"CASE_VERSION")
        }
        modifyDataType(columnName: "CASE_VERSION", newDataType: "NUMBER(19,0)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-52") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"DATE_OF_BIRTH")
        }
        modifyDataType(columnName: "DATE_OF_BIRTH", newDataType: "varchar2(2000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-53") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"DATE_OF_BIRTH")
        }
        modifyDataType(columnName: "DATE_OF_BIRTH", newDataType: "varchar2(2000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-54") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"DATE_OF_BIRTH")
        }
        modifyDataType(columnName: "DATE_OF_BIRTH", newDataType: "varchar2(2000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-55") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"EVENT_ONSET_DATE")
        }
        modifyDataType(columnName: "EVENT_ONSET_DATE", newDataType: "varchar2(4000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-56") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"EVENT_ONSET_DATE")
        }
        modifyDataType(columnName: "EVENT_ONSET_DATE", newDataType: "varchar2(4000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-57") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"EVENT_ONSET_DATE")
        }
        modifyDataType(columnName: "EVENT_ONSET_DATE", newDataType: "varchar2(4000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-58") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"INDICATION")
        }
        modifyDataType(columnName: "INDICATION", newDataType: "varchar2(32000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-59") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"INDICATION")
        }
        modifyDataType(columnName: "INDICATION", newDataType: "varchar2(32000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-60") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"INDICATION")
        }
        modifyDataType(columnName: "INDICATION", newDataType: "varchar2(32000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-61") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"PROTOCOL_NO")
        }
        modifyDataType(columnName: "PROTOCOL_NO", newDataType: "varchar2(2000 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-62") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"PROTOCOL_NO")
        }
        modifyDataType(columnName: "PROTOCOL_NO", newDataType: "varchar2(2000 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-63") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"PROTOCOL_NO")
        }
        modifyDataType(columnName: "PROTOCOL_NO", newDataType: "varchar2(2000 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "rahul (generated)", id: "1678951326-64") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName:"CASE_NUMBER")
        }
        modifyDataType(columnName: "CASE_NUMBER", newDataType: "varchar2(1020 char)", tableName: "SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-65") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName:"CASE_NUMBER")
        }
        modifyDataType(columnName: "CASE_NUMBER", newDataType: "varchar2(1020 char)", tableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }
    changeSet(author: "rahul (generated)", id: "1678951326-66") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName:"CASE_NUMBER")
        }
        modifyDataType(columnName: "CASE_NUMBER", newDataType: "varchar2(1020 char)", tableName: "SINGLE_ON_DEMAND_ALERT")
    }
    changeSet(author: "Nikhil (generated)", id: "1613989814626-501") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'is_deleted')
            }
        }

        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "true", default : "0" )
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1681369328-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'EVENT_ID')
            }
        }
        addColumn(tableName: "PRODUCT_EVENT_HISTORY") {
            column(name: "EVENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "hritik (generated)", id: "1681809616-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'EVENT_ID')
            }
        }
        addColumn(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "EVENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }



}
