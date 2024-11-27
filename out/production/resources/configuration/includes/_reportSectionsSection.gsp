<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" onclick="hideShowContent(this);"></i>
            <label class="rxmain-container-header-label click" onclick="hideShowContent(this);">
                <g:message code="app.label.details"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show" id="templateQueriesContainer">
            <g:render template="/templateQuery/templateQueries" model="['theInstance':configurationInstance]" />
        </div>

    </div>
</div>