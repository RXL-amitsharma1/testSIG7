package com.rxlogix.jobs

import grails.util.Holders

class SignalMemoReportJob {
    def signalMemoReportService
    static triggers = {
        simple repeatInterval: Holders.config.signal.autoNotification.memo.report.job.interval
    }

    def execute() {
        signalMemoReportService.generateAutoNotificationForSignal()
    }
}
