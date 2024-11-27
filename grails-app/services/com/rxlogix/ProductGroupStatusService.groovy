package com.rxlogix

import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.commandObjects.DictionaryGroupCO
import com.rxlogix.commandObjects.DictionaryGroupStatusCO
import com.rxlogix.commandObjects.ProductCO
import com.rxlogix.commandObjects.ProductGroupCO
import com.rxlogix.pvdictionary.DictionaryGroupCmd
import com.rxlogix.pvdictionary.exception.DictionaryGroupException
import com.rxlogix.signal.BatchLotData
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.signal.ProductGroupData
import com.rxlogix.signal.ProductGroupStatus
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.transform.Transformers
import org.hibernate.type.BooleanType
import org.hibernate.type.DoubleType
import org.hibernate.type.IntegerType
import org.hibernate.type.ListType
import org.hibernate.type.LongType
import org.hibernate.type.StringType
import org.springframework.http.MediaType
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

import java.lang.reflect.Field
import java.text.DateFormat
import java.text.SimpleDateFormat

class ProductGroupStatusService {

    def userService;

    def sessionFactory

    def springSecurityService

    def dynamicReportService

    static transactional = false
    def dataSource
    def signalDataSourceService
    def emailNotificationService
    def dictionaryGroupService
    def cacheService
    def restAPIService

    def saveProductGroupStatusAndAudit(DictionaryGroupStatusCO productGroupStatusCO, String username, String productGroupDataString) {
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- In")
        User user = userService.getUserByUsername(username);
        productGroupStatusCO.setApiUsername(user.getFullName());
        ProductGroupStatus productGroupStatus = saveProductGroupStatusAndData(productGroupStatusCO, username, null)
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- saveProductGroupStatusAndData called")
        saveAuditTrail(productGroupStatusCO, productGroupStatus.getId(), productGroupStatus.getUploadedAt(), productGroupDataString, user, productGroupStatus.getValidRecordCount())
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- saveProductGroupStatusAndData saved")
        productGroupStatus
    }

    def saveProductGroupStatusAndAudit(DictionaryGroupStatusCO productGroupStatusCO, String username, Long productGroupStatusId) {
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- In")
        User user = userService.getUserByUsername(username);
        productGroupStatusCO.setApiUsername(user.getFullName());

        String defaultUserGroup = fetUserDetailsFromPVR("UserGroup", "All Users", null);
        if(!CollectionUtils.isEmpty(productGroupStatusCO.getProductGroupList())) {
            List<DictionaryGroupCO> dictionaryGroups = new ArrayList<>();
            DictionaryGroupCO dictionaryGroupCO = null
            for(ProductGroupCO productGroupCO : productGroupStatusCO.getProductGroupList()) {
                String sharedWith=null;
                if(StringUtils.isEmpty(productGroupCO.getValidationError())) {
                    if(productGroupCO.getSharedWith().get("users")!=null && productGroupCO.getSharedWith().get("users").size()>0 ) {
                        for (String usr: productGroupCO.getSharedWith().get("users")) {
                            if(!StringUtils.isEmpty(usr)) {
                                String userResponseId = fetUserDetailsFromPVR("User", usr, productGroupCO);
                                sharedWith=(sharedWith==null)?userResponseId:(sharedWith+";"+userResponseId)
                            }
                        }
                    }
                    if(productGroupCO.getSharedWith().get("groups")!=null && productGroupCO.getSharedWith().get("groups").size() > 0 ) {
                        for (String usr: productGroupCO.getSharedWith().get("groups")) {
                            String userResponseId = fetUserDetailsFromPVR("UserGroup", usr, productGroupCO);
                            sharedWith=(sharedWith==null)?userResponseId:(sharedWith+";"+userResponseId)
                        }
                    }
                    if( sharedWith == null ) {
                        sharedWith=defaultUserGroup
                    }
                    String dataString = getProductDataString(productGroupCO)
                    dictionaryGroupCO = new DictionaryGroupCO();
                    dictionaryGroupCO.id = productGroupCO.id
                    dictionaryGroupCO.uniqueIdentifier = productGroupStatusCO.uniqueIdentifier
                    dictionaryGroupCO.groupName = productGroupCO.name
                    dictionaryGroupCO.groupOldName = productGroupCO.oldName
                    dictionaryGroupCO.type = productGroupCO.type
                    dictionaryGroupCO.description = productGroupCO.description
                    dictionaryGroupCO.copyGroups = productGroupCO.copyGroups
                    dictionaryGroupCO.sharedWith = sharedWith

                    if(StringUtils.isEmpty(productGroupCO.owner)) {
                        productGroupCO.owner=user.username
                    }
                    dictionaryGroupCO.owner=productGroupCO.owner
                    dictionaryGroupCO.tenantId = productGroupCO.tenantId
                    dictionaryGroupCO.includeSources= (productGroupCO.includeSources==null?"":productGroupCO.includeSources.join(","))
                    dictionaryGroupCO.includeSourcesToAdd = productGroupCO.includeSourcesToAdd
                    dictionaryGroupCO.data= dataString
                    dictionaryGroupCO.validationError=productGroupCO.validationError
                    dictionaryGroups.add(dictionaryGroupCO);
                }
            }
            productGroupStatusCO.setDictionaryGroups(dictionaryGroups)
        }
        ProductGroupStatus productGroupStatus = saveProductGroupStatusAndData(productGroupStatusCO, username, productGroupStatusId)
        String errorString = getDistinctErrorMessage(productGroupStatus, productGroupStatusCO.getProductGroupList())
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- sendMailOnBatchLotUpdate called")
        sendMailOnBatchLotUpdate(productGroupStatus, user, errorString)
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- sendMailOnBatchLotUpdate sent")
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- saveProductGroupStatusAndData called")
        saveAuditTrail(productGroupStatusCO, productGroupStatus.getId(), productGroupStatus.getUploadedAt(),
                getProductGroupString(productGroupStatusCO), user, productGroupStatus.getValidRecordCount())
        log.info("ProductGroupStatusService saveProductGroupStatusAndAudit- saveProductGroupStatusAndData saved")
        productGroupStatus
    }
    String fetUserDetailsFromPVR(String userType, String usrGrpName, ProductGroupCO productGroupCO) {
        String userTypeWithId = null;
        String error = null;
        try {
            Map response = dictionaryGroupService.fetchUserDetail(userType+"_:_"+usrGrpName)
            if( response!=null && response.size()>0 && response.get("id")!=null
                    && String.valueOf(response.get("id")).startsWith(userType+"_") ) {
                userTypeWithId = response.id
            } else {
                error = userType+" - "+usrGrpName+" does not received from PV Reports"
                log.error(error)
            }
        } catch(Exception ex) {
            error = "error occured while fetching " +userType+" - "+usrGrpName+" from PV Reports"
           log.error(error+":"+ex.toString())
           ex.printStackTrace();
        }
        if(productGroupCO!=null && userTypeWithId==null) {
            productGroupCO.setValidationError(productGroupCO.getValidationError()==null?error+"\n":productGroupCO.getValidationError()+error+"\n")
        }
        userTypeWithId
    }
    String getProductDataString(ProductGroupCO productGroupCO) {
        StringBuffer productDataString = new StringBuffer();
        Map<String,HashMap<String,String>> dataHm = new HashMap<String,HashMap<String,String>>();
        if(productGroupCO.getProducts()!=null && productGroupCO.getProducts().size()>0) {
            String source = null;
            String hierarchy = null;
            String prodString = null;
            for (ProductCO productGroupMap: productGroupCO.getProducts()) {
                source = productGroupMap.source;
                hierarchy = productGroupMap.hierarchy;
                prodString = "{\"id\":"+productGroupMap.productId+",\"name\":\""+productGroupMap.productName+"\"}";
                if(dataHm.get(productGroupMap.source)==null) {
                    dataHm.put(productGroupMap.source,new HashMap<String,String>())
                    dataHm.get(productGroupMap.source).put(hierarchy, prodString)
                } else {
                    if(dataHm.get(source).get(hierarchy)==null) {
                        dataHm.get(source).put(hierarchy,prodString)
                    } else {
                        dataHm.get(source).put(hierarchy,dataHm.get(source).get(hierarchy)+","+prodString)
                    }
                }
            }
        }
        if(dataHm.keySet()!=null && dataHm.keySet().size()>0) {
            productGroupCO.includeSourcesToAdd = addRequiredEmptyDatasourceAndProduct(dataHm);
            productDataString.append("{")
            for(String dataKey: dataHm.keySet()) {
                if(!productDataString.toString().equals("{")) {
                    productDataString.append(",")
                }
                productDataString.append("\""+dataKey+"\":");
                productDataString.append("{");
                int i =0
                for(String prodKey: dataHm.get(dataKey).keySet()) {
                    if(i>0) {
                        productDataString.append(",")
                    }
                    productDataString.append("\""+prodKey+"\":["+dataHm.get(dataKey).get(prodKey)+"]")
                    i=i+1
                }
                productDataString.append("}");
            }
            productDataString.append("}")
        }
        log.info("productDataString:"+productDataString)
        return productDataString;
    }

    Set addRequiredEmptyDatasourceAndProduct(Map<String,HashMap<String,String>> dataHm) {
        Set includeSourcesToAdd = []
        List requiredDS = Holders.config.pvs.productgroup.product.databases
        List requiredProdType = Holders.config.pvs.productgroup.product.types
        for(String dsName: requiredDS) {
            if(dataHm.get(dsName)==null) {
                Map map =[:];
                for(String prodType: requiredProdType) {
                    map.put(prodType,"")
                }
                dataHm.put(dsName,map)
            } else {
                for(String prodType: requiredProdType) {
                    if(dataHm.get(dsName).get(prodType)==null) {
                        dataHm.get(dsName).put(prodType,"")
                    }
                }
                includeSourcesToAdd.add(dsName)
            }
        }
        includeSourcesToAdd
    }

    def getProductGroupString(DictionaryGroupStatusCO productGroupStatusCO) {
        String productGroupString = null;
        productGroupStatusCO.getProductGroups().each {
            productGroupString = (productGroupString==null?"":",")+
                    "{\"id\":\""+it.id+"\",\"productGroupStatusId\":\""+it.productGroupStatusId+"\","+"\"version\":"+it.version+
                    ",\"uniqueIdentifier\":"+it.uniqueIdentifier+",\"groupName\":"+it.groupName+",\"type\":"+it.type+"\"description\":"+it.description+
                    ",\"copyGroups\":"+it.copyGroups+",\"sharedWith\":"+it.sharedWith+",\"owner\":"+it.owner+"\"tenantId\":"+it.tenantId+
                    ",\"includeSources\":"+it.includeSources+",\"data\":"+it.data+",\"validationError\":"+it.validationError+",\"status\":"+it.status+"\"}"
        }
        "["+productGroupString+"]"
    }

    def saveProductGroupStatusAndData(DictionaryGroupStatusCO productGroupStatusCO, String username, Long productGroupStatusId) {
        List <DictionaryGroupCO> dictionaryGroupCOs = productGroupStatusCO.getDictionaryGroups();
        List <ProductGroupCO> productGroups = productGroupStatusCO.getProductGroupList();
        ProductGroupStatus productGroupStatus = null
        int validDataCount = 0
        int invalidDataCount = 0

        if(dictionaryGroupCOs!=null && dictionaryGroupCOs.size()>0) {
            productGroupStatusCO = saveToMart(productGroupStatusCO);
        }
        String validationError = null
        productGroups.each {
            validationError = validateProductGroupData(it)
            it.setValidationError(validationError);
            if(it.getValidationError()!=null  && it.getValidationError().size()>0
                && !(it.getValidationError().equalsIgnoreCase("successfully created")
                    || it.getValidationError().equalsIgnoreCase("successfully updated") )  ){
                invalidDataCount = invalidDataCount+1
            } else {
                validDataCount = validDataCount+1
            }
            it.setUniqueIdentifier(productGroupStatusCO.getUniqueIdentifier())
            it.setProductGroupStatusId(productGroupStatusCO.getId())
            it.setValidationError(validationError)
        }

        ProductGroupStatus.withTransaction {

            if(productGroupStatusId!=null) {
                productGroupStatus = getProductGroupStatusById(productGroupStatusId);
                productGroupStatus.setValidRecordCount(validDataCount)
                productGroupStatus.setInvalidRecordCount(invalidDataCount)
                productGroupStatus.setIsApiProcessed(false)
            } else {
                productGroupStatus = new ProductGroupStatus(productGroupStatusCO.getUniqueIdentifier(),
                        productGroupStatusCO.getCount(),
                        validDataCount, invalidDataCount, new Date(), productGroupStatusCO.getApiUsername(), true, null)
            }
            if(productGroupStatusCO.pvrHttpError!=null) {
                productGroupStatus?.pvrError = productGroupStatusCO.pvrHttpError
            }
            log.info("productGroupStatus Unique Identifier:"+productGroupStatus.getUniqueIdentifier())
            productGroupStatus.save(validate: false)
            Session session = sessionFactory.currentSession
            session.flush()
            session.clear()
        }
        productGroups.each {
            it.setProductGroupStatusId(productGroupStatus.getId())
        }
        ProductGroupData productGroupData = null
        def batch = []
        for(def clientData : productGroups) {
            batch += getProductGroupData(clientData, productGroupStatusCO.getUniqueIdentifier())
            if (batch.size() > Holders.config.signal.batch.size) {
                ProductGroupData.withTransaction {
                    Session session = sessionFactory.currentSession
                    for (def cd in batch) {
                        cd.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
        }

        if (batch) {
            try {

                int tmpSaveData = 0;
                ProductGroupData.withTransaction {
                    Session session = sessionFactory.currentSession
                    for (def cd in batch) {
                        cd.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                }
            } catch (Throwable th ) {
                th.printStackTrace()
            }
        }
        productGroupStatus
    }

    def saveToMart(DictionaryGroupStatusCO productGroupStatusCO) throws DictionaryGroupException {
        List <DictionaryGroupCO> dgco = productGroupStatusCO.getDictionaryGroups();
        List <ProductGroupCO> productGroups = productGroupStatusCO.getProductGroupList();
        String url = Holders.config.app.dictionary.base.url
        String path = Holders.config.pvreports.api.productGroups.save.url
        def valuesMap = [:]
        def dictionaryGroupCmdList = []
        for(int i=0; i<dgco.size(); i++) {
            if(StringUtils.isEmpty(dgco.get(i).validationError)) {
                Map dictionaryGroupCmd = [:];
                dictionaryGroupCmd.put("id",dgco.get(i).id)
                dictionaryGroupCmd.put("newName",dgco.get(i).getGroupName())
                dictionaryGroupCmd.put("oldName",dgco.get(i).getGroupOldName())
                dictionaryGroupCmd.put("type",dgco.get(i).getType())
                dictionaryGroupCmd.put("description",dgco.get(i).getDescription())
                dictionaryGroupCmd.put("owner",dgco.get(i).getOwner())
                dictionaryGroupCmd.put("tenantId",dgco.get(i).getTenantId())
                dictionaryGroupCmd.put("dataSources",(StringUtils.isEmpty(dgco.get(i).getIncludeSources())?[]:dgco.get(i).getIncludeSources().split(",")))
                dictionaryGroupCmd.put("data",dgco.get(i).getData())
                dictionaryGroupCmd.put("sharedWith",dgco.get(i).getSharedWith())
                dictionaryGroupCmd.put("copyGroups",dgco.get(i).getCopyGroups())
                dictionaryGroupCmdList.add(dictionaryGroupCmd);
            }
        }
        if(!CollectionUtils.isEmpty(dictionaryGroupCmdList)) {
            valuesMap["productGroups"] = dictionaryGroupCmdList
            Map data = [productGroups: dictionaryGroupCmdList ]
            try {
                log.info("ProductGroupStatusService -> saveToMart - restAPIService called with url:\""
                        +url+"\",path:\""+path+"\" and data:\"\n"+new JsonBuilder(data).toPrettyString() +"\n\"")
                Map response = restAPIService.post(url, path, data)
                log.info("ProductGroupStatusService -> saveToMart - restAPIService response received with respose:\""+response+"\"")

                if(response!=null && response.data !=null && response.data.size()>0) {
                    for(String responseKey : response.data.keySet()) {
                        for(int i=0; i < productGroups.size(); i++) {
                            if (StringUtils.isEmpty(productGroups.get(i).validationError)) {
                                if(responseKey.equals(productGroups.get(i).oldName)) {
                                    productGroups.get(i).validationError=response.data.get(responseKey)
                                }
                            }
                        }
                    }
                }
            } catch(Exception ex) {
                productGroupStatusCO.pvrHttpError = "An Error occured while calling api/server";
                for(int i=0; i < productGroups.size(); i++) {
                    log.info("PV Report error:"+ex.toString())
                    productGroups.get(i).validationError="PV Report error: An Error occured while calling api/server"
                }
                ex.printStackTrace();
            }

        } else {
            log.info("In Bulk product Groups save - saveToMart - No valid Product Groups found...")
        }
        productGroupStatusCO
    }

    def saveDictionaryGroup(DictionaryGroupCO dictionaryGroupCO) {
        log.info("before dictionaryGroupService.save(dictionaryGroupCO.getDictionaryGroupCmd())....")
        try {
            dictionaryGroupService.save(dictionaryGroupCO.getDictionaryGroupCmd());
        } catch(Exception ex) {
            ex.printStackTrace();
            dictionaryGroupCO.setValidationError(dictionaryGroupCO.getValidationError()==null?ex.toString():
                    (dictionaryGroupCO.getValidationError()+"\n"+ex.toString()))
        }
        log.info("end dictionaryGroupService.save(dictionaryGroupCO.getDictionaryGroupCmd())....")

    }

    def saveAuditTrail(DictionaryGroupStatusCO productGroupStatusCO, Long productGroupStatusId, Date uploadedAt, String productGroupDataString, User user, Long validRecordCount) {
        try {
            Session session = sessionFactory.currentSession
            AuditTrail auditTrail = new AuditTrail()
            auditTrail.category = AuditTrail.Category.INSERT.toString()
            auditTrail.applicationName = "PV Signal"
            auditTrail.entityId = productGroupStatusId
            auditTrail.entityName = "Product Group status"
            auditTrail.entityValue = "{ Unique Identifier:"+productGroupStatusCO.getUniqueIdentifier()+
                    (validRecordCount==null?"":", processed:"+validRecordCount) +
                    ", total count:"+productGroupStatusCO.getCount()+
                    ",processedAt:" + DateUtil.toDateStringPattern(uploadedAt,DateUtil.DATEPICKER_FORMAT_AM_PM_3)+"}"
            auditTrail.moduleName = "Product Group status"
            auditTrail.username = user?.getUsername() ?: "System"
            auditTrail.fullname = user?.getFullName() ?: "System"
            auditTrail.save()

            AuditTrailChild auditTrailChild = null
            if(productGroupStatusCO.getProductGroupList() !=null && productGroupStatusCO.getProductGroupList().size()>0) {
                productGroupStatusCO.getProductGroupList().each {
                    auditTrailChild = new AuditTrailChild()
                    auditTrailChild.propertyName="Product Group Info"
                    auditTrailChild.newValue = objectToString(it)
                    auditTrailChild.auditTrail = auditTrail
                    auditTrailChild.save()
                }
            } else if(productGroupDataString!=null) {
                auditTrailChild = new AuditTrailChild()
                auditTrailChild.propertyName="Product Group Info"
                auditTrailChild.newValue = productGroupDataString
                auditTrailChild.auditTrail = auditTrail
                auditTrailChild.save()
            }
            session.flush()
            session.clear()
        } catch(ValidationException ve) {
            log.error(ve.toString())
        }
    }
    def sendMailOnBatchLotUpdate(ProductGroupStatus productGroupStatus, User user, String errorString) {
        if(!StringUtils.isEmpty(Holders.config.signal.psur.api.upload.toAddresses)) {
            List<String> toAddresses = Holders.config.signal.psur.api.upload.toAddresses.split(",")
            List uploads = null
            if(productGroupStatus.getUniqueIdentifier()!=null) {
                List summaryList = getProductGroupSummaryData(productGroupStatus.getId())
                uploads = fetchAttachments(productGroupStatus.getUniqueIdentifier(),getProductGroupsDatasByLotId(productGroupStatus.getId()),summaryList );
            }
            emailNotificationService.mailApiUploadStatus(productGroupStatus, user.getEmail(), toAddresses, uploads , errorString)
        } else {
            log.error("API Mail can not be sent because signal.psur.api.upload.toAddresses properting is missing...")
        }
    }

    File renderExcelReportFile(String name, List data, List summaryList, Map columns) {
        Map reportParams = new LinkedHashMap()
        reportParams.outputFormat = "XLSX"
        reportParams.name = name
        reportParams.columns = columns
        reportParams.summaryList = summaryList
        dynamicReportService.createProductGroupBatchReport(new JRMapCollectionDataSource(data), reportParams)
    }


    def fetchAttachments(String id, List<Map> dataMap,List summaryList ) {
        List uploads = []
        uploads << [name:id+".XLSX" , file:renderExcelReportFile(id, dataMap, summaryList, getProductGroupDataColumnNameMap())]
        uploads
    }

    ProductGroupStatus getProductGroupStatusById(Long productGroupStatusId) {
        ProductGroupStatus productGroupStatus
        ProductGroupStatus.withTransaction {
            List<ProductGroupStatus> productGroupStatuses = ProductGroupStatus.createCriteria().list { eq("id", productGroupStatusId) };
            if(productGroupStatuses!=null && productGroupStatuses.size()>0) {
                productGroupStatus = productGroupStatuses.get(0)
            }
        }
        productGroupStatus
    }

    def validateProductGroupData(ProductGroupCO dictionaryGroupCO) {
        String errorString = "";
        if(dictionaryGroupCO.getName()==null  ) {
            errorString = "Group Name must not be empty ";
        } else if(dictionaryGroupCO.getOldName()==null  ) {
                errorString="Group Old Name must not be empty ";
        } else if(!StringUtils.isEmpty(dictionaryGroupCO.getValidationError())) {
            errorString=dictionaryGroupCO.getValidationError()
        }
        dictionaryGroupCO.setValidationError(errorString)
        return errorString;
    }

    def objectToString(Object it) {
        "{\"id\":\""+it.id+"\",\"productGroupStatusId\":\""+it.productGroupStatusId+"\","+
                ",\"uniqueIdentifier\":"+it.uniqueIdentifier+",\"name\":"+it.name+",\"type\":"+it.type+"\"description\":"+it.description+
                ",\"copyGroups\":"+it.copyGroups+",\"sharedWith\":"+it.sharedWith+",\"owner\":"+it.owner+"\"tenantId\":"+it.tenantId+
                ",\"includeSources\":"+it.includeSources+",\"products\":"+it.products+",\"validationError\":"+it.validationError+"\"}"
    }

    Map getProductGroupStatusList(DataTableSearchRequest searchRequest, params) {
        User user = null
        List<Long> groupIds = null
        String timeZone = null
        List<ProductGroupStatus> productGroupsStatuses = getProductGroupsStatus(searchRequest, params, false);
        List<Map> batchLotStatusMap = createBatchLotStatusDTO(productGroupsStatuses, timeZone);
        Integer filteredCount = getBatchLotStatusCounts(user, searchRequest, searchRequest?.searchParam?.search?.getValue()?.toLowerCase(), params, groupIds, false)
        int totalCount
        ProductGroupStatus.withTransaction {
            totalCount=ProductGroupStatus.createCriteria().get { projections { count "id" } } as int
        }
        if(!CollectionUtils.isEmpty(batchLotStatusMap)) {
            batchLotStatusMap.each {
                bls ->
                Set<String> validationMessages = ProductGroupData.createCriteria().list {projections {property ('validationError')}
                    eq("productGroupStatusId", bls.get("id"))
                }
                if(!CollectionUtils.isEmpty(validationMessages)) {
                    validationMessages.removeAll([null])
                    Set validationMessagesSet = []
                    validationMessages.join("\n").split("\n").each { ln ->
                        if(ln!=null && !ln.equalsIgnoreCase("successfully created")
                                    && !ln.equalsIgnoreCase("successfully updated") ) {
                            validationMessagesSet.add(ln)
                        }
                    }
                    validationMessagesSet.removeAll([null])
                    bls.put("info",validationMessagesSet.join("\n"))
                } else {
                    bls.put("info","No records are processed!")
                }
            }
        }

        [aaData: batchLotStatusMap, recordsTotal: totalCount, recordsFiltered: filteredCount]
    }

    List getProductGroupsStatus(DataTableSearchRequest searchRequest, params, boolean isExport) {
        String orderByProperty = searchRequest?.orderBy() == 'id' ? Constants.Commons.BULK_API_BATCH_UPLOADED_DATE : searchRequest?.orderBy()
        String orderDirection = searchRequest?.orderBy() == 'id' ? "desc" : searchRequest?.searchParam?.orderDir()
        String searchAlertQuery = prepareValidatedBatchLotStatusHQL(searchRequest, params, null, null, orderByProperty, orderDirection)
        List<ProductGroupStatus> productGroupStatusList
        ProductGroupStatus.withTransaction {
            if(isExport==true) {
                productGroupStatusList= ProductGroupStatus.executeQuery(searchAlertQuery)
            } else {
                productGroupStatusList= ProductGroupStatus.executeQuery(searchAlertQuery, [offset: searchRequest?.searchParam?.start, max: searchRequest?.pageSize()])
            }
        }
        return productGroupStatusList;
    }

    List getProductGroupsStatusBySqlQry(DataTableSearchRequest searchRequest, params, boolean isExport) {
        String orderByProperty = searchRequest?.orderBy() == 'id' ? Constants.Commons.BULK_API_BATCH_UPLOADED_DATE : searchRequest?.orderBy()
        String orderDirection = searchRequest?.orderBy() == 'id' ? "desc" : searchRequest?.searchParam?.orderDir()
        String searchAlertQuery = prepareValidatedBatchLotStatusSQLQuery(searchRequest, params, null, null, orderByProperty, orderDirection)
        List<ProductGroupStatus> productGroupStatusList = getResultList(ProductGroupStatus.class, searchAlertQuery)
        return productGroupStatusList;
    }

    List<Map> getProductGroupsDatasByLotId(Long productGroupsId) {
        List<ProductGroupData> productGroupDataList = ProductGroupData.createCriteria().list { eq("productGroupStatusId", productGroupsId) };
        getProductGroupExcelData(productGroupDataList, getProductGroupDataColumnNameMap());
    }

    List<Map> getProductGroupsMap(List<ProductGroupStatus> productGroupStatusList, Map columnsMap) {
        List<Map> mapList = []
        String stringValue = ""
        List<ProductGroupData> productGroupDatas = null;
        Set productGroupDataValidationError = null
        productGroupStatusList.eachWithIndex { item, index ->
            Map row = [:]
            productGroupDataValidationError = []
            for (Map.Entry<String, Object> entry : columnsMap.entrySet()) {
                stringValue = ""
                if("count".equals(entry.getKey())) {
                    stringValue = String.valueOf(item.getProperty("validRecordCount")==null?0:item.getProperty("validRecordCount"))+"/"+
                            String.valueOf(item.getProperty(entry.getKey())==null?0:item.getProperty(entry.getKey()))
                } else if("validationError".equals(entry.getKey())) {
                    if(item.getProductGroups()!=null) {
                        for(int i=0; i<item.getProductGroups().size(); i++) {
                            if(!StringUtils.isEmpty(item.getProductGroups().get(i).getProperty("validationError"))) {
                                productGroupDataValidationError.add(item.getProductGroups().get(i).getProperty("validationError"))
                            }
                        }
                    }
                    stringValue = (productGroupDataValidationError==null || productGroupDataValidationError.size()==0)?"":productGroupDataValidationError.join(", ")
                } else if(item.getProperty(entry.getKey()) != null) {
                    if(item.getProperty(entry.getKey()) instanceof java.sql.Timestamp) {
                        stringValue = getDateInStringFormat(item.getProperty(entry.getKey()),DateUtil.DATEPICKER_FORMAT_AM_PM_3)
                    } else if("isApiProcessed".equals(entry.getKey())) {
                        if(item.getValidRecordCount()==null || item.getValidRecordCount()==0) {
                            stringValue = "Failed"
                        } else if(item.getCount()==item.getValidRecordCount()) {
                            stringValue = "Sucess"
                        } else if(item.getCount() > item.getValidRecordCount() && item.getValidRecordCount()>0) {
                            stringValue = "Partially Processed"
                        }
                    } else {
                        stringValue = String.valueOf(item.getProperty(entry.getKey()))
                    }
                }
                row.put(entry.getKey(), stringValue)
            }
            mapList.add(row)
        }
        mapList
    }

    List<Map> getProductGroupExcelData(List<ProductGroupData> batchLotStatusList, LinkedHashMap columnNameMap) {
        List<Map> mapList = []
        String importResult=""
        LinkedHashMap typesMap = Holders.config.pvsignal.pva.dictionary.list
        batchLotStatusList.eachWithIndex { item, index ->
            Map row = [:]
            for (Map.Entry<String, Object> entry : columnNameMap.entrySet()) {
                if("status".equals(entry.getKey())) {
                    if(item.getValidationError().equalsIgnoreCase("successfully created") || item.getValidationError().equalsIgnoreCase("successfully updated") ) {
                        row.put(entry.getKey(), "Success")
                    } else {
                        row.put(entry.getKey(), "Failure")
                    }
                } else if("type".equals(entry.getKey())) {
                    String typeString = null;
                    if(item.getValidationError().equalsIgnoreCase("successfully updated")) {
                        typeString = "Update";
                    }else if(item.getValidationError().equalsIgnoreCase("successfully created")) {
                        typeString = "New";
                    }else if(item.getValidationError().equalsIgnoreCase("successfully created")) {
                        typeString = "New";
                    }else if(item.getProperty("includeSources")!=null && item.getProperty("includeSources").toString().trim().length()>2) {
                        typeString = "Update";
                    } else {
                        typeString = "New";
                    }
                    row.put(entry.getKey(), typeString )
                } else if("includeSources".equals(entry.getKey())) {
                    if(!item.getValidationError().equalsIgnoreCase("successfully created")) {
                        row.put(entry.getKey(), (item.getProperty(entry.getKey())==null?"":String.valueOf(item.getProperty(entry.getKey())).replace("[","").replace("]","")))
                    }
                } else if("sharedWith".equals(entry.getKey())) {
                    Map sharedWithMap = new JsonSlurper().parseText(String.valueOf(item.getProperty(entry.getKey())).replace("=",":"))
                    String userNames="";
                    String groupNames="";
                    if(sharedWithMap!=null && sharedWithMap.size()>0) {
                        if(sharedWithMap.get("users")!=null) {
                            userNames=sharedWithMap.get("users").join(", ");
                        }
                        if(sharedWithMap.get("groups")!=null) {
                            groupNames=sharedWithMap.get("groups").join(", ");
                        }
                    }
                    if("".equals(userNames) && "".equals(groupNames)) {
                        groupNames="All Users"
                    }
                    row.put(entry.getKey(),
                            "User:"+userNames +"\n"+
                            "User Group:"+groupNames+""
                            )
                } else if("data".equals(entry.getKey())) {
                    List<Map> productDataList = new JsonSlurper().parseText(String.valueOf(item.getProperty(entry.getKey())))
                    log.info("productDataList :"+productDataList)
                    StringBuffer productDateSB = new StringBuffer();
                    productDataList.each { pdl ->
                        productDateSB.append("Source:"+pdl.get("source")+"\n")
                        productDateSB.append("Product Id:"+pdl.get("productId")+"\n"+"Product Name:"+pdl.get("productName")+"\n")
                        productDateSB.append("Hierarchy:"+typesMap.get(pdl.get("hierarchy"))+"\n")
                        if(!StringUtils.isEmpty(pdl.get("validationError"))
                                && !"null".equalsIgnoreCase(String.valueOf(pdl.get("validationError")))) {
                            productDateSB.append("Error Info:"+pdl.get("validationError")+"\n")
                        }
                        productDateSB.append("\n")
                    }
                    row.put(entry.getKey(), productDateSB.toString())
                } else {
                    row.put(entry.getKey(), String.valueOf(item.getProperty(entry.getKey())==null?"":item.getProperty(entry.getKey())))
                }
            }
            mapList.add(row)
        }
        mapList.each { pdl ->
            if("successfully created".equalsIgnoreCase(String.valueOf(pdl.get("validationError")))
                    || "successfully updated".equalsIgnoreCase(String.valueOf(pdl.get("validationError")))) {
                pdl.put("validationError","Successfully Processed")
            }
        }
        mapList
    }

    LinkedHashMap getProductGroupDataColumnNameMap() {
        LinkedHashMap map = Holders.config.signal.prodGroups.excel.columns
        map
    }

    String prepareValidatedBatchLotStatusHQL(DataTableSearchRequest searchRequest, params, List<Long> groupIds, User user,
                                             String orderByProperty, String orderDirection) {

        String searchKey = searchRequest?.searchParam?.search?.getValue()?.toLowerCase()
        StringBuilder searchAlertQuery = new StringBuilder()
        if (searchRequest) {
            searchAlertQuery.append(SignalQueryHelper.product_groups_status())
        } else {
            searchAlertQuery.append(SignalQueryHelper.product_groups_status_with_columns())
        }
        validatedBatchStatusSearchFilters(params, groupIds, searchAlertQuery, user, searchKey, searchRequest)

        if (org.apache.commons.lang.StringUtils.upperCase(orderByProperty) in [Constants.Commons.EVENTS, Constants.Commons.PRODUCTS]) {
            searchAlertQuery.append(" ORDER BY dbms_lob.substr(vs.${orderByProperty}, dbms_lob.getlength(vs.${orderByProperty}), 1) ${orderDirection} ")
        } else {
            String orderDir = orderByProperty == Constants.Commons.LAST_UPDATED ? 'desc' : orderDirection;
            searchAlertQuery.append(" ORDER BY vs.${orderByProperty} ${orderDir} ")
        }
        searchAlertQuery.toString()
    }

    String prepareValidatedBatchLotStatusSQLQuery(DataTableSearchRequest searchRequest, params, List<Long> groupIds, User user,
                                             String orderByProperty, String orderDirection) {

        String searchKey = searchRequest?.searchParam?.search?.getValue()?.toLowerCase()
        StringBuilder searchAlertQuery = new StringBuilder()
        if (searchRequest) {
            searchAlertQuery.append(SignalQueryHelper.product_groups_status_sql_qry())
        } else {
            searchAlertQuery.append(SignalQueryHelper.product_groups_status_with_columns_sql_query())
        }
        validatedBatchStatusSearchFiltersBySqlQry(params, groupIds, searchAlertQuery, user, searchKey, searchRequest)

        if (org.apache.commons.lang.StringUtils.upperCase(orderByProperty) in [Constants.Commons.EVENTS, Constants.Commons.PRODUCTS]) {
            searchAlertQuery.append(" ORDER BY dbms_lob.substr(vs.${getOderByColumnName(orderByProperty)}, dbms_lob.getlength(vs.${getOderByColumnName(orderByProperty)}), 1) ${orderDirection} ")
        } else {
            String orderDir = orderByProperty == Constants.Commons.LAST_UPDATED ? 'desc' : orderDirection;
            searchAlertQuery.append(" ORDER BY vs.${getOderByColumnName(orderByProperty)} ${orderDir} ")
        }
        searchAlertQuery.toString()
    }

    String getOderByColumnName(String propertyName) {
        String orderBy = "UPLOADED_DATE"
        Map propertyVsColumnMap = ["id":"ID","version":"version","uniqueIdentifier":"unique_Identifier","count":"count",
                                   "validRecordCount":"valid_Record_Count","invalidRecordCount":"invalid_Record_Count",
                                   "uploadedAt":"UPLOADED_DATE","addedBy":"added_By","isApiProcessed":"is_Api_Processed"]
        if(propertyVsColumnMap.get(propertyName)!=null) {
            orderBy = propertyVsColumnMap.get(propertyName)
        }
        orderBy
    }

    List<ProductGroupStatus> getResultList(Class className, String sql){
        List<ProductGroupStatus> productGroupStatusList = null
        ProductGroupStatus.withTransaction {
            try {
                Session session = sessionFactory.currentSession
                SQLQuery sqlQuery = session.createSQLQuery(sql)
                sqlQuery.setResultTransformer(CriteriaSpecification.PROJECTION)
                session.flush()
                session.clear()
                productGroupStatusList = sqlQuery.list()
            } catch(Exception ex) {
                log.info("Exception: "+ex.toString())
                ex.printStackTrace()
            }
        }
        productGroupStatusList
    }

    List<Map> createBatchLotStatusDTO(List<ProductGroupStatus> validatedSignals, String timeZone, boolean isDashboard = false) {
        List<Map> validatedSignalsDTO = []
        Map blMap = null;
        if (validatedSignals.size()) {
            for (ProductGroupStatus bls : validatedSignals) {
                blMap = new HashMap();
                blMap.put("id", bls.getId());
                blMap.put("version", bls.getVersion());
                blMap.put("uniqueIdentifier", bls.getUniqueIdentifier());
                blMap.put("count", bls.getCount());
                blMap.put("validRecordCount", bls.getValidRecordCount());
                blMap.put("invalidRecordCount", bls.getInvalidRecordCount());
                blMap.put("uploadedAt", getDateInStringFormat(bls.getUploadedAt(),DateUtil.DATEPICKER_FORMAT_AM_PM_3));
                blMap.put("addedBy", bls.getAddedBy());
                blMap.put("apiStatus", "");
                validatedSignalsDTO.add(blMap);
            }

        }
        validatedSignalsDTO
    }
    private int getBatchLotStatusCounts(User user, DataTableSearchRequest searchRequest, String searchKey, params, List<Long> groupIds, boolean isTotalCount) {
        int count
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append(SignalQueryHelper.product_groups_status_count())
        validatedBatchStatusSearchFilters(params, groupIds, searchAlertQuery, user, searchKey, searchRequest, isTotalCount)
        ProductGroupStatus.withTransaction {
            count = ProductGroupStatus.executeQuery(searchAlertQuery.toString())[0] as int
        }
        return count
    }
    private void validatedBatchStatusSearchFilters(params, List<Long> groupIds, StringBuilder searchAlertQuery, User user,
                                                   String searchKey, DataTableSearchRequest searchRequest, boolean isTotalCount = false) {
        if (!isTotalCount && searchKey) {
            searchAlertQuery.append(" and ( lower(vs.uniqueIdentifier) like lower('%${searchKey}%')  or ")
            searchAlertQuery.append("  lower(vs.addedBy) like lower('%${searchKey}%')  or ")
            searchAlertQuery.append("  concat( vs.validRecordCount, '/' , vs.count ) like lower('%${searchKey}%')  or ")
            searchAlertQuery.append("  lower(to_char((vs.uploadedAt),'DD-MON-YYYY HH:MI:ss AM')) like lower('%${searchKey}%') ) ")
        }

        if (!isTotalCount) {
            searchRequest?.searchParam?.columns.each {
                String searchValue = it?.search?.value?.toLowerCase()
                if(searchValue.equals("_")) {
                    searchValue="____"
                }
                if (searchValue) {
                    if (it.name.toLowerCase() == Constants.Commons.UNIQUE_IDENTIFIER.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like lower('%${searchValue}%')   )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ADDED_BY.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like lower('%${searchValue}%')  )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_UPLOADED_DATE.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(to_char((vs.${it.name}),'DD-MON-YYYY HH:MI:ss AM')) like lower('%${searchValue}%') )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_VALID_RECORD_COUNT.toLowerCase()) {
                        searchAlertQuery.append(" and ( concat( vs.validRecordCount, '/' , vs.count ) like lower('%${searchValue}%')  )")
                    } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_COUNT.toLowerCase()) {
                        searchAlertQuery.append(" and ( vs.${it.name} = ${searchValue} )")
                    }
                }
            }
        }
        log.info("searchAlertQuery:"+searchAlertQuery)
    }
    private void validatedBatchStatusSearchFiltersBySqlQry(params, List<Long> groupIds, StringBuilder searchAlertQuery, User user,
                                                   String searchKey, DataTableSearchRequest searchRequest, boolean isTotalCount = false) {
        if (!isTotalCount && searchKey) {
            searchKey = searchKey.toLowerCase()
            String esc_char = ""
            if (searchKey.contains('_')) {
                searchKey = searchKey.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchKey.contains('%')) {
                searchKey = searchKey.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                searchAlertQuery.append(" and ( lower(vs.unique_Identifier) like ('%${searchKey.replaceAll("'", "''")}%') escape '${esc_char}' or ")
                searchAlertQuery.append("  lower(vs.added_By) like ('%${searchKey.replaceAll("'", "''")}%') escape '${esc_char}'  or ")
                if(!searchKey.trim().equals("/")) {
                    searchAlertQuery.append("  concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like ('%${searchKey.replaceAll("'", "''")}%')  escape '${esc_char}' or ")
                }
                searchAlertQuery.append("  lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like ('%${searchKey.replaceAll("'", "''")}%') escape '${esc_char}' ) ")
            } else {
                searchAlertQuery.append(" and ( lower(vs.unique_Identifier) like '%${searchKey}%'  or ")
                searchAlertQuery.append("  lower(vs.added_By) like '%${searchKey}%'  or ")
                if(!searchKey.trim().equals("/")) {
                    searchAlertQuery.append("  concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like '%${searchKey}%'  or ")
                }
                searchAlertQuery.append("  lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchKey}%' ) ")
            }
        }
        if (!isTotalCount) {
            searchRequest?.searchParam?.columns.each {
                String searchValue = it?.search?.value?.toLowerCase()
                String esc_char = ""
                if (searchValue.contains('_')) {
                    searchValue = searchValue.replaceAll("\\_", "!_%")
                    esc_char = "!"
                } else if (searchValue.contains('%')) {
                    searchValue = searchValue.replaceAll("\\%", "!%%")
                    esc_char = "!"
                }
                if (searchValue) {
                    if (esc_char) {
                        if (it.name.toLowerCase() == Constants.Commons.UNIQUE_IDENTIFIER.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.unique_Identifier) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}'  )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ADDED_BY.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.added_By) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_UPLOADED_DATE.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_VALID_RECORD_COUNT.toLowerCase()) {
                            if(!searchValue.trim().equals("/")) {
                                searchAlertQuery.append(" and ( concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like '%${searchValue.replaceAll("'", "''")}%' escape '${esc_char}' )")
                            }
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_COUNT.toLowerCase()) {
                            searchAlertQuery.append(" and ( vs.count = ${searchValue.replaceAll("'", "''")} escape '${esc_char}' )")
                        }
                    } else {
                        if (it.name.toLowerCase() == Constants.Commons.UNIQUE_IDENTIFIER.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.unique_Identifier) like '%${searchValue}%'   )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_ADDED_BY.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(vs.added_By) like '%${searchValue}%'  )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_UPLOADED_DATE.toLowerCase()) {
                            searchAlertQuery.append(" and (lower(to_char((vs.UPLOADED_DATE),'DD-MON-YYYY HH:MI:ss AM')) like '%${searchValue}%' )")
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_VALID_RECORD_COUNT.toLowerCase()) {
                            if(!searchValue.trim().equals("/")) {
                                searchAlertQuery.append(" and ( concat( concat( vs.valid_Record_Count, '/' ), vs.count ) like '%${searchValue}%'  )")
                            }
                        } else if (it.name.toLowerCase() == Constants.Commons.BULK_API_BATCH_COUNT.toLowerCase()) {
                            searchAlertQuery.append(" and ( vs.count = ${searchValue} )")
                        }
                    }
                }
            }
        }
        log.info("searchAlertQuery:"+searchAlertQuery)
    }

    def getDateInStringFormat(Date date, String format) {
        String formattedDate = ""
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            if(date!=null) {
                formattedDate = dateFormat.format(date)
            }
        }catch (Exception ex) {
            log.error(date+"->"+ex.toString())
        }
        formattedDate
    }
    Integer getIntegerValue(String stringNum) {
        Integer intValue = null
        try {
            intValue = stringNum.toInteger()
        } catch (Exception ex) {
            log.error(ex.toString())
        }
        intValue
    }
    private String getDistinctErrorMessage(ProductGroupStatus productGroupStatus, List<ProductGroupCO> productGroupDatas) {
        String errorString = null;
        if (productGroupStatus.getValidRecordCount() == null
                || productGroupStatus.getValidRecordCount() == 0
                || productGroupStatus.getValidRecordCount() < productGroupStatus.getCount()) {
            Set<String> errorDetailsSet = null
            productGroupDatas.each { productGroupData ->
                if (productGroupData.getValidationError() != null && productGroupData.getValidationError().trim().length() > 0) {
                    if (errorDetailsSet == null) {
                        errorDetailsSet = new HashSet<String>();
                    }
                    productGroupData.getValidationError().split("\n").each { validationErr ->
                        errorDetailsSet.add(validationErr)
                    }
                }
            }
            if (errorDetailsSet != null && errorDetailsSet.size() > 0) {
                errorDetailsSet.each { errString ->
                    if (errorString == null) {
                        errorString = errString
                    } else {
                        errorString = errorString + "<br/>" + errString
                    }
                }
            }
        }
        errorString
    }
    List<Map> getProductGroupSummaryData(Long productGroupsId) {
        List<ProductGroupStatus> batchLotStatusList = ProductGroupStatus.createCriteria().list { eq("id", productGroupsId) };

        Long validRecordCount = 0
        Long invalidRecordCount = 0
        Long count = 0
        String uploadedAt =  getDateInStringFormat(batchLotStatusList[0].getUploadedAt(),DateUtil.DATEPICKER_FORMAT)
        batchLotStatusList.each{it ->
            validRecordCount = it.validRecordCount + validRecordCount}
        batchLotStatusList.each{it ->
            invalidRecordCount = it.invalidRecordCount + invalidRecordCount}
        batchLotStatusList.each{it ->
            count = (it.count==null?0:it.count)+ count}
        List mapList = [
                ['label': "Imported Date", 'value': uploadedAt],
                ['label': "Total number of Records", 'value': count],
                ['label': "Total number of Records Imported", 'value': validRecordCount],
                ['label': "Total number of Records Failed", 'value': invalidRecordCount]]
        return mapList
    }

    ProductGroupData getProductGroupData(ProductGroupCO productGroupCO, String uniqueIdentifier) {
        ProductGroupData prodGroupData = new ProductGroupData()
        prodGroupData.id = productGroupCO.id
        prodGroupData.productGroupStatusId = productGroupCO.productGroupStatusId
        prodGroupData.uniqueIdentifier = uniqueIdentifier
        prodGroupData.groupName = productGroupCO.name
        prodGroupData.groupOldName = productGroupCO.oldName
        prodGroupData.type = productGroupCO.type
        prodGroupData.description = productGroupCO.description
        prodGroupData.copyGroups = productGroupCO.copyGroups
        prodGroupData.owner = productGroupCO.owner
        prodGroupData.tenantId = productGroupCO.tenantId
        prodGroupData.includeSources = productGroupCO.includeSources
        try {
            prodGroupData.sharedWith = (new com.fasterxml.jackson.databind.ObjectMapper()).writeValueAsString(productGroupCO.sharedWith)
        } catch (Exception ex) {
            log.error("getProductGroupData -> Error in string conversion of prodGroupData.data to String:"+ex.toString())
        }
        try {
            prodGroupData.data = (new com.fasterxml.jackson.databind.ObjectMapper()).writeValueAsString(productGroupCO.products)
        } catch (Exception ex) {
            log.error("getProductGroupData -> Error in string conversion of prodGroupData.data to String:"+ex.toString())
        }
        prodGroupData.validationError = productGroupCO.validationError
        prodGroupData
    }

}
