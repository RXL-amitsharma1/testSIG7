var signal = signal || {};

signal.list_utils = (function () {
    var flag_it = function (flag, id) {
        var theHtml = '<i class="fa fa-flag-o text-muted rx-list-flag" data-id="' + id + '"></i>'
        if (flag) {
            theHtml = '<i class="fa fa-flag text-primary rx-list-flag" data-id="' + id + '"></i>'
        }
        return theHtml
    };

    var flag_handler = function (controller, action) {
        $(document).on('click', '.rx-list-flag', function (e) {
            var ele = e.target;
            var id = $(ele).attr('data-id');

            $.ajax({
                url: "/signal/" + controller + "/" + action + "?id=" + id,
            })
                .done(function (result) {
                    if (result.flagged) {
                        $(ele).removeClass('fa-flag-o').removeClass('text-muted');
                        $(ele).addClass('fa-flag').addClass('text-primary');
                    } else {
                        $(ele).addClass('fa-flag-o').addClass('text-muted');
                        $(ele).removeClass('fa-flag').removeClass('text-primary')
                    }
                })
        })
    };

    var priority_link = function (priority, id) {
        var icon_url = compose_priority_icon(priority);
        return '<a href="#" class="change-priority"><img data-field="priority" data-info="row" data-id="' +
            id + '" data-value="' + priority + '" src="' + icon_url + '"/></a>'
    };

    var compose_priority_icon = function (priority) {
        var icon_url = "/signal/assets/icons/default_priority.png";

        if (_.contains(['high', 'low', 'medium'], priority.toLowerCase())) {
            icon_url = "/signal/assets/icons/" + priority.toLowerCase() + "_priority.png";
        }
        return icon_url;
    };

    var change_priority = function (priorityEle, priority) {
        var icon_url = compose_priority_icon(priority);
        $(priorityEle).attr("src", icon_url);
        $(priorityEle).attr("data-value", priority);
    };

    var change_priorityTest = function (priorityEle, priority) {
        $(priorityEle).attr("data-value", priority);
    };

    change_priorityTest

    var find_field = function (table_row_ele, attr_name) {
        return $(table_row_ele).find("[data-attribute-name='" + attr_name + "']")
    };

    var set_value = function (table_row_ele, attr_name, id, data_fun, app_name) {
        var ele = find_field(table_row_ele, attr_name);
        ele.html(data_fun(id, app_name));
    };

    var get_due_in = function (id, app_name) {
        var dueIn = 0;
        $.ajax({
            url: "/signal/alert/dueIn?alertId=" + id + "&appName=" + app_name,
            async: false,
        })
            .done(function (data) {
                dueIn = data.result;
            });
        return dueIn
    };

    var due_in_comp = function (value) {
        if (value <= 0) {
            return "<div data-attribute-name='dueIn' style = 'color:red'>" + value + "</div>";
        } else {
            return "<div data-attribute-name='dueIn'>" + value + "</div>";
        }
    };

    var assigned_to_comp = function (id, assignedTo) {
        var html = '<div class="assignedToContainer"><select class="assignedToSelect form-control select2"></select><i class="mdi mdi-spin mdi-loading assignToProcessing" style="display: none"></i></div>';
        return html
    };

    var truncateTextAndShowTooltip = function (cutoff, wordbreak, escapeHtml) {
        var esc = function (t) {
            return t
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;');
        };

        return function (d, type, row) {
            // Order, search and type get the original data
            if (type !== 'display') {
                return d;
            }

            if (typeof d !== 'number' && typeof d !== 'string') {
                return d;
            }

            d = d.toString(); // cast numbers

            if (d.length <= cutoff) {
                return esc(d);
            }

            var shortened = d.substr(0, cutoff - 1);

            // Find the last white space character in the string
            if (wordbreak) {
                shortened = shortened.replace(/\s([^\s]*)$/, '');
            }

            // Protect against uncontrolled HTML input
            if (escapeHtml) {
                shortened = esc(shortened);
            }

            return '<span class="ellipsis" title="' + esc(d) + '">' + shortened + '&#8230;</span>';
        };
    };

    var add_filters = function (table, filters, filter_toggle_bt_container) {

        var yadcf_filters = _.filter(filters, function (f) {
            if (f[1] != 'customized') {
                return true;
            } else {
                return false;
            }
        });

        var cust_filters = _.difference(filters, yadcf_filters);

        yadcf.init(table, _.map(yadcf_filters, function (item) {
            if (item[2] == true)
                return {column_number: item[0], filter_type: item[1], filter_reset_button_text: 'x'};
            else
                return {column_number: item[0], filter_type: item[1], filter_reset_button_text: false};
        }));

        if (filter_toggle_bt_container) {
            add_filter_toggle_button('idxxxxxx', filter_toggle_bt_container);
        }

        if (cust_filters) {
            _.each(cust_filters, function (f) {
                $(table.column(f[0]).header()).append(create_stacked_filter(f[0]));
            });
        }
    };

    var create_stacked_filter = function (idx) {
        return $("<input type='text' data-index='" + idx + "' class='column-filter' placeholder='Type to filter'>" +
            "<input type='text' data-index='" + idx + "' class='column-filter' placeholder='Type to filter'>");
    };

    var add_filter_toggle_button = function (table_id, container) {
        $('.yadcf-filter-wrapper, .column-filter').hide();
        $('.column-filter').on('click', function (evt) {
            evt.preventDefault();
            return false;
        });
        $('.column-filter').on('keyup', function (evt) {
            // Perform search
            var index = $(this).data('index');
            $(table_id).DataTable().column(index).search($(this).val()).draw();
        });
        var filterToggle = "<i class='table-filter-toggle glyphicon glyphicon-filter' data-table='" + table_id +
            "' onclick='signal.list_utils.handle_filter_toggle' data-show-filter='true'></i>";
        var toggle_button = $.parseHTML(filterToggle);
        if (!(_.isUndefined(container) && _.isNull(container))) {
            container.append(toggle_button);
        }

        $(document).on('filter-toggle-init', function (evt) {
            $('.table-filter-toggle').on('click', function (evt) {
                var hide_show_fitler = function (tableId, hideOrShow) {
                    if (hideOrShow === false) {
                        $(tableId + '_wrapper .yadcf-filter-wrapper,.column-filter').hide();
                    } else {
                        $(tableId + '_wrapper .yadcf-filter-wrapper,.column-filter').show();
                    }
                };

                var targetFilterToggle = $(evt.target);
                var tableId = targetFilterToggle.data('table');
                var showFilter = targetFilterToggle.data('show-filter');
                if (showFilter === 'true') {
                    targetFilterToggle.data('showFilter', 'false');
                    hide_show_fitler(tableId, false);
                } else {
                    targetFilterToggle.data('showFilter', 'true');
                    hide_show_fitler(tableId, true);
                }
            });
        });

        $(document).trigger('filter-toggle-init');
        return $(toggle_button);
    };

    var handle_filter_toggle = function (evt) {
        $(this).attr('target-table');
    };

    return {
        flag_it: flag_it,
        flag_handler: flag_handler,
        priority_link: priority_link,
        change_priority: change_priority,
        find_field: find_field,
        set_value: set_value,
        get_due_in: get_due_in,
        due_in_comp: due_in_comp,
        assigned_to_comp: assigned_to_comp,
        truncateTextAndShowTooltip: truncateTextAndShowTooltip,
        add_filters: add_filters,
        handle_filter_toggle: handle_filter_toggle,
        add_filter_toggle_button: add_filter_toggle_button
    }
})();