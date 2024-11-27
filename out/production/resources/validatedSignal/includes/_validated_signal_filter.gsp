<%@ page import="com.rxlogix.user.User; com.rxlogix.config.PVSState" %>
<asset:javascript src="app/widgets/alert_filter.js"/>

<div id="filters" class="fuelux">
    <div class="panel panel-default">
        <div class="panel-heading">
            <div data-toggle="collapse" data-target="#filter-pane">
                <i class="show-filter fa fa-lg click fa-caret-right filter_icon"></i>
                <span class="rxmain-container-header-label">Filters</span>
            </div>
        </div>
        <g:form name="alert-list-filter" id="alert-list-filter">
            <div id="filter-pane" class="collapse filter">
                <div class="panel-body">
                    <div class="row">
                        <div class="col-xs-2 form-group">
                            <label for="signalNameFilter" class="">Signal Name</label>
                            <input type="text" placeholder="Signal Name" id="signalNameFilter",
                                   name="signalNameFilter"
                                   class="form-control filterInput">
                        </div>
                        <div class="col-xs-2 form-group">
                            <label for="productSelectionFilter" class="">Product Name</label>
                            <input type="text" placeholder="Product Name" id="productSelectionFilter",
                                   name="productSelectionFilter"
                                   class="form-control filterInput">
                        </div>
                        <div class="col-xs-2 form-group">
                            <label for="workflowStateFilter" class="">Workflow State</label>
                            <g:select class="form-control" id="workflowStateFilter" name="workflowStateFilter"
                                      from="${com.rxlogix.config.PVSState.findAll()}" optionKey="id"
                                      optionValue="value" noSelection="['':'']"/>
                        </div>

                        <div class="col-xs-2 form-group">
                            <label for="priorityFilter" class="">Priority</label>
                            <g:select class="form-control" id="priorityFilter" name="priorityFilter"
                                      from="${com.rxlogix.config.Priority.findAll()}" optionKey="id"
                                      optionValue="value" noSelection="['':'']"/>
                        </div>

                        <div class="col-xs-2 form-group">
                            <label for="dispositionFilter" class="">Disposition</label>
                            <g:select class="form-control" id="dispositionFilter" name="dispositionFilter"
                                      from="${com.rxlogix.config.Disposition.findAll()}" optionKey="id"
                                      optionValue="value" noSelection="['':'']"/>
                        </div>

                        <div class="col-xs-2 form-group">
                            <label for="assignedToFilter" class="">Assigned To</label>
                            <g:select class='form-control' name="assignedToFilter" id="assignedToFilter"
                                      from="${com.rxlogix.user.User.findAll().sort{it.fullName?.toLowerCase()}}" optionKey="id" optionValue="fullName"
                                      noSelection="['':'']" />
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-1 form-group pull-right">
                            <button id="alert-list-filter-apply-bt" class="btn btn-primary pull-right">Apply</button>
                        </div>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</div>
