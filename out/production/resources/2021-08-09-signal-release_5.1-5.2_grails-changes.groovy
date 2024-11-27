import com.rxlogix.Constants
import com.rxlogix.EmergingIssueService
import com.rxlogix.config.EVDASDateRangeInformation
import com.rxlogix.config.ExecutedEVDASDateRangeInformation
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.signal.SignalNotificationMemo
import com.rxlogix.signal.ViewInstance
import grails.converters.JSON

databaseChangeLog = {
    changeSet(author: "suraj (generated)", id: "1608626578695-1") {
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
    changeSet(author: "ujjwal (generated)", id: "1608626578695-14") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_NOTIFICATION_MEMO', columnName: 'SIGNAL_SOURCE')
        }
        grailsChange {
            change {
                List<SignalNotificationMemo> signalNotificationMemoList = SignalNotificationMemo.findAllBySignalSource(Constants.NULL_STRING)
                signalNotificationMemoList.each { SignalNotificationMemo signalNotificationMemo ->
                    signalNotificationMemo.signalSource = null
                    signalNotificationMemo.save(flush:true)
                }
                confirm "Successfully Updated Signal source field in SIGNAL_NOTIFICATION_MEMO Table."
            }
        }
    }
    changeSet(author: "Isha (generated)", id: "1608626578695-20") {
        grailsChange {
            change{
                try {
                    List<EVDASDateRangeInformation> evdasDateRangeList = EVDASDateRangeInformation.findAllByDateRangeStartAbsoluteIsNullAndDateRangeEndAbsoluteIsNullAndDateRangeEnum('CUMULATIVE')
                    List<ExecutedEVDASDateRangeInformation> executedEvdasDateRangeList = ExecutedEVDASDateRangeInformation.findAllByDateRangeStartAbsoluteIsNullAndDateRangeEndAbsoluteIsNullAndDateRangeEnum('CUMULATIVE')
                    evdasDateRangeList.each {
                        it.dateRangeStartAbsolute = new Date(EVDASDateRangeInformation.MIN_DATE)
                        it.dateRangeEndAbsolute = new Date()
                        it.save(flush: true)
                    }
                    executedEvdasDateRangeList.each {
                        it.dateRangeStartAbsolute = new Date(ExecutedEVDASDateRangeInformation.MIN_DATE)
                        it.dateRangeEndAbsolute = new Date()
                        it.save(flush: true)
                    }

                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while setting date range for cumulative EVDAS Alerts ###########")
                }
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608626578695-23") {
        not {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM EMERGING_ISSUE WHERE PRODUCT_SELECTION IS NOT NULL OR PRODUCT_GROUP_SELECTION IS NOT NULL;")
        }
        grailsChange {
            change {
                try {
                    EmergingIssueService emergingIssueService = ctx.emergingIssueService
                    List <EmergingIssue> emergingIssueList = EmergingIssue.findAllByProductSelectionIsNotNullOrProductGroupSelectionIsNotNull()
                    emergingIssueList.each { EmergingIssue emergingIssue ->
                        try {
                            List products = []
                            if (emergingIssue.productSelection) {
                                Map productMap = emergingIssueService.prepareProductMap(emergingIssue.productSelection, emergingIssue.dataSourceDict)
                                products += productMap.keySet()
                                products += productMap.values().flatten()
                            }
                            if (emergingIssue.productGroupSelection) {
                                products.add(emergingIssueService.getGroupNameFieldFromJsonProduct(emergingIssue.productGroupSelection, emergingIssue.dataSourceDict))
                            }
                            emergingIssue.products = products.join(',')
                            emergingIssue.save()
                        }catch(Exception ex){
                            println ex
                        }
                    }

                    confirm "Successfully Updated products in EMERGING_ISSUE Table."
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while updating products in emerging_issue for migration id 1608626578695-10 ###########")
                }
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608626578695-24") {
        not {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM EMERGING_ISSUE")
        }
        grailsChange {
            change {
                try {
                    EmergingIssueService emergingIssueService = ctx.emergingIssueService
                    List <EmergingIssue> emergingIssueList = EmergingIssue.getAll()
                    emergingIssueList.each { EmergingIssue emergingIssue ->
                        try {
                            List events = []
                            if (emergingIssue.eventName) {
                                Map eventMap = emergingIssueService.prepareEventMap(emergingIssue.eventName, [])
                                events += eventMap.keySet()
                                events += eventMap.values().flatten()
                            }
                            if (emergingIssue.eventGroupSelection) {
                                events.add(emergingIssueService.getGroupNameFieldFromJson(emergingIssue.eventGroupSelection))
                            }
                            emergingIssue.events = events.join(',')
                            emergingIssue.save()
                        }catch(Exception ex){
                            println ex
                        }
                    }

                    confirm "Successfully Updated events in EMERGING_ISSUE Table."
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while updating events in emerging_issue for migration id 1608626578695-13 ###########")
                }
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608626578695-100") {
        grailsChange {
            change {
                try {

                    ProductViewAssignment."pva".withTransaction{
                        def f = ProductViewAssignment."pva".createCriteria().list {
                            eq("hierarchy", "Product Group")
                            isEmpty("usersAssigned")
                            isEmpty("groupsAssigned")
                        }

                        f.each{ ProductViewAssignment assignment->
                            Map productMap = JSON.parse(assignment.product)
                            List finalProducts = ProductViewAssignment."pva".createCriteria().list {
                                sqlRestriction("JSON_VALUE(product,'\$.id') = ${productMap?.id}")
                                ne("id", assignment.id)
                            }
                            if (finalProducts) {
                                assignment.delete(flush: true, failOnError: true)
                            }
                        }
                    }

                    confirm "Removed all unassigned duplicate product group :)"
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while removing all unassigned duplicate product group :( ###########")
                }
            }
        }
    }
}