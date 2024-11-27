<g:set var="queryService" bean="queryService"/>
<div id="toAddContainerQEV" class="toAddContainerQEV queryBlankContainer">
    <div class="errorMessageOperator">Enter Valid Number</div>
    <div class="col-xs-4 expressionsNoPadFirst">
        <select name="selectField" readonly="true" class="form-control expressionField">
            <option default><g:message code="dataTabulation.select.field" /></option>
            <g:each in="${queryService.getReportFields()}" var="group">
                <optgroup label="${message(code: "app.reportFieldGroup.${group.text}")}">
                    <g:each in="${group.children}" var="field">
                        <option value="${field.name}">${message(code: "app.reportField.${field.name}")}</option>
                    </g:each>
                </optgroup>
            </g:each>
        </select>
    </div>

    <div class="col-xs-3 expressionsOperator">
        <g:select name="selectOperator" from="${[]}"
                  class="form-control expressionOp"
                  readonly="true"
                  noSelection="['': 'Select Operator']"/>
    </div>

    <div class="col-xs-4 expressionsNoPad">
        <g:textField name="selectValue"
                     class="form-control expressionValueText" placeholder="Value"/>
    </div>

    <div class="col-xs-4 expressionsNoPad expressionValueSelect1">
        <g:select name="selectSelect" from="${[]}"
                  class="form-control expressionValueSelect"
                  noSelection="['': 'Select Value']" multiple="true"/>
    </div>

    <div id="showSelectAuto" class="col-xs-3 expressionsNoPad">
        <g:select name="selectSelectAuto" from="${[]}"
                  class="form-control expressionValueSelectAuto" multiple="true"/>
    </div>

    <div class="col-xs-4 expressionsNoPad expressionValueSelect2">
         <g:select name="selectSelectNonCache" from="${[]}"
               class="form-control expressionValueSelectNonCache" multiple="true"/>
    </div>

    <div class="col-xs-4 fuelux expressionsNoPad">
        <div class="datepicker expressionValueDate">
            <div class="input-group">
                <input class="form-control expressionValueDateInput" name="selectDate"
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
    <g:render template="/includes/copyPasteModalQuery"/>
    <div class="col-xs-1">
        <i class="fa fa-search searchEventDmv1" id="searchEvents" data-toggle="modal" data-target="#eventModal"></i>
        <i tabindex="0" class="copy-paste-pencil fa fa-pencil-square-o copy-n-paste advance-filter-pencil"
           id="advance-filter-pencil" data-toggle="modal"></i>
    </div>

    <div class="col-xs-1" hidden="hidden">
        <g:if test="${type == "qev"}">
            <input class="${type}ReportField" value="${qev?.reportField?.name}"/>
            <input class="${type}Operator" value="${qev?.operator}"/>
            <input class="${type}Value" value="${qev?.value}"/>
            <input class="${type}Key" value="${qev?.key}"/>
        </g:if>
        <g:if test="${type == "fev"}">
            <input class="${type}ReportField" value="${fev?.reportField?.name}"/>
            <input class="${type}Operator" value="${fev?.operator}"/>
            <input class="${type}Value" value="${fev?.value}"/>
            <input class="${type}Key" value="${fev?.key}"/>
        </g:if>
    </div>
</div>