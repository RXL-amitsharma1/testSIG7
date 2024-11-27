package com.rxlogix.cache

import com.rxlogix.mapping.LmProduct
import grails.gorm.transactions.Transactional

class GenericNameCacheService {
    def cacheMap = [:]
    Map<String, LmProduct> reverseMap = [:]

    def set(lmProducts) {
        lmProducts?.each{ LmProduct lmProduct ->
            if(lmProduct.genericName) {
                if (cacheMap[lmProduct.genericName])
                    cacheMap[lmProduct.genericName] = cacheMap[lmProduct.genericName] + lmProduct
                else
                    cacheMap[lmProduct.genericName] = [lmProduct]
            }

            if (lmProduct.name) {
                reverseMap[lmProduct.name] = lmProduct
            }
        }
    }

    def get(){
        return cacheMap
    }

    def getGenericNameForProduct(String productName) {
        reverseMap[productName]?.genericName
    }
}
