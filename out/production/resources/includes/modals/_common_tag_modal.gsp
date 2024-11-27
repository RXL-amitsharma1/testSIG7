<style>
.tableModalBody tr.odd {
    background-color: #ebebeb!important;
}
.select2-container--default.select2-container--disabled .select2-selection--single {
    background-color:#f7f8f7!important;
    cursor: default;
}
.select2-container--default.select2-container--disabled .select2-selection--multiple {
    background-color: #f7f8f7!important;
    cursor: default;
}
.dataTables_filter{
    display:block!important;
}

.select2-container--default .select2-search--inline .select2-search__field {
    margin: 8px 0 0 10px !important;
    font-size: 13px!important;
    min-width: auto!important;
    height: 16px;
}
.mw-110 .select2-container--default .select2-search--inline .select2-search__field:nth-child(1) {
    min-width: 110px !important;
}
ul .select2-search .select2-search--inline{display: inline-block!important;}
.select2-selection__rendered li{display: inline-block;}
.tag-action{font-size: medium; padding: 5px 2px 2px 2px;}
.select2-container--default .select2-selection--single .select2-selection__rendered {
    font-size: 13px;
}
.select2-selection__choice{font-size:12px!important;}
input[type=checkbox], input[type=radio] {
    margin: 8px 0 0!important;
}
.popover {
    position: fixed !important;
}
.dt-center{
    border-top:1px solid #d4d4d4!important;
}
.td{
    border-top:1px solid #d4d4d4!important;
}
.justification-cell{
    border-top:1px solid #d4d4d4!important;
}
.td-action{
    border-top:1px solid #d4d4d4!important;
}
hr{
    margin-top:4px!important;
    margin-bottom:2px!important;
}
.popover.left>.arrow {
left: auto!important;
}

.modal-dialog .right-top .fa-plus {
    font-size: 15px!important;background: #69a73c;color: #fff;padding: 3px 7px; border-radius: 7px;margin-bottom: 5px;
}
#tag-container div.dataTables_scrollHeadInner{
    width:auto!important;
}
.pbr br{display:none!important}
.select2-selection__rendered li select2-search select2-search--inline input{display:inline-block;}
input::placeholder {font-size: 13px; margin-bottom: 20px;z-index: 100;}
.select2-container .select2-selection--multiple {
    overflow-y: auto;
    max-height: 50px!important;
    width: 100%;
    height: auto!important;
    max-width: 300px;
}
select.form-control + .select2 {
    display: inherit;
}

.ui-sortable-handle {
    cursor: default !important;
}

#select2-dynamicSelectFieldCategory-container {
    max-width: 210px;
}

</style>

<div class="modal fade " data-backdrop="static" id="commonTagModal" role="dialog">
    <div class="modal-dialog modal-md modal-width-lg" role="document">
        <div class="modal-content">
            <div class="rxmain-container-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <label class="modal-title"><g:message code="app.label.alert.category"/></label>
                <div class="configureFields pv-head-config">
                    <a href="#" class="pull-right ic-sm" data-dismiss="modal" aria-label="Close">
                        <i class="mdi mdi-close" aria-hidden="true"></i>
                    </a>

                    <a href="#" id="add-row" class="pull-right ic-sm ${saveCategoryAccess}">
                        <i class="mdi mdi-plus" aria-hidden="true"></i>
                    </a>
                </div>

            </div>
            <div id="tag-body" class="modal-body">
                <div id="tag-container" class="list">
                    <table class="table" id="categoryModalTable" style="width: 100%;">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.category.column.category"/></th>
                            <th><g:message code="app.label.category.column.subCategory"/></th>
                            <th><g:message code="app.label.category.column.alert"/></th>
                            <th><g:message code="app.label.category.column.private"/></th>
                            <th><g:message code="app.label.category.column.action"/></th>
                        </tr>
                        <thead>
                        <tbody id="categoryModalTableBody" class="tableModalBody" style="cursor: default !important;"></tbody>
                    </table>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="addTags" class="btn btn-primary addTags ${saveCategoryAccess}" data-dismiss="modal"><g:message
                        code="default.button.save.label"/></button>
                <button type="button" class="btn btn-default closeGenericValues" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>

