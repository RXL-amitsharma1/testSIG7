import com.rxlogix.Constants
import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.EvdasAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.EvdasHistory
import com.rxlogix.signal.ProductEventHistory
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.User
import org.hibernate.criterion.CriteriaSpecification

databaseChangeLog = {
//    >>>>>>>>>>>>>>>>>>>>>>>>>>SINGLE_CASE_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608824568994-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>ARCHIVED_SINGLE_CASE_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608824568994-4") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-5") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-6") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>>>AGG_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608824568994-7") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-8") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-9") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>ARCHIVED_AGG_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608824568994-10") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-11") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-12") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>EVDAS_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608824568994-13") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-14") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-15") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>ARCHIVED_EVDAS_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608824568994-16") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-17") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-18") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-19") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table SINGLE_CASE_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-20") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table ARCHIVED_SINGLE_CASE_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-21") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'CASE_HISTORY', columnName: 'JUSTIFICATION')
        }
        sql("alter table CASE_HISTORY modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-22") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIVITIES', columnName: 'JUSTIFICATION')
        }
        sql("alter table ACTIVITIES modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-23") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table AGG_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-24") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table ARCHIVED_AGG_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-25") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'product_event_history', columnName: 'JUSTIFICATION')
        }
        sql("alter table product_event_history modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-26") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table EVDAS_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-27") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table ARCHIVED_EVDAS_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-28") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'evdas_history', columnName: 'JUSTIFICATION')
        }
        sql("alter table evdas_history modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-29") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_HISTORY', columnName: 'JUSTIFICATION')
        }
        sql("alter table LITERATURE_HISTORY modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-30") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'JUSTIFICATION')
        }
        sql("alter table LITERATURE_ACTIVITY modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608824568994-31") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'DETAILS')
        }
        sql("alter table LITERATURE_ACTIVITY modify DETAILS VARCHAR2(12000 CHAR);")
    }

    changeSet(author: "nitesh (generated)", id: "1608824568994-32") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'changes')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "changes", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568994-33") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'changes')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "changes", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568995-34") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'Trend_Flag')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "Trend_Flag", type:"varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-35") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PROD_N_PERIOD')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PROD_N_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-36") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PROD_N_CUMUL')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PROD_N_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-37") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'FREQ_PERIOD')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "FREQ_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-38") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'FREQ_CUMUL')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "FREQ_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-39") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'EBGM_RR')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "EBGM_RR", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-40") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'EBGM_E')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "EBGM_E", type: "number") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568995-41") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_A')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_A", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-42") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_B')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_B", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-43") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_C')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_C", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-44") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_D')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_D", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-45") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'Trend_Flag')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "Trend_Flag", type:"varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-46") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PROD_N_PERIOD')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PROD_N_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-47") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PROD_N_CUMUL')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PROD_N_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-48") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FREQ_PERIOD')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "FREQ_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-49") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FREQ_CUMUL')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "FREQ_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-50") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'EBGM_RR')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "EBGM_RR", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-51") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'EBGM_E')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "EBGM_E", type: "number") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568995-52") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_A')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_A", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-53") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_B')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_B", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-54") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_C')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_C", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568995-55") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_D')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_D", type: "number") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "rishabh (generated)", id: "1608824568995-56") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CLIPBOARD_CASES', columnName: 'TEMP_CASE_IDS')
            }
        }
        addColumn(tableName: "CLIPBOARD_CASES") {
            column(name: "TEMP_CASE_IDS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }


}
