package com.rxlogix

import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.ProdAssignmentProcessState
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.ProductAssignmentLog
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.io.FilenameUtils
import com.rxlogix.exception.FileFormatException
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.config.AlertGroup


import javax.servlet.http.HttpSession
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

@Secured(["isAuthenticated()"])
class ProductAssignmentController {

    def productAssignmentService
    def productAssignmentImportService
    def userService
    def cacheService
    def dynamicReportService
    def attachmentableService
    def singleCaseAlertService
    HttpSession session = request.getSession()
    static def config = Holders.config
    def dataSource
    def dataSource_pva
    SignalAuditLogService signalAuditLogService

    def index() {
        Boolean isProductView = params.isProductView =="true"

        List columnOrder = getColumnOrder(isProductView, true)
        render(view: "index", model: [columnOrder  : (columnOrder as JSON).toString(), isEdit: SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_MANAGE_PRODUCT_ASSIGNMENTS"),
                                      isProductView: isProductView])
    }

    List getColumnOrder(Boolean isProductView, Boolean isIndex) {
        User user = userService.getUser()
        boolean isUpdate = !user.colOrder || user.colUserOrder
        if (!user.colOrder) {
            List sessionProductViewList = [
                    [name: "alertGroup", label: "Alert Group", seq: 1, listOrder: 0, containerView: 1],
                    [name: "product", label: "Product", seq: 2, listOrder: 0, containerView: 1],
                    [name: "hierarchy", label: "Product Hierarchy", seq: 3, listOrder: 1, containerView: 1],
                    [name: "assignedUserOrGroup", label: "Assignment", seq: 4, listOrder: 2, containerView: 1],
                    [name: "workflowGroup", label: "Workflow Group", seq: 6, listOrder: 3, containerView: 1],
                    [name: "createdBy", label: "User Id", seq: 5, listOrder: 0, containerView: 3]
            ]
            user.colOrder = new JsonBuilder(sessionProductViewList).toString()
        }
        if (!user.colUserOrder) {
            List sessionUserViewList = [
                    [name: "assignedUserOrGroup", label: "Assignment", seq: 3, listOrder: 0, containerView: 1],
                    [name: "product", label: "Product", seq: 1, listOrder: 1, containerView: 1],
                    [name: "hierarchy", label: "Product Hierarchy", seq: 2, listOrder: 2, containerView: 1],
                    [name: "workflowGroup", label: "Workflow Group", seq: 5, listOrder: 3, containerView: 1],
                    [name: "createdBy", label: "User Id", seq: 4, listOrder: 4, containerView: 3]
            ]
            user.colUserOrder = new JsonBuilder(sessionUserViewList).toString()
        }

        if (isUpdate)
            user.save(flush: true)

        if (isProductView) {
            return new JsonSlurper().parseText(user.colOrder) as List
        } else {
            return new JsonSlurper().parseText(user.colUserOrder) as List
        }
    }
    def fetchProductAssignment(DataTableSearchRequest searchRequest) {
        List dicList = PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }
        Map dicMap = [:]
        dicList.eachWithIndex { value, index ->
            dicMap[index] = value
        }
        Map requestMap = JSON.parse(params.args)
        Map userMap = cacheService.getAllUsers()
        Map groupMap = cacheService.getAllGroups()
        Map result = [:]
        if (requestMap.isProductView) {
            result = productAssignmentService.fetchValuesForProductView(requestMap, searchRequest, dicMap, groupMap, userMap)
        } else {
            result = productAssignmentService.fetchValuesForUserView(requestMap, searchRequest, dicMap, groupMap, userMap)
        }
        render result as JSON
    }

    //Commented code for bug pvs-54073
    /*
    Boolean checkIfSelectionIsValid(String selectedProducts){
        Boolean isNotValidSelection = false
        if(selectedProducts){
            Pattern pattern = Pattern.compile("^[a-zA-Z0-9. -_?]*")
            Boolean isValidProductName = true
            Map productMap = JSON.parse(selectedProducts)
            productMap.each{
                if(it.value){
                    it.value.each { Map map->
                        Matcher matcher = pattern.matcher(map.name)
                        isValidProductName = matcher.matches()
                        if(!isValidProductName || map.name.contains("(J)")){
                            isNotValidSelection = true
                        }
                    }
                }
            }
        }
        return isNotValidSelection
    }*/

    @Secured(["ROLE_MANAGE_PRODUCT_ASSIGNMENTS"])
    def saveProductAssignment() {
        //changing for this bug(PVS-54073) As per current requirement should support all unicode characters. Commenting this code as per this was older requirement not supported japanese(unicode character).
        /*if(params.selectedProducts){
            Boolean isNotValidSelection = checkIfSelectionIsValid(params.selectedProducts)
            if(isNotValidSelection){
                render([status: "warning"] as JSON)
                return
            }
        }*/

        List dicList = PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }
        Map dicMap = [:]
        dicList.eachWithIndex{value,index->
            dicMap[index] = value
        }
        Long workflowGroup = null
        Boolean isWfUpdate = true
        if(params.primaryUserOrGroup && params.primaryUserOrGroup.contains(Constants.USER_TOKEN)){
            Integer userId = params.primaryUserOrGroup.replace(Constants.USER_TOKEN,"") as Integer
            workflowGroup = User.get(userId)?.workflowGroup?.id
        } else {
            isWfUpdate = false
        }
        Map productsAndAssignment = prepareDataForCRUDOperation(params.selectedUserOrGroup,params.selectedProducts,params.selectedProductGroups)
        Map products = productsAndAssignment.products
        List assignmentList = productsAndAssignment.assignmentList
        try {
            productAssignmentService.saveProductAssignments (products, assignmentList, dicMap, params, workflowGroup, isWfUpdate)
        } catch (Exception ex) {
            ex.printStackTrace()
            if(ex.toString().contains("Error due to Alert Group present in different workflow groups.")){
                render([status: "fail", errorMessage: "Alert Group cannot be mapped to more than one Workflow Group."] as JSON)
            } else if(ex.toString().contains("Error Due to Primary Assignee has an assignment in another alert Group.")){
                render([status: "fail", errorMessage: "A primary assignee cannot be assigned to more than one Alert Group."] as JSON)
            } else {
                render([status: "fail", errorMessage: "Error while saving."] as JSON)
            }
            return
        }
        render([status: "success"] as JSON)
    }

    def upload() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        MultipartFile fileToUpload = request.getFile('file')
        String originalFileName = fileToUpload.originalFilename
        originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_")
        try {
            Integer fileCount = ProductAssignmentLog.countByImportedFileNameAndStatusInList(originalFileName, [ProdAssignmentProcessState.IN_PROCESS, ProdAssignmentProcessState.IN_READ])
            if (fileCount == 0) {
                File sourceFolder = new File(config.signal.product.assignment.import.folder.read as String)
                String savedFilePath = "${sourceFolder.absolutePath}/${originalFileName}"
                fileToUpload.transferTo(new File(savedFilePath))
                File uploadedFile = new File(savedFilePath)
                boolean isCorrectFormat =  productAssignmentImportService.checkFileFormat(new File(savedFilePath))
                if(isCorrectFormat) {
                    ProductAssignmentLog productAssignmentLog = productAssignmentImportService.saveProductAssignmentLog(originalFileName, userService.getUser())
                    String logsFilePath = "${config.signal.product.assignment.import.folder.logs}/${productAssignmentLog.id}"
                    productAssignmentImportService.createDir(logsFilePath)
                    productAssignmentImportService.copyFile(uploadedFile, logsFilePath)
                    responseDTO.message = message(code: "app.label.import.assignment.upload.inprogress")
                } else {
                    uploadedFile.delete()
                    responseDTO.message = message(code: "app.label.import.assignment.upload.file.format.incorrect")
                    responseDTO.status = false
                }
            } else {
                responseDTO.message = message(code: "app.label.import.assignment.upload.inprogress.same.file")
                responseDTO.status = false
            }
        } catch (FileFormatException ex) {
            log.error(ex.getStackTrace())
            responseDTO.message = message(code: "app.label.product.assignment.please.choose.file")
            responseDTO.status = false
        } catch (Exception ex) {
            responseDTO.message = message(code: "app.label.import.assignment.upload.file.format.incorrect")
            responseDTO.status = false
            log.error(ex.printStackTrace())
        }
        render(responseDTO as JSON)
    }

    def fetchImportAssignment(){
        String timezone = userService.user.preference.timeZone
        List productAssignmentLogList = ProductAssignmentLog.list([sort: "importedDate", order: "desc"]).collect {
            [importedFileName: it.importedFileName, id: it.id,
             generatedFileName:it.status == ProdAssignmentProcessState.SUCCESS ? "${FilenameUtils.removeExtension(it.importedFileName)}_Log.xlsx" : '-',
             importedBy: it.importedBy.name,
             importedDate: DateUtil.toDateStringWithTimeInAmPmFormat( it.importedDate, timezone ),
             status: it.status.value()]
        }
        render([importLogList: productAssignmentLogList] as JSON)
    }

    def importAssignmentFile(){
        File searchedFile = new File("${config.signal.product.assignment.import.folder.logs}/${params.logsId}/${params.fileName}")
        Boolean isImport = params.fileName?.contains("_Log")
        renderImportAssignmentOutputType(searchedFile, searchedFile.name)
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,isImport ? "Import Log": "Imported File" , "Product Assignment", [:], params.fileName)
    }

    void renderImportAssignmentOutputType(File importAssignmentFile, String name) {
        String extension = FilenameUtils.getExtension(importAssignmentFile.name)
        response.contentType = "$extension charset=UTF-8"
        response.contentLength = importAssignmentFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${name}" + "\"")
        response.getOutputStream().write(importAssignmentFile.bytes)
        response.outputStream.flush()
    }

    def prepareDataForCRUDOperation(String selectedUserOrGroup, String selectedProducts, String selectedProductGroups) {
        Map products = [:]
        List assignmentList = JSON.parse(selectedUserOrGroup)
        List productGroups = []
        if (selectedProducts) {
            products = JSON.parse(selectedProducts)
        }
        if (selectedProductGroups) {
            List productGroupSelection = JSON.parse(selectedProductGroups)
            productGroupSelection.each { prdMap ->
                String prdId = prdMap.id
                prdMap.name = prdMap.name.substring(0, prdMap.name.lastIndexOf(" (" + prdId + ")"))
                productGroups.add(prdMap)
            }
        }
        products.put("0", productGroups)
        return ["assignmentList":assignmentList,"products":products]
    }

    @Secured(["ROLE_MANAGE_PRODUCT_ASSIGNMENTS"])
    def updateProductAssignment(){
        // commented code for bug PVS-54073 FOR update.
        /*if(params.selectedProducts){
            Boolean isNotValidSelection = checkIfSelectionIsValid(params.selectedProducts)
            if(isNotValidSelection){
                render([status: "warning"] as JSON)
                return
            }
        }*/


        List dicList = PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }
        Map dicMap = [:]
        dicList.eachWithIndex{value,index->
            dicMap[index] = value
        }
        Map productsAndAssignment = prepareDataForCRUDOperation(params.selectedUserOrGroup, params.selectedProducts, params.selectedProductGroups)
        Map products = productsAndAssignment.products
        List assignmentList = productsAndAssignment.assignmentList

        Long workflowGroup = null
        Boolean isWfUpdate = true
        if(params.primaryUserOrGroup && params.primaryUserOrGroup.contains(Constants.USER_TOKEN)){
            Integer userId = params.primaryUserOrGroup.replace(Constants.USER_TOKEN,"") as Integer
            workflowGroup = User.get(userId)?.workflowGroup?.id
        } else {
            isWfUpdate = false
        }

        List<Long> newCreatedUserViews = []
        if(params.isProductView == "true") {
            productAssignmentService.updateAssignmentForProductView(products, assignmentList, params.assignmentId as Long, dicMap, params, workflowGroup, isWfUpdate, newCreatedUserViews)
        } else {
            productAssignmentService.updateAssignmentForUserView(products, assignmentList, params.assignmentId as Long, dicMap, params, workflowGroup, isWfUpdate, newCreatedUserViews)
        }
        if(newCreatedUserViews) {
            productAssignmentService.callProcForUserView(newCreatedUserViews)
        }

        render([status: "success"] as JSON)
    }

    @Secured(["ROLE_MANAGE_PRODUCT_ASSIGNMENTS"])
    def bulkUpdateProductAssignment() {
        List dicList = PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }
        Map dicMap = [:]
        dicList.eachWithIndex{value,index->
            dicMap[index] = value
        }

        Map bulkData = JSON.parse(params.bulkData) as Map
        Boolean isValidProducts = true
        //Commented code for bug PVS-54073 as per bug this is only for save. but when we are allowing time of save then it should be change in update, bulk update too.
        /*bulkData.each { productMap ->
            String selectedProducts = productMap.value.selectedProducts ? (productMap.value.selectedProducts as JSON).toString() : ""
            if (selectedProducts) {
                Boolean isNotValidSelection = checkIfSelectionIsValid(selectedProducts)
                if (isNotValidSelection) {
                    isValidProducts = false
                }
            }
        }
        if(!isValidProducts){
            render([status: "warning"] as JSON)
            return
        }*/
        bulkData.each { productMap ->
            Long workflowGroup = null
            Boolean isWfUpdate = true

            if(productMap.value.primaryUserOrGroup && productMap.value.primaryUserOrGroup.contains(Constants.USER_TOKEN)){
                Integer userId = productMap.value.primaryUserOrGroup.replace(Constants.USER_TOKEN,"") as Integer
                workflowGroup = User.get(userId).workflowGroup.id
            } else {
                isWfUpdate = false
            }

            String selectedUserOrGroup = (productMap.value.selectedUserOrGroup as JSON).toString()
            String selectedProducts = productMap.value.selectedProducts ? (productMap.value.selectedProducts as JSON).toString() : ""
            String selectedProductGroups = productMap.value.selectedProductGroups ? (productMap.value.selectedProductGroups as JSON).toString() : ""

            Map productsAndAssignment = prepareDataForCRUDOperation(selectedUserOrGroup, selectedProducts, selectedProductGroups)
            Map products = productsAndAssignment.products
            List assignmentList = productsAndAssignment.assignmentList

            Map primaryMap = [primaryUserOrGroup: productMap.value.primaryUserOrGroup, selectedUserOrGroup: (productMap.value.selectedUserOrGroup as JSON).toString()]
            List<Long> newCreatedUserViews = []
            if (params.isProductView == "true") {
                productAssignmentService.updateAssignmentForProductView(products, assignmentList, productMap.key as Long, dicMap, primaryMap, workflowGroup, isWfUpdate, newCreatedUserViews)
            } else {
                productAssignmentService.updateAssignmentForUserView(products, assignmentList, productMap.key as Long, dicMap, primaryMap, workflowGroup, isWfUpdate, newCreatedUserViews)
            }
            if(newCreatedUserViews) {
                productAssignmentService.callProcForUserView(newCreatedUserViews)
            }
        }
        render([status: "success"] as JSON)
    }

    @Secured(["ROLE_MANAGE_PRODUCT_ASSIGNMENTS"])
    def deleteAssignment() {
        List dicList = PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }
        Map dicMap = [:]
        dicList.eachWithIndex { value, index ->
            dicMap[index] = value
        }
        List<Long> newCreatedUserViews = []
        List<Long> selectedAssignmentIds = JSON.parse(params.selectedAssignmentIds)
        if (params.isProductView == "true") {
            selectedAssignmentIds.each {
                productAssignmentService.deleteProductViewAssignment(it as Long, false, null, false,
                        newCreatedUserViews, [], [], [], dicMap)
            }
        } else {
            selectedAssignmentIds.each {
                productAssignmentService.deleteUserViewAssignment(it as Long, false)
            }
        }
        if (newCreatedUserViews) {
            productAssignmentService.callProcForUserView(newCreatedUserViews)
        }
        render([status: "success"] as JSON)
    }

    def exportAssignment() {
        List groupsList = []
        cacheService.getAllGroups().each { groupsList.add(it.value) }
        List usersList = []
        cacheService.getAllUsers().each { usersList.add(it.value) }
        List productViewAssignments = productAssignmentService.exportAssignment(params, groupsList, usersList)
        params.outputFormat = "XLSX"
        User user = userService.getUser()
        String generatedBy = user.fullName
        String dateCreated = DateUtil.toDateStringWithTimeInAmPmFormat(user) + userService.getGmtOffset(user.preference.timeZone)
        Map filterMap = [Product: params.product, "Product Hierarchy": params.hierarchy, Assignment: params.assignedUserOrGroup, "User ID": params.createdBy, "Workflow Group": params.workflowGroup]
        Map criteriaInfo = [generatedBy: generatedBy, dateCreated: dateCreated, filterMap: filterMap, isFilter: params.isFilter]
        String filterString =""
        if(criteriaInfo.isFilter != "true"){
            filterString = Constants.Commons.NA_LISTED
        } else {
            criteriaInfo.filterMap.each{
                if(it.value) {
                    filterString += "${it.key}: ${it.value}\n"
                }
            }
        }
        List criteriaSheetList = [['label': 'Report Generated By', 'value': generatedBy], ['label': 'Report Generated Date', 'value': dateCreated], ['label': 'Filters', 'value': filterString], ['label':Constants.CriteriaSheetLabels.IMPORT_SHEET_NOTE, 'value': '']]
        File reportFile = dynamicReportService.createProductAssignmentReport(new JRMapCollectionDataSource(productViewAssignments), params, criteriaInfo)
        String reportName = "ProductAssignment" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        renderReportOutputType(reportFile, reportName)
        signalAuditLogService.createAuditForExport(criteriaSheetList,"Product Assignment" , "Product Assignment", params, reportName)
    }

    void renderReportOutputType(File reportFile, String reportName) {
        response.contentType = "${dynamicReportService.getContentType("XLSX")}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.${params.outputFormat}" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    def populateUnassignedProducts(String hierarchy) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        if(!cacheService.getUnassignedProductsCache(hierarchy)) {
            productAssignmentService.populateUnassignedProductsInBackground(hierarchy)
            responseDTO.message = message(code: "app.label.import.populate.unassigned.products.inprogress")
        } else {
            responseDTO.message = message(code: "app.label.import.populate.unassigned.products.same.hierarchy")
            responseDTO.status = false
        }
        render(responseDTO as JSON)
    }

    def columnOrder() {
        Boolean isProductView = params.isProductView == "true"
        List columnOrder = getColumnOrder(isProductView,false)
        render(columnOrder as JSON)
    }

    List setSessionColumns(String columnName,List listOfColumns) {
        List productColumnOrder = session.getAttribute(columnName)
        List<Map> newOrder = []
        int countPri = 0
        int countSec = 0
        if (productColumnOrder) {
            productColumnOrder.each { Map firstMap ->
                listOfColumns.each { Map secMap ->
                    if (firstMap.seq == secMap.seq && secMap.containerView == 1) {
                        firstMap.containerView = secMap.containerView
                        firstMap.listOrder = countPri++
                        newOrder.add(firstMap)
                    }
                }
            }
            productColumnOrder.each { Map firstMap ->
                listOfColumns.each { Map secMap ->
                    if (firstMap.seq == secMap.seq && secMap.containerView == 3) {
                        firstMap.containerView = secMap.containerView
                        firstMap.listOrder = countSec++
                        newOrder.add(firstMap)
                    }
                }
            }
        }
        return newOrder
    }
    def setColumnOrder() {
        User user = userService.user
        if(params.isProductView == "true"){
            user.colOrder = params.columnList
        } else {
            user.colUserOrder = params.columnList
        }
        user.save(flush:true)
        render([status: "success"] as JSON)

    }

    def fetchAssignmentForProducts() {
        if (params.productAssignment && params.productGroupAssignment) {
            render([status: "fail"] as JSON)
            return
        }
        List dicList = PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }
        Map dicMap = [:]
        Integer userAssignmentIndex = -1
        dicList.eachWithIndex{value,index->
            dicMap[index] = value
            if(value.toString() == "User Assignment"){
                userAssignmentIndex = index + 1
            }
        }
        int count = 0
        BigInteger assignmentId
        Map productAssignment = [:]
        String primaryUserOrGroupId =""
        List assignmentList =[]
        Long workflowGroup = userService.getUser().getWorkflowGroup()?.id
        Map userMap = cacheService.getAllUsers()
        Map groupMap = cacheService.getAllGroups()
        if (params.productAssignment) {
            productAssignment = JSON.parse(params.productAssignment)
            productAssignment.each { assignmentMap ->
                if (assignmentMap.value) {
                    if (assignmentMap.value.size() > 1) {
                        count = 99
                    } else if(assignmentMap.key == userAssignmentIndex && assignmentMap.value){
                        count = 99
                    } else {
                        assignmentId = assignmentMap.value[0].id as BigInteger
                        String hierarchy = productAssignmentService.getProductHierarchy(assignmentMap.key as Integer, dicMap)
                        ProductViewAssignment matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                            eq("workflowGroup",workflowGroup)
                            eq("hierarchy",hierarchy)
                            sqlRestriction("JSON_VALUE(product,'\$.id') = ${assignmentId}")
                            maxResults(1)
                        }
                        if(!matchedAssignment){
                            matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                                isNull('workflowGroup')
                                eq("hierarchy",hierarchy)
                                sqlRestriction("JSON_VALUE(product,'\$.id') = ${assignmentId}")
                                maxResults(1)
                            }
                        }
                        if(matchedAssignment){
                            primaryUserOrGroupId = matchedAssignment.primaryUserOrGroupId
                            matchedAssignment.usersAssigned.each{
                                if(userMap.get(it)) {
                                    assignmentList.add("User_${it}")
                                }
                            }
                            matchedAssignment.groupsAssigned.each{
                                if(groupMap.get(it)){
                                    assignmentList.add("UserGroup_${it}")
                                }
                            }
                            count ++
                        } else {
                            count = 99
                        }
                    }
                }
            }
        }
        List productGroupAssignment = []
        if (params.productGroupAssignment) {
            productGroupAssignment = JSON.parse(params.productGroupAssignment)
            if (productGroupAssignment.size() > 1) {
                count = 99
            } else {
                assignmentId = productGroupAssignment[0].id as BigInteger
                String hierarchy = "Product Group"
                ProductViewAssignment matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                    eq("workflowGroup",workflowGroup)
                    eq("hierarchy",hierarchy)
                    sqlRestriction("JSON_VALUE(product,'\$.id') = ${assignmentId}")
                    maxResults(1)
                }
                if(!matchedAssignment){
                    matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                        isNull('workflowGroup')
                        eq("hierarchy",hierarchy)
                        sqlRestriction("JSON_VALUE(product,'\$.id') = ${assignmentId}")
                        maxResults(1)
                    }
                }
                if(matchedAssignment){
                    primaryUserOrGroupId = matchedAssignment.primaryUserOrGroupId
                    matchedAssignment.usersAssigned.each{
                        if(userMap.get(it)){
                            assignmentList.add("User_${it}")
                        }
                    }
                    matchedAssignment.groupsAssigned.each{
                        if(groupMap.get(it)){
                            assignmentList.add("UserGroup_${it}")
                        }
                    }
                    count ++
                } else {
                    count = 99
                }
            }
        }

        Boolean isPrimaryUser = false
        if(primaryUserOrGroupId) {
            if (primaryUserOrGroupId.contains(Constants.USER_GROUP_TOKEN)) {
                Long groupId = primaryUserOrGroupId.replace(Constants.USER_GROUP_TOKEN, "") as Long
                if (groupMap.get(groupId)) {
                    isPrimaryUser = true
                }
            } else if (primaryUserOrGroupId.contains(Constants.USER_TOKEN)) {
                Long userId = primaryUserOrGroupId.replace(Constants.USER_TOKEN, "") as Long
                if (userMap.get(userId)) {
                    isPrimaryUser = true
                }
            }
        }
        if(count == 1 && assignmentList && isPrimaryUser){
            render([status: "success", primaryUserOrGroupId: primaryUserOrGroupId, assignmentList:assignmentList] as JSON)
        } else {
            render([status: "fail"] as JSON)
        }
    }
    String uniqueAlertGroups(){
        return productAssignmentService.uniqueAlertGroups() as JSON
    }
    String migrateProductViewAssignment() {
        Holders.config.product.view.assignment.migration.required = false
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try{
            ProductViewAssignment."pva".withTransaction { status ->
                List assignments = ProductViewAssignment.createCriteria().list {
                    isNull("alertGroup")
                }

                AlertGroup alertGroup = AlertGroup.findByNameIlike(params.alertGroup)
                if(!alertGroup){
                    alertGroup = new AlertGroup(name: params.alertGroup)
                    alertGroup.save(flush:true)
                }

                assignments.each { assignment ->
                    assignment.alertGroup = alertGroup.id
                    assignment.save()
                }
            }
            render responseDTO as JSON
        } catch(Exception ex){
            Holders.config.product.view.assignment.migration.required = true
            responseDTO.code = 500
            responseDTO.status = false
            ex.printStackTrace()
            render responseDTO as JSON
        }
    }
}
