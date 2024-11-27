<asset:javascript src="app/pvs/disposition/disposition.js"/>
<asset:javascript src="pickr/pickr.min.js"/>
<asset:stylesheet src="pickr.min.css"/>
<g:javascript>
            var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
</g:javascript>
<style>
.tooltip-inner {
    width: 400px;
}
</style>
<g:if test="${edit}">
    <g:hiddenField name="id" value="${dispositionInstance?.id}" id="instanceId"/>
    <g:hiddenField name="version" value="${dispositionInstance?.version}"/>
</g:if>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="value"><g:message code="label.value" default="Value"/></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="value" value="${dispositionInstance.value}"
                     class="form-control" maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Disposition', field: 'value')}"/>
    </div>

    <div class="col-lg-3">
        <label for="displayName"><g:message code="label.displayName" default="Display Name"/></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="displayName" value="${dispositionInstance.displayName}"
                     class="form-control" maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Disposition', field: 'displayName')}"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-7">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="display" id="displayDisposition" value="${dispositionInstance?.display}" class=""/>
            <label for="displayDisposition"><g:message code="label.display" default="Display"/></label>
        </div>

        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="validatedConfirmed" value="${dispositionInstance?.validatedConfirmed}"
                        class="checkboxAlert" id="validatedConfirmed"/>
            <label for="validatedConfirmed"><g:message code="label.validated.confirmed"
                                                       default="Validated Confirmed"/></label>
        </div>

        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="closed" value="${dispositionInstance?.closed}" class="checkboxAlert" id="closed"/>
            <label for="closed"><g:message code="label.closed" default="Closed"/></label>
        </div>

        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="reviewCompleted" value="${dispositionInstance?.reviewCompleted}" class="checkboxAlert"
                        id="reviewCompleted"/>
            <label for="closed"><g:message code="label.reviewCompleted" default="Review Completed"/></label>
        </div>
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="resetReviewProcess" id="resetReviewProcess" value="${dispositionInstance?.resetReviewProcess}" class="checkboxAlert"/>
            <label for="resetReviewProcess"><g:message code="label.resetReviewProcess" default="Reset Review Process"/><span><a tabindex="0" data-toggle="tooltip" data-container="body" title="<g:message code='app.tooltip.resetReviewProcess'/>"  data-placement="bottom" class="editAlertTags btn-edit-tag mid"><i class="glyphicon glyphicon-info-sign theme-color"></i></a></span></label>
        </div>

    </div>
</div>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="abbreviation"><g:message code="label.abbreviation" default="Abbreviation"/></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="abbreviation" value="${dispositionInstance.abbreviation}"
                     class="form-control" maxlength="3"/>
    </div>

    <div class="col-lg-1">
        <label for="color-picker"><g:message code="label.color" default="Color"/></label>
        <label><span class="required-indicator">*</span></label>
        <a href="javascript:void(0)" name="color-picker" class="color-picker"></a>
        <g:hiddenField name="colorCode" value="${dispositionInstance.colorCode ?: '#000'}"/>
    </div>

    <div class="col-lg-1">
        <label for="preview"><g:message code="app.label.preview" default="Preview"/></label>

        <div class="box">
            <div id="preview" class="box-inline ico-circle" style="display: none;"></div>
        </div>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="label.description" default="Description"/></label>
        <g:textField name="description" value="${dispositionInstance.description}"
                     class="form-control disposition-description" maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Disposition', field: 'description')}"/>
    </div>

</div>

<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
    <div class="row form-group">
        <div class="col-lg-5">
            <g:if test="${edit}">
                <span class="button"><g:actionSubmit class="save btn btn-primary" action="update"
                                                     value="${message(code: 'default.button.update.label', 'default': 'Update')}"/></span>
                <span class="button"><g:actionSubmit class="save btn pv-btn-grey hide" action="delete"
                                                     value="${message(code: 'default.button.delete.label', 'default': 'Delete')}"/></span>
            </g:if>
            <g:else>
                <span class="button"><g:submitButton name="create" class="save btn btn-primary"
                                                     value="${message(code: 'default.button.create.label', 'default': 'Create')}"/></span>
            </g:else>
        </div>
    </div>
</sec:ifAnyGranted>
<g:render template="/includes/modals/dispositionClosedConfirmation"/>
<script>
    var colorCode = "${fieldValue(bean: dispositionInstance, field: 'colorCode') ?: '#000'}";
    $(document).ready(function () {
        Pickr.create({
            el: '.color-picker',
            swatches: ['#FDBB40', '#5cb85c', '#d9534f', '#337ab7', '#7b7f8e'],
            default: colorCode,
            position: 'middle',
            components: {
                // Main components
                preview: true,
                opacity: false,
                hue: true,
                closeWithKey: 'Escape',
                // Input / output Options
                interaction: {
                    hex: false,
                    rgba: false,
                    hsla: false,
                    hsva: false,
                    cmyk: false,
                    input: true,
                    clear: false,
                    save: true
                }
            },
            onSave(hsva, instance) {
                $('#colorCode').val(hsva.toHEX().toString());
                updatePreview();
            }
        });
        $('#abbreviation').on('change, keyup', function () {
            updatePreview();
        });
        updatePreview();
    });

    var updatePreview = function () {
        $('#preview').html($('#abbreviation').val()).attr('style', 'background:' + $('#colorCode').val());
        if($('#abbreviation').val() == "") {
            $('#preview').hide();
        } else {
            $('#preview').show();
        }
    };
</script>
