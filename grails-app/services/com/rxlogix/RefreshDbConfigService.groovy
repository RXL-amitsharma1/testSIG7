package com.rxlogix

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql

import javax.sql.DataSource

@Transactional
class RefreshDbConfigService {

    DataSource dataSource_pva
    def signalDataSourceService

    void updateAppConfigurationsFromMart(String appName) {
        fetchConfigData(appName)
        log.debug("DB config update complete from app")
    }

    void fetchConfigData(String appName) {
        Sql sqlPva
        Sql sql
        try {
            sqlPva = new Sql(dataSource_pva)
            String insertStatements = "Begin execute immediate('delete from gtt_app_config');\n"
            String hashCodeStatement = "SELECT * FROM VW_CMT_SOURCE_HASH"
            String safetyHashCode = sqlPva.firstRow(hashCodeStatement).KEY_VALUE.toString()
            if (safetyHashCode) {
                String dbName = appName?.split("-")[0]
                def db = signalDataSourceService.getReportConnection(dbName?.toLowerCase())
                sql = new Sql(db)
                String externalHashCode = sql.firstRow(hashCodeStatement).KEY_VALUE.toString()
                if (externalHashCode == null || externalHashCode == "null") {
                    log.debug("No hashCode found on ${dbName?.toLowerCase()} DB. Going to seed hashCode ${safetyHashCode} from safety DB.")
                    String updateExternalHashCode = "UPDATE VW_CMT_SOURCE_HASH SET KEY_VALUE = '${safetyHashCode}'"
                    sql.execute(updateExternalHashCode)
                    externalHashCode = safetyHashCode
                }
                if (safetyHashCode != externalHashCode) {
                    log.warn("HashCode between safety DB and ${dbName?.toLowerCase()} DB is not matching. DB configuration refresh will be skipped.")
                } else {
                    if (db) {
                        sqlPva.eachRow("SELECT * FROM ${Holders.config.appConfig.view.name} WHERE APPLICATION_NAME='${appName}'", []) { sqlRow ->
                            insertStatements += "INSERT INTO gtt_app_config (ID, APPLICATION_NAME,CONFIG_DATA_TYPE, CONFIG_KEY, CONFIG_VALUE, DISPLAY_LABEL, " +
                                    "IS_MANDATORY, CREATED_DATE, UPDATED_DATE, CREATED_BY, MODIFIED_BY , CATEGORY, IS_DISPLAY, IS_ADVANCED, IS_MODIFIED_BY_USER, DESCRIPTION\n) " +
                                    "VALUES (${sqlRow.ID},'${sqlRow.APPLICATION_NAME}','${sqlRow.CONFIG_DATA_TYPE}','${sqlRow.CONFIG_KEY}'," +
                                    (sqlRow.CONFIG_VALUE ? "'${sqlRow.CONFIG_VALUE.stringValue()}'" : "null") +
                                    ",'${sqlRow.DISPLAY_LABEL}',${sqlRow.IS_MANDATORY}" +
                                    ",TO_TIMESTAMP('${sqlRow.CREATED_DATE}', 'YYYY-MM-DD HH24:MI:SS.FF'),TO_TIMESTAMP('${sqlRow.UPDATED_DATE}', 'YYYY-MM-DD HH24:MI:SS.FF')," +
                                    (sqlRow.CREATED_BY ? "'${sqlRow.CREATED_BY}'" : 'null') + "," +
                                    (sqlRow.MODIFIED_BY ? "'${sqlRow.MODIFIED_BY}'" : 'null') + ", " +
                                    "'${sqlRow.CATEGORY}',${sqlRow.IS_DISPLAY}," +
                                    "${sqlRow.IS_ADVANCED},${sqlRow.IS_MODIFIED_BY_USER},'${sqlRow.DESCRIPTION}');" + "\n"
                        }
                        log.debug("Going to sync db configurations for : " + dbName)

                        log.debug("DB : ${dbName?.toLowerCase()} , Query :" + insertStatements)
                        sql.execute(insertStatements + "\n END;")
                        sql.call("CALL p_pop_pvs_app_config()")
                    } else {
                        log.debug("DB '${dbName?.toLowerCase()}' not found.")
                    }
                }
            } else {
                log.warn("HashCode not found in safety DB. DB configuration refresh will be skipped.")
            }
        } catch (Exception exception) {
            log.error("An error Occurred when syncing config", exception)
        } finally {
            sqlPva?.close()
            sql?.close()
        }
    }
    void updateConfigurableAggFields(String appName){
        log.info("Configurable Agg Field Update Started from CMT for ${appName}")
        Sql sql
        Sql sqlPva
        List<String> keyIds = []
        def configDataList = []
        try {
            sqlPva = new Sql(signalDataSourceService.getReportConnection('pva'))
            String dbName = appName?.split("-")[0]
            def db = signalDataSourceService.getReportConnection(dbName?.toLowerCase())
            sql = new Sql(db)
            String sqlQuery = """
select key_id from AGG_RPT_FIELD_MAPPING where key_id is not null
"""
            sql.eachRow(sqlQuery) { row ->
                keyIds.add(row.key_id)
            }
            if(keyIds){
                def quotedKeyIds = keyIds.collect { "'${it}'" }
                sqlQuery = """
SELECT config_key, JSON_VALUE(config_value, '\$.SELECTABLE') AS selectable FROM vw_admin_app_config where config_key in (${quotedKeyIds.join(",")}) and application_name = '${appName}' and JSON_VALUE(config_value, '\$.CONFIGURABLE') = '1'
"""
                sqlPva.eachRow(sqlQuery) { row ->
                    if (row.selectable != null) {
                        configDataList.add([configKey: row.config_key, selectable: row.selectable])
                    }
                }
                if(configDataList){
                    sqlQuery = """
UPDATE AGG_RPT_FIELD_MAPPING
                SET enabled = CASE key_id 
"""
                    configDataList.each { config ->
                        String configKey = config.configKey
                        int selectable = config.selectable as int

                        sqlQuery += "WHEN '${configKey}' THEN ${selectable} "
                    }
                    sqlQuery += """
                ELSE enabled 
                END 
                WHERE key_id IN (${configDataList.collect { "'${it.configKey}'" }.join(",")})
            """
                    sql.execute(sqlQuery)
                    log.info("Configurable Agg Field Update Finished  for ${appName}")
                }
            }

        }catch(Exception ex){
            ex.printStackTrace()
        } finally {
            sql?.close()
            sqlPva?.close()
        }

    }

}
