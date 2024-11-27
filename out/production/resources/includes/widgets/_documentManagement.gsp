<div id="table" class="table-editable">
    <div class="alert alert-success" style="display:none">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <strong>Success!</strong><span id="successMessage">Document updated!</span>
    </div>

    <div class="alert alert-danger" style="display:none">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <strong>Error!</strong><span id="errorMessage"></span>
    </div>

    <div class="dt-buttons btn-group">
        <a id="show-doc-list-bt" class="btn btn-primary document-create ${buttonClass}"
            data-toggle="documentModal" data-target="#documentModal">
            New Document
        </a>
    </div>
    %{--TODO Please refactor the inline styling--}%
    <table class="table" id="alert-document-table" style="width: 100%">
        <thead>
        <tr>
            <th><g:message code="app.document.management.chronicleId.label" /></th>
            <th><g:message code="app.document.management.documentType.label"/></th>
            <th><g:message code="app.document.management.documentLink.label"/></th>
            <th><g:message code="app.reportField.productProductName" /></th>
            <th><g:message code="select.start.date"/></th>
            <th><g:message code="app.document.management.documentStatus.label"/></th>
            <th><g:message code="app.document.management.author.label"/></th>
            <th><g:message code="app.document.management.statusDate.label"/></th>
            <th><g:message code="app.document.management.targetDate.label"/></th>
            <th><g:message code="app.label.comments"/></th>
            <th></th>
        </tr>
        </thead>

        <tbody class="tableBody">
        </tbody>

    </table>
    <input type="hidden" id="alertId" value="${alertInst?.id}"/>
    <input type="hidden" id="documentTypelist" value="${documentTypeList}" />
    <div>


    </div>

</div>
