<div class="modal fade" id="modalFormat" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title">Formatting Options</label>
            </div>
            <div class="modal-body">
                <div id="modal-alert-container"></div>
                <label>Text Formatting Options</label>
                <div class="row">
                    <div class="col-md-4">
                        <div class="btn-group col-xs-12" data-toggle="buttons">
                            <label class="btn btn-default">
                                <input type="checkbox" name="bold" id="bold"><b>B</b>
                            </label>
                            <label class="btn btn-default">
                                <input type="checkbox" name="italic" id="italic"><i>I</i>
                            </label>
                            <label class="btn btn-default">
                                <input type="checkbox" name="underline" id="underline"><u>U</u>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-4">
                        <label>Font Color : <input type="text" id="fontColor"></label>
                    </div>

                    <div class="col-md-3">
                        <div class="pull-right">
                            <label>Target Columns<span class="required-indicator">*</span></label>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <select name="textTc" multiple="multiple" id="textTargetCol" class="form-control select2">
                        </select>
                    </div>
                </div>

                <hr/>
                <label>Cell Formatting Options</label>

                <div class="row">
                    <div class="col-md-4">
                        <label>Cell Color : <input type="text" id="cellColor"></label>
                    </div>

                    <div class="col-md-3">
                        <div class="pull-right">
                            <label>Target Columns<span class="required-indicator">*</span></label>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <select name="cellTc" multiple="multiple" id="cellTargetCol" class="form-control select2">
                        </select>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="saveFormat" class="btn btn-primary" data-dsismiss="modal">Save</button>
                <button type="button" id="clearFormat" class="btn btn-default">Clear</button>
            </div>
        </div>
    </div>
</div>