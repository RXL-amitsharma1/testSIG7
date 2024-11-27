<div class="modal fade" id="save-view-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="view-modal-title">Save View</h4>
            </div>

            <div class="modal-body">
                <div class="row" >
                    <div class="alert alert-danger view-error" style="display: none"></div>
                </div>
                <div class="row">
                    <div class="col-md-12 form-group" id="select-view-div">
                        <label><g:message code="app.label.view.instance.view.select"/></label>
                        <g:select  name="viewsListSelect" id="viewsListSelect" from="${[]}" value="" class="form-control select2 view-select">
                        </g:select>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12 form-group">
                        <label><g:message code="app.label.view.instance.name"/><span class="required-indicator">*</span></label>
                        <input type="text" class="form-control" id="view_name" maxlength="50"/>
                    </div>
                </div>
                <g:if test="${isShareFilterViewAllowed}">
                    <div class="row">
                        <div class="col-md-12 form-group mw-110" style="padding-top: 3px;">
                            <g:initializeShareWithElement bean="${viewInstance}" shareWithId="viewSharedWith" isWorkflowEnabled="false"/>
                        </div>
                    </div>
                </g:if>
                <div class="row">
                    <div class="col-md-3 form-check">
                        <input type="checkbox" class="form-check-input" id="view_default" checked="${viewInstance?.defaultValue}">
                        <label class="form-check-label">Default View</label>
                    </div>
                </div>
            </div>
            <input type="hidden" id="view_filters" value=""/>
            <input type="hidden" id="view_columnList" value=""/>
            <input type="hidden" id="view_alertType" value=""/>
            <input type="hidden" id="view_sorting" value=""/>
            <input type="hidden" id="view_advanced_filter" value=""/>
            <input type="hidden" id="view_id" value=""/>
            <input type="hidden" id="current_view_id" value=""/>
            <input type="hidden" id="isViewUpdateAllowed" value="${isViewUpdateAllowed}"/>

            <div class="modal-footer">
                <div class="buttons save_buttons" >
                    <button type="button" class="btn btn-primary save-view">
                        Save
                    </button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        Cancel
                    </button>
                </div>
                <div class="buttons edit_buttons" style="display: none;">
                    <button type="button" class="btn btn-primary edit-view">
                        Save
                    </button>
                    <button type="button" class="btn btn-primary delete-view" data-dismiss="modal">
                        Delete
                    </button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        Cancel
                    </button>

                </div>
            </div>
        </div>
    </div>
</div>



<script>
    $(document).ready(function () {
        $('.save-view').on('click', function () {
            $(this).prop("disabled", true);
            var sharedWithData = $('#viewSharedWith').val() ? JSON.stringify($('#viewSharedWith').val()) : null;
            var request = new Object();
            request['name'] = $("#view_name").val();
            if(typeof isAdhocCaseSeries !== "undefined" && isAdhocCaseSeries !== null){
                request['isAdhocCaseSeries'] = isAdhocCaseSeries;
            }
            request['filterMap'] = $("#view_filters").val();
            request['columnList'] = $("#view_columnList").val();
            request['alertType'] = $("#view_alertType").val();
            request['sorting'] = $("#view_sorting").val();
            request['advancedFilter'] = $("#view_advanced_filter").val();
            request['currentViewId'] = $('#current_view_id').val();
            request['viewSharedWith'] = sharedWithData;
            request['defaultView'] = $('#view_default').prop('checked');
            $.ajax({
                url: saveViewUrl,
                type: "POST",
                data: request,
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        $('#save-view-modal').modal('hide');
                        var pageURL = $(location).attr("href");
                        var preIndex = pageURL.indexOf('viewId');
                        var selectedView = data.viewId;
                        if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1 || pageURL.indexOf("isVigibase") != -1) && pageURL.indexOf('&' , preIndex ) != -1) {
                            window.location = pageURL.slice(0, preIndex - 1) + pageURL.slice(pageURL.indexOf('&' , preIndex )) + "&viewId=" + selectedView
                        } else if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1 || pageURL.indexOf("isVigibase") != -1)) {
                            window.location = pageURL.slice(0, preIndex - 1) + "&viewId=" + selectedView
                        } else if (pageURL.indexOf("viewId") != -1) {
                            window.location = pageURL.slice(0, pageURL.indexOf("viewId") + 7) + selectedView
                        } else {
                            if (pageURL.indexOf("#") != -1) {
                                pageURL = pageURL.slice(0, pageURL.indexOf("#"))
                            }
                            window.location = pageURL + "&viewId=" + selectedView
                        }
                        if (data.errorMessage != '') {
                            showErrorNotification(data.errorMessage, 50000);
                        }
                    } else {
                        $('#save-view-modal .view-error').html(data.errorMessage);
                        $('#save-view-modal .view-error').show()
                        $('.save-view').prop('disabled', false);

                    }
                }
            })
        });
        $('.edit-view').on('click', function () {
            if ($("#isViewUpdateAllowed").val() == 'true') {
                updateViewInstance()
            } else {
                bootbox.confirm({
                    message: $.i18n._('defaultViewUpdateOnly'),
                    buttons: {
                        confirm: {
                            label: $.i18n._('continue'),
                            className: 'btn-success'
                        },
                        cancel: {
                            label: $.i18n._('cancel'),
                            className: 'btn-danger'
                        }
                    },
                    callback: function (result) {
                        if (result == true) {
                            updateViewInstance()
                        }
                    }
                });
            }
        });
        $('.delete-view').on('click', function () {
            sessionStorage.setItem('isViewCall', true);
            var request = new Object();
            request['id'] = $('#viewsListSelect').val();
            request['alertType'] = $("#view_alertType").val();
            $.ajax({
                url: deleteViewUrl,
                type: "POST",
                data: request,
                dataType: "json",
                success: function (data) {
                    if(data.success == true){
                        var pageURL = $(location).attr("href");
                        if (pageURL.indexOf("viewId") != -1) {
                            window.location = pageURL.slice(0, pageURL.indexOf("viewId") - 1)
                        } else {
                            window.location = pageURL
                            location.reload()
                        }
                    } else {
                        showErrorNotification(data.errorMessage, 10000);
                    }
                },
                error: function (data) {
                    showErrorNotification(data.errorMessage, 10000);
                }
            })
        });

        $('#save-view-modal').on('hidden.bs.modal', function () {
            $('#save-view-modal .save_buttons').hide();
            $('#save-view-modal .edit_buttons').hide();
            $('#save-view-modal .view-error').hide();
            $('#save-view-modal #view_name').val('');
        });


        var updateViewInstance = function () {
            var sharedWithData = $('#viewSharedWith').val() ? JSON.stringify($('#viewSharedWith').val()) : null;
            var request = new Object();
            request['id'] = $('#viewsListSelect').val();
            request['name'] = $("#view_name").val();
            request['filterMap'] = $("#view_filters").val();
            request['columnList'] = $("#view_columnList").val();
            request['alertType'] = $("#view_alertType").val();
            request['sorting'] = $("#view_sorting").val();
            request['viewSharedWith'] = sharedWithData;
            request['advancedFilter'] = $("#view_advanced_filter").val();
            request['defaultView'] = $('#view_default').prop('checked');
            $.ajax({
                url: updateViewUrl,
                type: "POST",
                data: request,
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        var pageURL = $(location).attr("href");
                        var preIndex = pageURL.indexOf('viewId');
                        var selectedView = data.viewId;
                        if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1 || pageURL.indexOf("isVigibase") != -1) && pageURL.indexOf('&' , preIndex ) != -1) {
                            window.location = pageURL.slice(0, preIndex - 1) + pageURL.slice(pageURL.indexOf('&' , preIndex )) + "&viewId=" + selectedView
                        } else if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1 || pageURL.indexOf("isVigibase") != -1)) {
                            window.location = pageURL.slice(0, preIndex - 1) + "&viewId=" + selectedView
                        } else if (pageURL.indexOf("viewId") != -1) {
                            window.location = pageURL.slice(0, pageURL.indexOf("viewId") + 7) + selectedView
                        } else {
                            if (pageURL.indexOf("#") != -1) {
                                pageURL = pageURL.slice(0, pageURL.indexOf("#"))
                            }
                            window.location = pageURL + "&viewId=" + selectedView
                        }
                        if (data.errorMessage != '') {
                            showErrorNotification(data.errorMessage, 10000);
                        }
                    } else {
                        $('#save-view-modal .view-error').html(data.errorMessage);
                        $('#save-view-modal .view-error').show()
                    }
                }
            })
        }
    });
</script>