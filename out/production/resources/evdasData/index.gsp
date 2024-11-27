<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.evdasUpload.title"/></title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/common/rx_list_utils.js"/>
    <asset:javascript src="jquery-ui/jquery-ui.min.js"/>
    <asset:javascript src="jquery/jquery-picklist.js"/>
    <asset:javascript src="app/pvs/evdasData.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/users/user_edit.js"/>
    <asset:stylesheet src="jquery-picklist.css"/>

    <g:javascript>
        var getEvdasDataUrl = "${createLink(controller: 'evdasData', action: 'fetchEvdasData')}";
        var downloadDocumentUrl = "${createLink(controller: 'evdasData', action: 'downloadDocument')}";
        var downloadErrorLogUrl = "${createLink(controller: 'evdasData', action: 'downloadErrorLog')}";
    </g:javascript>

</head>

<body>

<g:render template="/includes/layout/flashErrorsDivs" bean="${1}" var="theInstance"/>

<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
    <div class="rxmain-container rxmain-container-top">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label click">
                    <g:message code="label.evdas.data.file.upload.header"/>
                    <i class="fa fa-info-circle show-directory"></i>
                </label>
            </div>

            <div class="rxmain-container-content">
                <g:form action="upload" enctype="multipart/form-data" id="formEvdasDataUpload" method="post">
                    <div class="row">
                        <div class="col-md-3">
                            <label for="description">
                                <g:message code="label.description"/>
                                <textArea maxlength="255" rows="4" cols="55" class="form-control" name="description"
                                          id="description"></textArea>
                            </label>
                        </div>

                        <div class="col-md-3">
                            <div class="modal fade" id="dataUploadModal" role="dialog">
                                <div class="modal-dialog" style="width:930px">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                                            <h4 class="modal-title">Note</h4>
                                        </div>

                                        <div class="modal-body">
                                            <span><g:message
                                                    code="app.label.evdas.data.upload.incorrect.file.format" encodeAs="HTML"/></span>
                                        </div>

                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-default"
                                                    data-dismiss="modal">Ok</button>
                                        </div>
                                    </div>

                                </div>
                            </div>
                            <div>
                                <label for="file">
                                    <g:message code="label.upload.file"/>
                                    <a href="javascript:void(0)" class="glyphicon glyphicon-info-sign themecolor" data-toggle="modal" data-target="#dataUploadModal"></a>
                                    <input type="file" name="file"
                                           id="file" accept=".xlsx,.xls,.csv">
                                </label>
                            </div>

                            <div>
                                <div class="m-t-15 substanceNameSelector" style="visibility: hidden">
                                    <label for="substanceName">
                                        <g:message code="label.evdas.data.file.substance.name"
                                                   default="Substance Name"/>
                                    </label>

                                    <div>
                                        <g:select name="substanceName" from="${substanceNames}"  class="form-control select2 panel-heading" />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-4">
                            <div>
                                <label>
                                    <g:message code="label.upload.file.type" default="File Type"/>
                                </label>

                                <div>
                                    <label class="radio-inline"><input type="radio" value="eRMR" checked
                                                                       name="dataType">eRMR</label>
                                    <label class="radio-inline"><input type="radio" value="Case Listing"
                                                                       name="dataType">Case Listing</label>
                                </div>
                            </div>

                            <div class="m-t-20">
                                <label>
                                    <g:message code="label.upload.file.data.duplicate"
                                               default="Duplicate Data/File Handling"/>
                                </label>

                                <div>
                                    <label class="radio-inline"><input type="radio" checked name="optDuplicate"
                                                                       value="1">Create Versions</label>
                                    <label class="radio-inline"><input type="radio" name="optDuplicate" value="2">Ignore
                                    </label>
                                    <label class="radio-inline"><input type="radio" name="optDuplicate"
                                                                       value="3">Overwrite
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-2">
                            <label for="submit">
                                <span>&nbsp;</span>
                                <input type="submit" class="form-control btn primaryButton btn-primary" value="Upload"
                                       id="submit" disabled>
                            </label>
                        </div>
                    </div>
                </g:form>
            </div>
        </div>
    </div>
</sec:ifAnyGranted>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click"><g:message
                    code="label.evdas.data.file.list.header"/></label>
        </div>

        <div class="rxmain-container-content">

            <div>
                <table id="eudraDataTable" class="row-border hover" width="100%">
                    <thead>
                    <tr>
                        <th><g:message code="label.evdas.data.file.list.document"/></th>
                        <th><g:message code="label.upload.file.type" default="File Type"/></th>
                        <th><g:message code="label.description"/></th>
                        <th><g:message code="label.evdas.data.file.list.date.range"/></th>
                        <th><g:message code="label.evdas.data.file.list.substance"/></th>
                        <th><g:message code="label.evdas.data.file.list.processed"/>/<g:message
                                code="label.evdas.data.file.list.total"/></th>
                        <th><g:message code="label.upload.timestamp"/></th>
                        <th><g:message code="label.upload.uploaded.by" default="Added By"/></th>
                        <th><g:message code="label.evdas.data.file.list.status"/></th>
                    </tr>
                    </thead>
                </table>
            </div>

        </div>
    </div>
</div>
<g:render template="/includes/modals/evdasDataUploadLog"/>
<g:render template="/includes/modals/showEvdasFiles"/>

</body>
</html>