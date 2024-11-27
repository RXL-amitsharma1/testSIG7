package com.rxlogix

import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

class NonCacheSelectableList implements SelectableList {

    def log = LoggerFactory.getLogger(NonCacheSelectableList.class)

    @Autowired
    DataSource pva
    String sqlString
    String fieldName

    @Override
    def getSelectableList() {
        Sql sql = null
        try {
            sql = new Sql(pva)
            // The SQL only has one column to return
            if(Holders.config.sqlString.nonCahcheSelectable.enabled){
                Map sqlMap = Holders.config.sqlString.nonCahcheSelectable.map
                if(this.fieldName && sqlMap.containsKey(this.fieldName))
                {
                    sqlString = sqlMap.get(this.fieldName)
                    return sql.rows(sqlString).collect { it[0] }
                }
            }
        } catch (Throwable e) {
            log.info("Exception while executing sql in table column selectable list" + this.fieldName)
        } finally {
            if (sql) {
                sql.close()
            }
        }
        return []
    }

    Map getPaginatedSelectableList(String searchTerm, int max, int offset, int tenantId) {
        Map resultMap = [result: [], totalCount: 0]
        Sql sql = new Sql(pva)
        try {
            // The SQL only has one column to return
            resultMap.result = sql.rows(sqlString + " OFFSET :offset ROWS FETCH NEXT :max ROWS ONLY", [max: max, offset: offset, TENANT_ID: tenantId, SEARCH_TERM: (searchTerm ? "%${searchTerm}%" : '%').toUpperCase()]).collect {
                it[0]
            }
            resultMap.totalCount= sql.rows(sqlString , [TENANT_ID: tenantId, SEARCH_TERM: (searchTerm ? "%${searchTerm}%" : '%').toUpperCase()]).collect {
                it[0]
            }?.size()
        } catch (Exception e) {
            log.error("exception while executing sql in table column selectable list for SQL ${sqlString} with message ${e.message}")
        } finally {
            sql.close()
        }
        return resultMap
    }

    def setDataSource(DataSource dataSource) {
        this.pva = dataSource
    }
}