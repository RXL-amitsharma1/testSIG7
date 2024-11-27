package com.rxlogix

import com.rxlogix.config.EvdasApplicationSettings
import grails.gorm.transactions.Transactional

@Transactional
class ApplicationSettingsService {

    Boolean fetchEvdasErmrUploadLocked() {
        EvdasApplicationSettings.first().evdasErmrUploadLocked
    }

    Boolean fetchEvdasCaseListingUploadLocked() {
        EvdasApplicationSettings.first().evdasCaseListingUploadLocked
    }

    void enableEvdasErmrUploadLock() {
        EvdasApplicationSettings.withNewSession {
            EvdasApplicationSettings applicationSettings = EvdasApplicationSettings.first()
            applicationSettings.evdasErmrUploadLocked = true
            applicationSettings.save(flush: true)
        }
    }

    void releaseEvdasErmrUploadLock() {
        EvdasApplicationSettings.withNewSession {
            EvdasApplicationSettings applicationSettings = EvdasApplicationSettings.first()
            applicationSettings.evdasErmrUploadLocked = false
            applicationSettings.save(flush: true)
        }
    }

    void enableEvdasCaseListingUploadLock() {
        EvdasApplicationSettings.withNewSession {
            EvdasApplicationSettings applicationSettings = EvdasApplicationSettings.first()
            applicationSettings.evdasCaseListingUploadLocked = true
            applicationSettings.save(flush: true)
        }
    }

    void releaseEvdasCaseListingUploadLock() {
        EvdasApplicationSettings.withNewSession {
            EvdasApplicationSettings applicationSettings = EvdasApplicationSettings.first()
            applicationSettings.evdasCaseListingUploadLocked = false
            applicationSettings.save(flush: true)
        }
    }
}
