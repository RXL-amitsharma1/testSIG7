package com.rxlogix.config.metadata

/**
 * This is used to construct the compilable ReportSQL
 */
class SourceTableMaster {

    String tableName
    String tableAlias
    String tableType
    Integer caseJoinOrder
    String caseJoinType
    String versionedData
    Integer hasEnterpriseId
    boolean isDeleted = false
    boolean isEudraField = true

    static mapping = {
        version false
        cache: "read-only"

        table "SOURCE_TABLE_MASTER"

        tableName column: "TABLE_NAME"              // Name of the case table as it exists in the db ( CASE_MASTER )
        tableAlias column: "TABLE_ALIAS"            // Alias used in the generated SQL ( CASE_MASTER = cm )
        tableType column: "TABLE_TYPE"              // CASE or LM flag ( tells us if its source data or a reference list)
        caseJoinOrder column: "CASE_JOIN_ORDER"     // The order in which the SQL joins should be constructed
        caseJoinType column:"CASE_JOIN_EQUI_OUTER"   // E for Eq Join O for Outer Join
        versionedData column:"VERSIONED_DATA"        // V for historical versioned data  Join S for straight Copy
        hasEnterpriseId column:"HAS_ENTERPRISE_ID"   // Flag for presence of TENANT_ID column in the table
        id name: "tableName", generator: "assigned"
        isDeleted column: "IS_DELETED"
        isEudraField column : "IS_EUDRAFIELD"
    }

    static constraints = {
        tableName(maxSize:40, nullable:false) // oracle doesn't like implicit unique on column which will already be set as the ID
        tableAlias(maxSize:10)
        tableType(maxSize:1)
        caseJoinOrder(nullable:true)
        caseJoinType(maxSize:1, nullable: true)
        versionedData(maxSize:1, nullable: true)
        hasEnterpriseId(maxSize:1, nullable:true)
    }

// this method is to compare pva _ app value with pvr one

    boolean equals(o) {

        if (this.is(o)) return true
        if (!(o instanceof SourceTableMaster)) return false

        SourceTableMaster that = (SourceTableMaster) o

        if (caseJoinOrder != that.caseJoinOrder) return false
        if (caseJoinType != that.caseJoinType) return false
        if (tableAlias != that.tableAlias) return false
        if (tableName != that.tableName) return false
        if (tableType != that.tableType) return false
        if (versionedData != that.versionedData) return false
        if (hasEnterpriseId != that.hasEnterpriseId) return false
        if (isEudraField != that.isEudraField) return false

        return true
    }

    int hashCode() {
        int result
        result = tableName.hashCode()
        result = 31 * result + (tableAlias != null ? tableAlias.hashCode() : 0)
        result = 31 * result + (tableType != null ? tableType.hashCode() : 0)
        result = 31 * result + (caseJoinOrder != null ? caseJoinOrder.hashCode() : 0)
        result = 31 * result + (caseJoinType != null ? caseJoinType.hashCode() : 0)
        result = 31 * result + (versionedData != null ? versionedData.hashCode() : 0)
        result = 31 * result + (hasEnterpriseId != null ? hasEnterpriseId.hashCode() : 0)
        result = 31 * result + (isEudraField ? 1 : 0)
        return result
    }

}
