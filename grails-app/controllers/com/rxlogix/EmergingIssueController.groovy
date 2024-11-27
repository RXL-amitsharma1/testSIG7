package com.rxlogix

import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.enums.ReportFormat
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.hibernate.Session
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.rxlogix.util.DateUtil
import org.springframework.context.MessageSource
import grails.util.Holders
import com.rxlogix.util.AlertUtil


@Secured(["isAuthenticated()"])
class EmergingIssueController implements AlertUtil{

    def emergingIssueService
    def dynamicReportService
    def userService
    def sessionFactory
    MessageSource messageSource
    def signalAuditLogService
    def dataObjectService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        //  1) Need to add the datasource in the review screen as well.
        //  2) Emerging safety issues button on the review screen.
        //  3) PE combination full comparison screen.
        //  4) A small modal window for pe combination comparison.
        EmergingIssue emergingIusse = new EmergingIssue()
        Boolean hasConfigurationEditorRole = SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")?true:false
        render(view: "index", model: [emergingIusseList: emergingIusse, hasConfigurationEditorRole: hasConfigurationEditorRole,isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM) ,callingScreen: "index"])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def emergingIssueMap = emergingIssueService.getEmergingIssueList(params)
        render emergingIssueMap as JSON
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save(Boolean ime, Boolean dme, Boolean emergingIssue, Boolean specialMonitoring, String eventSelection, String eventGroupSelection) {
        EmergingIssue emergingIssueObject = new EmergingIssue()
        if (eventSelection && eventSelection != '[]') {
            emergingIssueObject.eventName = eventSelection
        } else {
            emergingIssueObject.eventName = null
        }
        emergingIssueObject.eventName = eventSelection

        if(params.productGroupSelectionAssessment && params.productGroupSelectionAssessment != '[]'){
            emergingIssueObject.productGroupSelection=params.productGroupSelectionAssessment
        } else {
            emergingIssueObject.productGroupSelection=null
        }
        if (params.productSelection && params.productSelection != '[]') {
            emergingIssueObject.productSelection = params.productSelectionAssessment
        } else {
            emergingIssueObject.productSelection = null
        }
        emergingIssueObject.isMultiIngredient = Boolean.parseBoolean(params.isMultiIngredient)
        if (eventGroupSelection != '[]') {
            emergingIssueObject.eventGroupSelection = eventGroupSelection
        } else {
            emergingIssueObject.eventGroupSelection = null
        }
        emergingIssueObject.ime = ime ?: false
        emergingIssueObject.dme = dme ?: false
        emergingIssueObject.emergingIssue = emergingIssue ?: false
        emergingIssueObject.specialMonitoring = specialMonitoring ?: false
        if(emergingIssueObject.productSelection || emergingIssueObject.productGroupSelection) {
            emergingIssueObject.dataSourceDict = params.dataSourceDict
        }
        if((eventGroupSelection == '[]' || !eventGroupSelection) && !eventSelection){
            flash.error = message(code: "com.rxlogix.emergingIssue.eventName.required") //update changes for PVS-70350
            render(view: "index", model: [emergingIusseList: emergingIssueObject, callingScreen: "edit"])
        }
        List products = []
        if (emergingIssueObject.productSelection) {
            Map productMap = emergingIssueService.prepareProductMap(emergingIssueObject.productSelection, emergingIssueObject.dataSourceDict)
            products += productMap.keySet()
            products += productMap.values().flatten()
        }
        if (emergingIssueObject.productGroupSelection) {
            products.add(emergingIssueService.getGroupNameFieldFromJsonProduct(emergingIssueObject.productGroupSelection, emergingIssueObject.dataSourceDict))
        }
        emergingIssueObject.products = products.join(',')

        List events = []
        if (emergingIssueObject.eventName) {
            Map eventMap = emergingIssueService.prepareEventMap(emergingIssueObject.eventName, [])
            events += eventMap.keySet()
            events += eventMap.values().flatten()
        }
        if (emergingIssueObject.eventGroupSelection) {
            events.add(emergingIssueService.getGroupNameFieldFromJson(emergingIssueObject.eventGroupSelection))
        }
        emergingIssueObject.events = events.join(',')

        emergingIssueService.save(emergingIssueObject)
        redirect(view: "index")
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id) {
        EmergingIssue emergingIssue = EmergingIssue.read(id)
        def productSelection = emergingIssue?.productSelection
        def jsonSlurper = new JsonSlurper()
        render(view: "index", model: [emergingIusseList: emergingIssue, isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM), callingScreen: "edit"])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update(Boolean ime, Boolean dme, Boolean emergingIssue, Boolean specialMonitoring, String eventSelection, String eventGroupSelection) {
        Map emergingIssueObjectClone=new HashMap();
        EmergingIssue emergingIssueObject = EmergingIssue.get(params.id)
        EmergingIssue emergingIssueObject1 = EmergingIssue.get(params.id)
        emergingIssueObjectClone.put("id",emergingIssueObject1.getId())
        emergingIssueObjectClone.put("productGroupSelection",emergingIssueObject1.getProductGroupSelection()?:"")
        emergingIssueObjectClone.put("products",emergingIssueObject1.getProducts())
        emergingIssueObjectClone.put("productSelection",emergingIssueObject1.getProductSelection()?:"")
        emergingIssueObjectClone.put("eventGroupSelection",emergingIssueObject1.getEventGroupSelection()?:"")
        emergingIssueObjectClone.put("eventName",emergingIssueObject1.getEventName()?:"")
        emergingIssueObjectClone.put("ime",emergingIssueObject1.getIme())
        emergingIssueObjectClone.put("dme",emergingIssueObject1.getDme())
        emergingIssueObjectClone.put("dataSourceDict",emergingIssueObject1.getDataSourceDict())
        emergingIssueObjectClone.put("events",emergingIssueObject1.getEvents())
        emergingIssueObjectClone.put("emergingIssue",emergingIssueObject1.getEmergingIssue())
        emergingIssueObjectClone.put("specialMonitoring",emergingIssueObject1.getSpecialMonitoring())
        emergingIssueObjectClone.put("createdBy",emergingIssueObject1.getCreatedBy())
        emergingIssueObjectClone.put("dateCreated",emergingIssueObject1.getDateCreated())
        emergingIssueObjectClone.put("lastUpdated",emergingIssueObject1.getLastUpdated())
        emergingIssueObjectClone.put("modifiedBy",emergingIssueObject1.getModifiedBy())
        emergingIssueObject.ime = ime ?: false
        emergingIssueObject.dme = dme ?: false
        emergingIssueObject.emergingIssue = emergingIssue?: false
        emergingIssueObject.specialMonitoring = specialMonitoring ?: false
        if(params.productGroupSelectionAssessment && params.productGroupSelectionAssessment != '[]'){
            emergingIssueObject.productGroupSelection=params.productGroupSelectionAssessment
            emergingIssueObject1.productGroupSelection=params.productGroupSelectionAssessment
        } else {
            emergingIssueObject.productGroupSelection=null
            emergingIssueObject1.productGroupSelection=null
        }
        emergingIssueObject.isMultiIngredient = Boolean.parseBoolean(params.isMultiIngredient)

        if (emergingIssueObject.productSelection && emergingIssueObject.productSelection != '[]') {
            emergingIssueObject.productSelection = params.productSelectionAssessment
        } else {
            emergingIssueObject.productSelection = null
        }

        if (eventGroupSelection && eventGroupSelection != '[]') {
            emergingIssueObject.eventGroupSelection = eventGroupSelection
            emergingIssueObject1.eventGroupSelection = eventGroupSelection
        } else {
            emergingIssueObject.eventGroupSelection = null
            emergingIssueObject1.eventGroupSelection = null
        }

        if (eventSelection && eventSelection != '[]') {
            emergingIssueObject.eventName = eventSelection
        } else {
            emergingIssueObject.eventName = null
        }

        if(emergingIssueObject.productSelection || emergingIssueObject.productGroupSelection) {
            emergingIssueObject.dataSourceDict = params.dataSourceDict
            emergingIssueObject1.dataSourceDict = params.dataSourceDict
        }
        if((eventGroupSelection == '[]' || !eventGroupSelection) && !eventSelection){
            flash.error = message(code: "com.rxlogix.emergingIssue.eventName.required") //update changes for PVS-70350
            render(view: "edit", model: [emergingIusseList: emergingIssueObject, callingScreen: "index"])
        }
        emergingIssueService.update(emergingIssueObject,emergingIssueObjectClone)
        emergingIssueObjectClone.clear()
        redirect(view: "index")
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        Map emergingIssueObjectClone=new HashMap();
        EmergingIssue emergingIssue = EmergingIssue.get(id)
        EmergingIssue emergingIssueObject1 = EmergingIssue.get(id)
        emergingIssueObjectClone.put("id",emergingIssueObject1.getId())
        emergingIssueObjectClone.put("productGroupSelection",emergingIssueObject1.getProductGroupSelection()?:"")
        emergingIssueObjectClone.put("products",emergingIssueObject1.getProducts())
        emergingIssueObjectClone.put("productSelection",emergingIssueObject1.getProductSelection()?:"")
        emergingIssueObjectClone.put("eventGroupSelection",emergingIssueObject1.getEventGroupSelection()?:"")
        emergingIssueObjectClone.put("eventName",emergingIssueObject1.getEventName()?:"")
        emergingIssueObjectClone.put("ime",emergingIssueObject1.getIme())
        emergingIssueObjectClone.put("dme",emergingIssueObject1.getDme())
        emergingIssueObjectClone.put("dataSourceDict",emergingIssueObject1.getDataSourceDict())
        emergingIssueObjectClone.put("events",emergingIssueObject1.getEvents())
        emergingIssueObjectClone.put("emergingIssue",emergingIssueObject1.getEmergingIssue())
        emergingIssueObjectClone.put("specialMonitoring",emergingIssueObject1.getSpecialMonitoring())
        emergingIssueObjectClone.put("createdBy",emergingIssueObject1.getCreatedBy())
        emergingIssueObjectClone.put("dateCreated",emergingIssueObject1.getDateCreated())
        emergingIssueObjectClone.put("lastUpdated",emergingIssueObject1.getLastUpdated())
        emergingIssueObjectClone.put("modifiedBy",emergingIssueObject1.getModifiedBy())
        emergingIssueService.delete(emergingIssue,emergingIssueObjectClone)
        flash.message = message(code: "app.label.event.delete.success")
        render(responseDTO as JSON)
    }

    def exportReport(){
        Session session = sessionFactory.currentSession
        try {
            log.info("Exporting important events started")
            List emergingIssueList = emergingIssueService.getEmergingIssueListReport()
            def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
            def metadata = [sheetName: "Important Events",
                            columns  : [
                                    [title: messageSource.getMessage('app.label.product.label', null, locale), width: 25],
                                    [title: messageSource.getMessage('app.reportField.eventName', null, locale), width: 25],
                                    [title: Holders.config.importantEvents.ime.label, width: 25],
                                    [title: Holders.config.importantEvents.dme.label, width: 25],
                                    [title: Holders.config.importantEvents.stopList.label, width: 25],
                                    [title: Holders.config.importantEvents.specialMonitoring.label, width: 25],
                                    [title: messageSource.getMessage('app.important.issue.lastModifiedBy', null, locale), width: 25],
                                    [title: messageSource.getMessage('app.important.issue.lastModified', null, locale), width: 25],
                            ]]
            byte[] file = dynamicReportService.createEmergingIssueReport(emergingIssueList, metadata)

            log.info("Successfully exported important events")
            render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: "Important Events-"+DateUtil.toDateStringWithoutTimezone(new Date()) +"-" + System.currentTimeMillis() + ".xlsx")
            signalAuditLogService.createAuditForExport(null,"Important Events" ,"Important Events" ,params,"Important Events-"+DateUtil.toDateStringWithoutTimezone(new Date()) +"-" + System.currentTimeMillis() +".xlsx")
        } catch (Exception ex) {
            flash.error = message(code: 'app.label.impEvent.export')
            ex.printStackTrace()
            redirect(action: "index")
        } finally {
            session.flush()
            session.clear()
        }
    }

    private renderReportOutputType(File reportFile, Map params) {
        String reportName = "Important Events List" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" +
                "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }
}