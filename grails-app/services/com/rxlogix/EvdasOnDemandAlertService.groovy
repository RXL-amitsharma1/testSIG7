package com.rxlogix

import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.EvdasOnDemandAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.user.User
import com.rxlogix.util.AlertAsyncUtil
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import grails.async.PromiseList
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.sql.Sql
import org.apache.http.util.TextUtils
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Propagation

import java.text.SimpleDateFormat
import java.util.concurrent.*

import static grails.async.Promises.task
import static grails.async.Promises.waitAll

class EvdasOnDemandAlertService implements AlertUtil, LinkHelper, AlertAsyncUtil {

    static transactional = false

    EmailService emailService
    UserService userService
    SessionFactory sessionFactory
    private String userTimezone
    def alertService
    def dataSource
    def signalDataSourceService
    def emergingIssueService
    def aggregateCaseAlertService
    def CRUDService

    SimpleDateFormat dateWriteFormat = new SimpleDateFormat('dd-MMM-yyyy')

    void createOnDemandEvdasAlert(Long configId, Long executedConfigId, List<Map> alertData) {

        log.info("Data mining in PV Datahub gave " + alertData.size() + " PE combinations.")
        if (alertData) {
        try {
            EvdasConfiguration config = EvdasConfiguration.get(configId)
            ExecutedEvdasConfiguration executedConfig = ExecutedEvdasConfiguration.get(executedConfigId)
            List<Date> dateRangeStartEndDate = config.dateRangeInformation.getReportStartAndEndDate()
            Map selectedEvdasOnDemandAlertMap = setFlagsForAlert(alertData, dateRangeStartEndDate[1], executedConfig)

            Integer workerCnt = Holders.config.signal.worker.count as Integer
            List<EvdasOnDemandAlert> resultData = []
            ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
            List eiList = alertService.getEmergingIssueList()
            log.info("Thread Starts")
            List listednessData = []
            if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
                listednessData = aggregateCaseAlertService.evaluateListedness(executedConfig.id, null,false)
            }
            List<Future<EvdasOnDemandAlert>> futureList = alertData.collect { Map data ->
                executorService.submit({ ->
                    createOnDemandAlertParallely(data, config, executedConfig, selectedEvdasOnDemandAlertMap, eiList,listednessData)
                } as Callable)
            }
            futureList.each {
                resultData.add(it.get())
            }
            executorService.shutdown()
            log.info("Thread Ends")

            batchPersistData(resultData, executedConfig)
            alertService.updateOldExecutedConfigurations(config, executedConfig.id,ExecutedEvdasConfiguration)
            printExecutionMessage(config,executedConfig,resultData)
        } catch (Throwable ex) {
            throw ex
        }
    }
 }

    private setFlagsForAlert(List<Map> alertData, dateRangeEnd, executedConfig) {

        List previousOnDemandEvdasAlerts = EvdasOnDemandAlert.findAllByNameAndPeriodEndDateLessThan(executedConfig.name, dateRangeEnd,
                [order: "desc"])
        Map selectedEvdasOnDemandAlertMap = [:]

        for (Map data : alertData) {

            boolean isNew = true
            for (EvdasOnDemandAlert evdasOnDemandAlert : previousOnDemandEvdasAlerts) {

                if (evdasOnDemandAlert.substance == data.substance && evdasOnDemandAlert.ptCode == data.ptCode.toInteger()) {

                    isNew = false
                }
            }
            if (isNew) {

                selectedEvdasOnDemandAlertMap.put(data.substance + "-" + data.ptCode, "New")
            }
        }

        selectedEvdasOnDemandAlertMap
    }


    EvdasOnDemandAlert createOnDemandAlertParallely(Map data, EvdasConfiguration config,
                                                    ExecutedEvdasConfiguration executedConfig, Map selectedEvdasOnDemandAlertMap, List eiList, List listednessData = []) {

        String rorValue = "0.000"
        if (data.rorValue) {

            if (data.rorValue[0] == ".") {

                rorValue = "0" + data.rorValue
            } else {

                rorValue = data.rorValue
            }
        }

        String flags = selectedEvdasOnDemandAlertMap.get(data.substance + "-" + data.ptCode) ?: Constants.Commons.BLANK_STRING

        String impEvents = alertService.setImpEventValue(data.pt, data.ptCode, data.substance, data.substanceId, eiList, true)

        EvdasOnDemandAlert evdasOnDemandAlert = new EvdasOnDemandAlert(
                alertConfiguration: config,
                executedAlertConfiguration: executedConfig,
                name: executedConfig.name,
                createdBy: config.createdBy,
                modifiedBy: config.modifiedBy,
                dateCreated: executedConfig.dateCreated,
                lastUpdated: executedConfig.dateCreated,
                frequency: executedConfig.frequency,
                flags: flags,
                rorValue: rorValue,
                allRor: rorValue,
                totalFatal: data.totalFatal,
                newFatal: data.newFatal,
                totalSerious: data.totalSerious,
                newSerious: data.newSerious,
                totalEv: data.totalEv,
                newEv: data.newEv,
                totalEvLink: data.totalEvLink,
                newEvLink: data.newEvLink,
                dmeIme: data.dmeIme,
                pt: data.pt,
                ptCode: data.ptCode,
                soc: data.soc,
                newLit: data.newLit,
                totalLit: data.totalLit,
                sdr: data.sdr,
                smqNarrow: data.smqNarrow,
                substance: data.substance,
                substanceId: data.substanceId,
                hlgt: data.hlgt,
                hlt: data.hlt,
                newEea: data.newEea,
                totEea: data.totEea,
                newHcp: data.newHcp,
                totHcp: data.totHcp,
                newMedErr: data.newMedErr,
                totMedErr: data.totMedErr,
                newObs: data.newObs,
                totObs: data.totObs,
                newRc: data.newRc,
                totRc: data.totRc,
                newPaed: data.newPaed,
                totPaed: data.totPaed,
                newGeria: data.newGeria,
                totGeria: data.totGeria,
                europeRor: data.europeRor.toString().startsWith('.') ? "0" + data.europeRor : data.europeRor,
                northAmericaRor: data.northAmericaRor.toString().startsWith('.') ? "0" + data.northAmericaRor : data.northAmericaRor,
                japanRor: data.japanRor.toString().startsWith('.') ? "0" + data.japanRor : data.japanRor,
                asiaRor: data.asiaRor.toString().startsWith('.') ? "0" + data.asiaRor : data.asiaRor,
                restRor: data.restRor.toString().startsWith('.') ? "0" + data.restRor : data.restRor,
                ratioRorPaedVsOthers: data.ratioRorPaedVsOthers.toString().startsWith('.') ? "0" + data.ratioRorPaedVsOthers : data.ratioRorPaedVsOthers,
                ratioRorGeriatrVsOthers: data.ratioRorGeriatrVsOthers.toString().startsWith('.') ? "0" + data.ratioRorGeriatrVsOthers : data.ratioRorGeriatrVsOthers,
                changes: data.changes ? data.changes : '-',
                sdrPaed: data.sdrPaed,
                sdrGeratr: data.sdrGeratr,
                newSpont: data.newSpont,
                totSpontEurope: data.totSpontEurope,
                totSpontNAmerica: data.totSpontNAmerica,
                totSpontJapan: data.totSpontJapan,
                totSpontAsia: data.totSpontAsia,
                totSpontRest: data.totSpontRest,
                totSpont: data.totSpont,
                attributes: new JsonBuilder(data.attributes).toPrettyString(),
                listedness: null ,
                impEvents: impEvents,
        )
        if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
            listednessData?.each {
                if (it["ptCode"] == data.ptCode) {
                    evdasOnDemandAlert.listedness = evdasOnDemandAlert.setEvdasListednessValue(it["listed"] as String)?:false
                }
                if( evdasOnDemandAlert.listedness == null){
                    evdasOnDemandAlert.listedness = false
                }
            }
        } else {
            evdasOnDemandAlert.listedness = data.listedness
        }
        if (evdasOnDemandAlert.impEvents && evdasOnDemandAlert.impEvents != Constants.Commons.BLANK_STRING) {
            String[] impEventList = evdasOnDemandAlert.impEvents?.split(',')
            evdasOnDemandAlert.evImpEventList = []
            impEventList.each {
                if (it) {
                    evdasOnDemandAlert.evImpEventList.add(it.trim())
                }
            }
        }
        evdasOnDemandAlert
    }


    /**
     * Method to batch persist the aggregate alerts.
     * @param alertList
     * @param config
     */
    @Transactional
    void batchPersistEvdasOnDemandAlert(List<EvdasOnDemandAlert> alertList) {

        EvdasOnDemandAlert.withTransaction {

            List batch = []
            for (EvdasOnDemandAlert alert : alertList) {

                batch += alert
                if (batch.size() > Holders.config.signal.batch.size) {

                    Session session = sessionFactory.currentSession
                    for (EvdasOnDemandAlert alertInstance in batch) {

                        alertInstance.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {

                try {

                    Session session = sessionFactory.currentSession
                    for (EvdasOnDemandAlert alertIntance in batch) {

                        alertIntance.save(validate: false)
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

    /**
     * Method to batch persist the aggregate alert data and its history, activity and updates the other agg alerts.
     * @param data
     */
    @Transactional
    void batchPersistData(List<EvdasOnDemandAlert> alertList, ExecutedEvdasConfiguration executedConfig) {

        long time1 = System.currentTimeMillis()
        log.info("Now persisting the execution related data in a batch.")

        //Persist the alerts
        batchPersistEvdasOnDemandAlert(alertList)

        log.info("Persistance of execution related data in a batch is done.")
        long time2 = System.currentTimeMillis()
        log.info(((time2 - time1) / 1000) + " Secs were taken in the persistance of data for configuration " + executedConfig?.name)
    }

    private printExecutionMessage(executedConfig, config, alertData) {

        String executionMessage = "Execution of Configuration took ${executedConfig.totalExecutionTime}ms for evdas configuration ${config.name} [C:${config.id}, EC: ${executedConfig.id}]. It gave ${alertData ? alertData.size() : 0} PE combinations"
        log.info(executionMessage)
        log.info("Alert data save flow is complete.")
    }

    List<Map> fetchEvdasOnDemandAlertList(List eaList, AlertDataDTO alertDataDTO, Boolean isExport = false) {

        Map params = alertDataDTO.params
        List list = []
        List<Map> prevEvdasOnDemandAlertMap = []
        Boolean showSpecialPE = Boolean.parseBoolean(params.specialPE)

        list = fetchValuesForEvdasReport(eaList as List<EvdasOnDemandAlert>, showSpecialPE, isExport)
        Map prevColMap = Holders.config.signal.evdas.data.previous.columns.clone() as Map
        List prevExecs = []

        ExecutorService executorService = Executors.newFixedThreadPool(10)
        eaList.each { EvdasOnDemandAlert ea ->
            executorService.submit({

                prevExecs.eachWithIndex { pec, index ->
                    Map prevAlert = prevEvdasOnDemandAlertMap.find {

                        it.executedAlertConfigurationId = pec.id && it.substance == ea.substance && it.ptCode == ea.ptCode
                    }
                    Map countMap = [:]
                    if (prevAlert) {

                        prevColMap.each { fn, cn ->
                            countMap[fn] = prevAlert[fn] ?: "0"
                        }
                    } else {

                        prevColMap.each { fn, cn ->
                            countMap[fn] = "0"
                        }
                    }
                    String exeName = "exe" + (index)
                    list.find { it.id == ea.id }[exeName] = countMap
                }
            })
        }

        executorService.shutdown()
        executorService.awaitTermination(10, TimeUnit.MINUTES)

        list
    }

    List fetchValuesForEvdasReport(List<EvdasOnDemandAlert> evdasList, boolean showSpecialPE, Boolean isExport = false) {

        userTimezone = userService.getCurrentUserPreference()?.timeZone
        List list = []
        boolean ei = false, sm = false
        List returnValue = []

        ExecutorService executorService = Executors.newFixedThreadPool(50)

        List<Future> futureList = evdasList.collect { evdas ->
            executorService.submit({ ->

                List imgList = evdas?.impEvents?.split(',')

                Map ptMap = [isIme: imgList?.contains('ime') ? "true" : "false",
                             isDme: imgList?.contains('dme') ? "true" : "false",
                             isEi : imgList?.contains('ei') ? "true" : "false",
                             isSm : imgList?.contains('sm') ? "true" : "false"]
                evdas.toDto(userTimezone, showSpecialPE, ptMap, isExport)
            } as Callable)
        }

        futureList.each {
            list.add(it.get())
        }

        executorService.shutdown()
        list
    }

    boolean toggleEvdasOnDemandAlertFlag(long id) {

        EvdasOnDemandAlert alert = EvdasOnDemandAlert.findById(id)
        boolean orgValue = alert.flagged
        alert.flagged = !orgValue
        alert.save()
        alert.flagged
    }

    String getCaseListFileName(Long alertId){
        User currentUser = userService.getUser()
        EvdasOnDemandAlert evdasOnDemandAlert = EvdasOnDemandAlert.findById(alertId)
        String pt = evdasOnDemandAlert?.pt?:""
        String ingredient = evdasOnDemandAlert?.substance?:""
        String timezone = currentUser.preference.timeZone
        ingredient + "-" + pt + " Case List: " + DateUtil.stringFromDate(new Date(), DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteOnDemandAlert(ExecutedEvdasConfiguration executedEvdasConfiguration) {
        Sql sql = new Sql(dataSource)
        try {
            executedEvdasConfiguration.setIsDeleted(true)
            executedEvdasConfiguration.setIsEnabled(false)
            CRUDService.updateWithAuditLog(executedEvdasConfiguration)

            EvdasConfiguration evdasConfiguration = EvdasConfiguration.get(executedEvdasConfiguration.configId)
            evdasConfiguration.setIsDeleted(true)
            evdasConfiguration.setIsEnabled(false)
            CRUDService.updateWithAuditLog(evdasConfiguration)
        } catch(Exception ex){
            log.error(ex.printStackTrace())
        } finally {
            sql?.close()
        }
    }
}
