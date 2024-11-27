<div id="import-modal" class="modal fade" role="dialog">
  <div class="modal-dialog" role="document">
      <g:form action="importFile" controller="adHocAlert" method="POST">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
            <div class="row">
              <div class="col-xs-12">
                <label>Paste File Content</label>
                <g:textArea name="jsonContent" class="form-control"/>
              </div>
            </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default pv-btn-grey" data-dismiss="modal">Cancel</button>
        <input type="submit" class="btn btn-primary" value="Import"></input>
      </div>
    </div><!-- /.modal-content -->
      </g:form>
  </div><!-- /.modal-dialog -->
</div>