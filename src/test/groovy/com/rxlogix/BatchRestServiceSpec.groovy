package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.commandObjects.BatchLotCO
import com.rxlogix.commandObjects.BatchLotDataCO
import com.rxlogix.config.*
import com.rxlogix.signal.BatchLotData
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.slf4j.Logger
import spock.lang.Specification

import javax.print.DocFlavor

//@Mock([BatchLotStatus,User,BatchLotStatus,BatchLotData, AuditTrail])
//@TestFor(BatchRestService)
class BatchRestServiceSpec extends Specification implements ServiceUnitTest<BatchRestService> {
    DataTableSearchRequest searchRequest
    User user
    BatchLotStatus batchLotStatus
    BatchLotData batchLotData
    Logger logger
    void setup() {
        logger = Mock(Logger)
        service.log = logger
        service.sessionFactory = Mock(SessionFactory)

        searchRequest = new DataTableSearchRequest();
        searchRequest.searchParam = new DataTableSearchRequest.DataTableSearchParam()
        searchRequest.searchParam.start = 0
        searchRequest.searchParam.length = 50
        searchRequest.searchParam.draw = 1
        searchRequest.searchParam.search = new DataTableSearchRequest.Search()
        searchRequest.searchParam.search.value = ""
        searchRequest.searchParam.search.regex = false
        List<DataTableSearchRequest.Columns> column = new ArrayList<DataTableSearchRequest.Columns>()
        column.add(new DataTableSearchRequest.Columns())
        column.add(new DataTableSearchRequest.Columns())
        searchRequest.searchParam.columns = column
        List<DataTableSearchRequest.Order> order = new ArrayList<>()
        order.add(new DataTableSearchRequest.Order())
        searchRequest.searchParam.order = order
        searchRequest.searchParam.order[0].setColumn(1)
        searchRequest.searchParam.order[0].setDir("desc")

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

        batchLotStatus  = new BatchLotStatus(id: 1,version: 1,batchId: "1",batchDate: new Date(),addedBy: user,isApiProcessed: false,isEtlProcessed: false, etlStartDate: new Date(),etlStatus: "valid",uploadedAt: new Date())
        batchLotStatus.save(validate:false)

        batchLotData = new BatchLotData(id:1,batchLotId: 1,version: 1,productId:111,product: "drug",description: "test case")
        batchLotData.save(validate:false)

    }

    void "test getRemainingBatchLotCountForETL"() {
        setup:
        def myCriteria = [
                get : {Closure  cls -> [BatchLotStatus.count()]}
        ]
        BatchLotStatus.metaClass.static.createCriteria = { myCriteria }
        BatchLotStatus.createCriteria()
        when:
        Long result = service.getRemainingBatchLotCountForETL()

        then:
        result == 0
    }

    void "test runEtlForRemainingApiBatchLot"() {
        setup:
        Session session = Mock(Session)
        service.sessionFactory_pva.getCurrentSession() >> session
        when:
        String result = service.runEtlForRemainingApiBatchLot()
        then:
        result == "0"
    }
    void "test getBatchLotStatusList"(){
        setup:
        service.metaClass.getBatchLotStatuses = { DataTableSearchRequest searchRequest, params->
            return []
        }
        service.metaClass.createBatchLotStatusDTO = { List<BatchLotStatus> validatedSignals, String timeZone, boolean isDashboard = false->
            return []
        }
        service.metaClass.getBatchLotStatusCounts = { User user, DataTableSearchRequest searchRequest, String searchKey, params, List<Long> groupIds, boolean isTotalCount->
            return 2
        }
        def myCriteria = [
                get : {Closure  cls -> [BatchLotStatus.count()]}
        ]
        BatchLotStatus.metaClass.static.createCriteria = { myCriteria }
        BatchLotStatus.createCriteria()
        Map params = [:]
        when:
        Map result = service.getBatchLotStatusList(searchRequest,params)
        then:
        result.aaData == [:]
        result.recordsTotal == 0
        result.recordsFiltered ==2

    }

    void "test getLastETLBatchLots"(){
        setup:
        def myCriteria = new Expando();
        myCriteria.get = {Closure  cls -> [BatchLotStatus.findAll()]}
        BatchLotStatus.metaClass.static.createCriteria = { myCriteria }
        BatchLotStatus.createCriteria()
        when:
        List result  = service.getLastETLBatchLots()
        then:
        result == []

    }

    void "test getBatchLotStatuses"(){
        setup:
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append("SELECT vs FROM BatchLotStatus vs WHERE 1=1")
        searchAlertQuery.toString()
        service.metaClass.prepareValidatedBatchLotStatusHQL = { DataTableSearchRequest searchRequest, params, List<Long> groupIds, User user,
                                                                String orderByProperty, String orderDirection->
            return searchAlertQuery
        }
        Map params = [:]
        when:
        List result = service.getBatchLotStatuses(searchRequest,params)
        then:
        result == []
    }

    void "test getBatchLotStatusCounts"(){
        setup:
        String sKey = "searchKey"
        Map param =[:]
        List groupId =[1]
        service.metaClass.validatedBatchStatusSearchFilters = { params, List<Long> groupIds, StringBuilder searchAlertQuery, User user, String searchKey, DataTableSearchRequest searchRequest, boolean isTotalCount = false ->
            return searchAlertQuery
        }
        when:
        int count = service.getBatchLotStatusCounts(user, searchRequest, sKey, param, groupId, false)
        then:
        count == 0
    }

    void "test prepareValidatedBatchLotStatusHQL when searchRequest is persent"(){
        setup:
        String orderByProperty = "version"
        String orderDirection = "ascending"
        Map param =[:]
        List groupId =[1]
        when:
        String result = service.prepareValidatedBatchLotStatusHQL(searchRequest, param,groupId,user,
                orderByProperty, orderDirection)
        then:
        result == "SELECT vs FROM BatchLotStatus vs WHERE 1=1 ORDER BY vs.version ascending "
    }

    void "test prepareValidatedBatchLotStatusHQL when searchRequest is absent"(){
        setup:
        String orderByProperty = "version"
        String orderDirection = "ascending"
        Map param =[:]
        List groupId =[1]
        when:
        String result = service.prepareValidatedBatchLotStatusHQL(null, param,groupId,user,
                orderByProperty, orderDirection)
        then:
        result == "SELECT new Map(vs.batchId as batchId, vs.id as id , vs.batchDate as batchDate, vs.count as count,vs.validRecordCount as validRecordCount ,vs.invalidRecordCount as invalidRecordCount,vs.uploadedAt as uploadedAt,vs.addedBy as addedBy) FROM BatchLotStatus vs WHERE 1=1 ORDER BY vs.version ascending "
    }

    void "test prepareValidatedBatchLotStatusHQL when searchRequest is absent and orderByProperty is EVENTS"(){
        setup:
        String orderByProperty = "events"
        String orderDirection = "ascending"
        Map param =[:]
        List groupId =[1]
        when:
        String result = service.prepareValidatedBatchLotStatusHQL(null, param,groupId,user,
                orderByProperty, orderDirection)
        then:
        result == "SELECT new Map(vs.batchId as batchId, vs.id as id , vs.batchDate as batchDate, vs.count as count,vs.validRecordCount as validRecordCount ,vs.invalidRecordCount as invalidRecordCount,vs.uploadedAt as uploadedAt,vs.addedBy as addedBy) FROM BatchLotStatus vs WHERE 1=1 ORDER BY dbms_lob.substr(vs.${orderByProperty}, dbms_lob.getlength(vs.${orderByProperty}), 1) ${orderDirection} "
    }

    void "test prepareValidatedBatchLotStatusHQL when searchRequest is persent and orderByProperty is PRODUCTS"(){
        setup:
        String orderByProperty = "products"
        String orderDirection = "ascending"
        Map param =[:]
        List groupId =[1]
        when:
        String result = service.prepareValidatedBatchLotStatusHQL(searchRequest, param,groupId,user,
                orderByProperty, orderDirection)
        then:
        result == "SELECT vs FROM BatchLotStatus vs WHERE 1=1 ORDER BY dbms_lob.substr(vs.${orderByProperty}, dbms_lob.getlength(vs.${orderByProperty}), 1) ${orderDirection} "
    }

    void "test validatedBatchStatusSearchFilters when searchkey is absent"(){
        setup:
        Map params = [:]
        List groupId =[1]
        StringBuilder searchAlertQuery = new StringBuilder()
        when:
        String result =  service.validatedBatchStatusSearchFilters(params, groupId, searchAlertQuery,user, null, searchRequest, false)
        then:
        result == null

    }
    void "test validatedBatchStatusSearchFilters when searchkey is persent"(){
        setup:
        Map params = [:]
        List groupId =[1]
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append("new")
        searchAlertQuery.toString()
        String searchKey = "12/10/2021"
        when:
        String result =  service.validatedBatchStatusSearchFilters(params, groupId, searchAlertQuery,user,searchKey, searchRequest, false)
        then:
        result == null

    }

    void "test createBatchLotStatusDTO"(){
        setup:
        List signal = []
        when:
        List result = service.createBatchLotStatusDTO(signal,"UTC", false)
        then:
        result ==[]
    }

    void "test fetchAttachments"(){
        setup:
        String sb = "test"
        String id = "newFile"

        when:
        def result = service.fetchAttachments(id, sb)
        then:
        result.name == ["newFile.csv"]
        result.file == [[116, 101, 115, 116]]
    }

    void "test saveBatchClientRecord"(){
        setup:
        service.metaClass.saveBatchLotStatusAndData = { BatchLotCO batchLotCO, String username, Long batchLotId->
            return batchLotStatus
        }
        service.metaClass.sendMailOnBatchLotUpdate = { BatchLotStatus batchLotStatus, String username->
        }
        service.metaClass.saveAuditTrail= { BatchLotCO batchLotCO, Long batchLotId, Date uploadedAt, String batchLotClientDataString->
        }
        BatchLotCO batchLotCO = new BatchLotCO()
        when:
        def result = service.saveBatchClientRecord(batchLotCO, user.name,1)
        then:
        result == batchLotStatus
    }

    void "test saveBatchLotStatusAndData"(){
        setup:
        BatchLotCO batchLotCO = new BatchLotCO()
        when:
        def result =service.saveBatchLotStatusAndData(batchLotCO, user.name, 1)
        then:
        result ==null
    }

    void "test objectToString"(){
        setup:
        BatchLotData clientData = new BatchLotData()
        when:
        def result = service.objectToString(clientData)
        then:
        result == "{,{}"
    }

    void "test validateBatchLotData when batchLotData is empty"(){
        setup:
        BatchLotData clientData = new BatchLotData()
        when:
        String result = service.validateBatchLotData(clientData)
        then:
        result == "Data row must not be empty "
    }

    void "test validateBatchLotData"(){
        setup:
        batchLotData.fillExpiry = 123456789999999999
        batchLotData.bulkBatchDate = 1234
        batchLotData.packageReleaseDate = 12
        batchLotData.product = "67R8n2N1PxfhJSvuOqNuF2VYbtzgFF8ESr514xf8WkRTpJVoiQxnU1HNsf4WlgFDbIEhiJYoV7E PWQfe6VV6bXvQZ3 cfDYBlSc8DqjI34Mz6TjCqd2HaHkcOUglv35GXpWKIkdjFf6p2fhUedbAFh9ek0SadRypV8hLb4AxLgSPtt6PkSRgo8YgtJEW0QNdY3XXamWjwvztiRlVn7r2p6VyaU5HpYgKYA2SY5mh878H3n6CRriaT8upb3nkkS"
        batchLotData.productId = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFo"
        batchLotData.bulkBatch = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFooo"
        batchLotData.fillBatch = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFog"
        batchLotData.fillBatchName = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFol"
        batchLotData.fillUnits = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFor"
        batchLotData.packageBatch = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFom"
        batchLotData.packageCountry = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFog"
        batchLotData.packageUnit = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFol"
        batchLotData.shippingBatch = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFor"
        batchLotData.componentBatch = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFom"
        batchLotData.dataPeriod = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFog"
        batchLotData.udField1 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFol"
        batchLotData.udField2 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFor"
        batchLotData.udField3 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFom"
        batchLotData.udField4 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFog"
        batchLotData.udField5 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFol"
        batchLotData.udField6 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFor"
        batchLotData.udField7 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFom"
        batchLotData.udField8 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFol"
        batchLotData.udField9 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFor"
        batchLotData.udField10 = "ZH7VxvUpyW05dT78srwySob0ojAOsWKTIGDWOQmLfZyZK7bIuDrhUaOoeYOyWtmQiknAi8vC0RrIIyRSNG jBni2 n7vFiflXq98Q7Nh1M3HHlcWuhrlnHtAuz 2wyA7YK9fNUdG17xBfqa82X5sUIHNUC2vQlKn2NkLDRmh2iRb9RvryZp6WhZD8yFYP1wqm7LOtCTFpxGSszPMyV09yvoBY3P9XF 6qXiIifyOKDr3psu2ps2tXFeY1OwsmFom"
        when:
        String result = service.validateBatchLotData(batchLotData)
        then:
        result == "productId must be less than 250 charectors \n" +
                "product must be less than 250 charectors \n" +
                "BulkBatch must be less than 250 charectors \n" +
                "BulkBatchDate must be date in dd-MMM-yy format \n" +
                "FillBatch must be less than 250 charectors \n" +
                "FillBatchName must be less than 250 charectors \n" +
                "FillExpiry must be date in dd-MMM-yy format \n" +
                "FillUnits must be less than 250 charectors \n" +
                "PackageBatch must be less than 250 charectors \n" +
                "PackageCountry must be less than 250 charectors \n" +
                "PackageUnit must be less than 250 charectors \n" +
                "PackageReleaseDate must be date in dd-MMM-yy format \n" +
                "ShippingBatch must be less than 250 charectors \n" +
                "ComponentBatch must be less than 250 charectors \n" +
                "DataPeriod must be less than 250 charectors \n" +
                "UdField1 must be less than 250 charectors \n" +
                "UdField2 must be less than 250 charectors \n" +
                "UdField3 must be less than 250 charectors \n" +
                "UdField4 must be less than 250 charectors \n" +
                "UdField5 must be less than 250 charectors \n" +
                "UdField6 must be less than 250 charectors \n" +
                "UdField7 must be less than 250 charectors \n" +
                "UdField8 must be less than 250 charectors \n" +
                "UdField9 must be less than 250 charectors \n" +
                "UdField10 must be less than 250 charectors \n"

    }

    void "test addValidationError when is mandatory and column value is null and error string null"(){
        setup:
        batchLotData.fillExpiry = 123456789999999999
        String errorString = ""
        when:
        String result = service.addValidationError(errorString,null, "FillExpiry", true, 10, "DATE")
        then:
        result ==  "FillExpiry"+" must be mandatory \n"
    }

    void "test addValidationError when is mandatory and column value is null and error string is not null"(){
        setup:
        batchLotData.fillExpiry = 123456789999999999
        String errorString = "ERROR!"
        when:
        String result = service.addValidationError(errorString,null, "FillExpiry", true, 10, "DATE")
        then:
        result ==  errorString+"FillExpiry"+" must be mandatory \n"
    }

    void "test addValidationError when dataType is String and lenght is bigger and error string is empty"(){
        setup:
        batchLotData.product = "OjPC8xAHM6xRGwO8qYTVY7Ia40NN80rYAyE34oMj G8XaDmM6taqBepT4eDMQ6w2lciBjfClL1kKHKkP VSxvVVVkN24zAe4QyH8voY 6Ot0OnLxq3zRKqaCLs0x5j htV1RuHMeI1ME4xMrnMCptgNOiagNbxvB6qoG74GP8FeuATr z7csy770omH3tIVn9ZfdFSpPxyD6BqrkhOaTLXq caIca USPPCk6Gx8ro7J6Z7tgjEXcvbZ4WWl3Lgqqh50"
        batchLotData.save(validate:false)
        String errorString = ""
        when:
        String result = service.addValidationError(errorString,batchLotData.product, "product", false, 10, "STRING")
        then:
        result == "product"+" must be less than "+10+" charectors \n"
    }

    void "test addValidationError when dataType is String and lenght is bigger and"(){
        setup:
        batchLotData.product = "OjPC8xAHM6xRGwO8qYTVY7Ia40NN80rYAyE34oMj G8XaDmM6taqBepT4eDMQ6w2lciBjfClL1kKHKkP VSxvVVVkN24zAe4QyH8voY 6Ot0OnLxq3zRKqaCLs0x5j htV1RuHMeI1ME4xMrnMCptgNOiagNbxvB6qoG74GP8FeuATr z7csy770omH3tIVn9ZfdFSpPxyD6BqrkhOaTLXq caIca USPPCk6Gx8ro7J6Z7tgjEXcvbZ4WWl3Lgqqh50"
        batchLotData.save(validate:false)
        String errorString = "ERROR!"
        when:
        String result = service.addValidationError(errorString,batchLotData.product, "product", false, 10, "STRING")
        then:
        result == errorString+"product"+" must be less than "+10+" charectors \n"
    }

    void "test addValidationError when dataType is Date and error string is empty"(){
        setup:
        batchLotData.fillExpiry = 123456789999999999
        batchLotData.save(validate:false)
        String errorString = ""
        when:
        String result = service.addValidationError(errorString,batchLotData.fillExpiry, "FillExpiry", false, 10, "DATE")
        then:
        result ==  "FillExpiry"+" must be date in dd-MMM-yy format \n"
    }

    void "test addValidationError when dataType is Date"(){
        setup:
        batchLotData.fillExpiry = 123456789999999999
        batchLotData.save(validate:false)
        String errorString = "Error!"
        when:
        String result = service.addValidationError(errorString,batchLotData.fillExpiry, "FillExpiry", false, 10, "DATE")
        then:
        result ==  errorString+"FillExpiry"+" must be date in dd-MMM-yy format \n"
    }

    void "tets getBatchDataColumnMapping"(){
        when:
        Map result = service.getBatchDataColumnMapping()
        then:
        result == [:]
    }

    void "test getBatchDataColumnNameMap"(){
        when:
        Map result = service.getBatchDataColumnNameMap()
        then:
        result == [:]
    }

    void "test getBatchLotDatas"(){
        setup:
        BatchLotDataCO batchLotDataCO = new BatchLotDataCO(id: 1,apiUsername: "user",apiTocken: "abcd",batchId: 1, batchDate: new Date(),count: 10)
        when:
        List result = service.getBatchLotDatas(batchLotDataCO.getClientDatas())
        then:
        result == null
    }

    void "test getBatchLotCSVData"(){
        setup:
        List batchList = []
        when:
        String result = service.getBatchLotCSVData(batchList)
        then:
        result == "\"Batch No.\",\"Batch Date\",\"Processed\",\"Total\",\"Uploaded Date\",\"Added By\",\"Api Processed\",\"Etl Processed\"\n"
    }

    void "test getCSVString"(){
        setup:
        when:
        String result = service.getCSVString([],[:])
        then:
        result  == ""
    }

    void "test  getValue"(){
        setup:
        Object obj = "test"
        when:
        Object result = service.getValue(obj)
        then:
        result == "test"
    }

    void "test saveAuditTrail"(){
        setup:
        Session session = Mock(Session)
        service.sessionFactory.getCurrentSession() >> session
        UserService mockUserService = Mock(UserService)
        mockUserService.currentUserName() >> {
            return  "System"
        }
        service.userService = mockUserService
        BatchLotCO batchLotCO = new BatchLotCO(id: 1,apiUsername: "user",apiTocken: "abcd",batchId: 1, batchDate: new Date(),count: 10)
        when:
        service.saveAuditTrail(batchLotCO, batchLotStatus.id, batchLotStatus.getUploadedAt(), null)
        then:
        logger.info(null)

    }
    void "test batchLotDto"(){
        setup:
        Date abatchDate  = new Date()
        Date auploadedAt = new Date()
        when:
        Map result = service.batchLotDto(1l,1l,"2", abatchDate, 5l, 4l, 4l,auploadedAt, user.name)
        then:
        result ==  [
                id:1l,
                version:1l,
                batchId:"2",
                batchDate:(abatchDate.getTime()),
                count:5l,
                validRecordCount:4l,
                invalidRecordCount:4l,
                uploadedAt:(auploadedAt.getTime()),
                addedBy:user.name
        ]

    }

}