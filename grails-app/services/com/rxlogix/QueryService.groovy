package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.plugin.cache.Cacheable
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import org.springframework.context.NoSuchMessageException

import java.text.SimpleDateFormat

@Transactional
class QueryService {
    def userService
    def sqlGenerationService
    def messageSource
    def reportIntegrationService

    // This is used to replace query ids with executedQuery ids
    String assignQueriesFromMap(String JSONQuery, Map oldAndNewQueries) {
        Map dataMap = (new JsonSlurper()).parseText(JSONQuery)
        List containerGroupsList = dataMap.all.containerGroups
        for (int i = 0; i < containerGroupsList.size(); i++) {
            assignInsideGroup(containerGroupsList[i], oldAndNewQueries)
        }
        return new JSON(dataMap).toString()
    }

    //helper method, don't call this
    private void assignInsideGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Map oldAndNewQueries) {
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                assignInsideGroup(expressionsList[i], oldAndNewQueries)
            }
        } else {
            if (groupMap.containsKey('query')) {
                if (oldAndNewQueries.containsKey(groupMap.query)) {
                    groupMap.query = oldAndNewQueries[groupMap.query]
                }
            }
        }
    }

    List<Map> getQueryListForBusinessConfiguration() {
        List<Map> queryList = []
        //TODO: Fetch only those non parameterised queries where EUDRA flag is false -PS
        Map resp = reportIntegrationService.getQueryList("", 0, 9999, true)
        queryList = resp?.queryList
        return queryList?.unique()
    }

    SuperQueryDTO queryDetail(Long id) {
        SuperQueryDTO superQueryDTO
        try {
            String url = Holders.config.pvreports.url
            String queryDetailURI = Holders.config.pvreports.query.queryDetail.uri
            Map requestMap = [queryId: id]
            Map res = reportIntegrationService.get(url, queryDetailURI, requestMap)
            if (res.data?.result && res.data?.result !=null) {
                superQueryDTO = new SuperQueryDTO(res.data.result)
            } else if (id != null && (!res.data?.result ||res.data?.result == null )){
                throw new Exception("PVR returned 'NULL' when fetching query.")
            }
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
            throw th
        }
        return superQueryDTO
    }

    SuperQueryDTO queryDetailByName(String name) {
        SuperQueryDTO superQueryDTO
        try {
            String url = Holders.config.pvreports.url
            String queryDetailURI = Holders.config.pvreports.query.queryDetailByName.uri
            Map requestMap = [queryName: name]
            Map res = reportIntegrationService.get(url, queryDetailURI, requestMap)
            if (res.data.result) {
                superQueryDTO = new SuperQueryDTO(res.data.result)
            }
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return superQueryDTO
    }

    List<SuperQueryDTO> queryListDetail(List<Long> queryIds) {
        List<SuperQueryDTO> superQueryDTOS = []
        try {
            String url = Holders.config.pvreports.url
            String queryListDetailURI = Holders.config.pvreports.query.queryListDetail.uri
            Map requestMap = [queryIds: queryIds]
            Map res = reportIntegrationService.get(url, queryListDetailURI, requestMap)
            if (res.data.result) {
                superQueryDTOS = res.data.result.collect { Map query -> new SuperQueryDTO(query) }
            }
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return superQueryDTOS
    }

    SuperQueryDTO nonValidQuery() {
        SuperQueryDTO superQueryDTO
        try {
            String url = Holders.config.pvreports.url
            String nonValidQueryURI = Holders.config.pvreports.query.nonValidQuery.uri
            Map res = reportIntegrationService.get(url, nonValidQueryURI, [:])
            if (res.data.result) {
                superQueryDTO = new SuperQueryDTO(res.data.result)
            }
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return superQueryDTO
    }

    @Cacheable('reportFieldGroups')
    List getReportFields() {
        def fieldGroups = ReportFieldGroup.findAllByIsEudraField(false)
        List fieldSelection = []
        fieldGroups.each {
            fieldSelection.add(text: it.name, children: ReportField.findAllByFieldGroupAndQuerySelectableAndIsEudraField(it, true, false))
        }
        return fieldSelection
    }

    @Cacheable('reportFieldGroups')
    List getEudraReportFields() {
        def fieldGroups = ReportFieldGroup.findAllByIsEudraField(true)
        List fieldSelection = []
        fieldGroups.each {
            fieldSelection.add(text: it.name, children: ReportField.findAllByFieldGroupAndQuerySelectableAndIsEudraField(it, true, true))
        }
        return fieldSelection
    }

    // helper method, don't call this
    private def buildCriteriaFromGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Date nextRunDate, String timezone, Set<QueryExpressionValue> blanks, Locale locale) {
        String result = ""
        boolean hasKeyword = groupMap.keyword;
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                if (hasKeyword && i > 0) {
                    String keyword
                    try {
                        keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, locale)
                    } catch (NoSuchMessageException exception) {
                        log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                        keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                    }
                    result += " $keyword "
                }
                def executed = buildCriteriaFromGroup(expressionsList[i], nextRunDate, timezone, blanks, locale)
                result += "(${executed})";
            }
        } else {
            if (groupMap.keyword) {
                String keyword
                try {
                    keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, locale)
                } catch (NoSuchMessageException exception) {
                    log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                    keyword = messageSource.getMessage("app.keyword.${groupMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                }
                result += " $keyword "
            }

            ReportField reportField = ReportField.findByName(groupMap.field)
            // Extra Values
            HashMap extraValues = [:]

            result += convertExpressionToWhereClause(
                    new Expression(reportField: reportField, value: groupMap.value,
                            operator: groupMap.op as QueryOperatorEnum),
                    nextRunDate, timezone, blanks, extraValues, locale);
            if (!reportFields.contains(reportField)) {
                reportFields.add(reportField)
            }
        }
        return result
    }

    private String convertExpressionToWhereClause(Expression e, Date nextRunDate, String timezone, Set<QueryExpressionValue> blanks, HashMap extraValues, Locale locale) {
        String result = ""
        String columnName = ""
        String op = ""

        if (e.value == null || e.value.equals("")) {
            ParameterValue qev = blanks.find { qev ->
                e.reportField == qev.reportField && e.operator == qev.operator
            }

            if (qev) {
                e.value = qev.value
                blanks.remove(qev)
            } else {
                log.info("Could not find blank value to give value!")
            }
        }

        try {
            columnName = messageSource.getMessage("app.reportField.$e.reportField.name", null, locale)
        } catch (NoSuchMessageException exception) {
            log.warn("No app.reportField found for locale: $locale. Defaulting to English locale.")
            columnName = messageSource.getMessage("app.reportField.$e.reportField.name", null, Locale.ENGLISH)
        }

        try {
            op = messageSource.getMessage(e.operator.getI18nKey(), null, locale)
        } catch (NoSuchMessageException exception) {
            log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
            op = messageSource.getMessage(e.operator.getI18nKey(), null, Locale.ENGLISH)
        }

        if (e.reportField.dataType == PartialDate.class) {
            result = generatePartialDateWhereClause(e, nextRunDate, timezone, columnName)
            return result
        }

        if (e.operator == QueryOperatorEnum.IS_EMPTY) {
            result = "${columnName} Is null"
        } else if (e.operator == QueryOperatorEnum.IS_NOT_EMPTY) {
            result = "${columnName} Is not null"
        } else if (e.reportField.isString()) {
            //First, check our custom operator. String comparisons are case insensitive.
            if (e.operator == QueryOperatorEnum.EQUALS) {
                //Second, check if we have multiselect
                if (e?.value?.indexOf(";") == -1) {
                    result = "${columnName} = ${e.value}"
                } else {
                    //Multiselect Select2
                    List tokens = e?.value?.split(/;/) as List
                    String values = ""

                    tokens.eachWithIndex { it, index ->
                        if (index > 0) {
                            values += " OR ${columnName} = ${it}"
                        } else {
                            values += "${columnName} = ${it}"
                        }
                    }

                    result += values
                }
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                //Second, check if we have multiselect
                if (e.value.indexOf(";") == -1) {
                    result = "${columnName} <> ${e.value}"
                } else {
                    //Multiselect Select2
                    String[] tokens = e?.value?.split(/;/)
                    String values = ""

                    tokens.eachWithIndex { it, index ->
                        if (index > 0) {
                            values += " AND ${columnName} <> ${it}"
                        } else {
                            values += "${columnName} <> ${it}"
                        }
                    }

                    result += values
                }
            } else if (e.operator == QueryOperatorEnum.CONTAINS) {
                result = "${columnName} $op ${e.value}"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_CONTAIN) {
                result = "${columnName} $op ${e.value}"
            } else if (e.operator == QueryOperatorEnum.START_WITH) {
                result = "${columnName} $op ${e.value}"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_START) {
                result = "${columnName} $op ${e.value}"
            } else if (e.operator == QueryOperatorEnum.ENDS_WITH) {
                result = "${columnName} $op ${e.value}"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_END) {
                result = "${columnName} $op ${e.value}"
            }
        } else if (e.reportField.isNumber()) {
            result = "${columnName} ${e.operator.value()} ${e.value}"
        } else if (e.reportField.isDate()) {
            result = generateDateWhereClause(e, nextRunDate, timezone, columnName)
        }
        return result
    }

    String generatePartialDateWhereClause(Expression e, Date nextRunDate, String timezone, String columnName) {
        String result = ""
        if (e.value.matches(sqlGenerationService.PARTIAL_DATE_YEAR_ONLY)) { //??-???-yyyy
            String monthAndYear = e.value.substring(6)
            String startDate = "01-JAN${monthAndYear}"
            def startDates
            def endDates

            SimpleDateFormat dateFormat = new SimpleDateFormat(sqlGenerationService.PARTIAL_DATE_FMT)
            Date convertedStartDate = dateFormat.parse(startDate)
            Calendar c = Calendar.getInstance();
            c.setTime(convertedStartDate)
            c.set(Calendar.MONTH, Calendar.DECEMBER)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date convertedEndDate = c.time

            if (e.operator == QueryOperatorEnum.EQUALS) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                              ${columnName} <= ${convertDateToSQLDateTime(endDates[1])}"""
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """NOT (${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                                  ${columnName} <= ${convertDateToSQLDateTime(endDates[1])})"""
            } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} < ${convertDateToSQLDateTime(startDates[0])}"
            } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} <= ${convertDateToSQLDateTime(endDates[1])}"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} > ${convertDateToSQLDateTime(endDates[1])}"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} >= ${convertDateToSQLDateTime(startDates[0])}"
            } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                    e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                             ${columnName} <= ${convertDateToSQLDateTime(startDates[1])}"""
            } else if (e.operator == QueryOperatorEnum.LAST_X_DAYS || e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                    e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                             ${columnName} <= ${convertDateToSQLDateTime(startDates[1])}"""
            }
        } else if (e.value.matches(sqlGenerationService.PARTIAL_DATE_MONTH_AND_YEAR)) { //??-MMM-yyyy
            String monthAndYear = e.value.substring(2)
            String startDate = "01${monthAndYear}"
            def startDates
            def endDates

            SimpleDateFormat dateFormat = new SimpleDateFormat(sqlGenerationService.PARTIAL_DATE_FMT)
            Date convertedStartDate = dateFormat.parse(startDate)
            Calendar c = Calendar.getInstance();
            c.setTime(convertedStartDate)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date convertedEndDate = c.time

            if (e.operator == QueryOperatorEnum.EQUALS) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                             ${columnName} <= ${convertDateToSQLDateTime(endDates[1])}"""
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """NOT (${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                                 ${columnName} <= ${convertDateToSQLDateTime(endDates[1])})"""
            } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} < ${convertDateToSQLDateTime(startDates[0])}"
            } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} <= ${convertDateToSQLDateTime(endDates[1])}"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} > ${convertDateToSQLDateTime(endDates[1])}"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} >= ${convertDateToSQLDateTime(startDates[0])}"
            } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                    e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                            ${columnName} <= ${convertDateToSQLDateTime(startDates[1])}"""
            } else if (e.operator == QueryOperatorEnum.LAST_X_DAYS || e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                    e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= ${convertDateToSQLDateTime(startDates[0])} AND
                            ${columnName} ${convertDateToSQLDateTime(startDates[1])}"""
            }
        } else if (e.value.matches(sqlGenerationService.PARTIAL_DATE_FULL)) { //dd-MMM-yyyy
            result = generateDateWhereClause(e, nextRunDate, timezone, columnName)
        }
        return result
    }

    String generateDateWhereClause(Expression e, Date nextRunDate, String timezone, String columnName) {
        String result = ""
        def dates
        if (e.operator == QueryOperatorEnum.EQUALS) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone)
            result = """${columnName} >= ${convertDateToSQLDateTime(dates[0])} AND
                        ${columnName} <= ${convertDateToSQLDateTime(dates[1])}"""
        } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone)
            result = """NOT (${columnName} >= ${convertDateToSQLDateTime(dates[0])} AND
                             ${columnName} <= ${convertDateToSQLDateTime(dates[1])})"""
        } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone)
            result = "${columnName} < ${convertDateToSQLDateTime(dates[0])}"
        } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone)
            result = "${columnName} <= ${convertDateToSQLDateTime(dates[1])}"
        } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone)
            result = "${columnName} > ${convertDateToSQLDateTime(dates[0])}"
        } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone)
            result = "${columnName} >= ${convertDateToSQLDateTime(dates[0])}"
        } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
            dates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
            result = """${columnName} >= ${convertDateToSQLDateTime(dates[0])} AND
                    ${columnName} <= ${convertDateToSQLDateTime(dates[1])}"""
        } else if (e.operator == QueryOperatorEnum.LAST_X_DAYS || e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
            dates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
            result = """${columnName} >= ${convertDateToSQLDateTime(dates[0])} AND
                    ${columnName} <= ${convertDateToSQLDateTime(dates[1])}"""
        }
        return result
    }

    String convertDateToSQLDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(sqlGenerationService.DATE_FMT)
        return sdf.format(date)
    }

    Set<List<Map>> queryExpressionValuesForQuerySet(Long queryId) {
        Set<List<Map>> result = []
        try {
            String url = Holders.config.pvreports.url
            String queryExpressionValuesForQuerySetURI = Holders.config.pvreports.query.queryExpressionValuesForQuerySet.uri
            Map res = reportIntegrationService.get(url, queryExpressionValuesForQuerySetURI, [queryId: queryId])
            log.info("Response for queryExpressionValuesForQuerySet : " + res.data)
            result = res.data
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    Set<Map> queryExpressionValuesForQuery(Long queryId) {
        Set<Map> result = []
        try {
            String url = Holders.config.pvreports.url
            String queryExpressionValuesForQueryURI = Holders.config.pvreports.query.queryExpressionValuesForQuery.uri
            Map res = reportIntegrationService.get(url, queryExpressionValuesForQueryURI, [queryId: queryId])
            log.info("Response for queryExpressionValuesForQuery : " + res.data)
            result = res.data
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    @Transactional(readOnly = true)
    List<Map> customSQLValuesForQuery(Long queryId) {
        List<Map> result = []
        try {
            String url = Holders.config.pvreports.url
            String customSQLValuesForQueryURI = Holders.config.pvreports.query.customSQLValuesForQuery.uri
            Map res = reportIntegrationService.get(url, customSQLValuesForQueryURI, [queryId: queryId])
            log.info("Response for customSQLValuesForQuery : " + res.data)
            result = res.data
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    Integer getParameterSize(Long queryId) {
        Integer size = 0
        if (queryId) {
            try {
                String url = Holders.config.pvreports.url
                String queryParameterSizeURI = Holders.config.pvreports.query.queryParameterSize.uri
                Map res = reportIntegrationService.get(url, queryParameterSizeURI, [queryId: queryId])
                log.info("Response for getParameterSize : " + res.data.size)
                size = res.data.size as Integer
            } catch (ConnectException cex) {
                log.error(cex.getMessage())
            } catch (Throwable th) {
                log.error(th.getMessage())
            }
        }
        return size
    }

    String getQueriesIdsAsString(Long queryId) {
        String result = null
        if (queryId) {
            try {
                String url = Holders.config.pvreports.url
                String queryIdsURI = Holders.config.pvreports.query.getQueriesIdsAsString.uri
                Map res = reportIntegrationService.get(url, queryIdsURI, [queryId: queryId])
                log.info("Response for getQueriesIdsAsString : " + res.data)
                result = res.data.queryIds
            } catch (ConnectException cex) {
                log.error(cex.getMessage())
            } catch (Throwable th) {
                log.error(th.getMessage())
            }
        }
        return result
    }

    /*def getPvaQueryList() {
        def pvaQueryList = getQueryList()?.collect {
            [id: it.id, text: it.nameWithDescription]
        }
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
        pvaQueryList.add(0, [id: '', text: messageSource.getMessage("select.one", null, locale)])
        pvaQueryList
    }*/

}
