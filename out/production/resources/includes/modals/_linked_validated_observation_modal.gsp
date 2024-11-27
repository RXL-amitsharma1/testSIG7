%{--<div class="modal fade modal-xlg" data-backdrop="static" id="validatedObservationsModal" tabindex="-1" role="dialog" aria-hidden="true">--}%
%{--    <div class="modal-dialog" >--}%
%{--        <div class="modal-content">--}%
%{--            <div class="modal-header">--}%
%{--                <label class="modal-title"><g:message code="app.label.signal.management.review" />: <span id="caseNumber"></span>-<span id="productFamily"></span>--}%
%{--                </label>--}%
%{--                <input type="hidden" id="caseVersion" />--}%
%{--                <input type="hidden" id="productName" />--}%
%{--                <input type="hidden" id="alertConfigId" />--}%
%{--                <input type="hidden" id="pt" />--}%
%{--                <g:hiddenField name="exeConfigId" id="exeConfigId" value="${45}" />--}%
%{--                <button type="button" class="close pull-right" data-dismiss="modal" aria-label="Close">--}%
%{--                    <span aria-hidden="true">&times;</span></button>--}%
%{--           </div>--}%

%{--            <div class="modal-body">--}%

%{--                <div id="case-history-container" class="list">--}%
%{--                    <a data-toggle="collapse" href="#accordion-pvs-aggreview">--}%
%{--                        <g:message code="app.validatedSignal.aggregated.case.review" />--}%
%{--                    </a>--}%
%{--                    <label class="modal-title m-b-15"><g:message code="app.validatedSignal.aggregated.case.review"/></label>--}%
%{--                    <br/>--}%
%{--                    <table class="table caseHistoryModalTable" id="accordion-pvs-aggreview" style="width: 100%">--}%
%{--                        <thead>--}%
%{--                        <tr>--}%
%{--                            <th><g:message code="app.label.alert.name"/></th>--}%
%{--                            <th><g:message code="app.label.signal.product.name" /></th>--}%
%{--                            <th><g:message code="app.label.event.soc"/></th>--}%
%{--                            <th>Event PT</th>--}%
%{--                            <th></th>--}%
%{--                        </tr>--}%
%{--                        <thead>--}%
%{--                        <tbody id="caseHistoryModalTableBody" class="tableModalBody"></tbody>--}%
%{--                    </table>--}%
%{--                </div>--}%
%{--                <br/>--}%
%{--                <div id="case-history-container2" class="list">--}%
%{--                    <label class="modal-title m-b-15"><g:message code="app.validatedSignal.single.case.review"/></label>--}%
%{--                    <br/>--}%
%{--                    <table class="table caseHistoryModalTable" id="caseHistoryModalTable" style="width: 100%">--}%
%{--                        <thead>--}%
%{--                        <tr>--}%
%{--                            <th><g:message code="app.label.alert.name"/></th>--}%
%{--                            <th><g:message code="app.label.signal.product.name" /></th>--}%
%{--                            <th><g:message code="app.label.case.number"/></th>--}%
%{--                            <th>Event PT</th>--}%
%{--                            <th></th>--}%
%{--                        </tr>--}%
%{--                        <thead>--}%
%{--                        <tbody id="caseHistoryModalTableBody" class="tableModalBody"></tbody>--}%
%{--                    </table>--}%
%{--                </div>--}%
%{--                <br/>--}%
%{--                <div id="case-history-container3" class="list">--}%
%{--                    <label class="modal-title m-b-15"><g:message code="app.validatedSignal.new.literature.review"/></label>--}%
%{--                    <br/>--}%
%{--                    <table class="table caseHistoryModalTable" id="caseHistoryModalTable" style="width: 100%">--}%
%{--                        <thead>--}%
%{--                        <tr>--}%
%{--                            <th><g:message code="app.label.alert.name"/></th>--}%
%{--                            <th><g:message code="app.label.literature.details.column.title" /></th>--}%
%{--                            <th><g:message code="app.label.literature.details.column.authors"/></th>--}%
%{--                            <th><g:message code="app.label.literature.details.column.publication.date"/></th>--}%
%{--                            <th></th>--}%
%{--                        </tr>--}%
%{--                        <thead>--}%
%{--                        <tbody id="caseHistoryModalTableBody" class="tableModalBody"></tbody>--}%
%{--                    </table>--}%
%{--                </div>--}%
%{--                <br/>--}%
%{--                <div id="case-history-container4" class="list">--}%
%{--                    <label class="modal-title m-b-15"><g:message code="ad.hoc.validatedSignal.review.label"/></label>--}%
%{--                    <br/>--}%
%{--                    <table class="table caseHistoryModalTable" id="caseHistoryModalTable" style="width: 100%">--}%
%{--                        <thead>--}%
%{--                        <tr>--}%
%{--                            <th><g:message code="app.label.alert.name"/></th>--}%
%{--                            <th><g:message code="app.label.signal.product.name" /></th>--}%
%{--                            <th><g:message code="app.label.datasource"/></th>--}%
%{--                            <th>Event PT</th>--}%
%{--                            <th></th>--}%
%{--                        </tr>--}%
%{--                        <thead>--}%
%{--                        <tbody id="caseHistoryModalTableBody" class="tableModalBody"></tbody>--}%
%{--                    </table>--}%
%{--                </div>--}%
%{--            </div>--}%
%{--            <div class="modal-footer">--}%
%{--                <button type="button" class="btn btn-default case-history-modal-close" data-dismiss="modal">--}%
%{--                    <g:message code="default.button.close.label"/>--}%
%{--                </button>--}%
%{--            </div>--}%
%{--        </div>--}%
%{--    </div>--}%
%{--</div>--}%





<div class="modal fade modal-xlg" data-backdrop="static" id="validatedObservationsModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog" >
        <div class="modal-content">
            <div class="modal-header">
                <label class="modal-title"><g:message code="app.label.signal.management.review" />: <span id="caseNumber"></span>-<span id="productFamily"></span>
                </label>
                <button type="button" class="close pull-right" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
           </div>

            <div class="modal-body">
                <g:render template="/validatedSignal/includes/aggregateReview"/>
                <g:render template="/validatedSignal/includes/singleReview"/>
                <g:render template="/validatedSignal/includes/literatureReview"/>
                <g:render template="/validatedSignal/includes/adHocReview" model="[isTopic: false]"/>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default case-history-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>

