import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.signal.ValidatedSignal
import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "bhupender (generated)", id: "202306050-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'DATE_CLOSED_DISPOSITION')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "DATE_CLOSED_DISPOSITION", type: "varchar2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "20230605-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ACTUAL_DATE_CLOSED')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ACTUAL_DATE_CLOSED", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "20230605-03"){
        grailsChange{
            change{
                try {
                    List<ValidatedSignal> validatedSignalList = ValidatedSignal.getAll()
                    validatedSignalList?.each { validatedSignal ->
                        validatedSignal.signalStatusHistories?.each { signalStatusHistory ->
                            if(signalStatusHistory.signalStatus == Constants.WorkFlowLog.DATE_CLOSED){
                                validatedSignal.actualDateClosed = signalStatusHistory.dateCreated
                                validatedSignal.save(flush:true)
                            }
                        }
                    }
                }
                catch (Exception ex){
                    println "#### Error occurred while updating validated signal"
                    ex.printStackTrace()
                }
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "202306050-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'DATE_CLOSED_DISPOSITION_WORKFLOW')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "DATE_CLOSED_DISPOSITION_WORKFLOW", type: "varchar2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "202306050-05") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'DATE_CLOSED_WORKFLOW')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "DATE_CLOSED_WORKFLOW", type: "varchar2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "20230605-06") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'IS_DISPOSITION')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "IS_DISPOSITION", type: "number(1,0)", defaultValueBoolean: "true") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "20230605-07") {
        grailsChange{
            change{
                Sql sql = null
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    List<Map> updatedSignalMap = []
                    List<Map> signalMap = []
                    sql.eachRow("select id,signal_status from validated_signal where actual_date_closed is null ") { row ->
                        signalMap.add(id: row[0], signalStatus: row[1])
                    }
                    println "original map for validated Signal" + signalMap
                    signalMap?.each { signal ->
                        signal.signalStatus = 'Ongoing'

                        updatedSignalMap.add(id: signal.id, signalStatus: signal.signalStatus)
                    }
                    println "updatedSignalMap for signal = " + updatedSignalMap
                    sql.withBatch(100, "UPDATE validated_signal SET signal_status = :signalStatus WHERE ID = :id", { preparedStatement ->
                        updatedSignalMap?.each { signal ->
                            preparedStatement.addBatch(id: signal.id, signalStatus: signal.signalStatus)
                        }
                    })
                } catch(Exception ex){
                    println "#### Error while updating validating signal information."
                    println (ex.getMessage())
                }finally{
                    sql?.close()
                }
            }
        }
    }
}