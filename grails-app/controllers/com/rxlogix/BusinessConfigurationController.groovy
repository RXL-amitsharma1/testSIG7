package com.rxlogix

import com.rxlogix.config.ActionTemplate
import com.rxlogix.config.Disposition
import com.rxlogix.config.PVSState
import com.rxlogix.config.RuleMigrationStatus
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.BusinessConfiguration
import com.rxlogix.signal.RuleInformation
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonSlurper

@Secured(["isAuthenticated()"])
class BusinessConfigurationController implements AlertUtil {

    def CRUDService
    def workflowService
    def queryService
    def businessConfigurationService
    def cacheService
    def alertTagService
    def pvsGlobalTagService
    def userService
    def alertFieldService
    def dataObjectService
    def businessRulesMigrationService
    def excelService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {}

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def businessConfigList = businessConfigurationService.getAllBussinessConfigurationsList()
        render(businessConfigList as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create(Boolean isGlobalRule) {
        BusinessConfiguration businessConfiguration = new BusinessConfiguration()
        List actionList = ActionTemplate.list().collect { [id: it.id, name: it.name] }
        List dispositionList = Disposition.list().collect { [id: it.id, name: it.value] }
        Map datasourceMap = getDataSourceMap()
        Boolean isPVCM = dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
        [businessConfiguration: businessConfiguration, queries: queryService.getQueryListForBusinessConfiguration(), actionList: actionList,
         dispositionList: dispositionList, datasourceMap: datasourceMap, isGlobalRule: isGlobalRule, isPVCM: isPVCM]
    }

    def fetchSelectBoxValues(Long id) {
        Map selectBoxValues = [:]
        try {
            BusinessConfiguration businessConfiguration = BusinessConfiguration.get(id)
            if(businessConfiguration.dataSource == Constants.DataSource.JADER){
                selectBoxValues = businessConfigurationService.fetchSelectBoxValuesJader(businessConfiguration)
            }else{
                selectBoxValues = businessConfigurationService.getSelectBoxValues(businessConfiguration)
            }
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render selectBoxValues as JSON
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def saveBusinessConfiguration(Long id, String ruleName) {
        ruleName = ruleName?.trim()?.replaceAll("\\s{2,}", " ")
        Boolean isEditMode = false
        def datasourceMap = getDataSourceMap()
        Boolean isError = false
        int count = 0;
        Boolean duplicateFound = false
        String duplicateId = null
        BusinessConfiguration businessConfiguration = new BusinessConfiguration()

            try {
                List productIds = BusinessConfiguration.findAllByDataSource(params.dataSource)*.getAllProductIdsList().flatten()
                List<Long> oldBusinessConfigurationProductIds
                if (id) {
                    isEditMode = true
                    businessConfiguration = BusinessConfiguration.get(id)
                }

                if (businessConfigurationService.businessConfigurationNameCheck(isEditMode, id, ruleName)) {
                    flash.error = message(code: "app.label.business.configuration.name.error")
                    isError = true
                } else {
                    businessConfiguration.ruleName = ruleName
                    oldBusinessConfigurationProductIds = businessConfiguration.getAllProductIdsList()
                    businessConfiguration.productSelection = params.productSelection
                    businessConfiguration.isMultiIngredient = Boolean.parseBoolean(params.isMultiIngredient)
                    businessConfiguration.description = params.description
                    businessConfiguration.enabled = true
                    if (!id && params.dataSource) {
                        businessConfiguration.dataSource = params.dataSource
                    }

                    if(params.productGroupSelection != '[]'){
                        businessConfiguration.productGroupSelection = params.productGroupSelection
                    }else if(params.productGroupSelection == '[]'){
                        businessConfiguration.productGroupSelection = null
                    }

                    //Apply check for selection of either product or ingredient or family
                    def isProductOrFamilyOrIngredient = allowedDictionarySelection(businessConfiguration)
                    boolean isProductGroupOnly = businessConfiguration.productSelection && businessConfiguration.productGroupSelection
                    if (!isProductOrFamilyOrIngredient || isProductGroupOnly) {
                        def errMessage = message(code: "app.label.product.family.error.message")
                        flash.error = errMessage
                        throw new Exception(errMessage)
                    }

                    //Apply check for selection of product/family/ingredient's duplicate values.
                    productIds -= oldBusinessConfigurationProductIds
                    businessConfiguration.allProductIdsList.each { def productGroupId ->
                        if (!duplicateFound) {
                            count += productIds.count { it == productGroupId }
                            if (count > 0) {
                                duplicateFound = true
                                duplicateId = productGroupId
                            }
                        }
                    }
                    if (!duplicateFound) {
                        if (id) {
                            if(!businessConfigurationService.isOtherRuleEnabled(true ,params.dataSource)){
                                cacheService.setRuleInformationCache(businessConfiguration)
                                CRUDService.update(businessConfiguration)
                                flash.message = message(code: "app.label.business.configuration.update.sucess")
                            }
                            else{
                                flash.error = message(code: "app.business.configuration.enabled.warming.message",args: [Constants.RuleType.GLOBAL, Constants.RuleType.PRODUCT])
                                isError = true
                            }
                        } else {
                            if(!businessConfigurationService.isOtherRuleEnabled(true ,params.dataSource)){
                                CRUDService.save(businessConfiguration)
                                flash.message = message(code: "app.label.business.configuration.create.sucess")
                            }
                            else{
                                flash.error = message(code: "app.business.configuration.enabled.warming.message",args: [Constants.RuleType.GLOBAL, Constants.RuleType.PRODUCT])
                                isError = true
                            }
                        }
                        cacheService.prepareBusinessConfigCacheForSelectionType()
                    } else {
                        def jsonObj = businessConfiguration.parseJsonString(businessConfiguration.productSelection)
                        String prdName = getProductNames(jsonObj, duplicateId)
                        String dsLabel = datasourceMap.containsKey(businessConfiguration.dataSource) ? datasourceMap[businessConfiguration.dataSource] : businessConfiguration.dataSource
                        flash.error = message(code: "app.label.business.configuration.product.error", args: [prdName, dsLabel])
                        isError = true
                    }
                }
            } catch (Exception ve) {
                isError = true
                log.error(ve.getMessage())
            }
        if (isError){
            render(view: 'create', model: [businessConfiguration: businessConfiguration, datasourceMap: datasourceMap, isGlobalRule: false])
        } else {
            redirect(controller: 'businessConfiguration',action: 'index')
        }
    }

    def saveGlobalRule(Long id, String ruleName, String dataSource, String description) {
        ruleName = ruleName?.trim()?.replaceAll("\\s{2,}", " ")
        Map datasourceMap = getDataSourceMap()
        BusinessConfiguration businessConfiguration = new BusinessConfiguration()
        if(id){
            businessConfiguration = BusinessConfiguration.get(id)
        }
        ResponseDTO responseDTO = businessConfigurationService.saveGlobalRule(businessConfiguration,ruleName,dataSource,description)
        if (!responseDTO.status) {
            flash.error = responseDTO.message
            render(view: 'create', model: [businessConfiguration: businessConfiguration, datasourceMap: datasourceMap, isGlobalRule: true])
        } else {
            flash.message = responseDTO.message
            redirect(controller: 'businessConfiguration', action: 'index')
        }
    }

    private String getProductNames(jsonObj, duplicateId) {
        String prdName = Constants.Commons.BLANK_STRING
        def products = jsonObj.get('3')
        if (products) {
            prdName = (products.find { it.id == duplicateId })?.name
        }

        def families = jsonObj.get('2')
        if (families) {
            prdName = (families.find { it.id == duplicateId })?.name ?: prdName
        }

        def ingredients = jsonObj.get('1')
        if (ingredients) {
            prdName = (ingredients.find { it.id == duplicateId })?.name ?: prdName
        }

        def tradeNames = jsonObj.get('4')
        if (tradeNames) {
            prdName = (tradeNames.find { it.id == duplicateId })?.name ?: prdName
        }
        prdName
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id, Boolean isGlobalRule) {
        BusinessConfiguration businessConfiguration = BusinessConfiguration.read(id)
        def productSelection = businessConfiguration?.productSelection
        def jsonSlurper = new JsonSlurper()
        def datasourceMap = getDataSourceMap()
        render(view: 'create', model: [businessConfiguration: businessConfiguration, datasourceMap: datasourceMap, isGlobalRule: isGlobalRule, isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def createRule(Long id){
        BusinessConfiguration businessConfiguration = BusinessConfiguration.get(id)
        RuleInformation ruleInformation = new RuleInformation()
        businessConfigurationService.generateRenderModelForRules(ruleInformation, businessConfiguration)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def editRule(Long id) {
        RuleInformation ruleInformation = RuleInformation.get(id)
        businessConfigurationService.generateRenderModelForRules(ruleInformation, ruleInformation?.businessConfiguration)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def saveRule(Long businessConfigurationId, Boolean isSingleCaseAlert) {
        Boolean isError = false
        BusinessConfiguration businessConfiguration = BusinessConfiguration.get(businessConfigurationId)
        RuleInformation ruleInformation = new RuleInformation()
        try {
            ruleInformation.isSingleCaseAlertType = isSingleCaseAlert
            ruleInformation = businessConfigurationService.populateRuleInformation(params, ruleInformation)
            ruleInformation.ruleRank = (businessConfiguration.ruleInformations.collect { it.ruleRank }.max {
                it
            } ?: 0) + 1
            int count = RuleInformation.countByRuleNameIlike(ruleInformation.ruleName)
            if (!count) {
                businessConfiguration.addToRuleInformations(ruleInformation)
                CRUDService.saveWithFullUserName(ruleInformation)
                CRUDService.update(businessConfiguration)
                if(businessConfiguration.enabled){
                    cacheService.setRuleInformationCache(businessConfiguration)
                }

                flash.message = message(code: "app.label.rule.information.create.success")
            } else {
                //Setting this flag as true as it is going to redirect the flow to the create screen.
                isError = true
                flash.error = message(code: "app.label.business.configuration.rule.name.error")
            }
        }catch(Exception ve) {
            isError = true
            log.error("error occurred while saving rule information",ve)
        }
        if(isError){
            render(view: 'createRule', model: businessConfigurationService.generateRenderModelForRules(ruleInformation, businessConfiguration))
        } else {
            redirect(controller: 'businessConfiguration', action: 'index')
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def updateRule(Long id) {
        params.ruleName = params.ruleName?.trim()?.replaceAll("\\s{2,}", " ")
        Boolean isError = false
        RuleInformation ruleInformation = RuleInformation.get(id)
        ruleInformation?.ruleName = ruleInformation?.ruleName?.trim()?.replaceAll("\\s{2,}", " ")
        try {
            ruleInformation = businessConfigurationService.populateRuleInformation(params, ruleInformation)
            CRUDService.updateWithFullUserName(ruleInformation)
            if (ruleInformation?.businessConfiguration?.enabled) {
                cacheService.setRuleInformationCache(ruleInformation.businessConfiguration)
            }
            flash.message = message(code: "app.label.rule.information.update.success")
        } catch (Exception ve) {
            isError = true
            log.error("error occurred while saving rule information", ve)
        }
        if (isError) {
            render(view: 'editRule', model: businessConfigurationService.generateRenderModelForRules(ruleInformation, ruleInformation?.businessConfiguration))
        }else{
            redirect(controller: 'businessConfiguration',action: 'index')
        }
    }

    boolean removeRules(String ruleJSON) {
        boolean remove = false;
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).findAll {
            it.enabled == false
        }.collectEntries {
            b -> [b.name, b.enabled]
        }

        Map labelConfigGroup = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.keyId]
        }
        Iterator iter1 = labelConfig.keySet().iterator();
        while (iter1.hasNext()) {
            String key = iter1.next();
            String value = labelConfig.get(key);
            String keyId = labelConfigGroup.get(key);
            if (keyId && (ruleJSON?.contains(key) || ruleJSON?.contains(keyId)) && (labelConfig.containsKey(key) || labelConfig.containsKey(keyId))) {
                remove = true;
            }
            if (remove) {
                break;
            }
        }
        return remove;
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def fetchRules(Long id) {
        BusinessConfiguration businessConfiguration = BusinessConfiguration.read(id)
        def rules1 = businessConfiguration.getRuleInformations()
        def rules = []
        rules1?.each {
            if (!removeRules(it.ruleJSON)) {
                rules.add(it)
            }
        }
        businessConfiguration.ruleInformations = rules.sort { it.ruleRank }
        List ruleInformationDtos = businessConfiguration.collect { it.toDto(userService.getCurrentUserPreference()?.timeZone) }
        render(ruleInformationDtos as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def deleteRule(Long id) {
        try {
            RuleInformation ruleInformation = RuleInformation.get(id)
            BusinessConfiguration businessConfiguration = ruleInformation.businessConfiguration
            CRUDService.delete(ruleInformation)
            cacheService.setRuleInformationCache(businessConfiguration)
            flash.message = message(code: "app.label.rule.information.delete.success")
        } catch (Exception e) {
            log.error("error occurred while saving rule information", e)
        }
        redirect(controller: 'businessConfiguration', action: 'index')
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def cloneRule(Long id) {
        try {
            RuleInformation ruleInformation = RuleInformation.get(id)
            BusinessConfiguration businessConfiguration = ruleInformation.businessConfiguration
            RuleInformation newRuleInformation = new RuleInformation()
            Integer newRuleRank = businessConfiguration.ruleInformations.max {it.ruleRank}.ruleRank + 1
            newRuleInformation.with {
                customSqlQuery = ruleInformation.customSqlQuery
                ruleName = "${ruleInformation.ruleName} Copy"
                ruleRank = newRuleRank
                justificationText = ruleInformation.justificationText
                ruleJSON = ruleInformation.ruleJSON
                isFirstTimeRule = ruleInformation.isFirstTimeRule
                disposition = ruleInformation.disposition
                action = ruleInformation.action
                format = ruleInformation.format
                isSingleCaseAlertType = ruleInformation.isSingleCaseAlertType
                if (ruleInformation.tags != '{"tags":"[]"}') {
                    tags = ruleInformation.tags
                }
            }
            businessConfiguration.addToRuleInformations(newRuleInformation)
            CRUDService.saveWithFullUserName(newRuleInformation)
                cacheService.setRuleInformationCache(businessConfiguration)
            flash.message = message(code: "app.label.rule.information.clone.success")
        } catch (Exception e) {
            log.error("error occurred while saving rule information", e)
        }
        redirect(controller: 'businessConfiguration', action: 'index')
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def toggleEnableRule(Long id, Boolean attribute1) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: "app.label.rule.information.toggle.enable.success", args: [(attribute1 ? 'Enabled' : 'Disabled')]))
        try {
            RuleInformation ruleInformation = RuleInformation.get(id)
            ruleInformation.enabled = attribute1
            CRUDService.saveWithFullUserName(ruleInformation)
            cacheService.setRuleInformationCache(ruleInformation.businessConfiguration)
        } catch (Exception e) {
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.rule.information.toggle.enable.error")
            log.error("error occurred while enabling or disabling rule information", e)
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def toggleEnableBusinessConfiguration(Long id, Boolean attribute) {

            BusinessConfiguration businessConfiguration = businessConfigurationService.getBusinessConfiguration(id)
            if (!businessConfigurationService.isOtherRuleEnabled(!businessConfiguration.isGlobalRule, businessConfiguration.dataSource) || attribute) {

                businessConfiguration.enabled = attribute ? false : true
                CRUDService.save(businessConfiguration)
                if (businessConfiguration.enabled) {
                    cacheService.setRuleInformationCache(businessConfiguration)
                } else {
                    cacheService.clearRuleInformationCache(businessConfiguration.id)
                }
                cacheService.prepareBusinessConfigCacheForSelectionType()
                flash.message = message(code: "app.label.business.configuration.update.sucess")
            } else {
                flash.error = message(code: "app.business.configuration.enabled.warming.message", args:
                        [businessConfiguration.isGlobalRule ? Constants.RuleType.PRODUCT : Constants.RuleType.GLOBAL,
                         businessConfiguration.isGlobalRule ? Constants.RuleType.GLOBAL : Constants.RuleType.PRODUCT])
            }
        redirect(action: 'index')
    }

    //Todo remove this - start
    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {

        def businessConfiguration = new BusinessConfiguration()

        bindCreateConfigurations(businessConfiguration)

        if (!businessConfigurationService.confirmStateConfigurationSelection(businessConfiguration)) {
            flash.error = message(code: "app.label.business.configuration.auto.route.algo.error.message")
            render(view: "create", model: [businessConfiguration: businessConfiguration])
            return
        }

        try {
            CRUDService.save(businessConfiguration)
        } catch (Exception ve) {
            ve.printStackTrace()
            render(view: "create", model: [businessConfiguration: businessConfiguration])
            return
        }
        flash.message = message(code:"app.label.business.configuration.create.sucess")
        render(view: "edit", model: [businessConfiguration: businessConfiguration])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {

        def businessConfiguration = BusinessConfiguration.first()

        bindUpdateConfigurations(businessConfiguration)

        if (!businessConfigurationService.confirmStateConfigurationSelection(businessConfiguration)) {
            flash.error = message(code: "app.label.business.configuration.auto.route.algo.error.message")
            render(view: "create", model: [businessConfiguration: businessConfiguration])
            return
        }

        try {
            CRUDService.save(businessConfiguration)
        } catch (Exception ve) {
             ve.printStackTrace()
            render(view: "edit", model: [businessConfiguration: businessConfiguration])
            return
        }
        flash.message = message(code:"app.label.business.configuration.update.sucess")
        render(view: "edit", model: [businessConfiguration: businessConfiguration])
    }

    def rulesMigrations() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        Boolean exceptionCaught = businessRulesMigrationService.updateOldRuleJson()
        if (exceptionCaught) {
            responseDTO.status = false
            responseDTO.code = 500
        }
        render(responseDTO as JSON)
    }
    def exportRulesMigrationStatus(){
        List<RuleMigrationStatus> ruleMigrationStatusList = RuleMigrationStatus.list()
        List itemResultSet = []
        ruleMigrationStatusList.each{
            itemResultSet.add([it.entityId, it.entityClass, it.datasource,it.initialData,it.migratedData,it.isMigrationCompleted,it.error])
        }
        Map itemsDataMap = [itemOffsetX  : 0,
                            itemPath     : null,
                            itemName     : "STATUS_RESULT_SET",
                            itemOffsetY  : 6,
                            itemType     : "RESULTSET",
                            font         : [fontSize: 9, bold: false, italics: false, fontName: "Arial Unicode"],
                            itemWidth    : null,
                            itemResultSet: itemResultSet,
                            itemResultSetKeys: ["Id","Class","DataSource","Initial Data","Migrated Data","Status","Error"]
        ]
        Map itemsMap = [1: itemsDataMap]
        Map sheetDataMap = [sheetName   : "Migration Status",
                            writeRunDate: false,
                            itemsMap    : itemsMap]
        Map sheetMap = [1: sheetDataMap]
        Map data = [userID         : "1212",
                    sheetsMap      : sheetMap,
                    fileType       : "XLSX",
                    fileName       : Holders.config.tempDirectory + "/Business_rules_migrations_status.xlsx",

                    screenName     : "Compliance"]
        def map = excelService.saveFilePath((data as JSON).toString())
        String fullPath = map['filename']
        fullPath = fullPath.replace('\\', '/')
        String filename = fullPath.split("/").last()
        byte[] dataBytes = map['bytes']
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        response.setHeader("Content-Disposition", "attachment;filename=" + filename)
        response.setContentLength(dataBytes.length)
        response.getOutputStream().write(dataBytes)
    }

    def getDispositions() {
        def workflowStateId = PVSState.get(params.id)
        def dispositions = workflowService.getDispositions(workflowStateId)
        respond(dispositions, [formats: ['json']])
    }

    private bindCreateConfigurations(businessConfiguration) {
        bindData(businessConfiguration, params, [exclude: ['prrConfigs', 'ebgmConfigs', 'rorConfigs']])
        bindAlgoConfigurations(businessConfiguration, getAlgoConfigMap(Constants.BusinessConfigType.PRR))
        bindAlgoConfigurations(businessConfiguration, getAlgoConfigMap(Constants.BusinessConfigType.EBGM))
        bindAlgoConfigurations(businessConfiguration, getAlgoConfigMap(Constants.BusinessConfigType.ROR))
    }

    private bindUpdateConfigurations(businessConfiguration) {

        bindCreateConfigurations(businessConfiguration)
        //Remove deleted configurations.
        removeAlgoConfigurations(businessConfiguration, getAlgoConfigMap(Constants.BusinessConfigType.PRR))
        removeAlgoConfigurations(businessConfiguration, getAlgoConfigMap(Constants.BusinessConfigType.EBGM))
        removeAlgoConfigurations(businessConfiguration, getAlgoConfigMap(Constants.BusinessConfigType.ROR))

    }

    private bindAlgoConfigurations(businessConfiguration, configMap) {
        if (params."$configMap.enablingParam".equals("true")) {

            //If the custom config is enabled then we'll remove all the respective algo associations.
            if (params."$configMap.customConfigParam".equals("true")) {
                businessConfigurationService.removeAlgoConfigurations(businessConfiguration, configMap.configParam)
            } else {
                //Add or Update the PRR configurations.
                for (int i = 0; params.containsKey("$configMap.configParam[" + i + "].id"); i++) {

                    def bindingMap = [
                            algoType            : params.("$configMap.configParam[" + i + "].algoType"),
                            minThreshold        : params.("$configMap.configParam[" + i + "].minThreshold"),
                            maxThreshold        : params.("$configMap.configParam[" + i + "].maxThreshold"),
                            targetState         : params.("$configMap.configParam[" + i + "].targetState") ?: null,
                            targetDisposition   : params.("$configMap.configParam[" + i + "].targetDisposition") ?: null,
                            justification       : params.("$configMap.configParam[" + i + "].justification"),
                            firstTimeRule       : params.("$configMap.configParam[" + i + "].firstTimeRule") ?: false
                    ]
                    def isNewConfig = params.get("$configMap.configParam[" + i + "].new").equals("true")
                    def configId = params.("$configMap.configParam[" + i + "].id")

                    businessConfigurationService.addAlgoConfigurations(businessConfiguration, isNewConfig,
                            bindingMap, configMap, configId)
                }

                //Nullify the Custom config value
                businessConfiguration."${configMap.customConfig}" = null
            }
        } else {
            businessConfigurationService.removeAlgoConfigurations(businessConfiguration, configMap.configParam)
        }
    }

    private removeAlgoConfigurations(businessConfiguration, configMap) {
        for (int i = 0; params.containsKey("$configMap.configParam[" + i + "].id"); i++) {
            if (params.get("$configMap.configParam[" + i + "].deleted").equals("true")) {
                def algoConfigId = params.("$configMap.configParam[" + i + "].id")
                businessConfigurationService.removeAlgoConfigurations(algoConfigId,
                        configMap.type, businessConfiguration)
            }
        }
    }

    private getAlgoConfigMap(algoType) {
        def algoConfigMap = []
        switch(algoType) {
            case Constants.BusinessConfigType.PRR:
                algoConfigMap = [enablingParam: "enablePrr",
                                 customConfigParam: 'enablePrrCustomConfig',
                                 customConfig: 'prrCustomConfig',
                                 configParam: "prrConfigs",
                                 type : Constants.BusinessConfigType.PRR]
                break
            case Constants.BusinessConfigType.EBGM:
                algoConfigMap = [enablingParam: "enableEbgm",
                                 customConfigParam: 'enableEbgmCustomConfig',
                                 customConfig: 'ebgmCustomConfig',
                                 configParam: "ebgmConfigs",
                                 type : Constants.BusinessConfigType.EBGM]
                break
            case Constants.BusinessConfigType.ROR:
                algoConfigMap = [enablingParam: "enableRor",
                                 customConfigParam: 'enableRorCustomConfig',
                                 customConfig: 'rorCustomConfig',
                                 configParam: "rorConfigs",
                                 type : Constants.BusinessConfigType.ROR]
                break
        }
        algoConfigMap
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Long id) {
        BusinessConfiguration businessConfiguration = BusinessConfiguration.get(id)
        CRUDService.delete(businessConfiguration)
        cacheService.clearRuleInformationCache(businessConfiguration.id)
        cacheService.prepareBusinessConfigCacheForSelectionType()
        flash.message = "Business Configuration ${businessConfiguration.ruleName} deleted."
        redirect(controller: 'businessConfiguration', action: 'index')
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def rulesOrder(){
        try {
            def ruleOrderString = params.ruleOrderArray
            def ruleOrderArray = ruleOrderString.split(',')*.toInteger()
            for (int i = 0; i < ruleOrderArray.size(); i++) {
                RuleInformation ruleInformation = RuleInformation.get(ruleOrderArray[i])
                ruleInformation.ruleRank = i + 1
                CRUDService.saveWithFullUserName(ruleInformation)
                cacheService.setRuleInformationCache(ruleInformation.businessConfiguration)
            }
        }catch(Exception ve) {
            log.error("error occurred while saving rule information",ve)
        }
        redirect(controller: 'businessConfiguration', action: 'index')
    }

    def fetchRuleTags() {
        Long ruleId = params.ruleId as Long
        String tags = ruleId ? RuleInformation.get(ruleId).tags : null
        render(['tags':tags] as JSON)

    }
}
