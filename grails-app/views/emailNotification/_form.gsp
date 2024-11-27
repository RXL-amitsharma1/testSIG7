<%@ page import="grails.util.Holders" %>
<asset:javascript src="/app/pvs/emailNotification/email_notification.js" />

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<div class="row">

    %{--Email Notification Modules--}%
    <div class="col-md-10">
    <g:each var="key" in="${keys}">
        <g:if test="${modules.get(key)}">
            <div class="form-group m-0 m-l-10" style="padding-bottom: 8px" name="${key}" id="${key}">
                <div class="col-md-${column1Width}">
                    ${modules.get(key)?.moduleName.replaceAll("PVA" , Holders.config.signal.dataSource.safety.name).replace("Quantitative" , "Aggregate Review").replace("Qualitative" , "Individual Case Review")}
                </div>

                <div class="col-md-7">
                    <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                        <div class="col-md-${column1Width}">
                            <label class="pv-switch">
                                <input id="${key}"
                                       name="${key}"
                                       type="checkbox"
                                       class="form-element"
                                       data-value="${modules.get(key).isEnabled}"
                                />
                                <span class="switch-slider"></span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </g:if>
    </g:each>
</div>
</div>
