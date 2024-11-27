<div class="modal fade alert-pre-execution-check-modal" id="alert-pre-execution-check-modal" role="dialog" aria-labelledby="alert-pre-execution-check-label" data-keyboard="false" data-backdrop="static">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <label class="rxmain-container-header-label click" style="font-size: 16px">Alert Pre-Checks</label>
            </div>

            <div class="modal-body">
                <div class="form-check form-switch" style="padding-bottom: 8px; font-size: 15px">
                    <label><g:message code="app.alert.preExecution.pvrCheck"/></label>
                    <i class='glyphicon glyphicon-info-sign themecolor add-cursor' id="pvrCheckInfo"
                       style="top: 2px; cursor: pointer"></i>
                    <label class="switch-sm ">
                        <input  type="checkbox" id="pvrCheck" name="pvrCheck" class="pre-check-option" data-value="" hidden/>
                        <span class="slider round slider-small"></span>
                    </label>
                </div>

                <div class="form-check form-switch" style="padding-top: 12px;padding-bottom: 8px; font-size: 15px">
                    <label><g:message code="app.alert.preExecution.etlCheck"/></label>
                </div>


                <div class="form-check form-switch" style="padding-bottom: 8px; padding-left: 12px;font-size: 15px">
                    <label><g:message code="app.alert.preExecution.etl.versionAsOf"/></label>
                    <i class='glyphicon glyphicon-info-sign themecolor add-cursor' id="etlVersionAsOfCheckInfo"
                       style="top: 2px; cursor: pointer"></i>
                    <label class="switch-sm">
                        <input type="checkbox" id="versionAsOf" name="versionAsOf" class="pre-check-option" data-value="" hidden/>
                        <span class="round slider-small"></span>
                    </label>
                </div>



                <div class="row" style="padding-top: 8px;padding-left: 12px; font-size: 15px">
                    <div class ="col-sm-4"><label style="padding-top:8px ;font-size: 15px" ><g:message code="app.alert.preExecution.etl.latest"/></label></div>
                    <div class="form-check form-switch col-sm-4"
                         style="font-size: 15px ; margin-right: -40px !important;">
                        <label><g:message code="app.alert.preExecution.etl.latest.failure"/></label>
                        <i class='glyphicon glyphicon-info-sign themecolor add-cursor ' id="etlLatestFailureInfo"
                           style="top: 2px; cursor: pointer"></i>
                        <label class="switch-sm">
                            <input type="checkbox" id="etlFailure" name="etlFailure" class="pre-check-option" data-value="" hidden/>
                            <span class="slider round slider-small"></span>
                        </label>
                    </div>

                    <div class="form-check form-switch col-sm-4"
                         style="font-size: 15px">
                        <label><g:message code="app.alert.preExecution.etl.latest.inProgress"/></label>
                        <i class='glyphicon glyphicon-info-sign themecolor add-cursor ' id="etlLatestInProgressInfo"
                           style="top: 2px; cursor: pointer"></i>
                        <label class="switch-sm">
                            <input type="checkbox" id="etlInProgress" name="etlInProgress" class="pre-check-option" data-value="" hidden/>
                            <span class="slider round slider-small"></span>
                        </label>
                    </div>
                </div>
<br>

                <div style="background: #e1e2e399 !important; padding: 25px;
                border-radius: 10px">
                    <label style="font-size: 15px"><g:message code="app.alert.preExecution.auto.handling.of.alert"
                                                              default="Auto Handling Of Date Range For Disabled Alerts"/></label><br><br>

                    <div class="form-select row">
                        <div class="col-sm-3" style="font-size: 15px">
                            <label for="icrAutoAdjustmentRule"><g:message code="app.alert.preExecution.icr"
                                                                          default="Individual Case Alert (Scheduled)"/></label>
                        </div>

                        <div class="col-sm-1">
                            <label class="switch-sm" style="width: 50px;margin: 1px 29px 0 -5px;">
                                <input type="checkbox" id="singleCaseAutoAdjustmentEnabled"
                                       name="singleCaseAutoAdjustmentEnabled" data-value="" hidden/>
                                <span class="slider round slider-small"></span>
                            </label>
                        </div>

                        <div class="col-sm-9" style="width: 61.333333% !important; margin-left: 25px;">
                            <select id="icrAutoAdjustmentRule" class="form-control" style="font-size: 14px">
                                <option value="ALERT_PER_SKIPPED_EXECUTION">
                                    <g:message code="app.alert.preExecution.autoAdjust.alertForEverySkipped"
                                               default="Auto-Adjust Date And Execute Alert For Every Skipped Execution"/>
                                </option>
                                <option value="SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION">
                                    <g:message code="app.alert.preExecution.autoAdjust.singleAlert"
                                               default="Auto-Adjust Date And Execute A Single Alert For All Skipped Execution"/>
                                </option>
                                <option value="MANUAL_AUTO_ADJUSTMENT">
                                    <g:message code="app.alert.preExecution.autoAdjust.manual"
                                               default="Disable Alert Execution And Enable Based On Manual Intervention"/>
                                </option>
                            </select>
                        </div>
                        <i class='glyphicon glyphicon-info-sign themecolor add-cursor ' id="icrAutoAdjustmentInfo"
                           style="font-size:15px; top: 4px; left: 6px; cursor: pointer"></i>
                    </div>

                    <div class="form-select row">
                        <div class="col-sm-3" style="font-size: 15px">
                            <label for="aggregateAutoAdjustmentRule"><g:message code="app.alert.preExecution.aggregate"
                                                                                default="Aggregate Alert (Scheduled)"/></label>
                        </div>

                        <div class="col-sm-1">
                            <label class="switch-sm" style="width: 50px;margin: 1px 29px 0 -5px;">
                                <input type="checkbox" id="aggregateAutoAdjustmentEnabled"
                                       name="aggregateAutoAdjustmentEnabled" data-value="" hidden/>
                                <span class="slider round slider-small"></span>
                            </label>
                        </div>

                        <div class="col-sm-9" style="width: 61.333333% !important; margin-left: 25px;">
                            <select id="aggregateAutoAdjustmentRule" class="form-control" style="font-size: 14px">
                                <option value="ALERT_PER_SKIPPED_EXECUTION">
                                    <g:message code="app.alert.preExecution.autoAdjust.alertForEverySkipped"
                                               default="Auto-Adjust Date And Execute Alert For Every Skipped Execution"/>
                                </option>
                                <option value="SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION">
                                    <g:message code="app.alert.preExecution.autoAdjust.singleAlert"
                                               default="Auto-Adjust Date And Execute A Single Alert For All Skipped Execution"/>
                                </option>
                                <option value="MANUAL_AUTO_ADJUSTMENT">
                                    <g:message code="app.alert.preExecution.autoAdjust.manual"
                                               default="Disable Alert Execution And Enable Based On Manual Intervention"/>
                                </option>
                            </select>
                        </div>

                        <i class='glyphicon glyphicon-info-sign themecolor add-cursor ' id="aggAutoAdjustmentInfo"
                           style="font-size:15px; top: 4px; left: 6px; cursor: pointer"></i>

                    </div>
                </div>

            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class=" btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="button" id="save-alert-pre-check-values" class="btn btn-primary id-element">Save</button>
                </div>
            </div>
        </div>
    </div>
</div>