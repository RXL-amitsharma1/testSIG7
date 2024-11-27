var dataMiningVariable, dataMiningVariableValue, isMeddra = false, isOob = false, dmvSelectedData = [], data = [],
    tempArr = [], isAutocomplete = false, dicLevel, dicType, dataLoaded = false, validatable = false, DATASOURCE_PVA = 'pva';
$(function () {
    setTimeout(function () {
        initializeData()
    },100)
})
var initializeData=function () {
    $(".dmvPencil, .dmvEvent").hide();
    $("#dataMiningVariableValueDiv").children("#inputValue").hide();
    $("#dataMiningVariableString").empty();
    var selectedDb=$("#selectedDatasource").val();
    setDmvData();
    var checkState1 = false;
    var checkState2 = false;

    $("#dataMiningVariable").on('change', function () {
        var dataSheetOptions = $(".datasheet-options");
        dmvSelectedData = [];
        data = [];
        tempArr = [];
        if (this.value == null || this.value == "null" || this.value == "") {
            $("#dataMiningVariableValue").val(null);
            $("#dataMiningVariableString").empty();
            $('#foregroundQuery').on('change', function () {
                if($("#foregroundQuery").is(":checked")){ //saving the state of foregroundQuery checkbox before getting disabled
                    checkState1 = true;
                } else{
                    checkState1=false;
                }
            });
            $('#selectedDatasheet').on('change', function () {
                if ($("#selectedDatasheet").is(":checked")) {
                    checkState2 = true;
                } else {
                    checkState2 = false;
                }
            });
            if(checkState1){
                $("#foregroundQuery").prop("checked", true);
                $(".forgroundQuery").show();
            } else{
                $(".forgroundQuery").hide();
            }
            if(checkState2){
                $("#selectedDatasheet").prop("checked", true);
                dataSheetOptions.show();
            } else{
                dataSheetOptions.hide();
            }
            if(!$('#isMasterTemplateAlert').is(':checked')){
                $("#foregroundQuery").prop("disabled", false);
            }
            if(!$("#groupBySmq").is(":checked") && !$('#isMasterTemplateAlert').is(':checked')){
                $('#selectedDatasheet').prop('disabled',false);
            }
            if(selectedDb.includes('pva')){
                $('#studyDrugCases').prop("disabled",false);
            }
        } else {
            if($("#foregroundQuery").is(":checked")){
                checkState1 = true;
            }
            if($("#selectedDatasheet").is(":checked")){
                checkState2 = true;
            }
            $("#foregroundQuery").prop("checked", false);
            showHideForegroundQuery(false);
            $("#foregroundQuery").prop("disabled", true);
            init(this.value);
            $('#selectedDatasheet').prop('disabled',true);
            $('#selectedDatasheet').prop('checked',false);
            var dataSheetOptions = $(".datasheet-options");
            dataSheetOptions.hide();
            $('#studyDrugCases').prop("disabled",true);
            $('#studyDrugCases').prop("checked",false);
            $('#additionalOptions').addClass('hidden');
            $('#ignoreMedicationType').prop('checked',false);
            $('#considerPOI').prop('checked',false);

        }
        $("#inputValue").val("");
        setProductMining();
    })
    if (!JSON.parse(isOob)) {
        $(".dmvPencil").show();
    }
    if (JSON.parse(isMeddra)) {
        $(".dmvEvent").show();
    }
    setSelect2Values();
    if (!setSelect2($("#selectOperator1").val())) {
        removeSelect2();
    }
    modalOpener();

    $("#considerForegroundRun").on('change', function () {
        $("#foregroundSearch").val($(this).is(':checked'));
    });

    $("#productGroupSelect").on('change', function () {
        var productGroup=$("#productGroupSelect").val();
        var newProductGroup = $(".level-product-group").val();
        if (typeof productGroup !== "undefined" && productGroup !== null && productGroup !== "null") {
            greyoutCheckbox(true);
            $("#foregroundSearch").val(false);
            $('#considerForegroundRun').prop('checked', false);
        } else {
            if(typeof newProductGroup !== "undefined" && newProductGroup !== null && newProductGroup !== "null" &&  newProductGroup !== "") {
                greyoutCheckbox(true);
                $("#foregroundSearch").val(false);
                $('#considerForegroundRun').prop('checked', false);
            }
        }
    });
    var setProdDictFilter=function(count){
        var newProductGroup = $(".productGroupValues .dictionaryItem").length;
        var dmvValue= $("#dataMiningVariable").val();
        var selectedDb=$("#selectedDatasource").val();
        if(count>0 && selectedDb!=null && selectedDb!=='undefined' && selectedDb.includes('pva')){
            if(newProductGroup>0 || dmvValue!=null && dmvValue!="null"  && typeof dmvValue !=="undefined"){
                greyoutCheckbox(true);
                $("#foregroundSearch").val(false);
                $('#considerForegroundRun').prop('checked', false);
            }
            else{
                greyoutCheckbox(true);
            }
        }else{
            greyoutCheckbox(true);
            $("#foregroundSearch").val(false);
            $('#considerForegroundRun').prop('checked', false);
        }
    }
    $("#productModal").on('show.bs.modal', function () {
        var dataSource;
        var dmvValue= $("#dataMiningVariable").val();
        $(document).on('change', "#dataSourcesProductDict", function(event){
            dataSource=$(this).val();
            if(dataSource!=="pva" || dmvValue!=null && dmvValue!="null"  && typeof dmvValue !=="undefined"){
                greyoutCheckbox(true);
                $("#foregroundSearch").val(false);
                $('#considerForegroundRun').prop('checked', false);
            }
        });
        if (typeof $("#product_multiIngredient") != "undefined" && typeof $("#isMultiIngredient") != "undefined") {
            if($("#isMultiIngredient").val() == 'true') {
                $("#product_multiIngredient").prop('checked', true);
            } else {
                $("#product_multiIngredient").prop('checked', false);
            }
        }

        var foregroundSearchAttr = $("#foregroundSearchAttr").val();
        var foregroundSearch = $("#foregroundSearch").val();
        if (foregroundSearchAttr !== "") {
            sleep(1000).then(function () {
                if (JSON.parse(foregroundSearch)) {
                    if( typeof editAlert !== "undefined" &&(editAlert === "copy" || editAlert === "edit")) {
                        $('#considerForegroundRun').prop('checked', true);
                        greyoutCheckbox(false);
                    }
                }
            });
        }
        $(".prodDictFilterCol select").on('change', function () {
            var count=$(".prodDictFilterCol span span span span span.select2-selection__clear").length;
            setProdDictFilter(count);
        });

        $(".prodDictSearchColCalc select").on('change', function () {
            var count=$(".prodDictSearchColCalc span span span span span.select2-selection__clear").length;
            setProdDictFilter(count);
        });

    })
}

var greyoutCheckbox=function (flag) {
    $('#considerForegroundRun').prop('disabled', flag);
    if (flag) {
        $("#considerForegroundRun").css({
            "pointer-events": "none",
            "cursor": "not-allowed"
        })
    } else {
        $("#considerForegroundRun").css("pointer-events", "");
        $("#considerForegroundRun").css("cursor", "");
    }
}

var init = function (d) {
    $("#dataMiningVariableValueDiv").children("#inputValue").hide();
    $(".dmvPencil, .dmvEvent").hide();
    $("#dataMiningVariable").val(d);
    dataMiningVariable = $("#dataMiningVariable").val();
    setDmvData();
    dmvSelectedData = [];
    setSelect2Values();
    $("#selectValue1").val("").trigger("change");
    $("#dataMiningVariable").find('option').attr("data-level", dicLevel);
    if (!setSelect2($("#selectOperator1").val())) {
        removeSelect2();
    }
    if (setSelect2($("#selectOperator1").val())) {
        $("#inputValue").hide();
        $("#selectValue1").show();
    } else {
        removeSelect2();
        $("#inputValue").show();
        $("#selectValue1").hide();
    }
}

var setSelect2=function (opValue) {
    if(JSON.parse(isAutocomplete) && (opValue==OPERATOR_VALUE.CONTAINS || opValue==OPERATOR_VALUE.DOES_NOT_CONTAIN)){
        return false;
    }else if(!JSON.parse(isAutocomplete)){
        return false;
    }else{
        return true;
    }
}


var setDmvData = function () {
    $(".dmvPencil, .dmvEvent").hide();
    dataMiningVariableValue = $("#dataMiningVariableValue").val() != "" ? JSON.parse($("#dataMiningVariableValue").val()) : "";
    dataMiningVariable = $("#dataMiningVariable").val();
    if (dataMiningVariable != null && dataMiningVariable !== "null" && dataMiningVariable.indexOf(";") > -1) {
        var tempDataMiningVariable = dataMiningVariable.split(";");
        dataMiningVariable = tempDataMiningVariable[0];
        isMeddra = tempDataMiningVariable[1];
        isOob = tempDataMiningVariable[2];
        isAutocomplete = tempDataMiningVariable[3];
        dicLevel = tempDataMiningVariable[4];
        dicType = tempDataMiningVariable[5];
        validatable = tempDataMiningVariable[6];
        dmvOperations(dataMiningVariable, isMeddra, isOob);
        $("#dmvDataLevel").val(dicLevel);
        $("#dmvDataValidatable").val(validatable);
        if (typeof validatable === "undefined") {
            validatable = $("#dataMiningVariable").find('option:selected').attr("data-validatable");
        }
        if (dataMiningVariableValue.value !== "" && typeof dataMiningVariableValue !=="undefined") {
            $("#inputValue").val(dataMiningVariableValue.value);
            var dmvExistingData = "";
            if (dataMiningVariableValue.value != null && dataMiningVariableValue.value != "null" && dataMiningVariableValue.value != "") {
                dmvExistingData = dataMiningVariableValue.value.toString().split(dataMiningVariableValue.delimiter);
                for (var k = 0; k < dmvExistingData.length; k++) {
                    if (!dmvSelectedData.includes(dmvExistingData[k]) && dmvExistingData[k] != "") {
                        dmvSelectedData.push(dmvExistingData[k]);
                    }
                }
            }
        }
        $("#dataMiningVariableValueDiv").children("#inputValue").hide();
    }

    $(".dmvCopyAndPasteModal").on('shown.bs.modal', function () {
        var content = $("#inputValue").val();
        var delimiter = getCopyAndPasteDelimiter();
        if ($("#selectValue1").text() == null || $("#selectValue1").text() == "null") {
            content = "";
        }
        content=content.toString().replaceAll(',',delimiter);
        $(".dmvCopyAndPasteModal").find(".copyPasteContent").val(content);
        $(".dmvCopyAndPasteModal").find(".c_n_p_other_delimiter").val('');
    });
}

var getCopyAndPasteDelimiter = function() {
    var selectedValue = $('.dmvCopyAndPasteModal input:radio[name=delimiter]:checked').val();

    if (selectedValue === "none") {
        return null;
    } else if (selectedValue === "others" ) {
        var value = $('#copyAndPasteModal .c_n_p_other_delimiter').val();
        if (_.isEmpty(value))
            return null;
        else
            return value;
    } else
        return selectedValue;
}



var updateDmvVariable = function () {
    var dmvJson = {
        operator:typeof $("#selectOperator1").val()==="undefined"?"":$("#selectOperator1").val(),
        operatorDisplay: typeof  $("#selectOperator1 option:selected").text()==="undefined"?"":$("#selectOperator1 option:selected").text(),
        value: "",
        delimiter: DEFAULT_DELIMITER
    }
    for (var i = 0; i < dmvSelectedData.length; i++) {
        dmvJson.value = dmvJson.value + dmvSelectedData[i] + dmvJson.delimiter;
    }
    if ($("#inputValue").is(":visible")) {
        dmvJson.value = $("#inputValue").val();
    }
    if (dmvJson.value.charAt(dmvJson.value.length - 1) == dmvJson.delimiter) {
        dmvJson.value = dmvJson.value.substring(0, dmvJson.value.length - 1);
    }
    $("#inputValue").val(dmvJson.value);
    var dataMiningVariable = $("#dataMiningVariable").val();
    if (dataMiningVariable == null || dataMiningVariable == "null" || dataMiningVariable == "") {
        dmvJson.value = "";
        $("#dataMiningVariableString").empty();
    }
    $("#dataMiningVariableValue").val(JSON.stringify(dmvJson));
}


var dmvOperations = function (dmv, isMeddra, isOob) {
    var colSize;
    if ((JSON.parse(isMeddra) && !JSON.parse(isOob)) || (JSON.parse(isMeddra) && JSON.parse(isOob)) || (!JSON.parse(isMeddra) && !JSON.parse(isOob))) {
        colSize = 4;
    } else if (!JSON.parse(isMeddra) && JSON.parse(isOob)) {
        colSize = 6;
    } else {
        colSize = 6;
    }
    if (!(dmv == "" || dmv == null || dmv == "null")) {
        var operatorDiv = '<div class="toAddContainerQEV toAddContainer"><div class="col-xs-3" id="operatorDiv">'
        $.ajax({
            type: "GET",
            url: stringOperatorsUrl,
            dataType: 'json',
            async: false
        })
            .done(function (result) {
                operatorDiv += '<select name="selectOperator" id="selectOperator1" class="form-control expressionOp">'
                for (var i = 0; i < result.length; i++) {
                    var operatorArray = [OPERATOR_STRING.EQUALS, OPERATOR_STRING.NOT_EQUAL, OPERATOR_STRING.DOES_NOT_CONTAIN, OPERATOR_STRING.CONTAINS];
                    if ($.inArray(result[i].display, operatorArray) > -1) {
                        operatorDiv += '<option value="' + result[i].value + '">' + result[i].display + '</option>';
                    }
                }
                operatorDiv += '</select>';
        });
        operatorDiv += '</div>';
        var dataMiningVariableValueDiv = '<div id="dataMiningVariableValueDiv" class="form-group col-xs-' + colSize + ' expressionsNoPad showValue"><select type="text" name="selectValue1" id="selectValue1" style="width: 100%" class="form-control select2" placeholder="Value" value=""><input type="text" name="selectValue1" id="inputValue" class="form-control expressionValueText" placeholder="Value" value=""></div>';
        var searchEventIcon = '<i class="fa fa-search searchEventDmv dmvEvent" id="searchEvents" data-toggle="modal" data-target="#eventModal"></i>';
        var dataMiningVariableIcons = "";
        if (JSON.parse(isMeddra)) {
            dataMiningVariableIcons = dataMiningVariableIcons + searchEventIcon;
        }
        if (!JSON.parse(isOob)) {
            var copyPasteModal = '<div class="modal fade copyAndPasteModal dmvCopyAndPasteModal" id="copyAndPasteModal" data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="Copy/Paste Dialog">\n' +
                '    <div class="modal-dialog" role="document">\n' +
                '        <div class="modal-content">\n' +
                '            <div class="modal-header">\n' +
                '                <button type="button" class="close" onclick="closeAllCopyPasteModals();" aria-label="Close"><span aria-hidden="true">×</span></button>\n' +
                '                <h4 class="modal-title" id="myModalLabel">Paste/Import Values:</h4>\n' +
                '            </div>\n' +
                '            <div class="modal-body container-fluid copy-paste-modal-height-fix">\n' +
                '                <div class="row">\n' +
                '                    <label>Delimiters:</label>\n' +
                '                </div>\n' +
                '                <div class="row" id="delimiter-options">\n' +
                '                    <div class="icon-col" title="No delimiters">\n' +
                '                        <label class="no-bold add-cursor">\n' +
                '                            <input type="radio" name="delimiter" value="none" checked="checked">\n' +
                '                            None\n' +
                '                        </label>\n' +
                '                    </div>\n' +
                '                    <div class="icon-col" title="comma">\n' +
                '                        <label class="no-bold add-cursor">\n' +
                '                            <input type="radio" name="delimiter" value=",">\n' +
                '                            Comma\n' +
                '                        </label>\n' +
                '                    </div>\n' +
                '                    <div class="icon-col" title="semi-colon">\n' +
                '                        <label class="no-bold add-cursor">\n' +
                '                            <input type="radio" name="delimiter" value=";">\n' +
                '                            Semi-Colon\n' +
                '                        </label>\n' +
                '                    </div>\n' +
                '                    <div class="icon-col" title="space">\n' +
                '                        <label class="no-bold add-cursor">\n' +
                '                            <input type="radio" name="delimiter" value=" ">\n' +
                '                            Space\n' +
                '                        </label>\n' +
                '                    </div>\n' +
                '                    <div class="icon-col" title="new-line">\n' +
                '                        <label class="no-bold add-cursor">\n' +
                '                            <input type="radio" name="delimiter" value="\\n">\n' +
                '                            New-Line\n' +
                '                        </label>\n' +
                '                    </div>\n' +
                '                    <div class="icon-col" title="Others">\n' +
                '                        <label class="no-bold add-cursor">\n' +
                '                            <input type="radio" name="delimiter" value="others">\n' +
                '                            Others\n' +
                '                        </label>\n' +
                '                    </div>\n' +
                '                    <div class="icon-col">\n' +
                '                        <input type="text" class="c_n_p_other_delimiter">\n' +
                '                    </div>\n' +
                '                </div>\n' +
                '                <div class="row content-row">\n' +
                '                    <textarea class="copyPasteContent" maxlength="8000"></textarea>\n' +
                '                <div class="countBox"></div></div>\n' +
                '                <div class="row" style="text-align: right; width: 100%;padding-top: 5px;margin-top:2px">\n' +
                '                    <button type="button" class="btn btn-default validate-copy-paste">Validate Values</button>\n' +
                '                    <hr>\n' +
                '                </div>\n' +
                '\n' +
                '                <div class="row">\n' +
                '                    <span>Import values from a file</span>\n' +
                '                </div>\n' +
                '\n' +
                '                <div id="importValueSection" class="importValueSection">\n' +
                '                    <div class="row">\n' +
                '                        <div class="input-group col-xs-10">\n' +
                '                            <input type="text" class="form-control file_input" readonly="">\n' +
                '                            <label class="input-group-btn">\n' +
                '                                <span class="btn btn-primary">\n' +
                '                                    Choose File… <input type="file" id="file_input" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel" style="display: none;">\n' +
                '                                </span>\n' +
                '                            </label>\n' +
                '                        </div>\n' +
                '                    </div>\n' +
                '\n' +
                '                    <div id="fileFormatError" hidden="hidden">\n' +
                '                        <div class="row">\n' +
                '                            <div class="col-xs-12" style="color: #ff0000">\n' +
                '                                Invalid file selected, valid files are of "xls, xlsx" types!\n' +
                '                            </div>\n' +
                '                        </div>\n' +
                '                    </div>\n' +
                '                    <div id="noDataInExcel" hidden="hidden" style="color: #ff0000"></div>\n' +
                '\n' +
                '                    <div class="row">\n' +
                '                        <div class="col-xs-11 bs-callout bs-callout-info">\n' +
                '                            <h5>Note</h5>\n' +
                '\n' +
                '                            <div>Import values from a file:</div>\n' +
                '\n' +
                '                            <div>-    Values will only be imported from the first worksheet of excel file</div>\n' +
                '\n' +
                '                            <div>-    Values will be imported from first column of excel file</div>\n' +
                '\n' +
                '                            <div>-    Each value must be in a separate row of the excel file</div>\n' +
                '\n' +
                '                            <div>-    First cell of excel file is the field label</div>\n' +
                '                        </div>\n' +
                '                    </div>\n' +
                '                </div>\n' +
                '            </div>\n' +
                '\n' +
                '            <div class="modal-footer">\n' +
                '                <button type="button" class="btn btn-default cancel" onclick="closeAllCopyPasteModals(false);">Cancel</button>\n' +
                '                <button type="button" class="btn btn-success confirm-paste" onclick="closeAllCopyPasteModals();">Confirm</button>\n' +
                '                <button type="button" class="btn btn-success import-values" style="display: none">Import</button>\n' +
                '            </div>\n' +
                '        </div>\n' +
                '    </div>\n' +
                '</div>';

            dataMiningVariableIcons = dataMiningVariableIcons + copyPasteModal + '&nbsp;<i tabindex="0" class="fa fa-pencil-square-o dmvPencil copy-paste-pencil copy-n-paste advance-filter-pencil dmv" style="margin-right: 2px !important;" id="advance-filter-pencil" data-toggle="modal"></i><div id="importValuesTemplateContainer"></div>';
        }
        dataMiningVariableIcons = '<div class="expressionsNoPad" id="dmvIcon">' + dataMiningVariableIcons + '<div>';
        $("#dataMiningVariableString").html(operatorDiv + dataMiningVariableValueDiv + dataMiningVariableIcons + '</div>');
        $(".dmvEvent").on('click', function () {
            $("#dmvOpened").val(true);
        })
        $('.dmvEvent').on('show.bs.modal', function () {
            $("#dmvOpened").val(true);
        });
        $('#eventModal').on('hidden.bs.modal', function () {
            $("#dmvOpened").val(false);
        });
        if (typeof dataMiningVariableValue.operator === "undefined" || dataMiningVariableValue.operator=="") {
            $("#selectOperator1").val(OPERATOR_VALUE.EQUALS);
        } else {
            $("#selectOperator1").val(dataMiningVariableValue.operator);
        }
        var dmvJson = {
            operator: "",
            operatorDisplay: "",
            value: "",
            delimiter: DEFAULT_DELIMITER
        }
        $("#selectOperator1").on('change', function () {
            dmvSelectedData = [];
            data = [];
            tempArr = [];
            setSelect2(this.value);
            dmvJson.operator = this.value;
            dmvJson.operatorDisplay= $("#selectOperator1 option:selected").text();
            dmvJson.value = $("#dataMiningVariableValue").val() != "" ? JSON.parse($("#dataMiningVariableValue").val()).value : "";
            $("#dataMiningVariableValue").val(JSON.stringify(dmvJson));
            init($("#dataMiningVariable").val());
        })
        $("#selectValue1").val(dataMiningVariableValue.value);

        $("#selectValue1").on('select2:select', function (e) {
            var data = e.params.data;
            dmvJson.operator = $("#selectOperator1").val();
            dmvJson.operatorDisplay= $("#selectOperator1 option:selected").text();
            dmvJson.value = dmvJson.value == "" ? data.text : dmvJson.value + dmvJson.delimiter + data.text;
            $("#inputValue").val(dmvJson.value);
            $("#dataMiningVariableValue").val(JSON.stringify(dmvJson));
            dmvSelectedData.push(data.text);
            updateDmvVariable();
        });


        $("#selectValue1").on('select2:unselect', function (e) {
            var data = e.params.data;
            dmvJson.operator = $("#selectOperator1").val();
            dmvJson.operatorDisplay= $("#selectOperator1 option:selected").text();
            if (dmvJson.value.split(dmvJson.delimiter).length > 1) {
                dmvJson.value = dmvJson.value.replaceAll(dmvJson.delimiter + data.text, "");
            } else {
                dmvJson.value = dmvJson.value.replaceAll(data.text, "");
            }
            $("#inputValue").val(dmvJson.value);
            $("#dataMiningVariableValue").val(JSON.stringify(dmvJson));
            dmvSelectedData.pop(data.text);
            updateDmvVariable();
        })
        $("#selectOperator1").on('change', function () {
            if (this.value == OPERATOR.IS_EMPTY || this.value == OPERATOR.IS_NOT_EMPTY) {
                $("#dataMiningVariableValueDiv, #dmvIcon").hide();
            } else {
                $("#dataMiningVariableValueDiv, #dmvIcon").show();
            }
        })
        modalOpener();
    } else {
        $("#dataMiningVariableString").empty();
    }
}

var getMiningVariableData=function (dataMiningVariable) {
    var selectedDb=$("#selectedDatasource").val();
    if(selectedDb.length>1){
        selectedDb=DATASOURCE_PVA;
    }else{
        selectedDb=selectedDb[0];
    }
    var dmvDataTemp;
    $.ajax({
        type: "GET",
        url: getDmvData + '?useCase=' + dataMiningVariable + '&dataSource=' + selectedDb,
        async: false,
        dataType: 'json'
    }).done(function (data) {
            dmvDataTemp=data.data;
    });
    return dmvDataTemp;
}

var setSelect2Values = function () {
    if (dataMiningVariableValue !== "" && typeof dataMiningVariableValue!=="undefined") {
        var dmvValues = dataMiningVariableValue.value.toString().split(dataMiningVariableValue.delimiter);
        if (!JSON.parse(dataLoaded)) {
            for (var i = 0; i < dmvValues.length; i++) {
                if (dmvValues[i] !== "") {
                    tempArr.push(dmvValues[i]);
                    data.push({
                        id: dmvValues[i], text: dmvValues[i], selected: true
                    })
                }
            }
        }
        dataLoaded = true;
    }


    var selectedDb=$("#selectedDatasource").val();
    if(selectedDb.length>1){
        selectedDb=DATASOURCE_PVA;
    }else{
        selectedDb=selectedDb[0];
    }

    $("#selectValue1").select2({
        separator: ";",
        width: "100%",
        minimumInputLength: 0,
        multiple: true,
        closeOnSelect: true,
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: getDmvData+'?useCase='+dataMiningVariable+'&dataSource='+selectedDb,
            data: function (params) {
                return {
                    term: params.term || "",  //search term
                    page: params.page || 1,  //page number
                    max: 30
                };
            },
            processResults: function (data, params) {
                console.log(data.list)
                console.log(data.totalCount)
                params.page = params.page || 1;
                return {
                    results: data.list,
                    pagination: {
                        more:  (params.page * 30) < data.totalCount
                    }
                };
            }
        },
    });

    for (var i = 0; i < tempArr.length; i++) {
        var option = new Option(tempArr[i], tempArr[i], true, true);
        $('#selectValue1').append(option).trigger('change');
    }

}
