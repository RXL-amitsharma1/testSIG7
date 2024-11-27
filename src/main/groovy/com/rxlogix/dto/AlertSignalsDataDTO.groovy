package com.rxlogix.dto

import com.rxlogix.Constants
import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.ArchivedLiteratureAlert
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.SingleCaseAlert

class AlertSignalsDataDTO {

    Long configId
    String alertType
    Boolean isLatest

    def domainName
    def executedDomainName
    def alertDomainName
    def archivedAlertDomainName

    def configuration
    def executedConfiguration
    List archivedExecutedConfigurations

    List signalData

    AlertSignalsDataDTO(Long configId, String alertType, Boolean isLatest) {
        this.configId = configId
        this.alertType = alertType
        this.isLatest = isLatest
        this.domainName = getConfigurationDomain(alertType)
        this.executedDomainName = getExecutedConfigurationDomain(alertType)
        this.alertDomainName = getAlertDomainName(alertType)
        this.archivedAlertDomainName = getAlertDomainName(alertType, true)
    }

    def getConfigurationDomain(String alertType) {
        def domain = null
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            domain = Configuration
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            domain = EvdasConfiguration
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            domain = LiteratureConfiguration
        }
        return domain
    }

    def getExecutedConfigurationDomain(String alertType) {
        def executedDomain = null
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            executedDomain = ExecutedConfiguration
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            executedDomain = ExecutedEvdasConfiguration
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            executedDomain = ExecutedLiteratureConfiguration
        }
        return executedDomain
    }

    def getAlertDomainName(String alertType, Boolean isArchived = false) {
        def alertDomain = null
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            alertDomain = isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
        } else if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            alertDomain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            alertDomain = isArchived ? ArchivedEvdasAlert : EvdasAlert
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            alertDomain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
        }
        return alertDomain
    }

}
