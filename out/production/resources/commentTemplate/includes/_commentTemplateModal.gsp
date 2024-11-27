<%@ page import="grails.util.Holders; com.rxlogix.enums.ActionStatus; com.rxlogix.user.User; com.rxlogix.config.ActionType; com.rxlogix.config.ActionConfiguration;" %>

<div class="modal fade comment-template-modal" data-backdrop="static" role="dialog" aria-labelledby="comment-template-modal-label" data-keyboard="false" data-backdrop="static">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close close-comment-template" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <label class="rxmain-container-header-label click">Comment Template</label>
            </div>

            <div class="modal-body">
                <div class="msgContainer" style="display:none">
                    <div class="alert alert-danger" role="alert">
                        <span class="message"></span>
                    </div>
                </div>
                <div class="panel-group m-b-10">
                    <div class="panel panel-default">
                        <div class="panel-body">
                            <div class="row">
                                <div class="col-lg-6">
                                    <input type="hidden" id="labelConfig" value="${labelConfig}">
                                    <label for="template-name">
                                        Template Name
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <input type="text" id="template-name" value="" class="form-control" maxlength="255"/>
                                </div>
                            </div>
                        </div>
                        <div class="panel-body">
                            <div class="row">

                                <div class="col-lg-6 comment-template-count">
                                    <label>
                                        <g:message code="app.label.comment.counts" default="Type"/>
                                    </label>
                                    <select id="comment-template-counts" name="counts" class="form-control select2-accessible" multiple>
                                        <g:each in="${commentCountList}" var="countField">
                                                <option value="${countField.name}-val">${countField.display}</option>
                                        </g:each>
                                    </select>
                                </div>
                                <div class="col-lg-6 comment-template-score" style="margin-bottom: 10px">
                                    <label>
                                        <g:message code="app.label.comment.scores" default="Type"/>
                                    </label>
                                    <select id="comment-template-scores" name="scores" class="form-control select2-accessible" multiple>
                                        <g:each in="${commentScoresList}" var="scoreField">
                                                <option value="${scoreField.name}-val">${scoreField.display}</option>
                                        </g:each>
                                    </select>
                                </div>
                                <div>
                                    <button type="button" id="populate-comment" class="btn btn-primary id-element" style="float: right">Add</button>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-lg-12 form-group">
                                    <label for="comments">
                                        <g:message code="comment.details.label" default="Comments" />
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <g:textArea class="form-control tarea-200" name="comments" id="comment-template" value=""  />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="close-comment-template btn btn-default" >Close</button>
                    <button type="button" id="save-comment-template" class="btn btn-primary id-element">Save</button>
                </div>
            </div>
        </div>
    </div>
</div>