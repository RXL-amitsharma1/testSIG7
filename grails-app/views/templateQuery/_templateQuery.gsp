<%@ page import="com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.config.ReportTemplate; org.springframework.context.MessageSource; com.rxlogix.util.ViewHelper;" %>

<g:set var="configurationService" bean="configurationService"/>

<div id="templateQuery${i}" class="templateQuery-div" <g:if test="${hidden}">style="display:none;"</g:if>>

    <g:hiddenField name="templateQueries[${i}].version" value="${templateQueryInstance?.version}"/>
    <g:hiddenField name='templateQueries[${i}].id' value="${templateQueryInstance?.id}"/>
    <g:hiddenField name='templateQueries[${i}].dynamicFormEntryDeleted' value='false'/>
    <g:hiddenField name='templateQueries[${i}].new' value="${clone == true ? 'true' : (templateQueryInstance?.id == null ? 'true' : 'false')}"/>
    <div class="row templateContainer" id="templateQueries">

        <div class="col-md-4 templateWrapperRow">
            <div class="row">
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-12">
                            <label><g:message code="app.label.chooseAReportTemplate"/></label>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-11">
                            <div class="form-group">
                                <g:select name="templateQueries[${i}].template"
                                          from="${[]}"
                                          class="form-control selectTemplate"/>
                                <a tabindex="0" href="#"
                                   class="fa fa-times add-cursor templateQueryDeleteButton pull-right"
                                   id="templateQueries[${i}].deleteButton" data-toggle="tooltips"
                                   title="${message(code: 'tip.removeSection')}"></a>
                                <g:hiddenField name="templateQueries[${i}].rptTempId"
                                               value="${templateQueryInstance?.template}"/>

                            </div>
                        </div>

                        <div>
                            <a href="${templateQueryInstance?.template ? createLink(controller: 'template', action: 'view', id: templateQueryInstance?.template) : '#'}"
                               title="${message(code: 'app.label.viewTemplate')}" target="_blank"
                               class="templateQueryIcon templateViewButton glyphicon glyphicon-info-sign ${templateQueryInstance?.template ? '' : 'hide'}"></a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12 m-b-15">
                    <a tabindex="0" class="add-cursor showHeaderFooterArea"><g:message
                            code="add.header.title.and.footer"/></a>
                </div>

                <div class="clearfix"></div>
            </div>

            <div class="headerFooterArea" hidden="hidden">
                <div class="row">
                    <div class="col-md-12 m-b-15">
                        <g:textField name="templateQueries[${i}].header"
                                     maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.TemplateQuery', field: 'header')}"
                                     value="${templateQueryInstance?.header}"
                                     placeholder="${message(code: "placeholder.templateQuery.header")}"
                                     class="form-control"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 m-b-15">
                        <g:textField name="templateQueries[${i}].title"
                                     maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Configuration', field: 'name')}"
                                     value="${templateQueryInstance?.title}"
                                     placeholder="${message(code: "placeholder.templateQuery.title")}"
                                     class="form-control"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 m-b-15">
                        <g:textArea name="templateQueries[${i}].footer"
                                    value="${templateQueryInstance?.footer}"
                                    placeholder="${message(code: "placeholder.templateQuery.footer")}"
                                    maxlength="80" id="footerSelect"
                                    class="form-control footerSelect"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 m-b-15">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].headerProductSelection"
                                        name="templateQueries[${i}].headerProductSelection"
                                        value="${templateQueryInstance?.headerProductSelection}"
                                        checked="${templateQueryInstance?.headerProductSelection}"/>
                            <label for="templateQueries[${i}].headerProductSelection">
                                <g:message code="templateQuery.headerProductSelection.label"/>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 m-b-15">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].headerDateRange"
                                        name="templateQueries[${i}].headerDateRange"
                                        value="${templateQueryInstance?.headerDateRange}"
                                        checked="${templateQueryInstance?.headerDateRange}"/>
                            <label for="templateQueries[${i}].headerDateRange">
                                <g:message code="templateQuery.headerDateRange.label"/>
                            </label>
                        </div>
                    </div>
                </div>
                <g:hiddenField name="templateQueries[${i}].templateName"
                               value="${templateQueryInstance?.templateName}"/>
            </div>

            <div class="ciomsProtectedArea" ${(templateQueryInstance?.template == cioms1Id) ? "" : "hidden"}>
                <div class="row">
                    <div class="col-md-12 m-b-15">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].privacyProtected"
                                        name="templateQueries[${i}].privacyProtected"
                                        value="${templateQueryInstance?.privacyProtected}"
                                        checked="${templateQueryInstance?.privacyProtected}"/>
                            <label for=templateQueries[${i}].privacyProtected>
                                <g:message code="templateQuery.privacyProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 m-b-15">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].blindProtected"
                                        name="templateQueries[${i}].blindProtected"
                                        value="${templateQueryInstance?.blindProtected}"
                                        checked="${templateQueryInstance?.blindProtected}"/>
                            <label for=templateQueries[${i}].blindProtected>
                                <g:message code="templateQuery.blindProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-8 queryWrapperRow">
            <div class="row">
                %{--Query--}%
                <div class="col-md-6">
                    <div class="row">
                        <div class="col-md-12 ${hasErrors(bean: error, field: 'query', 'has-error')}">
                            <label class="m-l-5"><g:message code="app.label.chooseAQuery"/></label>
                        </div>
                    </div>

                    <div class="row queryContainer">
                        <g:if test="${editMode}">
                            <div>
                                <i class="fa fa-refresh fa-spin loading"></i>
                            </div>
                        </g:if>

                        <div class="col-lg-12">
                            <div class="doneLoading" style="padding-bottom: 5px;">
                                <div>
                                    <div class="col-md-11"><g:select name="templateQueries[${i}].query"
                                                                     from="${[]}"
                                                                     class="form-control selectQuery"/></div>

                                    <div><a href="${templateQueryInstance?.query ? createLink(controller: 'query', action: 'view', id: templateQueryInstance?.query) : '#'}"
                                            title="${message(code: 'app.label.viewQuery')}" target="_blank"
                                            class="templateQueryIcon queryViewButton glyphicon glyphicon-info-sign ${templateQueryInstance?.query ? '' : 'hide'}"></a>
                                    </div>
                                    <g:hiddenField name="templateQueries[${i}].queryId"
                                                   value="${templateQueryInstance?.query}"/>
                                    <g:hiddenField name="templateQueries[${i}].queryName"
                                                   value="${templateQueryInstance?.queryName}"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-3">
                    <div class="row">
                        <div class="col-md-12">
                            <label><g:message code="app.label.queryLevel"/></label>
                            <span class="glyphicon glyphicon-question-sign modal-link theme-color" style="cursor:pointer"
                                  data-toggle="modal"
                                  data-target="#queryLevelHelpModal" tabindex="0"></span>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div>
                                <g:select name="templateQueries[${i}].queryLevel"
                                          from="${ViewHelper.getQueryLevels()}"
                                          value="${templateQueryInstance?.queryLevel}"
                                          valueMessagePrefix="app.queryLevel"
                                          noSelection="['': '--Select One--']"
                                          class="form-control selectQueryLevel"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-3 sectionDateRange">
                    <g:render template="/configuration/dateRange" model="[templateQueryInstance: templateQueryInstance]"/>
                </div>

            </div>

            <div class="row">
                %{--Blank values--}%
                <div class="col-md-12">
                    <div class="templateSQLValues">
                        <g:if test="${templateQueryInstance?.templateValueLists?.size() > 0}">
                            <g:each var="tvl" in="${templateQueryInstance.templateValueLists}">
                                <g:each var="tv" in="${tvl.parameterValues}" status="j">
                                    <g:render template='/query/customSQLValue' model="['qev': tv, 'i': i, 'j': j]"/>
                                </g:each>
                            </g:each>
                        </g:if>
                    </div>
                </div>

                <div class="col-md-9">
                    <div class="queryExpressionValues">
                        <g:if test="${templateQueryInstance?.queryValueLists?.size() > 0}">
                            <g:each var="qvl" in="${templateQueryInstance.queryValueLists}">
                                <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                    <g:if test="${qev?.hasProperty('reportField')}">
                                        <g:render template='/query/toAddContainerQEV'
                                                  model="['type':'qev','qev': qev, 'i': i, 'j': j]"/>
                                    </g:if>
                                    <g:else>
                                        <g:render template='/query/customSQLValue'
                                                  model="['type':'qev','qev': qev, 'i': i, 'j': j]"/>
                                    </g:else>
                                </g:each>
                            </g:each>
                        </g:if>
                    </div>
                    <g:hiddenField class="validQueries" name="templateQueries[${i}].validQueries"
                                   value="${templateQueryInstance?.getQueriesIdsAsString()}"/>
                </div>
            </div>
        </div>
    </div>
</div>
<g:render template="/query/queryLevelHelp"/>