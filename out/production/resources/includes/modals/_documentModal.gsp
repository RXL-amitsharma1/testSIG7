<style>
.rxmain-container-header-label {
    cursor: pointer
}
</style>

<div class="modal fade" id="documentModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Select the documents</label>
            </button>
            </div>

            <div class="modal-body">
                <div id="filters" class="fuelux">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <div data-toggle="collapse" data-target="#filter-pane">
                                <i class="show-filter fa fa-lg click fa-caret-right filter_icon"></i>
                                <span class="rxmain-container-header-label">Filters</span>
                            </div>
                        </div>

                        <div id="filter-pane" class="collapse filter">
                            <div class="panel-body">
                                <div class="row">
                                    <div class="col-xs-4 form-group">
                                        <label for="workflowStateFilter" class="">Product Name</label>
                                        <g:select class="form-control filterComboBox" id="productNameFilter"
                                                  name="productNameFilter"
                                                  from="${productNames.sort({it.toUpperCase()})}" noSelection="['': '']"/>
                                    </div>

                                    <div class="col-xs-4 form-group">
                                        <label for="priorityFilter" class="">Document Type</label>
                                        <g:select class="form-control filterComboBox" id="documentTypeFilter"
                                                  name="documentTypeFilter"
                                                  from="${documentTypes.sort({it.toUpperCase()})}" noSelection="['': '']"/>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-xs-4 form-group">
                                        <div class="dt-buttons btn-group m-r-10">
                                            <a id="show-doc-list-bt" class="btn btn-primary filterDocuments">
                                                Filter Documents
                                            </a>
                                        </div>

                                        <div class="dt-buttons btn-group">
                                            <a id="show-doc-list-bt" class="btn btn-default resetFilter">
                                                Reset Filter
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                %{--TODO put a class for the style--}%
                <div id="document-container">
                    <table class="dataTable auto-scale" id="documentModalTable" width="100%">
                        <thead>
                        <tr>
                            <th></th>
                            <th>Id</th>
                            <th><g:message code="app.document.management.documentType.label"/></th>
                            <th><g:message code="app.document.management.documentLink.label"/></th>
                            <th><g:message code="app.reportField.productProductName"/></th>
                            <th><g:message code="select.start.date"/></th>
                            <th><g:message code="app.document.management.documentStatus.label"/></th>
                            <th><g:message code="app.document.management.author.label"/></th>
                            <th><g:message code="app.document.management.statusDate.label"/></th>
                        </tr>
                        <thead>
                        <tbody id="documentModalTableBody"></tbody>
                    </table>
                </div>
            </div>

            <div class="modal-footer">
                <div class="buttons">
                    <button type="button" class="btn btn-primary modal-add-btn">Add</button>
                </div>
            </div>
        </div>
    </div>
</div>