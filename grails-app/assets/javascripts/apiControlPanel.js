$(function () {
    var selectedEndPoints = selectedEndPoints;
    toggleEtlPvrConnectivity();
    toggleSignalCharts();
    toggleDisplayDueIn();
    toggleEndOfMilestone();
    updateDueInEndpoints();
    updateDateClosedBasedOnDisp();
    updateDateClosedBasedOnWorkflow();
    togglePGUpdate();
    generateToken();
    runMigrations();
    runPvaMigrations();
    getETLRunStatus();
    runEtl();
	clickRunETLButton();
    caseNarrativeExport();
    $("#dueInEndpointSelect").on('change', function() {
        $("#saveDueInEndpoint").css("display", "initial");
    })
    $("#dueInEndpointSelect").select2({
        multiple: true,
        placeholder: 'Select Values',
        allowClear: true
    });
    $("#dateClosedBasedOnDisp").on('change', function() {
        $("#saveDateClosedDisp").css("display", "initial");
    });
    $("#dateClosedBasedOnDisp").select2({
        multiple: true,
        placeholder: 'Select Values',
        allowClear: true
    });
    if (isDisposition){
        $('input[name=option][value=signalDisposition]').prop('checked', true);
    } else{
        $('input[name=option][value=signalWorkflow]').prop('checked', true);
    }
    $("#dateClosedBasedOnDispWorkflow").select2({
        multiple: true,
        placeholder: 'Select Values',
        allowClear: true
    });
    $("#dateClosedBasedOnDispWorkflow").on('change', function() {
        $("#saveDateClosedDispWorkflow").css("display", "initial");
    });
    initiateEndPointAndMilestoneCheck();
    initiateDateClosedBasedOnDisp();
    initiateDateClosedBasedOnWorkflow();
    initiateDispositionEndPoints();
    saveDispositionEndpoints();
    $("#newEndofReviewConfig").on('click',function () {
        $('#addToRowMapping').toggle();
        $('#addToRowMapping .signalDispositionSelect').prop("disabled", false);
        $('#addToRowMapping .signalStatus').prop("disabled", false);
        $('#addToRowMapping .select2-selection__clear').prop("disabled", false).css("cursor", ""); // Remove cursor style
        $('#addToRowMapping .select2-selection__rendered').css("cursor", ""); // Remove cursor style
    })
    getValuesOnRadioButtonChange();

    $('#excelFile').on('change', function () {
        if ($(this).val()) {
            if ($(this)[0].files[0].size > maxUploadLimit) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('fileUploadMaxSizeExceedError', maxUploadLimit/1048576), {autoHideDelay: 5000});
                $(this).val('');
                $('input:submit').attr('disabled', true);
            } else {
                $('input:submit').attr('disabled', false);
            }
        } else {
            $('input:submit').attr('disabled', true);
        }
    });

    $('#excelFileUpload').on('submit', function () {
        $('input:submit', this).attr('disabled', true);
        return true;
    });

});
    var isApiControlPanelETLButtonClicked = false;
    var getETLRunStatus = function() {
    $("#refreshETLStatus").on('click', function () {
        getETLRunStatusByAjx();
    });
    }
    var getETLRunStatusByAjx = function () {
        $('.reloaderBtn').addClass('glyphicon-refresh-animate');
        $.ajax({
            url: '/signal/apiControlPanel/getETLRunStatus',
            method: 'GET',
        })
            .done(function (data) {
                setTimeout(changeEtlStatus(data), 100 );
                console.log("all set !!!")
                $('.reloaderBtn').removeClass('glyphicon-refresh-animate');
            }).
            fail(function (err) {
                console.log(err);
                $('.reloaderBtn').removeClass('glyphicon-refresh-animate');
            }
        );
        return false;
    }
    var changeEtlStatus = function(data) {
        $('#etlStatusBtn').css('color', '#ffffff');
        if(data.lastEtlStatus == 'FAILED') {
            $('#etlStatusBtn').css('color', '#f3f3f3');
            $('#etlStatusBtn').css('background-color', '#ef5350');
            $("#etlStatusBtn").html('Failed');
        } else if(data.lastEtlStatus == 'RUNNING') {
            $('#etlStatusBtn').css('background-color', '#FDBB40');
            $("#etlStatusBtn").html('Running');
            $('#runApiEtlScheduleNowHref').attr("disabled","disabled");
        } else if(data.lastEtlStatus == 'COMPLETED_BUT_FAILED') {
            $('#etlStatusBtn').css('background-color', '#00BA9E');
            $("#etlStatusBtn").html('Success');
        } else if(data.lastEtlStatus == 'SUCCESS') {
            $('#etlStatusBtn').css('background-color', '#00BA9E');
            $("#etlStatusBtn").html('Success');
        }
        if(isApiControlPanelETLButtonClicked == true && data.lastEtlStatus != 'RUNNING') {
            $('#runApiEtlScheduleNow').modal('show')
        }
        isApiControlPanelETLButtonClicked = false;
        $("#lastEtlDate").html(data.lastEtlDate);
        if(data.lastEtlBatchIds.length <= 90) {
            $('#lastEtlBatchIds').html(data.lastEtlBatchIds);
        } else {
            $('#lastEtlBatchIds').html(data.lastEtlBatchIds.substring(0,90)+'...');
        }
        $('#lastEtlBatchIds').prop('title', data.lastEtlBatchIds);
    }
    var downloadLastETLBatchLot = function () {
        $.ajax({
            url: VALIDATED.lastETLBatchLotDownloadLink,
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            data: {},
        })
            .done(function (data) {
                var blob = new Blob([data], { type: "application/vnd.ms-excel" });
                var URL = window.URL || window.webkitURL;
                var downloadUrl = URL.createObjectURL(blob);
                var a = document.createElement('a')
                a.setAttribute('href', downloadUrl)
                a.setAttribute('download', 'Last_ETL_Batch_Lot.xlsx')
                a.trigger('click')
            },
        );
    }


    var toggleDisplayDueIn = function () {
        $("#toggleDisplayDueIn").on('change', function() {
            var data = {};
            data.currentStatus = $("#toggleDisplayDueIn").is(':checked');
            $.ajax({
                url: updateDisplayDueInCheckUrl,
                data: data,
            })
                .done(function (response) {
                    if(response.value){
                        $.Notification.notify('success', 'top right', "Success", "Due in display is enabled", {autoHideDelay: 5000});
                    } else{
                        $.Notification.notify('success', 'top right', "Success", "Due in display is disabled", {autoHideDelay: 5000});
                    }
                })
                .fail(function () {
                    $.Notification.notify('success', 'top right', "Failure", "Signal Assessment Charts is not configured, contact administrator", {autoHideDelay: 5000});
                }
            );
        });
    }


    var toggleEndOfMilestone = function () {
        $("#toggleEndOfMilestone").on('change', function() {
            var data = {};
            data.currentStatus = $("#toggleEndOfMilestone").is(':checked');
            $.ajax({
                url: enableEndOfMilestoneCheckUrl,
                data: data
            })
                .done(function (response) {
                    if(response.value){
                        $.Notification.notify('success', 'top right', "Success", "Auto population of end of review configuration is enabled", {autoHideDelay: 5000});
                        $('#dispositionEndOfReviewContainer').removeClass('hide').addClass('show');
                        $('#dueInEndpointSelect').val("").trigger('change');
                        $("#saveDueInEndpoint").css("display", "none");
                        $('#dueInEndpointSelect').prop("disabled", true);
                    } else{
                        $.Notification.notify('success', 'top right', "Success", "Auto population of end of review configuration is disabled", {autoHideDelay: 5000});
                        $('#dispositionEndOfReviewContainer').removeClass('show').addClass('hide');
                        $('#dueInEndpointSelect').prop("disabled", false);
                    }
                })
                .fail(function () {
                    $.Notification.notify('success', 'top right', "Failure", "Signal Assessment Charts is not configured, contact administrator", {autoHideDelay: 5000});
            });
        });
    }

    var initiateEndPointAndMilestoneCheck = function () {
        if(isEndOfMilestone){
            $('#dueInEndpointSelect').val("").trigger('change');
            $('#dueInEndpointSelect').prop("disabled", true);
        }else{
            $('#dispositionEndOfReviewContainer').css('display','none');
            if(isSignalWorkflowEnabled || selectedEndPoints!==""){
                $('#toggleEndOfMilestone').prop("disabled", true);
            }else if(selectedEndPoints === ""){
                $('#toggleEndOfMilestone').prop("disabled", false);
            }
            if(selectedEndPoints && selectedEndPoints!=="null"){
                var selectedEndPointArray = unescapeHTML(selectedEndPoints).split(',');
                // select these options
                $('#dueInEndpointSelect').val(selectedEndPointArray).trigger('change');
            }else{
                $('#dueInEndpointSelect').val("").trigger('change');
            }
        }
        $("#saveDueInEndpoint").css("display", "none");
    }
    // This function is used to converts `&nbsp;` to a standard space.
    var unescapeHTMLForDisposition = function (escapeHtml) {
        return escapeHtml != undefined ?
            escapeHtml.toString().replace(/&nbsp;/g," ") : ""
    }

    var initiateDateClosedBasedOnWorkflow = function () {
        if (isDisposition) {
            if (dateClosedWorkflowDisposition && dateClosedWorkflowDisposition != null) {
                var dateClosedDispositionArray = unescapeHTMLForDisposition(unescapeHTML(dateClosedWorkflowDisposition)).split(',');
                $('#dateClosedBasedOnDispWorkflow').val(dateClosedDispositionArray).trigger('change');
            } else {
                $('#dateClosedBasedOnDispWorkflow').val("").trigger('change');
            }
        } else {
            if (dateClosedWorkflow && dateClosedWorkflow != null) {
                var dateClosedWorkflowArray = unescapeHTMLForDisposition(unescapeHTML(dateClosedWorkflow)).split(',');
                $('#dateClosedBasedOnDispWorkflow').val(dateClosedWorkflowArray).trigger('change');
            } else {
                $('#dateClosedBasedOnDispWorkflow').val("").trigger('change');
            }
        }
        $("#saveDateClosedDispWorkflow").css("display", "none");
    };

    var initiateDateClosedBasedOnDisp = function () {
        if (dateClosedDisposition && dateClosedDisposition !== "null") {
            var dateClosedDispositionArray = unescapeHTMLForDisposition(unescapeHTML(dateClosedDisposition)).split(',');
            $('#dateClosedBasedOnDisp').val(dateClosedDispositionArray).trigger('change');
        } else {
            $('#dateClosedBasedOnDisp').val("").trigger('change');
        }
        $("#saveDateClosedDisp").css("display", "none");
    };

    var updateDueInEndpoints = function() {
        $("#saveDueInEndpoint").on('click', function () {
            var dueInEndpointSelection = $('#dueInEndpointSelect').val();
            var endPointString = "";
            if(dueInEndpointSelection){
                endPointString = dueInEndpointSelection.join(',');
            }
            var data = {};
            data.selectedEndPoints = endPointString
            $.ajax({
                url: updateSelectedEndpointsUrl,
                data: data
            })
                .done(function (response) {
                    if(response.value){
                        $.Notification.notify('success', 'top right', "Success", "Due in Endpoint configuration saved successfully", {autoHideDelay: 5000});
                        $('#toggleEndOfMilestone').prop("disabled", true);
                    } else{
                        $.Notification.notify('success', 'top right', "Success", "Selected Due in Endpoints Cleared", {autoHideDelay: 5000});
                        if(!isSignalWorkflowEnabled){
                            $('#toggleEndOfMilestone').prop("disabled", false);
                        }
                    }
                    selectedEndPoints = response.value;
                    $("#saveDueInEndpoint").css("display", "none");
                })
                .fail(function () {
                    $.Notification.notify('success', 'top right', "Failure", "Signal Assessment Charts is not configured, contact administrator", {autoHideDelay: 5000});
            });
        });
    }

    var updateDateClosedBasedOnDisp = function () {
        $("#saveDateClosedDisp").on('click', function () {
            var dateClosedDispositionSelect = $("#dateClosedBasedOnDisp").val();
            var data = {};
            if(dateClosedDispositionSelect){
                data.dateClosedDisposition = dateClosedDispositionSelect.join(',');
            }
            $.ajax({
                url:updateDateClosedBasedOnDispositionUrl,
                data: data,
            })
                .done(function (response) {
                    if(response.msg){
                        $.Notification.notify('warning', 'top right', "Warning", response.msg, {autoHideDelay: 5000});
                    } else{
                        $.Notification.notify('success', 'top right', "Success", "Date Closed Based on Disposition saved successfully", {autoHideDelay: 5000});
                        $("#saveDateClosedDisp").css("display", "none");
                        dateClosedDisposition = response.value
                    }
                })
                .fail(function () {
                    $.Notification.notify('error', 'top right', "Failure", "Unable to update Date Closed Based On Disposition", {autoHideDelay: 5000});
                })
        })
    };

    var updateDateClosedBasedOnWorkflow = function () {
        $("#saveDateClosedDispWorkflow").on('click', function () {
            var dateClosedWorkflowSelect = $("#dateClosedBasedOnDispWorkflow").val();
            var data = {};
            if(dateClosedWorkflowSelect){
                data.dateClosedWorkflow = dateClosedWorkflowSelect.join(',');
            }
            $.ajax({
                url: updateDateClosedBasedOnWorkflowUrl,
                data: data,
            })
                .done(function (response) {
                    $.Notification.notify('success', 'top right', "Success", "Date Closed Based on Disposition/Workflow saved successfully", {autoHideDelay: 5000});
                    if(isDisposition){
                        dateClosedWorkflowDisposition = response.value;
                    } else{
                        dateClosedWorkflow = response.value;
                    }
                    $("#saveDateClosedDispWorkflow").css("display", "none");
                })
                .fail(function () {
                    $.Notification.notify('error', 'top right', "Failure", "Date Closed Based on Workflow not able to update", {autoHideDelay: 5000});
                })
        })
    };

    var saveDispositionEndpoints = function() {
        $(".editDispositionEndpoints").on('click', function (){
            $(this).removeClass('showOk').addClass('noShow');
            $(this).parent().find('.saveDispositionEndpoints').removeClass('noShow').addClass('showOk');
            var $parentContainer = $(this).parent().parent();
            $parentContainer.find('.signalDispositionSelect').prop("disabled", false);
            $parentContainer.find('.select2-selection__clear').prop("disabled", false).css("cursor", ""); // Remove cursor style
            $parentContainer.find('.select2-selection__rendered').css("cursor", ""); // Remove cursor style
            $parentContainer.find('.signalStatus').prop("disabled", false);
        })
        $(".icons-class").on('mouseover', function (){
            $(".noShow").hide();
            $(".showOk").hide();
            $(this).find(".showOk").show();
        })
        $(".icons-class").on('mouseout', function (){
            $(".noShow").hide();
            $(".showOk").hide();
        })
        $(".saveDispositionEndpoints").on('click',function () {
            var dispositionIdList = $(this).parent().parent().find('.signalDispositionSelect').val();
            var signalStatus = $(this).parent().parent().find('.signalStatus').val();
            var previousDispositionIds = $(this).parent().parent().find('.signalDispositionSelect').attr("data-reload-value");
            var data = {};
            data.dispositionIdList = dispositionIdList;
            data.signalStatus = signalStatus;
            data.previousDispositionIds = previousDispositionIds;
            if(dispositionIdList==null){
                $.Notification.notify('warning', 'top right', "Warning", "Select Signal Disposition(s)", {autoHideDelay: 5000});
            }else if(signalStatus===""){
                $.Notification.notify('warning', 'top right', "Warning", "Select Signal Status", {autoHideDelay: 5000});
            }else{
                $.ajax({
                    url: updateDispositionEndPointsUrl,
                    data: data})
                    .done(function (response) {
                        if(response.message){
                            $.Notification.notify('warning', 'top right', "Warning", response.message, {autoHideDelay: 5000});
                        }
                        else{
                            if(response.status){
                                $('#dispositionEndPointContainer').html(response.data);
                                saveDispositionEndpoints();
                                $.Notification.notify('success', 'top right', "Success", "Record Saved Successfully", {autoHideDelay: 5000});
                            } else{
                                $.Notification.notify('success', 'top right', "Success", "Disposition Endpoints Cleared", {autoHideDelay: 5000});
                            }
                            $(".signalDispositionSelect").select2({
                                multiple: true,
                                placeholder: 'Select Values',
                                allowClear: true
                            });
                            $('.signalDispositionSelect').prop("disabled", true)
                            $('.select2-selection__clear').prop("disabled", true)

                            $('.signalStatus').prop("disabled", true);
                            $(".showOk").hide();
                        }
                    })
                    .fail(function () {
                        $.Notification.notify('success', 'top right', "Failure", "Signal Assessment Charts is not configured, contact administrator", {autoHideDelay: 5000});
                });
            }
        });
        $(".cancelEndPointUpdate").on('click',function () {
            var signalStatus = $(this).parent().parent().find('.signalStatus').attr("data-actual");
            var data = {};
            data.signalStatus = signalStatus
            if(signalStatus!=undefined){
                $.ajax({
                    url: deleteByEndPointUrl,
                    data: data
                })
                    .done(function (response) {
                        if(response.status){
                            $('#dispositionEndPointContainer').html(response.data);
                            saveDispositionEndpoints();
                            $.Notification.notify('success', 'top right', "Success", "Record deleted successfully", {autoHideDelay: 5000});
                        }else {
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                        }
                        $(".signalDispositionSelect").select2({
                            multiple: true,
                            placeholder: 'Select Values',
                            allowClear: true
                        });
                        $('.signalDispositionSelect').prop("disabled", true)
                        $('.select2-selection__clear').prop("disabled", true);
                        $('.signalStatus').prop("disabled", true);
                        $(".showOk").hide();
                });
            }else{
                $(".signalDispositionSelect").val('').trigger('change');
                $("#signalStatus").prop('selectedIndex',0);
                $('#addToRowMapping').toggle();
            }
        });
    }

    var initiateDispositionEndPoints = function(){
        $.ajax({
            url: dispositionEndPointTableUrl
        })
            .done(function (response) {
                if(response.status){
                    $('#dispositionEndPointContainer').html(response.data);
                    saveDispositionEndpoints();
                }else {
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                }
                $(".signalDispositionSelect").select2({
                    multiple: true,
                    placeholder: 'Select Values',
                    allowClear: true
                });
                $('.signalDispositionSelect').prop("disabled", true)
                $('.select2-selection__clear').prop("disabled", true);
                $('.signalStatus').prop("disabled", true);
                $(".showOk").hide();
        })
    }




    var toggleSignalCharts = function () {
        $("#toggleSignalCharts").on('change', function() {
            var data = {};
            data.currentStatus = $("#toggleSignalCharts").is(':checked');
            $.ajax({
                url: signalChartsUrl,
                data: data
            })
                .done(function (response) {
                    if(response.signalChartsStatus){
                        $.Notification.notify('success', 'top right', "Success", "Signal Assessment Charts is enabled", {autoHideDelay: 5000});
                    } else{
                        $.Notification.notify('success', 'top right', "Success", "Signal Assessment Charts is disabled", {autoHideDelay: 5000});
                    }
                })
                .fail(function () {
                    $.Notification.notify('success', 'top right', "Failure", "Signal Assessment Charts is not configured, contact administrator", {autoHideDelay: 5000});
            });
        });
    }
    var clickRunETLButton = function () {
	$("#runApiEtlScheduleNowHref").on('click', function () {
		if($('#runApiEtlScheduleNowHref').attr("disabled")!='disabled') {
		    isApiControlPanelETLButtonClicked = true;
		    getETLRunStatusByAjx();
		}
		return false;
	});

    }
    var runEtl = function () {
        $("#runApiEtl").on('click',function () {
            $('#runApiEtl').prop('disabled', true);
            $('#runApiEtlScheduleNowHref').attr("disabled","disabled");
            $('#runApiEtlScheduleNowHref').prop('href','javascript:return false');
            $('#runApiEtlScheduleNow').modal('hide');
            setTimeout($("#etlStatusBtn").html('Running'), 100);
            setTimeout($('#etlStatusBtn').css('background-color', '#FDBB40'), 100);
            $.ajax({
                url: "/signal/apiControlPanel/runApiETL"
            })
                .done(function (data) {
                    if(data.count > 0 ) {
                        $.Notification.notify('success', 'top right', "Success", "<g:message code= 'app.controlpanel.ETLHasBeenStarted' />" , {autoHideDelay: 5000});
                    }
                    getETLRunStatusByAjx()
                })
                .fail(function (data) {
                    getETLRunStatusByAjx()
            });
        });
    }
    var generateToken = function () {
        $('#token-gen-bt').on('click', function (evt) {
            $.ajax({
                url: '/signal/preference/generateAPIToken',
                method: 'GET'
            })
                .done(function (data) {
                    $('#api-token-field').val(data.token);
                })
                .fail(function (err) {
                    console.log(err);
            });
            return false;
        });
    }
    var runMigrations = function () {
        $('#run-migrations').on('click', function (evt) {
            $.ajax({
                url: '/signal/businessConfiguration/rulesMigrations',
                method: 'GET'
            })
                .done(function (data) {
                    if(data.status) {
                        $.Notification.notify('success', 'top right', "Success", "Business Rules Data Migrated Successfully", {autoHideDelay: 5000});
                    }else{
                        $.Notification.notify('error', 'top right', "Failure", "Business Rule Data Migration Failed, Check Export for more information", {autoHideDelay: 5000});
                    }
                    window.location.reload();
            });
            return false;
        });
    }

    var runPvaMigrations = function () {
        $('#run-pva-migrations').on('click', function (evt) {
            if($("#alertGroupMigration").val()){
                $("#run-pva-migrations").prop("disabled",true);
                var data = {};
                data['alertGroup'] = $("#alertGroupMigration").val()
                $.ajax({
                    url: '/signal/productAssignment/migrateProductViewAssignment',
                    method: 'POST',
                    data: data,
                    async: true,
                }).done(function (data) {
                    if (data.status) {
                        $.Notification.notify('success', 'top right', "Success", "Product View Assignments Migrated Successfully.", {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('error', 'top right', "Failure", "Product View Assignments Migration Failed, Please Contact Administrator.", {autoHideDelay: 5000});
                    }
                });
            } else {
                $.Notification.notify('error', 'top right', "Failure", "Default Alert Group Value is required for initiating Migration.", {autoHideDelay: 5000});
            }
            return false;
        });
    }
    var toggleEtlPvrConnectivity = function () {
        $("#toggleEtlPvrConnectivityCheck").on('change', function() {
            var data = {};
            data.currentStatus = $("#toggleEtlPvrConnectivityCheck").is(':checked');
            $.ajax({
                url: url,
                data: data
            })
                .done(function (response) {
                    if(response.isEnabledEtlPvrConnectivity){
                        $.Notification.notify('success', 'top right', "Success", "Alert Executions Pre-checks is enabled", {autoHideDelay: 5000});
                    } else{
                        $.Notification.notify('success', 'top right', "Success", "Alert Executions Pre-checks is disabled", {autoHideDelay: 5000});
                    }
                })
                .fail(function () {
                    $.Notification.notify('success', 'top right', "Failure", "ETL-PVR Connectivity is not configured, contact administrator", {autoHideDelay: 5000});
            });
        });
    }
    var togglePGUpdate = function () {
        $("#togglePGUpdateCheck").on('change', function () {
            var data = {};
            data.currentStatus = $("#togglePGUpdateCheck").is(':checked');
            $.ajax({
                url: pgUpdateUrl,
                data: data
            })
                .done(function (response) {
                    if (response.isEnabledPGUpdate) {
                        $.Notification.notify('success', 'top right', "Success", "Product Group update status is enabled", {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('success', 'top right', "Success", "Product Group update status is disabled", {autoHideDelay: 5000});
                    }
                })
                .fail(function () {
                    $.Notification.notify('error', 'top right', "Failure", "Product Group Update Connectivity is not configured, contact administrator", {autoHideDelay: 5000});
            });
        });
    }
    var getValuesOnRadioButtonChange = function () {
        $('input[type="radio"]').on('change', function() {
            var selectedValue = $(this).val();  // Get the selected radio button value

            // AJAX request to fetch the options based on the selected radio button
            $.ajax({
                url: getValuesforDropdownUrl,
                type: 'GET',
                data: {selectedOption: selectedValue},
                dataType: 'json'
            })
                .done(function(response) {
                    var options = response.data.list;
                    var selectElement = $('#dateClosedBasedOnDispWorkflow');

                    // Clear the existing options and add the new options
                    selectElement.empty();
                    isDisposition = response.data.isDisposition;
                    $.each(options, function(index, option) {
                        selectElement.append($('<option>', { value: option, text: option }));
                    });
                    initiateDateClosedBasedOnWorkflow();
                })
                .fail(function() {
                    $.Notification.notify('error', 'top right', "Failure", "Something unexpected occurred", {autoHideDelay: 5000});
            });
        });
    }

    var caseNarrativeExport = function() {
        $("#toggleExportAlways,#togglePromptUser").on("change", function(){
            if($("#toggleExportAlways").prop("checked") && $("#togglePromptUser").prop("checked")) {
                    $(this).prop("checked",false);
                    $.Notification.notify('warning', 'top right', "Warning", "Enabling more than one option concurrently is not allowed", {autoHideDelay: 5000});
                } else {
                    var data = {};
                    var changed = ($(this).attr("name") == "exportAlways") ? "Export Always" : "Prompt User";
                    data.currentStatus = $(this).is(':checked');
                    $.ajax({
                        url: (changed == "Export Always") ? updateExportAlways : updatePromptUser,
                        data: data
                    })
                        .done(function (response) {
                            if(response.code ===200){
                                if(response.data){
                                    $.Notification.notify('success', 'top right', "Success", ` ${changed} option is enabled for Case Narrative Export`, {autoHideDelay: 5000});
                                } else{
                                    $.Notification.notify('success', 'top right', "Success", `${changed} option is disabled for Case Narrative Export`, {autoHideDelay: 5000});
                                }
                            } else if(response.code === 500){
                                console.log(changed);
                                (changed === "Export Always")?($("#toggleExportAlways").prop("checked", false)):($("#togglePromptUser").prop("checked", false));
                                $.Notification.notify('error', 'top right', "Error!!!", "Please refresh the screen, Configuration was updated by some other user", {autoHideDelay: 5000});
                            }
                        })
                        .fail(function () {
                            $.Notification.notify('error', 'top right', "Failure", "ERROR !!!", {autoHideDelay: 5000});
                    });
                }
        });
    }


