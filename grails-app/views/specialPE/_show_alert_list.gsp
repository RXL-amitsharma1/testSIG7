<%@ page import="com.rxlogix.config.Tag; com.rxlogix.user.UserRole; com.rxlogix.user.Role; com.rxlogix.user.Group; com.rxlogix.config.SafetyGroup;org.joda.time.DateTimeZone; com.rxlogix.mapping.LmProduct" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                <g:message code="app.label.show.special.product.event"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-12">
                    <table id="specialPETable" class="row-border hover" width="100%">
                        <thead>
                        <tr>
                            <th>Product name</th>
                            <th>Event Name</th>
                            <th>Last Modified</th>
                            <th>Last Modified By</th>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                <th>Actions</th>
                            </sec:ifAnyGranted>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>