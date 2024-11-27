package com.rxlogix
import groovy.sql.Sql
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

class TableColumnSelectableList implements SelectableList {
    def log = LoggerFactory.getLogger(TableColumnSelectableList.class)

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
            return sql.rows(sqlString).collect { it[0] }
        } catch (Throwable e) {
            log.info("Exception while executing sql in table column selectable list" + this.fieldName)
        } finally {
            if (sql) {
                sql.close()
            }
        }
        return []
    }

    def setDataSource(DataSource dataSource) {
        this.pva = dataSource
    }
}