<%@ page import="com.rxlogix.config.Tag; com.rxlogix.user.UserRole; com.rxlogix.user.Role; com.rxlogix.user.Group; com.rxlogix.config.SafetyGroup;org.joda.time.DateTimeZone; com.rxlogix.mapping.LmProduct"%>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click" >
                <g:message code="app.label.add.alert.stop.list"/>
            </label>
        </div>
        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-12">
                    <table id="stopListTable" class="row-border hover" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.reportField.productProductName" /> </th>
                            <th><g:message code="app.reportField.eventName" /> </th>
                            <th><g:message code="app.date.added" /></th>
                            <th><g:message code="app.date.deactivated" /></th>
                            <th><g:message code="app.action.list.status.label" /></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>