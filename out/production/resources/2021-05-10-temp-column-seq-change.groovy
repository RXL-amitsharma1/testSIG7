import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.ViewInstance
import grails.converters.JSON
    databaseChangeLog = {
        changeSet(author: "suraj (generated)", id: "1620633908896-178") {
            preConditions(onFail: 'MARK_RAN') {
                columnExists(tableName: 'VIEW_INSTANCE', columnName: 'TEMP_COLUMN_SEQ')
            }
            grailsChange {
                change {
                    try {
                        List<ViewInstance> viewInstanceList = ViewInstance.findAllByAlertTypeAndTempColumnSeqIsNotNull("Signal Management")
                        viewInstanceList.each { viewInstance ->
                            if (viewInstance.user) {
                                Map viewInstanceCollection = JSON.parse(viewInstance.columnSeq)
                                viewInstance.tempColumnSeq = JsonOutput.toJson(viewInstanceCollection)
                                viewInstance.save(flush: true)
                            }
                        }
                    } catch (Exception ex) {
                        println(ex)
                        println("##################### Error updating temp_column_seq #############")
                    }
                }
            }
        }
        changeSet(author: "suraj (generated)", id: "1622453026565-909") {
            preConditions(onFail: 'MARK_RAN') {
                sqlCheck(expectedResult: '1', "select Count(*) from user_constraints where constraint_name = UPPER('UC_VALIDATED_SIGNALNAME_COL');")
            }
            dropUniqueConstraint(constraintName: "UC_VALIDATED_SIGNALNAME_COL", tableName: "VALIDATED_SIGNAL")
        }
        changeSet(author: "suraj (generated)", id: "1624446646-1") {
            preConditions(onFail: 'MARK_RAN') {
                not {
                    columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'IS_DUEDATE_UPDATED')
                }
            }
            addColumn(tableName: "VALIDATED_SIGNAL") {
                column(name: "IS_DUEDATE_UPDATED", type: "number(1, 0)", defaultValueBoolean: 'false') {
                    constraints(nullable: "true")
                }
            }
        }
    }
