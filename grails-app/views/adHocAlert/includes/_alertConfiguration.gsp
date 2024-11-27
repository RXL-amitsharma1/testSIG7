<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container ">
    <g:set var="userService" bean="userService"/>
    <g:hiddenField name="owner" id="owner" value="${userService.getUser().id}"/>

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                ${sectionLabelMap['alertcriteria']}
            </label>
        </div>

        <div class="rxmain-container-content">

            %{-- Report Criteria & Sections --}%
            <g:render
                    template="includes/alertCriteriaDynamic"
                    model="[alertInstance: alertInstance, formulations: formulations, lmReportTypes: lmReportTypes, countryNames: countryNames, isPVCM: isPVCM, alertCriteriaList: alertCriteriaList]"
            />

        </div>
    </div>
</div>
