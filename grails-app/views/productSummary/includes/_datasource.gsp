<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click" >
                <g:message code="app.signal.data.source"/>
            </label>
        </div>
        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-3">
                    <div class="form-group">
                        <g:select id="selectedDatasource" name="selectedDatasource"
                        from="${dataSourceMap.entrySet()}"
                        optionKey="key" optionValue="value"
                        class="form-control selectedDatasource"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>