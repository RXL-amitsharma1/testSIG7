<%@ page import="grails.util.Holders; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click" >
                <g:message code="app.create.product.group"/>
            </label>
        </div>
        <div class="rxmain-container-content">
            <g:form name="alertStopListForm" method="post" controller="productGroup" action="save" id="${productGroup?.id}" >
                <div class="row">
                    <div class="col-md-6">
                        <label>
                            Product Name
                            <span class="required-indicator">*</span>
                        </label>
                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection"></div>
                            <div class="iconSearch">
                                <a data-toggle="modal" data-target="#productModal" tabindex="0"><i class="fa fa-search productRadio"></i></a>
                            </div>
                        </div>
                        <g:hiddenField name="productSelection" class="productSelection" value="${productGroup?.productSelection}"/>

                    </div>

                    <div class="col-md-2">
                        <label>
                            <g:message code="app.label.productGroup.groupName" />
                            <span class="required-indicator">*</span>
                        </label>
                        <g:textField name="groupName" value="${productGroup?.groupName}" class="form-control"/>
                    </div>

                    <div class="col-md-2">
                        <label>
                            <g:message code="app.label.productGroup.classification" />
                            <span class="required-indicator">*</span>
                        </label>
                        <g:select name="classification" from="${com.rxlogix.enums.ProductClassification.values()}"
                        optionValue = "id" noSelection="['':'-Select-']" value="${productGroup?.classification}" class="form-control"/>
                    </div>

                    <div class="col-md-2">
                        <label>
                            <g:message code="app.label.productGroup.display" />
                            <span class="required-indicator">*</span>
                        </label>
                        <div>
                            <g:checkBox name="display" value="${productGroup?.display}"/>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <g:if test="${productGroup?.id}">
                        <g:submitButton name="addButton" class="btn btn-primary addButton" type="submit" style="float: right" value="${message(code: 'default.button.update.label')}"/>
                    </g:if>
                    <g:else>
                        <g:submitButton name="addButton" class="btn btn-primary addButton" type="submit" style="float: right" value="${message(code: 'default.button.add.label')}"/>
                    </g:else>
                </div>
            </g:form>
        </div>
    </div>
</div>
<g:render template="/configuration/copyPasteModal" />
<input type="hidden" id="editable" value="true">
<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>