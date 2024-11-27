package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.util.DateUtil
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugins.mail.MailService
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.Synchronized

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Handles logic behind deletion of alert and associated data
 */
@Transactional
class PreCheckService {
    static transactional = false
    def healthService
    def userService
    def dataSource_pva
    def dataSource
    def dataSource_spotfire
    def dataSource_eudra
    def dataSource_faers
    def dataSource_vaers
    def dataSource_vigibase
    def cacheService
    def customMessageService
    MailService mailService

    GrailsApplication grailsApplication

    List preChecks = new ArrayList()

    @Synchronized
    void executeSystemConfigurationPrecheck() {
        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        try {
            if (precheckEnabled) {
                log.debug("executeSystemConfigurationPrecheck job running..." + precheckEnabled)
                List<SystemPrecheckEmail> systemPrecheckEmailList = new ArrayList<SystemPrecheckEmail>()
                List destinationEmailAddress = Holders.config.system.precheck.configuration.emails.destination
                String emailSubject = customMessageService.getMessage('precheck.email.error.subject')
                try {
                    //Checking precheck status at mart end for SAFETY optional parameter changes
                    systemPrecheckEmailList = systemPrecheckEmailList + updateAppPrechecksFromMart([dataSource: dataSource_pva, type: Constants.SystemPrecheck.SAFETY, bootType: true, systemPrecheckEmailList: systemPrecheckEmailList])
                } catch (Exception ex) {
                    ex.printStackTrace()
                }

                try {
                    //Checking precheck status at mart end for SAFETY
                    systemPrecheckEmailList = systemPrecheckEmailList + insertDbStatus([dataSource: dataSource_pva, type: Constants.SystemPrecheck.SAFETY, bootType: false, systemPrecheckEmailList: systemPrecheckEmailList])
                } catch (Exception ex) {
                    ex.printStackTrace()
                }

                try {
                    //Checking precheck status at mart end for FAERS
                    if (Holders.config.signal.faers.enabled) {
                        systemPrecheckEmailList = systemPrecheckEmailList + insertDbStatus([dataSource: dataSource_faers, type: Constants.SystemPrecheck.FAERS, bootType: false, systemPrecheckEmailList: systemPrecheckEmailList])
                    }
                } catch (Exception ex) {
                    ex.printStackTrace()
                }

                try {
                    //Checking precheck status at mart end for VIGIBASE
                    if (Holders.config.signal.vigibase.enabled) {
                        systemPrecheckEmailList = systemPrecheckEmailList + insertDbStatus([dataSource: dataSource_vigibase, type: Constants.SystemPrecheck.VIGIBASE, bootType: false, systemPrecheckEmailList: systemPrecheckEmailList])
                    }
                } catch (Exception ex) {
                    ex.printStackTrace()
                }

                try {
                    //Checking precheck status at mart end for VAERS
                    if (Holders.config.signal.vaers.enabled) {
                        systemPrecheckEmailList = systemPrecheckEmailList + insertDbStatus([dataSource: dataSource_vaers, type: Constants.SystemPrecheck.VAERS, bootType: false, systemPrecheckEmailList: systemPrecheckEmailList])
                    }
                } catch (Exception ex) {
                    ex.printStackTrace()
                }

                try {
                    //Checking precheck status at mart end for EVDAS
                    if (Holders.config.signal.evdas.enabled) {
                        systemPrecheckEmailList = systemPrecheckEmailList + insertDbStatus([dataSource: dataSource_eudra, type: Constants.SystemPrecheck.EVDAS, bootType: false, systemPrecheckEmailList: systemPrecheckEmailList])
                    }
                } catch (Exception ex) {
                    ex.printStackTrace()
                }

                for (int k = 0; k < systemPrecheckEmailList?.size(); k++) {
                    try {
                        systemPrecheckEmailList[k]?.merge()
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                }

                List<SystemPrecheckEmail> emailData = SystemPrecheckEmail.findAllByEmailSent(false)?.unique { it?.name }
                List<SystemPrecheckEmail> preCheckData = new ArrayList<>()
                if (emailData) {
                    for (int k = 0; k < emailData?.size(); k++) {
                        SystemPrecheckEmail it=emailData[k]
                        SystemPreConfig preConfig = SystemPreConfig.findByNameAndDbTypeAndAppType(it?.name?.toUpperCase(), it?.dbType, it?.appType)
                        if (preConfig && !preConfig?.running && !preConfig?.optional && preConfig.previousRunningStatus) {
                            preConfig?.previousRunningStatus = preConfig?.running
                            String displayName = ''
                            if (preConfig.appType.equals("database") && !preConfig.entityKey.equals("DB_CHECKS")) {
                                if (preConfig?.dbType.equals(Constants.SystemPrecheck.SAFETY)) {
                                    displayName = preConfig?.displayName + " - " + "Safety DB";
                                } else if (preConfig?.dbType.equals(Constants.SystemPrecheck.FAERS)) {
                                    displayName = preConfig?.displayName + " - " + "FAERS";
                                } else if (preConfig?.dbType.equals(Constants.SystemPrecheck.EVDAS)) {
                                    displayName = preConfig?.displayName + " - " + "EVDAS";
                                } else if (preConfig?.dbType.equals(Constants.SystemPrecheck.VAERS)) {
                                    displayName = preConfig?.displayName + " - " + "VAERS";
                                } else if (preConfig?.dbType.equals(Constants.SystemPrecheck.VIGIBASE)) {
                                    displayName = preConfig?.displayName + " - " + "VigiBase";
                                }
                            } else {
                                displayName = preConfig?.displayName
                            }
                            SystemPrecheckEmail spe = new SystemPrecheckEmail()
                            spe?.name = displayName
                            spe?.reason = preConfig?.reason
                            preCheckData.add(spe)
                            it?.emailSent = true
                        }
                    }
                }
                if (destinationEmailAddress != null && !destinationEmailAddress.isEmpty() && preCheckData != null && !preCheckData.isEmpty()) {
                    log.debug("Sending email to : " + destinationEmailAddress)
                    try {
                        mailService.sendMail {
                            multipart false
                            async true
                            from "${Holders.config.grails.mail.displayName} <${Holders.config.grails.mail.default.from}>"
                            to destinationEmailAddress
                            subject emailSubject
                            html(view: '/email/failedPrecheck', model: [preCheckData: preCheckData])
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                        emailData.each {
                            it.emailSent = false
                        }
                    }

                }

            }

        } catch (Exception ex) {
            ex.printStackTrace()
        }finally{
            log.debug("executeSystemConfigurationPrecheck job finished." )
        }
    }

    boolean checkValueOfKeys(String[] keys) {
        boolean success = Boolean.TRUE
        for (int i = 0; i < keys?.size(); i++) {
            if (keys[i] == null || keys[i].equals("")) {
                success = Boolean.FALSE;
                break;
            }
        }
        return success
    }

    boolean exists(String[] folders) {
        boolean success = Boolean.TRUE
        for (int i = 0; i < folders?.size(); i++) {
            Path path = Paths.get(folders[i]);
            if (!Files.exists(path)) {
                success = Boolean.FALSE;
                break;
            }
        }
        return success
    }

    List<SystemPrecheckEmail> updateAppPrechecksFromMart(Map data) {
        log.debug("updateAppPrechecksFromMart method is calling...")
        String type = data.get("type")
        if (type == null || type.equals("")) {
            type = Constants.SystemPrecheck.SAFETY
        }
        List<SystemPrecheckEmail> systemPrecheckEmailList = data.get("systemPrecheckEmailList")
        log.debug("Before calling db procedure.")
        List<Map> result = getDbStatus(type, data.get("dataSource"), data.get("bootType"), true)
        log.debug("Result from db : "+result)
        for(int k=0;k<result?.size();k++){
            Map it=result[k]
            SystemPreConfig systemPreConfig = null
            if (data.get("bootType") == true) {
                systemPreConfig = SystemPreConfig.findByNameAndDbTypeAndAppType(it.name, type, 'application')
            } else {
                systemPreConfig = SystemPreConfig.findByNameAndDbTypeAndAppType(it.name, type, 'database')
            }
            if (!systemPreConfig) {
                systemPreConfig = new SystemPreConfig()
            }
            systemPreConfig.running = it.running
            systemPreConfig.name = it.name
            systemPreConfig.displayName = it.precheckName
            systemPreConfig.optional = it.optional
            systemPreConfig.warning = it.warning
            systemPreConfig.reason = it.comments
            systemPreConfig.appType = it.appType
            systemPreConfig.alertType = it.alertType
            systemPreConfig.dbType = type
            systemPreConfig.entityType = it.entityType
            systemPreConfig.entityKey = it.entityKey
            systemPreConfig.validationLevel = it.validationLevel
            systemPreConfig.orderSeq = Integer.parseInt(it.orderSeq)


            switch (systemPreConfig.name) {
                case Constants.SystemPrecheck.PVR:
                    try {
                        systemPreConfig.running = healthService.isRunning(Holders.config.pvreports.url + Holders.config.pvreports.healthCheck.uri)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " URL"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.PVCC:
                    try {
                        systemPreConfig.running = healthService.isRunning(Holders.config.pvcc.api.url + Holders.config.pvcc.api.healthcheck)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " URL"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.URL_LITERATURE:
                    try {
                        String literatureArticleUrl = Holders.config.app.literature.article.url
                        systemPreConfig.running = healthService.isRunning(literatureArticleUrl)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " URL"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.PVS:
                    try {
                        systemPreConfig.running = healthService.isDatabaseRunning(Constants.SystemPrecheck.PVS.toString())
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " connection"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.SAFETY:
                    try {
                        systemPreConfig.running = healthService.isDatabaseRunning(Constants.SystemPrecheck.SAFETY.toString())
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " connection"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.FAERS:
                    try {
                        systemPreConfig.running = healthService.isDatabaseRunning(Constants.SystemPrecheck.FAERS.toString())
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " connection"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.EVDAS:
                    try {
                        systemPreConfig.running = healthService.isDatabaseRunning(Constants.SystemPrecheck.EVDAS.toString())
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " connection"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.VAERS:
                    try {
                        systemPreConfig.running = healthService.isDatabaseRunning(Constants.SystemPrecheck.VAERS.toString())
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " connection"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.VIGIBASE:
                    try {
                        systemPreConfig.running = healthService.isDatabaseRunning(Constants.SystemPrecheck.VIGIBASE.toString())
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " connection"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.DSS:
                    try {
                        systemPreConfig.running = healthService.isRunning(Holders.config.dss.scores.url + "dss/")
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " URL"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.EBGM:
                    try {
                        systemPreConfig.running = healthService.isRunning(Holders.config.statistics.url + Holders.config.statistics.path.algoCheck)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " URL"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.IMPORT_CONFIG_FOLDER:
                    try {
                        String[] folders = [Holders.config.signal.configuration.import.folder.base, Holders.config.signal.configuration.import.folder.read, Holders.config.signal.configuration.import.folder.upload, Holders.config.signal.configuration.import.folder.fail, Holders.config.signal.configuration.import.folder.logs]
                        systemPreConfig.running = exists(folders)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " directories"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.IMPORT_PRODUCT_ASSIGNEMENT_FOLDER:
                    try {
                        String[] folders = [Holders.config.signal.product.assignment.import.folder.base, Holders.config.signal.product.assignment.import.folder.read, Holders.config.signal.product.assignment.import.folder.upload, Holders.config.signal.product.assignment.import.folder.logs, Holders.config.signal.product.assignment.import.folder.fail]
                        systemPreConfig.running = exists(folders)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " directories"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;


                case Constants.SystemPrecheck.SIGNAL_MANAGEMENT_FOLDER:
                    try {
                        String[] folders = [Holders.config.grails.attachmentable.uploadDir]
                        systemPreConfig.running = exists(folders)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " directories"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.SPOTFIRE_KEYS:
                    try {
                        String[] keys = [Holders.config.spotfire.server, Holders.config.spotfire.port, Holders.config.spotfire.path, Holders.config.spotfire.callbackUrl, Holders.config.spotfire.filename, Holders.config.spotfire.analysisRoot, Holders.config.spotfire.libraryRoot, Holders.config.spotfire.libraryFolder, Holders.config.spotfire.wsdl_location]
                        systemPreConfig.running = checkValueOfKeys(keys)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in Spotfire Configuration"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;


                case Constants.SystemPrecheck.ERMR_FOLDER:
                    try {
                        String[] folders = [Holders.config.signal.evdas.data.import.folder.base, Holders.config.signal.evdas.data.import.folder.read, Holders.config.signal.evdas.data.import.folder.upload, Holders.config.signal.evdas.data.import.folder.success, Holders.config.signal.evdas.data.import.folder.fail]
                        systemPreConfig.running = exists(folders)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " directories"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;


                case Constants.SystemPrecheck.CASE_LINE_LISTING_FOLDER:
                    try {
                        String[] folders = [Holders.config.signal.evdas.case.line.listing.import.folder.base,
                                            Holders.config.signal.evdas.case.line.listing.import.folder.read,
                                            Holders.config.signal.evdas.case.line.listing.import.folder.upload,
                                            Holders.config.signal.evdas.case.line.listing.import.folder.success,
                                            Holders.config.signal.evdas.case.line.listing.import.folder.fail]

                        systemPreConfig.running = exists(folders)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " directories"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.EVDAS_FOLDER:
                    try {
                        String[] folders = [Holders.config.signal.evdas.data.import.folder.read,
                                            Holders.config.signal.evdas.data.import.folder.upload,
                                            Holders.config.signal.evdas.data.import.folder.success,
                                            Holders.config.signal.evdas.data.import.folder.fail]

                        systemPreConfig.running = exists(folders)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " directories"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;


                case Constants.SystemPrecheck.RAM:
                    try {
                        int MB = 1024 * 1024;
                        int KB = 1024;
                        Runtime runtime = Runtime.getRuntime();
                        long freeMemory = runtime.freeMemory() / MB / KB
                        long totalMemory = runtime.totalMemory() / MB / KB
                        systemPreConfig.reason = "Available memory in application server : <br><b>Available memory in GB:</b> " + new DecimalFormat(".000").format(freeMemory) + "<br><b>Total memory in GB:</b> " + new DecimalFormat(".000").format(totalMemory)
                        if ((freeMemory / totalMemory) * 100 > Long.parseLong(it.threshHoldPercentage)) {
                            systemPreConfig.warning = false
                            systemPreConfig.running = true
                        } else {
                            systemPreConfig.warning = true
                            systemPreConfig.running = false
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.SPOTFIRE:
                    try {
                        String host = Holders.config.spotfire.automationProtocol + "://" + Holders.config.spotfire.server + ":" + Holders.config.spotfire.port
                        String url = "/spotfire/login.html"
                        systemPreConfig.running = healthService.isRunning(host + url)
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " URL"
                        } else {
                            systemPreConfig.reason = "Success"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                case Constants.SystemPrecheck.SPOTFIRE_CONNECTION:
                    try {
                        systemPreConfig.running = healthService.isDatabaseRunning(Constants.SystemPrecheck.SPOTFIRE.toString())
                        if (!systemPreConfig.running) {
                            systemPreConfig.reason = "Error in " + systemPreConfig.displayName + " connection"
                        } else {
                            systemPreConfig.reason = "Success"
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                    break;

                default: break;
            }
            getTableSpaceMessage(systemPreConfig, it, type)
            if (systemPreConfig && !systemPreConfig?.running && (systemPreConfig?.optional == false || systemPreConfig?.warning == false)) {
                SystemPrecheckEmail systemPrecheckEmail = new SystemPrecheckEmail()
                systemPrecheckEmail.name = systemPreConfig?.name
                systemPrecheckEmail.reason = systemPreConfig?.reason
                systemPrecheckEmail.emailSent = false
                systemPrecheckEmail.dateCreated = new Date()
                systemPrecheckEmail.appType = 'application'
                systemPrecheckEmail.dbType = Constants.SystemPrecheck.SAFETY
                systemPrecheckEmailList.add(systemPrecheckEmail)
            }
            log.debug("Email list added to systemPrecheckEmailList.")
            try {
                systemPreConfig.save(flush: true)
            } catch (Exception ex) {
                ex.printStackTrace()
            }
            log.debug("Pre-Check name is : "+systemPreConfig.name+" & running value is : "+systemPreConfig.running)
        }
        log.debug("updateAppPrechecksFromMart execution finished.")

        systemPrecheckEmailList
    }

    void updatePreCheckTableSpaceTime() {
        log.info("updatePreCheckTableSpaceTime execution started.")
        List<SystemPreConfig> systemPreConfigList = SystemPreConfig.findAllByAppTypeAndEntityKey('database', 'DB_CHECKS')
        String timezone = null
        String previousUtcTableSpaceTime = null, currentUsersZoneTableSpaceTime = null
        try {
            timezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        } catch (Exception ex) {
            timezone = Constants.UTC
        }
        for (int i = 0; i < systemPreConfigList?.size(); i++) {
            try {
                if (systemPreConfigList[i]?.name?.contains("DB_FREE_SPACE")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.PRECHECK_DATETIME_FMT_AM_PM, Locale.ENGLISH);
                    Date previousUtcTime = Date.from(LocalDateTime.parse(systemPreConfigList[i]?.tableSpaceTime, formatter).atZone(ZoneId.systemDefault()).toInstant());
                    previousUtcTableSpaceTime = DateUtil.stringFromDate(previousUtcTime, DateUtil.DATEPICKER_FORMAT_AM_PM_3, Constants.UTC)
                    Date currentUsersZoneTime = Date.from(LocalDateTime.parse(systemPreConfigList[i]?.tableSpaceTime, formatter).atZone(ZoneId.systemDefault()).toInstant());
                    currentUsersZoneTableSpaceTime = DateUtil.stringFromDate(currentUsersZoneTime, DateUtil.DATEPICKER_FORMAT_AM_PM_3, timezone)
                    systemPreConfigList[i]?.reason = systemPreConfigList[i]?.reason?.replaceAll(previousUtcTableSpaceTime, currentUsersZoneTableSpaceTime)
                    systemPreConfigList[i]?.save()
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
        log.info("updatePreCheckTableSpaceTime execution finished.")
    }

    String getTableSpaceMessage(SystemPreConfig systemPreConfig, Map it, String type, boolean callingFromLogin = false) {
        if (systemPreConfig?.name?.contains("DB_FREE_SPACE")) {
            String tableSpaceTime = null
            if (it.LAST_INS_UPD_DT != null) {
                String timezone = null
                try {
                    timezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
                } catch (Exception ex) {
                    timezone = Constants.UTC
                }
                String stringTime = it.LAST_INS_UPD_DT
                systemPreConfig.tableSpaceTime = stringTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.PRECHECK_DATETIME_FMT_AM_PM, Locale.ENGLISH);
                Date lastInstUpdatedDate = Date.from(LocalDateTime.parse(systemPreConfig.tableSpaceTime, formatter).atZone(ZoneId.systemDefault()).toInstant());
                tableSpaceTime = DateUtil.stringFromDate(lastInstUpdatedDate, DateUtil.DATEPICKER_FORMAT_AM_PM_3, timezone)
            }
            String tableSpaceMessage = ""
            if (type.equals(Constants.SystemPrecheck.SAFETY)) {
                tableSpaceMessage = "<b>Safety DB tablespaces status as of </b>" + tableSpaceTime + "<br>"
            } else if (type.equals(Constants.SystemPrecheck.FAERS)) {
                tableSpaceMessage = "<b>FAERS DB tablespaces status as of </b>" + tableSpaceTime + " <br>"
            } else if (type.equals(Constants.SystemPrecheck.VAERS)) {
                tableSpaceMessage = "<b>VAERS DB tablespaces status as of </b>" + tableSpaceTime + " <br>"
            } else if (type.equals(Constants.SystemPrecheck.VIGIBASE)) {
                tableSpaceMessage = "<b>VigiBase DB tablespaces status as of </b>" + tableSpaceTime + "<br>"
            } else if (type.equals(Constants.SystemPrecheck.EVDAS)) {
                tableSpaceMessage = "<b>EVDAS DB tablespaces status as of </b>" + tableSpaceTime + " <br>"
            }
            systemPreConfig.reason = tableSpaceMessage + systemPreConfig.reason
        }
    }

    List<SystemPrecheckEmail> insertDbStatus(Map data) {
        String type = data.get("type")
        if (type == null || type.equals("")) {
            type = Constants.SystemPrecheck.SAFETY
        }
        List<Map> result = getDbStatus(type, data.get("dataSource"), data.get("bootType"), false)
        List<SystemPrecheckEmail> systemPrecheckEmailList = data.get("systemPrecheckEmailList")
        for(int k=0;k<result?.size();k++){
            Map it=result[k]
            SystemPreConfig systemPreConfig = null
            if (data.get("bootType") == true) {
                systemPreConfig = SystemPreConfig.findByNameAndDbTypeAndAppType(it.name, type, 'application')
            } else {
                systemPreConfig = SystemPreConfig.findByNameAndDbTypeAndAppType(it.name, type, 'database')
            }
            if (!systemPreConfig) {
                systemPreConfig = new SystemPreConfig()
            }
            systemPreConfig.running = it.running
            systemPreConfig.name = it.name
            systemPreConfig.displayName = it.precheckName
            systemPreConfig.optional = it.optional
            systemPreConfig.warning = it.warning
            systemPreConfig.reason = it.comments
            systemPreConfig.appType = it.appType
            systemPreConfig.alertType = it.alertType
            systemPreConfig.dbType = type
            systemPreConfig.entityType = it.entityType
            systemPreConfig.entityKey = it.entityKey
            systemPreConfig.validationLevel = it.validationLevel
            systemPreConfig.orderSeq = Integer.parseInt(it.orderSeq)
            getTableSpaceMessage(systemPreConfig, it, type)
            if (systemPreConfig && !systemPreConfig?.running && (systemPreConfig?.optional == false || systemPreConfig?.warning == false)) {
                SystemPrecheckEmail systemPrecheckEmail = new SystemPrecheckEmail()
                systemPrecheckEmail.name = systemPreConfig?.name
                systemPrecheckEmail.reason = systemPreConfig?.reason
                systemPrecheckEmail.emailSent = false
                systemPrecheckEmail.dateCreated = new Date()
                systemPrecheckEmail.appType = systemPreConfig.appType
                systemPrecheckEmail.dbType = systemPreConfig.dbType
                systemPrecheckEmailList?.add(systemPrecheckEmail)
            }
            try {
                systemPreConfig.save()
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
        log.debug("insertDbStatus execution finished for datasource : "+type)
        systemPrecheckEmailList
    }


    void initializeMartPrechecks(Map data) {
        String type = data.get("type")
        if (type == null || type.equals("")) {
            type = Constants.SystemPrecheck.SAFETY
        }
        List<Map> result = getDbStatus(type, data.get("dataSource"), data.get("bootType"), false)
        for(int k=0;k<result?.size();k++){
            Map it=result[k]
            SystemPreConfig systemPreConfig = SystemPreConfig.findByNameAndDbType(it?.name, type)
            if (!systemPreConfig) {
                systemPreConfig = new SystemPreConfig()
            }
            systemPreConfig.running = it.running
            it.precheckName = it.precheckName
            systemPreConfig.name = it.name
            systemPreConfig.displayName = it.precheckName
            systemPreConfig.optional = it.optional
            systemPreConfig.warning = it.warning
            systemPreConfig.reason = it.comments
            systemPreConfig.appType = it.appType
            systemPreConfig.alertType = it.alertType
            systemPreConfig.dbType = type
            systemPreConfig.entityType = it.entityType
            systemPreConfig.entityKey = it.entityKey
            systemPreConfig.validationLevel = it.validationLevel
            systemPreConfig.orderSeq = Integer.parseInt(it.orderSeq)
            getTableSpaceMessage(systemPreConfig, it, type)
            try {
                systemPreConfig.save()
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
        log.debug("initializeMartPrechecks execution finished for datasource : ${type}")
    }

    def getDbStatus(String type, def dataSource, boolean bootType = false, boolean appPrechecks = false) {
        log.debug("getDbStatus execution started for datasource : "+type)
        def sql = new Sql(dataSource)
        def rowList = []
        try {
            log.debug("PKG_PVS_SYSTEM_PRECHECKS.F_PVS_DB_PRECHECKS procedure call started.")
            sql.call("{?= call PKG_PVS_SYSTEM_PRECHECKS.F_PVS_DB_PRECHECKS()}",
                    [Sql.NUMERIC]) { res ->
                List<GroovyRowResult> data = []
                if (res == 1) {
                    if (bootType == true) {
                        if (appPrechecks) {
                            data = sql.rows("SELECT a.INPUT_DATA,a.VALIDATION_LEVEL,a.UI_LABEL, a.PRECHECK_NAME, a.STATUS, a.ADDITIONAL_INFO, a.IMPACTED_MODULE , a.FLAG_MANDATORY , a.MODULE_KEY, b.MODULE_NAME, b.MODULE_ORDER, b.MODULE_DESCRIPTION FROM pvs_system_prechecks a JOIN pvs_system_prechecks_modules b ON ( upper(TRIM(a.module_key)) = upper(TRIM(b.module_key))) where a.SELECTABLE=1 and a.VALIDATION_LEVEL in ('PVS_APP')")
                        } else {
                            data = sql.rows("SELECT a.INPUT_DATA,a.VALIDATION_LEVEL,a.UI_LABEL, a.PRECHECK_NAME, a.STATUS, a.ADDITIONAL_INFO, a.IMPACTED_MODULE , a.FLAG_MANDATORY , a.MODULE_KEY, b.MODULE_NAME, b.MODULE_ORDER, b.MODULE_DESCRIPTION FROM pvs_system_prechecks a JOIN pvs_system_prechecks_modules b ON ( upper(TRIM(a.module_key)) = upper(TRIM(b.module_key))) where a.SELECTABLE=1")
                        }
                    } else {
                        data = sql.rows("SELECT a.INPUT_DATA,a.VALIDATION_LEVEL,a.UI_LABEL, a.PRECHECK_NAME, a.STATUS, a.ADDITIONAL_INFO, a.IMPACTED_MODULE , a.FLAG_MANDATORY , a.MODULE_KEY, b.MODULE_NAME, b.MODULE_ORDER, b.MODULE_DESCRIPTION FROM pvs_system_prechecks a JOIN pvs_system_prechecks_modules b ON ( upper(TRIM(a.module_key)) = upper(TRIM(b.module_key))) where a.SELECTABLE=1 and a.VALIDATION_LEVEL in ('PVS_DB','DB')")
                    }
                    for(int k=0;k<data?.size();k++){
                        Map it=data[k]
                        def tempMap = [:]
                        String precheckName = ""
                        String name = it.getProperty("PRECHECK_NAME")
                        String running = it.getProperty("STATUS")
                        String comments = it.getProperty("ADDITIONAL_INFO")
                        String alertType = it.getProperty("IMPACTED_MODULE")
                        oracle.sql.CLOB threshHoldPercentage = it.getProperty("INPUT_DATA")
                        String flagMandatory = it.getProperty("FLAG_MANDATORY")
                        String moduleKey = it.getProperty("MODULE_KEY")
                        String moduleName = it.getProperty("MODULE_NAME")
                        String moduleOrder = it.getProperty("MODULE_ORDER")
                        String moduleDescription = it.getProperty("MODULE_DESCRIPTION")
                        String validationLevel = it.getProperty("VALIDATION_LEVEL")
                        if (name?.contains("DB_FREE_SPACE")) {
                            sql.rows("select sys_extract_utc(TO_TIMESTAMP_TZ(to_char(min(last_ins_upd_dt), 'DD-Mon-YY HH12:MI:SS PM')||' '||to_char(systimestamp,'TZR'))) as utc_tz from pvs_tablespace_precheck").each {
                                tempMap.put("LAST_INS_UPD_DT", it.getProperty("utc_tz"))
                            }
                        }
                        boolean isOptional = false, isWarning = false, isRunning = false;
                        if (flagMandatory == 0 || flagMandatory.equals("0")) {
                            isOptional = true
                        }
                        if (moduleKey.equals("DB_CHECKS")) {
                            String dbLabel = ""
                            if (type.equals(Constants.SystemPrecheck.SAFETY)) {
                                dbLabel = "Safety DB";
                            } else if (type.equals(Constants.SystemPrecheck.FAERS)) {
                                dbLabel = "FAERS";
                            } else if (type.equals(Constants.SystemPrecheck.EVDAS)) {
                                dbLabel = "EVDAS";
                            } else if (type.equals(Constants.SystemPrecheck.VAERS)) {
                                dbLabel = "VAERS";
                            } else if (type.equals(Constants.SystemPrecheck.VIGIBASE)) {
                                dbLabel = "VigiBase";
                            } else if (type.equals(Constants.SystemPrecheck.JADER)) {
                                dbLabel = "JADER";
                            }
                            if (name.equals(Constants.SystemPrecheck.RAM)) {
                                precheckName = customMessageService.getMessage('precheck.ram.details')
                            } else {
                                precheckName = it.getProperty("UI_LABEL") + " - " + dbLabel
                            }
                        } else {
                            precheckName = it.getProperty("UI_LABEL")
                        }

                        if (validationLevel.equals("PVS_APP")) {
                            tempMap.put("appType", 'application')
                        } else {
                            tempMap.put("appType", 'database')
                        }

                        if (running == -1 || running == "-1") {
                            if (flagMandatory == 0 || flagMandatory.equals("0")) {
                                //If status is -1 and flagManadatory =0 then it will be consdered as warning
                                isOptional = true
                                isWarning = true

                            } else if (flagMandatory == 1 || flagMandatory.equals("1")) {
                                //If status is -1 and flagManadatory =1 then it will be consdered as error
                                isOptional = false
                                isWarning = false
                            } else {
                                isOptional = false
                                isWarning = false
                            }
                        } else if (running == 1 || running.equals("1")) {
                            isRunning = true
                        } else if (running == 0 || running.equals("0")) {
                            isRunning = false
                        } else if (running == -999 || running.equals("-999")) {
                            isRunning = true
                            isOptional = true
                            isWarning = true
                        } else {
                            isRunning = false
                        }

                        if (!validationLevel.equals("PVS_APP")) {
                            name = name + "_" + type?.toUpperCase()
                        }

                        tempMap.put("name", name?.toUpperCase())
                        tempMap.put("running", isRunning)
                        tempMap.put("optional", isOptional)
                        tempMap.put("warning", isWarning)
                        tempMap.put("comments", comments)
                        tempMap.put("precheckName", precheckName)
                        tempMap.put("alertType", alertType)
                        tempMap.put("entityKey", moduleKey)
                        tempMap.put("entityType", moduleName)
                        tempMap.put("orderSeq", moduleOrder)
                        tempMap.put("threshHoldPercentage", threshHoldPercentage?.asciiStream?.text)

                        if (addPrecheckIfEnabled(name)) {
                            rowList.add(tempMap)
                        }

                    }

                }
            }


            log.debug("PKG_PVS_SYSTEM_PRECHECKS.F_PVS_DB_PRECHECKS procedure call completed.")
        } catch (Exception exception) {
            exception.printStackTrace()
        } finally {
            sql.close()
            log.debug("getDbStatus execution finished for datasource : "+type)
        }
        rowList
    }

    boolean addPrecheckIfEnabled(String name) {
        boolean add = true
        if (name.equals(Constants.SystemPrecheck.VIGIBASE) && !Holders.config.signal.vigibase.enabled) {
            add = false
        } else if (name.equals(Constants.SystemPrecheck.FAERS) && !Holders.config.signal.faers.enabled) {
            add = false
        } else if ((name.equals(Constants.SystemPrecheck.EVDAS) || name.equals(Constants.SystemPrecheck.ERMR_FOLDER) || name.equals(Constants.SystemPrecheck.CASE_LINE_LISTING_FOLDER) || name.equals(Constants.SystemPrecheck.EVDAS_FOLDER)) && !Holders.config.signal.evdas.enabled) {
            add = false
        } else if (name.equals(Constants.SystemPrecheck.VAERS) && !Holders.config.signal.vaers.enabled) {
            add = false
        } else if ((name.equals(Constants.SystemPrecheck.SPOTFIRE) || name.equals(Constants.SystemPrecheck.SPOTFIRE_CONNECTION) || name.equals(Constants.SystemPrecheck.SPOTFIRE_KEYS)) && !Holders.config.signal.spotfire.enabled) {
            add = false
        } else if (name.equals(Constants.SystemPrecheck.EBGM) && !Holders.config.statistics.enable.ebgm) {
            add = false
        } else if (name.equals(Constants.SystemPrecheck.DSS) && !Holders.config.statistics.enable.dss) {
            add = false
        }
        return add
    }

    Map<String, List<Map>> preCheckList() {
        List preCheckInitialList = SystemPreConfig.findAll()
        for (int i = 0; i < preCheckInitialList.size(); i++) {
            if (!addPrecheckIfEnabled(preCheckInitialList.get(i).name)) {
                preCheckInitialList.remove(preCheckInitialList.get(i))
            }
        }
        List preCheckList = preCheckInitialList.collect {
            [
                    name      : it.name, displayName: it.displayName,
                    running   : it.running,
                    optional  : it.optional,
                    enabled   : it.enabled,
                    warning   : it.warning,
                    reason    : it.reason, appType: it.appType,
                    entityType: it.entityType?.trim(), orderSeq: it.orderSeq,
                    entityKey : it.entityKey?.trim()
            ]
        }.sort { it.orderSeq }.sort { it.running }
        Map<String, List<Map>> preCheckMap = preCheckList?.groupBy {
            it["entityType"]?.toString()?.trim()
        }
        Map<String, List<Map>> runningPreCheckMap = [:]
        Map<String, List<Map>> notRunningPreCheckMap = [:]
        Map<String, List<Map>> optionalPreCheckMap = [:]
        preCheckMap.each {
            if (checkIfPrecheckHeaderHasOptional(it.value)) {
                optionalPreCheckMap.put(it.key + "_" + checkIfPrecheckHeaderRunning(it.value) + "_" + checkIfPrecheckHeaderHasOptional(it.value), it.value)
            } else if (checkIfPrecheckHeaderRunning(it.value)) {
                runningPreCheckMap.put(it.key + "_" + checkIfPrecheckHeaderRunning(it.value) + "_" + checkIfPrecheckHeaderHasOptional(it.value), it.value)
            } else {
                notRunningPreCheckMap.put(it.key + "_" + checkIfPrecheckHeaderRunning(it.value) + "_" + checkIfPrecheckHeaderHasOptional(it.value), it.value)
            }
        }
        return notRunningPreCheckMap + optionalPreCheckMap + runningPreCheckMap
    }

    boolean checkIfPrecheckHeaderRunning(List<Map> lm) {
        boolean flag = true;
        lm.each {
            if (!it.running) {
                flag = false
                return
            }
        }
        return flag
    }


    boolean checkIfPrecheckHeaderHasOptional(List<Map> lm) {
        boolean flag = true;
        lm.each {
            if (!it.optional && !it.running) {
                flag = false
                return
            }
        }
        return flag
    }


    boolean isRunning(String name, String dbType) {
        if (dbType) {
            return SystemPreConfig.findByNameAndDbType(name.toUpperCase(), dbType)?.running
        } else {
            return SystemPreConfig.findByName(name.toUpperCase())?.running
        }
    }
}
