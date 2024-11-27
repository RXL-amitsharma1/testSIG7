import com.rxlogix.ExceptionHandlingController
import com.rxlogix.config.Priority
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.security.Authorize
import com.rxlogix.signal.Justification
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import grails.util.Holders

@Authorize
class AppUiConfigController implements ExceptionHandlingController {
    def workflowRuleService
    def justificationService
    def validatedSignalService
    def cacheService
    def userService

    String fetchExpectedDispositions(){
        Map responseMap = [:]
        Map dispositionMap =  workflowRuleService.fetchDispositionIncomingOutgoingMap()
        RestApiResponse.successResponseWithData(responseMap,null,dispositionMap)
        render responseMap as JSON
    }

    String fetchJustificationsForDisposition(Long id) {
        Map dataMap = [:]
        Map responseMap = [:]
        List<String> justifications = justificationService.fetchJustificationsForDisposition(id as Long, false)
        dataMap = ["justifications" : justifications]
        RestApiResponse.successResponseWithData(responseMap,null,dataMap)
        render responseMap as JSON
    }

    String fetchSignalsForValidatedConfirmedDispositions(String term, Integer page, Integer pageSize ){
        Map responseMap = [:]
        if(page && pageSize){
            Map dataMap = validatedSignalService.fetchAlertGridSignals(term, page, pageSize)
            RestApiResponse.successResponseWithData(responseMap,null,dataMap)
        }else{
            RestApiResponse.invalidParametersResponse(responseMap)
            render(status: HttpStatus.BAD_REQUEST.value(),text:responseMap as JSON)
            return
        }
        render responseMap as JSON
    }

    String fetchPriorityForAlert() {
        Map responseMap = [:]
        List dataList = []
        dataList = Priority.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "priorityId")
                property("value", "value")
                property("priorityOrder", "priorityOrder")
                property("iconClass", "classNames")
                property("reviewPeriod", "defaultReviewPeriod")
            }
            eq('display', true)
        } as List
        RestApiResponse.successResponseWithData(responseMap, null, dataList)
        render responseMap as JSON
    }

    String fetchJustificationsForPriority() {
        Map responseMap = [:]
        Map dataMap = [:]
        List justifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.justification
        dataMap = [message: justifications]
        RestApiResponse.successResponseWithData(responseMap, null, dataMap)
        render responseMap as JSON
    }


    // not used, will remove
    String fetchPossibleCategories(){
        Map responseMap = [:]
        List data = []
        data = cacheService.getCommonTagCache()
        def parentMap = [:]
        def result = []
        data.each { item ->
            if (item.parentId == null) {
                def parent = [
                        "id": item.id,
                        "name": item.text,
                        "display": item.display,
                        "subcategories": []
                ]
                parentMap[item.id] = parent
                result << parent
            }
        }

        data.each { item ->
            if (item.parentId != null) {
                def parent = parentMap[item.parentId]
                if (parent) {
                    parent.subcategories << [
                            "id": item.id,
                            "name": item.text,
                            "display": item.display
                    ]
                } else {
                    log.warn("Warning: Parent with id ${item.parentId} not found for item with id ${item.id}")
                }
            }
        }
        RestApiResponse.successResponseWithData(responseMap, null, data)
        render responseMap as JSON
    }

    String fetchApplicationConfig() {
        Map responseMap = [:]
        Map resultMap = [:]
        def timeOutInterval = Holders.config.springsession.timeout.interval * 60
        def sessionTimeoutReminder = Holders.config.springsession.timeout.dialogue.display.time * 60
        def keepAliveInterval = (Holders.config.springsession.timeout.interval % 3) * 60
        Map sessionTimeOutConfigs = ["sessionTimeout": timeOutInterval, "sessionTimeoutReminder": sessionTimeoutReminder, "sessionKeepAliveTimer": keepAliveInterval]
        resultMap.put("sessionTimeOutConfigs", sessionTimeOutConfigs)
        RestApiResponse.successResponseWithData(responseMap, null, resultMap)

        render responseMap as JSON

    }


}