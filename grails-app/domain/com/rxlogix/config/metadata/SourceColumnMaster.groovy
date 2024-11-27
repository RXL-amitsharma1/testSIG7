package com.rxlogix.config.metadata
import com.rxlogix.enums.DateRangeTypeCaseEnum

/**
 * Column mapping with its relationship to a LM_ table and TABLE_NAME, and a PRIMARY_KEY_ID as a PK constraint.
 *
 * Example: CASE_MASTER.COUNTRY_ID
 *      PRIMARY_KEY_ID: null because its not a PK of CASE_MASTER
 *      LM_TABLE_NAME : LM_COUNTRIES joined on COUNTRY_ID
 *      LM_DECODE_COLUMN : COUNTRY (this is how you get the name of the country from a COUNTRY_ID)
 */
class SourceColumnMaster {
    SourceTableMaster tableName     // should be joined to the ArgusTableMaster
    String columnName
    Long primaryKey
    SourceTableMaster lmTableName   // should be joined to the ArgusTableMaster
    String lmJoinColumn
    String lmDecodeColumn
    String columnType
    String reportItem
    String lmJoinType
    String concatField
    boolean isDeleted = false
    Integer minColumns
    boolean isEudraField = true

    static mapping = {
        version false
        cache: "read-only"

        table name: "SOURCE_COLUMN_MASTER"

        tableName column: "TABLE_NAME_ATM_ID"
        columnName column: "COLUMN_NAME"
        primaryKey column: "PRIMARY_KEY_ID"
        lmTableName column: "LM_TABLE_NAME_ATM_ID"
        lmJoinColumn column: "LM_JOIN_COLUMN"
        lmDecodeColumn column: "LM_DECODE_COLUMN"
        columnType column: "COLUMN_TYPE"
        reportItem column: "REPORT_ITEM"
        lmJoinType column:"LM_JOIN_EQUI_OUTER"   // E for Eq Join O for Outer Join
        id name: "reportItem", generator: "assigned"
        concatField column: "CONCATENATED_FIELD"
        isDeleted column: "IS_DELETED"
        minColumns column: "MIN_COLUMNS"
        isEudraField column : "IS_EUDRAFIELD"
    }

    static constraints = {
        tableName(nullable:false)
        columnName(maxSize: 40)
        primaryKey(nullable:true)
        lmTableName(nullable: true)
        lmJoinColumn(maxSize: 40, nullable: true)
        lmDecodeColumn(maxSize:40, nullable: true)
        columnType(maxSize:1)
        reportItem(maxSize:80, nullable: false) // oracle doesn't like implicit unique on column which will already be set as the ID
        lmJoinType(maxSize:1, nullable: true)
        concatField(maxSize:1, nullable: true)
        minColumns(nullable: true)
    }

    def static getFollowupReportDateColumn(DateRangeTypeCaseEnum dateRangeType) {
        def followupReportDateCol = null
        if (dateRangeType == DateRangeTypeCaseEnum.SAFTEY_RECEIPT_DATE) {
            followupReportDateCol = this.findByReportItem("CF_SAFETY_DATE").columnName
        } else if (dateRangeType == DateRangeTypeCaseEnum.CREATION_DATE) {
            followupReportDateCol = this.findByReportItem("CF_TIME_STAMP").columnName
        } else if (dateRangeType == DateRangeTypeCaseEnum.CASE_RECEIPT_DATE) {
            followupReportDateCol = this.findByReportItem("CF_RECEIPT_DATE").columnName
        }
        return followupReportDateCol
    }

    def static getRevMasterDateColumn(DateRangeTypeCaseEnum dateRangeType) {
        def revMasterDateCol = null
        if (dateRangeType == DateRangeTypeCaseEnum.SAFTEY_RECEIPT_DATE) {
            revMasterDateCol = this.findByReportItem("CRMST_VERSION_SAFETY_DATE")?.columnName
        } else if (dateRangeType == DateRangeTypeCaseEnum.CREATION_DATE) {
            revMasterDateCol = this.findByReportItem("CRMST_VERSION_CREATE_DATE")?.columnName
        } else if (dateRangeType == DateRangeTypeCaseEnum.CASE_RECEIPT_DATE) {
            revMasterDateCol = this.findByReportItem("CRMST_VERSION_RECEIPT_DATE")?.columnName
        } else if (dateRangeType == DateRangeTypeCaseEnum.CASE_LOCKED_DATE) {
            revMasterDateCol = this.findByReportItem("CRMST_LOCKED_DATE")?.columnName
        }
        return revMasterDateCol
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof SourceColumnMaster)) return false

        SourceColumnMaster that = (SourceColumnMaster) o

        if (columnName != that.columnName) return false
        if (columnType != that.columnType) return false
        if (concatField != that.concatField) return false
        if (lmDecodeColumn != that.lmDecodeColumn) return false
        if (lmJoinColumn != that.lmJoinColumn) return false
        if (lmJoinType != that.lmJoinType) return false
        if (lmTableName != that.lmTableName) return false
        if (primaryKey != that.primaryKey) return false
        if (reportItem != that.reportItem) return false
        if (tableName != that.tableName) return false
        if (minColumns != that.minColumns) return false
        if (isEudraField != that.isEudraField) return false
        return true
    }

    int hashCode() {
        int result
        result = tableName.hashCode()
        result = 31 * result + columnName.hashCode()
        result = 31 * result + (primaryKey != null ? primaryKey.hashCode() : 0)
        result = 31 * result + (lmTableName != null ? lmTableName.hashCode() : 0)
        result = 31 * result + (lmJoinColumn != null ? lmJoinColumn.hashCode() : 0)
        result = 31 * result + (lmDecodeColumn != null ? lmDecodeColumn.hashCode() : 0)
        result = 31 * result + columnType.hashCode()
        result = 31 * result + reportItem.hashCode()
        result = 31 * result + (lmJoinType != null ? lmJoinType.hashCode() : 0)
        result = 31 * result + (concatField != null ? concatField.hashCode() : 0)
        result = 31 * result + (minColumns != null ? minColumns.hashCode() : 0)
        result = 31 * result + (isEudraField ? 1 : 0)
        return result
    }
}
