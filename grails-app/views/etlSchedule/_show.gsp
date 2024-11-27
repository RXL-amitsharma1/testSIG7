<%@ page import="com.rxlogix.enums.EtlStatusEnum; java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; grails.util.Environment; com.rxlogix.Constants; org.apache.commons.lang3.text.WordUtils; com.rxlogix.enums.EtlStatusEnum" %>
<!doctype html>
<html>
<body>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>
<g:set var="startDT"
       value="${DateUtil.StringToDate(etlScheduleInstance?.startDateTime, Constants.DateFormat.WITHOUT_SECONDS)}"/>


<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">

    <div class="navScaffold">
        <g:link class="btn btn-primary" action="edit">
            <span class="glyphicon glyphicon-pencil icon-white"></span>
            <g:message code="default.edit.label" args="[entityName]"/>
        </g:link>
        <g:if test="${!etlScheduleInstance?.isInitial}">
            <button class="btn btn-primary" data-toggle="modal" data-target="#initialEtlModal">
                <g:message code="default.initialetl.label" args="[entityName]"/>
            </button>

            <!-- Modal -->
            <div class="modal fade" id="initialEtlModal" tabindex="-1" role="dialog">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                    aria-hidden="true">&times;</span></button>
                            <h4 class="modal-title"><g:message code="run.initial.etl"/></h4>
                        </div>

                        <div class="modal-body">
                            <p><g:message code="initialize.etl.now"/></p>
                        </div>

                        <div class="modal-footer">
                            <g:link class="btn btn-primary" action="initialize">
                                <g:message code="default.button.changeowner.label"/>
                            </g:link>
                            <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                                    code="default.button.cancel.label"/></button>
                        </div>
                    </div><!-- /.modal-content -->
                </div><!-- /.modal-dialog -->
            </div><!-- /.modal -->

        </g:if>

    </div>
</sec:ifAnyGranted>

    <div class="horizontalRuleFull m-t-15"></div>

    <div><b><u><g:message code="app.etlStatus.info.label"/></u></b></div>

    <div class="row">
        <div class="col-md-6" col-xs-offset-6>

            <div class="row m-t-5">
                <div class="col-md-${column1Width}"><label><g:message code="etlMaster.scheduleName.label"/></label>
                </div>

                <div class="col-md-${column2Width}">${etlScheduleInstance?.scheduleName}</div>
            </div>

            <div class="row m-t-5">
                <div class="col-md-${column1Width}"><label><g:message code="etlScheduler.start.date"/></label></div>

                <div class="col-md-${column2Width}"><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                                              model="[date: startDT]"/></div>
            </div>

            <g:each in="${etlScheduleInstance?.repeatInterval?.split(';')}">
                <div class="row m-t-5">
                    <div class="col-md-${column1Width}">
                        <g:if test="${it?.split('=').head().equals('BYDAY')}"><label><g:message
                                code="etlschedule.by.day"/></label></g:if>
                        <g:elseif test="${it?.split('=').head().equals('BYMONTH')}"><label><g:message
                                code="etlschedule.by.month"/></label></g:elseif>
                        <g:elseif test="${it?.split('=').head().equals('BYMONTHDAY')}"><label><g:message
                                code="etlschedule.day.of.month"/></label></g:elseif>
                        <g:elseif test="${it?.split('=').head().equals('BYSETPOS')}"><label><g:message
                                code="etlSchedule.by.set.pos"/></label></g:elseif>
                        <g:else>
                            <label>${WordUtils.capitalizeFully(it?.split("=").head())}</label>
                        </g:else>
                    </div>

                    <div class="col-md-${column2Width}">
                        <g:if test="${it.split("=").head().equals('UNTIL')}">
                            ${DateUtil.SimpleDateReformat(it.split("=").tail().first(), Constants.DateFormat.BASIC_DATE, com.rxlogix.Constants.DateFormat.SIMPLE_DATE)}
                        </g:if>
                        <g:elseif test="${it.split("=").head().equals('FREQ')}">
                            ${WordUtils.capitalizeFully(it.split("=").tail().first())}
                        </g:elseif>
                        <g:elseif test="${it.split("=").head().equals('BYDAY')}">
                            ${WordUtils.capitalizeFully(it.split("=").tail().first(), ",".toCharArray())}
                        </g:elseif>
                        <g:else>
                            <g:if test="${it.split("=").tail().size() != 0}">
                                ${it.split("=").tail().first()}
                            </g:if>
                        </g:else>
                    </div>
                </div>
            </g:each>

            <div class="row m-t-5">
                <div class="col-md-${column1Width}"><label><g:message code="default.button.enable.label"/></label></div>

                <div class="col-md-${column2Width}"><g:formatBoolean boolean="${etlScheduleInstance?.isDisabled}"
                                                                     false="Yes" true="No"/></div>
            </div>

            <div class="row m-t-5">
                <div class="col-md-${column1Width}"><label><g:message code="etl.execution.status"/></label></div>

                <div class="col-md-${column2Width} etlStatus">
                    <g:if test="${!etlStatus}">
                        <span><g:message code="etl.execution.no.status"/></span>
                    </g:if>
                    <g:elseif test="${EtlStatusEnum.SUCCESS == etlStatus}">
                        <span class="label label-success"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:elseif>
                    <g:elseif test="${EtlStatusEnum.RUNNING == etlStatus}">
                        <span class="label label-primary"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:elseif>
                    <g:elseif test="${EtlStatusEnum.FAILED == etlStatus}">
                        <span class="label label-danger"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:elseif>
                    <g:else>
                        <span class="label label-primary"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:else>
                </div>
            </div>

            <div class="row m-t-5">
                <div class="col-md-${column1Width}"><label><g:message code="etl.lastRun.dateTime"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:render template="/includes/widgets/dateDisplayWithTimezone"
                              model="[date: lastRunDateTime]"/>
                </div>
            </div>

        </div>
    </div>


</body>
</html>
