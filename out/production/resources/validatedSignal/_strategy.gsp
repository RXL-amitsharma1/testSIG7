<br/>
<div class="row">

    <div class="col-sm-12">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Strategy Details
                    </label>
                </div>
                <div class="rxmain-container-content">


                    <div class="row">
                        <div class="col-xs-12">
                            <div class="row">
                                <div class="col-xs-4">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label>Reference Number/Name</label>
                                            <div>${strategy.name}</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-4">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label>Type</label>
                                            <div>${strategy.type}</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-4">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label>PV Concepts</label>
                                            <div>
                                                ${strategy.medicalConcepts?.size()}
                                                ${strategy.medicalConcepts?.size() > 0 ? strategy.medicalConcepts.name*.join(",") : '-'}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <div class="row">
                                <div class="col-xs-4">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label>Products</label>
                                            <div>${strategy.getProductNameList()}</div>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-xs-4">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label>Start Date</label>
                                            <div>${strategy.startDate}</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-4">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <label>Description</label>
                                            <div>${strategy.description}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<g:render template="/includes/widgets/strategyAlerts" model="[id: strategy.id]"/>








