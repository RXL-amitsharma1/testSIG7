package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.ActivityDTO
import com.rxlogix.dto.AuditTrailDTO
import com.rxlogix.dto.CumThreadInfoDTO
import com.rxlogix.dto.LastReviewDurationDTO
import com.rxlogix.dto.ScaLastReviewDurationDTO
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.BusinessConfiguration
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.GlobalArticle
import com.rxlogix.signal.GlobalCase
import com.rxlogix.signal.GlobalProductEvent
import com.rxlogix.signal.EvdasHistory
import com.rxlogix.signal.ProductEventHistory
import com.rxlogix.signal.SingleCaseAlert
import groovy.transform.Synchronized
import org.hibernate.criterion.CriteriaSpecification

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import com.hazelcast.core.HazelcastInstance

/**
 * A singleton implementation for data carrier service. This service holds the data carrier maps which will be filled during the
 * course of alert executions and other flows.
 */
class DataObjectService {
    HazelcastInstance hazelcastInstance
    def grailsApplication

    static transactional = false

    //Carries the activity map.
    private Map<Long, ArrayList<ActivityDTO>> activityMap = new ConcurrentHashMap<Long, ArrayList<ActivityDTO>>()
    private Map<Long, ArrayList<AuditTrailDTO>> auditTrailMap = new ConcurrentHashMap<Long, ArrayList<AuditTrailDTO>>()

    //Carries the single case alert activity Map
    private Map<Long, CopyOnWriteArrayList<Activity>> scaActivityMap = new ConcurrentHashMap<Long, CopyOnWriteArrayList<Activity>>()

    //Carries the caseHistory map.
    private Map<Long, List<CaseHistory>> caseHistoryMap = new ConcurrentHashMap<Long, ArrayList<CaseHistory>>()
    private Map<Long, List<Long>> existingCaseHistoryMap = new ConcurrentHashMap<Long, CopyOnWriteArrayList<Long>>()

    private Map<Long, ArrayList<ActivityDTO>> evdasActivityMap = new ConcurrentHashMap<Long, ArrayList<ActivityDTO>>()

    //Carries the business config properties map.
    private Map<Long, ArrayList<Map>> businessConfigPropertiesMap = new ConcurrentHashMap<Long, ArrayList<Map>>()
    //Carries the evdas business config properties map.
    private Map<Long, ArrayList<Map>> evdasBusinessConfigPropertiesMap = new ConcurrentHashMap<Long, ArrayList<Map>>()

    //Carries the product event history activity data.
    private Map<Long, List<Map>> productEventMap = new ConcurrentHashMap<Long, List<Map>>()

    //Carries the stats related data
    private Map<Long, Map<String, Map<String, Map<String, String>>>> statsDataMap =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()

    private Map<Long, Map<String, Map<String, Map<String, String>>>> dssScoreDataMap =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()


    private Map<Long, CopyOnWriteArrayList<Map>> signalAlertMap = new ConcurrentHashMap()
    private Map<Long, CopyOnWriteArrayList<Map>> aggSignalAlertMap = new ConcurrentHashMap()
    private Map<Long, CopyOnWriteArrayList<Map>> signalAlertMapEvdasAlert = new ConcurrentHashMap()
    private Map<Long, CopyOnWriteArrayList<Map>> aggSignalAlertMapForSignalState = new ConcurrentHashMap()
    private Map<Long, ConcurrentHashMap<Long,CopyOnWriteArrayList<Map>>> alertIdSignalListMap = new ConcurrentHashMap()
    private Map<Long, ConcurrentHashMap<Long,CopyOnWriteArrayList<Map>>> alertIdSignalListMapEvdasAlert = new ConcurrentHashMap()


    //Carries the prr/ror related data
    private Map<Long, Map<String, Map<String, Map<String, String>>>> probDataMap =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()
    private Map<Long, Map<String, Map<String, Map<String, String>>>> rorProbDataMap =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()
    private Map<Long, Map<String, Map<String, Map<String, String>>>> prrSubGroupDataMap =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()
    private Map<Long, Map<String, Map<String, Map<String, String>>>> rorSubGroupDataMap =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()


    private Map<Long, List<EmailLog>> emailLogMap = new ConcurrentHashMap<Long, List<EmailLog>>()
    private Map<Long, List<InboxLog>> inboxLogMap = new ConcurrentHashMap<Long, List<InboxLog>>()

    private Map<String, String> labelIdMap = new HashMap<String, String>()
    private Map<String, String> idLabelMap = new HashMap<String, String>()
    private Map<Long,ConcurrentHashMap<Long,Integer>> caseIdListMap = new ConcurrentHashMap<>()
    private Map<Long,ArrayList<String>> pEComListMap = new ConcurrentHashMap<>()
    private Map<Long,String> tagsMap = new ConcurrentHashMap<>()
    private Map<String, ArrayList<String>> tagsList = new ConcurrentHashMap<>()
    private Map<Long,ConcurrentHashMap<Long,List>> caseTagMap = new ConcurrentHashMap<>()

    private Map<String ,Map<String, Integer>> keyIdMap = new ConcurrentHashMap<>()
    private Map<Long, Map<String, Map<String, Map<String, String>>>> statsDataMapSubgrouping =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()
    private Map<Long, Map<String, Map<String, Map<String, String>>>> ebgmStatsDataMapSubgrouping =
            new ConcurrentHashMap<Long, Map<String, Map<String, Map<String, String>>>>()

    private Map<String, List<Long>> exConfigIds = new ConcurrentHashMap<>()
    private Map<Long, Date> currentEndDateMap = new ConcurrentHashMap<>()
    private Map<String, Boolean> prevAssignedTo = new ConcurrentHashMap<String, Boolean>()
    private Map<String, Boolean> dataSourceMap = new HashMap<String, Boolean>()
    private Map<Long, String> dssMetaDataMap = new ConcurrentHashMap<Long, String>()
    private Map<String, Long> masterDbDoneMap = new ConcurrentHashMap<String, Long>()


    /**
     * The field values kept in the data object service so that at any given point of time the product view name can be fetched.
     * It can be kept in the Hazlecase cache as well to avoid any inconsistancy in the name but there will be no impact if we
     * keep things in the singleton service. These fields are related to product dictionary.
     */
    private String productViewName = Constants.Commons.BLANK_STRING
    private Map dictionaryMap = [:]
    private List dictionaryLevelsList = []
    private String dictionaryProductLevel = "1"
    private Map<Long, Disposition> defaultDispostionMap = new HashMap<Long, Disposition>()
    private Map<String, List<BusinessConfiguration>> enabledBusinessConfMap = new ConcurrentHashMap<>()
    private Map<Long, Date> firstVersionExecMap = new ConcurrentHashMap<>()
    private Map<Long, List<CaseHistory>> alertExistingCaseHistoryMap  = new ConcurrentHashMap<>()
    private Map<Long, List<ProductEventHistory>> alertExistingPEHistoryMap  = new ConcurrentHashMap<>()
    private Map<Long, List<EvdasHistory>> alertExistingEvdasHistoryMap  = new ConcurrentHashMap<>()
    private Map<Long , List<GlobalCase>>alertGlobalCaseMap = new ConcurrentHashMap<>()
    private Map<Long , List<GlobalProductEvent>>alertGlobalProductEventMap = new ConcurrentHashMap<>()
    private Map<Long , List<GlobalArticle>>alertGlobalArticleMap = new ConcurrentHashMap<>()
    private Map<Long , List<LastReviewDurationDTO>> lastReviewDurationMap = new ConcurrentHashMap<>()
    private Map<Long, List<BusinessConfiguration>> businessConfGroupMap = new ConcurrentHashMap<>()
    private Map<Long , List<Map>>alertGlobalProductAlertMap = new ConcurrentHashMap<>()
    private Map<Long , List<Map>>alertGlobalCaseAlertMap = new ConcurrentHashMap<>()
    private Map<String , String>socAbbreviationMap = new HashMap<String , String>()
    private Map<Long, Boolean> cummCaseSeriesGeneratedMap = new ConcurrentHashMap<>()
    private Map<Long, CumThreadInfoDTO> currentlyRunningCumulativeThread = new ConcurrentHashMap<>()

    private Map<Long, List<Map>> bulkCategoriesMap = new ConcurrentHashMap<Long, List<Map>>()
    private Map<String, Long> pecMap = new HashMap<>()
    private Map<String, Long> caseMap = new HashMap<>()
    public static String HAZELCAST_BULK_CATEGORIES_MAP = "hazelcastBulkCategoriesMap"
    /**
     * Sets the stats data map of maps based on the uniquely identified executed config peCom.
     * @param executedConfigId
     * @param productId
     * @param eventCode
     * @param valueMap
     */
    void setStatsDataMap(Long executedConfigId, String productId, String eventCode, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = statsDataMap.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productId)
        if(!eventMap) {
           eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventCode, valueMap)
        productEventStatsMap.put(productId, eventMap)
        this.statsDataMap.put(executedConfigId, productEventStatsMap)
    }

    void setDssScoreDataMap(Long executedConfigId, String productName, String eventName, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = dssScoreDataMap.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productName)
        if(!eventMap) {
            eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventName, valueMap)
        productEventStatsMap.put(productName, eventMap)
        this.dssScoreDataMap.put(executedConfigId, productEventStatsMap)
    }

    void setProbDataMap(Long executedConfigId, String productId, String eventCode, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = probDataMap.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productId)
        if(!eventMap) {
            eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventCode, valueMap)
        productEventStatsMap.put(productId, eventMap)
        this.probDataMap.put(executedConfigId, productEventStatsMap)
    }
    void setRorProbDataMap(Long executedConfigId, String productId, String eventCode, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = rorProbDataMap.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productId)
        if(!eventMap) {
            eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventCode, valueMap)
        productEventStatsMap.put(productId, eventMap)
        this.rorProbDataMap.put(executedConfigId, productEventStatsMap)
    }
    void setPrrSubGroupDataMap(Long executedConfigId, String productId, String eventCode, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = prrSubGroupDataMap.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productId)
        if(!eventMap) {
            eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventCode, valueMap)
        productEventStatsMap.put(productId, eventMap)
        this.prrSubGroupDataMap.put(executedConfigId, productEventStatsMap)
    }
    void setRorSubGroupDataMap(Long executedConfigId, String productId, String eventCode, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = rorSubGroupDataMap.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productId)
        if(!eventMap) {
            eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventCode, valueMap)
        productEventStatsMap.put(productId, eventMap)
        this.rorSubGroupDataMap.put(executedConfigId, productEventStatsMap)
    }

    void setStatsDataMapSubgrouping(Long executedConfigId, String productId, String eventCode, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = statsDataMapSubgrouping.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productId)
        if(!eventMap) {
            eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventCode, valueMap)
        productEventStatsMap.put(productId, eventMap)
        this.statsDataMapSubgrouping.put(executedConfigId, productEventStatsMap)
    }
    void setEbgmStatsDataMapSubgrouping(Long executedConfigId, String productId, String eventCode, Map<String, String> valueMap) {

        Map<String, Map<String, Map<String, String>>> productEventStatsMap = ebgmStatsDataMapSubgrouping.get(executedConfigId)
        if (!productEventStatsMap) {
            productEventStatsMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>()
        }
        Map<String, Map<String, String>> eventMap = productEventStatsMap.get(productId)
        if(!eventMap) {
            eventMap = new ConcurrentHashMap<String, Map<String, String>>()
        }
        eventMap.put(eventCode, valueMap)
        productEventStatsMap.put(productId, eventMap)
        this.ebgmStatsDataMapSubgrouping.put(executedConfigId, productEventStatsMap)
    }


    /**
     * Gets the stats map data based on the uniquely identified executed config peCom.
     * @param executedConfigId
     * @param productId
     * @param eventCode
     * @return
     */
    def getStatsDataMap(executedConfigId, productId, eventCode) {

        String productIdVal = String.valueOf(productId)
        String eventCodeVal = String.valueOf(eventCode)
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.statsDataMap.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productIdVal)
        def statsResults = eventMap?.get(eventCodeVal)
        statsResults
    }

    def getProbDataMap(executedConfigId, productId, eventCode) {

        String productIdVal = String.valueOf(productId)
        String eventCodeVal = String.valueOf(eventCode)
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.probDataMap.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productIdVal)
        def statsResults = eventMap?.get(eventCodeVal)
        statsResults
    }
    def getRorProbDataMap(executedConfigId, productId, eventCode) {

        String productIdVal = String.valueOf(productId)
        String eventCodeVal = String.valueOf(eventCode)
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.rorProbDataMap.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productIdVal)
        def statsResults = eventMap?.get(eventCodeVal)
        statsResults
    }
    def getPrrSubGroupDataMap(executedConfigId, productId, eventCode) {

        String productIdVal = String.valueOf(productId)
        String eventCodeVal = String.valueOf(eventCode)
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.prrSubGroupDataMap.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productIdVal)
        def statsResults = eventMap?.get(eventCodeVal)
        statsResults
    }
    def getRorSubGroupDataMap(executedConfigId, productId, eventCode) {

        String productIdVal = String.valueOf(productId)
        String eventCodeVal = String.valueOf(eventCode)
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.rorSubGroupDataMap.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productIdVal)
        def statsResults = eventMap?.get(eventCodeVal)
        statsResults
    }

    def getStatsDataMapSubgrouping(executedConfigId, productId, eventCode) {

        String productIdVal = String.valueOf(productId)
        String eventCodeVal = String.valueOf(eventCode)
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.statsDataMapSubgrouping.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productIdVal)
        def statsResults = eventMap?.get(eventCodeVal)
        statsResults
    }
    def getEbgmStatsDataMapSubgrouping(executedConfigId, productId, eventCode) {

        String productIdVal = String.valueOf(productId)
        String eventCodeVal = String.valueOf(eventCode)
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.ebgmStatsDataMapSubgrouping.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productIdVal)
        def statsResults = eventMap?.get(eventCodeVal)
        statsResults
    }

    def getDssScoresDataMap(executedConfigId, productName, eventName) {
        Map<String, Map<String, Map<String, String>>> productEventStatsMap = this.dssScoreDataMap.get(executedConfigId)
        Map<String, Map<String, String>> eventMap = productEventStatsMap?.get(productName)
        def statsResults = eventMap?.get(eventName)
        statsResults
    }

    /**
     * Clears up the stats data.
     * @param executedConfigId
     */
    void clearStatsDataMap(executedConfigId) {
        this.statsDataMap.remove(executedConfigId)
        this.probDataMap.remove(executedConfigId)
        this.rorProbDataMap.remove(executedConfigId)
        this.prrSubGroupDataMap.remove(executedConfigId)
        this.rorSubGroupDataMap.remove(executedConfigId)
        this.statsDataMapSubgrouping.remove(executedConfigId)
        this.ebgmStatsDataMapSubgrouping.remove(executedConfigId)
        this.dssScoreDataMap.remove(executedConfigId)
    }
    @Synchronized
    void setBusinessConfigPropertiesMap(Long id, Map businessConfigMap) {
        try {
            def mapList = this.businessConfigPropertiesMap.get(id)
            if(!mapList) {
                mapList = new ArrayList<Map>()
            }
            mapList.add(businessConfigMap)
            this.businessConfigPropertiesMap.put(id, mapList)
        } catch(Exception ex) {
            ex.printStackTrace()
        }
    }

    List<Map> getBusinessConfigPropertiesMapList(Long id) {
        return this.businessConfigPropertiesMap.get(id)
    }

    void clearBusinessConfigPropertiesMap(Long id) {
        this.businessConfigPropertiesMap.remove(id)
    }

    @Synchronized
    void setEvdasBusinessConfigPropertiesMap(Long id, Map businessConfigMap) {
        try {
            ArrayList mapList = this.evdasBusinessConfigPropertiesMap.get(id)
            if(!mapList) {
                mapList = new CopyOnWriteArrayList<Map>()
            }
            mapList.add(businessConfigMap)
            this.evdasBusinessConfigPropertiesMap.put(id, mapList)
        } catch(Exception ex) {
            ex.printStackTrace()
        }
    }

    List<Map> getEvdasBusinessConfigPropertiesMapList(Long id) {
        return this.evdasBusinessConfigPropertiesMap.get(id) as List
    }

    void clearEvdasBusinessConfigPropertiesMap(Long id) {
        this.evdasBusinessConfigPropertiesMap.remove(id)
    }

    /**
     * Sets the value in the activity map.
     * @param id
     * @param activityDTO
     */
    @Synchronized
    void setActivityToMap(Long id, ActivityDTO activityDTO) {
        ArrayList<ActivityDTO> list =  activityMap.get(id)
        if (!list) {
            list = []
        }
        list.add(activityDTO)
        activityMap.put(id, list)
    }

    /**
     * Fetches the ActivityDTO based on the peCom.
     * @param id
     * @return
     */
    List<ActivityDTO> getActivityDtoList(Long id) {
        return activityMap.get(id)
    }

    /**
     * Clears the activity map.
     * @param id
     */
    void clearActivityMap(Long id) {
        activityMap.remove(id)
    }

    @Synchronized
    void setBusinessRuleAuditTrail(Long id, AuditTrailDTO auditTrailDTO) {
        ArrayList<AuditTrailDTO> list =  auditTrailMap.get(id)
        if (!list) {
            list = []
        }
        list.add(auditTrailDTO)
        auditTrailMap.put(id, list)
    }
    List<AuditTrailDTO> getBusinessRuleAuditTrailList(Long id) {
        return auditTrailMap.get(id)
    }
    void clearAuditTrailMap(Long id) {
        auditTrailMap.remove(id)
    }

    /**
     * Sets the value in the activity map.
     * @param id
     * @param activityDTO
     */
    @Synchronized
    void setEvdasActivityToMap(Long id, ActivityDTO activityDTO) {
        ArrayList<ActivityDTO> list =  evdasActivityMap.get(id)
        if (!list) {
            list = []
        }
        list.add(activityDTO)
        evdasActivityMap.put(id, list)
    }

    /**
     * Fetches the ActivityDTO based on the peCom.
     * @param id
     * @return
     */
    List<ActivityDTO> getEvdasActivityDtoList(Long id) {
        return evdasActivityMap.get(id)
    }

    /**
     * Clears the activity map.
     * @param id
     */
    void clearEvdasActivityMap(Long id) {
        evdasActivityMap.remove(id)
    }

    //////////////////////////////////////////////////////////
    ///////////// Temporary Data List ////////////////////////
    //////////////////////////////////////////////////////////
    private List dataList = []
    void setDataList(data) {
        this.dataList = data
    }

    List getDataList() {
        this.dataList
    }

    //////////////////////////////////////////////////////////
    ///////////// Dictionary Values ////////////////////////
    //////////////////////////////////////////////////////////

    void prepareProductDictValues() {
        setProductViewName()
        setProductMapping()
    }

    void setProductViewName() {
        DictionaryMapping.withTransaction {
            DictionaryMapping dictionaryMapping =
                    DictionaryMapping."pva".findByIsProductAndLanguageValueAndViewType(true, "en", Constants.DictionaryFilterType.FILTER)
            this.productViewName = dictionaryMapping?.viewName
            this.dictionaryProductLevel = (dictionaryMapping.id)?.toString()
        }
    }

    String getProductViewName() {
        this.productViewName
    }

    def setProductMapping() {

        List<DictionaryMapping> dictionaryOtherMappingList = []

        DictionaryMapping.withTransaction {
            //TODO: We are putting the language locale value as en.
            dictionaryOtherMappingList = DictionaryMapping."pva".findAllByLanguageValueAndIdIsNotNull("en", [sort: 'viewName'])
        }

        dictionaryOtherMappingList.eachWithIndex { DictionaryMapping dictionaryMapping, Integer index ->

            String keyId = dictionaryMapping.viewName?.split("DISP_VIEW_")[1]

            Map mapData = ['productLinkView': dictionaryMapping.productLinkView, 'isProduct': dictionaryMapping.isProduct,
                           'fieldId'        : dictionaryMapping.id, 'label': dictionaryMapping.label, 'keyId': keyId,
                           'view'           : dictionaryMapping.viewName]
            this.dictionaryMap.put(index + 1, mapData)
            this.dictionaryLevelsList.add(dictionaryMapping.id)
        }
    }

    def getProductMapping() {
        this.dictionaryMap.sort { it.value.keyId }
    }

    def getDictionaryLevelsList() {
        this.dictionaryLevelsList
    }

    def getDictionaryProductLevel() {
        this.dictionaryProductLevel
    }

    /**
     * Sets the value in the activity map.
     * @param id
     * @param caseHistory
     */
    @Synchronized
    void setCaseHistoryToMap(Long id, CaseHistory caseHistory) {
        ArrayList<CaseHistory> list = caseHistoryMap.get(id)
        if (!list) {
            list = []
        }
        list.add(caseHistory)
        caseHistoryMap.put(id, list)
    }

    /**
     * Fetches the CaseHistory based on the peCom.
     * @param id
     * @return
     */
    List<CaseHistory> getCaseHistoryList(Long id) {
        return caseHistoryMap.get(id)
    }

    /**
     * Clears the caseHistory map.
     * @param id
     */
    void clearCaseHistoryMap(Long id) {
        caseHistoryMap.remove(id)
    }

    /**
     * Sets the value in the activity map.
     * @param id
     * @param caseHistory
     */
    void setExistingCaseHistoryToMap(Long id, Long caseHistoryId) {
        synchronized (existingCaseHistoryMap) {
            CopyOnWriteArrayList<Long> list = existingCaseHistoryMap.get(id)
            if (!list) {
                list = []
            }
            list.add(caseHistoryId)
            existingCaseHistoryMap.put(id, list)
        }
    }

    /**
     * Fetches the CaseHistory based on the peCom.
     * @param id
     * @return
     */
    List<Long> getExistingCaseHistoryList(Long id) {
        return existingCaseHistoryMap.get(id)
    }

    /**
     * Clears the caseHistory map.
     * @param id
     */
    void clearExistingCaseHistoryMap(Long id) {
        existingCaseHistoryMap.remove(id)
    }

    void setSCAActivityToMap(Long id, Activity activity) {
        synchronized (scaActivityMap) {
            CopyOnWriteArrayList<Activity> list = scaActivityMap.get(id)
            if (!list) {
                list = []
            }
            list.add(activity)
            scaActivityMap.put(id, list)
        }
    }

    /**
     * Fetches the Activity based on the peCom.
     * @param id
     * @return
     */
    List<Activity> getSCAActivityList(Long id) {
        return scaActivityMap.get(id)
    }

    /**
     * Clears the scaActivity map.
     * @param id
     */
    void clearSCAActivityMap(Long id) {
        scaActivityMap.remove(id)
    }

    void setDefaultDispositionMap(Long id, Disposition defaultDisposition) {
        defaultDispostionMap.put(id, defaultDisposition)
    }

    Disposition getDefaultDisposition(Long id) {
        return defaultDispostionMap.get(id)
    }

    void clearDefaultDispostion(Long id) {
        defaultDispostionMap.remove(id)
    }

    void setEnabledBusinessConfigList(String dataSource, List<BusinessConfiguration> enabledBusinessConfigList) {
        enabledBusinessConfMap.put(dataSource, enabledBusinessConfigList)
    }

    List<BusinessConfiguration> getEnabledBusinessConfigList(String dataSource) {
        return enabledBusinessConfMap.get(dataSource)
    }

    void setEnabledBusinessConfigProductGrpList(Long execConfigId, List<BusinessConfiguration> enabledBusinessConfigList) {
        businessConfGroupMap.put(execConfigId, enabledBusinessConfigList)
    }

    List<BusinessConfiguration> getEnabledBusinessConfigProductGrpList(Long execConfigId) {
        return businessConfGroupMap.get(execConfigId)
    }

    void clearEnabledBusinessConfigProductGrpList(Long execConfigId) {
        businessConfGroupMap.remove(execConfigId)
    }

    void setAggSignalAlertMap(Long execConfigId, String signalName, AggregateCaseAlert aggregateCaseAlert, String signalId){
        CopyOnWriteArrayList<Map> list = aggSignalAlertMap.get(execConfigId)
        if (!list) {
            list = new CopyOnWriteArrayList<>()
        }
        list.add([signalName: signalName, alert: aggregateCaseAlert, signalId: signalId])
        aggSignalAlertMap.put(execConfigId, list)
    }

    List<Map> getAggSignalAlertMap(Long execConfigId){
        return aggSignalAlertMap.get(execConfigId)
    }

    void clearAggSignalAlertMap(Long execConfigId){
        aggSignalAlertMap.remove(execConfigId)
    }

    void setSignalAlertMap(Long execConfigId, String signalName, SingleCaseAlert singleCaseAlert, String signalId) {
        CopyOnWriteArrayList<Map> list = signalAlertMap.get(execConfigId)
        if (!list) {
            list = []
        }
        list.add([signalName: signalName, alert: singleCaseAlert, signalId: signalId])
        signalAlertMap.put(execConfigId, list)
    }

    List<Map> getSignalAlertMap(Long execConfigId) {
        return signalAlertMap.get(execConfigId)
    }

    void clearSignalAlertMap(Long execConfigId) {
        signalAlertMap.remove(execConfigId)
    }

    void setAggSignalAlertMapForSignalState(Long execConfigId, def productIdSignalIdList=[:]){
        CopyOnWriteArrayList<Map> list = aggSignalAlertMapForSignalState.get(execConfigId)
        aggSignalAlertMapForSignalState.put(execConfigId, productIdSignalIdList)
    }

    def getAggSignalAlertMapForSignalState(Long execConfigId){
        return aggSignalAlertMapForSignalState.get(execConfigId)
    }

    void clearAggSignalAlertMapForSignalState(Long execConfigId){
        aggSignalAlertMapForSignalState.remove(execConfigId)
    }

    void setAlertIdSignalListMap(Long execConfigId,AggregateCaseAlert alert, List signalList,Long ruleId){
        ConcurrentHashMap<Long,CopyOnWriteArrayList<Map>> exConfigMap = alertIdSignalListMap.get(execConfigId)
        CopyOnWriteArrayList<Map> list
        if (!exConfigMap) {
            exConfigMap = new ConcurrentHashMap<>()
            list = new CopyOnWriteArrayList<>()
        } else {
            list = exConfigMap.get(ruleId)
            if(!list){
                list = new CopyOnWriteArrayList<>()
            }
        }
        list.add([alert: alert, signalList: signalList])
        exConfigMap.put(ruleId,list)
        alertIdSignalListMap.put(execConfigId, exConfigMap)
    }

    def getAlertIdSignalListMap(Long execConfigId){
        return alertIdSignalListMap.get(execConfigId)
    }

    void clearAlertIdSignalListMap(Long execConfigId){
        alertIdSignalListMap.remove(execConfigId)
    }

    void setAlertIdSignalListMapEvdasAlert(Long execConfigId,EvdasAlert alert, List signalList,Long ruleId){
        ConcurrentHashMap<Long,CopyOnWriteArrayList<Map>> exConfigMap = alertIdSignalListMapEvdasAlert.get(execConfigId)
        CopyOnWriteArrayList<Map> list
        if (!exConfigMap) {
            exConfigMap = new ConcurrentHashMap<>()
            list = new CopyOnWriteArrayList<>()
        }else{
            list = exConfigMap.get(ruleId)
            if(!list){
                list = new CopyOnWriteArrayList<>()
            }
        }
        list.add([alert: alert, signalList: signalList])
        exConfigMap.put(ruleId,list)
        alertIdSignalListMapEvdasAlert.put(execConfigId, exConfigMap)
    }

    def getAlertIdSignalListMapEvdasAlert(Long execConfigId){
        return alertIdSignalListMapEvdasAlert.get(execConfigId)
    }

    void clearAlertIdSignalListMapEvdasAlert(Long execConfigId){
        alertIdSignalListMapEvdasAlert.remove(execConfigId)
    }

    void setSignalAlertMapForEvdasAlert(Long execConfigId, String signalName, EvdasAlert evdasAlert, String signalId) {
        CopyOnWriteArrayList<Map> list = signalAlertMapEvdasAlert.get(execConfigId)
        if (!list) {
            list = []
        }
        list.add([signalName: signalName, alert: evdasAlert, signalId: signalId])
        signalAlertMapEvdasAlert.put(execConfigId, list)
    }

    List<Map> getSignalAlertMapForEvdasAlert(Long execConfigId) {
        return signalAlertMapEvdasAlert.get(execConfigId)
    }

    void clearSignalAlertMapForEvdasAlert(Long execConfigId) {
        signalAlertMapEvdasAlert.remove(execConfigId)
    }

    void setEmailLogMap(Long currentMS, List<EmailLog> lstEmailLog) {
        emailLogMap.put(currentMS, lstEmailLog)
    }

    List<EmailLog> getEmailLogMap(Long currentMS) {
        return emailLogMap.get(currentMS)
    }

    void clearEmailLogMap(Long currentMS) {
        emailLogMap.remove(currentMS)
    }

    void setInboxLogMap(Long currentMS, List<InboxLog> lstInboxLog) {
        inboxLogMap.put(currentMS, lstInboxLog)
    }

    List<InboxLog> getInboxLogMap(Long currentMS) {
        return inboxLogMap.get(currentMS)
    }

    void clearInboxLogMap(Long currentMS) {
        inboxLogMap.remove(currentMS)
    }

    void setLabelIdMap() {
        List<Map> dictionaryMap
        DictionaryMapping.withTransaction {
            dictionaryMap = DictionaryMapping.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('labelText', 'key')
                    property('id', 'value')
                }
            }
        }
        dictionaryMap.each {
            if (it.key) {
                labelIdMap.put(it.key.toString(), it.value?.toString())
            }
        }
    }

    void setIdLabelMap() {
        List<Map> dictionaryMap
        DictionaryMapping.withTransaction {
            dictionaryMap = DictionaryMapping.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('id', 'key')
                    property('labelText', 'value')
                }
            }
        }
        dictionaryMap.each {
            if (it.key) {
                idLabelMap.put(it.key.toString(), it.value?.toString())
            }
        }
    }

    Map<String, String> getIdLabelMap() {
        idLabelMap
    }

    Map<String, String> getLabelIdMap() {
        labelIdMap
    }

    @Synchronized
    void addCaseIdToMap(Long execConfigId,Long caseId,Integer versionNum){
        ConcurrentHashMap<Long,Integer> caseIdMap = caseIdListMap.get(execConfigId)
        if (!caseIdMap) {
            caseIdMap = [:]
        }
        if (!caseIdMap.containsKey(caseId))
            caseIdMap.put(caseId,versionNum)
        caseIdListMap.put(execConfigId, caseIdMap)
    }

    Map<Long,Integer> getCaseIdList(Long execConfigId){
        caseIdListMap.get(execConfigId)
    }

    void setTags(Long execConfigId,String tags){
        if(!tagsMap.get(execConfigId)){
            tagsMap.put(execConfigId, tags)
        }
    }

    String getTags(Long execConfigId){
        tagsMap.get(execConfigId)
    }

    @Synchronized
    void setTagsList(String pec, List tags) {
        ArrayList<String> list = tagsList.get(pec)
        if (!list) {
            list = []
        }
        list.addAll(tags)
        tagsList.put(pec, list)
    }
    @Synchronized
    void setKeyIdMap(String peCom, Integer prodHierarchyId, Integer eventHierarchyId) {
        Map<String, Integer> keyMap = keyIdMap.get(peCom)
        if(!keyMap){
            keyMap = ["prodHierarchyId": prodHierarchyId, "eventHierarchyId": eventHierarchyId]
            keyIdMap.put(peCom, keyMap)
        }

    }
    Map getKeyIdMap (String peCom) {
        keyIdMap.get(peCom)
    }
    void clearKeyIdMap(String peCom){
        keyIdMap.remove(peCom)
    }
    @Synchronized
    void setPecMap(String pecCom, Long id) {
        Long gpeId = pecMap.get(pecCom)
        if(!gpeId){
            pecMap.put(pecCom, id)
        }
    }
    Long getPecMap (String pecCom) {
        pecMap.get(pecCom)
    }
    void clearPecMap(){
        pecMap.clear()
    }

    @Synchronized
    void setCaseMap(String caseComb, Long id) {
        Long gpeId = caseMap.get(caseComb)
        if(!gpeId){
            caseMap.put(caseComb, id)
        }
    }
    Long getCaseMap (String caseComb) {
        caseMap.get(caseComb)
    }
    void clearCaseMap(){
        caseMap.clear()
    }
    List getTagsList (String pec) {
        tagsList.get(pec)
    }
    @Synchronized
    void removeTagForPEC(String pec) {
        tagsList.remove(pec)
    }

    void setCaseTags (Long execConfigId, Long caseId, List tags) {
        synchronized (caseTagMap) {
            ConcurrentHashMap<Long,List> exConfigCaseMap = caseTagMap.get(execConfigId)
            if (!exConfigCaseMap) {
                exConfigCaseMap = [:]
            }
            if (!exConfigCaseMap.containsKey(caseId))
                exConfigCaseMap.put(caseId, tags)
            caseTagMap.put(execConfigId, exConfigCaseMap)
        }
    }

    Map getCaseTags (Long execConfigId) {
        caseTagMap.get(execConfigId)
    }

    void clearCaseTagMap (Long execConfigId) {
        caseTagMap.remove(execConfigId)
    }
    void clearCaseIdList(Long execConfigId){
        caseIdListMap.remove(execConfigId)
    }

    void clearTagsFromMap(Long execConfigId){
        tagsMap.remove(execConfigId)
    }

    void setAlertExistingCaseHistoryList(Long configId, List<CaseHistory> existingCaseHistoryList) {
        alertExistingCaseHistoryMap.put(configId, existingCaseHistoryList)
    }

    CaseHistory getExistingCaseHistory(Long configId, String caseNumber) {
        alertExistingCaseHistoryMap.get(configId).find { it.caseNumber == caseNumber && it.isLatest }
    }
    CaseHistory getLatestDispositionCaseHistory(Long configId, String caseNumber) {
        List<CaseHistory> existingCaseHistoryList = alertExistingCaseHistoryMap.get(configId).findAll { it.caseNumber == caseNumber && it.change == Constants.HistoryType.DISPOSITION }
        if(existingCaseHistoryList){
            return existingCaseHistoryList.max{ it.lastUpdated }
        }
        return null
    }

    CaseHistory getExistingCaseHistoryWithWorkflowChanged(Long configId, String caseNumber) {
        alertExistingCaseHistoryMap.get(configId).find { it.caseNumber == caseNumber && it.change == Constants.HistoryType.DISPOSITION }
    }

    void clearAlertExistingCaseHistoryList(Long id) {
        alertExistingCaseHistoryMap.remove(id)
    }

    void setExistingPEHistoryList(Long configId, List<ProductEventHistory> existingPEHistoryList) {
        alertExistingPEHistoryMap.put(configId, existingPEHistoryList)
    }

    List<ProductEventHistory> getPEHistoryListByConfigId(String productName, String eventName, Long configId) {
        alertExistingPEHistoryMap.get(configId).findAll { it.productName == productName && it.eventName == eventName }
    }

    ProductEventHistory getPEHistoryByConfigId(String productName,Long productId,Long eventId, String eventName, Long configId) {
        alertExistingPEHistoryMap.get(configId).find {
            if (it.productId) {
                if(it.eventId){
                    it.productId == productId && it.eventId == eventId
                }else {
                    it.productId == productId && it.eventName == eventName
                }
            } else {
                // this is done for previous data where product id and event id were absent from product event history
                if(it.eventId){
                    it.productName == productName && it.eventId == eventId
                } else {
                    it.productName == productName && it.eventName == eventName
                }
            }
        }
    }

    ProductEventHistory getLatestDispositionPEHistory(String productName,Long productId,Long eventId, String eventName, Long configId) {
        List<ProductEventHistory> productEventHistoryList = alertExistingPEHistoryMap.get(configId).findAll() {
            if (it.productId) {
                if(it.eventId){
                    it.productId == productId && it.eventId == eventId && it.change == Constants.HistoryType.DISPOSITION
                } else {
                    it.productId == productId && it.eventName == eventName && it.change == Constants.HistoryType.DISPOSITION
                }
            } else {
                if(it.eventId){
                    it.productName == productName && it.eventId == eventId && it.change == Constants.HistoryType.DISPOSITION
                }
                else {
                    it.productName == productName && it.eventName == eventName && it.change == Constants.HistoryType.DISPOSITION
                }
            }
        }
        if(productEventHistoryList){
            Integer sizeOfProductEventHistoryList = productEventHistoryList.size()
            if(productEventHistoryList.clone().unique{it.lastUpdated}.size() < sizeOfProductEventHistoryList){
                return productEventHistoryList.max{ it.id }
            }else{
                return productEventHistoryList.max{ it.lastUpdated }
            }
        }
        return null
    }

    ProductEventHistory getPEHistoryByWorkflowChanged(String productName, String eventName, Long configId) {
        alertExistingPEHistoryMap.get(configId).find {
            it.productName == productName && it.eventName == eventName && it.change == Constants.HistoryType.DISPOSITION
        }
    }

    void clearExistingPEHistoryList(Long id) {
        alertExistingPEHistoryMap.remove(id)
    }

    void setGlobalCaseList(Long configId , List globalCaseList ) {
        alertGlobalCaseMap.put(configId , globalCaseList)
    }

    GlobalCase getGlobalCase(Long configId , Long caseId, Integer versionNum) {
        alertGlobalCaseMap.get(configId).find { it.caseId == caseId && it.versionNum == versionNum}
    }

    List<GlobalCase> getGlobalCaseList (Long configId) {
        alertGlobalCaseMap.get(configId) ?: []
    }

    void clearGlobalCaseMap(Long configId) {
        alertGlobalCaseMap.remove(configId)
    }

    void setGlobalProductEventList(Long configId , List globalProductEventList ) {
        alertGlobalProductEventMap.put(configId , globalProductEventList)
    }

    GlobalProductEvent getGlobalProductEventList(Long configId, String peCombination) {
        alertGlobalProductEventMap.get(configId).find { it.productEventComb == peCombination }
    }

    void clearGlobalProductEventMap(Long configId) {
        alertGlobalProductEventMap.remove(configId)
    }

    void setGlobalArticleList(Long configId , List globalArticleList ) {
        alertGlobalArticleMap.put(configId , globalArticleList)
    }

    void addGlobalArticleList(Long configId, List globalArticleList) {
        alertGlobalArticleMap.putIfAbsent(configId, [])
        alertGlobalArticleMap.get(configId).addAll(globalArticleList)
    }

    GlobalArticle getGlobalArticleList(Long configId, Long articleId) {
        alertGlobalArticleMap.get(configId).find { it.articleId == articleId }
    }

    void clearGlobalArticleMap(Long configId) {
        alertGlobalArticleMap.remove(configId)
    }
    @Synchronized
    void addPEComToMap(Long execConfigId,String peComb){
        ArrayList<String> list = pEComListMap.get(execConfigId)
        if (!list) {
            list = []
        }
        if (!list.contains(peComb))
            list.add(peComb)
        pEComListMap.put(execConfigId, list)
    }

    List<String> getPEComList(Long execConfigId){
        pEComListMap.get(execConfigId)
    }

    void clearPEComList(Long execConfigId){
        pEComListMap.remove(execConfigId)
    }

    void setExistingEvdasHistoryList(Long configId, List<EvdasHistory> existingPEHistoryList) {
        alertExistingEvdasHistoryMap.put(configId, existingPEHistoryList)
    }

    EvdasHistory getEvdasHistoryByConfigId(String productName, String eventName, Long configId) {
        alertExistingEvdasHistoryMap.get(configId).find { it.productName == productName && it.eventName == eventName }
    }
    EvdasHistory getLatestDispositionEvdasHistory(String productName, String eventName, Long configId) {
        List<EvdasHistory> evdasHistoryList = alertExistingEvdasHistoryMap.get(configId).findAll { it.productName == productName && it.eventName == eventName && it.change == Constants.HistoryType.DISPOSITION}
        if(evdasHistoryList){
            return evdasHistoryList.max{ it.lastUpdated }
        }
        return null
    }

    EvdasHistory getEvdasHistoryByWorkflowChanged(String productName, String eventName, Long configId) {
        alertExistingEvdasHistoryMap.get(configId).find {
            it.productName == productName && it.eventName == eventName && it.change == Constants.HistoryType.DISPOSITION
        }
    }

    void clearExistingEvdasHistoryList(Long id) {
        alertExistingEvdasHistoryMap.remove(id)
    }

    void setFirstVersionExecMap(Long id, Date firstExecDate) {
        firstVersionExecMap.put(id, firstExecDate)
    }

    Date getfirstVersionExecMap(Long id) {
        return firstVersionExecMap.get(id)
    }

    void clearfirstVersionExecMap(Long id) {
        firstVersionExecMap.remove(id)
    }

    void clearExConfigIds (String alertType) {
        exConfigIds.remove(alertType)
    }

    void setLastReviewDurationMap (Long exConfigId, List lastReviewDTOs) {
        lastReviewDurationMap.put(exConfigId, lastReviewDTOs)
    }

    List getLastReviewDurationMap (Long exConfigId) {
        lastReviewDurationMap.get(exConfigId)
    }

    void clearLastReviewDurationMap (Long exConfigId) {
        lastReviewDurationMap.remove(exConfigId)
    }

    LastReviewDurationDTO getLastReviewDurationDTO(String productName, String eventName, Long exConfigId) {
        lastReviewDurationMap.get(exConfigId).find {
            it.product == productName && it.event == eventName
        }
    }

    void setCurrentEndDateMap (Long exConfigId, Date currentEndDate) {
        currentEndDateMap.put(exConfigId, currentEndDate)
    }

    Date getCurrentEndDateMap (Long exConfigId) {
        currentEndDateMap.get(exConfigId)
    }

    void clearCurrentEndDateMap (Long exConfigId) {
        currentEndDateMap.remove(exConfigId)
    }

    ScaLastReviewDurationDTO getQualLastReviewDurationDTO (String caseNumber, Long exConfigId) {
        lastReviewDurationMap.get(exConfigId).find {
            it.caseNumber == caseNumber
        }
    }

    void setGlobalProductAlertMap(Long configId , List globalProductEventAlert ) {
        alertGlobalProductAlertMap.put(configId , globalProductEventAlert)
    }

    Map getGlobalProductAlertMap(Long configId, String peCombination) {
        alertGlobalProductAlertMap.get(configId).find { it.productEventComb == peCombination }
    }

    void clearGlobalProductAlertMap(Long configId) {
        alertGlobalProductAlertMap.remove(configId)
    }

    void setGlobalCaseAlertMap(Long configId , List globalCaseAlert ) {
        alertGlobalCaseAlertMap.put(configId , globalCaseAlert)
    }

    Map getGlobalCaseAlertMap(Long configId, Long caseId) {
        alertGlobalCaseAlertMap.get(configId).find { it.caseId == caseId }
    }

    List<Map> getGlobalCaseAlertList (Long configId) {
        alertGlobalCaseAlertMap.get(configId) ?: []
    }

    void clearGlobalCaseAlertMap(Long configId) {
        alertGlobalCaseAlertMap.remove(configId)
    }

    void saveSocAbbreviationCache(HashMap abbreviationMap) {
        socAbbreviationMap = abbreviationMap
    }

    String getAbbreviationMap(String soc) {
        return socAbbreviationMap.get(soc)
    }

    void setCummCaseSeriesGeneratedMap(Long id, Boolean value) {
        cummCaseSeriesGeneratedMap.put(id, value)
    }

    Boolean getCummCaseSeriesGeneratedMap(Long id) {
        cummCaseSeriesGeneratedMap.get(id)
    }

    void clearCummCaseSeriesGeneratedMap(Long id) {
        cummCaseSeriesGeneratedMap.remove(id)
    }

    Boolean containsCummCaseSeries(Long id) {
        cummCaseSeriesGeneratedMap.containsKey(id)
    }

    void setCumCaseSeriesThreadMap(Long id, CumThreadInfoDTO cumThreadInfoDTO) {
        currentlyRunningCumulativeThread.put(id, cumThreadInfoDTO)
    }

    Long getCumCaseSeriesIdFromThread(Long id) {
        currentlyRunningCumulativeThread.get(id).cumExecConfigId
    }

    CumThreadInfoDTO getCumCaseSeriesThreadFromMap(Long id) {
        currentlyRunningCumulativeThread.get(id)
    }

    void removeCumCaseSeriesThread(Long id) {
        currentlyRunningCumulativeThread.remove(id)
    }

    void saveBulkCategoriesListForCase(String key, Map catAndSubCat) {
        if(grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) {
            if (!hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP).containsKey(key)) {
                hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP).put(key, [catAndSubCat])
            } else {
                List<Map> catList = hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP).get(key)
                catList.add(catAndSubCat)
                hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP).put(key, catList)
            }
        }
        else{
            if (!bulkCategoriesMap.containsKey(key)) {
                bulkCategoriesMap.put(key, [catAndSubCat])
            } else {
                List<Map> catList = bulkCategoriesMap.get(key)
                catList.add(catAndSubCat)
                bulkCategoriesMap.put(key, catList)
            }
        }
    }

    void saveSubCatListForBulkCategories(String key, List<Map> catSubCatList) {
        if(grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) {
            hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP).put(key, catSubCatList)
        } else{
            bulkCategoriesMap.put(key, catSubCatList)
        }
    }


    List<Map> getBulkCategoriesListForCase(String key) {
        if(grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) {
            return hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP).get(key) as List
        } else{
            return bulkCategoriesMap.get(key) as List
        }
    }

    void clearBulkCategoriesListForCase() {
        if(grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) {
            hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP).clear()
        } else{
            bulkCategoriesMap.clear()
        }
    }

    def allBulkCategoriesMapList() {
        if(grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) {
            return hazelcastInstance.getMap(HAZELCAST_BULK_CATEGORIES_MAP)
        } else{
            return bulkCategoriesMap
        }
    }

    void setDataSourceMap(String key, Boolean value) {
        dataSourceMap.put(key, value)
    }

    Boolean getDataSourceMap(String key){
        Boolean val = dataSourceMap.get(key)
        return val?:false
    }

    void setDssMetaDataMap (Long exConfigId, String disabledNodes) {
        dssMetaDataMap.put(exConfigId, disabledNodes)
    }

    String getDssMetaDataMap (Long exConfigId) {
        dssMetaDataMap.get(exConfigId)
    }

    void clearDssMetaDataMap (Long exConfigId) {
        dssMetaDataMap.remove(exConfigId)
    }

    void setMasterDbDoneMap (String nodeUuid, Long masterExecId) {
        masterDbDoneMap.put(nodeUuid, masterExecId)
    }

    Long getMasterDbDoneMap (String nodeUuid) {
        masterDbDoneMap.get(nodeUuid)
    }

    void clearDssMetaDataMap (String nodeUuid) {
        masterDbDoneMap.remove(nodeUuid)
    }


}
