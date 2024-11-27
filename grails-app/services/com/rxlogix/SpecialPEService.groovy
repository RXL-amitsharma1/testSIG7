package com.rxlogix

import grails.gorm.transactions.Transactional
import com.rxlogix.config.SpecialPE


@Transactional
class SpecialPEService {

    def isSpecialPE(product, event,specialPEList) {
        if (specialPEList) {
            for(def specialPE : specialPEList) {
                def events  = specialPE.specialEvents?.split(",")
                def products = specialPE.specialProducts?.split(",")
                if (products.contains(product) && events.contains(event)) {
                    return true
                } else {
                    return false
                }
            }
        } else {
            return false
        }
    }
}
