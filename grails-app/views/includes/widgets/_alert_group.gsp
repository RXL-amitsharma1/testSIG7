<g:set var="productAssignmentService" bean="productAssignmentService"/>

<div>
    <g:select id="alertGroup" name="alertGroup"
              from="${productAssignmentService.uniqueAlertGroups() ?: []}"
              optionKey="id" optionValue="name"
              noSelection="['':'']"
              class="form-control select2"/>

</div>

<script>
    $(function () {
        showAlertGroups();
    });

    $('#alertGroup').select2().on('select2:open', function() {
        $('.select2-search__field').attr('maxlength', 100);
    });
</script>