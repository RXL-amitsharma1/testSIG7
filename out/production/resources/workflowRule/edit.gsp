<%@ page import="com.rxlogix.config.WorkflowRule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="workflowRule.edit" default="Edit WorkflowRule"/></title>
    <g:javascript>
        var fetchWarningMessageUrl = "${createLink(controller: 'workflowRule', action: 'disableCheck')}"
    </g:javascript>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <script>
    $(document).ready(function () {
        $('.disable-check').unbind().on('click', function (evt) {
            evt.preventDefault();
            var action = $(this).data('action');
            var data = {'id': $('#id').val()};
            var disable = false
            if (action == 'delete') {
                disable = true
            } else if (action == 'update') {
                if (!$('#displayPVSState').prop('checked')) {
                    disable = true
                }
            }

            if (disable) {
                $.ajax({
                    url: fetchWarningMessageUrl,
                    data: data,
                    success: function (response) {
                        var message = "";
                        var warning = true;
                        if (!response.alertsIncomeState) {
                            if (response.rulesIncomeState && response.rulesTargetState) {
                                message = "There are no PEC's or Cases associated with the Workflow States, and there are other Workflow Rules available for the same Incoming or Target State. Do you want to continue?"
                            } else if (!response.rulesIncomeState && !response.rulesTargetState) {
                                message = "There are no PEC's or Cases associated with the Workflow States, and there are no other Workflow Rules available for the same Incoming or Target State. Do you want to continue?"
                            } else if (!response.rulesIncomeState) {
                                message = "There are no PEC's or Cases associated with the Workflow States, and there are no other Workflow Rules available for the same Incoming State. Do you want to continue?"
                            } else {
                                message = "There are no PEC's or Cases associated with the Workflow States, and there are no other Workflow Rules available for the same Target State. Do you want to continue?"
                            }
                        } else {
                            if (!response.rulesIncomeState && !response.rulesTargetState) {
                                warning = false;
                                message = "Workflow Rule can't be disabled as there are PEC's or Cases associated with the Workflow States, and there are no other Workflow Rules available for the same Incoming or Target State."
                            } else if (response.rulesIncomeState && response.rulesTargetState) {
                                message = "There are PEC's or Cases associated with the Workflow States, and there are other Workflow Rules available for the same Incoming or Target State. Do you want to continue?"
                            } else if (!response.rulesIncomeState) {
                                warning = false;
                                message = "Workflow Rule can't be deleted as there are PEC's or Cases associated with the Workflow States, and there are no other Workflow Rules available for the same Incoming State"
                            } else {
                                warning = false;
                                message = "Workflow Rule can't be deleted as there are PEC's or Cases associated with the Workflow States, and there are no other Workflow Rules available for the same Target State"
                            }
                        }

                        bootbox.hideAll();
                        if (warning) {
                            bootbox.confirm({
                                message: message,
                                buttons: {
                                    confirm: {
                                        label: 'Ok',
                                        className: 'btn-primary'
                                    },
                                    cancel: {
                                        label: 'Cancel',
                                        className: 'btn-default'
                                    }
                                },
                                callback: function (result) {
                                    if (result) {
                                        //submit the form
                                        $('#workflowRuleForm').attr('action', action);
                                        $('#workflowRuleForm').submit();
                                    }
                                }
                            });
                        } else {
                            bootbox.alert({
                                message: message,
                                callback: function (result) {
                                    //does not change
                                }
                            });
                        }

                    }
                })
            } else {
                $('#workflowRuleForm').attr('action', action);
                $('#workflowRuleForm').submit();
            }
        })
    })
    </script>
</head>
<rx:container title="Edit Workflow Rule">
    <g:if test="${flash.error}">
        <div class="alert alert-danger alert-dismissible" role="alert" style="word-break: break-all">
            <button type="button" class="close" data-dismiss="alert">
                <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
                <span class="sr-only"><g:message code="default.button.close.label" /></span>
            </button>
            <g:if test="${flash.error}">
                <g:if test="${flash.error.contains('<linkQuery>')}">
                    ${flash.error.substring(0, flash.error.indexOf('<linkQuery>'))}
                    <a href="${flash.error.substring(flash.error.indexOf('<linkQuery>') + 11)}"><g:message code="see.details" /></a>
                </g:if>
                <g:else>
                    <g:if test="${flash.error instanceof List}">
                        <g:each in="${flash.error}" var="field">
                            ${raw(field)}<br>
                        </g:each>
                    </g:if>
                    <g:else>
                        ${raw(flash.error)}<br>
                    </g:else>
                </g:else>
            </g:if>
        </div>
    </g:if>
    <div class="nav bord-bt">
        <span class="menuButton"><g:link class="list btn btn-primary" action="index"><g:message code="workflowRule.list"
                                                                                               default="WorkflowRule List"/></g:link></span>
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="workflowRule.create.new" default="New WorkflowRule"/></g:link></span>
        </sec:ifAnyGranted>
    </div>

    <div class="body">
        <g:form method="post" name="workflowRuleForm" >
            <g:render template="form"
                      model="[workflowRuleInstance: workflowRuleInstance, edit: true, pvsStateList: pvsStateList]"/>
        </g:form>
    </div>
</rx:container>
</html>
