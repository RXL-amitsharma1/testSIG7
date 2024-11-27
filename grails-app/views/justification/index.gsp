<%@ page import="com.rxlogix.enums.JustificationFeatureEnum" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Justification</title>
    <g:javascript>
       var justificationListUrl = "${createLink(controller: 'justification', action: 'list')}";
       var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
    </g:javascript>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/justification/justification.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
</head>

<body>
<g:hiddenField name="deleteJustificationUrl" value="${createLink(controller: 'justification', action: 'delete')}"/>
<g:hiddenField name="editJustificationUrl" value="${createLink(controller: 'justification', action: 'edit')}"/>
<g:render template="/includes/layout/flashErrorsDivs" bean="${justification}" var="theInstance"/>
<div class="messageContainer"></div>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <g:render template="includes/showList" model="[justification: justification, 'mode': 'create']"/>
    </div>
</div>

<!-- Large modal -->
<div class="modal modal-justification fade bs-example-modal-lg" tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <label class="rxmain-container-header-label click" id="justificationModalHeader"><g:message code="app.label.justification.create"
                                                                              default="Create Justification"/></label>
            </div>

            <div class="modal-body">
                <div class="msgContainer" style="display:none">
                    <div class="alert alert-danger" role="alert">
                        <span class="message"></span>
                    </div>
                </div>

                <div class="rxmain-container-content">
                    <div class="row">
                        <g:render template="includes/form" model="[justification: justification, 'mode': 'create', justificationFeatureEnumList : JustificationFeatureEnum.all,dispositions: dispositions]"/>
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>