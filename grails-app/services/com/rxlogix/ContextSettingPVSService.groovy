package com.rxlogix

import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql

import javax.sql.DataSource

@Transactional
class ContextSettingPVSService {
    def signalDataSourceService
    def dataSource

    def serviceMethod() {
        Sql sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
        try{
            String webAppSchema = Holders.getConfig().toProperties().getProperty("dataSources.dataSource.username")
            log.info("${webAppSchema} schema is pass to context_setting_pvs db procedure")
            String query = SignalQueryHelper.context_setting_pvs_func_creation()
            sql.execute(query)
            query = SignalQueryHelper.context_setting_pvs_drop_policies()
            sql.execute(query)
            query = SignalQueryHelper.enable_row_level_security_sca()
            sql.execute(query)
            query = SignalQueryHelper.context_setting_pvs_add_policy_SCA(webAppSchema)
            sql.execute(query)
            query = SignalQueryHelper.enable_row_level_security_asca()
            sql.execute(query)
            query = SignalQueryHelper.context_setting_pvs_add_policy_ArchivedSCA(webAppSchema)
            sql.execute(query)
            query = SignalQueryHelper.context_setting_pvs_replace_procedure_p_set_context_sec()
            sql.execute(query)
            query = SignalQueryHelper.context_setting_pvs_replace_procedure_p_set_context()
            sql.execute(query)
            query = SignalQueryHelper.context_setting_pvs_p_set_context()
            sql.execute(query)

        } catch(Exception ex){
            ex.printStackTrace()
            log.error(ex.stackTrace.toString())
        } finally {
            sql?.close()
        }

    }
}
