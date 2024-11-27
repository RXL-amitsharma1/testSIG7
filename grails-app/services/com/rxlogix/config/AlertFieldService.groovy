package com.rxlogix.config

import com.rxlogix.ConfigConstants
import com.rxlogix.Constants
import com.rxlogix.signal.RuleInformation
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import java.lang.reflect.Field
import java.util.regex.Matcher
import java.util.regex.Pattern

@Transactional
class AlertFieldService {
    static transactional = false

    def userService
    def dataSource_pva
    def dataSource
    def dataSource_eudra
    def dataSource_faers
    def dataSource_vaers
    def dataSource_vigibase
    def dataSource_jader
    def cacheService
    def customMessageService
    def config = Holders.config


    List<AlertField> fetchAlertFieldsFromMart(def dataSource, String dbType) {
        log.debug("fetchAlertFieldsFromMart execution started.")
        def sql = new Sql(dataSource)
        def rowList = []
        List<GroovyRowResult> data = []
        Pattern pattern = Pattern.compile(" *#OR *")
        try {

            data = sql.rows("SELECT * FROM AGG_RPT_FIELD_MAPPING order by id asc")
            for (int k = 0; k < data?.size(); k++) {
                Matcher matcher = pattern.matcher(data.get(k).DISPLAY)
                String escapedDisplayName = ""
                AlertField alertField = new AlertField()
                alertField.setName(data.get(k).NAME)
                alertField.setDisplay(data.get(k).DISPLAY)
                alertField.setOldDisplay(data.get(k).OLD_DISPLAY)
                alertField.setDbType(dbType)
                alertField.setAlertType(data.get(k).ALERT_TYPE)
                alertField.setKeyId(data.get(k).KEY_ID)
                alertField.setType(data.get(k).TYPE)
                alertField.setEnabled(data.get(k).ENABLED == 1 ? true : false)
                alertField.setOptional(data.get(k).OPTIONAL == 1 ? true : false)
                alertField.setDataType(data.get(k).DATA_TYPE)
                alertField.setIsAutocomplete(data.get(k).ISAUTOCOMPLETE == 1 ? true : false)
                alertField.setIsFilter(data.get(k).IS_FILTER == 1 ? true : false)
                alertField.setIsNewColumn(data.get(k).IS_NEW_COLUMN == 1 ? true : false)
                alertField.setIsVisible(data.get(k).IS_VISIBLE == 1 ? true : false)
                alertField.setIsSmqVisible(data.get(k).IS_SMQ_VISIBLE == 1 ? true : false)
                alertField.setIsAdvancedFilterField(data.get(k).IS_ADVANCED_FILTER_FIELD == 1 ? true : false)
                alertField.setIsBusinessRuleField(data.get(k).IS_BUSINESS_RULE_FIELD == 1 ? true : false)
                alertField.setIsHyperLink(data.get(k).IS_HYPERLINK == 1 ? true : false)
                alertField.setContainerView(new BigDecimal(data.get(k).CONTAINER_VIEW).intValue())
                alertField.setIsAdhocVisible(data.get(k).IS_ADHOC_VISIBLE == 1 ? true : false)
                alertField.setFlagEnabled(data.get(k).PRR_ROR_EBGM_FLAG)
                if(matcher.find()) {
                    String matchWord = matcher.group(0)
                    escapedDisplayName = data.get(k).DISPLAY.replace(matchWord, "#OR")
                    alertField.setDisplay(escapedDisplayName)
                }
                rowList.add(alertField)
            }

        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            sql.close()
            log.debug("fetchAlertFieldsFromMart execution finished.")
        }
        rowList
    }
    List<AlertField> fetchOnDemandAlertFieldsFromMart(String dbType) {
        log.debug("fetchOnDemandAlertFieldsFromMart execution started.")
        String alertType = "AGGREGATE_CASE_ALERT"
        List<String> availableDataSources = Holders.config.pvsignal.supported.datasource.call()
        boolean isFaersEnabled = availableDataSources.contains(Constants.DataSource.FAERS)
        boolean isVaersEnabled = availableDataSources.contains(Constants.DataSource.VAERS)
        boolean isVigibaseEnabled = availableDataSources.contains(Constants.DataSource.VIGIBASE)
        Boolean isRor = cacheService.getRorCache()
        Map flagsMap = [
                dssFlag            : Holders.config.statistics.enable.dss,
                prrFlag            : Holders.config.statistics.enable.prr,
                prrFlagFaers       : isFaersEnabled && Holders.config.statistics.faers.enable.prr,
                prrFlagVaers       : isVaersEnabled && Holders.config.statistics.vaers.enable.prr,
                prrFlagVigibase    : isVigibaseEnabled && Holders.config.statistics.vigibase.enable.prr,
                rorFlag            : Holders.config.statistics.enable.ror,
                rorFlagFaers       : isFaersEnabled && Holders.config.statistics.faers.enable.ror,
                rorFlagVaers       : isVaersEnabled && Holders.config.statistics.vaers.enable.ror,
                rorFlagVigibase    : isVigibaseEnabled && Holders.config.statistics.vigibase.enable.ror,
                ebgmFlag           : Holders.config.statistics.enable.ebgm,
                ebgmFlagFaers      : isFaersEnabled && Holders.config.statistics.faers.enable.ebgm,
                ebgmFlagVaers      : isVaersEnabled && Holders.config.statistics.vaers.enable.ebgm,
                ebgmFlagVigibase   : isVigibaseEnabled && Holders.config.statistics.vigibase.enable.ebgm,
                customFieldsEnabled: Holders.config.custom.qualitative.fields.enabled,
        ]
        List cummTypeCountList = Holders.config.agaColumn.cumm.type.clone() as List
        List staticFieldList = []
        if(dbType == Constants.SystemPrecheck.FAERS){
           staticFieldList =  cacheService.getAlertFields("AGGREGATE_CASE_ALERT").findAll {
                it.name in ["productName","soc","pt","alertTags","impEvents","listed","aValue","bValue","cValue","dValue","eValue"]
            }
        }else if(dbType == Constants.SystemPrecheck.VAERS || dbType == Constants.SystemPrecheck.VIGIBASE){
            staticFieldList =  cacheService.getAlertFields("AGGREGATE_CASE_ALERT").findAll {
                it.name in ["productName","soc","pt","alertTags","listed"]
            }
        }
        List<AlertField> aggOnDemandField = staticFieldList
        aggOnDemandField +=  cacheService.getAlertFields(alertType).findAll {
           it.isAdhocVisible == true && it.dbType == dbType && it.type != 'subGroup' && it.enabled == true
       }
        List<AlertField> enabledAlertField = []
        try {
            aggOnDemandField.each{ AlertField colInfoRow ->
                String evalCondition = colInfoRow.flagEnabled
                // x -> PRR, y -> ROR, z -> EBGM
                boolean isEnabledBasedOnFlag = true
                if(dbType == Constants.SystemPrecheck.FAERS){
                    isEnabledBasedOnFlag = Eval.xyz(flagsMap.prrFlagFaers, flagsMap.rorFlagFaers, flagsMap.ebgmFlagFaers, evalCondition)
                    colInfoRow.name = colInfoRow.name.replace("Faers","")
                }else if(dbType == Constants.SystemPrecheck.VAERS){
                    isEnabledBasedOnFlag = Eval.xyz(flagsMap.prrFlagVaers, flagsMap.rorFlagVaers, flagsMap.ebgmFlagVaers, evalCondition)
                    colInfoRow.name = colInfoRow.name.replace("Vaers","")
                }else if(dbType == Constants.SystemPrecheck.VIGIBASE){
                    colInfoRow.name = colInfoRow.name.replace("Vigibase","")
                    isEnabledBasedOnFlag = Eval.xyz(flagsMap.prrFlagVigibase, flagsMap.rorFlagVigibase, flagsMap.ebgmFlagVigibase, evalCondition)
                }else{
                    isEnabledBasedOnFlag = Eval.xyz(flagsMap.prrFlag, flagsMap.rorFlag, flagsMap.ebgmFlag, evalCondition)
                }
                if (isEnabledBasedOnFlag) {
                    if (colInfoRow.name == "rorLCI" || colInfoRow.name == "rorValue") {
                        String newRorLabel = ""
                        if (!isRor && colInfoRow.display?.split("#OR")?.size() > 1) {
                            newRorLabel = colInfoRow.display?.split("#OR")[1]
                        } else {
                            newRorLabel = colInfoRow.display?.split("#OR")[0]
                        }
                        colInfoRow.display = newRorLabel
                    }
                    if(colInfoRow.name  == 'prrLCI'){
                        colInfoRow.secondaryName = "prrUCI"
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.name == 'rorLCI'){
                        colInfoRow.secondaryName = "rorUCI"
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.name == 'eb05'){
                        colInfoRow.secondaryName = "eb95"
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.type == 'count' && colInfoRow.name?.startsWith("new")){
                        String secondaryName
                        if(colInfoRow.name in cummTypeCountList){
                            secondaryName = colInfoRow.name?.replace("new","cumm")
                        }else{
                            secondaryName = colInfoRow.name?.replace("new","cum")
                        }
                        colInfoRow.secondaryName = secondaryName
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.keyId){
                        String dbKey = colInfoRow.keyId?.replace("REPORT","NEW")?.replace("FLG","COUNT")
                        colInfoRow.dbKey = dbKey
                    }
                    if( colInfoRow.name in ['listed', 'newSeriousCount', 'newFatalCount', 'eb05', 'newCount']){
                        // need to do for other dataSource
                        colInfoRow.containerView = 1
                    } else if(colInfoRow.containerView != 1){
                        // for Adhoc view container view is 3 for all optional columns
                        colInfoRow.containerView = 3
                    }

                    if(colInfoRow.name == "hlt"){
                        colInfoRow.dbKey = "HLT_NAME"
                        colInfoRow.type = "text"
                    }else if(colInfoRow.name == "hlgt"){
                        colInfoRow.dbKey = "HLGT_NAME"
                        colInfoRow.type = "text"
                    }else if(colInfoRow.name == "smqNarrow"){
                        colInfoRow.dbKey = "SMQ_NARROW_NAME"
                        colInfoRow.type = "text"
                    }
                    enabledAlertField.add(colInfoRow)
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            log.debug("fetchAlertFieldsFromMart execution finished.")
        }
        enabledAlertField
    }

    List<AlertField> fetchJaderAlertFieldsFromMart(List jaderField,Boolean isAdhoc = false) {
        log.debug("fetchJaderAlertFieldsFromMart execution started.")
        Boolean isRor = cacheService.getRorCache()
        Map flagsMap = [
                prrFlagJader    : Holders.config.statistics.jader.enable.prr,
                rorFlagJader    : Holders.config.statistics.jader.enable.ror,
                ebgmFlagJader   : Holders.config.statistics.jader.enable.ebgm,
                customFieldsEnabled: Holders.config.custom.qualitative.fields.enabled,
        ]
        List newJaderFields = jaderField
        if(isAdhoc){
            newJaderFields = jaderField.findAll{ it.isAdhocVisible == true}
        }
        List<AlertField> enabledAlertField = []
        try {
            newJaderFields.each{ AlertField colInfoRow ->
                String evalCondition = colInfoRow.flagEnabled
                // x -> PRR, y -> ROR, z -> EBGM
                boolean isEnabledBasedOnFlag = true
                isEnabledBasedOnFlag = Eval.xyz(flagsMap.prrFlagJader, flagsMap.rorFlagJader, flagsMap.ebgmFlagJader, evalCondition)
                if(colInfoRow.name == "priority"){
                    isEnabledBasedOnFlag = Holders.config.alert.priority.enable
                }
                if (isEnabledBasedOnFlag) {
                    if (colInfoRow.name == "rorLCIJader" || colInfoRow.name == "rorValueJader") {
                        String newRorLabel = ""
                        if (!isRor && colInfoRow.display?.split("#OR")?.size() > 1) {
                            newRorLabel = colInfoRow.display?.split("#OR")[1]
                        } else {
                            newRorLabel = colInfoRow.display?.split("#OR")[0]
                        }
                        colInfoRow.display = newRorLabel
                    }
                    if(colInfoRow.name  == 'prrLCIJader'){
                        colInfoRow.secondaryName = "prrUCIJader"
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.name == 'rorLCIJader'){
                        colInfoRow.secondaryName = "rorUCIJader"
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.name == 'eb05Jader'){
                        colInfoRow.secondaryName = "eb95Jader"
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.type == 'count' && colInfoRow.name?.startsWith("new")){
                        String secondaryName = colInfoRow.name?.replace("new","cum")
                        colInfoRow.secondaryName = secondaryName
                        colInfoRow.type = 'countStacked'
                    }
                    if(colInfoRow.keyId){
                        String dbKey = colInfoRow.keyId?.replace("REPORT","NEW")?.replace("FLG","COUNT")
                        colInfoRow.dbKey = dbKey
                    }
                    if(isAdhoc && colInfoRow.name.endsWith("Jader")){
                        colInfoRow.name = colInfoRow.name.replace("Jader","")
                        if(colInfoRow.secondaryName) {
                            colInfoRow.secondaryName = colInfoRow.secondaryName.replace("Jader", "")
                            if (colInfoRow.secondaryName in ['cumPediatricCount','cumCount']) {
                                colInfoRow.secondaryName = colInfoRow.secondaryName.replace("cum", "cumm")
                            }
                        }
                    }
                    enabledAlertField.add(colInfoRow)
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            log.debug("fetchAlertFieldsFromMart execution finished.")
        }
        enabledAlertField
    }

    void initiateDefaultAggregateAlertFields() {
        try {
            insertAlertFieldsInMart(Constants.SystemPrecheck.SAFETY);
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.faers.enabled) {
                insertAlertFieldsInMart(Constants.SystemPrecheck.FAERS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.vigibase.enabled) {
                insertAlertFieldsInMart(Constants.SystemPrecheck.VIGIBASE);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.vaers.enabled) {
                insertAlertFieldsInMart(Constants.SystemPrecheck.VAERS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.jader.enabled) {
                insertAlertFieldsInMart(Constants.SystemPrecheck.JADER);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.evdas.enabled) {
                insertAlertFieldsInMart(Constants.SystemPrecheck.EVDAS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }

    }

    void insertAlertFieldsInMart(String dbType) {
        def dataSource
        if (dbType.equals(Constants.SystemPrecheck.SAFETY)) {
            dataSource = dataSource_pva
        } else if (dbType.equals(Constants.SystemPrecheck.FAERS)) {
            dataSource = dataSource_faers
        } else if (dbType.equals(Constants.SystemPrecheck.VAERS)) {
            dataSource = dataSource_vaers
        } else if (dbType.equals(Constants.SystemPrecheck.VIGIBASE)) {
            dataSource = dataSource_vigibase
        } else if (dbType.equals(Constants.SystemPrecheck.JADER)) {
            dataSource = dataSource_jader
        } else if (dbType.equals(Constants.SystemPrecheck.EVDAS)) {
            dataSource = dataSource_eudra
        }
        def sql = new Sql(dataSource)
        try {
            int count=sql.rows("select count(1) as fieldCount  from AGG_RPT_FIELD_MAPPING WHERE TYPE !='subGroup'").get(0).fieldCount
            if(count<=0)
            {
                def fileReader =  Thread.currentThread().contextClassLoader.getResource("defaultAggregateField_${dbType}.sql")
                String sqlString = new File(fileReader.getFile()).text
                sqlString.split(";").each {
                    sql.execute(it)
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            sql?.close()
        }

    }

    void updateAlertFields() {
        log.debug("updateAlertFields method calling...")
        List<AlertField> alertFields = new ArrayList<>()

        try {
            log.debug("Fetching alert fields from safety db...")
            alertFields = alertFields + fetchAlertFieldsFromMart(dataSource_pva, Constants.SystemPrecheck.SAFETY);
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.faers.enabled) {
                log.debug("Fetching alert fields from faers db...")
                alertFields = alertFields + fetchAlertFieldsFromMart(dataSource_faers, Constants.SystemPrecheck.FAERS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.vigibase.enabled) {
                log.debug("Fetching alert fields from vigibase db...")
                alertFields = alertFields + fetchAlertFieldsFromMart(dataSource_vigibase, Constants.SystemPrecheck.VIGIBASE);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.vaers.enabled) {
                log.debug("Fetching alert fields from vaers db...")
                alertFields = alertFields + fetchAlertFieldsFromMart(dataSource_vaers, Constants.SystemPrecheck.VAERS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.jader.enabled) {
                log.debug("Fetching alert fields from jader db...")
                List jaderFields = fetchAlertFieldsFromMart(dataSource_jader, Constants.SystemPrecheck.JADER)
                List enabledJaderFields = fetchJaderAlertFieldsFromMart(jaderFields,false)
                cacheService.setJaderAlertFields(Constants.DataSource.JADER,enabledJaderFields)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.evdas.enabled) {
                log.debug("Fetching alert fields from evdas db...")
                alertFields = alertFields + fetchAlertFieldsFromMart(dataSource_eudra, Constants.SystemPrecheck.EVDAS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        cacheService.setAlertFields('AGGREGATE_CASE_ALERT', [])
        cacheService.removeAlertFields('AGGREGATE_CASE_ALERT')
        cacheService.setAlertFields('AGGREGATE_CASE_ALERT', alertFields)

        log.debug("Alert fields saved.")
    }

    void updateOnDemandAlertFields() {
        log.debug("updateAlertFields method calling...")

        try {
            log.debug("Fetching alert fields from safety db...")
            List<AlertField> alertFields  = fetchOnDemandAlertFieldsFromMart(Constants.SystemPrecheck.SAFETY)
            cacheService.setOnDemandAlertFields(Constants.DataSource.PVA,alertFields)
            if (Holders.config.signal.faers.enabled) {
                log.debug("Fetching alert fields from faers db...")
                List<AlertField> alertFieldsFaers = fetchOnDemandAlertFieldsFromMart(Constants.SystemPrecheck.FAERS)
                cacheService.setOnDemandAlertFields(Constants.DataSource.FAERS,alertFieldsFaers)
            }
            if (Holders.config.signal.vigibase.enabled) {
                log.debug("Fetching alert fields from Vigibase db...")
                List<AlertField> alertFieldsVigibase = fetchOnDemandAlertFieldsFromMart(Constants.SystemPrecheck.VIGIBASE)
                cacheService.setOnDemandAlertFields(Constants.DataSource.VIGIBASE,alertFieldsVigibase)
            }
            if (Holders.config.signal.vaers.enabled) {
                log.debug("Fetching alert fields from Vaers db...")
                List<AlertField> alertFieldsVaers = fetchOnDemandAlertFieldsFromMart(Constants.SystemPrecheck.VAERS)
                cacheService.setOnDemandAlertFields(Constants.DataSource.VAERS,alertFieldsVaers)
            }
            if(Holders.config.signal.jader.enabled){
                log.debug("Saving Adhoc alert fields for Jader db...")
                List jaderFields = fetchAlertFieldsFromMart(dataSource_jader, Constants.SystemPrecheck.JADER)
                List<AlertField> enabledJaderFields = fetchJaderAlertFieldsFromMart(jaderFields,true)
                cacheService.setOnDemandAlertFields(Constants.DataSource.JADER,enabledJaderFields)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        log.debug("Alert fields saved.")
    }
    List getAggOnDemandColumnList(String dataSource, boolean isSmq = false, boolean onlyNewFields = false){
        List aggOnDemandColumnList  = new ArrayList()
        List alertFieldList = new ArrayList()
        if(isSmq) {
            if(onlyNewFields) {
                alertFieldList = cacheService.getOnDemandAlertFields(dataSource)?.findAll {
                    it.isSmqVisible == true && it.isNewColumn == true
                }
            }else{
                alertFieldList = cacheService.getOnDemandAlertFields(dataSource)?.findAll {
                    it.isSmqVisible == true
                }
            }
        }else{
            if(onlyNewFields){
                alertFieldList = cacheService.getOnDemandAlertFields(dataSource)?.findAll{
                    it.isNewColumn == true
                }
            }else {
                alertFieldList = cacheService.getOnDemandAlertFields(dataSource)
            }
        }
                aggOnDemandColumnList = alertFieldList.collect {
                    [
                            optional             : it.optional,
                            enabled              : it.enabled,
                            isAutocomplete       : it.isAutocomplete,
                            isHyperLink          : it.isHyperLink,
                            isNewColumn          : it.isNewColumn,
                            name                 : it.name,
                            display              : (isSmq && it?.display.split("#OR").size() > 1) ? it.display.split("#OR")[1] : it.display.split("#OR")[0],
                            dataType             : it.dataType,
                            dbType               : it.dbType,
                            alertType            : it.alertType,
                            containerView        : it.containerView,
                            type                 : it.type,
                            isFilter             : it.isFilter,
                            isVisible            : it.isVisible,
                            isSmqVisible         : it.isSmqVisible,
                            isAdvancedFilterField: it.isAdvancedFilterField,
                            keyId                : it.keyId,
                            isBusinessRuleField  : it.isBusinessRuleField,
                            secondaryName        : it.secondaryName,
                            cssClass             : it.cssClass,
                            flagEnabled          : it.flagEnabled,
                            dbKey                : it.dbKey
                    ]
                }
        return aggOnDemandColumnList
    }
    List getJaderColumnList(String dataSource, boolean isSmq = false){
        List jaderColumnList  = new ArrayList()
        List alertFieldList = new ArrayList()
        if(isSmq) {
                alertFieldList = cacheService.getJaderAlertFields(dataSource)?.findAll {
                    it.isSmqVisible == true
                }
        }else{
                alertFieldList = cacheService.getJaderAlertFields(dataSource)
        }
        jaderColumnList = alertFieldList.collect {
            [
                    optional             : it.optional,
                    enabled              : it.enabled,
                    isAutocomplete       : it.isAutocomplete,
                    isHyperLink          : it.isHyperLink,
                    isNewColumn          : it.isNewColumn,
                    name                 : it.name,
                    display              : (isSmq && it?.display.split("#OR").size() > 1) ? it.display.split("#OR")[1] : it.display.split("#OR")[0],
                    dataType             : it.dataType,
                    dbType               : it.dbType,
                    alertType            : it.alertType,
                    containerView        : it.containerView,
                    type                 : it.type,
                    isFilter             : it.isFilter,
                    isVisible            : it.isVisible,
                    isSmqVisible         : it.isSmqVisible,
                    isAdvancedFilterField: it.isAdvancedFilterField,
                    keyId                : it.keyId,
                    isBusinessRuleField  : it.isBusinessRuleField,
                    secondaryName        : it.secondaryName,
                    cssClass             : it.cssClass,
                    flagEnabled          : it.flagEnabled,
                    dbKey                : it.dbKey
            ]
        }
        return jaderColumnList
    }


    def alertFields(String dbType, String alertType, Boolean needNewFields = null, Boolean isAdvancedFilterField = null, Boolean isBusinessRuleField = null, Boolean enableCheckFlag = false) {
        def alertFieldsTemp
        if (needNewFields == null) {
            if (isAdvancedFilterField == true) {
                alertFieldsTemp = cacheService.getAlertFields(alertType).findAll {
                    it.dbType == dbType && !dbType.equals("") && (it.isAdvancedFilterField.equals(true) || it.isAdvancedFilterField.equals("true"))
                }
            } else if (isBusinessRuleField == true) {
                alertFieldsTemp = cacheService.getAlertFields(alertType).findAll {
                    it.dbType == dbType && !dbType.equals("") && it.isBusinessRuleField == true
                }
            } else {
                alertFieldsTemp = cacheService.getAlertFields(alertType).findAll {
                    it.dbType == dbType && !dbType.equals("")
                }
            }
        } else {
            alertFieldsTemp = cacheService.getAlertFields(alertType).findAll {
                it.dbType == dbType && !dbType.equals("") && it.isNewColumn == needNewFields
            }
        }

        def alertFields = alertFieldsTemp.collect {
            [
                    optional             : it.optional,
                    enabled              : it.enabled,
                    isAutocomplete       : it.isAutocomplete,
                    isHyperLink          : it.isHyperLink,
                    isNewColumn          : it.isNewColumn,
                    name                 : it.name,
                    key                  : it.name,
                    display              : it.display,
                    oldDisplay           : it.oldDisplay,
                    label                : it.display,
                    value                : it.display,
                    dataType             : it.dataType,
                    dbType               : it.dbType,
                    alertType            : it.alertType,
                    containerView        : it.containerView,
                    type                 : it.type,
                    isFilter             : it.isFilter,
                    isVisible            : it.isVisible,
                    isSmqVisible         : it.isSmqVisible,
                    isAdvancedFilterField: it.isAdvancedFilterField,
                    keyId                : it.keyId,
                    isBusinessRuleField  : it.isBusinessRuleField

            ]
        }

        if (enableCheckFlag == true) {
            alertFields = alertFields.findAll { it.enabled }
        }

        return alertFields;
    }

    def getCommentTemplateCount() {
        List fieldList = getAlertFields('AGGREGATE_CASE_ALERT', null, true, null)?.findAll { it.enabled == true }
        List<Map> fieldList1 = []
        for (int i = 0; i < fieldList.size(); i++) {
            if (fieldList.get(i).name.contains("Count") || fieldList.get(i).name.contains("new") || fieldList.get(i).name.contains("tot")) {
                fieldList1.add([name: fieldList.get(i).name, enabled: fieldList.get(i).enabled, display: fieldList.get(i).display.contains("/")? fieldList.get(i).display.split("/")[0]:fieldList.get(i).display, dataType: fieldList.get(i).dataType])
                fieldList1.add([name: fieldList.get(i).name, enabled: fieldList.get(i).enabled, display: fieldList.get(i).display.contains("/")?fieldList.get(i).display.split("/")[1]:fieldList.get(i).display, dataType: fieldList.get(i).dataType])
            }else if(fieldList.get(i).name in ["hlgt","hlt","smqNarrow"]){
                fieldList1.add([name: fieldList.get(i).name, enabled: fieldList.get(i).enabled, display: fieldList.get(i).display, dataType: fieldList.get(i).dataType])
            }
        }
        fieldList1.removeAll{
            it.name in ["hlt","hlgt","smqNarrow"]
        }
        return fieldList1
    }


    def getAlertFields(String alertType, Boolean needNewFields = null, Boolean isAdvancedFilterField = null, Boolean isBusinessRuleField = null,Boolean onlyJaderField = false) {
        List fieldList = alertFields(Constants.SystemPrecheck.SAFETY, alertType, needNewFields, isAdvancedFilterField, isBusinessRuleField, true) as List<Map>
            if (Holders.config.signal.faers.enabled) {
                List fieldListFaers = alertFields(Constants.SystemPrecheck.FAERS, alertType, needNewFields, isAdvancedFilterField, isBusinessRuleField, true) as List<Map>
                fieldList += fieldListFaers
            }
            if (Holders.config.signal.evdas.enabled) {
                List fieldListEvdas = alertFields(Constants.SystemPrecheck.EVDAS, alertType, needNewFields, isAdvancedFilterField, isBusinessRuleField, true) as List<Map>
                fieldList += fieldListEvdas
            }
            if (Holders.config.signal.vaers.enabled) {
                List fieldListVaers = alertFields(Constants.SystemPrecheck.VAERS, alertType, needNewFields, isAdvancedFilterField, isBusinessRuleField, true) as List<Map>
                fieldList += fieldListVaers
            }
            if (Holders.config.signal.vigibase.enabled) {
                List fieldListVigibase = alertFields(Constants.SystemPrecheck.VIGIBASE, alertType, needNewFields, isAdvancedFilterField, isBusinessRuleField, true) as List<Map>
                fieldList += fieldListVigibase
            }
        fieldList
    }

    Map getLabelConfigMap(String alertType){
        Map labelConfig = getAlertFields(alertType, null, null, null).collectEntries {
            b -> [b.name, b.display]
        }
        return labelConfig
    }

    Map fetchLabelConfigMap(String dataSource, String dbSuffix) {
        // All the fields label related prepartion should be done here
        final String EB05_PREFIX = "eb05"
        final String PRRLCI_PREFIX = "prrLCI"
        final String RORLCI_PREFIX = "rorLCI"
        final String RORVALUE_PREFIX = "rorValue"

        List<Map> fieldList
        if(dataSource=='JADER'){
            List jaderColumnList = getJaderColumnList(Constants.DataSource.JADER,false)
            Map jaderConfigDisplay = [:]
            jaderColumnList.each{ it ->
                if(it.secondaryName != null &&  it.display.split("/").size() > 1){
                    jaderConfigDisplay.put(it.name, it.display.split("/")[0])
                    jaderConfigDisplay.put(it.secondaryName, it.display.split("/")[1])
                }else{
                    jaderConfigDisplay.put(it.name, it.display)
                }
            }
            return jaderConfigDisplay
        }else{
            fieldList = alertFields(dataSource, "AGGREGATE_CASE_ALERT", null, true, true, true)
        }
        Map labelConfigMap = [:]

        // Extracted common split logic to a closure
        def splitLogic = { value -> value.split("/") }

        Boolean isRor = cacheService.getRorCache()
        // Iterate over the fieldList and populate labelConfigMap
        fieldList.each { it ->
            def name = it.name
            def value = it.value

            if (name.contains("new") && value.contains("/")) {
                def values = splitLogic(value)
                labelConfigMap[name] = values[0]
                labelConfigMap[name.replace("new", "cum")] = values[1]
            } else if (name == EB05_PREFIX + dbSuffix) {
                def values = splitLogic(value)
                labelConfigMap[name] = values[0]
                labelConfigMap[name.replace(EB05_PREFIX, "eb95")] = values[1]
            } else if (name == PRRLCI_PREFIX + dbSuffix) {
                def values = splitLogic(value)
                labelConfigMap[name] = values[0]
                labelConfigMap[name.replace(PRRLCI_PREFIX, "prrUCI")] = values[1]
            } else if (name == RORLCI_PREFIX + dbSuffix) {
                def rorLabel = !isRor && value.split("#OR").size() > 1 ? value.split("#OR")[1] : value.split("#OR")[0]
                def values = splitLogic(rorLabel)
                labelConfigMap[name] = values[0]
                labelConfigMap[name.replace(RORLCI_PREFIX, "rorUCI")] = values[1]
            } else if (name == RORVALUE_PREFIX + dbSuffix) {
                def rorValueLabel = !isRor && value.split("#OR").size() > 1 ? value.split("#OR")[1] : value.split("#OR")[0]
                labelConfigMap[name] = rorValueLabel
            } else if (name == "freqPeriod") {
                def values = splitLogic(value)
                labelConfigMap["freqPeriod"] = values[0]
                labelConfigMap["cumFreqPeriod"] = values[1]
            } else {
                labelConfigMap[name] = value
            }
        }
        return labelConfigMap
    }
    void updateBusinessRuleFields() {
        Field[] fields = Constants.BusinessConfigAttributes.class.getDeclaredFields();
        List rules = RuleInformation.findAll().collect {
            !it.businessConfiguration.dataSource.equals( "eudra" )
        };
        try {
            for (int i = 0; i < rules.size(); i++) {
                String rule = rules.get(i).ruleJSON
                fields.each {
                    rule = rule.replaceAll(it.name, it.get(it.name))
                }
                rules.get(i).ruleJSON = rule
                rules.get(i).save()
            }
        } catch (Exception e) {
            println("########## Some error occurred in Migrating Business Rules Rule Information #############")
            e.printStackTrace()
        } finally {
        }
    }


    void updatePreviousCommentTemplate() {
        try {
            List<CommentTemplate> commentTemplates = CommentTemplate.findAll()
            List fields = getAlertFields('AGGREGATE_CASE_ALERT', null)
            List countfieldOldValue = []
            List countfieldNewValue = []
            commentTemplates.each {
                for (int i = 0; i < fields.size(); i++) {
                    if (fields.get(i).name.equals("pt")) {
                        it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[0] + ">/" + "<" + fields.get(i).oldDisplay.split("#OR")[1] + ">", "<" + fields.get(i).display.split("#OR")[0] + ">/" + "<" + fields.get(i).display.split("#OR")[1] + ">")
                        it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[0] + " >/" + "< " + fields.get(i).oldDisplay.split("#OR")[1] + ">", "<" + fields.get(i).display.split("#OR")[0] + " >/" + "< " + fields.get(i).display.split("#OR")[1] + ">")
                    } else if (fields.get(i).oldDisplay.contains("/") && !fields.get(i).name.contains("new") && !fields.get(i).oldDisplay.contains("#OR")) {
                        if (fields.get(i).name.equals("rationale")) {
                            it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[0] + "" + fields.get(i).oldDisplay.split("#OR")[1] + ">", "<" + fields.get(i).display.split("#OR")[0] + "" + fields.get(i).display.split("#OR")[1] + ">")
                            it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[0] + " " + fields.get(i).oldDisplay.split("#OR")[1] + " >", "<" + fields.get(i).display.split("#OR")[0] + " " + fields.get(i).display.split("#OR")[1] + " >")
                        } else {
                            it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("/")[0] + ">", "<" + fields.get(i).display.split("/")[0] + ">")
                            it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("/")[1] + ">", "<" + fields.get(i).display.split("/")[1] + ">")
                        }
                    } else if (fields.get(i).oldDisplay.contains("#OR") && !fields.get(i).name.equals("rationale")) {
                        if (fields.get(i).name in ['rorValue']) {
                            it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[0] + ">", "<" + fields.get(i).display.split("#OR")[0] + ">")
                            it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[1] + ">", "<" + fields.get(i).display.split("#OR")[1] + ">")
                        } else {
                            if (fields.get(i).oldDisplay.contains("#OR") && fields.get(i).oldDisplay.contains("/")) {
                                it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[0].split("/")[0] + ">", "<" + fields.get(i).display.split("#OR")[0].split("/")[0] + ">")
                                it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[0].split("/")[1] + ">", "<" + fields.get(i).display.split("#OR")[0].split("/")[1] + ">")
                                it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[1].split("/")[0] + ">", "<" + fields.get(i).display.split("#OR")[1].split("/")[0] + ">")
                                it.comments = it.comments.replace("<" + fields.get(i).oldDisplay.split("#OR")[1].split("/")[1] + ">", "<" + fields.get(i).display.split("#OR")[1].split("/")[1] + ">")
                            }
                        }
                    } else if (!fields.get(i).name.equals("rationale")) {
                        if(fields.get(i).oldDisplay.contains("/") && fields.get(i).name.contains("new")) {
                            countfieldOldValue = fields.get(i).oldDisplay.split("/")
                            countfieldNewValue = fields.get(i).display.split("/")
                            if(countfieldOldValue?.size() > 0 && countfieldNewValue?.size() > 0) {
                                it.comments = it.comments.replace(countfieldOldValue[0], countfieldNewValue[0])
                                it.comments = it.comments.replace(countfieldOldValue[1], countfieldNewValue[1])
                            }
                        }else{
                            it.comments = it.comments.replace(fields.get(i).oldDisplay, fields.get(i).display)
                        }
                    }
                }
                it.save();
            }
        }catch(Exception ex){
            ex.printStackTrace()
        }

    }

    void updateOldDisplayValue() {
        try {
            updateOldDisplayValueInMart(Constants.SystemPrecheck.SAFETY);
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.faers.enabled) {
                updateOldDisplayValueInMart(Constants.SystemPrecheck.FAERS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.vigibase.enabled) {
                updateOldDisplayValueInMart(Constants.SystemPrecheck.VIGIBASE);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.vaers.enabled) {
                updateOldDisplayValueInMart(Constants.SystemPrecheck.VAERS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        try {
            if (Holders.config.signal.evdas.enabled) {
                updateOldDisplayValueInMart(Constants.SystemPrecheck.EVDAS);
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }

    }

    void updateOldDisplayValueInMart(String dbType) {
        log.debug("updateOldDisplayValueInMart execution started.")
        def dataSource
        if (dbType.equals(Constants.SystemPrecheck.SAFETY)) {
            dataSource = dataSource_pva
        } else if (dbType.equals(Constants.SystemPrecheck.FAERS)) {
            dataSource = dataSource_faers
        } else if (dbType.equals(Constants.SystemPrecheck.VAERS)) {
            dataSource = dataSource_vaers
        } else if (dbType.equals(Constants.SystemPrecheck.VIGIBASE)) {
            dataSource = dataSource_vigibase
        } else if (dbType.equals(Constants.SystemPrecheck.EVDAS)) {
            dataSource = dataSource_eudra
        }
        def sql = new Sql(dataSource)
        try {
            sql.execute("update AGG_RPT_FIELD_MAPPING set OLD_DISPLAY=DISPLAY")
        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            sql.execute("COMMIT")
            sql?.close()
            log.debug("updateOldDisplayValueInMart execution finished.")
        }

    }
    Map getColumnInfoExport(boolean adhocRun,boolean groupBySmq,boolean isJader = false){
        Map labelConfig = [:]
        Map doubleStackedCountMap = config.agaColumnExcelExportMap.clone() as Map
        if (adhocRun) {
            List aggOnDemandColumnList =  getAggOnDemandColumnList(Constants.DataSource.PVA, groupBySmq, false)
            if(Holders.config.signal.faers.enabled){
                aggOnDemandColumnList +=  getAggOnDemandColumnList(Constants.DataSource.FAERS, groupBySmq, false)
            }
            if(Holders.config.signal.vaers.enabled){
                aggOnDemandColumnList +=  getAggOnDemandColumnList(Constants.DataSource.VAERS, groupBySmq, false)
            }
            if(Holders.config.signal.vigibase.enabled){
                aggOnDemandColumnList +=  getAggOnDemandColumnList(Constants.DataSource.VIGIBASE, groupBySmq, false)
            }
            if(Holders.config.signal.jader.enabled){
                aggOnDemandColumnList +=  getAggOnDemandColumnList(Constants.DataSource.JADER, groupBySmq, false)
            }
            aggOnDemandColumnList.each {b ->
                String[] displayString = b.display?.split("/")
                if(displayString.size() > 1) {
                    if(doubleStackedCountMap.containsKey(b.name)){
                        labelConfig.put(displayString[0], b.name)
                        labelConfig.put(displayString[1], doubleStackedCountMap.get(b.name))
                    }else if (b.name.startsWith("new")) {
                            String newCountKey = b.name
                            String cumCountKey = b.name.replace("new", "cum")
                            labelConfig.put(displayString[0], newCountKey)
                            labelConfig.put(displayString[1], cumCountKey)
                    }
                }else{
                    labelConfig.put(b.display, b.name)
                }
            }
        } else {
            List alertColList = []
            if(isJader){
                alertColList = getJaderColumnList(Constants.DataSource.JADER,groupBySmq)
            }else{
                alertColList =  getAlertFields('AGGREGATE_CASE_ALERT')
            }
            alertColList.each {b ->
                String[] displayString = b.display?.split("/")
                if(displayString.size() > 1) {
                    if(doubleStackedCountMap.containsKey(b.name)){
                        labelConfig.put(displayString[0], b.name)
                        labelConfig.put(displayString[1], doubleStackedCountMap.get(b.name))
                    }else if(b.name.startsWith("new")){
                        String newCountKey = b.name
                        String cumCountKey = b.name.replace("new","cum")
                        labelConfig.put(displayString[0], newCountKey)
                        labelConfig.put(displayString[1], cumCountKey)
                    }
                }else{
                    labelConfig.put(b.display, b.name)
                }
            }
        }
        labelConfig
    }

    void updateMeddraFields() {
        log.info( "updateMeddraFields execution started." )
        def sql = new Sql( dataSource_pva )
        def sql_faers = null
        try {
            sql.execute( "update AGG_RPT_FIELD_MAPPING set TYPE='TEXT' where NAME IN ('hlt','hlgt','smqNarrow')" )
            sql.execute( "update AGG_RPT_FIELD_MAPPING set DATA_TYPE='java.lang.String' where NAME IN ('positiveRechallenge','positiveDechallenge')" )
            if(Holders.config.signal.faers.enabled){
                sql_faers = new Sql(dataSource_faers)
                sql_faers.execute("update AGG_RPT_FIELD_MAPPING SET ENABLED = 0 where NAME IN ('positiveRechallengeFaers','positiveDechallengeFaers')")
            }
        } catch ( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql.execute( "COMMIT" )
            sql_faers?.execute("COMMIT")
            sql?.close()
            sql_faers?.close()
            log.info( "updateMeddraFields execution finished." )
            if(Holders.config.signal.faers.enabled){
                log.info("updated positive rechallenge and positive dechallenge for faers datasource.")
            }
        }
    }
    void updateRelatedColumnInMart(){
        log.info( "updating related column." )
        def sql = new Sql( dataSource_pva )
        try {
            sql.execute( "update AGG_RPT_FIELD_MAPPING set CONTAINER_VIEW= 3 where NAME = 'related' " )
        } catch ( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql.execute( "COMMIT" )
            sql?.close()
            log.info( "updated related column execution finished." )
        }
    }


    void updateNewEvEvdasLabel() {
        log.info( "updateNewEvEvdasLabel execution started." )
        def sql = new Sql( dataSource_eudra )
        try {
            sql.execute( "update AGG_RPT_FIELD_MAPPING set DISPLAY = REPLACE(DISPLAY, 'Cum', 'Total'),OLD_DISPLAY = REPLACE(OLD_DISPLAY, 'Cum', 'Total') where NAME='newEvEvdas'" )
        } catch( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql?.close()
            log.info( "updateNewEvEvdasLabel execution finished." )
        }
    }

    void updateFlgToFlag() {
        def sql = new Sql(dataSource_faers)
        try {
            int count = sql.rows("select count(1) as fieldCount  from AGG_RPT_FIELD_MAPPING WHERE KEY_ID ='SPON_FLG'")?.get(0)?.fieldCount
            if (count > 0) {
                sql.execute("update AGG_RPT_FIELD_MAPPING set KEY_ID = 'SPONT_FLAG' where KEY_ID ='SPON_FLG'")
                sql.execute("update AGG_RPT_FIELD_MAPPING set KEY_ID = REPLACE(KEY_ID, '_FLG', '_FLAG') where KEY_ID LIKE '%_FLG'")
            }
        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void updateSpecialCharacters() {
        List<Map> tableData = ConfigConstants.SpecialCharacterMigration.tableMap
        tableData.each {
            updateRestrictedSpecialCharactersExistingData(it.tableName, it.columns, it.characters  as String[])
        }
    }

    void updateRestrictedSpecialCharactersExistingData(String tableName, List<String> columns, String[] characters) {
        log.info("updateRestrictedSpecialCharactersExistingData execution started.")
        def sql = new Sql(dataSource)
        try {
            columns.each {
                String columnsName = it
                characters.each {
                    String specialChar = it
                    String query = "update ${tableName} set ${columnsName} = REPLACE(${columnsName}, '${specialChar}', '') where ${columnsName} LIKE '%${specialChar}%'"
                    log.info("Updating query {} : " + query)
                    sql.execute(query)
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            sql?.close()
            log.info("updateRestrictedSpecialCharactersExistingData execution finished.")
        }
    }
    void updatingDataTypeOfAggField(){
        log.info( "Updating DataType Of AggFields in mart." )
        def sql = new Sql( dataSource_pva )
        try {
            sql.execute( "update AGG_RPT_FIELD_MAPPING set DATA_TYPE='java.lang.String' where NAME = 'signalsAndTopics' " )
        } catch ( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql.execute( "COMMIT" )
            sql?.close()
            log.info( "Updating DataType Of AggFields in mart finished." )
        }
    }
    //Removed migration ,handled from DB side
}
