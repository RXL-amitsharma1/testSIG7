package com.rxlogix.dto

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.util.DateUtil

class MissedAlertNotificationDTO {
    String alertName
    String alertType
    String dateRangeInformation


    MissedAlertNotificationDTO(def configuration) {
        this.alertName = configuration.name
        if (configuration instanceof Configuration) {
            this.alertType = configuration.type
            this.dateRangeInformation = DateUtil.toDateString1(configuration?.alertDateRangeInformation?.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString1(configuration?.alertDateRangeInformation?.getReportStartAndEndDate()[1])
        } else if (configuration instanceof EvdasConfiguration) {
            this.alertType = Constants.AlertConfigType.EVDAS_ALERT
            this.dateRangeInformation = DateUtil.toDateString1(configuration?.dateRangeInformation?.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString1(configuration?.dateRangeInformation?.getReportStartAndEndDate()[1])
        } else if (configuration instanceof MasterConfiguration) {
            this.alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
            List<Configuration> configurations = Configuration.findAllByMasterConfigIdAndNextRunDate(configuration.id, configuration.nextRunDate)
            Configuration childConfiguration = configurations ? configurations[0] : null
            this.dateRangeInformation = DateUtil.toDateString1(childConfiguration?.alertDateRangeInformation?.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString1(childConfiguration?.alertDateRangeInformation?.getReportStartAndEndDate()[1])
        }

    }

}

