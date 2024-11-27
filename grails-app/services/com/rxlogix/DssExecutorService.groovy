package com.rxlogix

import com.opencsv.CSVWriter
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.MasterChildRunNode
import com.rxlogix.config.MasterConfigStatus
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.Transaction
import org.springframework.transaction.annotation.Propagation

@Transactional
class DssExecutorService {

    def evdasSqlGenerationService
    def signalDataSourceService
    def statisticsService
    def hazelcastService
    def masterExecutorService
    def alertService
    def grailsApplication
    def sessionFactory
    def dataObjectService
    def evdasAlertExecutionService
    def dataSource_eudra
    def reportExecutorService

    List<Long> currentlyDssRunning = []

    int getDssExecutionQueueSize() {
        return currentlyDssRunning.size()
    }

    void clearDssExecutionQueue() {
        currentlyDssRunning = []
    }

    List<MasterConfigStatus> getNextMasterForDSS() {
        List<MasterConfigStatus> configs = MasterConfigStatus.createCriteria().list {
            eq("isMiningDone", true)
            eq("dssExecuting", false)
            eq("isDssDone", false)
            eq("nodeUuid", hazelcastService.hazelcastInstance.cluster.localMember.uuid)
            if (currentlyDssRunning) {
                not {
                    'in'('masterExecId', currentlyDssRunning)
                }
            }
            order('id', 'asc')
        }
        configs?configs:[]
    }

    void fetchAlertData(Long masterExecId, String db, Map flagMap, String uuid, String masterNode, Long faersMasterExId,
                        Long vaersMasterExId, Long vigibaseMasterExId) {

        currentlyDssRunning << masterExecId
        String alertType = "Aggregate Case Alert"

        try {
            Map result = [countsCompleted: false, ebgmCompleted: false, prrCompleted: false, dssCompleted: false]
            Integer tryCountInit = 1
            Integer tryCountMax = Holders.config.master.safety.timeout.minutes?:300
            Integer tryCountMaxFaers = Holders.config.master.faers.timeout.minutes?:600
            if(db == "faers" || db == "vigibase")
                tryCountMax = tryCountMaxFaers
            boolean dssFlag = db == "pva" && Holders.config.statistics.enable.dss
            boolean ebgmFlag = db == "pva" ? Holders.config.statistics.enable.ebgm : Holders.config.statistics."${db}".enable.ebgm
            boolean prrFlag = db == "pva" ? Holders.config.statistics.enable.prr : Holders.config.statistics."${db}".enable.prr
            flagMap = [dssFlag: dssFlag, ebgmFlag: ebgmFlag, prrFlag: prrFlag]
            Boolean countsInProgress = false
            Boolean ebgmInProgress = false
            Boolean prrInProgress = false
            Boolean dssInProgress = false


            if(!flagMap.dssFlag)
                result.dssCompleted = true
            if(!flagMap.ebgmFlag)
                result.ebgmCompleted = true
            if(!flagMap.prrFlag)
                result.prrCompleted = true

            // fetch child exec ids for node
            Map childAlertDetails = getChildAlert(masterExecId, uuid)
            List<Long> childAlerts = childAlertDetails.childAlerts
            List<Long> faersAlerts = childAlertDetails.faersAlerts
            List<Long> evdasAlerts = childAlertDetails.evdasAlerts
            List<Long> vaersAlerts = childAlertDetails.vaersAlerts
            List<Long> vigibaseAlerts = childAlertDetails.vigibaseAlerts

            if(childAlerts) {
                // update child alert executing
                updateChildRunStatus(childAlerts)
                Long childAlertExId = childAlerts[0]
                ExecutedConfiguration childExConfig = ExecutedConfiguration.read(childAlertExId)
                Configuration childConfig = Configuration.read(childExConfig.configId)
                boolean isEventGroup = childConfig?.eventGroupSelection && childConfig?.groupBySmq ? true : false

                if (evdasAlerts) {
                    Sql sql = null
                    Sql newSql
                    try {
                        sql = new Sql(signalDataSourceService.getReportConnection(Constants.DataSource.EUDRA))
                        newSql = new Sql(signalDataSourceService.getReportConnection('dataSource'))
                        Map evdasDataMap = [:]
                        evdasAlerts.each { evdasConfigurationId ->
                            EvdasConfiguration evdasConfiguration = EvdasConfiguration.get(evdasConfigurationId as Long)
                            String query = """
           SELECT
           e.id AS ID,
           e.PRODUCT_SELECTION as productSelection,
           e.PRODUCT_GROUP_SELECTION as productGroupSelection,
           e.EVENT_SELECTION as eventSelection,
           e.EVENT_GROUP_SELECTION as eventGroupSelection,
           e.NAME as name,
           e.SUPER_QUERY_ID as query,
           edr.DATE_RNG_ENUM as dateRangeEnum,
           edr.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
           edr.DATE_RNG_START_ABSOLUTE as dateRangeStart,
           p.locale as locale
           FROM
           EVDAS_CONFIG e
           JOIN EVDAS_DATE_RANGE edr ON e.DATE_RANGE_INFORMATION_ID = edr.ID
           JOIN PVUSER o ON e.owner_id = o.id
	       JOIN PREFERENCE p ON o.preference_id = p.id
	   WHERE E.ID = ${evdasConfigurationId}
        """
                            if (!evdasConfiguration) {
                                newSql.rows(query).collectEntries {
                                    evdasDataMap = [name: it.name, id: it.id, query: it.query, productSelection: it.productSelection, productGroupSelection: it.productGroupSelection, eventSelection: it.eventSelection, eventGroupSelection: it.eventGroupSelection, locale: it.locale, dateRangeEnum: it.dateRangeEnum, dateRangeEnd: it.dateRangeEnd, dateRangeStart: it.dateRangeStart]
                                }
                            }
                            log.info("This is the EVDAS configuration: ${evdasConfiguration}")
                            log.info("This is the EVDAS data map: ${evdasDataMap}")
                            String gttInserts = evdasSqlGenerationService.initializeInsertGtts(evdasConfiguration, [], null, evdasDataMap)
                            log.info(gttInserts)
                            sql.execute(gttInserts)
                            log.info("GTT Inserts executed.")
                            String queryInserts = evdasSqlGenerationService.initializeQuerySql(evdasConfiguration, evdasDataMap)
                            log.info(queryInserts)
                            sql.execute(queryInserts)
                            log.info("Query Inserts executed.")
                            String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}"
                            createDir(masterFilePath)
                            log.info(masterFilePath)
                            List evdasData = evdasAlertExecutionService.prepareAlertDataFromMart(sql, evdasConfiguration, false, evdasDataMap.dateRangeEnum)
                            String fileName = "${masterFilePath}/${evdasConfigurationId}_${alertType}"
                            fileName = fileName + "_eudra"
                            alertService.saveAlertDataInFile(evdasData, fileName)
                        }
                    } catch (Throwable throwable) {
                        log.error(alertService.exceptionString(throwable))
                        throw throwable
                    } finally {
                        sql?.close()
                        newSql?.close()
                    }
                }

                if(vaersAlerts) {
                    Map resultVaers = [countsCompleted: false, ebgmCompleted: false, prrCompleted: false, dssCompleted: false]
                    Integer tryCountInitVaers = 1
                    Integer tryCountMaxVaers2 = Holders.config.master.safety.timeout.minutes?:300
                    Boolean countsInProgressVaers = false
                    Boolean ebgmInProgressVaers = false
                    Boolean prrInProgressVaers = false
                    Boolean dssInProgressVaers = false
                    boolean dssFlagVaers = false
                    boolean ebgmFlagVaers = Holders.config.statistics.vaers.enable.ebgm
                    boolean prrFlagVaers =  Holders.config.statistics.vaers.enable.prr
                    Map flagMapVaers = [dssFlag: dssFlagVaers, ebgmFlag: ebgmFlagVaers, prrFlag: prrFlagVaers]


                    if(!flagMapVaers.dssFlag)
                        resultVaers.dssCompleted = true
                    if(!flagMapVaers.ebgmFlag)
                        resultVaers.ebgmCompleted = true
                    if(!flagMapVaers.prrFlag)
                        resultVaers.prrCompleted = true


                    while (!(resultVaers.countsCompleted == true && resultVaers.ebgmCompleted == true && resultVaers.prrCompleted == true && resultVaers.dssCompleted == true)) {

                        Thread.sleep(60000)
                        masterExecutorService.checkDbActivityStatus(vaersMasterExId, "vaers", resultVaers, flagMapVaers)

                        if (resultVaers.countsCompleted && countsInProgressVaers == false) {
                            countsInProgressVaers = true
                            // fetch alert count data
                            fetchAlertCountData(masterExecId, "vaers", vaersAlerts, alertType, isEventGroup, vaersMasterExId)
                        }

                        if (resultVaers.prrCompleted && prrInProgressVaers == false) {
                            prrInProgressVaers = true
                            // fetch prr data
                            if(flagMapVaers.prrFlag) {
                                Boolean runPrr = masterNode == hazelcastService.getName()
                                fetchAlertPrrData(masterExecId, "vaers", vaersAlerts, alertType, runPrr, vaersMasterExId)
                            }
                        }

                        if (resultVaers.ebgmCompleted && ebgmInProgressVaers == false) {
                            ebgmInProgressVaers = true
                            // calculate ebgm by calling stats api
                            if(flagMapVaers.ebgmFlag) {
                                log.info("Checking cases in DB")
                                boolean isCasesAvail = reportExecutorService.checkPECsInDB(vaersMasterExId, "vaers")
                                if (isCasesAvail) {
                                    calculateEbgmScore(masterExecId, "vaers", vaersAlerts, alertType, vaersMasterExId)
                                    calculateAbcdScore(masterExecId, "vaers", vaersAlerts, alertType, vaersMasterExId)
                                }
                            }
                        }

                        if (resultVaers.ebgmCompleted) {
                            dssInProgressVaers = true
                        }


                        if (tryCountInitVaers > tryCountMaxVaers2) {
                            throw new Exception(Holders.config.alertExecution.status.timeout.message)
                        }
                        tryCountInitVaers++

                    }

                }


                if(faersAlerts) {
                    Map resultFaers = [countsCompleted: false, ebgmCompleted: false, prrCompleted: false, dssCompleted: false]
                    Integer tryCountInitFaers = 1
                    Integer tryCountMaxFaers2 = Holders.config.master.faers.timeout.minutes?:600
                    Boolean countsInProgressFaers = false
                    Boolean ebgmInProgressFaers = false
                    Boolean prrInProgressFaers = false
                    Boolean dssInProgressFaers = false
                    boolean dssFlagFaers = false
                    boolean ebgmFlagFaers = Holders.config.statistics.faers.enable.ebgm
                    boolean prrFlagFaers =  Holders.config.statistics.faers.enable.prr
                    Map flagMapFaers = [dssFlag: dssFlagFaers, ebgmFlag: ebgmFlagFaers, prrFlag: prrFlagFaers]


                    if(!flagMapFaers.dssFlag)
                        resultFaers.dssCompleted = true
                    if(!flagMapFaers.ebgmFlag)
                        resultFaers.ebgmCompleted = true
                    if(!flagMapFaers.prrFlag)
                        resultFaers.prrCompleted = true


                    while (!(resultFaers.countsCompleted == true && resultFaers.ebgmCompleted == true && resultFaers.prrCompleted == true && resultFaers.dssCompleted == true)) {

                        Thread.sleep(60000)
                        masterExecutorService.checkDbActivityStatus(faersMasterExId, "faers", resultFaers, flagMapFaers)

                        if (resultFaers.countsCompleted && countsInProgressFaers == false) {
                            countsInProgressFaers = true
                            // fetch alert count data
                            fetchAlertCountData(masterExecId, "faers", faersAlerts, alertType, isEventGroup, faersMasterExId)
                        }

                        if (resultFaers.prrCompleted && prrInProgressFaers == false) {
                            prrInProgressFaers = true
                            // fetch prr data
                            if(flagMapFaers.prrFlag) {
                                Boolean runPrr = masterNode == hazelcastService.getName()
                                fetchAlertPrrData(masterExecId, "faers", faersAlerts, alertType, runPrr, faersMasterExId)
                            }
                        }

                        if (resultFaers.ebgmCompleted && ebgmInProgressFaers == false) {
                            ebgmInProgressFaers = true
                            // calculate ebgm by calling stats api
                            if(flagMapFaers.ebgmFlag) {
                                log.info("Checking cases in DB")
                                boolean isCasesAvail = reportExecutorService.checkPECsInDB(faersMasterExId, "faers")
                                if (isCasesAvail) {
                                    calculateEbgmScore(masterExecId, "faers", faersAlerts, alertType, faersMasterExId)
                                    calculateAbcdScore(masterExecId, "faers", faersAlerts, alertType, faersMasterExId)
                                }
                            }
                        }

                        if (resultFaers.ebgmCompleted) {
                            dssInProgressFaers = true
                        }


                        if (tryCountInitFaers > tryCountMaxFaers2) {
                            throw new Exception(Holders.config.alertExecution.status.timeout.message)
                        }
                        tryCountInitFaers++

                    }

                }

                if(vigibaseAlerts) {
                    Map resultVigibase = [countsCompleted: false, ebgmCompleted: false, prrCompleted: false, dssCompleted: false]
                    Integer tryCountInitVigibase = 1
                    Integer tryCountMaxVigibase2 = Holders.config.master.faers.timeout.minutes?:600
                    Boolean countsInProgressVigibase = false
                    Boolean ebgmInProgressVigibase = false
                    Boolean prrInProgressVigibase = false
                    Boolean dssInProgressVigibase = false
                    boolean dssFlagVigibase = false
                    boolean ebgmFlagVigibase = Holders.config.statistics.vigibase.enable.ebgm
                    boolean prrFlagVigibase =  Holders.config.statistics.vigibase.enable.prr
                    Map flagMapVigibase = [dssFlag: dssFlagVigibase, ebgmFlag: ebgmFlagVigibase, prrFlag: prrFlagVigibase]


                    if(!flagMapVigibase.dssFlag)
                        resultVigibase.dssCompleted = true
                    if(!flagMapVigibase.ebgmFlag)
                        resultVigibase.ebgmCompleted = true
                    if(!flagMapVigibase.prrFlag)
                        resultVigibase.prrCompleted = true


                    while (!(resultVigibase.countsCompleted == true && resultVigibase.ebgmCompleted == true && resultVigibase.prrCompleted == true && resultVigibase.dssCompleted == true)) {

                        Thread.sleep(60000)
                        masterExecutorService.checkDbActivityStatus(vigibaseMasterExId, "vigibase", resultVigibase, flagMapVigibase)

                        if (resultVigibase.countsCompleted && countsInProgressVigibase == false) {
                            countsInProgressVigibase = true
                            // fetch alert count data
                            fetchAlertCountData(masterExecId, "vigibase", vigibaseAlerts, alertType, isEventGroup, vigibaseMasterExId)
                        }

                        if (resultVigibase.prrCompleted && prrInProgressVigibase == false) {
                            prrInProgressVigibase = true
                            // fetch prr data
                            if(flagMapVigibase.prrFlag) {
                                Boolean runPrr = masterNode == hazelcastService.getName()
                                fetchAlertPrrData(masterExecId, "vigibase", vigibaseAlerts, alertType, runPrr, vigibaseMasterExId)
                            }
                        }

                        if (resultVigibase.ebgmCompleted && ebgmInProgressVigibase == false) {
                            ebgmInProgressVigibase = true
                            // calculate ebgm by calling stats api
                            if(flagMapVigibase.ebgmFlag) {
                                log.info("Checking cases in DB")
                                boolean isCasesAvail = reportExecutorService.checkPECsInDB(vigibaseMasterExId, "vigibase")
                                if (isCasesAvail) {
                                    calculateEbgmScore(masterExecId, "vigibase", vigibaseAlerts, alertType, vigibaseMasterExId)
                                    calculateAbcdScore(masterExecId, "vigibase", vigibaseAlerts, alertType, vigibaseMasterExId)
                                }
                            }
                        }

                        if (resultVigibase.ebgmCompleted) {
                            dssInProgressVigibase = true
                        }


                        if (tryCountInitVigibase > tryCountMaxVigibase2) {
                            throw new Exception(Holders.config.alertExecution.status.timeout.message)
                        }
                        tryCountInitVigibase++

                    }

                }

                while (!(result.countsCompleted == true && result.ebgmCompleted == true && result.prrCompleted == true && result.dssCompleted == true)) {
                    Thread.sleep(60000)
                    masterExecutorService.checkDbActivityStatus(masterExecId, db, result, flagMap)

                    log.info("dss status: ${db}: ${flagMap}: ${result} : ${masterExecId}")

                    if (result.countsCompleted && countsInProgress == false) {
                        countsInProgress = true
                        // fetch alert count data
                        fetchAlertCountData(masterExecId, db, childAlerts, alertType, isEventGroup, null)
                        masterExecutorService.updateMasterConfigStatus("is_count_done", [masterExecId], uuid)
                    }

                    if (result.prrCompleted && prrInProgress == false) {
                        prrInProgress = true
                            // fetch prr data
                            if(flagMap.prrFlag) {
                            Boolean runPrr = masterNode == hazelcastService.getName()
                            fetchAlertPrrData(masterExecId, db, childAlerts, alertType, runPrr, null)
                        }
                        masterExecutorService.updateMasterConfigStatus("is_prr_done", [masterExecId], uuid)
                    }

                    if (result.ebgmCompleted && ebgmInProgress == false) {
                        ebgmInProgress = true
                        // calculate ebgm by calling stats api
                        if(flagMap.ebgmFlag) {
                            log.info("Checking cases in DB")
                            boolean isCasesAvail = reportExecutorService.checkPECsInDB(masterExecId, db)
                            if (isCasesAvail) {
                                calculateEbgmScore(masterExecId, db, childAlerts, alertType, null)
                                calculateAbcdScore(masterExecId, db, childAlerts, alertType, null)
                            }
                        }
                        masterExecutorService.updateMasterConfigStatus("is_ebgm_done", [masterExecId], uuid)
                    }

                    if (result.ebgmCompleted && result.countsCompleted && result.prrCompleted &&
                            result.dssCompleted && dssInProgress == false) {
                        dssInProgress = true
                        // fetch ebgm data and calculate dss data by calling dss api
                        if(flagMap.dssFlag) {
                            try {
                                log.info("Checking cases in DB")
                                boolean isCasesAvail = reportExecutorService.checkPECsInDB(masterExecId, db)
                                if(isCasesAvail) {
                                    if(reportExecutorService.checkAggCountInDB(masterExecId, db))
                                    {
                                        calculateDssScore(masterExecId, db, childAlerts, alertType)
                                    }
                                } else {
                                    log.info("No ebgm stats..")
                                }
                            } catch (Exception ex) {
                                log.info(ex.message)
                                Exception exception = new Exception("DSS Failed!")
                                throw exception
                            }
                        }
                        masterExecutorService.updateMasterConfigStatus("is_dss_done", [masterExecId], uuid)
                    }


                    if (tryCountInit > tryCountMax) {
                        throw new Exception(Holders.config.alertExecution.status.timeout.message)
                    }
                    tryCountInit++

                }

                updateChildRunStatus(childAlerts, true)
            }

        } catch (Exception th) {
            log.error(alertService.exceptionString(th))
            masterExecutorService.updateMasterConfigStatus("is_db_error", [masterExecId], uuid, th.message)
            throw th
        } finally {
            currentlyDssRunning.remove(masterExecId)
        }



    }

    void fetchAlertCountData(Long masterExecId, String db, List childAlerts, String alertType, boolean isEventGroup = false, Long faersMasterExId) {
        String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}"
        createDir(masterFilePath)
        List alertData = []
        childAlerts.collate(25).each { configs ->
            alertData = masterExecutorService.fetchMasterCountData(configs, faersMasterExId?:masterExecId, db, isEventGroup)
            Map<String, List<Map>> alertsByConfig = alertData.groupBy {
                it["CHILD_EXECUTION_ID"]
            }
            alertData  = []
            alertsByConfig.each { k, v ->
                String fileName = "${masterFilePath}/${k}_${alertType}"
                fileName = fileName + "_${db}"
                alertService.saveAlertDataInFile(v, fileName)
                ExecutedConfiguration ec = ExecutedConfiguration.get(k as Long)
                if (Objects.nonNull(ec)) { // Added for change PVS-70336
                    reportExecutorService.populateCriteriaSheetCount(ec, db)
                    log.info("fetchAlertCountData ec.criteriaCounts : " + ec.criteriaCounts)
                } else {
		    log.info("ExecutedConfiguration ec is getting null so, cannot populate criteria sheet count for given configId : "+k)	
		}
            }
        }
    }

    void fetchAlertPrrData(Long masterExecId, String db, List childAlerts, String alertType, Boolean runPrr, Long faersMasterExId) {
        String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}"
        createDir(masterFilePath)
        String prrFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}/prr"
        String rorFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}/ror"
        createDir(prrFilePath)
        createDir(rorFilePath)
        List alertData = []
        List prrAlertData  = []
        List rorAlertData = []
        childAlerts.collate(25).each { configs ->
            if(db == Constants.DataSource.PVA){
                log.info("Checking PRR cases in DB")
                Boolean caseAvailPrr = reportExecutorService.checkPecsInDbPrrRor(masterExecId, db)
                if(caseAvailPrr){
                    prrAlertData = masterExecutorService.fetchMasterPrrData(configs, faersMasterExId ?: masterExecId, db)
                    Map<String, List<Map>> alertsByConfigPrr = prrAlertData.groupBy {
                        it["CHILD_EXECUTION_ID"]
                    }
                    prrAlertData = []
                    alertsByConfigPrr.each { k, v ->
                        String fileName = "${prrFilePath}/${k}_${alertType}"
                        fileName = fileName + "_${db}"
                        alertService.saveAlertDataInFile(v, fileName)
                    }
                    rorAlertData = masterExecutorService.fetchMasterRorData(configs, faersMasterExId ?: masterExecId, db)
                    Map<String, List<Map>> alertsByConfigRor = rorAlertData.groupBy {
                        it["CHILD_EXECUTION_ID"]
                    }
                    rorAlertData = []
                    alertsByConfigRor.each { k, v ->
                        String fileName = "${rorFilePath}/${k}_${alertType}"
                        fileName = fileName + "_${db}"
                        alertService.saveAlertDataInFile(v, fileName)
                    }
                }
            }else {
                log.info("calling PRR fetch method in DB")
                alertData = masterExecutorService.fetchMasterPrrDataForOtherDataSource(configs, faersMasterExId ?: masterExecId, db)
                Map<String, List<Map>> alertsByConfig = alertData.groupBy {
                    it["CHILD_EXECUTION_ID"]
                }
                alertData = []
                alertsByConfig.each { k, v ->
                    String fileName = "${prrFilePath}/${k}_${alertType}"
                    fileName = fileName + "_${db}"
                    alertService.saveAlertDataInFile(v, fileName)
                }
            }
            // persist alert data in separate files as ex config id under a folder as master ex id
        }
    }

    void calculateEbgmScore(Long masterExecId, String db, List childAlerts, String alertType, Long faersMasterId) {

        String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}"
        createDir(masterFilePath)
        String ebgmFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}/ebgm"
        createDir(ebgmFilePath)
        List alertData = []
        childAlerts.collate(25).each { configs ->
            alertData = masterExecutorService.fetchMasterEbgmData(configs, faersMasterId?:masterExecId, db)
            Map<String, List<Map>> alertsByConfig = alertData.groupBy {
                it["CHILD_EXECUTION_ID"]
            }
            alertData = []
            alertsByConfig.each { k, v ->
                String fileName = "${ebgmFilePath}/${k}_${alertType}"
                fileName = fileName + "_${db}"
                alertService.saveAlertDataInFile(v, fileName)
            }
            // persist alert data in separate files as ex config id under a folder as master ex id
        }


    }

    void calculateAbcdScore(Long masterExecId, String db, List childAlerts, String alertType, Long faersMasterId) {

        String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}"
        createDir(masterFilePath)
        String abcdFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}/abcd"
        createDir(abcdFilePath)
        List alertData = []
        childAlerts.collate(25).each { configs ->
            alertData = masterExecutorService.fetchMasterAbcdData(configs, faersMasterId?:masterExecId, db)
            Map<String, List<Map>> alertsByConfig = alertData.groupBy {
                it["CHILD_EXECUTION_ID"]
            }
            alertData = []
            alertsByConfig.each { k, v ->
                String fileName = "${abcdFilePath}/${k}_${alertType}"
                fileName = fileName + "_${db}"
                alertService.saveAlertDataInFile(v, fileName)
            }
            // persist alert data in separate files as ex config id under a folder as master ex id
        }


    }

    void calculateDssScore(Long masterExecId, String db, List childAlerts, String alertType) {
        String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}"
        createDir(masterFilePath)
        String dssFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}/dss"
        createDir(dssFilePath)
        List alertData = []
        childAlerts.collate(25).each { configs ->
            alertData = masterExecutorService.fetchMasterDssData(configs, masterExecId, db)

            Map<String, List<Map>> alertsByConfig = alertData.groupBy {
                it["CHILD_EXECUTION_ID"]
            }
            alertData = []
            alertsByConfig.each { k, v ->
                String fileName = "${dssFilePath}/${k}_${alertType}"
                fileName = fileName + "_${db}"
                toCSV1(v, fileName)
            }
            // persist alert data in separate files as ex config id under a folder as master ex id
        }
    }

    private static void toCSV(List<Map<String, Object>> list, String fileName) {
        List headers = ["PRODUCT_NAME","PT","DSS_SCORE","PEC_IMP_POS","PEC_IMP_NEG"]
        StringBuffer sb = new StringBuffer();

        for (Map<String, Object> map : list) {
            for (int i = 0; i < headers.size(); i++) {
                sb.append(map.get(headers.get(i)) ?: "0")
                sb.append(i == headers.size()-1 ? "\n" : "|");
            }
        }
        PrintWriter pw = new PrintWriter(fileName)
        pw.write(sb.toString())
        pw.flush()
        pw.close()
        sb = null
    }

    private static void toCSV1(List<Map<String, Object>> list, String fileName) {
        CSVWriter csvWriter
        try {
            csvWriter = new CSVWriter(new FileWriter(fileName), '|' as char, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, "\n")
            String []columns = ["PRODUCT_NAME", "PT", "DSS_SCORE", "PEC_IMP_POS", "PEC_IMP_NEG"].toArray()
            for (Map<String, Object> map : list) {
                csvWriter.writeNext((String[])[map["PRODUCT_NAME"]? map["PRODUCT_NAME"] as String: "0", map["PT"] ?map["PT"] as String: "0",
                                               map["DSS_SCORE"] ?map["DSS_SCORE"] as String: "0", map["PEC_IMP_POS"] ?map["PEC_IMP_POS"] as String: "0",
                                               map["PEC_IMP_NEG"] ?map["PEC_IMP_NEG"] as String: "0"].toArray())
            }

        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            csvWriter?.close()
        }

    }

    void createDir(String logsFilePath) {
        File logsFileDir = new File(logsFilePath)
        if (!logsFileDir.exists()) {
            logsFileDir.mkdir()
        }
    }

    Map getChildAlert(Long masterExecId, String uuid) {
        List<MasterChildRunNode> childAlerts = MasterChildRunNode.createCriteria().list {
            eq("masterExecId", masterExecId)
            eq("nodeUuid", uuid)
            eq("isExecuting", false)
        }
        [childAlerts: childAlerts*.childExecId, faersAlerts: childAlerts?.collect{it.faersId}?.findAll{it!=null}, evdasAlerts: childAlerts?.collect{it.evdasId}?.findAll{it!=null},
         vaersAlerts: childAlerts?.collect{it.vaersId}?.findAll{it!=null}, vigibaseAlerts: childAlerts?.collect{it.vigibaseId}?.findAll{it!=null}]
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateChildRunStatus(List childExecs, Boolean fileGenerated=false){
        Session session = sessionFactory.openSession()
        Transaction tx = session.beginTransaction()
        try {
            String updateQuery = "update master_child_run_node set "
            if(fileGenerated) {
                updateQuery = updateQuery + "file_generated=true"
            } else {
                updateQuery = updateQuery + "is_executing=true"
            }
            updateQuery = updateQuery + " where child_exec_id in (" + childExecs?.join(",") + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            session.flush()
            session.clear()
            tx.commit()
            session.close()
        }
    }

}
