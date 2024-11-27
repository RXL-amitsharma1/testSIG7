<style>
.sortable-list {
    list-style: none;
    margin: 0;
    min-height: 60px;
    padding: 10px;
}

.sortable-list.required {
    background-color: #7bea86;
}

.sortable-list.ignore {
    background-color: #ffb29f;
}
</style>

<div class="modal fade" id="signalSummaryReportPreferenceModal" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <label class="modal-title">Choose Report Section</label>
            </button>
            </div>

            <div class="modal-body">
                <div id="filters" class="fuelux">
                    <div class="panel panel-default" style="padding: 10px;">
                        <div class="row">
                            <div class="col-md-11" style="margin-left: 110px;">
                                <g:select from="${[]}" name="signalSummaryReportUserPreference"/>
                            </div>

                            <div class="col-md-1" style="margin-left: -220px;">
                                <div style="height: 160px;">
                                    <button type="button" style="margin-top: 123px;"
                                            onclick="$(selectedElement).moveUp();">&#9650;</button>
                                </div>

                                <div>
                                    <button type="button" onclick="$(selectedElement).moveDown();">&#9660;</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" data-dismiss="modal" aria-label="Close" class="btn btn-default">Cancel</button>
                    <button type="button" data-dismiss="modal" class="btn btn-primary signalSummaryReportPreferenceSave">Save</button>
                </div>
            </div>
        </div>
    </div>
</div>