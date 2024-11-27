import com.rxlogix.config.EVDASDateRangeInformation
import com.rxlogix.config.ExecutedEVDASDateRangeInformation
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.EmergingIssueService

databaseChangeLog = {
    changeSet(author: "rishabh (generated)", id: "1622615055829-2") {
        not {
            columnExists(tableName: 'emerging_issue', columnName: 'product_group_selection')
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1622615055829-3") {
        not {
            columnExists(tableName: 'emerging_issue', columnName: 'product_selection')
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "product_selection", type: "CLOB")
        }
    }

    changeSet(author: "rishabh (generated)", id: "1622615055829-4") {
        not {
            columnExists(tableName: 'emerging_issue', columnName: 'data_source_dict')
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "data_source_dict", type: "varchar2(255 CHAR)")
        }
    }
    changeSet(author: "Nitesh (generated)", id: "1622615055829-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_TEMPLATE_ALERT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_TEMPLATE_ALERT", type: "number(1,0)", defaultValueBoolean: "false"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Isha (generated)", id: "1622615055829-7") {
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

    changeSet(author: "rishabh (generated)", id: "1622615055829-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'emerging_issue', columnName: 'products')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "products", type: "varchar2(8000 CHAR)")
        }
    }


    changeSet(author: "rishabh (generated)", id: "1622615055829-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'emerging_issue', columnName: 'events')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "events", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "rishabh (generated)", id: "1622615055829-22") {
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
                    println("######### Error occurred while updating products in emerging_issue for migration id 1622615055829-10 ###########")
                }
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1622615055829-23") {
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
                    println("######### Error occurred while updating events in emerging_issue for migration id 1622615055829-13 ###########")
                }
            }
        }
    }
}
