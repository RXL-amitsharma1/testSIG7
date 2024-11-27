package com.rxlogix.api

import com.rxlogix.ExceptionHandlingController
import com.rxlogix.config.Keyword
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.security.Authorize
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON

@Authorize
class QueryRestController implements AlertUtil , ExceptionHandlingController{
    /**
     * Helper method to convert an array of `QueryOperatorEnum` operators into a localized format.
     *
     * This method iterates over each operator in the provided enum array, constructing a
     * map containing the operator's name (used as the value) and its localized display
     * value (retrieved using `getI18nKey()`).
     *
     * @param operators An array of `QueryOperatorEnum` values
     * @return A list of maps, each containing a `value` and `display` key for each operator
     */
    private def operatorsI18n(QueryOperatorEnum[] operators) {
        List valueLessOperators = ["IS_EMPTY", "IS_NOT_EMPTY", "YESTERDAY", "LAST_WEEK", "LAST_MONTH", "LAST_YEAR"]
        operators.collect { QueryOperatorEnum operator ->
            return [value: operator.name(), display: message(code: operator.getI18nKey()), valueless: valueLessOperators.contains(operator.name())]
        }
    }



    String getAllQueryOperators() {
        Map data = [:]
        data.put('date', operatorsI18n(QueryOperatorEnum.dateOperators))
        data.put('number', operatorsI18n(QueryOperatorEnum.numericOperators))
        data.put('string', operatorsI18n(QueryOperatorEnum.stringOperators))
        data.put('boolean', operatorsI18n(QueryOperatorEnum.booleanOperators))
        render(data as JSON)
    }

    String getAllKeyWords() {
        def data = [[value  : "and",display: "and"],[value  : "or",display: "or"]]
        render(data as JSON)
    }
}