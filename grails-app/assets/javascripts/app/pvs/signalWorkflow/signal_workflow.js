$(function () {

    $("#signal-workflowRule-groups").select2();
    $("#allowedDispositions").select2();
    $(".from-state").select2();
    $(".to-state").select2();

    var initSignalWorkflowRuleTable = function () {
        var columns = create_signal_workflow_rule_table_columns();
        var table = $('#signalWorkflowRuleTable').DataTable({

            layout: {
                topStart: ['pageLength', 'search'],
                topEnd: null,
                bottomStart: ['info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            search: {
                smart: false
            },
            "ajax": {
                "url": signalWorkflowRuleUrl,
                "dataSrc": "ruleList",
            },
            drawCallback: function () {
                var rowsSignalworkflow = $('#signalWorkflowRuleTable').DataTable().rows().data();
                colEllipsis();
                webUiPopInit();
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            "initComplete": function () {
                var buttonEnableWorkflow = ''
                var buttonNewSignalWorkflowRule = ''
                if(typeof isEditingAllowed !== 'undefined' && isEditingAllowed) {
                    if(typeof enableWorkflow !== 'undefined' && enableWorkflow){
                        buttonEnableWorkflow = '<a class="btn btn-primary m-r-15 enable-workflow" href="#" >' + "Disable Signal Workflow" + '</a> ';
                    } else {
                        buttonEnableWorkflow = '<a class="btn m-r-15 enable-workflow disable-btn-workflow" href="#" >' + "Enable Signal Workflow" + '</a> ';
                    }
                    buttonNewSignalWorkflowRule = '<a class="btn btn-primary m-r-15" href="' + createUrl + '" >' + "New Signal Workflow Rule" + '</a> ';
                }
                var buttonSignalWorkflowList = '<a class="btn btn-primary m-r-15" href="' + signalWorkflowListUrl + '">' + "Signal Workflow List" + '</a> ';
                var buttonSignalWorkflowRule = '<a class="btn btn-primary m-r-15" href="' + signalWorkflowRule + '" >' + "Signal Workflow Rules" + '</a> ';
                var $divToolbar = $('.dt-search');
                $divToolbar.prepend(buttonEnableWorkflow + buttonSignalWorkflowList + buttonSignalWorkflowRule + buttonNewSignalWorkflowRule);
                addGridShortcuts(this);
                $('#signalWorkflowRuleTable').DataTable().draw();
            },
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "pagination": true,
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    };

    var initSignalWorkflowStateTable = function () {
        var columns = create_signal_workflow_state_table_columns();
        var table = $('#signalWorkflowStateTable').DataTable({

            layout: {
                topStart: ['pageLength', 'search'],
                topEnd: null,
                bottomStart: ['info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": signalWorkflowStateUrl,
                "dataSrc": "stateList",
            },
            "initComplete": function () {
                var buttonEnableWorkflow = ''
                var buttonNewSignalWorkflowRule = ''
                if(typeof isEditingAllowed !== 'undefined' && isEditingAllowed) {
                    if (typeof enableWorkflow !== 'undefined' && enableWorkflow) {
                        buttonEnableWorkflow = '<a class="btn btn-primary m-r-15 enable-workflow" href="#" >' + "Disable Signal Workflow" + '</a> ';
                    } else {
                        buttonEnableWorkflow = '<a class="btn m-r-15 enable-workflow disable-btn-workflow" href="#" >' + "Enable Signal Workflow" + '</a> ';
                    }
                    buttonNewSignalWorkflowRule = '<a class="btn btn-primary m-r-15" href="' + createUrl + '" >' + "New Signal Workflow Rule" + '</a> ';
                }
                var buttonSignalWorkflowList = '<a class="btn btn-primary m-r-15" href="' + signalWorkflowListUrl + '">' + "Signal Workflow List" + '</a> ';
                var buttonSignalWorkflowRule = '<a class="btn btn-primary m-r-15" href="' + signalWorkflowRule + '" >' + "Signal Workflow Rules" + '</a> ';
                var $divToolbar = $('.dt-search');
                $divToolbar.prepend(buttonEnableWorkflow + buttonSignalWorkflowList + buttonSignalWorkflowRule + buttonNewSignalWorkflowRule);
                addGridShortcuts(this);
            },
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "pagination": true,
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    };

    var create_signal_workflow_rule_table_columns = function () {
        var aoColumns = [
            {
                "className":"col-min-200 col-max-200",
                "mRender": function (data,type,row) {
                    return '<a href="' + '/signal/signalWorkflow/editSignalWorkflowRule' + '?' + 'id=' + row.id + '">' + addEllipsisWithEscape(row.ruleName) + '</a>';
                }
            },
            {
                "mData": "description",
                "className":"col-min-250 col-max-250 cell-break textPre",
                "mRender": function(data, type, row) {
                    var description= ''
                    if(row.description){
                        description = "<span>" + addEllipsisWithEscape(row.description) + "</span>";
                    }
                    return description;
                }
            },
            {
                "mData":"fromState",
                "className":"col-min-200 col-max-200"
            },
            {
                "mData": "toState",
                "className":"col-min-200 col-max-200"
            },
            {
                "mData": "allowedGroups",
                "className":"col-height word-break col-min-250 col-max-250"
            },
            {
                "mRender": function (data, type, row) {
                    if (row.display) {
                        return "<span>Yes</span>"
                    } else {
                        return "<span>No</span>"
                    }
                }
            }
        ];

        return aoColumns
    };

    var create_signal_workflow_state_table_columns = function () {
        var aoColumns = [
            {
                "className":"col-min-150 col-max-200",
                "mRender": function (data,type,row) {
                    return '<a href="' + '/signal/signalWorkflow/editSignalWorkflowState'  + '?' + 'id=' + row.id + '">' + escapeHTML(row.value) + '</a>';
                }
            },
            {
                "mData": "displayName",
                "className":"col-min-150 col-max-300 word-break"
            },
            {
                "mData":"allowedDispositions",
                "className":"col-min-150 col-max-200 word-break"
            },
            {
                "mRender": function (data, type, row) {
                    if (row.defaultDisplay) {
                        return "<span>Yes</span>"
                    } else {
                        return "<span>No</span>"
                    }
                }
            },
            {
                "mRender": function (data, type, row) {
                    if (row.dueInDisplay) {
                        return "<span>Yes</span>"
                    } else {
                        return "<span>No</span>"
                    }
                }
            }
        ];

        return aoColumns
    };

    initSignalWorkflowRuleTable();
    initSignalWorkflowStateTable();

    $(document).on('click', '.enable-workflow', function () {
        $this = $(this)
        if(typeof enableWorkflow !== 'undefined' && enableWorkflow) {
            $(this).text('Enable Signal Workflow');
            $(this).removeClass("btn-primary").addClass("disable-btn-workflow");
            enableWorkflow = false;
        } else {
            $(this).text('Disable Signal Workflow');
            $(this).removeClass("disable-btn-workflow").addClass("btn-primary");
            enableWorkflow = true;
        }
        $.ajax({
            url: enableSignalWorkflowUrl,
            data: {
                'enableWorkflow': enableWorkflow
            },
            dataType: 'json',
        })
            .done(function (response) {
                if(response.status) {
                    if (response.data) {
                        $.Notification.notify('success', 'top right', "Success", $.i18n._('enabledSignalWorkflow.success'), {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('success', 'top right', "Success", $.i18n._('disabledSignalWorkflow.success'), {autoHideDelay: 5000});
                    }
                } else {
                    if(response.message==="enableEndOfMilestone"){
                        $this.text('Enable Signal Workflow');
                        $this.removeClass("btn-primary").addClass("disable-btn-workflow");
                        enableWorkflow = false;
                        $.Notification.notify('warning', 'top right', "Warning", "Before enabling Signal workflow, the End of review milestone date auto population configuration option in the Signal Configurations section of the control panel should be disabled", {autoHideDelay: 5000});
                    }else{
                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                    }
                }
            });
    });

    $(".description-rule").attr('maxlength','255');
    addCountBoxToInputField(255, $('.description-rule'));

    if(typeof workflowStatesSignal !== "undefined") {
        $state = $('.to-state');
        var fromState = $('.from-state').val();

        function toStateValues(fromState, workflowStatesSignal) {
            $state.val(null).trigger("change.select2");
            $state.empty();
            $.each(workflowStatesSignal, function (index, workflowState) {
                if (workflowState !== fromState) {
                    $state.append(new Option(workflowState, workflowState, false, false)).trigger('change.select2');
                }
            });
        }

        $('.from-state').on('change', function () {
            fromState = $(this).val();
            toStateValues(fromState, workflowStatesSignal);
        });
    }

    $('#saveRule').on('click',function(){
        $(this).attr('disabled',true);
        $('#cancelButton').attr('disabled',true);
        $('#createSignalWorkFlowRule').submit();
    })
    $('#updateWorkflowState').on('click',function(){
        disableButton($(this),'/signal/signalWorkflow/updateWorkflowState',$('#updateForm'),$('#cancelState'))
    })
    $('#updateWorkflowRule').on('click',function(){
        disableButton($(this),'/signal/signalWorkflow/updateWorkflowRule',$('#updateWorkFlowRuleForm'),$('#deleteWorkflowRule'))
    })
    $('#deleteWorkflowRule').on('click',function(){
        disableButton($(this),'/signal/signalWorkflow/deleteWorkflowRule',$('#updateWorkFlowRuleForm'),$('#updateWorkflowRule'))
    })
    function disableButton(button,actionPath,form,neighbourButton){
        form.attr('action',actionPath)
        button.attr('disabled', true);
        neighbourButton.attr('disabled', true);
        form.submit();
    }
});
