package com.rxlogix

import com.rxlogix.config.AlertDocument
import com.rxlogix.config.ImportDetail
import com.rxlogix.config.ImportLog
import com.rxlogix.signal.AdHocAlert
import grails.gorm.transactions.Transactional
import org.springframework.validation.FieldError

@Transactional
class ImportLogService {
    def messageSource
    def userService

    def createLog(String type) {
        return new ImportLog(type: type, startTime: new Date(), numSucceeded: 0, numFailed: 0).save()

    }

    def saveLog(ImportLog log, String response, importItems){
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
        log.response = response
        log.endTime = new Date()
        log.numSucceeded = importItems?.count{it.errors.allErrors.size() == 0} ?: 0
        log.numFailed = importItems?.count{it.errors.allErrors.size() > 0} ?: 0
        log.details = logDetails(log, importItems,locale)
        log.save()

    }

    def logDetails(ImportLog log, importItems, locale){
        def i = -1
        def details = []
        importItems?.collect{ item ->
            i = i + 1
            if(item.errors.allErrors) {
                def msg = item.errors.allErrors.collect { messageSource.getMessage(it, locale) }
                def inputId
                if(item.class == AlertDocument)
                    inputId = item.chronicleId
                else
                    inputId = item.slimId

                def detail = new ImportDetail(recNum: i, inputIdentifier: inputId ?: "Unknown", message: msg ?: "Saved", importLog: log)
                details << detail
            }
        }
        return details

    }
}
