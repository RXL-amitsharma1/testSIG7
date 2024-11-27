<div class="panel panel-default rxmain-container rxmain-container-top" id="reportSection">
    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header panel-heading">
            <h4 class="rxmain-container-header-label">
                <a data-toggle="collapse" data-parent="#" aria-expanded="true" href="#templateQueriesContainer"><g:message code="app.label.reportSections"/></a>
            </h4>
        </div>

        <div class="rxmain-container-content rxmain-container-show" id="templateQueriesContainer">
            <g:render template="/templateQuery/templateQueries" model="['theInstance':configurationInstance, clone: clone,appType: appType]" />
        </div>

    </div>
</div>