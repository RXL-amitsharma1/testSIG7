<asset:javascript src="app/pvs/query/to_add_container.js"/>
<div id="toAddContainer" class="toAddContainer">
    <div id="errorMessageOperator" class="errorMessageOperator">Enter Valid Number</div>
    <div class="row">
    <div class="col-xs-3 expressionsNoPadFirst">
        <select name="selectField" id="selectField" class="form-control expressionField">
            <option value = "-1"><g:message code="dataTabulation.select.field" /></option>
            <g:each in="${fieldList}" var="field">
                <option value="${field.name}">${field.display}</option>
            </g:each>
        </select>
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

        <div class="col-xs-1 copy-paste-pencil">
            <i tabindex="0"
               class="fa fa-pencil-square-o copy-n-paste" id="advance-filter-pencil" data-toggle="modal"></i>
        </div>

        <div class="col-xs-1 expressionsNoPad addExpressionButton hidden">
            <g:submitButton type="button" name="Add" id="addExpression"
                            class="btn btn-primary"/>
        </div>
        <div class="col-xs-4 productGroupSelectionClass filterGroupCheckboxDiv"  style=" display: none;">
            <span>
                <input type="checkbox" id="productGroupSelection" name="productGroupSelection" value="productGroupSelection" class="filterGroupCheckbox">
                <label for="productGroupSelection" style="  font-size: 12px" > Product Group</label><br>
            </span>
        </div>

        <div class="col-xs-4  eventGroupSelectionClass filterGroupCheckboxDiv"  style="display: none;">
            <span>
                <input type="checkbox" id="eventGroupSelection" name="eventGroupSelection" value="eventGroupSelection" class ="filterGroupCheckbox">
                <label for="eventGroupSelection"  style="  font-size: 12px">Event Group</label><br>
            </span>
        </div>
    </div>
    </div>