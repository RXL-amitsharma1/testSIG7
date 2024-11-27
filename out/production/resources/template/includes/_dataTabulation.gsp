<%@ page import="com.rxlogix.util.ViewHelper" %>
<g:set var="templateService" bean="templateService"/>

    <div class="row">
        <g:if test="${editable}">
            <div class="col-xs-3">
                <div id="dtColumnSelect2" hidden="hidden">
                    <select name="selectField" id="selectField_dataTabulation_column" class="form-control selectField">
                        <option default><g:message code="dataTabulation.select.field"/></option>
                        <g:each in="${templateService.getReportFieldsForDTColumn()}" var="group">
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

                <div id="dtRowSelect2">
                    <select name="selectField" id="selectField_dataTabulation_row" class="form-control selectField">
                        <option default><g:message code="dataTabulation.select.field"/></option>
                        <g:each in="${templateService.getReportFieldsForDTRow()}" var="group">
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
            </div>
        </g:if>
        <div class="col-xs-9 previewLabel" style="padding-left: 25px;">
            <div class="row tabTitleBackground">
                <div class="col-xs-12">
                    <label><g:message code="data.tabulation.preview"/></label>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-5" style="padding-left: 30px; padding-right: 20px;">
            <div class="row" style="padding-top: 158px;">
                <div class="col-xs-12">
                    <label style="float:right;"><g:message code="app.label.rows" /></label>
                </div>
            </div>
            <div class="row rowsAndColumnsContainer rowsAndColumnsContainerBorder rowsContainer selectedContainerBorder">
                <div class="col-xs-12 no-padding columnScroll">
                    <div hidden="hidden"><g:render template="toAddRow" /></div>
                    <div id="rowsContainer" class="containerToBeSelected"></div>
                </div>
            </div>
        </div>

        <div class="col-xs-7 columnScroll" style="padding-left: 20px; padding-right: 30px;">
            <div id="columnMeasureContainer" style="">
                <g:each in="${reportTemplateInstance?.columnMeasureList}" var="columnMeasure" status="index">
                    <g:render template="columnMeasure" model="['columnMeasure': columnMeasure, 'index': index]"/>
                </g:each>
                <input class="btn btn-default btn-sm pv-btn-grey" type="button" id="addColumnMeasure" value="${message(code: 'default.button.add.label')}">
            </div>

            <div hidden="hidden"><g:render template="columnMeasure" model="['index': '']"/></div>
            <g:hiddenField name="numColMeas" value="${reportTemplateInstance?.columnMeasureList?.size()?:1}"/>
            <g:hiddenField name="validColMeasIndex" value=""/>
        </div>
    </div>

    <div class="row" style="padding-top: 30px; padding-right: 15px; padding-left: 15px;">
        <div class="col-xs-12 fieldOptions columnRenameArea" hidden="hidden">
            <i class="fa fa-times add-cursor closeRenameArea" style="float: right;"></i>
            <div class="row">
                <div class="col-xs-4 form-inline">
                    <label><g:message code="app.label.name" />:</label>
                    <div class="form-inline">
                        <input class="selectedColumnName form-control">
                        <input type="button" class="btn btn-default btn-sm resetThisCol pv-btn-grey" value="${message(code: 'default.button.reset.label')}">
                    </div>
                </div>
                <div class="col-xs-3 datasheetOption" hidden="hidden">
                    <label><g:message code="app.label.datasheet"/> </label>
                    <g:select class="form-control" name="selectDatasheet"
                              from="${ViewHelper.getDatasheet()}" optionKey="name" optionValue="display"/>
                </div>
                <div class="col-xs-3">
                    <a class="add-cursor showCustomExpression"><g:message code="app.template.customExpression" /></a>
                </div>
            </div>
            <div class="row customExpressionArea" hidden="hidden">
                <div class="col-xs-12">
                    <textarea class="form-control customExpressionValue"></textarea>
                </div>
            </div>
        </div>
    </div>

    <div id="measureOptionsArea">
        <g:each in="${reportTemplateInstance?.columnMeasureList}" var="columnMeasure" status="i">
            <g:each in="${columnMeasure?.measures}" var="measure" status="j">
                <g:render template="measureOptions" model="['measure': measure, 'colMeasIndex': i, 'measIndex': j]"/>
            </g:each>
        </g:each>

        <g:render template="measureOptions" model="['colMeasIndex': '', 'measIndex': '']"/>
    </div>

    <div class="row reassessListedness" hidden="hidden" style="padding-top: 10px;">
        <div class="col-xs-4">
            <label><g:message code="app.label.reassessListedness"/> </label>
            <g:select class="form-control" name="reassessListedness"
                      from="${ViewHelper.getReassessListedness()}" optionKey="name" optionValue="display"
                      value="${reportTemplateInstance?.reassessListedness}"/>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-12">
            <input id="JSONMeasures" name="JSONMeasures" hidden="hidden" value="${reportTemplateInstance.getJSONStringMeasures()}">
        </div>
    </div>


%{--<g:hiddenField name="columns" value="${templateService.getJSONStringRF(reportTemplateInstance?.columnList)}"/>--}%
<g:hiddenField name="rows" value="${templateService.getJSONStringRF(reportTemplateInstance?.rowList)}"/>
