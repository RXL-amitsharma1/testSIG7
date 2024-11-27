<%@ page import="com.rxlogix.Constants" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click" >
                <g:message code="app.signal.data.source"/>
            </label>
        </div>
        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-3">
                    <div class="form-group">
                        <select id="selectedDatasource" name="selectedDatasource" class="form-control selectedDatasource" multiple>
                            <g:if test="${action==com.rxlogix.Constants.AlertActions.CREATE}">
                                <g:each in="${dataSourceMap.entrySet()}" var="dataSource">
                                    <g:if test="${dataSource.key == configurationInstance.selectedDatasource}">
                                    <option value="${dataSource.key}" selected>${dataSource.value}</option>
                                    </g:if>
                                    <g:elseif test="${listOfSelectedDataSource?.contains(dataSource.key)}">
                                       <option value="${dataSource.key}" selected ${dataSource.key in enabledOptions?"":"disabled"}>${dataSource.value}</option>
                                    </g:elseif>
                                    <g:elseif test="${dataSource.key == defaultSelected && !configurationInstance.selectedDatasource}">
                                        <option value="${dataSource.key}" selected>${dataSource.value}</option>
                                    </g:elseif>
                                    <g:else>
                                        <option value="${dataSource.key}" ${dataSource.key in enabledOptions?"":"disabled"}>${dataSource.value}</option>
                                    </g:else>
                                </g:each>
                            </g:if>
                            <g:else>
                                <g:set var="selectedDataSources" id="selectedDataSources" value="${configurationInstance.selectedDatasource.split(',')}"/>
                                <g:each in="${dataSourceMap.entrySet()}" var="dataSource">
                                    <g:if test="${selectedDataSources.contains(dataSource.key)}">
                                        <option value="${dataSource.key}" selected ${dataSource.key in enabledOptions?"":"disabled"}>${dataSource.value}</option>
                                    </g:if>
                                    <g:else>
                                        <option value="${dataSource.key}" ${dataSource.key in enabledOptions?"":"disabled"}>${dataSource.value}</option>
                                    </g:else>
                                </g:each>
                            </g:else>
                        </select>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>