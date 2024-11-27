<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click" >
                <g:message code="app.label.alert.stop.list"/>
            </label>
        </div>
        <div class="rxmain-container-content">
            <form name="alertStopListForm" method="post" action="save" id="alertStopList" id="${alertStopList?.id}" >
                <div class="row">
                <div class="col-md-6">
                    <label><g:message code="app.label.productSelection" /></label>
                    <div class="wrapper">
                        <div id="showProductSelection" class="showDictionarySelection"></div>
                        <div class="iconSearch">
                            <a class="productRadio" data-toggle="modal" data-target="#productModal" tabindex="0"><i class="fa fa-search"></i></a>
                        </div>
                    </div>
                    <g:textField class="productSelected" name="productSelection" value="${alertStopList?.productName}" hidden="hidden"/>

                </div>
                <div class="col-md-6">
                    <label><g:message code="app.label.eventSelection"/></label>
                    <div class="wrapper">
                        <div id="showEventSelection" class="showDictionarySelection"></div>
                        <div class="iconSearch">
                            <a  id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0"><i class="fa fa-search"></i></a>
                        </div>
                    </div>
                    <g:textField  class="eventSelected" name="eventSelection" value="${alertStopList?.eventName}" hidden="hidden"/>
                </div>
            </div>
            <div class="row text-right m-t-10">
                <g:actionSubmit class="btn btn-primary addButton" action="save" type="submit"  value="${message(code: 'default.button.add.label')}"/>
            </div>
            </form>
         </div>
    </div>
</div>
<g:render template="/includes/modals/event_selection_modal" />
<g:render template="/includes/modals/product_selection_modal" />