<%@ page import="grails.util.Holders;com.rxlogix.config.Tag; com.rxlogix.user.UserRole; com.rxlogix.user.Role; com.rxlogix.user.Group; com.rxlogix.config.SafetyGroup;org.joda.time.DateTimeZone; com.rxlogix.mapping.LmProduct" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                Important Events List
            </label>
            <span id="emerging-dropdown" class="dropdown grid-icon pull-right">
                <span class="dropdown-toggle rxmain-dropdown-settings dropdown-emerging" data-toggle="dropdown"><i class="mdi mdi-format-list-bulleted font-24"></i></span>


                    <ul class="ul-ddm dropdown-menu emerging-export">
                        <li class="li-pin-width"><g:link controller="${EmergingIssue}" action="exportReport" class="m-r-30">
                            <i class="mdi mdi-export "></i> <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip text-no-uppercase">
                                <g:message code="export.to.excel"/>
                            </span>
                        </g:link></li>
                    </ul>
            </span>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-12">
                    <table id="stopListTable" class="row-border hover" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.product.label"/></th>
                            <th><g:message code="app.reportField.eventName"/></th>
                            <th>${Holders.config.importantEvents.ime.label}</th>
                            <th>${Holders.config.importantEvents.dme.label}</th>
                            <th>${Holders.config.importantEvents.stopList.label}</th>
                            <th>${Holders.config.importantEvents.specialMonitoring.label}</th>
                            <th><g:message code="app.important.issue.lastModifiedBy"/></th>
                            <th><g:message code="app.important.issue.lastModified"/></th>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                <th class="pvi-col-xs">Actions</th>
                            </sec:ifAnyGranted>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>