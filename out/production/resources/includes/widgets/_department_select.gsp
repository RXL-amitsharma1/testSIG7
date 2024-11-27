<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.user.UserDepartment" %>

<div>
    <g:select id="department" name="department"
              from="${UserDepartment.findAll()}"
              value="${domainInstance?.userDepartments?.departmentName}"
              optionKey="departmentName"
              optionValue="departmentName"
              multiple="multiple"
              class="form-control select2"/>
</div>

<script>
    $(document).ready(function () {
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
        showDepartmentWidget(isAdmin);
    });

    $('#department').select2().on('select2:open', function() {
        $('.select2-search__field').attr('maxlength', 100);
    });
</script>