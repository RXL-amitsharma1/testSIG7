import com.rxlogix.EmergingIssueService
import com.rxlogix.signal.EmergingIssue


databaseChangeLog = {
    changeSet(author: "nikhil (generated)", id: "160632973283-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM EMERGING_ISSUE WHERE PRODUCT_SELECTION IS NOT NULL OR PRODUCT_GROUP_SELECTION IS NOT NULL;")
            }
        }
        grailsChange {
            change {
                try {
                    EmergingIssueService emergingIssueService = ctx.emergingIssueService
                    List<EmergingIssue> emergingIssueList = EmergingIssue.findAllByProductSelectionIsNotNullOrProductGroupSelectionIsNotNull()
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
                        } catch (Exception ex) {
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

    changeSet(author: "nikhil (generated)", id: "1608788734763-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM EMERGING_ISSUE")
            }
        }
        grailsChange {
            change {
                try {
                    EmergingIssueService emergingIssueService = ctx.emergingIssueService
                    List<EmergingIssue> emergingIssueList = EmergingIssue.getAll()
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
                        } catch (Exception ex) {
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
}


