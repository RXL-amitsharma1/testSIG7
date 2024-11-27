<%@ page import="com.rxlogix.util.ViewHelper" %>
<g:set var="userService" bean="userService"/>

<div class="modal fade update_and_execute_modal" role="dialog" aria-labelledby="update_and_execute_modal" data-keyboard="false" data-backdrop="static">
    <div class="modal-dialog modal-md product-modal-dialog" role="document" >
        <div class="modal-content" >
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <label class="rxmain-container-header-label click" style="font-size: 14px">Update and Execute Alert(s)</label>
            </div>

            <div class="modal-body" style="overflow-x: hidden; height: 45vh">
                <form name="update_and_run_form unscheduled-form" id="update_and_run_form" action="../singleCaseAlert/updateAndExecuteAlert">
                    <g:render template="/alertAdministration/alertDateRange"
                    />
                    <div class="row">
                        <div class="col-xs-6" id="evaluatedDateAsDiv">
                            <label><g:message code="app.label.EvaluateCaseDateOn"/></label>
                            <div id="evaluateDateAsDiv">
                                <g:select name="evaluateDateAsNonSubmission"
                                          from="${ViewHelper.getEvaluateCaseDateI18n()}"
                                          optionValue="display" optionKey="name"
                                          value=""
                                          class="form-control evaluateDateAs" disabled="false"/>
                            </div>
                            <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden"
                                   value=""/>
                            <input class="hide" name="configId" id="configId" value=""/>
                            <input class="hide" name="configIdList" id="configIdList" hidden="hidden" value="">
                            <div style="margin-top: 10px">
                                <div class="fuelux">
                                    <div class="datepicker toolbarInline" id="asOfVersionDatePicker" hidden="hidden">
                                        <div class="input-group">
                                            <g:hiddenField name="asOfVersionDateValue"  value="${configurationInstance?.asOfVersionDate ?: null}"/>
                                            <input placeholder="${message(code: "select.version")}"
                                                   class="form-control" id="asOfVersionDateId"
                                                   name="asOfVersionDate" type="text"/>
                                            <g:render id="asOfVersion" template="/includes/widgets/datePickerTemplate"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div><br>
                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="runNow"
                                    name="runNow"
                                    value="runNow"
                                    checked="true"/>
                        <label for="runNow">
                            <g:message code="reportCriteria.exclude.run.now" default="Run Now"/>
                        </label>
                    </div>
                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="futureSchedule"
                                    name="futureSchedule"
                                    value="futureSchedule"
                                    checked="false"/>
                        <label for="futureSchedule" >
                            <g:message code="reportCriteria.exclude.future.schedule"  default="Future Schedule"/>
                        </label>
                    </div>
                    <g:render template="/includes/widgets/alertScheduleManual"
                              model="[configurationInstance: configurationInstance, userService: userService]"/>
                    <p id="updateAndExecuteInfoDiv"
                       style="bottom: 5px">On selecting “Run Now“ the system will start executing the alert on the current date/time</p>
                </form>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button class="btn primaryButton btn-primary repeat" id="update-and-run-alert" form="update_and_run_form" tabindex="0" accesskey="r"  type="submit">Update and Run</button>

                    <button type="button" class=" btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>