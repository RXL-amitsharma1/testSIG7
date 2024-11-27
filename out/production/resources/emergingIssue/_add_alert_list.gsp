<%@ page import="grails.util.Holders; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                Important Events
            </label>
        </div>

        <div class="rxmain-container-content">
            <g:form name="alertStopListForm" method="post" autocomplete="off" action="${callingScreen == 'index' ? "save" : "update"}" id="${emergingIusseList?.id}">
                <div class="row">
                    <div class="col-md-3 form-group">
                        <label>
                            <g:message code="app.label.productSelection"/>
                        </label>

                        <div class="wrapper">
                            <g:hiddenField name="productSelectionAssessment" value="${emergingIusseList?.productSelection}"/>
                            <g:hiddenField name="productGroupSelectionAssessment" value="${emergingIusseList?.productGroupSelection}"/>
                            <g:hiddenField name="isAssessmentDicitionary" id="isAssessmentDicitionary" value="true"/>
                            <div id="showProductSelectionAssessment" class="showDictionarySelection"></div>

                            <div class="iconSearch">
                                <a tabindex="0" id="searchProductsAssessment" data-toggle="modal"
                                   data-target="#productModalAssessment" class="productRadio">
                                    <i class="fa fa-search"></i></a>
                            </div>
                        </div>

                    </div>
                    <div class="col-md-3 form-group">
                        <label><g:message code="app.label.eventSelection"/><span class="required-indicator">*</span></label>

                        <div class="wrapper">
                            <div id="showEventSelection" class="showDictionarySelection"></div>

                            <div class="iconSearch">
                                <a id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0"
                                   data-toggle="tooltip" title="Select event" accesskey="}"><i class="fa fa-search"></i></a>
                            </div>
                        </div>
                        <g:textField class="eventSelected" name="eventSelection" value="${emergingIusseList?.eventName}"
                                     hidden="hidden"/>
                        <g:textField class="eventGroupSelected" name="eventGroupSelection" value="${emergingIusseList?.eventGroupSelection}"
                                     hidden="hidden"/>
                    </div>

                    <div class="col-md-6 row display-flex">
                        <div class="col-md-2 col-md-offset-1 text-right-prop">
                            <label for="ime">${Holders.config.importantEvents.ime.label}</label>
                            <g:checkBox name="ime" id="ime" value="${emergingIusseList?.ime}"/>
                        </div>

                        <div class="col-md-2 text-right-prop">
                            <label for="dme">${Holders.config.importantEvents.dme.label}</label>
                            <g:checkBox name="dme" id="dme" value="${emergingIusseList?.dme}"/>
                        </div>

                        <div class="col-md-2 text-right-prop">
                            <label for="emergingIssue">${Holders.config.importantEvents.stopList.label}</label>
                            <g:checkBox name="emergingIssue" value="${emergingIusseList?.emergingIssue}"/>
                        </div>

                        <div class="col-md-3">
                            <label for="specialMonitoring">${Holders.config.importantEvents.specialMonitoring.label}</label>
                            <g:checkBox name="specialMonitoring" value="${emergingIusseList?.specialMonitoring}"/>
                        </div>
                    </div>
                </div>
                <input type="hidden" name="dataSourceDict" id="productDictDataSource" value="${emergingIusseList?.dataSourceDict?:'pva'}"/>
                <input type="hidden" name="isMultiIngredient" id="isMultiIngredientAssessment" value="${emergingIusseList?.isMultiIngredient}">

                <div class="m-t-10">
                    <g:if test="${callingScreen == 'index'}">
                        <g:submitButton name="stopListSubmit" class="btn btn-primary addButton save-emerging" type="submit"
                                        value="${message(code: 'default.button.add.label')}"/>
                    </g:if>
                    <g:else>
                        <g:submitButton name="stopListSubmit" class="btn btn-primary addButton save-emerging" type="submit"
                                        value="${message(code: 'default.button.update.label')}"/>
                    </g:else>
                </div>
            </g:form>
        </div>
    </div>
</div>
<style>
</style>
<g:render template="/emergingIssue/emerging_product_selection_modal" model="[isPVCM: isPVCM]"/>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal"/>
</g:else>