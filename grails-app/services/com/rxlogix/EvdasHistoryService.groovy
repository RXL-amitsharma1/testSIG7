package com.rxlogix

import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.signal.EvdasHistory
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.Session

@Transactional
class EvdasHistoryService {

    def CRUDService
    def cacheService
    def sessionFactory

    def getEvdasHistoryTrendCounts(productName, eventName) {
        def ehCriteria = EvdasHistory.createCriteria()
        def ehResult = ehCriteria.list {
            eq("productName", productName)
            eq("eventName", eventName)
            order("id", "desc")
            maxResults(2)
        }
        if (ehResult.size() > 1) {
            def firstNewEv = ehResult[0]?.newEv
            def secondNewEv = ehResult[1]?.newEv
            if (firstNewEv && secondNewEv)
                if (secondNewEv * 1.5 < firstNewEv)
                    return Constants.Commons.POSITIVE
                else
                    return Constants.Commons.NEGATIVE
        }
        return Constants.Commons.EVEN
    }

    def getEvdasHistoryTrendPrr(productName, eventName) {
        def ehCriteria = EvdasHistory.createCriteria()
        def ehResult = ehCriteria.list {
            eq("productName", productName)
            eq("eventName", eventName)
            order("id", "desc")
            maxResults(2)
        }

        if (ehResult.size() > 1) {
            def firstPrrScore = Double.parseDouble(ehResult[0]?.prrValue)
            def secondPrrScore = Double.parseDouble(ehResult[0]?.prrValue)

            if (firstPrrScore && secondPrrScore) {
                def compareScore = Double.compare(firstPrrScore, secondPrrScore);

                if (compareScore > 0) {
                    return Constants.Commons.POSITIVE
                } else if (compareScore < 0) {
                    return Constants.Commons.NEGATIVE
                } else {
                    return Constants.Commons.NEGATIVE
                }
            }
        }
        return Constants.Commons.EVEN
    }

    void batchPersistHistory(peHistoryMapList) {

        EvdasHistory.withTransaction {

            def batch = []

            for(def peHistoryMap : peHistoryMapList) {
                batch += peHistoryMap
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (def peHistory in batch) {
                        EvdasHistory productEventHistory = prepareEvdastHistoryOnExecution(peHistory)
                        productEventHistory.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (def peHistory in batch) {
                        EvdasHistory productEventHistory = prepareEvdastHistoryOnExecution(peHistory)
                        productEventHistory.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th ) {
                    th.printStackTrace()
                }
            }
            log.info("History data is batch persisted.")
        }
    }

    void batchPersistEvdasHistory(peHistoryMapList) {

        EvdasHistory.withTransaction {

            def batch = []

            for(EvdasHistory evHistoryMap : peHistoryMapList) {
                batch += evHistoryMap
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (EvdasHistory evdasHistory in batch) {
                        evdasHistory.save(validate:false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (EvdasHistory evdasHistory in batch) {
                        evdasHistory.save(validate:false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th ) {
                    th.printStackTrace()
                }
            }
        }
    }

    def prepareEvdastHistoryOnExecution(peHistoryMap) {
        def productEventHistory = new EvdasHistory(peHistoryMap)
        productEventHistory
    }

    EvdasHistory getEvdasHistoryByPEC(String productName, String eventName, Long configId){
        EvdasHistory.createCriteria().get {
            eq("productName", productName)
            eq("eventName", eventName)
            eq("configId", configId)
            order("lastUpdated", "desc")
            maxResults(1)
        } as EvdasHistory
    }

}
