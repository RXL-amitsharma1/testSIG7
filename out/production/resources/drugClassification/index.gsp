<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper" contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Drug Classification</title>
    <asset:stylesheet src="configuration.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="boostrap-switch.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
    </g:else>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/drugClassification/drugClassification.js"/>

    <g:javascript>
        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}"
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var saveDrugClassificationUrl = "${createLink(controller: 'drugClassification', action: 'save')}";
        var editDrugClassificationUrl = "${createLink(controller: 'drugClassification', action: 'update')}";
        var deleteDrugClassificationUrl = "${createLink(controller: 'drugClassification', action: 'delete')}";
        var drugClassificationListUrl = "${createLink(controller: 'drugClassification', action: 'list')}";
        var fetchDrugClassificationUrl = "${createLink(controller: 'drugClassification', action: 'fetchDrugClassification')}"
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
    </g:javascript>
</head>

<body>

%{-- Create/Edit Drug Classification Form Panel --}%
<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <div class="row">
            <div class="col-md-7">
                <a data-toggle="collapse" href="#drugClassificationCreate" aria-expanded="true"
                   style="color:inherit;">Create Drug Classification</a>
            </div>
        </div>
    </div>

    <div id="drugClassificationCreate" class="panel-collapse rxmain-container-content rxmain-container-show collapse in"
         aria-expanded="true">
        <div id="success-message" class="alert alert-success" style="display: none;"></div>

        <div id="error-message" class="alert alert-danger" style="display: none;"></div>

        <div class="body">
            <g:select id="selectedDatasource" name="selectedDatasource"
                      from="${dataSourceMap.entrySet()}"
                      optionKey="key" optionValue="value"
                      class="form-control selectedDatasourceSignal" style="display: none"/>


            <g:form name="drug-classification-form" url="#">
                <div class="row">
                    <div class="col-md-4">
                        <label>
                            <g:message code="app.label.productSelection"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection" style="height: 157px"></div>

                            <div class="iconSearch">
                                <a class="fa fa-search productRadio" data-toggle="modal"
                                   data-target="#productModal" tabindex="0" title="Search Products"></a>
                            </div>
                        </div>
                        <g:textField class="productSelection" name="productSelection"
                                     value="${drugClassificationInstance?.productDictionarySelection}" hidden="hidden"/>
                    </div>

                    <div class="col-md-4">
                        <div class="form-group">
                            <label><g:message code="app.label.class.name"/><span class="required-indicator">*</span></label>
                            <g:textField name="className" class="form-control required" value="${drugClassificationInstance?.className}"/>
                        </div>

                        <div class="form-group">
                                <label><g:message code="app.label.classification.type"/><span class="required-indicator">*</span></label>
                                <g:select name="classificationType" class="form-control classificationType"
                                              from="${classificationTypeEnums}" optionValue="value"
                                              noSelection="['': '-Select-']" optionKey="key"
                                              value="${drugClassificationInstance?.classificationType}"/>
                        </div>

                        <div class="form-group">
                            <label><g:message code="app.label.productGroup.classification"/></label>
                            <div class="input-group">
                                <g:textField name="classification" class="form-control required" value="${drugClassificationInstance?.classification}"/>
                                <span class="input-group-addon add-classification-btn" style="cursor: pointer;"><i class="glyphicon glyphicon-plus-sign"></i></span>
                            </div>
                        </div>

                    </div>

                    <div class="col-md-4">
                        <div class="row">
                            <div class="col-md-12 classificationsDiv">
                                <label>Added Classifications<span class="required-indicator">*</span></label>
                                <div class="form-control" id="added-classification" style="overflow-y: auto; height:157px;white-space: pre-wrap !important"></div>
                            </div>
                        </div>
                        <input class="selectedDatasource" type="hidden" name="dataSource" value="faers"/>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12 text-right">
                        <button id="save-drug-classification" class="btn btn-primary"><g:message
                                code="default.button.save.label"/></button>
                        <button id="edit-drug-classification" class="btn btn-primary" style="display: none;"><g:message
                                code="default.button.update.label"/></button>
                        <button id="cancel-drug-classification" class="btn btn-default"><g:message
                                code="default.button.cancel.label"/></button>
                    </div>
                </div>
            </g:form>
        </div>
    </div>
</div>
</sec:ifAnyGranted>

%{-- Drug Classification List Panel--}%
<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <div class="row">
            <div class="col-md-7">
                <a data-toggle="collapse" href="#drugClassificationList" aria-expanded="true"
                   style="color:inherit;">Drug Classifications</a>
            </div>
        </div>
    </div>

    <div id="drugClassificationList" class="panel-collapse rxmain-container-content rxmain-container-show collapse in"
         aria-expanded="true">
        <div id="drugClassificationTableContainer">
            <table id="drugClassificationTable" width="100%">
                <thead>
                <tr>
                    <th>Product</th>
                    <th>Class Name</th>
                    <th>Added Classifications</th>
                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CONFIGURATION_CRUD">
                        <th>Action</th>
                    </sec:ifAnyGranted>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>

%{--Delete Modal--}%
<div class="modal fade" id="deleteDrugModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title">Delete Drug Classification</h4>
            </div>

            <div class="modal-body">
                <p>Are you sure you want to delete this?</p>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    <g:message code="default.button.cancel.label"/>
                </button>
                <button id="deleteDrugButton" data-dismiss="modal" class="btn btn-primary">
                    <span class="glyphicon glyphicon-trash icon-white"></span>
                        ${message(code: 'default.button.deleteRecord.label', default: 'Delete')}&nbsp;
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<g:render template="/configuration/copyPasteModal" />

<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList]"/>
    <script>
        var pvaUrls = {
            selectUrl: options.product.selectUrl,
            preLevelParentsUrl: options.product.preLevelParentsUrl,
            searchUrl: options.product.searchUrl
        };
        var otherUrls = {
            selectUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getSelectedProduct')}",
            preLevelParentsUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'pvsProductDictionary', action: 'searchProducts')}"
        };
        changeDataSource("faers");
        $("#productModal").find(".row").eq(1).hide();
    </script>
</g:if>
<g:else>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>

</body>
</html>