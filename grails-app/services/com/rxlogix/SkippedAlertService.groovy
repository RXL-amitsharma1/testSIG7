package com.rxlogix


import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.SkippedAlertStateEnum
import com.rxlogix.signal.SkippedAlertInformation
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.property.RRule
import org.grails.web.json.JSONObject

@Transactional
class SkippedAlertService {

    def configurationService
    def CRUDService

    SkippedAlertInformation saveFirstMissedExecutionInformation(Configuration configuration, String alertType, String alertDisableReason) {

        SkippedAlertInformation skippedAlertInformation

        try {
            Date nextRunDate = configuration.nextRunDate.clone()
            if (nextRunDate) {
                skippedAlertInformation = SkippedAlertInformation.findByConfigIdAndNextRunDateAndStateEnumNotEqual(configuration.id, nextRunDate, SkippedAlertStateEnum.DISCARDED)
                if (!skippedAlertInformation) {
                    log.info("Saving first missed execution information.")
                    skippedAlertInformation = new SkippedAlertInformation(configuration, alertType, true)
                    String nextGroupCode = fetchNextSkippedGroupId(configuration.id, alertType)
                    skippedAlertInformation.setGroupCode(nextGroupCode)
                    skippedAlertInformation.alertDisableReason = alertDisableReason
                    skippedAlertInformation.save(flush: true)
                    log.info("Saved successfully with group code ${nextGroupCode}")
                }
            }
        } catch (Throwable e) {
            log.error("Error occurred while saving first skipped alert information: ${e.message}", e)
            e.printStackTrace()
            throw e
        }

        return skippedAlertInformation
    }

    String fetchNextSkippedGroupId(Long configId, String alertType) {

        Integer existingSkippedAlertCount = SkippedAlertInformation.countByConfigIdAndAlertType(configId, alertType)
        if (!existingSkippedAlertCount) {
            return configId + Constants.Commons.DASH_STRING + Constants.Commons.ONE
        }
        List<SkippedAlertInformation> existingSkippedAlertList = SkippedAlertInformation.createCriteria().list {
            eq("configId", configId)
            eq("alertType", alertType)
            order("id", "desc")
            maxResults(1)
        }

        return configId + Constants.Commons.DASH_STRING + (Long.parseLong(existingSkippedAlertList.get(0)?.groupCode.split("-")[1]) + 1)

    }


    void syncSkippedAlerts(Configuration configuration, String alertType) {

        log.info("Syncing skipped alerts...")

        try {
            SkippedAlertInformation nextSkippedAlertInformation = createNextSkippedAlertInformation(configuration, alertType, configuration.skippedAlertGroupCode)
            if (nextSkippedAlertInformation) {
                log.info("Skipped alert information [${nextSkippedAlertInformation.id}] saved for configuration ${configuration.name}[${configuration.id}]")
            }
        } catch (Exception e) {
            log.error("Error occurred while syncing skipped alerts information: ${e.message}", e)
            e.printStackTrace()
        }

    }

    SkippedAlertInformation createNextSkippedAlertInformation(Configuration configuration, String alertType, String skippedAlertGroupCode) {

        SkippedAlertInformation nextSkippedAlertInformation

        SkippedAlertInformation lastSkippedAlertInformation = fetchLastSkippedAlertInformation(skippedAlertGroupCode, alertType)
        if (lastSkippedAlertInformation) {
            Date futureNextRunDate = configurationService.getNextDate(configuration, true, lastSkippedAlertInformation.nextRunDate)

            if (futureNextRunDate && (futureNextRunDate <= new Date())) {
                nextSkippedAlertInformation = new SkippedAlertInformation(configuration, alertType)
                nextSkippedAlertInformation.setGroupCode(skippedAlertGroupCode)
                setValuesForNextSkippedAlertInformation(configuration, lastSkippedAlertInformation, nextSkippedAlertInformation)
                nextSkippedAlertInformation = nextSkippedAlertInformation.save(flush: true)
            }
        }
        return nextSkippedAlertInformation
    }


    SkippedAlertInformation fetchLastSkippedAlertInformation(String groupCode, String alertType) {

        SkippedAlertInformation lastSkippedAlertInformation
        List<SkippedAlertInformation> existingSkippedAlertInformationList = SkippedAlertInformation.createCriteria().list {
            eq("groupCode", groupCode)
            eq("alertType", alertType)
            eq("stateEnum", SkippedAlertStateEnum.CREATED)
            isNull("exConfigId")
            order("id", "desc")
            maxResults(1)
        }

        if (existingSkippedAlertInformationList) {
            lastSkippedAlertInformation = existingSkippedAlertInformationList.get(0)
        }
        return lastSkippedAlertInformation
    }

    void updateSkippedAlertsPostAlertReadyForExecution(Configuration configuration, AdjustmentTypeEnum adjustmentTypeEnum) {

        List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.createCriteria().list {
            eq("groupCode", configuration.skippedAlertGroupCode)
            eq("stateEnum", SkippedAlertStateEnum.CREATED)
            isNull("exConfigId")
            order("id", "asc")
        }

        if (skippedAlertInformationList) {
            log.info("Updating state of skipped alerts to READY_FOR_EXECUTION..")
            if (adjustmentTypeEnum == AdjustmentTypeEnum.ALERT_PER_SKIPPED_EXECUTION) {
                SkippedAlertInformation skippedAlert = skippedAlertInformationList.get(0)
                configuration.skippedAlertId = skippedAlert.id
                updateSkippedAlertStateAndAdjustmentEnum(skippedAlert, SkippedAlertStateEnum.READY_FOR_EXECUTION, adjustmentTypeEnum)
                List<SkippedAlertInformation> skippedAlertListToBeDiscarded = skippedAlertInformationList - skippedAlert
                log.info("Deleting discarded skipped alerts information...")
                skippedAlertListToBeDiscarded.each {
                    it.delete(flush: true)
                }
            } else if (adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
                skippedAlertInformationList.each { skippedAlertInformation -> updateSkippedAlertStateAndAdjustmentEnum(skippedAlertInformation, SkippedAlertStateEnum.READY_FOR_EXECUTION, adjustmentTypeEnum)
                }
            }
        }

    }

    void updateSkippedAlertsPostExecutedConfigInitialization(Configuration configuration, Long execConfigId) {
        log.info("Updating skipped alerts state post ex-config initialization...")
        if (configuration.adjustmentTypeEnum == AdjustmentTypeEnum.ALERT_PER_SKIPPED_EXECUTION) {
            SkippedAlertInformation skippedAlert = SkippedAlertInformation.get(configuration.skippedAlertId)
            updateSkippedAlertStateAndExConfigId(skippedAlert, SkippedAlertStateEnum.EXECUTING, execConfigId)
        } else if (configuration.adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
            List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.findAllByGroupCodeAndStateEnumAndExConfigIdIsNull(configuration.skippedAlertGroupCode, SkippedAlertStateEnum.READY_FOR_EXECUTION)
            skippedAlertInformationList.each { skippedAlertInformation -> updateSkippedAlertStateAndExConfigId(skippedAlertInformation, SkippedAlertStateEnum.EXECUTING, execConfigId)
            }
        }

    }

    void updateSkippedAlertsState(Long exConfigId, SkippedAlertStateEnum stateEnum) {
        log.info("Updating state of skipped alerts to EXECUTING...")
        List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.findAllByExConfigId(exConfigId)

        skippedAlertInformationList.each { skippedAlertInformation ->
            skippedAlertInformation.stateEnum = stateEnum
            skippedAlertInformation.save(flush: true)
        }
    }

    void updateSkippedAlertsPostAlertCompletion(ExecutedConfiguration executedConfiguration) {

        log.info("Updating state of skipped alerts to EXECUTED...")
        if (executedConfiguration.adjustmentTypeEnum == AdjustmentTypeEnum.ALERT_PER_SKIPPED_EXECUTION) {

            SkippedAlertInformation skippedAlertInformation = SkippedAlertInformation.findByGroupCodeAndStateEnumAndExConfigId(executedConfiguration.skippedAlertGroupCode, SkippedAlertStateEnum.EXECUTING, executedConfiguration.id)
            if (skippedAlertInformation) {
                updateSkippedAlertState(skippedAlertInformation, SkippedAlertStateEnum.EXECUTED)
            }
        } else if (executedConfiguration.adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
            List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.findAllByGroupCodeAndStateEnumAndExConfigId(executedConfiguration.skippedAlertGroupCode, SkippedAlertStateEnum.EXECUTING, executedConfiguration.id)
            if (skippedAlertInformationList) {
                skippedAlertInformationList.each { skippedAlertInformation -> updateSkippedAlertState(skippedAlertInformation, SkippedAlertStateEnum.EXECUTED)
                }
            }
        }

    }

    void updateSkippedAlertState(SkippedAlertInformation skippedAlertInformation, SkippedAlertStateEnum stateEnum) {
        if (stateEnum == SkippedAlertStateEnum.DISCARDED) {
            skippedAlertInformation.setExConfigId(-1)
        }
        skippedAlertInformation.setStateEnum(stateEnum)
        skippedAlertInformation.save(flush: true)
    }

    void updateSkippedAlertStateAndExConfigId(SkippedAlertInformation skippedAlertInformation, SkippedAlertStateEnum stateEnum, Long exConfigId) {

        skippedAlertInformation.setStateEnum(stateEnum)
        skippedAlertInformation.setExConfigId(exConfigId)
        skippedAlertInformation.save(flush: true)

    }

    void updateSkippedAlertStateAndAdjustmentEnum(SkippedAlertInformation skippedAlertInformation, SkippedAlertStateEnum stateEnum, AdjustmentTypeEnum adjustmentTypeEnum) {

        skippedAlertInformation.setStateEnum(stateEnum)
        skippedAlertInformation.setAdjustmentTypeEnum(adjustmentTypeEnum)
        skippedAlertInformation.save(flush: true)

    }


    void setValuesForNextSkippedAlertInformation(Configuration configuration, SkippedAlertInformation lastSkippedAlertInformation, SkippedAlertInformation nextSkippedAlertInformation) {

        setNextRunDateForSkippedAlert(configuration, lastSkippedAlertInformation, nextSkippedAlertInformation)
        adjustCustomDateRangesForConfigAlert(configuration, lastSkippedAlertInformation, nextSkippedAlertInformation)


        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF && lastSkippedAlertInformation.asOfVersionDate != null) {
            adjustAsOfVersion(configuration, lastSkippedAlertInformation, nextSkippedAlertInformation)
        }

    }


    private void setNextRunDateForSkippedAlert(Configuration configuration, SkippedAlertInformation lastSkippedAlert, SkippedAlertInformation nextSkippedAlert) {

        if (configuration.adhocRun) {
            nextSkippedAlert.setNextRunDate(null)
        } else {
            nextSkippedAlert.setNextRunDate(configurationService.getNextDate(configuration, true, lastSkippedAlert.nextRunDate))
        }
    }

    private adjustCustomDateRangesForConfigAlert(Configuration configuration, SkippedAlertInformation lastSkippedAlert, SkippedAlertInformation nextSkippedAlert) {
        def runOnce = null
        def hourly = null

        if (JSON.parse(configuration.scheduleDateJSON).recurrencePattern) {
            runOnce = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.RUN_ONCE)
            hourly = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.HOURLY)
        }
        if (!runOnce && !hourly) {
            //Start and End check to make sure that date range is custom date range
            if (lastSkippedAlert.skippedAlertDateRangeInformation.dateRangeStartAbsolute && lastSkippedAlert.skippedAlertDateRangeInformation.dateRangeEndAbsolute) {
                List<Date> dateRange = getUpdatedStartandEndDate(configuration, lastSkippedAlert)
                if (dateRange) {
                    nextSkippedAlert.skippedAlertDateRangeInformation.dateRangeStartAbsolute = dateRange[0]
                    nextSkippedAlert.skippedAlertDateRangeInformation.dateRangeEndAbsolute = dateRange[1]
                }
            }
        }
    }

    List<Date> getUpdatedStartandEndDate(config, SkippedAlertInformation lastSkippedAlert) {
        if (config.scheduleDateJSON && config.isEnabled == true) {
            JSONObject timeObject = JSON.parse(config.scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                Date newStartDate
                Date newEndDate
                RRule recurRule = new RRule(timeObject.recurrencePattern)

                int interval = recurRule.recur.getInterval()
                String freq = recurRule.recur.getFrequency()
                if (interval < 0) interval = 1
                Date startDate = lastSkippedAlert.skippedAlertDateRangeInformation.dateRangeStartAbsolute
                if (!startDate) {
                    startDate = new Date()
                }
                Date endDate = lastSkippedAlert.skippedAlertDateRangeInformation.dateRangeEndAbsolute
                if (!endDate) {
                    endDate = new Date()
                }
                Calendar cal = Calendar.getInstance()
                cal.setTime(endDate)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                Boolean lastDayOfMonth = endDate == cal.getTime()
                use(TimeCategory) {
                    switch (freq) {
                        case Recur.WEEKLY:
                            newStartDate = startDate + interval.weeks
                            newEndDate = endDate + interval.weeks
                            break
                        case Recur.MONTHLY:
                            newStartDate = startDate + interval.months
                            newEndDate = endDate + interval.months
                            if (lastDayOfMonth) {
                                newEndDate = configurationService.updateDateToLastDateOfMonth(newEndDate)
                            }
                            break
                        case Recur.YEARLY:
                            newStartDate = startDate + interval.years
                            newEndDate = endDate + interval.years
                            break
                        default:
                            newStartDate = startDate
                            newEndDate = endDate
                            break
                    }
                }
                return [newStartDate, newEndDate]
            }
        }
    }

    private adjustAsOfVersion(Configuration configuration, SkippedAlertInformation lastSkippedAlert, SkippedAlertInformation nextSkippedAlert) {
        def runOnce = null
        def hourly = null


        if (JSON.parse(configuration.scheduleDateJSON).recurrencePattern) {
            runOnce = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.RUN_ONCE)
            hourly = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.HOURLY)
        }
        if (!runOnce && !hourly) {
            if (lastSkippedAlert.asOfVersionDate && lastSkippedAlert.nextRunDate != null) {
                nextSkippedAlert.asOfVersionDate = getUpdatedAsOfVersionDate(configuration, lastSkippedAlert)
            }
        }
    }

    def getUpdatedAsOfVersionDate(config, SkippedAlertInformation lastSkippedAlert) {
        if (config.scheduleDateJSON && config.isEnabled == true) {
            JSONObject timeObject = JSON.parse(config.scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                Date newAsOfVersionDate
                RRule recurRule = new RRule(timeObject.recurrencePattern)

                int interval = recurRule.recur.getInterval()
                String freq = recurRule.recur.getFrequency()
                if (interval < 0) interval = 1
                Date asOfVersionDate = lastSkippedAlert.asOfVersionDate
                if (!asOfVersionDate) {
                    asOfVersionDate = new Date()
                }
                Calendar cal = Calendar.getInstance()
                cal.setTime(asOfVersionDate)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                Boolean lastDayOfMonth = asOfVersionDate == cal.getTime()
                use(TimeCategory) {
                    switch (freq) {
                        case Recur.WEEKLY:
                            newAsOfVersionDate = asOfVersionDate + interval.weeks
                            break
                        case Recur.MONTHLY:
                            newAsOfVersionDate = asOfVersionDate + interval.months
                            if (lastDayOfMonth) {
                                newAsOfVersionDate = configurationService.updateDateToLastDateOfMonth(newAsOfVersionDate)
                            }
                            break
                        case Recur.YEARLY:
                            newAsOfVersionDate = asOfVersionDate + interval.years
                            break
                        case Recur.DAILY:
                            newAsOfVersionDate = asOfVersionDate + interval.days
                            break
                        default:
                            newAsOfVersionDate = asOfVersionDate
                            break
                    }
                }
                newAsOfVersionDate
            }
        }
    }

}

