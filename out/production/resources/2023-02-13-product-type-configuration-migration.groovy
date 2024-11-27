import com.rxlogix.config.Configuration
import com.rxlogix.signal.ProductTypeConfiguration
import com.rxlogix.signal.ValidatedSignal
import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "rahul (generated)", id: "16772183371-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'migrated_to_mart_date')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "migrated_to_mart_date", type: "TIMESTAMP"){
                constraints(nullable: "true")
            }
        }
    }




    changeSet(author: "uddesh teke (generated)", id: "1475327519312-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'DRUG_TYPE_NAME')
            }
        }

        sql("ALTER TABLE EX_RCONFIG ADD DRUG_TYPE_NAME VARCHAR2(4000 CHAR)")
    }


}