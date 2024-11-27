package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.gorm.DetachedCriteria

class EtlCaseTableStatus implements Serializable {

    String tableName
    Date stageStartTime
    Date stageEndTime
    Date transformationStartTime
    Date transformationEndTime

    static mapping = {
        datasource "pva"
        table name: "V_PVR_ETL_CASE_TABLE_STATUS"
        cache: "read-only"
        version false

        id composite:["tableName","stageStartTime"]

        tableName column: "TABLE_NAME"
        stageStartTime column: "STAGE_START_TIME"
        stageEndTime column: "STAGE_END_TIME"
        transformationStartTime column: "TRANSFORM_START_TIME"
        transformationEndTime column: "TRANSFORM_END_TIME"

    }

    static constraints = {
        id(nullable:true, unique:true)
        tableName unique: true, nullable: false
        stageStartTime nullable: true;
        stageEndTime nullable: true;
        transformationStartTime nullable: true;
        transformationEndTime nullable: true;

    }

    def getSelectableQuery() {
        EtlCaseTableStatus.withTransaction {
            EtlCaseTableStatus.where {
                isNotNull('stageStartTime')
            }.order('stageStartTime', 'desc').order('tableName', 'asc')
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof EtlCaseTableStatus)) return false

        EtlCaseTableStatus that = (EtlCaseTableStatus) o

        if (id != that.id) return false
        if (stageEndTime != that.stageEndTime) return false
        if (stageStartTime != that.stageStartTime) return false
        if (tableName != that.tableName) return false
        if (transformationEndTime != that.transformationEndTime) return false
        if (transformationStartTime != that.transformationStartTime) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (tableName != null ? tableName.hashCode() : 0)
        result = 31 * result + (stageStartTime != null ? stageStartTime.hashCode() : 0)
        result = 31 * result + (stageEndTime != null ? stageEndTime.hashCode() : 0)
        result = 31 * result + (transformationStartTime != null ? transformationStartTime.hashCode() : 0)
        result = 31 * result + (transformationEndTime != null ? transformationEndTime.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }
}
