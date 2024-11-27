package com.rxlogix

import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BaseEvdasAlert {

    def cacheService

    String name

    //Evdas related parameters.
    String rorValue
    Integer totalFatal
    Integer newFatal
    Integer totalSerious
    Integer newSerious
    Integer totalEv
    Integer newEv
    String pt
    Integer ptCode
    String soc
    String newLit
    String totalLit
    String sdr
    String smqNarrow
    String substance
    Integer substanceId
    String hlgt
    String hlt
    String newEea
    String totEea
    String newHcp
    String totHcp
    String newMedErr
    String totMedErr
    String newObs
    String totObs
    String newRc
    String totRc
    String newPaed
    String totPaed
    String newGeria
    String totGeria
    String europeRor
    String northAmericaRor
    String japanRor
    String asiaRor
    String restRor
    String allRor
    String newSpont
    String totSpont
    Date periodStartDate
    Date periodEndDate

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    boolean flagged = false

    static constraints = {
        alertConfiguration nullable: true
        executedAlertConfiguration nullable: true
        createdBy nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        modifiedBy nullable: true
        substance nullable: true, blank: true
        soc nullable: true, blank: true
        pt nullable: true, blank: true
        newEv nullable: true
        totalEv nullable: true
        newSerious nullable: true
        totalSerious nullable: true
        newFatal nullable: true
        totalFatal nullable: true
        rorValue nullable: true
        newLit nullable: true
        totalLit nullable: true
        sdr nullable: true
        smqNarrow nullable: true
        periodStartDate nullable: true
        periodEndDate nullable: true
        hlgt nullable: true
        hlt nullable: true
        newEea nullable: true
        totEea nullable: true
        newHcp nullable: true
        totHcp nullable: true
        newMedErr nullable: true
        totMedErr nullable: true
        newObs nullable: true
        totObs nullable: true
        newRc nullable: true
        totRc nullable: true
        newPaed nullable: true
        totPaed nullable: true
        newGeria nullable: true
        totGeria nullable: true
        europeRor nullable: true
        northAmericaRor nullable: true
        japanRor nullable: true
        asiaRor nullable: true
        restRor nullable: true
        allRor nullable: true
        newSpont nullable: true
        totSpont nullable: true
    }


    String getEvdasListednessValue(Boolean listed) {
        switch (listed) {
            case true:
                return "Yes"
            case false:
                return "No"
            default:
                return "N/A"
        }
    }

    Boolean evdasListednessValue(String listed) {
        switch (listed) {
            case '1':
                return true
            case '0':
                return false
            default:
                return null
        }
    }

    Boolean setEvdasListednessValue(String listed) {
        switch (listed) {
            case "Yes":
                return true
            case "No":
                return false
            default:
                return null
        }
    }
    String replaceStopListLabel(String impEvents){
        impEvents = impEvents?.replace('ei','sl')
        impEvents
    }
    @Override
    String toString(){
        "$name"
    }

}
