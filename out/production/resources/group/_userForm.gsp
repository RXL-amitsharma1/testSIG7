<%@ page import="com.rxlogix.enums.GroupType; com.rxlogix.mapping.LmProduct; com.rxlogix.user.Role; com.rxlogix.user.Group;" %>
<script>
    $(document).ready(function () {
        $("#sourceProfiles").select2();
        $("#dateRangeTypes").select2();
    });

</script>
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
        <g:textField  name="name" value="${groupInstance.name}" class="form-control"  maxlength="${gorm.maxLength( clazz: 'com.rxlogix.user.Group', field: 'name' )}"/>
    </div>

    <input type="hidden" name="groupType" value="USER_GROUP">

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
            <li role="presentation" class="active"><a href="#roles" aria-controls="roles" role="tab" data-toggle="tab"
                                                      aria-expanded="true">Roles</a></li>
            <li role="presentation" class=""><a href="#users" aria-controls="users" role="tab" data-toggle="tab"
                                                aria-expanded="false">Users</a></li>
        <input type="hidden" value="${groupRoleList}" name="groupRoleList" id="groupRoleList">
        </ul>



        <!-- Tab panes -->
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="roles">
                <div class="col-md-6 ">
                        <g:each var="role" in="${Role.list().findAll { !("ROLE_DEV".equalsIgnoreCase(it.authority)) }.sort{ a,b-> a.authorityDisplay?.toLowerCase() <=> b.authorityDisplay?.toLowerCase() }}" status="i">
                            <div class="form-group m-0 m-l-10" style="padding-bottom: 8px">
                                <div class="col-md-${column1Width}">
                                    <g:message code="app.role.${role.authority}" default="${role.authority}"/>
                                </div>
                                <div class="col-sm-6">
                                    <input type="hidden" name="${role.authority}">

                                    <div class="bootstrap-switch bootstrap-switch-wrapper bootstrap-switch-off bootstrap-switch-id-${role.authority} bootstrap-switch-small bootstrap-switch-animate"
                                         style="width: 82.6667px;"><div class="bootstrap-switch-container"
                                                                        style="width: 120px; margin-left: -40px;"><span
                                                class="bootstrap-switch-handle-on bootstrap-switch-primary"
                                                style="width: 40px;">Yes</span><span class="bootstrap-switch-label"
                                                                                     style="width: 40px;">&nbsp;</span><span
                                                class="bootstrap-switch-handle-off bootstrap-switch-default"
                                                style="width: 40px;">No</span><input type="checkbox"
                                                                                     name="${role.authority}"
                                                                                     id="${role.authority}">
                                    </div>

                                    </div>
                                </div>
                            </div>
                        </g:each>
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
                        </tbody>
                    </table>
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