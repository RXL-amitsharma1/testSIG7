<%@ page import="com.rxlogix.config.SignalStrategy; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.signal.AdHocAlertType; com.rxlogix.config.Priority; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.AlertAttributesService; com.rxlogix.SafetyLeadSecurityService;grails.util.Holders;" %>
<g:set var="grailsApplication" bean="grailsApplication" />
<g:set var="alertAttributesService" bean="alertAttributesService"/>
<div class="panel panel-default rxmain-container rxmain-container-top">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            <a data-toggle="collapse" data-parent="#accordion-pvs-form" href="#pvsAlertDetails" aria-expanded="true" class="">
                ${sectionLabelMap['alertdetails']}
            </a>
        </h4>
    </div>
    <div id="pvsAlertDetails" class="panel-collapse rxmain-container-content rxmain-container-show collapse in pos-rel" aria-expanded="true">
        <g:set var="totalEnabled" value="${0}"/>
        <g:set var="allEnabled" value="${new java.util.ArrayList<Map>()}"/>
        <g:set var="issuePreviouslyTracked" value="${[:]}"/>
        <g:each var="entry" in="${alertDetailsList}">
            <g:if test="${entry.name == 'issuePreviouslyTracked' && entry.enabled == true}">
                <!-- Assign issuePreviouslyTracked entry to the separate map -->
                <g:set var="issuePreviouslyTracked" value="${entry}"/>
            </g:if>
            <g:else>
            <g:if test="${entry.enabled == true}">
                <g:set var="totalEnabled" value="${totalEnabled+1}"/>
                <g:set var="allEnabled" value="${allEnabled << entry}"/>
            </g:if>
            </g:else>
        </g:each>
        <div class="row">
            <g:if test="${totalEnabled>=1}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[0]]'/>
                </div>
            </g:if>
            <g:if test="${totalEnabled>=2}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[1]]'/>
                </div>
            </g:if>
            <g:if test="${totalEnabled>=3}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[2]]'/>
                </div>
            </g:if>
            <g:if test="${totalEnabled>=4}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[3]]'/>
                </div>
            </g:if>

        </div>
        <div class="row">
            <g:if test="${totalEnabled>=5}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[4]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=6}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[5]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=7}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[6]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=8}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[7]]'/>

                </div>
            </g:if>
        </div>
        <div class="row">
            <g:if test="${totalEnabled>=9}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[8]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=10}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[9]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=11}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[10]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=12}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[11]]'/>

                </div>
            </g:if>
        </div>
        <div class="row">
            <g:if test="${totalEnabled>=13}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[12]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=14}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[13]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=15}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[14]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=16}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[15]]'/>

                </div>
            </g:if>
        </div>
        <div class="row">
            <g:if test="${totalEnabled>=17}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[16]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=18}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[17]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=19}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[18]]'/>

                </div>
            </g:if>
            <g:if test="${totalEnabled>=20}" >
                <div class="col-md-3">
                    <g:render template="includes/adhocAlertDynamicDetails" model='[entry: allEnabled[19]]'/>

                </div>
            </g:if>
        </div>
        <div class="row">
            <div class="col-md-3">
            </div>
            <div class="col-md-3">
            </div>
            <div class="col-md-3">
            </div>
            <div class="col-md-3">
                <g:if test="${issuePreviouslyTracked?.enabled == true && issuePreviouslyTracked?.name == 'issuePreviouslyTracked'}">

                    <div id="check-public" class="pull-right">

                        <g:checkBox name="issuePreviouslyTracked" value="${alertInstance?.issuePreviouslyTracked}"/>
                        <label for="issuePreviouslyTracked" class="ms-2">${issuePreviouslyTracked?.label}</label>

                    </div>

                </g:if>
            </div>
        </div>



    </div>
</div>
