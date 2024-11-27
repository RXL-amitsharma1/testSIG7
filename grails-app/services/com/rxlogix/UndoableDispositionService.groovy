package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.signal.UndoableDisposition
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class UndoableDispositionService {

    def cacheService
    def sessionFactory

    def serviceMethod() {

    }

    UndoableDisposition createUndoableObject(Map dispDataMap){
        UndoableDisposition undoableDisposition = new UndoableDisposition(dispDataMap)
        if(cacheService.getDispositionByValue(dispDataMap.currDispositionId as Long)?.isValidatedConfirmed() && dispDataMap.objectType != Constants.AlertConfigType.VALIDATED_SIGNAL){
            undoableDisposition.isEnabled=false
        }
        undoableDisposition
    }

    void persistUndoableDisposition(Map undoableMap){
        log.info("Batch persisting Undoable Dispositions")
        List<UndoableDisposition> undoableDispositionList = undoableMap.undoableDispositionList
        if(undoableDispositionList.size()){
            log.info("Batch persist of Undoable Disposition is started for " +undoableDispositionList.size()+ " objects" )
            Integer batchSize = Holders.config.signal.batch.size as Integer
            UndoableDisposition.withTransaction {
                List<UndoableDisposition> batch = []
                undoableDispositionList.each {
                    batch += it
                    if (batch.size().mod(batchSize) == 0) {
                        Session session = sessionFactory.currentSession
                        for(UndoableDisposition undoableDisposition in batch){
                            undoableDisposition.save(validate:false)
                        }
                        session.flush()
                        session.clear()
                        batch.clear()
                    }
                }
                if (batch) {
                    Session session = sessionFactory.currentSession
                    for(UndoableDisposition undoableDisposition in batch){
                        undoableDisposition.save(validate:false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            log.info("Batch persist of undoable dispositions executed successfully!")
        }
    }

    List<Long> getUndoableAlertList(List<Long> agaList, String alertType) {
        List results = new ArrayList<Long>()
        if (agaList.size() > 0) {
            try {
                log.info("Getting undoable disposition alert list for alert type: " + alertType)
                def startTime = System.currentTimeMillis()
                agaList.collate(Constants.AggregateAlertFields.BATCH_SIZE).each { batchIds ->
                    List<Long> batchResult = UndoableDisposition.createCriteria().list {
                        projections {
                            distinct("objectId", "objectId")
                        }
                        eq("objectType", alertType)
                        eq("isEnabled", true)
                        if (batchIds.size() > 0) {
                            or {
                                batchIds.collate(1000).each {
                                    'in'('objectId', it)
                                }
                            }
                        }
                    } as List<Long>
                    results.addAll(batchResult)
                    sessionFactory.currentSession.clear()
                }

                def endTime = System.currentTimeMillis()
                log.info("Got ${results.size()} alerts for undoing out of ${agaList.size()} alerts in time: " + (endTime - startTime) / 1000 + " sec")
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("Got error while fetching undoable alert list with error")
            }
        }
        results
    }

}


