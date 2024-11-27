package com.rxlogix

import com.rxlogix.config.EtlSchedule
import com.rxlogix.config.EtlStatus
import com.rxlogix.customException.EtlUpdateException
import com.rxlogix.mapping.EtlScheduleResult
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.grails.core.exceptions.GrailsRuntimeException
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

import java.sql.Timestamp

@Transactional(readOnly = true)
class EtlJobService {

    def dataSource_pva
    def userService

    /**
     * Method to schedule the etl operation.
     * @param schedule
     * @return
     */
    def enable() {

        def sql =  new Sql(dataSource_pva)

        try {
            sql.call("{call PKG_ETL_JOB.p_enable_incr_job()}")
        } catch(Exception ex){
            log.error(ex.message)
            throw ex;
        }finally{
            if(sql) {
                sql.close()
            }
        }

    }

    /**
     * Method to update the etl schedule.
     * @param schedule
     * @return
     */
    def update(EtlSchedule schedule) {

        def sql =  new Sql(dataSource_pva)

        try {
            def start = DateUtil.StringToDate(schedule.startDateTime, Constants.DateFormat.WITHOUT_SECONDS)
            def startDate = new Timestamp(start.getTime());

            Timestamp currentTimestamp = new Timestamp(new Date().getTime());
            if (startDate.before(currentTimestamp)) {
                throw new EtlUpdateException()
            }

            //Passing the value 'N' as from app the etl will not be initial but incremental.
            //Since this is called from the update thus 0 is passed.
            sql.call("{call PKG_ETL_JOB.p_create_job(?, ?, ?, ?)}", [startDate, getRecurrenceForETL(schedule.repeatInterval), 'N', 0])
        }catch(Exception ex){
            log.error(ex.message)
            throw ex
        }finally{
            if(sql) {
                sql.close()
            }
        }
    }

    /**
     * Method called to execute the etl operation when the run now is clicked.
     * @param schedule
     * @return
     */
    def initialize(EtlSchedule schedule) {

        def sql =  new Sql(dataSource_pva)
        try {
            def start = DateUtil.StringToDate(schedule.startDateTime, Constants.DateFormat.WITHOUT_SECONDS)
            def startDate = new java.sql.Timestamp(start.getTime());

            //Passing the value 'N' as from app the etl will not be initial but incremental.
            //Also the repeat interval parameter is sent as null.
            //Since this is called from run now flow thus 1 is passed.
            sql.call("{call PKG_ETL_JOB.p_create_job(?, ?, ?, ?)}", [startDate, null, 'N', 1])

        } catch(Exception ex){
            log.error(ex.message)
            throw ex
        } finally {
            if(sql) {
                sql.close()
            }
        }
    }

    /**
     * Method to disable the etl schedule.
     * @param schedule
     * @return
     */
    def disable(EtlSchedule schedule) {
        def sql =  new Sql(dataSource_pva)

        try{
            sql.call("{call PKG_ETL_JOB.p_disable_job('N')}")
        } catch(Exception ex) {
            log.error(ex.message)
            throw ex
        } finally {
            if(sql) {
                sql.close()
            }
        }
    }

    /**
     * This method fetches the schedule from the pva database
     */
    EtlSchedule getSchedule(boolean isRunNow = false) {
        def sql =  new Sql(dataSource_pva)

        EtlSchedule etlSchedule = EtlSchedule.first()

        if (etlSchedule && isRunNow) {
            try {
                def rows = sql.rows("SELECT * from V_PVR_SCHEDULER_JOBS WHERE ETL_MODE='INCR'")
                rows.collect {
                    etlSchedule.scheduleName = it.SCHEDULE_NAME
                    etlSchedule.startDateTime = it.START_DATETIME
                    etlSchedule.repeatInterval = it.REPEAT_INTERVAL
                    etlSchedule.isDisabled = it.DISABLED
                    etlSchedule.isInitial = it.IS_INITIAL
                }
            } catch(Exception ex) {
                log.error(ex.message)
            } finally {
                if(sql) {
                    sql.close()
                }
            }
        }
        return etlSchedule
    }

    /**
     * Metohd to fetch the etl schedule result.
     * @return
     */
    def getEtlScheduleResult() {
        def etlScheduleResultList = []
        try {
            EtlScheduleResult.withTransaction {
                etlScheduleResultList = (new EtlScheduleResult()).selectableQuery.list()
            }
        } catch (Exception ex) {
            log.error(ex.getMessage())
        }
        etlScheduleResultList
    }


    String getRecurrenceForETL(String recurrencePattern) throws Exception {
        if (recurrencePattern && !MiscUtil.validateScheduleDateJSON(recurrencePattern)) {
            throw new GrailsRuntimeException("### RepeatInterval isn't a valid recurrence pattern ###")
        }
//        Replacing COUNT = any_digit; or COUNT = any_digit end by blank as Oracle ETL doesn't handle COUNT
        return recurrencePattern?.replaceAll(~/COUNT=\d*($|;)/, "")
    }

    def getEtlStatus() {
        def etlStatus
        EtlStatus.withNewSession {
            etlStatus = EtlStatus.first()
        }
        etlStatus
    }

}
