<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils;com.rxlogix.user.Group; com.rxlogix.enums.GroupType; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.Constants;" %>
<style>
#select2-selectValue1-results>.select2-results__option[aria-selected=true] {
    display: none;
}
</style>
<g:render template="/includes/copyPasteModalQuery" />
<div class="panel panel-default rxmain-container rxmain-container-top" id="advacedOptionSection">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            <a data-toggle="collapse" data-parent="#accordion-pvs-form" href="#pvsAdvancedOptions" aria-expanded="true" class="">
                <g:message code="app.label.advanced.options"/>
            </a>
        </h4>
    </div>
    <div id="pvsAdvancedOptions" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true">
        <div class="row">
            <div class="col-xs-6">
                <div class="row">
                    <div class="col-md-12">
                        <label><g:message code="app.label.data.mining.variables1"/></label>
                        <span class="required-indicator">*</span>
                        <span class="glyphicon glyphicon-info-sign themecolor modal-link" style="cursor:pointer;"
                              data-toggle="modal"
                              data-target="#advancedOptionsHelpModal" style="cursor: pointer;">
                        </span>
                    </div>
                </div>

                <div class="row">
                    <div class="form-group col-xs-7  ${hasErrors(bean: configurationInstance, field: 'priority', 'has-error')}">
                        <g:select class="form-control expressionField" id="dataMiningVariable" name="dataMiningVariable"
                                  value="${configurationInstance?.dataMiningVariable}"
                                  noSelection="['null': message(code: 'select.one')]" optionValue="value" from="${[]}"/>
                        <input type="hidden" id="dataMiningVariableValue" name="dataMiningVariableValue" value="${configurationInstance?.dataMiningVariableValue}">
                        <input type="hidden" id="dmvDataLevel">
                        <input type="hidden" id="dmvDataValidatable">
                        <input type="hidden" id="dmvOpened" value="false">

                    </div>
                </div>
                <div class="row" id="dataMiningVariableString">
                </div>
                <div class="row" style="padding-bottom:10px;">
                    <span class="text-secondary" style="font-style: italic;">
                        <g:message code="app.label.data.mining.variables2"/>
                    </span>
                </div>
                <div class="row m-l-0">
                    <div class="checkbox checkbox-primary checkbox-not-allowed">
                        <g:checkBox id="isProductMining"
                                    name="isProductMining"
                                    value="${configurationInstance?.isProductMining}"
                                    checked="${configurationInstance?.isProductMining}"/>
                        <label for="applyAlertStopList">
                            <g:message code="advanced.option.background.product"/>
                        </label>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade advancedOptionsHelpModal" id="advancedOptionsHelpModal" tabindex="-1" role="dialog"
     aria-labelledby="Advanced Options Help">
    <div class="modal-dialog modal-lg " role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <span><b><g:message code="app.advanced.options.help.title"/></b></span>
            </div>
            <div class="modal-body container-fluid border">
                <div class="table-bordered">
                    <div><g:message code="app.advanced.options.help.text1"/></div><br>
                    <ul class="advancedOptionsModal">
                        <li><div><g:message code="app.advanced.options.help.text2"/></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text3"/></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text4"/></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text5"/></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text6"/><i
                                class="fa fa-pencil-square-o copy-n-paste help-dmv" style="cursor: no-drop !important;"></i></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text7"/><i
                                class="fa fa-search help-dmv" style="cursor: no-drop !important;"></i></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text8"/></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text9"/></div><br></li>
                        <li><div><g:message code="app.advanced.options.help.text10"/></div><br></li>
                    </ul>
                </div>


            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade dataSheetOptionsHelpModal" id="dataSheetOptionsHelpModal" tabindex="-1" role="dialog" aria-labelledby="Datasheet Selection help">
    <div class="modal-dialog modal-lg " role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <span><b><g:message code="app.datasheet.help.title.text"/></b></span>
            </div>
            <div class="modal-body container-fluid border">
                <div class="">
                    <ul class="dataSheetAdvancedModal table-bordered">
                        <li><div> <g:message code="app.datasheet.help.modal.text2" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text3" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text4" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text5" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text6" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text7" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text8" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text9" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text10" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text11" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text12" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text13" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text14" /></div><br></li>
                        <li><div> <g:message code="app.datasheet.help.modal.text15" /></div><br></li>
                    </ul>
                </div>


            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
            </div>
        </div>
    </div>
</div>
<asset:javascript src="app/pvs/advanceOption.js"/>
<g:javascript>
    $(document).on('click', '#saveRun', function (event) {
        updateDmvVariable();
    });
</g:javascript>