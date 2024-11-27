package com.rxlogix.mapping

import com.rxlogix.enums.StageKeyEnum

class EtlScheduleResult implements Serializable {

    Integer stageId
    StageKeyEnum stageKey
    Integer stepId
    Integer etlExecutionId
    Date startTime
    Date finishTime
    String passStatus

    static mapping = {
        datasource "pva"
        table name: "V_PVR_ETL_MASTER"
        cache: "read-only"
        version false

        id composite:["stageId","stepId"], generator: 'assigned'

        stageKey column: "STAGE_KEY"
        stepId column: "STEP_ID"
        startTime column: "START_TIME"
        finishTime column: "FINISH_TIME"
        passStatus column: "PASS_STATUS"
        etlExecutionId column: "ETL_EXECUTION_ID"
    }


    static constraints = {
        //id(nullable:true)
        startTime nullable: true
        stageId nullable: true
        stageKey nullable: true
        stepId nullable: true
        etlExecutionId nullable: true

    }

    def getSelectableQuery() {
        EtlScheduleResult.withTransaction {
            EtlScheduleResult.where {
                isNotNull('stepId')
            }.order('startTime', 'desc').order('stepId', 'asc')
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof EtlScheduleResult)) return false

        EtlScheduleResult that = (EtlScheduleResult) o

        if (etlExecutionId != that.etlExecutionId) return false
        if (finishTime != that.finishTime) return false
        if (id != that.id) return false
        if (passStatus != that.passStatus) return false
        if (stageId != that.stageId) return false
        if (stageKey != that.stageKey) return false
        if (startTime != that.startTime) return false
        if (stepId != that.stepId) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (stageId != null ? stageId.hashCode() : 0)
        result = 31 * result + (stageKey != null ? stageKey.hashCode() : 0)
        result = 31 * result + (stepId != null ? stepId.hashCode() : 0)
        result = 31 * result + (etlExecutionId != null ? etlExecutionId.hashCode() : 0)
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0)
        result = 31 * result + (finishTime != null ? finishTime.hashCode() : 0)
        result = 31 * result + (passStatus != null ? passStatus.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }

}
