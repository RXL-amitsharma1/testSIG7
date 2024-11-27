package com.rxlogix.helper

import com.rxlogix.config.*
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter

trait ConfigurationHelper {
    def params
    def userService
    def messageSource

    // This is needed to properly re-read the data being created/edited after a transaction rollback
    private populateModel(Configuration configurationInstance) {
        //Do not bind in any other way because of the clone contained in the params
        bindData(configurationInstance, params, [exclude: ["templateQueries", "tags", "isEnabled","asOfVersionDate"]])
        bindAsOfVersionDate(configurationInstance)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        bindNewTemplateQueries(configurationInstance)
        bindExistingTemplateQueryEdits(configurationInstance)

        def _toBeRemoved = configurationInstance?.templateQueries?.findAll {
            (it?.dynamicFormEntryDeleted || (it == null))
        }

        // if there are Template Queries to be removed
        if (_toBeRemoved) {
            configurationInstance?.templateQueries?.removeAll(_toBeRemoved)
        }

        //update the indexes
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            if (templateQuery) {
                templateQuery.index = i
            }
        }
    }

    private bindAsOfVersionDate(Configuration configuration) {
        if(configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
            configuration.asOfVersionDate = DateUtil.getAsOfVersion(params.asOfVersionDateValue,configuration.configSelectedTimeZone)
        } else {
            configuration.asOfVersionDate = null
        }
    }

    private setNextRunDateAndScheduleDateJSON(Configuration configurationInstance) {
        if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
            configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
        } else {
            configurationInstance.nextRunDate = null
        }
    }


    private bindNewTemplateQueries(Configuration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = 0; params.containsKey("templateQueries[" + i + "].id"); i++) {
            if (params.get("templateQueries[" + i + "].new").equals("true")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                TemplateQuery templateQueryInstance = new TemplateQuery(bindingMap)

                templateQueryInstance = (TemplateQuery) userService.setOwnershipAndModifier(templateQueryInstance)
                //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
                DateRangeInformation dateRangeInformationForTemplateQuery = templateQueryInstance.dateRangeInformationForTemplateQuery
                def dateRangeEnum = params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEnum")
                if (dateRangeEnum) {
                    dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
                    dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"), configurationInstance?.configSelectedTimeZone)
                    dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"), configurationInstance?.configSelectedTimeZone)
                    dateRangeInformationForTemplateQuery?.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeStartAbsolute)
                    dateRangeInformationForTemplateQuery?.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeEndAbsolute)
                }
                dateRangeInformationForTemplateQuery.templateQuery = templateQueryInstance

                dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer

                assignParameterValuesToTemplateQuery(templateQueryInstance, i)

                configurationInstance.addToTemplateQueries(templateQueryInstance)
            }
        }
    }

    private void assignParameterValuesToTemplateQuery(TemplateQuery templateQuery, int i) {
        templateQuery.queryValueLists?.each {
            List<ParameterValue> parameterValues = it.parameterValues
            it.parameterValues?.clear()
            parameterValues?.each {
                if (it.hasProperty('reportField')) {
                    QueryExpressionValue.get(it.id)?.delete()
                } else {
                    CustomSQLValue.get(it.id)?.delete()
                }
            }
        }
        templateQuery.queryValueLists = []
        templateQuery.templateValueLists?.each {
            List<ParameterValue> parameterValuesList = it.parameterValues
            it.parameterValues?.clear()
            parameterValuesList?.each {
                CustomSQLValue.get(it.id)?.delete()
            }
        }
        templateQuery.templateValueLists?.clear()

        if (params.containsKey("templateQuery" + i + ".qev[0].key")) {
            QueryValueList queryValueList = new QueryValueList(query: params.("templateQueries[" + i + "].query"))

            for (int j = 0; params.containsKey("templateQuery" + i + ".qev[" + j + "].key"); j++) {
                ParameterValue tempValue
                if (params.containsKey("templateQuery" + i + ".qev[" + j + "].field")) {
                    messageSource= MiscUtil.getBean("messageSource")
                    def operatorString=QueryOperatorEnum.valueOf(params.("templateQuery" + i + ".qev[" + j + "].operator"))
                    tempValue = new QueryExpressionValue(key: params.("templateQuery" + i + ".qev[" + j + "].key"),
                            reportField: ReportField.findByName(params.("templateQuery" + i + ".qev[" + j + "].field")),
                            operator: operatorString,
                            value: params.("templateQuery" + i + ".qev[" + j + "].value"),
                            operatorValue: messageSource.getMessage("app.queryOperator.$operatorString", null, Locale.ENGLISH))
                } else {
                    tempValue = new CustomSQLValue(key: params.("templateQuery" + i + ".qev[" + j + "].key"),
                            value: params.("templateQuery" + i + ".qev[" + j + "].value"))
                }
                queryValueList.addToParameterValues(tempValue)
            }
            templateQuery.addToQueryValueLists(queryValueList)
        }

        if (params.containsKey("templateQuery" + i + ".tv[0].key")) {
            TemplateValueList templateValueList = new TemplateValueList(template: params.("templateQueries[" + i + "].template"))

            for (int j = 0; params.containsKey("templateQuery" + i + ".tv[" + j + "].key"); j++) {
                ParameterValue tempValue
                tempValue = new CustomSQLValue(key: params.("templateQuery" + i + ".tv[" + j + "].key"),
                        value: params.("templateQuery" + i + ".tv[" + j + "].value"))
                templateValueList.addToParameterValues(tempValue)
            }
            templateQuery.addToTemplateValueLists(templateValueList)
        }
    }

    private bindExistingTemplateQueryEdits(Configuration configurationInstance) {
        //handle edits to the existing Template Queries
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            templateQuery.properties = bindingMap
            templateQuery = (TemplateQuery) userService.setOwnershipAndModifier(templateQuery)
            //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
            DateRangeInformation dateRangeInformationForTemplateQuery = templateQuery.dateRangeInformationForTemplateQuery
            def dateRangeEnum = params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEnum")
            if (dateRangeEnum) {
                dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
//                    if(dateRangeEnum.name() == DateRangeEnum.CUSTOM.name()) {
                dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"), configurationInstance?.configSelectedTimeZone)
                dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"), configurationInstance?.configSelectedTimeZone)
                dateRangeInformationForTemplateQuery?.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeEndAbsolute)
                dateRangeInformationForTemplateQuery?.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeStartAbsolute)
//                    }
            }
            dateRangeInformationForTemplateQuery.templateQuery = templateQuery

            if(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") && params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")=~ "-?\\d+") {
                dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer
            }


            assignParameterValuesToTemplateQuery(templateQuery, i)
        }
        configurationInstance
    }
}
