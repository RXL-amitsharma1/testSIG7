package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.Session
import org.hibernate.jdbc.Work

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

class MedicalConceptsService {

    def cacheService
    def sessionFactory

    static transactional =  false

    def addMedicalConcepts(def alert, concepts) {
        if (concepts) {
            def conceptsList = concepts.tokenize(',')
            conceptsList.each {
                def medicalConcepts = cacheService.getMedicalConcepts(it)
                if(medicalConcepts) {
                    alert.addToSignalConcepts(medicalConcepts)
                }
            }
        }
    }

    @Transactional
    def addMedicalConceptsWithAlertInBatch(def alert, String concepts,String alertType) {
        if (concepts) {
            def conceptsList = concepts.tokenize(',')
            List<Long> prevAlertList = []
            String insertValidatedSignalAlertQuery = null
            if(alertType == Constants.AlertConfigType.EVDAS_ALERT){
                prevAlertList = EvdasAlert.createCriteria().list {
                    'eq'("substanceId", alert.substanceId)
                    'eq'("ptCode", alert.ptCode)
                    'executedAlertConfiguration'{
                        eq("isDeleted", false)
                        eq("isEnabled", true)
                        eq("adhocRun", false)
                    }
                    projections {
                        property('id')
                    }
                }
                insertValidatedSignalAlertQuery = "INSERT INTO EVDAS_SIGNAL_CONCEPTS(EVDAS_ALERT_ID,MEDICAL_CONCEPTS_ID) VALUES(?,?)"
            }else if(alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
                prevAlertList = SingleCaseAlert.createCriteria().list {
                    'eq'("caseNumber", alert.caseNumber)
                    'eq'("productFamily", alert.productFamily)
                    'executedAlertConfiguration'{
                        eq("isDeleted", false)
                        eq("isEnabled", true)
                        eq("adhocRun", false)
                    }
                    projections {
                        property('id')
                    }
                }
                insertValidatedSignalAlertQuery = "INSERT INTO SINGLE_SIGNAL_CONCEPTS(SINGLE_CASE_ALERT_ID,MEDICAL_CONCEPTS_ID) VALUES(?,?)"


            }else {

                prevAlertList = AggregateCaseAlert.createCriteria().list {
                    'eq'("productId", alert.productId)
                    'eq'("ptCode", alert.ptCode)
                    'executedAlertConfiguration'{
                        eq("isDeleted", false)
                        eq("isEnabled", true)
                        eq("adhocRun", false)
                    }
                    projections {
                        property('id')
                    }
                }
                insertValidatedSignalAlertQuery = "INSERT INTO AGG_SIGNAL_CONCEPTS(AGG_ALERT_ID,MEDICAL_CONCEPTS_ID) VALUES(?,?)"
            }

            List alertSignalConcepts = []
            conceptsList.each {
                def medicalConcepts = cacheService.getMedicalConcepts(it)
                if (medicalConcepts) {
                    prevAlertList.each {
                        alertSignalConcepts.add([alertId: it.toString(), conceptsId: medicalConcepts.id.toString()])
                    }
                }
            }

            Session session = sessionFactory.currentSession
            session.doWork(new Work() {
                public void execute(Connection connection) throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedSignalAlertQuery)
                    def batchSize = Holders.config.signal.batch.size
                    int count = 0
                    try {
                        alertSignalConcepts.each {
                            preparedStatement.setString(1, it.alertId)
                            preparedStatement.setString(2, it.conceptsId)
                            preparedStatement.addBatch()
                            count += 1
                            if (count == batchSize) {
                                preparedStatement.executeBatch()
                                count = 0
                            }
                        }
                        preparedStatement.executeBatch()
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        preparedStatement.close()
                        session.flush()
                        session.clear()
                    }
                }
            })
        }
    }
}
