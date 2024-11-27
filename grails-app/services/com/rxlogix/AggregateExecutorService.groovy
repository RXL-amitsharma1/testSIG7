package com.rxlogix

import com.hazelcast.core.HazelcastInstance
import com.rxlogix.cache.CacheService
import com.rxlogix.config.AlertFieldService
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EtlStatusEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.json.JsonOutput
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import org.apache.spark.sql.SparkSession
import software.amazon.awssdk.services.s3.model.GetObjectResponse

import java.sql.Clob
import java.util.concurrent.ArrayBlockingQueue

@Transactional
class AggregateExecutorService implements AlertUtil{

    HazelcastInstance hazelcastInstance
    ReportExecutorService reportExecutorService
    CacheService cacheService
    DataObjectService dataObjectService
    SqlGenerationService sqlGenerationService
    AlertFieldService alertFieldService
    AlertService alertService

    public Queue<Long> configurationQueue = new ArrayBlockingQueue<>(Holders.config.aws.aggregate.execution.queue.size as int)
    public static String SCHEDULED_CONFIGURATION_FOR_EXECUTION = "scheduledConfigurationsForExecution"

    void initiateMovingAggConfigToQueue() {
        List<Configuration> configurationList = Configuration.getNextConfigurationToExecute(reportExecutorService.currentlyQuantRunning, 'pva', Constants.AlertConfigType.AGGREGATE_CASE_ALERT, true) as List<Configuration>
        if (configurationList?.size() > 0) {
            Map statusMap = fetchDataLoadStatus()
            switch (statusMap.status) {
                case EtlStatusEnum.NOT_STARTED:
                    triggerDataLoad()
                    break
                case EtlStatusEnum.RUNNING:
                    moveScheduledAlertToQueue(configurationList)
                    break
                case EtlStatusEnum.SUCCESS:
                    initiateQueuedAlertToProgress()
                    hazelcastInstance.getList(SCHEDULED_CONFIGURATION_FOR_EXECUTION)?.clear()
                    triggerDataLoad()
                    break
            }
        }
    }

    void fetchDataLoadStatus() {
    }

    void triggerDataLoad() {
    }

    void moveScheduledAlertToQueue(List configurationList) {
        configurationList.each{ configuration ->
            if (hazelcastInstance.getSet(SCHEDULED_CONFIGURATION_FOR_EXECUTION).size() >= Holders.config.aws.aggregate.execution.queue.size as int)
                return
            hazelcastInstance.getSet(SCHEDULED_CONFIGURATION_FOR_EXECUTION).add(configuration.id)
        }
    }

    void initiateQueuedAlertToProgress() {
        Set<Long> scheduledConfigurationList = hazelcastInstance.getSet(SCHEDULED_CONFIGURATION_FOR_EXECUTION)
        scheduledConfigurationList?.each { configurationId ->
            configurationQueue.offer(configurationId)
        }
    }

    boolean isConfigurationReadyForAWS(Configuration configuration) {
        return Holders.config.aws.aggregate.execution.enabled && (configuration.dateRangeType == DateRangeTypeCaseEnum.CASE_RECEIPT_DATE) && (configuration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) && configuration.includeLockedVersion && !(configuration.alertForegroundQueryId || configuration.dataMiningVariable || configuration.includeWHODrugs || configuration.studyCases || Holders.config.signal.sieveAnalysis.safetyDB || Holders.config.statistics.enable.dss || (Holders.config.statistics.enable.ror && !cacheService.getRorCache()))
    }

    void triggerLambdaForExecution(Map body, String lambdaARN) {
        LambdaClient lambdaClient = null
        Map resultMap = [:]
        String jsonPayload = JsonOutput.toJson(body)
        SdkBytes payload = SdkBytes.fromUtf8String(jsonPayload)

        try {
            lambdaClient = LambdaClient.builder().build()

            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(lambdaARN)
                    .payload(payload)
                    .build() as InvokeRequest;

            // Trigger the Lambda function
            InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest)

            String lambdaResponse = invokeResponse.payload().asUtf8String()
            resultMap = jsonToMap(lambdaResponse)
            log.info("Response from lambda : ${resultMap}")
            if (resultMap.statusCode != 200) {
                throw new Exception("Alert got failed while triggering LAMBDA.")
            }
        } catch (Exception exception) {
            log.error(exception.getMessage())
            throw exception
        } finally {
            lambdaClient?.close()
        }
    }

    List fetchDataFromDataLake(String selectQuery) {
        if (!selectQuery) {
            return []
        }
        log.info("Starting fetching alert data from AWS")
        def start = System.currentTimeSeconds()
        String dataWarehouseName = "s3://${Holders.config.aws.aggregate.bucket.name}/${Holders.config.aws.aggregate.datalake}"
        SparkSession sparkSession = null
        String awsRegion = Holders.config.aws.aggregate.region
        Dataset<Row> dataset = null
        try {
            sparkSession = SparkSession.builder()
                    .appName("GlueSparkConnector")
                    .master("local[10]")
                    .config("spark.sql.catalog.glue_catalog", "org.apache.iceberg.spark.SparkCatalog")
                    .config("spark.sql.catalog.glue_catalog.catalog-impl", "org.apache.iceberg.aws.glue.GlueCatalog")
                    .config("spark.sql.catalog.glue_catalog.io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
                    .config("spark.hadoop.aws.region", awsRegion)
                    .config("spark.sql.catalog.glue_catalog.warehouse", dataWarehouseName)
                    .getOrCreate() as SparkSession

            dataset = sparkSession.sql(selectQuery)
            log.info("Time taken to fetch data from AWS - ${System.currentTimeSeconds() - start} seconds" )
            log.info("Successfully fetched data from AWS")
            return dataset?.toJSON()?.collectAsList()
        } catch (Exception exception) {
            log.error("Error occurred while fetching alert data from AWS: " + exception.getMessage())
            throw exception
        } finally {
            if (dataset != null) {
                dataset.unpersist()  // Ensure dataset resources are released
            }
            if (sparkSession != null) {
                sparkSession.stop();  // Stop the session after task
            }
        }
    }

    ResponseInputStream<GetObjectResponse> readFileStatusFromS3(String fileName) {
        String bucketName = Holders.config.aws.aggregate.bucket.name
        String awsRegion = Holders.config.aws.aggregate.region

        S3Client s3Client = S3Client.builder()
                .region(Region.of(awsRegion)).build()

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build() as GetObjectRequest

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)
        return s3Object
    }

    boolean isLambdaFlowCompleted(Long runId, int retryCount) {
        int timeoutCount = Holders.config.aws.aggregate.lambda.timeout / Holders.config.aws.aggregate.lambda.interval as int
        Thread.sleep((Holders.config.aws.aggregate.lambda.interval * 60000) as int)        // Waiting for x min before checking the status from AWS
        try {
            if (retryCount < timeoutCount) {
                String fileName = "output/output_${runId}.json"
                def s3Object = readFileStatusFromS3(fileName)
                Map statusMap = jsonToMap(s3Object?.text)
                log.info("Checking lambda status - ${statusMap}")
                if (statusMap.scores."${runId}".STATUS.equalsIgnoreCase(Constants.EtlStatus.SUCCESS) && statusMap.counts."${runId}".STATUS.equalsIgnoreCase(Constants.EtlStatus.SUCCESS)) {
                    return true
                } else if (statusMap.scores."${runId}".STATUS.equalsIgnoreCase(Constants.SpotfireStatus.FAILED) || statusMap.counts."${runId}".STATUS.equalsIgnoreCase(Constants.SpotfireStatus.FAILED)){
                    throw new Exception("Alert got failed at lambda.")
                } else {
                    retryCount += 1
                    isLambdaFlowCompleted(runId, retryCount)
                }
            } else {
                throw new Exception("Alert got failed due to timeout at AWS lambda.")
            }
        } catch (Exception exception) {
            log.error("Error encountered while reading lambda status from S3 " + exception.getMessage())
            throw exception
        }
    }

    boolean checkPecsInAWS(String query) {
        log.info("Query:- " + query)
        Integer numOfCases = 0
        List data = fetchDataFromDataLake(query)
        log.info("RESPONSE : --  ${data}")
        data.each { row ->
            Map dataMap = jsonToMap(row)
            numOfCases = dataMap.TOTAL_PEC_COUNT as Integer
        }
        return numOfCases > 0
    }

    String fetchCriteriaCountFromAWS(String criteriaCountQuery) {
        log.info("Query for criteria count -> " + criteriaCountQuery)
        List data = fetchDataFromDataLake(criteriaCountQuery)
        Map countMap = [:]
        log.info("Response from AWS -> ${data}")
        data.each { row ->
            Map dataMap = jsonToMap(row)
            countMap['Alert Level New Count'] = dataMap.new_total_count ?:0
            countMap['Alert Level Cumulative Count'] = dataMap.cumm_total_count ?:0
            countMap['Alert Level New Study Count'] = dataMap.study_total_new_count ?:0
            countMap['Alert Level Cumulative Study Count'] = dataMap.study_total_cumm_count ?: 0
        }
        String jsonString = countMap? JsonOutput.toJson(countMap): null
        log.info("JSON string for criteria count -> " + jsonString)
        return jsonString
    }

    boolean checkAggCountInAWS(Long executedConfigurationId) {
        Integer numOfCases = 0
        try {
            String aggCountQuery = "SELECT count(1) FROM glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_app_agg_counts_${executedConfigurationId} agg_counts WHERE agg_counts.cumm_count > 0 AND agg_counts.pt_name IS NOT NULL LIMIT 1"
            log.info("Query : " + aggCountQuery)
            List data = fetchDataFromDataLake(aggCountQuery)
            log.info("Response: ${data}")
            data.each { row ->
                Map dataMap = jsonToMap(row)
                numOfCases = dataMap."count(1)" as Integer
            }
        } catch (Exception ex) {
            throw ex
        }
        numOfCases > 0
    }

    List<HashMap> getPrrRorData(Sql sql, String tableName, boolean isAws = false) {
        List<HashMap> prrData = []
        if (isAws) {
            String query = "SELECT * FROM ${tableName}"
            List data = fetchDataFromDataLake(query)
            data.each { row ->
                HashMap dataMap = jsonToMap(row) as HashMap
                prrData.add(dataMap)
            } as List<HashMap>
        } else {
            sql.eachRow("select * FROM $tableName", []) { GroovyResultSet resultSet ->
                HashMap map = [:]
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
                prrData.add(map)
            }
        }
    }

    void processAlertExecutionAndData(ExecutedConfiguration executedConfiguration) {
        List<ExecutedConfiguration> childAlerts = ExecutedConfiguration.findAllByExMasterTemplateId(executedConfiguration.id)
        log.info("Started processing alert execution for child alerts.")
        String fileName = "output/output.json"
        def s3Object = readFileStatusFromS3(fileName)
        Map statusMap = jsonToMap(s3Object?.text)
        String eventHierarchy = (jsonToMap(executedConfiguration.eventHierarchyMasterTmplt).values().first() as String).toLowerCase()
        Map configMapping = jsonToMap(Configuration.read(executedConfiguration.configId).masterConfigMapping)
        if (!(statusMap.SCORES."${configMapping.scoresConfigId}".STATUS.equalsIgnoreCase(Constants.EtlStatus.SUCCESS) && statusMap.COUNTS."${configMapping.countsConfigId}".STATUS.equalsIgnoreCase(Constants.EtlStatus.SUCCESS))) {
            throw new Exception("Alert got failed at the AWS.")
        }
        childAlerts.each { ExecutedConfiguration exConfig ->
            Configuration childConfig = Configuration.get(exConfig.configId)
            log.info("Executing child -> ${childConfig.name}")
            String productId = getProductIdsFromProductSelection(exConfig.productSelection)[0] as String
            // TODO: EBGM flow is now handled currently, this needs to be handled in future development

            // Fetching PRR and ROR data for child alerts
            String prrCaseCheckQuery = "select * from glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_prr_ror_ml_info_${eventHierarchy}_${configMapping.scoresConfigId}"
            boolean isCaseAvailPrrRor = checkPecsInAWS(prrCaseCheckQuery)
            if ((Holders.config.statistics.enable.prr || Holders.config.statistics.enable.ror) && isCaseAvailPrrRor) {
                generatePrrScoresForHierarchyAlert(configMapping.scoresConfigId as Long, exConfig, productId as String, eventHierarchy)
            }

            List alertData = prepareAlertDataForHierarchyAlertChild(exConfig, configMapping.countsConfigId as Long, productId, eventHierarchy)
            String alertFileName = "${Holders.config.signal.alert.file}/${exConfig.id}_${exConfig.type}_${exConfig.selectedDatasource}"
            alertService.saveAlertDataInFile(alertData, alertFileName)

            log.info("after finishing alert data in file.")
            Map<String, Long> otherDataSourcesExecIds = [:]
            otherDataSourcesExecIds.put(exConfig.selectedDatasource, exConfig.id)

            reportExecutorService.saveQuantData(childConfig, exConfig, alertFileName, otherDataSourcesExecIds, null)
            reportExecutorService.setValuesForChildConfiguration(childConfig, exConfig)
            String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(configMapping.countsConfigId as Long, false, true, eventHierarchy, productId)
            exConfig.criteriaCounts = fetchCriteriaCountFromAWS(criteriaCountQuery)
            exConfig.refresh()
            exConfig.save(flush: true)
            childConfig.refresh()
            childConfig.save(flush: true)
        }
    }

    void generatePrrScoresForHierarchyAlert(Long scoresConfigId, ExecutedConfiguration executedConfiguration, String productId, String eventHierarchy) {
        String prrTableName = "glue_catalog.${Holders.config.aws.aggregate.datalake}.prr_full_data_${eventHierarchy}_${scoresConfigId}"
        setPrrDataForHierarchyAlert(prrTableName, executedConfiguration, productId)

        String rorTableName = "glue_catalog.${Holders.config.aws.aggregate.datalake}.ror_full_data_${eventHierarchy}_${scoresConfigId}"
        setRorDataForHierarchyAlert(rorTableName, executedConfiguration, productId)
    }

    void setPrrDataForHierarchyAlert(String tableName, ExecutedConfiguration executedConfiguration, String productId) {
        List<HashMap> prrData = []

        String query = "SELECT * FROM ${tableName} WHERE BASE_ID = ${productId}"
        List data = fetchDataFromDataLake(query)
        data.each { row ->
            HashMap dataMap = jsonToMap(row) as HashMap
            prrData.add(dataMap)
        } as List<HashMap>

        prrData.each{ row->
            Map statsProperties = [:]
            statsProperties.prrValue = row.PRR as Double
            statsProperties.prrUCI = row.PRR_UCI as Double
            statsProperties.prrLCI = row.PRR_LCI as Double
            statsProperties.aValue = row.PRR_A as Double
            statsProperties.bValue = row.PRR_B as Double
            statsProperties.cValue = row.PRR_C as Double
            statsProperties.dValue = row.PRR_D as Double
            if(executedConfiguration.dataMiningVariable && !executedConfiguration.isProductMining && (executedConfiguration.productGroupSelection || executedConfiguration.productSelection)){
                String batchId = row.BATCH_ID ? String.valueOf(row.BATCH_ID) : ""
                String[] splitBatchId = batchId?.split("_")
                productId = splitBatchId ? splitBatchId[-1] : productId
            }
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            dataObjectService.setProbDataMap(executedConfiguration.id, productId, eventId, statsProperties as Map<String, String>)
        }
    }

    void setRorDataForHierarchyAlert(String tableName, ExecutedConfiguration executedConfiguration, String productId) {
        List<HashMap> rorData = []

        String query = "SELECT * FROM ${tableName} WHERE BASE_ID = ${}"
        List data = fetchDataFromDataLake(query)
        data.each { row ->
            HashMap dataMap = jsonToMap(row) as HashMap
            rorData.add(dataMap)
        } as List<HashMap>

        rorData?.each { Map row->
            Map statsProperties = [:]
            statsProperties.rorValue = row.ROR as Double
            statsProperties.rorUCI = row.ROR_UCI as Double
            statsProperties.rorLCI = row.ROR_LCI as Double
            statsProperties.chiSquare = row.CHI_SQUARE as Double
            if(executedConfiguration.dataMiningVariable && !executedConfiguration.isProductMining && (executedConfiguration.productGroupSelection || executedConfiguration.productSelection)){
                String batchId = row.BATCH_ID ? String.valueOf(row.BATCH_ID) : ""
                String[] splitBatchId = batchId?.split("_")
                productId = splitBatchId ? splitBatchId[-1] : productId
            }
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            dataObjectService.setRorProbDataMap(executedConfiguration.id, productId, eventId, statsProperties as Map<String, String>)
        }
    }

    List prepareAlertDataForHierarchyAlertChild(ExecutedConfiguration executedConfiguration, Long countsConfigId, String productId, String eventHierarchy) {
        List resultData = []
        String customReportSQl = sqlGenerationService.generateCustomReportSQL(countsConfigId, executedConfiguration.selectedDatasource, executedConfiguration.eventGroupSelection && executedConfiguration.groupBySmq, executedConfiguration.selectedDatasource, false, true, eventHierarchy, productId)
        List<Map> newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true)
        String tempSql = ''
        newFields?.each {
            if (it.isNewColumn && it.keyId && it.enabled) {
                String key = it.keyId.replace("REPORT_","").replace("_FLG","")
                tempSql = tempSql + "," + "NEW_" + key + "_COUNT" + " AS " + "NEW_" + key + "_COUNT" + " "
                tempSql = tempSql + "," + "CUMM_" +key+ "_COUNT" + " AS " + "CUMM_" + key + "_COUNT" + " "
            }
        }
        if (newFields != null) {
            customReportSQl = customReportSQl.replaceAll(",#NEW_DYNAMIC_COUNT", tempSql)
        } else {
            customReportSQl = customReportSQl.replaceAll(",#NEW_DYNAMIC_COUNT", ' ')
        }
        List resultList = fetchDataFromDataLake(customReportSQl)
        resultList.each { row ->
            HashMap dataMap = jsonToMap(row) as HashMap
            resultData.add(dataMap)
        }
        return resultData
    }

    List getProductIdsFromProductSelection (String productSelection) {
        def parsedProductSelection = new JsonSlurper().parseText(productSelection)
        List productIds = []
        parsedProductSelection.each { key, value ->
            if (value instanceof List && !value.isEmpty()) {
                value.each { item ->
                    if (item.id) {
                        productIds << item.id
                    }
                }
            }
        }
        return productIds
    }
}
