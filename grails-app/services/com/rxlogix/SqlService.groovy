package com.rxlogix


import groovy.sql.Sql

import java.sql.SQLException

class SqlService {

    /**
     * Test the validity of a SQL statement
     */

    static transactional = false
    def testSQLExecution(instance, String fieldName, String toValidate, Sql sql, String errorMessageToDisplay) {
        try {
            sql.firstRow(toValidate)
        } catch (SQLException e) {
            log.info("Failed to validate SQL statement: " + toValidate + " --> "+ e.localizedMessage)
            instance.getErrors().rejectValue(fieldName, errorMessageToDisplay)
        }
        return instance
    }
}
