package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.PVSState
import grails.gorm.transactions.Transactional

@Transactional
class PVSStateService {
    CacheService cacheService
    def saveState(PVSState pvsState) {
        !pvsState.hasErrors() && pvsState.save()

        cacheService.updateWorkflowCache()
    }
}
