<%@ page import="com.rxlogix.enums.GroupType; com.rxlogix.mapping.LmProduct; com.rxlogix.user.Role; com.rxlogix.user.Group;" %>
<asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>
<g:if test="${actionName == 'create' || actionName == 'save'}">
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update'}">
    <g:set var="editMode" value="${true}"/>
</g:if>
<div class="col-md-3">
    <label class="col-md-8" for="name">
        <g:message code="com.rxlogix.group.name.label"/>
        <span class="required-indicator">*</span>
    </label>

    <div class="col-md-12">
        <g:textField name="name" id="name"  value="${groupInstance.name}" class="form-control" maxlength="${gorm.maxLength( clazz: 'com.rxlogix.user.Group', field: 'name' )}"/>
    </div>
    <input type="hidden" name="groupType" value="WORKFLOW_GROUP">

    <div class="col-md-12">
        <label class="radio-inline">
            <g:radio name="forceJustification" value="true"
                     checked="${groupInstance ? groupInstance.forceJustification : true}"/>
            <g:message code="group.force.justification"/>
        </label>
        <label class="radio-inline">
            <g:radio name="forceJustification" value="false"
                     checked="${groupInstance ? !groupInstance.forceJustification : false}"/>
            No Justification
        </label>
    </div>

</div>


<div class="col-md-3">

    <label class="col-md-8" for="description">
        <g:message code="group.description.label"/>
    </label>

    <div class="col-md-12">
        <g:textArea name="description" value="${groupInstance?.description}"
                    rows="5" cols="40"
                    maxlength="${gorm.maxLength( clazz: 'com.rxlogix.user.Group', field: 'description' )}"
                    placeholder="${message( code: 'group.description.label' )}" class="form-control groupTextarea"/>
    </div>

</div>


<div class="col-md-12 m-t-40 justificationSelection">

    <div style="margin-top: 20px">

        <!-- Nav tabs -->
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#disposition" aria-controls="roles" role="tab"
                                                      data-toggle="tab" aria-expanded="true">Dispositions</a></li>
            <li role="presentation" class=""><a href="#users" aria-controls="users" role="tab" data-toggle="tab"
                                                aria-expanded="false">Users</a></li>
        </ul>
        <!-- Tab panes -->
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="disposition">
                <div class="row">
                    <div class="col-md-3" style="padding-right: 30px !important;">
                        <label class="col-md-8 m-t-15 justificationSelection" for="defaultQuantDisposition">
                            <g:message code="com.rxlogix.group.default.quant.disposition.type.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="col-md-12 justificationSelection">
                            <g:select name="defaultQuantDisposition.id" id="defaultQuantDisposition"
                                      class="form-control threshold-not-met select2 commonSelect2Class"
                                      from="${defaultDispositionList}" optionValue="displayName"
                                      noSelection="[ '': '-Select One-' ]" optionKey="id"
                                      value="${groupInstance?.defaultQuantDisposition?.id}"/>
                        </div>

                        <label class="col-md-8 m-t-15 justificationSelection" for="defaultEvdasDisposition">
                            <g:message code="com.rxlogix.group.default.evdas.disposition.type.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="col-md-12 justificationSelection">
                            <g:select name="defaultEvdasDisposition.id" id="defaultEvdasDisposition"
                                      class="form-control threshold-not-met select2 commonSelect2Class"
                                      from="${defaultDispositionList}" optionValue="displayName"
                                      noSelection="[ '': '-Select One-' ]" optionKey="id"
                                      value="${groupInstance?.defaultEvdasDisposition?.id}"/>
                        </div>

                        <label class="col-md-8 m-t-15 justificationSelection" for="defaultLitDisposition">
                            <g:message code="com.rxlogix.group.default.lit.disposition.type.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="col-md-12 justificationSelection">
                            <g:select name="defaultLitDisposition.id" id="defaultLitDisposition"
                                      class="form-control  requires-review select2 commonSelect2Class"
                                      from="${defaultDispositionList}" optionValue="displayName"
                                      noSelection="[ '': '-Select One-' ]" optionKey="id"
                                      value="${groupInstance?.defaultLitDisposition?.id}"/>
                        </div>

                        <label class="col-md-8 m-t-15 justificationSelection" for="defaultSignalDisposition">
                            <g:message code="com.rxlogix.group.signal.disposition.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="col-md-12 justificationSelection">
                            <g:select name="defaultSignalDisposition.id" id="defaultSignalDisposition"
                                      class="form-control select2 commonSelect2Class"
                                      from="${defaultSignalDispositionList}" optionValue="displayName"
                                      noSelection="[ '': '-Select One-' ]" optionKey="id"
                                      value="${groupInstance?.defaultSignalDisposition?.id}"/>
                        </div>

                        <label class="col-md-8 m-t-20 justificationSelection" for="defaultAdhocDisposition">
                            <g:message code="com.rxlogix.group.default.adhoc.disposition.type.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="col-md-12 justificationSelection">
                            <g:select name="defaultAdhocDisposition.id" id="defaultAdhocDisposition"
                                      class="form-control  requires-review select2 commonSelect2Class"
                                      from="${defaultDispositionList}" optionValue="displayName"
                                      noSelection="[ '': '-Select One-'  ]" optionKey="id"
                                      value="${groupInstance?.defaultAdhocDisposition?.id}"/>
                        </div>

                    </div>

                    <div class="col-md-3 justificationSelection" style="padding-right: 30px !important;">

                        <label class="col-md-8 m-t-20 justificationSelection" for="defaultQualiDisposition">
                            <g:message code="com.rxlogix.group.default.quali.disposition.type.label"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="col-md-12 justificationSelection">
                            <g:select name="defaultQualiDisposition.id" id="defaultQualiDisposition"
                                      class="form-control  requires-review select2 commonSelect2Class"
                                      from="${defaultDispositionList}" optionValue="displayName"
                                      noSelection="[ '': '-Select One-' ]" optionKey="id"
                                      value="${groupInstance?.defaultQualiDisposition?.id}"/>
                        </div>

                        <label class="col-md-8 m-t-15" for="autoRouteDisposition">
                            <g:message code="com.rxlogix.group.auto.route.disposition.label"/>
                        </label>

                        <div class="col-md-12">
                            <g:select name="autoRouteDisposition.id" id="autoRouteDisposition" class="form-control select2 commonSelect2Class"
                                      from="${defaultDispositionList}" optionValue="displayName"
                                      noSelection="[ '': '-Select One-' ]" optionKey="id"
                                      value="${groupInstance?.autoRouteDisposition?.id}"
                                      disabled="disabled"/>
                        </div>

                        <label class="col-md-8 m-t-15" for="justificationText">
                            <g:message code="com.rxlogix.group.justification.label"/>
                        </label>

                        <div class="col-md-12">
                            <g:textArea name="justificationText" rows="5" cols="40"
                                        maxlength="${gorm.maxLength( clazz: 'com.rxlogix.user.Group', field: 'justificationText' )}"
                                        id="justification" class="form-control groupTextarea"
                                        value="${groupInstance?.justificationText}"
                                        disabled=""/>
                            <small class="text-muted">Max: 4000 characters</small>
                        </div>
                    </div>

                    <div class="col-md-3 justificationSelection">

                        <label class="col-md-8 m-t-20" for="alertLevelDisposition">
                            <g:message code="com.rxlogix.group.alert.level.disposition.label"/>
                        </label>

                        <g:hiddenField name="dispositionValue" id="dispositionValue"
                                       value="${groupInstance?.alertDispositions?.id}"/>

                        <div class="col-md-12">
                            <g:select name="alertLevelDispositions" id="alertLevelDisposition" class="form-control select2 commonSelect2Class"
                                      from="${alertLevelDispositionList}"
                                      optionValue="displayName" optionKey="id"
                                      multiple="true"/>
                        </div>
                    </div>

                </div>
            </div>

            <div role="tabpanel" class="tab-pane" id="users">
                <div style="width: 100%">
                    <table class="table userTable">
                        <tbody><tr class="userTableHeader">
                            <th>Full Name</th>
                        </tr>

                        <g:each var="user" in="${groupUsersList}">
                            <tr>
                                <td>${user} <input type="hidden" name="selectedUsers" value="${user.id}">
                                </td>
                            </tr>
                        </g:each>


                        </tbody></table>
                </div>
                <a href="javascript:void(0)" class="btn btn-primary add-remove-user"
                   onclick="$('#addRemoveUserModal').modal('show')">
                    <span class=" icon-white"></span><g:message code="userGroup.add.remove.users.label"/></a>
            </div>
        </div>

    </div>

</div>


<div class="row">
    <div class="col-md-12 m-t-15">
        <g:hiddenField name="savedProductsList" value="${allowedProductsList?.join( "," )}" id="savedProductsList"/>
    </div>
</div>
<script>
    window.onload = function () {
        setTimeout(function () {
            signal.group_utils.loadAutoRouteValues($('#defaultQualiDisposition').val())
        }, 200);
    };
</script>
<style>

.nav.nav-tabs {
    border-bottom: 1px solid #ddd !important;
    box-shadow: none !important;
    background-image: none !important;
}
.nav.nav-tabs > li.active > a {
    border: 1px solid #ddd !important;
    border-bottom-color: transparent !important;
    border-radius: 4px 4px 0 0 !important;
    margin-left: 20px !important;
    color: black !important;
    font-weight: 700 !important;
    margin-bottom: -2px!important;
}
.nav.nav-tabs > li.active > a {
    background-color: #fff !important;
    border: 0 !important;
}
.nav {
    padding-left: 0 !important;
    margin-bottom: 0 !important;
    list-style: none !important;
}
.nav.nav-tabs > li > a {
     background-color: transparent !important;
    border-radius: 0 !important;
    border: none !important;
    color: #333 !important;
    cursor: pointer !important;
    line-height: 50px !important;
    font-weight: 500 !important;
    padding-left: 20px !important;
    padding-right: 20px !important;
}
.nav>li>a {
    position: relative !important;
    display: block !important;
}
.nav.nav-tabs > li.active > a {
    border: 1px solid #ddd !important;
    border-bottom-color: transparent !important;
    border-radius: 4px 4px 0 0 !important;
    margin-left: 20px !important;
    color: black !important;
    font-weight: 700 !important;
    margin-bottom: -2px !important;
}
</style>
