package com.rxlogix.cache

import com.rxlogix.Constants
import com.rxlogix.DataObjectService
import com.rxlogix.EmailNotification
import com.rxlogix.config.*
import com.rxlogix.enums.AlertType
import com.rxlogix.enums.GroupType
import com.rxlogix.pvdictionary.ProductDictionaryMetadata
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.FileUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils
import org.grails.web.json.JSONObject
import org.h2.store.fs.FileUtils

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import java.util.concurrent.CopyOnWriteArrayList
import grails.converters.JSON

/**
 * The singleton implementation of cache service. It will act as the in memory data cache for certain values.
 */
class CacheService {
    static final String CACHE_NAME_ALERT_COMMENT = "alertCommentCache"
    static final String CACHE_NAME_SINGLE_ALERT_COMMENT = "singleAlertCommentCache"
    static final String CACHE_NAME_ACTIVITY_TYPE = "activityTypeCache"
    static final String CACHE_NAME_PRIORITY = 'priorityCache'
    static final String CACHE_NAME_DISPOSITION = 'dispositionCache'
    static final String CACHE_NAME_MEDICAL_CONCEPTS = "medicalConceptsCache"
    static final String CACHE_NAME_BUSINESS_CONFIGURATION = "businessConfigurationCache"
    static final String CACHe_NAME_PVS_STATE = 'pvsStateCache'
    static final String CACHE_NAME_PV_DICTIONARY_LOCALIZATION = "pvDictionaryLocalization"
    static final String CACHE_NAME_PV_USER_PRODUCTS_IDS = "pvUserProductsIdsCache"
    static final String CACHE_NAME_PV_USER_PRODUCTS_NAMES = "pvUserProductsNamesCache"
    static final String CACHE_NAME_PV_SAFETY_GROUP_PRODUCTS_NAMES = "pvSafetyGroupProductsNamesCache"
    static final String CACHE_NAME_PV_USER_PREFERENCE = "pvUserPreferenceCache"
    static final String CACHE_NAME_PV_USER = "pvUserCache"
    static final String CACHE_NAME_PV_GROUP = "pvGroupCache"
    static final String CACHE_NAME_PV_GROUP_ALLOWED_PRODUCT = "pvGroupAllowedProdNameCache"
    static final String CACHE_NAME_PV_GROUP_ALLOWED_ID = "pvGroupAllowedProdIdCache"
    static final String CACHE_NAME_PV_SAFETY_GROUP_ALLOWED_PRODUCT = "pvSafetyGroupAllowedProdNameCache"
    static final String CACHE_NAME_PV_USER_ID = "pvUserIdCache"
    static final String CACHE_NAME_PRODUCTS_USED = "pvProductsUsed"
    static final String CACHE_NAME_BUSINESS_CONFIG_TYPE = "pvBusinessConfigType"
    static final String CACHE_NAME_ACTION_TEMPLATE = "actionTemplateCache"
    static final String CACHE_NAME_ACTION_CONFIG = "actionConfigCache"
    static final String CACHE_NAME_ACTION_TYPE = "actionTypeCache"
    static final String CACHE_NAME_RULE_INFORMATION = "ruleInformationCache"
   // static final String CACHE_HELP_MAP = "helpMapCache"
    static final String CACHE_NAME_STRAT_EBGM_COLUMNS = "stratEbgmColumnsCache"
    static final String CACHE_SUB_GROUPS_ENABLED = "subGroupsEnabledMap"
    static final String CACHE_NAME_ADVANCED_FILTER_COLUMNS = "advancedFilterColumnsCache"
    static final String CACHE_NAME_UPPER_PRODUCT_DICTIONARY = "upperProductDictionaryCache"
    static final String CACHE_NAME_EMAIL_NOTIFICATION = "emailNotificationCache"
    static final String CACHE_NAME_EMAIL_NOTIFICATION_MODULES = "emailNotificationModulesCache"
    static final String CACHE_NAME_RPT_FIELD_INDEX = "rptFieldIndexCache"
    static final String CACHE_NAME_SUB_GROUP_EBGM_COLUMNS = "subGroupEbgmColumnsCache"
    static final String CACHE_NAME_ALL_SUB_GROUP_COLUMNS = "AllSubGroupColumnsCache"
    static final String CACHE_NAME_PRIORITY_DISPOSITIONS = 'priorityDispositionsCache'
    static final String CACHE_NAME_ISROR = 'isRorCache'
    static final String CACHE_NAME_UNASSIGNED_PRODUCTS = 'unassignedProductsCache'
    static final String CACHE_DATA_MINING_VARIABLES = 'dataMiningVariablesCache'
    static final String CACHE_EVDAS_DRILL_DOWN_DATA = 'evdasDrillDownData'
    static final String CACHE_EVDAS_ALERT_Id = 'evdasAlertId'
    static final String CACHE_SAFETY_MART_RPT_TO_UI_LABEL = 'safetyMartRptToUiLabel'
    static final String CACHE_DEFAULT_DISP = "defaultDisp"
    static final String CACHE_DEFAULT_PRECHECK = "defaultPrecheck"
    static final String CACHE_PARTIAL_ALERT = "partialAlertCache"
    static final String CACHE_AGG_ON_DEMAND_COLUMNS = "aggOnDemandColumns"
    static final String CACHE_AGG_JADER_COLUMNS = "aggJaderColumns"
    static final String CACHE_COMMON_TAG = "commonTagsCache"
    static final String CACHE_COMMON_TAG_JSON = "commonTagsCacheJson"
    static final String MASTER_UPLOAD_RUNNING = 'hazelcastMasterUploadRunning'
    static final String SIGNAL_CONFIGURATION_MAP = 'signalConfigurationMap'
    static final String BLINDED_USER_CACHE = 'blindedUserCache'
    static final String BLINDED_USER_GROUP_CACHE = 'blindedUserGroupCache'
    static final String CACHE_SAFETY_MART_UNIQUE_IDENTIFIER = 'safetyMartUniqueIdentifier'
    static final String ADHOC_ALERT_CONFIGURATION_CACHE = 'adhocAlertConfigurationCache'
    static final String ADHOC_ALERT_REVIEW_CACHE = 'adhocAlertReviewCache'
    static final String ADHOC_ALERT_DETAILS_CACHE = 'adhocAlertDetailsCache'
    static final String ADHOC_ALERT_GROUP_CONFIGURATION_CACHE = 'adhocAlertGroupConfigurationCache'
    static final String ADHOC_ALERT_SECTION_LABEL_CACHE = 'adhocAlertSectionLebelCache'
    static final String ADHOC_ALERT_DYNAMIC_DROPDOWN_CACHE = 'adhocAlertDynamicDropDownCache'

    static final String ALERT_FIELD = "alertField"



    def dataSource_pva
    def dataSource
    def pvsProductDictionaryService
    def seedDataService
    def dataSource_faers
    def dataSource_vaers
    def dataSource_vigibase

    static transactional = false

    HazelcastService hazelcastService
    DataObjectService dataObjectService
    private Disposition defaultDisposition
    private PVSState defaultWorkflow
    private List<Map> smqList = []

    def initCache() {
        log.debug("Now Caching the metadata.")
        def start = System.currentTimeMillis()
        prepareAlertCommentCache()
        prepareActivityTypeCache()
        prepareWorkflowCache()
        prepareDefaultWorkflow()
        prepareDispositionCache()
        prepareDefaultDisposition()
        preparePriorityCache()
        prepareMedicalConcepts()
        prepareBusinessConfigurationCache()
        prepareSMQCache()
        preparePvDictionaryLocalization()
        prepareGroupCache()
        if(Holders.config.pvsignal.product.based.security){
            prepareProductIdsAndNameCacheForAllUsers()
            prepareSafetyGroupProductNameCacheForAllUsers()
            prepareProductsUsed()
        }
        prepareSafetyGroupCache()
        prepareUserPreferenceCache()
        prepareBusinessConfigCacheForSelectionType()
        prepareActionTemplateCache()
        prepareActionConfigurationCache()
        prepareActionTypeCache()
        prepareSingleCaseAlertCommentCache()
        prepareRuleInformationCache()
        prepareStratificationColumnCache()
        prepareSubGroupColumnCache()
        prepareAllOtherSubGroupColumnCache()
        updateSubGroupingConfiguration()
        if(Holders.config.prepare.possible.values.cache) {
            prepareAdvancedFilterPossibleValues()
        }
        prepareUpperHierarchyProductDictionaryCache()
        prepareCacheForEmailNotification()
        prepareRptFieldIndexCache()
     //   prepareHelpMapCache()
        prepareMiningVariables()
        prepareCommonTagCache()
//        prepareUiLabelCacheForSafety()
	prepareAdhocConfiguration()
        def end = System.currentTimeMillis()
        log.debug("It took " + ((end - start) / 1000) + " secs to cache the data.")
        log.debug("Caching of metadata is done.")
    }

    /**
     * Method to produce the key based on the passed argument. The criteria will be dependent type of alert configuration pased.
     * @param productName
     * @param eventName
     * @param configuration
     * @param evdasConfiguration
     * @return
     */
    String produceKey(String productName, String eventName, Long configId = null, String type) {
        String key
        if (productName && eventName && configId) {
            key = productName.hashCode() + eventName.hashCode() + configId.hashCode() + type
        } else {
            key = Long.MAX_VALUE as String
        }
        key
    }

    /////////////////// Business Configuration cache ////////////////////////
    def prepareBusinessConfigurationCache() {
        BusinessConfiguration.findAllByEnabled(true).each {
            getCache(CACHE_NAME_BUSINESS_CONFIGURATION).put(it.ruleName, it)
        }
    }

    void prepareProductIdsAndNameCacheForAllUsers() {
        List<User> userList = User.list()
        userList.each { User user ->
            prepareProductIdsAndNameCacheForUser(user)
        }
    }

    void prepareProductIdsAndNameCacheForUser(User user) {
        List<String> allowedProductIdsList = []
        List<String> allowedProductNamesList = []
        user.groups.each { Group group ->
            if(group.groupType != GroupType.WORKFLOW_GROUP){
                if(getCache(CACHE_NAME_PV_GROUP_ALLOWED_ID).get(group.id)){
                    allowedProductIdsList.addAll(getCache(CACHE_NAME_PV_GROUP_ALLOWED_ID).get(group.id).toList())
                }
                if(getCache(CACHE_NAME_PV_GROUP_ALLOWED_PRODUCT).get(group.id)){
                    allowedProductNamesList.addAll(getCache(CACHE_NAME_PV_GROUP_ALLOWED_PRODUCT).get(group.id).toList())
                }
            }
        }
        getCache(CACHE_NAME_PV_USER_PRODUCTS_IDS).put(user.id, allowedProductIdsList)
        getCache(CACHE_NAME_PV_USER_PRODUCTS_NAMES).put(user.id, allowedProductNamesList)
    }


    void prepareSafetyGroupProductNameCacheForAllUsers() {
        List<User> userList = User.list()
        userList.each { User user ->
            prepareSafetyGroupProductNameCacheForUser(user)
        }
    }

    void prepareSafetyGroupProductNameCacheForUser(User user) {
        List<String> allowedProductNamesList = []
        user.safetyGroups.each { SafetyGroup safetyGroup ->
            if(getCache(CACHE_NAME_PV_SAFETY_GROUP_ALLOWED_PRODUCT).get(safetyGroup.id)){
                allowedProductNamesList.addAll(getCache(CACHE_NAME_PV_SAFETY_GROUP_ALLOWED_PRODUCT).get(safetyGroup.id).toList())
            }
        }
        getCache(CACHE_NAME_PV_SAFETY_GROUP_PRODUCTS_NAMES).put(user.id, allowedProductNamesList)

    }

    def updateBusinessConfigurationCache(BusinessConfiguration businessConfiguration) {
        getCache(CACHE_NAME_BUSINESS_CONFIGURATION).put(businessConfiguration.ruleName, businessConfiguration)
    }

    def deleteBusinessConfigurationCache(BusinessConfiguration businessConfiguration) {
        getCache(CACHE_NAME_BUSINESS_CONFIGURATION).remove(businessConfiguration.ruleName)
    }

    def getAllBusinessConfigurations() {
        List<BusinessConfiguration> businessConfigurationList = []
        for (def key : getCache(CACHE_NAME_BUSINESS_CONFIGURATION).keySet()) {
            businessConfigurationList.add(getCache(CACHE_NAME_BUSINESS_CONFIGURATION).get(key))
        }
        businessConfigurationList
    }

    void clearBusinessConfigurationCache() {
        getCache(CACHE_NAME_BUSINESS_CONFIGURATION).clear()
    }

    void clearProductIdsAndNameCacheForUser(Long userId) {
        getCache(CACHE_NAME_PV_USER_PRODUCTS_NAMES).remove(userId)
        getCache(CACHE_NAME_PV_USER_PRODUCTS_IDS).remove(userId)
    }

    void clearSafetyGroupProductNameCacheForSafetyGroup(Long userId) {
        getCache(CACHE_NAME_PV_SAFETY_GROUP_PRODUCTS_NAMES).remove(userId)
    }

    /////////////////// Medical Concepts cache ////////////////////////
    def prepareMedicalConcepts() {
        MedicalConcepts.findAll().each {
            getCache(CACHE_NAME_MEDICAL_CONCEPTS).put(it.name, it)
        }
    }

    MedicalConcepts getMedicalConcepts(String value) {
        MedicalConcepts medicalConcepts = getCache(CACHE_NAME_MEDICAL_CONCEPTS).get(value)
        medicalConcepts
    }

    /////////////////// Activity Type cache ////////////////////////
    def prepareActivityTypeCache() {
        ActivityType.findAll().each {
            getCache(CACHE_NAME_ACTIVITY_TYPE).put(it.value.value, it)
        }
    }

    ActivityType getActivityTypeByValue(value) {
        ActivityType activityType = getCache(CACHE_NAME_ACTIVITY_TYPE).get(value)
        activityType
    }

    List<String> getProductIdsListByValue(Long value) {
        List<String> productIdsList = getCache(CACHE_NAME_PV_USER_PRODUCTS_IDS).get(value)
        productIdsList
    }

    List<String> getProductNamesListByValue(Long value) {
        List<String> productNamesList = getCache(CACHE_NAME_PV_USER_PRODUCTS_NAMES).get(value)
        productNamesList
    }

    List<String> getProductNamesListForSafetyLeadByValue(Long value) {
        List<String> productNamesList = getCache(CACHE_NAME_PV_SAFETY_GROUP_PRODUCTS_NAMES).get(value)
        productNamesList
    }

    /////////////////// Workflowe cache ////////////////////////
    def prepareWorkflowCache() {
        PVSState.findAll().each {
            getCache(CACHe_NAME_PVS_STATE).put(it.value, it)
        }
    }

    PVSState getPvsStateByValue(value) {
        PVSState pvsState = getCache(CACHe_NAME_PVS_STATE).get(value)
        pvsState
    }

    def updateWorkflowCache(PVSState workflow) {
        getCache(CACHe_NAME_PVS_STATE).put(workflow.value, workflow)
    }

    def deleteWorkflowCache(PVSState workflow) {
        getCache(CACHe_NAME_PVS_STATE).remove(workflow.value)
    }

    void clearWorkflowCache() {
        getCache(CACHe_NAME_PVS_STATE).clear()
    }

    void prepareDefaultWorkflow() {
        def workflowVal = Holders.config.pvsignal.workflow.default.value
        this.defaultWorkflow = PVSState.findByValue(workflowVal)
    }

    PVSState getDefaultWorkflow() {
        this.defaultWorkflow
    }

    /////////////////// Disposition cache ////////////////////////
    def prepareDispositionCache() {
        Disposition.list().each {
            getCache(CACHE_NAME_DISPOSITION).put(it.id, it)
        }
    }

    Set<Disposition> getDispositionByNameInList(List<String> list){
        getCache(CACHE_NAME_DISPOSITION).values().findAll{list?.contains(it.displayName)}
    }

    Map<Long,Disposition> getDispositionCacheMap(){
        getCache(CACHE_NAME_DISPOSITION)
    }

    List<Disposition> getDispositionListById(List<Long> idList){
        List<Disposition> dispositionList = []
        idList.each {
            dispositionList.add(getCache(CACHE_NAME_DISPOSITION).get(it))
        }
        return dispositionList
    }
    Disposition getDispositionByValue(Long id) {
        Disposition disposition =id?getCache(CACHE_NAME_DISPOSITION).get(id):null
        disposition
    }

    List<Disposition> getDispositionByReviewCompleted() {
        def dispositionList = getCache(CACHE_NAME_DISPOSITION).values()
        def reviewedDisposition = []
        for (Disposition disposition : dispositionList) {
            if (disposition.reviewCompleted) {
                reviewedDisposition.add(disposition)
            }
        }
        reviewedDisposition
    }
    List<Disposition> getValidatedDisposition() {
        def dispositionList = getCache(CACHE_NAME_DISPOSITION).values()
        def reviewedDisposition = []
        for (Disposition disposition : dispositionList) {
            if (disposition.validatedConfirmed) {
                reviewedDisposition.add(disposition)
            }
        }
        reviewedDisposition
    }
    List<Disposition> getNotReviewCompletedDisposition() {
        List<Disposition> dispositionList = getCache(CACHE_NAME_DISPOSITION).values() as List<Disposition>
        dispositionList.findAll { !it.reviewCompleted }
    }

    List<Disposition> getNotReviewCompletedAndClosedDisposition() {
        List<Disposition> dispositionList = getCache(CACHE_NAME_DISPOSITION).values() as List<Disposition>
        dispositionList.findAll { !it.reviewCompleted && !it.closed }
    }

    def updateDispositionCache(Disposition disposition) {
        getCache(CACHE_NAME_DISPOSITION).put(disposition.id, disposition)
    }

    def deleteDispositionCache(Disposition disposition) {
        getCache(CACHE_NAME_DISPOSITION).remove(disposition.id)
    }

    void clearDispositionCache() {
        getCache(CACHE_NAME_DISPOSITION).clear()
    }

    void prepareDefaultDisposition() {
        def dispositionVal = Holders.config.pvsignal.disposition.default.value
        this.defaultDisposition = Disposition.findByValue(dispositionVal)
    }

    Disposition getDefaultDisposition() {
        this.defaultDisposition
    }

    /////////////////// Priority cache ////////////////////////
    def preparePriorityCache() {
        log.debug("caching priority data")
        Priority.list().each {
            getCache(CACHE_NAME_PRIORITY).put(it.id, it)
            log.info(it.displayName + " : " +it.dispositionConfigs.size().toString())
            getCache(CACHE_NAME_PRIORITY_DISPOSITIONS).put(it.id, it.dispositionConfigs)
        }
    }

    Map<Long,Priority> getPriorityCacheMap(){
        getCache(CACHE_NAME_PRIORITY)
    }

    Priority getPriorityByValue(Long id) {
        if(!id){
            return null
        }
        Priority priority = getCache(CACHE_NAME_PRIORITY).get(id)
        priority
    }

    List<PriorityDispositionConfig> getDispositionConfigsByPriority(Long id) {
        CopyOnWriteArrayList<PriorityDispositionConfig> dispositionConfigs = getCache(CACHE_NAME_PRIORITY_DISPOSITIONS).get(id)
        dispositionConfigs
    }

    Priority prepareDefaultPriority() {
        String priorityVal = Holders.config.pvsignal.priority.signal.default.value ?: 'High'
        Priority.findByValue(priorityVal) ?: Priority.findByValue('High')
    }


    def updatePriorityCache(Priority priority) {
        getCache(CACHE_NAME_PRIORITY).put((Object)priority.id, priority)
        getCache(CACHE_NAME_PRIORITY_DISPOSITIONS).put((Object)priority.id, priority.dispositionConfigs)
    }

    def deletePriorityCache(Priority priority) {
        getCache(CACHE_NAME_PRIORITY).remove(priority.id)
        getCache(CACHE_NAME_PRIORITY_DISPOSITIONS).remove(priority.id)
    }

    void clearPriorityCache() {
        getCache(CACHE_NAME_PRIORITY).clear()
        getCache(CACHE_NAME_PRIORITY_DISPOSITIONS).clear()
    }

    /////////////////// Alert Comment cache ////////////////////////
    def prepareAlertCommentCache() {
        Map<String, List<AlertComment>> tmpData = new HashMap<>()

        int offset = 0
        int batchSize = 1000
        List<AlertComment> acList = null
        while (acList = AlertComment.list(offset: offset, max: batchSize)) {
            for (AlertComment alertComment : acList) {
                String key = produceKey(alertComment.productName, alertComment.eventName,
                        alertComment.configId, alertComment.alertType)
                List<AlertComment> dataList = tmpData.get(key)
                if (!dataList) {
                    dataList = new LinkedList<>()
                    tmpData.put(key, dataList)
                }
                dataList.add(alertComment)
            }
            offset = offset + acList.size()
        }

        tmpData.each { key, data ->
            this.getCache(CACHE_NAME_ALERT_COMMENT).put(key, data)
        }
    }


    def insertAlertCommentHistory(AlertComment alertComment) {
        String key = produceKey(alertComment.productName, alertComment.eventName,
                alertComment.configId, alertComment.alertType)

        if (key) {
            List<AlertComment> list = this.getCache(CACHE_NAME_ALERT_COMMENT).get(key) as List
            if (!list) {
                list = new LinkedList<>()
            }

            if (alertComment) {
                list.add(alertComment)
                Map cacheMap = this.getCache(CACHE_NAME_ALERT_COMMENT)
                cacheMap.set(key, list)
            }
        }
    }

    List<AlertComment> getAlertComments(String productName, String eventName, Long configId, String type) {
        String key = produceKey(productName, eventName, configId, type)
        if (key) {
            this.getCache(CACHE_NAME_ALERT_COMMENT).get(key)?.toList()
        } else {
            null
        }
        null
    }


    def prepareSingleCaseAlertCommentCache() {
        Map<String, List<AlertComment>> tmpData = new HashMap<>()

        int offset = 0
        int batchSize = 1000
        List<AlertComment> acList = null
        while (acList = AlertComment.list(offset: offset, max: batchSize)) {
            for (AlertComment alertComment : acList) {
                if (alertComment.caseNumber) {
                    String key = producekeyForCaseHistory(alertComment.caseNumber, alertComment.configId)
                    List<AlertComment> dataList = tmpData.get(key)
                    if (!dataList) {
                        dataList = new LinkedList<>()
                    }
                    dataList.add(alertComment)
                    tmpData.put(key, dataList)
                }
            }
            offset = offset + acList.size()
        }

        tmpData.each { key, data ->
            this.getCache(CACHE_NAME_SINGLE_ALERT_COMMENT).put(key, data)
        }
    }


    List<AlertComment> getSingleCaseAlertComments(String caseNumber, Long configId) {
        String key = producekeyForCaseHistory(caseNumber, configId)
        if (key) {
            this.getCache(CACHE_NAME_SINGLE_ALERT_COMMENT).get(key)?.toList()
        } else {
            null
        }
        null
    }

    def insertSingleCaseAlertCommentHistory(AlertComment alertComment) {
        String key = producekeyForCaseHistory(alertComment.caseNumber, alertComment.configId)
        if (key) {
            List<AlertComment> list = this.getCache(CACHE_NAME_SINGLE_ALERT_COMMENT).get(key) as List
            if (!list) {
                list = new LinkedList<>()
            }

            if (alertComment) {
                list.add(alertComment)
                Map cacheMap = this.getCache(CACHE_NAME_SINGLE_ALERT_COMMENT)
                cacheMap.set(key, list)
            }
        }
    }


    void deleteAlertComment(AlertComment ac) {
        if (ac.cacheKey) {
            this.hazelcastService.getCache(CACHE_NAME_ALERT_COMMENT).remove(ac.cacheKey)
        }
    }

    void clearAlertCommentCache() {
        this.getCache(CACHE_NAME_ALERT_COMMENT).clear()
    }

    String producekeyForCaseHistory(String caseNumber, Long configId) {
        String key
        if (caseNumber && configId) {
            key = caseNumber.hashCode().toString() + Long.hashCode(configId)
        } else {
            key = Long.MAX_VALUE as String
        }
        key
    }

    def prepareSMQCache() {
        smqList.clear()
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.rows("select * from smq_narrow_search").collect {
                setSMQList([name: it.smq_name + " (Narrow)", code: it.smq_code + "(N)"])
            }
            sql.rows("select * from smq_broad_search").collect {
                setSMQList([name: it.smq_name + " (Broad)", code: it.smq_code + "(B)"])
            }
        } finally {
            sql.close()
        }
        smqList.sort { it.name }
    }

    void setSMQList(Map smp) { smqList.add(smp) }

    Set<String> getSMQList() { smqList }

    Map getCache(String cacheName) {
        def cache = hazelcastService.getCache(cacheName)
        cache
    }

    List getListCache(String cacheName) {
        def cache = hazelcastService.getList(cacheName)
        cache
    }

    Set getSetCache(String cacheName) {
        def cache = hazelcastService.getSet(cacheName)
        cache
    }

    void saveToCache(String cacheName, Object key, Object value) {
        if (cacheName && key && value) {
            getCache(cacheName).put(key, value)
        }
    }

    void preparePvDictionaryLocalization() {
        ProductDictionaryMetadata.i18nValues.each {
            String lang = it.key
            it.value.each {
                getCache(CACHE_NAME_PV_DICTIONARY_LOCALIZATION).put(lang + "_" + it.key, it.value)
            }
        }
    }


    void prepareUserPreferenceCache() {
        User.list().each { User user ->
            setUserCacheByUserName(user)
            setUserCacheByUserId(user)
            setPreferenceCache(user)
        }
    }

    void setUserCacheByUserId(User user) {
        getCache(CACHE_NAME_PV_USER_ID).put(user.id, user)
    }

    User getUserByUserId(Long userId) {
        getCache(CACHE_NAME_PV_USER_ID).get(userId)
    }


    void setUserCacheByUserName(User user) {
        getCache(CACHE_NAME_PV_USER).put(user.username, user)
    }

    List<User> getAllUsersFromCacheByGroup(Long groupId){
        getCache(CACHE_NAME_PV_USER).findAll {
           it.value.enabled ? (it.value.groups? it.value.groups.id.contains(groupId):false) : false
        }.collect {it.value}
    }

    void setPreferenceCache(User user) {
        getCache(CACHE_NAME_PV_USER_PREFERENCE).put(user.id, user.preference)
    }


    Preference getPreferenceByUserId(Long userId) {
        if (CACHE_NAME_PV_USER_PREFERENCE != null) {
            getCache(CACHE_NAME_PV_USER_PREFERENCE)?.get(userId)
        }
    }

    User getUserByUserName(String userName) {
        getCache(CACHE_NAME_PV_USER).get(userName)
    }

    // To fetch user irrespective of case
    User getUserByUserNameIlike(String userName) {
        String matchUserName = getCache(CACHE_NAME_PV_USER).keySet().find { String names ->names.equalsIgnoreCase(userName)}
        if(matchUserName == null){
            matchUserName = getCache(CACHE_NAME_PV_USER).values()?.find{User user ->user?.fullName?.equalsIgnoreCase(userName)}
        }
        if(matchUserName){
            return  getCache(CACHE_NAME_PV_USER).get(matchUserName)
        }else{
            log.info("No user found for given name")
            return null
        }

    }

    List<User> generateUserListFromCache(){
        getCache(CACHE_NAME_PV_USER).collect {it.value}
    }

    String getPvDictionaryLocalizedString(String lang, String code) {
        getCache(CACHE_NAME_PV_DICTIONARY_LOCALIZATION)?.get(lang + "_" + code)
    }


    void prepareProductsUsed() {
        List<String> productList = SingleCaseAlert.createCriteria().list {
            projections {
                distinct('productName')
            }
        } as List<String>

        productList += AggregateCaseAlert.createCriteria().list {
            projections {
                distinct('productName')
            }
            'executedAlertConfiguration' {
                'eq'('selectedDatasource', Constants.DataSource.PVA)
            }
        }
        List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllBySelectedDatasource(Constants.DataSource.PVA)
        executedConfigurationList.each {
            if(it.getProductNameList()){
                productList.addAll(it.getProductNameList())
            }
        }
        Set productSet = productList as Set
        getSetCache(CACHE_NAME_PRODUCTS_USED).addAll(productSet)
    }

    Set<String> getProductsUsedSet() {
        getSetCache(CACHE_NAME_PRODUCTS_USED)
    }

    void addInProductsUsedCache(Set<String> productSet) {
        getSetCache(CACHE_NAME_PRODUCTS_USED).addAll(productSet)
    }

    void prepareGroupAndSafetyGroupProductsCacheForUser(User user) {
        if(Holders.config.pvsignal.product.based.security){
            clearProductIdsAndNameCacheForUser(user.id)
            prepareProductIdsAndNameCacheForUser(user)
            clearSafetyGroupProductNameCacheForSafetyGroup(user.id)
            prepareSafetyGroupProductNameCacheForUser(user)
        }
        setUserCacheByUserName(user)
        setUserCacheByUserId(user)
        setPreferenceCache(user)
    }

    void updateProductsCacheForGroup(List<Long> groupList) {
        List<User> userList = User.createCriteria().list {
            'groups' {
                'in'("id", groupList)
            }
        }
        userList.each { User user ->
            clearProductIdsAndNameCacheForUser(user.id)
            prepareProductIdsAndNameCacheForUser(user)
        }
    }

    void updateProductsCacheForSafetyGroup(List<Long> safetyGroupList) {
        List<User> userList = User.createCriteria().list {
            'safetyGroups' {
                'in'("id", safetyGroupList)
            }
        }
        userList.each { User user ->
            clearSafetyGroupProductNameCacheForSafetyGroup(user.id)
            prepareSafetyGroupProductNameCacheForUser(user)
        }
    }

    void prepareGroupCache() {
        ProductDictionaryCache productDictionaryCache
        Set<String> allowedDataIdsSet = new HashSet<>()
        Set<String> allowedDataProductsSet = new HashSet<>()

        Group.list().each { Group group ->
            getCache(CACHE_NAME_PV_GROUP).put(group.id, group)
            if (Holders.config.pvsignal.product.based.security) {
                if (group.groupType != GroupType.WORKFLOW_GROUP) {
                    productDictionaryCache = ProductDictionaryCache.findByGroup(group)
                    productDictionaryCache?.allowedDictionaryData?.each { data ->
                        if (dataObjectService.getDictionaryLevelsList().contains(data.fieldLevelId)) {
                            allowedDataIdsSet.addAll(data.allowedDataIds?.split(","))
                            allowedDataProductsSet.addAll(data.allowedData?.split(","))
                        }
                    }
                    getCache(CACHE_NAME_PV_GROUP_ALLOWED_PRODUCT).put(group.id, allowedDataProductsSet)
                    getCache(CACHE_NAME_PV_GROUP_ALLOWED_ID).put(group.id, allowedDataIdsSet)

                }
                allowedDataIdsSet.clear()
                allowedDataProductsSet.clear()
            }
        }
    }

    void prepareSafetyGroupCache() {
        ProductDictionaryCache productDictionaryCache
        Set<String> allowedDataProductsSet = new HashSet<>()

        SafetyGroup.list().each { SafetyGroup safetyGroup ->
            productDictionaryCache = ProductDictionaryCache.findBySafetyGroup(safetyGroup)
            productDictionaryCache?.allowedDictionaryData?.each { data ->
                if (dataObjectService.getDictionaryLevelsList().contains(data.fieldLevelId)) {
                    allowedDataProductsSet.addAll(data.allowedData?.split(","))
                }
            }
            getCache(CACHE_NAME_PV_SAFETY_GROUP_ALLOWED_PRODUCT).put(safetyGroup.id,allowedDataProductsSet)
            allowedDataProductsSet.clear()
        }
    }

    Group getGroupByGroupId(Long groupId) {
        getCache(CACHE_NAME_PV_GROUP).get(groupId)
    }

    List<Group> getDefaultWorkflowGroups(){
        List<Group> groupList = []
        getCache(CACHE_NAME_PV_GROUP).each { k,v ->
            if(v.groupType == GroupType.WORKFLOW_GROUP){
                groupList.add(v)
            }
        }
        groupList
    }

    List<Group> getUserGroups(){
        List<Group> groupList = []
        getCache(CACHE_NAME_PV_GROUP).each { k,v ->
            if(v.groupType == GroupType.USER_GROUP){
                groupList.add(v)
            }
        }
        groupList
    }

    void setGroupCache(Group group) {
        getCache(CACHE_NAME_PV_GROUP).put(group.id, group)
        if(Holders.config.pvsignal.product.based.security) {
            Set<String> allowedDataIdsSet = new HashSet<>()
            Set<String> allowedDataProductsSet = new HashSet<>()
            ProductDictionaryCache productDictionaryCache = ProductDictionaryCache.findByGroup(group)
            productDictionaryCache?.allowedDictionaryData?.each { data ->
                if (dataObjectService.getDictionaryLevelsList().contains(data.fieldLevelId)) {
                    allowedDataIdsSet.addAll(data.allowedDataIds?.split(","))
                    allowedDataProductsSet.addAll(data.allowedData?.split(","))
                }
            }
            getCache(CACHE_NAME_PV_GROUP_ALLOWED_PRODUCT).put(group.id, allowedDataProductsSet)
            getCache(CACHE_NAME_PV_GROUP_ALLOWED_ID).put(group.id, allowedDataIdsSet)
        }
    }

    void setProductGroupCache(SafetyGroup safetyGroup){
        Set<String> allowedDataProductsSet = new HashSet<>()
        ProductDictionaryCache productDictionaryCache = ProductDictionaryCache.findBySafetyGroup(safetyGroup)
        productDictionaryCache?.allowedDictionaryData?.each { data ->
            if (dataObjectService.getDictionaryLevelsList().contains(data.fieldLevelId)) {
                allowedDataProductsSet.addAll(data.allowedData?.split(","))
            }
        }
        getCache(CACHE_NAME_PV_SAFETY_GROUP_ALLOWED_PRODUCT).put(safetyGroup.id,allowedDataProductsSet)

    }

    void prepareBusinessConfigCacheForSelectionType() {
        List<BusinessConfiguration> businessConfigurationList = []
        getCache(CACHE_NAME_BUSINESS_CONFIG_TYPE).clear()
        dataObjectService.getDictionaryLevelsList().each {
            businessConfigurationList = BusinessConfiguration.findAllByEnabledAndProductDictionarySelection(true, it.toString())
            getCache(CACHE_NAME_BUSINESS_CONFIG_TYPE).put(it.toString(), businessConfigurationList)
        }
    }

    List<BusinessConfiguration> getBusinessConfigByProdDictSelection(String productDictionarySelection) {
        getCache(CACHE_NAME_BUSINESS_CONFIG_TYPE).get(productDictionarySelection)
    }

    void prepareActionTemplateCache() {
        ActionTemplate.list().each { ActionTemplate actionTemplate ->
            setActionTemplateCache(actionTemplate)
        }
    }

    void setActionTemplateCache(ActionTemplate actionTemplate) {
        getCache(CACHE_NAME_ACTION_TEMPLATE).put(actionTemplate.id, actionTemplate)
    }


    void clearActionTemplateCache(Long actionTemplateId) {
        getCache(CACHE_NAME_ACTION_TEMPLATE).remove(actionTemplateId)
    }

    ActionTemplate getActionTemplateCache(Long actionTemplateId) {
        getCache(CACHE_NAME_ACTION_TEMPLATE).get(actionTemplateId)

    }

    void prepareActionConfigurationCache() {
        ActionConfiguration.list().each { ActionConfiguration actionConfiguration ->
            setActionConfigurationCache(actionConfiguration)
        }
    }

    ActionConfiguration getActionConfigurationCache(Long actionConfigId) {
        getCache(CACHE_NAME_ACTION_CONFIG).get(actionConfigId)
    }

    void setActionConfigurationCache(ActionConfiguration actionConfiguration) {
        getCache(CACHE_NAME_ACTION_CONFIG).put(actionConfiguration?.id, actionConfiguration)
    }

    void removeActionConfiguration(Long actionConfigId) {
        getCache(CACHE_NAME_ACTION_CONFIG).remove(actionConfigId)
    }

    void prepareActionTypeCache() {
        ActionType.list().each { ActionType actionType ->
            setActionTypeCache(actionType)
        }
    }

    ActionType getActionTypeCache(Long actionTypeId) {
        getCache(CACHE_NAME_ACTION_TYPE).get(actionTypeId)
    }

    void setActionTypeCache(ActionType actionType) {
        getCache(CACHE_NAME_ACTION_TYPE).put(actionType.id, actionType)
    }

    void removeActionType(Long actionTypeId) {
        getCache(CACHE_NAME_ACTION_TYPE).remove(actionTypeId)
    }

    void prepareRuleInformationCache() {
        BusinessConfiguration.findAllByEnabled(true).each { BusinessConfiguration businessConfiguration ->
            setRuleInformationCache(businessConfiguration)
        }
    }

    void setRuleInformationCache(BusinessConfiguration businessConfiguration) {
        clearRuleInformationCache(businessConfiguration.id)
        List<RuleInformation> ruleInformationList = businessConfiguration.ruleInformations as List<RuleInformation>
        getCache(CACHE_NAME_RULE_INFORMATION).put(businessConfiguration.id, ruleInformationList)
    }

    List<RuleInformation> getRuleInformationList(Long businessConfigId) {
        getCache(CACHE_NAME_RULE_INFORMATION).get(businessConfigId)
    }

    void clearRuleInformationCache(Long businessConfigId) {
        getCache(CACHE_NAME_RULE_INFORMATION).remove(businessConfigId)
    }

    def prepareStratificationColumnCache() {
        Sql sql = new Sql(dataSource_pva)
        try {
            GroovyRowResult vwNameRow = sql.firstRow(SignalQueryHelper.ebgm_strat_view_sql())
            if(vwNameRow && vwNameRow.DSP_VIEW_NAME) {
                String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                sql.rows(query).each {
                    getCache(CACHE_NAME_STRAT_EBGM_COLUMNS).put(String.valueOf(it.ID), it.LABEL)
                }
            }
        } finally {
            sql.close()
        }
    }

    def prepareSubGroupColumnCache(){
        Sql sql = null
        Sql sql_faers = null
        Sql sql_vaers = null
        Sql sql_vigibase = null
        try {
            sql = new Sql(dataSource_pva)
            if (Holders.config.statistics.enable.ebgm) {
                sql.eachRow(SignalQueryHelper.ebgm_sub_group_view_sql("PVA-DB")) { vwNameRow ->
                    if (vwNameRow && vwNameRow.DSP_VIEW_NAME && (vwNameRow.PVS_STR_COLUMN == 'AGE_GROUP' || vwNameRow.PVS_STR_COLUMN == 'GENDER')) {
                        getCache(CACHE_SUB_GROUPS_ENABLED).put("ebgmSubGrpEnabled",true)
                        String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                        Map<String, String> idLabelMap = new LinkedHashMap<>()
                        sql.rows(query).each {
                            idLabelMap.put(String.valueOf(it.ID), it.LABEL)
                        }
                        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).put(vwNameRow.PVS_STR_COLUMN, idLabelMap)
                    }else{
                        getCache(CACHE_SUB_GROUPS_ENABLED).put("ebgmSubGrpEnabled",false)
                    }
                }
            }

            if (Holders.config.signal.faers.enabled && Holders.config.statistics.faers.enable.ebgm) {
                sql_faers = new Sql(dataSource_faers)
                sql_faers.eachRow(SignalQueryHelper.ebgm_sub_group_view_sql("FAERS-DB")) { vwNameRow ->
                    if (vwNameRow && vwNameRow.DSP_VIEW_NAME) {
                        getCache(CACHE_SUB_GROUPS_ENABLED).put("faersEbgmSubGrpEnabled",true)
                        String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                        Map<String, String> idLabelMap = new LinkedHashMap<>()
                        sql_faers.rows(query).each {
                            idLabelMap.put(String.valueOf(it.ID), it.LABEL)
                        }
                        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).put(vwNameRow.PVS_STR_COLUMN + "_" + Constants.DataSource.DATASOURCE_FAERS, idLabelMap)
                    }else{
                        getCache(CACHE_SUB_GROUPS_ENABLED).put("faersEbgmSubGrpEnabled",false)
                    }
                }
            }

            if (Holders.config.signal.vaers.enabled) {
                sql_vaers = new Sql(dataSource_vaers)
                sql_vaers.eachRow(SignalQueryHelper.ebgm_sub_group_view_sql("VAERS-DB")) { vwNameRow ->
                    if (vwNameRow && vwNameRow.DSP_VIEW_NAME) {
                        String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                        Map<String, String> idLabelMap = new LinkedHashMap<>()
                        sql_vaers.rows(query).each {
                            idLabelMap.put(String.valueOf(it.ID), it.LABEL)
                        }
                        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).put(vwNameRow.PVS_STR_COLUMN + "_" + Constants.DataSource.DATASOURCE_VAERS, idLabelMap)
                    }
                }
            }

            if (Holders.config.signal.vigibase.enabled) {
                sql_vigibase = new Sql(dataSource_vigibase)
                sql_vigibase.eachRow(SignalQueryHelper.ebgm_sub_group_view_sql("VIGIBASE-DB")) { vwNameRow ->
                    if (vwNameRow && vwNameRow.DSP_VIEW_NAME) {
                        String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                        Map<String, String> idLabelMap = new LinkedHashMap<>()
                        sql_vigibase.rows(query).each {
                            idLabelMap.put(String.valueOf(it.ID), it.LABEL)
                        }
                        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).put(vwNameRow.PVS_STR_COLUMN + "_" + Constants.DataSource.DATASOURCE_VIGIBASE, idLabelMap)
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        } finally {
            sql?.close()
            sql_faers?.close()
            sql_vaers?.close()
            sql_vigibase?.close()
        }
    }

    String getStratificationColumnName(String columnId) {
        getCache(CACHE_NAME_STRAT_EBGM_COLUMNS).get(columnId)
    }

    Map getSubgrpColumns(String columnId) {
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).get(columnId)
    }

    List getSubGroupColumns() {
        List lables = []
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).each { key, value ->
            if (!key.toString().contains(Constants.DataSource.DATASOURCE_FAERS))
                lables.add(value.values())
        }
        lables
    }

    List getSubGroupColumnFaers() {
        List lables = []
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).each { key, value ->
            if (key.toString().contains(Constants.DataSource.DATASOURCE_FAERS))
                lables.add(value.values())
        }
        lables
    }

    List getSubGroupColumnVaers() {
        List lables = []
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).each { key, value ->
            if (key.toString().contains(Constants.DataSource.DATASOURCE_VAERS))
                lables.add(value.values())
        }
        lables
    }

    List getSubGroupColumnVigibase() {
        List lables = []
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).each { key, value ->
            if (key.toString().contains(Constants.DataSource.DATASOURCE_VIGIBASE))
                lables.add(value.values())
        }
        lables
    }

    def prepareAllOtherSubGroupColumnCache(){
        Sql sql = null
        Map finalSubGroupMap = [:]
        try {
            List ebgmSubGroupingFields = ['EBGM', 'EB05', 'EB95']
            if (Holders.config.statistics.enable.prr || Holders.config.statistics.enable.ror) {
                Map<String, ArrayList<String>> columnMap = [:]
                List subGroupingFields = Holders.config.subgrouping.pva.subGroupColumnsList?.values()?.flatten()
                sql = new Sql(dataSource_pva)
                sql?.eachRow(SignalQueryHelper.other_sub_group_view_sql()) { vwNameRow ->
                    getCache(CACHE_SUB_GROUPS_ENABLED).put("prrSubGrpEnabled",true)
                    if (vwNameRow && vwNameRow.DSP_VIEW_NAME) {
                        String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                        List<String> labelList = new ArrayList<>()
                        sql?.rows(query).each {
                            labelList.add(it.LABEL)
                        }
                        columnMap.put(vwNameRow.PVS_STR_COLUMN, labelList)
                    }
                }
                boolean prrSubGrpEnabled = getCache(CACHE_SUB_GROUPS_ENABLED).get("prrSubGrpEnabled")
                if(prrSubGrpEnabled){
                    sql?.eachRow(SignalQueryHelper.rel_ror_sub_group_enabled()){ row ->
                        Integer integerValue = 0
                        if(row.PVS_VALUE){
                            // Converting the content of the CLOB to Integer
                            integerValue = Integer.valueOf(row.PVS_VALUE.trim())
                        }
                        if(integerValue == 1){
                            getCache(CACHE_SUB_GROUPS_ENABLED).put("rorRelSubGrpEnabled",true)
                        }else{
                            getCache(CACHE_SUB_GROUPS_ENABLED).put("rorRelSubGrpEnabled",false)
                        }
                    }
                }else{
                    getCache(CACHE_SUB_GROUPS_ENABLED).put("rorRelSubGrpEnabled",false)
                }
                subGroupingFields?.each { it ->
                    finalSubGroupMap.put(it, columnMap)
                }
            }
            if (Holders.config.statistics.enable.ebgm) {
                Map<String, ArrayList<String>> ebgmColumnMap = [:]
                sql?.eachRow(SignalQueryHelper.ebgm_other_sub_group_view_sql()) { vwNameRow ->
                    if (vwNameRow && vwNameRow.DSP_VIEW_NAME) {
                        String query = SignalQueryHelper.ebgm_strat_column_sql(vwNameRow.DSP_VIEW_NAME)
                        List<String> labelList = new ArrayList<>()
                        sql?.rows(query)?.each {
                            labelList.add(it.LABEL)
                        }
                        ebgmColumnMap.put(vwNameRow.PVS_STR_COLUMN, labelList)
                    }
                }
                ebgmSubGroupingFields.each { it ->
                    finalSubGroupMap.put(it, ebgmColumnMap)
                }
            }
            getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).put(Constants.DataSource.PVA, finalSubGroupMap)
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
        } finally {
            sql?.close()
        }
    }
    def getSubGroupsEnabled(String subgrp){
        return getCache(CACHE_SUB_GROUPS_ENABLED).get(subgrp)
    }

    Map getAllOtherSubGroupColumnsListMap(String dataSource){
        Map allSubGroupListMap = [:]
        getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each{ category, value ->
            List eachCategoryColumnList = []
            value.each{ subGroup, columns ->
                eachCategoryColumnList = eachCategoryColumnList + columns
            }
            allSubGroupListMap.put(category,eachCategoryColumnList)
        }
        return allSubGroupListMap
    }
    Map getRelativeSubGroupColumnsListMap(String dataSource){
        boolean rorRelSubGrpEnabled = getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
        Map allSubGroupListMap = [:]
        if(rorRelSubGrpEnabled) {
            getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each { category, value ->
                if (category.startsWith('ROR')) {
                    List eachCategoryColumnList = []
                    value.each { subGroup, columns ->
                        eachCategoryColumnList = eachCategoryColumnList + columns
                    }
                    String key = category.toString() + "-R"
                    allSubGroupListMap.put(key, eachCategoryColumnList)
                }
            }
        }
        return allSubGroupListMap
    }
    Map getAllOtherSubGroupMapCombined(String dataSource){
        Map allSubGroupListMap = [:]
        getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each{ category, value ->
            value.each{ subGroup, columns ->
                columns.each { column ->
                    String columnName = category.replaceAll('_',' ') + "(" + column + ")"
                    String label = toCamelCase(category)+"SubGroup"
                    allSubGroupListMap.put(columnName,label)
                }
            }
        }
        return allSubGroupListMap
    }

    Map getRelSubGroupMapCombined(String dataSource) {
        Map allSubGroupListMap = [:]
        boolean rorRelSubGrpEnabled = getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
        if(rorRelSubGrpEnabled) {
            getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each { category, value ->
                if (category.startsWith('ROR')) {
                    value.each { subGroup, columns ->
                        columns.each { column ->
                            String columnName = category.replaceAll('_', ' ') + "-R" + "(" + column + ")"
                            String label = toCamelCase(category) + "RelSubGroup"
                            allSubGroupListMap.put(columnName, label)
                        }
                    }
                }
            }
        }
        return allSubGroupListMap
    }

    Map allOtherSubGroupColumnUIList(String dataSource){
        Map allSubGroupListMap = [:]
        getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each{ category, value ->
            value.each{ subGroup, columns ->
                columns.each { column ->
                    String columnName = category.replaceAll('_',' ') + "(" + column + ")"
                    String label = toCamelCase(category)+ column
                    allSubGroupListMap.put(columnName,label)
                }
            }
        }
        return allSubGroupListMap
    }
    Map relativeSubGroupColumnUIList(String dataSource){
        Map allSubGroupColumnSorting = [:]
        boolean rorRelSubGrpEnabled = getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
        if(rorRelSubGrpEnabled) {
            getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each { category, value ->
                if (category.startsWith('ROR')) {
                    value.each { subGroup, columns ->
                        columns.each { column ->
                            String columnName = category.replaceAll('_', ' ') + "-R" + "(" + column + ")"
                            String label = toCamelCase(category) + "Rel" + column
                            allSubGroupColumnSorting.put(columnName, label)
                        }
                    }
                }
            }
        }
        return allSubGroupColumnSorting
    }
    Map getAllOtherSubGroupColumnsCamelCase(String dataSource){
        Map allSubGroupListMap = [:]
        getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each{ category, value ->
            Map subGroupMap= [:]
            value.each{ subGroup, columns ->
                subGroupMap.put(subGroup,columns)
            }
            allSubGroupListMap.put(toCamelCase(category),subGroupMap)
        }
        return allSubGroupListMap
    }

     String toCamelCase(String text) {
        text = text.toLowerCase().replaceAll( "(-)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } ).replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        return  text
    }


    void updateSubGroupingConfiguration(){
        Sql sql = new Sql(dataSource_pva)
        Sql sql_faers
        try{
            List aggRptFieldSubGroupList = []
            sql.eachRow("select name from AGG_RPT_FIELD_MAPPING where TYPE = 'subGroup'",[]){row ->
                aggRptFieldSubGroupList.add(row.name)
            }
            List subGroupList = []
            Map<String,String> prrRorSubGroupMap =  allOtherSubGroupColumnUIList(Constants.DataSource.PVA)
            Map<String,String> relativeSubGroupMap = relativeSubGroupColumnUIList(Constants.DataSource.PVA)
            List ebgmSafetyOldSubGroup = getSubGroupColumns()?.flatten()
            ebgmSafetyOldSubGroup.each{it ->
                String keyNameEbgm = "ebgm"+it
                String keyNameEb05 = "eb05"+it
                String keyNameEb95 = "eb95"+it
                subGroupList.add(keyNameEbgm)
                subGroupList.add(keyNameEb05)
                subGroupList.add(keyNameEb95)
            }
            if(prrRorSubGroupMap){
                subGroupList += prrRorSubGroupMap.values()
            }
            if(relativeSubGroupMap){
                subGroupList += relativeSubGroupMap.values()
            }
            if(subGroupList) {
                List subGroupInsert = subGroupList - aggRptFieldSubGroupList

                sql.withBatch(Holders.config.signal.batch.size as Integer) { stmt ->
                    subGroupInsert.each { it ->
                        String initialName = ""
                        String fieldName = ""
                        if (it.toString()?.toUpperCase()?.startsWith("EBGM")) {
                            initialName = "EBGM"
                            fieldName = it - "ebgm"
                        } else if (it.toString()?.toUpperCase()?.startsWith("EB05")) {
                            initialName = "EB05"
                            fieldName = it?.toString() - "eb05"
                        } else if (it.toString()?.toUpperCase()?.startsWith("EB95")) {
                            initialName = "EB95"
                            fieldName = it?.toString() - "eb95"
                        } else if (it.toString()?.toUpperCase()?.startsWith("RORLCIREL")) {
                            initialName = "ROR LCI-R"
                            fieldName = it?.toString() - "rorLciRel"
                        } else if (it.toString()?.toUpperCase()?.startsWith("RORUCIREL")) {
                            initialName = "ROR UCI-R"
                            fieldName = it?.toString() - "rorUciRel"
                        } else if (it.toString()?.toUpperCase()?.startsWith("RORREL")) {
                            initialName = "ROR-R"
                            fieldName = it?.toString() - "rorRel"
                        } else if (it.toString()?.toUpperCase()?.startsWith("RORLCI")) {
                            initialName = "ROR LCI"
                            fieldName = it?.toString() - "rorLci"
                        } else if (it.toString()?.toUpperCase()?.startsWith("RORUCI")) {
                            initialName = "ROR UCI"
                            fieldName = it - "rorUci"
                        } else if (it.toString()?.toUpperCase()?.startsWith("ROR")) {
                            initialName = "ROR"
                            fieldName = it?.toString() - "ror"
                        } else if (it.toString()?.toUpperCase()?.startsWith("PRRLCI")) {
                            initialName = "PRR LCI"
                            fieldName = it?.toString() - "prrLci"
                        } else if (it.toString()?.toUpperCase()?.startsWith("PRRUCI")) {
                            initialName = "PRR UCI"
                            fieldName = it?.toString() - "prrUci"
                        } else if (it.toString()?.toUpperCase()?.startsWith("PRR")) {
                            initialName = "PRR"
                            fieldName = it?.toString() - "prr"
                        } else if (it.toString()?.toUpperCase()?.startsWith("CHISQUARE")) {
                            initialName = "Chi-Square"
                            fieldName = it?.toString() - "chiSquare"
                        }
                        String displayName = "${initialName}(${fieldName})"
                        String keyId = initialName.replace("-", "_").replace(" ", "_") + "_" + fieldName.toUpperCase()

                        String insertStatement = "insert into AGG_RPT_FIELD_MAPPING (OPTIONAL,ENABLED ,ISAUTOCOMPLETE,NAME,DISPLAY,DATA_TYPE,ALERT_TYPE,CONTAINER_VIEW,TYPE,IS_FILTER,IS_VISIBLE,IS_SMQ_VISIBLE,IS_ADVANCED_FILTER_FIELD,IS_BUSINESS_RULE_FIELD,IS_NEW_COLUMN,IS_HYPERLINK,KEY_ID,OLD_DISPLAY) values (0,1,0,'${it}','${displayName}','java.lang.Number','AGGREGATE_CASE_ALERT',2,'subGroup',1,1,1,1,1,0,0,'${keyId}','${displayName}')"
                        log.debug("subGroup safety insertStatement : " + insertStatement)
                        stmt.addBatch(insertStatement)
                    }
                }
            }
            if(aggRptFieldSubGroupList){
                List subGroupDelete = aggRptFieldSubGroupList-subGroupList
                if(subGroupDelete) {
                    String deleteQuery = "DELETE FROM AGG_RPT_FIELD_MAPPING WHERE name IN ("
                    for (int i = 0; i < subGroupDelete.size(); i++) {
                        if (i > 0) {
                            deleteQuery += ", "
                        }
                        deleteQuery += "'" + subGroupDelete[i] + "'"
                    }
                    deleteQuery += ")"
                    sql.execute(deleteQuery)
                }
            }
            if (Holders.config.signal.faers.enabled){
                sql_faers = new Sql(dataSource_faers)
                aggRptFieldSubGroupList = []
                sql_faers.eachRow("select name from AGG_RPT_FIELD_MAPPING where TYPE = 'subGroup'",[]){row ->
                    aggRptFieldSubGroupList.add(row.name)
                }
                subGroupList = []
                List ebgmFaersOldSubGroup = getSubGroupColumnFaers()?.flatten()
                ebgmFaersOldSubGroup.each{it ->
                    String keyNameEbgm = "ebgm"+it+"Faers"
                    String keyNameEb05 = "eb05"+it+"Faers"
                    String keyNameEb95 = "eb95"+it+"Faers"
                    subGroupList.add(keyNameEbgm)
                    subGroupList.add(keyNameEb05)
                    subGroupList.add(keyNameEb95)
                }
                if(subGroupList) {
                    List subGroupInsert = subGroupList - aggRptFieldSubGroupList
                    sql_faers.withBatch(Holders.config.signal.batch.size as Integer) { stmt ->
                        subGroupInsert.each { it ->
                            String initialName = ""
                            String fieldName = ""
                            if (it.toString()?.toUpperCase()?.startsWith("EBGM")) {
                                initialName = "EBGM"
                                fieldName = it - "ebgm"
                            } else if (it.toString()?.toUpperCase()?.startsWith("EB05")) {
                                initialName = "EB05"
                                fieldName = it?.toString() - "eb05"
                            } else if (it.toString()?.toUpperCase()?.startsWith("EB95")) {
                                initialName = "EB95"
                                fieldName = it?.toString() - "eb95"
                            }
                            fieldName = fieldName - "Faers"
                            String displayName = "${initialName}(${fieldName}) (F)"
                            String keyId = initialName.replace("-", "_").replace(" ", "_") + "_" + fieldName.toUpperCase()
                            String insertStatement = "insert into AGG_RPT_FIELD_MAPPING (OPTIONAL,ENABLED ,ISAUTOCOMPLETE,NAME,DISPLAY,DATA_TYPE,ALERT_TYPE,CONTAINER_VIEW,TYPE,IS_FILTER,IS_VISIBLE,IS_SMQ_VISIBLE,IS_ADVANCED_FILTER_FIELD,IS_BUSINESS_RULE_FIELD,IS_NEW_COLUMN,IS_HYPERLINK,KEY_ID,OLD_DISPLAY) values (0,1,0,'${it}','${displayName}','java.lang.Number','AGGREGATE_CASE_ALERT',2,'subGroup',1,1,1,1,1,0,0,'${keyId}','${displayName}')"
                            log.debug("subGroup faers insertStatement : " + insertStatement)
                            stmt.addBatch(insertStatement)
                        }
                    }
                }
                if(aggRptFieldSubGroupList){
                    List subGroupDelete = aggRptFieldSubGroupList-subGroupList
                    if(subGroupDelete) {
                        String deleteQuery = "DELETE FROM AGG_RPT_FIELD_MAPPING WHERE name IN ("
                        for (int i = 0; i < subGroupDelete.size(); i++) {
                            if (i > 0) {
                                deleteQuery += ", "
                            }
                            deleteQuery += "'" + subGroupDelete[i] + "'"
                        }
                        deleteQuery += ")"
                        sql_faers.execute(deleteQuery)
                    }
                }
            }

        }catch(Exception ex){
            ex.printStackTrace()
        }finally{
            sql?.close()
            sql_faers?.close()
        }
    }


    Map getAllOtherSubGroupColumns(String dataSource){
        Map allSubGroups = [:]
        allSubGroups = getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource)
        return allSubGroups
    }
    Map getAllOtherSubGroupKeyValues(String dataSource){
        Map allSubGroupKeyValue = [:]
        getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each{ category, value ->
            value.each{ subGroup, columns ->
                columns.each { it ->
                    String key = toCamelCase(category.toString()) + it
                    String label = toCamelCase(category)+"SubGroup"
                    allSubGroupKeyValue.put(key,label)
                }
            }
        }
        allSubGroupKeyValue
    }
    Map getRelSubGroupKeyValues(String dataSource){
        Map allSubGroupKeyValue = [:]
        boolean rorRelSubGrpEnabled = getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
        if(rorRelSubGrpEnabled) {
            getCache(CACHE_NAME_ALL_SUB_GROUP_COLUMNS).get(dataSource).each { category, value ->
                if (category.startsWith('ROR')) {
                    value.each { subGroup, columns ->
                        columns.each { it ->
                            String key = toCamelCase(category.toString()) + "Rel" + it
                            String label = toCamelCase(category) + "RelSubGroup"
                            allSubGroupKeyValue.put(key, label)
                        }
                    }
                }
            }
        }
        allSubGroupKeyValue
    }

    Map getSubGroupKeyValues(){
        Map lables = [:]
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).each { key, value ->
            if (!key.toString().contains(Constants.DataSource.DATASOURCE_FAERS))
                value.each {
                    lables.put(it.value, key.toString().contains('AGE') ? 'Age' : key.toString().capitalize())
                }
        }
        lables
    }
    Map getSubGroupKeyColumns(){
        Map lables = [:]
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).each { key, value ->
            if (!key.toString().contains(Constants.DataSource.DATASOURCE_FAERS))
                value.each {
                    String keyValue = ""
                    if(key.toString().contains('AGE')){
                        keyValue = "Age"
                    }else if(key.toString().contains('GENDER')){
                        keyValue = "Gender"
                    }
                    lables.put(it.value, keyValue)
                }
        }
        lables
    }

    Map getSubGroupKeyValuesFears(){
        Map lables = [:]
        getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS).each { key, value ->
            if (key.toString().contains(Constants.DataSource.DATASOURCE_FAERS))
                value.each {
                    lables.put(it.value, key.toString().contains('AGE') ? 'Age' : key.toString().replace('_FAERS','').capitalize())
                }
        }
        lables
    }

    Map getSubGroupMap(){
        return getCache(CACHE_NAME_SUB_GROUP_EBGM_COLUMNS)
    }
    void prepareAdvancedFilterPossibleValues() {
        Map domainPropertyMap = Holders.config.advancedFilter.possible.values.map as Map<String, String>
        domainPropertyMap.each { String key, String value ->
            if(key != "country" || (key == "country" && Holders.config.custom.caseInfoMap.Enabled != true)) {
                List<String> possibleValuesList = SingleCaseAlert.createCriteria().list {
                    projections {
                        groupProperty(key)
                    }
                    isNotNull(key)
                } as List<String>
                saveAdvancedFilterPossibleValues(key, possibleValuesList)
            } else {
                List<String> countries = prepareDerivedCountry()
                saveAdvancedFilterPossibleValues(key, countries)
            }
        }
        domainPropertyMap.each { String key, String value ->
            if(key != "country" || (key == "country" && Holders.config.custom.caseInfoMap.Enabled != true)) {
                List<String> possibleValuesList = SingleOnDemandAlert.createCriteria().list {
                    projections {
                        groupProperty(key)
                    }
                    isNotNull(key)
                } as List<String>
                saveAdvancedFilterPossibleValues(key, possibleValuesList)
            }
        }
    }

    void saveAdvancedFilterPossibleValues(String key, List<String> advancedFilterPossibleValuesList){
        if(key == "country" && Holders.config.custom.caseInfoMap.Enabled == true)
            key = "derived_country"
        advancedFilterPossibleValuesList.each {
            Set<String> list = getCache(CACHE_NAME_ADVANCED_FILTER_COLUMNS).get(key) as Set
            if (!list) {
                list = new HashSet<>()
            }
            list.add(it)
            getCache(CACHE_NAME_ADVANCED_FILTER_COLUMNS).put(key, list)
        }

    }

    List<String> getPossibleValuesByKey(String key) {
        if(key == "country" && Holders.config.custom.caseInfoMap.Enabled == true)
            key = "derived_country"
        getCache(CACHE_NAME_ADVANCED_FILTER_COLUMNS).get(key) as List<String>
    }

    void prepareUpperHierarchyProductDictionaryCache() {
        ExecutorService executorService = Executors.newFixedThreadPool(10)
        try {
            List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllByIsDeletedAndIsEnabled(false, true)
            log.debug("Thread Starts For Caching")
            List<Future> futureList = executedConfigurationList.collect { ExecutedConfiguration executedConfiguration ->
                executorService.submit({ ->
                    setUpperHierarchyProductDictionaryCache(executedConfiguration)
                } as Runnable)
            }
            futureList.each {
                it.get()
            }
        } catch(Throwable th){
            th.printStackTrace()
        } finally {
            executorService.shutdown()
        }
    }

    void setUpperHierarchyProductDictionaryCache(ExecutedConfiguration executedConfiguration) {
        if (pvsProductDictionaryService.isLevelGreaterThanProductLevel(executedConfiguration)) {
            clearUpperHierarchyProductDictionaryCache(executedConfiguration?.id)
            String products = pvsProductDictionaryService.fetchProductNamesfromUpperHierarchy(executedConfiguration?.productSelection)
            synchronized (this) {
                getCache(CACHE_NAME_UPPER_PRODUCT_DICTIONARY).put(executedConfiguration?.id, products)
            }
        }
    }

    String getUpperHierarchyProductDictionaryCache(Long executedConfigId) {
        getCache(CACHE_NAME_UPPER_PRODUCT_DICTIONARY).get(executedConfigId)
    }

    void clearUpperHierarchyProductDictionaryCache(Long executedConfigId) {
        getCache(CACHE_NAME_UPPER_PRODUCT_DICTIONARY).remove(executedConfigId)
    }

    void prepareCacheForEmailNotification() {
        EmailNotification.list().each { it ->
            getCache(CACHE_NAME_EMAIL_NOTIFICATION).put(it.key, it.isEnabled)
            getCache(CACHE_NAME_EMAIL_NOTIFICATION_MODULES).put(it.key, it.moduleName)
        }
    }

    boolean isEmailNotificationEnabled(String key) {
        getCache(CACHE_NAME_EMAIL_NOTIFICATION).get(key)
    }

    void setEmaiNotificationCache(String key, boolean enabledFlag) {
        getCache(CACHE_NAME_EMAIL_NOTIFICATION).put(key, enabledFlag)
    }

    String getModuleName(String key) {
        getCache(CACHE_NAME_EMAIL_NOTIFICATION_MODULES).get(key)
    }

    void prepareRptFieldIndexCache(){
        Sql sql = new Sql(dataSource)
        try {
            sql.rows("SELECT ARGUS_NAME,RF_INFO_IDX from rpt_field_info").collect {
                getCache(CACHE_NAME_RPT_FIELD_INDEX).put(it.ARGUS_NAME, it.RF_INFO_IDX)
            }
        } finally {
            sql.close()
        }
    }

    String getRptFieldIndexCache(String rptField){
        "${rptField}_${getCache(CACHE_NAME_RPT_FIELD_INDEX).get(rptField)}"
    }

    Map getAllUsers() {
        getCache(CACHE_NAME_PV_USER_ID)
    }

    Map getAllGroups() {
        getCache(CACHE_NAME_PV_GROUP)
    }

    void saveRorCache(Boolean isRor) {
        getCache(CACHE_NAME_ISROR).put('isRor', isRor)
    }

    void savePreCheckCache(String preCheckValue) {
        getCache(CACHE_DEFAULT_PRECHECK).put('defaultPrecheck', preCheckValue)
    }

    String getPreCheckCache() {
        getCache(CACHE_DEFAULT_PRECHECK).get('defaultPrecheck')
    }

    Boolean getRorCache() {
        getCache(CACHE_NAME_ISROR).get('isRor')
    }

    void setUnassignedProductsCache(String hierarchy) {
        getCache(CACHE_NAME_UNASSIGNED_PRODUCTS).put(hierarchy, true)
    }

    Boolean getUnassignedProductsCache(String hierarchy) {
        getCache(CACHE_NAME_UNASSIGNED_PRODUCTS).get(hierarchy)
    }

    void clearUnassignedProductsCache(String hierarchy) {
        getCache(CACHE_NAME_UNASSIGNED_PRODUCTS).remove(hierarchy)
    }

    def prepareDerivedCountry() {
        List<String> derivedCounties = []
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.rows("select distinct country_group_id derived_country from vw_lco_country where lang_id = '1' order by country_group_id").collect {
                derivedCounties << it.derived_country
            }
        } finally {
            sql.close()
        }
        derivedCounties
    }
    List getDmvData(String dataMiningVariable,String searchKey,String selectedDatasource) {
        List miningVariableMap=[]
        List miningVariableMap1=[]
        if(selectedDatasource.equals("pva")){
            miningVariableMap = getMiningVariables("pva")?.collect{it.value}
        } else if(selectedDatasource.equals("faers")){
            miningVariableMap = getMiningVariables("faers")?.collect{it.value}
        }
        miningVariableMap.each {
            if(dataMiningVariable.equals(it.use_case)){
                miningVariableMap1.add(it.data)
                return
            }
        }
        List newList=[]
        if(!miningVariableMap1.isEmpty())
        {
            miningVariableMap1?.get(0)?.each {
                if(it.toString().toLowerCase().contains(searchKey.toLowerCase())){
                    newList.add(it)
                }
            }
        }
        return newList
    }
    List getDmvDataList( Sql sql,String useCase,boolean autocomplete) {
        log.info("Data mining variable value fetching for : "+useCase)
        Map tableInfoMap=new HashMap()
        List result=[]
        try {
            sql.rows("SELECT DECODE_TABLE,DECODE_COLUMN FROM pvs_batch_signal_constants_dsp WHERE use_case ='"+useCase+"'").collect {
                tableInfoMap.put(it.DECODE_TABLE,it.DECODE_COLUMN)
            }
            String tableName=tableInfoMap.keySet().getAt(0)
            String columnName=tableInfoMap.values().getAt(0)
            if(tableName)
            {
                String  query="SELECT "+columnName+" from "+tableName+" order by "+columnName+" asc"
                sql.rows(query).collect{
                    result.add(it[columnName])
                }
            }
        }catch(Exception ex){
            ex.printStackTrace()
        } finally {
            sql.close()
        }
        return result
    }

    def prepareMiningVariables() {
        Sql sql = null
        Sql sql_faers = null
        try {
            sql = new Sql(dataSource_pva)
            Map<String, String> idLabelMap = new LinkedHashMap<>()
            Map<String, String> meddraMap = new LinkedHashMap<>()
            sql.rows(SignalQueryHelper.find_meddra_field_sql()).each {
                meddraMap.put(String.valueOf(it.key_id), ['label': it.ui_label, 'use_case': it.use_case])
            }
            sql.rows(SignalQueryHelper.batch_variables_sql()).each {
                List data=[]
                boolean isMeddra=false;
                if(meddraMap.containsKey(String.valueOf(it.key_id))){
                    isMeddra=true;
                }
                idLabelMap.put(String.valueOf(it.key_id), ['label': it.ui_label, 'use_case': it.use_case,"isMeddra":isMeddra,"isOob":it.paste_import_option==1?false:true,'isautocomplete': it.isautocomplete==1?true:false,'data':data,'dic_level': it.dic_level,'dic_type': it.dic_type,'validatable':it.validatable==1?true:false, 'table_name': it.DECODE_TABLE, 'column_name': it.DECODE_COLUMN])
            }
            getCache(CACHE_DATA_MINING_VARIABLES).put('pva', idLabelMap)
            if (Holders.config.signal.faers.enabled) {
                sql_faers = new Sql(dataSource_faers)
                idLabelMap = new LinkedHashMap<>()
                meddraMap= new LinkedHashMap<>()
                sql_faers.rows(SignalQueryHelper.find_meddra_field_sql()).each {
                    meddraMap.put(String.valueOf(it.key_id), ['label': it.ui_label, 'use_case': it.use_case])
                }
                sql_faers.rows(SignalQueryHelper.batch_variables_sql()).each {
                    List data=[]
                    boolean isMeddra=false;
                    if(meddraMap.containsKey(String.valueOf(it.key_id))){
                        isMeddra=true;
                    }
                    idLabelMap.put(String.valueOf(it.key_id), ['label': it.ui_label, 'use_case': it.use_case,"isMeddra":isMeddra,"isOob":it.paste_import_option==1?false:true,'isautocomplete': it.isautocomplete==1?true:false,'data':data,'dic_level': it.dic_level,'dic_type': it.dic_type,'validatable':it.validatable==1?true:false, 'table_name': it.DECODE_TABLE, 'column_name': it.DECODE_COLUMN])
                }
                getCache(CACHE_DATA_MINING_VARIABLES).put('faers', idLabelMap)
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e)
        } finally {
            sql?.close()
            sql_faers?.close()
        }
    }

    def getMiningVariables(String dataSource){
        getCache(CACHE_DATA_MINING_VARIABLES).get(dataSource)
    }

    def setEvdasDrillDownData(List<Map> data = [], String sessionId){
        getCache(CACHE_EVDAS_DRILL_DOWN_DATA).put('drillDownData' + sessionId, data)
    }

    List<Map> getEvdasDrillDownData(String sessionId){
        getCache(CACHE_EVDAS_DRILL_DOWN_DATA).get('drillDownData' + sessionId) as List<Map>
    }


    def setPartialAlertCache(Long masterId, Boolean isPartial){
        getCache(CACHE_PARTIAL_ALERT).put(masterId,isPartial)
    }

    Boolean getPartialAlertCache(Long masterId){
        getCache(CACHE_PARTIAL_ALERT).get(masterId) as Boolean
    }
    def setSignalAndConfigurationId(String signalId,Long configurationId){
        getCache(SIGNAL_CONFIGURATION_MAP).put(configurationId, signalId)
    }
    def getSignalId(def configurationId) {
        def  signalId = getCache(SIGNAL_CONFIGURATION_MAP).get(configurationId)
        signalId
    }
    def clearSignalIdFromCache(def configurationId){
        getCache(SIGNAL_CONFIGURATION_MAP).remove(configurationId)
    }

    def clearPartialAlertCache(Long masterExecId){
        getCache(CACHE_PARTIAL_ALERT).remove(masterExecId)
    }

    def clearEvdasDrillDownData(String sessionId){
        getCache(CACHE_EVDAS_DRILL_DOWN_DATA).remove('drillDownData' + sessionId)
    }

    def setEvdasAlertId(Long alertId = null, String sessionId){
        getCache(CACHE_EVDAS_ALERT_Id).put('alertId' + sessionId, alertId)
    }

    Long getEvdasAlertId(String sessionId){
        getCache(CACHE_EVDAS_ALERT_Id).get('alertId' + sessionId) as Long
    }

    def clearEvdasAlertId(String sessionId){
        getCache(CACHE_EVDAS_ALERT_Id).remove('alertId' + sessionId)
    }

    void setDefaultDisp(String type, Long dispId){
        getCache(CACHE_DEFAULT_DISP).put(type, dispId)
    }

    void removeDefaultDisp(String type){
        getCache(CACHE_DEFAULT_DISP).remove(type)
    }
    Disposition getDefaultDisp(String type){
        Disposition disposition = getDispositionByValue(getCache(CACHE_DEFAULT_DISP).get(type) as Long)
        disposition
    }

    // saving rpt to Ui label mapping for safety, call this query on reload on case series
    def prepareUiLabelCacheForSafety() {
        Sql sql = new Sql(dataSource_pva)
        Map<String, String> idLabelMap = new LinkedHashMap<>()
        Map <String,String> rptFieldMap = new LinkedHashMap<>()
        try {
            String query = SignalQueryHelper.rpt_to_ui_label_table()
            sql.rows(query).each {
                String label = it.CUSTOM_UI_LABEL?:it.UI_LABEL
                String uniqueFieldId = it.UNIQUE_FIELD_ID
                idLabelMap.put(String.valueOf(it.RPT_FIELD), String.valueOf(label))
                rptFieldMap.put(String.valueOf(uniqueFieldId), String.valueOf(it.RPT_FIELD))
            }
            getCache(CACHE_SAFETY_MART_UNIQUE_IDENTIFIER).put('safety',rptFieldMap)
            getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL).put('safety', idLabelMap)
        } finally {
            sql.close()
        }
    }

    def getRptToUiLabelMapForSafety() {
        getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL).get('safety')
    }

    def getCacheUniqueFieldId(){
        getCache(CACHE_SAFETY_MART_UNIQUE_IDENTIFIER).get('safety')
    }

    def prepareRptToUiLabelInfoPvr() {
        // rpt field to Ui label map for En
        Map rptFieldToUiLabelMapEn = [:]

        // rpt field to Ui label map for Ja
        Map rptFieldToUiLabelMapJa = [:]

        // rpt field to SQL query Map which is used for fetching value
        Map rptFieldToSqlQueryMap = [:]


        // currently fetching all these values from Safety Mart
        Sql sql = new Sql(dataSource_pva)

        try {
            String query = SignalQueryHelper.rpt_to_ui_label_table_pvr()
            sql.rows(query).each {
                String text = it.TEXT
                String loc = it.LOC
                String code = it.CODE
                if(code.contains("app.reportField.") && !code.contains(".label")){
                    // contains UI label
                    if(loc == 'en'){
                        // set english label
                        rptFieldToUiLabelMapEn.put(code.substring(16), text)
                    }else if(loc == 'ja'){
                        // set japanese title, there are 9 fields for which japanese is blank but english has value
                        rptFieldToUiLabelMapJa.put(code.substring(16), text)
                    }else{
                        // loc is *, update values if they are null for both eng and jap
                        if(!rptFieldToUiLabelMapEn.containsKey(code.substring(16))){
                            rptFieldToUiLabelMapEn.put(code.substring(16), text)
                        }
                        if(!rptFieldToUiLabelMapJa.containsKey(code.substring(16))){
                            rptFieldToUiLabelMapJa.put(code.substring(16), text)
                        }
                    }
                }else if(code.contains("app.dropdown.")){
                    // contains dropdown SQL
                    rptFieldToSqlQueryMap.put(code.substring(13), text)
                }
            }
            println rptFieldToUiLabelMapEn.get('masterPbrerBucket')
            getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL).put('en', rptFieldToUiLabelMapEn)
            getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL).put('ja', rptFieldToUiLabelMapJa)
            getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL).put('dropdown', rptFieldToSqlQueryMap)
        } finally {
            sql.close()
        }
    }

    String getRptToUiLabelInfoPvrEn(String field){
        getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL)?.get('en')?.get(field)
    }

    String getRptToUiLabelInfoPvrJa(String field){
        getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL)?.get('ja')?.get(field)
    }

    String getRptToUiLabelInfoPvrSql(String field){
        getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL)?.get('dropdown')?.get(field)
    }

    void setAlertFields(String type, List<AlertField> alertFields) {
        getCache(ALERT_FIELD).put(type, alertFields)
    }
    void setOnDemandAlertFields(String dataSource, List<AlertField> alertFields) {
        getCache(CACHE_AGG_ON_DEMAND_COLUMNS).put(dataSource, alertFields)
    }
    void setJaderAlertFields(String dataSource, List<AlertField> alertFields) {
        getCache(CACHE_AGG_JADER_COLUMNS).put(dataSource, alertFields)
    }

    void removeAlertFields(String type) {
        getCache(ALERT_FIELD).remove(type)
    }

    def getAlertFields(String type) {
        getCache(ALERT_FIELD).get(type)
    }
    def getOnDemandAlertFields(String dataSource){
        getCache(CACHE_AGG_ON_DEMAND_COLUMNS).get(dataSource)
    }
    def getJaderAlertFields(String dataSource){
        getCache(CACHE_AGG_JADER_COLUMNS).get(dataSource)
    }

    def prepareCommonTagCache(){
        Sql sql = null
        List <Map> tags = []
        List<Map> tagsNested = []
        Map<String, List<Map>> childMap = [:]
        try {
            sql = new Sql(dataSource_pva)
            sql.eachRow("select * from code_value where code_list_id = ${Holders.config.mart.codeValue.tags.value} and is_master_data=1 and is_deleted =0 ORDER BY UPPER(value) ASC" , []) { row ->
                Map rowData = [id : row.id , text : row.value   , parentId : row.parent_id, display : row.display]
                tags << rowData
                if (row.parent_id == null) {
                    tagsNested << [id: row.id, name: row.value, display: row.display, subcategories: []]
                } else {
                    Map child = [id: row.id, name: row.value, display: row.display]
                    if (childMap.get(row.parent_id)) {
                        childMap.put(row.parent_id, (childMap.get(row.parent_id) + child))
                    } else {
                        childMap.put(row.parent_id, [child])
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Error on fetching Tags ")
        } finally {
            try {
                sql?.close()
            } catch (Throwable notableToHandle) {
                log.error("Failed to close the Sql", notableToHandle)
            }
        }
        tagsNested.each {
            it.put('subcategories', childMap.get(it.id) ?: [])
        }
        getCache(CACHE_COMMON_TAG).put('category', tags.sort({ it?.text.toUpperCase() }))
        getCache(CACHE_COMMON_TAG).put('categoryJson', tagsNested.sort({ it?.name.toUpperCase() }))
    }
    def getCommonTagCache() {
        getCache(CACHE_COMMON_TAG).get('category')
    }

    def getCommonTagCacheJson() {
        getCache(CACHE_COMMON_TAG).get('categoryJson')
    }
      
    boolean masterUploadRunningStatus(Boolean updatedValue) {
        if (updatedValue != null) {
            getCache(MASTER_UPLOAD_RUNNING).put('isMasterCurrentlyUploading', updatedValue)
            return true
        } else {
            return getCache(MASTER_UPLOAD_RUNNING).get('isMasterCurrentlyUploading')
        }

    }

    Boolean updateUiLabelCacheForSafety(List data) {
        Boolean cacheUpdateStatus = true
        Sql sql = new Sql(dataSource_pva)
        if (data) {
            try {
                List filteredData = data.findAll {
                    it["langId"] == "en"
                }// only default lang as multilingual not yet implemented in pvs
                if (filteredData) {
                    Map<String, String> idLabelMap = new LinkedHashMap<>()
                    String query = SignalQueryHelper.rpt_to_ui_label_table_with_params(filteredData*.fieldId.join("', '"))
                    sql.rows(query).each {
                        String label = it.CUSTOM_UI_LABEL ?: it.UI_LABEL
                        idLabelMap.put(String.valueOf(it.RPT_FIELD), String.valueOf(label))
                    }
                    def map = getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL).get('safety')
                    idLabelMap.each { key, value ->
                        map.put(key, value)
                    }
                    getCache(CACHE_SAFETY_MART_RPT_TO_UI_LABEL).put('safety', map)
                } else {
                    log.info("No data found for lang id = * ")
                }
            } catch (Exception exception) {
                cacheUpdateStatus = false
                log.error("An error Occurred while updating cache. ")
                exception.printStackTrace()
            } finally {
                sql.close()
                return cacheUpdateStatus
            }
        } else {
            return true
        }
    }

    void prepareUserBlindingCache(List blindedUserList){
        getListCache(BLINDED_USER_CACHE).addAll(blindedUserList)

    }

    void prepareUserGroupBlindingCache(List blindedUserGroupList){
        getListCache(BLINDED_USER_GROUP_CACHE).addAll(blindedUserGroupList)
    }

    List getUserBlindingCache(){
        getListCache(BLINDED_USER_CACHE)
    }

    List getUserGroupBlindingCache(){
        getListCache(BLINDED_USER_GROUP_CACHE)
    }

    void clearUserBlindingCache(){
        getListCache(BLINDED_USER_CACHE).clear()
    }

    /**
     * @ This method prepared all required configuration for Adhoc Alert.
     * Screen name :- Adhoc Alert Configuration, Review, Details, Field Selection.
     */
    void prepareAdhocConfiguration() {
        try{
            // Assuming the JSON string is stored in Holders.config.adhoc.alert.configuration.dynamic.fields
            List<Map> parsedList = Holders.config.adhoc.alert.configuration.dynamic.fields as List<Map>

            // Parse the JSON string into List<Map>
            List<Map> adhocAlertConfigurationAttributes = []

            if (!parsedList.isEmpty()) {
                try {
                    //List<Map> parsedList = new JsonSlurper().parseText(jsonString) as List<Map>
                    adhocAlertConfigurationAttributes = parsedList.collect { new HashMap<>(it) }
                } catch (Exception e) {
                    log.error("Error parsing JSON string to List<Map>: ${e.message}")
                }
            } else {
                log.warn("Adhoc alert configuration JSON string is empty or null")
            }

            List<Map> adhocAlertAttribWithMandatoryCheck = adhocAlertAttribMandatoryCheck(adhocAlertConfigurationAttributes)

            prepareAdhocAlertConfigurationCache(adhocAlertAttribWithMandatoryCheck)

            prepareAdhocAlertDetailCache(adhocAlertAttribWithMandatoryCheck)

            prepareAdhocAlertReviewList(adhocAlertAttribWithMandatoryCheck)

            Map<String, List<Map>> groupedConfiguration = prepareAdhocAlertGroupedConfigurationCache(adhocAlertAttribWithMandatoryCheck)

            prepareAdhocAlertSectionLabelMap(groupedConfiguration)

        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    private List<Map> adhocAlertAttribMandatoryCheck(List<Map> adhocAlertConfigurationAttributes) {

        List<Map> adhocAlertAttribWithMandatoryCheck = adhocAlertConfigurationAttributes
                .collect {
            // If the field is in the alwaysMandatoryFields list, force 'mandatory' and 'enabled' to true
            if (Constants.AdhocAlert.alwaysMandatoryFields.contains(it.name)) {
                [name                    : it.name, label: MiscUtil.sanitizeString(it.label), enabled: true, mandatory: true, orderSequence: it.orderSequence, section: it.section,
                 orderSequence_detailPage: it.orderSequence_detailPage, section_label: MiscUtil.sanitizeString(it.section_label), name_detailPage: it.name_detailPage, review: it.review, name_reviewPage: it.name_reviewPage]
            } else {
                [name                    : it.name, label: MiscUtil.sanitizeString(it.label), enabled: it.enabled, mandatory: it.mandatory, orderSequence: it.orderSequence, section: it.section,
                 orderSequence_detailPage: it.orderSequence_detailPage, section_label: MiscUtil.sanitizeString(it.section_label), name_detailPage: it.name_detailPage, review: it.review, name_reviewPage: it.name_reviewPage]
            }
        }
        adhocAlertAttribWithMandatoryCheck
    }

    private void prepareAdhocAlertConfigurationCache(List<Map> adhocAlertAttribWithMandatoryCheck) {
        // First List: Filter by `enabled == true` and group by `section`, include `name`, `label`, `enabled`, `mandatory`, and `orderSequence`
        List<Map<String, Object>> adhocAlertConfigurationList = adhocAlertAttribWithMandatoryCheck
                .collect {
            [name: it.name, label: it.label, enabled: it.enabled, mandatory: it.mandatory, orderSequence: it.orderSequence, section: it.section, section_label: it.section_label]
        }
        .groupBy { it.section }
                .collectMany { section, values -> values }
                .sort { it.orderSequence }
        getListCache(ADHOC_ALERT_CONFIGURATION_CACHE).clear()
        getListCache(ADHOC_ALERT_CONFIGURATION_CACHE).addAll(adhocAlertConfigurationList)
    }

    private void prepareAdhocAlertDetailCache(List<Map> adhocAlertAttribWithMandatoryCheck) {
        // Second List: Filter by `enabled == true`, include `name`, `label_detailPage` as `label`, `orderSequence_detailPage` as `orderSequence`
        List<Map<String, Object>> adhocAlertDetailsList = adhocAlertAttribWithMandatoryCheck
                .findAll { it.enabled == true && it.orderSequence_detailPage }
                .collect {
            [name: it.name_detailPage ?: it.name, label: it.label, orderSequence: it.orderSequence_detailPage, section_label: it.section_label]
        }
                .sort { it.orderSequence }
        List<List<Object>> composeAdhocAlertDetailList = composeAdhocAlertDetailList(adhocAlertDetailsList)
        getListCache(ADHOC_ALERT_DETAILS_CACHE).clear()
        getListCache(ADHOC_ALERT_DETAILS_CACHE).addAll(composeAdhocAlertDetailList)
    }

    private void prepareAdhocAlertReviewList(List<Map> adhocAlertAttribWithMandatoryCheck) {
        // Third List: Filter by `enabled == true` and `label_reviewPage`, include `name`, `label_reviewPage` as `label`
        List<Map<String, Object>> reviewList = adhocAlertAttribWithMandatoryCheck
                .findAll { it.enabled == true && it.review == true }
                .collect { [name: it.name_reviewPage ?: it.name, label: it.label] }

        List<Map<String, Object>> adhocAlertFormattedReviewList = formatterReviewList(reviewList)
        getListCache(ADHOC_ALERT_REVIEW_CACHE).clear()
        getListCache(ADHOC_ALERT_REVIEW_CACHE).addAll(adhocAlertFormattedReviewList)
    }

    private Map<String, List<Map>> prepareAdhocAlertGroupedConfigurationCache(List<Map> adhocAlertAttribWithMandatoryCheck) {
        //Fourth List: Group by section
        Map<String, List<Map>> groupedConfiguration = adhocAlertAttribWithMandatoryCheck.groupBy { it.section }.collectEntries { section, attributes ->
            // Sort attributes by 'orderSequence' for each section
            [(section): attributes.sort { it.orderSequence }]
        }

        getCache(ADHOC_ALERT_GROUP_CONFIGURATION_CACHE).clear()
        getCache(ADHOC_ALERT_GROUP_CONFIGURATION_CACHE).put(Constants.AdhocAlert.GROUPED_CONFIGURATION, groupedConfiguration)
        groupedConfiguration
    }

    private void prepareAdhocAlertSectionLabelMap(Map<String, List<Map>> groupedConfiguration) {
        //Fifth List: Group by section label name
        Map<String, String> sectionLabelMap = groupedConfiguration.collectEntries { section, items ->
            [(section): items[0]?.section_label]
        }
        getCache(ADHOC_ALERT_SECTION_LABEL_CACHE).clear()
        getCache(ADHOC_ALERT_SECTION_LABEL_CACHE).put(Constants.AdhocAlert.SECTION_LABEL_MAP, sectionLabelMap)
    }

    List<List<Object>> composeAdhocAlertDetailList(List<Map> detailPageList) {

        List<List<Object>> attributes = []
        List<List<Object>> groupedAttributes = [] // For grouped attributes (width != 12)
        List<List<Object>> standaloneAttributes = [] // For standalone attributes (width == 12)

        // Split the list into sections of 6 elements each
        List<List<Map<String, Object>>> sections = detailPageList.collate(6)

        sections.each { section ->
            section.each { item ->
                String width = getWidth(item.name)
                if (width == '12') {
                    // Wrap each standalone attribute inside a list
                    standaloneAttributes.add([[item.label, item.name, width]])
                } else {
                    // Add grouped attributes directly
                    if (item.name != 'matchingAlerts' && item.name != 'currentDisposition' && item.name != 'disposition') {
                        groupedAttributes.add([item.label, item.name, width])
                    }
                }
            }
        }

        // Combine grouped attributes into blocks of 6 elements each
        attributes.addAll(groupedAttributes.collate(6))

        // Add standalone attributes as separate lists
        attributes.addAll(standaloneAttributes)
        return attributes
    }


    List<Map<String, Object>> formatterReviewList(List<Map<String, Object>> thirdList) {
        // Transform `thirdList` into the required format by setting `containerView` conditionally
        List<Map<String, Object>> formattedList = thirdList.collect { entry ->
            [
                    "containerView": entry["name"] == "initDataSrc" ? 3 : 1,  // Set containerView to 3 for "initDataSrc", else 1
                    "label"        : entry["label"],                         // Use the label value from `thirdList`
                    "name"         : entry["name"]                           // Use the name value from `thirdList`
            ]
        }
        return formattedList
    }

    List getAdhocConfigurationCache() {
        return getListCache(ADHOC_ALERT_CONFIGURATION_CACHE)
    }

    List getAdhocDetailsCache() {
        return getListCache(ADHOC_ALERT_DETAILS_CACHE)
    }

    List getAdhocReviewCache() {
        return getListCache(ADHOC_ALERT_REVIEW_CACHE)
    }

    Map<String, List<Map>> getAdhocGroupConfigurationCache(String key) {
        return getCache(ADHOC_ALERT_GROUP_CONFIGURATION_CACHE).get(key)
    }

    Map<String, String> getAdhocSectionLabelMap(String key) {
        return getCache(ADHOC_ALERT_SECTION_LABEL_CACHE).get(key)
    }

    void prepareAdhocAlertDynamicDropDown() {
        // Fetch dropdown values from config.groovy
        Map<String, List<Map<String, String>>> dropdownValues = Holders.config.adhoc.alert.dynamic.dropdownValues

        // Filter values key-wise where 'disabled' == 'No'
        Map<String, List<String>> filteredByKeys = dropdownValues.collectEntries { key, values ->
            [(key): values.findAll { it.disabled == "No" }
                    .collect { it.value }
                    .sort { a, b -> a.toUpperCase() <=> b.toUpperCase() }]
        }
        getCache(ADHOC_ALERT_DYNAMIC_DROPDOWN_CACHE).clear()
        getCache(ADHOC_ALERT_DYNAMIC_DROPDOWN_CACHE).put(Constants.AdhocAlert.DROPDOWN_VALUES_BY_KEYS, filteredByKeys)
    }

    Map<String, List<String>> getAdhocAlertDynamicDropDownCache(String key) {
        return getCache(ADHOC_ALERT_DYNAMIC_DROPDOWN_CACHE).get(key)
    }

    String getWidth(String name) {
        switch (name) {
            case ['notes', 'description', 'commentSignalStatus']:
                return '12'
            case 'actionTaken':
                return '4'
            default:
                return '2' // Default width or dynamic logic here
        }
    }

}
