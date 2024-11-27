<g:set var="queryService" bean="queryService"/>
<asset:javascript src="app/pvs/query/to_add_container.js"/>
<div id="toAddContainer" class="row toAddContainer">
    <div id="errorMessageOperator" class="errorMessageOperator">Enter Valid Number</div>
    <div class="col-xs-4 expressionsNoPadFirst">
        <select name="selectField" id="selectField" class="form-control expressionField">
            <option default><g:message code="dataTabulation.select.field" /></option>
            <g:if test="${isEudraQuery}">
                <g:each in="${queryService.getEudraReportFields()}" var="group">
                    <optgroup label="${message(code: "app.reportFieldGroup.${group.text}")}">
                        <g:each in="${group.children}" var="field">
                            <option value="${field.name}">${message(code: "app.reportField.${field.name}")}</option>
                        </g:each>
                    </optgroup>
                </g:each>
            </g:if>
            <g:else>
                <g:each in="${queryService.getReportFields()}" var="group">
                    <optgroup label="${message(code: "app.reportFieldGroup.${group.text}")}">
                        <g:each in="${group.children}" var="field">
                            <option value="${field.name}">${message(code: "app.reportField.${field.name}")}</option>
                        </g:each>
                    </optgroup>
                </g:each>

            </g:else>
        </select>

        <div class="hide reportFieldOptions">
            <option default><g:message code="dataTabulation.select.field" /></option>
            <g:each in="${queryService.getReportFields()}" var="group">
                <optgroup label="${message(code: "app.reportFieldGroup.${group.text}")}">
                    <g:each in="${group.children}" var="field">
                        <option value="${field.name}">${message(code: "app.reportField.${field.name}")}</option>
                    </g:each>
                </optgroup>
            </g:each>
        </div>

        <div class="hide reportFieldOptionsEudra">
            <option default><g:message code="dataTabulation.select.field" /></option>
            <g:each in="${queryService.getEudraReportFields()}" var="group">
                <optgroup label="${message(code: "app.reportFieldGroup.${group.text}")}">
                    <g:each in="${group.children}" var="field">
                        <option value="${field.name}">${message(code: "app.reportField.${field.name}")}</option>
                    </g:each>
                </optgroup>
            </g:each>
        </div>
    </div>

    <div class="col-xs-3 expressionsOperator">
        <g:select name="selectOperator" id="selectOperator" from="${[]}"
                  class="form-control expressionOp"
                  noSelection="['': message(code: 'select.operator')]"/>
    </div>

    <div class="col-xs-3">
        <div id="showValue" class="row expressionsNoPad showValue">
            <g:textField name="selectValue" id="selectValue"
                         class="form-control expressionValueText" placeholder="${message(code: 'value')}"/>
        </div>

        <div id="showSelect" class="row expressionsNoPad">
            <g:select name="selectSelect" id="selectSelect" from="${[]}"
                      class="form-control expressionValueSelect"
                      noSelection="['': message(code:'select.value')]" multiple="true"/>
        </div>

        <div id="showSelectAuto" class="row expressionsNoPad">
            <g:select name="selectSelectAuto" from="${[]}"
                      class="form-control expressionValueSelectAuto" multiple="true"/>
        </div>

        <div id="showSelectNonCache" class="row expressionsNoPad">
            <g:select name="selectSelectNonCache" from="${[]}"
                   class="form-control expressionValueSelectNonCache" multiple="true"/>
        </div>

        <div id="showDate" class="row fuelux expressionsNoPad">
            <div class="datepicker expressionValueDate" id="selectDate">
                <div class="input-group">
                    <input class="form-control expressionValueDateInput" id="selectDateInput" name="selectDate"
                           type="text"/>

                    <div class="input-group-btn">
                        <button type="button" class="btn btn-default dropdown-toggle"
                                data-toggle="dropdown">
                            <span class="glyphicon glyphicon-calendar"></span>
                            <span class="sr-only"><g:message code="scheduler.toggleCalendar" /></span>
                        </button>

                        <div class="dropdown-menu dropdown-menu-right datepicker-calendar-wrapper"
                             role="menu">
                            <div class="datepicker-calendar">
                                <div class="datepicker-calendar-header">
                                    <button type="button" class="prev"><span
                                            class="glyphicon glyphicon-chevron-left"></span><span
                                            class="sr-only"><g:message code="scheduler.previousMonth" /></span></button>
                                    <button type="button" class="next"><span
                                            class="glyphicon glyphicon-chevron-right"></span><span
                                            class="sr-only"><g:message code="scheduler.nextMonth" /></span></button>
                                    <button type="button" class="title">
                                        <span class="month">
                                            <span data-month="0"><g:message code="scheduler.january" /></span>
                                            <span data-month="1"><g:message code="scheduler.february" /></span>
                                            <span data-month="2"><g:message code="scheduler.march" /></span>
                                            <span data-month="3"><g:message code="scheduler.april" /></span>
                                            <span data-month="4"><g:message code="scheduler.may" /></span>
                                            <span data-month="5"><g:message code="scheduler.june" /></span>
                                            <span data-month="6"><g:message code="scheduler.july" /></span>
                                            <span data-month="7"><g:message code="scheduler.august" /></span>
                                            <span data-month="8"><g:message code="scheduler.september" /></span>
                                            <span data-month="9"><g:message code="scheduler.october" /></span>
                                            <span data-month="10"><g:message code="scheduler.november" /></span>
                                            <span data-month="11"><g:message code="scheduler.december" /></span>
                                        </span> <span class="year"></span>
                                    </button>
                                </div>
                                <table class="datepicker-calendar-days">
                                    <thead>
                                    <tr>
                                        <th><g:message code="scheduler.short.sunday" /></th>
                                        <th><g:message code="scheduler.short.monday" /></th>
                                        <th><g:message code="scheduler.short.tuesday" /></th>
                                        <th><g:message code="scheduler.short.wednesday" /></th>
                                        <th><g:message code="scheduler.short.thursday" /></th>
                                        <th><g:message code="scheduler.short.friday" /></th>
                                        <th><g:message code="scheduler.short.saturday" /></th>
                                    </tr>
                                    </thead>
                                    <tbody></tbody>
                                </table>

                                <div class="datepicker-calendar-footer">
                                    <button type="button" class="datepicker-today"><g:message code="scheduler.today" /></button>
                                </div>
                            </div>

                            <div class="datepicker-wheels" aria-hidden="true">
                                <div class="datepicker-wheels-month">
                                    <h2 class="header"><g:message code="scheduler.month" /></h2>
                                    <ul>
                                        <li data-month="0"><button type="button"><g:message code="scheduler.short.january" /></button></li>
                                        <li data-month="1"><button type="button"><g:message code="scheduler.short.february" /></button></li>
                                        <li data-month="2"><button type="button"><g:message code="scheduler.short.march" /></button></li>
                                        <li data-month="3"><button type="button"><g:message code="scheduler.short.April" /></button></li>
                                        <li data-month="4"><button type="button"><g:message code="scheduler.may" /></button></li>
                                        <li data-month="5"><button type="button"><g:message code="scheduler.short.june" /></button></li>
                                        <li data-month="6"><button type="button"><g:message code="scheduler.short.july" /></button></li>
                                        <li data-month="7"><button type="button"><g:message code="scheduler.short.august" /></button></li>
                                        <li data-month="8"><button type="button"><g:message code="schedulere.short.september" /></button></li>
                                        <li data-month="9"><button type="button"><g:message code="scheduler.short.octoberr" /></button></li>
                                        <li data-month="10"><button type="button"><g:message code="scheduler.short.november" /></button></li>
                                        <li data-month="11"><button type="button"><g:message code="scheduler.short.december" /></button></li>
                                    </ul>
                                </div>

                                <div class="datepicker-wheels-year">
                                    <h2 class="header"><g:message code="scheduler.year" /></h2>
                                    <ul></ul>
                                </div>

                                <div class="datepicker-wheels-footer clearfix">
                                    <button type="button" class="btn datepicker-wheels-back"><span
                                            class="glyphicon glyphicon-arrow-left"></span><span
                                            class="sr-only"><g:message code="scheduler.return.to.calendar" /></span></button>
                                    <button type="button"
                                            class="btn datepicker-wheels-select"><g:message code="scheduler.select" /> <span
                                            class="sr-only"><g:message code="scheduler.month.and.year" /></span></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row extraValues"></div>
    </div>

    <div class="col-xs-1 expressionsNoPad">
        <a href="#" class="copy-n-paste modal-link" tabindex="0" title="Edit Query"><i class="fa fa-pencil-square-o"></i></a>
    </div>

    <div class="col-xs-1" hidden="hidden">
    </div>
</div>

<!-- Modal for copy and paste -->
<div class="modal fade" id="copyAndPasteModal" tabindex="-1" role="dialog" aria-labelledby="Copy/Paste Dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="please.paste.the.contents.below" />:</h4>
            </div>
            <div class="modal-body container-fluid">
                <div class="row">
                    <label><g:message code="app.delimiters" />:</label>
                </div>
                <div class="row" id="delimiter-options">
                    <div class="icon-col" title="No delimiters">
                        <input type="radio" name="delimiter" checked="checked" value="none" id="delimiter_none">
                        <span title="No delimiters"><g:message code="app.label.none" /></span>
                    </div>
                    <div class="icon-col" title="comma">
                        <input type="radio" name="delimiter" value=","><span>','</span>
                    </div>
                    <div class="icon-col" title="semi-colon">
                        <input type="radio" name="delimiter" value=";"><span>';'</span>
                    </div>
                    <div class="icon-col" title="space">
                        <input type="radio" name="delimiter" value=" "><span>' '</span>
                    </div>
                    <div class="icon-col" title="Others">
                        <input type="radio" name="delimiter" value="others"><span><g:message code="app.others" /></span>
                    </div>
                    <div class="icon-col">
                        <input type="text" id="c_n_p_other_delimiter" >
                    </div>
                </div>
                <div class="row content-row">
                    <textarea id="copyPasteContent" class="form-control"></textarea>
                </div>
                <div class="row">
                </div>
                <div class="row"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn confirm-paste btn-primary"><g:message code="default.button.confirm.label" /></button>
                <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>

            </div>
        </div>
    </div>
</div>