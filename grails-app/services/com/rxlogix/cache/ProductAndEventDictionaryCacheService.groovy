package com.rxlogix.cache

import com.rxlogix.mapping.LmProduct
import com.rxlogix.mapping.MedDraPT
import grails.gorm.transactions.Transactional
import com.rxlogix.Constants

/**
 * Cache service for the product dictionary.
 */
@Transactional
class ProductAndEventDictionaryCacheService {

    //TODO: Need to use some generic cache framework.
    def productCacheArgusMap = [:]
    def productCacheFaersMap = [:]

    def ptCacheMap = [:]

    def prepareProductCache(String selectedDataSource) {
        log.info("Caching the products for datasrouce " + selectedDataSource)
        try {
            def products = LmProduct."$selectedDataSource".list([sort: "name", order: "asc"]).unique { it.name }
            if (selectedDataSource == Constants.DataSource.FAERS) {
                productCacheFaersMap.put("products", products)
            } else {
                productCacheArgusMap.put("products", products)
            }
            log.info("Caching the products for datasrouce " + selectedDataSource + " done.")
        } catch(Exception ex) {
            log.info("Failed to cache the products from datasrouce " + selectedDataSource)
        }

    }

    def getProductFromCache(selectedDataSource) {
        if (selectedDataSource == Constants.DataSource.FAERS) {
            return productCacheFaersMap.products
        } else {
            return productCacheArgusMap.products
        }
    }

    def prepareEventCache(selectedDataSource) {
        log.info("Caching the events for datasrouce " + selectedDataSource)
        try {
            def events = MedDraPT."$selectedDataSource".list([sort: "name", order: "asc"])
            if (selectedDataSource == Constants.DataSource.FAERS) {
                ptCacheMap.put("events", events)
            } else {
                ptCacheMap.put("events", events)
            }
            log.info("Caching the events for datasrouce " + selectedDataSource + " done.")
        } catch(Exception ex) {
            log.info("Failed to cache the events from datasrouce " + selectedDataSource)
        }

    }

    def getEventFromCache(selectedDataSource) {
        return ptCacheMap.events
    }

}
