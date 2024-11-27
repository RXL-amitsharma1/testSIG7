package com.rxlogix.dto

import grails.util.Holders
import groovy.sql.Sql

class PvaCategoryDTO {
    def static dataSource_pva

    Long id
    String value
    boolean display
    boolean isDeleted
    boolean isMasterData
    String referencedEntity
    String codeName
    String parentValue
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    static List<PvaCategoryDTO> findAll() {
        String findAllSql = """select code_value.id, 
                                   code_value.value, 
                                   code_value.created_by, 
                                   code_value.date_created, 
                                   code_value.display, 
                                   code_value.is_deleted, 
                                   code_value.is_master_data,
                                   code_value.last_updated, 
                                   code_value.modified_by, 
                                   code_value.referenced_entity, 
                                   code_list.code_name, 
                                   temp_code_value.value as parent_value from code_value
                                   left join code_list on code_value.code_list_id = code_list.id
                                   left join (select id, value from code_value) temp_code_value on temp_code_value.id = code_value.parent_id where code_value.code_list_id = ${Holders.config.mart.codeValue.tags.value} and code_value.is_deleted = 0"""

        List <PvaCategoryDTO> categories = []
        Sql sql = null
        try {
            sql = new Sql(getDataSource_pva())
            sql.eachRow(findAllSql , []) { row ->
                PvaCategoryDTO rowData = new PvaCategoryDTO()
                rowData.id = row.id as Long
                rowData.value = row.value
                rowData.createdBy = row.created_by
                rowData.dateCreated = row.getTimestamp("date_created")
                rowData.display = row.display
                rowData.isDeleted = row.is_deleted
                rowData.isMasterData = row.is_master_data
                rowData.lastUpdated = row.getTimestamp("last_updated")
                rowData.modifiedBy = row.modified_by
                rowData.referencedEntity = row.referenced_entity
                rowData.codeName = row.code_name
                rowData.parentValue = row.parent_value

                categories.add(rowData)
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        return categories
    }

    static def getDataSource_pva() {
        if (!dataSource_pva) {
            def app = Holders.getGrailsApplication()
            dataSource_pva = app.getMainContext().getBean('dataSource_pva')
        }

        dataSource_pva
    }
}