import com.rxlogix.ViewInstanceService
import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "nikhil (generated)", id: "16453738783-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM VIEW_INSTANCE WHERE name= 'System View' AND ALERT_TYPE='Single Case Alert - Faers';")
            }
        }
        grailsChange {
            change {
                try {
                    def grailsApplication = Holders.grailsApplication
                    ViewInstanceService viewInstanceService = ctx.viewInstanceService
                    Map viDetailMap = grailsApplication.config.configurations.viewInstances.find { it.name == "System View" && it.alertType == "Single Case Alert - Faers" }
                    if (viDetailMap) {

                        Map columnOrderMap = viewInstanceService.addOrUpdateColumnMap(viDetailMap)
                        viewInstanceService.updateViewInstanceObjects(columnOrderMap, viDetailMap, null, null)

                        confirm "Successfully Updated View Instance: 'Single Case Alert - Faers'"
                    }
                } catch (Exception ex) {
                    println("######## Some error occured while updating the View Instance  ##########")
                    ex.printStackTrace()
                }
            }
        }
    }

}


