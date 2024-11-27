var dateRangeFrom = null;
var dateRangeTo = null;
var oldAssignedToVal;
var selectedAlertId;
var selectedConfId;
var  configId;
var dataSheetParams = {}
var INVALID_DATE = 'Invalid date';
$(function () {

    function handleInvalidDate(element) {
        $(element).val(newSetDefaultDisplayDateFormat($(element).val()));
        if ($(element).val() == INVALID_DATE) {
            $(element).val('');
        } else {
            element.value = moment($(element).val(), DATE_TEMPLATES).utc($(element).val()).format(DEFAULT_DATE_DISPLAY_FORMAT);
        }
    }

    $("#dateRangeStart").on('focusout', function() {
        handleInvalidDate(this);
    });

    $("#dateRangeEnd").on('focusout', function() {
        handleInvalidDate(this);
    });

    $(document).on("click", ".alertName", function () {
        $('.popupBox').hide();
        var oldVal = unescapeHTML($(this).html());
        var id = $(this).attr("data-id");
        var $this = $(this);
        var $textEditDiv = $("#alertNameEdit");
        if($(this).attr("data-editable") =="true"){
            showEditDiv($(this), $textEditDiv, $textEditDiv.find('.newVal'));
            $textEditDiv.find('.newVal').val(oldVal).focus();
            $textEditDiv.find(".saveButton").off().on('click', function (e) {
                var newVal = $textEditDiv.find('.newVal').val();
                if ((newVal !== oldVal) && newVal && newVal.trim().length > 0) {
                    ajaxCall(editAlertNameUrl, {alertName: newVal, id: id},
                        function (response) {
                            if (response.status) {
                                table.ajax.reload();
                                $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                            } else {
                                $this.html(oldVal);
                                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                            }
                        },
                        function (err) {
                            $this.html(oldVal);
                        });
                } else
                    $this.html(oldVal);
                $(".popupBox").hide();
            });
        }else{
            $.Notification.notify('warning', 'top right', "Warning", "Permission Denied! Only Owner or Admin can Edit Alert.", {autoHideDelay: 5000});
        }
    });

    $(document).on("change", ".dateRangeEnumClass", function () {
        var dateRangeEnum = $(this).val();
        var alertId = $('#dateRangeEnum').attr("data-alertId")
        if (dateRangeEnum in X_OPERATOR_ENUMS) {
            $('.relativeDateRangeValue').show();
            $('#datePickerFromDiv').hide();
            $('#datePickerToDiv').hide();
        } else if (dateRangeEnum == 'CUSTOM') {
            $('.relativeDateRangeValue').hide();
            $('#datePickerFromDiv').show();
            $('#datePickerToDiv').show();
            dateRangeFrom = null
            dateRangeTo = null
            setFromAndToDate();
        } else {
            $('.relativeDateRangeValue').hide();
            $('#datePickerFromDiv').hide();
            $('#datePickerToDiv').hide();
        }
    });
    $(document).on("click", ".dateRangeType", function () {
        $('.popupBox').hide();
        var allowEdit =$(this).closest('tr').find('.alertName').attr('data-editable');
        if(allowEdit == "true"){
            $('#evaluateDateAsNonSubmission').prop("disabled",false)
            if ($("#evaluateDateAsNonSubmission option[value='VERSION_ASOF']").length == 0) {
                $("#evaluateDateAsNonSubmission").append('<option value="VERSION_ASOF">' + $.i18n._('versionAsOf') + '</option>');
            }
            var id = $(this).attr("data-id");
            $('#dateRangeEnum').attr("data-alertId", id);
            var $this = $(this);
            var dateRangeEnum = $(this).attr("data-dateRangeEnum");
            var X = $(this).attr("data-relativeDateRangeValue");
            var dateRangeStartAbsolute = $(this).attr("data-dateRangeStartAbsolute");
            var dateRangeEndAbsolute = $(this).attr("data-dateRangeEndAbsolute");

            $('.dateRangeEnumClass').val(dateRangeEnum);
            $('.relativeDateRangeValue').val(X);
            var $DateRangeEditDiv = $("#dateRangeEdit");
            if (dateRangeEnum in (X_OPERATOR_ENUMS)) {
                $('.relativeDateRangeValue').show();
                $('#datePickerFromDiv').hide();
                $('#datePickerToDiv').hide();
            } else if (dateRangeEnum == 'CUSTOM') {
                $('.relativeDateRangeValue').hide();
                $('#datePickerFromDiv').show();
                $('#datePickerToDiv').show();
                dateRangeFrom = setDefaultDisplayDateFormat(dateRangeStartAbsolute)
                dateRangeTo = setDefaultDisplayDateFormat(dateRangeEndAbsolute)
                setFromAndToDate();
            } else {
                $('.relativeDateRangeValue').hide();
                $('#datePickerFromDiv').hide();
                $('#datePickerToDiv').hide();
            }
            var columnElement =$(this).closest('td');
            var dateAsOf =columnElement.find('.EvaluateDateAsClass').attr("data-evaluatedateas");
            if(dateAsOf == "VERSION_ASOF"){
                var date=columnElement.find('.EvaluateDateAsClass').attr("data-asofversiondate");
                $('#asOfVersionDateValue').val(moment(date).utc(date).format("MM/DD/YYYY"));
                $(".asOfVersionDatePicker").val(setDefaultDisplayDateFormat(date));
                $('.asOfVersionDatePicker').show();
            }else {
                $(".asOfVersionDatePicker").val("");
                $('#asOfVersionDateValue').val("");
                $('.asOfVersionDatePicker').hide();
            }
            initializeAsOfDate();
            $('#evaluateDateAsNonSubmission').val(dateAsOf);
            $('#evaluateDateAs').val(dateAsOf);
            showEditDiv($(this), $DateRangeEditDiv, null);
            if ($(this).attr('data-selectedDataSource') && ($(this).attr('data-selectedDataSource').includes("vigibase") || $(this).attr('data-selectedDataSource').includes("vaers") || $(this).attr('data-selectedDataSource').includes("faers") ) && !$(this).attr('data-selectedDataSource').includes("pva")) {
                $('#evaluateDateAsNonSubmission').prop("disabled", true)
                $("#evaluateDateAsNonSubmission option[value='VERSION_ASOF']").remove();
            }
        }else{
            $.Notification.notify('warning', 'top right', "Warning", "Permission Denied! Only Owner or Admin can Edit Alert.", {autoHideDelay: 5000});
        }

    });
    $(document).on("click", ".saveButtonDateRange", function () {
        var Id = $('#dateRangeEnum').attr("data-alertId")
        var dateRange = $('.dateRangeEnumClass').val()
        var fromDate = dateRangeFrom
        var toDate = dateRangeTo
        var xRelativeDate = $('.relativeDateRangeValue').val()
        var asOfVersionDate
        var evaluateDateAs=$('#evaluateDateAsNonSubmission').val();
        if(evaluateDateAs != 'LATEST_VERSION'){
            asOfVersionDate= $('#asOfVersionDateValue').val();

        }

        if (dateRange in (X_OPERATOR_ENUMS)) {
            fromDate = null
            toDate = null
        } else if (dateRange == 'CUSTOM') {
            fromDate = $('#dateRangeStart').val();
            toDate = $('#dateRangeEnd').val();
            xRelativeDate = 1
        } else {
            fromDate = null
            toDate = null
            xRelativeDate = 1
        }
        updateAlertDateRange(Id, dateRange, fromDate, toDate, xRelativeDate,evaluateDateAs,asOfVersionDate)
    });

    function updateAlertDateRange(id, dateRange, startDate, endDate, relativeDate,evaluateDateAs,asOfVersionDate) {
        ajaxCall(updateDateRangeUrl, {
                "id": id,
                "dateRangeEnum": dateRange,
                "dateRangeStartAbsolute": startDate,
                "dateRangeEndAbsolute": endDate,
                "relativeDateRangeValue": relativeDate,
                "evaluateDateAs": evaluateDateAs,
                "asOfVersionDate": asOfVersionDate
            },
            function (response) {
                if (response.status) {
                    table.ajax.reload();
                    $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                } else {

                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                }
            },
            function (err) {

                $.Notification.notify('error', 'top right', "Error", 'Something went wrong!', {autoHideDelay: 5000});
            });
        $(".popupBox").hide();
    }

    function setFromAndToDate() {
        $('#datePickerFromDiv').datepicker({
            allowPastDates: true,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        }).on('click', function (evt) {
            dateRangeFrom = setDefaultDisplayDateFormat($('#datePickerFromDiv').datepicker('getDate'));
        });

        $('#datePickerToDiv').datepicker({
            allowPastDates: true,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        }).on('click', function (evt) {
            dateRangeTo = setDefaultDisplayDateFormat($('#datePickerToDiv').datepicker('getDate'));

        });
        $('#dateRangeStart').val(dateRangeFrom);
        $('#dateRangeEnd').val(dateRangeTo);
        if (dateRangeFrom != null) {
            $('#datePickerFromDiv').datepicker('setDate', dateRangeFrom);
        }
        if (dateRangeTo != null) {
            $('#datePickerToDiv').datepicker('setDate', dateRangeTo);
        }
    }

    function showEditDiv(parent, div, enterField) {
        var position = parent.offset();
        div.css("left", position.left -100);
        div.css("top", position.top);
        div.show();
        if (enterField) {
            enterField.on("keydown", function (evt) {
                evt = evt || window.event;
                if (evt.keyCode == 13) {//27 is the code for Enter
                    div.find(".saveButton").trigger('click');
                }
            });
        }
    }

    function ajaxCall(url, data, success, error) {
        $.ajax({
            type: "GET",
            url: url,
            data: data
        })
            .done(function (result) {
                success(result);
            })
            .fail(function (err) {
                $.Notification.notify('error', 'top right', "Error", "An Error occurred.", {autoHideDelay: 5000});
                window.scrollTo(0, 0);
        });
    }


    function setDefaultDisplayDateFormat(date) {
        return moment(date).utc(date).format("DD-MMM-YYYY");
    }

    $('.cancelButton').on('click', function () {
        $(".popupBox").hide();
        $('.assignedToSelect').empty();
    });
    $(document).on("click", ".assignedToClass", function () {
        $('.popupBox').hide();
        var allowEdit =$(this).closest('tr').find('.alertName').attr('data-editable');
        if(allowEdit == "true"){
            $('.assignedToSelect').empty();
            $('.assignToProcessing').show();
            if ($(this).attr('data-selectedDataSource')) {
                let productAssignment = $(this).attr('data-productSelection');
                let productGroupAssignment = $(this).attr('data-productGroupSelection') !== "[]" && $(this).attr('data-productGroupSelection') != "null" ? $(this).attr('data-productGroupSelection') : "";
                let data = {};
                data["productAssignment"] = productAssignment;
                data["productGroupAssignment"] = productGroupAssignment;
                var $this = $(this)
                var levelNames = options.product.levelNames.split(',');
                var userAssignmentIndex = levelNames.findIndex(x => x === "User Assignment") + 1;
                if (productAssignment || productGroupAssignment) {
                    let changeValue = true;
                    if (productAssignment != "null" && productAssignment !== "") {
                        let productMap = JSON.parse(productAssignment);
                        if (productMap[userAssignmentIndex] && productMap[userAssignmentIndex].length) {
                            var dataNameId = productMap[userAssignmentIndex][0];
                            var selectorAssigned = $('.assignedToSelect');
                            var userOrGroupId;
                            if (userIdList.findIndex(x => x === dataNameId.id) === -1) {
                                userOrGroupId = "UserGroup_" + dataNameId.id
                            } else {
                                userOrGroupId = "User_" + dataNameId.id
                            }
                            var option1 = new Option(dataNameId.name, userOrGroupId, false, false);
                            selectorAssigned.empty();
                            selectorAssigned.append(option1).trigger('change.select2');
                            changeValue = false;
                        }
                    }
                    $.ajax({
                        url: fetchAssignmentForProductsUrl,
                        type: "POST",
                        data: data
                    })
                        .done(function (response) {
                            $('.assignToProcessing').hide();
                            let bindData = {"id": $this.attr("data-assignedtoid"), "name": $this.html()}
                            if (response.status === "success") {
                                $('.assignedToSelect').empty();
                                bindShareWith($('.assignedToSelect'), sharedWithListUrl, sharedWithValuesUrl, bindData, false, true);
                            } else if (response.status === "fail" && changeValue) {
                                $('.assignedToSelect').empty();
                                bindShareWith($('.assignedToSelect'), sharedWithListUrl, sharedWithValuesUrl, bindData, false);
                            }
                        })
                        .fail(function () {
                            $('.assignToProcessing').hide();
                            $.Notification.notify('error', 'top right', "Error", "Sorry could not fetch assignment for selected product.", {autoHideDelay: 5000});
                            bindShareWith($('.assignedToSelect'), sharedWithListUrl, sharedWithValuesUrl, "", false);
                    });
                }
            }
            selectedAlertId = $this.attr('id');
            oldAssignedToVal = $this.attr("data-assignedtoid");

            showEditDiv($(this), $('#assignedToEdit'), null);
        }else{
            $.Notification.notify('warning', 'top right', "Warning", "Permission Denied! Only Owner or Admin can Edit Alert.", {autoHideDelay: 5000});
        }

    });
        $("input[name='allSheets']").on('change', function () {
            $("#selectedDatasheets").val($("input[type='checkbox'][name='allSheets']:checked").val());
            $('#dataSheet').select2({placeholder: $.i18n._("selectOne"), allowClear: true});
            dataSheetParams["enabledSheet"] =$("#selectedDatasheets").val();
            bindDatasheet2WithData($("#dataSheet"), dataSheetList, dataSheetParams);
        });

    $("#allSheets").on('change', function(){
        if($(this).is(':checked')){
            $(this).val('ALL_SHEET')
        }else{
            $(this).val('CORE_SHEET')
        }
    });
    $(document).on("click", ".datasheets", function (){
        $('.popupBox').hide();
        var allowEdit =$(this).closest('tr').find('.alertName').attr('data-editable');
        if(allowEdit == "true") {
            configId = $(this).attr('data-id');
            let bindData = $(this).attr("data-selecteddatasheet");
            var datasheetSelect = $('.dataSheet');
            let changeValue = true;
            var enabledSheet = $(this).attr("data-sheetType");
            if(enabledSheet == "ALL_SHEET"){
                $('.datasheet-options').find($("input[id='allSheets']").prop('checked',true));
            }else{
                $('.datasheet-options').find($("input[id='allSheets']").prop('checked',false));
            }
                datasheetSelect.empty();
            if($("#allSheets").is(':checked')){
                $("#allSheets").val('ALL_SHEET')
            }else{
                $("#allSheets").val('CORE_SHEET')
            }
                dataSheetParams["enabledSheet"] = $("#allSheets").val();
                dataSheetParams["dataSource"] = $(this).attr("data-selectedDatasource")?$(this).attr("data-selectedDatasource"):"pva";
                dataSheetParams["isProductGroup"] = false
                dataSheetParams["products"] = $(this).closest('tr').find('#productSelectionAssessment').attr('value')
                if (dataSheetParams["products"] == null || dataSheetParams["products"] ==='' ) {
                    dataSheetParams["isProductGroup"] = true
                    dataSheetParams["products"] = $(this).closest('tr').find('#productGroupSelectionAssessment').attr('value')
                }
                $('#products').val(dataSheetParams["products"]);
                if(bindData){
                    $.each(JSON.parse(bindData),function (i,data){
                        datasheetSelect.append($("<option></option>").attr("value",data.id).attr("selected", "selected").text(data.text));
                    });
                }
                bindDatasheet2WithData($('.dataSheet'), dataSheetList, dataSheetParams);
                showEditDiv($(this), $('.datasheet-options'));
                showEditDiv($(this), $('#dataSheetEdit'));
        }else if(allowEdit){
            $.Notification.notify('warning', 'top right', "Warning", "Permission Denied! Only Owner or Admin can Edit Alert.", {autoHideDelay: 5000});
        }
    });

    $(document).on("click", ".shareWithCell", function () {
        $('.popupBox').hide();

        var allowEdit =$(this).closest('tr').find('.alertName').attr('data-editable');
        if(allowEdit == "true"){
            selectedConfId = $(this).find('.shareWithClass').attr('id');
            $('.shareWithSelect').empty();
            $('.shareWithProcessing').show();
            let bindData = $(this).find('.shareWithClass').attr("data-shareWithMap");
            var shareWithData = JSON.parse(bindData);

            if ($(this).find('.shareWithClass').attr('data-selectedDataSource')) {

                var prodGroupAssign = $(this).find('.shareWithClass').attr('data-productGroupSelection')
                let productAssignment = $(this).find('.shareWithClass').attr('data-productSelection');
                let productGroupAssignment = prodGroupAssign !== "[]" && prodGroupAssign != "null" ? prodGroupAssign : "";
                let data = {};
                data["productAssignment"] = productAssignment;
                data["productGroupAssignment"] = productGroupAssignment;
                var $this = $(this).find('.shareWithClass')
                var levelNames = options.product.levelNames.split(',');
                var userAssignmentIndex = levelNames.findIndex(x => x === "User Assignment") + 1;
                if (productAssignment || productGroupAssignment) {

                    let changeValue = true;
                    if (productAssignment != "null" && productAssignment !== "") {
                        let productMap = JSON.parse(productAssignment);
                        if (productMap[userAssignmentIndex] && productMap[userAssignmentIndex].length) {
                            var dataNameId = productMap[userAssignmentIndex][0];
                            var selectorShare = $('#sharedWith');
                            var userOrGroupId;
                            if (userIdList.findIndex(x => x === dataNameId.id) === -1) {
                                userOrGroupId = "UserGroup_" + dataNameId.id
                            } else {
                                userOrGroupId = "User_" + dataNameId.id
                            }
                            var option1 = new Option(dataNameId.name, userOrGroupId, false, false);
                            selectorShare.empty();
                            selectorShare.append(option1).trigger('change.select2');
                            changeValue = false;

                        }
                    }
                    $.ajax({
                        url: fetchAssignmentForProductsUrl,
                        type: "POST",
                        data: data
                    })
                        .done(function (response) {
                            $('.shareWithProcessing').hide();

                            var sharedData = ""
                            if (response.status === "success") {
                                $('.shareWithSelect').empty();
                                bindShareWith2WithData($('.shareWithSelect'), sharedWithUrl, shareWithData, true, true);
                            } else if (response.status === "fail" && changeValue) {
                                $('.shareWithSelect').empty();
                                bindShareWith2WithData($('.shareWithSelect'), sharedWithUrl, shareWithData, true);//sharedWithData
                            }
                        })
                        .fail(function () {
                            $('.shareWithProcessing').hide();
                            $.Notification.notify('error', 'top right', "Error", "Sorry could not fetch assignment for selected product.", {autoHideDelay: 5000});
                            $('.shareWithSelect').empty()
                            bindShareWith2WithData($('#sharedWith'), sharedWithUrl, shareWithData, true);
                    });
                }
            }
            selectedAlertId = $this.attr('id');
            showEditDiv($(this), $('#shareWithEdit'), null);
        }else if(allowEdit){
            $.Notification.notify('warning', 'top right', "Warning", "Permission Denied! Only Owner or Admin can Edit Alert.", {autoHideDelay: 5000});
        }

    });


    $(document).on("click", "td.schedulerCell", function (e) {
        $('.popupBox').hide();
        var $this = $(this);
        var allowEdit =$this.closest('tr').find('.alertName').attr('data-editable');
        if(allowEdit == "true"){
            var id = $this.find(".scheduler").attr("data-id");
            var oldVal = $this.find(".scheduler").attr("data-value");
            var oldLabel = $this.find(".scheduler").text();
            var $schedulerEditDiv = $("#schedulerEditDiv");
            showEditDiv($this, $schedulerEditDiv);
            $schedulerEditDiv.find('#myDatePicker').datepicker('setRestrictedDates', []);
            $schedulerEditDiv.find('.repeat-end').find('.end-on-date').datepicker('setRestrictedDates', []);


            var schedulerInfoJSON = parseServerJson(oldVal);
            if (schedulerInfoJSON?.recurrencePattern.indexOf("WEEKLY") < 0)
                highlightCurrentDayForWeeklyFrequency(userTimeZone);
            $schedulerEditDiv.scheduler('value', schedulerInfoJSON);
            $('#configSelectedTimeZone').val(schedulerInfoJSON?.timeZone.name);


            $schedulerEditDiv.find(".saveButtonScheduler").off().on('click', function (e) {
                var newVal = $schedulerEditDiv.find('#scheduleDateJSON').val();
                const date = new Date(new Date().toDateString());
                let day = date.getDate();
                let month = date.toLocaleString('default', {month: 'short'})
                let year = date.getFullYear();
                let currentDate = `${day}-${month}-${year}`;
                let startDateCheck = $schedulerEditDiv.find('#MyStartDate').val() < currentDate
                let endDateCheck
                if ($schedulerEditDiv.find('#MyEndDate').val()) {
                    const endDateFinal = new Date($schedulerEditDiv.find('#MyEndDate').val())
                    endDateCheck = endDateFinal < date
                }
                var repeatExecutionBoolean = false
                var repeatValue = $("#schedulerEditDiv").find('.repeat-options').find('.selected-label').html();
                if (repeatValue == "None (run once)") {
                    repeatExecutionBoolean = false
                } else {
                    repeatExecutionBoolean = true
                }
                if (startDateCheck === true || ($schedulerEditDiv.find('#MyEndDate').val() && endDateCheck === true)) {
                    e.preventDefault();
                    $.Notification.notify('error', 'top right', "Error", "The start/end date cannot be a past date.", {autoHideDelay: 5000});
                    return
                }
                ajaxCall(updateScheduleDateJSON_URL, {
                        scheduleDateJSON: newVal,
                        id: id,
                        repeatExecution: repeatExecutionBoolean
                    },
                    function (result) {
                        if (result.status) {
                            table.ajax.reload();
                            $.Notification.notify('success', 'top right', "Success", result.message, {autoHideDelay: 5000});
                        } else {
                            $.Notification.notify('error', 'top right', "Error", result.message, {autoHideDelay: 5000});
                        }
                    },
                    function (err) {
                        $.Notification.notify('error', 'top right', "Error", 'An Error Occurred!', {autoHideDelay: 5000});
                    });

                $schedulerEditDiv.hide();

            });
        }else{
            $.Notification.notify('warning', 'top right', "Warning", "Permission Denied! Only Owner or Admin can Edit Alert.", {autoHideDelay: 5000});
        }


    });


    function parseServerJson(scheduleInfo) {
        var scheduleInfoJson = JSON.parse(scheduleInfo);
        if (scheduleInfoJson?.timeZone) {
            if (scheduleInfoJson.timeZone.text) {
                delete scheduleInfoJson.timeZone["text"];
            }
            if (scheduleInfoJson.timeZone.selected != undefined) {
                delete scheduleInfoJson.timeZone["selected"];
            }
        }
        return scheduleInfoJson
    }

    var months= {'01':'Jan','02':'Feb', '03':'Mar', '04':'Apr','05':'May','06':'Jun','07':'Jul', '08':'Aug', '09':'Sep', '10':'Oct', '11':'Nov', '12':'Dec'};

    function highlightCurrentDayForWeeklyFrequency(timeZone) {

        var currentDayOfWeek = moment.tz(timeZone).day();
        switch (currentDayOfWeek) {
            case 0:
                $('#repeat-weekly-sun').checkbox('check').addClass("active");
                break;
            case 1:
                $('#repeat-weekly-mon').checkbox('check').addClass("active");
                break;
            case 2:
                $('#repeat-weekly-tue').checkbox('check').addClass("active");
                break;
            case 3:
                $('#repeat-weekly-wed').checkbox('check').addClass("active");
                break;
            case 4:
                $('#repeat-weekly-thu').checkbox('check').addClass("active");
                break;
            case 5:
                $('#repeat-weekly-fri').checkbox('check').addClass("active");
                break;
            case 6:
                $('#repeat-weekly-sat').checkbox('check').addClass("active");
        }
    }

    $(document).on("click", ".unscheduleAlert", function () {
        var id = $(this).attr('data-id');
        ajaxCall(unschedule_alert_url, {
                id: id,
                repeatExecution: "false",
                scheduleDateJSON: ''
            },
            function (response) {
                if (response.status) {
                    table.ajax.reload();
                    $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                } else {

                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                }
            },
            function (err) {

                $.Notification.notify('error', 'top right', "Error", 'Something went wrong!', {autoHideDelay: 5000});
            });

    });

    function formSchedulerCell(id, value, label) {
        if (label == 'null') {
            label = ''
        }
        return "<span class='scheduler' style='word-break: break-all;' data-id='" + id + "' data-value='" + value + "'>" + label + "</span>";
    }


    $(document).on("click", ".saveButtonAssignedTo", function (e) {

        var newVal = $('.assignedToSelect').val();
        if ((newVal !== oldAssignedToVal && newVal !== 'User_' + oldAssignedToVal && newVal !== 'UserGroup_' + oldAssignedToVal) && newVal && newVal.trim().length > 0) {
            ajaxCall(changeAssignedToGroup_Url, {alertId: selectedAlertId, assignedToValue: newVal},
                function (response) {
                    if (response.status) {
                        table.ajax.reload();
                        $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                    }
                },
                function (err) {
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                });
        }
        $(".popupBox").hide();
        $('.assignedToSelect').empty();
    });

    $(document).on("click", ".saveButtonShareWith", function (e) {

        var newVal = $('.shareWithSelect').val();

        ajaxCall(changeShareWith_Url, {id: selectedConfId, shareWithValue: JSON.stringify(newVal)},
            function (response) {
                if (response.status) {
                    table.ajax.reload();
                    $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                }
            },
            function (err) {
                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
            });


        $(".popupBox").hide();
    });
    $(document).on("click", ".saveButtonDatasheets", function (e) {
        var newVal = $('.dataSheet').val();
        var enabledSheet = $("#selectedDatasheets").val();
        ajaxCall(changeDatasheets_Url, {"id": configId, "datasheetValue":newVal?.join(',')?newVal?.join(','):[],"enabledSheet":enabledSheet},
            function (response) {
                if (response.status) {
                    table.ajax.reload();
                    $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                }
            },
            function (err) {
                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
            });

        $(".popupBox").hide();
    });


    // Browse file upload Start
    $(document).on('click', '#importConfigurationFileModal .file-uploader .browse', function () {
        var fileUploaderElement = $(this).closest('.file-uploader');
        var file = fileUploaderElement.find('.file');
        var fileName = fileUploaderElement.find('.form-control').val();
        file.trigger('click');
    });
    $(document).on('change', '#importConfigurationFileModal .file-uploader .file', function () {
        var currentElement = $(this);
        var inputBox = currentElement.parent('.file-uploader').find('.form-control');
        if(!_.isEmpty(currentElement.val())){
            inputBox.val(currentElement.val().match(/[^\\/]*$/)[0]);
        }
        inputBox.trigger('change');
    });

    $( '#importConfigurationFileUploadForm' ).on('submit', function( e ) {
        e.preventDefault();
        var formData = new FormData(this);

        var $this = $(this);
        if ($this.find('.file').val()) {
            $this.find(".upload").attr("disabled", true);
            $.ajax({
                url: uploadFileUrl,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false
            })
                .done(function (response) {
                        $this.find(".upload").attr("disabled", true);
                        $('#importConfigurationFileModal').modal('hide');
                    $('#importConfigurationFileUploadForm')[0].reset();
                        if (response.status) {
                            $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                        } else {
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                        }
                    })
                .fail(function () {
                        $this.find(".upload").attr("disabled", true);
                        $.Notification.notify('error', 'top right', "Error", "Sorry, This File Format Not Accepted", {autoHideDelay: 5000});
                    }
            );
        }
    });
    $(document).on('click','.delete', function () {
        var id = $(this).attr("data-id");
        bootbox.confirm({
            title: 'Delete Alert',
            message: "Are you sure you want to delete this Alert?",
            buttons: {
                confirm: {
                    label:'<span class="glyphicon glyphicon-trash icon-white"></span> Delete',
                    className: 'btn btn-danger'
                },
                cancel: {
                    label: 'Cancel',
                    className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    ajaxCall(delete_url, {id: id},
                        function (response) {
                            if (response.status) {
                                table.ajax.reload();
                                $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                            } else {
                                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                            }
                        },
                        function (err) {
                            $.Notification.notify('error', 'top right', "Error", "An Error Occurred!", {autoHideDelay: 5000});
                        });
                } else {
                    event.preventDefault();
                }
            }
        });

    });


    $(document).on('click','.showProductDictionary', function () {
        let dataSource = $(this).parent().find(".selectedDatasource").val()
        $("#dataSourcesProductDict").val(dataSource)
        $("#dataSourcesProductDictAssessment").val(String(dataSource)).trigger('change');
        $('#clickedDatasource').val(dataSource);
        $('.popupBox').hide();
        var allowEdit =$(this).closest('tr').find('.alertName').attr('data-editable');
        if(allowEdit == "true"){
            $(this).parent().find('#searchProductsAssessment').trigger("click");
        }else{
            $.Notification.notify('warning', 'top right', "Warning", "Permission Denied! Only Owner or Admin can Edit Alert.", {autoHideDelay: 5000});
        }
    });


    $(document).on('click', '.addAllProductsAssessment', function () {
        var alertId = currentEditingRow.find('.alertName').attr('data-id');
        var prodSelection = currentEditingRow.find('#productSelectionAssessment').val();
        var prodGroupSelection = currentEditingRow.find('#productGroupSelectionAssessment').val();

        ajaxCall(editProductSelect_url, {
                alertId: alertId,
                productSelection: prodSelection,
                productGroupSelection: prodGroupSelection
            },
            function (response) {
                if (response.status) {
                    table.ajax.reload();
                    if (response.data['isChanged'])
                        $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                } else {
                    if (response.message === "Product is required"){ //converted error message to warning message to sync up with PVS-47979
                        $.Notification.notify('warning', 'top right', "Warning", response.message, {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                    }
                }
            },
            function (err) {
                $.Notification.notify('error', 'top right', "Error", "An error occurred", {autoHideDelay: 5000});
            });

    });

    $("#importConfigurationFileModal").on('shown.bs.modal', function () {
        $('input:submit').attr('disabled', true);
    });

    $(document).on('click', '.cancelUploadModalButton', function () {
        $('#importConfigurationFileUploadForm')[0].reset();
    });

    DATE_FMT_TZ = "DD-MMM-YYYY";


    function initializeAsOfDate() { // updated manually entered functionality for bug/PVS-38925
        var asOf = null;
        var isFirstTimeUpdate = true;
        var asOfVersionElement = $('#asOfVersionDateValue');
        if (asOfVersionElement != null && asOfVersionElement.val() != INVALID_DATE) {
            asOf = setDefaultDisplayDateFormat(document.getElementById('asOfVersionDateValue').value);
        }

        $('.asOfVersionDatePicker').datepicker({
            allowPastDates: true,
            date: asOf
        }).on('changed.fu.datepicker', function (evt, date) {
            asOf = date;
            updateInputField();
        }).on('click', function (event) {
            if (isFirstTimeUpdate === false && !$(event.target).is('.asOfVersionDate')) {
                asOf = ($(".asOfVersionDatePicker").datepicker('getDate'));
            } else {
                if (asOf != INVALID_DATE && asOf != "") {
                    $(".asOfVersionDatePicker").datepicker('setDate', asOf);
                } else {
                    $(".asOfVersionDatePicker").datepicker('setDate', new Date());
                }
                isFirstTimeUpdate = false;
            }
            updateInputField();
        });

        var updateInputField = function () {
            if (asOf != INVALID_DATE && asOf != "") {
                $('input[name="asOfVersionDate"]').val(setDefaultDisplayDateFormat(asOf));
            } else {
                $('input[name="asOfVersionDate"]').val("");
                asOf = ""
            }

        };
        updateInputField();

        $(".asOfVersionDate").on('focusout', function () {
            $(this).val(newSetDefaultDisplayDateFormat($(this).val()));
            if ($(this).val() == INVALID_DATE) {
                $(this).val('');
            }

            document.getElementById('asOfVersionDateValue').value = moment($(this).val(), DATE_TEMPLATES).utc($(this).val()).format(DEFAULT_DATA_ANALYSIS_DATE_FORMAT);
        });
    }


    $(document).on('change', '#evaluateDateAsNonSubmission', function () {
        var selection = $(this).val();
        if (selection == "VERSION_ASOF") {
            $('.asOfVersionDatePicker').show();
        } else {
            $('.asOfVersionDatePicker').hide();
        }
    });
    function renderDateWithTimeZone(date) {
        return moment(date).tz(userTimeZone).format(DATE_FMT_TZ);
    }
    $('.datepicker-wheels-month ul li').children().on('click', function (e) {
        this.closest('ul').find( '.selected' ).removeClass( 'selected' );
        $( e.currentTarget ).parent().addClass( 'selected' );

    });
});
