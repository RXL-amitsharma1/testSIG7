<div id="createEventModal" class="modal fade" tabindex="-1">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span></button>
                <h4 id="modalTitle" class="modal-title"></h4>
            </div>

            <div class="modal-footer">
                %{--<div style="margin-bottom: 10px">
                    <a class="btn btn-primary prModalHide" id="createReportRequest" target="_blank"
                       href="">${message(code: "app.label.report.request.create")}</a>
                </div>--}%
                <div style="margin-bottom: 10px">
                    <a class="btn btn-primary" target="_blank"
                       href="/signal/singleCaseAlert/create">Create Individual Case Alert</a>
                </div>
                <div style="margin-bottom: 10px">
                    <a class="btn btn-primary" target="_blank"
                       href="/signal/aggregateCaseAlert/create">Create Aggregate Alert</a>
                </div>
                <div style="margin-bottom: 10px">
                    <a class="btn btn-primary" target="_blank"
                       href="/signal/validatedSignal/create">Create Signal</a>
                </div>

                <div style="margin-bottom: 10px">
                    <a class="btn btn-primary action-create" id="createAction"
                       href="#">Create Action</a>
                </div>

               %{-- <div id="createActionItemObj">
                    <a class="btn btn-primary prModalHide actionItemId" id="createActionItem">
                        <g:img dir="/reports/assets" file="actionItem.png" width="20px" height="20px"/>
                        ${message(code: "app.label.action.item.create")}
                    </a>
                </div>--}%
            </div>
            <g:hiddenField name="dueDateHidden" value=""/>
        </div>
    </div>
</div>