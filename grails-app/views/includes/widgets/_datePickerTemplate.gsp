<div class="input-group-btn">
    <button type="button" class="btn btn-default dropdown-toggle meetingDate" data-toggle="dropdown">
        <span class="glyphicon glyphicon-calendar"></span>
        <span class="sr-only"><g:message code="scheduler.toggleCalendar" /></span>
    </button>

    <div class="dropdown-menu dropdown-menu-right datepicker-calendar-wrapper" role="menu">
        <div class="datepicker-calendar">
            <div class="datepicker-calendar-header">
                <button type="button" class="prev"><span class="glyphicon glyphicon-chevron-left"></span><span
                        class="sr-only"><g:message code="previous.month" />h</span></button>
                <button type="button" class="next"><span class="glyphicon glyphicon-chevron-right"></span><span
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
                        class="glyphicon glyphicon-arrow-left"></span><span class="sr-only"><g:message code="scheduler.return.to.calendar" /></span>
                </button>
                <button type="button" class="btn datepicker-wheels-select"><g:message code="scheduler.select" /> <span
                        class="sr-only"><g:message code="scheduler.month.and.year" /></span></button>
            </div>
        </div>
    </div>
</div>
