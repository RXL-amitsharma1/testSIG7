package com.rxlogix

import com.rxlogix.config.ProductGroup
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.JSONAudit
import groovy.json.JsonSlurper

@DirtyCheck
abstract class BaseConfiguration implements AlertUtil {

    String name
    User owner
    String scheduleDateJSON
    Date nextRunDate
    String description
    boolean isPublic = false
    boolean isDeleted = false
    boolean isEnabled = true
    boolean adhocRun = false
    List<ProductGroup> productGroups =[]
    DateRangeTypeCaseEnum dateRangeType =  DateRangeTypeCaseEnum.CASE_RECEIPT_DATE

    String productSelection
    String eventSelection
    String studySelection
    String configSelectedTimeZone = "UTC"
    String productGroupSelection
    String eventGroupSelection

    Date asOfVersionDate
    EvaluateCaseDateEnum evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
    boolean excludeFollowUp = false
    boolean includeLockedVersion = true
    boolean adjustPerScheduleFrequency = true
    boolean excludeNonValidCases = true
    boolean groupBySmq = false

    boolean limitPrimaryPath = false
    boolean includeMedicallyConfirmedCases = false
    boolean missedCases = true
    boolean studyCases = false
    boolean ignoreStudyType = false
    boolean onlyPoi = false
    Boolean isStandalone = false

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    int numOfExecutions = 0
    long totalExecutionTime = 0 // this will be in milliseconds
    String blankValuesJSON
    String type
    String selectedDatasource = "pva"

    def configurationService

    User assignedTo
    Group assignedToGroup
    Group workflowGroup

    boolean isAutoTrigger = false
    boolean repeatExecution = false
    String drugType
    String drugClassification

    String referenceNumber
    String productDictionarySelection

    //case series drilldown
    String aggExecutionId
    String aggAlertId
    String aggCountType
    Boolean isCaseSeries = false

    String alertQueryName
    String alertForegroundQueryName
    int alertTriggerCases
    int alertTriggerDays
    String alertRmpRemsRef
    Date onOrAfterDate
    String spotfireSettings

    boolean applyAlertStopList = false
    boolean suspectProduct = true
    def dataObjectService
    Long alertCaseSeriesId
    String alertCaseSeriesName
    String dataMiningVariable
    @JSONAudit
    String dataMiningVariableValue
    Boolean isProductMining
    Boolean foregroundSearch = false
    String foregroundSearchAttr
    Boolean includeWHODrugs = false
    String drugRecordNumbers
    String excludedAlertGroups
    static hasMany = [productGroups: ProductGroup, shareWithUser: User, shareWithGroup: Group]

    static mapping = {
        name column: "NAME"
        owner column: "PVUSER_ID"
        scheduleDateJSON column: "SCHEDULE_DATE"
        nextRunDate column: "NEXT_RUN_DATE"
        description column: "DESCRIPTION"
        isPublic column: "IS_PUBLIC"
        isDeleted column: "IS_DELETED"
        isEnabled column: "IS_ENABLED"
        foregroundSearch column: "FG_SEARCH"
        dateRangeType column: "DATE_RANGE_TYPE"
        drugType column: "DRUG_TYPE"
        configSelectedTimeZone column: "SELECTED_TIME_ZONE"
        asOfVersionDate column: "AS_OF_VERSION_DATE"
        evaluateDateAs column: "EVALUATE_DATE_AS"
        excludeFollowUp column: "EXCLUDE_FOLLOWUP"
        includeLockedVersion column: "INCLUDE_LOCKED_VERSION"
        adjustPerScheduleFrequency column: "ADJUST_PER_SCHED_FREQUENCY"
        excludeNonValidCases column: "EXCLUDE_NON_VALID_CASES"
        numOfExecutions column: "NUM_OF_EXECUTIONS"
        totalExecutionTime column: "TOTAL_EXECUTION_TIME"
        includeMedicallyConfirmedCases column: "INCL_MEDICAL_CONFIRM_CASES"
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        studySelection column: "STUDY_SELECTION", sqlType: DbUtil.longStringType
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        blankValuesJSON column: "BLANK_VALUES", sqlType: DbUtil.longStringType
        selectedDatasource column: "SELECTED_DATA_SOURCE"
        groupBySmq column: "GROUP_BY_SMQ"
        workflowGroup column: "WORKFLOW_GROUP"
        alertQueryName column: "QUERY_NAME"
        alertForegroundQueryName column: "FG_QUERY_NAME"
        spotfireSettings column: "SPOTFIRE_SETTINGS" ,sqlType: DbUtil.longStringType
        createdBy sqlType: "varchar(100)"
        modifiedBy sqlType: "varchar(100)"
        productGroupSelection sqlType: "varchar2(8000 CHAR)"
        eventGroupSelection sqlType: "varchar2(8000 CHAR)"
        dataMiningVariableValue column: "DATA_MINING_VARIABLE_VALUE",sqlType: DbUtil.longStringType
        foregroundSearchAttr column: "FG_SEARCH_ATTR",sqlType: DbUtil.longStringType
        includeWHODrugs column: 'INCLUDE_WHO_DRUGS'
    }

    static constraints = {
        name(nullable: false, maxSize: 512)
        description(nullable: true, maxSize: 8000)
        nextRunDate(nullable: true)
        scheduleDateJSON(nullable: true, maxSize: 1024, validator:{val, obj ->
            def result = true
            if(!obj.isCaseSeries &&!obj.isStandalone && !obj.scheduleDateJSON){
                result = 'app.label.datetime.invalid'
            }
            return result
        })
        isStandalone nullable: true
        blankValuesJSON(nullable: true)
        lastUpdated(nullable: true)
        productGroups(nullable: true, blank: true)
        dateRangeType(nullable: true)
        drugType(nullable: true)
        productSelection(validator: { val, obj ->
            if ((!val && !obj.productGroups && !obj.studySelection && !obj.productGroupSelection) && !(obj.adhocRun && obj.dataMiningVariable) && !obj.isMasterTemplateAlert) {
                return ["com.rxlogix.Configuration.productSelection.nullable"]
            } else if (!val && obj.isMasterTemplateAlert && !obj.productHierarchyMasterTmplt){
                return ["com.rxlogix.Configuration.productHierarchySelection.nullable"]
            } else if (obj.selectedDatasource.contains("eudra") && ((val && obj.productGroupSelection) || (obj.productGroupSelection && JSON.parse(obj.productGroupSelection).size() > 1))) {
                return ["com.rxlogix.evdas.Configuration.productSelection.multiple"]
            }
        }, nullable: true)
        studySelection(nullable: true)
        eventSelection(nullable: true)
        evaluateDateAs(nullable: true)
        asOfVersionDate(nullable: true, validator: { val, obj ->
            if (obj.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF && !val) {
                return "version.date.not.null"
            }
        })
        numOfExecutions(min: 0, nullable: false)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        type nullable: true
        assignedTo nullable: true, validator: { value, obj ->
            def result = true
            if(!obj.assignedTo){
                result = obj.assignedToGroup ? true :  'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true
        selectedDatasource (nullable: true, validator:{ value, obj->
            if(obj.selectedDatasource=="eudra"){
                return ["com.rxlogix.Configuration.selectedDatasource.eudra"]
            }
        })
        productDictionarySelection nullable: true
        referenceNumber nullable: true
        aggExecutionId nullable: true
        aggAlertId nullable: true
        aggCountType nullable: true
        isCaseSeries nullable: true
        drugClassification nullable: true
        workflowGroup nullable: true
        alertQueryName nullable: true
        alertForegroundQueryName nullable: true
        alertRmpRemsRef(nullable: true)
        onOrAfterDate nullable: true
        spotfireSettings(nullable: true)
        suspectProduct(nullable: true)
        productGroupSelection (nullable: true,validator:{val,obj->
            if(!val && obj.selectedDatasource.split(',').size() > 1){
                return ["com.rxlogix.Configuration.productGroupSelection.nullable"]
            }
        })
        eventGroupSelection (nullable: true)
        alertCaseSeriesId (nullable: true)
        alertCaseSeriesName (nullable: true)
        dataMiningVariable (nullable: true)
        dataMiningVariableValue (nullable: true)
        isProductMining (nullable: true)
        foregroundSearch(nullable: true)
        foregroundSearchAttr (nullable: true)
        includeWHODrugs (nullable: true)
        drugRecordNumbers (nullable: true)
        excludedAlertGroups(nullable:true)
    }

    List getEventDictionaryValues() {
        List result = [[],[],[],[],[],[]]
        parseDictionary(result, eventSelection)
        return result
    }

    List getProductDictionaryValues() {
        List result = [[],[],[],[],[]]
        parseDictionary(result, productSelection)
        return result
    }

    List getStudyDictionaryValues() {
        List result = [[],[],[]]
        parseDictionary(result, studySelection)
        return result
    }

    private parseDictionary(List result, String dictionarySelection) {
        if (dictionarySelection) {
            Map values = new JsonSlurper().parseText(dictionarySelection)
            values.each { k, v ->
                if (!k.equals("isMultiIngredient")) {
                    int level = k.toInteger()
                    if (level != Constants.DRUG_RECORD_NUMBER_LEVEL_IDX) {
                        v.each {
                            result[level - 1].put(it["id"], it["name"])
                        }
                    }
                }
            }
        }
    }

    // this has the logic for version SQL
    def getAsOfVersionDateCustom(isExecuted) {
        if(this.asOfVersionDate) {
            return asOfVersionDate
        } else if(evaluateDateAs == EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD) {
            return null
        } else if(evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
            if(isExecuted) {
                return new Date()
            } else
                return RelativeDateConverter.calculateLatestVersion()
        } else {
            return null
        }
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser?.isAdmin() || owner == currentUser)

    }

    def buildProductNameList() {
      getNameFieldFromJson(productSelection).split(',')

    }

    def propertyMissing(propName) {
        if (propName == 'productNames') {
            return buildProductNameList()
        }
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        def prdList = []
        if(prdName) {
            prdList = prdName.tokenize(',')
        }
        return prdList
    }

    def getGroupNameFieldFromJson(jsonString){
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                prdName=jsonObj.collect{
                    it.name.substring(0,it.name.lastIndexOf('(') - 1)
                }.join(",")
            }
        }
        prdName
    }

    def getPrdNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        def prdList = []
        if (prdName) {
            prdList = prdName.tokenize(',')
        } else {
            []
        }

        return prdList
    }

    def getProductCodeList() {
        String prdId = getIdFieldFromJson(this.productSelection)
        if(prdId) {
            prdId.tokenize(',')
        } else {
            []
        }
        return prdId
    }

    def getProductGroupList() {
        getIdsForProductGroup(this.productGroupSelection)
    }

    def getEventSelectionList() {
        def evtName = getNameFieldFromJson(this.eventSelection)
        if (evtName) {
            evtName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }

    List getAllEventSelectionList() {
        String evtName = getAllEventNameFieldFromJson(this.eventSelection)
        evtName = evtName ? evtName.toLowerCase().tokenize(',') : []
    }

    String getEventGroupSelectionList() {
        def evtName = getEventGroupFromJson(this.eventGroupSelection)
        return evtName
    }

    def getEventCodeList() {
        String evtList = getIdFieldFromJson(this.eventSelection)
        if(evtList) {
            evtList.tokenize(',')
        } else {
            []
        }
        return evtList
    }

    def getProductType() {
        Map dictionaryMap = dataObjectService.getLabelIdMap()
        def jsonSlurper = new JsonSlurper()
        def prodSelType
        if (this.productSelection) {
            def jsonObj = jsonSlurper.parseText(this.productSelection ?: "")
            jsonObj.find { k, v ->
                v.find {
                    if (k == dictionaryMap.get('Ingredient')) {//Id kept as 1 for the ingredient.
                        prodSelType = Constants.ProductSelectionType.INGREDIENT
                    } else if (k == dictionaryMap.get('Family')) {//Id kept as 2 for the product family.
                        prodSelType = Constants.ProductSelectionType.FAMILY
                    } else if (k == dictionaryMap.get('Product Name')) {//Id kept as 3 for the product.
                        prodSelType = Constants.ProductSelectionType.PRODUCT
                    }
                }
            }
        }
        prodSelType
    }

    Integer getExpectedExecutionTime() {
        if (totalExecutionTime && numOfExecutions >= 1) {
            return totalExecutionTime
        }
        return 0
    }

}
