<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container ">
    <g:set var="userService" bean="userService"/>
    <g:if test="${action == "copy"}">
        <g:hiddenField name="owner" id="owner" value="${userService.getUser().id}"/>
    </g:if>
    <g:else>
        <g:hiddenField name="owner" id="owner" value="${configurationInstance?.owner?.id ?: userService.getUser().id}"/>
    </g:else>    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.alert.criteria"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-4" style="padding-bottom: 10px">
                    <label class="radio-inline">
                        <g:radio name="selectedDatasource" value="pubmed"
                                 checked="${configurationInstance.selectedDatasource?configurationInstance.selectedDatasource == 'pubmed' : true}"/>
                        ${com.rxlogix.Constants.LiteratureFields.PUBMED}
                    </label>
                    <label class="radio-inline">
                        <g:radio name="selectedDatasource" value="embase"
                                 checked="${configurationInstance.selectedDatasource?configurationInstance.selectedDatasource == 'embase' : false}"/>
                        ${com.rxlogix.Constants.LiteratureFields.EMBASE}
                    </label>
                </div>
            </div>
            %{-- Report Criteria & Sections --}%
            <g:render template="includes/alertCriteria"
                      model="[configurationInstance: configurationInstance, action: action, templateList: templateList, productGroupList: productGroupList, dateMap: dateMap]"/>
        </div>
    </div>
</div>
