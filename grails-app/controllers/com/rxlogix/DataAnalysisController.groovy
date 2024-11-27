package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.audit.AuditTrail
import com.rxlogix.commandObjects.SpotfireCommand
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.SpotfireSettingsDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.ProductClassification
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.json.JsonOutput
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.user.User
import com.rxlogix.util.SecurityUtil
import com.rxlogix.util.Tuple2
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql

import javax.servlet.http.Cookie
import java.sql.Clob
import java.sql.ResultSet

@Secured(["isAuthenticated()"])
class DataAnalysisController implements SanitizePaginationAttributes {

    def spotfireService
    def userService
    def searchService
    def alertService
    def CRUDService
    def dataObjectService
    def beforeInterceptor = [action: this.&auth, except: ['accessDenied', 'keepAlive']]
    Set<Tuple2> jobIds = new HashSet<>()
    def aggregateCaseAlertService
    def configurationService
    def dataSource_pva
    def signalAuditLogService

    def auth() {
        if (!spotfireService.getUserByName(userService.getUser()?.username)) {
            flash.error = message(code: 'app.spotfire.access.denied')
            redirect action: 'accessDenied'
            return false
        }
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def index() {
        String username = userService.getUser()?.username ?: ""
        String secret = Holders.config.spotfire.token_secret
        String libraryRoot = Holders.config.spotfire.libraryRoot
        String value = Holders.config.spotfire.symptomsComanifestation
        String columnName = Holders.config.spotfire.symptomsComanifestationColumnName
        String setFilterString
        switch(value?.toLowerCase()){
            case 'yes':
                value = 'Yes'
                break
            case 'no':
                value = 'No'
                break
            default:
                value = ''
                break
        }
        if(value)
            setFilterString ='SetFilter(tableName=\"Case, Demographics & AEs\", columnName = \"' + columnName + '\", values= {"' + value + '"});'
        else
            setFilterString ='SetFilter(tableName=\"Case, Demographics & AEs\", columnName = \"' + columnName + '\", Operation= {\"Reset\"});'
        return [wp_url         : spotfireService.composeSpotfireUrl(),
                auth_token     : SecurityUtil.encrypt(secret, username),
                libraryRoot    : libraryRoot,
                callback_server: Holders.config.spotfire.callbackUrl,
                setFilterString : setFilterString
        ]
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def create() {
        String cur_username = userService.getUser()?.username
        //The reports case series will be passed instead of application case series.
        //Setup of case series id will be dependent on the type of case series created. If the date range for which
        //execution was done, is cumulative then then cummulative case series id will be used and else case series id will be used.
        List<Map> executedConfigs = spotfireService.fetchCaseSeriesForSpotfire()

        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(params.long("alertId"))
        Long selectedCaseSeries = executedConfiguration?.pvrCaseSeriesId

        [user_name: spotfireService.getHashedValue(cur_username), executedConfigs: executedConfigs, selectedCaseSeries: selectedCaseSeries]
    }

    @Secured(['ROLE_DATA_ANALYSIS', 'ROLE_OPERATIONAL_METRICS', 'ROLE_PRODUCTIVITY_AND_COMPLIANCE'])
    def view(String fileName) {
        Boolean  isFaersVaersDataSource = params.isFaersVaersDataSource?.toBoolean()?: false
        String username = userService.getUser()?.username ?: ""
        User user = userService.getUserByUsername(username)
        if (user) {
            Map spotfireConfig = Holders.config.spotfire as Map
            String token = SecurityUtil.encrypt(spotfireConfig.token_secret as String, username)

            String cookie = 'ticket=' + spotfireService.getHashedValue(username) + '; path=/' + spotfireConfig.path

            String serverUrl = spotfireConfig.protocol + "://" + spotfireConfig.server + ":" + spotfireConfig.port + "/" + spotfireConfig.path +
                    URLEncoder.encode(fileName, "UTF-8") + "&auth_token=" + URLEncoder.encode(token, "UTF-8") +
                    "&cbs=" + URLEncoder.encode(spotfireConfig.callbackUrl as String, "UTF-8")
            if (spotfireService.fetchHidingOptions(fileName) != null && spotfireService.fetchHidingOptions(fileName) != "") {
                serverUrl += "&options=" + URLEncoder.encode(spotfireService.fetchHidingOptions(fileName), "UTF-8")
            }
            if(!isFaersVaersDataSource) {
                serverUrl += '&configurationBlock='
                serverUrl += params.configurationBlock && params.configurationBlock != "null" ? URLEncoder.encode(params.configurationBlock as String, "UTF-8") : ''
            }

            String finalUrl = spotfireConfig.protocol + "://" + spotfireConfig.server + "/SpotfireWeb/LogOut.aspx?logoutByUser=true"

            log.info("Server Url --------: $serverUrl")
            log.info("Token is ------ : $token")

            spotfireService.addAuthToken(token, username, user.fullName, user.email)
            response.addCookie(new Cookie("pvr-spotfire-cookie", System.currentTimeMillis().toString()))
            return [user_name      : spotfireService.getHashedValue(username),
                    fileName       : fileName,
                    libraryRoot    : spotfireConfig.libraryRoot,
                    hidingOptions  : spotfireService.fetchHidingOptions(fileName),
                    wp_url         : spotfireService.composeSpotfireUrl(),
                    auth_token     : token,
                    callback_server: spotfireConfig.callbackUrl,
                    caseSeriesId   : params.configurationBlock,
                    server_url     : serverUrl,
                    cookie         : cookie,
                    interval       : spotfireConfig.keepAlive.interval,
                    finalUrl       : finalUrl
            ]
        } else {
            render status: 403, contentType: 'text/html', text: "You are not allowed to view the page"
        }
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def list() {
        render(spotfireService.reportFilesMapData as JSON)
    }


    def checkSpotfireFile(String name) {
        if (spotfireService.getReportFilesByName(name)) {
            render([url: spotfireService.spotfireFileUrl(name)] as JSON)
        }
        render status: 404, contentType: 'application/json', text: '{"status": "Failed"}'
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def generate(SpotfireCommand spotfireCommand) {
        log.info("The product family ids are : " + spotfireCommand.productFamilyIds.toString())
        log.info("Case Series Id is : " + spotfireCommand.caseSeriesId)
        log.info("Reporting Start Date is : " + spotfireCommand.actualFromDate)
        log.info("Reporting End Date is : " + spotfireCommand.actualEndDate)
        log.info("As of Date is : " + spotfireCommand.actualAsOfDate)
        log.info("Report Type is : " + spotfireCommand.type)
        log.info("File Full Name is : " + spotfireCommand.fullFileName)
        spotfireCommand.fullFileName = spotfireCommand.fullFileName?.replaceAll("[^a-zA-Z0-9\\-]", "_");
        log.info("File Full Name after special character removal is : " + spotfireCommand.fullFileName)

        if (spotfireCommand.validate()) {
            spotfireService.reserveFileName(spotfireCommand.fullFileName)

            def respMsg = spotfireService.generateReport(
                    spotfireCommand.productFamilyIds,
                    spotfireCommand.actualFromDate,
                    spotfireCommand.actualEndDate,
                    spotfireCommand.actualAsOfDate,
                    spotfireCommand.caseSeriesId,
                    spotfireCommand.type,
                    spotfireCommand.fullFileName,
                    false)

            JsonSlurper slurper = new JsonSlurper()

            def jsonRsp = respMsg ? slurper.parseText(respMsg) : null
            if (Holders.config.spotfire.fileBasedReportGen) {
                if (respMsg == '') {
                    flash.message = message(code: 'app.spotfire.success.msg')
                    redirect action: 'index'
                } else {
                    flash.error = message(code: 'app.spotfire.failed.generate.report')
                    redirect view: 'index'
                }
            } else if (jsonRsp?.JobId) {
                if (jobIds == null) {
                    jobIds = new HashSet<>()
                }

                jobIds.add(new Tuple2(jsonRsp.JobId, jsonRsp.StatusCode))


                flash.message = message(code: 'app.spotfire.success.msg')
                redirect action: 'index'
            } else {
                flash.error = message(code: 'app.spotfire.failed.generate.report')
                redirect view: 'index'
            }
        } else {
            flash.warning = message(code: 'app.spotfire.failed.generate.report')

            def lmProductFamilies = []
            try {
                LmProductFamily."pva".withTransaction {
                    lmProductFamilies = LmProductFamily."pva".list()
                }
            } catch (Throwable ex) {
                log.error(ex.getMessage())
            }

            List<Map> executedConfigs = spotfireService.fetchCaseSeriesForSpotfire()

            String productFamilyJson = JsonOutput.toJson(spotfireCommand.productFamilyIds)
            render(view: 'create', model: [spotfireCommand: spotfireCommand, lmProductFamilies: lmProductFamilies, executedConfigs: executedConfigs, productFamilyJson: productFamilyJson])
        }
    }

    def accessDenied() {
        render view: 'access_denied'
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def keepAlive() {
        request.getSession false
        render status: 200
    }

    def getProductFamilyList() {
        forSelectBox(params)
        List items = []
        Integer totalCount = 0
        try {
            LmProductFamily."pva".withTransaction {
                items = LmProductFamily."pva".createCriteria().list([offset: Math.max(params.page - 1, 0) * params.max, max: params.max, order: 'asc', sort: 'name']) {
                    if (params.term) {
                        iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(params.term)}%")
                    }
                }
                totalCount = items?.totalCount
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage())
        }
        render([list: items?.collect { [id: it.id, text: it.name] }, totalCount: totalCount] as JSON)
    }

    def generateDataAnalysis() {

        ExecutedConfiguration ec = ExecutedConfiguration.findById(params.executedConfigId as Long)
        // moved audit-log code to top, so that
        // entry will created before triggering spotfire API
        String dateRangeSpotfire = ""
        if(params.spotfireDateRange.contains('PR_DATE_RANGE')){
            dateRangeSpotfire = "Interval"
        }else{
            dateRangeSpotfire = "Cumulative"
        }
        signalAuditLogService.createAuditLog([
                entityName : Constants.AuditLog.typeToEntityMap.get(ec.type),
                moduleName : Constants.AuditLog.typeToEntityMap.get(ec.type)+(ec.isLatest ? "" : ": Archived Alert")+": Data Analysis",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: ec.getInstanceIdentifierForAuditLog(),
                description: "Executing Spotfire Report",
                username   : userService.getUser()?.username ?: ec.owner.username,
                fullname   : userService.getUser()?.fullName ?: ec.owner.fullName
        ] as Map, [[propertyName: "Alert Name", oldValue: "", newValue: ec?.name],
                   [propertyName: "Date Range", oldValue: "", newValue: dateRangeSpotfire],
                   [propertyName: "Owner", oldValue: "", newValue: ec?.owner?.fullName ?: userService.getUser()?.fullName]] as List)
        Configuration configuration = Configuration.get(ec.configId)
        ExecutedConfiguration executedConfigurationFaers = ExecutedConfiguration.findByConfigId(Configuration.findByIntegratedConfigurationId(configuration?.id)?.id)
        ExecutedConfiguration executedConfigurationVigibase = ExecutedConfiguration.findByConfigId(Configuration.findByIntegratedConfigurationId(configuration?.id)?.id)
        ExecutedConfiguration executedConfigurationVaers = ExecutedConfiguration.findByConfigId(Configuration.findByIntegratedConfigurationId(configuration?.id)?.id)
        String drugType = ProductClassification.DRUG
        if (ec.spotfireSettings) {
            DateRangeEnum currentDateRange = params.spotfireDateRange
            List<DateRangeEnum> listOfDateRange = SpotfireSettingsDTO.fromJson(ec.spotfireSettings).rangeType
            if (listOfDateRange.contains(currentDateRange) && (ExecutionStatus.findByExecutedConfigIdAndType(ec.id, ec.type).spotfireExecutionStatus != null)) {
                render(["Error"] as JSON)
                return
            }
        }
        if (configuration.type != Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            drugType = ec?.drugType == Constants.DrugType.VACCINE ? ProductClassification.VACCINCE : ProductClassification.DRUG
        } else {
            Sql sql = new Sql(dataSource_pva)
            List idList = []
            def prodId = null
            if (ec.productSelection) {
                prodId = getIdFieldFromJson(ec.productSelection)
                idList = prodId.split(',').toList()
            } else if (ec.productGroupSelection) {
                def jsonObj = null
                jsonObj = parseJsonString(ec.productGroupSelection)
                if (jsonObj) {
                    jsonObj.each {
                        idList.add(it?.id)
                    }
                }
                idList.each {
                    String dataJson
                    sql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID = ${it as BigInteger}") { ResultSet resultSetObj ->
                        Clob clob = resultSetObj.getClob("GRP_DATA")
                        if (clob) {
                            dataJson = clob.getSubString(1, (int) clob.length())
                        }
                    }
                    prodId = getIdFieldFromJson(dataJson)
                    idList = prodId.split(',').toList()
                }
            }
            boolean isVaccine = false
            boolean isDrug = false
            idList.each {
                def id = it as BigInteger
                def rows = sql.rows("SELECT * FROM pvs_vaccine_products WHERE PRODUCT_ID = ${id}")
                isVaccine = (rows.size() > 0)
                if (!isVaccine) {
                    rows = sql.rows("SELECT * FROM pvs_drug_products WHERE PRODUCT_ID = ${id}")
                }
                isDrug = (rows.size() > 0)
            }
            drugType = isVaccine ? ProductClassification.VACCINCE : ProductClassification.DRUG
            sql?.close()
        }
        String productSelection = ec.productSelection ?: ec?.productGroupSelection
        String studySelection = ec.studySelection
        DateRangeEnum dateRange = params.spotfireDateRange
        List<DateRangeEnum> dateRangeEnums = []
        if (ec.spotfireSettings) {
            SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(ec.spotfireSettings)
            drugType = settings.type
            dateRangeEnums = settings.rangeType
        }
        dateRangeEnums.add(dateRange)
        if (studySelection || productSelection || configuration.productGroupSelection || (ec.dataMiningVariable && ec.adhocRun)) {
            SpotfireSettingsDTO settings = new SpotfireSettingsDTO()
            settings.type = drugType
            settings.rangeType = dateRangeEnums
            if (configuration.type != Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                settings.dataSource = configuration.selectedDatasource.split(",")
            } else {
                settings.dataSource = [Constants.DataSource.PVA]
            }
            ec.spotfireSettings = settings.toJsonString()
        } else {
            ec.spotfireSettings = null
        }
        ec.save(flush: true)
        boolean isReportingCompleted = false
        if (ec.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            boolean isSpotfire = false
            boolean isFaersSpotfire = false
            boolean isVigibaseSpotfire = false
            boolean isVaersSpotfire = false
            if (ec.spotfireSettings) {
                SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(ec.spotfireSettings)
                isSpotfire = settings.dataSource.contains('pva')
                isFaersSpotfire = settings.dataSource.contains('faers')
                isVigibaseSpotfire = settings.dataSource.contains('vigibase')
                isVaersSpotfire = settings.dataSource.contains('vaers')
            }
            boolean isCaseSeriesGenerated = aggregateCaseAlertService.generateCaseSeriesForReporting(ec, false, isSpotfire, isFaersSpotfire, isVigibaseSpotfire, isVaersSpotfire)
            ec = ExecutedConfiguration.findById(params.executedConfigId as Long)
            if (ec.pvrCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForQuantAlert(ec, false)
            }
            if (ec.pvrCumulativeCaseSeriesId && ec.pvrCaseSeriesId != ec.pvrCumulativeCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForQuantAlert(ec, true)
            }
            if (ec.faersCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForFaersAlert(ec, executedConfigurationFaers?.id, false)
            }
            if (ec.faersCumCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForFaersAlert(ec, executedConfigurationFaers?.id, true)
            }
            if (ec.vigibaseCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVigibaseAlert(ec, executedConfigurationVigibase?.id, false)
            }
            if (ec.vigibaseCumCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVigibaseAlert(ec, executedConfigurationVigibase?.id, true)
            }
            if (ec.vaersCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVaersAlert(ec, executedConfigurationVaers?.id, false)
            }
            if (ec.vaersCumCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVaersAlert(ec, executedConfigurationVaers?.id, true)
            }
        } else {
            isReportingCompleted = true
        }
        if (isReportingCompleted) {
            ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(ec.id, ec.type)
            if (!executionStatus) {
                executionStatus = spotfireService.createExecutionStatus(ec)
            }
            try {
                String updateQuery = "Update ExecutionStatus set spotfireExecutionStatus = :status where id = :id"
                spotfireService.updateExecutionStatusForReporting(updateQuery, [status: ReportExecutionStatus.GENERATING, id: executionStatus?.id])
                log.info("Calling generateSpotfireReport()")
                if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE.name() || params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_SAFETYDB.name()) {
                    spotfireService.executeSpotfireReport(ec, false)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE.name() || params.spotfireDateRange == DateRangeEnum.CUMULATIVE_SAFETYDB.name()) {
                    spotfireService.executeSpotfireReport(ec, true, true)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE_FAERS.name()) {
                    spotfireService.executeSpotfireReportFaers(ec, true)
                } else if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_FAERS.name()) {
                    spotfireService.executeSpotfireReportFaers(ec, false)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE_VIGIBASE.name()) {
                    spotfireService.executeSpotfireReportVigibase(ec, true)
                } else if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_VIGIBASE.name()) {
                    spotfireService.executeSpotfireReportVigibase(ec, false)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE_VAERS.name()) {
                    spotfireService.executeSpotfireReportVaers(ec, true)
                } else if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_VAERS.name()) {
                    spotfireService.executeSpotfireReportVaers(ec, false)
                }
            } catch (Throwable ex) {
                String stackTrace = alertService.exceptionString(ex).replaceAll("'", "''")
                String updateQuery = "Update ExecutionStatus set spotfireExecutionStatus = :status, stackTrace = :stackTrace where id = :id"
                spotfireService.updateExecutionStatusForReporting(updateQuery, [status: ReportExecutionStatus.ERROR, stackTrace: stackTrace, id: executionStatus?.id])
                spotfireService.sendErrorNotification(ec)
                log.error("Failed generate spotfire report file while generating", ex.printStackTrace())
            }
        }
        render(["Success"] as JSON)
    }

    def getIdFieldFromJson(String jsonString) {
        def prdId = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdId = jsonString
            else {
                def prdVal = jsonObj.find { k, v ->
                    v.find {
                        it.containsKey('id')
                    }
                }?.value.findAll {
                    it.containsKey('id')
                }.collect {
                    it.id
                }
                prdId = prdVal ? prdVal.sort().join(',') : ""
            }
        }
        prdId
    }

    def parseJsonString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }

}
