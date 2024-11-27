<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.TemplateTypeEnum" %>
<g:set var="templateService" bean="templateService"/>

<div class="row">
        <g:if test="${editable}">
            <div class="col-xs-3">
                <select name="selectField" id="selectField_lineListing" class="form-control selectField">
                    <option default><g:message code="dataTabulation.select.field" /></option>
                    <g:each in="${templateService.getReportFieldsForCLL()}" var="group">
                        <optgroup label="${message(code: "app.reportFieldGroup.${group.text}")}">
                            <g:each in="${group.children}" var="field">
                                <option argusName="${field.argusColumn.tableName.tableAlias}.${field.argusColumn.columnName}"
                                        reportFieldName="${field.name}"
                                        value="${field.id}">${message(code: "app.reportField.${field.name}")}</option>
                            </g:each>
                        </optgroup>
                    </g:each>
                </select>
            </div>
        </g:if>

        <div class="col-xs-9" style="padding-left: 25px; padding-right: 25px;">
            <div class="row tabTitleBackground">
                <div class="col-xs-12">
                    <label><g:message code="line.listing.preview" /></label>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <label><g:message code="app.label.grouping"/></label>
                    <label style="float: right" class="no-bold add-cursor">
                        <g:checkBox name="pageBreakByGroup" value="${reportTemplateInstance.pageBreakByGroup}" /> <g:message code="page.break.by.group" />
                    </label>
                </div>
            </div>

            <div class="row groupingContainer shortBorder rowsAndColumnsContainer">
                <div class="col-xs-12 no-padding columnScroll">
                    <div id="groupingContainer" class="containerToBeSelected groupingContainer"></div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <label><g:message code="app.label.columns"/></label>
                </div>
            </div>

            %{--
                todo:  selectedColumnsContainer:  this needs to be populated from the model
                The widget needs to paint itself, not be dependent on a second server call to initiate JS to fill the widget
            --}%
            <div class="row columnsContainer rowsAndColumnsContainer rowsAndColumnsContainerBorder selectedContainerBorder">
                <div class="col-xs-12 no-padding columnScroll">
                    <div hidden="hidden"><g:render template="toAddColumn" /></div>
                    <div id="columnsContainer" class="containerToBeSelected columnsContainer"></div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <label><g:message code="app.label.rowColumns" /></label>
                </div>
            </div>

            <div class="row rowColumnsContainer shortBorder rowsAndColumnsContainer">
                <div class="col-xs-12 no-padding columnScroll">
                    <div id="rowColumnsContainer" class="containerToBeSelected rowColumnsContainer"></div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <label style="float: right" class="no-bold add-cursor">
                        <g:checkBox name="columnShowTotal" value="${reportTemplateInstance?.columnShowTotal}" /> <g:message code="show.total"/>
                    </label>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 fieldOptions columnRenameArea" hidden="hidden">
                    <i class="fa fa-times add-cursor closeRenameArea" style="float: right;"></i>
                    <div class="row">
                        <div class="col-xs-3">
                            <label><g:message code="cognosReport.name.label" />:</label>
                            <div class="form-inline">
                                <input class="selectedColumnName form-control">
                                <input type="button" class="btn btn-default btn-sm resetThisCol pv-btn-grey" value="${message(code: 'default.button.reset.label')}">
                            </div>
                        </div>
                        <div class="col-xs-2 datasheetOption" hidden="hidden">
                            <label><g:message code="app.label.datasheet"/>: </label>
                            <g:select class="form-control" name="selectDatasheet" from="${ViewHelper.getDatasheet()}"
                                      optionKey="name" optionValue="display"/>
                        </div>
                        <div class="col-xs-3">
                            <a class="add-cursor showCustomExpression"><g:message code="app.template.customExpression"/></a>
                        </div>
                        <div class="col-xs-4">
                            <label class="add-cursor no-bold">
                                <input type="checkbox" class="commaSeparated"> <g:message code="app.template.CommaSeparatedValue"/>
                            </label>

                            <label class="add-cursor no-bold">
                                <input type="checkbox" class="suppressRepeating"> <g:message code="app.template.suppressRepeatingValues" />
                            </label>

                            <label class="add-cursor no-bold">
                                <input type="checkbox" class="blindedValues"> <g:message code="blinded.values" />
                            </label>
                        </div>
                    </div>
                    <div class="row customExpressionArea" hidden="hidden">
                        <div class="col-xs-12">
                            <textarea class="form-control customExpressionValue"></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row reassessListedness" hidden="hidden" style="padding-top: 10px;">
                <div class="col-xs-4">
                    <label><g:message code="app.label.reassessListedness"/>: </label>
                    <g:select class="form-control" name="reassessListedness"
                              from="${ViewHelper.getReassessListedness()}" optionKey="name" optionValue="display"
                              value="${reportTemplateInstance?.reassessListedness}"/>
                </div>
            </div>
        </div>
    </div>


<g:hiddenField name="columns" value="${reportTemplateInstance?.templateType == TemplateTypeEnum.CASE_LINE ? templateService.getJSONStringRF(reportTemplateInstance?.columnList) : null}"/>
<g:hiddenField name="grouping" value="${reportTemplateInstance?.templateType == TemplateTypeEnum.CASE_LINE ? templateService.getJSONStringRF(reportTemplateInstance?.groupingList) : null}"/>
<g:hiddenField name="rowCols" value="${reportTemplateInstance?.templateType == TemplateTypeEnum.CASE_LINE ? templateService.getJSONStringRF(reportTemplateInstance?.rowColumnList) : null}"/>