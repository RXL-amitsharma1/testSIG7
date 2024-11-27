package com.rxlogix.config

import com.rxlogix.BaseEvdasAlert
import com.rxlogix.Constants
import com.rxlogix.json.JsonOutput
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.transform.ToString

@ToString()
@DirtyCheck
@CollectionSnapshotAudit
class EvdasOnDemandAlert extends BaseEvdasAlert {
    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','justification','undoJustification']]
    //Configuration related information
    EvdasConfiguration alertConfiguration
    ExecutedEvdasConfiguration executedAlertConfiguration

    String totalEvLink
    String newEvLink
    String dmeIme
    String attributes
    String ratioRorPaedVsOthers
    String ratioRorGeriatrVsOthers
    String changes
    String sdrPaed
    String sdrGeratr
    String totSpontEurope
    String totSpontNAmerica
    String totSpontJapan
    String totSpontAsia
    String totSpontRest
    String format

    String frequency
    String flags

    Boolean listedness = false

    String impEvents
    List<String> evImpEventList = []
    String undoJustification
    Map<String, Object> customAuditProperties

    static transients = ['undoJustification','customAuditProperties']
    static mapping = {
        table("EVDAS_ON_DEMAND_ALERT")
        id generator:'sequence', params:[sequence:'evdas_on_demand_sequence']
        executedAlertConfiguration(column: 'exec_configuration_id')
        alertConfiguration(column: 'alert_configuration_id')
        evImpEventList joinTable: [name: "EOD_ALERT_IMP_EVENT_LIST", key: "EVDAS_ON_DEMAND_ALERT_ID", column: "EVDAS_ON_DEMAND_IMP_EVENTS"]
        attributes type: 'text', sqlType: 'clob'
    }

    static constraints = {
        dmeIme nullable: true, blank: true
        format nullable: true, blank: true, maxSize: 8000
        attributes nullable: true
        attributes nullable: true
        frequency nullable: true
        flags nullable: true
        totalEvLink nullable: true, size: 0..600
        newEvLink nullable: true, size: 0..600
        ratioRorPaedVsOthers nullable: true
        ratioRorGeriatrVsOthers nullable: true
        changes nullable: true
        sdrPaed nullable: true
        sdrGeratr nullable: true
        totSpontEurope nullable: true
        totSpontNAmerica nullable: true
        totSpontJapan nullable: true
        totSpontAsia nullable: true
        totSpontRest nullable: true
        listedness nullable: true
        impEvents nullable: true
        evImpEventList nullable: true
    }

    Map toDto(timeZone = "UTC", isSpecialPE = false,
              Map ptMap = [isIme: "", isDme: "", isEi: "", isSm: ""], isExport = false) {
        Map alertData = [
                id                     : this.id,
                alertName              : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                flagged                : this.flagged,
                alertConfigId          : this.alertConfigurationId,
                execConfigId           : this.executedAlertConfigurationId,
                productName            : this.substance,
                substanceId            : this.substanceId,
                soc                    : this.soc,
                preferredTerm          : this.pt,
                dmeIme                 : this.dmeIme ?: "",
                newEv                  : this.newEv ?: 0,
                totalEv                : this.totalEv ?: 0,
                newSerious             : this.newSerious ?: 0,
                totalSerious           : this.totalSerious ?: 0,
                newFatal               : this.newFatal ?: 0,
                totalFatal             : this.totalFatal ?: 0,
                //Spontaneous counts are mapped with the 'Ev' counts.
                newSponCount           : this.newEv ?: 0,
                cumSponCount           : this.totalEv ?: 0,
                newSeriousCount        : this.newSerious ?: 0,
                cumSeriousCount        : this.totalSerious ?: 0,
                rorValue               : this.rorValue ?: "",
                newLit                 : this.newLit ?: "",
                totalLit               : this.totalLit ?: "",
                sdr                    : this.sdr ?: "",
                smqNarrow              : this.smqNarrow ?: "",
                isSpecialPE            : isSpecialPE,
                dataSource             : Constants.DataSource.EVDAS.toUpperCase(),
                eb05                   : '-',
                eb95                   : '-',
                ebgm                   : '-',
                impEvents              : (isExport && this.impEvents)?replaceStopListLabel(this.impEvents).toUpperCase():this.impEvents ? this.impEvents.toUpperCase() : Constants.Commons.BLANK_STRING,
                ime                    : ptMap.isIme,
                dme                    : ptMap.isDme,
                ei                     : ptMap.isEi,
                isSm                   : ptMap.isSm,
                lastUpdated            : this.lastUpdated,
                format                 : this.format,
                hlgt                   : this.hlgt ?: "",
                hlt                    : this.hlt ?: "",
                newEea                 : this.newEea ?: "",
                totEea                 : this.totEea ?: "",
                newHcp                 : this.newHcp ?: "",
                totHcp                 : this.totHcp ?: "",
                newMedErr              : this.newMedErr ?: "",
                totMedErr              : this.totMedErr ?: "",
                newObs                 : this.newObs ?: "",
                totObs                 : this.totObs ?: "",
                newRc                  : this.newRc ?: "",
                totRc                  : this.totRc ?: "",
                newPaed                : this.newPaed ?: "",
                totPaed                : this.totPaed ?: "",
                newGeria               : this.newGeria ?: "",
                totGeria               : this.totGeria ?: "",
                europeRor              : this.europeRor ?: "",
                northAmericaRor        : this.northAmericaRor ?: "",
                japanRor               : this.japanRor ?: "",
                asiaRor                : this.asiaRor ?: "",
                restRor                : this.restRor ?: "",
                allRor                 : this.allRor ?: "",
                newSpont               : this.newSpont ?: "",
                totSpont               : this.totSpont ?: "",
                ptCode                 : this.ptCode ?: "",
                newEvLink              : this.newEvLink ?: "",
                totalEvLink            : this.totalEvLink ?: "",
                ratioRorPaedVsOthers   : this.ratioRorPaedVsOthers ?: "",
                ratioRorGeriatrVsOthers: this.ratioRorGeriatrVsOthers ?: "",
                changes                : this.changes ?: "",
                sdrPaed                : this.sdrPaed ?: "",
                sdrGeratr              : this.sdrGeratr ?: "",
                totSpontEurope         : this.totSpontEurope ?: "",
                totSpontNAmerica       : this.totSpontNAmerica ?: "",
                totSpontJapan          : this.totSpontJapan ?: "",
                totSpontAsia           : this.totSpontAsia ?: "",
                totSpontRest           : this.totSpontRest ?: "",
                listed                 : getEvdasListednessValue(this.listedness),
                prrValue               : '-',
        ]
        alertData.padiatric = (int) ((this.id % 2 == 0) ? (this.totalEv) * 12 / 100 : (this.totalEv) * 25 / 100)
        alertData.geriatric = (int) ((this.id % 2 == 0) ? (this.totalEv) * 60 / 100 : (this.totalEv) * 70 / 100)
        alertData
    }

    def getProductName() {
        substance
    }

    User getUserByUserId(Long userId) {
        cacheService.getUserByUserId(userId)
    }

    Group getGroupByGroupId(Long groupId) {
        cacheService.getGroupByGroupId(groupId)
    }

    @Override
    String toString(){
        super.toString()
    }
    def getInstanceIdentifierForAuditLog() {
        return this.executedAlertConfiguration.getInstanceIdentifierForAuditLog() + ": " + this.productName + "-" + this.pt
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (this.undoJustification != null && this.undoJustification != "") {
            newValues.put("undoJustification", this.undoJustification)
        }
        // custom audit properties added before CRUD operation
        for(Map.Entry customAuditEntry: this.customAuditProperties){
            if (customAuditEntry.getValue() != null && customAuditEntry.getValue() != "") {
                newValues.put(customAuditEntry.getKey(), customAuditEntry.getValue())
                oldValues.put(customAuditEntry.getKey(), "")
            }
        }
        this.customAuditProperties=[:]
        return [newValues: newValues, oldValues: oldValues]
    }

}
