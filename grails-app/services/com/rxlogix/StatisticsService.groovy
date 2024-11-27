package com.rxlogix

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.util.AlertUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import groovyx.net.http.Method

import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Clob
import java.sql.ResultSet

@Transactional
class StatisticsService implements AlertUtil {

    def grailsApplication
    def metadataLocation = "metadata"
    def signalDataSourceService
    def reportIntegrationService
    def dataObjectService
    def cacheService
    def alertService
    def reportExecutorService
    def aggregateExecutorService

    def mergeStatsScoresForMaster(Long masterExecId, Long execConfigId, String selectedDatasource, List<String> allFiles) {
        def status = [:]
        try {
            String subgrpAgeFlag = (selectedDatasource == Constants.DataSource.VAERS || selectedDatasource == Constants.DataSource.VIGIBASE) ? "false" : isSubgrpEnabled(Holders.config.subgrouping.ageGroup.name, selectedDatasource)
            String subgrpGenderFlag = (selectedDatasource == Constants.DataSource.VAERS || selectedDatasource == Constants.DataSource.VIGIBASE) ? "false" : isSubgrpEnabled(Holders.config.subgrouping.gender.name, selectedDatasource)

            log.info("Reading the output.")
            def timeBefore = System.currentTimeMillis()
            String ebgmFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId?:execConfigId}/ebgm"
            String outputFilePath = "${ebgmFilePath}/${execConfigId}_Aggregate Case Alert"
            outputFilePath = outputFilePath + "_${selectedDatasource}"
            allFiles.add(outputFilePath)
            processStatsData_ebgm2(outputFilePath, execConfigId, subgrpAgeFlag, subgrpGenderFlag)
            def timeAfter = System.currentTimeMillis()
            status.status = 1
            log.info("Total time taken in reading and saving the file data is " + (timeAfter - timeBefore) / 1000 + " Secs.")
        } catch(Exception ex) {
            log.error("Error occured merging scores", ex)
        }
        status

    }

    def mergeStatsScores(String fileNameStr, String selectedDatasource = "pva",
                         Long execConfigId, List alertList, boolean isProductGroup,boolean isEventGroup, boolean dataMiningVariable = false,
                            Long masterConfigId = null, String productId = "") {

        def status = [:]
        def dataSourceObj = signalDataSourceService.getDataSource(selectedDatasource)
        Sql sql = new Sql(dataSourceObj)
        String fileNameOut=""

        try {
            String subgrpAgeFlag = (selectedDatasource == Constants.DataSource.VAERS || selectedDatasource == Constants.DataSource.VIGIBASE) ? "false" : isSubgrpEnabled(Holders.config.subgrouping.ageGroup.name, selectedDatasource)
            String subgrpGenderFlag = (selectedDatasource == Constants.DataSource.VAERS || selectedDatasource == Constants.DataSource.VIGIBASE) ? "false" : isSubgrpEnabled(Holders.config.subgrouping.gender.name, selectedDatasource)
            ExecutedConfiguration exConfig = ExecutedConfiguration.get(execConfigId)
            execConfigId = exConfig?.masterExConfigId ?: execConfigId
            if(reportExecutorService.checkPECsInDB(execConfigId, selectedDatasource)){
                log.info("Reading the output.")
                def timeBefore = System.currentTimeMillis()
                processStatsData_ebgm_for_other_datasource(sql, execConfigId, subgrpAgeFlag, subgrpGenderFlag)
                def timeAfter = System.currentTimeMillis()
                status.status = 1
                log.info("Total time taken in reading and saving the file data is "+ (timeAfter - timeBefore) / 1000 + " Secs.")
            } else {
                status.status = 1
                log.info("No ebgm data from stats.")
            }

        } catch(java.sql.SQLSyntaxErrorException sqe) {
            sqe.printStackTrace()
            log.info("Exception encountered while the statistics table thus statistics scores merging will not be calculated.");
        } catch(Throwable e) {
            e.printStackTrace()
            log.info("Exception encountered while the statistics table thus statistics scores merging will not be calculated.");
        } finally {
            boolean clearFiles = Holders.config.statistics.files.clear
            sql?.close()
            if (clearFiles) {
                try {
                    clearStatusFiles(fileNameStr, fileNameOut)
                } catch(Throwable th) {
                    log.error(th.getMessage())
                }
            }
        }
        status
    }

    def calculateStatisticalScores(String selectedDatasource = "pva",
                                   Long executedConfigId, Boolean isMaster=false) {

        def status = [:]

        try {
            status = callStatisticalApi(selectedDatasource,executedConfigId, isMaster)

        } catch(java.sql.SQLSyntaxErrorException sqe) {
            sqe.printStackTrace()
            log.info("Exception encountered while connecting the statistics table thus no statistics scores will be calculated.");
        } catch(Throwable e) {
            e.printStackTrace()
            log.info("Exception encountered while executing EBGM code thus no statistics scores will be calculated.");
        }
        log.info("Status coming from statistics api is : "+ status.status + ". O means failure | 1 means success.")
        log.info("Statistics block Execution finished.")
        status
    }

    def calculateStatisticalScoresPRR(String selectedDatasource = "pva",
                                   Long executedConfigId, Boolean isMaster=false) {

        def status = [:]

        try {
            status = callPRRStatisticalApi(selectedDatasource,executedConfigId, isMaster)

        } catch(java.sql.SQLSyntaxErrorException sqe) {
            sqe.printStackTrace()
            log.info("Exception encountered while connecting the statistics PRR table thus no PRR statistics scores will be calculated.");
        } catch(Throwable e) {
            e.printStackTrace()
            log.info("Exception encountered while executing PRR code thus no statistics scores will be calculated.");
        }
        log.info("Status coming from PRR statistics api is : "+ status.status + ". O means failure | 1 means success.")
        log.info("PRR Statistics block Execution finished.")
        status
    }
    def calculateStatisticalScoresROR(String selectedDatasource = "pva",
                                      Long executedConfigId, Boolean isMaster=false) {

        def status = [:]

        try {
            status = callRORStatisticalApi(selectedDatasource,executedConfigId, isMaster)

        } catch(java.sql.SQLSyntaxErrorException sqe) {
            sqe.printStackTrace()
            log.info("Exception encountered while connecting the statistics ROR table thus no ROR statistics scores will be calculated.");
        } catch(Throwable e) {
            e.printStackTrace()
            log.info("Exception encountered while executing ROR code thus no statistics scores will be calculated.");
        }
        log.info("Status coming from ROR statistics api is : "+ status.status + ". O means failure | 1 means success.")
        log.info("ROR Statistics block Execution finished.")
        status
    }

    private clearStatusFiles(fileNameStr, fileNameOut) {

        log.info("Now cleaning alert input file.")
        String alertFileName = fileNameStr +"_ALERT_INPUT.txt"
        File alertFile = new File(alertFileName)
        if (alertFile.exists()) {
            alertFile.delete()
        }
        log.info("Now cleaning stats output file.")
        String ebgmOutputFilePath = fileNameOut + "_STATS_OUTPUT_EBGM.csv"
        File outputFile1 = new File(ebgmOutputFilePath)
        if (outputFile1.exists()) {
            outputFile1.delete()
        }
    }

    private clearDssFiles(List<String> inputDssFiles, fileNameOut) {

        log.info("Now cleaning dss input file.")
        inputDssFiles.each { String inputDssFile->
            try {
                File alertFile = new File(inputDssFile)
                if (alertFile.exists()) {
                    alertFile.delete()
                    log.info("Deleted file "+inputDssFile)
                }
            } catch(Exception e){
                log.error("Some error occurred while deleting file "+ inputDssFile)
            }
        }
        log.info("Now cleaning dss output file.")
        String dssOutputFilePath = "${fileNameOut}_DSS_SCORE.csv"
        File outputFile1 = new File(dssOutputFilePath)
        if (outputFile1.exists()) {
            outputFile1.delete()
            log.info("Deleted file "+dssOutputFilePath)
        }
    }


    private Map callStatisticalApi(String dataSource, Long exConfigId, Boolean isMaster=false) {
        int status = 0

        def url = Holders.config.statistics.url
        def query = [data_source: dataSource, executable_config_id: exConfigId]
        if(isMaster) {
            query.put("products_to_filter", [])
        } else {
            query.put("products_to_filter", ["BS_PG"])
        }
        def path = Holders.config.statistics.path.algoscores
        log.info("Statistics URL : "+ url)
        log.info("Path is:"+ path)
        log.info("Query is:"+ query)
        def response = reportIntegrationService.postData(url, path, query, Method.POST)
        if (response.status == 200) {
           status = 1
        }
        [status:status, error: response.error]
    }
    private Map callPRRStatisticalApi(String dataSource, Long exConfigId, Boolean isMaster=false) {
        int status = 0

        def url = Holders.config.statistics.internal.url
        def query = [data_source: dataSource, executable_config_id: exConfigId]
        Map mlConfig = Holders.config.mlStats.prr.configurations?.clone() as Map
        String tempDir = Holders.config.tempDirectory
        mlConfig.put("TMP_DIR",tempDir)
        Map dbDetails = Holders.config.dataSources.pva
        Map dbInfo = [url:dbDetails.url,username:dbDetails.username,password:dbDetails.password]
        if(isMaster) {
            mlConfig.put("COMMON_COLS",['BASE_ID', 'MEDDRA_PT_CODE', 'BATCH_ID', 'CHILD_EXECUTION_ID'])
            query.put("products_to_filter", [])
        } else {
            mlConfig.put("COMMON_COLS",['BASE_ID', 'MEDDRA_PT_CODE', 'BATCH_ID'])
            query.put("products_to_filter", ["BS_PG"])
        }
        query.put("subgrp_columns",["PRR", "PRR_LCI", "PRR_UCI"])
        query.put("ml_config",mlConfig)
        query.put("score_type","PRR")
        def path = Holders.config.statistics.path.prrRorScores
        log.info("PRR Statistics URL : "+ url)
        log.info("Path is:"+ path)
        log.info("Query is:"+ query)
        query.put("db_details",dbInfo)
        def response = reportIntegrationService.postData(url, path, query, Method.POST)
        if (response.status == 200) {
            status = 1
        }
        [status:status, error: response.error]
    }
    private Map callRORStatisticalApi(String dataSource, Long exConfigId, Boolean isMaster=false) {
        int status = 0

        def url = Holders.config.statistics.internal.url
        def query = [data_source: dataSource, executable_config_id: exConfigId]
        Map mlConfig = Holders.config.mlStats.prr.configurations?.clone() as Map
        String tempDir = Holders.config.tempDirectory
        mlConfig.put("TMP_DIR",tempDir)
        Map dbDetails = Holders.config.dataSources.pva
        Map dbInfo = [url:dbDetails.url,username:dbDetails.username,password:dbDetails.password]
        boolean rorRelSubGrpEnabled = cacheService.getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
        if(isMaster) {
            mlConfig.put("COMMON_COLS",['BASE_ID', 'MEDDRA_PT_CODE', 'BATCH_ID', 'CHILD_EXECUTION_ID'])
            query.put("products_to_filter", [])
        } else {
            mlConfig.put("COMMON_COLS",['BASE_ID', 'MEDDRA_PT_CODE', 'BATCH_ID'])
            query.put("products_to_filter", ["BS_PG"])
        }
        if(rorRelSubGrpEnabled){
            query.put("subgrp_columns",["ROR", "ROR_LCI", "ROR_UCI","CHI_SQUARE","ROR_REL","ROR_LCI_REL","ROR_UCI_REL"])
        }else {
            query.put("subgrp_columns",["ROR", "ROR_LCI", "ROR_UCI","CHI_SQUARE"])
        }
        query.put("ml_config",mlConfig)
        query.put("score_type","ROR")
        def path = Holders.config.statistics.path.prrRorScores
        log.info("ROR Statistics URL : "+ url)
        log.info("Path is:"+ path)
        log.info("Query is:"+ query)
        query.put("db_details",dbInfo)
        def response = reportIntegrationService.postData(url, path, query, Method.POST)
        if (response.status == 200) {
            status = 1
        }
        [status:status, error: response.error]
    }

    private Map mergeStatsDataApi(String alertFileName, String fileNameStr, Long execConfigId, Long masterExConfigId, String productId) {

        int status = 0
        def url = Holders.config.statistics.url
        def query = [alert_input_filepath: alertFileName, executable_config_id:execConfigId]
        if(masterExConfigId) {
            query.put("products_to_filter", productId)
            query.put("master_id", masterExConfigId)
        } else {
            query.put("products_to_filter", ["BS_PG"])
            query.put("master_id", 0)
        }
        def path = Holders.config.statistics.path.algoscore.merge
        log.info("Statistics URL : "+ url)
        log.info("Path is:"+ path)
        log.info("Query is:"+ query)
        def response = reportIntegrationService.postData(url, path, query, Method.POST)
        if (response.status == 200) {
            status = 1
        }
        [status: status, error: response.error]
    }

    private processStatsData_ebgm(fileNameStr, executedConfigId, String subgrpAgeFlag, String subgrpGenderFlag) {
        def file = new File(fileNameStr)
        def ebgmStatScore = []
        def headers = []
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId as Long)
        file.readLines().eachWithIndex { row, rowIndex ->
            if (rowIndex == 0) {
                headers = row.split(',')
            }
            else{
                def tmpMap = [:]
                def cells = row.split(',').eachWithIndex { cell, cellIndex ->
                    if(cellIndex<headers.size()){
                        tmpMap[headers[cellIndex]] = cell
                    }
                }
                Map statsMap =[:]
                tmpMap.each{key, value ->
                    if( key == "BASE_ID" || key == "MEDDRA_PT_CODE"){
                        statsMap.put(key, value)
                    } else {
                        List keyList = key.split("_")
                        if(keyList.size()==1){
                            statsMap.put(key, value)
                        }
                        else {
                            String scoreValue = (value != -1)? value:0
                            if (statsMap.get(keyList[0])) {
                                Map subGroupMap = statsMap.get(keyList[0])
                                subGroupMap?.put(keyList[-1], scoreValue)
                                statsMap.put(keyList[0], subGroupMap)
                            } else {
                                statsMap.put(keyList[0], [(keyList[-1]) : scoreValue])
                            }
                        }
                    }
                }
                ebgmStatScore.add(statsMap)
            }
        }


        ebgmStatScore.forEach { it ->
            String productId = it.BASE_ID ? String.valueOf(it.BASE_ID) : ""
            String eventId = it.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(it.MEDDRA_PT_CODE)): ""

            Map statsProperties = [:]
            statsProperties.ebgm = it.EBGM
            statsProperties.eb05 = it.EB05
            statsProperties.eb95 = it.EB95
            dataObjectService.setStatsDataMap(executedConfigId, productId, eventId, statsProperties)
            if((subgrpAgeFlag.equals("true") || subgrpGenderFlag.equals("true")) && !executedConfiguration?.dataMiningVariable) {
                Map statsPropertiesSubGrouping = [:]
                statsPropertiesSubGrouping.ebgmAge = getMapString(it.get("EBGMAGE"))
                statsPropertiesSubGrouping.eb05Age = getMapString(it.get("EB05AGE"))
                statsPropertiesSubGrouping.eb95Age = getMapString(it.get("EB95AGE"))
                statsPropertiesSubGrouping.ebgmGender = getMapString(it.get("EBGMGENDER"))
                statsPropertiesSubGrouping.eb05Gender = getMapString(it.get("EB05GENDER"))
                statsPropertiesSubGrouping.eb95Gender = getMapString(it.get("EB95GENDER"))

                dataObjectService.setStatsDataMapSubgrouping(executedConfigId, productId, eventId, statsPropertiesSubGrouping)
            }

        }
    }

    private processStatsData_ebgm_for_other_datasource(sql, executedConfigId, String subgrpAgeFlag, String subgrpGenderFlag) {
        List<Map> ebgmData = []
        def ebgmStatScore = []

        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId as Long)

        sql.eachRow("select * FROM pvs_ebgm_output_$executedConfigId", []) { GroovyResultSet resultSet ->
            Map map = [:]
            resultSet.toRowResult().eachWithIndex { it, i ->
                def value = ""
                if (it.value instanceof Clob) {
                    //Handle Clob data
                    value = it.value.asciiStream.text
                } else {
                    value = it.value
                }
                map.put(it.key, value)
            }
            ebgmData.add(map)
        }

        ebgmData.each { tmpMap ->
            Map statsMap =[:]
            tmpMap.each{key, value ->
                if( key == "BASE_ID" || key == "MEDDRA_PT_CODE"){
                    statsMap.put(key, value)
                } else {
                    List keyList = key.split("_")
                    if(keyList.size()==1){
                        statsMap.put(key, value)
                    }
                    else {
                        String scoreValue = (value != -1)? value:0
                        if (statsMap.get(keyList[0])) {
                            Map subGroupMap = statsMap.get(keyList[0])
                            subGroupMap?.put(keyList[-1], scoreValue)
                            statsMap.put(keyList[0], subGroupMap)
                        } else {
                            statsMap.put(keyList[0], [(keyList[-1]) : scoreValue])
                        }
                    }
                }
            }
            ebgmStatScore.add(statsMap)
        }

        ebgmStatScore.forEach { it ->
            String productId = it.BASE_ID ? String.valueOf(it.BASE_ID) : ""
            String eventId = it.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(it.MEDDRA_PT_CODE)): ""

            Map statsProperties = [:]
            statsProperties.ebgm = it.EBGM
            statsProperties.eb05 = it.EB05
            statsProperties.eb95 = it.EB95
            dataObjectService.setStatsDataMap(executedConfigId, productId, eventId, statsProperties)
            if((subgrpAgeFlag.equals("true") || subgrpGenderFlag.equals("true")) && !executedConfiguration?.dataMiningVariable) {
                Map statsPropertiesSubGrouping = [:]
                statsPropertiesSubGrouping.ebgmAge = getMapString(it.get("EBGMAGE"))
                statsPropertiesSubGrouping.eb05Age = getMapString(it.get("EB05AGE"))
                statsPropertiesSubGrouping.eb95Age = getMapString(it.get("EB95AGE"))
                statsPropertiesSubGrouping.ebgmGender = getMapString(it.get("EBGMGENDER"))
                statsPropertiesSubGrouping.eb05Gender = getMapString(it.get("EB05GENDER"))
                statsPropertiesSubGrouping.eb95Gender = getMapString(it.get("EB95GENDER"))

                dataObjectService.setStatsDataMapSubgrouping(executedConfigId, productId, eventId, statsPropertiesSubGrouping)
            }

        }
    }

    String getMapString(Map statsMap){
        return statsMap? (statsMap?.toString() - '[')?.minus(']') : ""
    }

    private writeAlertFile(List resultData, File alertFile, boolean isProductGroup,boolean isEventGroup, boolean dataMiningVariable) {
        log.info("Writing alert file for alert execution.")
        def t1 = System.currentTimeMillis()
        resultData.each {
            String productId = ""
            if(isProductGroup && !dataMiningVariable){
                productId = "${it["PRODUCT_ID"]}-PG"
            } else {
                productId = it["PRODUCT_ID"]
            }

            String[] ptCodeWithSMQ = it["PT_CODE"] ? String.valueOf(it["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
            String ptCode = ptCodeWithSMQ ? ptCodeWithSMQ[0] ? ptCodeWithSMQ[0] : 0 : Constants.Commons.UNDEFINED_NUM
            String smqCode = ptCodeWithSMQ.size() > 1 ? ptCodeWithSMQ[1] : null
            String ptCodeEvntGrp = isEventGroup ? ptCode + '-EG' : ptCode
            alertFile << productId  + Constants.Commons.COMMA + ptCodeEvntGrp + (smqCode ? Constants.Commons.DASH_STRING + smqCode : "") + "\n"
        }

        def t2 = System.currentTimeMillis()
        log.info("The time taken to write the alert file is "+ (t2 - t1) / 1000 + " Secs. ")
    }


    private writeStatsFile(Sql sql, File statsFile, def executedConfigId) {
        log.info("Writing file for stats execution.")
        def resultList = []
        def t1 = System.currentTimeMillis()

        //Call the stats data stored proc with run id and batch size.
        String queryCalling = "{call p_send_stats_data_ebgm(?,?)}"
        sql.call(queryCalling, [executedConfigId, 10000])

        //Open the select result data
        sql.eachRow("select * FROM pvs_ebgm_full_data_$executedConfigId", []) { ResultSet resultSetObj->
            Clob clob =  resultSetObj.getClob("DT")
            if(clob){
                resultList.add(clob.getSubString(1,(int)clob.length()))
            }
        }
        def t2 = System.currentTimeMillis()
        log.info("The time taken to read is "+ (t2 - t1) / 1000 + " Secs. ")
        sql.close()

        def t11 = System.currentTimeMillis()
        log.info("The size of stats list is " + resultList.size())
        resultList.each {def row ->
            String[] singleList = row.split("\\n");
            int i = 0
            singleList.each {def str ->
                def replacedString = str.replace("<Q>", "")
                def newStringVal = replacedString.replace("</Q>", "")
                if (newStringVal) {
                   def newString = newStringVal.trim().substring(0, newStringVal.length() - 2)
                    statsFile << "$newString" + "\n"
                }
                i++
            }
        }
        def t22 = System.currentTimeMillis()
        log.info("The time taken to write the stats file is "+ (t22 - t11) / 1000 + " Secs. ")
    }
    def getStatsPrrRorDataForOtherDataSource(Sql sql, Long executedConfigId, Configuration config) {
        //Call the stats data stored proc with run id and batch size.

        // passing product list as null in parameter, db picking products from gtt
        String queryCalling = "{call p_send_stats_data_prr(?, ?)}"
        sql.call(queryCalling, [executedConfigId, null])

        //Open the select result data
        sql.eachRow("select * FROM pvs_prr_full_data_$executedConfigId", []) { row->
            Map statsProperties = [:]
            statsProperties.prrValue = row.NORMAL_PRR as Double
            statsProperties.prrUCI = row.NORMAL_UPPER_CI_PRR as Double
            statsProperties.prrLCI = row.NORMAL_LOWER_CI_PRR as Double
            statsProperties.prrStr = row.PRR
            statsProperties.prrStrLCI = row.LCI_PRR
            statsProperties.prrStrUCI = row.UCI_PRR
            statsProperties.prrMh = row.MH_PRR as Double
            statsProperties.rorValue = row.NORMAL_ROR as Double
            statsProperties.rorLCI = row.NORMAL_LOWER_95_CI_ROR as Double
            statsProperties.rorUCI = row.NORMAL_UPPER_95_CI_ROR as Double
            statsProperties.rorStr = row.ROR
            statsProperties.rorStrLCI = row.LCI_ROR
            statsProperties.rorStrUCI = row.UCI_ROR
            statsProperties.rorMh = row.MH_ROR as Double
            statsProperties.chiSquare = row.NORMAL_CHI_SQ as Double
            statsProperties.aValue = row.PRR_A as Double
            statsProperties.bValue = row.PRR_B as Double
            statsProperties.cValue = row.PRR_C as Double
            statsProperties.dValue = row.PRR_D as Double
            String productId = row.PRODUCT_NAME
            String eventId = row.PT_NAME ? String.valueOf(row.PT_NAME).replace('"', '') : ""
            dataObjectService.setProbDataMap(executedConfigId, productId, eventId, statsProperties)
        }
    }

    def getStatsPRRData(Sql sql, Long executedConfigId, Configuration config, boolean isEligibleForAwsExecution = false) {
        List<HashMap> prrData = []
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId as Long)
        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
        String tableName = "PRR_FULL_DATA_$executedConfigId"
        if(prrSubGrpEnabled && !executedConfiguration?.dataMiningVariable){
            tableName = "PVS_PRR_OUTPUT_$executedConfigId"
        }

        if (isEligibleForAwsExecution) {
            tableName = "glue_catalog.${Holders.config.aws.aggregate.datalake}.prr_full_data_${executedConfigId}"
        }

        prrData = aggregateExecutorService.getPrrRorData(sql, tableName, isEligibleForAwsExecution) as List<HashMap>

        prrData.each{ row->
            Map statsProperties = [:]
            statsProperties.prrValue = row.PRR as Double
            statsProperties.prrUCI = row.PRR_UCI as Double
            statsProperties.prrLCI = row.PRR_LCI as Double
            statsProperties.aValue = row.PRR_A as Double
            statsProperties.bValue = row.PRR_B as Double
            statsProperties.cValue = row.PRR_C as Double
            statsProperties.dValue = row.PRR_D as Double
            String productId = row.BASE_ID ? String.valueOf(row.BASE_ID) : ""
            if(executedConfiguration.dataMiningVariable && !executedConfiguration.isProductMining && (executedConfiguration.productGroupSelection || executedConfiguration.productSelection)){
                String batchId = row.BATCH_ID ? String.valueOf(row.BATCH_ID) : ""
                String[] splitBatchId = batchId?.split("_")
                productId = splitBatchId ? splitBatchId[-1] : productId
            }
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            dataObjectService.setProbDataMap(executedConfigId, productId, eventId, statsProperties)

            if(prrSubGrpEnabled && !executedConfiguration?.dataMiningVariable) {
                Map prrRorStatsSubGroupDataMap = [:]
                prrRorStatsSubGroupDataMap.prrSubGroup = prepareSubGroupDataMap(row, "PRR")
                prrRorStatsSubGroupDataMap.prrLciSubGroup = prepareSubGroupDataMap(row, "PRR_LCI")
                prrRorStatsSubGroupDataMap.prrUciSubGroup = prepareSubGroupDataMap(row, "PRR_UCI")
                dataObjectService.setPrrSubGroupDataMap(executedConfigId, productId, eventId, prrRorStatsSubGroupDataMap)
            }
        }
    }

    def getStatsRORData(Sql sql, Long executedConfigId, Configuration config, boolean isEligibleForAwsExecution = false) {
        List<HashMap> rorData = []
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId as Long)
        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
        Boolean isRor = cacheService.getRorCache()
        String tableName = "ROR_FULL_DATA_$executedConfigId"
        if(prrSubGrpEnabled && !executedConfiguration?.dataMiningVariable && isRor){
            tableName = "PVS_ROR_OUTPUT_$executedConfigId"
        }
        if (isEligibleForAwsExecution) {
            tableName = "glue_catalog.${Holders.config.aws.aggregate.datalake}.ror_full_data_${executedConfigId}"
        }

        rorData = aggregateExecutorService.getPrrRorData(sql, tableName, isEligibleForAwsExecution) as List<HashMap>

        rorData?.each { Map row->
            Map statsProperties = [:]
            statsProperties.rorValue = row.ROR as Double
            statsProperties.rorUCI = row.ROR_UCI as Double
            statsProperties.rorLCI = row.ROR_LCI as Double
            statsProperties.chiSquare = row.CHI_SQUARE as Double
            String productId = row.BASE_ID ? String.valueOf(row.BASE_ID) : ""
            if(executedConfiguration.dataMiningVariable && !executedConfiguration.isProductMining && (executedConfiguration.productGroupSelection || executedConfiguration.productSelection)){
                String batchId = row.BATCH_ID ? String.valueOf(row.BATCH_ID) : ""
                String[] splitBatchId = batchId?.split("_")
                productId = splitBatchId ? splitBatchId[-1] : productId
            }
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            dataObjectService.setRorProbDataMap(executedConfigId, productId, eventId, statsProperties)

            if(prrSubGrpEnabled && !executedConfiguration?.dataMiningVariable && isRor) {
                Map prrRorStatsSubGroupDataMap = [:]
                boolean rorRelSubGrpEnabled = cacheService.getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
                prrRorStatsSubGroupDataMap.rorSubGroup = prepareSubGroupDataMap(row, "ROR")
                prrRorStatsSubGroupDataMap.rorLciSubGroup = prepareSubGroupDataMap(row, "ROR_LCI")
                prrRorStatsSubGroupDataMap.rorUciSubGroup = prepareSubGroupDataMap(row, "ROR_UCI")
                prrRorStatsSubGroupDataMap.chiSquareSubGroup = prepareSubGroupDataMap(row, "Chi-Square")
                if(rorRelSubGrpEnabled) {
                    prrRorStatsSubGroupDataMap.rorRelSubGroup = prepareSubGroupDataMap(row, "ROR_REL")
                    prrRorStatsSubGroupDataMap.rorLciRelSubGroup = prepareSubGroupDataMap(row, "ROR_LCI_REL")
                    prrRorStatsSubGroupDataMap.rorUciRelSubGroup = prepareSubGroupDataMap(row, "ROR_UCI_REL")
                }
                dataObjectService.setRorSubGroupDataMap(executedConfigId, productId, eventId, prrRorStatsSubGroupDataMap)
            }
        }
    }

    String prepareSubGroupDataMap(Map row, String category) {
        String resultString
        try {
            Map allSubGroupColumnMap = cacheService.getAllOtherSubGroupColumns(Constants.DataSource.PVA)
            if (allSubGroupColumnMap) {
                Map columnInfoMap = allSubGroupColumnMap.get(category)
                if(category == "ROR_REL" || category == "ROR_LCI_REL" || category == "ROR_UCI_REL"){
                    String relCategory = category - "_REL"
                    columnInfoMap = allSubGroupColumnMap.get(relCategory)
                }
                Map allDataMap = [:]
                columnInfoMap?.each { key, valueList ->
                    Map subGroupMap = [:]
                    valueList?.each { it ->
                        if(category == "Chi-Square"){
                            category = "CHI_SQUARE"
                        }
                        String columnName = category + key + "_" + it
                        def columnValue = row[columnName]
                        if (columnValue && columnValue != -1) {
                            subGroupMap.put(it, columnValue as Double)
                        } else {
                            subGroupMap.put(it, 0.0)
                        }
                    }
                    allDataMap.put(key, subGroupMap)
                }
                resultString = allDataMap ? allDataMap as JSON : ""
            }
        } catch (Exception ex) {
            resultString = ""
        }
        return resultString
    }

    def getStatsABCDData(Sql sql, Long executedConfigId, Configuration config){
        String queryCalling = "{call p_create_ebgm_final_table(?)}"
        sql.call(queryCalling, [executedConfigId])

        //Open the select result data
        sql.eachRow("select * FROM pvs_ebgm_final_$executedConfigId", []) { row->
            //handle SMQ
            String productId = row.BASE_ID ? String.valueOf(row.BASE_ID) : ""
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            // get previous map of stat properties
            Map statsProperties = dataObjectService.getProbDataMap(executedConfigId, productId, eventId)
            // if prr and ror are disabled then ebgm value wont be stored
            if(statsProperties!=null){
                statsProperties.aValue = row.EBGM_A as Double
                statsProperties.bValue = row.EBGM_B as Double
                statsProperties.cValue = row.EBGM_C as Double
                statsProperties.dValue = row.EBGM_D as Double
                statsProperties.eValue = row.E as Double
                statsProperties.rrValue = row.RR as Double
                dataObjectService.setProbDataMap(executedConfigId, productId, eventId, statsProperties)
            }else{
                statsProperties = [:]
                statsProperties.aValue = row.EBGM_A as Double
                statsProperties.bValue = row.EBGM_B as Double
                statsProperties.cValue = row.EBGM_C as Double
                statsProperties.dValue = row.EBGM_D as Double
                statsProperties.eValue = row.E as Double
                statsProperties.rrValue = row.RR as Double
                dataObjectService.setProbDataMap(executedConfigId, productId, eventId, statsProperties)
            }
        }
        sql.close()
    }

    def getStatsEbgmData(Sql sql, Long executedConfigId, String selectedDatasource) {
        //Open the select result data
        List<Map> ebgmData = []
        def ebgmStatScore = []
        String subgrpAgeFlag = selectedDatasource == Constants.DataSource.VAERS ? "false" : isSubgrpEnabled(Holders.config.subgrouping.ageGroup.name, selectedDatasource)
        String subgrpGenderFlag = selectedDatasource == Constants.DataSource.VAERS ? "false" : isSubgrpEnabled(Holders.config.subgrouping.gender.name, selectedDatasource)

        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId as Long)

        sql.eachRow("select * FROM pvs_ebgm_output_$executedConfigId", []) { GroovyResultSet resultSet ->
            Map map = [:]
            resultSet.toRowResult().eachWithIndex { it, i ->
                def value = ""
                if (it.value instanceof Clob) {
                    //Handle Clob data
                    value = it.value.asciiStream.text
                } else {
                    value = it.value
                }
                map.put(it.key, value)
            }
            ebgmData.add(map)
        }


        ebgmData.each { tmpMap ->
            Map statsMap = [:]
            tmpMap.each { key, value ->
                if (key == "BASE_ID" || key == "MEDDRA_PT_CODE" || key == "BATCH_ID") {
                    statsMap.put(key, value)
                } else {
                    List keyList = key.split("_")
                    if (keyList.size() == 1) {
                        statsMap.put(key, value)
                    } else {
                        Double scoreValue = (value != -1) ? (value as Double) : 0.0
                        if (keyList[0] == "EBGMGENDER" || keyList[0] == "EBGMAGE" || keyList[0] == "EB05GENDER" || keyList[0] == "EB05AGE"
                                || keyList[0] == "EB95GENDER" || keyList[0] == "EB95AGE") {
                            if (statsMap.get(keyList[0])) {
                                Map subGroupMap = statsMap.get(keyList[0])
                                subGroupMap?.put(keyList[-1], scoreValue)
                                statsMap.put(keyList[0], subGroupMap)
                            } else {
                                statsMap.put(keyList[0], [(keyList[-1]): scoreValue])
                            }
                        } else {
                            int lastIndex = key.lastIndexOf("_")
                            String newKey = key.substring(0, lastIndex)
                            String subGroup = ""
                            String statsMapKey = ""
                            if (newKey.startsWith("EBGM")) {
                                subGroup = newKey - "EBGM"
                                statsMapKey = "EBGMSUBGROUP"
                            } else if (newKey.startsWith("EB05")) {
                                subGroup = newKey - "EB05"
                                statsMapKey = "EB05SUBGROUP"
                            } else if (newKey.startsWith("EB95")) {
                                subGroup = newKey - "EB95"
                                statsMapKey = "EB95SUBGROUP"
                            }
                            if (statsMap.get(statsMapKey)) {
                                Map ebgmSubGroup = statsMap.get(statsMapKey)
                                if (ebgmSubGroup.get(subGroup)) {
                                    Map subGroupMap = ebgmSubGroup.get(subGroup)
                                    subGroupMap.put((keyList[-1]), scoreValue)
                                } else {
                                    Map newScoreMap = [(keyList[-1]): scoreValue]
                                    ebgmSubGroup.put(subGroup, newScoreMap)
                                }
                            } else {
                                Map ebgmSubGroup = [:]
                                ebgmSubGroup[subGroup] = [(keyList[-1]): scoreValue]
                                statsMap.put(statsMapKey, ebgmSubGroup)
                            }
                        }
                    }
                }
            }
        ebgmStatScore.add(statsMap)
    }

        ebgmStatScore.forEach { it ->
            String productId = it.BASE_ID ? String.valueOf(it.BASE_ID) : ""
            String eventId = it.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(it.MEDDRA_PT_CODE)): ""

            Map statsProperties = [:]
            statsProperties.ebgm = it.EBGM
            statsProperties.eb05 = it.EB05
            statsProperties.eb95 = it.EB95
            dataObjectService.setStatsDataMap(executedConfigId, productId, eventId, statsProperties)
            if((subgrpAgeFlag.equals("true") || subgrpGenderFlag.equals("true")) && !executedConfiguration?.dataMiningVariable) {
                Map statsPropertiesSubGrouping = [:]
                statsPropertiesSubGrouping.ebgmAge = getMapString(it.get("EBGMAGE"))
                statsPropertiesSubGrouping.eb05Age = getMapString(it.get("EB05AGE"))
                statsPropertiesSubGrouping.eb95Age = getMapString(it.get("EB95AGE"))
                statsPropertiesSubGrouping.ebgmGender = getMapString(it.get("EBGMGENDER"))
                statsPropertiesSubGrouping.eb05Gender = getMapString(it.get("EB05GENDER"))
                statsPropertiesSubGrouping.eb95Gender = getMapString(it.get("EB95GENDER"))

                dataObjectService.setStatsDataMapSubgrouping(executedConfigId, productId, eventId, statsPropertiesSubGrouping)
            }
            if(selectedDatasource == Constants.DataSource.PVA && !executedConfiguration?.dataMiningVariable){
                Map ebgmStatsPropertiesSubGrouping = [:]
                ebgmStatsPropertiesSubGrouping.ebgmSubGroup = it.get("EBGMSUBGROUP") ? it.get("EBGMSUBGROUP") as JSON : ""
                ebgmStatsPropertiesSubGrouping.eb05SubGroup = it.get("EB05SUBGROUP") ? it.get("EB05SUBGROUP") as JSON : ""
                ebgmStatsPropertiesSubGrouping.eb95SubGroup = it.get("EB95SUBGROUP") ? it.get("EB95SUBGROUP") as JSON : ""

                dataObjectService.setEbgmStatsDataMapSubgrouping(executedConfigId, productId, eventId, ebgmStatsPropertiesSubGrouping)
            }

        }
    }


    private String isSubgrpEnabled(String subgroup, String selectedDatasource){
        if(selectedDatasource == Constants.DataSource.FAERS) {
            subgroup = subgroup + "_" + Constants.DataSource.DATASOURCE_FAERS
        }
        cacheService.getSubgrpColumns(subgroup)? "true": "false"
    }

    def setDefaultSubGroupValues(def aca, boolean isDataMiningVariable){
        Boolean isRor = cacheService.getRorCache()
        if(!isDataMiningVariable) {
            aca.prrSubGroup = prepareSubGroupDataMap([:], "PRR")
            aca.prrLciSubGroup = prepareSubGroupDataMap([:], "PRR_LCI")
            aca.prrUciSubGroup = prepareSubGroupDataMap([:], "PRR_UCI")
            if(isRor) {
                aca.rorSubGroup = prepareSubGroupDataMap([:], "ROR")
                aca.rorLciSubGroup = prepareSubGroupDataMap([:], "ROR_LCI")
                aca.rorUciSubGroup = prepareSubGroupDataMap([:], "ROR_UCI")
                aca.chiSquareSubGroup = prepareSubGroupDataMap([:], "Chi-Square")
                boolean rorRelSubGrpEnabled = cacheService.getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
                if (rorRelSubGrpEnabled) {
                    aca.rorRelSubGroup = prepareSubGroupDataMap([:], "ROR_REL")
                    aca.rorLciRelSubGroup = prepareSubGroupDataMap([:], "ROR_LCI_REL")
                    aca.rorUciRelSubGroup = prepareSubGroupDataMap([:], "ROR_UCI_REL")
                }
            }
        }

        aca.ebgmSubGroup = prepareSubGroupDataMap([:], "EBGM")
        aca.eb05SubGroup = prepareSubGroupDataMap([:], "EB05")
        aca.eb95SubGroup = prepareSubGroupDataMap([:], "EB95")
        Map oldEbgmSubGroup = cacheService.getSubGroupMap()
        Map ebgmAge = [:]
        Map ebgmGender = [:]
        Map eb05Age = [:]
        Map eb05Gender = [:]
        Map eb95Age = [:]
        Map eb95Gender = [:]
        oldEbgmSubGroup[Holders.config.subgrouping.ageGroup.name]?.each { id, value ->
            ebgmAge.put(value,0.0)
            eb05Age.put(value,0.0)
            eb95Age.put(value,0.0)
        }
        oldEbgmSubGroup[Holders.config.subgrouping.gender.name]?.each { id, value ->
            ebgmGender.put(value,0.0)
            eb05Gender.put(value,0.0)
            eb95Gender.put(value,0.0)
        }
        aca.ebgmAge = getMapString(ebgmAge)
        aca.ebgmGender = getMapString(ebgmGender)
        aca.eb05Age = getMapString(eb05Age)
        aca.eb05Gender = getMapString(eb05Gender)
        aca.eb95Age = getMapString(eb95Age)
        aca.eb95Gender = getMapString(eb95Gender)
    }

    String processMetaJsonFile(String fileNameMetaJson) {
        String deactivatedNodes
        try {
            File file = new File(fileNameMetaJson)
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file))
            String row
            while ((row = bufferedReader.readLine()) != null) {
                deactivatedNodes = new JsonBuilder(JSON.parse(row)?.ALL_DEACTIVATED_LABELS)?.toString()
            }
        } catch(Exception e){
            log.error("Error occurred while reading ${fileNameMetaJson} file")
            e.printStackTrace()
        }
        deactivatedNodes
    }
    private processDssScoresData(outputFileName, executedConfigId) {
        File file = new File(outputFileName)

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file))
        String row;
        Map dssDataProperties
        while ((row = bufferedReader.readLine()) != null) {
            String[] str = row.split("\\|")
            dssDataProperties = [:]
            dssDataProperties.dssScore = str[2]
            dssDataProperties.pecImpNumHigh = Double.parseDouble(str[3])
            dssDataProperties.pecImpNumLow = Double.parseDouble(str[4])
            String productName = str[0]
            String eventName = str[1] ? String.valueOf(str[1]).replace('"', '') : ""
            dataObjectService.setDssScoreDataMap(executedConfigId, productName, eventName, dssDataProperties)
        }
        dssDataProperties = null
        bufferedReader.close()
    }

    private processDssScoresData2(fileNameStr, executedConfigId) {
        List<Map> alertDataList = []
        try {
            alertDataList = alertService.loadAlertDataFromFile(fileNameStr)
        } catch (Exception ex) {
            log.error("File not found: " + ex.printStackTrace())
        }
        for(Map row: alertDataList) {
            Map dssDataProperties = [:]
            dssDataProperties.dssScore = row.DSS_SCORE
            dssDataProperties.pecImpNumHigh = Double.parseDouble(row.PEC_IMP_POS?row.PEC_IMP_POS as String:"0")
            dssDataProperties.pecImpNumLow = Double.parseDouble(row.PEC_IMP_POS?row.PEC_IMP_NEG as String:"0")
            String productName = row.PRODUCT_NAME
            String eventName = row.PT ? String.valueOf(row.PT).replace('"', '') : ""
            dataObjectService.setDssScoreDataMap(executedConfigId, productName, eventName, dssDataProperties)
        }
        alertDataList = []

    }

    void mergeDSSScores(String fileNameStr, Long executedConfigurationId, Long masterConfigId, String fileNameMetaJson = null) {
        try {
            log.info("Reading the output.")
            def timeBefore = System.currentTimeMillis()
            processDssScoresData(fileNameStr, executedConfigurationId)
            def timeAfter = System.currentTimeMillis()
            log.info("Total time taken in reading and saving the file data is "+ (timeAfter - timeBefore) / 1000 + " Secs.")
        }catch(Throwable e) {
            e.printStackTrace()
            log.info("Exception encountered while the statistics table thus statistics scores merging will not be calculated. --- DSS Module");
        } finally {
            boolean clearFiles = Holders.config.statistics.files.clear
            if (clearFiles && !masterConfigId) {
                try {
                    String path = (Holders.config.statistics.inputfile.path as String)
                    String alertFileName = path +"DSS_REQUEST_${executedConfigurationId}.txt"

                    clearDssFiles([alertFileName, fileNameMetaJson], fileNameStr)
                } catch(Throwable th) {
                    log.error(th.getMessage())
                }
            }
        }
    }

    void mergeDSSScores2(Long executedConfigurationId, Long masterExecId, String selectedDatasource, List<String> allFiles) {
        try {
            log.info("Reading the output.")
            String dssFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecId}/dss"
            String outputFilePath = "${dssFilePath}/${executedConfigurationId}_Aggregate Case Alert"
            outputFilePath = outputFilePath + "_${selectedDatasource}"
            def timeBefore = System.currentTimeMillis()
            allFiles.add(outputFilePath)
            processDssScoresData(outputFilePath, executedConfigurationId)
            def timeAfter = System.currentTimeMillis()
            log.info("Total time taken in reading and saving the file data is "+ (timeAfter - timeBefore) / 1000 + " Secs.")
        }catch(Throwable e) {
            e.printStackTrace()
            log.info("Exception encountered while the statistics table thus statistics scores merging will not be calculated. --- DSS Module");
        } finally {

        }
    }


    void sendApiCallMasterDssScores(Long executedConfigurationId, List prevAlertIds, Boolean smqFlag){
        if(executedConfigurationId) {
            String url = Holders.config.dss.scores.url
            String uriPath = Holders.config.dss.scores.path
            Map query = [alert_id: executedConfigurationId as Long, prev_alert_ids: prevAlertIds, is_master: 1]

            log.info("Starting the API call for fetching DSS Scores...")
            try {
                log.info("DSS Request - "+ query)
                Map ret = reportIntegrationService.postData(url, uriPath, query, Method.POST)
                if (ret.status == 200) {
                    log.info("API call returned with status: ${ret.status}")
                } else {
                    log.info("API call failed with status: ${ret?.status}")
                }
            } catch (Throwable th) {
                throw th
            }
        }
    }

    void sendApiCallForDssScores2(Long executedConfigurationId, List prevIds=[]){
        List latestExecConfigId = []
        if(prevIds){
            latestExecConfigId = [prevIds.sort().reverse()[0]]
        }
        if(executedConfigurationId) {
            String url = Holders.config.dss.scores.url
            String uriPath = Holders.config.dss.scores.path
            Map query = [alert_id: executedConfigurationId as Long, prev_alert_ids: latestExecConfigId, is_master: 0]
            log.info("Starting the API call for fetching DSS Scores...")
            try {
                log.info("DSS Request - "+ query)
                Map ret = reportIntegrationService.postData(url, uriPath, query, Method.POST)
                if (ret.status == 200) {
                    log.info("API call returned with status: ${ret.status}")
                } else {
                    log.info("API call failed with status: ${ret?.status}")
                    throw new Exception("API call failed with status: ${ret?.status}")
                }
            } catch (Throwable th) {
                log.error(th.printStackTrace())
                throw th
            }
        }
    }


    private processStatsData_ebgm2(fileNameStr, executedConfigId, String subgrpAgeFlag, String subgrpGenderFlag) {
        List<Map> alertDataList = []
        try {
            alertDataList = alertService.loadAlertDataFromFile(fileNameStr)
        } catch (Exception ex) {
            log.error("File not found: " + ex.printStackTrace())
        }
        def ebgmStatScore = []
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId as Long)
        Map statsMap
        alertDataList.each { tmpMap ->
            statsMap =[:]
            tmpMap.each{key, value ->
                if( key == "BASE_ID" || key == "MEDDRA_PT_CODE" || key == "BATCH_ID"){
                    statsMap.put(key, value)
                } else {
                    List keyList = key.split("_")
                    if (keyList.size() == 1) {
                        statsMap.put(key, value)
                    } else {
                        Double scoreValue = (value != -1) ? (value as Double) : 0
                        if (keyList[0] == "EBGMGENDER" || keyList[0] == "EBGMAGE" || keyList[0] == "EB05GENDER" || keyList[0] == "EB05AGE"
                                || keyList[0] == "EB95GENDER" || keyList[0] == "EB95AGE") {
                            if (statsMap.get(keyList[0])) {
                                Map subGroupMap = statsMap.get(keyList[0])
                                subGroupMap?.put(keyList[-1], scoreValue)
                                statsMap.put(keyList[0], subGroupMap)
                            } else {
                                statsMap.put(keyList[0], [(keyList[-1]): scoreValue])
                            }
                        } else {
                            int lastIndex = key.lastIndexOf("_")
                            String newKey = key.substring(0, lastIndex)
                            String subGroup = ""
                            String statsMapKey = ""
                            if (newKey.startsWith("EBGM")) {
                                subGroup = newKey - "EBGM"
                                statsMapKey = "EBGMSUBGROUP"
                            } else if (newKey.startsWith("EB05")) {
                                subGroup = newKey - "EB05"
                                statsMapKey = "EB05SUBGROUP"
                            } else if (newKey.startsWith("EB95")) {
                                subGroup = newKey - "EB95"
                                statsMapKey = "EB95SUBGROUP"
                            }
                            if (statsMap.get(statsMapKey)) {
                                Map ebgmSubGroup = statsMap.get(statsMapKey)
                                if (ebgmSubGroup.get(subGroup)) {
                                    Map subGroupMap = ebgmSubGroup.get(subGroup)
                                    subGroupMap.put((keyList[-1]), scoreValue)
                                } else {
                                    Map newScoreMap = [(keyList[-1]): scoreValue]
                                    ebgmSubGroup.put(subGroup, newScoreMap)
                                }
                            } else {
                                Map ebgmSubGroup = [:]
                                ebgmSubGroup[subGroup] = [(keyList[-1]): scoreValue]
                                statsMap.put(statsMapKey, ebgmSubGroup)
                            }
                        }
                    }
                }
            }
            ebgmStatScore.add(statsMap)
        }
        statsMap = null
        Map statsProperties
        ebgmStatScore.forEach { it ->
            String productId = it.BASE_ID ? String.valueOf(it.BASE_ID) : ""
            String eventId = it.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(it.MEDDRA_PT_CODE)): ""

            statsProperties = [:]
            statsProperties.ebgm = it.EBGM
            statsProperties.eb05 = it.EB05
            statsProperties.eb95 = it.EB95
            dataObjectService.setStatsDataMap(executedConfigId, productId, eventId, statsProperties)
            if((subgrpAgeFlag.equals("true") || subgrpGenderFlag.equals("true")) && !executedConfiguration?.dataMiningVariable) {
                Map statsPropertiesSubGrouping = [:]
                statsPropertiesSubGrouping.ebgmAge = getMapString(it.get("EBGMAGE"))
                statsPropertiesSubGrouping.eb05Age = getMapString(it.get("EB05AGE"))
                statsPropertiesSubGrouping.eb95Age = getMapString(it.get("EB95AGE"))
                statsPropertiesSubGrouping.ebgmGender = getMapString(it.get("EBGMGENDER"))
                statsPropertiesSubGrouping.eb05Gender = getMapString(it.get("EB05GENDER"))
                statsPropertiesSubGrouping.eb95Gender = getMapString(it.get("EB95GENDER"))

                dataObjectService.setStatsDataMapSubgrouping(executedConfigId, productId, eventId, statsPropertiesSubGrouping)
            }
            if(executedConfiguration?.selectedDatasource == Constants.DataSource.PVA && !executedConfiguration?.dataMiningVariable){
                Map ebgmStatsPropertiesSubGrouping = [:]
                ebgmStatsPropertiesSubGrouping.ebgmSubGroup = it.get("EBGMSUBGROUP") ? it.get("EBGMSUBGROUP") as JSON : ""
                ebgmStatsPropertiesSubGrouping.eb05SubGroup = it.get("EB05SUBGROUP") ? it.get("EB05SUBGROUP") as JSON : ""
                ebgmStatsPropertiesSubGrouping.eb95SubGroup = it.get("EB95SUBGROUP") ? it.get("EB95SUBGROUP") as JSON : ""

                dataObjectService.setEbgmStatsDataMapSubgrouping(executedConfigId, productId, eventId, ebgmStatsPropertiesSubGrouping)
            }

        }
        statsProperties = null
        alertDataList = []
    }

    private processStatsData_ebgm3(fileNameStr, executedConfigId, String subgrpAgeFlag, String subgrpGenderFlag) {
        List<Map> alertDataList = []
        def ebgmStatScore = []
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId as Long)
        alertDataList.each { tmpMap ->
            Map statsMap =[:]
            tmpMap.each{key, value ->
                if( key == "BASE_ID" || key == "MEDDRA_PT_CODE"){
                    statsMap.put(key, value)
                } else {
                    List keyList = key.split("_")
                    if(keyList.size()==1){
                        statsMap.put(key, value)
                    }
                    else {
                        String scoreValue = (value != -1)? value:0
                        if (statsMap.get(keyList[0])) {
                            Map subGroupMap = statsMap.get(keyList[0])
                            subGroupMap?.put(keyList[-1], scoreValue)
                            statsMap.put(keyList[0], subGroupMap)
                        } else {
                            statsMap.put(keyList[0], [(keyList[-1]) : scoreValue])
                        }
                    }
                }
            }
            ebgmStatScore.add(statsMap)
        }
        ebgmStatScore.forEach { it ->
            String productId = it.BASE_ID ? String.valueOf(it.BASE_ID) : ""
            String eventId = it.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(it.MEDDRA_PT_CODE)): ""

            Map statsProperties = [:]
            statsProperties.ebgm = it.EBGM
            statsProperties.eb05 = it.EB05
            statsProperties.eb95 = it.EB95
            dataObjectService.setStatsDataMap(executedConfigId, productId, eventId, statsProperties)
            if((subgrpAgeFlag.equals("true") || subgrpGenderFlag.equals("true")) && !executedConfiguration?.dataMiningVariable) {
                Map statsPropertiesSubGrouping = [:]
                statsPropertiesSubGrouping.ebgmAge = getMapString(it.get("EBGMAGE"))
                statsPropertiesSubGrouping.eb05Age = getMapString(it.get("EB05AGE"))
                statsPropertiesSubGrouping.eb95Age = getMapString(it.get("EB95AGE"))
                statsPropertiesSubGrouping.ebgmGender = getMapString(it.get("EBGMGENDER"))
                statsPropertiesSubGrouping.eb05Gender = getMapString(it.get("EB05GENDER"))
                statsPropertiesSubGrouping.eb95Gender = getMapString(it.get("EB95GENDER"))

                dataObjectService.setStatsDataMapSubgrouping(executedConfigId, productId, eventId, statsPropertiesSubGrouping)
            }

        }
    }

    def getStatsDssData(Sql sql, Long executedConfigId, String selectedDatasource) {
        //Open the select result data
        List<Map> dssData = []

        sql.eachRow("select * FROM pvs_dss_results_$executedConfigId", []) { GroovyResultSet resultSet ->
            Map map = [:]
            resultSet.toRowResult().eachWithIndex { it, i ->
                def value = ""
                if (it.value instanceof Clob) {
                    //Handle Clob data
                    value = it.value.asciiStream.text
                } else {
                    value = it.value
                }
                map.put(it.key, value)
            }
            dssData.add(map)
        }

        String deactivatedNodes
        sql.eachRow("select * FROM PVS_DSS_META_INFO_$executedConfigId", []) { GroovyResultSet resultSet ->
            deactivatedNodes = new JsonBuilder(JSON.parse(resultSet.getString("META_INFO"))?.ALL_DEACTIVATED_LABELS)?.toString()
        }

        if(deactivatedNodes) {
            dataObjectService.setDssMetaDataMap(executedConfigId, deactivatedNodes)
        }


        for(Map row: dssData) {
            Map dssDataProperties = [:]
            dssDataProperties.dssScore = row.DSS_SCORE
            dssDataProperties.pecImpNumHigh = Double.parseDouble(row.PEC_IMP_POS?row.PEC_IMP_POS as String:"0")
            dssDataProperties.pecImpNumLow = Double.parseDouble(row.PEC_IMP_POS?row.PEC_IMP_NEG as String:"0")
            String productName = row.PRODUCT_NAME
            String eventName = row.PT ? String.valueOf(row.PT).replace('"', '') : ""
            dataObjectService.setDssScoreDataMap(executedConfigId, productName, eventName, dssDataProperties)
        }


    }


    private processDssScoresData3(fileNameStr, executedConfigId) {
        List<Map> alertDataList = []
        try {
            alertDataList = alertService.loadAlertDataFromFile(fileNameStr)
        } catch (Exception ex) {
            log.error("File not found: " + ex.printStackTrace())
        }
        for(Map row: alertDataList) {
            Map dssDataProperties = [:]
            dssDataProperties.dssScore = row.DSS_SCORE
            dssDataProperties.pecImpNumHigh = Double.parseDouble(row.PEC_IMP_POS?row.PEC_IMP_POS as String:"0")
            dssDataProperties.pecImpNumLow = Double.parseDouble(row.PEC_IMP_POS?row.PEC_IMP_NEG as String:"0")
            String productName = row.PRODUCT_NAME
            String eventName = row.PT ? String.valueOf(row.PT).replace('"', '') : ""
            dataObjectService.setDssScoreDataMap(executedConfigId, productName, eventName, dssDataProperties)
        }

    }

    def getPrrRorDataForHierarchyAlerts() {

    }


}
