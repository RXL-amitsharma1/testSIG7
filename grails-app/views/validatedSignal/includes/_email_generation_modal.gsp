<%@ page import="grails.util.Holders" %>
<div id="emailGenerationModal" class="modal fade" role="dialog">
    <div class="modal-dialog modal-lg">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title"><g:message code="email.generation.label.header"/></label>
            </div>

            <form id="emailGenerationForm" enctype="multipart/form-data" method="post">

                <div class="modal-body">
                    <div class="row">
                        <div class="col-sm-12">
                            <div class="row m-t-10 followUpReporters to mailReceiver">
                                <div class="col-md-12 required">
                                    <label for="sentTo" class="control-label lbl-elipsis"><g:message
                                            code="email.generation.label.to"/></label>
                                    <select id="sentTo"
                                            class="lbl-required form-control  select2 select2-box wrapped-multiselect select-split customValueMultiSelect2"
                                            name="sentTo"
                                            multiple="multiple">
                                    </select>
                                </div>
                            </div>


                            <div class="row m-t-10 mailSubject">
                                <div class="subject col-md-12 required">
                                    <label for="subject" class="control-label lbl-elipsis"><g:message
                                            code="email.generation.label.subject"/></label>
                                    <g:textField id="subject" name="subject" value=""  maxlength="255"
                                                class="form-control fm-text-area p-t-0"/>
                                </div>
                            </div><br>

                            <div class="modalFUQMsg col-md-12 required message">
                                <label for="emailContentMessage" class="control-label"><g:message
                                        code="email.generation.label.message" default="Email Content"/></label>
                                <textarea id="emailContentMessage" name="emailContentMessage" cols="100" rows="15"
                                          class="form-control hide" disabled></textarea>
                            </div>

                            <div class="attachments fileName m-t-10 col-md-12">
                                <table class="table table-striped pv-inner-table m-b-0">
                                    <thead>
                                    <tr>
                                        <th class="first-header">#</th>
                                        <th class="col-md-12  true"><label class="control-label lbl-elipsis"><g:message
                                                code="caseDetails.attachments"/></label></th>
                                        <th><a href="javascript:void(0);" class="pull-right add-attachment"
                                               data-tabular="true">
                                            <i class="fa fa-plus-square-o"></i></a>
                                        </th>
                                    </tr>
                                    </thead>
                                    <tbody class="pv-draggable-list ui-sortable" id="attachmentSection-followUp"
                                           style="">
                                    <tr class="row-table pv-section-record ui-sortable-handle hide deleted" data-id="">
                                        <td class="new-index badge-index">0</td>
                                        <td>
                                            <div class="col-sm-12 pv-col-attachment">
                                                <a href="javascript:void(0)" class="attachmentRow m-r-10"
                                                   data-deleted="false" data-filename="" data-storageid=""
                                                   data-redact="false"></a>
                                            </div>
                                        </td>
                                        <td>
                                            <a href="javascript:void(0);" title="Remove this section"
                                               class="pull-right delete-attachment">
                                                <i class="md md-close" aria-hidden="true"></i>
                                            </a>
                                        </td>
                                    </tr>
                                    <tr class="row-table pv-section-record ui-sortable-handle" style="">
                                        <td class="new-index badge-index first-index">1</td>
                                        <td>
                                            <div class="internal" data-id="attachmentFile">
                                                <div class="file-uploader" data-provides="fileupload">
                                                    <input type="file" name="file" data-mandatory="false" data-mandatoryset="0" class="file">
                                                    <div class="input-group file-attach">
                                                        <input type="text" class="form-control fileName" disabled="" placeholder="Attach a file" value="" name="followUpQuery.attachments[0].fileName">
                                                        <span class="input-group-btn ">
                                                            <button class="browse btn btn-primary btn-file-upload" type="button"><i class="glyphicon glyphicon-search"></i>
                                                            </button>
                                                        </span>
                                                    </div>
                                                    <div class="input-group attached-file hide"></div>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <a href="javascript:void(0);" title="Remove this section"
                                               class="pull-right delete-attachment">
                                                <i class="md md-close" aria-hidden="true"></i>
                                            </a>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>

                            <g:hiddenField id="emailGenerationId" name="emailGenerationId"/>
                            <g:hiddenField id="signalRmmId" name="signalRmmId"/>
                            <g:hiddenField id="maxFileSize" name="maxFileSize"
                                           value="${Holders.config.rxlogix.pvintake.fuq.fileMaxSize}"/>

                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-dark-grey waves-effect" id="emailGenResetBtn"><g:message
                            code="email.generation.label.reset"/></button>
                    <button data-dismiss="modal" class="btn pv-btn-grey waves-effect btn-default"><g:message
                            code="email.generation.label.cancel"/></button>

                    <g:actionSubmit class="btn btn-primary" action="sendEmailForRmms" id="sendMessage"
                                    type="submit"
                                    value="${message(code: "email.generation.label.send")}"/>

                </div>
            </form>

        </div>
    </div>
</div>


