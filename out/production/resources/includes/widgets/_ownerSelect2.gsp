<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.user.User" %>

<label><g:message code="owners.select.to.change" /></label>
<g:select id="owner" name="owner"
          value="${userInstance.id}"
          from="${User.findAllByEnabled(true)}"
          optionKey="id"
          optionValue="fullName"
          class="form-control owner-select col-mid-2"/>


<script>
    $(document).ready(function () {
      /* var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
        showTagWidget(isAdmin);*/
        $(".owner-select").select2();
    });

</script>