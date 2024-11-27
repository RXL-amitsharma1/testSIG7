<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click" >
                <g:message code="app.create.special.pe"/>
            </label>
        </div>
        <div class="rxmain-container-content">
            <g:form name="alertStopListForm" method="post" action="save" id="${alertStopList?.id}" >
                <div class="row">
                    <div class="col-md-6">
                        <label><g:message code="app.label.special.productSelection"/><span
                                class="required-indicator">*</span></label>
                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection"></div>
                            <div class="iconSearch">
                                <i class="fa fa-search" class="productRadio" data-toggle="modal" data-target="#productModal"></i>
                            </div>
                        </div>
                        <g:textField class="productSelection" name="productSelection" value="${alertStopList?.specialProducts}" hidden="hidden"/>

                    </div>
                    <div class="col-md-6">
                        <label><g:message code="app.label.special.eventSelection"/><span
                                class="required-indicator">*</span></label>
                        <div class="wrapper">
                            <div id="showEventSelection" class="showDictionarySelection"></div>
                            <div class="iconSearch">
                                <i class="fa fa-search" id="searchEvents" data-toggle="modal" data-target="#eventModal"></i>
                            </div>
                        </div>
                        <g:textField  class="eventSelection" name="eventSelection" value="${alertStopList?.specialEvents}" hidden="hidden"/>
                    </div>
                </div>
                <div class="row m-t-10 text-right">
                    <g:if test="${alertStopList?.id}">
                        <g:actionSubmit class="btn btn-primary addButton" action="save" type="submit"  value="${message(code: 'default.button.update.label')}"/>
                    </g:if>
                    <g:else>
                        <g:actionSubmit class="btn btn-primary addButton" action="save" type="submit"  value="${message(code: 'default.button.add.label')}"/>
                    </g:else>

                </div>
            </g:form>
        </div>
    </div>
</div>
<g:render template="/includes/modals/event_selection_modal" />
<g:render template="/includes/modals/product_selection_modal" />