var signal = signal || {};

signal.fieldManagement = (function () {

    var fieldConfigurationBar;
    var fieldList = [];
    var initOrder = true;
    var url;
    var updateUrl;

    var updateFieldListForProductAssignment = function (columnOrder/*,caseListTable,tableId,fixedColCount*/) {
        url = columnOrderUrl+"?isProductView="+isProductView;
        fieldList = JSON.parse(columnOrder)
    };

    var manageFieldConfigurationPanel = (function () {

        //toggle the side panel
        var toggleBackdrop = function () {
            if ($('.field-config-bar-toggle').data("backdrop")) {
                var rightPanelBackdrop = "<div class='fieldconfig-backdrop'></div>";
                if ($("#wrapper").find(".fieldconfig-backdrop").length < 1) {
                    $(rightPanelBackdrop).appendTo("#wrapper");
                } else {
                    $(".fieldconfig-backdrop").remove();
                }
            }
        };

        var adjustColumnsBarWidth = function(){
            if (typeof allFourEnabled !== 'undefined' && typeof allThreeEnabled !== 'undefined' && typeof anyTwoEnabled !== 'undefined' && typeof anyOneEnabled !== 'undefined') {
                columnsBarWidth = allFourEnabled ? 1240 : allThreeEnabled ? 1039 : anyTwoEnabled ? 840 : anyOneEnabled ? 649 : 431;
                return columnsBarWidth;
            }
            return 431;
        };

        //move the side panel
        var showFieldConfigurationPanel = function () {
            if (fieldConfigurationBar.css("right") === "0px") {
                fieldConfigurationBar.css("right", -adjustColumnsBarWidth());
            } else {
                fieldConfigurationBar.css("right", "0");
            }
        };

        // hide/show side panel
        var hideFieldConfigurationPanel = function () {
            fieldConfigurationBar.css("right", -adjustColumnsBarWidth()); // hide panel
            toggleBackdrop();
        };

        //bind sort event with the fields
        var bindFieldConfigurationSortEvent = function () {
            fieldConfigurationBar.find(".display-config-field").sortable({
                connectWith: ".short-field",
                stop: function (event, ui) {
                    var isSortable = true;
                    var targetList = ui.item.parent();
                    var element = ui.item.text();
                    if(element.endsWith("(F)")){
                        isSortable = targetList.hasClass("display-config-field3") || targetList.hasClass("display-config-field")
                    } else if(element.endsWith("(E)")){
                        isSortable = targetList.hasClass("display-config-field4") || targetList.hasClass("display-config-field")
                    } else if(element.endsWith("(VA)")){
                        isSortable = targetList.hasClass("display-config-field5") || targetList.hasClass("display-config-field")
                    } else if(element.endsWith("(VB)")){
                        isSortable = targetList.hasClass("display-config-field6") || targetList.hasClass("display-config-field")
                    } else {
                        isSortable = targetList.hasClass("display-config-field2") || targetList.hasClass("display-config-field")
                    }
                    if (!isSortable) {
                        $( this ).sortable("cancel");
                    }
                }
            }).disableSelection();

            fieldConfigurationBar.find(".display-config-field2, .display-config-field3, .display-config-field4, .display-config-field5, .display-config-field6").sortable({
                connectWith: ".short-field-primary"
            }).disableSelection();
        };

        //build the side panel with corresponding columns
        var buildFieldViewConfiguration = function (fieldContainerReference) {

            //filter the primary fields
            var filterPrimaryFields = function () {
                return _.filter(fieldList, function (ele) {
                    return ele.containerView === 1;
                });
            };

            //filter the optional fields safetyDB
            var filterOptionalSDBFields = function () {
                return _.filter(fieldList, function (ele) {
                    return ele.containerView === 2;
                });
            };

            //filter the optional fields
            var filterOptionalFields = function () {
                return _.filter(fieldList, function (ele) {
                    return ele.containerView === 3;
                });
            };

            //filter the optional fields evdasDB
            var filterOptionalEDBFields = function () {
                return _.filter(fieldList, function (ele) {
                    return ele.containerView === 4;
                });
            };

            //filter the optional fields vaersDB
            var filterOptionalVAERSFields = function () {
                return _.filter(fieldList, function (ele) {
                    return ele.containerView === 5;
                });
            };

            //filter the optional fields vigibaseDB
            var filterOptionalVIGIBASEFields = function () {
                return _.filter(fieldList, function (ele) {
                    return ele.containerView === 6;
                });
            };

            //adding field item to the side panel
            var addFieldItems = function (field, index_start) {
                var build_field_item = function (fieldItem) {
                    return '<a href="#" class="list-group-item" data-field="' + fieldItem.name + '">' + fieldItem.label + '</a>';
                };

                var listOrder = index_start;

                return _.reduce(field, function (memo, item) {
                    if (!_.has(item, 'listOrder'))
                        item.listOrder = listOrder++;

                    memo = memo + build_field_item(item) + "\n";
                    return memo;
                }, "");
            };

            //build list of primary fields
            var buildPrimaryFieldList = function () {
                fieldConfigurationBar.find('.list-group-primary').html(addFieldItems(_.sortBy(filterPrimaryFields(), function (ele) {
                    return ele.listOrder;
                }), 0));
            };

            //build list of optionalsdb fields
            var buildOptionalSDBFieldList = function () {
                fieldConfigurationBar.find('.list-group-optionalsdb').html(addFieldItems(filterOptionalSDBFields(), 0));
            };

            //build list of optional fields
            var buildOptionalFieldList = function () {
                fieldConfigurationBar.find('.list-group-optional').html(addFieldItems(filterOptionalFields(), 0));
            };

            //build list of optionaledb fields
            var buildOptionalEDBFieldList = function () {
                fieldConfigurationBar.find('.list-group-optionaledb').html(addFieldItems(filterOptionalEDBFields(), 0));
            };

            //build list of optionalVAERS fields
            var buildOptionalVAERSFieldList = function () {
                fieldConfigurationBar.find('.list-group-optionalvdb').html(addFieldItems(filterOptionalVAERSFields(), 0));
            };

            //build list of optionalVIGIBASE fields
            var buildOptionalVIGIBASEFieldList = function () {
                fieldConfigurationBar.find('.list-group-optionalvgdb').html(addFieldItems(filterOptionalVIGIBASEFields(), 0));
            };


            //fetch the column sequence
            var init = function () {
                $.ajax({
                    url: url,
                    cache: false,
                    async: false,
                })
                    .done(function (data) {
                        if (data && data.length > 0) {
                            fieldList = _.collect(data, function (field) {
                                var fieldObject = {};
                                fieldObject.name = field.name;
                                if (typeof field.label === "undefined") {
                                    fieldObject.label = field.display;
                                } else {
                                    fieldObject.label = field.label;
                                }
                                fieldObject.seq = field.seq;
                                fieldObject.listOrder = field.listOrder;
                                fieldObject.containerView = field.containerView;
                                return fieldObject;
                            });
                        }
                        setColumnSeq(fieldList);
                        buildPrimaryFieldList();
                        buildOptionalSDBFieldList();
                        buildOptionalFieldList();
                        buildOptionalEDBFieldList();
                        buildOptionalVAERSFieldList();
                        buildOptionalVIGIBASEFieldList();
                    })
            };

            init();
        };

        //load the side panel
        var loadFieldConfiguration = function () {
            var fieldContainerReference = fieldConfigurationBar.data('fieldcontainerreference');
            buildFieldViewConfiguration(fieldContainerReference);
        };

        var init = function (showPanel, isLoadConfig) {
            if (showPanel) {
                showFieldConfigurationPanel();
            }
            loadFieldConfiguration();
            if(isLoadConfig == true) {
                bindFieldConfigurationSortEvent();
            }
        };

        //build column list for each section
        var build_column_list = function (parentComp, containerNumber, defaultValue) {
            var setSeq = function (eleName) {
                var obj = _.find(defaultValue, function (n) {
                    return n.name === eleName;
                });

                return obj.seq;
            };
            var listOrder = 0;
            return _.reduce(parentComp.children("a.list-group-item"), function (memo, ele) {
                var label = $(ele).html();
                var name = $(ele).attr('data-field');

                var obj = {};
                obj.label = label;
                obj.name = name;
                obj.containerView = containerNumber;
                obj.seq = setSeq(name);
                obj.listOrder = listOrder++;
                memo.push(obj);

                return memo;
            }, [])
        };
        var primaryColumns = function (sort) {
            var primaryColumns = _.filter(fieldList, function (ele) {
                return ele.containerView === 1;
            });
            if (sort) {
                return _.sortBy(primaryColumns, function (ele) {
                    return ele.listOrder;
                });
            } else {
                return primaryColumns;
            }
        };

        //build new column order
        var buildColumnOrder = function (tableId, fixedColCount) {
            var columnSize = $(tableId).DataTable().columns().nodes().length;

            var getPreDefOrder = function () {
                var preDefOrder = [];
                for (var i = 0; i < columnSize; i++) {
                    preDefOrder.push({idx: i, name: $(tableId).DataTable().columns().dataSrc()[i]})
                }
                return preDefOrder
            };

            var buildNewOrder = function () {
                var preDefOrder = getPreDefOrder();
                var newOrder = [];
                var sortedPrimaryCols = primaryColumns(true);
                if (initOrder) {
                    initOrder = false;
                    _.each(sortedPrimaryCols, function (cfg) {
                        var found = _.find(preDefOrder, function (pd) {
                            return cfg.name === pd.name;
                        });
                        if (found)
                            newOrder.push(found.idx);
                    });
                    return newOrder;
                } else {
                    newOrder = _.collect(sortedPrimaryCols, function (colCfg) {
                        var found = _.find(preDefOrder, function (ele) {
                            return colCfg.name === ele.name;
                        });
                        var sequence;
                        if (found) {
                            sequence = colCfg.seq;
                        } else {
                            sequence = 0;
                        }
                        return sequence;
                    });
                    return newOrder;
                }
            };
            var init = function () {
                var fixedColumns = _.range(0, fixedColCount);
                var columns = _.range(0, columnSize);
                var newOrder = buildNewOrder();
                var restColumns = _.filter(columns, function (i) {
                    return !_.contains(newOrder, i)
                });
                var finalOrder = newOrder.concat(restColumns);
                finalOrder = _.difference(finalOrder, fixedColumns);
                finalOrder = _.union(fixedColumns, finalOrder);
                return finalOrder;
            };

            return init();
        };

        //save the new column sequence on backend
        var saveColumns = function (caseListTable, tableId, fixedColCount, isReorder) {
            var buildColumnConfigList = function () {
                var order = 0;
                _.each(fieldList, function (ele) {
                    ele.listOrder = 9999;
                });

                _.each(primaryColumns(), function (ele) {
                    ele.listOrder = order++;
                });

                return fieldList;
            };

            var colList = buildColumnConfigList();
            if(!isReorder) {
                caseListTable.colReorder?.order(buildColumnOrder(tableId, fixedColCount), true);
                setColumnVisibility(fieldList, caseListTable, fixedColCount);
            }
            caseListTable.draw();

            setTimeout(function() {
                $('.yadcf-filter-wrapper').hide();
            },400)



            //update column sequence on back end
            $.ajax({
                url: updateUrl,
                method: 'POST',
                data: {
                    'columnList': JSON.stringify(colList)
                },
            })
                .done(function (result) {});
        };

        //update the column visibility
        var setColumnVisibility = function (columnConfigs, dataTable, fixedColCount) {
            var visibleColumns = [];
            var hiddenColumns = [];

            _.each(columnConfigs, function (configObj, index) {
                if (configObj.containerView === 1) {
                    (typeof alertType != "undefined" && alertType != null && alertType === 'adhoc') ? visibleColumns.push(index + fixedColCount - 1) : visibleColumns.push(index + fixedColCount) // added for bug/PVS-58947
                } else {
                    (typeof alertType != "undefined" && alertType != null && alertType === 'adhoc') ? hiddenColumns.push(index + fixedColCount - 1) : hiddenColumns.push(index + fixedColCount)
                }
            });


            dataTable.columns(visibleColumns).visible(true,false);
            dataTable.columns(hiddenColumns).visible(false,false);

        };

        //process the new rearranged column order and visibility
        var processList = function (caseListTable, tableId, fixedColCount) {
            var tmpConfigs = build_column_list(fieldConfigurationBar.find('.list-group-primary'), 1, fieldList);
            if (typeof apptype !== 'undefined' && apptype === 'Aggregate Case Alert' && (isJaderAvailable == "false" || isJaderAvailable == false)) {
                if (tmpConfigs.length <= 80) {
                    tmpConfigs = tmpConfigs.concat(build_column_list(fieldConfigurationBar.find('.list-group-optionalsdb'), 2, fieldList));
                    tmpConfigs = tmpConfigs.concat(build_column_list(fieldConfigurationBar.find('.list-group-optional'), 3, fieldList));
                    tmpConfigs = tmpConfigs.concat(build_column_list(fieldConfigurationBar.find('.list-group-optionaledb'), 4, fieldList));
                    tmpConfigs = tmpConfigs.concat(build_column_list(fieldConfigurationBar.find('.list-group-optionalvdb'), 5, fieldList));
                    tmpConfigs = tmpConfigs.concat(build_column_list(fieldConfigurationBar.find('.list-group-optionalvgdb'), 6, fieldList));
                    fieldList = tmpConfigs;
                    saveColumns(caseListTable, tableId, fixedColCount);
                } else {
                    $.Notification.notify('warning', 'top right', "Warning", "A maximum of 80 fields are allowed as part of primary view fields, apart from the standard fields. Please remove the additional fields and save the changes again", {autoHideDelay: 5000});
                }
            } else {
                tmpConfigs = tmpConfigs.concat(build_column_list(fieldConfigurationBar.find('.list-group-optional'), 3, fieldList));
                fieldList = tmpConfigs;
                saveColumns(caseListTable, tableId, fixedColCount);
            }
        };

        //return visibility based on column map
        var visible = function (name) {
            var labelConfigCopyJson = $("#labelConfigCopyJson").val()
            if (typeof labelConfigCopyJson === "undefined") {
                var v = _.find(fieldList, function (obj) {
                    return obj.name === name && obj.containerView === 1;
                });
            } else {
                labelConfigCopyJson = JSON.parse(labelConfigCopyJson)
                var v = _.find(fieldList, function (obj) {
                    if (labelConfigCopyJson.hasOwnProperty(name)) {
                        return obj.name === name && obj.containerView === 1 && labelConfigCopyJson[name] == true;
                    }
                    return obj.name === name && obj.containerView === 1;
                });
            }
            return !_.isEmpty(v);
        };

        return {
            init: init,
            hideFieldConfigurationPanel: hideFieldConfigurationPanel,
            processList: processList,
            buildColumnOrder: buildColumnOrder,
            visible: visible,
            saveColumns:saveColumns
        }

    })();

    var init = function (caseListTable, tableId, fixedColCount, isLoadConfig) {
        var configuredId = $("#configureQualitativeFields, #configureQuantitativeOnDemandFields, #ic-configureQuantitativeOnDemandFields, #configureQualitativeOnDemandFields, #configureEvdasOnDemandFields, #ic-configureEvdasOnDemandFields, #ic-configureQualitativeOnDemandFields, #ic-configureQualitativeFields, #configureValidatedSignalFields,#ic-configureValidatedSignalFields, #configureQuantitativeFields,#ic-configureQuantitativeFields,#ic-configureEvdasFields, #configureEvdasFields, #configureAdhocFields, #ic-configureAdhocFields, #configureLiteratureFields, #ic-configureLiteratureFields,#configureProductAssignmentFields");
        configuredId.on('click', function (event) {
            event.preventDefault();
            fieldConfigurationBar = $('#' + $(this).data('fieldconfigurationbarid'));
            manageFieldConfigurationPanel.init(true, isLoadConfig);
        });

        var flag=false;
        //when user clicks on save button
        $('#btnSaveListConfig').off('click').on('click', function (e) {
            if ($('#select-all').is(':checked')) {
                flag=true;
            }
            $(".tooltip").hide();
            const table = $('#alertsDetailsTable').DataTable()
            table.on('draw', function () {
                if(flag===true){
                    $(".select-all-check input#select-all").prop('checked', true);
                    $(".alert-select-all").prop('checked',true);
                    $('.evdas-header-row input#select-all').prop("checked",true);
                    $('#literatureDetailsTableRow input#select-all').prop("checked",false);
                    flag=false;
                }
            });
            e.preventDefault();
            manageFieldConfigurationPanel.processList(caseListTable, tableId, fixedColCount);
            manageFieldConfigurationPanel.hideFieldConfigurationPanel();
            $(document).trigger('fieldsRearranged', table);
            selectedFilter=true;
        });

        //when user clicks on close button
        $('#btnCloseListConfig').on('click', function (event) {
            event.preventDefault();
            manageFieldConfigurationPanel.hideFieldConfigurationPanel();
        });
        caseListTable.colReorder?.order(manageFieldConfigurationPanel.buildColumnOrder(tableId, fixedColCount), true);
    };

    //populate the side panel when page loads for the first time
    var populateColumnList = function (urlParam, updateUrlParam) {
        fieldConfigurationBar = $('#qualitativeFields, #quantitativeOnDemandFields, #validatedSignalFields, #quantitativeFields, #evdasFields, #adhocFields, #productFields, #qualitativeOnDemandFields, #evdasOnDemandFields');
        url = urlParam;
        updateUrl = updateUrlParam;
        manageFieldConfigurationPanel.init(false);
    };

    //set column visibility when page loads
    var visibleColumns = function (name) {
        return manageFieldConfigurationPanel.visible(name)
    };

    //bind sort event with the fields
    var bindFieldConfigurationSortEvent = function () {
        fieldConfigurationBar.find(".display-config-field").sortable({
            connectWith: ".short-field",
            stop: function (event, ui) {
                var isSortable = true;
                var targetList = ui.item.parent();
                var element = ui.item.text();
                if(alertType && (alertType.startsWith("Aggregate Case Alert on Demand") || alertType.startsWith("Aggregate Case Alert - SMQ on Demand"))) {
                    isSortable = targetList.hasClass("display-config-field2") || targetList.hasClass("display-config-field")
                }else {
                    if (element.endsWith("(F)")) {
                        isSortable = targetList.hasClass("display-config-field3") || targetList.hasClass("display-config-field")
                    } else if (element.endsWith("(E)")) {
                        isSortable = targetList.hasClass("display-config-field4") || targetList.hasClass("display-config-field")
                    } else if (element.endsWith("(VA)")) {
                        isSortable = targetList.hasClass("display-config-field5") || targetList.hasClass("display-config-field")
                    } else if (element.endsWith("(VB)")) {
                        isSortable = targetList.hasClass("display-config-field6") || targetList.hasClass("display-config-field")
                    } else {
                        isSortable = targetList.hasClass("display-config-field2") || targetList.hasClass("display-config-field")
                    }
                }
                if (!isSortable) {
                    $( this ).sortable("cancel");
                }
            }
        }).disableSelection();

        fieldConfigurationBar.find(".display-config-field2, .display-config-field3, .display-config-field4, .display-config-field5, .display-config-field6").sortable({
            connectWith: ".short-field-primary"
        }).disableSelection();
    };

    return {
        init: init,
        visibleColumns: visibleColumns,
        populateColumnList: populateColumnList,
        updateFieldListForProductAssignment :updateFieldListForProductAssignment,
        bindFieldConfigurationSortEvent: bindFieldConfigurationSortEvent

    }
})();
