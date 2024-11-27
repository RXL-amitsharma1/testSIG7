package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.CaseDataDTO
import com.rxlogix.signal.*
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import oracle.jdbc.driver.OracleTypes
import org.apache.http.util.TextUtils

import java.sql.Clob
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.sql.Types

@Transactional
class CaseInfoService {

    def dataSource_pva
    def grailsApplication
    def dataSource_eudra
    def dataSource_faers
    def dataSource_vaers
    def dataSource_vigibase
    def dataSource_jader
    def signalDataSourceService
    def userService
    def singleCaseAlertService
    def pvsAlertTagService
    def pvsGlobalTagService
    def signalExecutorService
    def singleOnDemandAlertService
    CacheService cacheService
    def sqlGenerationService

    Map getCaseDetailMap(String caseNumber, String version, String followUpNumber, String alertId, List exportList,
                         Boolean isFaers, Boolean isVaers, Boolean isVigibase,Boolean isJader, Boolean evdasCase, Boolean isAdhocRun, Boolean isCaseDuplicate,Boolean isArchived,
                         String duplicateCheck = null, Boolean isVersion = false, Boolean compareScreen = false, Boolean isAvailVersion,
                         Boolean isChildCase = false, Boolean isStandalone = false) {
        log.info("getCaseDetailMap method is calling...")

        Integer caseType
        if (duplicateCheck != null) {
            caseType = duplicateCheck.equalsIgnoreCase('M') ? 1 : 0
        } else {
            def domain = isAdhocRun ? SingleOnDemandAlert : isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
            boolean isDuplicated = (alertId && alertId!=null) ? domain.get(alertId)?.getIsDuplicate() : (isCaseDuplicate ? 0 : 1)
            caseType = isDuplicated ? 1 : 0
        }
        Map treeViewMap = [alertType: Constants.AlertConfigType.SINGLE_CASE_ALERT, wwid: "", alertId: alertId, isFaers: isFaers,isVigibase: isVigibase,isJader:isJader,isAdhocRun: isAdhocRun, absentValues: "", compareScreen: compareScreen]

        Map caseInfoMap = getCaseInfoMap(caseNumber, version, followUpNumber, exportList, isFaers, isVaers, isVigibase,isJader, evdasCase ?: false,
                caseType, isVersion, treeViewMap, isChildCase, isStandalone,false)
        // versionNum needs to be overridden because DB might give upto date
        // version will not match current version
        String versionNum = String.valueOf(caseInfoMap.versionNum)
        Integer childCaseType = caseInfoMap.childCaseType?Integer.parseInt(caseInfoMap.childCaseType):1
        caseInfoMap.remove("versionNum")
        caseInfoMap.remove("childCaseType")
        List sectionNameList = caseInfoMap.sectionNameList as List
        Map sectionRelatedInfo = caseInfoMap.sectionRelatedInfo as Map
        Map fieldRelatedInfo = caseInfoMap.fieldRelatedInfo as Map
        caseInfoMap.remove("sectionNameList")
        caseInfoMap.remove("sectionRelatedInfo")
        caseInfoMap.remove("fieldRelatedInfo")

        caseInfoMap = hasValues(caseInfoMap)
        List absentValue = findAbsentValues(caseInfoMap)
        removeAbsentValueFromTreeMap(sectionNameList,absentValue)


        List versionNumberList
        if (isFaers || isVaers || isVigibase || isJader) {
            versionNumberList = [[id: 1, desc: "Version#1"]]
        } else {
            versionNumberList = getVersionNumberList(caseInfoMap?.Versions?.Versions)
        }
        String ciomsReportUrl = getCiomsReportUrl(caseNumber, versionNum)
        Map alertDetailMap = !isAdhocRun && !isStandalone ? getAlertDetailMap(alertId, caseNumber, versionNum, followUpNumber,isArchived) : [:]
        if (isChildCase) {
            alertDetailMap.caseVersion = versionNum
        }
        caseInfoMap.remove("Versions")
        int versionValue = 0
        int followUpNumberValue
        String followUpSelectionValue = followUpNumber
        if (!isFaers && !isVaers && !isVigibase && !isJader) {
            try {
                if (followUpNumber) {
                    followUpNumberValue = Integer.parseInt(followUpNumber)
                    versionValue = followUpNumberValue - 1
                } else {
                    if(isChildCase){
                        followUpSelectionValue = getDefaultFollowUpNumber(caseNumber, versionNum, childCaseType)
                        versionValue = Integer.parseInt(followUpSelectionValue)
                        followUpNumber = versionValue
                    } else {
                        followUpSelectionValue = getDefaultFollowUpNumber(caseNumber, version, caseType)
                        versionValue = Integer.parseInt(followUpSelectionValue)
                        followUpNumber = versionValue
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace()
                versionValue = 0
            }
        }
        def caseDetailMap = [data          : caseInfoMap, version: version, versionNumberList: versionNumberList, absentValue: absentValue?.join(","),
                             caseNumber    : caseNumber, followUpNumber: followUpNumber, isFaers: isFaers, isVaers: isVaers, isVigibase: isVigibase,isJader:isJader,
                             ciomsReportUrl: ciomsReportUrl, caseDetail: alertDetailMap, versionValue: versionValue, followUpSelectionValue: followUpSelectionValue, versionNum:versionNum,
                             sectionNameList: sectionNameList as JSON?:"[]", sectionRelatedInfo: sectionRelatedInfo, fieldRelatedInfo: fieldRelatedInfo,
                             sectionNameListComp: sectionNameList, isAvailVersion: isAvailVersion, isChildCase: isChildCase]

        if (alertId) {
            caseDetailMap.alertId = alertId
        }
        if (isAdhocRun) {
            caseDetailMap.caseVersionAdhoc = versionNum
        }
        if(isStandalone){
            caseDetailMap.caseVersionIsStandalone = versionNum
        }
        log.info("getCaseDetailMap method calling finizhed.")
        caseDetailMap
    }

    void removeAbsentValueFromTreeMap(List sectionNameList, List absentValue){
        sectionNameList.each {treeMap->
            if(treeMap.text == "Case Detail"){
                treeMap.children.removeIf{it.text in absentValue}
            }
        }
    }

    //Checks if map has values in it or not.
    Map hasValues(Map caseInfoMap) {
        caseInfoMap.each { k, v ->
            v.each { map ->
                map << [(Constants.CaseDetailFields.CONTAINS_VALUES): map.any { it.value && !it.value.trim().isEmpty() && !(it.key in [Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO]) }]
            }
        }
    }

    //TODO: It needs to be refactored
    List findAbsentValues(Map caseInfoMap) {
        List absentValues = []
        caseInfoMap.each { k, v ->
            if (!v?.containsValues.any { it }) {
                absentValues.add(k)
            }
        }
        return absentValues
    }

    String getDefaultFollowUpNumber(String caseNumber, String version, int caseType = 0) {
        String followUpNumber
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        try {
            def sql_statement = SignalQueryHelper.default_followup_number(caseNumber, version, caseType)
            log.info sql_statement
            sql.eachRow(sql_statement, []) { resultSet ->
                followUpNumber = resultSet.getString('significant_counter')
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        followUpNumber
    }

    /**
     * This method provides the case information map which is further used in rendering of case detail screen.
     * @param caseNumber
     * @param version
     * @param followUpNumber
     * @param exportList
     * @param isFaers
     * @param evdasCase
     * @return
     */
    Map getCaseInfoMap(String caseNumber, String version, String followUpNumber, List exportList, Boolean isFaers = false, Boolean isVaers = false, Boolean isVigibase = false,Boolean isJader = false, Boolean evdasCase, Integer caseType = 0, Boolean isVersion = false,
                       Map treeViewMap = [:], Boolean isChildCase = false, Boolean isStandalone=false, Boolean isExport = false) {
        log.info("getCaseInfoMap method is calling...")
        Map caseMultiMap = [:]
        if (isVersion)
            followUpNumber = null
        if (evdasCase) {
            caseMultiMap = getEvdasCaseInfoMap(caseNumber, version, exportList)
        } else if (isVaers) {
            caseMultiMap = populateCaseInformationMapVaers(caseNumber, version, followUpNumber, exportList, caseType)
        } else if (isVigibase) {
            caseMultiMap = populateCaseInformationMap(caseNumber, version, followUpNumber, exportList, caseType, treeViewMap, isFaers, isVigibase, isJader,false, isStandalone,isExport)
        }else if(isJader){
            caseMultiMap = populateCaseInformationMap(caseNumber, version, followUpNumber, exportList, caseType, treeViewMap, isFaers, isVigibase,isJader, true, isStandalone,isExport)
        } else {
            caseMultiMap = populateCaseInformationMap(caseNumber, version, followUpNumber, exportList, caseType, treeViewMap, isFaers, isVigibase,isJader, isChildCase, isStandalone,isExport)
        }
        log.info("getCaseInfoMap method calling finished.")
        return caseMultiMap
    }

    String getChildCaseVersion(String caseNumber, Sql sql){
        log.info("getChildCaseVersion method is calling...")
        String sqlStmt = SignalQueryHelper.child_case_max_veriosn(caseNumber)
        List list = sql.rows(sqlStmt)
        String maxVersion = ""
        if(list){
            maxVersion = list.get(0).MAX_VERSION
        }
        log.info("getChildCaseVersion method calling finished.")
        return maxVersion

    }

    private Map populateCaseInformationMap(String caseNumber, String version, String followUpNumber, List exportList,
                                              Integer caseType = 0, Map treeViewMap, Boolean isFaers, Boolean isVigibase,Boolean isJader, Boolean isChildCase = false, Boolean isStandalone=false,Boolean isExport = false) {
        Sql sql = null
        Map caseMultiMap = [:]
        try {
            if(isFaers){
                sql = new Sql(dataSource_faers)
            } else if(isVigibase) {
                sql = new Sql(dataSource_vigibase)
            } else if(isJader) {
                sql = new Sql(dataSource_jader)
            } else {
                sql = new Sql(dataSource_pva)
                DbUtil.executePIIProcCall(sql, Constants.PII_OWNER, Constants.PII_ENCRYPTION_KEY)
            }
            log.info("Started fetching case details")
            log.info("is jader :" + isJader)

            Map caseInfoMap = [:]

            Map dataMap = [:]
            if ((!TextUtils.isEmpty(caseNumber) && !TextUtils.isEmpty(version)) || (!TextUtils.isEmpty(caseNumber) && isChildCase)) {
                if (followUpNumber == "-" || followUpNumber=="undefined") {
                    followUpNumber = null
                }
                if (followUpNumber && !isVigibase) {
                    version = null
                }
                if(isChildCase){
                    version = getChildCaseVersion(caseNumber, sql)
                }
                getCaseDetailsMap(caseInfoMap, caseNumber, version, sql, exportList, dataMap, followUpNumber, caseType, treeViewMap, isFaers, isVigibase, isJader, isChildCase, isStandalone,isExport)

            }

            caseMultiMap = dataMap
            caseMultiMap["versionNum"] = caseInfoMap.versionNum
            caseMultiMap["childCaseType"] = caseInfoMap.childCaseType
            caseMultiMap["sectionNameList"] = caseInfoMap.sectionNameList
            caseMultiMap["sectionRelatedInfo"] = caseInfoMap.sectionRelatedInfo
            caseMultiMap["fieldRelatedInfo"] = caseInfoMap.fieldRelatedInfo
        } catch (Throwable th) {
            log.error(th.getMessage())
            th.printStackTrace()
        } finally {
            sql?.close()
        }
        caseMultiMap
    }
    private Map populateCaseInformationMapVaers(String caseNumber, String version, String followUpNumber, List exportList, Integer caseType = 0) {
        Sql sql = null
        Map caseMultiMap = [:]

        try {
            sql = new Sql(dataSource_vaers)

            Map caseInfoMap = Holders.config.pvsignal.caseInformationMap_vaers
            Map dataMap = [:]
            caseInfoMap.each { k, v ->
                dataMap.put(k, [])
            }
            if (!TextUtils.isEmpty(caseNumber) && !TextUtils.isEmpty(version)) {

                if (followUpNumber == "-" || followUpNumber=="undefined") {
                    followUpNumber = null
                }
                if (followUpNumber) {
                    version = null
                }

                getCaseDetailsMapForVaers(caseInfoMap, caseNumber, version, sql, exportList, dataMap, followUpNumber, caseType)
            }

            caseInfoMap.each { k, v ->
                caseMultiMap["${k}"] = dataMap["${k}"]
            }
            caseMultiMap["versionNum"] = caseInfoMap.versionNum
        } catch (Throwable th) {
            log.error(th.getMessage())
            th.printStackTrace()
        } finally {
            sql?.close()
        }
        return caseMultiMap
    }

    private void getCaseDetailsMap(Map caseInfoMap, String caseNumber, String version, Sql sql, List exportList, Map dataMap, String followUpNumber, Integer caseType = 0,
                                   Map treeViewMap, Boolean isFaers, Boolean isVigibase,Boolean isJader, Boolean isChildCase=false, Boolean isStandalone = false,Boolean isExport = false) {
        log.info("getCaseDetailsMap method is calling...")

        List allSectionListInfo = sql.rows(SignalQueryHelper.retrieve_case_details_sections_mapping_info())
        List<GroovyRowResult> sectionListInfo = initCaseDetailsSectionsInfo(allSectionListInfo)
        Map sectionRelatedInfo = sectionListInfo.collectEntries { [it?.UD_SECTION_NAME ?: it?.SECTION_NAME, [sectionKey: it?.SECTION_KEY, isFullText: it?.IS_FULL_TEXT == 1]] }
        Map fieldRelatedInfo = [:]
        List sectionNameList = []
        ExecutorService executorService = signalExecutorService.threadPoolForCaseDetail()
        List<Callable<Map>> callables = new ArrayList<Callable<Map>>()
        String versionNum = ""
        String childCaseType = ""
        User currentUser = userService.getUser()
        String blindedGTTQuery = sqlGenerationService.insertGTTForBlindedDataForCaseDetail(currentUser)
        sectionListInfo.each { GroovyRowResult eachSection ->
            log.info("eachSection data {} " + eachSection)
            callables.add(new Callable<Map>() {
                @Override
                Map call() throws Exception {
                    Sql sql1
                    Map tmpDataMap = [:]
                    Map tmpFieldMap = [:]
                    String sectionNameUd
                    try {
                        sectionNameUd = eachSection.UD_SECTION_NAME ?: eachSection.SECTION_NAME
                        if(isFaers) {
                            sql1 = new Sql(signalDataSourceService.getReportConnection("faers"))
                        } else if(isVigibase) {
                            sql1 = new Sql(signalDataSourceService.getReportConnection("vigibase"))
                        } else if(isJader) {
                            sql1 = new Sql(signalDataSourceService.getReportConnection("jader"))
                        } else {
                            sql1 = new Sql(signalDataSourceService.getReportConnection("pva"))
                            DbUtil.executePIIProcCall(sql1, Constants.PII_OWNER, Constants.PII_ENCRYPTION_KEY)
                        }
                        sql1.execute(blindedGTTQuery)

                        String insertStr = """INSERT INTO GTT_INP_CASE_DETAILS (CASE_NUM, VERSION_NUM, FOLLOWUP_NUM, MASTER_CASE, SECTION_ID) 
                                     VALUES('${caseNumber}', ${version}, ${followUpNumber?:null}, ${1}, ${eachSection.ID})
                                    """
                        log.info("query executing {} "+insertStr)
                        sql1.execute(insertStr)
                        log.info("query executed.")
                        log.info("pkg_module_case_details.p_dynamic_case_details calling...")
                        sql1.call("{call pkg_module_case_details.p_dynamic_case_details(?,?,?,?)}", [null, 101, 101, Sql.resultSet(OracleTypes.CURSOR)]) { presentCursor ->
                            Object metaData = presentCursor.invokeMethod("getMetaData", null)
                            int colCount = metaData?.getColumnCount()
                            List currentSectionList = eachSection.FIELDS
                            tmpFieldMap.put(sectionNameUd, [:])
                            currentSectionList.each {
                                tmpFieldMap.get(sectionNameUd).put(it.UI_LABEL, it.FIELD_VARIABLE)
                            }

                            //Have to populate UI label and field variable manually for case references because it is fixed.
                            if (sectionNameUd == "Case References") {
                                tmpFieldMap.put(sectionNameUd, [(Constants.CaseDetailUniqueName.REFERENCE_TYPE)  : Constants.CaseDetailUniqueName.REFERENCE_TYPE_VAL,
                                                                (Constants.CaseDetailUniqueName.REFERENCE_NUMBER): Constants.CaseDetailUniqueName.REFERENCE_NUMBER_VAL])
                            }

                            List<Map> tmpList = []
                            while (presentCursor.next()) {
                                Map map = [:]
                                if (!versionNum) {
                                    try {
                                        versionNum = presentCursor.getString(Constants.CaseDetailFields.VERSION_NUM)
                                    } catch (Exception e) {
                                        log.error("Couldn't fetch " + Constants.CaseDetailFields.VERSION_NUM + " column from section " + sectionNameUd)
                                    }
                                }
                                if (!childCaseType && isChildCase) {
                                    try {
                                        childCaseType = presentCursor.getString(Constants.CaseDetailFields.MASTER_CASE)
                                    } catch (Exception e) {
                                        log.error("Couldn't fetch " + Constants.CaseDetailFields.MASTER_CASE + " column from section " + sectionNameUd)
                                    }
                                }
                                (1..colCount).each {
                                    String colName = metaData?.getColumnName(it)
                                    String columnType = metaData.getColumnType(it)
                                    String val = null
                                    if (columnType == Types.CLOB) {
                                        Clob clob = presentCursor ? presentCursor.getClob(colName) : null
                                        if (clob) {
                                            val = clob.getSubString(1, (int) clob.length())
                                        }
                                    } else {
                                        val = presentCursor ? presentCursor.getString(colName) : null
                                    }
//                                    TODO: Remove this commented code after 54 release
//                                    String val = ""
//                                    if(sectionNameStr == "Pregnancy Information"){
//                                        val = "17IN00000000002366"
////                                        val = "16US000738"
//                                    } else {
//                                        val = presentCursor ? presentCursor.getString(colName) : null
//                                    }
                                    if (!(colName in [Constants.CaseDetailFields.TENANT_ID, Constants.CaseDetailFields.SECTION,
                                                      Constants.CaseDetailFields.CASE_ID, Constants.CaseDetailFields.VERSION_NUM,
                                                      Constants.CaseDetailFields.MASTER_CASE])) {
                                        populateValueInMap(colName, val, map, isExport)
                                    }
                                }
                                tmpList.add(map)
                            }
                            tmpDataMap.put(sectionNameUd, tmpList)
                        }
                        log.info("pkg_module_case_details.p_dynamic_case_details calling finished.")
                    } catch (Exception e) {
                        log.error("Some error occurred while fetching section information for section "+(sectionNameUd))
                        e.printStackTrace()
                        throw e
                    } finally {
                        sql1?.close()
                    }
                    log.info("getCaseDetailsMap method calling finished.")
                    return [dataMap: tmpDataMap, sectionMap: tmpFieldMap, exportString: sectionNameUd]
                }
            })

        }

        List<Future<Map>> futures = executorService.invokeAll(callables)
        Map tmpDataMap =[:]
        int count = 0
        futures.each {
            try {
                Map returnedMap = it.get()
                tmpDataMap << returnedMap?.dataMap
                fieldRelatedInfo << returnedMap?.sectionMap
                String exportString = returnedMap?.sectionMap
                if (!exportList.contains(exportString)) {
                    exportList.add(exportString)
                }
            } catch (Exception e) {
                count++
                e.printStackTrace()
            }
        }
        log.info("Out of " + sectionListInfo.size() + " sections " + count + " sections failed to load properly")
        sorDataMap(dataMap, sectionListInfo, tmpDataMap)
        caseInfoMap.versionNum = versionNum
        caseInfoMap.childCaseType = childCaseType
        putValueInSectionMap(sectionNameList, dataMap)
        caseInfoMap.sectionNameList = getTreeViewNodesForFlexible(treeViewMap.isJader, treeViewMap.alertType, treeViewMap.alertId,
                treeViewMap.isFaers, treeViewMap.isVigibase ,treeViewMap.isAdhocRun, treeViewMap.compareScreen, sectionNameList, isChildCase, isStandalone)
        caseInfoMap.sectionRelatedInfo = sectionRelatedInfo
        caseInfoMap.fieldRelatedInfo = fieldRelatedInfo
    }

    private List<GroovyRowResult> initCaseDetailsSectionsInfo(List<GroovyRowResult> allSectionListInfo) {
        List<GroovyRowResult> sectionListInfo = []

        if (!allSectionListInfo.isEmpty()) {
            List sectionFieldsList = []
            Iterator it = allSectionListInfo.iterator()
            GroovyRowResult current = it.next(), next

            while (it.hasNext()) {
                sectionFieldsList.add([UI_LABEL: current.UI_LABEL, FIELD_VARIABLE: current.FIELD_VARIABLE])
                next = it.next()

                if (next.SECTION_NAME != current.SECTION_NAME) {
                    current.put("FIELDS", sectionFieldsList)
                    sectionListInfo.add(current)
                    sectionFieldsList = []
                }

                current = next
            }

            sectionFieldsList.add([UI_LABEL: current.UI_LABEL, FIELD_VARIABLE: current.FIELD_VARIABLE])
            current.put("FIELDS", sectionFieldsList)
            sectionListInfo.add(current)
        }

        return sectionListInfo;
    }


    /*
     * This method will sort all the section fields according to SECTION_POSITION and if SECTION_POSITION is null then
     * it will put it in the back
     */
    void sorDataMap(Map dataMap, List sectionMapInfo, Map tmpDataMap) {
        sectionMapInfo.sort { a, b ->
            !a."SECTION_POSITION" ? (!b."SECTION_POSITION" ? 0 : 1) : (!b."SECTION_POSITION" ? -1 : a."SECTION_POSITION" <=> b."SECTION_POSITION")
        }
        List keySet = []
        sectionMapInfo?.each{
            keySet.add(it?.UD_SECTION_NAME?:it?.SECTION_NAME)
        }
        keySet?.each {
            if(tmpDataMap?.get(it)) {
                dataMap.put(it, tmpDataMap?.get(it))
            }
        }
    }

    void populateValueInMap(String key, String val, Map map, Boolean isExport = false) {
        if (key == Constants.CaseDetailUniqueName.DOB_VAL && Holders.config.enable.show.dob == false) {
            map[key] = "#########"
        } else {
            if (key.equalsIgnoreCase("susar") && !val) {
                map[key] = Constants.Commons.DASH_STRING
            } else if (isExport && key == Constants.CaseInforMapFields.CASE_NARRATIVE) {
                List<String> caseNarrativeList = splitCellContentCaseDetail(val)
                Integer idx = 1
                caseNarrativeList.each { it ->
                    map[key.concat(idx as String)] = it
                    idx++
                }
            } else {
                map[key] = val
            }
        }
    }

    void putValueInSectionMap(List sectionNameList, Map dataMap) {
        dataMap.each { k, v ->
            boolean added = false
            v.each { map ->
                if (!added && map.any { it.value && !it.value.trim().isEmpty() }) {
                    sectionNameList.add(k)
                    added = true
                }
            }
        }
    }
    private void getCaseDetailsMapForVaers(Map caseInfoMap, String caseNumber, String version, Sql sql, List exportList, Map dataMap, String followUpNumber, Integer caseType = 0) {

        Integer resultSetCount = 0
        sql.call("{call P_CASE_DETAILS(?,?,?,?,?,?,?,?,?)}",
                [caseNumber, version, followUpNumber, caseType,
                 Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR),
                 Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR),
                 Sql.NUMERIC]) { po_case_info, po_prod_info, po_event_info, po_narrative_info, po_version_num ->

            if (po_case_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_case_info.next()) {
                    resultSetCount++
                    prepareDataMapForVaers(po_case_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.CASE_INFORMATION)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVaers(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.CASE_INFORMATION)
                }
            }

            if (po_event_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_event_info.next()) {
                    resultSetCount++
                    prepareDataMapForVaers(po_event_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.EVENT_INFORMATION_VAERS)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVaers(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.EVENT_INFORMATION_VAERS)
                }
            }

            if (po_prod_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_prod_info.next()) {
                    resultSetCount++
                    prepareDataMapForVaers(po_prod_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PRODUCT_INFORMATION_VAERS)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVaers(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PRODUCT_INFORMATION_VAERS)
                }
            }

            if (po_narrative_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_narrative_info.next()) {
                    resultSetCount++
                    prepareDataMapForVaers(po_narrative_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.NARRATIVE_INFORMATION)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVaers(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.NARRATIVE_INFORMATION)
                }
            }
        }
    }

    private void prepareDataMapForVaers(GroovyResultSet case_info, Map caseInfoMap, Map dataMap, List exportList, String mapKey) {
        Map map = [:]
        caseInfoMap.get(mapKey).each { key, value ->
            //Thus code is encapsulated in try catch so that it doesn't break.
            try {
                map[key] = case_info ? case_info.getString(key.toString().toUpperCase()) : null
            } catch (Throwable th) {
                th.printStackTrace()
                log.error("Error in column ${key} of table ${mapKey}.")
                log.error(th.getMessage())
            }
        }
        dataMap["${mapKey}"].add(map)
        if (!exportList.contains(mapKey)) {
            exportList.add(mapKey)
        }
    }

    private void getCaseDetailsMapForVigibase(Map caseInfoMap, String caseNumber, String version, Sql sql, List exportList, Map dataMap, String followUpNumber, Integer caseType = 0) {

        Integer resultSetCount = 0
        sql.call("{call P_CASE_DETAILS(?,?,?,?,?,?,?,?,?,?,?)}",
                [caseNumber, version, followUpNumber, caseType,
                 Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR),
                 Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR),
                 Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR),
                 Sql.NUMERIC]) { po_case_info, po_prod_info, po_event_info, po_pe_info, po_dosage_info, po_patient_info, po_version_num ->

            if (po_case_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_case_info.next()) {
                    resultSetCount++
                    prepareDataMapForVigibase(po_case_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.CASE_INFORMATION)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVigibase(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.CASE_INFORMATION)
                }
            }

            if (po_event_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_event_info.next()) {
                    resultSetCount++
                    prepareDataMapForVigibase(po_event_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.EVENT_INFORMATION_VIGIBASE)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVigibase(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.EVENT_INFORMATION_VIGIBASE)
                }
            }

            if (po_prod_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_prod_info.next()) {
                    resultSetCount++
                    prepareDataMapForVigibase(po_prod_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PRODUCT_INFORMATION_VIGIBASE)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVigibase(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PRODUCT_INFORMATION_VIGIBASE)
                }
            }

            if (po_pe_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_pe_info.next()) {
                    resultSetCount++
                    prepareDataMapForVigibase(po_pe_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PE_INFORMATION)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVigibase(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PE_INFORMATION)
                }
            }

            if (po_dosage_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_dosage_info.next()) {
                    resultSetCount++
                    prepareDataMapForVigibase(po_dosage_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.DOSAGE_REGIMEN_VIGIBASE)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVigibase(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.DOSAGE_REGIMEN_VIGIBASE)
                }
            }

            if (po_patient_info != null) {
                resultSetCount = Constants.Commons.ZERO
                while (po_patient_info.next()) {
                    resultSetCount++
                    prepareDataMapForVigibase(po_patient_info, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PATIENT_INFORMATION)
                }
                if (resultSetCount == Constants.Commons.ZERO) {
                    prepareDataMapForVigibase(null, caseInfoMap, dataMap, exportList, Constants.CaseInforMapFields.PATIENT_INFORMATION)
                }
            }
        }
    }

    private void prepareDataMapForVigibase(GroovyResultSet case_info, Map caseInfoMap, Map dataMap, List exportList, String mapKey) {
        Map map = [:]
        caseInfoMap.get(mapKey).each { key, value ->
            //Thus code is encapsulated in try catch so that it doesn't break.
            try {
                map[key] = case_info ? case_info.getString(key.toString().toUpperCase()) : null
            } catch (Throwable th) {
                th.printStackTrace()
                log.error("Error in column ${key} of table ${mapKey}.")
                log.error(th.getMessage())
            }
        }
        dataMap["${mapKey}"].add(map)
        if (!exportList.contains(mapKey)) {
            exportList.add(mapKey)
        }
    }

    private void prepareDataMap(GroovyResultSet case_info, Map caseInfoMap, Map dataMap, List exportList, String mapKey) {
        Map map = [:]
        Map reporterCompCausa = ["Reporter Causality":Holders.config.pvs.case.info.reporter.causality,
                                 "Company Causality": Holders.config.pvs.case.info.company.causality]
        caseInfoMap.get(mapKey).each { key, value ->
            //Thus code is encapsulated in try catch so that it doesn't break.
            try {
                if(key == 'Event Seriousness' && Holders.config.custom.caseInfoMap.Enabled == false){
                    Integer eventSeriousnessFlag = (case_info ? case_info.getInt("Event Seriousness") : null)
                    map[key] = eventSeriousnessFlag && eventSeriousnessFlag == 1 ? "Yes" : "No"
                } else if(mapKey == "Product Event Information" && key in reporterCompCausa && reporterCompCausa.get(key)){
                    map[reporterCompCausa.get(key)] = case_info ? case_info.getString(key) : null
                } else if(Holders.config.custom.caseInfoMap.Enabled == true && mapKey == "Case Information" && key == "Country"){
                    map["Derived Country"] = case_info ? case_info.getString(key) : null
                } else if(Holders.config.custom.caseInfoMap.Enabled == true && mapKey == "Case Information" && key == "Seriousness"){
                    map["Outcome"] = case_info ? case_info.getString(key) : null
                } else {
                    map[key] = case_info ? case_info.getString(key) : null
                }
            } catch (Throwable th) {
                th.printStackTrace()
                log.error("Error in column ${key} of table ${mapKey}.")
                log.error(th.getMessage())
            }
        }
        try{
            if(case_info){
                map[Holders.config.signal.caseDetail.unique.column.name] = case_info.getString(Holders.config.signal.caseDetail.unique.column.name)
            }
        } catch(Exception ex){
            log.error("Error fetching check_seq_num ")
        }
        dataMap["${mapKey}"].add(map)
        if (!exportList.contains(mapKey)) {
            exportList.add(mapKey)
        }
    }

    /**
     * This method provides the transformed list of version number of the case
     * @param versions - List of versions coming from DB
     * @return
     */
    List<Map> getVersionNumberList(List<String> versions) {
        List<Map> versionNumberList = []
        if (versions) {
            String text
            Integer versionId
            versions.each {
                def version = (it in [Constants.BlindingStatus.REDACTED, Constants.BlindingStatus.BLINDED]) ? it : it as Integer
                Map versionNumber = [:]
                versionNumber.id = version
                text = Holders.config.custom.caseInfoMap.Enabled ? "Version Number#" : "Follow-up Number#"
                versionNumber.desc = text + version
                versionNumberList.add(versionNumber)
            }
        }
        return versionNumberList
    }


    String getCiomsReportUrl(String caseNumber, String version) {
        String ciomsReportUrl = ""

        if (!TextUtils.isEmpty(caseNumber) && !TextUtils.isEmpty(version)) {
            ciomsReportUrl = Holders.config.pvreports.ciomsI.export.uri + caseNumber + "&versionNumber=" + version
        }
        return ciomsReportUrl
    }

    Map getAlertDetailMap(String alertId, String caseNumber, String version, String followUpNumber,boolean isArchived) {
        Map alertDetail = [:]
        if (alertId) {
            def singleCaseAlert = isArchived ?  ArchivedSingleCaseAlert.get(alertId) : SingleCaseAlert.get(alertId)
            alertDetail = [
                    alertId                  : alertId,
                    priority                 : [value: singleCaseAlert?.priority?.value, iconClass: singleCaseAlert?.priority?.iconClass],
                    disposition              : singleCaseAlert?.disposition?.displayName,
                    dispositionId            : singleCaseAlert?.dispositionId,
                    isTopicAttached          : false,
                    isValidationStateAchieved: singleCaseAlert?.disposition?.validatedConfirmed,
                    configId                 : singleCaseAlert?.alertConfiguration?.id,
                    productName              : singleCaseAlert?.productName,
                    productFamily            : singleCaseAlert?.productFamily,
                    primaryEvent             : singleCaseAlert?.getAttr(cacheService.getRptFieldIndexCache('masterPrimEvtPrefTerm')),
                    caseNumber               : caseNumber,
                    caseVersion              : version,
                    followUpNumber           : followUpNumber,
                    pt                       : singleCaseAlert?.pt,
                    assignedTo               : singleCaseAlert?.assignedTo ? singleCaseAlert?.assignedTo?.toMap() : singleCaseAlert?.assignedToGroup?.toMap(),
                    execConfigId             : singleCaseAlert?.executedAlertConfiguration?.id,
                    caseId                   : singleCaseAlert?.caseId,
                    alertName                : singleCaseAlert?.name,
                    executedConfigId         : singleCaseAlert?.executedAlertConfiguration?.id,
                    folowUpNumber            : followUpNumber
            ]
        }
        alertDetail
    }

    List getTreeViewNodesForFlexible(Boolean isJader = false, String alertType, def alertId, Boolean isFaers = false,Boolean isVigibase = false, Boolean isAdhocRun = false,
                          Boolean compareScreen = null, List sectionNameList = [], Boolean isChildCase=false, Boolean isStandalone=false) {

        List keys = []
        List treeNodes = ["Case Detail"]
        Boolean isCommentsEnabled = Holders.config.icr.comments.enable
        if(isChildCase && !isJader){
            treeNodes = ["Workflow and Categories Management", "Case Detail"]
        } else if(isChildCase && isJader){
            treeNodes = ["Case Detail"]
        } else if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            if (alertId) {
                if (isFaers || isVigibase) {
                    treeNodes = ["Case Detail"]
                } else if (isAdhocRun || isStandalone) {
                    treeNodes = ["Workflow and Categories Management", "Case Detail"]
                } else if (compareScreen) {
                    treeNodes = ["Workflow and Categories Management", "Case Detail"]
                } else {
                    treeNodes = Constants.CaseDetailFields.TREE_NODES.clone() as List<String>
                }
                if (!isCommentsEnabled){
                    treeNodes.remove(Constants.CaseDetailFields.COMMENTS)
                }
            }
        }
        treeNodes.each { node ->
            keys << [text    : node,
                     state   : [opened: true],
                     children: node == "Case Detail" ? childNodesCaseDetailForFlexible(sectionNameList) : null,
                     li_attr : [id: "${node.toLowerCase().replaceAll("\\s", "_")}"]]
        }
        keys
    }


    List getTreeViewNodes(String alertType, String wwid = "", Long alertId, Boolean isFaers = false, Boolean isVaers = false, Boolean isVigibase = false, Boolean isAdhocRun = false, String absentValues, Boolean compareScreen = null, Boolean isStandalone = false) {

        List keys = []
        List absentValuesList = absentValues?.split(",")
        List treeNodes = ["Case Detail"]
        Map caseInfoMap = [:]
        Boolean isCommentsEnabled = Holders.config.icr.comments.enable

        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            if (alertId) {
                if (isFaers || isVaers || isVigibase) {
                    treeNodes = ["Case Detail"]
                } else if (isAdhocRun || isStandalone) {
                    treeNodes = ["Workflow and Categories Management", "Case Detail"]
                } else if (compareScreen) {
                    treeNodes = ["Workflow and Categories Management", "Case Detail"]
                } else {
                    treeNodes = Constants.CaseDetailFields.TREE_NODES.clone() as List<String>
                }
                if (!isCommentsEnabled){
                    treeNodes.remove(Constants.CaseDetailFields.COMMENTS)
                }
            }
            if(isVaers){
                caseInfoMap = Holders.config.pvsignal.caseInformationMap_vaers
            } else if(isVigibase){
                caseInfoMap = Holders.config.pvsignal.caseInformationMap_vigibase
            } else {
                caseInfoMap = Holders.config.pvsignal.caseInformationMap_1
                if (Holders.config.custom.caseInfoMap.Enabled) {
                    caseInfoMap = caseInfoMap + Holders.config.pvsignal.customAdditionalCaseInfoMap
                } else {
                    caseInfoMap[Constants.CaseInforMapFields.CASE_INFORMATION].remove(Constants.CaseInforMapFields.PRIMARY_IND_INFORMATION)
                    caseInfoMap[Constants.CaseInforMapFields.CASE_INFORMATION].remove(Constants.CaseInforMapFields.PRE_ANDA)
                }

                caseInfoMap = caseInfoMap + Holders.config.pvsignal.caseInformationMap_2

                if (Holders.config.custom.caseInfoMap.Enabled) {
                    caseInfoMap = caseInfoMap + Holders.config.pvsignal.customProductRefCaseInfoMap
                }

                caseInfoMap = caseInfoMap + Holders.config.pvsignal.caseInformationMap_3

                if (Holders.config.custom.caseInfoMap.Enabled) {
                    caseInfoMap = caseInfoMap + Holders.config.pvsignal.customReporteCaseInfoMap
                }
            }

            absentValuesList.each {
                caseInfoMap.remove(it)
            }

        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            if (wwid) {
                caseInfoMap = Holders.config.pvsignal.caseInformationMap_1 + Holders.config.pvsignal.caseInformationMap_2 +
                        Holders.config.pvsignal.caseInformationMap_3
            } else {
                caseInfoMap = Holders.config.pvsignal.evdasCaseInfoMap
            }
        }
        treeNodes.each { node ->
            keys << [text    : node,
                     state   : [opened: true],
                     children: node == "Case Detail" ? childNodesCaseDetail(caseInfoMap) : null,
                     li_attr : [id: "${node.toLowerCase().replaceAll("\\s", "_")}"]]
        }
        keys
    }

    def childNodesCaseDetailForFlexible(List caseInfoList) {
        List keys = []
        caseInfoList.each { node ->
            if (node != 'Versions') {
                keys << [text   : node,
                         state  : [opened: true],
                         icon   : false,
                         li_attr: [id: "${node.toLowerCase().replaceAll("\\s", "_")}"]]
            }
        }
        keys
    }

    def childNodesCaseDetail(Map caseInfoMap) {
        List keys = []
        caseInfoMap.keySet().each { node ->
            if (node != 'Versions') {
                keys << [text   : node,
                         state  : [opened: true],
                         icon   : false,
                         li_attr: [id: "${node.toLowerCase().replaceAll("\\s", "_")}"]]
            }
        }
        keys
    }

    List addCaseToSingleCaseAlert(List<String> caseNumber) {
        Sql sql = new Sql(dataSource_pva)
        Map attributeMap = Holders.config.pvsignal.attributeMap.clone() as Map
        Boolean fdaBuild  = Holders.config.custom.qualitative.fields.enabled
        if (fdaBuild) {
            attributeMap.putAll(Holders.config.pvsignal.attributeMapCustom as Map)
        }
        List singleAlertCaseList = []
        List noValueList= ['malfunctionDevices','deviceComboProduct']
        try {
            String sql_statement = SignalQueryHelper.add_case_sql(caseNumber)
            if (Holders.config.custom.qualitative.fields.enabled)
                sql_statement = SignalQueryHelper.add_case_sql_custom_col(caseNumber)
            else
                sql_statement = SignalQueryHelper.add_case_sql(caseNumber)
            sql.eachRow(sql_statement, []) { resultSet ->
                Map map = [:]
                attributeMap.each { k, v ->
                    if (k == 'masterCaseId' || k == 'cpiProdIdResolved') {
                        map[cacheService.getRptFieldIndexCache(k)] = resultSet.getLong(k)
                    } else if (k == 'casePatInfoPvrUdNumber2') {
                        String patientAge = fdaBuild ? cacheService.getRptFieldIndexCache(k) : cacheService.getRptFieldIndexCache('patInfoPatientAgeYears')
                        map[patientAge] = resultSet.getString(k)
                    }  else if(k == 'dvProdEventTimeOnsetDays'){
                        map[cacheService.getRptFieldIndexCache(k)] = resultSet.getInt(k)
                    } else if(!(k in noValueList)){
                        map[cacheService.getRptFieldIndexCache(k)] = resultSet.getString(k)
                    }
                }
                singleAlertCaseList.add(map)
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
            e.printStackTrace()
        } finally {
            sql.close()
        }
        singleAlertCaseList
    }

    def caseDifference(caseNumber, followUpNumber) {
        Sql sql = null

        def genInfo = []
        def prodInfo = []
        def eventInfo = []
        def deletedProdInfo = []
        def deletedEventInfo = []
        def modifiedInfo = []
        def caseDiffMap = [:]

        try {
            sql = new Sql(dataSource_pva)
            sql.call("{call p_get_diff_data(?,?,?,?,?,?,?,?,?)}",
                    [1, caseNumber, followUpNumber,
                     Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR),
                     Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR),
                     Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR)
                    ]
            ) { new_gen_info, new_prod_info, new_evt_info, modified_info, deleted_prod_info, deleted_evt_info ->
                if (new_gen_info != null) {
                    while (new_gen_info.next()) {
                        genInfo.add([
                                "followUpDate"  : new_gen_info.getAt(0),
                                "caseLockedDate": new_gen_info.getAt(1)
                        ])
                    }
                }
                if (new_prod_info != null) {
                    while (new_prod_info.next()) {
                        prodInfo.add([
                                "prodName"     : new_prod_info.getAt(0),
                                "dossageInfo"  : new_prod_info.getAt(1),
                                "formulation"  : new_prod_info.getAt(2),
                                "concentration": new_prod_info.getAt(3),
                                "suspect"      : new_prod_info.getAt(4)
                        ])
                    }
                }
                if (new_evt_info != null) {
                    while (new_evt_info.next()) {
                        eventInfo.add([
                                "eventPt"         : new_evt_info.getAt(0),
                                "eventSoc"        : new_evt_info.getAt(1),
                                "eventSeriousness": new_evt_info.getAt(2),
                                "eventOutcome"    : new_evt_info.getAt(3)
                        ])
                    }
                }
                if (modified_info != null) {
                    while (modified_info.next()) {
                        modifiedInfo.add([
                                "fieldName": modified_info.getAt(1),
                                "oldValue" : modified_info.getAt(2),
                                "newValue" : modified_info.getAt(3),
                        ])
                    }
                }
                if (deleted_prod_info != null) {
                    while (deleted_prod_info.next()) {
                        deletedProdInfo.add([
                                "prodName"     : deleted_prod_info.getAt(0),
                                "dossageInfo"  : deleted_prod_info.getAt(1),
                                "formulation"  : deleted_prod_info.getAt(2),
                                "concentration": deleted_prod_info.getAt(3),
                                "suspect"      : deleted_prod_info.getAt(4)
                        ])
                    }
                }
                if (deleted_evt_info != null) {
                    while (deleted_evt_info.next()) {
                        deletedEventInfo.add([
                                "eventPt"         : deleted_evt_info.getAt(0),
                                "eventSoc"        : deleted_evt_info.getAt(1),
                                "eventSeriousness": deleted_evt_info.getAt(2),
                                "eventOutcome"    : deleted_evt_info.getAt(3)
                        ])
                    }
                }
            }
            caseDiffMap = [
                    "genInfo"         : genInfo,
                    "prodInfo"        : prodInfo,
                    "eventInfo"       : eventInfo,
                    "modifiedInfo"    : modifiedInfo,
                    "deletedEventInfo": deletedEventInfo,
                    "deletedProdInfo" : deletedProdInfo
            ]

        } catch (java.sql.SQLException sqlE) {
            log.error(sqlE.getMessage())
            sqlE.printStackTrace()
        } catch (Throwable tr) {
            log.error(tr.getMessage())
            tr.printStackTrace()
        } finally {
            sql?.close()
        }
        caseDiffMap
    }

    def getCaseNarativeInfo(caseNumber, followUpNumber) {
        Sql sql = new Sql(dataSource_pva)
        def caseMap = [newNarrative: '', oldNarrative: '']
        try {
            sql.call("{call p_get_diff_data_narrative(?,?,?,?,?)}",
                    [Holders.config.signal.default.tenant.id, caseNumber, followUpNumber,
                     Sql.resultSet(OracleTypes.CURSOR), Sql.resultSet(OracleTypes.CURSOR)
                    ]
            ) { new_ver_info, old_ver_info ->
                if (new_ver_info != null) {
                    while (new_ver_info.next()) {
                        Clob clob = new_ver_info.getAt(0)
                        caseMap.newNarrative = clob?.asciiStream?.text ?: Constants.Commons.BLANK_STRING
                    }
                }
                if (old_ver_info != null) {
                    while (old_ver_info.next()) {
                        Clob clob = old_ver_info.getAt(0)
                        caseMap.oldNarrative = clob?.asciiStream?.text ?: Constants.Commons.BLANK_STRING
                    }
                }
            }
        } catch (Throwable tr) {
            log.error(tr.getMessage(), tr)
            tr.printStackTrace()
        } finally {
            sql.close()
        }
        caseMap
    }

    def getEvdasCaseDetailMap(String caseNumber, String version, Integer alertId, List exportList) {
        Map caseInfoMap = getEvdasCaseInfoMap(caseNumber, version, exportList)
        def versionNumberList = getVersionNumberListForEvdas(caseInfoMap?.Versions?.Versions)
        caseInfoMap.remove("Versions")
        caseInfoMap.remove("versionNum")
        def caseDetailMap = [data: caseInfoMap, version: version, versionNumberList: versionNumberList, caseNumber: caseNumber, alertId: alertId, controllerName: 'evdasAlert', evdasCase: true]
        caseDetailMap
    }

    def getEvdasCaseDetailMap(String wwid, Integer alertId) {
        String caseNumber
        String versionNumber
        Integer caseType
        Map caseDetailMap = [:]
        Sql sql = new Sql(dataSource_eudra)
        try {
            def sql_statement = SignalQueryHelper.evdas_wwid_case_and_version_sql(wwid)
            log.info(sql_statement)
            sql.eachRow(sql_statement, []) { resultSet ->
                caseNumber = resultSet.getString('case_num')
                versionNumber = resultSet.getString('version_num')
                caseType = resultSet.getString('flag_master_case') == '1' ? 1 : 0
            }
            Map treeViewMap = [alertType: Constants.AlertConfigType.EVDAS_ALERT, wwid: wwid, alertId: alertId, isFaers: false, isAdhocRun: false, absentValues: "", compareScreen: null]
            Map caseInfoMap = getCaseInfoMap(caseNumber, versionNumber, null, [], false, false, false,false, null, caseType, false, treeViewMap,false)

            List sectionNameList = caseInfoMap.sectionNameList as List
            Map sectionRelatedInfo = caseInfoMap.sectionRelatedInfo as Map
            Map fieldRelatedInfo = caseInfoMap.fieldRelatedInfo as Map
            caseInfoMap.remove("sectionNameList")
            caseInfoMap.remove("sectionRelatedInfo")
            caseInfoMap.remove("fieldRelatedInfo")
            def versionNumberList = getVersionNumberListForEvdas(caseInfoMap?.Versions?.Versions)
            List<Integer> versionNumbers = new JsonSlurper().parseText(caseInfoMap?.Versions?.Versions.toString()) as List<Integer>
            caseInfoMap.remove("Versions")
            caseInfoMap.remove("versionNum")
            caseInfoMap.remove("childCaseType")
            caseInfoMap = hasValues(caseInfoMap)
            List absentValue = findAbsentValues(caseInfoMap)
            removeAbsentValueFromTreeMap(sectionNameList,absentValue)
            caseDetailMap = [data: caseInfoMap, version: Collections.max(versionNumbers), versionNumberList: versionNumberList, caseNumber: caseNumber, alertId: alertId, controllerName: 'evdasAlert', wwid: wwid, caseType: caseType,
                             sectionNameList: sectionNameList as JSON, sectionRelatedInfo: sectionRelatedInfo, fieldRelatedInfo: fieldRelatedInfo]
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        caseDetailMap
    }

    def getVersionNumberListForEvdas(List<String> versions){
        List<Map> versionNumberList = []
        if (versions) {
            List<Integer> versionNumbers = new JsonSlurper().parseText(versions.toString()) as List<Integer>
            String text
            Integer versionId
            versionNumbers?.each {
                Map versionNumber = [:]
                versionNumber.id = it
                text = Holders.config.custom.caseInfoMap.Enabled? Constants.VERSION_NUMBER: Constants.FOLLOW_UP_NUMBER
                if(it == 1 || it == 0){
                    versionNumber.text = Constants.INITIAL
                    versionNumber.desc = Constants.INITIAL
                }else{
                    it-=1
                    versionNumber.desc = text + it
                }
                versionNumberList.add(versionNumber)
            }
        }
        return versionNumberList
    }

    Map getEvdasCaseDetailMap(String wwid, String caseNumber, String version, Integer alertId, List exportList, String followUpNumber, Integer caseType) {
        Map treeViewMap = [alertType: Constants.AlertConfigType.EVDAS_ALERT, wwid: wwid, alertId: alertId, isFaers: false, isAdhocRun: false, absentValues: "", compareScreen: null]
        Map caseInfoMap = getCaseInfoMap(caseNumber, version, followUpNumber, exportList, false, false, false, false,null, caseType, false, treeViewMap, false)
        List sectionNameList = caseInfoMap.sectionNameList as List
        Map sectionRelatedInfo = caseInfoMap.sectionRelatedInfo as Map
        Map fieldRelatedInfo = caseInfoMap.fieldRelatedInfo as Map
        caseInfoMap.remove("sectionNameList")
        caseInfoMap.remove("sectionRelatedInfo")
        caseInfoMap.remove("fieldRelatedInfo")
        List versionNumberList = getVersionNumberList(caseInfoMap?.Versions?.Versions)
        caseInfoMap.remove("Versions")
        caseInfoMap.remove("versionNum")
        caseInfoMap.remove("childCaseType")
        caseInfoMap = hasValues(caseInfoMap)
        List absentValue = findAbsentValues(caseInfoMap)
        removeAbsentValueFromTreeMap(sectionNameList,absentValue)
        Map caseDetailMap = [data: caseInfoMap, version: version, versionNumberList: versionNumberList, caseNumber: caseNumber, alertId: alertId, controllerName: 'evdasAlert', wwid: wwid, caseType: caseType,
                             sectionNameList: sectionNameList as JSON, sectionRelatedInfo: sectionRelatedInfo, fieldRelatedInfo: fieldRelatedInfo]
        caseDetailMap
    }

    def getEvdasCaseInfoMap(String caseNumber, String version, exportList) {
        String cl = "(" + "'" + caseNumber + "'" + "," + version + ")"
        Map caseInfoMap = Holders.config.pvsignal.evdasCaseInfoMap
        Map caseMultiMap = [:]
        Map dataMap = [:]
        caseInfoMap.each { k, v ->
            dataMap.put(k, [])
        }
        Sql sql = null
        try {
            if (cl) {
                sql = new Sql(dataSource_eudra)
                sql.call("{call p_case_details(?,?,?,?)}",
                        [caseNumber, version, null, Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]) { new_gen_info ->
                    if (new_gen_info != null) {
                        while (new_gen_info.next()) {
                            caseInfoMap.each { k, v ->
                                Map map = [:]
                                v.each { key, value ->
                                    //It may happen that keys are not alligned with the db side query labels
                                    //Thus code is encapsulated in try catch so that it doesn't break.
                                    try {
                                        map[key] = new_gen_info.getString(key)
                                    } catch (Throwable thr) {
                                        log.error(thr.getMessage())
                                        thr.printStackTrace()
                                    }
                                }
                                if (dataMap["${k}"].size() > 0) {
                                    boolean isExist = false
                                    dataMap["${k}"].each {
                                        if (it.equals(map))
                                            isExist = true
                                    }
                                    if (!isExist) {
                                        dataMap["${k}"].add(map)
                                    }

                                } else {
                                    dataMap["${k}"].add(map)
                                }

                                if (!exportList.contains(k)) {
                                    exportList.add(k)
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
            log.error(th.printStackTrace())
            th.printStackTrace()
        } finally {
            sql?.close()
        }
        caseInfoMap.each { k, v ->
            caseMultiMap["${k}"] = dataMap["${k}"]
        }

        caseMultiMap
    }

    List<Map> getAttachmentListMap(Long alertId) {
        SingleCaseAlert singleCaseAlert = SingleCaseAlert.findById(alertId)
        String timezone = userService.user.preference.timeZone
        List<Map> attachments = singleCaseAlert?.attachments.collect {
            [
                    name       : it.name,
                    description: AttachmentDescription.findByAttachment(it)?.description,
                    timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone),
                    modifiedBy : AttachmentDescription.findByAttachment(it)?.createdBy
            ]
        }
        if (attachments) {
            attachments
        } else {
            [
                    [name: "", description: "", timeStamp: "", modifiedBy: ""]
            ]
        }
    }

    Map getFullCaseListData(CaseDataDTO caseDataDTO, Boolean isAdhocRun) {
        if (caseDataDTO.caseList && caseDataDTO.caseNumber && caseDataDTO.version) {
            Map<String, Object> result = [next             : null,
                                          previous         : null, fullCaseList: caseDataDTO.caseListString,
                                          detailsParameters: caseDataDTO.detailsParameters, totalCount: caseDataDTO.totalCount,
                                          execConfigId     : caseDataDTO.id]
            List caseList = caseDataDTO.caseList
            Integer indexOfCase = caseList.findIndexOf {
                it.caseNumber == caseDataDTO.caseNumber && it.alertId == caseDataDTO.alertId
            }
            if (caseDataDTO.params) {
                caseDataDTO.params.id = caseDataDTO.id
                if (indexOfCase == caseDataDTO.params.length - 1 && caseDataDTO.totalCount > caseDataDTO.params.length) {
                    caseDataDTO.params.start += caseDataDTO.params.length
                    if (isAdhocRun) {
                        caseList = singleOnDemandAlertService.fetchNextXFullCaseList(caseDataDTO.params, caseDataDTO.isFaers, caseDataDTO.isVaers, caseDataDTO.isVigibase,caseDataDTO.isJader)
                    } else {
                        caseList = singleCaseAlertService.fetchNextXFullCaseList(caseDataDTO.params, caseDataDTO.isFaers, caseDataDTO.isVaers, caseDataDTO.isVigibase,caseDataDTO.isJader)
                    }
                    indexOfCase = -1
                } else if (indexOfCase == -1) {
                    caseDataDTO.params.start -= caseDataDTO.params.length
                    if (isAdhocRun) {
                        caseList = singleOnDemandAlertService.fetchNextXFullCaseList(caseDataDTO.params, caseDataDTO.isFaers, caseDataDTO.isVaers, caseDataDTO.isVigibase,caseDataDTO.isJader)
                    } else {
                        caseList = singleCaseAlertService.fetchNextXFullCaseList(caseDataDTO.params, caseDataDTO.isFaers, caseDataDTO.isVaers, caseDataDTO.isVigibase,caseDataDTO.isJader)
                    }
                    indexOfCase = caseDataDTO.params.length - 2
                } else if (indexOfCase == 0 && caseDataDTO.params.start != 0) {
                    caseDataDTO.params.start -= 1
                    if (isAdhocRun) {
                        caseList = singleOnDemandAlertService.fetchNextXFullCaseList(caseDataDTO.params, caseDataDTO.isFaers, caseDataDTO.isVaers, caseDataDTO.isVigibase,caseDataDTO.isJader)
                    } else {
                        caseList = singleCaseAlertService.fetchNextXFullCaseList(caseDataDTO.params, caseDataDTO.isFaers, caseDataDTO.isVaers, caseDataDTO.isVigibase,caseDataDTO.isJader)
                    }
                        indexOfCase = 1
                    
                }
                result.fullCaseList = JsonOutput.toJson(caseList)
                result.detailsParameters = JsonOutput.toJson(caseDataDTO.params)
            }
            result.version = caseDataDTO.version
            if (indexOfCase < caseList.size() - 1) {
                result.next = [caseNumber: caseList[indexOfCase + 1].caseNumber, caseVersion: caseList[indexOfCase + 1].caseVersion, alertId: caseList[indexOfCase + 1].alertId, followUpNumber: caseList[indexOfCase + 1].followUpNumber]
            }

            if (indexOfCase > 0 && caseList.size() - 1 >= indexOfCase) {
                result.previous = [caseNumber: caseList[indexOfCase - 1].caseNumber, caseVersion: caseList[indexOfCase - 1].caseVersion, alertId: caseList[indexOfCase - 1].alertId, followUpNumber: caseList[indexOfCase - 1].followUpNumber]
            } else if (indexOfCase == -1) {
                result.previous = [caseNumber: caseDataDTO.caseList[indexOfCase - 1].caseNumber, caseVersion: caseDataDTO.caseList[indexOfCase - 1].caseVersion, alertId: caseDataDTO.caseList[indexOfCase - 1].alertId, followUpNumber: caseDataDTO.caseList[indexOfCase - 1].followUpNumber]
            }
            return result
        }
        return [version:caseDataDTO?.version, execConfigId:caseDataDTO?.id]
    }
    Map getUniqueColumns(Map uniqueColumnMap, Boolean isFaers){
        String deviceInformation = ""
        List freeTextField = []
        Sql sql
        try{
            if (isFaers){
                sql = new Sql(dataSource_faers)
            } else {
                sql = new Sql(dataSource_pva)
            }
            List sectionList = sql?.rows("SELECT * FROM case_details_section WHERE  FLAG_ENABLE=1 and ( SECTION_KEY != 'Versions' or SECTION_KEY is null)")
            Map deviceInfoMap = sectionList?.find{it."SECTION_KEY" == "DeviceInformation"}
            deviceInformation = deviceInfoMap ? (deviceInfoMap?.UD_SECTION_NAME ?: deviceInfoMap?.SECTION_NAME) : "Device Information"
            List freeTextList = sectionList?.findAll {it."IS_FULL_TEXT" == 1}
            freeTextList?.each{
                freeTextField.add(it?.UD_SECTION_NAME?:it?.SECTION_NAME)
            }
            Map sectionMap = sectionList.collectEntries { [it?.SECTION_NAME, it?.UD_SECTION_NAME ?: it?.SECTION_NAME] }
            List fieldList = sql?.rows("SELECT SECTION_NAME, CASE_DIFFERENCE_IDENTIFIER, UI_LABEL FROM case_details_field_mapping")
            Map fieldMap = fieldList.groupBy {it?.SECTION_NAME}
            Map tmpFieldMap = [:]
            sectionMap.each{ key, val ->
                tmpFieldMap.put(val, fieldMap.get(key))
            }
            tmpFieldMap.each{ key, val ->
                List uniqueFields = []
                if(key == "Case References"){
                    uniqueColumnMap.put(key, [Constants.CaseDetailUniqueName.REFERENCE_TYPE, Constants.CaseDetailUniqueName.REFERENCE_NUMBER,Constants.CaseDetailFields.CHECK_SEQ_NUM])
                } else if(val?.any{it?.CASE_DIFFERENCE_IDENTIFIER}){
                    uniqueFields = val?.findAll{it?.CASE_DIFFERENCE_IDENTIFIER}?.UI_LABEL
                    uniqueFields?.removeAll(Collections.singleton(null))
                    uniqueColumnMap.put(key,uniqueFields)
                } else {
                    uniqueFields = val?.UI_LABEL
                    uniqueFields?.removeAll(Collections.singleton(null))
                    uniqueColumnMap.put(key, uniqueFields)
                }
            }
        } catch(Exception e){
            log.error("Error occurred while fetching unique column map")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        ["deviceInformation": deviceInformation, freeTextField: freeTextField]
    }

    Map fetchCaseVersions(String executionId, String caseId, Boolean isCaseSeries, String caseNumber, String alertId,
                          Boolean isAdhocRun, Boolean isArchived, Boolean isChildCase = false, String versionNum = "0") {
        if(isAdhocRun && !isCaseSeries && !isChildCase){
            def alert = SingleOnDemandAlert.get(alertId as Long)
            caseId = alert?.caseId as String
            executionId = alert?.executedAlertConfiguration?.id as String
        }
        List versionsList = []
        Sql sql = null
        try {
            sql = new Sql(dataSource_pva)
            versionsList = callVersionListProc(executionId,caseId,sql)
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            sql.close()
        }

        Integer caseVersion = 0
        if(isChildCase){
            caseVersion = versionNum as Integer
        }
        if(alertId != "" && alertId!=null) {
            caseVersion = isCaseSeries ? fetchLastAggReviewedVersion(executionId as Long, caseId, alertId as Long, isAdhocRun, isArchived) : fetchLastSingleReviewedVersion(executionId as Long, caseNumber, isAdhocRun)
        }
        Boolean isLastVersionPresent = false
        Boolean isArgusSource = false
        Integer followUp
        versionsList.eachWithIndex{it,index->
            if(it.versionNum == caseVersion) {
                it.put("lastVersion" , true)
                isLastVersionPresent = true
                followUp = it.followUpNum
            } else {
                it.put("lastVersion" , false)
            }
            // setting parameter true if version exist in current map
            if(it?.versionNum?.toString() == versionNum) {
                it.put("versionNumExist" , true)
            } else {
                it.put("versionNumExist" , false)
            }
            if(it.followUpNum == 0){
                it.data = 'Initial'
            } else if(it.sourceType == Constants.pvaDataSources.ARGUS || it.sourceType == Constants.pvaDataSources.PVCM){
                it.data = 'Follow-Up#' + it.followUpNum
                isArgusSource = true
            } else{
                it.data = it.versionNum == 0 ? "Initial" : 'Version#' + it.versionNum
            }
        }
        String firstParam = ""
        String secondParam = ""
        if(versionsList && versionsList.get(0)?.sourceType == "ARGUS"){
            firstParam = "followUpNum"
            secondParam = "versionNum"
        } else{
            firstParam = "versionNum"
            secondParam = "followUpNum"
        }
        // version List must exist current version we are seeing
        // otherwise alert level categories will not fetched correctly
        versionsList.sort{row1,row2 -> row2[firstParam] <=> row1[firstParam] ?: (row2["lastVersion"] <=> row1["lastVersion"] ?:( row2["versionNumExist"] <=> row1["versionNumExist"] ?: row2[secondParam] <=> row1[secondParam]))}.unique{it[firstParam]}
        return [versionsList:versionsList , isLastVersionPresent : isLastVersionPresent, isArgusDataSource: isArgusSource, previousVersion: caseVersion, previousFollowUp: followUp]
    }

    Integer fetchLastSingleReviewedVersion(Long exConfigId , String caseNumber, Boolean isAdhocRun) {
        List<Long> reviewedDispositions = cacheService.getDispositionByReviewCompleted()*.id
        ExecutedConfiguration exConfig = ExecutedConfiguration.get(exConfigId)
        Configuration configuration = Configuration.get(exConfig?.configId)
        List alert = ArchivedSingleCaseAlert.createCriteria().list{
            eq('alertConfiguration', configuration)
            eq('caseNumber', caseNumber)
            if(reviewedDispositions.size()>0) {
                'in'('disposition.id', reviewedDispositions)
            }

            order('dateCreated','desc')
            maxResults(1)
        }

        return alert[0]?.caseVersion

    }

    Integer fetchLastAggReviewedVersion(Long executedConfigId, String caseId, Long alertId, Boolean isAdhocRun, Boolean isArchived) {
        ExecutedConfiguration exConfig = ExecutedConfiguration.get(executedConfigId)
        if(exConfig?.adhocRun){
            return 999999
        }
        List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(exConfig.configId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], exConfig.type).reportVersion.collect {
            (int) it
        }
        SingleCaseAlert singleCaseAlert = SingleCaseAlert.get(alertId)

        def aggregateCaseAlert
        if(exConfig?.adhocRun){
            aggregateCaseAlert = AggregateOnDemandAlert.get(singleCaseAlert?.aggAlertId as Long)
        } else{
            aggregateCaseAlert = AggregateCaseAlert.get(singleCaseAlert?.aggAlertId as Long)?:ArchivedAggregateCaseAlert.get(singleCaseAlert?.aggAlertId as Long)
        }
        List<Long> execIds = ExecutedConfiguration.createCriteria().list{
            eq('configId', exConfig.configId)
            order('dateCreated', 'desc')
            if (exConfig.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                eq("selectedDatasource", exConfig.selectedDatasource)
            }
            eq("type", exConfig.type)
            eq("isLatest", false)
            eq("adhocRun", exConfig.adhocRun)
            eq("isDeleted", false)
            if (reportVersions.size() > 0) {
                or {
                    reportVersions.collate(999).each {
                        'in'('numOfExecutions', it)
                    }
                }
            }
        }*.id
        Sql sql = null
        def version
        try {
            String execStatement = ""
            sql = new Sql(dataSource_pva)
            if(execIds.size()>0) {
                version = callPreviousVersionProc(executedConfigId, caseId, aggregateCaseAlert, execIds, sql)
            } else{
                version = -1
            }
        }catch(Exception ex){
              ex.printStackTrace()
        }finally{
            sql?.close()
        }

        return version
    }

    List compareCategories(List categoriesListVersionAvailable , List categoriesListVersionCompare) {
        List categories = []
        categoriesListVersionAvailable.each{
            String tagText = it.tagText
            String subTagText = it.subTagText
            Map tagCompareMap = categoriesListVersionCompare.find{map-> map.tagText == tagText && map.privateUser == it.privateUser && map.tagType == it.tagType}
            if(tagCompareMap){
                String subTagsCompared = tagCompareMap.subTagText
                if(subTagsCompared == subTagText){
                    it.comparedType = 'U'
                    categories.add(it)
                }else{
                    List subTagList = subTagsCompared.split(';')
                    String subTagsUnchanged = ""
                    String subTagsAdded = ""
                    String subTagsDeleted = ""
                    subTagText?.split(';').each{subTag->
                        if(subTagList.contains(subTag)){
                            if(subTagsUnchanged == ""){
                                subTagsUnchanged += subTag
                            }else{
                                subTagsUnchanged += ";" + subTag
                            }
                            subTagList.remove(subTag)
                        }else{
                            if(subTagsAdded == ""){
                                subTagsAdded += subTag
                            }else{
                                subTagsAdded += ";" + subTag
                            }
                        }
                    }
                    subTagList?.each{subTag->
                        if(subTagsDeleted == ""){
                            subTagsDeleted += subTag
                        }else{
                            subTagsDeleted += ";" + subTag
                        }
                    }
                    Map tempMap = it
                    if(subTagsUnchanged != ""){
                        tempMap = it.clone()
                        tempMap.subTagText = subTagsUnchanged
                        tempMap.comparedType = 'U'
                        categories.add(tempMap)
                    }
                    if(subTagsDeleted != ""){
                        tempMap = it.clone()
                        tempMap.subTagText = subTagsDeleted
                        tempMap.comparedType = 'D'
                        categories.add(tempMap)
                    }
                    if(subTagsAdded!=""){
                        tempMap = it.clone()
                        tempMap.subTagText = subTagsAdded
                        tempMap.comparedType = 'A'
                        categories.add(tempMap)
                    }

                }
                categoriesListVersionCompare.remove(tagCompareMap)
            }else{
                it.comparedType = 'A'
                categories.add(it)
            }
        }

        categoriesListVersionCompare.each{
            it.comparedType = 'D'
            categories.add(it);
        }
        return categories
    }

    List callVersionListProc(String executionId, String caseId, Sql sql) {
        List versionsList = []
        log.info("Case Id is" + caseId)
        log.info("executionId is" + executionId)
        log.info("calling p_case_version_diff_info procedure " )
        def beforeTime = System.currentTimeMillis()
        sql.call("{call p_case_version_diff_info(?,?,?)}",
                [executionId, caseId, Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]) { ref_cur ->
            if (ref_cur != null) {
                while (ref_cur.next()) {
                    versionsList.add([
                            "versionNum"     : ref_cur.getAt(0),
                            "receiptDate"    : ref_cur.getAt(1),
                            "followUpNum"    : ref_cur.getAt(2),
                            "flagSignificant": ref_cur.getAt(3),
                            "sourceType"     : ref_cur.getAt(4),
                    ])
                }
            }
        }
        def afterTime = System.currentTimeMillis()
        log.info("p_case_version_diff_info took " + (afterTime - beforeTime) / 1000 + " seconds ")

        return versionsList
    }

    Integer callPreviousVersionProc(Long executedConfigId, String caseId, def aggregateCaseAlert, List execIds, Sql sql){
        Integer version
        log.info("executedConfigId is" + executedConfigId)
        log.info("case Id is" + caseId)
        log.info("Product Id is " + aggregateCaseAlert.productId)
        log.info("PT code is " + aggregateCaseAlert.ptCode)
        String execStatement = ""
        execStatement = "Begin " +
                "execute immediate('delete from GTT_ALERT_PRODUCT_LIST'); "
        if (execIds) {
            execIds.eachWithIndex {value,index->
                execStatement += "INSERT INTO GTT_ALERT_PRODUCT_LIST(col_1,col_2) values (${value as String}, ${index+1 as String});"
            }
            execStatement += " END;"
            sql.execute(execStatement)
            sql.call("{call pkg_pvs_case_drill_down.p_case_drill_prev_version(?,?,?,?,?,?,?,?,?)}",
                    [executedConfigId, caseId as Integer, aggregateCaseAlert.productId as Long, aggregateCaseAlert.ptCode, null, null,null, null,Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]){ref_cur->
                if (ref_cur != null) {
                    while (ref_cur.next()) {
                        version = ref_cur.getAt(0)
                    }
                }
            }
        }
        return version
    }

    Map getGobalAndAlertSpecificCategoriesList(String alertId, String version, Boolean isArchived,Boolean isAdhocRun, Long caseId = null) {
        def categoryList = [:]
        Boolean isCategoryEditable = false
        Boolean isFoundAlertArchived = true
        Long foundAlertId
        List<Map> allGlobalTagsList = []
        def alert
        if (alertId) {
            Long scaId = alertId as Long
            List<Map> allAlertTagsList = []
            def sca
            if(!isAdhocRun){
                sca = isArchived ? ArchivedSingleCaseAlert.get(scaId) : SingleCaseAlert.get(scaId)
            }else{
                sca =  SingleOnDemandAlert.get(alertId)
            }
            if (sca.caseVersion?.toString() == version ) {
                isCategoryEditable = true
                String singleCaseType = isAdhocRun ? Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND : Constants.AlertConfigType.SINGLE_CASE_ALERT
                allAlertTagsList = pvsAlertTagService.getAllAlertSpecificTags([scaId], singleCaseType)
                allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags([sca?.globalIdentityId], Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.Commons.REVIEW)

            } else if (!isArchived) {
                alert = ArchivedSingleCaseAlert.createCriteria().list {
                    eq('alertConfiguration.id', sca.alertConfiguration.id)
                    eq('caseVersion', version as Integer)
                    eq('caseId', sca.caseId)
                    maxResults(1)
                }
                if (alert[0]) {
                    foundAlertId = alert[0]?.id
                    allAlertTagsList = pvsAlertTagService.getAllAlertSpecificTags([alert[0].id], Constants.AlertConfigType.SINGLE_CASE_ALERT)
                    allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags([alert[0]?.globalIdentityId], Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.Commons.REVIEW)

                }

            } else if(!isAdhocRun){
                alert = SingleCaseAlert.createCriteria().list {
                    eq('alertConfiguration.id', sca.alertConfiguration.id)
                    eq('caseVersion', version as Integer)
                    eq('caseId', sca.caseId)
                    maxResults(1)
                }
                isFoundAlertArchived = false
                if (!alert) {
                    alert = ArchivedSingleCaseAlert.createCriteria().list {
                        eq('alertConfiguration.id', sca.alertConfiguration.id)
                        eq('caseVersion', version as Integer)
                        eq('caseId', sca.caseId)
                        ne('executedAlertConfiguration.id', sca.executedAlertConfiguration.id)
                        maxResults(1)
                    }
                    isFoundAlertArchived = true
                }
                if (alert[0]) {
                    foundAlertId = alert[0]?.id
                    allAlertTagsList = pvsAlertTagService.getAllAlertSpecificTags([alert[0].id], Constants.AlertConfigType.SINGLE_CASE_ALERT)
                    allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags([alert[0]?.globalIdentityId], Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.Commons.REVIEW)

                }

            }
            if (!allGlobalTagsList) {
                GlobalCase globalCase = GlobalCase.findByCaseIdAndVersionNum(sca?.caseId, version)
                log.info('global case parameter (case Id,version,case):' + sca?.caseId + ',' + version + ',' + globalCase)

                if(globalCase){
                    allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags([globalCase.id], Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.Commons.REVIEW)
                }
            }

            categoryList = allGlobalTagsList + allAlertTagsList

        }else{
            GlobalCase globalCase = GlobalCase.findByCaseIdAndVersionNum(caseId, version)

            if(globalCase){
                allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags([globalCase.id], Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.Commons.REVIEW)
            }
            categoryList = allGlobalTagsList
        }
        Map categoryMap = [categoryList: categoryList?.sort{ it.tagText?.toLowerCase()}, isCategoryEditable: isCategoryEditable, isFoundAlertArchived: isFoundAlertArchived, foundAlertId: foundAlertId]

        return categoryMap
    }

    Long fetchCaseId(String caseNumber){
        List data = []
        def sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        try {
            def sql_statement = "select distinct case_id from c_identification where case_num='" +  caseNumber +"'"
            data=sql.rows(sql_statement)
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        return data[0]['case_id'] as Long
    }

    List createTreeMapForCaseComp(List availVerTreeMapChil, List verCompTreeMapChil) {
        Sql sql
        List caseDetailChildList = []
        try {
            sql = new Sql(dataSource_pva)
            List sectionListInDb = sql.rows("select NVL(UD_SECTION_NAME, SECTION_NAME) as SECTION_NAME from case_details_section where flag_enable = 1 order by SECTION_POSITION asc")
            sectionListInDb?.each {
                String sectionName = it."SECTION_NAME"
                Map availSectMap = availVerTreeMapChil?.find { it?.text == sectionName } as Map
                Map compSectMap = verCompTreeMapChil?.find { it?.text == sectionName } as Map
                if (availSectMap) {
                    caseDetailChildList.add(availSectMap)
                } else if (compSectMap) {
                    caseDetailChildList.add(compSectMap)
                }
            }
        } catch (Exception e) {
            log.error("Couldn't fetch section list from DB for data source pva")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        caseDetailChildList
    }

    Map compareVersionInfo(params) {
        String caseDetailStr = "Case Detail"
        ExecutorService executorService = signalExecutorService.threadPoolForCaseDetail()
        List<Callable<Map>> callables = new ArrayList<Callable<Map>>(){{
            add(new Callable<Map>() {
                @Override
                Map call() throws Exception {
                    getCaseDetailMap(params.caseNumber, params.versionAvailable, params.followUpAvailable, params.alertId, [],
                            params.boolean('isFaers'), false, false,false,false ,params.boolean('isAdhocRun'), params.isCaseDuplicate,
                            params.boolean('isArchived'), 'M', false, true, true, false, false)
                }
            })
            add(new Callable<Map>() {
                @Override
                Map call() throws Exception {
                    getCaseDetailMap(params.caseNumber, params.versionCompare, params.followUpCompare, params.alertId, [],
                            params.boolean('isFaers'), false, false,false,false,params.boolean('isAdhocRun'), params.isCaseDuplicate,
                            params.boolean('isArchived'), 'M', false, true, false, false, false)
                }
            })
        }}
        Map versionAvailableMap = [:]
        Map versionCompareMap = [:]
        List<Future<Map>> futures = executorService.invokeAll(callables)
        futures.each{
            try{
                Map retMap = it.get()
                if(retMap?.isAvailVersion){
                    versionAvailableMap = retMap
                } else {
                    versionCompareMap = retMap
                }
            } catch (Exception e){
                log.error("Some error occurred while fetching values for case comparison")
                e.printStackTrace()
                throw e
            }
        }
        List finalTreeView = []

        List returnedTreeList = createTreeMapForCaseComp(versionAvailableMap?.sectionNameListComp?.find { it.text == caseDetailStr }?."children",
                versionCompareMap?.sectionNameListComp?.find { it.text == caseDetailStr }?."children")
        versionAvailableMap.sectionNameListComp?.each { Map sectMap ->
            if (sectMap.text == caseDetailStr) {
                sectMap.put("children", returnedTreeList)
            }
            finalTreeView.add(sectMap)
        }
        [versionAvailableData: versionAvailableMap.data, versionCompareData: versionCompareMap.data, sectionNameList : finalTreeView]
    }

    List<String> splitCellContentCaseDetail(String content) {
        List<String> splitedContentList = []
        if (content.length() <= Constants.ExcelConstants.MAX_CELL_NARRATIVE_LENGTH_XLSX) {
            splitedContentList.add(content)
        } else {
            splitedContentList.add(content.substring(0, Constants.ExcelConstants.MAX_CELL_NARRATIVE_LENGTH_XLSX))
            splitedContentList.addAll(splitCellContentCaseDetail(content.substring(Constants.ExcelConstants.MAX_CELL_NARRATIVE_LENGTH_XLSX)))
        }
        return splitedContentList
    }
}
