package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.Disposition
import com.rxlogix.config.PvsAppConfiguration
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.signal.SignalWorkflowState
import com.rxlogix.signal.SystemConfig
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.util.CollectionUtils
import grails.util.Holders

import javax.xml.bind.ValidationException
import java.text.DateFormat
import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class ApiControlPanelController {

    def batchRestService

    def userService
    def alertAttributesService
    def CRUDService
    def caseNarrativeConfigurationService
    SignalAuditLogService signalAuditLogService
    BusinessRulesMigrationService businessRulesMigrationService

    @Secured(['ROLE_ADMIN'])
    def index() {
        getETLStatus()
    }

    def updateDisplayDueIn(){
        // update Display Due In Flag
        SystemConfig systemConfig = SystemConfig.first()
        systemConfig.displayDueIn = params.currentStatus == "true"
        CRUDService.save(systemConfig)
        render ([value: systemConfig.displayDueIn] as JSON)
    }


    def updateEndOfMilestone(){
        // update Display Due In Flag
        SystemConfig systemConfig = SystemConfig.first()
        if(!systemConfig.enableSignalWorkflow){
            systemConfig.enableEndOfMilestone = params.currentStatus == "true"
            CRUDService.save(systemConfig)
        }
        render([value:  systemConfig.enableEndOfMilestone] as JSON)
    }

    def updateSelectedEndpoints(){
        // update Display Due In Flag
        SystemConfig systemConfig = SystemConfig.first()
        if(!systemConfig.enableEndOfMilestone){
            systemConfig.selectedEndPoints = params.selectedEndPoints
        }
        CRUDService.save(systemConfig)
        render([value:  systemConfig.selectedEndPoints] as JSON)
    }

    def updateDateClosedBasedOnDisposition() {
        SystemConfig systemConfig = SystemConfig.first()
        List dateClosedDispList = params.dateClosedDisposition ? params.dateClosedDisposition.split(',') : []
        List dispositionList = Disposition.findAllBySignalStatusForDueDate(Constants.WorkFlowLog.DATE_CLOSED).collect {
            it.displayName
        }
        def commonElements = dateClosedDispList.intersect(dispositionList)
        if (commonElements && systemConfig.enableEndOfMilestone) {
            render([msg: "${message(code: 'controlPanel.updateDateClosedBasedOnDisposition.allowed')}"] as JSON)
        } else {
            systemConfig.dateClosedDisposition = params.dateClosedDisposition
            CRUDService.save(systemConfig)
            log.info("Updated the value for Date Closed Based On Disposition to ${params.dateClosedDisposition}")
            render([value: systemConfig.dateClosedDisposition] as JSON)
        }
    }

    def updateDateClosedBasedOnWorkflow() {
        SystemConfig systemConfig = SystemConfig.first()
        String updatedValue = params.dateClosedWorkflow
        if (systemConfig.isDisposition) {
            systemConfig.dateClosedDispositionWorkflow = updatedValue
        } else {
            systemConfig.dateClosedWorkflow = updatedValue
        }
        CRUDService.save(systemConfig)
        log.info("Updated the value for Date Closed Based On Disposition/Workflow to ${updatedValue}")
        render([value: updatedValue] as JSON)
    }

    def updateDispositionEndPoints(){
        // update Display Due In Flag
        // parameters for Audit log entries
        String oldDispositionValues, newDispositionValues, oldSignalStatus='', newSignalStatus
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        SystemConfig systemConfig = SystemConfig.first()
        List dateClosedBasedOnDispList = systemConfig.dateClosedDisposition ? systemConfig.dateClosedDisposition.split(',') : []
        def dispositionIdList =[]
        Boolean isUpdatePossible = true
        def unlinkedDispositionIdList =params.get('previousDispositionIds')?.split(",")
        if(params.get('dispositionIdList[]') && params.get('dispositionIdList[]').class == String){
            dispositionIdList << params.get('dispositionIdList[]')
        }else if(params.get('dispositionIdList[]')){
            dispositionIdList = params.get('dispositionIdList[]') as List
        }
        // temporary list for disposition names
        List tempDispositionList = []
        unlinkedDispositionIdList.each {
            def disposition = Disposition.get(it)
            oldSignalStatus = disposition?.signalStatusForDueDate
            tempDispositionList.add(disposition?.displayName)
        }
        if(unlinkedDispositionIdList){
            unlinkedDispositionIdList-=dispositionIdList
        }

        if(params.signalStatus.equalsIgnoreCase(Constants.WorkFlowLog.DATE_CLOSED)){
            for (String id : dispositionIdList){
                Disposition disposition = Disposition.get(id as Long)
                if(dateClosedBasedOnDispList?.contains(disposition.displayName)){
                    isUpdatePossible = false
                    responseDTO.message = "${message(code: 'controlPanel.updateDispositionEndPoint.allowed')}"
                    break
                }
            }
        }
        // list of dispositions where signal status is removed
        //consider case when we change signal status to such value that it already includes
        newSignalStatus = params.signalStatus
        oldDispositionValues = tempDispositionList.join(',')
        tempDispositionList.clear()
        if(isUpdatePossible){
            unlinkedDispositionIdList.each {
                def disposition = Disposition.get(it)
                disposition.signalStatusForDueDate = null
                CRUDService.save(disposition)
            }
            dispositionIdList.each{
                def disposition = Disposition.get(it)
                tempDispositionList.add(disposition.displayName)
                disposition.signalStatusForDueDate = params.signalStatus
                CRUDService.save(disposition)
            }
        }
        newDispositionValues = tempDispositionList.join(',')
        if(isUpdatePossible) {
            createAuditForEndOfReview(oldDispositionValues, newDispositionValues, oldSignalStatus, newSignalStatus)
        }
        responseDTO.data = dispositionEndPointTableHtml()
        render(responseDTO as JSON)
    }

    def createAuditForEndOfReview(String oldDispositionValues, String newDispositionValues, String oldSignalStatus, String newSignalStatus){
        String auditEntityName = "Disposition End of Review Milestone Configuration: Signal Disposition(s)- ${newDispositionValues}, Signal Status- ${newSignalStatus}"
        if (oldDispositionValues == "") {
            // If unlinkedDispositionIdList is empty || null== new record is created
            signalAuditLogService.createAuditLog([
                    entityName : "Control Panel",
                    moduleName : "Control Panel",
                    category   : AuditTrail.Category.INSERT.toString(),
                    entityValue: auditEntityName,
                    username   : userService.getUser().username,
                    fullname   : userService.getUser().fullName
            ] as Map, [[propertyName: "Signal Disposition(s)", oldValue: oldDispositionValues, newValue: newDispositionValues],
                       [propertyName: "Signal Status", oldValue: oldSignalStatus, newValue: newSignalStatus]] as List)
        } else {
            // else record is updated
            // disposition old value new value,
            // signal status old value new value
            List auditChildList = []
            if (oldDispositionValues != newDispositionValues) {
                auditChildList << [propertyName: "Signal Disposition(s)", oldValue: oldDispositionValues, newValue: newDispositionValues]
            }
            if (oldSignalStatus != newSignalStatus) {
                auditChildList << [propertyName: "Signal Status", oldValue: oldSignalStatus, newValue: newSignalStatus]
            }
            signalAuditLogService.createAuditLog([
                    entityName : "Control Panel",
                    moduleName : "Control Panel",
                    category   : AuditTrail.Category.UPDATE.toString(),
                    entityValue: auditEntityName,
                    username   : userService.getUser().username,
                    fullname   : userService.getUser().fullName
            ] as Map, auditChildList as List)
        }
    }

    def dispositionEndPointTable() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        responseDTO.data = dispositionEndPointTableHtml()
        render(responseDTO as JSON)
    }

    def deleteByEndPoint() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        List dispositionList = []
        if(params.signalStatus){
            def dispositionIdList = Disposition.findAllBySignalStatusForDueDate(params.signalStatus).collect{
                it.id
            }
            dispositionIdList?.each{
                def disposition = Disposition.get(it)
                dispositionList.add(disposition?.displayName)
                disposition.signalStatusForDueDate = null
                CRUDService.save(disposition)
            }
        }
        // Record Deleted Entry for milestone endpoint
        String auditEntityName = "Disposition End of Review Milestone Configuration: Signal Disposition(s)- ${dispositionList.join(',')}, Signal Status- ${params.signalStatus}"
        signalAuditLogService.createAuditLog([
                entityName : "Control Panel",
                moduleName : "Control Panel",
                category   : AuditTrail.Category.DELETE.toString(),
                entityValue: auditEntityName,
                username   : userService.getUser().username,
                fullname   : userService.getUser().fullName
        ] as Map, [[propertyName: "isDeleted", oldValue: "false", newValue: "true"]] as List)
        responseDTO.data = dispositionEndPointTableHtml()
        render(responseDTO as JSON)
    }

    String dispositionEndPointTableHtml(){
        String tableHtml
        List endPointList = []
        List selectedDispositionList = []
        Disposition.findAllBySignalStatusForDueDateIsNotNull().each {
            selectedDispositionList.add(it.displayName)
            if(!endPointList.contains(it.signalStatusForDueDate)){
                endPointList.add(it.signalStatusForDueDate)
            }
        }
        List finalList = []
        endPointList.each {
            Map map = [:]
            map.endPoint = it
            List list = Disposition.findAllByReviewCompletedAndSignalStatusForDueDate(true, it).collect {disposition->
                [text: disposition.displayName, value : disposition.id]
            }
            map.dispositions = list
            finalList.add(map)
        }
        List dispositionList = Disposition.findAllByReviewCompletedAndSignalStatusForDueDateIsNull(true).collect {
            [value: it.id, text:it.displayName]
        }
        List selectableDispositions = dispositionList
        def dueInEndpointValues = alertAttributesService.get('signalHistoryStatus') as List<String>
        List selectableEndPointValues = dueInEndpointValues-endPointList
        def x =[finalList: finalList, dueInEndpointValues: dueInEndpointValues, dispositionList: dispositionList,selectedDispositionList: selectedDispositionList, selectableDispositions: selectableDispositions]

        tableHtml = g.render(template: '/validatedSignal/includes/dispositionEndpointTable', model: [
                finalList: finalList,
                dueInEndpointValues: dueInEndpointValues,
                dispositionList: dispositionList,
                selectedDispositionList: selectedDispositionList,
                selectableDispositions: selectableDispositions,
                selectableEndPointValues: selectableEndPointValues
        ])
        tableHtml
    }

    Map getETLStatus() {
        List<BatchLotStatus> batchLotStatuses = batchRestService.getLastETLBatchLots()
        List<BatchLotStatus> lastSuccessedBatchLotStatuses = batchRestService.getLastSuccessfullETLBatchLots()
        String lastEtlStatus = null;
        Date lastEtlDate = null;
        String lastEtlBatchIds = "";
        if(!CollectionUtils.isEmpty(batchLotStatuses)) {
            if(isELTStatus(batchLotStatuses, null) ) {
                lastEtlStatus = "RUNNING";
            } else if(isELTStatus(batchLotStatuses, "STARTED") ) {
                lastEtlStatus = "RUNNING";
            } else if(isELTStatus(batchLotStatuses, "FAILED") ) {
                lastEtlStatus = "FAILED";
            } else if(isELTStatus(batchLotStatuses, "COMPLETED") ) {
                if(batchRestService.getLastBatchLotETLNotSuccessfulCount()) {
                    lastEtlStatus = "SUCCESS";
                } else {
                    lastEtlStatus = "COMPLETED_BUT_FAILED";
                }
            }
        }
        if(!CollectionUtils.isEmpty(lastSuccessedBatchLotStatuses)) {
            lastSuccessedBatchLotStatuses.eachWithIndex { item, index ->
                if(lastEtlDate==null) {
                    lastEtlDate=item.getEtlStartDate();
                } else if(lastEtlDate.before(item.getEtlStartDate())) {
                    lastEtlDate=item.getEtlStartDate();
                }
                lastEtlBatchIds = (lastEtlBatchIds.trim().length()==0)?(item.getBatchId()):(lastEtlBatchIds+", "+item.getBatchId());
            }
        }
        if(lastEtlStatus==null) {
            lastEtlStatus="NOT STARTED"
        }
        User user = userService.getUser()
        PvsAppConfiguration etlPvrConnectivityCheck = PvsAppConfiguration.findByKey(Constants.ENABLE_ALERT_EXECUTION)
        PvsAppConfiguration signalChartsConfiguration = PvsAppConfiguration.findByKey(Constants.ENABLE_SIGNAL_CHARTS)
        PvsAppConfiguration pgUpdateCheck = PvsAppConfiguration.findByKey(Constants.PRODUCT_GROUP_UPDATE)
        SystemConfig systemConfig = SystemConfig.first()
        def dueInEndpointValues = Holders.config.pvsignal.signalHistoryStatus
        List<String> signalStatusList = alertAttributesService.get('signalHistoryStatus') as List<String>
        List<String> signalWorkflowStatesList = SignalWorkflowState.findAll().collect{
            it.value
        }
        List reviewCompletedDisplist = Disposition.findAllByReviewCompleted(true).collect {disposition->
            disposition.displayName
        }
        List dateClosedWorkflowList = []
        if(systemConfig.enableSignalWorkflow){
            signalStatusList.addAll(signalWorkflowStatesList)
            dateClosedWorkflowList = systemConfig.isDisposition ? reviewCompletedDisplist : SignalWorkflowState.list().collect{ it.displayName }
        }

        [lastEtlStatus               : lastEtlStatus,
         lastEtlDate                 : getDateInStringFormat(lastEtlDate, DateUtil.DATEPICKER_FORMAT_AM_PM_3),
         lastEtlBatchIds             : lastEtlBatchIds,
         remainingBatchLotCountForETL: batchRestService.getRemainingBatchLotCountForETL(),
         pvdETLCompleted             : batchRestService.isPVDETLCompleted(),
         apiToken                    : user?.preference?.apiToken,
         etlPvrConnectivityCheck     : etlPvrConnectivityCheck?.booleanValue,
         isEnableSignalCharts        : signalChartsConfiguration.booleanValue,
         pgUpdateCheck               : pgUpdateCheck?.booleanValue,
         isDisplayDueIn              : systemConfig.displayDueIn,
         isEndOfMilestone            : systemConfig.enableEndOfMilestone,
         selectedEndPoints           : systemConfig.selectedEndPoints as String,
         dateClosedDisposition       : systemConfig.dateClosedDisposition as String,
         dateClosedWorkflow          : systemConfig.dateClosedWorkflow as String,
         dateClosedWorkflowDisposition: systemConfig.dateClosedDispositionWorkflow as String,
         isDisposition               : systemConfig.isDisposition,
         dueInEndpointValues         : dueInEndpointValues,
         signalStatusList            : signalStatusList,
         isSignalWorkflowEnabled     : systemConfig.enableSignalWorkflow,
         reviewCompletedDispList     : reviewCompletedDisplist,
         dateClosedWorkflowList      : dateClosedWorkflowList,
         promptUser                  : caseNarrativeConfigurationService.isPromptUserEnabled(),
         exportAlways                : caseNarrativeConfigurationService.isExportAlwaysEnabled(),
         rulesMigrationRequired      : businessRulesMigrationService.checkMigrationRequired()
        ]
    }



    boolean isELTStatus(List<BatchLotStatus> batchLotStatuses, String status) {
        boolean isELTStatus = false
        batchLotStatuses.eachWithIndex { item, index ->
            if(item.getEtlStatus()==status ) {
                isELTStatus = true
            }
        }
        isELTStatus
    }

    Map getETLRunStatus() {
        render getETLStatus() as JSON
    }

    def runApiETL() {
        def count = batchRestService.runEtlForRemainingApiBatchLot()
        Map map = getETLStatus()
        map.put("count", count)
        render map as JSON

    }
    def getDateInStringFormat(Date date, String format) {
        String formattedDate = null
        try {
            //DateFormat dateFormat = new SimpleDateFormat(format);
            if(date!=null) {
                //formattedDate = dateFormat.format(date)
                formattedDate = DateUtil.stringFromDate(date, format, "UTC")
            }
        }catch (Exception ex) {
            log.error(date+"->"+ex.toString())
        }
        formattedDate
    }

    def toggleEtlPvrConnectivityCheck(boolean currentStatus) {
        PvsAppConfiguration pvsAppConfiguration = PvsAppConfiguration.findByKey(Constants.ENABLE_ALERT_EXECUTION)
        try {
            if (pvsAppConfiguration) {
                pvsAppConfiguration.booleanValue = currentStatus
                CRUDService.updateWithAuditLog(pvsAppConfiguration)
                render([isEnabledEtlPvrConnectivity: currentStatus] as JSON)
            }
        }
        catch (Exception exception) {
            exception.printStackTrace()
        }
    }

    def enableSignalChartsCheck(boolean currentStatus) {
        PvsAppConfiguration pvsAppConfiguration = PvsAppConfiguration.findByKey(Constants.ENABLE_SIGNAL_CHARTS)
        try {
            if (pvsAppConfiguration) {
                pvsAppConfiguration.booleanValue = currentStatus
                CRUDService.updateWithAuditLog(pvsAppConfiguration)
                render([signalChartsStatus: currentStatus] as JSON)
            }
        }
        catch (Exception exception) {
            exception.printStackTrace()
        }
    }

    def enablePGUpdateCheck(boolean currentStatus) {
        PvsAppConfiguration pvsAppConfiguration = PvsAppConfiguration.findByKey(Constants.PRODUCT_GROUP_UPDATE)
        try {
            if (pvsAppConfiguration) {
                pvsAppConfiguration.booleanValue = currentStatus
                CRUDService.updateWithAuditLog(pvsAppConfiguration)
                render([isEnabledPGUpdate: currentStatus] as JSON)
            }
        }
        catch (Exception exception) {
            exception.printStackTrace()
        }
    }
    def updateExportAlways(){
        ResponseDTO responseDTO = caseNarrativeConfigurationService.setExportAlways("true".equals(params.currentStatus))
        render (responseDTO as JSON)
    }

    def updatePromptUser(){
        ResponseDTO responseDTO = caseNarrativeConfigurationService.setPromptUser("true".equals(params.currentStatus))
        render (responseDTO as JSON)
    }

    def getValuesforWorkflowDropdown(){
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        SystemConfig systemConfig = SystemConfig.first()
        if(params.selectedOption == 'signalDisposition'){
            List reviewCompletedDisplist = Disposition.findAllByReviewCompleted(true).collect {disposition->
                disposition.displayName
            }
            responseDTO.data = [list: reviewCompletedDisplist, isDisposition: true]
            systemConfig.isDisposition = true
        } else{
            List workflowList = SignalWorkflowState.list().collect{
                it.displayName
            }
            responseDTO.data = [list: workflowList, isDisposition: false]
            systemConfig.isDisposition = false
        }
        CRUDService.save(systemConfig)
        render(responseDTO as JSON)
    }


}
