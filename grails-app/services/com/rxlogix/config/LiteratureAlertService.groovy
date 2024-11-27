package com.rxlogix.config

import com.rxlogix.*
import com.rxlogix.audit.AuditTrail
import com.rxlogix.cache.CacheService
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.AlertReviewDTO
import com.rxlogix.dto.QueryDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.helper.LinkHelper
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.GlobalArticle
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.signal.SystemConfig
import com.rxlogix.signal.UndoableDisposition
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.xml.StreamingMarkupBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang.StringUtils
import org.apache.http.ConnectionClosedException
import org.grails.datastore.mapping.query.Query
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.jdbc.Work
import org.hibernate.sql.JoinType
import org.joda.time.DateTimeZone
import org.springframework.transaction.annotation.Propagation

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader
import javax.xml.stream.events.XMLEvent
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import org.joda.time.DateTime
import static com.rxlogix.util.DateUtil.DEFAULT_DATE_FORMAT
import static groovyx.net.http.ContentType.URLENC

@Transactional
class LiteratureAlertService implements Alertbililty, LinkHelper, EventPublisher, AlertUtil {

    def CRUDService
    def userService
    def configurationService
    def sessionFactory
    def emailService
    def messageSource
    def literatureActivityService
    def validatedSignalService
    def dynamicReportService
    def alertService
    def actionService
    def alertTagService
    def alertCommentService
    ActivityService activityService
    EmailNotificationService emailNotificationService
    DispositionService dispositionService
    def signalExecutorService
    def pvsAlertTagService
    def pvsGlobalTagService
    def dataObjectService
    LiteratureHistoryService literatureHistoryService
    def archiveService
    CacheService cacheService
    def spotfireService
    def undoableDispositionService
    CustomMessageService customMessageService
    def signalAuditLogService
    LiteratureExecutionService literatureExecutionService
    def queryService

    void createAlert(Long configId, Long executedConfigId, List<Map<String, Object>> alertDataList) throws Exception {
        ExecutorService executorService = null
        try {
            if (alertDataList) {
                LiteratureConfiguration config = LiteratureConfiguration.get(configId)
                ExecutedLiteratureConfiguration executedConfig = ExecutedLiteratureConfiguration.get(executedConfigId)
                Map monthMap = ["01": "Jan", "02": "Feb", "03": "Mar", "04": "Apr", "05": "May", "06": "Jun", "07": "Jul", "08": "Aug", "09": "Sep", "10": "Oct", "11": "Nov", "12": "Dec"]
                Disposition defaultDisposition = executedConfig?.getOwner()?.getWorkflowGroup()?.defaultLitDisposition
                Integer workerCnt = Holders.config.signal.worker.count as Integer
                List<LiteratureAlert> resultData = []
                int alertDataListSize = alertDataList.size() > 9000 ? 9000 : alertDataList.size()
                alertDataList = alertDataList.take(alertDataListSize)

                log.info("Thread Starts")
                executorService = Executors.newFixedThreadPool(5)

                def allArticleIds = fetchArticlesFromData(alertDataList);
                List<Long> newArticleIds = fetchNewArticles(allArticleIds)
                pvsGlobalTagService.batchPersistGlobalArticles(newArticleIds)
                List<GlobalArticle> globalArticleList = fetchGlobalArticles(allArticleIds)
                dataObjectService.setGlobalArticleList(executedConfig.id , globalArticleList)
                List<LiteratureHistory> existingLiteratureHistoryList = LiteratureHistory.createCriteria().list {
                    eq("litConfigId",config.id)
                    order("lastUpdated", "desc")
                } as List<LiteratureHistory>
                // Setting globalArticleList to null as it's no longer needed
                globalArticleList = null
                log.info("Literature Alert Data Creation Started")
                alertDataList.collate(5000).each { List<LiteratureAlert> literatureAlertList ->
                    List<Future<LiteratureAlert>> futureList = literatureAlertList.collect { Map data ->
                        executorService.submit({ ->
                            parallellyCreateLiteratureAlert(data, config, executedConfig, defaultDisposition, monthMap, existingLiteratureHistoryList)
                        } as Callable)
                    }
                    futureList.each {
                        resultData.add(it.get())
                    }
                }
                // Setting alertDataList to null as it's no longer needed
                alertDataList = null
                log.info("Literature Alert Data Creation Completed")
                log.info("Thread Ends")
                batchPersistData(resultData, executedConfig, config)
                alertService.updateOldExecutedConfigurationsLiterature(config, executedConfig.id,ExecutedLiteratureConfiguration, resultData.countBy {it.dispositionId})
                persistValidatedSignalWithLiteratureAlert(config.id, executedConfig.id, LiteratureAlert)
                List<Long> prevExecConfigIds = alertService.getLiteraturePrevExConfigIds(executedConfig, config.id)
                if(prevExecConfigIds.size() > 0) {
                    Session session = sessionFactory.currentSession
                    insertLitAlertActions(session, executedConfigId, prevExecConfigIds[0] as Long)
                }
                archiveService.moveDatatoArchive(executedConfig, LiteratureAlert, prevExecConfigIds)
                // Setting resultData to null as it's no longer needed
                resultData = null
            } else {
                log.error("There is no data to execute")
            }
        } catch (Throwable e) {
            log.error(e.printStackTrace())
            throw e
        } finally{
            dataObjectService.clearGlobalArticleMap(executedConfigId)
            if (executorService != null) {
                executorService.shutdown()
            }
        }
    }

    String convertDateFormat(String dateString, def monthMap) {
        if (!dateString || dateString.trim().isEmpty()) {
            return ""
        }

        String[] dateParts = dateString.split("-")
        if (dateParts.length != 3) {
            return ""
        }
        String year = dateParts[0]
        String month = dateParts[1]
        String day = dateParts[2].padLeft(2, '0')

        String monthText = monthMap[month]
        return "${day}-${monthText}-${year}"
    }

    def createEmbaseAlerts(LiteratureConfiguration config, ExecutedLiteratureConfiguration executedConfig, String filePath) {
        List<EmbaseLiteratureAlert> alertsBatch = []
        Map<String, Integer> totalDispositionCounts = [:]
        Map<String, String> monthMap = [
                '01': 'Jan', '02': 'Feb', '03': 'Mar', '04': 'Apr',
                '05': 'May', '06': 'Jun', '07': 'Jul', '08': 'Aug',
                '09': 'Sep', '10': 'Oct', '11': 'Nov', '12': 'Dec'
        ]
        log.info("Starting Embase Alerts creation")
        try {
            List<LiteratureHistory> existingLiteratureHistoryList = LiteratureHistory.createCriteria().list { //+
                eq("litConfigId", config.id)
                order("lastUpdated", "desc")
            } as List<LiteratureHistory>
            Disposition defaultDisposition = executedConfig?.getOwner()?.getWorkflowGroup()?.defaultLitDisposition //+

            XMLInputFactory factory = XMLInputFactory.newInstance()

            try (FileInputStream inputStream = new FileInputStream(filePath)) {
                XMLStreamReader reader = factory.createXMLStreamReader(inputStream)
                try {
                    while (reader.hasNext()) {
                        int event = reader.next()
                        if (event == XMLEvent.START_ELEMENT && reader.localName == 'bibrecord') {
                            EmbaseLiteratureAlert alert = createAlertFromRecord(reader, monthMap)
                            alertsBatch << alert
                            if (alertsBatch.size() >= 500) {
                                processAlertsBatch(config, executedConfig, existingLiteratureHistoryList, defaultDisposition, alertsBatch, totalDispositionCounts)
                                alertsBatch.clear()
                            }
                        }
                    }
                } finally {
                    reader.close()
                }
            } catch (Exception e) {
                log.error("Error processing XML file", e)
                throw e
            }

            if (!alertsBatch.isEmpty()) {
                processAlertsBatch(config, executedConfig, existingLiteratureHistoryList, defaultDisposition, alertsBatch, totalDispositionCounts)
            }

            alertService.updateOldExecutedConfigurationsLiterature(config, executedConfig.id, ExecutedLiteratureConfiguration, totalDispositionCounts)

            persistValidatedSignalWithLiteratureAlert(config.id, executedConfig.id, EmbaseLiteratureAlert)
            List<Long> prevExecConfigIds = alertService.getLiteraturePrevExConfigIds(executedConfig, config.id)
            if (prevExecConfigIds.size() > 0) {
                Session session = sessionFactory.currentSession
                insertLitAlertActions(session, executedConfig.id, prevExecConfigIds[0] as Long)
            }
            archiveService.moveDatatoArchive(executedConfig, EmbaseLiteratureAlert, prevExecConfigIds)
        } catch (Throwable e) {
            log.error(e.printStackTrace())
            throw e
        } finally {
            dataObjectService.clearGlobalArticleMap(executedConfig.id)
        }
    }

    void processAlertsBatch(LiteratureConfiguration config, ExecutedLiteratureConfiguration executedConfig,
                            List<LiteratureHistory> existingLiteratureHistoryList, Disposition defaultDisposition,
                            List<EmbaseLiteratureAlert> alertBatch, Map totalDispositionCounts) {
        log.info("Processing Embase Alerts batch")
        if (alertBatch) {
            def allArticleIds = alertBatch.collect { it.articleId as Long }
            List<Long> newArticleIds = fetchNewArticles(allArticleIds)
            pvsGlobalTagService.batchPersistGlobalArticles(newArticleIds)
            List<GlobalArticle> globalArticleList = fetchGlobalArticles(allArticleIds)
            dataObjectService.addGlobalArticleList(executedConfig.id , globalArticleList)
            globalArticleList = null

            populateEmbaseAlerts(config, executedConfig, alertBatch, defaultDisposition, existingLiteratureHistoryList)

            batchPersistEmbaseData(alertBatch)

            alertBatch.countBy { it.disposition.id }.each { key, value ->
                totalDispositionCounts[key] = (totalDispositionCounts[key] ?: 0) + value
            }
            alertBatch = null
        } else {
            log.error("There is no data to execute")
        }
    }

    def populateEmbaseAlerts(LiteratureConfiguration config, ExecutedLiteratureConfiguration executedConfig,
                             List<EmbaseLiteratureAlert> embaseLiteratureAlerts, Disposition defaultDisposition,
                             List<LiteratureHistory> existingLiteratureHistoryList) {

        embaseLiteratureAlerts.each {alert ->
            alert.dateCreated = executedConfig?.dateCreated ?: (new Date())
            alert.lastUpdated = executedConfig?.lastUpdated ?: (new Date())
            alert.name = config.name
            alert.assignedTo = config?.assignedTo ? config.assignedTo : null
            alert.assignedToGroup = config?.assignedToGroup ? config.assignedToGroup : null
            alert.disposition = defaultDisposition
            alert.priority = config.priority
            alert.productSelection = config.productSelection ? getNameFieldFromJson(config.productSelection):getGroupNameFieldFromJson(config.productGroupSelection)
            alert.eventSelection = config.eventSelection ? getNameFieldFromJson(config.eventSelection):getGroupNameFieldFromJson(config.eventGroupSelection)
            alert.searchString = config.searchString ?: Constants.Commons.DASH_STRING
            alert.litSearchConfig = config
            alert.exLitSearchConfig = executedConfig
            alert.globalIdentity = dataObjectService.getGlobalArticleList(executedConfig.id , Long.valueOf(alert.articleId) )

            LiteratureHistory existingLiteratureHistory = existingLiteratureHistoryList.find {it.articleId == Long.valueOf(alert.articleId) && it.isLatest}
            if(existingLiteratureHistory){
                alert.disposition = cacheService.getDispositionByValue(existingLiteratureHistory.currentDispositionId)
                if (existingLiteratureHistory.currentAssignedTo) {
                    alert.assignedTo = cacheService.getUserByUserId(existingLiteratureHistory.currentAssignedToId)
                } else {
                    alert.assignedToGroup = cacheService.getGroupByGroupId(existingLiteratureHistory.currentAssignedToGroupId)
                }
                alert.priority = cacheService.getPriorityByValue(existingLiteratureHistory.currentPriorityId)
            }
        }
    }

    void insertLitAlertActions(Session session, Long executedConfigId, Long prevExecConfigId) {
        // Retrieve the alert actions using a helper method
        String sqlStatement = SignalQueryHelper.lit_alert_actions(executedConfigId, prevExecConfigId)
        List<Map<String, String>> alertIdAndActionIdList = session.createSQLQuery(sqlStatement)
                .list()
                .collect { row -> [col1: row[0].toString(), col2: row[1].toString(), col3: '1'] }
        // Log the start of the batch execution
        log.info("Batch execution of TABLE LIT_ALERT_ACTIONS started")

        // Insert the alert actions into the database
        String insertActionSingleAlertQuery = SignalQueryHelper.insert_lit_actions_sql()
        alertService.batchPersistForStringParameters(session, alertIdAndActionIdList, insertActionSingleAlertQuery, 3)

        // Log the end of the batch execution
        log.info("Batch execution of TABLE LIT_ALERT_ACTIONS ended")

        // Update the actions count in the alert
        log.info("Now updating the Actions Count in Alert")
        String actionCountSql = SignalQueryHelper.lit_alert_actions_count(executedConfigId)
        session.createSQLQuery(actionCountSql).executeUpdate()
        log.info("Action Count For Alert updated")
    }

    LiteratureAlert parallellyCreateLiteratureAlert(Map<String,Object> alertData, LiteratureConfiguration config, ExecutedLiteratureConfiguration executedConfig,
                                                    Disposition defaultDisposition,Map monthMap, List<LiteratureHistory> existingLiteratureHistoryList) {

        String publicationDate = getPublicationDate(alertData, monthMap, executedConfig.dateRangeInformation.getReportStartAndEndDate())
        String articleAbstract = ""
        if (alertData.containsKey(Constants.LiteratureFields.ABSTRACT_TEXT)) {
            articleAbstract = formatAbstractData(alertData.AbstractText)
        }
        String citation = alertData.Citations?.join("\n")
        if (citation.length() >= 32000) {
            citation = citation?.trim()?.substring(0, 31999)
        }
        Integer articleID = alertData.PMID as Integer
        LiteratureAlert literatureSearchAlert = new LiteratureAlert(
                litSearchConfig: config,
                exLitSearchConfig: executedConfig,
                dateCreated: executedConfig?.dateCreated ?: (new Date()),
                lastUpdated: executedConfig?.lastUpdated ?: (new Date()),
                name: config.name,
                assignedTo: config?.assignedTo ? config.assignedTo : null,
                assignedToGroup: config?.assignedToGroup ? config.assignedToGroup : null,
                disposition: defaultDisposition,
                priority: config.priority,
                productSelection : config.productSelection ? getNameFieldFromJson(config.productSelection):getGroupNameFieldFromJson(config.productGroupSelection),
                eventSelection: config.eventSelection ? getNameFieldFromJson(config.eventSelection):getGroupNameFieldFromJson(config.eventGroupSelection),
                searchString: config.searchString ?: Constants.Commons.DASH_STRING,
                articleId: articleID ? articleID : Constants.Commons.DASH_STRING,
                articleTitle: alertData.ArticleTitle ? String.valueOf(alertData.ArticleTitle) : Constants.Commons.DASH_STRING,
                articleAbstract: articleAbstract ? String.valueOf(articleAbstract) : Constants.Commons.DASH_STRING,
                articleAuthors: alertData.Authors ? String.valueOf(alertData.Authors.collect { "${it.ForeName} ${it.LastName}" }.join(', ')) : Constants.Commons.DASH_STRING,
                publicationDate: publicationDate ?: Constants.Commons.DASH_STRING,
                citation: citation
        )
        literatureSearchAlert.globalIdentity = dataObjectService.getGlobalArticleList(executedConfig.id , Long.valueOf(articleID) )
        LiteratureHistory existingLiteratureHistory = existingLiteratureHistoryList.find {it.articleId == Long.valueOf(articleID) && it.isLatest}
        if(existingLiteratureHistory){
            literatureSearchAlert.disposition = cacheService.getDispositionByValue(existingLiteratureHistory.currentDispositionId)
            if (existingLiteratureHistory.currentAssignedTo) {
                literatureSearchAlert.assignedTo = cacheService.getUserByUserId(existingLiteratureHistory.currentAssignedToId)
            } else {
                literatureSearchAlert.assignedToGroup = cacheService.getGroupByGroupId(existingLiteratureHistory.currentAssignedToGroupId)
            }
            literatureSearchAlert.priority = cacheService.getPriorityByValue(existingLiteratureHistory.currentPriorityId)
        }
        literatureSearchAlert
    }

    @Transactional
    void batchPersistData(List<LiteratureAlert> alertList, ExecutedLiteratureConfiguration executedConfig, LiteratureConfiguration config) {
        def time1 = System.currentTimeMillis()
        log.info("Now persisting the execution related data in a batch.")

        //Persist the alerts
        batchPersistLiteratureAlert(alertList)

        log.info("Persistance of execution related data in a batch is done.")
        def time2 = System.currentTimeMillis()
        log.info(((time2 - time1) / 1000) + " Secs were taken in the persistance of data for configuration " + executedConfig.name + "id is : " + executedConfig.id)
    }
    /**
     * This function is used to format Article Abstract List in required format.
     * @param articleAbstractList
     * @return String
     */
    String formatAbstractData(List articleAbstractList){
        String articleAbstract =""
        try{
            if (articleAbstractList.size() > 1) {
                articleAbstractList.each {
                    articleAbstract += it.Label ? it.Label.toLowerCase().capitalize() + ": " : ""
                    articleAbstract += it.Text ?: ""
                    articleAbstract += "\n"

                }
            } else if (articleAbstractList.size() == 1) {
                articleAbstract += articleAbstractList[0].Label ? articleAbstractList[0].Label.toLowerCase().capitalize() + ": " : ""
                articleAbstract += articleAbstractList[0].Text

            }
            return articleAbstract
        }catch(Exception exception){
            log.info("Exception Occurred Article AbstractList.")
            return articleAbstractList.toString()
        }

    }

    @Transactional
    void batchPersistEmbaseData(List<EmbaseLiteratureAlert> alertList) {
        def time1 = System.currentTimeMillis()
        log.info("Now persisting the execution related data in a batch.")

        EmbaseLiteratureAlert.withTransaction {
            def batch = []
            for (EmbaseLiteratureAlert alert : alertList) {
                batch += alert
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (EmbaseLiteratureAlert alertIntance in batch) {
                        //Validate false is required to make sure that additional grails related check is not added to db.
                        alertIntance.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (EmbaseLiteratureAlert alertIntance in batch) {
                        //Validate false is required to make sure that additional grails related check is not added to db.
                        alertIntance.save(validate: true, failOnError: true)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("Alert data is batch persisted.")
        }

        log.info("Persistance of execution related data in a batch is done.")
        def time2 = System.currentTimeMillis()
        log.info(((time2 - time1) / 1000) + " Secs were taken in the persistance of data")
    }

    @Transactional
    def persistValidatedSignalWithLiteratureAlert(Long configId, Long exeConfigId, def domain) {
        List<BaseLiteratureAlert> attachSignalAlertList = domain == LiteratureAlert ?
                getAttachSignalAlertList(exeConfigId) :
                getAttachSignalEmbaseAlertList(exeConfigId)

        if (attachSignalAlertList) {
            List<Map<String, String>> alertIdAndSignalIdList = new ArrayList<>()
            log.info("Now saving the signal across the PE.")
            List<String> articleList = attachSignalAlertList.collect {
                it.articleId.toString()
            }
            Session session = sessionFactory.currentSession
            String sql_statement = domain == LiteratureAlert ?
                    SignalQueryHelper.signal_alert_ids_literature(articleList.join(","), exeConfigId, configId) :
                    SignalQueryHelper.signal_alert_ids_embase_literature(articleList.join(","), exeConfigId, configId)

            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list().each { row ->
                alertIdAndSignalIdList.add([col2: row[0].toString(), col1: row[1].toString(), col3: '1'])
            }

            alertIdAndSignalIdList = alertIdAndSignalIdList.unique {
                [it.col2, it.col1]
            }
            String insertValidatedQuery = domain == LiteratureAlert ?
                    "INSERT INTO VALIDATED_LITERATURE_ALERTS(VALIDATED_SIGNAL_ID,LITERATURE_ALERT_ID,IS_CARRY_FORWARD) VALUES(?,?,?)" :
                    "INSERT INTO VALIDATED_EMBASE_LITERATURE_ALERTS(VALIDATED_SIGNAL_ID,EMBASE_LITERATURE_ALERT_ID,IS_CARRY_FORWARD) VALUES(?,?,?)"

            session.doWork(new Work() {
                public void execute(Connection connection) throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedQuery)
                    def batchSize = Holders.config.signal.batch.size
                    int count = 0
                    try {
                        alertIdAndSignalIdList.each {
                            preparedStatement.setString(1, it.col1)
                            preparedStatement.setString(2, it.col2)
                            preparedStatement.setString(3, it.col3?:'0')
                            preparedStatement.addBatch()
                            count += 1
                            if (count == batchSize) {
                                preparedStatement.executeBatch()
                                count = 0
                            }
                        }
                        preparedStatement.executeBatch()
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        preparedStatement.close()
                        session.flush()
                        session.clear()
                    }
                }
            })
            log.info("Signal are saved across the system.")

        }

    }

    List<LiteratureAlert> getAttachSignalAlertList(Long exeConfigId) {
        List<LiteratureAlert> attachSignalAlertList = LiteratureAlert.createCriteria().list {
            eq("exLitSearchConfig.id", exeConfigId)
            'disposition' {
                eq("validatedConfirmed", true)
            }
            createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
            isNull('vs.id')
        } as List<LiteratureAlert>
        attachSignalAlertList
    }

    List<EmbaseLiteratureAlert> getAttachSignalEmbaseAlertList(Long exeConfigId) {
        List<EmbaseLiteratureAlert> attachSignalAlertList = EmbaseLiteratureAlert.createCriteria().list {
            eq("exLitSearchConfig.id", exeConfigId)
            'disposition' {
                eq("validatedConfirmed", true)
            }
            createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
            isNull('vs.id')
        } as List<EmbaseLiteratureAlert>
        attachSignalAlertList
    }

    void batchPersistLiteratureAlert(alertList) {

        LiteratureAlert.withTransaction {
            def batch = []
            for (LiteratureAlert alert : alertList) {
                batch += alert
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (LiteratureAlert alertIntance in batch) {
                        //Validate false is required to make sure that additional grails related check is not added to db.
                        alertIntance.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (LiteratureAlert alertIntance in batch) {
                        //Validate false is required to make sure that additional grails related check is not added to db.
                        alertIntance.save(validate: true, failOnError: true)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("Alert data is batch persisted.")
        }
    }

    private String getAuthorNames(int noOfAuthors, def authorsList) {
        String authorNames = " "

        noOfAuthors.times {
            def author = authorsList[it]
            authorNames += author.ForeName + " " + author.LastName
        }
        return authorNames
    }

    LiteratureConfiguration persistConfiguration(LiteratureConfiguration configurationInstance, Map params) {
        configurationInstance.owner = configurationInstance?.owner ?: userService.getUser()
        setStartAndEndDate(configurationInstance, params)
        //Set the workflow group from the logged in user.
        configurationInstance.workflowGroup = userService.getUser().workflowGroup
        configurationInstance = (LiteratureConfiguration) CRUDService.save(configurationInstance)
        return configurationInstance
    }

    private void setNextRunDateAndScheduleDateJSON(LiteratureConfiguration configurationInstance) {
        try {
            if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
                configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
            } else {
                configurationInstance.nextRunDate = null
            }
        }catch(Exception e){
            configurationInstance.scheduleDateJSON = null
        }
    }

    private void setStartAndEndDate(LiteratureConfiguration configurationInstance, Map params) {
        if (configurationInstance?.dateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {

            try {
                configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute = DateUtil.getEndDate(params?.dateRangeStartAbsolute, configurationInstance?.configSelectedTimeZone)
                configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute = DateUtil.getEndDate(params?.dateRangeEndAbsolute, configurationInstance?.configSelectedTimeZone)
            } catch (Exception e) {
                configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute = null
                configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute = null
            }
        } else {
            configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute = null
            configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute = null
        }
    }

    Map fetchIdListForLiteratureData(LiteratureConfiguration configurationInstance) {
        Map ret = [:]
        String url = Holders.config.app.literature.url
        String path = Holders.config.app.literature.id.uriPath
        String apiKey = Holders.config.app.literature.api.key
        String termValue = getTermForAPI(configurationInstance)
        Map dateRangeMap = getStartAndEndDateForLiterature(configurationInstance)
        //TODO : We have put a cap of result of 1 lacs in the result.
        Map query = [db: 'pubmed', term: termValue, mindate: dateRangeMap?.mindate, maxdate: dateRangeMap?.maxdate, datetype: 'pdat', retmode: 'JSON', retmax: 100000, usehistory: 'y']
        if (apiKey) {
            query.put("api_key", apiKey)
        }
        RESTClient endpoint = new RESTClient(url)
        if (Holders.config.literature.proxy.enabled) {
            log.info("Proxy is Enabled")
            endpoint.setProxy(Holders.config.literature.proxy.url, Holders.config.literature.proxy.port, Holders.config.literature.proxy.scheme)
        }
        endpoint.handler.failure = { resp -> ret = [status: resp.status] }
        log.info("Starting the first API call...")
        try {
            def resp = endpoint.get(
                    path: path,
                    query: query
            )
            log.info("Ending the first API call...")
            if (resp.status == 200) {
                ret = [status: resp.status, data: resp.data]
                log.debug("Response is: " + ret)
            }
            if (ret?.data?.esearchresult) {
                Map data = ret.data.esearchresult
                log.info("Number of records received by API call are :  ${data.idlist.size()}")
                return ["webEnv": data?.webenv, "queryKey": data?.querykey]
            }
        } catch (Throwable th) {
            log.error(th.printStackTrace())
        } finally {
            endpoint.shutdown()
        }
        return [:]
    }

    void fetchEmbaseLiteratureData(LiteratureConfiguration literatureConfiguration, String filePath) {
        Map xmlResponse = [:]
        String url = Holders.config.literature.embase.url
        String path = Holders.config.literature.embase.path

        String apiKey = Holders.config.literature.embase.apiKey
        String insttoken = Holders.config.literature.embase.insttoken
        String httpaccept = 'application/xml'

        String selectedProduct = getProductListForEmbaseLiterature(literatureConfiguration)
        String dateQuery = getStartAndEndDateForEmbaseLiterature(literatureConfiguration)
        String searchQuery = literatureConfiguration.searchString

        String embaseSearchQuery = buildFinalQuery(selectedProduct, dateQuery, searchQuery)

        initializeXmlFile(filePath)

        ExecutorService executor = Executors.newFixedThreadPool(3)
        List<Future<Void>> futures = []
        log.info("Embase query string: ${embaseSearchQuery}")
        try {
            Map query = [
                    apiKey     : apiKey,
                    insttoken  : insttoken,
                    httpaccept : httpaccept,
                    start      : 1,
                    count      : 1
            ]

            RESTClient endpoint = new RESTClient(url)
            def resp = endpoint.post(
                    path: path,
                    query: query,
                    requestContentType: URLENC,
                    body: [
                            query: embaseSearchQuery
                    ]
            )

            if (resp.status == 200) {
                xmlResponse = [status: resp.status, data: resp.data]
                int totalRecords = xmlResponse?.data?.header?.hits?.toInteger() ?: 0
                log.info("Total records found: ${totalRecords}")

                int count = 200

                int maxLimit = Math.min(totalRecords, 500000)
                for (int start = 1; start <= maxLimit; start += count) {
                    final int currentStart = start
                    futures << executor.submit({
                        RESTClient threadEndpoint = new RESTClient(url)
                        fetchAndSaveData(threadEndpoint, embaseSearchQuery, path, apiKey, insttoken, httpaccept, currentStart, count, filePath)
                    } as Callable<Void>)
                }

                for (Future<Void> future : futures) {
                    future.get()
                }
                log.info("All requests completed.")
            } else {
                log.error("Embase API returned a status of ${resp.status}")
                throw new Exception(Constants.EMBASE_API_CALL_FAILED_MESSAGE)
            }
        } catch (Throwable th) {
            log.error(th.printStackTrace())
            throw th
        } finally {
            closeXmlFile(filePath)
            executor.shutdown()
        }
    }

    private void fetchAndSaveData(RESTClient endpoint, String embaseSearchQuery, String path, String apiKey, String insttoken, String httpaccept, int start, int count, String filePath) {
        Map xmlResponse = [:]
        int maxRetries = 3
        int retryCount = 0
        int waitTime = 2000

        long requestStartTime = System.currentTimeMillis()

        while (retryCount < maxRetries) {
            try {
                Map query = [
                        apiKey     : apiKey,
                        insttoken  : insttoken,
                        httpaccept : httpaccept,
                        start      : start,
                        count      : count
                ]

                def resp = endpoint.post(
                        path: path,
                        query: query,
                        requestContentType: URLENC,
                        body: [
                                query: embaseSearchQuery
                        ]
                )

                if (resp.status == 200) {
                    xmlResponse = [status: resp.status, data: resp.data]
                    if (xmlResponse?.data?.results?.bibdataset?.bibrecord) {
                        List records = xmlResponse.data.results.bibdataset.bibrecord.collect()
                        if (!records.isEmpty()) {
                            appendEmbaseLiteratureDataToFile(records, filePath)
                        }
                    }
                    break
                } else if (resp.status == 429) {
                    retryCount++
                    log.warn("Received 429 status. Retrying... (${retryCount}/${maxRetries})")
                    if (retryCount < maxRetries) {
                        Thread.sleep(waitTime)
                    } else {
                        log.error("Max retries reached. Failed to fetch data for start ${start}.")
                        throw new Exception(Constants.EMBASE_API_CALL_FAILED_MESSAGE)
                    }
                } else {
                    log.error("Failed to fetch data for start ${start}: ${resp.status}")
                    throw new Exception(Constants.EMBASE_API_CALL_FAILED_MESSAGE)
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt()
                log.error("Request interrupted for start ${start}: ${ie.message}")
                throw new Exception(Constants.EMBASE_API_CALL_FAILED_MESSAGE)
            } catch (Exception e) {
                log.error("Error processing request for start ${start}: ${e.message}")
                throw e
            } finally {
                endpoint.shutdown()
                long requestEndTime = System.currentTimeMillis()
                log.info("Time taken for request (start: ${start}, count: ${count}): ${requestEndTime - requestStartTime} milliseconds")
            }
        }
    }

    void initializeXmlFile(String filePath) {
        File file = new File(filePath)

        BufferedWriter writer = null
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"))
            writer.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            writer.write('<recordsList>\n')
        } catch (IOException ioe) {
            log.error("IOException occurred while initializing XML file ${filePath}: ${ioe.message}", ioe)
            throw ioe
        } catch (Throwable throwable) {
            log.error("An unexpected error occurred while initializing XML file ${filePath}: ${throwable.message}", throwable)
            throw throwable
        } finally {
            try {
                if (writer != null) {
                    writer.close()
                }
            } catch (IOException ioe) {
                log.error("Error while closing writer for file ${filePath}: ${ioe.message}", ioe)
                throw ioe
            }
        }
    }

    synchronized void appendEmbaseLiteratureDataToFile(def alertData, String filePath) {
        File file = new File(filePath)

        BufferedWriter writer = null
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"))
            def xmlContent = new StreamingMarkupBuilder().bind {
                alertData.each { record ->
                    mkp.yield record
                }
            }

            writer.write(xmlContent.toString())
        } catch (IOException ioe) {
            log.error("IOException occurred while appending data to file ${filePath}: ${ioe.message}", ioe)
            throw ioe
        } catch (Throwable throwable) {
            log.error("An unexpected error occurred while appending data to file ${filePath}: ${throwable.message}", throwable)
            throw throwable
        } finally {
            try {
                if (writer != null) {
                    writer.close()
                }
            } catch (IOException ioe) {
                log.error("Error while closing writer for file ${filePath}: ${ioe.message}", ioe)
                throw ioe
            }
        }
    }

    void closeXmlFile(String filePath) {
        File file = new File(filePath)

        BufferedWriter writer = null
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"))
            writer.write("</recordsList>\n")
        } catch (IOException ioe) {
            log.error("IOException occurred while closing XML file ${filePath}: ${ioe.message}", ioe)
            throw ioe
        } catch (Throwable throwable) {
            log.error("An unexpected error occurred while closing XML file ${filePath}: ${throwable.message}", throwable)
            throw throwable
        } finally {
            try {
                if (writer != null) {
                    writer.close()
                }
            } catch (IOException ioe) {
                log.error("Error while closing writer for file ${filePath}: ${ioe.message}", ioe)
                throw ioe
            }
        }
    }

    def fetchDataForIds(Map data, Integer count=0) {
        String url = Holders.config.app.literature.url
        String uriPath = Holders.config.app.literature.data.uriPath
        String apiKey = Holders.config.app.literature.api.key

        Map query = [db: 'pubmed', WebEnv: data?.webEnv, query_key: data?.queryKey, retmode: 'xml']
        if (apiKey) {
            query.put("api_key", apiKey)
        }
        def ret = [:]

        HTTPBuilder http = new HTTPBuilder(url)
        if (Holders.config.literature.proxy.enabled) {
            log.info("Proxy is Enabled")
            http.setProxy(Holders.config.literature.proxy.url, Holders.config.literature.proxy.port, Holders.config.literature.proxy.scheme)
        }
        def result = null
        log.info("Starting the second API call...")
       try {
            http.request(Method.POST, ContentType.TEXT) {
                uri.path = uriPath
                uri.query = query
                response.success = { resp, reader ->
                    ret.status = resp.status
                    log.info("The status response is : " + resp.status)
                    result = reader.text
                }
                response.failure = { resp -> ret = [status: resp.status] }
            }
            log.info("Ending the second API call...")
            if (ret.status == 200) {
                return result
            }
            else {
                log.info("API CALL FAILED.")
                return Constants.API_CALL_FAILED_MESSAGE
            }
       } catch (ConnectionClosedException cce) {
           // trying to fetch data multiple times if response failed occasionally
           log.error("Connection Closed Exception catched for data ${data}")
           if (count < 5) {
               return fetchDataForIds(data, count + 1)
           }else{
               throw cce
           }
       } catch (Throwable th) {
            log.error(th.printStackTrace())
            throw th
        } finally {
           http.shutdown()
        }
        return null

    }

    def getParsedData(String result) {
        def alertData = null
        if (result) {
            XmlSlurper parser = new XmlSlurper()
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            log.info("Starting to parse XML Data")
            alertData = parser.parseText(result)
            log.info("Finished parsing XML Data")
        }
        return alertData
    }

    List<String> getProductList(LiteratureConfiguration config) {
        List<String> prdList
        if(config.productSelection){
            prdList = config.getProductNameList()
        } else {
            String prdName = config.getNameFieldFromJson(spotfireService.getJsonForGroup(config.getIdsForProductGroup()))
            if (prdName) {
                prdList = prdName.tokenize(',')
            }
        }
        return prdList
    }

    String getTermForAPI(LiteratureConfiguration config) {
        List result = []
        List<String> prdList = getProductList(config)
        prdList ? result.add("("+prdList.join(" OR ")+")") : ""
        List<String> eventList
        if(config.eventSelection) {
            eventList = config.getEventSelectionList()
        } else {
            String eventName = config.getNameFieldFromJson(spotfireService.getJsonForGroup(config.getIdsForEventGroup()))
            if (eventName) {
                eventList = eventName.tokenize(',')
            }
        }

        eventList ? result.add("("+eventList.join(" OR ")+")") : ""
        config.searchString ? result.add(config.searchString) : ""
        String term = result[0]
        for (int i = 1; i < result.size(); i++) {
            term += " AND ${result[i]}"
        }
        return term
    }

    Map fetchLiteratureSearchAlertResultMap(Map params, DataTableSearchRequest searchRequest, Long configId, Boolean isArchived = false, String datasource = 'pubmed') {
        Map resultMap = [:]
        Map filterMap = [:]
        def dispFiltersList = []
        searchRequest.searchParam.columns.each { column ->
            if (column.search.value) {
                String key = column.data
                String value = column.search.value
                filterMap.put(key, value)
            }
        }
        List<String> openDisposition = Disposition.findAllByClosedAndReviewCompleted(false, false).collect {
            it.displayName
        }
        def escapedFilters = null
        if (params.filters) {
            if(params.filters.startsWith("[") && params.filters != "[]"){
                def slurper = new JsonSlurper()
                escapedFilters = slurper.parseText(params.filters)
                if(escapedFilters) {
                    dispFiltersList = new ArrayList(escapedFilters)
                }
            } else {
                dispFiltersList = params.filters.split(",")
            }
        } else {
            dispFiltersList = openDisposition
        }

        Map orderColumnMap = [name: searchRequest.searchParam.orderBy(), dir:searchRequest?.searchParam?.orderDir()]

        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(datasource)

        resultMap.filters = filterList(configId, isArchived, isEmbase)
        if (!params.containsKey('filters') || params.filters) {
            if (isEmbase) {
                resultMap.resultList = literatureEmbaseAlertListAssignedToUserOrGroup(dispFiltersList, filterMap, orderColumnMap, searchRequest.pageSize(), searchRequest.searchParam.start, configId, params)
            } else {
                resultMap.resultList = literatureAlertListAssignedToUserOrGroup(dispFiltersList, filterMap, orderColumnMap, searchRequest.pageSize(), searchRequest.searchParam.start, configId, params)
            }
            resultMap.filteredCount = getLiteratureAlertFilteredCount(configId, dispFiltersList, filterMap, isArchived, isEmbase)
        } else {
            resultMap.resultList = []
            resultMap.filteredCount = 0
        }
        resultMap.resultCount = getLiteratureAlertTotalCount(configId, isArchived, isEmbase)
        resultMap
    }

    List<Map> literatureEmbaseAlertListAssignedToUserOrGroup(def dispFiltersList, Map filterMap, Map orderColumnMap, Integer max, Integer offset,Long configId, Map params) {
        def startTime = System.currentTimeMillis()
        def domainName
        Disposition defaultLitDisposition = userService.getUser().workflowGroup.defaultLitDisposition
        cacheService.setDefaultDisp(Constants.AlertType.LITERATURE, defaultLitDisposition.id as Long)
        boolean isDispFilters = dispFiltersList.size() > 0
        Map queryParameters = [configId: configId]
        if(params.isArchived == "true") {
            queryParameters.put("isLatest", false)
            domainName = ArchivedEmbaseLiteratureAlert
        }
        else {
            queryParameters.put("isLatest", true)
            domainName = EmbaseLiteratureAlert
        }

        if (isDispFilters) {
            queryParameters.put("dispList", dispFiltersList)
        }

        String literatureAlertHQL = prepareLiteratureAlertHQL(filterMap, orderColumnMap, isDispFilters, queryParameters, domainName)

        List<EmbaseLiteratureAlert> literatureSearchAlertList = domainName.executeQuery(literatureAlertHQL, queryParameters, [offset: offset, max: max])

        List<Map> literatureSearchAlertDTO = []
        List<Long> alertIdList = literatureSearchAlertList.collect { it.id }
        List<String> articleIdList = literatureSearchAlertList.collect { it.articleId as String}
        List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList, domainName)
        List<Map> alertTagNameList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList , Constants.AlertType.LITERATURE_ALERT)
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags(literatureSearchAlertList.collect{it.globalIdentity.id} , Constants.AlertType.LITERATURE_ALERT)
        List<Long> undoableAlertIdList =  undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertType.LITERATURE)
        List<AlertComment> alertCommentList = alertCommentService.getAlertCommentByArticleIdList(articleIdList)
        ExecutorService executorService = signalExecutorService.threadPoolForLitListExec()
        List<Future> futureList = literatureSearchAlertList.collect { def literatureAlert ->
            executorService.submit({ ->
                UndoableDisposition undoableDisposition = null
                List<Map> litAlertTags = alertTagNameList.findAll{it.alertId == literatureAlert.id}
                List<Map> globalTags = globalTagNameList.findAll{it.globalId == literatureAlert.globalIdentity.id }
                globalTags = globalTags.unique(false) { a, b ->
                    a.tagText <=> b.tagText
                }
                List<Map> allTags = litAlertTags + globalTags
                List<Map> tagNameList = allTags.sort{tag1 , tag2 -> tag1.priority <=> tag2.priority}

                List<Map> validatedSignals = alertValidatedSignalList.findAll {
                    it.id == literatureAlert.id
                }?.collect { [name: it.name + "(S)", signalId: it.signalId, disposition: it.disposition] }

                AlertComment commentObj = alertCommentList.find {
                    it.articleId == literatureAlert.articleId as String
                }
                String comment = commentObj?.comments ?: null
                Boolean isAttached = false
                Boolean isAttachedToCurrentAlert = literatureAlert.attachments as boolean
                if (isAttachedToCurrentAlert) {
                    isAttached = true
                }
                literatureAlert.toDto(tagNameList, validatedSignals, false, comment, isAttached, undoableAlertIdList.contains(literatureAlert.id)?:false, commentObj?.id)
            } as Callable)
        }
        futureList.each {
            literatureSearchAlertDTO.add(it.get())
        }
        cacheService.removeDefaultDisp(Constants.AlertType.LITERATURE)
        def endTime = System.currentTimeMillis()
        log.info("Got ${literatureSearchAlertDTO.size()} alerts in time: " + (endTime - startTime) / 1000 + " sec")
        literatureSearchAlertDTO
    }

    List<Map> literatureAlertListAssignedToUserOrGroup(def dispFiltersList, Map filterMap, Map orderColumnMap, Integer max, Integer offset,Long configId, Map params) {
        def startTime = System.currentTimeMillis()
        def domainName
        Disposition defaultLitDisposition = userService.getUser().workflowGroup.defaultLitDisposition
        cacheService.setDefaultDisp(Constants.AlertType.LITERATURE, defaultLitDisposition.id as Long)
        boolean isDispFilters = dispFiltersList.size() > 0
        Map queryParameters = [configId: configId]
        if(params.isArchived == "true") {
            queryParameters.put("isLatest", false)
            domainName = ArchivedLiteratureAlert
        }
        else {
            queryParameters.put("isLatest", true)
            domainName = LiteratureAlert
        }

        String literatureAlertHQL = prepareLiteratureAlertHQL(filterMap, orderColumnMap, isDispFilters, queryParameters, domainName)

        if (isDispFilters) {
            queryParameters.put("dispList", dispFiltersList)
        }
        List literatureSearchAlertList = domainName.executeQuery(literatureAlertHQL, queryParameters, [offset: offset, max: max])
        List<Map> literatureSearchAlertDTO = []
        List<Long> alertIdList = literatureSearchAlertList.collect { it.id }
        List<String> articleIdList = literatureSearchAlertList.collect { it.articleId as String}
        List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList, domainName)
        List<Map> alertTagNameList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList , Constants.AlertType.LITERATURE_ALERT)
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags(literatureSearchAlertList.collect{it.globalIdentity?.id} , Constants.AlertType.LITERATURE_ALERT)
        List<Long> undoableAlertIdList =  undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertType.LITERATURE)
        List<AlertComment> alertCommentList = alertCommentService.getAlertCommentByArticleIdList(articleIdList)
        ExecutorService executorService = signalExecutorService.threadPoolForLitListExec()
        List<Future> futureList = literatureSearchAlertList.collect { def literatureAlert ->
            executorService.submit({ ->
                UndoableDisposition undoableDisposition = null
                List<Map> litAlertTags = alertTagNameList.findAll{it.alertId == literatureAlert.id}
                List<Map> globalTags = globalTagNameList.findAll{it.globalId == literatureAlert.globalIdentityId }
                globalTags = globalTags.unique(false) { a, b ->
                    a.tagText <=> b.tagText
                }
                List<Map> allTags = litAlertTags + globalTags
                List<Map> tagNameList = allTags.sort{tag1 , tag2 -> tag1.priority <=> tag2.priority}

                List<Map> validatedSignals = alertValidatedSignalList.findAll {
                    it.id == literatureAlert.id
                }?.collect { [name: it.name + "(S)", signalId: it.signalId,disposition: it.disposition] }

                AlertComment commentObj = alertCommentList.find {
                    it.articleId == literatureAlert.articleId as String
                }
                String comment = commentObj?.comments ?: null
                Boolean isAttached = false
                Boolean isAttachedToCurrentAlert = literatureAlert.attachments as boolean
                if (isAttachedToCurrentAlert) {
                    isAttached = true
                }
                literatureAlert.toDto(tagNameList, validatedSignals,false, comment, isAttached, undoableAlertIdList.contains(literatureAlert.id)?:false,commentObj?.id)
            } as Callable)
        }
        futureList.each {
            literatureSearchAlertDTO.add(it.get())
        }
        cacheService.removeDefaultDisp(Constants.AlertType.LITERATURE)
        def endTime = System.currentTimeMillis()
        log.info("Got ${literatureSearchAlertDTO.size()} alerts in time: " + (endTime - startTime) / 1000 + " sec")
        literatureSearchAlertDTO
    }

    List<Long> getAlertIdsForAttachments(Long alertId, boolean isArchived = false, boolean isEmbase = false){
        def domain = getDomainObject(isArchived, isEmbase)
        List<Long> litAlertList = []
        List<Long> archivedAlertIds = []
        def litAlert
        if (isEmbase) {
            ArchivedEmbaseLiteratureAlert.withTransaction {
                litAlert = domain.findById(alertId.toInteger())
                archivedAlertIds = ExecutedLiteratureConfiguration.findAllByConfigId(litAlert.litSearchConfig.id).collect {
                    it.id
                }
                litAlertList = ArchivedEmbaseLiteratureAlert.createCriteria().list {
                    projections {
                        property('id')
                    }
                    eq('articleId', litAlert?.articleId)
                    if (archivedAlertIds) {
                        or {
                            archivedAlertIds.collate(1000).each {
                                'in'('exLitSearchConfig.id', it)
                            }
                        }
                    }
                } as List<Long>

                archivedAlertIds = litAlertList.findAll {
                    ArchivedEmbaseLiteratureAlert.get(it).exLitSearchConfig.id < litAlert.exLitSearchConfig.id
                }
            }
        } else {
            ArchivedLiteratureAlert.withTransaction {
                litAlert = domain.findById(alertId.toInteger())
                archivedAlertIds = ExecutedLiteratureConfiguration.findAllByConfigId(litAlert.litSearchConfig.id).collect {
                    it.id
                }
                litAlertList = ArchivedLiteratureAlert.createCriteria().list {
                    projections {
                        property('id')
                    }
                    eq('articleId', litAlert?.articleId)
                    if (archivedAlertIds) {
                        or {
                            archivedAlertIds.collate(1000).each {
                                'in'('exLitSearchConfig.id', it)
                            }
                        }
                    }
                } as List<Long>

                archivedAlertIds = litAlertList.findAll {
                    ArchivedLiteratureAlert.get(it).exLitSearchConfig.id < litAlert.exLitSearchConfig.id
                }
            }
        }
        archivedAlertIds + litAlert.id
    }

    boolean checkAttachmentsForAlert(List<Long> alertIds){
        boolean  isAttached = false
        ArchivedLiteratureAlert.withTransaction {
            alertIds.each { Long litAlertId ->
                def litAlert = ArchivedLiteratureAlert.get(litAlertId) ?: LiteratureAlert.get(litAlertId)
                if (litAlert.attachments)
                    isAttached = true
            }
        }
        isAttached
    }

    def fetchPreviousLiteratureAlerts(List prevExecConfigIdList){
        def prevAlertsLiteratureAlerts
        ArchivedLiteratureAlert.withTransaction {
            prevAlertsLiteratureAlerts = ArchivedLiteratureAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('id','id')
                    property('articleId','articleId')
                }
                if (prevExecConfigIdList){
                    or {
                        prevExecConfigIdList.collate(1000).each{
                            'in'('exLitSearchConfig.id', it)
                        }
                    }
                }
            } as List<Long>
        }
        return prevAlertsLiteratureAlerts
    }

    List<Map> filterList(Long configId, Boolean isArchived = false, Boolean isEmbase = false) {
        String baseDomainName = isEmbase ? "EmbaseLiteratureAlert" : "LiteratureAlert"
        String domainName = isArchived ? "Archived${baseDomainName}" : baseDomainName

        String dispositionListHQL = prepareDispositionHQL(domainName)
        List<Disposition> dispositionList = Disposition.executeQuery(dispositionListHQL, [configId: configId])
        List<Map> filterMap = dispositionList.collect {
            [value: it.displayName, closed: it.closed, isClosed: it.reviewCompleted]
        }
        filterMap

    }

    def prepareDispositionHQL(String domainName) {
        StringBuilder dispositionQuery = new StringBuilder()
        dispositionQuery.append("SELECT Distinct(disp) from Disposition disp, ${domainName}  lsa ")
        dispositionQuery.append("where disp = lsa.disposition and lsa.exLitSearchConfig.id = :configId")
        dispositionQuery.toString()

    }

    //Using HQL as we have to perform sorting on CLOB Column
    String prepareLiteratureAlertHQL(Map filterMap, Map orderColumnMap, boolean isDispFilters, Map queryParameters, def domainClass) {
        Long workflowGroupId = userService?.user?.workflowGroup?.id
        StringBuilder query = new StringBuilder()
        query.append(createBaseQuery(domainClass, workflowGroupId))

        filterMap.each { key, value ->
            query.append(" AND ").append(buildFilterCondition(key, value, queryParameters, domainClass))
        }

        if (isDispFilters) {
            query.append(" AND lsa.disposition.displayName in (:dispList) ")
        }

        query.append(buildOrderClause(orderColumnMap))

        return query.toString()
    }

    private String createBaseQuery(Class domainClass, Long workflowGroupId) {
        return """
        SELECT lsa FROM ${domainClass.simpleName} lsa
        LEFT OUTER JOIN lsa.assignedTo atUser
        LEFT OUTER JOIN lsa.assignedToGroup atGroup
        LEFT OUTER JOIN lsa.exLitSearchConfig elsc
        WHERE lsa.exLitSearchConfig.id = :configId
        AND elsc.isLatest = :isLatest
        AND elsc.isDeleted = false
        AND elsc.workflowGroup.id = ${workflowGroupId}
    """
    }

    private String buildFilterCondition(String key, Object value, Map queryParameters, def domainClass) {
        switch (key) {
            case 'alertName':
                return buildLikeCondition('lsa.name', value, queryParameters, 'name')
            case 'articleId':
                queryParameters.put('articleId', "%${value}%")
                return "str(lsa.articleId) like :articleId"
            case 'title':
                return buildLikeCondition('lsa.articleTitle', value, queryParameters, 'articleTitle')
            case 'authors':
                return buildLikeCondition('lsa.articleAuthors', value, queryParameters, 'articleAuthors')
            case 'publicationDate':
                queryParameters.put('publicationDate', "%${value.toLowerCase()}%")
                return "lower(lsa.publicationDate) like :publicationDate"
            case 'disposition':
                queryParameters.put('disposition', "%${value.toLowerCase()}%")
                return "lower(lsa.disposition.displayName) like :disposition"
            case 'assignedTo':
                queryParameters.put('userName', "%${value.toLowerCase()}%")
                return "(lower(atUser.fullName) like :userName OR lower(atGroup.name) like :userName)"
            case 'productName':
                return buildLikeCondition('lsa.productSelection', value, queryParameters, 'productSelection')
            case 'eventName':
                return buildLikeCondition('lsa.eventSelection', value, queryParameters, 'eventSelection')
            case 'signal':
                return buildSignalCondition(value, queryParameters, 'signal', domainClass)
            case 'affiliationOrganization':
                return buildLikeCondition('lsa.affiliationOrganization', value, queryParameters, 'affiliationOrganization')
            case 'articleAbstract':
                return buildLikeCondition('lsa.articleAbstract', value, queryParameters, 'articleAbstract')
            case 'sourceTitle':
                return buildLikeCondition('lsa.sourceTitle', value, queryParameters, 'sourceTitle')
            case 'issn':
                return buildLikeCondition('lsa.issn', value, queryParameters, 'issn')
            case 'sourceLink':
                return buildLikeCondition('lsa.sourceLink', value, queryParameters, 'sourceLink')
            case 'publicationYear':
                return buildLikeCondition('lsa.publicationYear', value, queryParameters, 'publicationYear')
            case 'chemicalName':
                return buildLikeCondition('lsa.chemicalName', value, queryParameters, 'chemicalName')
            case 'manufacturer':
                return buildLikeCondition('lsa.manufacturer', value, queryParameters, 'manufacturer')
            case 'tradeName':
                return buildLikeCondition('lsa.tradeName', value, queryParameters, 'tradeName')
            case 'tradeItemManufacturer':
                return buildLikeCondition('lsa.tradeItemManufacturer', value, queryParameters, 'tradeItemManufacturer')
            case 'externalSource':
                return buildLikeCondition('lsa.externalSource', value, queryParameters, 'externalSource')
            case 'publisherEmail':
                return buildLikeCondition('lsa.publisherEmail', value, queryParameters, 'publisherEmail')
            case 'enzymeCommissionNumber':
                return buildLikeCondition('lsa.enzymeCommissionNumber', value, queryParameters, 'enzymeCommissionNumber')
            case 'digitalObjectIdentifier':
                return buildLikeCondition('lsa.digitalObjectIdentifier', value, queryParameters, 'digitalObjectIdentifier')
            case 'citation':
                return buildLikeCondition('lsa.citation', value, queryParameters, 'citation')
            default:
                return ""
        }
    }

    private String buildLikeCondition(String field, String value, Map queryParameters, String paramName) {
        if (value?.contains('_')) {
            queryParameters.put(paramName, "%${EscapedILikeExpression.escapeString(value)}%")
            return "lower(${field}) like :${paramName} escape '!'"
        } else {
            queryParameters.put(paramName, "%${value.toLowerCase()}%")
            return "lower(${field}) like :${paramName}"
        }
    }

    private String buildSignalCondition(String value, Map queryParameters, String paramName, Class domainClass) {
        queryParameters.put(paramName, "%${EscapedILikeExpression.escapeString(value)}%")
        return """
        lsa.id in (
            select distinct(lsa1.id) 
            from ${domainClass.simpleName} lsa1 
            LEFT OUTER JOIN lsa1.validatedSignals signal 
            where lower(signal.name) like :${paramName} escape '!'
        )
    """
    }

    private String buildOrderClause(Map orderColumnMap) {
        StringBuilder orderClause = new StringBuilder(" ORDER BY ")

        switch (orderColumnMap.name) {
            case 'alertName':
                orderClause.append("lsa.name ${orderColumnMap.dir}")
                break
            case 'articleId':
                orderClause.append("str(lsa.articleId) ${orderColumnMap.dir}")
                break
            case 'title':
                orderClause.append("lsa.articleTitle ${orderColumnMap.dir}")
                break
            case 'authors':
                orderClause.append("lsa.articleAuthors ${orderColumnMap.dir}")
                break
            case 'publicationDate':
                orderClause.append(buildPublicationDateOrderClause(orderColumnMap.dir))
                break
            case 'disposition':
                orderClause.append("lsa.disposition.displayName ${orderColumnMap.dir}")
                break
            case 'assignedTo':
                orderClause.append("atUser.fullName ${orderColumnMap.dir}, atGroup.name ${orderColumnMap.dir}")
                break
            case 'productName':
                orderClause.append("lsa.productSelection ${orderColumnMap.dir}")
                break
            case 'eventName':
                orderClause.append("lsa.eventSelection ${orderColumnMap.dir}")
                break
            case 'actions':
                orderClause.append("lsa.actionCount ${orderColumnMap.dir}")
                break
            case 'affiliationOrganization':
                orderClause.append("lsa.affiliationOrganization ${orderColumnMap.dir}")
                break
            case 'sourceTitle':
                orderClause.append("lsa.sourceTitle ${orderColumnMap.dir}")
                break
            case 'issn':
                orderClause.append("lsa.issn ${orderColumnMap.dir}")
                break
            case 'sourceLink':
                orderClause.append("lsa.sourceLink ${orderColumnMap.dir}")
                break
            case 'publicationYear':
                orderClause.append("lsa.publicationYear ${orderColumnMap.dir}")
                break
            case 'chemicalName':
                orderClause.append("lsa.chemicalName ${orderColumnMap.dir}")
                break
            case 'casRegistryNumber':
                orderClause.append("lsa.casRegistryNumber ${orderColumnMap.dir}")
                break
            case 'manufacturer':
                orderClause.append("lsa.manufacturer ${orderColumnMap.dir}")
                break
            case 'tradeName':
                orderClause.append("lsa.tradeName ${orderColumnMap.dir}")
                break
            case 'tradeItemManufacturer':
                orderClause.append("lsa.tradeItemManufacturer ${orderColumnMap.dir}")
                break
            case 'externalSource':
                orderClause.append("lsa.externalSource ${orderColumnMap.dir}")
                break
            case 'publisherEmail':
                orderClause.append("lsa.publisherEmail ${orderColumnMap.dir}")
                break
            case 'enzymeCommissionNumber':
                orderClause.append("lsa.enzymeCommissionNumber ${orderColumnMap.dir}")
                break
            case 'digitalObjectIdentifier':
                orderClause.append("lsa.digitalObjectIdentifier ${orderColumnMap.dir}")
                break
            default:
                orderClause.append("lsa.lastUpdated desc")
                break
        }

        return orderClause.toString()
    }

    private String buildPublicationDateOrderClause(String direction) {
        return """
        CASE 
            WHEN lsa.publicationDate LIKE '___-____' 
                THEN TO_DATE('01-'||lsa.publicationDate, 'DD-Mon-YYYY') 
            WHEN lsa.publicationDate LIKE '____' 
                THEN TO_DATE('01-Dec-'||lsa.publicationDate, 'DD-Mon-YYYY') 
            WHEN lsa.publicationDate LIKE '-' 
                THEN TO_DATE('31-Dec-9999', 'DD-Mon-YYYY') 
            WHEN lsa.publicationDate LIKE '__-___-____' 
                THEN TO_DATE(lsa.publicationDate, 'DD-Mon-YYYY') 
        END ${direction}
    """
    }

    Integer getLiteratureAlertTotalCount(Long configId, Boolean isArchived = false, Boolean isEmbase = false) {
        Integer totalCount = 0
        Group workflowGroup = userService.getUser().workflowGroup
        def domain = getDomainObject(isArchived, isEmbase)
        totalCount = domain.createCriteria().count {
            eq('exLitSearchConfig.id', configId)
            'exLitSearchConfig' {
                eq('workflowGroup', workflowGroup)
            }
        }
        totalCount
    }

    Integer getLiteratureAlertFilteredCount(Long configId, def dispFilterList, Map filterMap, Boolean isArchived = false, Boolean isEmbase = false) {
        Integer filteredCount = 0
        Group workflowGroup = userService.getUser().workflowGroup
        def domain = getDomainObject(isArchived, isEmbase)
        filteredCount = domain.createCriteria().count {
            eq('exLitSearchConfig.id', configId)
            'exLitSearchConfig' {
                eq('workflowGroup', workflowGroup)
            }
            //If filter maps are coming then we prepare the filter map.
            and {
                filterMap.each { key, value ->
                    switch (key) {
                        case 'alertName':
                            ilike('name', '%' + value + '%')
                            break
                        case 'title':
                            ilike('articleTitle', '%' + value + '%')
                            break
                        case 'articleId':
                            sqlRestriction "cast( article_id AS char( 256 ) ) like '%${value}%'"
                            break
                        case 'authors':
                            ilike('articleAuthors', '%' + value + '%')
                            break
                        case 'publicationDate':
                            ilike('publicationDate', '%' + value + '%')
                            break
                        case 'disposition':
                            'disposition' {
                                ilike('displayName', '%' + value + '%')
                            }
                            break
                        case 'assignedTo':
                            createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                            createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                            or {
                                ilike('at.fullName', '%' + value + '%')
                                ilike('atg.name', '%' + value + '%')
                            }
                            break
                        case 'productName':
                            ilike('productSelection', '%' + value + '%')
                            break
                        case 'eventName':
                            ilike('eventSelection', '%' + value + '%')
                            break
                        case 'affiliationOrganization':
                            ilike('affiliationOrganization', '%' + value + '%')
                            break
                        case 'articleAbstract':
                            ilike('articleAbstract', '%' + value + '%')
                            break
                        case 'sourceTitle':
                            ilike('sourceTitle', '%' + value + '%')
                            break
                        case 'issn':
                            ilike('issn', '%' + value + '%')
                            break
                        case 'sourceLink':
                            ilike('sourceLink', '%' + value + '%')
                            break
                        case 'publicationYear':
                            ilike('publicationYear', '%' + value + '%')
                            break
                        case 'chemicalName':
                            ilike('chemicalName', '%' + value + '%')
                            break
                        case 'manufacturer':
                            ilike('manufacturer', '%' + value + '%')
                            break
                        case 'tradeName':
                            ilike('tradeName', '%' + value + '%')
                            break
                        case 'tradeItemManufacturer':
                            ilike('tradeItemManufacturer', '%' + value + '%')
                            break
                        case 'externalSource':
                            ilike('externalSource', '%' + value + '%')
                            break
                        case 'publisherEmail':
                            ilike('publisherEmail', '%' + value + '%')
                            break
                        case 'enzymeCommissionNumber':
                            ilike('enzymeCommissionNumber', '%' + value + '%')
                            break
                        case 'digitalObjectIdentifier':
                            ilike('digitalObjectIdentifier', '%' + value + '%')
                            break
                    }
                }
                if (dispFilterList.size() > 0) {
                    'disposition' {
                        'in'('displayName', dispFilterList)
                    }
                }
            }
        }
        filteredCount
    }

    //Fetching Configuration Object from ExecutedConfiguration Id
    LiteratureConfiguration getAlertConfigObject(Long executedConfigId) {
        LiteratureAlert.findByExLitSearchConfig(ExecutedLiteratureConfiguration.get(executedConfigId))?.litSearchConfig
    }

    def updateLiteratureSearchAlertStates(def alert, Map map, LiteratureConfiguration literatureConfiguration, Boolean isArchived = false, Boolean isEmbase = false) {
        def domain = getDomainObject(isArchived, isEmbase)
        domain.findAllByArticleIdAndLitSearchConfig(alert.articleId, literatureConfiguration).each {
            switch (map.change) {
                case Constants.HistoryType.DISPOSITION:
                    alert.disposition = map.disposition
                    break
                case Constants.HistoryType.ASSIGNED_TO:
                    alert.assignedTo = map.assignedTo
                    alert.assignedToGroup = map.assignedToGroup
                    break
                case Constants.HistoryType.PRIORITY:
                    alert.priority = map.priority
                    break
            }
            alert.save(flush: true)
        }
    }

    def sendMailOfAssignedToAction(List<User> oldUserList, List<User> newUserList, User currUser, def alert, Boolean isArchived = false, String newUserName) {
        String alertLink = createHref("LiteratureAlert", "details", [callingScreen: "review", configId: alert.exLitSearchConfig.id, isArchived: isArchived])
        //Send email to assigned User
        String newMessage = messageSource.getMessage('app.email.case.assignment.literature.message.newUser', null, Locale.default)
        String oldMessage = messageSource.getMessage('app.email.case.assignment.literature.message.oldUser', null, Locale.default)
        List emailDataList = userService.generateLiteratureEmailDataForAssignedToChange(newMessage, newUserList, oldMessage, oldUserList)

        Map<String, Object> alertData = [
                name                : alert.name,
                productSelection    : alert.productSelection,
                priority            : alert.priority,
                dispositionName     : alert.disposition?.displayName
        ]

        Map<String, Object> eventData = [
                alert        : alertData,
                emailDataList: emailDataList,
                alertLink    : alertLink,
                newUserName  : newUserName
        ]

        notify'literature.send.email.batch.published', eventData
    }

    @Transactional
    Map changeDisposition(String selectedRows, Disposition targetDisposition, String validatedSignalName,
                              String justification, Boolean isArchived,signalId, Boolean isEmbase = false) {
        ValidatedSignal validatedSignal;
        def domain = getDomainObject(isArchived, isEmbase)
        Boolean attachedSignalData = false
        User loggedInUser = userService.user
        List selectedRowsList = JSON.parse(selectedRows)
        boolean bulkUpdate = selectedRowsList.size() > 1
        Long execConfigId
        Integer reviewCounts = 0
        boolean isReviewCompleted = targetDisposition.reviewCompleted
        List<UndoableDisposition> undoableDispositionList =[]
        selectedRowsList.each { Map<String, Long> selectedRow ->
            def alert = domain.get(selectedRow["alert.id"])
            execConfigId = alert?.exLitSearchConfig.id
            if (alert) {
                if (!alert?.disposition?.isValidatedConfirmed()) {
                    Disposition previousDisposition = alert.disposition
                    Map dispDataMap = [objectId: alert.id, objectType: Constants.AlertType.LITERATURE, prevDispositionId: previousDisposition.id,
                                       currDispositionId: targetDisposition.id, prevDispPerformedBy: alert.dispPerformedBy]
                    UndoableDisposition undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)

                    undoableDispositionList.add(undoableDisposition)
                    alert.disposition = targetDisposition
                    alert.customAuditProperties = ["justification": justification]
                    alert.isDispChanged = true
                    alert.dispPerformedBy = loggedInUser.fullName
                    if(emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_LITERATURE)) {
                        emailNotificationService.mailHandlerForDispChangeLiterature(alert, previousDisposition, isArchived)
                    }
                    createActivityForDispositionChange(alert, previousDisposition, targetDisposition, justification, loggedInUser)
                    literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,alert,Constants.HistoryType.DISPOSITION,justification),isArchived)
                }
                else if(alert?.disposition.isValidatedConfirmed()){
                    if(justification){
                        justification = justification.replace('.', ' ') + "-- "+ customMessageService.getMessage("validatedObservation.justification.article", "${validatedSignalName}")
                    }
                    else
                        justification = customMessageService.getMessage("validatedObservation.justification.article", "${validatedSignalName}")
                    literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,alert,Constants.HistoryType.DISPOSITION,justification),isArchived)
                }
                if(isReviewCompleted) {
                    reviewCounts +=1
                }
                List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions;
                boolean isValidatedDate=false;
                if (validatedSignalName) {
                    Disposition defaultSignalDisposition = loggedInUser?.getWorkflowGroup()?.defaultSignalDisposition
                    String defaultValidatedDate=Holders.config.signal.defaultValidatedDate
                    isValidatedDate = validatedDateDispositions.contains(defaultSignalDisposition.value);
                    validatedSignal = validatedSignalService.attachAlertToSignal(validatedSignalName, alert.exLitSearchConfig.productSelection, alert, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT, defaultSignalDisposition, null, signalId, isValidatedDate)
                    boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
                        Integer dueIn
                        boolean dueInStartEnabled = Holders.config.dueInStart.enabled
                        String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
                        String previousDueDate=DateUtil.fromDateToString(validatedSignal.actualDueDate,DEFAULT_DATE_FORMAT)
                        if (enableSignalWorkflow) {
                            dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(validatedSignal.id, dueInStartPoint) : validatedSignalService.calculateDueIn(validatedSignal.id, validatedSignal.workflowState)
                        } else {
                            dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(validatedSignal.id, dueInStartPoint) : validatedSignalService.calculateDueIn(validatedSignal.id, defaultValidatedDate)
                        }
                    // Entry should only created when new signal is created
                        if (dueIn != null && SystemConfig.first().displayDueIn && signalId==null) {
                            validatedSignalService.saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                                                            signalId    : validatedSignal.id, "createdDate":null], true)
                        }
                    signalAuditLogService.createAuditLog([
                            entityName: "Signal: Literature Review Observations",
                            moduleName: "Signal: Literature Review Observations",
                            category: AuditTrail.Category.INSERT.toString(),
                            entityValue: "${validatedSignalName}: Article associated",
                            username: userService.getUser().username,
                            fullname: userService.getUser().fullName
                    ] as Map, [[propertyName: "Article associated", oldValue: "", newValue: "${alert.articleId}"]] as List)

                    attachedSignalData = true
                }
                alert.save()
            }
        }
        if(execConfigId && isReviewCompleted && !isArchived) {
            alertService.updateReviewCountsForLiterature(execConfigId, reviewCounts)
        }
        if(selectedRowsList.size() > 0 && !isArchived) {
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }
        Map signal=validatedSignalService.fetchSignalDataFromSignal(validatedSignal,null,null);
        [attachedSignalData: attachedSignalData,signal:signal]
    }
    def undoneLiteratureHistory(def literatureAlert) {
        log.info("Marking previous Literature history as Undone")
        ExecutedLiteratureConfiguration ec = ExecutedLiteratureConfiguration.get(literatureAlert.exLitSearchConfig.id as Long)
        LiteratureHistory literatureHistory = LiteratureHistory.createCriteria().get{
            eq('searchString', literatureAlert.searchString)
            eq('change', Constants.HistoryType.DISPOSITION)
            eq('articleId', literatureAlert.articleId as Long)
            eq('litExecConfigId', literatureAlert.exLitSearchConfig.id as Long)
            eq('litConfigId', literatureAlert.litSearchConfig.id as Long)
            order('lastUpdated', 'desc')
            maxResults(1)
        } as LiteratureHistory
        if (literatureHistory) {
            literatureHistory.isUndo = true
            CRUDService.save(literatureHistory)
            log.info("Successfully marked previous Literature History as Undone for literatureAlert alert: ${literatureAlert?.id}")
        }
    }

    @Transactional
    def revertDisposition(Long id, String justification, def domain) {
        log.info("Reverting Dispostion Started for Literature alert")
        Boolean dispositionReverted = false
        String oldDispName = ""
        String targetDisposition = ""
        def literatureAlert = domain.get(id)
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertType.LITERATURE)
            order('dateCreated', 'desc')
            maxResults(1)
        }

        if (literatureAlert && undoableDisposition?.isEnabled) {
            try {

                Disposition oldDisp = cacheService.getDispositionByValue(literatureAlert.disposition?.id)
                oldDispName = oldDisp?.displayName
                Disposition newDisposition = cacheService.getDispositionByValue(undoableDisposition.prevDispositionId)
                targetDisposition = newDisposition?.displayName

                Long execConfigId
                Integer reviewCounts = 0
                boolean isReviewCompleted = newDisposition.reviewCompleted

                if(isReviewCompleted) {
                    reviewCounts +=1
                }
                execConfigId = literatureAlert?.exLitSearchConfigId
                undoableDisposition.isUsed = true
                // saving state before undo for activity: 60067
                def prevUndoDisposition = literatureAlert.disposition
                def prevUndoDispPerformedBy = literatureAlert.dispPerformedBy

                literatureAlert.disposition = newDisposition
                literatureAlert.dispPerformedBy = undoableDisposition.prevDispPerformedBy
                literatureAlert.undoJustification = justification

                def activityMap = [
                        'Disposition': [
                                'previous': prevUndoDisposition ?: "",
                                'current': literatureAlert.disposition ?: ""
                        ],
                        'Performed By': [
                                'previous': prevUndoDispPerformedBy ?: "",
                                'current': literatureAlert.dispPerformedBy ?: ""
                        ]
                ]

                String activityChanges = activityMap.collect { k, v ->
                    def previous = v['previous'] ?: ""
                    def current = v['current'] ?: ""
                    if (previous != current) {
                        "$k changed from \'$previous\' to \'$current\'"
                    } else {
                        null
                    }
                }.findAll().join(', ')

                CRUDService.update(literatureAlert)
                CRUDService.update(undoableDisposition)

                UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: id, type: Constants.AlertType.LITERATURE])
                undoneLiteratureHistory(literatureAlert)
                literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,literatureAlert,Constants.HistoryType.UNDO_ACTION,justification,true),false)

                createActivityForUndoAction(literatureAlert, justification, activityChanges)
                if(execConfigId && isReviewCompleted ) {
                    alertService.updateReviewCountsForLiterature(execConfigId, reviewCounts)
                }
                dispositionReverted=true
                log.info("Dispostion reverted successfully for Literature alert Id: " + id)
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("some error occoured while reverting disposition")
            }
        }
        [attachedSignalData: null, incomingDisposition: oldDispName, targetDisposition: targetDisposition, dispositionReverted:dispositionReverted]
    }

    def createActivityForUndoAction(def literatureAlert, String justification, String activityChanges) {
        log.info("Creating Activity for reverting disposition")
        //ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.UndoAction.value)
        ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.UndoAction)
        String changeDetails = Constants.ChangeDetailsUndo.UNDO_DISPOSITION_CHANGE + " with " + activityChanges
        User loggedInUser = userService.user
        String productName = getNameFieldFromJson(literatureAlert.litSearchConfig.productSelection)
        String eventName = getNameFieldFromJson(literatureAlert.litSearchConfig.eventSelection)

        literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig,activityType, loggedInUser, changeDetails, justification,
                productName, eventName, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)

    }

    void createActivityForDispositionChange(def literatureAlert, Disposition previousDisposition, Disposition targetDisposition,
                                            String justification, User loggedInUser) {
        ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        String changeDetails  = "Disposition changed from '$previousDisposition' to '$targetDisposition'"
        String productName = getNameFieldFromJson(literatureAlert.litSearchConfig.productSelection)
        String eventName = getNameFieldFromJson(literatureAlert.litSearchConfig.eventSelection)

        literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig,activityType, loggedInUser, changeDetails, justification,
                productName, eventName, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)
    }

    List getLiteratureActivityList(Long configId) {

        List<Map> literatureActivityListMap = LiteratureActivity.createCriteria().list() {
            eq("executedConfiguration.id", configId)
        }.collect { LiteratureActivity literatureActivity ->
            literatureActivity.toDto()
        }
        return literatureActivityListMap
    }

    File getLiteratureActivityExportedFile(Map params) {
        List selectedActivities = []
        List<Map> literatureActivityList = getLiteratureActivityList(params.getLong("configId"))
        literatureActivityList.each {
            it.articleId = it.articleId + ""
            it['type'] = activityService.breakActivityType(it['type'] as String)
            if(it.justification){
                it.details = it.details + "-- with Justification '" + it.justification + "'"
            }
        }
        literatureActivityList = literatureActivityList?.sort {
            -it.activity_id
        }
        File reportFile = dynamicReportService.createLiteratureActivityReport(new JRMapCollectionDataSource(literatureActivityList), params)
        reportFile
    }

    def listSelectedAlerts(String alerts, def domainName) {
        String[] alertList = alerts.split(",")
        alertList.collect {
            domainName.findById(Integer.valueOf(it))
        }
    }

    List<Map> getExportedList(Map params, Boolean isEmbase = false) {
        def domain = getDomainObject(params.boolean('isArchived'), isEmbase)
        Disposition defaultLitDisposition = userService.getUser().workflowGroup.defaultLitDisposition
        cacheService.setDefaultDisp(Constants.AlertType.LITERATURE, defaultLitDisposition.id as Long)
        List literatureAlertList =[]
        if(params.selectedCases){
            literatureAlertList = listSelectedAlerts(params.selectedCases, domain)
        }else{
            List filters = []
            if (params.dispositionFilters) {
                filters = Eval.me(params.dispositionFilters)
            }
            if(filters) {
                literatureAlertList = domain.createCriteria().list() {
                    eq('exLitSearchConfig.id', params.long("configId"))
                    or {
                        'disposition' {
                            if (filters) {
                                    'in'('displayName', filters)
                            }
                        }
                    }
                    and {
                        params.each { k, v ->
                            switch (k) {
                                case 'alertName':
                                    ilike('name', '%' + v + '%')
                                    break
                                case 'title':
                                    ilike('articleTitle', '%' + v + '%')
                                    break
                                case 'articleId':
                                    sqlRestriction "cast(article_id AS char(256)) like '%${v}%'"
                                    break
                                case 'authors':
                                    ilike('articleAuthors', '%' + v + '%')
                                    break
                                case 'publicationDate':
                                    ilike('publicationDate', '%' + v + '%')
                                    break
                                case 'disposition':
                                    'disposition' {
                                        ilike('displayName', '%' + v + '%')
                                    }
                                    break
                                case 'assignedTo':
                                    createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                                    createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                                    or {
                                        ilike('at.fullName', '%' + v + '%')
                                        ilike('atg.name', '%' + v + '%')
                                    }
                                    break
                                case 'productName':
                                    ilike('productSelection', '%' + v + '%')
                                    break
                                case 'signal':
                                    createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                                    iLikeWithEscape('vs.name', "%${EscapedILikeExpression.escapeString(v)}%")
                                    break
                                case 'citation':
                                    ilike('citation', '%' + v + '%')
                                    break
                                case 'publicationYear':
                                    ilike('publicationDate', '%' + v + '%')
                                    break
                                case 'affiliationOrganization':
                                    ilike('affiliationOrganization', '%' + v + '%')
                                    break
                                case 'sourceTitle':
                                    ilike('sourceTitle', '%' + v + '%')
                                    break
                                case 'issn':
                                    ilike('issn', '%' + v + '%')
                                    break
                                case 'sourceLink':
                                    ilike('sourceLink', '%' + v + '%')
                                    break
                                case 'chemicalName':
                                    ilike('chemicalName', '%' + v + '%')
                                    break
                                case 'casRegistryNumber':
                                    ilike('casRegistryNumber', '%' + v + '%')
                                    break
                                case 'manufacturer':
                                    ilike('manufacturer', '%' + v + '%')
                                    break
                                case 'tradeName':
                                    ilike('tradeName', '%' + v + '%')
                                    break
                                case 'tradeItemManufacturer':
                                    ilike('tradeItemManufacturer', '%' + v + '%')
                                    break
                                case 'externalSource':
                                    ilike('externalSource', '%' + v + '%')
                                    break
                                case 'publisherEmail':
                                    ilike('publisherEmail', '%' + v + '%')
                                    break
                                case 'enzymeCommissionNumber':
                                    ilike('enzymeCommissionNumber', '%' + v + '%')
                                    break
                                case 'digitalObjectIdentifier':
                                    ilike('digitalObjectIdentifier', '%' + v + '%')
                                    break
                            }
                        }
                    }
                    //added by Amrendra Kumar, bug/PVS-4720 end

                } as List
            }
        }
        List<Long> alertIdList = literatureAlertList.collect{it.id}
        List<String> articleIdList = literatureAlertList.collect { it.articleId as String}
        List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList,domain)
        List<Map> alertTagNameList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList, Constants.AlertType.LITERATURE_ALERT) // updated code for PVS-55993
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags(literatureAlertList.collect{it.globalIdentityId}, Constants.AlertType.LITERATURE_ALERT)
        List<AlertComment> alertCommentList = alertCommentService.getAlertCommentByArticleIdList(articleIdList)
        List<Map> litActivityListMap = []
        ExecutorService executorService = signalExecutorService.threadPoolForLitListExec()
        List<Future> futureList = literatureAlertList.collect {def literatureAlert ->
            executorService.submit({ ->
                List<Map> litAlertTags = alertTagNameList.findAll{it.alertId == literatureAlert.id}
                List<Map> globalTags = globalTagNameList.findAll{it.globalId == literatureAlert.globalIdentityId }
                globalTags = globalTags.unique(false) { a, b ->
                    a.tagText <=> b.tagText
                }
                List<Map> allTags = litAlertTags + globalTags
                List<String> tagNameList = allTags.collect{tag->
                    if(tag.subTagText == null) {
                        tag.tagText + tag.privateUser + tag.tagType
                    }
                    else{
                        String subTags = tag.subTagText.split(";").join("(S);")
                        tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + " (S)"
                    }
                }
                List validatedSignals = alertValidatedSignalList.findAll {
                    it.id == literatureAlert.id
                }?.collect { [name: it.name + "(S)", signalId: it.signalId] }

                String comment = alertCommentList.find {
                    it.articleId == literatureAlert.articleId as String
                }?.comments ?: null

                literatureAlert.toDto(tagNameList,validatedSignals,true,comment)
            } as Callable)
        }
        futureList.each {
            litActivityListMap.add(it.get())
        }
        return litActivityListMap
    }

    /*
    The 'isFilterRequired' flag is introduced to address a scenario where inconsistencies occur.
    When a filter is applied on the alert details screen and the user navigates to the activities screen to export data,
    the URL parameters incorrectly retain the filter from the alert details screen, causing inconsistencies.
    */
    List getCriteriaList(Map params, ExecutedLiteratureConfiguration executedLiteratureConfiguration, Boolean isFilterRequired = false) {

        //the code is added to introduce the column level filter in the criteria sheet
        StringBuilder filterString = new StringBuilder()
        Map keyUIMapping = [
                "articleId"      : Constants.CriteriaSheetLabels.ARTICLE_ID,
                "disposition"    : Constants.CriteriaSheetLabels.CURRENT_DISPOSITION,
                "title"          : Constants.CriteriaSheetLabels.TITLE,
                "publicationDate": Constants.CriteriaSheetLabels.PUBLICATION_DATE,
                "authors"        : Constants.CriteriaSheetLabels.AUTHORS,
                "assignedTo"     : Constants.CriteriaSheetLabels.ASSIGNED_TO,
                "signal"         : Constants.CriteriaSheetLabels.SIGNAL,
                "productName"    : Constants.CriteriaSheetLabels.PRODUCT_NAME,
                "alertTags"      : Constants.CriteriaSheetLabels.CATEGORIES
        ]
        params.each { key, value ->
            if(keyUIMapping.containsKey(key))
                filterString.append("${keyUIMapping[key]} : $value\n")
        }

        String filterStringResult = filterString.toString()

        String timeZone = userService.getCurrentUserPreference()?.timeZone
        String dateRange = DateUtil.toDateString(executedLiteratureConfiguration?.dateRangeInformation.dateRangeStartAbsolute) +
                " - " + DateUtil.toDateString(executedLiteratureConfiguration?.dateRangeInformation.dateRangeEndAbsolute)
        String date_range_type = executedLiteratureConfiguration?.dateRangeInformation?.dateRangeEnum.toString()
        String alertName = executedLiteratureConfiguration?.name
        def literatureConfig = LiteratureConfiguration.get(executedLiteratureConfiguration?.configId)
        String productSelection = executedLiteratureConfiguration?.productSelection ? ViewHelper.getDictionaryValues(executedLiteratureConfiguration, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(executedLiteratureConfiguration, DictionaryTypeEnum.PRODUCT_GROUP)


        List criteriaSheetList = [
                ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': alertName ? alertName : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.PRIORITY, 'value': literatureConfig.priority?.displayName ?: ""],
                ['label': Constants.CriteriaSheetLabels.ASSIGNED_TO, 'value': executedLiteratureConfiguration.assignedTo?.name ?: executedLiteratureConfiguration.assignedToGroup?.name],
                ['label': 'Share With Users/Groups', 'value': getListOfShareWith(literatureConfig.shareWithUsers, literatureConfig.shareWithGroup)],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': productSelection ? productSelection : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.SEARCH_STRING, 'value': executedLiteratureConfiguration.searchString ?: ""],
                ['label': Constants.CriteriaSheetLabels.DATASOURCE, 'value': literatureConfig.selectedDatasource == 'pubmed' ? Constants.DataSource.PUB_MED : Constants.DataSource.EMBASE],
                ['label': Constants.CriteriaSheetLabels.DATE_RANGE, 'value': dateRange ? dateRange : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.ARTICLE_COUNT, 'value': params.totalCount ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.DATE_CREATED, 'value': DateUtil.stringFromDate(executedLiteratureConfiguration?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName ?: ""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))],
        ]
        if (filterStringResult && isFilterRequired) {
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.COLUMN_LEVEL_FILTER, 'value': (filterStringResult)])
        }
        return criteriaSheetList
    }

    Map getStartAndEndDateForLiterature(LiteratureConfiguration configurationInstance){
        Map dateRangeMap = [:]
        List result = []
        LiteratureDateRangeInformation dateRangeInformation = configurationInstance.dateRangeInformation
        result = dateRangeInformation.getReportStartAndEndDate()
        String DATE_FORMAT = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        if(result){
            dateRangeMap.put("mindate", sdf.format(result[0]))
            dateRangeMap.put("maxdate", sdf.format(result[1]))
        }
        return dateRangeMap
    }

    String getStartAndEndDateForEmbaseLiterature(LiteratureConfiguration configurationInstance) {
        String dateQuery = ""
        LiteratureDateRangeInformation dateRangeInformation = configurationInstance.dateRangeInformation
        List result = dateRangeInformation.getReportStartAndEndDate()
        String DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT)
        if(result && result.size() == 2){
            String startDate = sdf.format(result.get(0))
            String endDate = sdf.format(result.get(1))
            dateQuery = "([${startDate} to ${endDate}]/ld)"
        }
        return dateQuery
    }

    String getProductListForEmbaseLiterature(LiteratureConfiguration configurationInstance) {
        String productQuery = ""
        def productList = getProductList(configurationInstance)
        if (productList && productList.size() > 0) {
            productQuery = "(${productList.join(' or ')})"
        }
        return productQuery
    }

    String buildFinalQuery(String selectedProductQuery, String dateQuery, String searchQuery) {
        List<String> queryParts = []

        if (selectedProductQuery) {
            queryParts.add(selectedProductQuery)
        }
        if (dateQuery) {
            queryParts.add(dateQuery)
        }
        if (searchQuery) {
            queryParts.add(searchQuery)
        }

        return queryParts.join(' AND ')
    }

    Boolean isDateWithinRange(String dateString, Date startDate, Date endDate) {
        Date parsedDate = null
        List<String> dateFormats = [
                "yyyy",       // for '2023'
                "MM-yyyy",    // for '06-2023'
                "dd-MM-yyyy", // for '26-06-2023'
                "MMM-yyyy",   // for 'Jun-2023'
                "dd-MMM-yyyy" // for '26-Jun-2023'
        ]

        Calendar startCal = Calendar.getInstance()
        startCal.setTime(startDate)
        Calendar endCal = Calendar.getInstance()
        endCal.setTime(endDate)
        Calendar dateCal = Calendar.getInstance()

        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format)
                sdf.setLenient(false) // Set lenient to false to strictly parse dates
                parsedDate = sdf.parse(dateString)
                dateCal.setTime(parsedDate)

                if (format.equals("yyyy") && dateCal.get(Calendar.YEAR) >= startCal.get(Calendar.YEAR) && dateCal.get(Calendar.YEAR) <= endCal.get(Calendar.YEAR)) {
                    return true
                }

                if (format.equals("MM-yyyy") || format.equals("MMM-yyyy")) {
                    if (dateCal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR) && dateCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)) {
                        if (dateCal.get(Calendar.MONTH) >= startCal.get(Calendar.MONTH) && dateCal.get(Calendar.MONTH) <= endCal.get(Calendar.MONTH)) {
                            return true
                        }
                    }
                }

                if (format.equals("dd-MM-yyyy") || format.equals("dd-MMM-yy")) {
                    if (!parsedDate.before(startDate) && !parsedDate.after(endDate)) {
                        return true
                    }
                }

            } catch (Throwable th) {
            }
        }

        return false
    }


    /**
     * Retrieves the publication date from the given data map.
     *
     * @param data The data map containing current literature article details details.
     * @param monthMap A map translating month names to their corresponding numeric values.
     * @param dateRange A list of date ranges of literature executed configuration  to validate the publication date against.
     * @return The publication date as a String.
     */
    String getPublicationDate(Map data, Map monthMap, List<Date> dateRange) {
        Date startDate = dateRange[0]
        Date endDate = dateRange[1]
        // List of required date types to process fetched from PubMed XML
        List<String> requiredDates = [Constants.LiteratureFields.PUB_DATE, Constants.LiteratureFields.ARTICLE_DATE, Constants.LiteratureFields.MEDLINE_DATE]
        Map<String, String> factoredDatesMap = [:]

        // Processing each date type
        requiredDates.each { dateType ->
            String finalDate = ""
            String day = data."${dateType}Day" ? "${data."${dateType}Day"}".padLeft(2, '0') + "-" : ""
            String month = data."${dateType}Month" ?: ""
            if (month.length() == 1) {
                month = month.padLeft(2, '0')
            }
            month = month ? ((month.length() == 3) ? "${month}-" : "${monthMap.get(month)}-") : ""
            String year = data."${dateType}Year" ?: ""
            finalDate = day + month + year
            factoredDatesMap.put(dateType, finalDate)
        }
        return (factoredDatesMap.PubDate != "" && isDateWithinRange(factoredDatesMap.PubDate, startDate, endDate)) ? factoredDatesMap.PubDate :
                (factoredDatesMap.ArticleDate != "" && isDateWithinRange(factoredDatesMap.ArticleDate, startDate, endDate)) ? factoredDatesMap.ArticleDate : factoredDatesMap.MedlineDate
    }

    LiteratureConfiguration delete(LiteratureConfiguration configurationInstance) {
        configurationInstance.isDeleted = true
        configurationInstance.isEnabled = false
        List<ExecutedLiteratureConfiguration> executedConfigurationList = ExecutedLiteratureConfiguration.findAllByConfigId(configurationInstance.id)
        if(executedConfigurationList){
            executedConfigurationList.each {
                it.isDeleted = true
                it.save(flush:true)
            }
        }
        configurationInstance.save(flush: true)
        return configurationInstance
    }

    Closure saveLiteratureActivity = { AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived = false, Boolean isEmbase = false ->
        List<LiteratureActivity> activityList = []
        List<LiteratureHistory> literatureHistories = []
        List<LiteratureHistory> existingLiteratureHistories = []
        List<UndoableDisposition> undoableDispositionList = []
        alertLevelDispositionDTO.activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        List<Long> groupIds = alertLevelDispositionDTO.loggedInUser.groups.findAll { it.groupType == GroupType.USER_GROUP }*.id
        alertLevelDispositionDTO.assignedToGroup = groupIds
        alertLevelDispositionDTO.literatureHistories = getExistingLiteratureHistoryList(alertLevelDispositionDTO.alertList, alertLevelDispositionDTO.execConfigId)
        Map<Long, String> tagsNameMap = getTagsNameList(alertLevelDispositionDTO.execConfigId, alertLevelDispositionDTO.reviewCompletedDispIdList, isEmbase)
        //This will create pool of threads to be executed in future
        ExecutorService executorService = Executors.newFixedThreadPool(20)
        List<Future> futureList = alertLevelDispositionDTO.alertList.collect { alertMap ->
            executorService.submit({ ->
                UndoableDisposition undoableDisposition = null
                LiteratureActivity activity = alertService.createLiteratureActivityForBulkDisposition(alertMap, alertLevelDispositionDTO)
                Map literatureHistoryAndExistingLiteratureHistoryMap = createLiteratureAndExistingLiteratureHistory(alertMap, tagsNameMap, alertLevelDispositionDTO)
                if(!isArchived){
                    Map dispDataMap = [objectId: alertMap.id, objectType: Constants.AlertType.LITERATURE, prevDispositionId: alertMap.disposition.id,
                                       currDispositionId: alertLevelDispositionDTO.targetDisposition.id, prevDispPerformedBy: alertMap.dispPerformedBy]
                    undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)
                }
                [activity: activity, undoableDisposition: undoableDisposition?:null, literatureHistoryAndExistingLiteratureHistoryMap: literatureHistoryAndExistingLiteratureHistoryMap]
            } as Callable)
        }
        futureList.each {
            activityList.add(it.get()['activity'])
            literatureHistories.add(it.get()['literatureHistoryAndExistingLiteratureHistoryMap'].literatureHistories)
            undoableDispositionList.add(it.get()['undoableDisposition'])
            LiteratureHistory existingLiteratureHistory = it.get()['literatureHistoryAndExistingLiteratureHistoryMap'].existingLiteratureHistories
            if (existingLiteratureHistory) {
                existingLiteratureHistories.add(existingLiteratureHistory)
            }
        }
        executorService.shutdown()
        Map activityAndHistoryMap = [activityList           : activityList, literatureHistories: literatureHistories,
                                     existingLiteratureHistories: existingLiteratureHistories, id: alertLevelDispositionDTO.execConfigId]
        //To execute the given functionality in asynchronous environment
        notify 'literature.activity.event.published', activityAndHistoryMap
        undoableDispositionList.removeAll([null])
        if(undoableDispositionList.size()>0){
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }
    }

    Integer changeAlertLevelDisposition(AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchive = false, Boolean isEmbase = false) {
        alertService.changeLiteratureAlertLevelDisposition(saveLiteratureActivity, alertLevelDispositionDTO, isArchive, isEmbase)
    }

    void updateAutoRouteDisposition (def alertId, ResponseDTO responseDTO, Boolean isArchived = false) {
        def literatureAlert = isArchived ? ArchivedLiteratureAlert.get(alertId) : LiteratureAlert.get(alertId)
        if (literatureAlert) {
            Group workflowGroup = literatureAlert.exLitSearchConfig?.workflowGroup
            Boolean isAutoRouteDisposition = workflowGroup.autoRouteDisposition && (literatureAlert.disposition == workflowGroup.defaultLitDisposition)
            boolean isReviewCompleted = false
            if (isAutoRouteDisposition) {
                try {
                    isReviewCompleted = workflowGroup.autoRouteDisposition.reviewCompleted
                    Disposition previousDisposition = literatureAlert.disposition
                    literatureAlert.customAuditProperties = ["justification": workflowGroup.justificationText]
                    literatureAlert.disposition = workflowGroup.autoRouteDisposition
                    log.info("Update Auto Route Disposition from ${previousDisposition?.displayName} to ${literatureAlert.disposition?.displayName}")
                    String justification = workflowGroup.forceJustification ? workflowGroup.justificationText : ""
                    CRUDService.save(literatureAlert)
                    if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.DISPOSITION_AUTO_ROUTE_LA)) {
                        emailNotificationService.mailHandlerForAutoRouteDispLA(literatureAlert, previousDisposition)
                    }
                    createActivityForDispositionChange(literatureAlert, previousDisposition, literatureAlert.disposition, justification, userService.user)
                    literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,literatureAlert,Constants.HistoryType.DISPOSITION,justification),isArchived)
                    responseDTO.status = true
                } catch (Throwable th) {
                    log.error(th.getMessage(),th)
                    responseDTO.message = "Some exception occured while updating Auto Route Disposition"
                }
            }
            if(isReviewCompleted && !isArchived) {
                alertService.updateReviewCountsForLiterature(literatureAlert.exLitSearchConfigId, 1)
            }
        }
    }

    List<Long> fetchNewArticles(List<Long> articleIds) {
        List oldArticleIds = GlobalArticle.createCriteria().list {
            projections {
                property('articleId')
            }
            or {
                articleIds?.collate(1000).each {
                    'in'("articleId", it)
                }
            }
        }
        return articleIds - oldArticleIds

    }

    List fetchGlobalArticles(List<Long> articleIds) {
        List globalArticles = GlobalArticle.createCriteria().list {
            or {
                articleIds?.collate(1000).each {
                    'in'("articleId", it)
                }
            }
        }
        return globalArticles
    }

    List<Long> fetchArticlesFromData(def alertData) {
        List<Long> articleIds = new ArrayList()
        for (def data : alertData) {
            String articleID = data?.PMID
            articleID ? articleIds.add(Long.valueOf(articleID)) : null
        }

        return articleIds

    }

    @Transactional(propagation = Propagation.NEVER)
    List getFirstAndLastExecutionDate(LiteratureConfiguration configurationInstance, String timeZone, String firstExecutionDate, String lastExecutionDate) {
        List<ExecutedLiteratureConfiguration> executedConfiguration = ExecutedLiteratureConfiguration.findAllByConfigId(configurationInstance.id)
        if (executedConfiguration) {
            String firstExecutionStartDate
            String firstExecutionEndDate
            String lastExecutionStartDate
            String lastExecutionEndDate
            def length = executedConfiguration.size()
            def firstExecutionObject = executedConfiguration[0]
            getDateRangeExecutedLiteratureConfig(firstExecutionObject)
            def lastExecutionObject = executedConfiguration[length - 1]
            getDateRangeExecutedLiteratureConfig(lastExecutionObject)
            if (firstExecutionObject.dateRangeInformation.dateRangeStartAbsolute && firstExecutionObject.dateRangeInformation.dateRangeEndAbsolute) {
                firstExecutionStartDate = DateUtil.stringFromDate(firstExecutionObject.dateRangeInformation.dateRangeStartAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                firstExecutionEndDate = DateUtil.stringFromDate(firstExecutionObject.dateRangeInformation.dateRangeEndAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                firstExecutionDate = firstExecutionStartDate + "-" + firstExecutionEndDate
            }
            if (firstExecutionObject.dateRangeInformation.dateRangeStartAbsolute && firstExecutionObject.dateRangeInformation.dateRangeEndAbsolute) {
                lastExecutionStartDate = DateUtil.stringFromDate(lastExecutionObject.dateRangeInformation.dateRangeStartAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                lastExecutionEndDate = DateUtil.stringFromDate(lastExecutionObject.dateRangeInformation.dateRangeEndAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
                lastExecutionDate = lastExecutionStartDate + "-" + lastExecutionEndDate
            }
        }
        [firstExecutionDate, lastExecutionDate]
    }

    @Transactional(propagation = Propagation.NEVER)
    Map generateResultMap(Map resultMap, DataTableSearchRequest searchRequest,String selectedAlertsFilter) {

        List literatureAlerts = []
        Map configList = generateAlertReviewMaps(createAlertReviewDTO(selectedAlertsFilter), executedConfigForLiterTypeAlert, searchRequest)
        println configList
        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        resultMap.recordsTotal = configList?.totalCount
        if (configList?.configurationsList?.size() > 0) {
            configList.configurationsList?.each { ExecutedLiteratureConfiguration executedConfiguration ->

                String dateRange = getDateRangeExecutedLiteratureConfig(executedConfiguration)
                Map va = [
                        id                : executedConfiguration.id,
                        name              : executedConfiguration.name,
                        searchString      : executedConfiguration.searchString,
                        articleCount      : executedConfiguration.articleCount,
                        dateRange         : dateRange,
                        selectedDatasource: executedConfiguration?.selectedDatasource?.equalsIgnoreCase(Constants.DataSource.PUB_MED) ? Constants.DataSource.PUB_MED : Constants.LiteratureFields.EMBASE,
                        modifiedBy        : executedConfiguration.modifiedBy ? userService.getUserByUsername(executedConfiguration.modifiedBy)?.fullName : Constants.SYSTEM_USER,
                        dateCreated       : DateUtil.stringFromDate(executedConfiguration.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                        lastUpdated       : DateUtil.stringFromDate(executedConfiguration.lastUpdated, DateUtil.DATEPICKER_FORMAT, timeZone),
                        IsShareWithAccess : getUserService().hasAccessShareWith()
                ]
                literatureAlerts.add(va)
            }
            resultMap = [aaData: literatureAlerts as Set, recordsTotal: configList.totalCount, recordsFiltered: configList.filteredCount]
        }
        resultMap
    }

    @Transactional(propagation = Propagation.NEVER)
    AlertReviewDTO createAlertReviewDTO(String filterWithUsersAndGroups = "") {

        Long workflowGroupId = getUserService().getUser().workflowGroup.id
        AlertReviewDTO alertReviewDTO = new AlertReviewDTO()
        alertReviewDTO.workflowGrpId = workflowGroupId
        alertReviewDTO.shareWithConfigs = getUserService().getUserLitConfigurations()
        alertReviewDTO.filterWithUsersAndGroups = (filterWithUsersAndGroups == "null" || filterWithUsersAndGroups == "") ? [] : filterWithUsersAndGroups?.substring(1,filterWithUsersAndGroups.length()-1).replaceAll("\"", "").split(",");
        alertReviewDTO
    }

    Closure executedConfigForLiterTypeAlert = { AlertReviewDTO alertReviewDTO ,DataTableSearchRequest searchRequest->

        eq("isLatest", true)
        eq("isDeleted", false)
        eq("isEnabled", true)
        String searchString = searchRequest?.searchParam?.search?.value;
        String esc_char = ""
        Group workflowGroup = userService.getUser().workflowGroup
        User currentUser = userService.getUser()
        String groupIds = currentUser.groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}.collect { it.id }.join(",")
        if(StringUtils.isNotBlank(searchString)){
            searchString = searchString.toLowerCase()
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                or {sqlRestriction("""lower(name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    sqlRestriction("""lower(search_string) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    sqlRestriction("""lower(selected_data_source) like '%${searchString.replaceAll("'", "''")}%'""")
                }
            } else {
                or {sqlRestriction("""lower(name) like '%${searchString.replaceAll("'", "''")}%'""")
                    sqlRestriction("""lower(search_string) like '%${searchString.replaceAll("'", "''")}%'""")
                    sqlRestriction("""lower(selected_data_source) like '%${searchString.replaceAll("'", "''")}%'""")
                }
            }
        }
        sqlRestriction("""CONFIG_ID IN 
           (${SignalQueryHelper.literature_configuration_sql(getUserService().getCurrentUserId(), workflowGroup?.id,
                groupIds, alertReviewDTO.filterWithUsersAndGroups)}
           )""")
        if(StringUtils.isNotBlank(searchRequest?.orderBy())) {
            order(new Query.Order(searchRequest?.orderBy(),  Query.Order.Direction.valueOf(searchRequest?.searchParam?.orderDir().toUpperCase())).ignoreCase())
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    Map generateAlertReviewMaps(AlertReviewDTO alertReviewDTO, Closure executedConfigReviewClosure,DataTableSearchRequest searchRequest) {

        List<ExecutedLiteratureConfiguration> configurations = ExecutedLiteratureConfiguration.createCriteria().list(max: searchRequest.pageSize(), offset: searchRequest.searchParam.start) {
            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO,searchRequest)
        } as List<ExecutedLiteratureConfiguration>
        alertReviewDTO.filterWithUsersAndGroups = []
        Integer totalCount = ExecutedLiteratureConfiguration.createCriteria().count() {
            executedConfigReviewClosure.delegate = delegate
            executedConfigReviewClosure(alertReviewDTO,null)
        } as Integer
        Integer filteredCount = configurations?.totalCount
        [configurationsList: configurations, totalCount: totalCount, filteredCount: filteredCount]
    }

    @Transactional(propagation = Propagation.NEVER)
    private getDateRangeFromExecutedConfiguration(ExecutedLiteratureConfiguration c) {
        String dateRange = DateUtil.toDateString(c.dateRangeInformation.dateRangeStartAbsolute) + " to " +
                DateUtil.toDateString(c.dateRangeInformation.dateRangeEndAbsolute)
        return dateRange
    }

    @Transactional(propagation = Propagation.NEVER)
    boolean checkSearchCriteria(params) {
        if (params.searchString || params.productSelection || (params.productGroupSelection!="[]" && params.productGroupSelection)) {
            return true
        }
        return false
    }

    @Transactional(propagation = Propagation.NEVER)
    Map getShareWithUserAndGroup(String exeConfigId){
        ExecutedLiteratureConfiguration executedConfiguration = ExecutedLiteratureConfiguration.get(exeConfigId)
        if (executedConfiguration) {
            LiteratureConfiguration config = LiteratureConfiguration.findByName(executedConfiguration.name)
            List<Map> users = config.getShareWithUsers()?.collect{[id: Constants.USER_TOKEN + it.id, name: it.fullName]}
            List<Map> groups= config.getShareWithGroups()?.collect{[id: Constants.USER_GROUP_TOKEN + it.id, name: it.name]}
            return [users: users, groups: groups, all: users + groups]
        } else {
            log.info("Could not get the SharedWith User and Group as config id is not valid.")
        }
    }

    void editShareWith(def params) {
        if (params.sharedWith && params.executedConfigId) {
            ExecutedLiteratureConfiguration executedConfiguration = ExecutedLiteratureConfiguration.get(Long.parseLong(params.executedConfigId))
            LiteratureConfiguration config = LiteratureConfiguration.findByName(executedConfiguration.name)
            getUserService().bindSharedWithConfiguration(config, params.sharedWith, true)
            getCRUDService().update(config)
            getCRUDService().update(executedConfiguration)
        } else {
            log.info("Could not edit share with as execution config id is not valid")
        }
    }

    void setDateRange(LiteratureConfiguration configurationInstance, Map params) {
        LiteratureDateRangeInformation dateRangeInformation = new LiteratureDateRangeInformation()
        dateRangeInformation.dateRangeEnum = params.dateRangeEnum[0]
        if (dateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM) {
            if(params.dateRangeStartAbsolute != 'Invalid date' || params.dateRangeEndAbsolute != 'Invalid date'){
                dateRangeInformation.dateRangeStartAbsolute = new SimpleDateFormat("yyyy-MM-dd").parse(params.dateRangeStartAbsolute)
                dateRangeInformation.dateRangeEndAbsolute = new SimpleDateFormat("yyyy-MM-dd").parse( params.dateRangeEndAbsolute)
            }
            else{
                dateRangeInformation.dateRangeStartAbsolute = null
                dateRangeInformation.dateRangeEndAbsolute = null
            }
        }
        dateRangeInformation.relativeDateRangeValue = Integer.parseInt(params.relativeDateRangeValue)
        dateRangeInformation.literatureConfiguration = configurationInstance
        dateRangeInformation.save()
        configurationInstance.dateRangeInformation = dateRangeInformation
        configurationInstance.save(flush: true)
        configurationInstance.markDirty('dateRangeInformation')
        //Setting start and end date in case of last X Enum for audit scenario PVS-69127 this will be updated again at the time of execution also
        literatureExecutionService.getStartAndEndDateRangeForLiteratureAlert(configurationInstance)
    }

    void updateAlertDateRange(LiteratureDateRangeInformation dateRangeInformation, Map params) {
        String timezone = Holders.config.server.timezone
        String dateRangeEnum = params.("alertDateRangeInformation.dateRangeEnum")
        if (dateRangeEnum) {
            dateRangeInformation?.dateRangeEnum = dateRangeEnum
            String startDateAbsolute = params.("alertDateRangeInformation.dateRangeStartAbsolute")
            String endDateAbsolute = params.("alertDateRangeInformation.dateRangeEndAbsolute")
            if (dateRangeEnum == DateRangeEnum.CUSTOM.name()) {
                //check for blank values in custom date
                try {
                    dateRangeInformation.dateRangeStartAbsolute = DateUtil.stringToDate(startDateAbsolute, 'dd-MMM-yyyy', timezone)
                    dateRangeInformation.dateRangeEndAbsolute = DateUtil.stringToDate(endDateAbsolute, 'dd-MMM-yyyy', timezone)
                } catch (Exception e) {
                    dateRangeInformation?.dateRangeStartAbsolute = null
                    dateRangeInformation?.dateRangeEndAbsolute = null
                }
            } else {
                dateRangeInformation?.dateRangeStartAbsolute = null
                dateRangeInformation?.dateRangeEndAbsolute = null
            }
        }
    }

    void deleteLiteratureConfig(LiteratureConfiguration configurationInstance) {
        configurationInstance.isDeleted = true
        configurationInstance.isEnabled = false
        configurationInstance.save(flush: true)
    }

    Integer changeAlertLevelDisposition(Disposition targetDisposition, String justificationText, def domain, Long exConfigId, Boolean isArchive = false, Boolean isEmbase = false) {
        AlertLevelDispositionDTO alertLevelDispositionDTO = dispositionService.populateAlertLevelDispositionDTO(targetDisposition, justificationText, domain, null, exConfigId)
        alertLevelDispositionDTO.loggedInUser = userService.getUser()
        Integer updatedRowsCount = changeAlertLevelDisposition(alertLevelDispositionDTO, isArchive, isEmbase)
        return updatedRowsCount
    }

    void getExistingLiteratureHistoryList(List alertList, Long execConfigId) {
        List<Long> articleIds = alertList.collect { it.articleId as Long }
        List<LiteratureHistory> existingLiteratureHistories = LiteratureHistory.createCriteria().list {
            eq('litExecConfigId', execConfigId as Long)
            or {
                articleIds.collate(1000).each {
                    'in'("articleId", it)
                }
            }
            eq('isLatest', true)
        } as List<LiteratureHistory>
        existingLiteratureHistories
    }

    Map<Long, String> getTagsNameList(Long execConfigId, List<Long> reviewCompletedDispIdList, Boolean isEmbase = false) {
        Map<Long, String> tagsNameMap = [:]
        if (reviewCompletedDispIdList) {
            Session session = sessionFactory.currentSession
            String sql_statement
            if (isEmbase) {
                sql_statement = SignalQueryHelper.list_embase_literature_tag_name(execConfigId, reviewCompletedDispIdList)
            } else {
                sql_statement = SignalQueryHelper.list_literature_tag_name(execConfigId, reviewCompletedDispIdList)
            }
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list()?.each { row ->
                tagsNameMap.put(row[0], row[1])
            }
        }
        tagsNameMap
    }

    Map createLiteratureAndExistingLiteratureHistory(Map alertMap, Map<Long, String> tagsNameMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        Map literatureHistoryMap = literatureHistoryMap(alertMap, tagsNameMap, alertLevelDispositionDTO)
        LiteratureHistory existingCaseHistory = alertLevelDispositionDTO.literatureHistories.find {
            it.articleId == literatureHistoryMap.articleId && it.litExecConfigId == literatureHistoryMap.litExecConfigId
        }
        LiteratureHistory history = new LiteratureHistory(literatureHistoryMap)
        history = populateCaseHistory(history, existingCaseHistory, literatureHistoryMap, alertLevelDispositionDTO)
        [literatureHistories: history, existingLiteratureHistories: existingCaseHistory]
    }

    Map literatureHistoryMap(Map alertMap, Map<Long, String> tagsNameMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        Map literatureHistory = [
                "litConfigId"       : alertMap.litSearchConfig,
                "articleId"             : alertMap.articleId,
                "searchString"          : alertMap.searchString,
                "currentDisposition"    : alertLevelDispositionDTO.targetDisposition,
                "currentPriority"       : alertMap.priority,
                "currentAssignedTo"     : alertMap.assignedTo,
                "currentAssignedToGroup": alertMap.assignedToGroup,
                "justification"         : alertLevelDispositionDTO.justificationText,
                "litExecConfigId"       : alertLevelDispositionDTO.execConfigId,
                "change"                : Constants.HistoryType.DISPOSITION,
                "tagName"               : alertService.getAlertTagNames(alertMap.id, tagsNameMap),

        ]
        literatureHistory
    }

    LiteratureHistory populateCaseHistory(LiteratureHistory history, LiteratureHistory existingLiteratureHistory, Map literatureHistoryMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        if (existingLiteratureHistory) {
            if (literatureHistoryMap.change != Constants.Commons.BLANK_STRING) {
                history.properties = existingLiteratureHistory.properties
                history.litConfigId = literatureHistoryMap.litSearchConfig
                history.litExecConfigId = literatureHistoryMap.litExecConfigId
                if (literatureHistoryMap.change == Constants.HistoryType.DISPOSITION) {
                    history.currentDisposition = literatureHistoryMap.currentDisposition
                    history.change = literatureHistoryMap.change
                }
                existingLiteratureHistory.isLatest = false
            }
        }
        history.justification = literatureHistoryMap.justification
        history.isLatest = true
        history.createdBy = alertLevelDispositionDTO.loggedInUser.username
        history.modifiedBy = alertLevelDispositionDTO.loggedInUser.username
        history
    }

    def getDomainObject(Boolean isArchived, Boolean isEmbase = false) {
        def domain
        if (isEmbase) {
            domain = isArchived ? ArchivedEmbaseLiteratureAlert : EmbaseLiteratureAlert
        } else {
            domain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
        }
        domain
    }

    @Transactional(propagation = Propagation.NEVER)
    String getDateRangeExecutedLiteratureConfig(ExecutedLiteratureConfiguration exConfig) {

        if (!exConfig?.dateRangeInformation.dateRangeEndAbsolute && !exConfig?.dateRangeInformation.dateRangeStartAbsolute) {
            DateRangeEnum dateRange = exConfig.dateRangeInformation.dateRangeEnum
            int dateRangeValue = exConfig.dateRangeInformation.relativeDateRangeValue
            ZoneId zoneId = ZoneId.of(DateTimeZone.forID(Holders.config.server.timezone).ID)
            if (DateRangeEnum.YESTERDAY.equals(dateRange) || DateRangeEnum.LAST_X_DAYS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusDays(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            } else if (DateRangeEnum.LAST_WEEK.equals(dateRange) || DateRangeEnum.LAST_X_WEEKS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusWeeks(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            } else if (DateRangeEnum.LAST_MONTH.equals(dateRange) || DateRangeEnum.LAST_X_MONTHS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusMonths(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            } else if (DateRangeEnum.LAST_YEAR.equals(dateRange) || DateRangeEnum.LAST_X_YEARS.equals(dateRange)) {
                setStartAndEndDateInConfig(exConfig, Date.from(DateUtil.convertToLocalDateViaInstant(exConfig.dateCreated).minusYears(dateRangeValue).atStartOfDay(zoneId).toInstant()))
            }
        }
        getDateRangeFromExecutedConfiguration(exConfig)

    }

    void setStartAndEndDateInConfig(ExecutedLiteratureConfiguration exConfig, Date startDate) {
        use(TimeCategory) {
            exConfig.dateRangeInformation.dateRangeEndAbsolute = DateUtil.endOfDay(exConfig.dateCreated - 1)
        }
        exConfig.dateRangeInformation.dateRangeStartAbsolute = startDate
    }

    void dissociateLiteratureAlertFromSignal(def alert, Disposition targetDisposition, String justification, ValidatedSignal signal,
                                             Boolean isArchived) {
        Disposition previousDisposition = alert.disposition
        validatedSignalService.changeToInitialDisposition(alert, signal, targetDisposition)
        literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,alert,Constants.HistoryType.DISPOSITION,justification),isArchived)
        String changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition' and dissociated from signal '$signal.name'"
        Map attrs = [product: getNameFieldFromJson(alert.litSearchConfig.productSelection),
                     event  : getNameFieldFromJson(alert.litSearchConfig.eventSelection)]
        activityService.createLiteratureActivity(alert.exLitSearchConfig, ActivityType.findByValue(ActivityTypeValue.DispositionChange), userService.getUser(),
                changeDetails, justification, attrs, attrs.product, attrs.event, alert.assignedTo, alert.articleId, alert.assignedToGroup, alert.searchString)
    }

    /**
     * Method to change assignedTo value of LiteratureAlert(Activity and History are being asynchronously saved in DB)
     * @param literatureAlertList
     * @param assignedToValue
     * @param isArchived
     * @param bulkUpdate
     */
    void changeAssignedToGroup(List literatureAlertList, String assignedToValue, Boolean isArchived, boolean bulkUpdate) {
        User currUser = userService.getUser()
        List activityList = []
        List literatureHistoryList = []
        List exLiteratureHistoryList = []
        log.info("Changing assignedTo for Literature Alerts.")
        literatureAlertList.each { def literatureAlert ->
            String eventName = literatureAlert.eventSelection
            String oldUserName = userService.getAssignedToName(literatureAlert)
            List<User> oldUserList = userService.getUserListFromAssignToGroup(literatureAlert)
            literatureAlert = userService.assignGroupOrAssignTo(assignedToValue, literatureAlert)
            String newUserName = userService.getAssignedToName(literatureAlert)
            List<User> newUserList = userService.getUserListFromAssignToGroup(literatureAlert)
            literatureAlert.save(flush: true)

            if (emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.ASSIGNEE_UPDATE))
                sendMailOfAssignedToAction(oldUserList, newUserList, currUser, literatureAlert, isArchived, newUserName)

            addLiteratureActivityToList(literatureAlert, oldUserName, newUserName, eventName, currUser, activityList)
            def literatureHistoryMap = literatureHistoryService.getLiteratureHistoryMap(null, literatureAlert, Constants.HistoryType.ASSIGNED_TO, null)
            addLiteratureHistoryToList(literatureHistoryMap, isArchived, literatureHistoryList, exLiteratureHistoryList)
        }
        log.info("AssignedTo changed Successfully.")
        log.info("Activity and History for assignedTo change is being saved.")
        Map activityAndHistoryMap = [activityList               : activityList, literatureHistories: literatureHistoryList,
                                     existingLiteratureHistories: exLiteratureHistoryList]
        //this is an asynchronous call to a function in ConsumerService
        notify 'literature.activity.event.published', activityAndHistoryMap
    }

    /**
     * Method is used to create and add literatureHistory and existingLiteratureHistory to their respective lists.
     * @param literatureHistoryMap
     * @param isArchived
     * @param literatureHistoryList
     * @param exLiteratureHistoryList
     */
    void addLiteratureHistoryToList(Map literatureHistoryMap, Boolean isArchived, List literatureHistoryList, List exLiteratureHistoryList) {
        String change = literatureHistoryMap.change
        LiteratureHistory existingLiteratureHistory = literatureHistoryService.getLatestLiteratureHistory(literatureHistoryMap.articleId, literatureHistoryMap.litExecConfigId)
        LiteratureHistory literatureHistory = new LiteratureHistory(literatureHistoryMap)
        if (existingLiteratureHistory) {
            if (literatureHistory.change != Constants.Commons.BLANK_STRING) {
                literatureHistory.litConfigId = literatureHistoryMap.litConfigId
                literatureHistory.articleId = literatureHistoryMap.articleId
                literatureHistory.litExecConfigId = literatureHistoryMap.litExecConfigId
                literatureHistory.currentAssignedTo = literatureHistoryMap.currentAssignedTo
                literatureHistory.currentAssignedToGroup = literatureHistoryMap.currentAssignedToGroup
                literatureHistory.change = change
            }
            existingLiteratureHistory.isLatest = false
        }
        literatureHistory.justification = literatureHistoryMap.justification
        literatureHistory.isLatest = !isArchived
        literatureHistory.setLastUpdated(new Date())
        literatureHistoryList.add(literatureHistory)
        exLiteratureHistoryList.add(existingLiteratureHistory)
    }

    /**
     * Method is used to create and add literature activity to a list.
     * @param literatureAlert
     * @param oldUserName
     * @param newUserName
     * @param eventName
     * @param currUser
     * @param activityList
     */
    void addLiteratureActivityToList(def literatureAlert, String oldUserName, String newUserName, String eventName, User currUser, List activityList) {
        Map map = [config          : literatureAlert.exLitSearchConfig,
                   type            : getActivityByType(ActivityTypeValue.AssignedToChange),
                   details         : "Assigned To changed from '${oldUserName}' to '${newUserName}'",
                   justification   : null,
                   productSelection: literatureAlert.productSelection,
                   eventName       : eventName,
                   assignedTo      : literatureAlert.assignedTo,
                   searchString    : literatureAlert.searchString,
                   articleId       : literatureAlert.articleId,
                   assignedToGroup : literatureAlert.assignedToGroup]
        if (map.config) {
            LiteratureActivity literatureActivity = new LiteratureActivity(
                    type: map.type,
                    details: map.details,
                    timestamp: DateTime.now(),
                    justification: map.justification,
                    productName: map.productSelection,
                    eventName: map.eventName,
                    articleId: map.articleId,
                    searchString: map.searchString
            )
            if (currUser) {
                literatureActivity.performedBy = currUser
            }
            if (map.assignedTo) {
                literatureActivity.assignedTo = map.assignedTo
            } else if (map.assignedToGroup) {
                literatureActivity.assignedToGroup = map.assignedToGroup
            }
            literatureActivity.setExecutedConfiguration(map.config)
            activityList.add(literatureActivity)
        }
    }

    String prepareSearchStringForQuery(Long queryId) {
        SuperQueryDTO data = queryService.queryDetail(queryId)
        Map expressionObj = getExpressionObject(data.JSONQuery)  // Only call when necessary

        // Choose method based on query type
        if (data.queryType == QueryTypeEnum.QUERY_BUILDER) {
            return generateSearchStringForQueryBuilder(expressionObj)
        } else {
            return generateSearchStringForSetBuilder(expressionObj.keyword, data.queries)
        }
    }


    String generateSearchStringForQueryBuilder(Map expressionObj) {
        StringBuilder searchString = new StringBuilder()

        if (expressionObj.containsKey('keyword')) {
            searchString.append('(')
            expressionObj.expressions.eachWithIndex { expr, index ->
                if (index > 0) {
                    searchString.append(" ${expressionObj.keyword.toUpperCase()} ")
                }
                searchString.append(generateSearchStringForQueryBuilder(expr as Map))
            }
            searchString.append(')')
        } else {
            if (expressionObj.containsKey('expressions')) {
                expressionObj.expressions.eachWithIndex { expr, index ->
                    searchString.append(generateSearchStringForQueryBuilder(expr as Map))
                }
            } else {
                searchString.append(generateExpression(expressionObj))
            }
        }

        return searchString.toString()
    }


    String generateExpression(Map expression) {
        if (!expression) return ''

        String operator = expression.op == 'CONTAINS' ? ':' : (expression.op == 'EQUALS' ? '/' : '')
        return "'${expression.value}'${operator}${expression.field.replace('embase', '')}"
    }


    String generateSearchStringForSetBuilder(def keyword, List<QueryDTO> queries) {
        if (!queries) return ''

        StringBuilder searchString = new StringBuilder()
        String operator = getOperatorForSetBuilder(keyword)

        queries.eachWithIndex { QueryDTO queryDTO, index ->
            Map queryMap = getExpressionObject(queryDTO.JSONQuery)
            if (index > 0) {
                searchString.append(" ${operator} ")
            }
            searchString.append(generateSearchStringForQueryBuilder(queryMap))
        }

        return searchString.toString()
    }

    Map getExpressionObject(String jsonQuery) {
        if (!jsonQuery) return [:]

        Map object = new JsonSlurper().parseText(jsonQuery)

        if (object?.all?.containerGroups?.size() > 1 && object?.all?.keyword) {
            return [
                    expressions: [object.all.containerGroups[0], object.all.containerGroups[1]],
                    keyword    : object.all.keyword
            ]
        } else {
            return object.all.containerGroups[0] as Map
        }
    }


    String getOperatorForSetBuilder(def keyword) {
        return ['INTERSECT': 'AND', 'UNION': 'OR', 'MINUS': 'NOT'].get(keyword, 'AND')
    }


    private EmbaseLiteratureAlert createAlertFromRecord(XMLStreamReader reader, def monthMap) {
        EmbaseLiteratureAlert alert = new EmbaseLiteratureAlert()
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'head':
                        handleHead(reader, alert, monthMap)
                        break
                    case 'item-info':
                        handleItemInfo(reader, alert)
                        break
                    case 'explosions':
                        handleExplosions(reader, alert)
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'bibrecord') {
                break
            }
        }
        return alert
    }

    // record.head
    private void handleHead(XMLStreamReader reader, EmbaseLiteratureAlert alert, def monthMap) {
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'citation-title':
                        handleCitationTitle(reader, alert)
                        break
                    case 'author-group':
                        handleAuthorGroup(reader, alert)
                        break
                    case 'abstracts':
                        handleAbstracts(reader, alert)
                        break
                    case 'source':
                        handleSource(reader, alert, monthMap)
                        break
                    case 'enhancement':
                        handleEnhancement(reader, alert)
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'head') {
                break
            }
        }
    }

    // record.head.enhancement
    private void handleEnhancement(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'chemicalgroup':
                        handleChemicalGroup(reader, alert)
                        break
                    case 'manufacturergroup':
                        handleManufacturerGroup(reader, alert)
                        break
                    case 'tradenamegroup':
                        handleTradeNameGroup(reader, alert)
                        break
                    case 'descriptorgroup':
                        handleDescriptorGroup(reader, alert)
                        break
                    default:
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'enhancement') {
                break
            }
        }
    }

    // record.head.enhancement.tradenamegroup
    private void handleTradeNameGroup(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        def tradeNames = []
        def tradeItemManufacturers = []

        while (reader.hasNext()) {
            int event = reader.next()

            switch (event) {
                case XMLEvent.START_ELEMENT:
                    if (reader.localName == 'tradenames') {
                        processTradeNames(reader, tradeNames, tradeItemManufacturers)
                    }
                    break
                case XMLEvent.END_ELEMENT:
                    if (reader.localName == 'tradenamegroup') {
                        alert.tradeName = tradeNames.join('\n')
                        alert.tradeItemManufacturer = tradeItemManufacturers.join('\n')
                        return
                    }
                    break
            }
        }
    }

    // record.head.enhancement.tradenamegroup.tradenames
    private void processTradeNames(XMLStreamReader reader, List tradeNames, List tradeItemManufacturers) {
        while (reader.hasNext()) {
            int event = reader.next()

            switch (event) {
                case XMLEvent.START_ELEMENT:
                    if (reader.localName == 'trademanuitem') {
                        processTradeManuItem(reader, tradeNames, tradeItemManufacturers)
                    }
                    break
                case XMLEvent.END_ELEMENT:
                    if (reader.localName == 'tradenames') {
                        return
                    }
                    break
            }
        }
    }

    // record.head.enhancement.tradenamegroup.tradenames.trademanuitem
    private void processTradeManuItem(XMLStreamReader reader, List tradeNames, List tradeItemManufacturers) {
        while (reader.hasNext()) {
            int event = reader.next()

            switch (event) {
                case XMLEvent.START_ELEMENT:
                    if (reader.localName == 'tradename') {
                        tradeNames << extractTextWithTags(reader, 'tradename')
                    } else if (reader.localName == 'manufacturer') {
                        tradeItemManufacturers << processManufacturer(reader)
                    }
                    break
                case XMLEvent.END_ELEMENT:
                    if (reader.localName == 'trademanuitem') {
                        return
                    }
                    break
            }
        }
    }

    // record.head.enhancement.tradenamegroup.tradenames.trademanuitem.manufacturer
    private String processManufacturer(XMLStreamReader reader) {
        while (reader.hasNext()) {
            int event = reader.next()

            switch (event) {
                case XMLEvent.START_ELEMENT:
                    if (reader.localName == 'manufacturername') {
                        return extractTextWithTags(reader, 'manufacturername')
                    }
                    break
                case XMLEvent.END_ELEMENT:
                    if (reader.localName == 'manufacturer') {
                        return ''
                    }
                    break
            }
        }
        return ''
    }

    // record.head.enhancement.manufacturergroup
    private void handleManufacturerGroup(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        def manufacturersList = []

        while (reader.hasNext()) {
            int event = reader.next()

            switch (event) {
                case XMLEvent.START_ELEMENT:
                    if (reader.localName == 'manufacturers') {
                        processManufacturers(reader, manufacturersList)
                    }
                    break
                case XMLEvent.END_ELEMENT:
                    if (reader.localName == 'manufacturergroup') {
                        alert.manufacturer = manufacturersList.join('\n')
                        return
                    }
                    break
            }
        }
    }

    // record.head.enhancement.manufacturergroup.manufacturers
    private void processManufacturers(XMLStreamReader reader, List manufacturersList) {
        while (reader.hasNext()) {
            int event = reader.next()

            switch (event) {
                case XMLEvent.START_ELEMENT:
                    if (reader.localName == 'manufacturer') {
                        manufacturersList << extractManufacturerName(reader)
                    }
                    break
                case XMLEvent.END_ELEMENT:
                    if (reader.localName == 'manufacturers') {
                        return
                    }
                    break
            }
        }
    }

    // // record.head.enhancement.manufacturergroup.manufacturers.manufacturer
    private String extractManufacturerName(XMLStreamReader reader) {
        while (reader.hasNext()) {
            int event = reader.next()

            switch (event) {
                case XMLEvent.START_ELEMENT:
                    if (reader.localName == 'manufacturername') {
                        return extractTextWithTags(reader, 'manufacturername')
                    }
                    break
                case XMLEvent.END_ELEMENT:
                    if (reader.localName == 'manufacturer') {
                        return ''
                    }
                    break
            }
        }
        return ''
    }

    // record.head.enhancement.chemicalgroup
    private void handleChemicalGroup(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        def chemicalNames = []
        def casRegistryNumbers = []
        def enzymeCommissionNumbers = []

        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'chemicals':
                        processChemicals(reader, chemicalNames, casRegistryNumbers, enzymeCommissionNumbers)
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'chemicalgroup') {
                break
            }
        }

        alert.chemicalName = chemicalNames.join('\n')
        alert.casRegistryNumber = casRegistryNumbers.join('\n')
        alert.enzymeCommissionNumber = enzymeCommissionNumbers.join('\n')
    }

    // record.head.enhancement.chemicalgroup.chemicals
    private void processChemicals(XMLStreamReader reader, List chemicalNames, List casRegistryNumbers, List enzymeCommissionNumbers) {
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT && reader.localName == 'chemical') {
                processChemical(reader, chemicalNames, casRegistryNumbers, enzymeCommissionNumbers)
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'chemicals') {
                break
            }
        }
    }

    // record.head.enhancement.chemicalgroup.chemicals.chemical
    private void processChemical(XMLStreamReader reader, List chemicalNames, List casRegistryNumbers, List enzymeCommissionNumbers) {
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'chemical-name':
                        chemicalNames << extractTextWithTags(reader, 'chemical-name')
                        break
                    case 'cas-registry-number':
                        casRegistryNumbers << extractTextWithTags(reader, 'cas-registry-number')
                        break
                    case 'enzyme-commission-number':
                        enzymeCommissionNumbers << extractTextWithTags(reader, 'enzyme-commission-number')
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'chemical') {
                break
            }
        }
    }

    // record.head.'author-group'
    private void handleAuthorGroup(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        def authorList = []
        def organizationList = []

        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'author':
                        while (reader.hasNext()) {
                            event = reader.next()
                            if (event == XMLEvent.START_ELEMENT && reader.localName == 'autidx') {
                                authorList << extractTextWithTags(reader, 'autidx')
                            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'author') {
                                break
                            }
                        }
                        break
                    case 'affiliation':
                        while (reader.hasNext()) {
                            event = reader.next()
                            if (event == XMLEvent.START_ELEMENT && reader.localName == 'organization') {
                                organizationList << extractTextWithTags(reader, 'organization')
                            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'affiliation') {
                                break
                            }
                        }
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'author-group') {
                break
            }
        }

        alert.articleAuthors = (alert.articleAuthors ?: '') + authorList.join('\n') + '\n'
        alert.affiliationOrganization = (alert.affiliationOrganization ?: '') + organizationList.join('\n') + '\n'
    }

    // record.head.'citation-title'
    private void handleCitationTitle(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT && reader.localName == 'titletext') {
                while (reader.hasNext()) {
                    event = reader.next()

                    if (event == XMLEvent.START_ELEMENT && reader.localName == 'ttltext') {
                        alert.articleTitle = extractTextWithTags(reader, 'ttltext')
                    } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'titletext') {
                        break
                    }
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'citation-title') {
                break
            }
        }
    }

    // record.head.source
    private void handleSource(XMLStreamReader reader, EmbaseLiteratureAlert alert, def monthMap) {
        def issnNumbers = []

        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'sourcetitle':
                        alert.sourceTitle = extractTextWithTags(reader, 'sourcetitle')
                        break
                    case 'issn':
                        issnNumbers.addAll(handleIssn(reader))
                        break
                    case 'website':
                        alert.sourceLink = handleWebsite(reader)
                        break
                    case 'publisher':
                        alert.publisherEmail = handlePublisher(reader)
                        break
                    case 'publicationdate':
                        alert.publicationDate = handlePublicationDate(reader, monthMap)
                        break
                    case 'publicationyear':
                        alert.publicationYear = extractTextWithTags(reader, 'publicationyear')
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'source') {
                break
            }
        }

        alert.issn = issnNumbers.join('\n')
    }

    // record.head.source.issn
    private List<String> handleIssn(XMLStreamReader reader) {
        def issnNumbers = []
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT && reader.localName == 'issnnr') {
                issnNumbers << reader.getElementText()
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'issn') {
                break
            }
        }
        return issnNumbers
    }

    // record.head.source.website
    private String handleWebsite(XMLStreamReader reader) {
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT && reader.localName == 'email') {
                return extractTextWithTags(reader, 'email')
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'website') {
                break
            }
        }
        return null
    }

    // record.head.source.publisher
    private String handlePublisher(XMLStreamReader reader) {
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT && reader.localName == 'e-address') {
                return extractTextWithTags(reader, 'e-address')
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'publisher') {
                break
            }
        }
        return null
    }

    // record.head.source.publicationdate
    private String handlePublicationDate(XMLStreamReader reader, def monthMap) {
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT && reader.localName == 'pubdate') {
                return convertDateFormat(extractTextWithTags(reader, 'pubdate'), monthMap)
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'publicationdate') {
                break
            }
        }
        return null
    }

    // record.head.abstracts
    private void handleAbstracts(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        List<String> abstractParagraphs = []

        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT && reader.localName == 'abstract') {
                abstractParagraphs.addAll(handleAbstract(reader))
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'abstracts') {
                break
            }
        }

        alert.articleAbstract = abstractParagraphs.join('\n')
    }

    // record.head.abstracts.'abstract'
    private List<String> handleAbstract(XMLStreamReader reader) {
        List<String> paragraphs = []

        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT && reader.localName == 'para') {
                paragraphs << extractTextWithTags(reader, 'para')
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'abstract') {
                break
            }
        }

        return paragraphs
    }

    // record.head.enhancement.descriptorgroup
    private void handleDescriptorGroup(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        def descriptorGroups = []

        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT && reader.localName == 'descriptor') {
                def descriptorData = [
                        mainTerm           : '-',
                        weight             : '-',
                        drugTerm           : '-',
                        diseaseTerm        : '-',
                        deviceTerm         : '-',
                        descriptorLinkTerms: []
                ]
                handleDescriptorDetails(reader, descriptorData)
                descriptorGroups << descriptorData
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'descriptorgroup') {
                break
            }
        }

        alert.descriptorGroupsJson = JsonOutput.toJson(descriptorGroups)
    }

    // record.head.enhancement.descriptorgroup.descriptor
    private void handleDescriptorDetails(XMLStreamReader reader, Map descriptorData) {
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'mainterm':
                        descriptorData.mainTerm = extractTextWithTags(reader, 'mainterm')
                        break
                    case 'weight':
                        descriptorData.weight = extractTextWithTags(reader, 'weight')
                        break
                    case 'drgterm':
                        descriptorData.drugTerm = extractTextWithTags(reader, 'drgterm')
                        break
                    case 'disterm':
                        descriptorData.diseaseTerm = extractTextWithTags(reader, 'disterm')
                        break
                    case 'devterm':
                        descriptorData.deviceTerm = extractTextWithTags(reader, 'devterm')
                        break
                    case 'link':
                        def linkData = handleLink(reader)
                        descriptorData.descriptorLinkTerms << linkData
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'descriptor') {
                break
            }
        }
    }

    // record.explosions
    private void handleExplosions(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        def explosionGroups = []

        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT && reader.localName == 'explosion') {
                def explosionData = [
                        mainTerm          : '-',
                        weight            : '-',
                        drugTerm          : '-',
                        diseaseTerm       : '-',
                        deviceTerm        : '-',
                        ancestors         : '-',
                        explosionLinkTerms: []
                ]
                handleExplosionDetails(reader, explosionData)
                explosionGroups << explosionData
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'explosions') {
                break
            }
        }

        alert.explosionGroupsJson = JsonOutput.toJson(explosionGroups)
    }

    // record.explosions.explosion
    private void handleExplosionDetails(XMLStreamReader reader, Map explosionData) {
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'mainterm':
                        explosionData.mainTerm = extractTextWithTags(reader, 'mainterm')
                        break
                    case 'weight':
                        explosionData.weight = extractTextWithTags(reader, 'weight')
                        break
                    case 'drgterm':
                        explosionData.drugTerm = extractTextWithTags(reader, 'drgterm')
                        break
                    case 'disterm':
                        explosionData.diseaseTerm = extractTextWithTags(reader, 'disterm')
                        break
                    case 'devterm':
                        explosionData.deviceTerm = extractTextWithTags(reader, 'devterm')
                        break
                    case 'ancestors':
                        explosionData.ancestors = handleAncestors(reader)
                        break
                    case 'link':
                        def linkData = handleLink(reader)
                        explosionData.explosionLinkTerms << linkData
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'explosion') {
                break
            }
        }
    }

    // record.explosions.explosion.ancestors
    private String handleAncestors(XMLStreamReader reader) {
        def ancestors = []
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT && reader.localName == 'ancestor') {
                ancestors << extractTextWithTags(reader, 'ancestor')
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'ancestors') {
                break
            }
        }
        return ancestors.join(', ')
    }

    // record.head.enhancement.descriptorgroup.descriptor.link
    // record.explosions.explosion.link
    private Map handleLink(XMLStreamReader reader) {
        def linkData = [linkTerm: '-', subLinkTerms: '-']
        while (reader.hasNext()) {
            int event = reader.next()
            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'linkterm':
                        linkData.linkTerm = extractTextWithTags(reader, 'linkterm')
                        break
                    case 'sublinkterm':
                        def subLinkTerm = extractTextWithTags(reader, 'sublinkterm')
                        linkData.subLinkTerms = linkData.subLinkTerms == "-"
                                ? subLinkTerm
                                : linkData.subLinkTerms + ", " + subLinkTerm
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'link') {
                break
            }
        }
        return linkData
    }

    // record.'item-info'
    private void handleItemInfo(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'external-source':
                        alert.externalSource = extractTextWithTags(reader, 'external-source')
                        break
                    case 'itemidlist':
                        handleItemIdList(reader, alert)
                        break
                    default:
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'item-info') {
                break
            }
        }
    }

    // record.'item-info'.itemidlist
    private void handleItemIdList(XMLStreamReader reader, EmbaseLiteratureAlert alert) {
        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.START_ELEMENT) {
                switch (reader.localName) {
                    case 'doi':
                        alert.digitalObjectIdentifier = extractTextWithTags(reader, 'doi')
                        break
                    case 'pui':
                        alert.articleId = extractTextWithTags(reader, 'pui').toInteger()
                        break
                    default:
                        break
                }
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == 'itemidlist') {
                break
            }
        }
    }

    // Some values may have xml tags inside them, which may cause parsing to fail. To avoid this, we should read values with xml tags
    def extractTextWithTags(XMLStreamReader reader, String elementName) {
        StringBuilder contentBuilder = new StringBuilder()

        while (reader.hasNext()) {
            int event = reader.next()

            if (event == XMLEvent.CHARACTERS) {
                contentBuilder.append(reader.getText())
            } else if (event == XMLEvent.START_ELEMENT) {
                contentBuilder.append("<").append(reader.localName).append(">")
            } else if (event == XMLEvent.END_ELEMENT && reader.localName == elementName) {
                break
            } else if (event == XMLEvent.END_ELEMENT) {
                contentBuilder.append("</").append(reader.localName).append(">")
            }
        }

        return contentBuilder.toString()
    }

}
