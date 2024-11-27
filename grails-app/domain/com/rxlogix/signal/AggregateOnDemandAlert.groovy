package com.rxlogix.signal

import com.rxlogix.BaseAggregateAlert
import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil
import grails.converters.JSON
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.text.DecimalFormat
import static com.rxlogix.util.DateUtil.toDateString

@ToString()
@EqualsAndHashCode(callSuper = true)
@DirtyCheck
@CollectionSnapshotAudit
class AggregateOnDemandAlert extends BaseAggregateAlert implements AlertUtil, GroovyInterceptable {
    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','dueDate','justification','undoJustification']]

    def dataObjectService
    def cacheService

    //Configuration related information
    Configuration alertConfiguration
    ExecutedConfiguration executedAlertConfiguration

    String pregnancy

    //PRR values
    Double prrValue
    Double prrLCI
    Double prrUCI
    String prrStr
    String prrStrLCI
    String prrStrUCI
    String prrMh

    //ROR values
    Double rorValue
    Double rorLCI
    Double rorUCI
    String rorStr
    String rorStrLCI
    String rorStrUCI
    String rorMh

    // A,B,C,D,E and RR values
    Double aValue
    Double bValue
    Double cValue
    Double dValue
    Double eValue
    Double rrValue

    //PEC Importance values
    String pecImpHigh
    String pecImpLow

    String format

    //Updates as per PVS-2792
    Integer newCount = 0
    Integer cummCount = 0
    Integer newPediatricCount = 0
    Integer cummPediatricCount = 0
    Integer newInteractingCount = 0
    Integer cummInteractingCount = 0

    Integer newGeriatricCount = 0
    Integer cumGeriatricCount = 0
    Integer newNonSerious = 0
    Integer cumNonSerious = 0

    String ebgmStr
    String eb05Str
    String eb95Str


    String impEvents
    List<String> aggImpEventList = []
    GlobalProductEvent globalIdentity

    Integer prodHierarchyId
    Integer eventHierarchyId
    String newCountsJSON
    String undoJustification
    Map<String, Object> customAuditProperties
    static transients = ['undoJustification','customAuditProperties']


    static hasMany = [pvsAlertTag: PvsAlertTag]
    static constraints = {
        globalIdentity nullable: true
        prrLCI nullable: true
        prrUCI nullable: true
        rorLCI nullable: true
        rorUCI nullable: true
        pregnancy nullable: true
        prrStr nullable: true
        rorStr nullable: true
        format nullable: true, blank: true, maxSize: 8000
        pecImpLow nullable: true
        pecImpHigh nullable: true
        prrStrLCI nullable: true
        prrStrUCI nullable: true
        prrMh nullable: true
        rorStrLCI nullable: true
        rorStrUCI nullable: true
        rorMh nullable: true
        ebgmStr nullable: true
        eb05Str nullable: true
        eb95Str nullable: true
        impEvents nullable: true
        aggImpEventList nullable: true
        prodHierarchyId nullable: true
        eventHierarchyId nullable: true
        aValue nullable: true
        bValue nullable: true
        cValue nullable: true
        dValue nullable: true
        eValue nullable: true
        rrValue nullable: true
        newCountsJSON nullable: true
    }

    static mapping = {
        table("AGG_ON_DEMAND_ALERT")
        id generator: 'sequence', params: [sequence: 'agg_on_demand_sequence']
        executedAlertConfiguration(column: 'exec_configuration_id')
        alertConfiguration(column: 'alert_configuration_id')
        prrLCI(column: 'PRR05')
        prrUCI(column: 'PRR95')
        rorLCI(column: 'ROR05')
        rorUCI(column: 'ROR95')
        prrStrLCI(column: 'PRR_STR05')
        prrStrUCI(column: 'PRR_STR95')
        rorStrLCI(column: 'ROR_STR05')
        rorStrUCI(column: 'ROR_STR95')
        ebgmSubGroup(column: 'EBGM_SUB_GROUP')
        eb95SubGroup(column: 'EB95_SUB_GROUP')
        eb05SubGroup(column: 'EB05_SUB_GROUP')
        newCountsJSON(column: 'NEW_COUNTS_JSON')
        alertConfiguration lazy: false
        executedAlertConfiguration lazy: false
        aggImpEventList joinTable: [name: "AOD_ALERT_IMP_EVENT_LIST", key: "AGG_ON_DEMAND_ALERT_ID", column: "AGA_ON_DEMAND_IMP_EVENTS"], indexColumn: [name: "AGA_IMP_EVENT_LIST_IDX"]
        pvsAlertTag joinTable: [name: "AGG_DEMAND_ALERT_TAGS", column: "PVS_ALERT_TAG_ID", key: "AGG_ALERT_ID"]
        prrStr column: 'PRR_STR', sqlType: DbUtil.stringType
        rorStr column: 'ROR_STR', sqlType: DbUtil.stringType
        ebgmStr type: 'text', sqlType: 'clob'
        eb05Str type: 'text', sqlType: 'clob'
        eb95Str type: 'text', sqlType: 'clob'
        eb95Age type: 'text', sqlType: 'clob'
        eb05Age type: 'text', sqlType: 'clob'
        ebgmAge type: 'text', sqlType: 'clob'
        eb05Gender type: 'text', sqlType: 'clob'
        eb95Gender type: 'text', sqlType: 'clob'
        ebgmGender type: 'text', sqlType: 'clob'
        newCountsJSON type: 'text', sqlType: 'clob'
    }

    Map toDto(Boolean isSpecialPE = false, Map ptMap = [isIme: "", isDme: "", isEi: "", isSm: ""], List<String> tagNameList = null,Boolean isExport = false) {
        String trend = Constants.Commons.EVEN
        def isRor = cacheService?.getRorCache()
        boolean checkRoR = !isRor && this.executedAlertConfiguration.dataMiningVariable
        DecimalFormat df = new DecimalFormat('#.#####')
        Double chisquare = Double.valueOf(df.format(this.chiSquare))
        DecimalFormat df1 = new DecimalFormat('#.###')
        Double eVal = this.eValue ? Double.valueOf(df1.format(this?.eValue as Double)): -1
        Double rrVal = this.rrValue ? Double.valueOf(df1.format(this?.rrValue as Double)) : -1
        Map aggAlertData = [
                id                          : this.id,
                alertName                   : this.name,
                productName                 : this.productName,
                flagged                     : this.flagged as String,
                newStudyCount               : this.newStudyCount,
                cumStudyCount               : this.cumStudyCount,
                newSponCount                : this.newSponCount,
                cumSponCount                : this.cumSponCount,
                newSeriousCount             : this.newSeriousCount,
                cumSeriousCount             : this.cumSeriousCount,
                newFatalCount               : this.newFatalCount,
                cumFatalCount               : this.cumFatalCount,
                positiveRechallenge         : this.positiveRechallenge ?: '-',
                positiveDechallenge         : this.positiveDechallenge ?: '-',
                listed                      : this.listed ?: '-',
                related                     : this.related ?: '-',
                pregnancy                   : this.pregnancy ?: '-',
                prrValue                    : this.prrValue==0 ? '0' : this.prrValue != -1 ? this.prrValue.toString() : '-',
                prrLCI                       : this.prrLCI==0 ? '0' :  this.prrLCI != -1.0 ? this.prrLCI?.toString() : '-',
                prrUCI                       : this.prrUCI==0 ? '0' :  this.prrUCI != -1.0 ? this.prrUCI?.toString() : '-',
                rorValue                    : (checkRoR)? "-": this.rorValue==0 ? '0' :  this.rorValue != -1.0 ? this.rorValue.toString() : '-',
                rorLCI                       : (checkRoR)? "-": this.rorLCI==0 ? '0' :  this.rorLCI != -1.0 ? this.rorLCI?.toString() : '-',
                rorUCI                       : (checkRoR)? "-": this.rorUCI==0 ? '0' :  this.rorUCI!=-1.0 ? this.rorUCI?.toString() : '-',
                eb05                        : this.eb05==0 ? '0' :  this.eb05 !=-1 ? this.eb05: '-' ,
                eb95                        : this.eb95==0 ? '0' :  this.eb95 !=-1 ? this.eb95: '-',
                ebgm                        : this.ebgm==0 ? '0' :  this.ebgm != -1.0 ? this.ebgm : '-',
                highPecImp                  : this.pecImpHigh,
                lowPecImp                   : this.pecImpLow,
                preferredTerm               : this.pt,
                primaryEvent                : this.pt,
                soc                         : this.soc,
                execConfigId                : this.executedAlertConfigurationId,
                alertConfigId               : this.alertConfigurationId,
                productId                   : this.productId as String,
                level                       : getLevel(this.executedAlertConfiguration.productSelection),
                ptCode                      : this.ptCode,
                trend                       : trend,
                dataSource                  : this.alertConfiguration.selectedDatasource.toUpperCase(),
                groupBySmq                  : this.alertConfiguration.groupBySmq,
                lastUpdated                 : this.lastUpdated,
                asOfDate                    : toDateString(this.periodEndDate),
                executionDate               : toDateString(this.dateCreated),
                configId                    : this.alertConfigurationId,
                isSpecialPE                 : isSpecialPE,
                impEvents                   : (isExport && this.impEvents)?replaceImpEventsLabels(this.impEvents).toUpperCase():this.impEvents ? this.impEvents.toUpperCase() : Constants.Commons.BLANK_STRING,
                ime                         : ptMap.isIme,
                dme                         : ptMap.isDme,
                ei                          : ptMap.isEi,
                isSm                        : ptMap.isSm,
                format                      : this.format,
                alertTags                   : tagNameList,
                eb95Age                     : this.eb95Age ?: '-',
                eb05Age                     : this.eb05Age ?: '-',
                ebgmAge                     : this.ebgmAge ?: '-',
                eb05Gender                  : this.eb05Gender ?: '-',
                eb95Gender                  : this.eb95Gender ?: '-',
                ebgmGender                  : this.ebgmGender ?: '-',
                ebgmSubGroup                : this.ebgmSubGroup ?: '-',
                chiSquareSubGroup           : this.chiSquareSubGroup ?: '-',
                rorRelSubGroup             : this.rorRelSubGroup ?: '-',
                rorLciRelSubGroup            : this.rorLciRelSubGroup ?: '-',
                rorUciRelSubGroup          : this.rorUciRelSubGroup ?: '-',
                eb05SubGroup                : this.eb05SubGroup ?: '-',
                eb95SubGroup                : this.eb95SubGroup ?: '-',
                rorSubGroup                 : this.rorSubGroup ?: '-',
                rorLciSubGroup              : this.rorLciSubGroup ?: '-',
                rorUciSubGroup              : this.rorUciSubGroup ?: '-',
                prrSubGroup                 : this.prrSubGroup ?: '-',
                prrLciSubGroup              : this.prrLciSubGroup ?: '-',
                prrUciSubGroup              : this.prrUciSubGroup ?: '-',
                chiSquare                   : !(this.cummCount ==0 && this.newCount ==0) ? chisquare : '0',
                flags                       : '-',
                eb05Str                     : this.eb05Str ?: '-',
                eb95Str                     : this.eb95Str ?: '-',
                ebgmStr                     : this.ebgmStr ?: '-',
                newCount                    : this.newCount,
                cummCount                   : this.cummCount,
                newPediatricCount           : this.newPediatricCount,
                cummPediatricCount          : this.cummPediatricCount,
                newInteractingCount         : this.newInteractingCount,
                cummInteractingCount        : this.cummInteractingCount,
                isFaersOnly                 : this.newCount == -1,
                newGeriatricCount           : this.newGeriatricCount,
                cumGeriatricCount           : this.cumGeriatricCount,
                newNonSerious               : this.newNonSerious,
                cumNonSerious               : this.cumNonSerious,
                fullSoc                     : dataObjectService.getAbbreviationMap(this.soc),
                productSelection            : this.executedAlertConfiguration?.productSelection,
                aValue                      : this.aValue?:0,
                bValue                      : this.bValue?:0,
                cValue                      : this.cValue?:0,
                dValue                      : this.dValue?:0,
                eValue                      : eVal!=-1? eVal : '-',
                rrValue                     : rrVal!=-1? rrVal: '-',

        ]
        if (this.newCountsJSON) {
            Map newCountDataMap = parseJsonString(this.newCountsJSON)
            for (String k: newCountDataMap?.keySet()) {
                String key = k
                String value = newCountDataMap.get(k)
                aggAlertData.put(key, value)
            }
        }
        aggAlertData
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.executedAlertConfiguration.productSelection)
        if (prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }

    int getLevel(String productJson) {
        if (productJson) {
            Map jsonMap = parseJsonString(productJson)
            def level = jsonMap.find { it.value }?.key
            level as int
        }
        0
    }

    def getProductDictionarySelection() {
        this.executedAlertConfiguration.productDictionarySelection
    }
    @Override
    String toString(){
        super.toString()
    }

    def getInstanceIdentifierForAuditLog() {
        return this.executedAlertConfiguration.getInstanceIdentifierForAuditLog() + ": (" + this.productName + "-" + this.pt + ")"
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
