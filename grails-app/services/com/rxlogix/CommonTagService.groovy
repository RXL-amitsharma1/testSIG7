package com.rxlogix

import asset.pipeline.AssetSpecLoader
import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.config.ArchivedEmbaseLiteratureAlert
import com.rxlogix.config.ArchivedLiteratureAlert
import com.rxlogix.config.EmbaseLiteratureAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.dto.CategoryDTO
import com.rxlogix.dto.RequestCategoryDTO
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.sql.Sql
import groovyx.net.http.Method
import org.hibernate.SessionFactory

import javax.sql.DataSource
import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import org.springframework.web.context.request.RequestContextHolder

@Transactional
class CommonTagService implements EventPublisher {

    def reportIntegrationService
    def signalDataSourceService
    def userService
    def dataObjectService
    def cacheService
    def signalAuditLogService
    DataSource dataSource_pva
    PvsGlobalTagService pvsGlobalTagService
    SessionFactory sessionFactory

    def getQualAlertCategories(def params) {

        Long alertId = params.alertId as Long
        def sca
        if (params.domain == "Qualitative on demand") {
            sca = SingleOnDemandAlert.findById(alertId)
        } else if (Boolean.parseBoolean(params.isArchived)) {
            sca = ArchivedSingleCaseAlert.findById(alertId)
        } else {
            sca = SingleCaseAlert.findById(alertId)
        }
        Long caseId = sca.caseId
        Long caseSeriesId = sca.executedAlertConfiguration.pvrCaseSeriesId
        String tenantId = Holders.config.categories.tenantId
        String alertLevelParams = tenantId + "," + caseSeriesId + "," + caseId + "," + sca.caseVersion
        String globalLevelParams = tenantId + "," + caseId + "," + sca.caseVersion

        Map inputMap = [alertLevelParams: alertLevelParams, globalLevelParams: globalLevelParams,
                        alertLevelId: Holders.config.category.singleCase.alertSpecific,
                        globalLevelId: Holders.config.category.singleCase.global]

        getCategories(inputMap)
    }

    def getQuanAlertCategories(def params) {

        Long alertId = params.alertId as Long
        def aca
        if (params.domain == "Quantitative on demand") {
            aca = AggregateOnDemandAlert.findById(alertId)
        } else if (Boolean.parseBoolean(params.isArchived)) {
            aca = ArchivedAggregateCaseAlert.findById(alertId)
        } else {
            aca = AggregateCaseAlert.findById(alertId)
        }
        Long productId = aca.productId
        Long ptCode = aca.ptCode
        String smq = aca.smqCode
        Integer prodHierarchyId = aca.prodHierarchyId ?:(-1)
        Integer eventHierarchyId = aca.eventHierarchyId?:(-1)
        Long execConfigId = aca.executedAlertConfiguration.id

        String tenantId = Holders.config.categories.tenantId
        String alertLevelParams = tenantId + ","+alertId + ","+execConfigId
        String globalLevelParams = tenantId + ","+productId + "," + ptCode + "," + smq  + "," + prodHierarchyId + "," + eventHierarchyId

        Map inputMap = [alertLevelParams: alertLevelParams, globalLevelParams: globalLevelParams,
                        alertLevelId: Holders.config.category.aggregateCase.alertSpecific,
                        globalLevelId: Holders.config.category.aggregateCase.global]
        getCategories(inputMap)


    }

    def getLitAlertCategories(def params) {
        def domain
        def isArchived = Boolean.parseBoolean(params.isArchived)
        if (Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))) {
            domain =  isArchived ? ArchivedEmbaseLiteratureAlert : EmbaseLiteratureAlert
        } else {
            domain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
        }
        Long alertId = params.alertId as Long
        def aca = domain.findById(alertId)
        Long articleId = aca.articleId
        String tenantId = Holders.config.categories.tenantId
        String alertLevelParams = tenantId + ","+alertId
        String globalLevelParams = tenantId + ","+articleId

        Map inputMap = [alertLevelParams: alertLevelParams, globalLevelParams: globalLevelParams,
                        alertLevelId: Holders.config.category.literature.alertSpecific,
                        globalLevelId: Holders.config.category.literature.global]
        getCategories(inputMap)
    }

    def saveQualAlertCategories(Long alertId, def existingRows, def newRows, boolean isArchived) {

        def sca = isArchived ? ArchivedSingleCaseAlert.findById(alertId) : SingleCaseAlert.findById(alertId)
        Long caseId = sca.caseId
        Long caseSeriesId = sca.executedAlertConfiguration.pvrCaseSeriesId
        String dataSource = sca.alertConfiguration.selectedDatasource.toUpperCase()
        String userName = userService.getCurrentUserName()
        Long execConfigId = sca.executedAlertConfiguration.id
        String moduleName = isArchived ? "Archived Individual Case Review: Categories" : "Individual Case Review: Categories"
        String entityValue = sca?.getInstanceIdentifierForAuditLog()
        signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, moduleName as String, entityValue,sca.getClass().getSimpleName())
        Map inputMap = [ alert:sca, alertId: alertId, caseSeriesId: caseSeriesId, caseId: caseId, userName: userName, type: "Qualitative",
                        module: Constants.PVS_CASE_SERIES_OWNER, dataSource: dataSource, existingRows: existingRows,
                        newRows: newRows, versionNum: sca.caseVersion, execConfigId: execConfigId]

        saveCategories(inputMap)
    }

    def saveQualOnDemandAlertCategories(Long alertId, def existingRows, def newRows) {

        SingleOnDemandAlert singleOnDemandAlert = SingleOnDemandAlert.findById(alertId)
        Long caseId = singleOnDemandAlert.caseId
        Long caseSeriesId = singleOnDemandAlert.executedAlertConfiguration.pvrCaseSeriesId
        String dataSource = singleOnDemandAlert.alertConfiguration.selectedDatasource.toUpperCase()
        String userName = userService.getCurrentUserName()
        Long execConfigId = singleOnDemandAlert.executedAlertConfigurationId
        String entityValue = singleOnDemandAlert?.getInstanceIdentifierForAuditLog()
        signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, "AdHoc Individual Case Review: Categories", entityValue,singleOnDemandAlert.getClass().getSimpleName())
        Map inputMap = [alert: singleOnDemandAlert, alertId: alertId, caseSeriesId: caseSeriesId, caseId: caseId, userName: userName, type: "Qualitative",
                        module: Constants.PVS_CASE_SERIES_OWNER, dataSource: dataSource, existingRows: existingRows,
                        newRows: newRows, versionNum: singleOnDemandAlert.caseVersion, execConfigId: execConfigId, isAdhoc: true]

        saveCategories(inputMap)
    }

    def saveQuanAlertCategories(Long alertId, def existingRows, def newRows, boolean  isArchived = false, User currentUser = null, Boolean isRestCall = false) {

        def aca =  isArchived ? ArchivedAggregateCaseAlert.findById(alertId) : AggregateCaseAlert.findById(alertId)
        Long productId = aca.productId
        Long ptCode = aca.ptCode
        String smq = aca.smqCode
        String dataSource = aca.alertConfiguration.selectedDatasource.toUpperCase()
        String userName = currentUser?.fullName ?: userService.getCurrentUserName()
        Long execConfigId = aca.executedAlertConfiguration.id
        Integer prodHierarchyId = aca.prodHierarchyId?:-1
        Integer eventHierarchyId = aca.eventHierarchyId?:-1
        String moduleName = isArchived ? "Archived Aggregate Review: Categories" : "Aggregate Review: Categories"
        String entityValue = aca?.getInstanceIdentifierForAuditLog()
        signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, moduleName, entityValue,aca.getClass().getSimpleName(), currentUser)
        Map inputMap = [alert: aca, alertId: alertId, productId: productId, ptCode: ptCode, smq: smq, userName: userName, type: "Quantitative",
                        module: Constants.PVS_CASE_SERIES_OWNER, dataSource: dataSource, existingRows: existingRows,
                        newRows: newRows, execConfigId: execConfigId, prodHierarchyId: prodHierarchyId, eventHierarchyId: eventHierarchyId]

        isRestCall ? saveCategoriesQuantitative(inputMap) : saveCategories(inputMap)


    }

    def saveQuanOnDemandAlertCategories(Long alertId, def existingRows, def newRows) {
        AggregateOnDemandAlert aggregateOnDemandAlert = AggregateOnDemandAlert.findById(alertId)
        Long productId = aggregateOnDemandAlert.productId
        Long ptCode = aggregateOnDemandAlert.ptCode
        String smq = aggregateOnDemandAlert.smqCode
        String dataSource = aggregateOnDemandAlert.alertConfiguration.selectedDatasource.toUpperCase()
        String userName = userService.getCurrentUserName()
        Long execConfigId = aggregateOnDemandAlert.executedAlertConfiguration.id
        Integer prodHierarchyId = aggregateOnDemandAlert.prodHierarchyId?:-1
        Integer eventHierarchyId = aggregateOnDemandAlert.eventHierarchyId?:-1
        String entityValue = aggregateOnDemandAlert?.getInstanceIdentifierForAuditLog()
        signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, "Adhoc Aggregate Review: Categories", entityValue, aggregateOnDemandAlert.getClass().getSimpleName())
        Map inputMap = [alert: aggregateOnDemandAlert, alertId: alertId, productId: productId, ptCode: ptCode, smq: smq, userName: userName, type: "Quantitative",
                        module: Constants.PVS_CASE_SERIES_OWNER, dataSource: dataSource, existingRows: existingRows,
                        newRows: newRows, execConfigId: execConfigId, isAdhoc: true, prodHierarchyId: prodHierarchyId, eventHierarchyId: eventHierarchyId]

        saveCategories(inputMap)
    }

    def saveLitAlertCategories(Long alertId, def existingRows, def newRows, boolean isArchived = false, boolean isEmbase = false) {
        def domain
        if (isEmbase) {
            domain = isArchived ? ArchivedEmbaseLiteratureAlert : EmbaseLiteratureAlert
        } else {
            domain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
        }

        def lca = domain.findById(alertId)
        Long articleId = lca.articleId
        Long execConfigId = lca.exLitSearchConfig.id

        String userName = userService.getCurrentUserName()
        String moduleName = isArchived ? "Archived Literature Review: Categories" : "Literature Review: Categories"
        String entityValue = lca?.getInstanceIdentifierForAuditLog()
        signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, moduleName, entityValue, lca.getClass().getSimpleName())
        Map inputMap = [alert: lca, alertId: alertId, articleId: articleId, userName: userName, type: "Literature",
                        module: Constants.PVS_CASE_SERIES_OWNER, dataSource: "", existingRows: existingRows,
                        newRows: newRows, execConfigId: execConfigId]

        saveCategories(inputMap)
    }

    List getCategories(Map inputMap, User currentUser = null){
        List<Map> output = new ArrayList<>()
        Long loggedUserId = currentUser != null ? currentUser.id : userService.getCurrentUserId()
        Boolean isAdmin = currentUser != null ? currentUser.isAdmin() : userService.getUser().isAdmin()

        ExecutorService executorService = Executors.newFixedThreadPool(2)
        try {
            Set<Callable> callables = new HashSet<Callable>()

            callables.add({ ->
                getTagData(inputMap.alertLevelId, inputMap.alertLevelParams, loggedUserId, isAdmin)

            })

            callables.add({ ->
                getTagData(inputMap.globalLevelId, inputMap.globalLevelParams, loggedUserId, isAdmin)
            })

            List<Future> futureList = executorService.invokeAll(callables)

            futureList.each {
                Map res = it.get()
                if(!res.isEmpty())
                    output << it.get()
            }
            output = getSortedOutput(output)
        }catch(Exception ex){
            log.error(ex.printStackTrace())
        }finally {
            executorService.shutdown()
        }

        output
    }

    List<Map> getSortedOutput(ArrayList<Map> maps) {
        List<Map> response = []
        List<Map> privateTags = []
        for(int i=0; i<maps.size(); i++){
            for(Map.Entry tag : maps.get(i).entrySet()){
                response.add(tag.value)
            }
        }
        Collections.sort( response, new Comparator<Map>()
        {
            int compare( Map o1, Map o2 )
            {
                Date d1 = new Date(o1.updatedDate as String)
                Date d2 = new Date(o2.updatedDate as String)
                return (d1.compareTo(d2)) != 0 ?(d1.compareTo(d2)) :(o1.priority.compareTo( o2.priority ))
            }
        } )
        privateTags = response?.stream().findAll {it.private == true && it.privateAccess == false}
        response -= privateTags
        response
    }

    private Map saveCategories(Map inputMap){
        Map result = [:]

        try{
            List<CategoryDTO> current =  convertToSeparateRows(inputMap.newRows, inputMap.execConfigId)
            List<CategoryDTO> previous =  convertToSeparateRows(inputMap.existingRows, inputMap.execConfigId)
            List<CategoryDTO> categories = buildAllCategories(previous, current, inputMap)
            List<CategoryDTO> categoriesPrev= buildAllCategories(convertToSeparateRows(inputMap.newRows,inputMap.execConfigId), convertToSeparateRows(inputMap.existingRows,inputMap.execConfigId), inputMap)
            Map categoriesMap = [current : categories, previous: categoriesPrev]
            if(categories) {
                List<CategoryDTO> alertLevel = categories.findAll{
                    it.getFactGrpId() == Holders.config.category.singleCase.alertSpecific
                }
                if(alertLevel) {
                    notify 'categories.populate.version.published', [categories: alertLevel, execConfigId : inputMap.execConfigId]
                }
                def url = Holders.config.pvcc.api.url
                def path = Holders.config.pvcc.api.path.save
                def query = JsonOutput.toJson(categoriesMap)
                result = reportIntegrationService.postData(url, path, query, Method.POST)
            }

        }
        catch(Exception e){
            log.error(e.printStackTrace())
        }
        if(result?.result?.status=="Success"){
            def alert = inputMap?.alert
            alert?.lastUpdated = new Date()
            alert?.save(flush:true)
        }
        result

    }

    private Map saveCategoriesQuantitative(Map inputMap){
        Map result = [:]

        try{
            List<CategoryDTO> current =  convertToSeparateRowsQuant(inputMap.newRows, inputMap.execConfigId)
            List<CategoryDTO> previous =  convertToSeparateRowsQuant(inputMap.existingRows, inputMap.execConfigId)
            List<CategoryDTO> categories = buildAllCategories(previous, current, inputMap)
            List<CategoryDTO> categoriesPrev= buildAllCategories(convertToSeparateRowsQuant(inputMap.newRows,inputMap.execConfigId), convertToSeparateRowsQuant(inputMap.existingRows,inputMap.execConfigId), inputMap)
            Map categoriesMap = [current : categories, previous: categoriesPrev]
            if(categories) {
                List<CategoryDTO> alertLevel = categories.findAll{
                    it.getFactGrpId() == Holders.config.category.singleCase.alertSpecific
                }
                if(alertLevel) {
                    notify 'categories.populate.version.published', [categories: alertLevel, execConfigId : inputMap.execConfigId]
                }
                def url = Holders.config.pvcc.api.url
                def path = Holders.config.pvcc.api.path.save
                def query = JsonOutput.toJson(categoriesMap)
                result = reportIntegrationService.postData(url, path, query, Method.POST)
            }

        }
        catch(Exception e){
            log.error(e.printStackTrace())
        }
        if(result?.result?.status=="Success"){
            def alert = inputMap?.alert
            alert?.lastUpdated = new Date()
            alert?.save(flush:true)
        }
        result

    }

    List<Map> getCommonTags() {

        Sql sql = null
        List <Map> tags = []
        try {
            sql = new Sql(signalDataSourceService.getDataSource('pva'))
            sql.eachRow("select * from code_value where code_list_id = ${Holders.config.mart.codeValue.tags.value} and is_master_data=1 and is_deleted =0 ORDER BY UPPER(value) ASC" , []) { row ->
                Map rowData = [id : row.id , text : row.value   , parentId : row.parent_id, display : row.display]
                tags << rowData
            }
            sql.close()
        } catch (Throwable t) {
            log.error("Error on fetching Tags ")
        } finally {
            try {
                sql?.close()
            } catch (Throwable notableToHandle) {
                log.error("Failed to close the Sql", notableToHandle)
            }
        }
        return tags.sort({it?.text.toUpperCase()})
    }

    def getTagData(Integer alertLevelId, String params, Long loggedUserId, Boolean isAdmin){
        Map<String, Map> output = new HashMap<>()
        try{
            def url = Holders.config.pvcc.api.url
            def path = Holders.config.pvcc.api.path.get
            def query = [grpId: alertLevelId, grpParams: params]
            Map result = reportIntegrationService.get(url, path, query)
            Map res = result.data?:[:]
            User currentUser = User.get(loggedUserId)
            Map<Integer, ArrayList<String>> dateChange = [:]
            for(TreeMap map in res.data){
                if(dateChange.containsKey(map.catId)){
                    dateChange.get(map.catId)[0] = dateChange.get(map.catId)[0]<fromStringDate(map.createdDate, currentUser)?dateChange.get(map.catId)[0]:fromStringDate(map.createdDate, currentUser)
                    dateChange.get(map.catId)[1] = dateChange.get(map.catId)[1]>fromStringDate(map.updatedDate, currentUser)?dateChange.get(map.catId)[1]:fromStringDate(map.updatedDate, currentUser)
                }else{
                    def dates = []
                    dates.add(map.createdDate? fromStringDate(map.createdDate, currentUser): "")
                    dates.add(map.updatedDate? fromStringDate(map.updatedDate, currentUser): "")
                    dateChange.put(map.catId, dates)
                }
            }
            for(TreeMap map in res.data){
                Integer privateUserId = map.privateUserId
                Boolean alertLevel = (Holders.config.categories.group.alertLevel).contains(map.factGrpId)
                Map row = [category: [id: map.catId, name: map.catName], subcategory: [[id: map.subCatId, name: map.subCatName]],
                           alert: alertLevel, private: privateUserId > 0? true: false,
                           privateAccess: privateUserId == loggedUserId ? true: false,
                           createdBy: getUserFullName(map.createdBy), updatedBy:getUserFullName(map.updatedBy), priority: map.priority,
                           createdDate: dateChange.get(map.catId)?dateChange.get(map.catId)[0]:"",
                           updatedDate: dateChange.get(map.catId)?dateChange.get(map.catId)[1]:"",
                           updatedDateInit: map.updatedDate, updatedByInit: map.updatedBy,
                           createdDateInit: map.createdDate, createdByInit: map.createdBy,
                           autoTagged: map.isAutoTagged > 0? true: false,
                           autoTaggedEditable: map.isAutoTagged > 0? isAdmin: true,
                           isRetained: map.isRetained > 0? true: false,
                           execConfigId: map.udNumber1, privateUserId:map.privateUserId,
                           dataSource: map.dataSource
                           ]
                String categoryString = map.catName + '(' + alertLevel +')('+row.private +')(' + row.privateAccess+')'
                Map rowExists = output.get(categoryString)
                if(rowExists && rowExists.alert == row.alert && row.private == rowExists.private && row.privateAccess == rowExists.privateAccess){
                    if(row.subcategory[0].id != 0 && row.subcategory[0].name != null) {
                        rowExists.subcategory << row.subcategory[0]
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
                        Date existingRowCreatedDate = rowExists.createdDate ? sdf.parse(rowExists.createdDate) : null
                        Date rowCreatedDate = row.createdDate ? sdf.parse(row.createdDate) : null
                        Date existingRowUpdatedDate = rowExists.updatedDate ? sdf.parse(rowExists.updatedDate) : null
                        Date rowUpdatedDate = row.updatedDate ? sdf.parse(row.updatedDate) : null
                        if (rowUpdatedDate && existingRowUpdatedDate && rowUpdatedDate.after(existingRowUpdatedDate)) {
                            rowExists.updatedDate = row.updatedDate
                            rowExists.updatedBy = row.updatedBy
                        }
                        if (existingRowCreatedDate  && rowCreatedDate && existingRowCreatedDate.after(rowCreatedDate)) {
                            rowExists.createdDate = row.createdDate
                            rowExists.createdBy = row.createdBy
                        }
                        output.put(categoryString, rowExists)
                    }
                }else {
                    if(row.subcategory[0].name == null)
                        row.subcategory = []
                    output.put(categoryString, row)
                }

            }

        }
        catch(Exception e){
            log.error(e.getMessage())
        }

        output
    }

    private List<CategoryDTO> convertToSeparateRows(List<Map> rows, Long execConfigId){
        List<CategoryDTO> categories = new LinkedList<>()

        for(Map categoryRow : rows){

            if(categoryRow.subcategory.size() > 0){
                for(Map subcat : categoryRow.subcategory){
                    categories.add(getCategoryRow(categoryRow, subcat, execConfigId))
                }
            }
            else{
                categories.add(getCategoryRow(categoryRow, null, execConfigId))
            }
        }
        categories
    }

    private List<CategoryDTO> convertToSeparateRowsQuant(List<Map> rows, Long execConfigId){
        List<CategoryDTO> categories = new LinkedList<>()

        for(Map categoryRow : rows){
            if(categoryRow?.category?.subcategories?.size() > 0){
                for(Map subcat : categoryRow.category.subcategories){
                    categories.add(getCategoryRow(categoryRow, subcat, execConfigId))
                }
            }
            else{
                categories.add(getCategoryRow(categoryRow, null, execConfigId))
            }
        }
        categories
    }

    private CategoryDTO getCategoryRow(Map row, Map subcategory, Long execConfigId){
        CategoryDTO separateRow = new CategoryDTO()
        separateRow.setCatName(row.category.name)
        separateRow.setCatId(row.category.id instanceof String? row.category.id.isLong()? Long.parseLong(row.category.id): null: row.category.id)

        separateRow.setSubCatName(subcategory? subcategory.name : null)
        separateRow.setSubCatId(subcategory?.id instanceof String? subcategory.id.isLong() ? Long.parseLong(subcategory.id): null: subcategory?.id)

        separateRow.setPrivateUserId(row.private ? userService.getCurrentUserId() : null)
        separateRow.setAlertLevel(row.alert)
        separateRow.setPriority(row.priority)
        separateRow.setCreatedBy(row.createdByInit)
        separateRow.setCreatedDate(row.createdDateInit)
        separateRow.setUpdatedBy(row.updatedByInit)
        separateRow.setUpdatedDate(row.updatedDateInit)
        separateRow.setFactGrpCol10(row.privateUserId.toString())
        separateRow.setIsAutoTagged(row.autoTagged? 1:0)
        separateRow.setIsRetained(row.isRetained?1:0)
        separateRow.setUdNumber1(row.execConfigId!=-1?row.execConfigId:execConfigId)
        separateRow.setDataSource(row.dataSource)
        separateRow
    }

    private List<CategoryDTO> buildAllCategories(List<CategoryDTO> previous, List<CategoryDTO> current, Map inputMap){

        List<CategoryDTO> finalCategories = new LinkedList<>();
        CategoryDTO category
        Integer count = 0
        Long loggedUserId = userService.getCurrentUserId()
        // insert and update category
        for(CategoryDTO currentRow: current){
            Boolean insert = true
            Boolean update = false
            Boolean dateUpdate = true

            for(CategoryDTO previousRow: previous){
                if(currentRow.catId == previousRow.catId && currentRow.subCatId == previousRow.subCatId &&
                currentRow.catName == previousRow.catName && currentRow.subCatName == previousRow.subCatName &&
                currentRow.alertLevel == previousRow.alertLevel){
                    insert = false
                    //handled existing private category for new User
                    if (currentRow.privateUserId != null && (currentRow.privateUserId != previousRow.factGrpCol10)) {
                        break
                    }
                    if(currentRow.privateUserId != previousRow.privateUserId || currentRow.priority != previousRow.priority) {
                        update = true
                        currentRow.setIsAutoTagged(previousRow.getIsAutoTagged())
                        currentRow.setIsRetained(previousRow.getIsRetained())
                        currentRow.setUdNumber1(previousRow.getUdNumber1())
                        if(previousRow.priority > 900){
                            dateUpdate = false
                            currentRow.setUpdatedBy(previousRow.getUpdatedBy())
                            currentRow.setUpdatedDate(previousRow.getUpdatedDate())
                        }
                    }
                    break
                }
            }
            if(insert || update) {
                finalCategories.add(buildCategory(currentRow, insert ? "I" : "U", inputMap, update?currentRow.priority:null, dateUpdate))
                count++
            }
        }

        // delete category
        for(CategoryDTO previousRow: previous){
            Boolean delete = true
            Boolean update = false
            for(CategoryDTO currentRow: current){
                if(currentRow.catId == previousRow.catId && currentRow.subCatId == previousRow.subCatId &&
                        currentRow.catName == previousRow.catName && currentRow.subCatName == previousRow.subCatName &&
                        currentRow.alertLevel == previousRow.alertLevel){
                        delete = false
                        break
                }
            }
            if(previousRow.privateUserId >0 && previousRow.privateUserId != loggedUserId){
                delete = false
                update = true
            }
            if(delete){
                finalCategories.add(buildCategory(previousRow, "D", inputMap, null, true))
            }
            else if(update){
                finalCategories.add(buildCategory(previousRow, "U", inputMap, count, true))
                count++
            }
        }
        updateCreatedDate(finalCategories)
    }


    private CategoryDTO buildCategory(CategoryDTO categoryDTO, String ops, Map inputMap, Integer count=null, Boolean dateUpdate){
        categoryDTO.setAlertId(inputMap.alertId as String)
        categoryDTO.setDmlType(ops)
        categoryDTO.setModule(inputMap.module)
        if(categoryDTO.dataSource == 'undefined'){
            categoryDTO.setDataSource(inputMap.dataSource)
        }
        categoryDTO.setIsAdhoc(inputMap.containsKey("isAdhoc"))
        if(count != null)
            categoryDTO.setPriority(count)
        if(ops == "I") {
            categoryDTO.setCreatedBy(inputMap.userName)
            categoryDTO.setCreatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
        }
        if(dateUpdate) {
            categoryDTO.setUpdatedBy(inputMap.userName)
            categoryDTO.setUpdatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
        }
        categoryDTO.setFactGrpCol1(Holders.config.categories.tenantId)

        switch(inputMap.type){
            case "Quantitative":
                if(categoryDTO.alertLevel){
                    categoryDTO.setFactGrpId(Holders.config.category.aggregateCase.alertSpecific)
                    categoryDTO.setFactGrpCol2(inputMap.alertId as String)
                    categoryDTO.setFactGrpCol3(inputMap.execConfigId as String)
                }else{
                    categoryDTO.setFactGrpId(Holders.config.category.aggregateCase.global)
                    categoryDTO.setFactGrpCol2(inputMap.productId as String)
                    categoryDTO.setFactGrpCol3(inputMap.ptCode as String)
                    categoryDTO.setFactGrpCol4(inputMap.smq?: "null")
                    categoryDTO.setFactGrpCol5(inputMap.prodHierarchyId as String)
                    categoryDTO.setFactGrpCol6(inputMap.eventHierarchyId as String)
                }
                break
            case "Qualitative":
                if(categoryDTO.alertLevel){
                    categoryDTO.setFactGrpId(Holders.config.category.singleCase.alertSpecific)
                    categoryDTO.setFactGrpCol2(inputMap.caseSeriesId as String)
                    categoryDTO.setFactGrpCol3(inputMap.caseId as String)
                    categoryDTO.setFactGrpCol4(inputMap.versionNum as String)
                }else{
                    categoryDTO.setFactGrpId(Holders.config.category.singleCase.global)
                    categoryDTO.setFactGrpCol2(inputMap.caseId as String)
                    categoryDTO.setFactGrpCol3(inputMap.versionNum as String)
                }
                break
            case "Literature":
                if(categoryDTO.alertLevel){
                    categoryDTO.setFactGrpId(Holders.config.category.literature.alertSpecific)
                    categoryDTO.setFactGrpCol2(inputMap.alertId as String)
                }else{
                    categoryDTO.setFactGrpId(Holders.config.category.literature.global)
                    categoryDTO.setFactGrpCol2(inputMap.articleId as String)
                }
                break

        }
        categoryDTO
    }

    private String fromStringDate(String inputDate, User currentUser){
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inputDate)
        String timeZone = currentUser.preference.timeZone
        return DateUtil.StringFromDate(date, DateUtil.DATEPICKER_FORMAT_AM_PM_2, timeZone)
    }

    private String getUserFullName(String userName){
        return userService.getUserByUsername(userName)?.getFullName()?: Constants.Commons.SYSTEM
    }

    private void isAlertLevelChange(CategoryDTO currentRow, List<CategoryDTO> deletedRows){
        CategoryDTO deletedRow = deletedRows.find {it -> it.catName == currentRow.catName &&
                                it.subCatName == currentRow.subCatName}
        if(deletedRow){
            currentRow.setCreatedBy(deletedRow.getCreatedBy())
            currentRow.setCreatedDate(deletedRow.getCreatedDate())
        }
    }

    private List<CategoryDTO> updateCreatedDate(List<CategoryDTO> finalCategories){
        List<CategoryDTO> deletedRows = finalCategories.findAll {it -> it.dmlType == "D"}
        finalCategories.each { it ->
            if(it.dmlType == "I"){
                isAlertLevelChange(it, deletedRows)
            }
        }
        finalCategories
    }

    void syncETLCasesWithCategories() {

        final Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        List caseIds = []
        List <String> caseIdString = []
        try {
            def qry = SignalQueryHelper.fetchETLCases()
            sql.eachRow(qry, []) { row ->
                caseIds.add(row.CASE_ID)
            }
            caseIds.each {
                pvsGlobalTagService.refreshGlobalTagsForCaseId(it as String)
            }
            caseIds.collate(999).each {
                caseIdString.add(it.join(",").toString())
            }
            if (caseIdString) {
                String updateQuery = SignalQueryHelper.updateETLCases(caseIdString)
                sql.executeUpdate(updateQuery)
            }

        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    def fetchAllAlertAndGlobalCategories(def alertList,Integer alertLevelId, Integer globalLevelId){
        List<Map> output = []
        ExecutorService executorService = Executors.newFixedThreadPool(2)
        try {
            Set<Callable> callables = new HashSet<Callable>()

            callables.add({ ->
                fetchAllTagData(alertList, alertLevelId)

            })

            callables.add({ ->
                fetchAllTagData(alertList,globalLevelId)
            })

            List<Future> futureList = executorService.invokeAll(callables)


            futureList.each {
                def res = it.get()
                if(res)
                    output << it.get()
            }

        }catch(Exception ex){
            log.error(ex.printStackTrace())
        }finally {
            executorService.shutdown()
        }

        output
    }

    def fetchAllTagData(def alertList, Integer grpId){
        def result = [:]

        try {

            RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO()
            requestCategoryDTO.grpId = grpId
            List<CategoryDTO> catList = []

            alertList.each{it ->
                    CategoryDTO categoryDTO = new CategoryDTO()
                switch (grpId){
                    case 1:
                        categoryDTO.setFactGrpId(Holders.config.category.singleCase.alertSpecific)
                        categoryDTO.setFactGrpCol2(it.executedAlertConfiguration.pvrCaseSeriesId as String)
                        categoryDTO.setFactGrpCol3(it.caseId as String)
                        categoryDTO.setFactGrpCol4(it.caseVersion as String)
                        break
                    case 2:
                        categoryDTO.setFactGrpId(Holders.config.category.singleCase.global)
                        categoryDTO.setFactGrpCol2(it.caseId as String)
                        categoryDTO.setFactGrpCol3(it.caseVersion as String)
                        break
                    case 3:
                        categoryDTO.setFactGrpId(Holders.config.category.aggregateCase.alertSpecific)
                        categoryDTO.setFactGrpCol2(it.id as String)
                        break
                    case 4:
                        categoryDTO.setFactGrpId(Holders.config.category.aggregateCase.global)
                        categoryDTO.setFactGrpCol2(it.productId as String)
                        categoryDTO.setFactGrpCol3(it.ptCode as String)
                        categoryDTO.setFactGrpCol4(it.smqCode?: "null")
                        break
                    case 5:
                        categoryDTO.setFactGrpId(Holders.config.category.literature.alertSpecific)
                        categoryDTO.setFactGrpCol2(it.id as String)
                        break
                    case 6:
                        categoryDTO.setFactGrpId(Holders.config.category.literature.global)
                        categoryDTO.setFactGrpCol2(it.articleId as String)
                        break
                }

                    catList.add(categoryDTO)
                }

            requestCategoryDTO.categoryDTOS = catList

            def url = Holders.config.pvcc.api.url
            def path = Holders.config.pvcc.api.path.fetchAll
            def query = JsonOutput.toJson(requestCategoryDTO)
            result = reportIntegrationService.postData(url, path, query)

        }
        catch (Exception e) {
            log.error(e.printStackTrace())
        }
        result.result

    }

    def fetchCommonCategories(def params){

        def domain
        def alertList
        Integer alertLevelId
        Integer globalLevelId
        List<String> alertIds = params.alertId.split(',')
        Boolean isArchived = params.isArchived.toBoolean()
        switch (params.domain){
            case 'Qualitative':
                domain = isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
                alertLevelId = Holders.config.category.singleCase.alertSpecific
                globalLevelId = Holders.config.category.singleCase.global
                break
            case 'Qualitative on demand':
                domain = SingleOnDemandAlert
                alertLevelId = Holders.config.category.singleCase.alertSpecific
                globalLevelId = Holders.config.category.singleCase.global
                break
            case 'Quantitative':
                domain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
                alertLevelId = Holders.config.category.aggregateCase.alertSpecific
                globalLevelId = Holders.config.category.aggregateCase.global
                break
            case 'Quantitative on demand':
                domain = AggregateOnDemandAlert
                alertLevelId = Holders.config.category.aggregateCase.alertSpecific
                globalLevelId = Holders.config.category.aggregateCase.global
                break
            case 'Literature':
                Boolean isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
                if (isEmbase) {
                    domain = isArchived ? ArchivedEmbaseLiteratureAlert : EmbaseLiteratureAlert
                } else {
                    domain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
                }
                alertLevelId = Holders.config.category.literature.alertSpecific
                globalLevelId = Holders.config.category.literature.global
                break
        }

        alertList = domain.findAllByIdInList(alertIds*.toLong())


        Map <StringBuilder , Integer> tagsCount = [:]
        Map <StringBuilder , Map> tagsData = [:]

        log.info( "Fetching all Categories")
        def startTime =System.currentTimeMillis()
        List allCategories= fetchAllAlertAndGlobalCategories( alertList, alertLevelId,  globalLevelId)
        def endTime =System.currentTimeMillis()
        log.info( "Fetching all Categories completed, Total time taken -  Time: ${endTime- startTime}")
        StringBuilder sBuilder = new StringBuilder()
        StringBuilder sBuilder2 = new StringBuilder()


        Map <String,String> caseMap = [:]
        Map <String , Map> data = [:]
        Map <String,Integer> dataCount =[:]


        dataObjectService.clearBulkCategoriesListForCase() //for save
        allCategories.data[0].each {
            sBuilder.setLength(0)
            sBuilder2.setLength(0)
            String caseId
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(it.udNumber1 as Integer )
            Boolean isAdhocRun = executedConfiguration ? executedConfiguration.adhocRun : false
            if(params.domain in ['Qualitative','Qualitative on demand'] ){
                 caseId = (it.factGrpId == 1 ? it.factGrpCol3: it.factGrpCol2) + it.catName
            }else if(params.domain in ['Quantitative','Quantitative on demand']){
                 caseId = (it.factGrpId == 3 ? it.factGrpCol2: it.factGrpCol2+'-'+it.factGrpCol3+'-'+it.factGrpCol4) + it.catName
            }else{ // Literature
                caseId =  it.factGrpId +'-'+it.factGrpCol2 + it.catName
            }


            if((params.domain in ['Qualitative','Quantitative','Literature']) || ((params.domain == 'Quantitative on demand' || params.domain == 'Qualitative on demand') && isAdhocRun)) {
                sBuilder.append(it.subCatName + '@' + it.catName + '-' + it.factGrpId + '-' + it.privateUserId)
                sBuilder2.append(it.catName + '-' + it.factGrpId + '-' + it.privateUserId)
            }
            if(sBuilder?.length() != 0 || sBuilder2?.length() != 0) {
                if (!tagsCount.containsKey(sBuilder.toString())) {

                    tagsCount.put(sBuilder.toString(), 1)
                    tagsData.put(sBuilder.toString(), it)
                } else {
                    tagsCount.put(sBuilder.toString(), tagsCount.get(sBuilder.toString()) + 1)
                    if (!tagsData.get(sBuilder.toString()).isAutoTagged && it.isAutoTagged) {
                        tagsData.put(sBuilder.toString(), it)
                    }
                }
                if (!dataCount.containsKey(sBuilder2.toString())) {
                    dataCount.put(sBuilder2.toString(), 1)
                    caseMap.put(caseId, !it.subCatName ? sBuilder2.toString() : '')
                    data.put(sBuilder2.toString(), it)
                } else {
                    if (!caseMap.containsKey(caseId)) {
                        dataCount.put(sBuilder2.toString(), dataCount.get(sBuilder2.toString()) + 1)
                        caseMap.put(caseId, !it.subCatName ? sBuilder2.toString() : '')
                        if (data.get(sBuilder2.toString()).subCatName && !it.subCatName) {
                            if (data.get(sBuilder2.toString()).isAutoTagged) {
                                it.isAutoTagged = 1
                            }
                            data.put(sBuilder2.toString(), it)
                        }
                    }
                }
            }
            //for save
            dataObjectService.saveBulkCategoriesListForCase(caseId , Collections.synchronizedMap(new HashMap(it)) )


        }
        allCategories.data[1].each {
            sBuilder.setLength(0)
            sBuilder2.setLength(0)
            String caseId
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(it.udNumber1 as Integer )
            Boolean isAdhocRun = executedConfiguration ? executedConfiguration.adhocRun : false
            if(params.domain in ['Qualitative','Qualitative on demand'] ){
                caseId = (it.factGrpId == 1 ? it.factGrpCol3: it.factGrpCol2) + it.catName
            }else if(params.domain in ['Quantitative','Quantitative on demand']){
                caseId = (it.factGrpId == 3 ? it.factGrpCol2: it.factGrpCol2+'-'+it.factGrpCol3+'-'+it.factGrpCol4) + it.catName
            }else{ // Literature
                caseId =  it.factGrpId +'-'+it.factGrpCol2 + it.catName
            }


            if((params.domain in ['Qualitative','Quantitative','Literature']) || ((params.domain == 'Quantitative on demand' || params.domain == 'Qualitative on demand') && isAdhocRun)) {
                sBuilder.append(it.subCatName + '@' + it.catName + '-' + it.factGrpId + '-' + it.privateUserId)
                sBuilder2.append(it.catName + '-' + it.factGrpId + '-' + it.privateUserId)
            }
            if(sBuilder?.length() != 0 || sBuilder2?.length() != 0) {
                if (!tagsCount.containsKey(sBuilder.toString())) {
                    tagsCount.put(sBuilder.toString(), 1)
                    tagsData.put(sBuilder.toString(), it)
                } else {
                    tagsCount.put(sBuilder.toString(), tagsCount.get(sBuilder.toString()) + 1)
                    if (!tagsData.get(sBuilder.toString()).isAutoTagged && it.isAutoTagged) {
                        tagsData.put(sBuilder.toString(), it)
                    }
                }

                if (!dataCount.containsKey(sBuilder2.toString())) {
                    dataCount.put(sBuilder2.toString(), 1)
                    caseMap.put(caseId, !it.subCatName ? sBuilder2.toString() : '')
                    data.put(sBuilder2.toString(), it)
                } else {
                    if (!caseMap.containsKey(caseId)) {
                        dataCount.put(sBuilder2.toString(), dataCount.get(sBuilder2.toString()) + 1)
                        caseMap.put(caseId, !it.subCatName ? sBuilder2.toString() : '')
                        if (data.get(sBuilder2.toString()).subCatName && !it.subCatName) {
                            if (data.get(sBuilder2.toString()).isAutoTagged) {
                                it.isAutoTagged = 1
                            }
                            data.put(sBuilder2.toString(), it)
                        }
                    }
                }
            }
            //for save
            dataObjectService.saveBulkCategoriesListForCase(caseId , Collections.synchronizedMap(new HashMap(it)) )

        }

        def commonCategories = []
        tagsCount?.each{key,value->

            if(value == alertList.size()){
                commonCategories.add(tagsData.get(key))

                if(data.containsKey(key.split('@')[1])){

                    data.remove(key.split('@')[1])
                }
            }
        }

        dataCount?.each{key,value ->

            if(value == alertList.size() && caseMap.values().contains(key) && data.get(key)){

                commonCategories.add(data.get(key))
            }

        }

        convertCategories(commonCategories)

    }


    def convertCategories(List<Map> commonCategories){
        User currentUser = userService.getUser()
        Long loggedUserId = currentUser.id

        Map<String, Map> output = new HashMap<>()
        try {

            for (TreeMap map in commonCategories) {
                Integer privateUserId = map.privateUserId
                Boolean alertLevel = (Holders.config.categories.group.alertLevel).contains(map.factGrpId)
                Map row = [category          : [id: map.catId, name: map.catName], subcategory: [[id: map.subCatId, name: map.subCatName]],
                           alert             : alertLevel, private: privateUserId > 0 ? true : false,
                           privateAccess     : privateUserId == loggedUserId ? true : false,
                           createdBy         : "", updatedBy: "", priority: map.priority,
                           createdDate       : "",
                           updatedDate       : "",
                           updatedDateInit   : "", updatedByInit: "",
                           createdDateInit   : "", createdByInit: "",
                           autoTagged        : map.isAutoTagged > 0 ? true : false,
                           autoTaggedEditable:  true,
                           isRetained        : map.isRetained > 0 ? true : false,
                           execConfigId      : map.udNumber1, privateUserId: map.privateUserId
                ]
                String categoryString = map.catName + '(' + alertLevel + ')(' + row.private + ')(' + row.privateAccess + ')'
                Map rowExists = output.get(categoryString)
                if (rowExists && rowExists.alert == row.alert && row.private == rowExists.private && row.privateAccess == rowExists.privateAccess) {
                    if (row.subcategory[0].id != 0 && row.subcategory[0].name != null) {
                        rowExists.subcategory << row.subcategory[0]
                        output.put(categoryString, rowExists)
                    }
                } else {
                    if (row.subcategory[0].name == null)
                        row.subcategory = []
                    output.put(categoryString, row)
                }

            }
        }catch(Exception e){
        log.error(e.getMessage())
    }

    output.values()
    }

    Map saveCommonCategories(Map params, Boolean isRestCall = false){
        List<Map> existingRows = isRestCall ? params.existingRows : JSON.parse(params.existingRows)
        List<Map> newRows = isRestCall ? params.newRows : JSON.parse(params.newRows)
        Long startTime1 = System.currentTimeMillis()

        List<CategoryDTO> current = convertToSeparateRowsQuant(newRows, null)
        List<CategoryDTO> previous = convertToSeparateRowsQuant(existingRows, null)
        Long endTime1 = System.currentTimeMillis()
        log.info('Time taken to Convert to separate DTOs: '+ (endTime1 - startTime1))

        Long startTime2 = System.currentTimeMillis()
        List<CategoryDTO> categories = buildAllCommonCategories(previous, current)
        Long endTime2 = System.currentTimeMillis()
        log.info('Time taken to build category DTO: '+ (endTime2 - startTime2))

        categories.sort{
            it.dmlType
        }


        Boolean isArchived = isRestCall ? params.isArchived : params.isArchived != 'false'
        Boolean isCaseSeries =  params.isCaseSeries != 'false'
        def alertList = []
        def domain
        def type
        if(params.type == 'Qualitative'){
            domain = isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
            type  ='Qualitative'
        }else if(params.type == 'Qualitative on demand'){
            domain = SingleOnDemandAlert
            type  ='Qualitative'
        }else if(params.type == 'Quantitative'){
            domain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
            type  ='Quantitative'
        }else if(params.type == 'Quantitative on demand'){
            domain = AggregateOnDemandAlert
            type  ='Quantitative'
        } else {
            Boolean isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
            if (isEmbase) {
                domain = isArchived ? ArchivedEmbaseLiteratureAlert : EmbaseLiteratureAlert
            } else {
                domain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
            }
            type = 'Literature'
        }
        if (params.selectedRow == 'current') {
            def alert = domain.get(params.alertId as Long)
            if (alert) {
                alertList.add(alert)
            }
        } else {
            alertList = domain.createCriteria().list {
                'in'("id", params.alertId*.toLong())
            }
        }

        List<CategoryDTO> currentCategoriesAllAlert =[]
        List<CategoryDTO> prevCategoriesAllAlert =[]

        Long startTime3 = System.currentTimeMillis()

        alertList.each{
            // creating entityValue based on alert
            if(params.type == 'Qualitative'){
                signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, Constants.AuditLog.SINGLE_REVIEW + Constants.AuditLog.CATEGORY, it.getInstanceIdentifierForAuditLog(), domain.getSimpleName())
            }else if(params.type == 'Quantitative'){
                signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, Constants.AuditLog.AGGREGATE_REVIEW + Constants.AuditLog.CATEGORY, it.getInstanceIdentifierForAuditLog(), domain.getSimpleName())
            } else if(params.type == 'Literature'){
                signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, Constants.AuditLog.LITERATURE_REVIEW + Constants.AuditLog.CATEGORY, it.getInstanceIdentifierForAuditLog(), domain.getSimpleName())
            } else if(params.type == 'Quantitative on demand'){
                signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, Constants.AuditLog.ADHOC_AGGREGATE_REVIEW + Constants.AuditLog.CATEGORY, it.getInstanceIdentifierForAuditLog(), domain.getSimpleName())
            } else if(params.type == 'Qualitative on demand'){
                signalAuditLogService.saveAuditTrailForCategories(existingRows as List<Map>, newRows as List<Map>, Constants.AuditLog.ADHOC_SINGLE_REVIEW + Constants.AuditLog.CATEGORY, it.getInstanceIdentifierForAuditLog(), domain.getSimpleName())
            }
            for(CategoryDTO catDTO in categories){
                String bulkCatListKey
                switch (type) {
                    case 'Qualitative':
                        catDTO.factGrpId = catDTO.alertLevel ? Holders.config.category.singleCase.alertSpecific : Holders.config.category.singleCase.global
                        bulkCatListKey = it.caseId + '' + catDTO.getCatName()
                        break
                    case 'Quantitative':
                        catDTO.factGrpId = catDTO.alertLevel ? Holders.config.category.aggregateCase.alertSpecific : Holders.config.category.aggregateCase.global
                        bulkCatListKey = (catDTO.factGrpId == 3 ? it.id : it.productId + '-' + it.ptCode + '-' + it.smqCode) + catDTO.getCatName()
                        break
                    case 'Literature':
                        catDTO.factGrpId = catDTO.alertLevel ? Holders.config.category.literature.alertSpecific : Holders.config.category.literature.global
                        bulkCatListKey = (catDTO.factGrpId == 5 ? catDTO.factGrpId+'-'+it.id : catDTO.factGrpId + '-' + it.articleId) + catDTO.getCatName()
                        break
                }

                switch (catDTO.dmlType) {
                    case 'D':
                        List<Map> catSubcatList = dataObjectService.getBulkCategoriesListForCase(bulkCatListKey)

                        if ((!catDTO.getSubCatName()) && catSubcatList) {
                            for (Map map in catSubcatList) {

                                CategoryDTO newCatDTO = new CategoryDTO()
                                CategoryDTO prevCatDTO = new CategoryDTO()
                                newCatDTO = catDTO.clone()
                                if (map.subCatId) {
                                    newCatDTO.setSubCatId(map.subCatId)
                                    newCatDTO.setSubCatName(map.subCatName)
                                }
                                newCatDTO = addAlertDetailsTocategory(newCatDTO, type, it, map,isCaseSeries)

                                currentCategoriesAllAlert.add(newCatDTO)
                                prevCatDTO = newCatDTO.clone()
                                prevCatDTO.setDmlType('I')
                                prevCatDTO.setCreatedBy(userService.getCurrentUserName())
                                prevCatDTO.setCreatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
                                prevCatDTO.setUpdatedBy(userService.getCurrentUserName())
                                prevCatDTO.setUpdatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
                                prevCategoriesAllAlert.add(prevCatDTO)
                            }
                            dataObjectService.saveSubCatListForBulkCategories(bulkCatListKey, [])

                        } else if (catSubcatList) {
                            Map catMap = catSubcatList?.find { map ->
                                map.catName == catDTO.catName && map.subCatName == catDTO.subCatName
                            }
                            catSubcatList = catSubcatList - catMap

                            dataObjectService.saveSubCatListForBulkCategories(bulkCatListKey, catSubcatList)

                            CategoryDTO newCatDTO = new CategoryDTO()
                            CategoryDTO prevCatDTO = new CategoryDTO()
                            newCatDTO = catDTO.clone()
                            newCatDTO = addAlertDetailsTocategory(newCatDTO, type, it, catMap,isCaseSeries)
                            currentCategoriesAllAlert.add(newCatDTO)
                            prevCatDTO = newCatDTO.clone()
                            prevCatDTO.setDmlType('I')
                            prevCatDTO.setCreatedBy(userService.getCurrentUserName())
                            prevCatDTO.setCreatedDate(new Date().format(Constants.DateFormat.NO_TZ))
                            prevCatDTO.setUpdatedBy(userService.getCurrentUserName())
                            prevCatDTO.setUpdatedDate(new Date().format(Constants.DateFormat.NO_TZ))
                            prevCategoriesAllAlert.add(prevCatDTO)

                        }
                        break
                    case 'I':
                        List<Map> catSubcatList = dataObjectService.getBulkCategoriesListForCase(bulkCatListKey)
                        Map existingCategory = catSubcatList.find {
                            it.catName == catDTO.catName && it.factGrpId == catDTO.factGrpId
                        }

                        if (!existingCategory) {
                            CategoryDTO newInsertCatDto = catDTO.clone()
                            newInsertCatDto = addAlertDetailsTocategory(newInsertCatDto, type, it, null,isCaseSeries)
                            currentCategoriesAllAlert.add(newInsertCatDto)
                            CategoryDTO prevInsertCatDTO = newInsertCatDto.clone()
                            prevInsertCatDTO.setDmlType('D')
                            prevCategoriesAllAlert.add(prevInsertCatDTO)
                        } else {

                            if (catDTO.subCatName) {
                                Map existingSubCategory = catSubcatList.find {
                                    it.catName == catDTO.catName && it.subCatName == catDTO.subCatName
                                }
                                if (!existingSubCategory) {
                                    CategoryDTO newInsertCatDto = catDTO.clone()
                                    newInsertCatDto = addAlertDetailsTocategory(newInsertCatDto, type, it, null, isCaseSeries)
                                    currentCategoriesAllAlert.add(newInsertCatDto)
                                    CategoryDTO prevInsertCatDTO = newInsertCatDto.clone()
                                    prevInsertCatDTO.setDmlType('D')
                                    prevCategoriesAllAlert.add(prevInsertCatDTO)
                                }
                            }
                        }
                        break
                    case 'U':
                        List<Map> catSubcatList = dataObjectService.getBulkCategoriesListForCase(bulkCatListKey)
                        Map existingCategory = catSubcatList.find {
                            it.catName == catDTO.catName && it.factGrpId == catDTO.factGrpId
                        }
                        if (!existingCategory) {
                            CategoryDTO newInsertCatDto = catDTO.clone()
                            newInsertCatDto = addAlertDetailsTocategory(newInsertCatDto, type, it, null, isCaseSeries)
                            currentCategoriesAllAlert.add(newInsertCatDto)
                            CategoryDTO prevInsertCatDTO = newInsertCatDto.clone()
                            prevInsertCatDTO.setDmlType('D')
                            prevCategoriesAllAlert.add(prevInsertCatDTO)
                        } else {
                            Map existingSubCategory;
                            if (catDTO.subCatName) {
                                existingSubCategory = catSubcatList.find {
                                    it.catName == catDTO.catName && it.subCatName == catDTO.subCatName
                                }
                            } else {
                                existingSubCategory = catSubcatList.find {
                                    it.catName == catDTO.catName && it.catName == catDTO.catName
                                }
                            }
                            if (!existingSubCategory) {
                                CategoryDTO newInsertCatDto = catDTO.clone()
                                newInsertCatDto = addAlertDetailsTocategory(newInsertCatDto, type, it, null,isCaseSeries)
                                currentCategoriesAllAlert.add(newInsertCatDto)
                                CategoryDTO prevInsertCatDTO = newInsertCatDto.clone()
                                prevInsertCatDTO.setDmlType('D')
                                prevCategoriesAllAlert.add(prevInsertCatDTO)
                            } else {
                                CategoryDTO newInsertCatDto = catDTO.clone()
                                newInsertCatDto = addAlertDetailsTocategory(newInsertCatDto, type, it, existingSubCategory,isCaseSeries)
                                currentCategoriesAllAlert.add(newInsertCatDto)
                                CategoryDTO prevInsertCatDTO = newInsertCatDto.clone()
                                prevInsertCatDTO.setDmlType('U')
                                prevCategoriesAllAlert.add(prevInsertCatDTO)
                            }
                        }
                        break
                }
            }
        }


        Long endTime3 = System.currentTimeMillis()
        log.info('Time taken to create DTO for each case: '+ (endTime3 - startTime3))


        def result = [:]
        Map categoriesMap = [current: currentCategoriesAllAlert, previous: prevCategoriesAllAlert]

        Long startTime4 = System.currentTimeMillis()
        if (currentCategoriesAllAlert) {

            List<CategoryDTO> alertLevel = currentCategoriesAllAlert.findAll {
                it.getFactGrpId() == Holders.config.category.singleCase.alertSpecific
            }
            if (alertLevel) {
                notify 'categories.populate.version.published', [categories: alertLevel]
            }
            def url = Holders.config.pvcc.api.url
            def path = Holders.config.pvcc.api.path.save
            def query = JsonOutput.toJson(categoriesMap)
            result = reportIntegrationService.postData(url, path, query, Method.POST)
        }

        Long endTime4 = System.currentTimeMillis()
        log.info('Time taken in post category data call :'+ (endTime4 - startTime4) )
        //Added for PVS-56930
        if (result?.result?.status == "Success" && alertList) {
            alertList.each {
                it?.lastUpdated = new Date()
                it?.save(flush: true)
            }
        }

        result

    }

    private CategoryDTO addAlertDetailsTocategory(CategoryDTO newCatDTO, String type, def alert, Map catMap,boolean isCaseSeries) {

        if (type != 'Literature') {
            newCatDTO.setUdNumber1(alert.executedAlertConfiguration.id)
            newCatDTO.setAlertId(alert.id as String)
            newCatDTO.setModule(Constants.PVS_CASE_SERIES_OWNER)
            newCatDTO.setDataSource(alert.alertConfiguration.selectedDatasource.toUpperCase())
            if(isCaseSeries){
                newCatDTO.setIsAdhoc(false)
            }else {
                newCatDTO.setIsAdhoc(alert.getClass().simpleName == 'AggregateOnDemandAlert' ? true : alert.adhocRun)
            }
        } else {
            newCatDTO.setUdNumber1(alert.exLitSearchConfig.id)
            newCatDTO.setAlertId(alert.id as String)
            newCatDTO.setModule(Constants.PVS_CASE_SERIES_OWNER)
            newCatDTO.setDataSource("")
            newCatDTO.setIsAdhoc(true)

        }
        String dmlType = newCatDTO.dmlType
        if (dmlType == 'D') {
            newCatDTO.setPriority(catMap.priority)
            newCatDTO.setCreatedBy(catMap.createdBy)
            newCatDTO.setCreatedDate(catMap.createdDate)
        } else if (dmlType == 'I') {
            newCatDTO.setPriority(newCatDTO.priority)
            newCatDTO.setCreatedBy(userService.getCurrentUserName())
            newCatDTO.setCreatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
        } else { //update
            newCatDTO.setPriority(catMap.priority)
            newCatDTO.setIsAutoTagged(catMap.isAutoTagged)
            newCatDTO.setIsRetained(catMap.isRetained)
        }
        newCatDTO.setUpdatedBy(userService.getCurrentUserName())
        newCatDTO.setUpdatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
        switch (type) {
            case "Quantitative":
                if (newCatDTO.alertLevel) {
                    newCatDTO.setFactGrpId(Holders.config.category.aggregateCase.alertSpecific)
                    newCatDTO.setFactGrpCol2(alert.id as String)
                    newCatDTO.setFactGrpCol3(alert.executedAlertConfiguration.id as String)
                } else {
                    newCatDTO.setFactGrpId(Holders.config.category.aggregateCase.global)
                    newCatDTO.setFactGrpCol2(alert.productId as String)
                    newCatDTO.setFactGrpCol3(alert.ptCode as String)
                    newCatDTO.setFactGrpCol4(alert.smqCode ?: "null")
                    newCatDTO.setFactGrpCol5(alert.prodHierarchyId as String)
                    newCatDTO.setFactGrpCol6(alert.eventHierarchyId as String)
                }
                break
            case "Qualitative":
                if (newCatDTO.alertLevel) {
                    newCatDTO.setFactGrpId(Holders.config.category.singleCase.alertSpecific)
                    newCatDTO.setFactGrpCol2(alert.executedAlertConfiguration.pvrCaseSeriesId as String)
                    newCatDTO.setFactGrpCol3(alert.caseId as String)
                    newCatDTO.setFactGrpCol4(alert.caseVersion as String)
                } else {
                    newCatDTO.setFactGrpId(Holders.config.category.singleCase.global)
                    newCatDTO.setFactGrpCol2(alert.caseId as String)
                    newCatDTO.setFactGrpCol3(alert.caseVersion as String)
                }
                break
            case "Literature":
                if (newCatDTO.alertLevel) {
                    newCatDTO.setFactGrpId(Holders.config.category.literature.alertSpecific)
                    newCatDTO.setFactGrpCol2(alert.id as String)
                } else {
                    newCatDTO.setFactGrpId(Holders.config.category.literature.global)
                    newCatDTO.setFactGrpCol2(alert.articleId as String)
                }
                break

        }
        newCatDTO
    }



    private List<CategoryDTO> buildAllCommonCategories(List<CategoryDTO> previous, List<CategoryDTO> current){

        List<CategoryDTO> finalCategories = new LinkedList<>();
        CategoryDTO category
        Integer count = 0
        Long loggedUserId = userService.getCurrentUserId()

        // insert and update category
        for(CategoryDTO currentRow: current){
            Boolean insert = true
            Boolean update = false
            Boolean dateUpdate = true

            for(CategoryDTO previousRow: previous){
                if(currentRow.catId == previousRow.catId && currentRow.subCatId == previousRow.subCatId &&
                        currentRow.catName == previousRow.catName && currentRow.subCatName == previousRow.subCatName &&
                        currentRow.alertLevel == previousRow.alertLevel){
                    insert = false
                    //handled existing private category for new User
                    if (currentRow.privateUserId != null && (currentRow.privateUserId != previousRow.factGrpCol10)) {
                        break
                    }
                    if(currentRow.privateUserId != previousRow.privateUserId ) {//removed priority ==>|| currentRow.priority != previousRow.priority
                        update = true
                        currentRow.setIsAutoTagged(previousRow.getIsAutoTagged())
                        currentRow.setIsRetained(previousRow.getIsRetained())
                        currentRow.setUdNumber1(previousRow.getUdNumber1())
                        if(previousRow.priority > 900){
                            dateUpdate = false
                            currentRow.setUpdatedBy(previousRow.getUpdatedBy())
                            currentRow.setUpdatedDate(previousRow.getUpdatedDate())
                        }
                    }
                    break
                }
            }
            if(insert || update) {
                finalCategories.add(buildCommonCategory(currentRow, insert ? "I" : "U", dateUpdate))
                count++
            }
        }

        // delete category
        for(CategoryDTO previousRow: previous){
            Boolean delete = true
            Boolean update = false
            for(CategoryDTO currentRow: current){
                if(currentRow.catId == previousRow.catId && currentRow.subCatId == previousRow.subCatId &&
                        currentRow.catName == previousRow.catName && currentRow.subCatName == previousRow.subCatName &&
                        currentRow.alertLevel == previousRow.alertLevel){
                    delete = false
                    break
                }
            }
            if(previousRow.privateUserId >0 && previousRow.privateUserId != loggedUserId){
                delete = false
                update = true
            }
            if(delete){
                finalCategories.add(buildCommonCategory(previousRow, "D", true))
            }
            else if(update){
                finalCategories.add(buildCommonCategory(previousRow, "U", true))
                count++
            }
        }
        finalCategories
    }


    private CategoryDTO buildCommonCategory(CategoryDTO categoryDTO, String ops, Boolean dateUpdate){
        categoryDTO.setDmlType(ops)

        String userName = userService.getCurrentUserName()
        if(ops == "I") {
            categoryDTO.setCreatedBy(userName)
            categoryDTO.setCreatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
        }
        if(dateUpdate) {
            categoryDTO.setUpdatedBy(userName)
            categoryDTO.setUpdatedDate(new Date().format("yyyy-MM-dd HH:mm:ss"))
        }
        categoryDTO.setFactGrpCol1(Holders.config.categories.tenantId)
        categoryDTO
    }

    def getQuantitativeAlertCategories(String domain, Long alertId, Boolean isArchived, User currentUser) {
        def aca
        if (isArchived) {
            aca = ArchivedAggregateCaseAlert.findById(alertId)
        } else {
            aca = AggregateCaseAlert.findById(alertId)
        }
        Long productId = aca.productId
        Long ptCode = aca.ptCode
        String smq = aca.smqCode
        Integer prodHierarchyId = aca.prodHierarchyId ?:(-1)
        Integer eventHierarchyId = aca.eventHierarchyId?:(-1)
        Long execConfigId = aca.executedAlertConfiguration.id

        String tenantId = Holders.config.categories.tenantId
        String alertLevelParams = tenantId + ","+alertId + ","+execConfigId
        String globalLevelParams = tenantId + ","+productId + "," + ptCode + "," + smq  + "," + prodHierarchyId + "," + eventHierarchyId

        Map inputMap = [alertLevelParams: alertLevelParams, globalLevelParams: globalLevelParams,
                        alertLevelId: Holders.config.category.aggregateCase.alertSpecific,
                        globalLevelId: Holders.config.category.aggregateCase.global]
        getCategories(inputMap, currentUser)
    }

}
