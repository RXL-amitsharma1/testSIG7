<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.user.User" %>

<g:javascript>
        var listReportsUrl = "${createLink(controller: 'ownershipRest', action: 'listReports')}"
        var listUsersUrl = "${createLink(controller: 'usersRest', action: 'listUsers')}"
        var previous = "${userInstance.id}"
        var previousName = "${userInstance.fullName}"
</g:javascript>
<g:form>
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                <g:message code="app.ownership.label"/></h3>

        </div>
    </div>

    <div class="row">

        <div class="col-md-4 form-group">
            <label><g:message code="owners.select.to.change"/></label>
            <g:select id="owner" name="owner"
                      value="${userInstance.id}"
                      from="${User.findAllByEnabled(true)}"
                      optionKey="id"
                      optionValue="fullName"
                      class="form-control owner-select"
                      disabled="${!SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}"/>
        </div>

    </div>

    <div class="row">
        <div class="col-md-4">
            <div class="panel-group">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a data-toggle="collapse" href='#collapse1'><g:message code="ownership.reports.label"/><span
                                    class="caret"></span></a>
                        </h4>
                    </div>

                    <div id='collapse1' data-id='${userInstance.id}' class='panel-collapse collapse'></div>
                </div>
            </div>
        </div>

    </div>

    <div class="modal fade" id="changeOwnerModal" tabindex="-1" role="dialog" aria-labelledby="changeOwnerModalLabel"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="changeOwnerModalLabel">Change Owner</h4>
                </div>

                <div class="modal-body">
                    <div id="nameToChange"><g:message code="ownership.change.alert"/>?</div>

                    <p></p>

                    <div class="from" style="font-weight:bold;"><g:message code="currently.owned.by"/>: <span id="previousName"
                            class="previousFullName"></span></div>

                    <div class="to" style="font-weight:bold;"><g:message code="change.owner.to"/>: <span
                            class="currentFullName"></span></div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                            code="default.button.cancel.label"/></button>
                    <button id="ownerChangeButton" class="btn  btn-primary">
                        <span class="glyphicon glyphicon-trash icon-white"></span>
                        ${message(code: 'default.button.changeowner.label', default: 'Change owner')}
                    </button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
</g:form>


<asset:javascript type="text/jsx" src="app/ownership.js"/>


