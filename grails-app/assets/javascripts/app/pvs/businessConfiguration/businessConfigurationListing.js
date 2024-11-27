//= require app/pvs/common/rx_common.js

$(function () {
    var table;
    signal.business_config_list_utils.init_business_config_table();
    signal.business_config_list_utils.delete_rule();
    signal.business_config_list_utils.init_collapsible();
    signal.business_config_list_utils.save_rule_rank();
    signal.business_config_list_utils.toggle_enable_rule();
});


var signal = signal || {};

signal.business_config_list_utils = (function () {

    var init_business_config_table = function () {
        var columns = create_business_table_columns();
        table = $('#' +
            'rxTableQueries').DataTable({

            layout: {
                topStart: ['buttons', 'search'],
                topEnd: null,
                bottomStart: ['info', {paging: {type: "full_numbers"}}],
                bottomEnd: null
            },
            customProcessing: true,
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            search: {
                smart: false
            },
            initComplete: function () {
                $('.dt-input').val("");
                var bcTable = $('#rxTableQueries').DataTable();
                if(!isAdmin){
                    bcTable.buttons().remove();
                }else{
                    actionButton('#rxTableQueries');
                }
                addGridShortcuts(this);
            },
            "ajax": {
                "url": listQueriesUrl,
                "dataSrc": ""
            },
            drawCallback: function (settings) {
                colEllipsis();
                webUiPopInit();
            },
            "createdRow": function (row, data, dataIndex) {
                $(row).addClass('business-configuration-parent-record');
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            buttons: [
                {
                    text: 'New Product Rule',
                    className: 'meeting-create btn-primary',
                    init: function (dt, node, config) {
                        $(node).attr('href', businessConfigurationCreateUrl + "?isGlobalRule=" + false);
                    },
                    action: function (e, dt, node, config) {
                        window.location.href = businessConfigurationCreateUrl + "?isGlobalRule=" + false;
                    }
                },
                {
                    text: 'New Global Rule',
                    className: 'meeting-create btn-primary',
                    init: function (dt, node, config) {
                        $(node).attr('href', businessConfigurationCreateUrl + "?isGlobalRule=" + true);
                    },
                    action: function (e, dt, node, config) {
                        window.location.href = businessConfigurationCreateUrl + "?isGlobalRule=" + true;
                    }
                }
            ],
            "aaSorting": [[5, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        loadTableOption('#rxTableQueries');
    };

    var create_business_table_columns = function () {
        var aoColumns = [
            {
                "className": 'details-control',
                "sortable": false,
                "data": null,
                "defaultContent": ''
            },
            {
                "mData": "ruleName",
                "sortable": true,
                "mRender": function (data, type, row) {
                    return '<span class="">' + escapeAllHTML(data) + '</span>';
                }
            },
            {
                "mData": "description",
                "sClass": "col-min-150 col-max-250 textPre",
                "mRender": function (data, type, row) {
                    return addEllipsisWithEscape(row.description);
                }
            },
            {"mData": "products",
                "sClass": "col-min-100 col-max-300",
                "mRender": function (data, type, row) {
                    return "<span class='word-wrap-break-word'>" + escapeHTML(row.products) + "</span>";
                }
            },
            {"mData": "modifiedBy",
                "mRender": function(data, type, full) {
                    if (data.toUpperCase() === SYSTEM_USER) {return SYSTEM_USER}
                    else
                        return data;
                }

            },
            {
                "mData": "lastModified",
                "sClass": "dataTableColumnCenter col-min-100 col-max-150",
                "mRender": function (data, type, row) {
                    if(row.lastModified=="null" || row.lastModified==null){
                        return '' ;
                    } else {
                        return moment.utc(row.lastModified).format('DD-MMM-YYYY hh:mm:ss A');
                    }
                }
            },
            {
                "mData": "enabled",
                "mRender": function (data, type, row) {
                    if (row.enabled) {
                        return '<i class="fa fa-check-circle-o" aria-hidden="true"></i>'
                    } else {
                        return '<i class="fa fa-ban" aria-hidden="true"></i>'
                    }
                }
            }
        ];
        if (isAdmin) {
            aoColumns.push.apply(aoColumns, [{
                "mData": null,
                "sortable": false,
                "aTargets": ["id"],
               "className":'col-min-100',
                "mRender": function (data, type, row) {
                    var url = businessConfigurationEditUrl + '/' + row.id + "?isGlobalRule=" + row.isGlobalRule;
                    var actionButton = '<div class="hidden-btn btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="' + url + '">Edit</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="javascript:deleteRecord(' + data.id + ')" data-instanceid="' + data["id"] + ' class="delete-record">' + $.i18n._('delete') + '</a></li>';

                    var toggleUrl = toggleEnableBCUrl + "?id=" + row.id + "&attribute=" + row.enabled;
                    if (row.enabled) {
                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="' + toggleUrl + '" class="delete-record">' + $.i18n._('disable') + '</a></li>';
                    } else {
                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="' + toggleUrl + '" class="delete-record">' + $.i18n._('enable') + '</a></li>'
                    }
                    actionButton = actionButton + "</ul></div>"
                    return actionButton;
                }
            }]);
        }
        return aoColumns
    };

    var delete_rule = function () {
        $(document).on('click', '.deleteRuleUrl', function (e) {
            e.preventDefault();
            var $this = $(this);
            bootbox.confirm({
                message: "Are you sure, you want to delete it?",
                buttons: {
                    confirm: {
                        label: 'Yes',
                        className: 'btn-primary'
                    },
                    cancel: {
                        label: 'No',
                        className: 'btn-default'
                    }
                },
                callback: function (result) {
                    console.log('This was logged in the callback: ' + result);
                    if (result) {
                        console.log($this.attr('href'));
                        window.location.href = $this.attr('href');
                    }
                }
            });
        });
    };

    var init_collapsible = function () {
        $('#rxTableQueries tbody').on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = table.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            }
            else {
                // Open this row
                row.child(format(row.data())).show();
                row.child().addClass('business-configuration-child-record');
                tr.addClass('shown');
                $('.rank-rule').on('click', function () {
                    $('#modalRuleOrder').modal('show');
                    $('#ruleOrderList').sortable();
                });
            }
        });
    };

    var save_rule_rank = function () {
        $('#saveRuleOrder').on('click', function () {
            var optionTexts = [];
            $("#ruleOrderList li input").each(function () {
                optionTexts.push($(this).val())
            });
            $('#ruleOrderArray').val(optionTexts);
            $('#modalRuleOrder').modal('hide');
        });
    };

    var toggle_enable_rule = function () {
        $(document).on('click', '.toggle-enable-rule', function (e) {
            e.preventDefault();
            var $this = $(this);
            var id = $(this).data('id');
            $.ajax({
                url: toggleEnableRuleUrl + '/' + id + '?attribute1=' + $(this).data('enabled'),
                async: false
            }).done(function (payload) {
                if (payload.status) {
                    var tr = $this.parents('.business-configuration-child-record').prev('.business-configuration-parent-record');
                    var row = table.row(tr);
                    row.child.hide();
                    tr.removeClass('shown');
                    row.child(format(row.data())).show();
                    row.child().addClass('business-configuration-child-record');
                    tr.addClass('shown');
                    showSuccessMsg(payload.message);
                } else {
                    showErrorMsg(payload.message);
                }
            });
        });
    };

    return {
        init_business_config_table: init_business_config_table,
        delete_rule: delete_rule,
        init_collapsible: init_collapsible,
        save_rule_rank: save_rule_rank,
        toggle_enable_rule: toggle_enable_rule
    }
})();

function deleteRecord(id) {
    bootbox.confirm({
        message: "Are you sure, you want to delete it?",
        buttons: {
            confirm: {
                label: 'Yes',
                className: 'btn-primary'
            },
            cancel: {
                label: 'No',
                className: 'btn-default'
            }
        },
        callback: function (result) {
            console.log('This was logged in the callback: ' + result);
            if (result) {
                window.location.href = deleteUrl + "/" + id;
            }
        }
    });
}


function registerHandleBarHelper() {
    Handlebars.registerHelper("inc", function (value, options) {
        return parseInt(value) + 1;
    });
    Handlebars.registerHelper("append", function (str1, str2, str3) {
        var result = str1 + '/' + str2;
        if (typeof str3 == 'boolean') {
            result += '?attribute1=' + str3;
        }
        return result;
    });
}

function processRuleList(ruleList) {
    var lastUpdated;
    businessInfo=ruleList? ruleList[0]:null ;
    ruleInfo = businessInfo.ruleInformations;
    $.each(ruleInfo, function (index, value) {
        lastUpdated = new Date(value.lastUpdated);
        ruleInfo[index].lastModified = moment.utc(lastUpdated).tz(userTimeZone).format('DD-MMM-YYYY hh:mm:ss A');
        if(ruleInfo[index].modifiedBy.toUpperCase()==SYSTEM_USER){
            ruleInfo[index].modifiedBy = SYSTEM_USER;
        }
        if (businessInfo.dataSource == RULE_FLAGS.PVA.toLowerCase())
            ruleInfo[index].ruleType = value.isSingleCaseAlertType ? RULE_FLAGS.ICR : RULE_FLAGS.AGGREGATE;
        else if(businessInfo.dataSource == RULE_FLAGS.FAERS.toLowerCase()){
            ruleInfo[index].ruleType = RULE_FLAGS.FAERS
        }else if(businessInfo.dataSource == RULE_FLAGS.VIGIBASE.toLowerCase()){
            ruleInfo[index].ruleType = RULE_FLAGS.VIGIBASE
        }else if(businessInfo.dataSource == RULE_FLAGS.JADER.toLowerCase()){
            ruleInfo[index].ruleType = RULE_FLAGS.JADER
        }
        else
            ruleInfo[index].ruleType = businessInfo.dataSource == RULE_FLAGS.VAERS.toLowerCase() ? RULE_FLAGS.VAERS : RULE_FLAGS.EVDAS;

    });
    return ruleInfo;
}

function addRulesModal(payload) {
    $(function () {
        var sortedList = $('.identifier-controls', payload).sort(function (lhs, rhs) {
            return parseInt($(lhs).attr("ruleRank")) - parseInt($(rhs).attr("ruleRank"));
        });
        $('#ruleOrderList').html('');
        $.each(sortedList.prevObject, function (index, value) {
            $('#ruleOrderList').append('<li class="list-group-item list-group-item-action" style="font-size: medium"><input type="hidden" value="' + value.id + '"/>' + escapeHTML(value.ruleName) + '</li>')
        })
    });
}

/* Formatting function for row details - modify as you need */
function format(d) {
    var data = {};
    $.ajax({
        url: fetchRulesUrl + '/' + d.id,
        async: false
    }).done(function (payload) {
        data.payload = processRuleList(payload);
        data.addRuleUrl = addRuleUrl + '/' + d.id;
        data.editRuleUrl = editRuleUrl;
        data.deleteRuleUrl = deleteRuleUrl;
        data.cloneRuleUrl = cloneRuleUrl;
        data.toggleEnableRuleUrl = toggleEnableRuleUrl;
        data.isAdmin = isAdmin
    });
    addRulesModal(data.payload);
    registerHandleBarHelper();
    return signal.utils.render('rules_table', data);
}

function getErrorMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
        '</button> ' + msg;
    '</div>';
    return alertHtml;
}


function getSuccessMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-success alert-dismissible" role="alert"> ' +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
        '</button> ' + msg;
    '</div>';
    return alertHtml;
}


function showErrorMsg(msg) {
    removeExistingMessageHolder();
    var alertHtml = getErrorMessageHtml(msg);
    $('body .messageContainer').prepend(alertHtml);
    $('body').scrollTop(0);
}

function showSuccessMsg(msg) {
    removeExistingMessageHolder();
    var alertHtml = getSuccessMessageHtml(msg);
    $('body .messageContainer').prepend(alertHtml);
    $('body').scrollTop(0);
}

function removeExistingMessageHolder() {
    $('.messageContainer').html("");
}