

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

grails.config.locations = ["application_4.groovy","application_3.groovy","application_two.groovy" , "file:${userHome}/.signal/config.groovy"]

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'raw' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType.'text/html' = 'html'
    }
}

// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

environments {
    development {
        grails.logging.jul.usebridge = true
        grails.plugin.console.enabled = true


    }
    production {
        grails.logging.jul.usebridge = false
        grails.plugin.console.enabled = true

    }
    demo {
        grails.logging.jul.usebridge = true
        grails.plugin.console.enabled = true


    }
    test {
        grails.mail.enabled = false
    }
}

hibernate {
    flush.mode = 'COMMIT'
    cache {
        queries = false
        use_second_level_cache = true
        use_query_cache = true
        setProperty 'region.factory_class', 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory'
    }
    //This is to resolve hibernate many-to-many lazy loading issue
    enable_lazy_load_no_trans = true
    jdbc {
        use_get_generated_keys = true
    }
    format_sql = true
    use_sql_comments = true
    logSql = false
    singleSession = true
    order_inserts = true
    batch_versioned_data = true
    closureEventTriggeringInterceptorClass = com.rxlogix.CustomClosureEventTriggeringInterceptor
}

oracleProperties {
    jmxEnabled = true
    initialSize = 30
    maxActive = 130
    minIdle = 30
    maxIdle = 50
    maxWait = 10000
    maxAge = 10 * 60000
    timeBetweenEvictionRunsMillis = 5000
    minEvictableIdleTimeMillis = 60000
    validationQuery = "SELECT 1 FROM DUAL"
    validationQueryTimeout = 3
    validationInterval = 15000
    testOnBorrow = true
    testWhileIdle = true
    testOnReturn = false
    //https://github.com/grails/grails-core/issues/3017
    // controls for leaked connections
    abandonWhenPercentageFull = 80 // settings are active only when pool is 80% full
    removeAbandonedTimeout = 120
    removeAbandoned = true
    jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
    defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
}

oracleProperties_mart {
    jmxEnabled = true
    initialSize = 5
    maxActive = 500
    minIdle = 10
    maxIdle = 15
    maxWait = 10000
    maxAge = 10 * 60000
    timeBetweenEvictionRunsMillis = 5000
    minEvictableIdleTimeMillis = 60000
    validationQuery = "SELECT 1 FROM DUAL"
    validationQueryTimeout = 3
    validationInterval = 15000
    testOnBorrow = true
    testWhileIdle = true
    testOnReturn = false
    //https://github.com/grails/grails-core/issues/3017
    // controls for leaked connections
    abandonWhenPercentageFull = 80 // settings are active only when pool is 80% full
    removeAbandonedTimeout = 120
    removeAbandoned = true
    jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
    defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
}

environments {
    development {

        //Quartz properties
        quartz {
            scheduler.instanceName = "PVS_INSTANCE_SCHEDULER"
            scheduler.misfirePolicy =  "doNothing"
            threadPool.class = "org.quartz.simpl.SimpleThreadPool"
            threadPool.threadCount = 30
            threadPool.threadPriority = 5
            threadPool.makeThreadsDaemons = true
            autoStartup = true
            jdbcStore = false
            jobStore.misfireThreshold = 5000
        }

        dataSources {
            dataSource {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@localhost:1521/orcl'
                username = 'pvs'
                password = '{cipher}263nqfc4c+iVWqUjahazWA=='
                properties = oracleProperties
            }
            pva {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = "org.hibernate.dialect.Oracle12cDialect"
                url = 'jdbc:oracle:thin:@10.100.22.80:1521/PVSDEVDB'
                username = "PVS_DB_PVS61"
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            faers {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.54:1521/FAERSDB'
                username = 'PVS_FAERS_DB_55JULY'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            eudra {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.80:1521/PVSDEVDB'
                username = 'PVS_EVD_DB_55JULY'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            spotfire {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = "org.hibernate.dialect.Oracle10gDialect"
                url = "jdbc:oracle:thin:@10.100.22.212:1521/PVRDEMO"
                username = "spotfireadmin1141"
                password = "{cipher}ey2xVYoxVUcHjLac81Ddhw=="
                passwordEncryptionCodec="com.rxlogix.RxCodec"
                readOnly = true
                properties = oracleProperties_mart
            }
            vaers {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.99:1521/PVSDEVDB'
                username = 'PVS_VAERS_17_AUG'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            vigibase {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.99:1521/PVSDEVDB'
                username = 'PVS_VB_IT'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            jader {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.80:1521/PVSDEVDB'
                username = 'PVS_JADER_DEV_DRF_AWS'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
        }
    }
    test {
        dataSources {
            dataSource {
                dbCreate = "create-drop"
                url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
            }
            pva {
                dbCreate = "create-drop"
                url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
            }
        }
    }
    production {

        //Quartz properties
        quartz {
            scheduler.instanceName = "PVS_INSTANCE_SCHEDULER"
            scheduler.misfirePolicy =  "doNothing"
            threadPool.class = "org.quartz.simpl.SimpleThreadPool"
            threadPool.threadCount = 30
            threadPool.threadPriority = 5
            threadPool.makeThreadsDaemons = true
            jobStore.misfireThreshold = 5000
            autoStartup = true
            jdbcStore = false
        }

        dataSources {
            dataSource {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@localhost:1521/orcl'
                username = 'pvs_qa'
                password = 'rxlogix'
                properties = oracleProperties
            }
            pva {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.212:1521/PVSDEVDB'
                username = 'evdas_app_06'
                password = 'rxlogix'
                readOnly = true
                properties = oracleProperties
            }
            faers {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.54:1521/FAERSDB'
                username = 'PVS_FAERS_DB_55JULY'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            eudra {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.80:1521/PVSDEVDB'
                username = 'PVS_EVD_DB_55JULY'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            spotfire {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = "org.hibernate.dialect.Oracle10gDialect"
                url = "jdbc:oracle:thin:@10.100.22.212:1521/PVRDEMO"
                username = "spotfireadmin1141"
                password = "{cipher}ey2xVYoxVUcHjLac81Ddhw=="
                passwordEncryptionCodec="com.rxlogix.RxCodec"
                readOnly = true
                properties = oracleProperties_mart
            }
            vaers {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.99:1521/PVSDEVDB'
                username = 'PVS_VAERS_17_AUG'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            vigibase {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.99:1521/PVSDEVDB'
                username = 'PVS_VB_IT'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
            jader {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = 'org.hibernate.dialect.Oracle12cDialect'
                url = 'jdbc:oracle:thin:@10.100.22.80:1521/PVSDEVDB'
                username = 'PVS_JADER_DEV_1'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties = oracleProperties_mart
            }
        }
    }
}

//Evdas in the application can be turned off and on based on the customer's need.

signal.evdasconfig = "eudra"

pvsignal.default.app.user = "signaldev"

server {
    compression {
        enabled = true
        setProperty 'mime-types', 'application/json,application/xml,text/html,text/xml,text/plain'
    }
}

// OUTLOOK CONFIGURATIONS
outlook {
    appId = '9b5b182d-5eea-443e-96f2-1b26e8a5d42b'
    secret = 'trqgILZ3]%qslYJRC0023*='
    redirectUrl = 'http://localhost:8080/signal/outlook/response'
    authorizeUrl = 'https://login.microsoftonline.com'
    apiUrl = 'https://outlook.office.com'
    scopes = [
            "openid",
            "https://outlook.office.com/calendars.read",
            "https://outlook.office.com/calendars.read.shared",
    ]
}

grails.gorm.default = {
    mapping = {
        id column: "ID"
        dateCreated column: "DATE_CREATED"
        lastUpdated column: "LAST_UPDATED"
        createdBy column: "CREATED_BY"
        modifiedBy column: "MODIFIED_BY"
        version column: "VERSION"
    }

    constraints = {
        createdBy maxSize: 50
        modifiedBy maxSize: 50
        oldValue maxSize: 262136
        newValue maxSize: 262136
    }
}

grails {
    assets {
        handlebars {
            templateRoot = 'hbs-templates'
            templatePathSeperator = "/"
        }
    }
}

// log4j configuration
log4j.main = {
    appenders {
        console name: 'stdout'

        environments {
            cloudbees {
                String tmpDir = System.getProperty("java.io.tmpdir")
                console name: 'app', layout: pattern(conversionPattern: '%d [%X{user} : %t] %-5p %c{8} %x - %m%n')
                file name: 'performance', file: '$tmpDir/logs/performance.log', layout: pattern(conversionPattern: '%d [%X{user}] %m%n')
            }
            production {
                file name: 'app', file: 'logs/pvsignal.log', layout: pattern(conversionPattern: '%d [%X{user} : %t] %-5p %c{8} %x - %m%n')
                file name: 'performance', file: 'logs/performance.log', layout: pattern(conversionPattern: '%d [%X{user}] %m%n')
            }
            development {
                console name: 'app', layout: pattern(conversionPattern: '%d [%X{user} : %t] %-5p %c{8} %x - %m%n')
                file name: 'performance', file: 'logs/performance.log', layout: pattern(conversionPattern: '%d [%X{user}] %m%n')
            }
            demo {
                console name: 'app', layout: pattern(conversionPattern: '%d [%X{user} : %t] %-5p %c{8} %x - %m%n')
                file name: 'performance', file: 'logs/performance.log', layout: pattern(conversionPattern: '%d [%X{user}] %m%n')
            }

            test {
                console name: 'stdout', layout: pattern(conversionPattern: '%d [%X{user} : %t] %-5p %c{8} %x - %m%n')
            }
        }
        'null' name: 'stacktrace' // this will kill the stacktrace.log
    }

    root {
        error 'app'
        additivity = false
    }

    debug 'grails.plugin.mail',
            'grails.app.services.grails.plugin.mail'
    'com.sun.mail'
    //   'org.hibernate.SQL'

    info 'grails.app.conf.pvsignal3.BootStrap',
            'grails.app.services.com.rxlogix', // all our services
            'grails.app.controllers.com.rxlogix', // all of our controllers
            'grails.app.jobs', // all jobs
            'com.rxlogix.util.marshalling'

    error 'org.grails.web.servlet',        // controllers
            'org.grails.web.pages',          // GSP
            'org.grails.web.sitemesh',       // layouts
            'org.grails.web.mapping.filter', // URL mapping
            'org.grails.web.mapping',        // URL mapping
            'org.grails.commons',            // core / classloading
            'org.grails.plugins',            // plugins
            'org.grails.orm.hibernate',      // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'
}

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.rxlogix.user.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.rxlogix.user.UserRole'
grails.plugin.springsecurity.authority.className = 'com.rxlogix.user.Role'


grails.plugin.springsecurity.logout.afterLogoutUrl = "/login/auth"
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.rejectIfNoRule = false
grails.plugin.springsecurity.useSecurityEventListener = true
grails.plugin.springsecurity.home = '/login/auth'
grails.plugin.springsecurity.adh.errorPage = null
grails.plugin.springsecurity.fii.rejectPublicInvocations = false
requestCache.createSession = true
grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileName = ['changelog_release_6.1.groovy']

externalDirectory = "${userHome}/.signal/"
tempDirectory = "${System.getProperty("java.io.tmpdir")}/${appName}/"


//TO do: tabel column header help map info from extnal files will be done in later release
//adhocHelpMapFile="adhocHelpMapFile.cfg"
//literatureHelpFile="literatureHelpFile.cfg"
//singleHelpMapFile="singleHelpMapFile.cfg"
//aggregateHelpMapFile="aggregateHelpMapFile.cfg"
//evdasHelpMapFile="evdasHelpMapFile.cfg"



grails.plugin.auditLog.maxExtract= 10000
grails.plugin.auditLog.sectionModuleEntities=['configuration','alertPreExecutionCheck']
grails.plugin.auditLog.auditDomainClassName = 'com.rxlogix.audit.AuditTrail'
grails.plugin.auditLog.auditChildDomainClassName = 'com.rxlogix.audit.AuditTrailChild'
grails.plugin.auditLog.async.enabled = true
audit.custom.module.names = ["Action","Action Configuration","Action Template","Action Type","Ad-Hoc Alert Configuration",
                             "Ad-Hoc Review","Ad-Hoc Review: Action","Ad-Hoc Review: Attachment","Ad-Hoc Review: Export",
                             "Adhoc Aggregate Review: Categories","Adhoc Individual Case Review: Categories","Archived Individual Case Review: Categories",
                             "Adhoc Aggregate Review","Adhoc Aggregate Review: Export",
                             "Adhoc EVDAS Review","Adhoc EVDAS Review: Export","Adhoc Individual Case Review","Adhoc Individual Case Review: Case Form: Export",
                             "Adhoc Individual Case Review: Export","Ad-Hoc Review: Action: Export",
                             "Advanced Filter","Aggregate Alert Configuration","Aggregate Alert Configuration: Execution","Aggregate Review",
                             "Aggregate Review: Archived Alert","Aggregate Review: Action","Aggregate Review: Archived Alert: Action",
                             "Aggregate Review: Archived Alert: Comment","Aggregate Review: Attachment","Aggregate Review: Case Series","Aggregate Review: Categories",
                             "Aggregate Review: Comment","Aggregate Review: Comments: Export","Aggregate Review: Data Analysis","Aggregate Review: Export","Aggregate Review: Report",
                             "Alert Pre-Checks","Alert Pin-Unpin Fields","Archived Aggregate Review: Attachment","Archived Individual Case Review: Attachment","Attachment",
                             "Audit Log Details: Export","Audit Log: Export","Business Rules","Comment Template","Configuration","Control Panel",
                             "Control Panel: Export","Dashboard: Alert Widget","Dashboard: My Links","Disposition","Drug Classification","EVDAS Alert Configuration",
                             "EVDAS Alert Configuration: Execution","EVDAS Data Upload","EVDAS Data Upload: Export","EVDAS Review","EVDAS Review: Archived Alert",
                             "EVDAS Review Dashboard: Export","Aggregate Review Dashboard: Export","EVDAS Review: Action","EVDAS Review: Case Detail: Export","EVDAS Review: Comment",
                             "EVDAS Review: Export","Email Configuration","EVDAS Review: Attachment","Group Management","Import Configuration: Export","Important Events",
                             "Individual Case Alert Configuration","Individual Case Alert Configuration: Execution","Individual Case Review",
                             "Individual Case Review: Archived Alert: Attachment","Individual Case Review: Archived Alert: Comments",
                             "Individual Case Review Dashboard: Export","Individual Case Review: Action","Individual Case Review: Archived Alert","Individual Case Review: Archived Alert: Action",
                             "Individual Case Review: Archived Alert: Export","Individual Case Review: Attachment","Individual Case Review: Case Detail: Export",
                             "Individual Case Review: Case Form: Export","Individual Case Review: Categories","Individual Case Review: Comments","Individual Case Review: Data Analysis",
                             "Individual Case Review: Export","Individual Case Review: Report","Individual Case Review: Report: Export","Individual Case Review: Case Form: Generate",
                             "Justification","Literature Alert Configuration","Literature Alert Configuration: Execution","Literature Review","Literature Review: Archived Alert",
                             "Literature Review: Categories","Literature Review: Action","Literature Review: Attachment","Literature Review: Categories",
                             "Literature Review: Comment","Literature Review: Export","Others","Priority","Product Assignment","Product Assignment Import",
                             "Product Assignment: Export","Product Type Configuration","Product Type Configuration: Export","Report Section: Template","Reporting","Reporting: Export",
                             "Signal","Signal: Activity: Export","Signal: Meeting","Signal: Meetings: Export",
                             "Signal: Signal WorkFlow Log","Signal Memo Configuration","Signal Pin-Unpin Fields","Signal Workflow","Signal Workflow State","Signal: Action",
                             "Signal: Activity: Export","Signal: Ad-Hoc Review Observations","Signal: Aggregate Review Observations","Signal: Communication","Signal: Data Analysis",
                             "Signal: Email","Signal: Export","Signal: Generated Chart","Signal: Individual Case Review Observations","Signal: Literature Review Observations","Signal: RMMs",
                             "Signal: Reference","Substance Frequency","User Group Management","User Management","User Preference","View","Workflow Group Management","Workflow Rule",]

grails {
    plugin {
        auditLog {
            actorClosure = { request, session ->
                if (request.applicationContext.springSecurityService.principal instanceof String) {
                    return [actor   : request.applicationContext.springSecurityService.principal,
                            fullName: request.applicationContext.springSecurityService.principal]
                }
                def username = request.applicationContext.springSecurityService.principal?.username
                def fullName = ""
                if (request.applicationContext.springSecurityService?.isLoggedIn()) {
                    fullName = request.applicationContext.springSecurityService.principal?.fullName
                }
                return [actor: username, fullName: fullName]
            }
        }
    }
}

grails {
    plugin {
        auditLog {
            currentActorTimezoneClosure = { request, session ->
                try {
                    if (request.applicationContext.userService) {
                        return request.applicationContext.userService.getCurrentUserPreference()?.timeZone
                    }
                } catch (e) {
                }
                return 'GMT'
            }
        }
    }
}

grails.plugin.springsecurity.onAbstractAuthenticationFailureEvent = { e, appCtx ->
    appCtx.getBean('auditTrailService').logUserAuthenticationEvent e?.authentication?.name, "LOGIN_FAILED"
}
grails.plugin.springsecurity.onAuthenticationSuccessEvent = { e, appCtx ->
    appCtx.getBean('auditTrailService').logUserAuthenticationEvent e?.authentication?.name, "LOGIN_SUCCESS"
}

spotfire {

    symptomsComanifestation = 'No' // possible values -> 'Yes' || 'No' || 'Yes','No'
    logoutUrl = "https://sf-app-dev.rxlogix.com/spotfire/auth/v1/generic-frontchannel-logout"
    symptomsComanifestationColumnName = 'Symptoms/Co-manifestation'
}

spotfire.operationalReport.name = "Signal Operational Report"
spotfire.operationalReport.url = "${-> grails.util.Holders?.config?.spotfire?.libraryRoot}/${-> grails.util.Holders?.config?.spotfire?.operationalReport?.name}"
spotfire.operationalReport.hidingOptions = ["Status bar"]
spotfire.riskSummaryReport.name = "Signal Operational Report Risk Screen"
spotfire.riskSummaryReport.url = "${-> grails.util.Holders?.config?.spotfire?.libraryRoot}/${-> grails.util.Holders?.config?.spotfire?.riskSummaryReport?.name}"
spotfire.riskSummaryReport.hidingOptions = ["Status bar"]

spotfire.productivityAndComplianceReport.name = "CASE REVIEW COMPLIANCE"
spotfire.productivityAndComplianceReport.url = "${-> grails.util.Holders?.config?.spotfire?.libraryRoot}/${-> grails.util.Holders?.config?.spotfire?.productivityAndComplianceReport?.name}"
spotfire.dataAnalysis.hidingOptions = ["Status bar"]

// enable only when you need it
grails.resources.resourceLocatorEnabled = true
grails.attachmentable.poster.evaluator = { "unknown" }

pvsignal {

    //The map of data source supported inx the application.
    // The key represents the datasource and the value represents what will be the display string for that.

    //Default mart datasource.
    supported.datasource = { ->
        def dataSourceList = []
        dataSourceList << "pva"
        if (grails.util.Holders?.config?.signal?.faers?.enabled) {
            dataSourceList << "faers"
        }
        if (grails.util.Holders?.config?.signal?.vaers?.enabled) {
            dataSourceList << "vaers"
        }
        if (grails.util.Holders?.config?.signal?.vigibase?.enabled) {
            dataSourceList << "vigibase"
        }
        if (grails.util.Holders?.config?.signal?.jader?.enabled) {
            dataSourceList << "jader"
        }
        if (grails.util.Holders?.config?.signal?.evdas?.enabled) {
            dataSourceList << "eudra"
        }

        return dataSourceList
    }

    signalHistoryStatusWithWorkflowState = ["Validation Date","Assessment Date","Date Closed","Safety Observation Validation","Signal Analysis & Prioritization","Signal Assessment","Recommendation for Action","Exchange of Information","Documentation & Archiving"]

    pbrerReport {
        signalInclusionsAndExclusions {
            signalHistoryStatus = ["Date Closed"]
            excludedDispositions = []   //Add values (not display name) of dispositions that needs to be excluded
        }

        columnNames {
            signalName = 'Signal term'
            dateDetected = 'Date detected'
            dateClosed = 'Date closed (for closed signals)'
            signalSource = "Source or trigger of signal"
            reasonForEvaluation = "Reason summary"
            evaluationMethod = "Method of signal evaluation"
            actionTaken = 'Outcome, if closed'
            signalStatus = 'Status (new, ongoing or closed)'
        }

        signalStatus {
            newSignal = 'New'
            closed = 'Closed'
            ongoing = 'Ongoing'
            newAndClosed = 'New, Closed'
        }
    }

    csrfProtection {
        enabled = true
        excludeURLPatterns = ["/saml/", "/console/", "/j_spring_security_check", '/odata', '/spotfire/rx_validate', '/tags', '/adHocAlert' , '/executedCaseSeries', '/token','/batchlot/import', '/runEtl', '/productGroup','/productGroup/saveProductGroup','/login/authenticate','/login/authAjax','/scim/','/applicationNotification/','/businessConfig/']
    }

}


grails.databinding.dateFormats = ['MMddyyyy',
                                  'yyyy-MM-dd HH:mm:ss.S',
                                  "yyyy-MM-dd'T'hh:mm:ss'Z'",
                                  "yyyy-MM-dd'T'hh:mm:ssZ",
                                  "yyyy-MM-dd'T'HH:mm:ssZ",
                                  "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                                  "dd.MM.yyyy",
                                  "dd-MMM-yyyy",
                                  "MM/dd/yyyy"]

signal.evdas.data.upload.job.interval = 300000l //300000l 5 min
signal.evdas.data.import.job.interval = 300000l //3600000l 1hr
signal.evdas.case.line.listing.upload.job.interval = 300000l  //300000l 5 min
signal.evdas.case.line.listing.import.job.interval = 3600000l  //3600000l 1hr

signal.case.form.folder.save = "${userHome}/case_form"

signal.evdas.ermr.csv.filename.date.seperator = "##"

signal.quantitative.data.prev.columns = ["prr"  : ["prrValue": "PRR", "prrLCI": "PRR LCI/PRR UCI"],
                                         "ror"  : ["rorValue": "ROR", "rorLCI": "ROR LCI/ROR UCI"],
                                         "prrvb"  : ["prrValue": "PRR", "prrLCI": "PRR LCI/PRR UCI"],
                                         "rorvb"  : ["rorValue": "ROR", "rorLCI": "ROR LCI/ROR UCI"],
                                         "ebgm" : ["ebgm": "EBGM", "eb05": "EB05/EB95"],
                                         "count": ["newSponCount"       : "New Spon/Cum Spon",
                                                   "newSeriousCount"    : "New Serious/Cum Serious",
                                                   "newFatalCount"      : "New Fatal/Cum Fatal",
                                                   "newStudyCount"      : "New Study/Cum Study",
                                                   "newCount"           : "New Count/Cum Count",
                                                   "newPediatricCount"  : "New Paed/Cum Paed",
                                                   "newInteractingCount": "New Interacting/Cum Interacting",
                                                   "newGeriatricCount"  : "New Geria/Cum Geria",
                                                   "newNonSerious"      : "${-> grails.util.Holders?.config?.signal?.serious?.column?.header}"],
                                         "vaersCount": ["newSeriousCount"    : "New Serious/Cum Serious",
                                                        "newFatalCount"      : "New Fatal/Cum Fatal",
                                                        "newCount"           : "New Count/Cum Count",
                                                        "newPediatricCount"  : "New Paed/Cum Paed",
                                                        "newGeriatricCount"  : "New Geria/Cum Geria"],
                                         "vigibaseCount": ["newSeriousCount"    : "New Serious/Cum Serious",
                                                           "newFatalCount"      : "New Fatal/Cum Fatal",
                                                           "newCount"           : "New Count/Cum Count",
                                                           "newPediatricCount"  : "New Paed/Cum Paed",
                                                           "newGeriatricCount"  : "New Geria/Cum Geria"],
                                         "jaderCount": [ "newCount"           : "New Count/Cum Count",
                                                         "newSeriousCount"    : "New Serious/Cum Serious",
                                                         "newFatalCount"      : "New Fatal/Cum Fatal",
                                                         "newPediatricCount"  : "New Paed/Cum Paed",
                                                         "newGeriatricCount"  : "New Geria/Cum Geria"]]

signal.quantitative.groupbySmq.data.prev.columns = ["count": ["newSponCount"       : "New Spon/Cum Spon",
                                                              "newSeriousCount"    : "New Serious/Cum Serious",
                                                              "newFatalCount"      : "New Fatal/Cum Fatal",
                                                              "newStudyCount"      : "New Study/Cum Study",
                                                              "newCount"           : "New Count/Cum Count",
                                                              "newPediatricCount"  : "New Paed/Cum Paed",
                                                              "newInteractingCount": "New Interacting/Cum Interacting",
                                                              "newGeriatricCount"  : "New Geria/Cum Geria",
                                                              "newNonSerious"      : "${-> grails.util.Holders?.config?.signal?.serious?.column?.header}"]]

server.contextPath = '/signal'

businessConfiguration {
    attributes {
        aggregate {
            algorithm = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.LISTEDNESS, value: 'Listed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.DME_AGG_ALERT, value: "${-> grails.util.Holders?.config?.importantEvents?.dme?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.IME_AGG_ALERT, value: "${-> grails.util.Holders?.config?.importantEvents?.ime?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.SPECIAL_MONITORING, value: "${-> grails.util.Holders?.config?.importantEvents?.specialMonitoring?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.STOP_LIST, value: "${-> grails.util.Holders?.config?.importantEvents?.stopList?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE, value: 'Chi-Square'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_EVENT, value: 'New Event'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, value: 'Previous Period Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY, value: 'All Periods Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.TREND_TYPE, value: 'Trend Type'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.TREND_FLAG, value: 'Trend Flag'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.FREQ_PERIOD, value: 'Freq Period'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FREQ_PERIOD, value: 'Cum Freq Period'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.DSS_SCORE, value: 'DSS Score'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.E_VALUE, value: 'E'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RR_VALUE, value: 'RR'],
            ]
            trendFlagValue = [
                    [key: com.rxlogix.Constants.Commons.NEW_UPPERCASE, value: com.rxlogix.Constants.Commons.NEW_UPPERCASE],
                    [key: com.rxlogix.Constants.Commons.NO_UPPERCASE, value: com.rxlogix.Constants.Commons.NO_UPPERCASE],
                    [key: com.rxlogix.Constants.Commons.YES_UPPERCASE, value: com.rxlogix.Constants.Commons.YES_UPPERCASE]
            ]
            freqPeriod = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.FREQ_PERIOD, value: 'Freq Period'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FREQ_PERIOD, value: 'Cum Freq Period']
            ]
            counts = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SPON_COUNT, value: 'New Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SPON_COUNT, value: 'Cum Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_STUDY_COUNT, value: 'New Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_STUDY_COUNT, value: 'Cum Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.POSITIVE_RE_CHALLENGE, value: 'Positive Re-challenge'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_INTERACTING_COUNT, value:'New Inter'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_INTERACTING_COUNT, value:'Cum Inter'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT, value: 'New Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT, value: 'Cum Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PEDIATRIC_COUNT, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_PEDIATRIC_COUNT, value: 'Cum Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_NON_SERIOUS_COUNT, value: "${-> grails.util.Holders?.config?.signal?.new?.serious?.column?.header}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_NON_SERIOUS_COUNT, value: "${-> grails.util.Holders?.config?.signal?.cum?.serious?.column?.header}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PROD_COUNT, value: 'New Prod Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_CUM_COUNT, value: 'Cum Prod Count'],

            ]
            formatOptions = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SPON_COUNT, value: 'New Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SPON_COUNT, value: 'Cum Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_STUDY_COUNT, value: 'New Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_STUDY_COUNT, value: 'Cum Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.E_VALUE, value: 'E'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RR_VALUE, value: 'RR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE, value: 'Chi-Square'],
            ]
            textType=[com.rxlogix.Constants.BusinessConfigAttributes.LISTEDNESS, com.rxlogix.Constants.BusinessConfigAttributes.DME_AGG_ALERT,
                      com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY,
                      com.rxlogix.Constants.BusinessConfigAttributes.IME_AGG_ALERT, com.rxlogix.Constants.BusinessConfigAttributes.SPECIAL_MONITORING,
                      com.rxlogix.Constants.BusinessConfigAttributes.STOP_LIST]
            booleanType=[com.rxlogix.Constants.BusinessConfigAttributes.NEW_EVENT]
            subGroup = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE, value: 'EB95']
            ]
            prrGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE, value: 'PRR UCI'],
            ]
            rorGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE, value: 'ROR UCI']
            ]
            trendTypeValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CONTINUING_TREND,value:'Continuing Trend'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EMERGING_TREND,value:'Emerging Trend'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NO_TREND,value:'No Trend']
            ]
            ebgmGroupValues = businessConfiguration.attributes.aggregate.subGroup
            chiSquareValue = [[key:'chiSquare',value: com.rxlogix.Constants.BusinessConfigAttributes.Chi_Square]]
        }
        faers {
            algorithm = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_FAERS, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_FAERS, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_FAERS, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_FAERS, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_FAERS, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_FAERS, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_FAERS, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_FAERS, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_FAERS, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.LISTEDNESS, value: 'Listed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.DME_AGG_ALERT, value: "${-> grails.util.Holders?.config?.importantEvents?.dme?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.IME_AGG_ALERT, value: "${-> grails.util.Holders?.config?.importantEvents?.ime?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.SPECIAL_MONITORING, value: "${-> grails.util.Holders?.config?.importantEvents?.specialMonitoring?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.STOP_LIST, value: "${-> grails.util.Holders?.config?.importantEvents?.stopList?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_FAERS, value: 'Chi-Square'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_EVENT, value: 'New Event'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, value: 'Previous Period Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY, value: 'All Periods Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.TREND_TYPE, value: 'Trend Type'],
            ]
            counts = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_FAERS, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_FAERS, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SPON_COUNT_FAERS, value: 'New Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SPON_COUNT_FAERS, value: 'Cum Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_FAERS, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_FAERS, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_FAERS, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_FAERS, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_STUDY_COUNT_FAERS, value: 'New Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_STUDY_COUNT_FAERS, value: 'Cum Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_INTER_FAERS, value:'New Inter'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_INTER_FAERS, value:'Cum Inter'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIA_FAERS, value: 'New Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_GERIA_FAERS, value: 'Cum Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PAED_FAERS, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_PAED_FAERS, value: 'Cum Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_NON_SERIOUS_FAERS, value: "New Non-Ser"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_NON_SERIOUS_FAERS, value: "Cum Non-Ser"],
            ]
            formatOptions = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_FAERS, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_FAERS, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SPON_COUNT_FAERS, value: 'New Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SPON_COUNT_FAERS, value: 'Cum Spon'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_FAERS, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_FAERS, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_FAERS, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_FAERS, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_STUDY_COUNT_FAERS, value: 'New Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_STUDY_COUNT_FAERS, value: 'Cum Study'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_FAERS, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_FAERS, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_FAERS, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_FAERS, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_FAERS, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_FAERS, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_FAERS, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_FAERS, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_FAERS, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_FAERS, value: 'Chi-Square'],
            ]
            prrGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_FAERS, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_FAERS, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_FAERS, value: 'PRR UCI'],
            ]
            rorGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_FAERS, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_FAERS, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_FAERS, value: 'ROR UCI']
            ]
            subGroup = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_FAERS, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_FAERS, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_FAERS, value: 'EB95']
            ]
            ebgmGroupValues = businessConfiguration.attributes.faers.subGroup
            chiSquareValue = [[key:'chiSquareFaers',value: com.rxlogix.Constants.BusinessConfigAttributes.Chi_Square]]
            textType=[com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY]
            booleanType=[]
        }
        evdas {
            algorithm = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.ROR_EUROPE_EVDAS, value: 'ROR (-) Europe'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.ROR_N_AMERICA_EVDAS, value: 'ROR (-) N America'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.ROR_JAPAN_EVDAS, value: 'ROR (-) Japan'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.ROR_ASIA_EVDAS, value: 'ROR (-) Asia'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.ROR_REST_EVDAS, value: 'ROR (-) Rest'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.ROR_ALL_EVDAS, value: 'ROR (-) All'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.SDR_EVDAS, value: 'SDR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.DME_AGG_ALERT, value: "${-> grails.util.Holders?.config?.importantEvents?.dme?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.IME_AGG_ALERT, value: "${-> grails.util.Holders?.config?.importantEvents?.ime?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.SPECIAL_MONITORING, value: "${-> grails.util.Holders?.config?.importantEvents?.specialMonitoring?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.STOP_LIST, value: "${-> grails.util.Holders?.config?.importantEvents?.stopList?.label}"],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.CHANGES_EVDAS, value: 'Changes'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_IME_DME, value: 'IME/DME'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.RELTV_ROR_PAED_VS_OTHR, value: 'Relative ROR - Paed vs Others'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_PAED, value: 'SDR Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.RELTV_ROR_GERTR_VS_OTHR, value: 'Relative ROR - Geriatr vs Others'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_GERTR, value: 'SDR Geriatr'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_LISTEDNESS, value: 'Listed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_EVENT, value: 'New Event'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.PREVIOUS_CATEGORY, value: 'Previous Period Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.ALL_CATEGORY, value: 'All Periods Category']
            ]
            counts = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_EV_EVDAS, value: 'New EV'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_EV_EVDAS, value: 'Total EV'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_EEA_EVDAS, value: 'New EEA'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_EEA_EVDAS, value: 'Total EEA'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_HCP_EVDAS, value: 'New HCP'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_HCP_EVDAS, value: 'Total HCP'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_SERIOUS_EVDAS, value: 'New Serious'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SERIOUS_EVDAS, value: 'Total Serious'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_OBS_EVDAS, value: 'New Obs'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_OBS_EVDAS, value: 'Total Obs'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_FATAL_EVDAS, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_FATAL_EVDAS, value: 'Total Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_MED_ERR_EVDAS, value: 'New Med Err'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_MED_ERR_EVDAS, value: 'Total Med Err'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_PLUS_RC_EVDAS, value: 'New +RC'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_PLUS_RC_EVDAS, value: 'Total +RC'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_LITERATURE_EVDAS, value: 'New Lit'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_LITERATURE_EVDAS, value: 'Total Lit'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_PAED_EVDAS, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_PAED_EVDAS, value: 'Total Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_GERIAT_EVDAS, value: 'New Geriatr'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_GERIAT_EVDAS, value: 'Total Geriatr'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_SPON_EVDAS, value: 'New Spont'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EVDAS, value: 'Total Spont'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EUROPE, value: 'Total Spont Europe'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_N_AMERICA, value: 'Total Spont N America'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_JAPAN, value: 'Total Spont Japan'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_ASIA, value: 'Total Spont Asia'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_REST, value: 'Total Spont Rest']
            ]
            formatOptions = businessConfiguration.attributes.evdas.counts + businessConfiguration.attributes.evdas.algorithm
            textType=[com.rxlogix.Constants.BusinessConfigAttributesEvdas.SDR_EVDAS, com.rxlogix.Constants.BusinessConfigAttributesEvdas.CHANGES_EVDAS,
                      com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_IME_DME, com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_PAED,
                      com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_GERTR, com.rxlogix.Constants.BusinessConfigAttributesEvdas.EVDAS_LISTEDNESS,
                      com.rxlogix.Constants.BusinessConfigAttributesEvdas.PREVIOUS_CATEGORY, com.rxlogix.Constants.BusinessConfigAttributesEvdas.ALL_CATEGORY,
                      com.rxlogix.Constants.BusinessConfigAttributesEvdas.IME_AGG_ALERT, com.rxlogix.Constants.BusinessConfigAttributesEvdas.SPECIAL_MONITORING,
                      com.rxlogix.Constants.BusinessConfigAttributesEvdas.STOP_LIST, com.rxlogix.Constants.BusinessConfigAttributesEvdas.DME_AGG_ALERT]
            booleanType=[com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_EVENT]
            algorithmThreshold {
                threshold = [
                        [key: 'rorEuropeEvdas', value: 'ROR (-) Europe'],
                        [key: 'rorNAmericaEvdas', value: 'ROR (-) N America'],
                        [key: 'rorJapanEvdas', value: 'ROR (-) Japan'],
                        [key: 'rorAsiaEvdas', value: 'ROR (-) Asia'],
                        [key: 'rorRestEvdas', value: 'ROR (-) Rest'],
                        [key: 'rorAllEvdas', value: 'ROR (-) All'],
                        [key: 'ratioRorPaedVsOthersEvdas', value: 'Relative ROR - Paed vs Others'],
                        [key: 'ratioRorGeriatrVsOthersEvdas', value: 'Relative ROR - Geriatr vs Others'],
                ]
            }
        }
        vaers {
            algorithm = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_VAERS, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_VAERS, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_VAERS, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_VAERS, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_VAERS, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_VAERS, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_VAERS, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_VAERS, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_VAERS, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_VAERS, value: 'Chi-Square'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, value: 'Previous Period Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY, value: 'All Periods Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.LISTEDNESS, value: 'Listedness']
            ]
            counts = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_VAERS, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_VAERS, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VAERS, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VAERS, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VAERS, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VAERS, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VAERS, value: 'New Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VAERS, value: 'Cum Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PAED_VAERS, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_PAED_VAERS, value: 'Cum Paed']
            ]
            formatOptions = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_VAERS, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_VAERS, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VAERS, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VAERS, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VAERS, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VAERS, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VAERS, value: 'New Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VAERS, value: 'Cum Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PAED_VAERS, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_PAED_VAERS, value: 'Cum Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_VAERS, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_VAERS, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_VAERS, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_VAERS, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_VAERS, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_VAERS, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_VAERS, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_VAERS, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_VAERS, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_VAERS, value: 'Chi-Square']
            ]
            ebgmGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_VAERS, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_VAERS, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_VAERS, value: 'EB95']
            ]
            prrGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_VAERS, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_VAERS, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_VAERS, value: 'PRR UCI'],
            ]
            rorGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_VAERS, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_VAERS, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_VAERS, value: 'ROR UCI']
            ]
            chiSquareValue = [[key:'chiSquareVaers',value: com.rxlogix.Constants.BusinessConfigAttributes.Chi_Square]]
            textType=[com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY]
            booleanType=[]
        }
        vigibase {
            algorithm = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_VIGIBASE, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_VIGIBASE, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_VIGIBASE, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_VIGIBASE, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_VIGIBASE, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_VIGIBASE, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_VIGIBASE, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_VIGIBASE, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_VIGIBASE, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_VIGIBASE, value: 'Chi-Square'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, value: 'Previous Period Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY, value: 'All Periods Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.LISTEDNESS, value: 'Listedness']
            ]
            counts = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_VIGIBASE, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_VIGIBASE, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VIGIBASE, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VIGIBASE, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VIGIBASE, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VIGIBASE, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VIGIBASE, value: 'New Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VIGIBASE, value: 'Cum Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PAED_VIGIBASE, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_PAED_VIGIBASE, value: 'Cum Paed']
            ]
            formatOptions = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_VIGIBASE, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_VIGIBASE, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VIGIBASE, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VIGIBASE, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VIGIBASE, value: 'New Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VIGIBASE, value: 'Cum Ser'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VIGIBASE, value: 'New Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VIGIBASE, value: 'Cum Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PAED_VIGIBASE, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_PAED_VIGIBASE, value: 'Cum Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_VIGIBASE, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_VIGIBASE, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_VIGIBASE, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_VIGIBASE, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_VIGIBASE, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_VIGIBASE, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_VIGIBASE, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_VIGIBASE, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_VIGIBASE, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_VIGIBASE, value: 'Chi-Square']
            ]
            ebgmGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_VIGIBASE, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_VIGIBASE, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_VIGIBASE, value: 'EB95']
            ]
            prrGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_VIGIBASE, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_VIGIBASE, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_VIGIBASE, value: 'PRR UCI'],
            ]
            rorGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_VIGIBASE, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_VIGIBASE, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_VIGIBASE, value: 'ROR UCI']
            ]
            chiSquareValue = [[key:'chiSquareVigibase',value: com.rxlogix.Constants.BusinessConfigAttributes.Chi_Square]]
            textType=[com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY]
            booleanType=[]
        }
        jader {
            algorithm = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_JADER, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_JADER, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_JADER, value: 'PRR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_JADER, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_JADER, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_JADER, value: 'ROR UCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_JADER, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_JADER, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_JADER, value: 'EB95'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_JADER, value: 'Chi-Square'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, value: 'Previous Period Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY, value: 'All Periods Category'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.LISTEDNESS, value: 'Listedness'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.E_VALUE_JADER, value: 'E'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RR_VALUE_JADER, value: 'RR']
            ]
            counts = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_JADER, value: 'New Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_JADER, value: 'Cum Count'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_JADER, value: 'New Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_JADER, value: 'Cum Fatal'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_JADER, value: 'New Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_JADER, value: 'Cum Geria'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NEW_PAED_JADER, value: 'New Paed'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CUM_PAED_JADER, value: 'Cum Paed']
            ]
            formatOptions = [
                    [key: "NEW_COUNT", value: 'New Count'],
                    [key: "CUM_COUNT", value: 'Cum Count'],
                    [key: 'NEW_FATAL', value: 'New Fatal'],
                    [key: 'CUM_FATAL', value: 'Cum Fatal'],
                    [key: 'NEW_GER', value: 'New Geria'],
                    [key: 'CUM_GER', value: 'Cum Geria'],
                    [key: 'NEW_PEDIA', value: 'New Paed'],
                    [key: 'CUM_PEDIA', value: 'Cum Paed'],
                    [key: 'PRR', value: 'PRR'],
                    [key: 'PRRLCI', value: 'PRR LCI'],
                    [key: 'PRRUCI', value: 'PRR UCI'],
                    [key: 'ROR', value: 'ROR'],
                    [key: 'RORLCI', value: 'ROR LCI'],
                    [key: 'RORUCI', value: 'ROR UCI'],
                    [key: 'EBGM', value: 'EBGM'],
                    [key: 'EB05', value: 'EB05'],
                    [key: 'EB95', value: 'EB95']
            ]
            countThreshold = [
                    [key: 'newCountJader', value: 'New Count'],
                    [key: 'cumCountJader', value: 'Cum Count'],
                    [key: 'newFatalCountJader', value: 'New Fatal'],
                    [key: 'cumFatalCountJader', value: 'Cum Fatal'],
                    [key: 'newGeriatricCountJader', value: 'New Geria'],
                    [key: 'cumGeriatricCountJader', value: 'Cum Geria'],
                    [key: 'newPediatricCountJader', value: 'New Paed'],
                    [key: 'cumPediatricCountJader', value: 'Cum Paed'],
            ]
            ebgmGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EBGM_SCORE_JADER, value: 'EBGM'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB05_SCORE_JADER, value: 'EB05'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EB95_SCORE_JADER, value: 'EB95']
            ]
            prrGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRR_SCORE_JADER, value: 'PRR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRLCI_SCORE_JADER, value: 'PRR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.PRRUCI_SCORE_JADER, value: 'PRR UCI'],
            ]
            rorGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.ROR_SCORE_JADER, value: 'ROR'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORLCI_SCORE_JADER, value: 'ROR LCI'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RORUCI_SCORE_JADER, value: 'ROR UCI']
            ]
            chiSquareValue = [[key:'chiSquareJader',value: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE_JADER]]
            trendTypeValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.CONTINUING_TREND,value:'Continuing Trend'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.EMERGING_TREND,value:'Emerging Trend'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.NO_TREND,value:'No Trend']
            ]
            errGroupValues = [
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.E_VALUE_JADER, value: 'E'],
                    [key: com.rxlogix.Constants.BusinessConfigAttributes.RR_VALUE_JADER, value: 'RR'],
            ]
            textType=[com.rxlogix.Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, com.rxlogix.Constants.BusinessConfigAttributes.ALL_CATEGORY]
            booleanType=[]
        }
    }
}


//PVR API URL's
pvreports.adHoc.report.templates.uri = "/reports/public/api/adHocReport/templates/list"

pv {
    plugin {
        dictionary {
            enabled = true
            select2v4 = true
            product {}

            event {
                levels = ['SOC', 'HLGT', 'HLT', 'PT', 'LLT', 'Synonyms', 'SMQ Broad', 'SMQ Narrow']
                columns = ['SOC', 'HLGT', 'HLT', 'PT', 'LLT', 'Synonyms']
            }

            study {
                levels = ['Project Number', 'Study Number', 'Center']
                columns = ['ProtocolNumber', 'StudyNumber']
            }
        }
    }
}

//Add the enteries in the below list in order to add/update the fileds in the Advanced Filter.
signal.scaColumnList = [[name: "flags", display: "Flags", dataType: 'java.lang.String'],
                        [name: "priority.id", display: "Priority", dataType: 'java.lang.String'],
                        [name: "productName", display: "Product Name", dataType: 'java.lang.String'],
                        [name: "ptList", display: "All PTs (SUR)", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "conComitList", display: "All Conmeds", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "caseInitReceiptDate", display: "Receipt Date", dataType: 'java.util.Date'],
                        [name: "lockedDate", display: "Locked Date", dataType: 'java.util.Date'],
                        [name: "dueDate", display: "Due In", dataType: 'java.lang.Number'],
                        [name: "disposition.id", display: "Disposition", dataType: 'java.lang.String'],
                        [name: "signal", display: "Signal", dataType: 'java.lang.String'],
                        [name: "listedness", display: "Listedness", dataType: 'java.lang.String'],
                        [name: "outcome", display: "Outcome", dataType: 'java.lang.String'],
                        [name: "serious", display: "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled?'Serious/Non-Serious':'Serious'}", dataType: 'java.lang.String'],
                        [name: "caseReportType", display: "Report Type", dataType: 'java.lang.String'],
                        [name: "country", display: "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled?'Derived Country':'Country'}", dataType: 'java.lang.String'],
                        [name: "reportersHcpFlag", display: "HCP", dataType: 'java.lang.String'],
                        [name: "death", display: "Death", dataType: 'java.lang.String'],
                        [name: "age", display: "Age Group", dataType: 'java.lang.String'],
                        [name: "gender", display: "Gender", dataType: 'java.lang.String'],
                        [name: "caseType", display: "Case Type", dataType: 'java.lang.String'],
                        [name: "completenessScore", display: "Completeness Score", dataType: 'java.lang.Number'],
                        [name: "indNumber", display: "Primary IND#",isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "appTypeAndNum", display: "Application#", dataType: 'java.lang.String'],
                        [name: "compoundingFlag", display: "Compounding Flag", dataType: 'java.lang.String'],
                        [name: "medErrorPtList", display: "Medication Error PTs", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "patientAge", display: "Age", dataType: 'java.lang.Number'],
                        [name: "suspectProductList", display: "All Suspect Products", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "rechallenge", display: "Rechallenge", dataType: 'java.lang.String'],
                        [name: "caseNumber", display: "Case(f/u#)", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "assignedTo.id", display: "Assigned To(User)", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "assignedToGroup.id", display: "Assigned To(Group)", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "pt", display: "PT", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "caseNarrative", display: "Case Narrative", dataType: 'java.lang.String'],
                        [name: "name", display: "Alert Name", dataType: 'java.lang.String'],
                        [name: "submitter", display: "Submitter", dataType: 'java.lang.String'],
                        [name: "tags", display: "Categories", dataType: 'java.lang.String'],
                        [name: "subTags", display: "Sub Categories", dataType: 'java.lang.String'],
                        [name: "currentRun", display: "Current Period Category", dataType: 'java.lang.Boolean'],
                        [name: "malfunction", display: "Malfunction", dataType: 'java.lang.String'],
                        [name: "comboFlag", display: "Combo Flag", dataType: 'java.lang.String'],
                        [name: "caseSeries", display: "Case Series", isAutocomplete: true,dataType: 'java.lang.String'],
                        [name: "indication", display: "Indication",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "eventOutcome", display: "Event  Outcome",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "causeOfDeath", display: "Cause of Death",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "seriousUnlistedRelated", display: "SUR",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "patientMedHist", display: "Patient Med Hist",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "patientHistDrugs", display: "Patient Hist Drugs",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "batchLotNo", display: "Batch/Lot#",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "timeToOnset", display: "Time To Onset" , dataType: 'java.lang.Number'],
                        [name: "caseClassification", display: "Case Classification",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "initialFu", display: "Initial/F/u",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "protocolNo", display: "Protocol#",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "isSusar", display: "SUSAR", dataType: 'java.lang.String'],
                        [name: "therapyDates", display: "Therapy Dates",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "doseDetails", display: "Dose Details",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "justification", display: "Justification",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "preAnda", display: "Pre-ANDA",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "primSuspProdList", display: "Primary Suspect Product", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "primSuspPaiList", display: "Primary Suspect PAI", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "paiAllList", display: "All Suspect PAIs", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "allPtList", display: "All PTs", isAutocomplete: true, dataType: 'java.lang.String'],
                        [name: "dispPerformedBy", display: "Performed By",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "dispLastChange", display: "Last Disposition Date", dataType: 'java.util.Date'],
                        [name: "comments", display: "Comments", dataType: 'java.lang.String'],
                        [name: "genericName", display: "Generic Name (Suspect)",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "caseCreationDate", display: "Case Creation Date", dataType: 'java.util.Date'],
                        [name: "dateOfBirth", display: "DOB",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "eventOnsetDate", display: "Event Onset Date" ,isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "pregnancy", display: "Pregnancy",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "medicallyConfirmed", display: "Medically Confirmed",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "allPTsOutcome", display: "All PTs - Outcome",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "crossReferenceInd", display: "Cross Reference IND",isAutocomplete: true , dataType: 'java.lang.String'],
                        [name: "region", display: "Region", dataType: 'java.lang.String']
]

signal.agaColumnList.safetyDb = [[name: "flags", display: "Flags", dataType: 'java.lang.String'],
                                 [name: "priority.id", display: "Priority", dataType: 'java.lang.String'],
                                 [name: "productName", display: "Product Name", dataType: 'java.lang.String'],
                                 [name: "soc", display: "SOC", dataType: 'java.lang.String'],
                                 [name: "dueDate", display: "Due In", dataType: 'java.lang.Number'],
                                 [name: "disposition.id", display: "Disposition", dataType: 'java.lang.String'],
                                 [name: "signal", display: "Signal", dataType: 'java.lang.String'],
                                 [name: "listed", display: "Listed", dataType: 'java.lang.String'],
                                 [name: "positiveRechallenge", display: "+ve Rechallenge", dataType: 'java.lang.String'],
                                 [name: "positiveDechallenge", display: "+ve Dechallenge", dataType: 'java.lang.String'],
                                 [name: "related", display: "Related", dataType: 'java.lang.String'],
                                 [name: "pregenency", display: "Pregnancy", dataType: 'java.lang.String'],
                                 [name: "newSponCount", display: "New Spon", dataType: 'java.lang.Number'],
                                 [name: "cumSponCount", display: "Cum Spon", dataType: 'java.lang.Number'],
                                 [name: "newSeriousCount", display: "New Ser", dataType: 'java.lang.Number'],
                                 [name: "cumSeriousCount", display: "Cum Ser", dataType: 'java.lang.Number'],
                                 [name: "newStudyCount", display: "New Study", dataType: 'java.lang.Number'],
                                 [name: "cumStudyCount", display: "Cum Study", dataType: 'java.lang.Number'],
                                 [name: "newFatalCount", display: "New Fatal", dataType: 'java.lang.Number'],
                                 [name: "cumFatalCount", display: "Cum Fatal", dataType: 'java.lang.Number'],
                                 [name: "newGeriatricCount", display: "New Geria", dataType: 'java.lang.Number'],
                                 [name: "cumGeriatricCount", display: "Cum Geria", dataType: 'java.lang.Number'],
                                 [name: "newNonSerious", display: "${-> grails.util.Holders?.config?.signal?.new?.serious?.column?.header}", dataType: 'java.lang.Number'],
                                 [name: "cumNonSerious", display: "${-> grails.util.Holders?.config?.signal?.cum?.serious?.column?.header}", dataType: 'java.lang.Number'],
                                 [name: "eb05", display: "EB05", dataType: 'java.lang.Number'],
                                 [name: "eb95", display: "EB95", dataType: 'java.lang.Number'],
                                 [name: "pecImpNumLow", display: "PEC Imp Low", dataType: 'java.lang.Number'],
                                 [name: "pecImpNumHigh", display: "DSS Score", dataType: 'java.lang.Number'],
                                 [name: "ebgm", display: "EBGM", dataType: 'java.lang.Number'],
                                 [name: "prrValue", display: "PRR", dataType: 'java.lang.Number'],
                                 [name: "prrLCI", display: "PRR LCI", dataType: 'java.lang.Number'],
                                 [name: "prrUCI", display: "PRR UCI", dataType: 'java.lang.Number'],
                                 [name: "rorValue", display: "ROR", dataType: 'java.lang.Number'],
                                 [name: "rorLCI", display: "ROR LCI", dataType: 'java.lang.Number'],
                                 [name: "rorUCI", display: "ROR UCI", dataType: 'java.lang.Number'],
                                 [name: "trendType", display: "Trend Type", dataType: 'java.lang.String'],
                                 [name: "freqPriority", display: "Frequency Priority", dataType: 'java.lang.String'],
                                 [name: "assignedTo.id", display: "Assigned To(User)", isAutocomplete: true, dataType: 'java.lang.String'],
                                 [name: "assignedToGroup.id", display: "Assigned To(Group)", isAutocomplete: true, dataType: 'java.lang.String'],
                                 [name: "pt", display: "PT/SMQ/Event Group", isAutocomplete: true, dataType: 'java.lang.String'],
                                 [name: "newCount", display: "New Count", dataType: 'java.lang.Number'],
                                 [name: "cummCount", display: "Cum Count", dataType: 'java.lang.Number'],
                                 [name: "newPediatricCount", display: "New Paed", dataType: 'java.lang.Number'],
                                 [name: "cummPediatricCount", display: "Cum Paed", dataType: 'java.lang.Number'],
                                 [name: "newInteractingCount", display: "New Interacting", dataType: 'java.lang.Number'],
                                 [name: "cummInteractingCount", display: "Cum Interacting", dataType: 'java.lang.Number'],
                                 [name: "aggImpEventList", display: "IMP Events", dataType: 'java.lang.String'],
                                 [name: "chiSquare", display: "Chi-Square", dataType: 'java.lang.Number'] ,
                                 [name: "tags", display: "Categories", dataType: 'java.lang.String'] ,
                                 [name: "subTags", display: "Sub Categories", dataType: 'java.lang.String'],
                                 [name: "currentRun", display: "Current Period Category", dataType: 'java.lang.Boolean'],
                                 [name: "justification", display: "Justification",isAutocomplete: true , dataType: 'java.lang.String'],
                                 [name: "dispPerformedBy", display: "Performed By",isAutocomplete: true , dataType: 'java.lang.String'],
                                 [name: "dispLastChange", display: "Last Disposition Date", dataType: 'java.util.Date'],
                                 [name: "comment", display: "Comments", dataType: 'java.lang.String'],
                                 [name: "trendFlag", display: "Trend Flag", dataType: 'java.lang.String'],
                                 [name: "newProdCount", display: "New Prod Count", dataType: 'java.lang.Number'],
                                 [name: "cumProdCount", display: "Cum Prod Count", dataType: 'java.lang.Number'],
                                 [name: "freqPeriod", display: "Freq Period", dataType: 'java.lang.Number'],
                                 [name: "cumFreqPeriod", display: "Cum Freq Period", dataType: 'java.lang.Number'],
                                 [name: "reviewedFreqPeriod", display: "Reviewed Freq Period", dataType: 'java.lang.Number'],
                                 [name: "reviewedCumFreqPeriod", display: "Reviewed Cum Freq Period", dataType: 'java.lang.Number'],
                                 [name: "aValue", display: "A", dataType: 'java.lang.Number'],
                                 [name: "bValue", display: "B", dataType: 'java.lang.Number'],
                                 [name: "cValue", display: "C", dataType: 'java.lang.Number'],
                                 [name: "dValue", display: "D", dataType: 'java.lang.Number'],
                                 [name: "eValue", display: "E", dataType: 'java.lang.Number'],
                                 [name: "rrValue", display: "RR", dataType: 'java.lang.Number']
]

signal.agaColumnList.faers = [[name: "newSponCountFaers", display: "New Spon (F)", dataType: 'java.lang.Number'],
                              [name: "cumSponCountFaers", display: "Cum Spon (F)", dataType: 'java.lang.Number'],
                              [name: "newStudyCountFaers", display: "New Study (F)", dataType: 'java.lang.Number'],
                              [name: "freqPriorityFaers", display: "Frequency Priority (F)", dataType: 'java.lang.String'],
                              [name: "positiveRechallengeFaers", display: "+ve Rechallenge (F)", dataType: 'java.lang.String'],
                              [name: "positiveDechallengeFaers", display: "+ve Dechallenge (F)", dataType: 'java.lang.String'],
                              [name: "cumStudyCountFaers", display: "Cum Study (F)", dataType: 'java.lang.Number'],
                              [name: "newFatalCountFaers", display: "New Fatal (F)", dataType: 'java.lang.Number'],
                              [name: "cumFatalCountFaers", display: "Cum Fatal (F)", dataType: 'java.lang.Number'],
                              [name: "newCountFaers", display: "New Count (F)", dataType: 'java.lang.Number'],
                              [name: "cummCountFaers", display: "Cum Count (F)", dataType: 'java.lang.Number'],
                              [name: "newSeriousCountFaers", display: "New Ser (F)", dataType: 'java.lang.Number'],
                              [name: "cumSeriousCountFaers", display: "Cum Ser (F)", dataType: 'java.lang.Number'],
                              [name: "newPediatricCountFaers", display: "New Paed (F)", dataType: 'java.lang.Number'],
                              [name: "cummPediatricCountFaers", display: "Cum Paed (F)", dataType: 'java.lang.Number'],
                              [name: "newInteractingCountFaers", display: "New Interacting (F)", dataType: 'java.lang.Number'],
                              [name: "cummInteractingCountFaers", display: "Cum Interacting (F)", dataType: 'java.lang.Number'],
                              [name: "newGeriatricCountFaers", display: "New Geria (F)", dataType: 'java.lang.Number'],
                              [name: "cumGeriatricCountFaers", display: "Cum Geria (F)", dataType: 'java.lang.Number'],
                              [name: "newNonSeriousFaers", display: "New Non-Ser (F)", dataType: 'java.lang.Number'],
                              [name: "cumNonSeriousFaers", display: "Cum Non-Ser (F)", dataType: 'java.lang.Number'],
                              [name: "eb05Faers", display: "EB05 (F)", dataType: 'java.lang.Number'],
                              [name: "eb95Faers", display: "EB95 (F)", dataType: 'java.lang.Number'],
                              [name: "ebgmFaers", display: "EBGM (F)", dataType: 'java.lang.Number'],
                              [name: "prrValueFaers", display: "PRR (F)", dataType: 'java.lang.Number'],
                              [name: "prrLCIFaers", display: "PRR LCI (F)", dataType: 'java.lang.Number'],
                              [name: "prrUCIFaers", display: "PRR UCI (F)", dataType: 'java.lang.Number'],
                              [name: "rorValueFaers", display: "ROR (F)", dataType: 'java.lang.Number'],
                              [name: "rorLCIFaers", display: "ROR LCI (F)", dataType: 'java.lang.Number'],
                              [name: "rorUCIFaers", display: "ROR UCI (F)", dataType: 'java.lang.Number'],
                              [name: "chiSquareFaers", display: "Chi-Square (F)", dataType: 'java.lang.Number'] ,
                              [name: "relatedFaers", display: "Related (F)", dataType: 'java.lang.String'] ,
                              [name: "pregenencyFaers", display: "Pregnancy (F)", dataType: 'java.lang.String'] ,
                              [name: "trendTypeFaers", display: "Trend Type (F)", dataType: 'java.lang.String']
]

signal.agaColumnList.evdas = [[name: "hlgtEvdas", display: "HLGT (E)", dataType: 'java.lang.String'],
                              [name: "hltEvdas", display: "HLT (E)", dataType: 'java.lang.String'],
                              [name: "smqNarrowEvdas", display: "SMQ (E)", dataType: 'java.lang.String'],
                              [name: "newEeaEvdas", display: "New EEA (E)", dataType: 'java.lang.Number'],
                              [name: "totEeaEvdas", display: "Total EEA (E)", dataType: 'java.lang.Number'],
                              [name: "newHcpEvdas", display: "New HCP (E)", dataType: 'java.lang.Number'],
                              [name: "totHcpEvdas", display: "Total HCP (E)", dataType: 'java.lang.Number'],
                              [name: "newSeriousEvdas", display: "New Ser (E)", dataType: 'java.lang.Number'],
                              [name: "totalSeriousEvdas", display: "Total Ser (E)", dataType: 'java.lang.Number'],
                              [name: "newMedErrEvdas", display: "New Med Err (E)", dataType: 'java.lang.Number'],
                              [name: "totMedErrEvdas", display: "Total Med Err (E)", dataType: 'java.lang.Number'],
                              [name: "newObsEvdas", display: "New Obs (E)", dataType: 'java.lang.Number'],
                              [name: "totObsEvdas", display: "Total Obs (E)", dataType: 'java.lang.Number'],
                              [name: "newFatalEvdas", display: "New Fatal (E)", dataType: 'java.lang.Number'],
                              [name: "totalFatalEvdas", display: "Total Fatal(E)", dataType: 'java.lang.Number'],
                              [name: "newRcEvdas", display: "New +RC (E)", dataType: 'java.lang.Number'],
                              [name: "totRcEvdas", display: "Total +RC (E)", dataType: 'java.lang.Number'],
                              [name: "newLitEvdas", display: "New Lit (E)", dataType: 'java.lang.Number'],
                              [name: "totalLitEvdas", display: "Total Lit (E)", dataType: 'java.lang.Number'],
                              [name: "newPaedEvdas", display: "New Paed (E)", dataType: 'java.lang.Number'],
                              [name: "totPaedEvdas", display: "Total Paed (E)", dataType: 'java.lang.Number'],
                              [name: "ratioRorPaedVsOthersEvdas", display: "Relative ROR (-) Paed vs Others (E)", dataType: 'java.lang.Number'],
                              [name: "newGeriaEvdas", display: "New Geria (E)", dataType: 'java.lang.Number'],
                              [name: "totGeriaEvdas", display: "Total Geria (E)", dataType: 'java.lang.Number'],
                              [name: "ratioRorGeriatrVsOthersEvdas", display: "Relative ROR (-) Geriatr vs Others (E)", dataType: 'java.lang.Number'],
                              [name: "sdrGeratrEvdas", display: "SDR Geriatr (E)", dataType: 'java.lang.String'],
                              [name: "newSpontEvdas", display: "New Spon (E)", dataType: 'java.lang.Number'],
                              [name: "totSpontEvdas", display: "Total Spon (E)", dataType: 'java.lang.Number'],
                              [name: "totSpontEuropeEvdas", display: "Tot Spont Europe (E)", dataType: 'java.lang.Number'],
                              [name: "totSpontNAmericaEvdas", display: "Tot Spont N America (E)", dataType: 'java.lang.Number'],
                              [name: "totSpontJapanEvdas", display: "Tot Spont Japan (E)", dataType: 'java.lang.Number'],
                              [name: "totSpontAsiaEvdas", display: "Tot Spont Asia (E)", dataType: 'java.lang.Number'],
                              [name: "totSpontRestEvdas", display: "Tot Spont Rest (E)", dataType: 'java.lang.Number'],
                              [name: "sdrPaedEvdas", display: "SDR Paed (E)", dataType: 'java.lang.String'],
                              [name: "europeRorEvdas", display: "ROR(Europe) (E)", dataType: 'java.lang.Number'],
                              [name: "northAmericaRorEvdas", display: "ROR(North America) (E)", dataType: 'java.lang.Number'],
                              [name: "japanRorEvdas", display: "ROR(Japan) (E)", dataType: 'java.lang.Number'],
                              [name: "asiaRorEvdas", display: "ROR(Asia) (E)", dataType: 'java.lang.Number'],
                              [name: "restRorEvdas", display: "ROR(Rest) (E)", dataType: 'java.lang.Number'],
                              [name: "changesEvdas", display: "Changes (E)", dataType: 'java.lang.String'],
                              [name: "newEvEvdas", display: "New EV (E)", dataType: 'java.lang.Number'],
                              [name: "totalEvEvdas", display: "Total EV (E)", dataType: 'java.lang.Number'],
                              [name: "dmeImeEvdas", display: "IME/DME (E)", dataType: 'java.lang.String'],
                              [name: "sdrEvdas", display: "SDR (E)", dataType: 'java.lang.String'],
                              [name: "allRorEvdas", display: "ROR(-)(All) (E)", dataType: 'java.lang.Number'],
]

signal.evdasColumnList = [[name: "flags", display: "Flags", dataType: 'java.lang.String'],
                          [name: "priority.id", display: "Priority", dataType: 'java.lang.String'],
                          [name: "substance", display: "Substance", dataType: 'java.lang.String'],
                          [name: "listedness", display: "Listed", dataType: 'java.lang.Boolean'],
                          [name: "soc", display: "SOC", dataType: 'java.lang.String'],
                          [name: "dueDate", display: "Due In", dataType: 'java.lang.Number'],
                          [name: "name", display: "Alert Name", dataType: 'java.lang.String'],
                          [name: "disposition.id", display: "Disposition", dataType: 'java.lang.String'],
                          [name: "signal", display: "Signal", dataType: 'java.lang.String'],
                          [name: "hlgt", display: "HLGT", dataType: 'java.lang.String'],
                          [name: "changes", display: "Changes", dataType: 'java.lang.String'],
                          [name: "hlt", display: "HLT", dataType: 'java.lang.String'],
                          [name: "sdr", display: "SDR", dataType: 'java.lang.String'],
                          [name: "sdrPaed", display: "SDR Paed", dataType: 'java.lang.String'],
                          [name: "sdrGeratr", display: "SDR Geriatr", dataType: 'java.lang.String'],
                          [name: "smqNarrow", display: "SMQ", dataType: 'java.lang.String'],
                          [name: "newEv", display: "New EV", dataType: 'java.lang.Number'],
                          [name: "totalEv", display: "Total EV", dataType: 'java.lang.Number'],
                          [name: "newSerious", display: "New Ser", dataType: 'java.lang.Number'],
                          [name: "totalSerious", display: "Total Ser", dataType: 'java.lang.Number'],
                          [name: "newFatal", display: "New Fatal", dataType: 'java.lang.Number'],
                          [name: "totalFatal", display: "Total Fatal", dataType: 'java.lang.Number'],
                          [name: "newRc", display: "New +RC", dataType: 'java.lang.Number'],
                          [name: "totRc", display: "Total +RC", dataType: 'java.lang.Number'],
                          [name: "newPaed", display: "New Paed", dataType: 'java.lang.Number'],
                          [name: "totPaed", display: "Total Paed", dataType: 'java.lang.Number'],
                          [name: "newGeria", display: "New Geria", dataType: 'java.lang.Number'],
                          [name: "totGeria", display: "Total Geria", dataType: 'java.lang.Number'],
                          [name: "newLit", display: "New Lit", dataType: 'java.lang.Number'],
                          [name: "totalLit", display: "Total Lit", dataType: 'java.lang.Number'],
                          [name: "newEea", display: "New EEA", dataType: 'java.lang.Number'],
                          [name: "totEea", display: "Total EEA", dataType: 'java.lang.Number'],
                          [name: "newMedErr", display: "New Med Err", dataType: 'java.lang.Number'],
                          [name: "totMedErr", display: "Total Med Err", dataType: 'java.lang.Number'],
                          [name: "newHcp", display: "New HCP", dataType: 'java.lang.Number'],
                          [name: "totHcp", display: "Total HCP", dataType: 'java.lang.Number'],
                          [name: "newObs", display: "New Obs", dataType: 'java.lang.Number'],
                          [name: "totObs", display: "Total Obs", dataType: 'java.lang.Number'],
                          [name: "newSpont", display: "New Spont", dataType: 'java.lang.Number'],
                          [name: "totSpont", display: "Total Spont", dataType: 'java.lang.Number'],
                          [name: "totSpontRest", display: "Tot Spont Rest", dataType: 'java.lang.Number'],
                          [name: "totSpontAsia", display: "Tot Spont Asia", dataType: 'java.lang.Number'],
                          [name: "totSpontJapan", display: "Tot Spont Japan", dataType: 'java.lang.Number'],
                          [name: "totSpontNAmerica", display: "Tot Spont N America", dataType: 'java.lang.Number'],
                          [name: "totSpontEurope", display: "Tot Spont Europe", dataType: 'java.lang.Number'],
                          [name: "rorValue", display: "ROR(-) All", dataType: 'java.lang.Number'],
                          [name: "asiaRor", display: "ROR(Asia)", dataType: 'java.lang.Number'],
                          [name: "restRor", display: "ROR(Rest)", dataType: 'java.lang.Number'],
                          [name: "japanRor", display: "ROR(Japan)", dataType: 'java.lang.Number'],
                          [name: "europeRor", display: "ROR(Europe)", dataType: 'java.lang.Number'],
                          [name: "northAmericaRor", display: "ROR(North America)", dataType: 'java.lang.Number'],
                          [name: "ratioRorPaedVsOthers", display: "Relative ROR (-) Paed vs Others", dataType: 'java.lang.Number'],
                          [name: "ratioRorGeriatrVsOthers", display: "Relative ROR (-) Geriatr vs Others", dataType: 'java.lang.Number'],
                          [name: "assignedTo.id", display: "Assigned To(User)", isAutocomplete: true, dataType: 'java.lang.String'],
                          [name: "assignedToGroup.id", display: "Assigned To(Group)", isAutocomplete: true, dataType: 'java.lang.String'],
                          [name: "pt", display: "PT", isAutocomplete: true, dataType: 'java.lang.String'],
                          [name: "dmeIme", display: "IME/DME", isAutocomplete: false, dataType: 'java.lang.String'],
                          [name: "evImpEventList", display: "IMP Events", dataType: 'java.lang.String'],
                          [name: "justification", display: "Justification",isAutocomplete: true , dataType: 'java.lang.String'],
                          [name: "dispPerformedBy", display: "Performed By",isAutocomplete: true , dataType: 'java.lang.String'],
                          [name: "dispLastChange", display: "Last Disposition Date", dataType: 'java.util.Date'],
                          [name: "comment", display: "Comments", dataType: 'java.lang.String'],
]

signal.scaOnDemandColumnList = [[name: "productName", display: "Product Name", dataType: 'java.lang.String'],
                                [name: "ptList", display: "All PTs (SUR)", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "conComitList", display: "All Conmeds", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "caseInitReceiptDate", display: "Receipt Date", dataType: 'java.util.Date'],
                                [name: "lockedDate", display: "Locked Date", dataType: 'java.util.Date'],
                                [name: "listedness", display: "Listedness", dataType: 'java.lang.String'],
                                [name: "outcome", display: "Outcome", dataType: 'java.lang.String'],
                                [name: "serious", display: "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled?'Serious/Non-Serious':'Serious'}", dataType: 'java.lang.String'],
                                [name: "caseReportType", display: "Report Type", dataType: 'java.lang.String'],
                                [name: "country", display: "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled?'Derived Country':'Country'}", dataType: 'java.lang.String'],
                                [name: "reportersHcpFlag", display: "HCP", dataType: 'java.lang.String'],
                                [name: "death", display: "Death", dataType: 'java.lang.String'],
                                [name: "age", display: "Age Group", dataType: 'java.lang.String'],
                                [name: "gender", display: "Gender", dataType: 'java.lang.String'],
                                [name: "caseType", display: "Case Type", dataType: 'java.lang.String'],
                                [name: "completenessScore", display: "Completeness Score", dataType: 'java.lang.Number'],
                                [name: "indNumber", display: "Primary IND#", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "appTypeAndNum", display: "Application#", dataType: 'java.lang.String'],
                                [name: "compoundingFlag", display: "Compounding Flag", dataType: 'java.lang.String'],
                                [name: "medErrorPtList", display: "Medication Error PTs", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "patientAge", display: "Age", dataType: 'java.lang.Number'],
                                [name: "suspectProductList", display: "All Suspect Products", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "rechallenge", display: "Rechallenge", dataType: 'java.lang.String'],
                                [name: "caseNumber", display: "Case(f/u#)", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "pt", display: "PT", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "caseNarrative", display: "Case Narrative", dataType: 'java.lang.String'],
                                [name: "submitter", display: "Submitter", dataType: 'java.lang.String'],
                                [name: "tags", display: "Categories", dataType: 'java.lang.String'],
                                [name: "subTags", display: "Sub Categories", dataType: 'java.lang.String'],
                                [name: "currentRun", display: "Current Period Category", dataType: 'java.lang.Boolean'],
                                [name: "malfunction", display: "Malfunction", dataType: 'java.lang.String'],
                                [name: "comboFlag", display: "Combo Flag", dataType: 'java.lang.String'],
                                [name: "indication", display: "Indication",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "eventOutcome", display: "Event  Outcome",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "causeOfDeath", display: "Cause of Death",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "seriousUnlistedRelated", display: "SUR",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "patientMedHist", display: "Patient Med Hist",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "patientHistDrugs", display: "Patient Hist Drugs",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "batchLotNo", display: "Batch/Lot#",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "timeToOnset", display: "Time To Onset" , dataType: 'java.lang.Number'],
                                [name: "caseClassification", display: "Case Classification",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "initialFu", display: "Initial/F/u",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "protocolNo", display: "Protocol#",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "isSusar", display: "SUSAR", dataType: 'java.lang.String'],
                                [name: "therapyDates", display: "Therapy Dates",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "doseDetails", display: "Dose Details",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "preAnda", display: "Pre-ANDA",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "caseSeries", display: "Case Series", isAutocomplete: true,dataType: 'java.lang.String'],
                                [name: "primSuspProdList", display: "Primary Suspect Product", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "primSuspPaiList", display: "Primary Suspect PAI", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "paiAllList", display: "All Suspect PAIs", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "allPtList", display: "All PTs", isAutocomplete: true, dataType: 'java.lang.String'],
                                [name: "genericName", display: "Generic Name (Suspect)",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "caseCreationDate", display: "Case Creation Date", dataType: 'java.util.Date'],
                                [name: "dateOfBirth", display: "DOB",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "eventOnsetDate", display: "Event Onset Date" ,isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "pregnancy", display: "Pregnancy",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "medicallyConfirmed", display: "Medically Confirmed",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "allPTsOutcome", display: "All PTs - Outcome",isAutocomplete: true , dataType: 'java.lang.String'],
                                [name: "crossReferenceInd", display: "Cross Reference IND",isAutocomplete: true , dataType: 'java.lang.String']]

signal.agaOnDemandColumnList.safetyDb = [[name: "productName", display: "Product Name", dataType: 'java.lang.String'],
                                         [name: "soc", display: "SOC", dataType: 'java.lang.String'],
                                         [name: "listed", display: "Listed", dataType: 'java.lang.String'],
                                         [name: "positiveRechallenge", display: "+ve Rechallenge", dataType: 'java.lang.String'],
                                         [name: "positiveDechallenge", display: "+ve Dechallenge", dataType: 'java.lang.String'],
                                         [name: "related", display: "Related", dataType: 'java.lang.String'],
                                         [name: "pregnancy", display: "Pregnancy", dataType: 'java.lang.String'],
                                         [name: "newSponCount", display: "New Spon", dataType: 'java.lang.Number'],
                                         [name: "cumSponCount", display: "Cum Spon", dataType: 'java.lang.Number'],
                                         [name: "newSeriousCount", display: "New Ser", dataType: 'java.lang.Number'],
                                         [name: "cumSeriousCount", display: "Cum Ser", dataType: 'java.lang.Number'],
                                         [name: "newStudyCount", display: "New Study", dataType: 'java.lang.Number'],
                                         [name: "cumStudyCount", display: "Cum Study", dataType: 'java.lang.Number'],
                                         [name: "newFatalCount", display: "New Fatal", dataType: 'java.lang.Number'],
                                         [name: "cumFatalCount", display: "Cum Fatal", dataType: 'java.lang.Number'],
                                         [name: "newGeriatricCount", display: "New Geria", dataType: 'java.lang.Number'],
                                         [name: "cumGeriatricCount", display: "Cum Geria", dataType: 'java.lang.Number'],
                                         [name: "newNonSerious", display: "${-> grails.util.Holders?.config?.signal?.new?.serious?.column?.header}", dataType: 'java.lang.Number'],
                                         [name: "cumNonSerious", display: "${-> grails.util.Holders?.config?.signal?.cum?.serious?.column?.header}", dataType: 'java.lang.Number'],
                                         [name: "eb05", display: "EB05", dataType: 'java.lang.Number'],
                                         [name: "eb95", display: "EB95", dataType: 'java.lang.Number'],
                                         [name: "ebgm", display: "EBGM", dataType: 'java.lang.Number'],
                                         [name: "prrValue", display: "PRR", dataType: 'java.lang.Number'],
                                         [name: "prrLCI", display: "PRR LCI", dataType: 'java.lang.Number'],
                                         [name: "prrUCI", display: "PRR UCI", dataType: 'java.lang.Number'],
                                         [name: "rorValue", display: "ROR", dataType: 'java.lang.Number'],
                                         [name: "rorLCI", display: "ROR LCI", dataType: 'java.lang.Number'],
                                         [name: "rorUCI", display: "ROR UCI", dataType: 'java.lang.Number'],
                                         [name: "pt", display: "PT/SMQ/Event Group", isAutocomplete: true, dataType: 'java.lang.String'],
                                         [name: "newCount", display: "New Count", dataType: 'java.lang.Number'],
                                         [name: "cummCount", display: "Cum Count", dataType: 'java.lang.Number'],
                                         [name: "newPediatricCount", display: "New Paed", dataType: 'java.lang.Number'],
                                         [name: "cummPediatricCount", display: "Cum Paed", dataType: 'java.lang.Number'],
                                         [name: "newInteractingCount", display: "New Interacting", dataType: 'java.lang.Number'],
                                         [name: "cummInteractingCount", display: "Cum Interacting", dataType: 'java.lang.Number'],
                                         [name: "aggImpEventList", display: "IMP Events", dataType: 'java.lang.String'],
                                         [name: "chiSquare", display: "Chi-Square", dataType: 'java.lang.Number'],
                                         [name: "tags", display: "Categories", dataType: 'java.lang.String'],
                                         [name: "subTags", display: "Sub Categories", dataType: 'java.lang.String'],
                                         [name: "currentRun", display: "Current Period Category", dataType: 'java.lang.Boolean'],
                                         [name: "aValue", display: "A", dataType: 'java.lang.Number'],
                                         [name: "bValue", display: "B", dataType: 'java.lang.Number'],
                                         [name: "cValue", display: "C", dataType: 'java.lang.Number'],
                                         [name: "dValue", display: "D", dataType: 'java.lang.Number'],
                                         [name: "eValue", display: "E", dataType: 'java.lang.Number'],
                                         [name: "rrValue", display: "RR", dataType: 'java.lang.Number']
]
signal.evdasOnDemandColumnList = [[name: "substance", display: "Substance", dataType: 'java.lang.String'],
                                  [name: "listedness", display: "Listed", dataType: 'java.lang.Boolean'],
                                  [name: "soc", display: "SOC", dataType: 'java.lang.String'],
                                  [name: "hlgt", display: "HLGT", dataType: 'java.lang.String'],
                                  [name: "changes", display: "Changes", dataType: 'java.lang.String'],
                                  [name: "hlt", display: "HLT", dataType: 'java.lang.String'],
                                  [name: "sdr", display: "SDR", dataType: 'java.lang.String'],
                                  [name: "sdrPaed", display: "SDR Paed", dataType: 'java.lang.String'],
                                  [name: "sdrGeratr", display: "SDR Geriatr", dataType: 'java.lang.String'],
                                  [name: "smqNarrow", display: "SMQ", dataType: 'java.lang.String'],
                                  [name: "newEv", display: "New EV", dataType: 'java.lang.Number'],
                                  [name: "totalEv", display: "Total EV", dataType: 'java.lang.Number'],
                                  [name: "newSerious", display: "New Ser", dataType: 'java.lang.Number'],
                                  [name: "totalSerious", display: "Total Ser", dataType: 'java.lang.Number'],
                                  [name: "newFatal", display: "New Fatal", dataType: 'java.lang.Number'],
                                  [name: "totalFatal", display: "Total Fatal", dataType: 'java.lang.Number'],
                                  [name: "newRc", display: "New +RC", dataType: 'java.lang.Number'],
                                  [name: "totRc", display: "Total +RC", dataType: 'java.lang.Number'],
                                  [name: "newPaed", display: "New Paed", dataType: 'java.lang.Number'],
                                  [name: "totPaed", display: "Total Paed", dataType: 'java.lang.Number'],
                                  [name: "newGeria", display: "New Geria", dataType: 'java.lang.Number'],
                                  [name: "totGeria", display: "Total Geria", dataType: 'java.lang.Number'],
                                  [name: "newLit", display: "New Lit", dataType: 'java.lang.Number'],
                                  [name: "totalLit", display: "Total Lit", dataType: 'java.lang.Number'],
                                  [name: "newEea", display: "New EEA", dataType: 'java.lang.Number'],
                                  [name: "totEea", display: "Total EEA", dataType: 'java.lang.Number'],
                                  [name: "newMedErr", display: "New Med Err", dataType: 'java.lang.Number'],
                                  [name: "totMedErr", display: "Total Med Err", dataType: 'java.lang.Number'],
                                  [name: "newHcp", display: "New HCP", dataType: 'java.lang.Number'],
                                  [name: "totHcp", display: "Total HCP", dataType: 'java.lang.Number'],
                                  [name: "newObs", display: "New Obs", dataType: 'java.lang.Number'],
                                  [name: "totObs", display: "Total Obs", dataType: 'java.lang.Number'],
                                  [name: "newSpont", display: "New Spont", dataType: 'java.lang.Number'],
                                  [name: "totSpont", display: "Total Spont", dataType: 'java.lang.Number'],
                                  [name: "totSpontRest", display: "Tot Spont Rest", dataType: 'java.lang.Number'],
                                  [name: "totSpontAsia", display: "Tot Spont Asia", dataType: 'java.lang.Number'],
                                  [name: "totSpontJapan", display: "Tot Spont Japan", dataType: 'java.lang.Number'],
                                  [name: "totSpontNAmerica", display: "Tot Spont N America", dataType: 'java.lang.Number'],
                                  [name: "totSpontEurope", display: "Tot Spont Europe", dataType: 'java.lang.Number'],
                                  [name: "rorValue", display: "ROR(-) All", dataType: 'java.lang.Number'],
                                  [name: "asiaRor", display: "ROR(Asia)", dataType: 'java.lang.Number'],
                                  [name: "restRor", display: "ROR(Rest)", dataType: 'java.lang.Number'],
                                  [name: "japanRor", display: "ROR(Japan)", dataType: 'java.lang.Number'],
                                  [name: "europeRor", display: "ROR(Europe)", dataType: 'java.lang.Number'],
                                  [name: "northAmericaRor", display: "ROR(North America)", dataType: 'java.lang.Number'],
                                  [name: "ratioRorPaedVsOthers", display: "Relative ROR (-) Paed vs Others", dataType: 'java.lang.Number'],
                                  [name: "ratioRorGeriatrVsOthers", display: "Relative ROR (-) Geriatr vs Others", dataType: 'java.lang.Number'],
                                  [name: "pt", display: "PT/SMQ/Event Group", isAutocomplete: true, dataType: 'java.lang.String'],
                                  [name: "dmeIme", display: "IME/DME", isAutocomplete: false, dataType: 'java.lang.String'],
                                  [name: "evImpEventList", display: "IMP Events", dataType: 'java.lang.String'],]

signal.scaFaersColumnList = [[name: "productName", display: "Product Name", dataType: 'java.lang.String'],
                             [name: "ptList", display: "All PTs (SUR)", dataType: 'java.lang.String'],
                             [name: "conComitList", display: "All Conmeds", dataType: 'java.lang.String'],
                             [name: "caseInitReceiptDate", display: "Receipt Date", dataType: 'java.util.Date'],
                             [name: "lockedDate", display: "Locked Date", dataType: 'java.util.Date'],
                             [name: "outcome", display: "Outcome", dataType: 'java.lang.String'],
                             [name: "serious", display: "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled?'Serious/Non-Serious':'Serious'}", dataType: 'java.lang.String'],
                             [name: "caseReportType", display: "Report Type", dataType: 'java.lang.String'],
                             [name: "country", display: "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled?'Derived Country':'Country'}", dataType: 'java.lang.String'],
                             [name: "reportersHcpFlag", display: "HCP", dataType: 'java.lang.String'],
                             [name: "death", display: "Death", dataType: 'java.lang.String'],
                             [name: "age", display: "Age Group", dataType: 'java.lang.String'],
                             [name: "gender", display: "Gender", dataType: 'java.lang.String'],
                             [name: "caseType", display: "Case Type", dataType: 'java.lang.String'],
                             [name: "completenessScore", display: "Completeness Score", dataType: 'java.lang.Number'],
                             [name: "indNumber", display: "Primary IND#", dataType: 'java.lang.String'],
                             [name: "appTypeAndNum", display: "Application#", dataType: 'java.lang.String'],
                             [name: "compoundingFlag", display: "Compounding Flag", dataType: 'java.lang.String'],
                             [name: "medErrorsPt", display: "Medication Error PTs", dataType: 'java.lang.String'],
                             [name: "patientAge", display: "Age", dataType: 'java.lang.Number'],
                             [name: "suspectProductList", display: "All Suspect Products", dataType: 'java.lang.String'],
                             [name: "rechallenge", display: "Rechallenge", dataType: 'java.lang.String'],
                             [name: "caseNumber", display: "Case(f/u#)", isAutocomplete: true, dataType: 'java.lang.String'],
                             [name: "pt", display: "PT/SMQ/Event Group", isAutocomplete: true, dataType: 'java.lang.String'],
                             [name: "caseNarrative", display: "Case Narrative", dataType: 'java.lang.String'],
                             [name: "submitter", display: "Submitter", dataType: 'java.lang.String'],
                             [name: "indication", display: "Indication",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "seriousUnlistedRelated", display: "SUR",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "batchLotNo", display: "Batch/Lot#",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "initialFu", display: "Initial/F/u",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "isSusar", display: "SUSAR" , dataType: 'java.lang.String'],
                             [name: "therapyDates", display: "Therapy Dates",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "doseDetails", display: "Dose Details",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "preAnda", display: "Pre-ANDA",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "comments", display: "Comments", dataType: 'java.lang.String'],
                             [name: "justification", display: "Justification",isAutocomplete: true , dataType: 'java.lang.String'],
                             [name: "primSuspPaiList", display: "Primary Suspect PAI", isAutocomplete: true, dataType: 'java.lang.String'],
                             [name: "paiAllList", display: "All Suspect PAIs", isAutocomplete: true, dataType: 'java.lang.String'],
                             [name: "allPtList", display: "All PTs", dataType: 'java.lang.String']]


signal.scaVigibaseColumnList = [
        ['name':'caseNumber', 'display':'Case', 'isAutocomplete':true, 'dataType':'java.lang.String'],
        [name: "caseInitReceiptDate", display: "Receipt Date", dataType: 'java.util.Date'],
        [name: "productName", display: "Product Name", dataType: 'java.lang.String'],
        [name: "pt", display: "PT/SMQ/Event Group", isAutocomplete: true, dataType: 'java.lang.String'],
        [name: "age", display: "Age", dataType: 'java.lang.Number'],
        [name: "gender", display: "Gender", dataType: 'java.lang.String'],
        [name: "caseReportType", display: "Report Type", dataType: 'java.lang.String'],
        [name: "eventOutcome", display: "Event  Outcome",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "serious", display: "Serious", dataType: 'java.lang.String'],
        [name: "indication", display: "Indication",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "suspectProductList", display: "All Suspect Products", dataType: 'java.lang.String'],
        [name: "allPtList", display: "All PTs", dataType: 'java.lang.String'],
        [name: "conComit", display: "All Conmeds", dataType: 'java.lang.String'],
        [name: "initialFu", display: "Initial/F/u",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "region", display: "Region", dataType: 'java.lang.String'],
        [name: "tags", display: "Categories", dataType: 'java.lang.String'],
        [name: "comments", display: "Comments", dataType: 'java.lang.String']]
signal.scaJaderColumnList = [
        [name: "productName", display: "Product Name", dataType: 'java.lang.String'],
        [name: "conComitList", display: "All Conmeds",isAutocomplete: true, dataType: 'java.lang.String'],
        [name: "caseInitReceiptDate", display: "Initial Receipt Date", dataType: 'java.util.Date'],
        [name: "caseReportType", display: "Report Type", dataType: 'java.lang.String'],
        [name: "reportersHcpFlag", display: "HCP", dataType: 'java.lang.String'],
        [name: "death", display: "Death", dataType: 'java.lang.String'],
        [name: "gender", display: "Gender", dataType: 'java.lang.String'],
        [name: "age", display: "Age", dataType: 'java.lang.Number'],
        [name: "suspectProductList", display: "All Suspect Products",isAutocomplete: true, dataType: 'java.lang.String'],
        [name: "caseNumber", display: "Case(f/u#)", isAutocomplete: true, dataType: 'java.lang.String'],
        [name: "pt", display: "PT/SMQ/Event Group", isAutocomplete: true, dataType: 'java.lang.String'],
        [name: "genericName", display: "Generic Name",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "eventOnsetDate", display: "Onset Date" ,isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "eventOutcome", display: "Event  Outcome",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "patientMedHist", display: "Patient Med Hist",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "tags", display: "Categories", dataType: 'java.lang.String'],
        [name: "reporterQualification", display: "Reporter Qualification",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "riskCategory", display: "Risk category",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "indication", display: "Indication",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "therapyDates", display: "Therapy Dates",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "doseDetails", display: "Dose Details",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "comments", display: "Comments", dataType: 'java.lang.String'],
        [name: "rechallenge", display: "Rechallenge", dataType: 'java.lang.String'],
//        [name: "justification", display: "Justification",isAutocomplete: true , dataType: 'java.lang.String'],
        [name: "allPtList", display: "All PTs",isAutocomplete: true, dataType: 'java.lang.String']]

agaColumnExcelExportMap = ['eb05': 'eb95', 'prrLCI': 'prrUCI', 'rorLCI': 'rorUCI', 'eb05Faers': 'eb95Faers', 'prrLCIFaers': 'prrUCIFaers', 'rorLCIFaers': 'rorUCIFaers', 'eb05Vaers': 'eb95Vaers', 'prrLCIVaers': 'prrUCIVaers', 'rorLCIVaers': 'rorUCIVaers', 'eb05Vigibase': 'eb95Vigibase', 'prrLCIVigibase': 'prrUCIVigibase', 'rorLCIVigibase': 'rorLCIVigibase', 'newEvEvdas': 'totalEvEvdas', 'newSerious': 'totalSerious', 'newFatal': 'totalFatal', 'newCount': 'cummCount', 'newPediatricCount': 'cummPediatricCount', 'newInteractingCount': 'cummInteractingCount', "newPediatricCountFaers": "cummPediatricCountFaers", "newInteractingCountFaers": "cummInteractingCountFaers", "newCountFaers": "cummCountFaers", "newPediatricCountVaers": "cummPediatricCountVaers", "newGeriatricCountVaers": "cumGeriatricCountVaers", "newCountVaers": "cummCountVaers", "newPediatricCountVigibase": "cummPediatricCountVigibase", "newGeriatricCountVigibase": "cumGeriatricCountVigibase", "newCountVigibase": "cummCountVigibase", "newEeaEvdas": "totEeaEvdas", "newHcpEvdas": "totHcpEvdas", "newSeriousEvdas": "totalSeriousEvdas", "newMedErrEvdas": "totMedErrEvdas", "newObsEvdas": "totObsEvdas", "newRcEvdas": "totRcEvdas", "newLitEvdas": "totalLitEvdas", "newGeriaEvdas": "totGeriaEvdas", "newSpontEvdas": "totSpontEvdas", "newPaedEvdas": "totPaedEvdas", "newFatalEvdas": "totalFatalEvdas", "freqPeriod": "cumFreqPeriod", "reviewedFreqPeriod": "reviewedCumFreqPeriod", "eb05Jader": "eb95Jader", "prrLCIJader": "prrUCIJader", "rorLCIJader": "rorUCIJader", "newPediatricCountJader": "cumPediatricCountJader", "newGeriatricCountJader": "cumGeriatricCountJader", "newCountJader": "cumCountJader", "newFatalCountJader": "cumFatalCountJader", "newSeriousCountJader": "cumSeriousCountJader"]

//////////////////SAML RELATED CONFIGS//////////////////////////////////////////


grails.plugin.springsecurity.failureHandler.exceptionMappings = [
        [exception: com.rxlogix.user.sso.exception.SSOUserLockedException.name, url: '/login/ssoAuthFail?locked=true'],
        [exception: com.rxlogix.user.sso.exception.SSOUserNotConfiguredException.name, url: '/login/ssoAuthFail?notfound=true'],
        [exception: com.rxlogix.user.sso.exception.SSOUserAccountExpiredException.name, url: '/login/ssoAuthFail?notfound=true']
]

grails.plugin.springsecurity.saml.active = false
grails.plugin.springsecurity.saml.metadata.url = '/saml/metadata'
grails.plugin.springsecurity.saml.afterLoginUrl = '/'
grails.plugin.springsecurity.saml.afterLogoutUrl = '/'
grails.plugin.springsecurity.saml.responseSkew = 300000  // seconds
grails.plugin.springsecurity.saml.signatureAlgorithm = 'rsa-sha512'
grails.plugin.springsecurity.saml.digestAlgorithm = 'sha256'
grails.plugin.springsecurity.saml.autoCreate.active = false  //If you want the plugin to generate users in the DB as they are authenticated via SAML
grails.plugin.springsecurity.saml.autoCreate.assignAuthorities = false

grails.plugin.springsecurity.saml.userGroupAttribute = "memberOf"
//grails.plugin.springsecurity.saml.metadata.providers = [[pvsignal: "/home/ashok/projects/pvs/pvsignal/security/client-tailored-saml-idp-metadata.xml"]]
grails.plugin.springsecurity.saml.metadata.providers = [[pvsignal: "${userHome}/.reports/client-tailored-saml-idp-metadata.xml"]]
grails.plugin.springsecurity.saml.metadata.sp.file = "${userHome}/.reports/saml-sp-metadata.xml"

//grails.plugin.springsecurity.saml.metadata.sp.file = "/home/ashok/projects/pvs/pvsignal/security/saml-sp-metadata.xml"
grails.plugin.springsecurity.saml.autoCreate.active = true // this is must set TRUE for getCurrentUser() to work with SAML - PVR-6910
grails.plugin.springsecurity.saml.autoCreate.assignAuthorities = true
grails.plugin.springsecurity.saml.keyManager.storeFile = "file:${userHome}/.reports/keystore.jks"
grails.plugin.springsecurity.saml.keyManager.storePass = "changeit"
grails.plugin.springsecurity.saml.keyManager.passwords.keyAlias = 'pvsignal'
grails.plugin.springsecurity.saml.keyManager.passwords.keyPass = "changeit"
grails.plugin.springsecurity.saml.keyManager.passwords = [pvsignal: "changeit"]
grails.plugin.springsecurity.saml.keyManager.defaultKey = "pvsignal"
grails.plugin.springsecurity.saml.defaultKey = 'pvsignal'
grails.plugin.springsecurity.saml.metadata.defaultIdp = 'http://127.0.0.1:6060/auth/realms/master'
grails.plugin.springsecurity.saml.autoCreate.key = 'username'
grails.plugin.springsecurity.saml.autoCreate.assignAuthorities = true  //If you want the plugin to assign the authorities that come from the SAML message.
grails.plugin.springsecurity.saml.maxAuthenticationAge = 7200 //use in case authentication issue date can be of long back.
grails.plugin.springsecurity.saml.metadata.sp.defaults = [

        local                       : true,
        entityId                    : 'pvsignal',
        alias                       : 'pvsignal',
        securityProfile             : 'metaiop',
        signingKey                  : 'pvsignal',
        encryptionKey               : 'pvsignal',
        tlsKey                      : 'pvsignal',
        requireArtifactResolveSigned: false,
        requireLogoutRequestSigned  : true,
        requireLogoutResponseSigned : true
]

saml.local.logout.enabled = false
saml.local.logout.url = "/signal/saml/logout?local=true"

//Properties to set the load balancer related SAML context provider.
saml.lb.enabled = false
saml.lb.serverName = "proxy-server"
saml.lb.port = 443
saml.lb.scheme = "https"
saml.lb.contextPath = "/signal"

//////////////////SAML RELATED CONFIGS//////////////////////////////////////////


endpoints.enabled = false
endpoints.info.enabled = true
management.info.git.mode = 'full'

supported.datasource = { ->
    def dataSourceList = []
    dataSourceList << "pva"
    if (grails.util.Holders?.config?.signal?.faers?.enabled) {
        dataSourceList << "faers"
    }
    if (grails.util.Holders?.config?.signal?.vaers?.enabled) {
        dataSourceList << "vaers"
    }
    if (grails.util.Holders?.config?.signal?.vigibase?.enabled) {
        dataSourceList << "vigibase"
    }
    if (grails.util.Holders?.config?.signal?.jader?.enabled) {
        dataSourceList << "jader"
    }
    if (grails.util.Holders?.config?.signal?.evdas?.enabled) {
        dataSourceList << "eudra"
    }

    return dataSourceList
}

quartz.props.threadPool.jader.threadCount = 10
advancedFilter.rpt.field.map = ["country": "${-> grails.util.Holders?.config?.custom?.caseInfoMap?.Enabled?'csiderived_country':'masterCountryId'}", "submitter": "csisender_organization"]
signalManagement.productSummary.enabled = false
signal.dataBaseInterceptor.check = true
alertExecution.status.aws.failure.code = 5
auto.alert.job.cron.exp = '0 */8 * ? * *'

signal.description.migration.start.comment = "--Migrated description--"
signal.description.migration.end.comment = "--End of migrated description--"
signal.timespan.deletion.ondemand.alert = 90

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/**', access: ['permitAll']],
        [pattern: '/logout/local', access: ['permitAll']],
        [pattern: '/console/**', access: ['ROLE_DEV']],
        [pattern: '/saml/metadata', access: ['permitAll']],
        [pattern: '/monitoring/**', access: ['ROLE_DEV']],
        [pattern: '/dbconsole/**', access: ['ROLE_DEV']],
        [pattern: '/error', access: ['permitAll']],
        [pattern: '/index', access: ['permitAll']],
        [pattern: '/index.gsp', access: ['permitAll']],
        [pattern: '/shutdown', access: ['permitAll']],
        [pattern: '/assets/**', access: ['permitAll']],
        [pattern: '/controlPanel/**', access: ['isAuthenticated()']],
        [pattern: '/auditLogEvent/**', access: ['IS_AUTHENTICATED_FULLY']],
        [pattern: '/**/js/**', access: ['permitAll']],
        [pattern: '/**/css/**', access: ['permitAll']],
        [pattern: '/**/images/**', access: ['permitAll']],
        [pattern: '/**/favicon.ico', access: ['permitAll']],
        [pattern: '/spotfire/rx_validate', access: ['permitAll']],
        [pattern: '/studyDictionary/**', access: ['isAuthenticated()']],
        [pattern: '/eventDictionary/**', access: ['isAuthenticated()']],
        [pattern: '/productDictionary/**', access: ['isAuthenticated()']],
        [pattern: '/info', access: ['ROLE_DEV']],
        [pattern: '/health/**', access: ['permitAll']],
        [pattern: '/webjars/**', access: ['ROLE_DEV']],
        [pattern: '/mailOAuth/**', access: ['ROLE_DEV']],
        [pattern: '/scim/v2/**', access: ['permitAll']], //Required for Grails SCIM end points.
        [pattern: '/scim**/**', access: ['permitAll']], //Required for Grails SCIM end points.
        [pattern: '/export/**',access:['isAuthenticated()']],
        [pattern: '/services/**',access:['isAuthenticated()']],
        [pattern: '/MetaData/**',access:['isAuthenticated()']],
        [pattern: '/applicationNotification/**', access: ['permitAll']],//Required for hazelcast notification for configuration update on
        [pattern: '/businessConfig/**', access: ['permitAll']]
]
signal.customTimeZoneMap.abbreviatedTimeZoneMap = ["EST5EDT"       : "America/New_York", "EST": "America/New_York", "EET": "Asia/Nicosia", "CET": "Africa/Windhoek", "CST6CDT": "America/Winnipeg",
                                                   "HST"           : "Etc/GMT+10", "America/Punta_Arenas": "America/Cuiaba", "Asia/Atyrau": "Asia/Oral", "Asia/Barnaul": "Asia/Jakarta",
                                                   "Asia/Famagusta": "Africa/Addis_Ababa", "Asia/Tomsk": "Asia/Bangkok", "Europe/Astrakhan": "Asia/Dubai", "Europe/Kirov": "Asia/Aden",
                                                   "Europe/Saratov": "Asia/Dubai", "Europe/Ulyanovsk": "Asia/Dubai", "MET": "Europe/Zurich", "MST": "America/Yellowknife",
                                                   "MST7MDT"       : "America/Yellowknife", "PST8PDT": "America/Whitehorse", "WET": "UTC"]