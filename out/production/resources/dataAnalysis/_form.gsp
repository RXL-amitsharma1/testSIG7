
<div class="panel-group">
    <div class="panel panel-default rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header panel-heading">
            <h4 class="rxmain-container-header-label">
                <a data-toggle="collapse" data-parent="#accordion-pvs-analysis" href="#pvsAnalysis" aria-expanded="true" class="">
                    <g:message code="app.label.generateFile"/>
                </a>
            </h4>
        </div>
        <div id="pvsAnalysis" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true">
            <div class="row">
                <div class="col-md-4">
                    <div class="fuelux">
                        <div class="form-group">
                        <label for="productFamilyIds">
                            <g:message code="app.product.family" default="Product Family"/><span
                                class="required-indicator">*</span>
                        </label>
                        <div>
                            <g:select id="productFamilyIds"
                                      name="productFamilyIds"
                                      from="${[]}"
                                      class="form-control"/>
                        </div>
                    </div>

                        <div class="form-group">
                            <label for="caseSeriesId">Limit to Case Series</label>

                            <div>
                                <g:select id="caseSeriesId" name="caseSeriesId" from="${executedConfigs}"
                                          noSelection="${['': message(code: 'select.one')]}"
                                          value="${selectedCaseSeries}"
                                          optionKey="id" optionValue="name" class="form-control selsect2"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="">
                        <div class="form-group fuelux">
                            <label>
                                <g:message code="app.spotfire.report.period.start.date" default="Report Period Start Date"/><span
                                    class="required-indicator">*</span>
                            </label>
                            <div class="datepicker input-group" id="spotfireFromDate">
                                <g:textField name="fromDate" placeholder="Start Date"
                                             class="form-control custom-form-control"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                        <div class="form-group fuelux">
                            <label>
                                <g:message code="app.spotfire.report.period.end.date" default="Report Period End Date"/><span
                                    class="required-indicator">*</span>
                            </label>

                            <div class="datepicker input-group" id="spotfireEndDate">
                                <g:textField name="endDate" placeholder="End Date"
                                             class="form-control custom-form-control"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="fuelux form-group">
                        <label>
                            <g:message code="app.asof.date" default="As of Date"/><span
                                class="required-indicator">*</span>
                        </label>

                        <div class="datepicker input-group" id="spotfireAsOfDate">
                            <g:textField name="asOfDate" placeholder="As of Date" value="${spotfireCommand?.asOfDate}"
                                         class="form-control custom-form-control"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>
                            <g:message code="app.spotfire.file.type" default="File Type"/>
                        </label>
                        <div>
                            <div class="row">
                                <div class="col-md-12 m-t-5">
                                    <div class="radio radio-inline">
                                        <input type="radio" name="type" id="spotfireDrug" value="drug"
                                            ${spotfireCommand?.type == "drug" ? "checked" : ""}>
                                        <label for="spotfireDrug">
                                            Drug
                                        </label>
                                    </div>

                                    <div class="radio radio-inline">
                                        <input type="radio" name="type" id="spotfireVaccine" value="vacc"
                                            ${spotfireCommand?.type == "vacc" ? "checked" : ""}>
                                        <label for="spotfireVaccine">
                                            Vaccine
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="">
                        <div class="form-group fuelux">
                            <label>
                                <g:message code="app.spotfire.filename" default="File Name"/><span
                                    class="required-indicator">*</span>
                            </label>
                            <div>
                                <g:textField class="form-control" value="${spotfireCommand?.fullFileName}"
                                             name="fullFileName" id="fullFileName" />
                                <small class="text-muted"><g:message code="app.spotfire.filename.help.text" /></small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="pull-right">
                        <button type="submit" class="btn btn-primary" id="generate">
                            <span class="glyphicon glyphicon-flash icon-white"></span>
                            Generate
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

