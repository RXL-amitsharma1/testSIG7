<!-- Modal -->

%{--
Input parameters:
    (required) data-instancename : Name of the object to be deleted
    (required) data-instanceid   : Database identifier of the object
    (required) data-instancetype : Type of the object
                                   Currently there are 5 types, 'configuration', query', 'template', 'cognosReport' and 'accessControlGroup'.
                                   Additonal types may be added to i18n json files as needed.
    (optional) data-extramessage : Any additional comments you want inserted into the dialog

All variable must be passed to the dialog as request parameters, not in a view model map.

To include the gsp in the page, use:
<g:render template="/includes/widgets/deleteRecord"/>
--}%


<div class="modal fade" id="deleteModal" tabindex="-1" role="dialog" aria-labelledby="deleteModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteModalLabel"></h4>
            </div>

            <div class="modal-body">
                <div id="nameToDelete"></div>
                <p></p>
                <div class="description" style="font-weight:bold;"></div>
                <div class="extramessage"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                <button id="deleteButton" class="btn btn-danger">
                    <span class="glyphicon glyphicon-trash icon-white"></span>
                    ${message(code: 'default.button.deleteRecord.label', default: 'Delete')}
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<asset:javascript src="app/pvs/deleteModal.js"/>

