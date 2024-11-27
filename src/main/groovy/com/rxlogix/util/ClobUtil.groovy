package com.rxlogix.util

import grails.core.GrailsDomainClass
import grails.core.GrailsApplication
import org.grails.datastore.mapping.model.MappingContext
import org.grails.orm.hibernate.cfg.GrailsDomainBinder
import groovy.sql.Sql
import org.hibernate.Session
import com.rxlogix.Constants

import java.sql.Clob

class ClobUtil {

    def sessionFactory
    /**
     this function converts strings
     like 'genericComment' to 'GENERIC_COMMENT'
     */
    String camelCaseToUpperCaseWithUnderScore(String fieldName) {
        // Check if the field name needs transformation
        if (fieldName.matches("[a-z]+[A-Z]+.*")) {
            // Use regular expression to split camel case into words
            def words = fieldName.replaceAll(/(?<=[a-z])(?=[A-Z])/, '_').split('_')

            // Convert words to uppercase and join with underscores
            return words.collect { it.toUpperCase() }.join('_')
        } else {
            return fieldName.toUpperCase()
        }
    }

    String getColumnMapping(MappingContext mappingContext, Class<?> domainClass, String fieldName) {
        // Retrieve the mapped column name from the MappingContext
        def domainMapping = mappingContext?.getPersistentEntity(domainClass)?:null
        def propertyMapping = domainMapping?.getPropertyByName(fieldName)?:null
        return propertyMapping?.column?.name?:null
    }

    String getTableMapping(MappingContext mappingContext, Class<?> domainClass) {
        // Retrieve the mapped table name from the MappingContext
        def domainMapping = mappingContext?.getPersistentEntity(domainClass)?:null
        return domainMapping?.table?.name?:null
    }

    String fetchClob(Object object, String field, Sql sqlInstance, MappingContext mappingContext) {
        Session session = sessionFactory?.currentSession
        def clobData = ""
        Sql sql = sqlInstance
        Long id = object.id
        String objectClass = object.getClass().toString().split('\\.')[-1]
        objectClass = objectClass.substring(0, 1).toLowerCase() + objectClass.substring(1)
        def columnName = camelCaseToUpperCaseWithUnderScore(field)
        def tableName = camelCaseToUpperCaseWithUnderScore(objectClass)

        /**
         Retrieve the mapped column name
         from the MappingContext
         */
        def mappedColumnName = getColumnMapping(mappingContext, object.getClass(), field)
        if (mappedColumnName != null) {
            columnName = mappedColumnName
        }
        /**
         Now we have to make a check regarding
         whether the domain-Object has a different
         mapped tableName
         */
        def mappedTableName = getTableMapping(mappingContext, object.getClass())
        if (mappedTableName != null) {
            tableName = mappedTableName
        }

        try {
            sql?.eachRow("SELECT ${columnName} FROM ${tableName} WHERE id=:id", [id: id]) { row ->
                def clob = row[columnName] as Clob
                if (clob) {
                    clobData = clob.getSubString(1l, clob.length() as Integer)
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            sql?.close()
            session?.flush()
            session?.clear()
        }

        return clobData
    }

    List<String> splitClobContent(String content) {
        List<String> stringList = []
        if (content.length() <= Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX) {
            stringList.add(content)
        } else {
            stringList.add(content.substring(0, Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX))
            stringList.addAll(splitClobContent(content.substring(Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX)))
        }
        return stringList
    }
}
