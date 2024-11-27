println "##############Started Loading External Config###############"

grails.plugin.springsecurity.ldap.context.managerDn = 'PVS_LDAP_CONTEXT_MANGER_DN'
grails.plugin.springsecurity.ldap.context.managerPassword = 'PVS_LDAP_ADMIN_USER_PASSWORD'
grails.plugin.springsecurity.ldap.context.server = 'PVS_LDAP_SERVER_FQDN_NAME'
grails.plugin.springsecurity.ldap.search.base = 'PVS_LDAP_SEARCH_BASE'
grails.plugin.springsecurity.ldap.authorities.groupSearchBase = 'PVS_LDAP_GROUP_SEARCH_BASE'
grails.plugin.springsecurity.ldap.search.filter = 'PVS_LDAP_SEARCH_FILTER'
grails.plugin.springsecurity.ldap.authorities.groupSearchFilter = 'PVS_LDAP_GROUP_SEARCH_FILTER'
grails.plugin.springsecurity.ldap.fullName.attribute = "PVS_LDAP_FULLNAME_ATTRIBUTE"
grails.plugin.springsecurity.ldap.email.attribute = "PVS_LDAP_MAIL_ATTRIBUTE"
grails.plugin.springsecurity.ldap.uid.attribute = "PVS_LDAP_USERNAME_ATTRIBUTE"
grails.plugin.springsecurity.ldap.users.search.filter = 'PVS_LDAP_USERS_SEARCH_FILTER'

grails.attachmentable.uploadDir = "INSTALLATION_DIRECTORY/.signal"
//Hazelcast Configurations
hazelcast.server.instance.name ='SERVER_IP'

tempDirectory = "INSTALLATION_DIRECTORY/.signal"

hazelcast.network.nodes = ["PVS_HAZELCAST_NETWORK_NODES"]	// User input


environments{
	production{
		dataSources{
			dataSource{
				driverClassName = 'oracle.jdbc.OracleDriver'
				dialect = "org.hibernate.dialect.Oracle12cDialect"
				url = 'jdbc:oracle:thin:@PVS_PVA_DB_HOSTNAME_PORT_AND_INSTANCE'
				username = 'PVS_WEB_APP_USER'
				password= '{cipher}PVS_WEB_APP_USER_PASSWORD'
				passwordEncryptionCodec="com.rxlogix.RxCodec"
				properties = oracleProperties
			}
			
			pva	{
				driverClassName = 'oracle.jdbc.OracleDriver'
				dialect = "org.hibernate.dialect.Oracle12cDialect"
				url = 'jdbc:oracle:thin:@PVS_PVA_DB_HOSTNAME_PORT_AND_INSTANCE'
				username = 'PVS_PVA_USER'
				password= '{cipher}PVS_PVA_USER_PASSWORD'
				passwordEncryptionCodec="com.rxlogix.RxCodec"
				readOnly = true
				properties = oracleProperties_mart
			}
			
			eudra	{
				driverClassName = 'oracle.jdbc.OracleDriver'
				dialect = "org.hibernate.dialect.Oracle12cDialect"
				url = "jdbc:oracle:thin:@PVS_PVA_DB_HOSTNAME_PORT_AND_INSTANCE"
				username = 'PVS_EVDAS_USER'
				password= '{cipher}PVS_EVDAS_USER_PASSWORD'
				passwordEncryptionCodec="com.rxlogix.RxCodec"
				readOnly = true
				properties = oracleProperties_mart
			}
			
			faers	{
				driverClassName = 'oracle.jdbc.OracleDriver'
				dialect = "org.hibernate.dialect.Oracle12cDialect"
				url = "jdbc:oracle:thin:@PVS_FAERS_DB_HOSTNAME_PORT_AND_INSTANCE"
				username = 'PVS_FAERS_USER'
				password= '{cipher}PVS_FAERS_USER_PASSWORD'
				passwordEncryptionCodec="com.rxlogix.RxCodec"
				readOnly = true
				properties = oracleProperties_mart
			}
			
			spotfire{
				driverClassName = 'oracle.jdbc.OracleDriver'
				dialect = "org.hibernate.dialect.Oracle10gDialect"
				url = "jdbc:oracle:thin:@PVS_PVA_DB_HOSTNAME_PORT_AND_INSTANCE"
				username = "PVS_PVA_USER"
				password = "{cipher}PVS_PVA_USER_PASSWORD"
				passwordEncryptionCodec="com.rxlogix.RxCodec"
				readOnly = true
				properties = oracleProperties_mart
			}
		}
	}
}



statistics.url = "PVS_STATISTICS_SERVER_URL"
statistics.enable.prr = PVS_PRR_ENABLED
statistics.enable.ror = PVS_ROR_ENABLED
statistics.enable.ebgm = PVS_EBGM_ENABLED

// EMAIL CONFIGURATIONS
grails.mail.default.from = "PVS_DEFAULT_EMAIL_ID"
grails.mail.enabled =  GRAILS_MAIL_ENABLED

grails {
	mail {
		host = "PVS_SMTP_SERVER"
		port = PVS_SMTP_PORT
		username = "PVS_SMTP_USERNAME"
		password = "PVS_SMTP_PASSWORD"
		props = [
				"mail.smtp.starttls.enable" : "true",
				"mail.smtp.ehlo" : "false",
				"mail.smtp.port" : "PVS_SMTP_PORT",
				"mail.smtp.auth" : "false",
				"mail.smtp.debug" : "false"
		]
	}
}

pvsignal.seeding.user = "SEEDING_USER"
grails.serverURL="PVS_APP_SERVER_URL"
signal.serverURL="PVS_APP_SERVER_URL"



pvreports.url="PVS_PVREPORTS_URL"
pvreports.web.url="PVS_PVREPORTS_URL"

signal.evdas.case.line.listing.import.folder.base = "INSTALLATION_DIRECTORY/evdas_caseListing"
signal.evdas.case.line.listing.import.folder.read = "${signal.evdas.case.line.listing.import.folder.base}/read"
signal.evdas.case.line.listing.import.folder.upload = "${signal.evdas.case.line.listing.import.folder.base}/upload"
signal.evdas.case.line.listing.import.folder.success = "${signal.evdas.case.line.listing.import.folder.base}/success"
signal.evdas.case.line.listing.import.folder.fail = "${signal.evdas.case.line.listing.import.folder.base}/fail"

signal.evdas.data.import.folder.base = "INSTALLATION_DIRECTORY/evdas_ermr"
signal.evdas.data.import.folder.read = "${signal.evdas.data.import.folder.base}/read"
signal.evdas.data.import.folder.upload = "${signal.evdas.data.import.folder.base}/upload"
signal.evdas.data.import.folder.success = "${signal.evdas.data.import.folder.base}/success"
signal.evdas.data.import.folder.fail = "${signal.evdas.data.import.folder.base}/fail"

pvcc.api.url = "PVCC_API_URL"



//Delta config for 4.8 release
app.dictionary.base.url =  "PVS_PVREPORTS_URL"

//Delta config for 5.0 release

//Delta config for 5.1 release


//Delta config for 5.2 release

//Delta config for 5.3 release


environments{
	production{
		dataSources{
			vaers	{
				driverClassName = 'oracle.jdbc.OracleDriver'
				dialect = "org.hibernate.dialect.Oracle12cDialect"
				url = "jdbc:oracle:thin:@PVS_VAERS_DB_HOSTNAME_PORT_AND_INSTANCE"
				username = 'PVS_VAERS_USER'
				password= '{cipher}PVS_VAERS_USER_PASSWORD'
				passwordEncryptionCodec="com.rxlogix.RxCodec"
				readOnly = true
				properties = oracleProperties_mart
			}
		}
	}
}



//Delta config for 5.4 release

disposition.signal.outcome.mapping.enabled = DISPOSITION_SIGNAL_OUTCOME_MAPPING_ENABLED
dss.scores.url = "PVS_DSS_SERVER_URL"
statistics.enable.dssScores = PVS_DSS_ENABLED
signal.agg.calculate.trend.flag = ENABLE_TREND_FLAG_CALCULATION

signal.categories.migration.enabled = SIGNAL_CATEGORIES_MIGRATION_ENABLED
dss.enable.autoProposed = DSS_ENABLE_AUTOPROPOSED

validatedSignal.end.of.review = SIGNAL_END_OF_REVIEW

//Delta Config for 5.4.1 release

//Delta Config for 5.4.2 release

//Delta config for 5.5 release

environments{
	production{
		dataSources{
			vigibase {
				driverClassName = 'oracle.jdbc.OracleDriver'
				dialect = 'org.hibernate.dialect.Oracle12cDialect'
				url = 'jdbc:oracle:thin:@PVS_VIGIBASE_DB_HOSTNAME_PORT_AND_INSTANCE'
				username = 'PVS_VIGIBASE_USER'
				password = '{cipher}PVS_VIGIBASE_USER_PASSWORD'
				readOnly = true
				properties = oracleProperties_mart
			}
		}
	}
}

//Delta config for 5.6 release

println "##############Finished Loading External Config###############"
