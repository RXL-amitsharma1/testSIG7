<!-- Right Sidebar -->
<%@ page import="com.rxlogix.Constants;" %>

<div class="side-bar right-bar ${appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT ? !isJaderAvailable ? 'agg-alert' : '':''} caselist-config-panel ${allFourEnabled? 'all-four-enabled': allThreeEnabled? 'all-three-enabled': (anyTwoEnabled ? '': (anyOneEnabled ? 'any-one-enabled' : 'no-one-enabled'))}"
     id="${fieldConfigurationBarId}" data-fieldcontainerreference="${fieldConfigurationBarId}" style="${allFourEnabled ? 'right: -1240px' : allThreeEnabled ? 'right: -1039px' : anyTwoEnabled ? 'right: -840px' : anyOneEnabled ? 'right: -649px' : 'right: -431px'}">
    <div class="pv-list-config-action">
        <a class="ic-sm" href="javascript:void(0);" id="btnSaveListConfig" title="Save Changes"><i class="md md-done"></i> </a>
        <a class="text-danger ic-sm" href="javascript:void(0);" id="btnCloseListConfig" title="Cancel Changes"><i class="md md-close"></i> </a>
    </div>
    <div style="overflow: hidden" class="pv-grid-setting-box">
        <div class="col-sm-12 ">
            <div class="pvi-list-group list-group">

                <div class="inner-shadow-box">
                    <div class="phead-1 phead"><g:message code="app.label.column.side.panel.primary"></g:message></div>
                    <div class="list-group-primary display-config-field short-field-primary" ondragover="event.preventDefault()"></div>
                </div>

                <g:if test="${appType == com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT && !isJaderAvailable}">
                    <div class="inner-shadow-box">
                        <div class="phead-2 phead"><g:message code="app.label.column.side.panel.optionalsdb"></g:message></div>
                        <div class="list-group-optionalsdb display-config-field2 short-field" ondragover="event.preventDefault()"></div>
                    </div>
                    <g:if test="${appType == com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT && isFaersEnabled}">
                        <div class="inner-shadow-box">
                            <div class="phead-3 phead"><g:message code="app.label.column.side.panel.optionalfdb"></g:message></div>
                            <div class="list-group-optional display-config-field3 short-field" ondragover="event.preventDefault()"></div>
                        </div>
                    </g:if>
                    <g:if test="${appType == com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT && isEvdasEnabled}">
                        <div class="inner-shadow-box">
                            <div class="phead-4 phead"><g:message code="app.label.column.side.panel.optionaledb"></g:message></div>
                            <div class="list-group-optionaledb display-config-field4 short-field" ondragover="event.preventDefault()"></div>
                        </div>
                    </g:if>
                    <g:if test="${appType == com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT && isVaersEnabled}">
                        <div class="inner-shadow-box">
                            <div class="phead-5 phead"><g:message code="app.label.column.side.panel.optionalvdb"></g:message></div>
                            <div class="list-group-optionalvdb display-config-field5 short-field" ondragover="event.preventDefault()"></div>
                        </div>
                    </g:if>
                    <g:if test="${appType == com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT && isVigibaseEnabled}">
                        <div class="inner-shadow-box">
                            <div class="phead-6 phead"><g:message code="app.label.column.side.panel.optionalvgdb"></g:message></div>
                            <div class="list-group-optionalvgdb display-config-field6 short-field" ondragover="event.preventDefault()"></div>
                        </div>
                    </g:if>
                </g:if>
                <g:elseif test="${appType == com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT && isJaderAvailable}">
                    <div class="inner-shadow-box">
                        <div class="phead-2 phead"><g:message code="app.label.column.side.panel.optional"></g:message></div>
                        <div class="list-group-optional display-config-field2 short-field" ondragover="event.preventDefault()"></div>
                    </div>
                </g:elseif>
                <g:elseif test="${appType != com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                    <div class="inner-shadow-box">
                        <div class="phead-2 phead"><g:message code="app.label.column.side.panel.optional"></g:message></div>
                        <div class="list-group-optional display-config-field2 short-field" ondragover="event.preventDefault()"></div>
                    </div>
                </g:elseif>

            </div>
        </div>
    </div>
</div>
<!-- END Right-bar -->
<script>
    $(document).ready(function () {
        var width ;
        if( typeof isJaderAvailable != 'undefined' && isJaderAvailable){
           $('.agg-alert.caselist-config-panel').css('width',"431 px");
        } else if (typeof allFourEnabled !== 'undefined' && typeof allThreeEnabled !== 'undefined' && typeof anyTwoEnabled !== 'undefined' && typeof anyOneEnabled !== 'undefined') {
            width = allFourEnabled ? 1240 :  allThreeEnabled ? 1039 : anyTwoEnabled ? 840 : anyOneEnabled ? 649 : 431;
            $('.agg-alert.caselist-config-panel').css('width',width + "px");
        }
    });
</script>
