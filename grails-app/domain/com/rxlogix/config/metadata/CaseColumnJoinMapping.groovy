package com.rxlogix.config.metadata

/**
 * This will give the mapping of SQL join columns between multiple case tables.
 *
 * Example the following DB tables: CASE_EVENT is joined with CASE_MASTER table using CASE_ID
 * Example : CASE_EVENT_ASSESS is joined with CASE_MASTER.CASE_ID and CASE_PRODUCT.SEQ_NUM and CASE_EVENT.SEQ_NUM
 */
class CaseColumnJoinMapping {

    Long id
    SourceTableMaster tableName     // should be joined to the ReportTableMapping
    String columnName


    SourceTableMaster mapTableName  // should be joined to the ReportTableMapping
    String mapColumnName
    boolean isDeleted = false

    static mapping = {
        version false
        cache: "read-only"

        table name: "CASE_COLUMN_JOIN_MAPPING"

        tableName column: "TABLE_NAME_ATM_ID"
        columnName column: "COLUMN_NAME"            // DB Column name on which to join ( CASE_ID )
        mapTableName column: "MAP_TABLE_NAME_ATM_ID"
        mapColumnName column: "MAP_COLUMN_NAME"
        isDeleted column: "IS_DELETED"

    }

    static constraints = {
        tableName(nullable:false)
        columnName(blank:false, maxSize:40)
        mapTableName(nullable:false)
        mapColumnName(blank:false, maxSize:40)
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof CaseColumnJoinMapping)) return false

        CaseColumnJoinMapping that = (CaseColumnJoinMapping) o

        if (columnName != that.columnName) return false
//        if (id != that.id) return false
        if (mapColumnName != that.mapColumnName) return false
        if (mapTableName != that.mapTableName) return false
        if (tableName != that.tableName) return false
//        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = id.hashCode()
        result = 31 * result + tableName.hashCode()
        result = 31 * result + columnName.hashCode()
        result = 31 * result + mapTableName.hashCode()
        result = 31 * result + mapColumnName.hashCode()
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }

}
