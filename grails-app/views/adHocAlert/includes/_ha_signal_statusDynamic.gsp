<%@ page import="com.rxlogix.config.Disposition; com.rxlogix.config.SignalStrategy; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.signal.AdHocAlertType; com.rxlogix.config.Priority; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.AlertAttributesService; com.rxlogix.SafetyLeadSecurityService;" %>
<g:set var="grailsApplication" bean="grailsApplication"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                ${sectionLabelMap['signalstatus']}
            </label>
        </div>

        <div class="rxmain-container-content">
            <g:set var="totalEnabled" value="${0}"/>
            <g:set var="allEnabled" value="${new java.util.ArrayList<Map>()}"/>
            <g:each var="entry" in="${signalStatusList}">
                <g:if test="${entry.enabled == true}">
                    <g:set var="totalEnabled" value="${totalEnabled+1}"/>
                    <g:set var="allEnabled" value="${allEnabled << entry}"/>
                </g:if>
            </g:each>
            <div class="row">
                <g:if test="${totalEnabled>=1}" >
                    <div class="col-md-3">
                        <g:render template="includes/adhocAlertHaSignalStatusDynamic" model='[entry: allEnabled[0]]'/>
                    </div>
                </g:if>
                <g:if test="${totalEnabled>=2}" >
                    <div class="col-md-3">
                        <g:render template="includes/adhocAlertHaSignalStatusDynamic" model='[entry: allEnabled[1]]'/>
                    </div>
                </g:if>
                <g:if test="${totalEnabled>=3}" >
                    <div class="col-md-3">
                        <g:render template="includes/adhocAlertHaSignalStatusDynamic" model='[entry: allEnabled[2]]'/>
                    </div>
                </g:if>
                <g:if test="${totalEnabled>=4}" >
                    <div class="col-md-3">
                        <g:render template="includes/adhocAlertHaSignalStatusDynamic" model='[entry: allEnabled[3]]'/>
                    </div>
                </g:if>

            </div>
        </div>
    </div>
</div>