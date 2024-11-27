<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                <g:message code="app.create.product.group.list"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-md-12">
                    <table id="productGroupTable" class="row-border hover" width="100%">
                        <thead>
                        <tr>
                            <th>Group Name</th>
                            <th>Product Name</th>
                            <th>Classification</th>
                            <th>Display</th>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                <th>Actions</th>
                            </sec:ifAnyGranted>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>