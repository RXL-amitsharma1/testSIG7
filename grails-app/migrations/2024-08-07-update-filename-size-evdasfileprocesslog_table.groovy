import com.rxlogix.Constants
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.ViewInstance
import groovy.json.JsonSlurper

databaseChangeLog = {
    changeSet(author: "Mohit kumar", id: "14072023104999-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_FILE_PROCESS_LOG', columnName: 'FILE_NAME')
        }
        sql("alter table EVDAS_FILE_PROCESS_LOG ALTER COLUMN FILE_NAME TYPE VARCHAR(1000);")
    }
    changeSet(author: "Mohit kumar", id: "14072023104999-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_FILE_PROCESS_LOG', columnName: 'SAVED_NAME')
        }
        sql("alter table EVDAS_FILE_PROCESS_LOG ALTER COLUMN SAVED_NAME TYPE VARCHAR(1000);")
    }
    //Added for PVS-72928
    changeSet(author: "Shoaib Akhtar", id: "1724851056631") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'UNDOABLE_DISP', columnName: 'PREV_JUSTIFICATION')
        }
        sql("ALTER TABLE UNDOABLE_DISP ALTER COLUMN PREV_JUSTIFICATION TYPE VARCHAR(16000);")
    }  
    changeSet(author: "yogesh (generated)", id: "1724927685688-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VIEW_INSTANCE')
        }
        grailsChange {
            change {
                try {
                    List<ViewInstance> viewInstanceList = ViewInstance.createCriteria().list {
                        ilike('alertType', "%${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}%")
                        ilike('alertType', "%${Constants.AlertConfigTypeShort.ON_DEMAND}%")
                        ilike('columnSeq', "%${Constants.AlertConfigTypeShort.CONTAINER_2_STRING}%") // Search for text pattern within columnSeq
                    }

                    if(viewInstanceList?.size()>0){
                        viewInstanceList.each{
                            def jsonSlurper = new JsonSlurper()
                            def data = jsonSlurper.parseText(it.columnSeq)
// Iterate through each entry and update containerView if it is 2
                            data.each { key, value ->
                                if (value.containerView == 2) {
                                    value.containerView = 3
                                }
                            }
                            it.columnSeq = JsonOutput.toJson(data)
                            it.save(flush:true)
                        }

                    }
                }
                catch (Exception ex) {
                    println("########Some error occurred while updating container number for aggregate alert######")
                    ex.printStackTrace()
                }
            }
        }
    }
}