package com.rxlogix

import com.monitorjbl.xlsx.StreamingReader
import com.rxlogix.audit.AuditTrail
import com.rxlogix.commandObjects.LdapCommand
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ProdAssignmentProcessState
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.ProductAssignmentLog
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.signal.UserViewAssignment
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.util.CsvDataImporter
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.SignalUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.sql.Sql
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.boon.core.Sys
import org.hibernate.Session
import org.hibernate.StatelessSession
import org.hibernate.Transaction
import org.joda.time.DateTimeZone
import org.springframework.transaction.annotation.Propagation

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static grails.async.Promises.task
import com.rxlogix.config.AlertGroup

class ProductAssignmentImportService {
    static transactional = false
    def cacheService
    def userService
    def ldapService
    def grailsApplication
    def productAssignmentService
    def dataSource_pva
    def dynamicReportService
    def messageSource
    def sessionFactory_pva
    def sessionFactory
    def alertService
    def CRUDService
    def signalAuditLogService
    def signalDataSourceService
    def attachmentableService
    def config = Holders.config
    File readFolder = new File(config.signal.product.assignment.import.folder.read as String)
    File uploadFolder = new File(config.signal.product.assignment.import.folder.upload as String)
    File failFolder = new File(config.signal.product.assignment.import.folder.fail as String)
    File logsDir = new File(config.signal.product.assignment.import.folder.logs as String)

    def checkAndCreateBaseDirs() {
        File baseFolder = new File(config.signal.product.assignment.import.folder.base as String)
        if (!baseFolder.exists()) {
            log.debug("Base folder not found, creating it.")
            baseFolder.mkdir()
        }
        if (!readFolder.exists()) {
            log.debug("Source folder not found, creating it.")
            readFolder.mkdir()
        }

        if (!uploadFolder.exists()) {
            log.debug("Upload folder not found, creating it.")
            uploadFolder.mkdir()
        }

        if (!failFolder.exists()) {
            log.debug("Upload folder not found, creating it.")
            failFolder.mkdir()
        }

        if (!logsDir.exists()) {
            log.debug("Success folder not found, creating it.")
            logsDir.mkdir()
        }
    }

    void startProcessingUploadedFile() {
        boolean isFileUploading = checkIfFileExistsInUploadFolder()
        ProductAssignmentLog productAssignmentLog = getNextFileProdAssignLog()
        if (isFileUploading || !productAssignmentLog)
            return
        File fileToUpload = getNextFileToUpload(productAssignmentLog, readFolder)
        try {
            log.info("Job for importing the Product Assignment Starts")
            updateProductAssignmentLog(productAssignmentLog.id, ProdAssignmentProcessState.IN_PROCESS)
            fileToUpload = moveFile(fileToUpload, config.signal.product.assignment.import.folder.upload as String)
            persistProductAssignmentsFromExcel(fileToUpload, productAssignmentLog)
        } catch (Throwable th) {
            log.error(th.getMessage(), th)
            updateProductAssignmentLog(productAssignmentLog.id, ProdAssignmentProcessState.FAILED)
            moveFile(fileToUpload, config.signal.product.assignment.import.folder.fail as String)
        }

    }

    void resumeInProgressFile() {
            ProductAssignmentLog productAssignmentLog = getUploadedFileProdAssignLog()
            File fileToUpload = getNextFileToUpload(productAssignmentLog, uploadFolder)
            try {
                task {
                    if (fileToUpload) {
                        log.info("Job for importing the Product Assignment Resumed")
                        persistProductAssignmentsFromExcel(fileToUpload, productAssignmentLog)
                    }
                }
            } catch (Throwable th) {
                log.error(th.getMessage(), th)
                updateProductAssignmentLog(productAssignmentLog.id, ProdAssignmentProcessState.FAILED)
                moveFile(fileToUpload, config.signal.product.assignment.import.folder.fail as String)
            }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateProductAssignmentLog(Long id, ProdAssignmentProcessState prodAssignmentProcessState) {
        ProductAssignmentLog.executeUpdate("Update ProductAssignmentLog set status = :status where id = ${id}",
                [status: prodAssignmentProcessState])
        if (prodAssignmentProcessState.value() in ["Success", "Failed"])
            createAuditForAssignImport(id, prodAssignmentProcessState)
    }

    void createAuditForAssignImport(def importLogId, ProdAssignmentProcessState prodAssignmentProcessState) {
        def prodAssignLog = ProductAssignmentLog.get(importLogId as Long)
        def auditTrailMap = [
                entityName : 'productAssignmentLog',
                moduleName : "Product Assignment Import",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: prodAssignLog.importedFileName + ": " + prodAssignmentProcessState.value(),
                description: "",
                username   : prodAssignLog.importedBy.username,
                fullname   : prodAssignLog.importedBy.fullName
        ]
        List<Map> auditChildMap = []
        def childEntry = [:]
        childEntry = [
                propertyName: "File Name",
                newValue    : prodAssignLog?.importedFileName]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Imported By",
                newValue    : prodAssignLog?.importedBy.fullName]
        auditChildMap << childEntry
        signalAuditLogService.createAuditLog(auditTrailMap, auditChildMap)
    }

    void persistProductAssignmentsFromExcel(File fileToUpload, ProductAssignmentLog productAssignmentLog) {
        def totStart=System.currentTimeSeconds()
        Map processedRecords = processingOfImportedRecords(fileToUpload)
        List<Map> productViewList = processedRecords.recordsCreated
        List<Map> userViewList = processedRecords.userViewList
        ExecutorService executorService = Executors.newFixedThreadPool(2)
        List<Callable<Map>> callables = new ArrayList<Callable<Map>>(){{
            add(new Callable<Map>() {
                @Override
                Map call() throws Exception {
                    saveProductAssignmentInBatches(productViewList, processedRecords)
                }
            })
            add(new Callable<Map>() {
                @Override
                Map call() throws Exception {
                    saveUserViewAssignmentInBatches(userViewList)
                }
            })
        }}

        List<Future<Boolean>> futures = executorService.invokeAll(callables)
        futures.each {
            try {
                it.get()
            } catch (Exception e) {

                e.printStackTrace()
            }
        }
        executorService.shutdown()

        transferImportLogFile(processedRecords, "${productAssignmentLog.id}/${fileToUpload.name}",productAssignmentLog.importedBy)
        fileToUpload.delete()
        updateProductAssignmentLog(productAssignmentLog.id, ProdAssignmentProcessState.SUCCESS)
        log.info("total time taken in persistProductAssignmentsFromExcel is ${System.currentTimeSeconds()-totStart}")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void saveProductAssignmentInBatches(List<Map> productViewList, Map processedRecords) {
        Integer numberOfRowsProcessed = 0
        Integer batchSize = Constants.ProductAssignment.batchSize as Integer
        log.info(" Product View List to be processed: ${productViewList.size()}")

        List dicList = PVDictionaryConfig.ProductConfig.views.collect {
            messageSource.getMessage(it.code, null, Locale.default)
        }
        Map dicMap = [:]
        Map hierarchyMap = [:]
        dicList.eachWithIndex { value, index ->
            dicMap[index] = value
            hierarchyMap[value] = index
        }
        List<ProductViewAssignment> productViewAssignmentList=[]
        List<ProductViewAssignment> updateAuditBatchPVA = []
        List<ProductViewAssignment> createAuditBatchPVA = []
        productViewList.collate(batchSize).each { assignmentList ->
            numberOfRowsProcessed += assignmentList.size()
            persistProductAssignment(assignmentList, dicMap, hierarchyMap, productViewAssignmentList,updateAuditBatchPVA, createAuditBatchPVA)
            log.info(" Number of rows processed: $numberOfRowsProcessed ")
        }
        log.info("now batch persisting the processed records in mart")
        if(productViewAssignmentList.size()>0){
            log.info("now batch persisting the processed records in mart list size is ${productViewAssignmentList.size()}")
            def startSave=System.currentTimeSeconds()
            productAssignmentService.batchPersistProductAssignment(productViewAssignmentList, ProductViewAssignment,true, processedRecords, updateAuditBatchPVA, createAuditBatchPVA)
            log.info("time taken in batch persist is ${System.currentTimeSeconds()-startSave} seconds")
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void saveUserViewAssignmentInBatches(List<Map> userViewList) {
        Integer numberOfRowsProcessed = 0
        Integer batchSize = Constants.ProductAssignment.batchSize as Integer
        log.info(" User View List to be processed: ${userViewList.size()}")
        Session sessionApp = sessionFactory.currentSession

        List dicList = PVDictionaryConfig.ProductConfig.views.collect {
            messageSource.getMessage(it.code, null, Locale.default)
        }
        Map dicMap = [:]
        Map hierarchyMap = [:]
        dicList.eachWithIndex { value, index ->
            dicMap[index] = value
            hierarchyMap[value] = index + 1 as Long
        }

        userViewList.collate(batchSize).each { assignmentList ->
            numberOfRowsProcessed += assignmentList.size()
            UserViewAssignment."pva".withNewSession {
                List<Long> newCreatedUserViews = []
                persistUserAssignment(assignmentList, newCreatedUserViews,dicMap,hierarchyMap)
                Session session = sessionFactory_pva.currentSession
                session.flush()
                session.clear()
                if (newCreatedUserViews) {
                    productAssignmentService.callProcForUserView(newCreatedUserViews)
                }
            }
            log.info(" Number of rows processed for User View: $numberOfRowsProcessed ")
            sessionApp.flush()
            sessionApp.clear()
        }
    }

    void persistProductAssignment(List<Map> dataList,Map dicMap=[:],Map hierarchyMap=[:],List<ProductViewAssignment> productViewAssignmentList, List<ProductViewAssignment>updateAuditBatchPVA, List<ProductViewAssignment>createAuditBatchPVA) {
        Long wfGroup = null
        String hierarchy, products, productGroup
        List userList = []
        List <AlertGroup> alertGroupList = AlertGroup.getAll()

        ProductViewAssignment."pva".withNewSession {
            dataList.each { Map assign ->
                AlertGroup alertGroup = alertGroupList.find { it.name?.toLowerCase() == assign?.alertGroup?.toLowerCase() }
                if(!alertGroup){
                    alertGroup = new AlertGroup(name: assign.alertGroup)
                    alertGroup.save(flush:true)
                }
                def st = System.currentTimeSeconds()
                hierarchy = assign['hierarchy']
                userList = []
                generateUserOrGroupId(assign.userIdList as List, assign.groupIdList as List, userList)
                wfGroup = assign.workflowGroupId
                if (hierarchy.equalsIgnoreCase("product group")) {
                    productGroup = assign.products
                    products = null
                } else {
                    products = assign.products
                    productGroup = null
                }
                String primaryUserOrGroup = ""
                if(assign.isUserFirst=='true')
                    primaryUserOrGroup = userList.find{it.startsWith(Constants.USER_TOKEN)}
                else
                    primaryUserOrGroup = userList.find{it.startsWith(Constants.USER_GROUP_TOKEN)}
                Map userGroup = [primaryUserOrGroup: primaryUserOrGroup]
                if (!wfGroup && primaryUserOrGroup.contains(Constants.USER_TOKEN)) {
                    Integer userId = primaryUserOrGroup.replace(Constants.USER_TOKEN, "") as Integer
                    wfGroup = cacheService.getUserByUserId(userId)?.workflowGroup?.id
                }
                Map productsAndAssignment = productAssignmentService.prepareDataForCRUDOperation(userList.toString(), products, productGroup)
                Map product = productsAndAssignment.products
                List assignmentList = productsAndAssignment.assignmentList
                BigInteger assignmentId
                product.each { sequence, selectionMap ->
                    if (selectionMap) {
                        assignmentId = selectionMap[0].id as BigInteger
                    }
                }
                boolean notNewUserForThisProduct = false
                List<ProductViewAssignment> matchedAssignments = ProductViewAssignment."pva".createCriteria().list {
                    eq("hierarchy", hierarchy)
                    eq("alertGroup", alertGroup.id as String)
                    sqlRestriction("JSON_VALUE(product,'\$.id') = ${assignmentId}")
                }
                ProductViewAssignment matchedAssignment = null
                if(matchedAssignments?.size()==1)
                    matchedAssignment = matchedAssignments?.get(0)
                if(matchedAssignment && matchedAssignment?.primaryUserOrGroupId?.contains(Constants.USER_TOKEN)){
                    wfGroup = matchedAssignment.workflowGroup
                }
                else {
                    wfGroup = null
                }
                if (matchedAssignment) {
                    notNewUserForThisProduct = true
                } else {
                    notNewUserForThisProduct = false
                }
                productAssignmentService.saveProductAssignmentForExcelUpload(product, assignmentList, dicMap, userGroup, wfGroup, notNewUserForThisProduct, productViewAssignmentList, primaryUserOrGroup, alertGroup?.id as String, updateAuditBatchPVA, createAuditBatchPVA)
            }
            Session session = sessionFactory_pva.currentSession
            session.flush()
            session.clear()
        }
    }

    void persistUserAssignment(List<Map> dataList, List<Long> newCreatedUserViews,Map dicMap=[:],Map hierarchyMap=[:]) {
        String hierarchy, products
        List hierarchiesList = dataList*.hierarchy
        List<Long> usersList = dataList*.user.unique() - null
        List<Long> groupsList = dataList*.group.unique() - null
        def startTime=System.currentTimeSeconds()

        List<UserViewAssignment> userViewList=  UserViewAssignment."pva".createCriteria().list {
            'or' {
                usersList.collate(1000).each {
                    'in'("userAssigned", it)
                }
                groupsList.collate(1000).each {
                    'in'("groupAssigned", it)
                }
                hierarchiesList.collate(1000).each {
                    'in'("hierarchy", it)
                }
            }
        }
        dataList.each { Map assign ->
            hierarchy = assign['hierarchy']
            products = (assign.products as JSON).toString()
            Long hierarchyId = hierarchyMap.get(hierarchy) ?: 0
            Long hierarchyKeyId = hierarchyId + 199
            if (assign.user) {
                newCreatedUserViews.add(assign.user)
                productAssignmentService.saveUserAssignmentForExcelUpload(assign.user, null, hierarchy, products, hierarchyKeyId,userViewList)
            } else {
                newCreatedUserViews.add(assign.group)
                productAssignmentService.saveUserAssignmentForExcelUpload(null, assign.group, hierarchy, products, hierarchyKeyId,userViewList)
            }
        }
    }

    // TODO: This needs to be modified, instead of using this string append approach we need to use Map and whole method and
    //  class needs to be refactored accordingly. -> Chetan
    void generateUserOrGroupId(List<Long> userIdList, List<Long> groupIdList, List userList) {
        userIdList.each {
            userList.add("User_" + it)
        }
        groupIdList.each {
            userList.add("UserGroup_" + it)
        }
    }

    String generateProducts(Integer hierarchyKey, Map product) {
        Map productDictionaryMap = SignalUtil.inititalizeProductDictionaryMap()
        String dictionarySelection = "${hierarchyKey + 1}"
        productDictionaryMap[dictionarySelection] << [name: product.name as String, id: product.id as String]
        new JsonBuilder(productDictionaryMap).toString()
    }

    Map generateProductMap(String value, Integer hierarchyKey, List<Map> productNameIdList) {
        String productName = value.trim().toLowerCase()
        Map product = productNameIdList.find {
            it.name.toLowerCase() == productName && it.hierarchy == hierarchyKey as String
        }
        if (!product) {
            return null
        }
        [name: product.name as String, id: product.id as String]
    }

    void fetchProductIds(List<String> productNameList, String hierarchyKey, List<Map> productNameIdList, HashSet productNameIdSet) {
        String className = hierarchyKey.equals(Constants.Commons.PRODUCT_GROUP_VALUE) ? "com.rxlogix.mapping.LmProduct199" : "com.rxlogix.pvdictionary.product.view.LmProdDic${200 + hierarchyKey.toInteger()}"
        String productName = productNameList.collect { "'$it'" }.join(',')
        List productIdList = Class.forName(className, true, Thread.currentThread().getContextClassLoader()).createCriteria().list {
            sqlRestriction("lower(col_2) in ($productName)")
            eq("lang", "en")
        }
        productIdList.each { productList ->
            String setKey = generateKeyForProductNameIdListMap(productList.name, productList.viewId, hierarchyKey)
            if (!productNameIdSet.contains(setKey)) {
                productNameIdSet.add(setKey)
                productNameIdList.add([name: productList.name, id: productList.viewId as String, hierarchy: hierarchyKey])
            }
        }
    }

    String generateKeyForProductNameIdListMap(String name, String viewId, String hierarchyKey) {
        "${name}${viewId}${hierarchyKey}"
    }

    String generateProductsGroupJson(String value, List<Map> productNameIdList) {
        String productName = value.trim().toLowerCase()
        List productGroupList = []
        Map product = productNameIdList.find { it.name.toLowerCase() == productName && it.hierarchy == "199" }
        if (!product) {
            return null
        }
        productGroupList.add([name: product.name as String, id: product.id as String])
        new JsonBuilder(productGroupList).toString()
    }

    void createDir(String logsFilePath) {
        File logsFileDir = new File(logsFilePath)
        if (!logsFileDir.exists()) {
            logsFileDir.mkdir()
        }
    }

    boolean checkIfFileExistsInUploadFolder() {
        if (uploadFolder.exists() && uploadFolder.isDirectory())
            return uploadFolder.listFiles().size()
        false
    }

    File getNextFileToUpload(ProductAssignmentLog productAssignmentLog, File folder) {
        List<File> fileListToProcess = []
        folder.eachFileRecurse(FileType.FILES) { file ->
            fileListToProcess << file
        }
        fileListToProcess.find { it.name == productAssignmentLog.importedFileName }
    }

    ProductAssignmentLog getNextFileProdAssignLog() {
        ProductAssignmentLog.createCriteria().get {
            eq("status", ProdAssignmentProcessState.IN_READ)
            order("importedDate", "asc")
            maxResults(1)
        } as ProductAssignmentLog
    }

    ProductAssignmentLog getUploadedFileProdAssignLog() {
        ProductAssignmentLog.createCriteria().get {
            eq("status", ProdAssignmentProcessState.IN_PROCESS)
            order("importedDate", "desc")
            maxResults(1)
        } as ProductAssignmentLog
    }

    File moveFile(File file, String destination, String newFileName = null) {
        String updatedPath = "$destination/${newFileName ?: file.name}"
        File newDestination = new File(updatedPath)
        Files.move(file.toPath(), newDestination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        new File(updatedPath)
    }

    void copyFile(File file, String destination) {
        String updatedPath = "$destination/${file.name}"
        File newDestination = new File(updatedPath)
        Files.copy(file.toPath(), newDestination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    Long generateUserIdFromCache(String username, List<User> userList) {
           userList.find {it.username.toLowerCase() == username.toLowerCase()}?.id
    }

    Long generateUserIdForExcel(String username, List<User> userList, Long workflowGroupId, List<Long> newUserIds) {
          Long userId = generateUserIdFromCache(username, userList)
          if(!userId) {
              userId = userIdFromLdap(username, workflowGroupId)
              if(userId)
                  newUserIds << userId
          }
          return userId
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Long userIdFromLdap(String userName, Long workflowGroupId) {
        String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
        ldapService.getLdapEntry("$uid=$userName").size()
        User user = User.findByUsername(userName)
        List<LdapCommand> ldapEntry = ldapService.getLdapEntry("$uid=$userName")
        try {
            if (ldapEntry?.size() && !user) {
                log.info("Save new user present in Ldap but not in application")
                Preference preference = new Preference(locale: new Locale('en'), timeZone: DateTimeZone.UTC.ID, createdBy: "Application", modifiedBy: "Application")
                String fullName = ldapEntry[0]?.getFullName()
                String email = ldapEntry[0]?.getEmail()
                user = new User(username: userName, preference: preference, createdBy: "Application", modifiedBy: "Application", fullName: fullName, email: email)
                if(workflowGroupId){
                    Group wfGroup = Group.get(workflowGroupId)
                    user.addToGroups(wfGroup)
                }
                user.save()
                cacheService.setUserCacheByUserName(user)
                cacheService.setUserCacheByUserId(user)
            }
        } catch(Exception ex) {
            ex.printStackTrace()
            log.error(ex.getMessage())
        }
        user?.id
    }

    Long groupIdFromTheSystem(String groupName) {
        List<Group> userGroupList = cacheService.getUserGroups()
        userGroupList.find { it.name.toLowerCase() == groupName.toLowerCase() }?.id
    }

    @Transactional
    ProductAssignmentLog saveProductAssignmentLog(String fileName, User user) {
        ProductAssignmentLog productAssignmentLog = new ProductAssignmentLog(importedFileName: fileName, importedBy: user, importedDate: new Date(),
                status: ProdAssignmentProcessState.IN_READ)
        productAssignmentLog.save()

    }

    List<Map> processFile(File fileToBeProcessed) {
        Map baseColumnTypeMap = ['product': 'String', 'product hierarchy': 'String', 'assignment': 'String', 'workflow group': 'String','alert group': 'String', 'import': 'String']
        String fileExtension = FilenameUtils.getExtension(fileToBeProcessed.name).toLowerCase()
        switch (fileExtension) {
            case 'xls':
            case 'xlsx':
            case 'xlx':
            case 'xlsm':
            case 'xlm':
                processExcelFile(fileToBeProcessed, baseColumnTypeMap, fileExtension)
                break
            case 'csv':
                processCsvFile(fileToBeProcessed, baseColumnTypeMap)
                break
            default:
                log.error("Not supported")
        }
    }

    boolean checkFileFormat(File fileToBeProcessed) {
        Map baseColumnTypeMap = ['product': 'String', 'product hierarchy': 'String', 'assignment': 'String', 'workflow group': 'String','alert group': 'String', 'import': 'String']
        boolean isCorrectFormat = false
        String fileExtension = FilenameUtils.getExtension(fileToBeProcessed.name).toLowerCase()
        switch (fileExtension) {
            case 'xls':
            case 'xlsx':
            case 'xlx':
            case 'xlsm':
            case 'xlm':
                isCorrectFormat = checkExcelFileFormat(fileToBeProcessed, baseColumnTypeMap, fileExtension)
                break
            case 'csv':
                isCorrectFormat = checkCsvFileFormat(fileToBeProcessed, baseColumnTypeMap)
                break
            default:
                log.error("Not supported")
        }
        isCorrectFormat
    }

    List<Map> processExcelFile(File file, Map<String, String> baseColumnTypeMap, String fileExtension) {
        InputStream is = new FileInputStream(file)
        Workbook workbook
        if (Constants.XLS_FORMAT.equalsIgnoreCase(fileExtension)) {
          workbook = WorkbookFactory.create(file)
        } else {
          workbook = StreamingReader.builder()
                    .rowCacheSize(40000)     // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)           // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is)
        }
        Sheet sheet = workbook.getSheetAt(0)
        List dataList = []
        Iterator<Row> rowIterator = sheet.rowIterator()
        rowIterator.next()
        rowIterator.each { row ->
            Cell cell
            Map map = [:]
            baseColumnTypeMap.eachWithIndex { String name, String type, index ->
                cell = row.getCell(index)
                map.put(name, cell ? cell.getStringCellValue().replaceAll("\u00A0", "").trim() : '')
            }
            if (map['product'] || map['product hierarchy'] || map['assignment'] || map['workflow group'] || map['import'] || map['alert group']) {
                dataList.add(map)
            }
        }
        workbook.close()
        dataList
    }

    boolean checkExcelFileFormat(File file, Map<String, String> baseColumnTypeMap, String fileExtension) {
        InputStream is = new FileInputStream(file)
        Workbook workbook
        if (Constants.XLS_FORMAT.equalsIgnoreCase(fileExtension)) {
            workbook = WorkbookFactory.create(file)
        } else {
            workbook = StreamingReader.builder()
                    .rowCacheSize(3)     // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)           // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is)
        }
        Sheet sheet = workbook.getSheetAt(0)
        Row headerRow = sheet.rowIterator().next()
        List<String> headerNamesList = []
        headerRow.each { Cell cell ->
            headerNamesList.add(cell.getStringCellValue().trim().toLowerCase())
        }
        workbook.close()
        int index = headerNamesList.findLastIndexOf { it }
        if ((index + 1) != headerNamesList.size()) {
            headerNamesList = headerNamesList.take(index + 1)
        }
        headerNamesList == baseColumnTypeMap.keySet() as List<String>
    }

    boolean checkCsvFileFormat(File file, Map<String, String> baseColumnTypeMap) {
        Reader inputFile = new FileReader(file)
        CSVParser parser = new CSVParser(inputFile, CSVFormat.EXCEL
                           .withFirstRecordAsHeader())
        List<String> headerNamesList = parser.headerNames.collect {
            CsvDataImporter.removeBom(it).trim().toLowerCase()
        }
        headerNamesList == baseColumnTypeMap.keySet() as List<String>
    }

    Map toActualColumnMap(Map<String, String> baseColumnTypeMap) {
        Map actualColumnMap = [:]
        String revisedKey
        baseColumnTypeMap.eachWithIndex{ String k,String v,int index ->
            revisedKey = k.toLowerCase()
            actualColumnMap[revisedKey] = v
        }
        actualColumnMap
    }

    List<Map> processCsvFile(File file, Map<String, String> baseColumnTypeMap) {
        Reader inputFile = new FileReader(file)
        CSVParser parser = new CSVParser(inputFile, CSVFormat.EXCEL
                .withHeader(baseColumnTypeMap.keySet() as String[])
                .withFirstRecordAsHeader()
                .withTrim())
        List dataList = []
        baseColumnTypeMap = ['Product': 'String', 'Product Hierarchy': 'String', 'Assignment': 'String', 'Workflow Group': 'String', 'Import': 'String']
        parser.each { row ->
            String rowValue
            Map map = [:]
            baseColumnTypeMap.eachWithIndex { String name, String type, index ->
                if(index == 0) {
                    rowValue = CsvDataImporter.removeBom(row.get(0))
                } else {
                    rowValue = row.get(name)
                }
                map.put(name, rowValue ? rowValue.toString().trim() : '')
            }
            map = toActualColumnMap(map)
            if (map['product'] || map['product hierarchy'] || map['assignment'] || map['workflow group'] || map['import']) {
                dataList.add(map)
            }
        }
        dataList
    }

    Map processingOfImportedRecords(File file) {
        List<Map> recordsCreated = []
        List<Map> userViewRecords = []
        List<Map> recordsFailed = []
        List dicList = PVDictionaryConfig.ProductConfig.views.collect {
            messageSource.getMessage(it.code, null, Locale.default)
        }
        Map hierarchyMap = [:]
        dicList.eachWithIndex { value, index ->
            hierarchyMap[value] = index
        }
        List<Group> groupList = cacheService.getDefaultWorkflowGroups()
        List<User> userList = cacheService.generateUserListFromCache()
        List<Map> totalRecordsImported = processFile(file)
        Integer discardedRecords = totalRecordsImported.count {it.import == '0'}
        List<Map> recordsToBeProcessed = totalRecordsImported.findAll { it.import != '0' }
        log.info(" Generating product ids ")
        List<Map> productNameIdList = generateProductNameIdList(recordsToBeProcessed, hierarchyMap)
        log.info(" Product ids are generated ")
        List<Long> newUserIds = []
        recordsToBeProcessed.collate(1000).each{
            it.each { Map record ->
                String importValue = record['import']
                if (importValue == null || importValue.trim() == '') {
                    String assignment = record['assignment']
                    String wfGroup = record['workflow group']
                    String hierarchy = record['product hierarchy']
                    String product = record['product']
                    String alertGroup = record['alert group']
                    String productJson = null
                    Map productMap = null
                    def hierarchyEntry
                    List<Long> userIdList = []
                    List<Long> groupIdList = []
                    Long workflowGroupId = null
                    if (wfGroup) {
                        workflowGroupId = groupList.find { it.name.toLowerCase() == wfGroup.trim().toLowerCase() }?.id
                    }
                    List<Boolean> isUserFirst = [true,true];
                    assignment.split(',').each {
                        addUserOrGroupIdList(it, userList, userIdList, groupIdList, workflowGroupId, newUserIds,isUserFirst)
                    }
                    int cellValueSize = assignment.split(',').size()
                    int totalAvailableAssignSize = userIdList.size() + groupIdList.size()
                    if (hierarchy) {
                        if (hierarchy.trim().toLowerCase() == "product group") {
                            productMap = generateProductMap(product, 199, productNameIdList)
                            productJson = generateProductsGroupJson(product, productNameIdList)
                        } else {
                            hierarchyEntry = hierarchyMap.find {
                                it.key.toString().toLowerCase() == hierarchy.trim().toLowerCase()
                            }
                            if (hierarchyEntry) {
                                productMap = generateProductMap(product, hierarchyEntry.value as Integer, productNameIdList)
                                if (productMap) {
                                    productJson = generateProducts(hierarchyEntry.value as Integer, productMap)
                                }
                            }
                        }
                    }
                    if (((!userIdList.size() || !groupIdList.size()) && totalAvailableAssignSize != cellValueSize) || (wfGroup && !workflowGroupId) || !productJson) {
                        recordsFailed.add(record)
                    } else {
                        if (!workflowGroupId && userIdList) {
                            workflowGroupId = cacheService.getUserByUserId(userIdList[0])?.workflowGroup?.id
                        }
                        Map existingRecord = recordsCreated.find {
                            it.products == productJson && it.hierarchy == hierarchy && it.workflowGroupId && workflowGroupId &&
                                    it.workflowGroupId == workflowGroupId
                        }
                        String firstAssigneeIsUser = isUserFirst.get(1)==true?'true':'false'
                        if (existingRecord) {
                            existingRecord.userIdList = userIdList as Set
                            existingRecord.groupIdList = groupIdList as Set
                            existingRecord.isUserFirst = firstAssigneeIsUser
                        } else {
                            record['Import'] = null
                            record.put("userIdList", userIdList)
                            record.put("groupIdList", groupIdList)
                            record.put("workflowGroupId", workflowGroupId)
                            record.put("products", productJson)
                            record.put("hierarchy", hierarchy)
                            record.put("isUserFirst", firstAssigneeIsUser)
                            record.put("alertGroup", alertGroup)
                            recordsCreated.add(record)
                        }

                        //For user view assignment
                        userIdList.each { Long userId ->
                            Map userViewMap = userViewRecords.find {
                                it.hierarchy == hierarchy && it.user == userId
                            }
                            if (userViewMap) {
                                userViewMap.products.add(productMap)
                            } else {
                                userViewRecords.add([
                                        "hierarchy"      : hierarchy,
                                        "user"           : userId,
                                        "products"       : [productMap] as Set
                                ])
                            }
                        }
                        groupIdList.each { Long groupId ->
                            Map userViewMap = userViewRecords.find {
                                it.hierarchy == hierarchy && it.group == groupId
                            }
                            if (userViewMap) {
                                userViewMap.products.add(productMap)
                            } else {
                                userViewRecords.add([
                                        "hierarchy"      : hierarchy,
                                        "group"          : groupId,
                                        "products"       : [productMap] as Set
                                ])
                            }
                        }
                    }
                } else {
                    recordsFailed.add(record)
                }
            }
        }
        // add missing users in db
        if(newUserIds)
            createNewUsersToPvUserWebappTable(newUserIds.unique())
        log.info(" File has been processed ")
        [totalRecordsImported: totalRecordsImported, recordsCreated: recordsCreated, recordsFailed: recordsFailed, recordsDiscarded: discardedRecords, userViewList: userViewRecords]
    }

    List<Map> generateProductNameIdList(List<Map> recordsToBeProcessed, Map hierarchyMap) {
        List<Map> productNameIdList = []
        HashSet<String> productNameIdSet = []
        Map<String, List> productMap = [:]
        recordsToBeProcessed.collate(1000).each { List<Map> records ->
            records.each { Map record ->
                String hierarchy = record['product hierarchy']
                String product = record['product']
                if (hierarchy && product) {
                    if (hierarchy.trim().toLowerCase() == "product group") {
                        addProductInHierarchyMap(Constants.Commons.PRODUCT_GROUP_VALUE, product.trim().toLowerCase().replaceAll("'", "''"), productMap)
                    } else {
                        def hierarchyEntry = hierarchyMap.find {
                            it.key.toString().toLowerCase() == hierarchy.trim().toLowerCase()
                        }
                        if (hierarchyEntry) {
                            addProductInHierarchyMap(hierarchyEntry.value.toString(), product.trim().toLowerCase().replaceAll("'", "''"), productMap)
                        }
                    }
                }
            }
            productMap.each {
                fetchProductIds(it.value, it.key, productNameIdList, productNameIdSet)
            }
            productMap.clear()
        }
        productNameIdList
    }

    List<String> addProductInHierarchyMap(String hierarchyValue, String product, Map<String, List> productMap) {
        List<String> list = productMap.get(hierarchyValue)
        if (!list) {
            list = []
        }
        if (!list.contains(product)) {
            list.add(product)
        }
        productMap.put(hierarchyValue, list)
    }

    void addUserOrGroupIdList(String assignment, List<User> userList, List<Long> userIdList, List<Long> groupIdList, Long workflowGroupId, List<Long> newUserIds, List<Boolean> isUserFirst) {
        Long userId = generateUserIdForExcel(assignment.trim(), userList, workflowGroupId, newUserIds)
        if (userId){
            userIdList.add(userId)
            if(isUserFirst.get(0)){
                isUserFirst[0] = false
                isUserFirst[1] = true
            }
        }
        Long groupId = groupIdFromTheSystem(assignment.trim())
        if (groupId){
            groupIdList.add(groupId)
            if(isUserFirst.get(0)){
                isUserFirst[0] = false
                isUserFirst[1] = false
            }
        }
    }

    void transferImportLogFile(Map processedRecords, String fileName, User importedBy) {
        def startTime = System.currentTimeSeconds()
        Integer recordsImported = processedRecords.totalRecordsImported.size() - processedRecords.recordsFailed.size() - processedRecords.recordsDiscarded
        Map importedInformation = [importedDate : DateUtil.toDateStringWithoutTimezone(new Date()), totalRecords: processedRecords.totalRecordsImported.size(), recordsImported: recordsImported,
                                   recordsFailed: processedRecords.recordsFailed.size(), recordsDiscarded: processedRecords.recordsDiscarded]
        Map params = [outputFormat: "XLSX"]
        List<Map> failedRecords = (List<Map>) processedRecords.recordsFailed
        File importAssignmentFile = dynamicReportService.createImportAssignmentReport(new JRMapCollectionDataSource(failedRecords), params, importedInformation, importedBy)
        String logFileName = "${FilenameUtils.removeExtension(fileName)}_Log.xlsx"
        moveFile(importAssignmentFile, config.signal.product.assignment.import.folder.logs as String, logFileName)
        log.info("total time taken in transferImportLogFile is ${System.currentTimeSeconds()-startTime}")
    }

    void createNewUsersToPvUserWebappTable(List<Long> userIds) {

        final Sql sql
        List<User> allUsers = cacheService.generateUserListFromCache()
        List<User> userList = allUsers.findAll{ userIds.contains(it.id)}
        Date timeStamp = new Date()
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String sqlStatement = SignalQueryHelper.migrateAllUsersToPvUserWebappTable(userList, timeStamp)
            sql?.execute(sqlStatement)
            log.info("New User's data successfully updated in PVUSER_WEBAPP table.")
        } catch (Exception ex) {
            log.error("New User's data could not be updated in PVUSER_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

}
