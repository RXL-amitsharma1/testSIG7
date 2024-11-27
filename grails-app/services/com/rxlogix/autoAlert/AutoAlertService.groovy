package com.rxlogix.autoAlert

import com.rxlogix.config.Configuration
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource
import java.time.ZoneId

@Transactional
class AutoAlertService {

    def reportExecutorService

    @Autowired
    DataSource dataSource_pva

    def addCasesToAlert() {
        final Sql sql = new Sql(dataSource_pva)
        try {
            def qry = SignalQueryHelper.select_auto_alert_sql()
            def row = sql.firstRow(qry)
            def lastSuccessETL
            if (row)
                lastSuccessETL = row.FINISH_DATETIME
            if (lastSuccessETL) {
                //Time of last scheduled check
                Date lastCheckDate = new Date(System.currentTimeMillis() - 480 * 1000)
                //Time of successful ETL run
                Date lastSuccessDate = Date.from(lastSuccessETL.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())
                if (lastSuccessDate > lastCheckDate) {
                    log.info("Auto Alert is triggered ")
                    attachCasesToConfiguration()
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
    }

    def attachCasesToConfiguration() {
        def configList = Configuration.autoAlertTriggerConfiguration().list()
        if (configList) {
            log.info("Auto execution of configuration started.")
            log.info("Configurations captured are : " + configList)
            configList.each { Configuration config ->
                try {
                    config.isAutoTrigger = true
                    config.executing = true
                    reportExecutorService.executeAlertJobQualitative config, true, { Long configId, Long executedConfigId ->
                        Configuration configuration = Configuration.get(configId)
                        configuration.executing = false
                        configuration.save()
                        log.info("Auto Alert Configuration is executed.")
                    }, { Long configId, Long executedConfigId, def ese ->
                        Configuration configuration = Configuration.get(configId)
                        configuration.executing = false
                        configuration.save()
                        log.info("Error occured during execution of Auto Alert Configuration.")
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
            log.info("Auto execution of configuration completed.")

        }
    }

}
