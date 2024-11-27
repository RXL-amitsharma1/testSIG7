<!-- Modal -->
<script type="text/javascript">
    $(document).ready(function () {
        addCountBoxToInputField(4000,$('.refBlank'));

        $("#referenceForm").on('change', '#referenceLink', function () {
            $("[name=_action_addReference]").attr('disabled', $(this).val() ? false : true);
        })
    })
</script>

<div id="myReference" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title"><g:message code="signal.workflow.reference.modal.title"/></label>
            </div>
            <g:form id="referenceForm" name="referenceForm"
                    method="post" autocomplete="off">
                <div class="modal-body">
                    <div class="form-group m-t-10" style="padding-bottom:10px">
                        <label><g:message code="signal.workflow.reference.modal.link"/><span
                                class="required-indicator"></span></label>
                        <g:textField class="form-control refBlank" type="input" name="referenceLink" id="referenceLink"
                                     style="width: 85%;float: right;"/>
                    </div>
                    <div class="form-group" style="padding-bottom:10px">
                        <label><g:message code="signal.workflow.description"/></label>
                        <g:textField class="form-control refBlank" type="input" name="description" id='description' value=""
                                     style="width: 85%;float: right;"/>
                    </div>
                </div>

                <div class="modal-footer">
                    <div class="text-right">

                        <g:actionSubmit class="btn btn-primary" action="addReference"
                                        type="submit" id="saveReference"
                                        value="${message(code: "default.button.save.label")}" disabled="true"/>
                        <button data-dismiss="modal"
                                class="btn btn-default">${message(code: "default.button.cancel.label")}</button>

                    </div>
                </div>
            </g:form>
        </div>
    </div>
</div>


