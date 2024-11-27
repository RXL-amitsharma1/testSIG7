<html>
<%@ page import="grails.util.Holders; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.label.productAssignments"/></title>
    <asset:javascript src="app/pvs/productAssignment/product_assignment.js"/>
    <asset:javascript src="purify/purify.min.js" />
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="app/pvs/product_assignment.css"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/pvs/alerts_review/alert_review.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>

    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/validated_signal/assessment_dictionary.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <g:javascript>
        var hasConfigurationEditorRole = "true";
        var columnOrder = "${columnOrder}";
        var isEditable = ${isEdit};
        // window.localStorage.setItem("columnOrder",columnOrder)
        var isProductAssignment = true;
        var isProductView = ${isProductView};
        var fetchProductAssignmentUrl = "${createLink(controller: 'productAssignment', action: 'fetchProductAssignment')}";
        var updateProductAssignmentUrl = "${createLink(controller: 'productAssignment', action: 'updateProductAssignment')}";
        var bulkUpdateProductAssignmentUrl = "${createLink(controller: 'productAssignment', action: 'bulkUpdateProductAssignment')}";
        var deleteAssignmentUrl = "${createLink(controller: 'productAssignment', action: 'deleteAssignment')}";
        var columnOrderUrl = "${createLink(controller: 'productAssignment', action: 'columnOrder')}";
        var updateColumnOrderUrl = "${createLink(controller: 'productAssignment', action: 'setColumnOrder')}";
        var exportAssignmentUrl = "${createLink(controller: 'productAssignment', action: 'exportAssignment')}";
        var saveProductAssignmentUrl = "${createLink(controller: 'productAssignment', action: 'saveProductAssignment')}";
        var sharedWithUrl = "${createLink(controller: 'user', action: 'searchShareWithUserGroupList')}";
        var fetchImportAssignmentUrl = "${createLink(controller: 'productAssignment', action: 'fetchImportAssignment')}";
        var uploadFileUrl = "${createLink(controller: 'productAssignment', action: 'upload')}";
        var populateUnassignedProductUrl = "${createLink(controller: 'productAssignment', action: 'populateUnassignedProducts')}";

        var dataSourcesColorMapValues = '{"faers": "rgb(112, 193, 179)", "eudra": "rgb(157, 129, 137)", "vaers": "rgb(92, 184, 92)", "vigibase": "rgb(255, 87, 51)"}';
        var options = { spinnerPath:"${assetPath(src: 'select2-spinner.gif')}" };
        options.product = {
            levelNames: "${PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }.join(",")}",
            dicColumnCount: ${com.rxlogix.pvdictionary.config.PVDictionaryConfig.ProductConfig.columns.size()},
            selectUrl: "${createLink(controller: 'productDictionary', action: 'getSelectedItem')}",
            preLevelParentsUrl: "${createLink(controller: 'productDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'productDictionary', action: 'searchViews')}"
        };
        intializeDictionariesAssessment(options);
    </g:javascript>
</head>

<body>

<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
            <div class="row">
                <div class="col-md-4">
                    <label class="rxmain-container-header-label m-t-5">${message(code: "app.label.productAssignments")}</label>
                </div>

                <div class="col-md-8 ico-menu pull-right">
                    <span class="dropdown grid-icon pull-right" id="reportIconMenu">
                        <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
                            <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10"
                               style="margin-right:5px"></i>
                        </span>
                        <ul class="dropdown-menu ul-ddm">

                            <li class="li-pin-width">
                                <a tabindex="0" id="configureProductAssignmentFields" data-fieldconfigurationbarid="productFields"
                                   data-pagetype="product_assignment" class="%{--pull-right --}%test text-left-prop field-config-bar-toggle"
                                   data-backdrop="true" data-container="columnList-container" title="Choose Fields" accesskey="c">
                                    <i class="mdi mdi-settings-outline font-16"></i>
                                    <g:message code="app.label.field.selection"/>
                                </a>
                            </li>

                            <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_MANAGE_PRODUCT_ASSIGNMENTS">
                            <li class="li-pin-width">
                                <a class="test text-left-prop" id="import-assignments" href="#">
                                    <i class="mdi mdi-import"></i>
                                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                        Import Assignments</span>
                                </a>
                            </li>
                            <li class="li-pin-width">
                                <a class="test text-left-prop" id="import-log" href="#">
                                    <i class="mdi mdi-import"></i>
                                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                        Import Log</span>
                                </a>
                            </li>
                            </sec:ifAnyGranted>
                            <li class="li-pin-width">
                                <a class="test text-left-prop" id="exportProductAssignment">
                                    <i class="mdi mdi-export"></i>
                                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                        Export To</span>
                                </a>
                            </li>
                            <li class="li-pin-width">
                                <a class="test text-left-prop" id="apply-filters" href="#">
                                    <i class="mdi mdi-filter-outline"></i>
                                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                        Filters</span>
                                </a>
                            </li>
                            <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_MANAGE_PRODUCT_ASSIGNMENTS">
                            <li class="li-pin-width">
                                <a class="test text-left-prop" id="populate-unassigned-products" href="#">
                                    <i class="mdi mdi-plus-outline"></i>
                                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                        Populate Unassigned Products</span>
                                </a>
                            </li>
                            </sec:ifAnyGranted>
                            <li class="li-pin-width">
                                <g:link class="test text-left-prop" id="change-view" controller="productAssignment" action="index" params="[isProductView:!isProductView]" >
                                    <i class="mdi mdi-file-document-box-outline"></i>
                                    <span data-title="Filters" class="test changeViewLabel" tabindex="0" role="button"
                                          accesskey="y">
                                        Change To User View</span>
                                </g:link>
                            </li>
                        </ul>
                    </span>

                    <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_MANAGE_PRODUCT_ASSIGNMENTS">
                    <span class=" configureFields ">
                        <a href="#" id="productAssignmentNewRow" class="pull-right ic-rm"
                           data-toggle="tooltip" data-placement="bottom">
                            <i class="md md-add" area-hidden="true"></i></a>
                    </span>
                    </sec:ifAnyGranted>

                </div>
            </div>
        </div>

        <g:render template="/includes/layout/flashErrorsDivs"/>
        <div class="collapse in" id="productManagementContainer">
            <div class="rxmain-container-content ">
                <table id="productAssignmentTable" class="row-border hover simple-alert-table" width="100%">
                    <thead>
                    <tr>
                        <th data-idx="0" class="col-md-half sorting_disabled" data-field=""></th>
                        <th data-idx="1" class="col-md-3-half" data-field="product"><g:message
                                code="product.assignment.label.product"
                                default="Product"/>
                            <span class="required-indicator">*</span>
                        </th>
                        <th data-idx="2" class="col-md-2" data-field="hierarchy"><g:message
                                code="product.assignment.label.product.hierarchy"
                                default="Product Hierarchy"/>
                        </th>
                        <th data-idx="3" class="col-md-2" data-field="assignedUserOrGroup"><g:message
                                code="product.assignment.label.assignment"
                                default="Assignment"/>
                            <span class="required-indicator">*</span>
                        </th>
                        <th data-idx="4" class="col-md-1" data-field="createdBy"><g:message
                                code="product.assignment.label.user.id"
                                default="User ID"/>
                        </th>
                        <th data-idx="5" class="col-md-2" data-field="workflowGroup"><g:message
                                code="product.assignment.label.workflow.group"
                                default="Workflow Group"/>
                        </th>
                        <th data-idx="6" class="col-md-1 sorting_disabled" data-field="action"></th>
                    </tr>
                    </thead>
                    <tfoot class="productAssignmentTable_foot hide">
                    <tr role="row" class="productAssignmentTable_row">
                        <td class="col-md-half"></td>
                        <td class="col-md-3-half">
                            <div class="row">
                                <div class="wrapper">
                                    <g:hiddenField name="productSelectionAssessment" value=""/>
                                    <g:hiddenField name="productGroupSelectionAssessment" value=""/>
                                    <div id="showProductSelectionAssessment" class="showDictionarySelection"></div>

                                    <div class="iconSearch">
                                        <a tabindex="0" id="searchProductsAssessment" data-toggle="modal"
                                           data-target="#productModalAssessment" class="productRadio">
                                            <i class="fa fa-search"></i></a>
                                    </div>
                                </div>
                            </div>
                        </td>
                        <td class="col-md-2">
                        </td>
                        <td class="col-md-2">
                            <g:initializeShareWithElementProductSelection shareWithId="selectUserOrGroup" isWorkflowEnabled="false" isLabel="${false}"/>
                            <input type="hidden" id="primaryAssignment">
                        </td>
                        <td class="col-md-1"></td>
                        <td class="col-md-2"></td>
                        <td class="col-md-1">
                            <a href="javascript:void(0);" title="Save"
                               class="table-row-save hidden-ic pv-ic saveAssignment"
                               data-editing="false">
                                <i class="mdi mdi-check" aria-hidden="true"></i>
                            </a>
                            <a href="javascript:void(0);" id="productAssignmentRemoveRow" title="Delete"
                               class="table-row-save hidden-ic pv-ic">
                                <i class="mdi mdi-close" area-hidden="true"></i>
                            </a>
                        </td>
                    </tr>
                    </tfoot>

                </table>
            </div>
        </div>


        <div class="modal fade" id="bulkUpdateModal" data-keyboard="false" data-backdrop="static">
            <div class="modal-dialog modal-md">
                <div class="modal-content">

                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span></button>
                        <label class="modal-title">Apply To All</label>
                    </button>
                    </div>

                    <div class="modal-body">
                        <div>
                            <label class="labelBold m-r-15">
                                <input class="m-r-5" type="radio" name="bulkOptions" id="current" value="current" checked>
                                Current
                            </label>
                            <label class="labelBold">
                                <input class="m-r-5" type="radio" name="bulkOptions" id="allSelected" value="allSelected">
                                Selected (<span id="totalNumberOfSelectedRow"></span>)
                            </label>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" id="addFile" class="btn btn-primary" data-dismiss="modal">
                            Ok
                        </button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="bulkDeleteModal" data-keyboard="false" data-backdrop="static">
            <div class="modal-dialog modal-md">
                <div class="modal-content">

                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span></button>
                        <label class="modal-title">Apply To All</label>
                    </button>
                    </div>

                    <div class="modal-body">
                        <div>
                            <label class="labelBold m-r-15">
                                <input class="m-r-5" type="radio" name="bulkOptions" id="current_delete" value="current" checked>
                                Current
                            </label>
                            <label class="labelBold">
                                <input class="m-r-5" type="radio" name="bulkOptions" id="allSelected_delete" value="allSelected">
                                Selected (<span id="totalNumberOfSelectedRows"></span>)
                            </label>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" id="addFile" class="btn btn-primary" data-dismiss="modal">
                            Ok
                        </button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<g:hiddenField name="isAssessmentDicitionary" id="isAssessmentDicitionary" value="true"/>

<g:hiddenField name="selectedDatasource" value="pva"></g:hiddenField>

<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'productFields']"/>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <g:render template="/configuration/copyPasteModal"/>
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>

<g:render template="includes/assessment_product_selection_modal"/>
<g:render template="/includes/modals/productAssignmentFileModal"/>
<g:render template="/includes/modals/importLogModal"/>
<g:render template="/includes/modals/importAssignmentFormatModal"/>
<g:render template="/includes/modals/populateUnassignedProductsModal"/>
<script>
    var pvaUrls = {
        selectUrl: options.product.selectUrl,
        preLevelParentsUrl: options.product.preLevelParentsUrl,
        searchUrl: options.product.searchUrl
    };
</script>
</body>
</html>
