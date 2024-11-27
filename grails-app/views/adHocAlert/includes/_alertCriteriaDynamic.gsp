<%@ page import="grails.util.Holders; com.rxlogix.config.EvaluationReferenceType; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<g:set var="totalEnabled" value="${0}"/>
<g:set var="allEnabled" value="${new java.util.ArrayList<Map>()}"/>
<g:set var="productStudyFlag" value="${false}"/>
<g:set var="matchingAlerts" value="${[:]}"/>
<g:each var="entry" in="${alertCriteriaList}">
    <g:if test="${entry.name == 'matchingAlerts' && entry.enabled == true}">
        <!-- Assign matchingAlerts entry to the separate map -->
        <g:set var="matchingAlerts" value="${entry}"/>
    </g:if>
    <g:else>
        <g:if test="${entry.enabled == true}">
            <g:if test="${entry.name in ['productSelection', 'study', 'studySelection']}">
                <g:if test="${!productStudyFlag}">
                    <g:set var="totalEnabled" value="${totalEnabled + 1}"/>
                    <g:set var="allEnabled" value="${allEnabled << entry}"/>
                    <g:set var="productStudyFlag" value="${true}"/>
                </g:if>
            </g:if>
            <g:else>
                <g:set var="totalEnabled" value="${totalEnabled + 1}"/>
                <g:set var="allEnabled" value="${allEnabled << entry}"/>
            </g:else>
        </g:if>
    </g:else>
</g:each>
<!--Scenario 1 start----->
<g:if test="${
    allEnabled.size() > 1 &&
    (
    (allEnabled[0]?.name == 'eventSelection' &&
    allEnabled[1]?.name in ['productSelection', 'study', 'studySelection']
    ) ||
    (allEnabled[1]?.name == 'eventSelection' &&
    allEnabled[0]?.name in ['productSelection', 'study', 'studySelection'])
    )
}">
<div class="row">
    <g:if test="${totalEnabled>=1}" >

            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[0]]'/>


    </g:if>
    <g:if test="${totalEnabled>=2}" >
        <div class="col-md-3">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[1]]'/>
        </div>
    </g:if>
    <g:if test="${totalEnabled>=3}" >
        <div class="col-md-3">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[2]]'/>
        <g:if test="${totalEnabled>=5}" >
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[4]]'/>
        </g:if>
        </div>
    </g:if>
    <g:if test="${totalEnabled>=4}" >
        <div class="col-md-3">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[3]]'/>
        <g:if test="${totalEnabled>=6}" >
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[5]]'/>
        </g:if>
        </div>
    </g:if>
</div>
<div class="row">
    <g:if test="${totalEnabled>=7}" >
        <div class="col-md-3">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[6]]'/>

        </div>
    </g:if>
    <g:if test="${totalEnabled>=8}" >
        <div class="col-md-3">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[7]]'/>

        </div>
    </g:if>
    <g:if test="${totalEnabled>=9}" >
        <div class="col-md-3">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[8]]'/>

        </div>
    </g:if>
    <g:if test="${totalEnabled>=10}" >
        <div class="col-md-3">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[9]]'/>

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
        <g:if test="${matchingAlerts?.name == 'matchingAlerts'}">
            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: matchingAlerts]'/>
        </g:if>
    </div>
</div>
</g:if>
<g:elseif test="${allEnabled.size() > 0 && (allEnabled[0]?.name == 'eventSelection' || allEnabled[0]?.name in ['productSelection', 'study', 'studySelection'])}">
    <div class="row">
        <g:if test="${totalEnabled>=1}" >

            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[0]]'/>


        </g:if>
        <g:if test="${totalEnabled>=2}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[1]]'/>
                <g:if test="${totalEnabled>=5}" >
                    <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[4]]'/>
                </g:if>
            </div>
        </g:if>
        <g:if test="${totalEnabled>=3}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[2]]'/>
                <g:if test="${totalEnabled>=6}" >
                    <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[5]]'/>
                </g:if>
            </div>
        </g:if>
        <g:if test="${totalEnabled>=4}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[3]]'/>
                <g:if test="${totalEnabled>=7}" >
                    <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[6]]'/>
                </g:if>
            </div>
        </g:if>
    </div>
    <div class="row">
        <g:if test="${totalEnabled>=8}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[7]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=9}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[8]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=10}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[9]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=11}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[10]]'/>

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
            <g:if test="${matchingAlerts?.name == 'matchingAlerts'}">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: matchingAlerts]'/>
            </g:if>
        </div>
    </div>
</g:elseif>
<g:else>
    <div class="row">
        <g:if test="${totalEnabled>=1}" >

            <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[0]]'/>


        </g:if>
        <g:if test="${totalEnabled>=2}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[1]]'/>
            </div>
        </g:if>
        <g:if test="${totalEnabled>=3}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[2]]'/>
            </div>
        </g:if>
        <g:if test="${totalEnabled>=4}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[3]]'/>
            </div>
        </g:if>
    </div>
    <div class="row">
        <g:if test="${totalEnabled>=5}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[4]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=6}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[5]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=7}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[6]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=8}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[7]]'/>

            </div>
        </g:if>
    </div>
    <div class="row">
        <g:if test="${totalEnabled>=9}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[8]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=10}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[9]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=11}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[10]]'/>

            </div>
        </g:if>
        <g:if test="${totalEnabled>=12}" >
            <div class="col-md-3">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: allEnabled[11]]'/>

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
            <g:if test="${matchingAlerts?.name == 'matchingAlerts'}">
                <g:render template="includes/adhocAlertCriteriaDynamicDetails" model='[entry: matchingAlerts]'/>
            </g:if>
        </div>
    </div>
</g:else>
<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList, isPVCM: isPVCM]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal" />
    <g:render template="/includes/modals/product_selection_modal" />
    <g:render template="/includes/modals/study_selection_modal" />
</g:else>
<g:render template="includes/matching_alerts" />

